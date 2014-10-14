package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

import java.util.List;

/**
 * Интерфейс, реализующий логику по проверке прав доступа
 * @author dsultanbekov
 *
 */
public interface FormDataAccessService {
	/**
	 * Проверка прав на чтение данных
	 * @param userInfo информация о пользователе, для которого проверяются права
	 * @param formDataId идентификатор карточки данных по налоговой форме
	 */
	void canRead(TAUserInfo userInfo, long formDataId);

	/**
	 * Проверка прав на редактирование данных (права на чтение проверяется отдельно)
	 * @param userInfo идентификатор пользователя, для которого проверяются права
	 * @param formDataId идентификатор карточки данных по налоговой форме
     * @param manual признак версии ручного ввода
	 */	
	void canEdit(TAUserInfo userInfo, long formDataId, boolean manual);

    /**
     * Проверка разрешения пользователю создавать заданную НФ
     * @param userInfo Пользователь
     * @param formTemplateId Макет НФ
     * @param kind Тип НЫ
     * @param departmentReportPeriodId Отчетный период подразделения
     */
    void canCreate(TAUserInfo userInfo, int formTemplateId, FormDataKind kind, int departmentReportPeriodId);

    /**
     * Проверка того, что у пользователя есть права на создание версии ручного ввода
     * @param userInfo информация о пользователе
     * @param formDataId данные формы
     */
    void canCreateManual(Logger logger, TAUserInfo userInfo, long formDataId);

    /**
     * Проверка того, что у пользователя есть права на удаление карточки с данными налоговой формы
     * @param userInfo информация о пользователе
     * @param formDataId идентификатор карточки с данными формы
     */
    void canDelete(TAUserInfo userInfo, long formDataId);

    /**
     * Проверка того, что у пользователя есть права на удаление карточки с данными налоговой формы
     * @param userInfo информация о пользователе
     * @param formDataId идентификатор карточки с данными формы
     */
    void canDeleteManual(Logger logger, TAUserInfo userInfo, long formDataId);
	
	/**
	 * Получить список переходов, которые данный пользователь может выполнить над данным объектом {@link FormData}
	 * @param userInfo информация о пользователе
	 * @param formDataId идентификатор записи данных формы
	 * @return список переходов жизненного цикла, которые может выполнить текущий пользователь над данным объектом {@link FormData}
	 */
	List<WorkflowMove> getAvailableMoves(TAUserInfo userInfo, long formDataId);
	
	/**
	 * Получить объект, содержащий {@link FormDataAccessParams параметры доступа} пользователя к объекту {@link FormData}
	 * Если для одного и того же объекта FormData нужно получить значения нескольких флагов доступа, то
	 * использование этого метода более предпочтительно, так как его реализация более эффективно запрашивает данные
	 * из БД
	 * @param userInfo информация о пользователе
	 * @param formDataId идентификатор записи данных формы
	 * @return объект, содержащий информацию о правах доступа пользователя к данной налоговой форме
	 */
	FormDataAccessParams getFormDataAccessParams(TAUserInfo userInfo, long formDataId, boolean manual);

    /**
     * Список доступных типов НФ
     * @param userInfo Пользователь
     * @param taxTypes Виды нлога
     */
    List<FormDataKind> getAvailableFormDataKind(TAUserInfo userInfo, List<TaxType> taxTypes);

    /**
     * Проверка наличия и статуса приемника при осуществлении перевода формы
     * в статус "Подготовлена"/"Утверждена"/"Принята". Если проверка не пройдена, то выкидывается ServiceException
     * @param formDataId
     * @return true, если есть назначенная НФ-приемник или декларация-приемник
     */
    boolean checkDestinations(long formDataId);
}
