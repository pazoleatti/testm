package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.*;

import java.io.InputStream;
import java.util.*;

/**
 * Dao для работы с {@link DeclarationTemplate шаблонами деклараций}
 *
 * @author dsultanbekov
 */
public interface DeclarationTemplateDao extends PermissionDao {

    /**
     * Получить полный список всех деклараций
     *
     * @return список всех DeclarationTemplate
     */
    List<DeclarationTemplate> listAll();

    /**
     * Получить шаблон декларации (без тела скрипта).
     * Скрипт получается с помощью метода {@link #getDeclarationTemplateScript(int)}
     *
     * @param declarationTemplateId идентификатор шаблона декларации
     * @return объект шаблона декларации
     * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если декларации с таким id не существует
     */
    DeclarationTemplate get(int declarationTemplateId);

    /**
     * Возвращает идентификатор действующего {@link com.aplana.sbrf.taxaccounting.model.DeclarationTemplate описания декларации} по виду декларации
     * Такое описание для каждого вида декларации в любой отчетном периоде может быть только одно
     *
     * @param declarationTypeId идентификатор вида декларации
     * @return идентификатор описания декларации
     * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если не удалось найти активное описание декларации по заданному типу,
     *                                                                    или если обнаружено несколько действуюшие описаний по данному виду декларации
     */
    int getActiveDeclarationTemplateId(int declarationTypeId, int reportPeriodId);

    /**
     * Сохранить шаблон декларации.
     * Если сохраняется новый объект, то у него должен быть пустой id (id == null), в этом случае он будет сгенерирован
     *
     * @param declarationTemplate объект шаблона декларации
     * @return идентификатор сохранённой записи в БД
     */
    int save(DeclarationTemplate declarationTemplate);

    /**
     * Создание новой версии макета декларации
     *
     * @param declarationTemplate версия макета
     * @return идентификатор
     */
    int create(DeclarationTemplate declarationTemplate);

    /**
     * Обновление данных версий макетов.
     * Батч апдейт.
     *
     * @param declarationTemplates объект шаблона декларации
     * @return массив успешных апдейтов обновленных версий (0 - неуспешный, 1 - успешный)
     */
    int[] update(List<DeclarationTemplate> declarationTemplates);

    /**
     * Задать Jrxml-файла
     *
     * @param declarationTemplateId идентификатор шаблона декларации
     * @param jrxmlBlobId           идентификатор бинарного представления шаблона
     * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если не существует шаблона декларации с таким id
     */
    void setJrxml(int declarationTemplateId, String jrxmlBlobId);

    /**
     * Получение тела скрипта.
     *
     * @param declarationTemplateId идентификатор макета
     * @return тело скрипта
     */
    String getDeclarationTemplateScript(int declarationTemplateId);

    /**
     * Получить список идентификаторов макетов деклараций по фильтру
     *
     * @param filter фильтр
     * @return список отфильтрованых идентификаторов
     */
    List<Integer> getByFilter(TemplateFilter filter);

    /**
     * получить все идентификаторы шаблонов деклараций
     *
     * @return список всех идентификаторов
     */
    List<Integer> listAllId();

    /**
     * Возвращяет список актуальных макетов по типу макета
     *
     * @param declarationTypeId ид типа макета
     * @return список DeclarationTemplate
     */
    List<DeclarationTemplate> fetchAllByType(int declarationTypeId);

    /**
     * Получает список id версий макета по типу шаблона и статусу версии.
     *
     * @param decTypeId     вид шаблона
     * @param decTemplateId идентификатор шаблона, котрый исключить из поиска, если нет такого то 0
     * @param statusList    статус формы
     * @return список версий
     */
    List<Integer> getDeclarationTemplateVersions(int decTypeId, int decTemplateId, List<Integer> statusList, Date actualStartVersion, Date actualEndVersion);

    /**
     * Метод для поиска пересечений версий макетов в указанных датах
     *
     * @param formTypeId         вид шаблона
     * @param formTemplateId     дентификатор шаблона, который исключить из поиска, если нет такого то 0
     * @param actualStartVersion дата начала
     * @param actualEndVersion   дата окончания
     * @return список пеересечений
     */
    List<VersionSegment> findFTVersionIntersections(int formTypeId, int formTemplateId, Date actualStartVersion, Date actualEndVersion);

    /**
     * Поиск даты окончания версии макета, которая находится следующей по дате(т.е. "справа") от данной версии
     *
     * @param typeId             идентификатор вида налога
     * @param actualBeginVersion дата актуализации версии, для которой ведем поиск
     * @return дата окончания
     */
    Date getDTVersionEndDate(int typeId, Date actualBeginVersion);

