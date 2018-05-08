package com.aplana.sbrf.taxaccounting.web.mvc;


import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.permissions.ConfigurationPermissionSetter;
import com.aplana.sbrf.taxaccounting.permissions.Permission;
import com.aplana.sbrf.taxaccounting.service.ConfigurationService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * Контроллер для работы с настройками конфигурационных параметров
 */
@RestController
public class ConfigurationController {

    private ConfigurationService configurationService;

    private SecurityService securityService;

    private ConfigurationPermissionSetter configurationPermissionSetter;

    public ConfigurationController(ConfigurationService configurationService, SecurityService securityService, ConfigurationPermissionSetter configurationPermissionSetter) {
        this.configurationService = configurationService;
        this.securityService = securityService;
        this.configurationPermissionSetter = configurationPermissionSetter;
    }

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
    @GetMapping(value = "/rest/asyncParam")
    public JqgridPagedList<AsyncTaskTypeData> fetchAsyncParams(@RequestParam PagingParams pagingParams) {
        PagingResult<AsyncTaskTypeData> data = configurationService.fetchAsyncParams(pagingParams, securityService.currentUserInfo());
        return JqgridPagedResourceAssembler.buildPagedList(
                data,
                data.getTotalCount(),
                pagingParams
        );
    }

    /**
     * Получение страницы общих параметров для администрирования
     *
     * @param pagingParams параметры пагинации
     * @return страница {@link JqgridPagedList} с данными {@link Configuration}
     */
    @GetMapping(value = "/rest/commonParam", params = "projection=admin")
    public JqgridPagedList<Configuration> fetchAdminCommonParams(@RequestParam PagingParams pagingParams) {
        PagingResult<Configuration> data = configurationService.fetchCommonParams(pagingParams, ConfigurationParamGroup.COMMON, securityService.currentUserInfo());
        setCommonConfigParamPermission(data);
        return JqgridPagedResourceAssembler.buildPagedList(
                data,
                data.getTotalCount(),
                pagingParams
        );
    }

    /**
     * Получение страницы общих параметров для налогов
     *
     * @return страница {@link JqgridPagedList} с данными {@link Configuration}
     */
    @GetMapping(value = "/rest/commonParam", params = "projection=taxes")
    public JqgridPagedList<Configuration> fetchTaxesCommonParams(@RequestParam PagingParams pagingParams) {
        PagingResult<Configuration> data = configurationService.fetchCommonParams(pagingParams, ConfigurationParamGroup.COMMON_PARAM, securityService.currentUserInfo());
        setCommonConfigParamPermission(data);
        return JqgridPagedResourceAssembler.buildPagedList(
                data,
                data.getTotalCount(),
                pagingParams
        );
    }

    /**
     * Получение страницы параметров электронной почты
     *
     * @return страница {@link JqgridPagedList} с данными {@link Configuration}
     */
    @GetMapping(value = "/rest/emailParam")
    public JqgridPagedList<Configuration> fetchEmailConfigParams(@RequestParam PagingParams pagingParams){
        PagingResult<Configuration> data = configurationService.fetchEmailParams(pagingParams, securityService.currentUserInfo());
        setCommonConfigParamPermission(data);
        return JqgridPagedResourceAssembler.buildPagedList(
                data,
                data.getTotalCount(),
                pagingParams
        );
    }

    /**
     * Полученние страницы общих параметров администрирования, которые не созданы
     *
     * @return страница {@link JqgridPagedList} с данными {@link Configuration}
     */
    @GetMapping(value = "/rest/commonParam", params = "projection=notCreated")
    public JqgridPagedList<Configuration> fetchNonCreatedCommonParams(@RequestParam PagingParams pagingParams) {
        PagingResult<Configuration> result = configurationService.fetchNonCreatedCommonParams(pagingParams, securityService.currentUserInfo());
        return JqgridPagedResourceAssembler.buildPagedList(result, result.getTotalCount(), pagingParams);
    }

    /**
     * Проверка конфигурационного параметра, представляющего из себя путь в файловой системе,
     * на наличие доступа на чтение/запись в зависимости от сути параметра
     *
     * @param param конфигурационный параметр
     * @return uuid идентификатор логгера с результатом проверки
     */
    @PostMapping(value = "/actions/commonParam/checkFileSystemAccess")
    public String checkReadWriteAccess(@RequestParam Configuration param) {
        return configurationService.checkFileSystemAccess(param, securityService.currentUserInfo());
    }

    /**
     * Проверка конфигурационного параметра на валидность введенного значения и наличия доступа к пути в файловой системе
     *
     * @param param конфигурационный параметр
     * @return uuid идентификатор логгера с результатом проверки
     */
    @PostMapping(value = "/actions/commonParam/checkValidate")
    public String checkConfigParam(@RequestParam Configuration param) {
        return configurationService.checkConfigParam(param, securityService.currentUserInfo());
    }

    /**
     * Создание конфигурационного параметра
     *
     * @param commonParam общий параметр
     * @return uuid идентификатор логгера с результатом создания параметра
     */
    @PostMapping(value = "/rest/commonParam/create")
    public String create(@RequestParam Configuration commonParam) {
        return configurationService.create(commonParam, securityService.currentUserInfo());
    }

    /**
     * Удаление параметров конфигураций
     *
     * @param codes список кодов удаляемых конфигурационных параметров
     * @return uuid идентификатор логгера с результатом удаления параметра
     */
    @PostMapping(value = "/actions/commonParam/remove")
    public String remove(@RequestBody List<String> codes) {
        return configurationService.remove(codes, securityService.currentUserInfo()
        );
    }

    /**
     * Сохранение параметра конфигурации
     *
     * @param commonParam конфигурационный параметр
     * @return uuid идентификатор логгера с результатом сохранения параметра
     */
    @PostMapping(value = "/rest/commonParam/update")
    public String updateCommonParam(@RequestParam Configuration commonParam) {
        return configurationService.updateCommonParam(commonParam, securityService.currentUserInfo());
    }

    /**
     * Сохранение параметра конфигурации
     *
     * @param asyncParam конфигурационный параметр
     * @return uuid идентификатор логгера с результатом сохранения параметра
     */
    @PostMapping(value = "/rest/asyncParam/update")
    public String updateAsyncParam(@RequestParam AsyncTaskTypeData asyncParam) {
        return configurationService.updateAsyncParam(asyncParam, securityService.currentUserInfo());
    }

    /**
     * Сброс значений общих параметров к значениям по умолчанию
     */
    @PostMapping(value = "/actions/resetCommonParams")
    public void resetCommonParams() {
        TAUserInfo userInfo = securityService.currentUserInfo();
        configurationService.resetCommonParams(userInfo);
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
