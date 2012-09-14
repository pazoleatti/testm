package com.aplana.sbrf.taxaccounting.controller.formdata;

import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.dao.FormDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.PredefinedRowsDao;
import com.aplana.sbrf.taxaccounting.dao.dictionary.TransportTaxDao;
import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.Form;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.Script;

@Service
public class FormDataScriptingService {
	Log logger = LogFactory.getLog(getClass());
	
	@Autowired
	private FormDao formDao;
	
	@Autowired
	PredefinedRowsDao predefinedRowsDao;
	
	@Autowired
	private FormDataDao formDataDao;	
	
	@Autowired
	private TransportTaxDao transportTaxDao;
	
	public FormData createForm(Logger logger, int formId) {
		Form form = formDao.getForm(formId);
		FormData result = new FormData(form);
		List<DataRow> predefinedRows = predefinedRowsDao.getPredefinedRows(form);
		for (DataRow predefinedRow: predefinedRows) {
			DataRow dataRow = result.appendDataRow(predefinedRow.getAlias());
			// TODO: копирование данных по столбцам
		}
		
		Script createScript = form.getCreateScript();
		if (createScript != null && createScript.getBody() != null) {
			ScriptEngine engine = getScriptEngine();		
			engine.put("logger", logger);
			engine.put("formData", result);
			try {
				engine.eval(createScript.getBody());
			} catch (ScriptException e) {
				logger.error(e);
			}
		}
		return result;
	}

	public void processFormData(Logger logger, FormData formData) {
		Form form = formData.getForm();
		ScriptEngine engine = getScriptEngine();
		engine.put("logger", logger);
		List<Script> calcScripts = form.getCalcScripts();
		for (Script calcScript: calcScripts) {
			if (calcScript.isRowScript()) {
				performRowScript(calcScript, formData, engine, logger);
			} else {
				engine.put("formData", formData);
				try {
					engine.eval(calcScript.getBody());
				} catch (Exception e) {
					logger.error(e);
				}
			}
		}
		checkMandatoryColumns(formData, logger);
		logger.setMessageDecorator(null);
	}

	private void performRowScript(Script rowScript, FormData formData, ScriptEngine engine, Logger logger) {
		RowMessageDecorator messageDecorator = new RowMessageDecorator();
		messageDecorator.setOperationName(rowScript.getName());
		logger.setMessageDecorator(messageDecorator);
		int rowIndex = 0;
		for (DataRow row: formData.getDataRows()) {
			++rowIndex;
			messageDecorator.setRowIndex(rowIndex);
			engine.put("row", row);
			engine.put("rowIndex", rowIndex);
			engine.put("rowAlias", row.getAlias());
			try {
				engine.eval(rowScript.getBody());
			} catch (Exception e) {
				logger.error(e);
				break;
			}
		}
		logger.setMessageDecorator(null);
	}
	
	private void checkMandatoryColumns(FormData formData, Logger logger) {
		List<Column> columns = formData.getForm().getColumns();
		RowMessageDecorator messageDecorator = new RowMessageDecorator();
		messageDecorator.setOperationName("Проверка обязательных полей");
		int rowIndex = 0;
		for (DataRow row: formData.getDataRows()) {
			++rowIndex;
			messageDecorator.setRowIndex(rowIndex);
			List<String> columnNames = new ArrayList<String>();
			for (Column col: columns) {
				if (col.isMandatory() && row.get(col.getAlias()) == null) {
					columnNames.add(col.getName());
				}
			}
			if (!columnNames.isEmpty()) {
				logger.error("Не заполнены столбцы %s", columnNames.toString());
			}
		}
	}
	
	private ScriptEngine getScriptEngine() {
		ScriptEngineManager factory = new ScriptEngineManager();
		ScriptEngine engine = factory.getEngineByName("groovy");
		// TODO: продумать способ, для публикации DAO-объектов в скриптах
		// давать всё сразу как-то нехорошо
		engine.put("transportTaxDao", transportTaxDao);
		engine.put("formDataDao", formDataDao);		
		return engine;
	}
}
