package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.*;

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
	 * @param reportPeriod отчетный период в котором создается форма
	 * @return созданный и проинициализированный объект данных.
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException если у пользователя нет прав создавать налоговую форму с такими параметрами
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.ServiceException если при создании формы произошли ошибки, вызванные несоблюдением каких-то бизнес-требований, например отсутствием
	 *		обязательных параметров
	 */
	FormData createFormData(Logger logger, int userId, int formTemplateId, int departmentId, FormDataKind kind, ReportPeriod reportPeriod);
	
	/**
	 * Выполнить расчёты по налоговой форме
	 * @param logger логгер-объект для фиксации диагностических сообщений
	 * @param userId идентификатор пользователя, запросившего операцию
	 * @param formData объект с данными по налоговой форме
	 */
	void doCalc(Logger logger, int userId, FormData formData);

	/**
	 * Выполнить проверки по налоговой форме
	 * @param logger логгер-объект для фиксации диагностических сообщений
	 * @param userId идентификатор пользователя, запросившего операцию
	 * @param formData объект с данными по налоговой форме
	 */
	void doCheck(Logger logger, int userId, FormData formData);

	/**
	 * Сохранить данные по налоговой форме
	 * Если форма новая то блокирует её после сохранения.
	 * 
	 * @param userId идентификатор пользователя, выполняющего операцию
	 * @param formData объект с данными налоговой формы
	 * @return идентификатор сохранённой записи
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException если у пользователя нет прав редактировать налоговую форму с такими параметрами
	 */
	long saveFormData(Logger logger, int userId, FormData formData);
	
	/**
	 * Получить данные по налоговой форме
	 * @param userId идентификатор пользователя, выполняющего операцию
	 * @param formDataId идентификатор записи, которую необходимо считать
	 * @return объект с данными по налоговой форме
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException если у пользователя нет прав просматривать налоговую форму с такими параметрами
	 */
	FormData getFormData(int userId, long formDataId, Logger logger);

	/**
	 * Удалить данные по налоговой форме
	 * @param userId идентификатор пользователя, выполняющего операцию
	 * @param formDataId идентификатор записи, котрую нужно удалить
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException если у пользователя недостаточно прав для удаления записи
	 */
	void deleteFormData(int userId, long formDataId);

	/**
	 * Выполнить изменение статуса карточки
	 * @param formDataId идентификатор объекта {@link FormData}
	 * @param userId идентификатор пользователя, от имени которого выплняется действие
	 * @param move @{link WorkflowMove переход жизненного цикла}, который нужно выполнить
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.ServiceException
	 */
	public void doMove(long formDataId, int userId, WorkflowMove move, Logger logger);

	/**
	 * Создаёт налоговую форму без проверок прав доступа
	 * Метод предназначен для использования при реализации механизма консолидации, когда требуется создавать формы в чужих подразделениях
	 * @param logger объект журнала
	 * @param user пользователь, выполняющий операцию
	 * @param formTemplateId идентификатор шаблона формы
	 * @param departmentId идентификатор подразделения
	 * @param kind тип налоговой формы
	 * @return созданный объект FormData (еще не сохранённый в БД)
	 */
	FormData createFormDataWithoutCheck(Logger logger, TAUser user, int formTemplateId, int departmentId, FormDataKind kind);

	/**
	 * Добавляет строку в форму и выполняет соответствующие скрипты.
	 *
	 * @param logger логгер для регистрации ошибок
	 * @param userId идентификатор пользователя
	 * @param formData данные формы
	 * @param currentDataRow текущая выбранная строка в НФ. Может использоваться для того, чтобы вставить новую строку после текущей
	 */
	void addRow(Logger logger, int userId, FormData formData, DataRow currentDataRow);
	
	/**
	 * Удаляет строку из формы и выполняет соответствующие скрипты.
	 *
	 * @param logger логгер для регистрации ошибок
	 * @param userId идентификатор пользователя
	 * @param formData данные формы
	 * @param currentDataRow текущая выбранная строка в НФ, которую пытаемся удалить
	 */
	void deleteRow(Logger logger, int userId, FormData formData, DataRow currentDataRow);

	
	/**
	 * Заблокировать FormData.
	 * @param formDataId - идентификатор налоговой формы
	 * @param userId - идентификатор пользователя
	 * @return true - если удалось заблокировать налоговую форму, иначе - false
	 * */
	boolean lock(long formDataId, int userId);
	
	/**
	 * Снять блокировку с FormData.
	 * @param formDataId - идентификатор налоговой формы
	 * @param userId - идентификатор пользователя
	 * @return true - если удалось разблокировать налоговую форму, иначе - false
	 * */
	boolean unlock(long formDataId, int userId);

	/**
	 * Снять все блокировки с FormData для пользователя
	 * @param userId идентификатор пользователя
	 * @return true - если удалось разблокировать, иначе - false
	 */
	boolean unlockAllByUserId(int userId);

	/**
	 * Получить информацию о состоянии блокировки налоговой формы.
	 * @param formDataId - идентификатор налоговой формы
	 * @return информацию о блокировке объекта
	 */
	ObjectLock<Long> getObjectLock(long formDataId);
}
