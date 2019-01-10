package com.aplana.sbrf.taxaccounting.web.mvc;


import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * Контроллер для работы с асинхронными задачами
 *
 * @author dloshkarev
 */
@RestController
public class AsyncTaskController {

    private AsyncManager asyncManager;
    private SecurityService securityService;

    public AsyncTaskController(AsyncManager asyncManager, SecurityService securityService) {
        this.asyncManager = asyncManager;
        this.securityService = securityService;
    }

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
     * Получение страницы асинхронных задач {@link AsyncTaskDTO}
     *
     * @param pagingParams параметры пагинации
     * @return страница асинхронных задач
     */
    @GetMapping(value = "/rest/async")
    public JqgridPagedList<AsyncTaskDTO> fetchAsyncs(@RequestParam(required = false) String filter, @RequestParam PagingParams pagingParams) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        PagingResult<AsyncTaskDTO> tasks = asyncManager.getTasks(filter, pagingParams, userInfo);
        return JqgridPagedResourceAssembler.buildPagedList(
                tasks,
                tasks.getTotalCount(),
                pagingParams
        );
    }

    /**
     * Прерывание задач по идентификаторам
     *
     * @param ids идентификаторы асинхронных задач
     */
    @PostMapping(value = "/actions/async/interrupt")
    public void interruptTask(@RequestParam Long[] ids) {
        asyncManager.interruptAllTasks(Arrays.asList(ids), securityService.currentUserInfo(), TaskInterruptCause.CANCELLED);
    }
}