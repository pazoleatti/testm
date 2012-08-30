package com.aplana.sbrf.taxaccounting.controller.formdata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.dao.dictionary.TransportOkatoDao;
import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.script.FormDataScriptingService;
import com.aplana.sbrf.taxaccounting.script.RowScript;

@Service
public class FormDataScriptingServiceImpl implements FormDataScriptingService {
	Log logger = LogFactory.getLog(getClass());
	private final static String ROOT = "C:/workspace/scripts/src/groovy";
	
	@Autowired
	private TransportOkatoDao transportOkatoDao;

	public void processFormData(Logger logger, FormData formData) {
		ScriptEngineManager factory = new ScriptEngineManager();
		ScriptEngine engine = factory.getEngineByName("groovy");		
		engine.put("logger", logger);
		// TODO: продумать способ, для публикации DAO-объектов в скриптах
		// давать всё сразу как-то нехорошо
		engine.put("transportOkatoDao", transportOkatoDao);
		RowMessageDecorator rowMessageDecorator = new RowMessageDecorator();
		logger.setMessageDecorator(rowMessageDecorator);
		List<RowScript> rowScripts = getFormRowScripts(formData.getForm().getId());
		for (RowScript rowScript: rowScripts) {
			performRowScript(rowScript, formData, engine, logger, rowMessageDecorator);
		}
		checkMandatoryColumns(formData, logger, rowMessageDecorator);
		logger.setMessageDecorator(null);
	}

	private void performRowScript(RowScript rowScript, FormData formData, ScriptEngine engine, Logger logger, RowMessageDecorator messageDecorator) {
		messageDecorator.setOperationName(rowScript.getName());
		int rowIndex = 0;
		for (DataRow row: formData.getDataRows()) {
			++rowIndex;
			messageDecorator.setRowIndex(rowIndex);
			engine.put("row", row.getData());
			engine.put("rowIndex", rowIndex);
			engine.put("rowAlias", row.getAlias());
			try {
				engine.eval(rowScript.getScript());
			} catch (Exception e) {
				logger.error(e);
				break;
			}
		}
	}
	
	private void checkMandatoryColumns(FormData formData, Logger logger, RowMessageDecorator messageDecorator) {
		List<Column> columns = formData.getForm().getColumns();
		messageDecorator.setOperationName("Проверка обязательных полей");
		int rowIndex = 0;
		for (DataRow row: formData.getDataRows()) {
			++rowIndex;
			messageDecorator.setRowIndex(rowIndex);
			List<String> columnNames = new ArrayList<String>();
			for (Column col: columns) {
				if (col.isMandatory() && row.getColumnValue(col.getAlias()) == null) {
					columnNames.add(col.getName());
				}
			}
			if (!columnNames.isEmpty()) {
				logger.error("Не заполнены столбцы %s", columnNames.toString());
			}
		}
	}

	@Override
	public List<RowScript> getFormRowScripts(int formId) {
		File dir = new File(ROOT + "/" + formId + "/rows");
		if (!dir.exists() || !dir.isDirectory()) {
			return Collections.emptyList();
		}
		String[] scriptNames = dir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".groovy");
			}
		});
		List<RowScript> result =new ArrayList<RowScript>(scriptNames.length);
		for (String fileName: scriptNames) {
			RowScript rc = new RowScript();
			rc.setName(fileName.substring(0, fileName.length() - 7));
			InputStream is = null;
			try {
				is = new FileInputStream(dir.getAbsoluteFile() + "/" + fileName);
				rc.setScript(IOUtils.toString(is, "UTF-8"));
			} catch (IOException e) {
				logger.error(e);
			} finally {
				IOUtils.closeQuietly(is);
			}
			result.add(rc);
		}
		return result;

	}
}
