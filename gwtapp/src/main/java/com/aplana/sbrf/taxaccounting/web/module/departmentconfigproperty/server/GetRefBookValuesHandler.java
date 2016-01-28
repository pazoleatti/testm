package com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.server;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
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
    PeriodService reportService;
    @Autowired
    RefBookHelper refBookHelper;

    Map<String, RefBookDataProvider> refProviders;
    Map<String, String> refAliases;

    @Override
    public GetRefBookValuesResult execute(GetRefBookValuesAction getRefBookValuesAction, ExecutionContext executionContext) throws ActionException {

        //кэшируем список провайдеров для атрибутов-ссылок, чтобы для каждой строки их заново не создавать
        refProviders = new HashMap<String, RefBookDataProvider>();
        refAliases = new HashMap<String, String>();
        RefBook refBook = rbFactory.get(getRefBookValuesAction.getSlaveRefBookId());
        for (RefBookAttribute attribute : refBook.getAttributes()) {
            if (attribute.getAttributeType() == RefBookAttributeType.REFERENCE) {
                refProviders.put(attribute.getAlias(), rbFactory.getDataProvider(attribute.getRefBookId()));
                RefBook refRefBook = rbFactory.get(attribute.getRefBookId());
                RefBookAttribute refAttribute = refRefBook.getAttribute(attribute.getRefBookAttributeId());
                refAliases.put(attribute.getAlias(), refAttribute.getAlias());
            }
        }

        GetRefBookValuesResult result = new GetRefBookValuesResult();
        RefBookDataProvider providerMaster = rbFactory.getDataProvider(getRefBookValuesAction.getRefBookId());


        String filterMaster = DepartmentParamAliases.DEPARTMENT_ID.name() + " = " + getRefBookValuesAction.getDepartmentId();

        Calendar calendarFrom = reportService.getEndDate(getRefBookValuesAction.getReportPeriodId());

        PagingResult<Map<String, RefBookValue>> paramsMaster = providerMaster.getRecords(
                addDayToDate(calendarFrom.getTime(), -1), null, filterMaster, null);
        if (paramsMaster.isEmpty()) {
            return result;
        }
        result.setNotTableValues(convert(paramsMaster, getRefBookValuesAction.getRefBookId(), false).get(0));
        if (paramsMaster.get(0).containsKey(RefBook.RECORD_ID_ALIAS)) {
            result.setRecordId(paramsMaster.get(0).get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue());
        }

        RefBookDataProvider providerSlave = rbFactory.getDataProvider(getRefBookValuesAction.getSlaveRefBookId());
        String filterSlave = "LINK = " + result.getRecordId();
        RefBookAttribute sortAttr = rbFactory.get(getRefBookValuesAction.getSlaveRefBookId()).getAttribute("ROW_ORD");
        PagingResult<Map<String, RefBookValue>> paramsSlave = providerSlave.getRecords(
                addDayToDate(calendarFrom.getTime(), -1), null, filterSlave, sortAttr);
        result.setTableValues(convert(paramsSlave, getRefBookValuesAction.getSlaveRefBookId(), true));

        return result;
    }

    @Override
    public void undo(GetRefBookValuesAction getRefBookValuesAction, GetRefBookValuesResult getRefBookValuesResult, ExecutionContext executionContext) throws ActionException {

    }

    private List<Map<String, TableCell>> convert(List<Map<String, RefBookValue>> data, Long refBookId, boolean needDeref) {
        List<Map<String, TableCell>> converted = new ArrayList<Map<String, TableCell>>();
        for (Map<String, RefBookValue> row : data) {
            converted.add(convertRow(row, refBookId, needDeref));
        }

        return converted;
    }

    private Map<String, TableCell> convertRow(Map<String, RefBookValue> data, Long refBookId, boolean needDeref) {
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
                        if (refProviders.get(a).isRecordsExist(Arrays.asList(data.get(a).getReferenceValue())).isEmpty()) {
                            Map<String, RefBookValue> refValue = refProviders.get(a).getRecordData(data.get(a).getReferenceValue());
                            cell.setDeRefValue(refValue.get(refAliases.get(a)).toString());
                        } else {
                            //Если ссылка на несуществующую запись, то отображаем пустое поле
                            cell.setDeRefValue("");
                        }
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