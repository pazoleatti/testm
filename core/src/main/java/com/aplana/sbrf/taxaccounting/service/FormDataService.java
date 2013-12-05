package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

import java.io.InputStream;

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
	 * @param userInfo информация о пользователе, запросившего операцию
	 * @param formTemplateId идентификатор шаблона формы, по которой создавать объект
	 * @param departmentId идентификатор {@link com.aplana.sbrf.taxaccounting.model.Department подразделения}, к которому относится форма
	 * @param kind {@link FormDataKind тип налоговой формы} (первичная, сводная, и т.д.), это поле необходимо, так как некоторые виды
	 *		налоговых форм в одном и том же подразделении могут существовать в нескольких вариантах (например один и тот же РНУ  на уровне ТБ
	 *		- в виде первичной и консолидированной)
	 * @param reportPeriod отчетный период в котором создается форма
     * @param periodOrder номер месяца для ежемесячных форм (для остальных параметр отсутствует)
	 * @return созданный и проинициализированный объект данных.
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException если у пользователя нет прав создавать налоговую форму с такими параметрами
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.ServiceException если при создании формы произошли ошибки, вызванные несоблюдением каких-то бизнес-требований, например отсутствием
	 *		обязательных параметров
	 */
	long createFormData(Logger logger, TAUserInfo userInfo, int formTemplateId, int departmentId, FormDataKind kind,
                        ReportPeriod reportPeriod, Integer periodOrder);

    /**
     * Метод для импорта данных из xls-файлов
     * 
     * @param logger
     * @param userInfo
     * @param formDataId
     * @param is
     * @param fileName
     * 
     * TODO (sgoryachkin) заменить параметры is и fileName на uid блоба
     */
    void importFormData(Logger logger, TAUserInfo userInfo, long formDataId, InputStream is, String fileName);

    /**
     * Метод для импорта данных из модуля миграции
     *
     * @param logger
     * @param userInfo
     * @param formDataId
     * @param is
     * @param fileName
     *
     */
    void migrationFormData(Logger logger, TAUserInfo userInfo, long formDataId, InputStream is, String fileName);
	
	/**
	 * Выполнить расчёты по налоговой форме
	 * @param logger логгер-объект для фиксации диагностических сообщений
	 * @param userInfo информация о пользователе, запросившего операцию
	 * @param formData объект с данными по налоговой форме
	 */
	void doCalc(Logger logger, TAUserInfo userInfo, FormData formData);

	/**
	 * Выполнить проверки по налоговой форме
	 * @param logger логгер-объект для фиксации диагностических сообщений
	 * @param userInfo информация о пользователе, запросившего операцию
	 * @param formData объект с данными по налоговой форме
	 */
	void doCheck(Logger logger, TAUserInfo userInfo, FormData formData);

	/**
	 * Сохранить данные по налоговой форме
	 * Если форма новая то блокирует её после сохранения.
	 * 
	 * @param userInfo информация о пользователея, выполняющего операцию
	 * @param formData объект с данными налоговой формы
	 * @return идентификатор сохранённой записи
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException если у пользователя нет прав редактировать налоговую форму с такими параметрами
	 */
	long saveFormData(Logger logger, TAUserInfo userInfo, FormData formData);
	
	/**
	 * Получить данные по налоговой форме
	 * @param userInfo информация о пользователе, выполняющего операцию
	 * @param formDataId идентификатор записи, которую необходимо считать
	 * @return объект с данными по налоговой форме
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException если у пользователя нет прав просматривать налоговую форму с такими параметрами
	 */
	FormData getFormData(TAUserInfo userInfo, long formDataId, Logger logger);

	/**
	 * Удалить данные по налоговой форме
	 * @param userInfo информация о пользователе, выполняющего операцию
	 * @param formDataId идентификатор записи, котрую нужно удалить
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException если у пользователя недостаточно прав для удаления записи
	 */
	void deleteFormData(TAUserInfo userInfo, long formDataId);

	/**
	 * Выполнить изменение статуса карточки
	 * @param formDataId идентификатор объекта {@link FormData}
	 * @param userInfo информация о пользователе, от имени которого выполняется действие
	 * @param move @{link WorkflowMove переход жизненного цикла}, который нужно выполнить
	 * @param note Причина возврата (перехода) по ЖЦ в Системе
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.ServiceException
	 */
	void doMove(long formDataId, TAUserInfo userInfo, WorkflowMove move, String note, Logger logger);

    /**
     * Создаёт налоговую форму без проверок прав доступа
     * Метод предназначен для использования при реализации механизма консолидации, когда требуется создавать формы в чужих подразделениях
     * @param logger объект журнала
     * @param userInfo информация о пользователе, выполняющего операцию
     * @param formTemplateId идентификатор шаблона формы
     * @param departmentId идентификатор подразделения
     * @param kind тип налоговой формы
     * @param reportPeriodId идентифиуатор отчетного периода
     * @param periodOrder номер месяца для ежемесячных форм (для остальных параметр отсутствует)
     * @param importFormData признак импорта
     * @return созданный объект FormData (еще не сохранённый в БД)
     */
	long createFormDataWithoutCheck(Logger logger, TAUserInfo userInfo, int formTemplateId, int departmentId,
                                    FormDataKind kind, int reportPeriodId, Integer periodOrder, boolean importFormData);

	/**
	 * Добавляет строку в форму и выполняет соответствующие скрипты.
	 *
	 * @param logger логгер для регистрации ошибок
	 * @param userInfo информация о пользователе
	 * @param formData данные формы
	 * @param currentDataRow текущая выбранная строка в НФ. Может использоваться для того, чтобы вставить новую строку после текущей
	 */
	void addRow(Logger logger, TAUserInfo userInfo, FormData formData, DataRow<Cell> currentDataRow);
	
	/**
	 * Удаляет строку из формы и выполняет соответствующие скрипты.
	 *
	 * @param logger логгер для регистрации ошибок
	 * @param userInfo информация о пользователе
	 * @param formData данные формы
	 * @param currentDataRow текущая выбранная строка в НФ, которую пытаемся удалить
	 */
	void deleteRow(Logger logger, TAUserInfo userInfo, FormData formData, DataRow<Cell> currentDataRow);

	
	/**
	 * Заблокировать FormData.
	 * @param formDataId - идентификатор налоговой формы
	 * @param userInfo информация о пользователе
	 * @return true - если удалось заблокировать налоговую форму, иначе - false
	 * */
	void lock(long formDataId, TAUserInfo userInfo);
	
	/**
	 * Снять блокировку с FormData.
	 * @param formDataId - идентификатор налоговой формы
	 * @param userInfo информация о пользователе
	 * @return true - если удалось разблокировать налоговую форму, иначе - false
	 * */
	void unlock(long formDataId, TAUserInfo userInfo);

	/**
	 * Снять все блокировки с FormData для пользователя
	 * @param userInfo информация о пользователе
	 * @return true - если удалось разблокировать, иначе - false
	 */
	@Deprecated
	boolean unlockAllByUser(TAUserInfo userInfo);

	/**
	 * Получить информацию о состоянии блокировки налоговой формы.
	 * @param formDataId - идентификатор налоговой формы
	 * @return информацию о блокировке объекта
	 */
	ObjectLock<Long> getObjectLock(long formDataId, TAUserInfo userInfo);
}
