package com.aplana.sbrf.taxaccounting.service.refbook;

import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.KppSelect;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.action.DepartmentConfigsFilter;
import com.aplana.sbrf.taxaccounting.model.action.ImportDepartmentConfigsAction;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.DepartmentConfig;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecord;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.model.result.ImportDepartmentConfigsResult;
import com.aplana.sbrf.taxaccounting.service.ScriptExposed;

import java.util.List;

/**
 * Сервис для работы с настройками подразделениями
 */
@ScriptExposed
public interface DepartmentConfigService {
    /**
     * Возвращяет страницу настроек подразделений для отображения в GUI
     *
     * @param filter       объект содержащих данные используемые для фильтрации
     * @param pagingParams параметры пагиинации
     * @return список объектов содержащих данные о настройках подразделений
     */
    PagingResult<DepartmentConfig> fetchAllByFilter(DepartmentConfigsFilter filter, PagingParams pagingParams);

    /**
     * Возвращяет список настроек подразделений по ид подразделения
     *
     * @param departmentId ид подразделения
     * @return список настроек подразделений
     */
    List<DepartmentConfig> fetchAllByDepartmentId(int departmentId);

    /**
     * Возвращяет список настроек подразделений по КПП/ОКТМО
     *
     * @param kpp       КПП
     * @param oktmoCode код ОКТМО
     * @return список настроек подразделений
     */
    List<DepartmentConfig> fetchAllByKppAndOktmo(String kpp, String oktmoCode);

    /**
     * Возвращяет страницу из значений КПП тербанка по фильтру
     *
     * @param departmentId тербанк, из настроек которого будут браться КПП
     * @param kpp          фильтр поиска
     * @param pagingParams данные пагинатора
     * @return страница из значений КПП тербанка
     */
    PagingResult<KppSelect> findAllKppByDepartmentIdAndKpp(int departmentId, String kpp, PagingParams pagingParams);

    /**
     * Возвращяет кол-во записей настроек подразделений по фильтру
     *
     * @param filter фильтр
     * @return кол-во записей
     */
    int fetchCount(DepartmentConfigsFilter filter);

    /**
     * Создаёт запись настройки подразделений, используется в gui, обрабатывает всякие ошибки
     *
     * @param departmentConfig данные записи настроек подразделений
     */
    ActionResult create(DepartmentConfig departmentConfig, TAUserInfo user);

    /**
     * Изменяет запись настройки подразделений
     *
     * @param departmentConfig данные записи настроек подразделений
     */
    ActionResult update(DepartmentConfig departmentConfig, TAUserInfo user);

    /**
     * Удаляет запись настройки подразделений
     *
     * @param ids список id записи настроек подразделений
     */
    ActionResult delete(List<Long> ids, TAUserInfo user);

    /**
     * Удаляет настройки подразделений
     *
     * @param departmentConfigs удаляемые настройки подразделений
     * @param logger            логгер, нужен для универсального провайдера справочников
     */
    void delete(List<DepartmentConfig> departmentConfigs, Logger logger);

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

    /**
     * Преобразует настройку подразделений из модели {@link DepartmentConfig} в модель {@link RefBookRecord}, с которой работаю провайдеры справочников
     *
     * @param departmentConfig настройка подразделений
     * @return настройка подразделений в виде RefBookRecord
     */
    RefBookRecord convertToRefBookRecord(DepartmentConfig departmentConfig);
}
