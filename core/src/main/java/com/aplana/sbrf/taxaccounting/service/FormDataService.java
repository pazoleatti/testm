package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;

/**
 * Сервис для работы с {@link FormData данными по налоговым формам}
 * @author dsultanbekov
 */
public interface FormDataService {
	/**
	 * Создать налоговую форму заданного типа
	 * При создании формы выполняются следующие действия:
	 * 1) создаётся пустой объект
	 * 2) если в объявлении формы заданы строки по-умолчанию (начальные данные), то эти строки копируются в созданную форму
	 * 3) если в объявлении формы задан скрипт создания, то этот скрипт выполняется над создаваемой формой
	 * @param logger логгер-объект для фиксации диагностических сообщений
	 * @param userId идентификатор пользователя, запросившего операцию
	 * @param formTemplateId идентификатор шаблона формы, по которой создавать объект
	 * @param departmentId идентификатор {@link com.aplana.sbrf.taxaccounting.model.Department подразделения}, к которому относится форма
	 * @param kind {@link FormDataKind тип налоговой формы} (первичная, сводная, и т.д.), это поле необходимо, так как некоторые виды
	 *		налоговых форм в одном и том же подразделении могут существовать в нескольких вариантах (например один и тот же РНУ  на уровне ТБ
	 *		- в виде первичной и консолидированной) 
	 * @return созданный и проинициализированный объект данных.
	 * @throws com.aplana.sbrf.taxaccounting.service.exception.AccessDeniedException если у пользователя нет прав создавать налоговую форму с такими параметрами
	 * @throws com.aplana.sbrf.taxaccounting.service.exception.ServiceException если при создании формы произошли ошибки, вызванные несоблюдением каких-то бизнес-требований, например отсутствием
	 *		обязательных параметров
	 */
	FormData createFormData(Logger logger, int userId, int formTemplateId, int departmentId, FormDataKind kind);
	
	/**
	 * Выполнить расчёты по налоговой форме
	 * @param logger логгер-объект для фиксации диагностических сообщений
	 * @param userId идентификатор пользователя, запросившего операцию
	 * @param formData объект с данными по налоговой форме
	 */
	void doCalc(Logger logger, int userId, FormData formData);
	
	/**
	 * Сохранить данные по налоговой форме
	 * @param userId идентификатор пользователя, выполняющего операцию
	 * @param formData объект с данными налоговой формы
	 * @return идентификатор сохранённой записи
	 * @throws com.aplana.sbrf.taxaccounting.service.exception.AccessDeniedException если у пользователя нет прав редактировать налоговую форму с такими параметрами
	 */
	long saveFormData(int userId, FormData formData);
	
	/**
	 * Получить данные по налоговой форме
	 * @param userId идентификатор пользователя, выполняющего операцию
	 * @param formDataId идентификатор записи, которую необходимо считать
	 * @return объект с данными по налоговой форме
	 * @throws com.aplana.sbrf.taxaccounting.service.exception.AccessDeniedException если у пользователя нет прав просматривать налоговую форму с такими параметрами
	 */
	FormData getFormData(int userId, long formDataId);
	
	/**
	 * Удалить данные по налоговой форме
	 * @param userId идентификатор пользователя, выполняющего операцию
	 * @param formDataId идентификатор записи, котрую нужно удалить
	 * @throws com.aplana.sbrf.taxaccounting.service.exception.AccessDeniedException если у пользователя недостаточно прав для удаления записи
	 */
	void deleteFormData(int userId, long formDataId);
	
}
