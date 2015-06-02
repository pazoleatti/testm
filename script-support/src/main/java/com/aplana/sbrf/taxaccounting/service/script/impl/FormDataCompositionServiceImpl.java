package com.aplana.sbrf.taxaccounting.service.script.impl;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.service.*;
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
    private LogEntryService logEntryService;

    @Autowired
    private DepartmentServiceImpl departmentService;

    @Autowired
    private DepartmentReportPeriodDao departmentReportPeriodDao;

	@Override
	public void compose(FormData formData, int departmentReportPeriodId, Integer periodOrder,  int formTypeId, FormDataKind kind) {
        // пересобирается ли форма
        boolean isRecompose = true;
        // Если экземпляр не найден, то он создается
        /*if (formData == null) {
            isRecompose = false;
            DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.get(departmentReportPeriodId);
            int formTemplateId = 0;
            try {
                formTemplateId = formTemplateDao.getActiveFormTemplateId(formTypeId, departmentReportPeriod.getReportPeriod().getId());
            } catch (DaoException e) {
                throw new ServiceException(e.getLocalizedMessage(), e);
            }
            // Создание формы в том же периоде
			long dFormDataId = formDataService.createFormDataWithoutCheck(scriptComponentContext.getLogger(),
                    scriptComponentContext.getUserInfo(), formTemplateId, departmentReportPeriodId, kind, periodOrder, false);
			formData = formDataDao.get(dFormDataId, false);
        } else {
            FormTemplate formTemplate = formTemplateDao.get(formData.getFormTemplateId());
            formData.initFormTemplateParams(formTemplate);
        }*/

        if (formData.getState() != WorkflowState.ACCEPTED) {
            auditService.add(FormDataEvent.COMPOSE, scriptComponentContext.getUserInfo(),
					formData.getDepartmentId(), formData.getReportPeriodId(),
					null, formData.getFormType().getName(), formData.getKind().getId(), "Событие инициировано Системой", null, formTypeId);

			// Execute composition scripts
			formDataScriptingService.executeScript(scriptComponentContext.getUserInfo(), formData,
                    FormDataEvent.COMPOSE, scriptComponentContext.getLogger(), null);

            String formName = formData.getFormType().getName();
            String kindName = formData.getKind().getName();
            String departmentName = departmentService.get(formData.getDepartmentId()).getName();
            if (scriptComponentContext.getLogger().containsLevel(LogLevel.ERROR)) {
                throw new ServiceLoggerException("Налоговая форма-приемник не сформирована: Подразделение: «%s», Тип: «%s», Вид: «%s».",
                        logEntryService.save(scriptComponentContext.getLogger().getEntries()),
                        departmentName, kindName, formName);
            } else {
                scriptComponentContext.getLogger().info("%s: %s налоговая форма-приемник: Подразделение: «%s», Тип: «%s», Вид: «%s».",
                        FormDataEvent.COMPOSE.getTitle(), isRecompose? "Переформирована" : "Сформирована", departmentName, kindName, formName);
            }

            formDataDao.save(formData);
			// Коммитим строки после отработки скрипта. http://jira.aplana.com/browse/SBRFACCTAX-3637
			dataRowDao.commit(formData);
            formDataService.deleteReport(formData.getId(), formData.isManual());
            logBusinessService.add(formData.getId(), null, scriptComponentContext.getUserInfo(), FormDataEvent.COMPOSE,
                    "Событие инициировано Системой");
		}
	}

	@Override
	public void setScriptComponentContext(ScriptComponentContext context) {
		scriptComponentContext = context;
	}
}
