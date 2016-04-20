package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataAccessParams;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DataRowResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.FillPreviousAction;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class FillPreviousHandler extends AbstractActionHandler<FillPreviousAction, DataRowResult> {

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private FormDataAccessService accessService;

    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;

    @Autowired
    private FormDataService formDataService;

    @Autowired
    private DataRowService dataRowService;

    @Autowired
    private SecurityService securityService;

    private static final ThreadLocal<SimpleDateFormat> SIMPLE_DATE_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };
    private final static String NOT_FOUND_MESSAGE = "В предыдущих корректирующих и в обычном (не корректирующем) периоде не найден экземпляр формы в статусе \"Принята\". Копирование данных не может быть выполнено.";
    private final static String FOUND_CORRECTION_MESSAGE = "Найден экземпляр формы предыдущего периода, дата сдачи корректировки: ";
    private final static String FOUND_SIMPLE_MESSAGE = "Найден экземпляр формы в основном (не корректирующем) периоде.";
    private final static String SUCCESS_MESSAGE = "Данные успешно скопированы.";
    private final static String RESTRICT_EDIT_MESSAGE = "Нет прав на редактирование налоговой формы!";
    private final static String CLOSED_PERIOD_MESSAGE = "Отчетный период подразделения закрыт!";

    public FillPreviousHandler() {
        super(FillPreviousAction.class);
    }

    @Override
    public DataRowResult execute(FillPreviousAction action, ExecutionContext context) throws ActionException {
        DataRowResult result = new DataRowResult();
        Logger logger = new Logger();

        // Проверки
        FormDataAccessParams formDataAccessParams = accessService.getFormDataAccessParams(
                securityService.currentUserInfo(), action.getFormData().getId(), false);

        if (!formDataAccessParams.isCanEdit()) {
            throw new ActionException(RESTRICT_EDIT_MESSAGE);
        }

        // Текущий отчетный период подразделения
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(
                action.getFormData().getDepartmentReportPeriodId());

        if (!departmentReportPeriod.isActive()) {
            throw new ActionException(CLOSED_PERIOD_MESSAGE);
        }

        DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
        filter.setDepartmentIdList(Arrays.asList(departmentReportPeriod.getDepartmentId()));
        filter.setReportPeriodIdList(Arrays.asList(departmentReportPeriod.getReportPeriod().getId()));
        // Список всех отчетных периодов для пары отчетный период-подразделение
        List<DepartmentReportPeriod> departmentReportPeriodList = departmentReportPeriodService.getListByFilter(filter);

        Collections.sort(departmentReportPeriodList, new Comparator<DepartmentReportPeriod>() {
            @Override
            public int compare(DepartmentReportPeriod o1, DepartmentReportPeriod o2) {
                if (o1.getCorrectionDate() == null) {
                    return -1;
                }
                if (o2.getCorrectionDate() == null) {
                    return 1;
                }
                return o1.getCorrectionDate().compareTo(o2.getCorrectionDate());
            }
        });

        FormData prevFormData = formDataService.getPreviousFormDataCorrection(action.getFormData(),
                departmentReportPeriodList, departmentReportPeriod);

        if (prevFormData == null) {
            logger.error(NOT_FOUND_MESSAGE);
        } else {
            // Копирование строк
            DepartmentReportPeriod prevFormDepartmentReportPeriod = departmentReportPeriodService.get(
                    prevFormData.getDepartmentReportPeriodId());

            logger.info(prevFormDepartmentReportPeriod.getCorrectionDate() != null ? FOUND_CORRECTION_MESSAGE +
                    SIMPLE_DATE_FORMAT.get().format(prevFormDepartmentReportPeriod.getCorrectionDate()) + "." : FOUND_SIMPLE_MESSAGE);

            dataRowService.copyRows(prevFormData.getId(), action.getFormData().getId());
            logger.info(SUCCESS_MESSAGE);
        }

        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(FillPreviousAction action, DataRowResult result, ExecutionContext context) throws ActionException {
        // Не требуется
    }
}
