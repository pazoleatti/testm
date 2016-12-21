package com.aplana.sbrf.taxaccounting.web.module.declarationlist.server;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetDeclarationTypeAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetDeclarationTypeResult;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.server.DepartmentParamAliases;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GetDeclarationTypeForCreateHandler extends AbstractActionHandler<GetDeclarationTypeAction, GetDeclarationTypeResult> {

    public GetDeclarationTypeForCreateHandler() {
        super(GetDeclarationTypeAction.class);
    }

    @Autowired
    DeclarationTypeService declarationTypeService;

    @Autowired
    DepartmentReportPeriodService departmentReportPeriodService;

    @Autowired
    PeriodService periodService;

    @Autowired
    RefBookFactory rbFactory;

    @Override
    public GetDeclarationTypeResult execute(GetDeclarationTypeAction action, ExecutionContext executionContext) throws ActionException {
        GetDeclarationTypeResult result = new GetDeclarationTypeResult();
        result.setDeclarationTypes(declarationTypeService.getTypes(action.getDepartmentId(), action.getReportPeriod(), action.getTaxType()));
        result.setCorrectionDate(departmentReportPeriodService.getLast(action.getDepartmentId(), action.getReportPeriod()).getCorrectionDate());
        result.setTaxType(action.getTaxType());

        if (action.getTaxType() == TaxType.PROPERTY || action.getTaxType() == TaxType.TRANSPORT || action.getTaxType() == TaxType.INCOME || action.getTaxType() == TaxType.LAND) {
            RefBookDataProvider provider = rbFactory.getDataProvider(RefBook.WithTable.getByTaxType(action.getTaxType()).getRefBookId());
            ReportPeriod period = periodService.getReportPeriod(action.getReportPeriod());
            Date version = DateUtils.addDays(period.getEndDate(), -1);
            String filter = DepartmentParamAliases.DEPARTMENT_ID.name() + " = " + action.getDepartmentId();

            PagingResult<Map<String, RefBookValue>> records = provider.getRecords(version, null, filter, null);
            if (records != null && !records.isEmpty()) {
                result.setFilter("LINK = " + records.get(0).get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue());
            }
            // для выставления дат актуальности у виджетов выбора из справочника
            result.setVersion(version);
        }
        return result;
    }

    @Override
    public void undo(GetDeclarationTypeAction getDeclarationTypeAction, GetDeclarationTypeResult getDeclarationTypeResult, ExecutionContext executionContext) throws ActionException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
