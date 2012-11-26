package com.aplana.sbrf.taxaccounting.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.aplana.sbrf.taxaccounting.model.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.log.impl.RowScriptMessageDecorator;
import com.aplana.sbrf.taxaccounting.log.impl.ScriptMessageDecorator;
import com.aplana.sbrf.taxaccounting.service.FormDataScriptingService;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

@Service
public class FormDataScriptingServiceImpl implements ApplicationContextAware, FormDataScriptingService {
	@Autowired
	private FormTemplateDao formTemplateDao;
	@Autowired
	private FormDataDao formDataDao;

	private Map<String, Object> scriptExposedBeans;

	/* (non-Javadoc)
	 * @see com.aplana.sbrf.taxaccounting.service.impl.FormDataScriptingService#createForm(com.aplana.sbrf.taxaccounting.log.Logger, int)
	 */
	@Override
	public FormData createForm(Logger logger, int formTemplateId, int departmentId, FormDataKind kind) {
		FormTemplate form = formTemplateDao.get(formTemplateId);
		FormData result = new FormData(form);

		result.setState(WorkflowState.CREATED);
		result.setDepartmentId(departmentId);
		// TODO: сюда хорошо бы добавить проверку, что данный тип формы соответствует
		// виду формы (FormType) и уровню подразделения (например сводные нельзя делать на уровне ниже ТБ)
		result.setKind(kind);

		for (DataRow predefinedRow : form.getRows()) {
			DataRow dataRow = result.appendDataRow(predefinedRow.getAlias());
			for (Map.Entry<String, Object> entry : predefinedRow.entrySet()) {
				dataRow.put(entry.getKey(), entry.getValue());
			}
		}

		// Execute scripts for the form event CREATE
		List<Script> scripts = form.getScriptsByEvent(FormDataEvent.CREATE);
		executeScripts(scripts, result, logger);
		return result;
	}

	/* (non-Javadoc)
	 * @see com.aplana.sbrf.taxaccounting.service.impl.FormDataScriptingService#processFormData(com.aplana.sbrf.taxaccounting.log.Logger, com.aplana.sbrf.taxaccounting.model.FormData)
	 */
	@Override
	public void processFormData(Logger logger, FormData formData) {
		FormTemplate form = formTemplateDao.get(formData.getFormTemplateId());
		List<Script> calcScripts = form.getScriptsByEvent(FormDataEvent.CALCULATE);

		executeScripts(calcScripts, formData, logger);
		checkMandatoryColumns(formData, form, logger);
		logger.setMessageDecorator(null);
	}

	/**
	 * Выполняет последовательность сриптов для определенной формы
	 *
	 * @param scripts  Список скриптов. Порядок формы задает порядок выполнения.
	 * @param formData Данные формы
	 * @param logger   Логгер для сохранения ошщибок скрипта.
	 */
	private void executeScripts(List<Script> scripts, FormData formData, Logger logger) {
		ScriptEngine engine = getScriptEngine();
		engine.put("logger", logger);
		engine.put("formData", formData);
		ScriptMessageDecorator d = new ScriptMessageDecorator();
		for (Script calcScript : scripts) {
			if (calcScript.isRowScript()) {
				performRowScript(calcScript, formData, engine, logger);
			} else {
				try {
					logger.setMessageDecorator(d);
					if (calcScript.getCondition() != null) {
						d.setScriptName(calcScript.getName() + " - Условие исполнения");
						boolean check = (Boolean) engine.eval(calcScript.getCondition());
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
	}

	private void performRowScript(Script rowScript, FormData formData, ScriptEngine engine, Logger logger) {
		RowScriptMessageDecorator d = new RowScriptMessageDecorator();
		logger.setMessageDecorator(d);
		int rowIndex = 0;
		for (DataRow row : formData.getDataRows()) {
			++rowIndex;
			d.setRowIndex(rowIndex);
			engine.put("row", row);
			engine.put("rowIndex", rowIndex);
			engine.put("rowAlias", row.getAlias());
			try {
				if (rowScript.getCondition() != null) {
					d.setScriptName(rowScript.getName() + " - Условие исполнения");
					boolean check = (Boolean) engine.eval(rowScript.getCondition());
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

	private void checkMandatoryColumns(FormData formData, FormTemplate formTemplate, Logger logger) {
		List<Column> columns = formTemplate.getColumns();
		RowScriptMessageDecorator messageDecorator = new RowScriptMessageDecorator();
		messageDecorator.setScriptName("Проверка обязательных полей");
		logger.setMessageDecorator(messageDecorator);
		int rowIndex = 0;
		for (DataRow row : formData.getDataRows()) {
			++rowIndex;
			messageDecorator.setRowIndex(rowIndex);
			List<String> columnNames = new ArrayList<String>();
			for (Column col : columns) {
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
		scriptExposedBeans = new ConcurrentHashMap<String, Object>();
		scriptExposedBeans.putAll(context.getBeansOfType(ScriptExposed.class));
	}
}
