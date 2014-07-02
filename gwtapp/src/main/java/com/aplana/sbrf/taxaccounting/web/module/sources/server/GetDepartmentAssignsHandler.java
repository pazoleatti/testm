package com.aplana.sbrf.taxaccounting.web.module.sources.server;

import java.util.*;

import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.GetDepartmentAssignsAction;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.GetDepartmentAssignsResult;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.DepartmentAssign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class GetDepartmentAssignsHandler extends AbstractActionHandler<GetDepartmentAssignsAction, GetDepartmentAssignsResult> {

	@Autowired
	private SourceService sourceService;
	

    public GetDepartmentAssignsHandler() {
        super(GetDepartmentAssignsAction.class);
    }

    @Override
    public GetDepartmentAssignsResult execute(GetDepartmentAssignsAction action, ExecutionContext context) throws ActionException {

        List<DepartmentAssign> departmentAssigns = new LinkedList<DepartmentAssign>();
        // TODO а можно сделать что бы с базы данные приходили отсортированными?
        //TODO Должны отображаться только те нф, у которых есть действующий макет в периоде
        Date periodFrom = PeriodConvertor.getDateFrom(action.getPeriodsInterval());
        Date periodTo = PeriodConvertor.getDateTo(action.getPeriodsInterval());
        if(action.isForm()){
            List<DepartmentFormType> depFormAssigns = sourceService.getDFTByDepartment(action.getDepartmentId(), action.getTaxType(), periodFrom, periodTo);
            // если без пейджинга то это формирование норм, с пейджингом нужно выносить в дао формирование
            for (DepartmentFormType dfa : depFormAssigns) {
                DepartmentAssign departmentAssign = new DepartmentAssign();
                departmentAssign.setId(dfa.getId());
                departmentAssign.setDepartmentId(dfa.getDepartmentId());
                departmentAssign.setTypeId(dfa.getFormTypeId());
                departmentAssign.setTypeName(sourceService.getFormType(dfa.getFormTypeId()).getName());
                departmentAssign.setKind(dfa.getKind());
                departmentAssign.setForm(true);
                departmentAssigns.add(departmentAssign);
            }
        } else {
            List<DepartmentDeclarationType> depDecAssigns = sourceService.getDDTByDepartment(action.getDepartmentId(), action.getTaxType(), periodFrom, periodTo);
            for (DepartmentDeclarationType dda : depDecAssigns) {
                DepartmentAssign departmentAssign = new DepartmentAssign();
                departmentAssign.setId((long) dda.getId());
                departmentAssign.setDepartmentId(dda.getDepartmentId());
                departmentAssign.setTypeId(dda.getDeclarationTypeId());
                departmentAssign.setTypeName(sourceService.getDeclarationType(dda.getDeclarationTypeId()).getName());
                departmentAssign.setForm(false);
                departmentAssigns.add(departmentAssign);
            }
        }

        GetDepartmentAssignsResult result = new GetDepartmentAssignsResult();
        result.setDepartmentAssigns(departmentAssigns);

		return result;
    }

    @Override
    public void undo(GetDepartmentAssignsAction action, GetDepartmentAssignsResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }
}
