package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;
import com.aplana.sbrf.taxaccounting.service.TaxNotificationService;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Component("CreateTaxNotificationAsyncTask")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@SuppressWarnings("unchecked")
public class CreateTaxNotificationAsyncTask extends AbstractAsyncTask {

    private final TaxNotificationService taxNotificationService;

    public CreateTaxNotificationAsyncTask(TaxNotificationService taxNotificationService) {
        this.taxNotificationService = taxNotificationService;
    }

    @Override
    public String createDescription(TAUserInfo userInfo, Map<String, Object> params) {
        return getDescription(params);
    }

    private String getDescription(Map<String, Object> params) {
        DeclarationData declaration = (DeclarationData) params.get("declaration");
        Department department = (Department) params.get("department");
        ReportPeriod period = (ReportPeriod) params.get("period");
        List<RefBookAsnu> asnuList = (List<RefBookAsnu>) params.get("asnuList");

        String asnuString = "";
        if (isNotEmpty(asnuList)) {
            Collection<String> asnuCodes = Collections2.transform(asnuList, new Function<RefBookAsnu, String>() {
                @Override
                public String apply(RefBookAsnu asnu) {
                    return asnu.getCode();
                }
            });
            asnuString = " по АСНУ: " + StringUtils.join(asnuCodes, ", ");
        }

        return String.format("\"Формирование Уведомлений о неудержанном налоге для территориального банка %s за период %d: %s%s по КНФ %d\"",
                department.getShortName(), period.getTaxPeriod().getYear(), period.getName(), asnuString, declaration.getId());
    }

    @Override
    protected BusinessLogicResult executeBusinessLogic(AsyncTaskData taskData, Logger logger) {
        Map<String, Object> params = taskData.getParams();
        DeclarationData declaration = (DeclarationData) params.get("declaration");
        List<RefBookAsnu> asnuList = (List<RefBookAsnu>) params.get("asnuList");

        String fileUuid = taxNotificationService.create(declaration, asnuList, logger);

        if (logger.containsLevel(LogLevel.ERROR) || fileUuid == null) {
            return new BusinessLogicResult(false, null);
        }
        return new BusinessLogicResult(true, NotificationType.REF_BOOK_REPORT, fileUuid);
    }

    @Override
    protected String getNotificationMsg(AsyncTaskData taskData) {
        Map<String, Object> params = taskData.getParams();
        return "Завершена операция " + getDescription(params);
    }

    @Override
    protected String getErrorMsg(AsyncTaskData taskData, boolean unexpected) {
        Map<String, Object> params = taskData.getParams();
        Department department = (Department) params.get("department");
        ReportPeriod period = (ReportPeriod) params.get("period");
        return String.format("Не выполнена операция \"Формирование Уведомлений о неудержанном налоге для территориального банка %s за период %d: %s\"",
                department.getShortName(), period.getTaxPeriod().getYear(), period.getName());
    }

    @Override
    protected AsyncQueue checkTaskLimit(String taskDescription, TAUserInfo user, Map<String, Object> params, Logger logger) {
        return AsyncQueue.LONG;
    }

    @Override
    protected AsyncTaskType getAsyncTaskType() {
        return AsyncTaskType.CREATE_NOT_HOLDING_TAX_NOTIFICATIONS;
    }

}
