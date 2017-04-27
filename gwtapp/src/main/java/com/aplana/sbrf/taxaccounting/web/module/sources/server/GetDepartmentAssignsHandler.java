package com.aplana.sbrf.taxaccounting.web.module.sources.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.source.SourceMode;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.GetDepartmentAssignsAction;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.GetDepartmentAssignsResult;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.DepartmentAssign;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;

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
        Date periodFrom = PeriodConvertor.getDateFrom(action.getPeriodsInterval());
        Date periodTo = PeriodConvertor.getDateTo(action.getPeriodsInterval());
        QueryParams queryParams = new QueryParams();
        queryParams.setSearchOrdering(action.getOrdering());
        queryParams.setAscending(action.isAscSorting());
        List<DepartmentDeclarationType> depDecAssigns = sourceService.getDDTByDepartment(action.getDepartmentId(),
                action.getTaxType(), periodFrom, periodTo, queryParams);
        for (DepartmentDeclarationType dda : depDecAssigns) {
            DepartmentAssign departmentAssign = new DepartmentAssign();
            departmentAssign.setId((long) dda.getId());
            departmentAssign.setDepartmentId(dda.getDepartmentId());
            departmentAssign.setTypeId(dda.getDeclarationTypeId());
            departmentAssign.setTypeName(sourceService.getDeclarationType(dda.getDeclarationTypeId()).getName());
            departmentAssign.setDeclaration(true);
            departmentAssigns.add(departmentAssign);
        }

        /** Получаем уже назначенные связки и обрезаем их */
        DepartmentAssign selectedLeftObject = action.getSelectedLeft();
        List<ComparableSourceObject> impossibleAssign = new ArrayList<ComparableSourceObject>();

        if (selectedLeftObject != null) {
            if (action.isForm()) {
                List<DepartmentFormType> departmentImpFormTypes = new ArrayList<DepartmentFormType>();
                if (!selectedLeftObject.isDeclaration()) {
                    if (action.getMode() == SourceMode.SOURCES) {
                        departmentImpFormTypes = sourceService.
                                getFormDestinations(selectedLeftObject.getDepartmentId(), selectedLeftObject.getTypeId(), selectedLeftObject.getKind(), periodFrom, periodTo);
                    } else {
                        departmentImpFormTypes = sourceService.
                                getDFTSourcesByDFT(selectedLeftObject.getDepartmentId(), selectedLeftObject.getTypeId(),
                                        selectedLeftObject.getKind(), periodFrom, periodTo, queryParams);
                    }
                    impossibleAssign.add(new ComparableSourceObject(selectedLeftObject.getKind(), selectedLeftObject.getTypeId(), null, selectedLeftObject.getDepartmentId()));
                }
                for (DepartmentFormType dft : departmentImpFormTypes) {
                    impossibleAssign.add(new ComparableSourceObject(dft.getKind(), dft.getFormTypeId(), null, dft.getDepartmentId()));
                }
            }

            Iterator<DepartmentAssign> it = departmentAssigns.iterator();
            while (it.hasNext()) {
                DepartmentAssign assign = it.next();
                if (!assign.isDeclaration() || action.getMode() == SourceMode.SOURCES) {
                    for (ComparableSourceObject currentAssign : impossibleAssign) {
                        if (assign.getKind() == currentAssign.formKind && assign.getTypeId() == currentAssign.formTypeId && action.getDepartmentId() == currentAssign.departmentId) {
                            assign.setEnabled(false);
                            assign.setChecked(false);
                            break;
                        }
                    }
                } else {
                    for (ComparableSourceObject currentAssign : impossibleAssign) {
                        if (assign.getTypeId() == currentAssign.declarationTypeId && action.getDepartmentId() == currentAssign.departmentId) {
                            assign.setEnabled(false);
                            assign.setChecked(false);
                            break;
                        }
                    }
                }
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

    private final class ComparableSourceObject {
        private FormDataKind formKind;
        private Integer formTypeId;
        private Integer declarationTypeId;
        private int departmentId;

        private ComparableSourceObject(FormDataKind formKind, Integer formTypeId, Integer declarationTypeId, int departmentId) {
            this.formKind = formKind;
            this.formTypeId = formTypeId;
            this.declarationTypeId = declarationTypeId;
            this.departmentId = departmentId;
        }
    }
}
