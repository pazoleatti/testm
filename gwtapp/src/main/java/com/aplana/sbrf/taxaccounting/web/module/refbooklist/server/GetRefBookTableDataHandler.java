package com.aplana.sbrf.taxaccounting.web.module.refbooklist.server;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookType;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskContext;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskParam;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskParamType;
import com.aplana.sbrf.taxaccounting.scheduler.api.exception.TaskSchedulingException;
import com.aplana.sbrf.taxaccounting.scheduler.api.manager.TaskManager;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared.GetTableDataAction;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared.GetTableDataResult;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared.TableModel;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Stanislav Yasinskiy
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP')")
@Component
public class GetRefBookTableDataHandler extends AbstractActionHandler<GetTableDataAction, GetTableDataResult> {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    RefBookFactory refBookFactory;

    @Autowired
    TaskManager taskManager;

    public GetRefBookTableDataHandler() {
        super(GetTableDataAction.class);
    }

    @Override
    public GetTableDataResult execute(GetTableDataAction action, ExecutionContext executionContext) throws ActionException {
        GetTableDataResult result = new GetTableDataResult();

        try {
            Map<String, TaskParam> paramMap = new HashMap<String, TaskParam>();
            TaskParam testB = new TaskParam(0 ,"testB", TaskParamType.BOOLEAN, "false");
            TaskParam testI = new TaskParam(1 ,"testI", TaskParamType.INT, "1");
            TaskParam testF = new TaskParam(2 ,"testF", TaskParamType.FLOAT, "2.5");
            TaskParam testD = new TaskParam(3 ,"testD", TaskParamType.DATE, new SimpleDateFormat(TaskParam.DATE_FORMAT).format(new Date()));

            paramMap.put(testB.getName(), testB);
            paramMap.put(testI.getName(), testI);
            paramMap.put(testF.getName(), testF);
            paramMap.put(testD.getName(), testD);

            TaskContext taskContext = new TaskContext();
            taskContext.setTaskName("TestTask");
            taskContext.setNumberOfRepeats(-1);
            taskContext.setSchedule("0 25 * * * ?");
            taskContext.setUserTaskJndi("ejb/taxaccounting/scheduler-task.jar/SimpleUserTask#com.aplana.sbrf.taxaccounting.scheduler.api.task.UserTaskRemote");
            taskContext.setParams(paramMap);
            taskManager.createTask(taskContext);

            //taskManager.startTask(107L);

            //taskManager.deleteTask(3L);
        } catch (TaskSchedulingException e) {
            e.printStackTrace();
        }

        /*List<RefBook> list = refBookFactory.getAll(true, action.getType()); // запросить только видимые справочники

        List<TableModel> returnList = new ArrayList<TableModel>();
        boolean isFiltered = action.getFilter() != null && !action.getFilter().isEmpty();
        for (RefBook model : list) {
            if (!isFiltered || model.getName().toLowerCase().contains(action.getFilter().toLowerCase())) {
                returnList.add(new TableModel(model.getId(), model.getName(), RefBookType.EXTERNAL));
            }
        }

        result.setTableData(returnList);*/
        return result;
    }

    @Override
    public void undo(GetTableDataAction action, GetTableDataResult result, ExecutionContext executionContext) throws ActionException {
        // Не требуется
    }
}
