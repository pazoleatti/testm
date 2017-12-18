package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Выгрузка шаблона ТФ (Excel) для Налоговой формы
 */
@Component("CreateExcelTemplateAsyncTask")
public class CreateExcelTemplateAsyncTask extends AbstractDeclarationAsyncTask {

    @Autowired
    private TAUserService userService;

    @Autowired
    private DeclarationDataService declarationDataService;

    @Autowired
    private ReportService reportService;

    @Override
    protected AsyncTaskType getAsyncTaskType() {
        return AsyncTaskType.EXCEL_TEMPLATE_DEC;
    }

    @Override
    protected BusinessLogicResult executeBusinessLogic(final AsyncTaskData taskData, Logger logger) {
        Map<String, Object> params = taskData.getParams();
        long declarationDataId = (Long) params.get("declarationDataId");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));
        DeclarationData declarationData = declarationDataService.get(declarationDataId, userInfo);

        if (declarationData != null) {
            String uuid;
            try {
                uuid = declarationDataService.createExcelTemplate(declarationData, userInfo, logger);
                reportService.createDec(declarationDataId, uuid, DeclarationDataReportType.EXCEL_TEMPLATE_DEC);
            } catch (Exception e) {
                throw new ServiceException(String.format("Выгрузка шаблона ТФ (Excel) для Налоговой формы %s не может быть выполнена: %s",
                        getDeclarationDescription(taskData.getUserId(), params),
                        e.getMessage() != null && !e.getMessage().isEmpty() ? e.getMessage() : e.getClass().getSimpleName()), e);
            }
            return new BusinessLogicResult(true, NotificationType.REF_BOOK_REPORT, uuid);
        }

        return new BusinessLogicResult(true, null);
    }

    @Override
    protected String getErrorMsg(AsyncTaskData taskData, boolean unexpected) {
        return "Произошла непредвиденная ошибка при формировании шаблона ТФ (Excel)";
    }

    @Override
    protected String getNotificationMsg(AsyncTaskData taskData) {
        return "Сформирован шаблон ТФ (Excel)";
    }

    @Override
    public String getDescription(TAUserInfo userInfo, Map<String, Object> params) {
        return getAsyncTaskType().getDescription();
    }
}
