package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.action.UpdateTemplateStatusAction;
import com.aplana.sbrf.taxaccounting.model.action.UpdateTemplateAction;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.model.result.UpdateTemplateStatusResult;
import com.aplana.sbrf.taxaccounting.model.result.UpdateTemplateResult;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * Сервис для работы с шаблонами деклараций
 */
public interface DeclarationTemplateService {
    /**
     * Получить полный список всех деклараций
     *
     * @return список всех DeclarationTemplate
     */
    List<DeclarationTemplate> listAll();

    /**
     * Возвращяет макет декларации без скриптов
     *
     * @param declarationTemplateId идентификатор шаблона декларации
     * @return макет декларации без скриптов
     */
    DeclarationTemplate get(int declarationTemplateId);

    /**
     * Возвращяет макет декларации со скриптами
     *
     * @param declarationTemplateId идентификатор макета декларации
     * @return макет декларации вместе со скриптами
     */
    DeclarationTemplate fetchWithScripts(int declarationTemplateId);

    /**
     * Сохранить шаблон декларации.
     * Если сохраняется новый объект, то у него должен быть пустой id (id == null), в этом случае он будет сгенерирован
     * Производится очистка blob_data, в случае если значение идентификаторов было измененно по сравнению с базой.
     *
     * @param declarationTemplate объект шаблона декларации
     * @param userInfo            информация о пользователе
     * @return идентификатор сохранённой записи в БД
     * @throws com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException если у пользователя нет прав на изменение шаблона декларации
     */
    int save(DeclarationTemplate declarationTemplate, TAUserInfo userInfo);

    /**
     * Возвращает идентификатор действующего {@link DeclarationTemplate описания декларации} по виду декларации
     * Такое описание для каждого вида декларации в любой момент времени может быть только одно
     *
     * @param declarationTypeId идентификатор вида декларации
     * @return идентификатор описания декларации
     * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если не удалось найти активное описание декларации по заданному типу,
     *                                                                    или если обнаружено несколько действуюшие описаний по данному виду декларации
     */
    int getActiveDeclarationTemplateId(int declarationTypeId, int reportPeriodId);

    /**
     * Получить шаблон Jrxml-файла
     *
     * @param declarationTemplateId идентификатор вида декларации
     * @return jrxml-файл в виде строки
     */
    String getJrxml(int declarationTemplateId);

    /**
     * Получение тела скрипта.
     *
     * @param declarationTemplateId идентификатор вида декларации
     * @return тело скрипта
     */
    String getDeclarationTemplateScript(int declarationTemplateId);

    /**
     * Возвращяет список актуальных макетов по типу макета
     *
     * @param declarationTypeId ид типа макета
     * @param userInfo          пользователь запрашивающий данные
     * @return список DeclarationTemplate
     */
    List<DeclarationTemplate> fetchAllByType(int declarationTypeId, TAUserInfo userInfo);

    List<VersionSegment> findFTVersionIntersections(int templateId, int typeId, Date actualBeginVersion, Date actualEndVersion);

    /**
     * Удаление макета.
     * Макеты со статусом фиктивной версии удаляются, с остальными статусами помечаются как удаленные
     *
     * @param declarationTemplateId идентификатор версия декларации
     * @return удаленный идентфикатор
     */
    int delete(int declarationTemplateId);

    /**
     * Возвращает версию макета ближайшую к данной спрвва.
     *
     * @param declarationTemplateId идентификатор версия макета
     * @param status                статус
     * @return ближайшая правее
     */
    DeclarationTemplate getNearestDTRight(int declarationTemplateId, VersionedObjectStatus... status);

    /**
     * Получает дату окончания макета. Расчет осуществляется путем поиска
     * версии макета ближайшей "справа" по дате.
     *
     * @param declarationTemplateId идентификатор макета декларации
     * @return дата окончания актуальности макета
     */
    Date getDTEndDate(int declarationTemplateId);

