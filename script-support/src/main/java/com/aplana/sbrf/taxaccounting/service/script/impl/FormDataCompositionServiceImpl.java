package com.aplana.sbrf.taxaccounting.service.script.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.FormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;
import com.aplana.sbrf.taxaccounting.service.FormDataScriptingService;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.script.FormDataCompositionService;
import com.aplana.sbrf.taxaccounting.service.script.ScriptComponentContext;
import com.aplana.sbrf.taxaccounting.service.script.ScriptComponentContextHolder;

/**
 * Сервис, отвечающий за интеграцию/дезинтеграцию форм. Поставляется в скрипты и позволяет формам посылать события
 * интеграции другим формам.
 *
 * @author Vitalii Samolovskikh
 * @see com.aplana.sbrf.taxaccounting.model.FormDataEvent
 */
@Component("formDataCompositionService")
@Scope(value="prototype")
public class FormDataCompositionServiceImpl implements FormDataCompositionService, ScriptComponentContextHolder {
	
	private ScriptComponentContext scriptComponentContext;

	@Autowired
	private FormTypeDao formTypeDao;

	@Autowired
	private FormTemplateDao formTemplateDao;

	@Autowired
	private ReportPeriodDao reportPeriodDao;

	@Autowired
	private FormDataDao formDataDao;

	@Autowired
	private FormDataService formDataService;

	@Autowired
	private FormDataScriptingService formDataScriptingService;

    @Autowired
    private DepartmentDao departmentDao;

	/**
	 * Интеграция формы (источника данных) в другую форму (потребителя) происходит в несколько этапов:
	 * <ol>
	 * <li>Поиск формы-потребителя по заданным параметрам. Отчетный период берется текущий.</li>
	 * <li>Если форма-потребитель не найдена, она создается автоматически.</li>
	 * <li>Форме-потребителю отправляется событие {@link com.aplana.sbrf.taxaccounting.model.FormDataEvent#COMPOSE}</li>
	 * <li>На форме-потребителе выполняются скрипты, привязанные к событию {@link com.aplana.sbrf.taxaccounting.model.FormDataEvent#COMPOSE}</li>
	 * </ol>
	 * <p/>
	 * Этот метод вызывается тогда, когда форма-источник переходит в состояние "Принята". Вызов должен происходить из
	 * скрипта, прикрепленного к соответствующему событию.
	 *
	 * @param departmentId идентификатор {@link com.aplana.sbrf.taxaccounting.model.Department подразделения}
	 *                     формы-потребителя. В скрипте, вызывающем данный метод, необходимо определить подразделение
	 *                     формы-потребителя. Как правило, это вышестоящее подразделение подразделения формы-источника
	 *                     или подразделение формы-источника.
	 * @param formTypeId   {@link com.aplana.sbrf.taxaccounting.model.FormType вид формы-потребителя}.
	 * @param kind         тип формы-потребителя: консолидированная, сводная.
	 */
	@Override
	public void compose(FormData sformData, int departmentId, int formTypeId, FormDataKind kind, Logger logger) {
		TaxType taxType = formTypeDao.getType(formTypeId).getTaxType();
		ReportPeriod currentPeriod = reportPeriodDao.getCurrentPeriod(taxType);

		// Find form data.
		FormData dformData = formDataDao.find(formTypeId, kind, departmentId, currentPeriod.getId());

		// Create form data if doesn't exist.
		if (dformData == null) {
			// TODO: Надо подумать, что делать с пользователем.
			int formTemplateId = formTemplateDao.getActiveFormTemplateId(formTypeId);
			dformData = formDataService.createFormDataWithoutCheck(logger, null, formTemplateId, departmentId, kind, currentPeriod.getId(), false);
		}

		if(dformData.getState() != WorkflowState.ACCEPTED){
			// Execute composition scripts
			// TODO: Надо подумать, что делать с пользователем да и вообще.
			formDataScriptingService.executeScript(null, dformData, FormDataEvent.COMPOSE, logger, null);
			formDataDao.save(dformData);
		} else {
            FormTemplate sformTemplate = formTemplateDao.get(sformData.getFormTemplateId());
            FormTemplate dformTemplate = formTemplateDao.get(dformData.getFormTemplateId());
            Department sformDepartment =  departmentDao.getDepartment(dformData.getDepartmentId());
            logger.error("Невозможно принять \""+sformTemplate.getType().getName()+"\", поскольку уже принята форма: "+dformData.getKind().getName()+" \""+dformTemplate.getType().getName()+"\" ("+sformDepartment.getName()+").");
		}
	}


	@Override
	public void setScriptComponentContext(ScriptComponentContext context) {
		this.scriptComponentContext = scriptComponentContext;		
	}
}
