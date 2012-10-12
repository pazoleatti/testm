package com.aplana.sbrf.taxaccounting.controller.formdata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.dao.FormDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.Form;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.Script;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

@Service
public class FormDataScriptingService implements ApplicationContextAware {
	@Autowired
	private FormDao formDao;
	@Autowired
	private FormDataDao formDataDao;
	
	private Map<String, Object> scriptExposedBeans;
	
	public FormData createForm(Logger logger, int formId) {
		Form form = formDao.getForm(formId);
		FormData result = new FormData(form);
		for (DataRow predefinedRow: form.getRows()) {
			DataRow dataRow = result.appendDataRow(predefinedRow.getAlias());
			for (Map.Entry<String, Object> entry: predefinedRow.entrySet()) {
				dataRow.put(entry.getKey(), entry.getValue());
			}
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
		engine.put("formData", formData);
		List<Script> calcScripts = form.getCalcScripts();
		ScriptMessageDecorator d = new ScriptMessageDecorator();
		for (Script calcScript: calcScripts) {
			if (calcScript.isRowScript()) {
				performRowScript(calcScript, formData, engine, logger);
			} else {
				try {
					logger.setMessageDecorator(d);
					if (calcScript.getCondition() != null) {
						d.setScriptName(calcScript.getName() + " - Условие исполнения");
						boolean check = (Boolean)engine.eval(calcScript.getCondition());
						if (!check) {
							continue;
						}
					}
					d.setScriptName(calcScript.getName());
					engine.eval(calcScript.getBody());
				} catch (Exception e) {
					logger.error(e);
				} finally {
					logger.setMessageDecorator(null);
				}
			}
		}
		checkMandatoryColumns(formData, logger);
		logger.setMessageDecorator(null);
	}

	private void performRowScript(Script rowScript, FormData formData, ScriptEngine engine, Logger logger) {
		RowScriptMessageDecorator d = new RowScriptMessageDecorator();
		logger.setMessageDecorator(d);
		int rowIndex = 0;
		for (DataRow row: formData.getDataRows()) {
			++rowIndex;
			d.setRowIndex(rowIndex);
			engine.put("row", row);
			engine.put("rowIndex", rowIndex);
			engine.put("rowAlias", row.getAlias());
			try {
				if (rowScript.getCondition() != null) {
					d.setScriptName(rowScript.getName() + " - Условие исполнения");
					boolean check = (Boolean)engine.eval(rowScript.getCondition());
					if (!check) {
						continue;
					}
				}
				d.setScriptName(rowScript.getName());
				engine.eval(rowScript.getBody());
			} catch (Exception e) {
				logger.error(e);
				// TODO: возможно не стоит делать брек, а ограничится выводом сообщения?
				// или даже сделать опцию в скрипте, определяющую данное поведение
				break;
			} finally {
				Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
				bindings.remove("row");
				bindings.remove("rowIndex");
				bindings.remove("rowAlias");
			}
		}
		logger.setMessageDecorator(null);
	}
	
	private void checkMandatoryColumns(FormData formData, Logger logger) {
		List<Column> columns = formData.getForm().getColumns();
		RowScriptMessageDecorator messageDecorator = new RowScriptMessageDecorator();
		messageDecorator.setScriptName("Проверка обязательных полей");
		logger.setMessageDecorator(messageDecorator);
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
		logger.setMessageDecorator(null);
	}
	
	private ScriptEngine getScriptEngine() {
		ScriptEngineManager factory = new ScriptEngineManager();
		ScriptEngine engine = factory.getEngineByName("groovy");
		Bindings b = engine.createBindings();
		b.putAll(scriptExposedBeans);
		engine.put("formDataDao", formDataDao);
		engine.setBindings(b, ScriptContext.ENGINE_SCOPE);
		return engine;
	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		context = context.getParent();
		scriptExposedBeans = new ConcurrentHashMap<String, Object>();
		scriptExposedBeans.putAll(context.getBeansOfType(ScriptExposed.class));
	}
}
