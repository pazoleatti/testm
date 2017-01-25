package com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.server;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.server.DepartmentParamAliases;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared.GetRefBookValuesAction;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared.GetRefBookValuesResult;
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
public class GetRefBookValuesHandler extends AbstractActionHandler<GetRefBookValuesAction, GetRefBookValuesResult> {

    public GetRefBookValuesHandler() {
        super(GetRefBookValuesAction.class);
    }

    @Autowired
    RefBookFactory rbFactory;
    @Autowired
    PeriodService periodService;
    @Autowired
    RefBookHelper refBookHelper;
    @Autowired
    LogEntryService logEntryService;

    @Override
    public GetRefBookValuesResult execute(GetRefBookValuesAction action, ExecutionContext executionContext) throws ActionException {
        Logger logger = new Logger();

        //кэшируем список провайдеров для атрибутов-ссылок, чтобы для каждой строки их заново не создавать
        Map<String, RefBookDataProvider> refProviders = new HashMap<String, RefBookDataProvider>();
        Map<String, String> refAliases = new HashMap<String, String>();
        RefBook refBook = rbFactory.get(action.getSlaveRefBookId());
        for (RefBookAttribute attribute : refBook.getAttributes()) {
            if (attribute.getAttributeType() == RefBookAttributeType.REFERENCE) {
                refProviders.put(attribute.getAlias(), rbFactory.getDataProvider(attribute.getRefBookId()));
                RefBook refRefBook = rbFactory.get(attribute.getRefBookId());
                RefBookAttribute refAttribute = refRefBook.getAttribute(attribute.getRefBookAttributeId());
                refAliases.put(attribute.getAlias(), refAttribute.getAlias());
            }
        }

        GetRefBookValuesResult result = new GetRefBookValuesResult();
        RefBookDataProvider providerMaster = rbFactory.getDataProvider(action.getRefBookId());


        String filterMaster = DepartmentParamAliases.DEPARTMENT_ID.name() + " = " + action.getDepartmentId();

        ReportPeriod reportPeriod = periodService.getReportPeriod(action.getReportPeriodId());

        PagingResult<Map<String, RefBookValue>> paramsMaster = providerMaster.getRecords(
                addDayToDate(reportPeriod.getEndDate(), -1), null, filterMaster, null);
        if (paramsMaster.isEmpty()) {
            return result;
        }
        result.setNotTableValues(convert(paramsMaster, action.getRefBookId(), false, refProviders, refAliases).get(0));
        if (paramsMaster.get(0).containsKey(RefBook.RECORD_ID_ALIAS)) {
            result.setRecordId(paramsMaster.get(0).get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue());
        }

        RefBookDataProvider providerSlave = rbFactory.getDataProvider(action.getSlaveRefBookId());
        String filterSlave = "";
        if (action.getTaxType() == TaxType.NDFL) {
            filterSlave = "REF_BOOK_NDFL_ID = " + result.getRecordId();
        } else if (action.getTaxType() == TaxType.PFR) {
            filterSlave = "REF_BOOK_FOND_ID = " + result.getRecordId();
        }
        RefBookAttribute sortAttr = rbFactory.get(action.getSlaveRefBookId()).getAttribute("ROW_ORD");
        PagingResult<Map<String, RefBookValue>> paramsSlave = providerSlave.getRecords(
                addDayToDate(reportPeriod.getEndDate(), -1), null, filterSlave, sortAttr);
        result.setTableValues(convert(paramsSlave, action.getSlaveRefBookId(), true, refProviders, refAliases));

        //Проверяем справочные значения для полученной таблицы
        if (action.getOldUUID() == null) {
            checkReferenceValues(refBook, result.getTableValues(), reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate(), logger);
            if (logger.getMainMsg() != null) {
                result.setUuid(logEntryService.save(logger.getEntries()));
                result.setErrorMsg(logger.getMainMsg());
            }
        }

        // Заполняем период действия настроек
        RefBookRecordVersion version = providerMaster.getRecordVersionInfo(result.getRecordId());
        result.setConfigStartDate(version.getVersionStart());
        result.setConfigEndDate(version.getVersionEnd());
        return result;
    }

