package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.security.TAUser;

/**
 * Интерфейс сервиса, реализующего выполение скриптов над данными налоговых форм
 */
public interface FormDataScriptingService {

	/**
	 * Создать налоговую форму заданного типа
	 * При создании формы выполняются следующие действия:
	 * 1) создаётся пустой объект
	 * 2) если в объявлении формы заданы строки по-умолчанию (начальные данные), то эти строки копируются в созданную форму
	 * 3) если в объявлении формы задан скрипт создания, то этот скрипт выполняется над создаваемой формой
	 * @param logger логгер-объект для фиксации диагностических сообщений
	 * @param formTemplateId идентификатор шаблона формы, по которой создавать объект
	 * @param departmentId идентификатор {@link Department подразделения}, к которому относится форма
	 * @param kind {@link FormDataKind тип налоговой формы} (первичная, сводная, и т.д.), это поле необходимо, так как некоторые виды
	 * 		налоговых форм в одном и том же подразделении могут существовать в нескольких вариантах (например один и тот же РНУ  на уровне ТБ
	 * 		- в виде первичной и консолидированной)
	 * @param currentUser тпользователь, создающий форму
	 * @return созданный и проинициализированный объект данных.
	 */
	public FormData createForm(Logger logger, int formTemplateId, int departmentId, FormDataKind kind, TAUser currentUser);

	/**
	 * Выполнить рассчётные скрипты по форме. Скрипты используются как для расчёта значений, так и для валидации введённых значений
	 * @param logger логге-объект для вывода диагностических сообщений
	 * @param formData данные формы, по которым необходимо выполнить расчёт.
	 * @param currentUser текущий пользователь, рассчитывающий форму
	 */
	public void processFormData(Logger logger, FormData formData, TAUser currentUser);

}