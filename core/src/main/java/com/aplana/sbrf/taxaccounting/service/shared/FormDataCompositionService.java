package com.aplana.sbrf.taxaccounting.service.shared;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

import static com.aplana.sbrf.taxaccounting.model.FormDataEvent.*;

/**
 * Сервис, отвечающий за интеграцию/дезинтеграцию форм. Поставляется в скрипты и позволяет формам посылать события
 * интеграции другим формам.
 *
 * @author Vitalii Samolovskikh
 * @see com.aplana.sbrf.taxaccounting.model.FormDataEvent
 */
@ScriptExposed(formDataEvents = {
		AFTER_MOVE_APPROVED_TO_ACCEPTED,
		AFTER_MOVE_ACCEPTED_TO_APPROVED,
		AFTER_MOVE_CREATED_TO_ACCEPTED,
		AFTER_MOVE_ACCEPTED_TO_CREATED,
		AFTER_MOVE_PREPARED_TO_ACCEPTED,
        AFTER_MOVE_ACCEPTED_TO_PREPARED
})
public interface FormDataCompositionService {
	/**
	 * Консолидация формы-потребителя из форм-источников в несколько этапов:
	 * <ol>
	 * <li>Если экземпляр формы-потребителя не найден, то он создается автоматически.</li>
	 * <li>Экземпляру формы-потребителю отправляется событие {@link com.aplana.sbrf.taxaccounting.model.FormDataEvent#COMPOSE}</li>
	 * </ol>
	 * <p/>
	 * Этот метод вызывается тогда, когда форма-источник переходит в статус «Принята».
     * @param formData Экземпляр формы-приемника (может отсутствовать)
     * @param departmentReportPeriodId Отчетный период подразделения
     * @param periodOrder Месяц для ежемесячных форм (для квартальных форм не задается)
	 * @param formTypeId Вид формы-приемника
	 * @param kind  Тип формы-приемника
	 */
	void compose(FormData formData, int departmentReportPeriodId, Integer periodOrder, int formTypeId, FormDataKind kind);
}