    /**
     * Обновленее статуса НФ
     *
     * @param versionStatus         статус
     * @param declarationTemplateId ижентификатор
     * @return идентифиактор
     */
    int updateVersionStatus(VersionedObjectStatus versionStatus, int declarationTemplateId);

    /**
     * Получает макет декларации по типу и году (версии)
     *
     * @return идентификатор макета, либо null, если он не найден
     */
    Integer get(int declarationTypeId, int year);

    /**
     * Обновляет скрипт макета
     */
    void updateScript(DeclarationTemplate declarationTemplate, Logger log, TAUserInfo userInfo);

    DeclarationSubreport getSubreportByAlias(int declarationTypeId, String alias);

    /**
     * Валидировать модель данных для описания декларации
     */
    void validateDeclarationTemplate(DeclarationTemplate declarationTemplate, Logger logger);

    /**
     * Возвращает список проверок формы. Если идентификатор макета не указан, то возвращаются дефолтные проверки для типа формы
     *
     * @param declarationTypeId     идентификатор типа формы, к которому привязаны проверки по-умолчанию
     * @param declarationTemplateId идентификатор шаблона, к которому привязаны проверки
     * @return список проверок
     */
    List<DeclarationTemplateCheck> getChecks(int declarationTypeId, Integer declarationTemplateId);

    /**
     * Изменяет существующие проверки
     *
     * @param checks                список проверок
     * @param declarationTemplateId идентификатор макета
     */
    void updateChecks(List<DeclarationTemplateCheck> checks, Integer declarationTemplateId);

    /**
     * Изменяет макет
     * если у макета скрипты = null, то их не трогает
     *
     * @param action   параметры операции
     * @param userInfo пользователь
     * @return результат выполнения операции
     */
    UpdateTemplateResult update(UpdateTemplateAction action, TAUserInfo userInfo);

    /**
     * Изменяет статус макета (Вводит/Выводит из действия)
     *
     * @param action   параметры операции
     * @param userInfo пользователь
     * @return результат выполнения операции
     */
    UpdateTemplateStatusResult updateStatus(UpdateTemplateStatusAction action, TAUserInfo userInfo);

    /**
     * Загружает xsd файл макета без привязки к макету (это будет сделано при сохранении макета)
     *
     * @param declarationTemplateId идентификатор макета
     * @param inputStream           данные xsd
     * @param fileName              имя файла
     * @return uuid на загруженный файл
     */
    String uploadXsd(int declarationTemplateId, InputStream inputStream, String fileName);

    /**
     * Выгрузить xsd шаблон
     *
     * @param declarationTemplateId идентификатор макета
     * @return данные xsd файла
     */
    BlobData downloadXsd(int declarationTemplateId);

    /**
     * Экспорт скриптов, xsd и jrxml из макета в архив
     *
     * @param declarationTemplateId идентификатор макета
     * @param os                    поток, в который надо записать результирующий файл
     */
    void exportDeclarationTemplate(TAUserInfo userInfo, Integer declarationTemplateId, OutputStream os);

    /**
     * Импорт архива со скриптами, xsd и т.д в макет
     * Включает бизнес-проверки перед выполнением импорта
     *
     * @param declarationTemplateId идентификатор макета
     * @param fileData              содержимое архива
     */
    ActionResult importDeclarationTemplate(TAUserInfo userInfo, int declarationTemplateId, InputStream fileData);

    /**
     * Удаляет отчеты (pdf и xlsx) для форм, которые связаны с изменяемым jrxml-шаблоном
     *
     * @param declarationTemplateId идентификатор макета
     */
    void deleteJrxmlReports(TAUserInfo userInfo, int declarationTemplateId);

    /**
     * Экспорт скриптов, xsd и jrxml из всех макетов в архив
     *
     * @param os поток, в который надо записать результирующий файл
     */
    void exportAllDeclarationTemplates(TAUserInfo userInfo, OutputStream os);
}
