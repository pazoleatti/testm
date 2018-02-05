package com.aplana.sbrf.taxaccounting.web.mvc;


import com.aplana.sbrf.taxaccounting.model.AsyncTaskTypeData;
import com.aplana.sbrf.taxaccounting.model.Configuration;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.service.ConfigurationService;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Контроллер для работы с настройками конфигурационных параметров
 */

@RestController
public class ConfigurationController {

    @Autowired
    private ConfigurationService configurationService;

    /**
     * Привязка данных из параметров запроса
     *
     * @param binder спец. DataBinder для привязки
     */
    @InitBinder
    public void init(ServletRequestDataBinder binder) {
        binder.registerCustomEditor(PagingParams.class, new RequestParamEditor(PagingParams.class));
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

    @GetMapping(value = "rest/configuration/commonParam")
    public JqgridPagedList<Configuration> fetchAllCommonParam(@RequestParam PagingParams pagingParams) {
        PagingResult<Configuration> data = configurationService.fetchAllCommonParam(pagingParams);
        return JqgridPagedResourceAssembler.buildPagedList(
                data,
                data.getTotalCount(),
                pagingParams
        );
    }


}
