package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.SourcesAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.SourcesResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Обработчик получения списка источников/приемников
 */
@Service
@PreAuthorize("hasAnyRole('N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_OPER', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class DeclarationSourcesHandler extends AbstractActionHandler<SourcesAction, SourcesResult> {

    @Autowired
    SourceService sourceService;
    @Autowired
    DeclarationDataService declarationDataService;
    @Autowired
    SecurityService securityService;
    @Autowired
    DeclarationDataScriptingService scriptingService;

    public DeclarationSourcesHandler() {
        super(SourcesAction.class);
    }

    @Override
    public SourcesResult execute(SourcesAction action, ExecutionContext executionContext) throws ActionException {
        SourcesResult result = new SourcesResult();
        if (!declarationDataService.existDeclarationData(action.getDeclarationId())) {
            result.setExistDeclarationData(false);
            result.setDeclarationDataId(action.getDeclarationId());
            return result;
        }
        TAUserInfo userInfo = securityService.currentUserInfo();
        Logger logger = new Logger();
        DeclarationData declaration = declarationDataService.get(action.getDeclarationId(), userInfo);
        List<RelationViewModel> relationViewModelList = new ArrayList<>();

        //Получаем нф-источники
        relationViewModelList.addAll(toRelationViewModel(sourceService.getDeclarationSourcesInfo(declaration, true, false, null, userInfo, logger)));
        relationViewModelList.addAll(toRelationViewModel(sourceService.getDeclarationDestinationsInfo(declaration, true, false, null, userInfo, logger)));
        result.setData(relationViewModelList);
        return result;
    }

    private List<RelationViewModel> toRelationViewModel(List<Relation> relationList) {
        List<RelationViewModel> relationViewModelList = new ArrayList<>();
        for (Relation relation : relationList){
            RelationViewModel model = new RelationViewModel();
            model.setAccruing(relation.isAccruing());
            model.setAsnuId(relation.getAsnuId());
            model.setComparativePeriod(toPeriodViewModel(relation.getComparativePeriod()));
            model.setComparativePeriodName(relation.getComparativePeriodName());
            model.setComparativePeriodStartDate(relation.getComparativePeriodStartDate());
            model.setComparativePeriodYear(relation.getComparativePeriodYear());
            model.setCorrectionDate(relation.getCorrectionDate());
            model.setCreated(relation.isCreated());
            model.setDeclarationDataId(relation.getDeclarationDataId());
            model.setDeclarationState(relation.getDeclarationState());
            model.setDeclarationTemplate(relation.getDeclarationTemplate());
            model.setDeclarationTypeName(relation.getDeclarationTypeName());
            model.setDepartment(relation.getDepartment());
            model.setDepartmentId(relation.getDepartmentId());
            model.setDepartmentReportPeriod(toPeriodViewModel(relation.getDepartmentReportPeriod()));
            model.setFormDataId(relation.getFormDataId());
            model.setFormDataKind(relation.getFormDataKind());
            model.setFormTypeName(relation.getFormTypeName());
            model.setFullDepartmentName(relation.getFullDepartmentName());
            model.setKpp(relation.getKpp());
            model.setManual(relation.isManual());
            model.setMonth(relation.getMonth());
            model.setPerformerNames(relation.getPerformerNames());
            model.setPerformers(relation.getPerformers());
            model.setPeriodName(relation.getPeriodName());
            model.setSource(relation.isSource());
            model.setStatus(relation.isStatus());
            model.setTaxOrganCode(relation.getTaxOrganCode());
            model.setTaxType(relation.getTaxType());
            model.setYear(relation.getYear());
            relationViewModelList.add(model);
        }
        return relationViewModelList;
    }

    private DepartmentReportPeriodViewModel toPeriodViewModel(DepartmentReportPeriod period) {

        return new DepartmentReportPeriodViewModel(period.getId(), toReportPeriodViewModel(period.getReportPeriod()), period.getDepartmentId(), period.isActive(), period.getCorrectionDate().toDate());
    }

    private ReportPeriodViewModel toReportPeriodViewModel(ReportPeriod reportPeriod) {
        ReportPeriodViewModel viewModel = new ReportPeriodViewModel();
        viewModel.setId(reportPeriod.getId());
        viewModel.setName(reportPeriod.getName());
        viewModel.setAccName(reportPeriod.getAccName());
        viewModel.setOrder(reportPeriod.getOrder());
        viewModel.setTaxPeriod(reportPeriod.getTaxPeriod());
        viewModel.setStartDate(reportPeriod.getStartDate().toDate());
        viewModel.setEndDate(reportPeriod.getEndDate().toDate());
        viewModel.setCalendarStartDate(reportPeriod.getCalendarStartDate().toDate());
        viewModel.setDictTaxPeriodId(reportPeriod.getDictTaxPeriodId());
        return viewModel;
    }

    @Override
    public void undo(SourcesAction sourcesAction, SourcesResult sourcesResult, ExecutionContext executionContext) throws ActionException {

    }


}
