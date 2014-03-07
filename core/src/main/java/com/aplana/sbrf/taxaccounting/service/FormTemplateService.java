package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

import java.util.Date;
import java.util.List;

/**
 * Сервис для работы с шаблонами налоговых форм. В первую очередь предназначен для использования в админке
 * @author dsultanbekov
 */
public interface FormTemplateService {
	/**
	 * Получить полный список всех описаний налоговых форм
	 * (Внимание, объекты в результирующей коллекции могут быть только частично инициализированы,
	 * в них может остаться незаполненной информация по столбцам, скрипта и т.д.) 
	 * @return список всех FormTemplate
	 */
	List<FormTemplate> listAll();
	/**
	 * Получить макет налоговой формы (без скрипта). Для получения скрипта использовать {@link #getFormTemplateScript(int)}
	 * @param formTemplateId идентификатор макета
	 * @return объект, представляющий описание налоговой формы
	 */
	FormTemplate get(int formTemplateId);
	/**
	 * Валидировать модель данных для описания налоговой формы
	 * @param formTemplate объект, содержащий описание налоговой формы
	 * @param logger объект, для ведения логов
	 */
	void validateFormTemplate(FormTemplate formTemplate, Logger logger);
	/**
	 * Сохранить описание налоговой формы
	 * @param formTemplate объект, содержащий описание налоговой формы
	 * @return идентификатор сохранённой записи
	 */
	int save(FormTemplate formTemplate);

    /**
     * Возвращает идентификатор действующего {@link FormTemplate описания налоговой формы} по виду налоговой формы
     * Такое описание для каждого вида формы в аанном отчетном пеииоде может быть только одно
     * @param formTypeId идентификатор вида налоговой формы
     * @param reportPeriodId идентификатор отчетного периода
     * @return идентификатор описания налоговой формы
     * @throws DaoException если не удалось найти активное описание налоговой формы по заданному типу,
     * 	или если обнаружено несколько действуюшие описаний по данному виду формы
     */
    int getActiveFormTemplateId(int formTypeId, int reportPeriodId);

	/**
	 * Снять блокировку с formTemplate.
	 * @param formTemplateId - идентификатор шаблона налоговой формы
	 * @param userInfo - информация о пользователе
	 * @return true - если удалось разблокировать налоговую форму, иначе - false
	 * */
	boolean unlock(int formTemplateId,  TAUserInfo userInfo);

	/**
	 * Блокировка formTemplate.
	 * @param formTemplateId - идентификатор налоговой формы
	 * @param userInfo - информация о пользователе
	 * @return информацию о блокировке объекта
	 */
	boolean lock(int formTemplateId,  TAUserInfo userInfo);

	/**
	 * Проверяет, не заблокирован ли шаблон формы другим пользователем
	 * @param formTemplateId - идентификатор налоговой формы
	 * @param userInfo - информация о пользователе
	 */
	void checkLockedByAnotherUser(Integer formTemplateId, TAUserInfo userInfo);

    /**
     * Исполяет для теста написанный скрипт от имени пользователя controlUnp
     * @param formTemplate - шаблон налоговой формы
     */
    void executeTestScript(FormTemplate formTemplate);

    /**
     * Получение скрипта для {@link FormTemplate}.
     * @param formTemplateId - идентификатор налоговой формы
     * @return тело скрипта
     */
    String getFormTemplateScript(int formTemplateId);

    /**
     * Получить макет налоговой формы.
     * @param formTemplateId идентификатор макета
     * @return объект, представляющий полное описание налоговой формы
     */
    FormTemplate getFullFormTemplate(int formTemplateId);

    /**
     * Получить макеты налоговых форм по фильтру
     * @param filter фильтр
     * @return отфильтрованный список налоговых форм
     */
    List<FormTemplate> getByFilter(TemplateFilter filter);

    /**
     * Получить версии макетов налоговых форм с определеннным статусом
     * @param formTypeId тип налоговой формы
     * @param status статус версии макета НФ
     * @return список версий налоговых форм
     */
    List<FormTemplate> getFormTemplateVersionsByStatus(int formTypeId, VersionedObjectStatus... status);

    List<SegmentIntersection> findFTVersionIntersections(FormTemplate formTemplate, Date actualEndVersion, VersionedObjectStatus... status);

    /**
     * Удаление макета.
     * Макеты со статусом фиктивной версии удаляются, с остальными статусами помечаются как удаленные
     * @param formTemplate макет для удаления
     * @return идентификатор удаленного объекта
     */
    int delete(FormTemplate formTemplate);

    /**
     * Возвращает версию макета ближайшую к данной спрвва.
     * @param formTemplateId идентификатор версии макета
     * @param status статус
     * @return ближайшая правее
     */
    FormTemplate getNearestFTRight(int formTemplateId, VersionedObjectStatus... status);

    Date getFTEndDate(int formTemplateId);

    /**
     * Возвращает количество версий для вида шаблона
     * @param formTypeId вид шаблона
     * @param status статусы
     * @return количество
     */
    int versionTemplateCount(int formTypeId, VersionedObjectStatus... status);

    /**
     * Является ли форма ежемесячной.
     * @param formId идентификатор формы
     * @return
     */
    boolean isMonthly(int formId);

    int updateVersionStatus(int versionStatus, int formTemplateId);
}
