package com.aplana.sbrf.taxaccounting.service.script.impl;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.FormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;
import com.aplana.sbrf.taxaccounting.service.FormDataScriptingService;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.script.FormDataCompositionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

/**
 * Сервис, отвечающий за интеграцию/дезинтеграцию форм. Поставляется в скрипты и позволяет формам посылать события
 * интеграции другим формам.
 *
 * @author Vitalii Samolovskikh
 * @see com.aplana.sbrf.taxaccounting.model.FormDataEvent
 */
@Service
public class FormDataCompositionServiceImpl implements FormDataCompositionService {

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
	public void compose(int departmentId, int formTypeId, FormDataKind kind, Logger logger) {
		TaxType taxType = formTypeDao.getType(formTypeId).getTaxType();
		ReportPeriod currentPeriod = reportPeriodDao.getCurrentPeriod(taxType);

		// Find form data.
		FormData formData = formDataDao.find(formTypeId, kind, departmentId, currentPeriod.getId());

		// Create form data if doesn't exist.
		if (formData == null) {
			// TODO: Надо подумать, что делать с пользователем.
			int formTemplateId = formTemplateDao.getActiveFormTemplateId(formTypeId);
			formData = formDataService.createFormDataWithoutCheck(logger, null, formTemplateId, departmentId, kind);
		}

		if(formData.getState() == WorkflowState.CREATED){
			// Execute composition scripts
			// TODO: Надо подумать, что делать с пользователем да и вообще.
			formDataScriptingService.executeScripts(null, formData, FormDataEvent.COMPOSE, logger);
			formDataScriptingService.executeScripts(null, formData, FormDataEvent.CALCULATE, logger);
			formDataDao.save(formData);
		} else {
			logger.error("Невозможно принять форму. Сводная форма вышестоящего уровня уже принята.");
		}
	}
}
