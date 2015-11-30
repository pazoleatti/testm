package com.aplana.sbrf.taxaccounting.service.script.impl;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.FormDataScriptingService;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.LogBusinessService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
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

	@Override
	public void compose(FormData formData, int departmentReportPeriodId, Integer periodOrder,  int formTypeId, FormDataKind kind) {
        if (formData.getState() != WorkflowState.ACCEPTED) {
            auditService.add(FormDataEvent.COMPOSE, scriptComponentContext.getUserInfo(), null, formData, "Событие инициировано Системой", null);
			// Запускаем скрипт консолидации
			formDataScriptingService.executeScript(scriptComponentContext.getUserInfo(), formData,
                    FormDataEvent.COMPOSE, scriptComponentContext.getLogger(), null);
			// Проверяем результат выполнения скрипта
			String formName = formData.getFormType().getName();
            String kindName = formData.getKind().getTitle();
            String departmentName = departmentService.get(formData.getDepartmentId()).getName();
            if (scriptComponentContext.getLogger().containsLevel(LogLevel.ERROR)) {
                throw new ServiceLoggerException("Налоговая форма-приемник не сформирована: Подразделение: «%s», Тип: «%s», Вид: «%s».",
                        logEntryService.save(scriptComponentContext.getLogger().getEntries()),
                        departmentName, kindName, formName);
            } else {
                scriptComponentContext.getLogger().info("%s: %s налоговая форма-приемник: Подразделение: «%s», Тип: «%s», Вид: «%s».",
                        FormDataEvent.COMPOSE.getTitle(), "Переформирована", departmentName, kindName, formName);
            }
			// Удаляем ранее сформированные отчеты - стали неактуальными
            formDataService.deleteReport(formData.getId(), formData.isManual(), scriptComponentContext.getUserInfo().getUser().getId(), "Выполнена Подготовка/Утверждение/Принятие налоговой формы");
			//TODO Точно ли инициировано системой? Сейчас консолидируем по кнопке "Консолидировать". Аналогично в начале этого метода
            logBusinessService.add(formData.getId(), null, scriptComponentContext.getUserInfo(), FormDataEvent.COMPOSE, "Событие инициировано Системой");
		}
	}

	@Override
	public void setScriptComponentContext(ScriptComponentContext context) {
		scriptComponentContext = context;
	}
}