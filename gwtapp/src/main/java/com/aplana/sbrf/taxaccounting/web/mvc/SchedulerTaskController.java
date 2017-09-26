package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TaskSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.service.api.ConfigurationService;
import com.aplana.sbrf.taxaccounting.service.scheduler.SchedulerService;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import org.springframework.beans.factory.annotation.Autowired;
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

    private ConfigurationService configurationService;
    private final SchedulerService schedulerService;

    @Autowired
    public SchedulerTaskController(ConfigurationService configurationService, SchedulerService schedulerService) {
        this.configurationService = configurationService;
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

    @GetMapping(value = "/rest/taskList")
    public JqgridPagedList<TaskSearchResultItem> fetchSchedulerTasks(@RequestParam PagingParams pagingParams) {
        PagingResult<TaskSearchResultItem> taskList = configurationService.getAllSchedulerTaskWithPaging(pagingParams);
        return JqgridPagedResourceAssembler.buildPagedList(
                taskList,
                taskList.getTotalCount(),
                pagingParams
        );
    }

    @PostMapping(value = "/actions/taskList/changeState")
    public void changeStateSchedulerTasks(@RequestParam Long[] ids, boolean isActive) {
        List<Long> tasksIds = new ArrayList<Long>();
        Collections.addAll(tasksIds, ids);
        configurationService.setActiveSchedulerTask(isActive, tasksIds);
        schedulerService.updateAllTask();
    }
}
