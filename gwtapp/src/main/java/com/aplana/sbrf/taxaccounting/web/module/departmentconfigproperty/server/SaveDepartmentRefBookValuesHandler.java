package com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.server.DepartmentParamAliases;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared.SaveDepartmentRefBookValuesAction;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared.SaveDepartmentRefBookValuesResult;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared.TableCell;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class SaveDepartmentRefBookValuesHandler extends AbstractActionHandler<SaveDepartmentRefBookValuesAction, SaveDepartmentRefBookValuesResult> {

    public SaveDepartmentRefBookValuesHandler() {
        super(SaveDepartmentRefBookValuesAction.class);
    }

    @Autowired
    RefBookFactory rbFactory;
    @Autowired
    DepartmentReportPeriodService reportPeriodService;
    @Autowired
    SecurityService securityService;
    @Autowired
    PeriodService reportService;
    @Autowired
    LogEntryService logEntryService;

    @Override
    public SaveDepartmentRefBookValuesResult execute(SaveDepartmentRefBookValuesAction saveDepartmentRefBookValuesAction, ExecutionContext executionContext) throws ActionException {


        Logger logger = new Logger();
        logger.setTaUserInfo(securityService.currentUserInfo());
        SaveDepartmentRefBookValuesResult result = new SaveDepartmentRefBookValuesResult();
        for (Map<String, TableCell> row : saveDepartmentRefBookValuesAction.getRows()) {
            String sig = row.get("SIGNATORY_ID").getDeRefValue();
            Integer signatoryId = (sig == null || sig.isEmpty()) ?
                    null :
                    Integer.parseInt(row.get("SIGNATORY_ID").getDeRefValue().trim());
            String approveDocName = row.get("APPROVE_DOC_NAME").getStringValue();
            String approveOrgName = row.get("APPROVE_ORG_NAME").getStringValue();

            if ((signatoryId != null && signatoryId == 1)
                    && ((approveDocName != null && !approveDocName.isEmpty())
                    ||  (approveOrgName != null && !approveOrgName.isEmpty()))
               ) {
                logger.error("Поля \"Наименование документа представителя\", " +
                        "\"Наименование организации представителя\" " +
                        "должны заполняться только в том случае, если " +
                        "поле \"Признак лица подписавшего документ\" равно значению \"2\" (представитель налогоплательщика)");
                result.setHasFatalError(true);
                result.setErrorType(SaveDepartmentRefBookValuesResult.ERROR_TYPE.INCORRECT_FIELDS);
                break;
            }
        }

        Map<Pair<String, String>, Integer> counter = new HashMap<Pair<String, String>, Integer>();
        for (Map<String, TableCell> row : saveDepartmentRefBookValuesAction.getRows()) {
            Pair<String, String> p = new Pair<String, String>(row.get("KPP").getStringValue(), row.get("TAX_ORGAN_CODE").getStringValue());
            if (!counter.containsKey(p)) {
                counter.put(p, new Integer(0));
            }
            counter.put(p, counter.get(p) + 1);

        }

        for (Integer count : counter.values()) {
            if (count > 1) {
                logger.error("Нарушено требование к уникальности, уже существуют элементы с такими значениями атрибута " +
                        "\"Код налогового органа, КПП\"" +
                        " в указанном периоде!");
                result.setHasFatalError(true);
                result.setErrorType(SaveDepartmentRefBookValuesResult.ERROR_TYPE.HAS_DUPLICATES);
                break;
            }
        }

        if (result.isHasFatalError()) {
            result.setUuid(logEntryService.save(logger.getEntries()));
            return result;
        }


        RefBookDataProvider provider = rbFactory.getDataProvider(saveDepartmentRefBookValuesAction.getRefBookId());
        ReportPeriod rp = reportService.getReportPeriod(saveDepartmentRefBookValuesAction.getReportPeriodId());
        String filter = DepartmentParamAliases.DEPARTMENT_ID.name() + " = " + saveDepartmentRefBookValuesAction.getDepartmentId();

        boolean needEdit = false;
        Long recordId = saveDepartmentRefBookValuesAction.getRecordId();

        // Поиск версий настроек для указанного подразделения. Если они есть - создаем новую версию с существующим record_id, иначе создаем новый record_id (по сути элемент справочника)
        List<Pair<Long, Long>> recordPairsExistence = provider.checkRecordExistence(null, filter);
        if (recordPairsExistence.size() != 0) {
            //Проверяем, к одному ли элементу относятся версии
            Set<Long> recordIdSet = new HashSet<Long>();
            for (Pair<Long, Long> pair : recordPairsExistence) {
                recordIdSet.add(pair.getSecond());
            }

            if (recordIdSet.size() > 1) {
                throw new ActionException("Версии настроек, отобраные по фильтру, относятся к разным подразделениям");
            }

            // Существуют версии настроек для указанного подразделения
            recordId = recordPairsExistence.get(0).getSecond();
        }

        List<Pair<Long, Long>> recordPairs = provider.checkRecordExistence(rp.getCalendarStartDate(), filter);
        if (recordPairs.size() != 0) {
            needEdit = true;
            // Запись нашлась
            if (recordPairs.size() != 1) {
                throw new ActionException("Найдено несколько настроек для подразделения ");
            }
            recordId = recordPairs.get(0).getFirst();
        }

        Map<String, RefBookValue> notTable = convert(saveDepartmentRefBookValuesAction.getNotTableParams());
        notTable.put(DepartmentParamAliases.DEPARTMENT_ID.name(), new RefBookValue(RefBookAttributeType.REFERENCE, saveDepartmentRefBookValuesAction.getDepartmentId().longValue()));

        if (!needEdit) {
            RefBookRecord record = new RefBookRecord();
            record.setValues(notTable);
            record.setRecordId(recordId);
            recordId = provider.createRecordVersion(logger, rp.getCalendarStartDate(), null, Arrays.asList(record)).get(0);
        } else {
            provider.updateRecordVersion(logger, recordId, rp.getCalendarStartDate(), null, notTable);
        }

        if (recordId != null) {
            List<Map<String, RefBookValue>> convertedRows = convertRows(saveDepartmentRefBookValuesAction.getRows());

            RefBookDataProvider providerSlave = rbFactory.getDataProvider(saveDepartmentRefBookValuesAction.getSlaveRefBookId());
            String filterSlave = "LINK = " + recordId;
            RefBookAttribute sortAttr = rbFactory.get(saveDepartmentRefBookValuesAction.getSlaveRefBookId()).getAttribute("ROW_ORD");

            PagingResult<Map<String, RefBookValue>> paramsSlave = providerSlave.getRecords(rp.getCalendarStartDate(), null, filterSlave, sortAttr);

            Set<Map<String, RefBookValue>> toUpdate = new HashSet<Map<String, RefBookValue>>();
            Set<Map<String, RefBookValue>> toAdd = new HashSet<Map<String, RefBookValue>>();
            Set<Map<String, RefBookValue>> toDelete = new HashSet<Map<String, RefBookValue>>();

            int maxRowOrd = 0;
            for (Map<String, RefBookValue> rowFromClient : convertedRows) {
                boolean contains = false;
                for (Map<String, RefBookValue> rowFromServer : paramsSlave) {
                    if (rowFromClient.get("TAX_ORGAN_CODE").getStringValue().equals(rowFromServer.get("TAX_ORGAN_CODE").getStringValue())
                            && rowFromClient.get("KPP").getStringValue().equals(rowFromServer.get("KPP").getStringValue())) {
                        contains = true;
                        for (Map.Entry<String, RefBookValue> v : rowFromServer.entrySet()) {
                            if (rowFromClient.get(v.getKey()) == null || rowFromClient.get(v.getKey()).isEmpty()) {
                                rowFromClient.put(v.getKey(), v.getValue());
                            }
                        }
                        rowFromClient.put("LINK",new RefBookValue(RefBookAttributeType.REFERENCE, recordId));
                        rowFromClient.put("ROW_ORD",rowFromServer.get("ROW_ORD"));
                        rowFromClient.put("record_id",rowFromServer.get("record_id"));
                        rowFromClient.put("DEPARTMENT_ID", new RefBookValue(RefBookAttributeType.REFERENCE, saveDepartmentRefBookValuesAction.getDepartmentId().longValue()));
                        toUpdate.add(rowFromClient);
                        break;
                    }
                }
                if (rowFromClient.containsKey("ROW_ORD") && rowFromClient.get("ROW_ORD") != null) {
                    int rowOrd = rowFromClient.get("ROW_ORD").getNumberValue().intValue();
                    if (rowOrd > maxRowOrd) {
                        maxRowOrd = rowOrd;
                    }
                }
                if (!contains) {
                    rowFromClient.put("LINK", new RefBookValue(RefBookAttributeType.REFERENCE, recordId));
                    rowFromClient.put("ROW_ORD", new RefBookValue(RefBookAttributeType.NUMBER, ++maxRowOrd));
                    rowFromClient.put("DEPARTMENT_ID", new RefBookValue(RefBookAttributeType.REFERENCE, saveDepartmentRefBookValuesAction.getDepartmentId().longValue()));
                    toAdd.add(rowFromClient);
                }
            }

            List<RefBookRecord> recordsToAdd = new ArrayList<RefBookRecord>();
            for (Map<String, RefBookValue> add : toAdd) {
                RefBookRecord record = new RefBookRecord();
                record.setValues(add);
                record.setRecordId(null);
                recordsToAdd.add(record);
            }


            for (Map<String, RefBookValue> rowFromServer : paramsSlave) {
                boolean notFound = true;
                for (Map<String, RefBookValue> rowFromClient : convertedRows) {
                    if (rowFromClient.get("TAX_ORGAN_CODE").getStringValue().equals(rowFromServer.get("TAX_ORGAN_CODE").getStringValue())
                            && rowFromClient.get("KPP").getStringValue().equals(rowFromServer.get("KPP").getStringValue())) {
                        notFound = false;
                        break;
                    }
                }
                if (notFound) {
                    toDelete.add(rowFromServer);
                }
            }

            List<Long> deleteIds = new ArrayList<Long>();
            for (Map<String, RefBookValue> del : toDelete) {
                deleteIds.add(del.get("record_id").getNumberValue().longValue());
            }

            if (!logger.containsLevel(LogLevel.ERROR)) {
                if (!recordsToAdd.isEmpty()) {
                    providerSlave.createRecordVersion(logger, rp.getCalendarStartDate(), null, recordsToAdd);
                }

                for (Map<String, RefBookValue> up : toUpdate) {
                    providerSlave.updateRecordVersion(logger, up.get("record_id").getNumberValue().longValue(), null, null, up);
                }

                if (!deleteIds.isEmpty()) {
                    providerSlave.deleteRecordVersions(logger, deleteIds);
                }
            }
        }
        return new SaveDepartmentRefBookValuesResult();
    }

    @Override
    public void undo(SaveDepartmentRefBookValuesAction saveDepartmentRefBookValuesAction, SaveDepartmentRefBookValuesResult saveDepartmentRefBookValuesResult, ExecutionContext executionContext) throws ActionException {

    }

    private List<Map<String,RefBookValue>> convertRows(List<Map<String, TableCell>> rows) {
        List<Map<String,RefBookValue>> convertedRows = new ArrayList<Map<String,RefBookValue>>();
        for (Map<String, TableCell> row : rows) {
            Map<String,RefBookValue> convertedRow = convert(row);
            convertedRows.add(convertedRow);
        }
        return convertedRows;
    }

    private Map<String,RefBookValue> convert(Map<String, TableCell> row) {
        Map<String,RefBookValue> convertedRow = new HashMap<String, RefBookValue>();
        for (Map.Entry<String, TableCell> e : row.entrySet()) {
            RefBookValue value = null;
            if (e.getValue().getType() == null) {
                convertedRow.put(e.getKey(), null);
                continue;
            }
            switch (e.getValue().getType()) {
                case STRING:
                    value = new RefBookValue(e.getValue().getType(), e.getValue().getStringValue());
                    break;
                case DATE:
                    value = new RefBookValue(e.getValue().getType(), e.getValue().getDateValue());
                    break;
                case NUMBER:
                    value = new RefBookValue(e.getValue().getType(), e.getValue().getNumberValue());
                    break;
                case REFERENCE:
                    value = new RefBookValue(e.getValue().getType(), e.getValue().getRefValue());
                    break;
                default:
                    break;
            }
            convertedRow.put(e.getKey(), value);
        }
        return convertedRow;
    }
}
