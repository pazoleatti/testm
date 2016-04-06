package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Сервис для работы с шаблонами деклараций 
 * @author dsultanbekov
  */
public interface DeclarationTemplateService {
	/**
	 * Получить полный список всех деклараций
	 * @return список всех DeclarationTemplate
	 */
	List<DeclarationTemplate> listAll();
	/**
	 * Получить шаблон декларации
	 * @param declarationTemplateId идентификатор шаблона декларации
	 * @return объект шаблона декларации
	 */
	DeclarationTemplate get(int declarationTemplateId);
	/**
	 * Сохранить шаблон декларации.
	 * Если сохраняется новый объект, то у него должен быть пустой id (id == null), в этом случае он будет сгенерирован
     * Производится очистка blob_data, в случае если значение идентификаторов было измененно по сравнению с базой.
	 * @param declarationTemplate объект шаблона декларации
	 * @return идентификатор сохранённой записи в БД
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException если у пользователя нет прав на изменение шаблона декларации
	 */
	int save(DeclarationTemplate declarationTemplate);

    /**
     * Изменение информации о версиях шаблонов
     * @param declarationTemplates шаблоны
     */
    void update(List<DeclarationTemplate> declarationTemplates);
	/**
	 * Возвращает идентификатор действующего {@link DeclarationTemplate описания декларации} по виду декларации
	 * Такое описание для каждого вида декларации в любой момент времени может быть только одно
	 * @param declarationTypeId идентификатор вида декларации
	 * @return идентификатор описания декларации
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если не удалось найти активное описание декларации по заданному типу,
	 * 	или если обнаружено несколько действуюшие описаний по данному виду декларации
	 */
	int getActiveDeclarationTemplateId(int declarationTypeId, int reportPeriodId);

	/**
	 * Получить шаблон Jrxml-файла
	 * @param declarationTemplateId идентификатор вида декларации
	 * @return jrxml-файл в виде строки
	 */
	String getJrxml(int declarationTemplateId);
	
	/**
	 * Снять блокировку с declarationTemplate.
	 * @param declarationTemplateId - идентификатор шаблона налоговой формы
	 * @param userInfo - информация о пользователе
	 * @return true - если удалось разблокировать форму декларации, иначе - false
	 * */
	boolean unlock(int declarationTemplateId, TAUserInfo userInfo);

	/**
	 * Блокировка declarationTemplate.
	 * @param declarationTemplateId - идентификатор налоговой формы
	 * @param userInfo - информация о пользователе
	 * @return информацию о блокировке объекта
	 */
	boolean lock(int declarationTemplateId, TAUserInfo userInfo);

	/**
	 * Проверяет, не заблокирован ли шаблон декларации другим пользователем
	 * @param declarationTemplateId идентификатор вида декларации
	 * @param userInfo - информация о пользователе
	 */
	void checkLockedByAnotherUser(Integer declarationTemplateId, TAUserInfo userInfo);

    /**
     * Получить информацию о состоянии блокировки шаблона декларации
     * @param declarationTemplateId - идентификатор вида декларации
     * @return информацию о блокировке объекта
     */
    LockData getObjectLock(final Integer declarationTemplateId, final TAUserInfo userInfo);

    /**
     * Получение тела скрипта.
     * @param declarationTemplateId идентификатор вида декларации
     * @return тело скрипта
     */
    String getDeclarationTemplateScript(int declarationTemplateId);

    /**
     * Получить список шаблонов деклараций по фильтру
     * @param filter фильтр
     * @return отфильтрованый список шаблонов
     */
    List<DeclarationTemplate> getByFilter(TemplateFilter filter);

    /**
     * Получить версии макетов деклараций с определеннным статусом
     * @param formTypeId тип налоговой формы
     * @param status статус версии макета НФ
     * @return список версий налоговых форм
     */
    List<DeclarationTemplate> getDecTemplateVersionsByStatus(int formTypeId, VersionedObjectStatus... status);

    /**
     * Получить идентификаторы версии макетов деклараций с определеннным статусом
     * @param formTypeId тип налоговой формы
     * @param status статус версии макета НФ
     * @return список версий налоговых форм
     */
    List<Integer> getDTVersionIdsByStatus(int formTypeId, VersionedObjectStatus... status);