    @Override
    public void undo(GetRefBookValuesAction getRefBookValuesAction, GetRefBookValuesResult getRefBookValuesResult, ExecutionContext executionContext) throws ActionException {

    }

    /**
     * Проверка существования записей справочника на которые ссылаются атрибуты.
     * Считаем что все справочные атрибуты хранятся в универсальной структуре как и сами настройки
     * @param refBook
     * @param rows
     * @return возвращает true, если проверка пройдена
     */
    private void checkReferenceValues(RefBook refBook, List<Map<String, TableCell>> rows, Date versionFrom, Date versionTo, Logger logger) {
        Map<RefBookDataProvider, List<RefBookLinkModel>> references = new HashMap<RefBookDataProvider, List<RefBookLinkModel>>();

        RefBookDataProvider provider = rbFactory.getDataProvider(refBook.getId());
        RefBookDataProvider oktmoProvider = rbFactory.getDataProvider(96L);

        int i = 1;
        for (Map<String, TableCell> row : rows) {
            for (Map.Entry<String, TableCell> e : row.entrySet()) {
                TableCell cell = e.getValue();
                if (cell.getType() == null) {
                    continue;
                }
                if (cell.getType() == RefBookAttributeType.REFERENCE
                        && !e.getKey().equals("DEPARTMENT_ID")) { //Подразделения не версионируются и их нет смысла проверять
                    if (cell.getRefValue() != null) {
                        //Собираем ссылки на справочники и группируем их по провайдеру, обрабатывающему справочники
                        RefBookDataProvider linkProvider = e.getKey().equals("OKTMO") ? oktmoProvider : provider;
                        if (!references.containsKey(linkProvider)) {
                            references.put(linkProvider, new ArrayList<RefBookLinkModel>());
                        }
                        //Сохраняем данные для отображения сообщений
                        references.get(linkProvider).add(new RefBookLinkModel(i, e.getKey(), cell.getRefValue(), null, versionFrom, versionTo));
                    }
                }
            }
            i++;
        }

        //Проверяем ссылки и выводим соообщения если надо
        refBookHelper.checkReferenceValues(refBook, references, RefBookHelper.CHECK_REFERENCES_MODE.DEPARTMENT_CONFIG, logger);
    }

    private List<Map<String, TableCell>> convert(List<Map<String, RefBookValue>> data, Long refBookId, boolean needDeref, Map<String, RefBookDataProvider> refProviders, Map<String, String> refAliases) {
        List<Map<String, TableCell>> converted = new ArrayList<Map<String, TableCell>>();
        for (Map<String, RefBookValue> row : data) {
            converted.add(convertRow(row, refBookId, needDeref, refProviders, refAliases));
        }

        return converted;
    }

    private Map<String, TableCell> convertRow(Map<String, RefBookValue> data, Long refBookId, boolean needDeref, Map<String, RefBookDataProvider> refProviders, Map<String, String> refAliases) {
        Map<String, TableCell> res = new HashMap<String, TableCell>();
        for (String a : data.keySet()) {
            TableCell cell = new TableCell();
            switch (data.get(a).getAttributeType()) {
                case STRING:
                    cell.setStringValue(data.get(a).getStringValue());
                    break;
                case DATE:
                    cell.setDateValue(data.get(a).getDateValue());
                    break;
                case NUMBER:
                    cell.setNumberValue(data.get(a).getNumberValue());
                    break;
                case REFERENCE:
                    cell.setRefValue(data.get(a).getReferenceValue());
                    if (needDeref && data.get(a).getReferenceValue() != null) {
                        //if (refProviders.get(a).isRecordsExist(Arrays.asList(data.get(a).getReferenceValue())).isEmpty()) {
                            Map<String, RefBookValue> refValue = refProviders.get(a).getRecordData(data.get(a).getReferenceValue());
                            cell.setDeRefValue(refValue.get(refAliases.get(a)).toString());
                        /*} else {
                            //Если ссылка на несуществующую запись, то отображаем пустое поле
                            cell.setDeRefValue("");
                        }*/
                    }
                    break;
                default:

                    break;
            }
            cell.setType(data.get(a).getAttributeType());
            res.put(a, cell);
        }
        return res;
    }

    private Date addDayToDate(Date date, int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, days);
        return c.getTime();
    }
}