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
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractDeclarationAsyncTask extends AbstractAsyncTask {

    private static final String DECLARATION_DESCRIPTION = "№: %d, Период: \"%s, %s%s\", Подразделение: \"%s\", Вид: \"%s\"%s";

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

    @Override
    public BalancingVariants checkTaskLimit(Map<String, Object> params, Logger logger) throws AsyncTaskException {
        TAUserInfo userInfo = getUserInfo(params);
        DeclarationData declarationData = getDeclaration(params);
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());
        DeclarationDataReportType ddReportType = getDeclarationDataReportType(params);

        Long value = declarationDataService.getValueForCheckLimit(userInfo, declarationData.getId(), ddReportType);
        if (value == null) {
            throw new AsyncTaskException(new ServiceLoggerException("Налоговая форма не сформирована", null));
        }

        String taskName = declarationDataService.getTaskName(ddReportType, declarationTemplate.getType().getTaxType());
        return checkTask(getReportType(), value, taskName, getTaskLimitMsg(value, params));
    }

    protected String getTaskLimitMsg(Long value, Map<String, Object> params) {
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(getDeclaration(params).getDeclarationTemplateId());
        return String.format("xml файл %s имеет слишком большой размер(%s Кбайт)!", declarationTemplate.getType().getTaxType().getDeclarationShortName(), value);
    }

    protected DeclarationDataReportType getDeclarationDataReportType(Map<String, Object> params) {
        return DeclarationDataReportType.getDDReportTypeByReportType(getReportType());
    }

    protected final String getDeclarationDescription(Map<String, Object> params) {
        DeclarationData declaration = getDeclaration(params);
        Department department = departmentService.getDepartment(declaration.getDepartmentId());
        DepartmentReportPeriod reportPeriod = departmentReportPeriodService.get(declaration.getDepartmentReportPeriodId());
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declaration.getDeclarationTemplateId());

        return String.format(DECLARATION_DESCRIPTION,
                declaration.getId(),
                reportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                reportPeriod.getReportPeriod().getName(),
                getCorrectionDateString(reportPeriod),
                department.getName(),
                declarationTemplate.getType().getName(),
                getAdditionalString(declaration, params));
    }

    protected final DeclarationData getDeclaration(Map<String, Object> params) {
        long declarationDataId = (Long) params.get("declarationDataId");
        return declarationDataService.get(declarationDataId, getUserInfo(params));
    }

    private String getCorrectionDateString(DepartmentReportPeriod reportPeriod) {
        return reportPeriod.getCorrectionDate() != null ? String.format(" с датой сдачи корректировки %s", SDF_DD_MM_YYYY.get().format(reportPeriod.getCorrectionDate())) : "";
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
        return str.isEmpty()?"":(", " + str);
    }
}
