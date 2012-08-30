package com.aplana.sbrf.taxaccounting.controller.formdata;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.dao.RowCheck;
import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;

@Service
public class FormDataService {
	public Logger validateFormData(FormData formData) {
		Logger logger = new Logger();
		checkRows(formData, logger);
		return logger;
	}
	
	private void performRowCheck(RowCheck check, FormData formData, ScriptEngine engine, Logger logger, RowCheckLogMessageDecorator messageDecorator) {
		messageDecorator.setOperationName(check.getName());
		int rowIndex = 0;
		for (DataRow row: formData.getDataRows()) {
			++rowIndex;
			messageDecorator.setRowIndex(rowIndex);
		    engine.put("row", row.getData());
	        engine.put("rowIndex", rowIndex);
	        try {
	        	engine.eval(check.getScript());
	        } catch (Exception e) {
	        	logger.error(e);
	        	break;
	        }
		}
		
	}
	
	private void checkRows(FormData formData, Logger logger) {
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("groovy");		
		engine.put("logger", logger);		
		RowCheckLogMessageDecorator rowMessageDecorator = new RowCheckLogMessageDecorator();
		logger.setMessageDecorator(rowMessageDecorator);
		for (RowCheck rowCheck: formData.getForm().getRowChecks()) {
			performRowCheck(rowCheck, formData, engine, logger, rowMessageDecorator);
		}
		logger.setMessageDecorator(null);
	}
}
