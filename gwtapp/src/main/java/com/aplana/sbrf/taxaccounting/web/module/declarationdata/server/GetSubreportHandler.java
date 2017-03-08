package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.*;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author lhaziev
 *
 */
@Service
@PreAuthorize("hasAnyRole('N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_OPER', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class GetSubreportHandler extends AbstractActionHandler<GetSubreportAction, GetSubreportResult> {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private DeclarationDataService declarationDataService;

    @Autowired
    private DeclarationTemplateService declarationTemplateService;

    @Autowired
    private PeriodService periodService;

    @Autowired
    private RefBookFactory rbFactory;

    public GetSubreportHandler() {
        super(GetSubreportAction.class);
    }

    @Override
    public GetSubreportResult execute(GetSubreportAction action, ExecutionContext executionContext) throws ActionException {
        GetSubreportResult result = new GetSubreportResult();
        TAUserInfo userInfo = securityService.currentUserInfo();
        DeclarationData declarationData = declarationDataService.get(action.getDeclarationId(), userInfo);
        Map<Long, RefBookParamInfo> refBookParamInfoMap = new HashMap<Long, RefBookParamInfo>();
        for(DeclarationSubreport subreport: declarationTemplateService.get(declarationData.getDeclarationTemplateId()).getSubreports()) {
            if (subreport.getId() == action.getDeclarationSubreportId()) {
                result.setSelectRecord(subreport.isSelectRecord());
                for (DeclarationSubreportParam declarationSubreportParam : subreport.getDeclarationSubreportParams()) {
                    if (declarationSubreportParam.getType().equals(DeclarationSubreportParamType.REFBOOK)) {
                        RefBook refBook = rbFactory.getByAttribute(declarationSubreportParam.getRefBookAttributeId());
                        refBookParamInfoMap.put(declarationSubreportParam.getRefBookAttributeId(), new RefBookParamInfo(refBook.getName(), refBook.isVersioned(), refBook.isHierarchic()));
                    }
                }
            }
        }

        result.setRefBookParamInfoMap(refBookParamInfoMap);
        if (refBookParamInfoMap.size() > 0) {
            ReportPeriod reportPeriod = periodService.getReportPeriod(declarationData.getReportPeriodId());
            result.setStartDate(reportPeriod.getCalendarStartDate());
            result.setEndDate(reportPeriod.getEndDate());
        }
        return result;
    }


    @Override
    public void undo(GetSubreportAction action, GetSubreportResult result, ExecutionContext executionContext) throws ActionException {

    }

}
