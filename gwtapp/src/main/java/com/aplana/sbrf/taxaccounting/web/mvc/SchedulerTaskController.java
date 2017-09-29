package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TaskSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.service.api.SchedulerTaskService;
import com.aplana.sbrf.taxaccounting.service.scheduler.SchedulerService;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Контроллер для работы со списком задач по расписанию (Администрирование -> Планировщик задач)
 */

@RestController
public class SchedulerTaskController {

    private SchedulerTaskService schedulerTaskService;
    private final SchedulerService schedulerService;

    public SchedulerTaskController(SchedulerService schedulerService, SchedulerTaskService schedulerTaskService) {
        this.schedulerTaskService = schedulerTaskService;
        this.schedulerService = schedulerService;
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
     * @param pagingParams параметры пагинации
     * @return список {@link JqgridPagedList} задач {@link TaskSearchResultItem}
     */
    @GetMapping(value = "/rest/schedulerTask")
    public JqgridPagedList<TaskSearchResultItem> fetchSchedulerTasks(@RequestParam PagingParams pagingParams) {
        PagingResult<TaskSearchResultItem> taskList = schedulerTaskService.fetchAllSchedulerTasks(pagingParams);
        return JqgridPagedResourceAssembler.buildPagedList(
                taskList,
                taskList.getTotalCount(),
                pagingParams
        );
    }

    /**
     * Запуск выполнения задач по расписанию
     *
     * @param ids идентификаторы задач
     */
    @PostMapping(value = "/actions/schedulerTask/activate")
    public void activateSchedulerTasks(@RequestParam Long[] ids) {
        List<Long> tasksIds = new ArrayList<Long>();
        Collections.addAll(tasksIds, ids);
        schedulerTaskService.setActiveSchedulerTask(true, tasksIds);
        schedulerService.updateAllTask();
    }

    /**
     * Остановка выполнения задач по расписанию
     *
     * @param ids идентификаторы задач
     */
    @PostMapping(value = "/actions/schedulerTask/deactivate")
    public void deactivateStateSchedulerTasks(@RequestParam Long[] ids) {
        List<Long> tasksIds = new ArrayList<Long>();
        Collections.addAll(tasksIds, ids);
        schedulerTaskService.setActiveSchedulerTask(false, tasksIds);
        schedulerService.updateAllTask();
    }
}
