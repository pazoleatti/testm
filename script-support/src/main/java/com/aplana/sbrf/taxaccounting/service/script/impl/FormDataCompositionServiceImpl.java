package com.aplana.sbrf.taxaccounting.service.script.impl;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.service.script.ReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.shared.FormDataCompositionService;
import com.aplana.sbrf.taxaccounting.service.shared.ScriptComponentContext;
import com.aplana.sbrf.taxaccounting.service.shared.ScriptComponentContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Сервис, отвечающий за интеграцию/дезинтеграцию форм. Поставляется в скрипты и позволяет формам посылать события
 * интеграции другим формам.
 *
 * @author Vitalii Samolovskikh
 * @see com.aplana.sbrf.taxaccounting.model.FormDataEvent
 */
@Component("formDataCompositionService")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FormDataCompositionServiceImpl implements FormDataCompositionService, ScriptComponentContextHolder {
	
	private ScriptComponentContext scriptComponentContext;
	
	@Autowired
	private DataRowDao dataRowDao;

	@Autowired
	private FormTemplateDao formTemplateDao;

	@Autowired
	private FormDataDao formDataDao;

	@Autowired
	private FormDataService formDataService;

	@Autowired
	private FormDataScriptingService formDataScriptingService;

	@Autowired
	private LogBusinessService logBusinessService;

	@Autowired
	private AuditService auditService;

    @Autowired
    private ReportPeriodService reportPeriodService;

    @Autowired
    private LogEntryService logEntryService;

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
	public void compose(FormData sformData, int departmentId, int formTypeId, FormDataKind kind) {
        // Find form data.
        FormData dformData;
        if (sformData.getPeriodOrder() == null) {
            dformData = formDataDao.find(formTypeId, kind, departmentId, sformData.getReportPeriodId());
        } else {
            Integer taxPeriodId = reportPeriodService.get(sformData.getReportPeriodId()).getTaxPeriod().getId();
            dformData = formDataDao.findMonth(formTypeId, kind, departmentId, taxPeriodId, sformData.getPeriodOrder());
        }

        // Create form data if doesn't exist.
		if (dformData == null) {
			// TODO: Надо подумать, что делать с пользователем.
			int formTemplateId = formTemplateDao.getActiveFormTemplateId(formTypeId);
            // Создание формы в том же периоде
			long dFormDataId = formDataService.createFormDataWithoutCheck(scriptComponentContext.getLogger(),
                    scriptComponentContext.getUserInfo(), formTemplateId, departmentId, kind,
                    sformData.getReportPeriodId(), sformData.getPeriodOrder(), false);
			dformData = formDataDao.get(dFormDataId);
        }

		if(dformData.getState() != WorkflowState.ACCEPTED){
			auditService.add(FormDataEvent.COMPOSE, scriptComponentContext.getUserInfo(),
					dformData.getDepartmentId(), dformData.getReportPeriodId(),
					null, dformData.getFormType().getId(), dformData.getKind().getId(), "Событие инициировано Системой");
			
			// Execute composition scripts
			formDataScriptingService.executeScript(scriptComponentContext.getUserInfo(), dformData,
                    FormDataEvent.COMPOSE, scriptComponentContext.getLogger(), null);

            if (scriptComponentContext.getLogger().containsLevel(LogLevel.ERROR)) {
                throw new ServiceLoggerException(
                        "Произошли ошибки при консолидации данных в приемнике: " +
                                dformData.getKind().getName() + " \"" + dformData.getFormType().getName() + "\".",
                        logEntryService.save(scriptComponentContext.getLogger().getEntries()));
            }

            formDataDao.save(dformData);
			// Коммитим строки после отработки скрипта. http://jira.aplana.com/browse/SBRFACCTAX-3637
			dataRowDao.commit(dformData.getId());
            logBusinessService.add(dformData.getId(), null, scriptComponentContext.getUserInfo(), FormDataEvent.COMPOSE,
                    "Событие инициировано Системой");
		}
	}

	@Override
	public void setScriptComponentContext(ScriptComponentContext context) {
		scriptComponentContext = context;
	}
}
