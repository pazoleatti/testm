package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetPerformerAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetPerformerResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Service
public class GetPerformerHandler extends AbstractActionHandler<GetPerformerAction, GetPerformerResult> {

    @Autowired
    private FormDataService formDataService;

    @Autowired
    DepartmentService departmentService;

    @Autowired
    private LockDataService lockService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private TAUserService taUserService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;

    public GetPerformerHandler() {
        super(GetPerformerAction.class);
    }

    @Override
    public GetPerformerResult execute(GetPerformerAction action, ExecutionContext executionContext) throws ActionException {
        TAUserInfo userInfo = securityService.currentUserInfo();
        GetPerformerResult result = new GetPerformerResult();
        Logger logger = new Logger();
        FormData formData = formDataService.getFormData(userInfo, action.getFormData().getId(), action.getFormData().isManual(), logger);
        result.setFormData(formData);
        boolean isActivePeriod = departmentReportPeriodService.get(formData.getDepartmentReportPeriodId()).isActive();
        if (isActivePeriod && (formData.getState().equals(WorkflowState.CREATED) || formData.getState().equals(WorkflowState.PREPARED))) {
            String key = formDataService.generateTaskKey(action.getFormData().getId(), ReportType.EDIT_FD);
            LockData lockData = lockService.getLock(key);
            if (lockData == null) {
                result.setReadOnlyMode(false);
                result.setCreateLock(true);
                lockService.lock(key, userInfo.getUser().getId(),
                        formDataService.getFormDataFullName(action.getFormData().getId(), action.getFormData().isManual(), null, ReportType.EDIT_FD));
            } else if (lockData != null && lockData.getUserId() == userInfo.getUser().getId()) {
                result.setReadOnlyMode(false);
            } else {
                result.setReadOnlyMode(true);
                logger.error("Редактирование параметров печатной формы недоступно, так как данный экземпляр налоговой формы в текущий момент редактируется другим пользователем \"%s\"(с %s)",
                        taUserService.getUser(lockData.getUserId()).getName(), getFormedDate(lockData.getDateLock()));
                result.setUuid(logEntryService.save(logger.getEntries()));
            }
        } else {
            result.setReadOnlyMode(true);
        }
        Set<Integer> avSet = new HashSet<Integer>();
        avSet.addAll(departmentService.getPrintFormDepartments(action.getFormData()));
        result.setAvailableDepartments(avSet);
        result.setDepartments(new ArrayList<Department>(departmentService.getRequiredForTreeDepartments(avSet).values()));
        return result;
    }

    private static String getFormedDate(Date dateToForm) {
        // Преобразуем Date в строку вида "dd.mm.yyyy hh:mm"
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm z");
        formatter.format(dateToForm);
        return (formatter.format(dateToForm));
    }

    @Override
    public void undo(GetPerformerAction getPerformerAction, GetPerformerResult getPerformerResult, ExecutionContext executionContext) throws ActionException {
        //do nothing
    }
}
