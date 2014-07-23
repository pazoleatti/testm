package com.aplana.sbrf.taxaccounting.web.module.sources.server;

import java.util.*;

import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.source.SourceMode;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.GetDepartmentAssignsAction;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.GetDepartmentAssignsResult;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.CurrentAssign;
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
                departmentAssign.setDeclaration(false);
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
                departmentAssign.setDeclaration(true);
                departmentAssigns.add(departmentAssign);
            }
        }

        /** Получаем уже назначенные связки и обрезаем их */
        DepartmentAssign selectedLeftObject = action.getSelectedLeft();
        List<ComparableSourceObject> currentAssigns = new ArrayList<ComparableSourceObject>();
        if (selectedLeftObject != null) {
            if (action.isForm()) {
                List<DepartmentFormType> departmentFormTypes;
                if (selectedLeftObject.isDeclaration()) {
                    departmentFormTypes = sourceService.
                            getDFTSourceByDDT(selectedLeftObject.getDepartmentId(), selectedLeftObject.getTypeId(), periodFrom, periodTo);
                } else {
                    if (action.getMode() == SourceMode.SOURCES) {
                        departmentFormTypes = sourceService.
                                getDFTSourcesByDFT(selectedLeftObject.getDepartmentId(), selectedLeftObject.getTypeId(), selectedLeftObject.getKind(), periodFrom, periodTo);
                    } else {
                        departmentFormTypes = sourceService.
                                getFormDestinations(selectedLeftObject.getDepartmentId(), selectedLeftObject.getTypeId(), selectedLeftObject.getKind(), periodFrom, periodTo);
                    }
                }
                for (DepartmentFormType dft : departmentFormTypes) {
                    currentAssigns.add(new ComparableSourceObject(dft.getKind(), dft.getFormTypeId(), null, dft.getDepartmentId()));
                }
            } else {
                if (action.getMode() == SourceMode.SOURCES) {
                    List<DepartmentFormType> departmentFormTypes = sourceService
                            .getDFTSourceByDDT(selectedLeftObject.getDepartmentId(), selectedLeftObject.getTypeId(), periodFrom, periodTo);
                    for (DepartmentFormType dft : departmentFormTypes) {
                        currentAssigns.add(new ComparableSourceObject(dft.getKind(), dft.getFormTypeId(), null, dft.getDepartmentId()));
                    }
                } else {
                    List<DepartmentDeclarationType> departmentFormTypes = sourceService.
                            getDeclarationDestinations(selectedLeftObject.getDepartmentId(), selectedLeftObject.getTypeId(), selectedLeftObject.getKind(), periodFrom, periodTo);
                    for (DepartmentDeclarationType ddt : departmentFormTypes) {
                        currentAssigns.add(new ComparableSourceObject(null, null, ddt.getDeclarationTypeId(), ddt.getDepartmentId()));
                    }
                }
            }

            Iterator<DepartmentAssign> it = departmentAssigns.iterator();
            while (it.hasNext()) {
                DepartmentAssign assign = it.next();
                if (!assign.isDeclaration() || action.getMode() == SourceMode.SOURCES) {
                    for (ComparableSourceObject currentAssign : currentAssigns) {
                        if (assign.getKind() == currentAssign.formKind && assign.getTypeId() == currentAssign.formTypeId && action.getDepartmentId() == currentAssign.departmentId) {
                            it.remove();
                            break;
                        }
                    }
                } else {
                    for (ComparableSourceObject currentAssign : currentAssigns) {
                        if (assign.getTypeId() == currentAssign.declarationTypeId && action.getDepartmentId() == currentAssign.departmentId) {
                            it.remove();
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

    private class ComparableSourceObject {
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
