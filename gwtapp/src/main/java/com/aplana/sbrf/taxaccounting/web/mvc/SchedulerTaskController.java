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
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Контроллер для работы со списком задач по расписанию
 */

@RestController
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
        binder.registerCustomEditor(List.class, new RequestParamEditor(List.class));
        binder.registerCustomEditor(Boolean.class, new RequestParamEditor(Boolean.class));

    }

    /**
     * Получение страницы задач
     *
     * @param pagingParams параметры пагинации
     * @return страница {@link JqgridPagedList} задач {@link TaskSearchResultItem}
     */
    @GetMapping(value = "/rest/schedulerTask")
    public JqgridPagedList<TaskSearchResultItem> fetchSchedulerTasks(@RequestParam PagingParams pagingParams) {
        PagingResult<TaskSearchResultItem> taskList = schedulerTaskService.fetchAllByPaging(pagingParams);

        return JqgridPagedResourceAssembler.buildPagedList(
                taskList,
                taskList.getTotalCount(),
                pagingParams
        );
    }

    /**
     * Запуск выполнения задач по расписанию
     *
     * @param tasksIds идентификаторы задач
     */
    @PostMapping(value = "/actions/schedulerTask/activate")
    public void activateSchedulerTasks(@RequestBody List<Long> tasksIds) {
        schedulerTaskService.updateActiveByIds(true, tasksIds);
        schedulerService.updateAllTask();

    }

    /**
     * Остановка выполнения задач по расписанию
     *
     * @param tasksIds идентификаторы задач
     */
    @PostMapping(value = "/actions/schedulerTask/deactivate")
    public void deactivateStateSchedulerTasks(@RequestBody List<Long> tasksIds) {
        schedulerTaskService.updateActiveByIds(false, tasksIds);
        schedulerService.updateAllTask();
    }

    /**
     * Получение задачи планировщика по идентификатору
     *
     * @param idTaskScheduler идентификатор задачи
     * @return объект {@link SchedulerTaskData} или null
     */
    @GetMapping(value = "/rest/schedulerTaskData/{idTaskScheduler}")
    public SchedulerTaskData fetchOne(@PathVariable long idTaskScheduler) {
        return schedulerTaskService.fetchOne(idTaskScheduler);
    }

    /**
     * Обновление задачи планировщика
     *
     * @param schedulerTaskData измененная задача планировщика
     */
    @PostMapping(value = "/rest/schedulerTaskData/update")
    public void update(@RequestParam(value = "schedulerTaskModel") SchedulerTaskData schedulerTaskData) {
        schedulerTaskService.update(schedulerTaskData);
    }

    /**
     * Валидация крон выражения на соответствие формату
     *
     * @param cronString строка расписания в формате cron
     * @return признак валидности
     */
    @PostMapping(value = "/action/schedulerTaskData/validateCron")
    public boolean validateCron(@RequestParam(value = "cronString") String cronString) {
        return schedulerTaskService.validateScheduleCronString(cronString);
    }


}
