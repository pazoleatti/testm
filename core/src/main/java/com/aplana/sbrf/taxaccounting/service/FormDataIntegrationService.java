package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

/**
 * Сервис, отвечающий за интеграцию/дезинтеграцию форм. Поставляется в скрипты и позволяет формам посылать события
 * интеграции другим формам.
 *
 * @author Vitalii Samolovskikh
 * @see com.aplana.sbrf.taxaccounting.model.FormDataEvent
 */
public interface FormDataIntegrationService extends ScriptExposed {
	/**
	 * Интеграция формы (источника данных) в другую форму (потребителя) происходит в несколько этапов:
	 * <ol>
	 * <li>Поиск формы-потребителя по заданным параметрам. Отчетный период берется текущий.</li>
	 * <li>Если форма-потребитель не найдена, она создается автоматически.</li>
	 * <li>Форме-потребителю отправляется событие {@link com.aplana.sbrf.taxaccounting.model.FormDataEvent#INTEGRATE}</li>
	 * <li>На форме-потребителе выполняются скрипты, привязанные к событию {@link com.aplana.sbrf.taxaccounting.model.FormDataEvent#INTEGRATE}</li>
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
	 * @param logger       логгер для сохранения результатов работы скриптов
	 */
	public void integrateTo(int departmentId, int formTypeId, FormDataKind kind, Logger logger);

	/**
	 * Дезинтеграция налоговой формы  - это процесс, который происходит, когда какая либо из форм, являющихся источником
	 * данных для другой формы (потребителя), переходит из состояния "Принята" в состояние "Создана" или "Утверждена". И
	 * процесс этот происходит в несколько этапов.
	 * <ol>
	 * <li>Поиск формы-потребителя по заданным параметрам. Налоговый период берется текущий.</li>
	 * <li>Если форма найдена, ей отправляется событие {@link com.aplana.sbrf.taxaccounting.model.FormDataEvent#DISINTEGRATE}</li>
	 * <li>На форме-потребителе выполняются скрипты, привязанные к событию {@link com.aplana.sbrf.taxaccounting.model.FormDataEvent#DISINTEGRATE}</li>
	 * <li>...</li>
	 * <li>Profit!</li>
	 * </ol>
	 *
	 * @param departmentId идентификатор {@link com.aplana.sbrf.taxaccounting.model.Department подразделения}
	 *                     формы-потребителя. В скрипте, вызывающем данный метод, необходимо определить подразделение
	 *                     формы-потребителя. Как правило, это вышестоящее подразделение подразделения формы-источника
	 *                     или подразделение формы-источника.
	 * @param formTypeId   {@link com.aplana.sbrf.taxaccounting.model.FormType вид формы-потребителя}.
	 * @param kind         тип формы-потребителя: консолидированная, сводная.
	 * @param logger       логгер для сохранения результатов работы скриптов
	 */
	public void disintegrateFrom(int departmentId, int formTypeId, FormDataKind kind, Logger logger);
}
