package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TaskSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTask;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTaskData;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTaskParam;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTaskParamType;
import com.aplana.sbrf.taxaccounting.service.SchedulerTaskService;
import com.aplana.sbrf.taxaccounting.service.scheduler.SchedulerService;
import com.aplana.sbrf.taxaccounting.web.model.SchedulerTaskModel;
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
// TODO Необходим рефакторинг бэкенда и фронтенда см. https://jira.aplana.com/browse/SBRFNDFL-3138
public class SchedulerTaskController {

    private final SchedulerTaskService schedulerTaskService;
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
        binder.registerCustomEditor(SchedulerTaskData.class, new RequestParamEditor(SchedulerTaskData.class));
        binder.registerCustomEditor(SchedulerTask.class, new RequestParamEditor(SchedulerTask.class));
        binder.registerCustomEditor(SchedulerTaskParam.class, new RequestParamEditor(SchedulerTaskParam.class));
        binder.registerCustomEditor(SchedulerTaskParamType.class, new RequestParamEditor(SchedulerTaskParamType.class));
        binder.registerCustomEditor(SchedulerTaskModel.class, new RequestParamEditor(SchedulerTaskModel.class));
        binder.registerCustomEditor(List.class, new RequestParamEditor(List.class));
        binder.registerCustomEditor(Boolean.class, new RequestParamEditor(Boolean.class));

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
        List<Long> tasksIds = new ArrayList<>();
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
        List<Long> tasksIds = new ArrayList<>();
        Collections.addAll(tasksIds, ids);
        schedulerTaskService.setActiveSchedulerTask(false, tasksIds);
        schedulerService.updateAllTask();
    }

    /**
     *Отображение редактируемой задачи планировщика
     *
     * @param idTaskScheduler идентификаторы задач
     */
    @GetMapping(value = "/rest/updateSchedulerTask")
    public SchedulerTaskData fetchUpdateSchedulerTask(long idTaskScheduler) {
        return schedulerTaskService.getSchedulerTask(idTaskScheduler);
    }

    /**
     *Редактирование задачи планировщика
     *
     * @param schedulerTaskModel измененная задача планировщика
     */
    @PostMapping(value = "/actions/updateSchedulerTask")
    public String updateSchedulerTask(@RequestParam(value = "schedulerTaskModel") SchedulerTaskModel schedulerTaskModel) {
       return schedulerTaskService.updateTask(schedulerTaskModel.getShedulerTaskData() );
    }
}
