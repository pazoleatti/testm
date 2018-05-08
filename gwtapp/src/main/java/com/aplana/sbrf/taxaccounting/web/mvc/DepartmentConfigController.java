package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.action.DepartmentConfigFetchingAction;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.model.result.DepartmentConfigFetchingResult;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.refbook.DepartmentConfigService;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Контроллер для работы с настройками подразделений
 */
@RestController
public class DepartmentConfigController {

    private DepartmentConfigService departmentConfigService;

    public DepartmentConfigController(DepartmentConfigService departmentConfigService) {
        this.departmentConfigService = departmentConfigService;
    }

    @InitBinder
    public void init(ServletRequestDataBinder binder) {
        binder.registerCustomEditor(PagingParams.class, new RequestParamEditor(PagingParams.class));
        binder.registerCustomEditor(DepartmentConfigFetchingAction.class, new RequestParamEditor(DepartmentConfigFetchingAction.class));
    }

    /**
     * Получает настройки подразделений для отображения
     * @param filter        фильтр поиска
     * @param pagingParams  параметры пагинации
     * @return  возвращает список настроек подразделений
     */
    @GetMapping(value = "/rest/departmentConfig")
    public JqgridPagedList<DepartmentConfigFetchingResult> fetchDepartmentConfig(@RequestParam DepartmentConfigFetchingAction filter, @RequestParam PagingParams pagingParams) {
        if (filter.getDepartmentId() == null || filter.getReportPeriodId() == null) {
            return new JqgridPagedList<>();
        }
        List<DepartmentConfigFetchingResult> resultDataList = departmentConfigService.fetchDepartmentConfigs(filter);
        PagingResult<DepartmentConfigFetchingResult> result = new PagingResult<>(resultDataList, resultDataList.size());
        return JqgridPagedResourceAssembler.buildPagedList(
                result,
                result.size(),
                pagingParams
        );
    }
}
