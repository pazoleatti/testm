package com.aplana.sbrf.taxaccounting.web.module.sources.server.assembler;

import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.CurrentAssign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SourcesAssembler {
	
	@Autowired
	private SourceService departmentFormTypeService;
	
	@Autowired
	private DepartmentService departmentService;

    public List<CurrentAssign> assembleDDT(List<DepartmentDeclarationType> departmentDeclarationTypes, TaxType taxType, boolean isControlUNP) {
        List<CurrentAssign> currentAssigns = new ArrayList<CurrentAssign>();
        for (DepartmentDeclarationType departmentDeclarationType : departmentDeclarationTypes) {
            currentAssigns.add(assemble(departmentDeclarationType, taxType, isControlUNP));
        }

        Map<Integer, DeclarationType> declarationTypes = new HashMap<Integer, DeclarationType>();
        for (DepartmentDeclarationType departmentDeclarationType : departmentDeclarationTypes) {
            declarationTypes.put(departmentDeclarationType.getDeclarationTypeId(),
                    departmentFormTypeService.getDeclarationType(departmentDeclarationType.getDeclarationTypeId()));
        }
        Collections.sort(departmentDeclarationTypes, new DepartmentDeclarationTypeComparator(declarationTypes));

        return currentAssigns;
    }

    private CurrentAssign assemble(DepartmentDeclarationType ddt, TaxType taxType, boolean isControlUNP){
        CurrentAssign result = new CurrentAssign();
        result.setTaxType(ddt.getTaxType());
        result.setDepartmentId(ddt.getDepartmentId());
        result.setDepartmentName(departmentService.getParentsHierarchy(ddt.getDepartmentId()));
        result.setId((long) ddt.getId());
        result.setDeclarationType(departmentFormTypeService.getDeclarationType(ddt.getDeclarationTypeId()));
        result.setName(result.getDeclarationType().getName());
        result.setStartDateAssign(ddt.getPeriodStart());
        result.setEndDateAssign(ddt.getPeriodEnd());
        if (!isControlUNP)
            result.setEnabled(taxType == ddt.getTaxType());
        return result;
    }
}
