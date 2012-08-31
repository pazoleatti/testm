package com.aplana.sbrf.taxaccounting.controller.formdata;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.dao.FormDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.dictionary.TransportTaxDao;
import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.Form;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.script.FormDataScriptingService;
import com.aplana.sbrf.taxaccounting.script.RowScript;
import com.aplana.sbrf.taxaccounting.script.Script;

@Service
public class FormDataScriptingServiceImpl implements FormDataScriptingService {
	Log logger = LogFactory.getLog(getClass());
	private final static String ROOT = "C:/workspace/scripts/src/groovy";
	
	@Autowired
	private FormDao formDao;
	
	@Autowired
	private FormDataDao formDataDao;	
	
	@Autowired
	private TransportTaxDao transportTaxDao;
	
	@Override
	public FormData createForm(Logger logger, int formId) {
		Form form = formDao.getForm(formId);
		FormData result = new FormData(form);
		
		Script createScript = getCreationScript(formId);
		if (createScript != null) {
			ScriptEngine engine = getScriptEngine();		
			engine.put("logger", logger);
			engine.put("formData", result);
			try {
				engine.eval(createScript.getScript());
			} catch (ScriptException e) {
				logger.error(e);
			}
		}
		return result;
	}

	public void processFormData(Logger logger, FormData formData) {
		ScriptEngine engine = getScriptEngine();
		engine.put("logger", logger);
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
			engine.put("row", row);
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
				if (col.isMandatory() && row.get(col.getAlias()) == null) {
					columnNames.add(col.getName());
				}
			}
			if (!columnNames.isEmpty()) {
				logger.error("Не заполнены столбцы %s", columnNames.toString());
			}
		}
	}
	
	private String getFileContent(File file) {
		try {
			return FileUtils.readFileToString(file, "UTF-8");
		} catch (IOException e) {
			logger.error("Failed to read file content", e);
			return null;
		}
	}

	@Override
	public List<RowScript> getFormRowScripts(int formId) {
		File dir = new File(ROOT + "/" + formId + "/rows");
		if (!dir.exists() || !dir.isDirectory()) {
			return Collections.emptyList();
		}
		Collection<File> scriptNames = FileUtils.listFiles(dir, new String[] {"groovy"}, false);
		List<RowScript> result = new ArrayList<RowScript>(scriptNames.size());
		for (File file: scriptNames) {
			RowScript rc = new RowScript();
			String filename = file.getName();
			rc.setName(FilenameUtils.removeExtension(filename));
			rc.setScript(getFileContent(file));
			result.add(rc);
		}
		return result;

	}

	@Override
	public Script getCreationScript(int formId) {
		File f = new File(ROOT + "/" + formId + "/create.groovy");
		if (!f.exists()) {
			return null;
		}
		String scriptText = getFileContent(f);
		if (scriptText == null) {
			return null;
		} else {
			Script result = new Script();
			result.setScript(scriptText);
			return result;
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
