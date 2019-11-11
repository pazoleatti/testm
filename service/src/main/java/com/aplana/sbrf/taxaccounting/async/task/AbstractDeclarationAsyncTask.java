package com.aplana.sbrf.taxaccounting.async.task;


import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.utils.DepartmentReportPeriodFormatter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractDeclarationAsyncTask extends AbstractAsyncTask {

    private static final String DECLARATION_DESCRIPTION = "№: %d, Период: \"%s\", Подразделение: \"%s\", Вид: \"%s\"%s";

    protected static final String FAIL = "Не выполнена операция \"%s\" для налоговой формы: %s.";
    protected static final String CAUSE = " Причина: %s";

    @Autowired
    private DeclarationDataService declarationDataService;
    @Autowired
    private DeclarationTemplateService declarationTemplateService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;
    @Autowired
    private RefBookFactory refBookFactory;
    @Autowired
    private DepartmentReportPeriodFormatter departmentReportPeriodFormatter;

    @Override
    public AsyncQueue checkTaskLimit(String taskDescription, TAUserInfo userInfo, Map<String, Object> params, Logger logger) throws AsyncTaskException {
        DeclarationData declarationData = getDeclaration(userInfo, params);

        Long value = declarationDataService.getValueForCheckLimit(userInfo, declarationData.getId(), getAsyncTaskType(), params);
        if (value == null) {
            throw new AsyncTaskException(new ServiceLoggerException("Налоговая форма не сформирована", null));
        }

        return checkTask(value, taskDescription, getTaskLimitMsg(value, params));
    }

    protected String getTaskLimitMsg(Long value, Map<String, Object> params) {
        return String.format("xml файл налоговой формы имеет слишком большой размер(%s Кбайт)!", value);
    }

    protected final String getDeclarationDescription(int userId, Map<String, Object> params) {
        DeclarationData declaration = getDeclaration(userId, params);
        Department department = departmentService.getDepartment(declaration.getDepartmentId());
        DepartmentReportPeriod reportPeriod = departmentReportPeriodService.fetchOne(declaration.getDepartmentReportPeriodId());
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declaration.getDeclarationTemplateId());

        return String.format(DECLARATION_DESCRIPTION,
                declaration.getId(),
                departmentReportPeriodFormatter.getPeriodDescription(reportPeriod),
                department.getName(),
                declarationTemplate.getType().getName(),
                getAdditionalString(declaration, params));
    }

    protected final DeclarationData getDeclaration(int userId, Map<String, Object> params) {
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));
        return getDeclaration(userInfo, params);
    }

    protected final DeclarationData getDeclaration(TAUserInfo userInfo, Map<String, Object> params) {
        long declarationDataId = (Long) params.get("declarationDataId");
        return declarationDataService.get(declarationDataId, userInfo);
    }

    @Override
    public String createDescription(TAUserInfo userInfo, Map<String, Object> params) {
        throw new UnsupportedOperationException();
    }

    private String getCorrectionDateString(DepartmentReportPeriod reportPeriod) {
        return reportPeriod.getCorrectionDate() != null ? String.format(" с датой сдачи корректировки %s", SDF_DD_MM_YYYY.format(reportPeriod.getCorrectionDate())) : "";
    }

    /**
     * Формирует сообщение:
     * Налоговый орган: "%s", КПП: "%s", ОКТМО: "%s", АСНУ: "%s"
     */
    protected String getAdditionalString(DeclarationData declarationData, Map<String, Object> params) {
        List<String> messages = new ArrayList<String>();

        if (declarationData.getTaxOrganCode() != null) {
            messages.add(String.format("Налоговый орган: \"%s\"", declarationData.getTaxOrganCode()));
        }

        if (declarationData.getKpp() != null) {
            messages.add(String.format("КПП: \"%s\"", declarationData.getKpp()));
        }

        if (declarationData.getOktmo() != null) {
            messages.add(String.format("ОКТМО: \"%s\"", declarationData.getOktmo()));
        }

        if (declarationData.getAsnuId() != null) {
            RefBookDataProvider asnuProvider = refBookFactory.getDataProvider(RefBook.Id.ASNU.getId());
            String asnuName = asnuProvider.getRecordData(declarationData.getAsnuId()).get("NAME").getStringValue();

            messages.add(String.format("АСНУ: \"%s\"", asnuName));
        }

        String str = StringUtils.join(messages.toArray(), ", ", null);
        return str.isEmpty() ? "" : (", " + str);
    }
}