    /**
     * Поиск версии макета, которая находится следующей по дате(т.е. "справа") от данной версии
     *
     * @param typeId             идентификатор вида налога
     * @param statusList         список статусов макатеов, которые искать
     * @param actualBeginVersion дата актуализации версии, для которой ведем поиск
     * @return идентификатор "правой" версии макета
     */
    int getNearestDTVersionIdRight(int typeId, List<Integer> statusList, Date actualBeginVersion);

    /**
     * Удаляет версию шаблона.
     * По идее удалять полностью только фейковые версии шаблонов.
     *
     * @param declarationTemplateId идентификатор макета
     * @return удаленный идентификатор макета
     */
    int delete(int declarationTemplateId);

    /**
     * Удаляет версии шаблонов.
     *
     * @param templateIds идентификатор макета
     */
    void delete(Collection<Integer> templateIds);

    /**
     * Количество весий для вида шаблона
     *
     * @param decTypeId  вид шаблона
     * @param statusList статусы
     * @return количество
     */
    int versionTemplateCount(int decTypeId, List<Integer> statusList);

    /**
     * Количество активных версий для вида шаблона
     *
     * @param typeIds вид шаблона
     * @return количество
     */
    List<Map<String, Object>> versionTemplateCountByType(Collection<Integer> typeIds);

    int updateVersionStatus(VersionedObjectStatus versionStatus, int decTemplateId);

    /**
     * Проверка существования активного шаблона декларации
     * с типом declarationTypeId и датой актуальности которой является период включающий
     * reportPeriodId
     *
     * @param declarationTypeId вид шаблона
     * @param reportPeriodId    отчетный период
     * @return
     */
    boolean existDeclarationTemplate(int declarationTypeId, int reportPeriodId);

    void deleteXsd(int dtId);

    void deleteJrxml(int dtId);

    /**
     * Получает макет декларации по типу и году (версии)
     *
     * @param declarationTypeId
     * @param year
     * @return идентификатор макета, либо null, если он не найден
     */
    Integer get(int declarationTypeId, int year);

    /**
     * Обновляет скрипт макета
     *
     * @param declarationTemplateId идентификатор макета
     * @param script                скрипт
     */
    void updateScript(int declarationTemplateId, String script);

    /**
     * Удаляет из макета файлы DECLARATION_TEMPLATE_FILE
     *
     * @param declarationTemplateId идентификатор макета
     * @param blobDataIds           идентификатор файла
     */
    void deleteTemplateFile(int declarationTemplateId, List<String> blobDataIds);

    /**
     * Добавляет к макету файлы DECLARATION_TEMPLATE_FILE
     *
     * @param declarationTemplateId идентификатор макета
     * @param blobDataIds           идентификатор файла
     */
    void createTemplateFile(int declarationTemplateId, List<String> blobDataIds);

    /**
     * Получает файл из макета по его имени
     * @param declarationTemplateId идентификатор макета
     * @param fileName название файла, приложенного к макету
     * @return содержимое файла
     */
    InputStream getTemplateFileContent(int declarationTemplateId, String fileName);

    /**
     * Возвращает признак фатальности проверки внутри формы по ее коду
     *
     * @param code       код проверки
     * @param templateId идентификатор макета
     * @return ошибка фатальна?
     */
    boolean isCheckFatal(DeclarationCheckCode code, int templateId);

    /**
     * Возвращает список проверок формы. Если идентификатор макета не указан, то возвращаются дефолтные проверки для типа формы
     *
     * @param declarationTypeId     идентификатор типа формы, к которому привязаны проверки по-умолчанию
     * @param declarationTemplateId идентификатор шаблона, к которому привязаны проверки
     * @return список проверок
     */
    List<DeclarationTemplateCheck> getChecks(int declarationTypeId, Integer declarationTemplateId);

    /**
     * Создает новые проверки для макета
     *
     * @param checks                список проверок
     * @param declarationTemplateId идентификатор макета
     */
    void createChecks(List<DeclarationTemplateCheck> checks, Integer declarationTemplateId);

    /**
     * Изменяет существующие проверки
     *
     * @param checks                список проверок
     * @param declarationTemplateId идентификатор макета
     */
    void updateChecks(List<DeclarationTemplateCheck> checks, Integer declarationTemplateId);

    /**
     * Поиск ID блоба содержащего XSD файл макета по ID макета
     *
     * @param declarationTemplateId ID макета
     * @return ID блоба содержащего XSD файл макета
     */
    String findXsdIdByTemplateId(Integer declarationTemplateId);
}