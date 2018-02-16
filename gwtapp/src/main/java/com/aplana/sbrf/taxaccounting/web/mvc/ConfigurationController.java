package com.aplana.sbrf.taxaccounting.web.mvc;


import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.permissions.ConfigurationPermissionSetter;
import com.aplana.sbrf.taxaccounting.permissions.Permission;
import com.aplana.sbrf.taxaccounting.service.ConfigurationService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * Контроллер для работы с настройками конфигурационных параметров
 */

@RestController
public class ConfigurationController {

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ConfigurationPermissionSetter configurationPermissionSetter;

    /**
     * Привязка данных из параметров запроса
     *
     * @param binder спец. DataBinder для привязки
     */
    @InitBinder
    public void init(ServletRequestDataBinder binder) {
        binder.registerCustomEditor(PagingParams.class, new RequestParamEditor(PagingParams.class));
        binder.registerCustomEditor(Configuration.class, new RequestParamEditor(Configuration.class));
        binder.registerCustomEditor(AsyncTaskTypeData.class, new RequestParamEditor(AsyncTaskTypeData.class));
    }

    /**
     * Получение страницы с данными о типах асинхронных задач
     *
     * @param pagingParams параметры пагинации
     * @return страница {@link JqgridPagedList} с данными {@link AsyncTaskTypeData}
     */
    @GetMapping(value = "rest/configuration/asyncParam")
    public JqgridPagedList<AsyncTaskTypeData> fetchAllAsyncParam(@RequestParam PagingParams pagingParams) {
        PagingResult<AsyncTaskTypeData> data = configurationService.fetchAllAsyncParam(pagingParams);
        return JqgridPagedResourceAssembler.buildPagedList(
                data,
                data.getTotalCount(),
                pagingParams
        );
    }

    /**
     * Получение страницы с данными конфигураций "Администрирование - Конфигурационные параметры - Общие параметры"
     *
     * @param pagingParams параметры пагинации
     * @return страница {@link JqgridPagedList} с данными {@link Configuration}
     */
    @GetMapping(value = "rest/configuration/commonParam", params = "projection=adminCommonParam")
    public JqgridPagedList<Configuration> fetchAllCommonParam(@RequestParam PagingParams pagingParams) {
        PagingResult<Configuration> data = configurationService.fetchAllCommonParam(pagingParams, ConfigurationParamGroup.COMMON);
        setCommonConfigParamPermission(data);
        return JqgridPagedResourceAssembler.buildPagedList(
                data,
                data.getTotalCount(),
                pagingParams
        );
    }

    /**
     * Получение списка с данными конфигураций "Налоги - Общие параметры"
     *
     * @return список {@link Configuration}
     */
    @GetMapping(value = "rest/configuration/commonParam" , params = "projection=taxesCommonParam")
    public JqgridPagedList<Configuration> fetchCommonParams(@RequestParam PagingParams pagingParams) {
        PagingResult<Configuration> data = configurationService.fetchAllCommonParam(pagingParams, ConfigurationParamGroup.COMMON_PARAM);
        setCommonConfigParamPermission(data);
        return JqgridPagedResourceAssembler.buildPagedList(
                data,
                data.getTotalCount(),
                pagingParams
        );
    }

    /**
     * Полученние зачений параметров "Администрирование - Конфигурационные параметры - Общие параметры", для которых не указано значеие
     *
     * @return страница {@link JqgridPagedList} с данными {@link Configuration}
     */
    @GetMapping(value = "rest/configuration/commonParam", params = "projection=selectNonChanged")
    public JqgridPagedList<Configuration> fetchAllNonChangedCommonParam(@RequestParam PagingParams pagingParams) {
        PagingResult<Configuration> result = configurationService.fetchAllNonChangedCommonParam(pagingParams);
        return JqgridPagedResourceAssembler.buildPagedList(result, result.getTotalCount(), pagingParams);
    }

    /**
     * Получение информации о доступе для чтения и записи для конфигурационого параметра
     *
     * @param param конфигурационный параметр
     * @return uuid идентификатор логгера
     */
    @PostMapping(value = "action/configuration/commonParam", params = "projection=checkReadWriteAccess")
    public String checkReadWriteAccess(@RequestParam Configuration param) {
        return configurationService.checkReadWriteAccess(securityService.currentUserInfo(), param);
    }

    /**
     * Создание нового значения конфигурационного параметра
     *
     * @param commonParam общий параметр
     * @return uuid идентификатор логгера
     */
    @PostMapping(value = "action/configuration/commonParam/create")
    public String create(@RequestParam Configuration commonParam) {
        return configurationService.create(commonParam, securityService.currentUserInfo());
    }

    /**
     * Удаление параметров конфигураций
     *
     * @param names список наименований удаляемых конфигурационных параметров
     * @return uuid идентификатор логгера
     */
    @PostMapping(value = "action/configuration/remove")
    public String remove(@RequestBody List<String> names) {
        return configurationService.remove(names, securityService.currentUserInfo()
        );
    }

    /**
     * Редактирование параметров конфигураций
     *
     * @param commonParam конфигурационные параметры "Общие параметры"
     * @return uuid идентификатор логгера
     */
    @PostMapping(value = "action/configuration/commonParam/update")
    public String updateCommonParam(@RequestParam Configuration commonParam) {
        return configurationService.updateCommonParam(commonParam, securityService.currentUserInfo());
    }

    /**
     * Редактирование параметров конфигураций
     *
     * @param asyncParam конфигурационные параметры "Параметры асинхронных задач"
     * @return uuid идентификатор логгера
     */
    @PostMapping(value = "action/configuration/asyncParam/update")
    public String updateAsyncParam(@RequestParam AsyncTaskTypeData asyncParam) {
        return configurationService.updateAsyncParam(asyncParam, securityService.currentUserInfo());
    }

    /**
     * Установка значений общих параметров по умолчанию
     */
    @PostMapping(value = "/actions/configuration/commonParams/changeToDefaultCommonParams")
    public void setCommonParamsDefault() {
        TAUserInfo userInfo = securityService.currentUserInfo();
        configurationService.setCommonParamsDefault(userInfo);
    }

    /**
     * Устанавливаются права доступа для конфигураций "Общие параметры" через {@link Permission}
     *
     * @param params список параметров
     */
    private void setCommonConfigParamPermission(List<Configuration> params) {
        for (Configuration param : params) {
            configurationPermissionSetter.setPermissions(param, null);
        }
    }
}