    List<VersionSegment> findFTVersionIntersections(int templateId, int typeId, Date actualBeginVersion, Date actualEndVersion);

    /**
     * Удаление макета.
     * Макеты со статусом фиктивной версии удаляются, с остальными статусами помечаются как удаленные
     * @param declarationTemplateId идентификатор версия декларации
     * @return удаленный идентфикатор
     */
    int delete(int declarationTemplateId);

    /**
     * Удаление макетов.
     * Макеты со статусом фиктивной версии удаляются, с остальными статусами помечаются как удаленные
     * @param templateIds макет для удаления
     */
    void delete(@NotNull Collection<Integer> templateIds);

    /**
     * Возвращает версию макета ближайшую к данной спрвва.
     * @param declarationTemplateId идентификатор версия макета
     * @param status статус
     * @return ближайшая правее
     */
    DeclarationTemplate getNearestDTRight(int declarationTemplateId, VersionedObjectStatus... status);

    /**
     * Получает дату окончания макета. Расчет осуществляется путем поиска
     * версии макета ближайшей "справа" по дате.
     * @param declarationTemplateId идентификатор макета декларации
     * @return дата окончания актуальности макета
     */
    Date getDTEndDate(int declarationTemplateId);

    /**
     * Возвращает количество версий для вида шаблона
     * @param typeId идентификатор вида шаблона
     * @param status статусы
     * @return количество
     */
    int versionTemplateCount(int typeId, VersionedObjectStatus... status);

    /**
     * Возвращает количество активных версий для каждого переданного вида шаблона
     * @param formTypeIds вид шаблона
     * @return количество активных версий для id макета
     */
    Map<Long, Integer> versionTemplateCountByFormType(Collection<Integer> formTypeIds);

    /**
     * Обновленее статуса НФ
     * @param versionStatus статус
     * @param declarationTemplateId ижентификатор
     * @return идентифиактор
     */
    int updateVersionStatus(VersionedObjectStatus versionStatus, int declarationTemplateId);

    void deleteXsd(int dtId);

    void deleteJrxml(int dtId);

    /**
     * Проверка существования активного шаблона декларации
     * с типом declarationTypeId и датой актуальности которой является период включающий
     * reportPeriodId
     *
     * @param declarationTypeId вид шаблона
     * @param reportPeriodId отчетный период
     * @return true-если существует
     */
    boolean existDeclarationTemplate(int declarationTypeId, int reportPeriodId);

    /**
     * Поиск экземпляров деклараций использующих данную версию макета, которые имеют pdf/xlsx отчеты и/или
     * для которых созданы блокировки для задания формирования pdf/xlsx отчета
     * @param dtId идентификатор макета декларации
     * @param dataIds идентификторы деклараций использующих jrxml этого макета
     * @param lockDataIds идентификторы деклараций, для которых есть блокировка
     *                    (т.е. для них запущено создание), относящихся к этому макету
     * @return true - если есть декларации
     */
    boolean checkExistingDataJrxml(int dtId, Logger logger);

    /**
     * Получает идентификаторы деклараций, которые используют jrxml этого макета
     * @param dtId
     */
    Collection<Long> getDataIdsThatUseJrxml(int dtId, TAUserInfo userInfo);

    /**
     * Получает идентификаторы деклараций, для которых формируется отчет по этому jrxml
     * @param dtId
     */
    Collection<Long> getLockDataIdsThatUseJrxml(int dtId);

    /**
     * Получает макет декларации по типу и году (версии)
     * @param declarationTypeId
     * @param year
     * @return идентификатор макета, либо null, если он не найден
     */
    Integer get(int declarationTypeId, int year);

    /**
     * Обновляет скрипт макета
     * @param declarationTemplateId идентификатор макета
     * @param script скрипт
     */
    void updateScript(DeclarationTemplate declarationTemplate, Logger log, TAUserInfo userInfo);

    /**
     *
     * @param declarationTypeId
     * @param alias
     * @return
     */
    DeclarationSubreport getSubreportByAlias(int declarationTypeId, String alias);

    /**
     * Валидировать модель данных для описания декларации
     * @param declarationTemplate
     * @param logger
     */
    void validateDeclarationTemplate(DeclarationTemplate declarationTemplate, Logger logger);
}
