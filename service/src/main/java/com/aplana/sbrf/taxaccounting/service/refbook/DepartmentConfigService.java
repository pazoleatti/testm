package com.aplana.sbrf.taxaccounting.service.refbook;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.action.DepartmentConfigsFilter;
import com.aplana.sbrf.taxaccounting.model.action.ImportDepartmentConfigsAction;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.DepartmentConfig;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.model.result.ImportDepartmentConfigsResult;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.ScriptExposed;

import java.util.List;

/**
 * Сервис для работы с настройками подразделениями
 */
@ScriptExposed
public interface DepartmentConfigService {

    /**
     * Возвращяет настройку подразделений по идентификатору, null если не найдена
     *
     * @param id идентификатор версии настройки подразделений
     * @return настройка подразделений
     */
    DepartmentConfig findById(long id);

    /**
     * Возвращяет список настроек подразделений по ид подразделения
     *
     * @param departmentId ид подразделения
     * @return список настроек подразделений
     */
    List<DepartmentConfig> findAllByDepartmentId(int departmentId);

    /**
     * Возвращяет пары КПП/ОКТМО формы, для которых существуют настройки подразделений по ТБ.
     * Настройки берутся только актуальные на текущую дату или которые пересекаются с периодом формы, но не переходят в другой ТБ в старших версиях
     *
     * @param declarationId  ид формы
     * @param departmentId   ид подразделений
     * @param reportPeriodId ид периода формы
     * @return пары КПП/ОКТМО
     */
    List<KppOktmoPair> findAllKppOktmoPairs(long declarationId, Integer departmentId, int reportPeriodId);

    List<KppOktmoPair> findAllKppOktmoPairs(Integer departmentId, int reportPeriodId);

    /**
     * Возвращяет страницу настроек подразделений для отображения в GUI
     *
     * @param filter       объект содержащих данные используемые для фильтрации
     * @param pagingParams параметры пагиинации
     * @return список объектов содержащих данные о настройках подразделений
     */
    PagingResult<DepartmentConfig> findPageByFilter(DepartmentConfigsFilter filter, PagingParams pagingParams);

    /**
     * Возвращяет все пары КПП/ОКТМО из формы и настройки подразделений, которые актуальны на текущую дату или пересекаются с периодом формы,
     * и связывает их по КПП/ОКТМО
     *
     * @param declaration НФ
     * @return список пар КПП/ОКТМО из формы и связанные с ними настройки подразделений
     */
    List<Pair<KppOktmoPair, DepartmentConfig>> findAllByDeclaration(DeclarationData declaration);

    /**
     * Возвращяет список настроек подразделений по КПП/ОКТМО
     *
     * @param kpp   КПП
     * @param oktmo код ОКТМО
     * @return список настроек подразделений
     */
    List<DepartmentConfig> findAllByKppAndOktmo(String kpp, String oktmo);

    /**
     * Возвращяет страницу из значений КПП тербанка по фильтру
     *
     * @param departmentId тербанк, из настроек которого будут браться КПП
     * @param kpp          фильтр поиска
     * @param pagingParams данные пагинатора
     * @return страница из значений КПП тербанка
     */
    PagingResult<KppSelect> findAllKppByDepartmentIdAndKppContaining(int departmentId, String kpp, PagingParams pagingParams);

    /**
     * Возвращяет пары КПП/ОКТМО
     *
     * @param filter       фильтр
     * @param pagingParams параметры пагинации и сортировки
     * @return страница пар КПП/ОКТМО
     */
    PagingResult<ReportFormCreationKppOktmoPair> findAllKppOktmoPairsByFilter(KppOktmoPairFilter filter, PagingParams pagingParams);

    /**
     * Возвращяет кол-во записей настроек подразделений по фильтру
     *
     * @param filter фильтр
     * @return кол-во записей
     */
    int countByFilter(DepartmentConfigsFilter filter);

    /**
     * Создаёт настройку подразделений с проверкой прав и дополнительной обработкой ошибок. Используется из GUI
     *
     * @param departmentConfig настройка подразделений
     * @return {@link ActionResult}
     */
    ActionResult createForGui(DepartmentConfig departmentConfig);

    /**
     * Создаёт настройку подразделений
     *
     * @param departmentConfig настройка подразделений
     * @param logger           логгер
     * @throws ServiceException если не прошла проверка перечений с существующими настройками той же пары КПП/ОКТМО
     */
    void create(DepartmentConfig departmentConfig, Logger logger);

    /**
     * Изменяет настройку подразделений с проверкой прав и дополнительной обработкой ошибок. Используется из GUI
     *
     * @param departmentConfig настройка подразделений
     * @return {@link ActionResult}
     */
    ActionResult updateForGui(DepartmentConfig departmentConfig);

    /**
     * Изменяет настройку подразделений
     *
     * @param departmentConfig настройка подразделений
     * @param logger           логгер
     * @throws ServiceException если не прошла проверка перечений с существующими настройками той же пары КПП/ОКТМО
     */
    void update(DepartmentConfig departmentConfig, Logger logger);

    /**
     * Удаляет настройки подразделений с проверкой прав и по отдельности, пропуская те, на которых произошла ошибка. Используется из GUI
     *
     * @param ids список id записи настроек подразделений
     * @return {@link ActionResult}
     */
    ActionResult deleteForGui(List<Long> ids);

    /**
     * Удаляет настройку подразделений
     *
     * @param departmentConfig настройка подразделений
     * @param logger           логгер
     */
    void delete(DepartmentConfig departmentConfig, Logger logger);

    /**
     * Удаляет настройки указанного подразделения
     *
     * @param departmentId ид подразделения
     */
    void deleteByDepartmentId(int departmentId);

    /**
     * Выполняет проверки возможности сохранения настройки подразделений
     *
     * @param departmentConfig         сохраняемая настройка подразделений
     * @param relatedDepartmentConfigs список настроек подразделений, относительно которых будут выполняться проверки
     * @throws ServiceException если проверка не прошла
     */
    void checkDepartmentConfig(DepartmentConfig departmentConfig, List<DepartmentConfig> relatedDepartmentConfigs);

    /**
     * Создает асинхронную задачу на формирование excel
     *
     * @param filter       фильтр
     * @param pagingParams параметры сортировки
     * @param userInfo     пользователь запустивший операцию
     * @return результат создания задачи
     */
    ActionResult createTaskToCreateExcel(DepartmentConfigsFilter filter, PagingParams pagingParams, TAUserInfo userInfo);

    /**
     * Создаёт задачу на загрузку данных настроек подразделений из excel файла
     *
     * @param action   параметры загрузки
     * @param userInfo пользователь, запустивший операцию
     * @return результат создания задачи
     */
    ImportDepartmentConfigsResult createTaskToImportExcel(ImportDepartmentConfigsAction action, TAUserInfo userInfo);

    /**
     * Выполняет загрузку данных настроек подразделений из excel файла
     *
     * @param departmentId ид подразделения настроек подразделений , куда будут загружены данные
     * @param blobData     данные файла
     * @param userInfo     пользователь запустивший задачу
     */
    void importExcel(int departmentId, BlobData blobData, TAUserInfo userInfo, Logger logger);
}
