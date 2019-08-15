package com.aplana.sbrf.taxaccounting.service.impl.refbook;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Aspect
@Component
@Configurable
public class RefBookAuditAspect {

    @Autowired
    private AuditService auditService;
    @Autowired
    private RefBookFactory refBookFactory;

    private static final String CREATE_REPORT_PERIOD_NOTE_TEMPLATE = "Справочник \"Дополнительные интервалы для загрузки данных\". " +
            "Создание записи: \nПериод: %s, АСНУ: %s. Границы действия записи: с %s по %s. " +
            "\nДата начала интервала: %s \nДата окончания интервала: %s";
    private static final String EDIT_REPORT_PERIOD_NOTE_TEMPLATE = "Справочник \"Дополнительные интервалы для загрузки данных\". " +
            "Изменение записи с параметрами: \nПериод: %s, АСНУ: %s. Границы действия записи: с %s по %s. " +
            "\nДата начала интервала: %s \nДата окончания интервала: %s";

    @AfterReturning(pointcut="execution(public * com.aplana.sbrf.taxaccounting.service.impl.refbook.CommonRefBookServiceImpl.createRecord(..)) " +
            "&& args(userInfo, refBookId, record)", returning="actionResult")
    public void createAuditEntryOnCreateRefBookRecord(TAUserInfo userInfo, Long refBookId, Map<String,
                                                        RefBookValue> record, ActionResult actionResult) {
        if (refBookId == RefBook.Id.REPORT_PERIOD_IMPORT.getId()) {
            createReportPeriodAuditEntry(userInfo, refBookId, record, CREATE_REPORT_PERIOD_NOTE_TEMPLATE);
        }
    }

    @AfterReturning(pointcut="execution(public * com.aplana.sbrf.taxaccounting.service.impl.refbook.CommonRefBookServiceImpl.editRecord(..)) " +
            "&& args(userInfo, refBookId, recordId, record)", returning="actionResult")
    public void createAuditEntryOnUpdateRefBookRecord(TAUserInfo userInfo, long refBookId, long recordId,
                                                        Map<String, RefBookValue> record, ActionResult actionResult) {
        if (refBookId == RefBook.Id.REPORT_PERIOD_IMPORT.getId()) {
            createReportPeriodAuditEntry(userInfo, refBookId, record, EDIT_REPORT_PERIOD_NOTE_TEMPLATE);
        }
    }

    private void createReportPeriodAuditEntry(TAUserInfo userInfo, long refBookId,
                                  Map<String, RefBookValue> record, String noteTemplate) {
        String note = String.format(
                noteTemplate,
                getPeriodTypeCode(record.get("REPORT_PERIOD_TYPE_ID").getReferenceValue()), // <Объект."Код периода">
                getAsnuCode(record.get("ASNU_ID").getReferenceValue()), // <Объект."АСНУ">
                getFormattedRecordDate(record.get("record_version_from").getDateValue()), // <Объект. "Дата начала действия записи">
                record.get("record_version_to").getValue() != null ? getFormattedRecordDate(record.get("record_version_to").getDateValue()) : "", // <Объект. "Дата окончания действия записи">
                getFormattedPeriodDate(record.get("PERIOD_START_DATE").getDateValue()), // <Объект. "Дата начала интервала">
                getFormattedPeriodDate(record.get("PERIOD_END_DATE").getDateValue()) // <Объект. "Дата окончания интервала">
        );
        auditService.add(null, userInfo, note);
    }

    private String getFormattedPeriodDate(Date date) {
        SimpleDateFormat periodDateFormat = new SimpleDateFormat("dd.MM");
        return periodDateFormat.format(date);
    }

    private String getFormattedRecordDate(Date date) {
        SimpleDateFormat periodDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        return periodDateFormat.format(date);
    }

    private String getPeriodTypeCode(Long reportPeriodTypeId) {
        RefBookDataProvider reportPeriodProvider = refBookFactory.getDataProvider(RefBook.Id.PERIOD_CODE.getId());
        return reportPeriodProvider.getRecordData(reportPeriodTypeId).get("CODE").getStringValue();
    }

    private String getAsnuCode(Long asnuId) {
        RefBookDataProvider asnuProvider = refBookFactory.getDataProvider(RefBook.Id.ASNU.getId());
        return asnuProvider.getRecordData(asnuId).get("CODE").getStringValue();
    }
}
