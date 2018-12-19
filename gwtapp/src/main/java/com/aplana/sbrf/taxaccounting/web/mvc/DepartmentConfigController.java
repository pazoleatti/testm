package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import com.aplana.sbrf.taxaccounting.model.KppSelect;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.action.DepartmentConfigsFilter;
import com.aplana.sbrf.taxaccounting.model.action.ImportDepartmentConfigsAction;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.model.refbook.DepartmentConfig;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDepartment;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.model.result.ImportDepartmentConfigsResult;
import com.aplana.sbrf.taxaccounting.permissions.DepartmentConfigPermission;
import com.aplana.sbrf.taxaccounting.permissions.DepartmentConfigPermissionSetter;
import com.aplana.sbrf.taxaccounting.service.refbook.DepartmentConfigService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Контроллер для работы с настройками подразделений
 */
@RestController
public class DepartmentConfigController {

    @Autowired
    private DepartmentConfigService departmentConfigService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private DepartmentConfigPermissionSetter departmentConfigPermissionSetter;

    @InitBinder
    public void init(ServletRequestDataBinder binder) {
        binder.registerCustomEditor(PagingParams.class, new RequestParamEditor(PagingParams.class));
        binder.registerCustomEditor(DepartmentConfigsFilter.class, new RequestParamEditor(DepartmentConfigsFilter.class));
        binder.registerCustomEditor(DepartmentConfig.class, new RequestParamEditor(DepartmentConfig.class));
        binder.registerCustomEditor(RefBookDepartment.class, new RequestParamEditor(RefBookDepartment.class));
        binder.registerCustomEditor(DepartmentType.class, new RequestParamEditor(DepartmentType.class));
        binder.registerCustomEditor(ImportDepartmentConfigsAction.class, new RequestParamEditor(ImportDepartmentConfigsAction.class));
    }

    /**
     * Получает настройки подразделений для отображения
     *
     * @param filter       фильтр поиска
     * @param pagingParams параметры пагинации
     * @return возвращает список настроек подразделений
     */
    @GetMapping(value = "/rest/departmentConfig")
    public JqgridPagedList<DepartmentConfig> fetchDepartmentConfig(@RequestParam DepartmentConfigsFilter filter, @RequestParam PagingParams pagingParams) {
        if (filter.getDepartmentId() == null) {
            return new JqgridPagedList<>();
        }
        PagingResult<DepartmentConfig> result = departmentConfigService.fetchAllByFilter(filter, pagingParams);

        setDepartmentConfigsPermission(result);

        return JqgridPagedResourceAssembler.buildPagedList(
                result,
                result.getTotalCount(),
                pagingParams
        );
    }

    private void setDepartmentConfigsPermission(PagingResult<DepartmentConfig> departmentConfigs) {
        for (DepartmentConfig departmentConfig : departmentConfigs) {//noinspection unchecked
            departmentConfigPermissionSetter.setPermissions(departmentConfig,
                    DepartmentConfigPermission.UPDATE, DepartmentConfigPermission.DELETE);
        }
    }

    /**
     * Возвращяет страницу КПП, полученных из настроек Тербанка
     *
     * @param departmentId тербанк, из настроек которого будут браться КПП
     * @param kpp          значение поиска по КПП
     * @param pagingParams параметры пагинации
     * @return возвращает страницу из списка КПП, взятых из настроек подразделений
     */
    @GetMapping(value = "/rest/departmentConfig/kppSelect")
    public JqgridPagedList<KppSelect> findAllKppByDepartmentIdAndKpp(@RequestParam String kpp, @RequestParam int departmentId, @RequestParam PagingParams pagingParams) {
        PagingResult<KppSelect> result = departmentConfigService.findAllKppByDepartmentIdAndKpp(departmentId, kpp, pagingParams);

        return JqgridPagedResourceAssembler.buildPagedList(
                result,
                result.getTotalCount(),
                pagingParams
        );
    }

    /**
     * Создание записи настроек подразделений
     */
    @PostMapping(value = "/actions/departmentConfig/create")
    public ActionResult create(@RequestBody DepartmentConfig departmentConfig) {
        return departmentConfigService.create(departmentConfig, securityService.currentUserInfo());
    }

    /**
     * Создание записи настроек подразделений
     */
    @PostMapping(value = "/actions/departmentConfig/update")
    public ActionResult update(@RequestBody DepartmentConfig departmentConfig) {
        return departmentConfigService.update(departmentConfig, securityService.currentUserInfo());
    }

    /**
     * Создание записи настроек подразделений
     */
    @PostMapping(value = "/actions/departmentConfig/delete")
    public ActionResult delete(@RequestBody List<Long> ids) {
        return departmentConfigService.delete(ids, securityService.currentUserInfo());
    }

    /**
     * Формирование отчета excel
     */
    @PostMapping(value = "/actions/departmentConfig/export/excel")
    public ActionResult exportToExcel(@RequestParam DepartmentConfigsFilter filter, @RequestParam PagingParams pagingParams) {
        return departmentConfigService.createTaskToCreateExcel(filter, pagingParams, securityService.currentUserInfo());
    }

    /**
     * Загрузка данных из excel
     */
    @PostMapping(value = "/actions/departmentConfig/import")
    public ImportDepartmentConfigsResult importExcel(@RequestParam(value = "uploader") MultipartFile file,
                                                     @RequestParam ImportDepartmentConfigsAction action)
            throws IOException {
        if (file.isEmpty()) throw new ServiceException("Загружаемый файл пуст. Загружать в Систему пустые файлы настроек подразделений запрещено.");

        TAUserInfo userInfo = securityService.currentUserInfo();
        try (InputStream inputStream = file.getInputStream()) {
            action.setInputStream(inputStream);
            action.setFileName(file.getOriginalFilename());
            return departmentConfigService.createTaskToImportExcel(action, userInfo);
        }
    }
}
