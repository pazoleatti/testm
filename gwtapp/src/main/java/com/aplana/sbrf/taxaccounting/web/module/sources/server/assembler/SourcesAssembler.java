package com.aplana.sbrf.taxaccounting.web.module.sources.server.assembler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.CurrentAssign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;

@Component
public class SourcesAssembler {
	
	@Autowired
	private SourceService departmentFormTypeService;
	
	@Autowired
	private DepartmentService departmentService;

    public List<CurrentAssign> assembleDFT(List<DepartmentFormType> departmentFormTypes, TaxType taxType, boolean isControlUNP){
    	List<CurrentAssign> currentAssigns = new ArrayList<CurrentAssign>();
    	for (DepartmentFormType departmentFormType : departmentFormTypes) {
			currentAssigns.add(assemble(departmentFormType, taxType, isControlUNP));
		}
    	
		Map<Integer, FormType> formTypes = new HashMap<Integer, FormType>();
		for (DepartmentFormType departmentFormType : departmentFormTypes) {
			formTypes.put(departmentFormType.getFormTypeId(),
					departmentFormTypeService.getFormType(departmentFormType.getFormTypeId()));
		}
		Collections.sort(departmentFormTypes, new DepartmentFormTypeComparator(formTypes));
		
    	return currentAssigns;
    }
    
    private CurrentAssign assemble(DepartmentFormType dft, TaxType taxType, boolean isControlUNP){
    	CurrentAssign result = new CurrentAssign();
        result.setTaxType(dft.getTaxType());
        result.setDepartmentId(dft.getDepartmentId());
    	result.setDepartmentName(departmentService.getParentsHierarchy(dft.getDepartmentId()));
    	result.setId(dft.getId());
        result.setFormType(departmentFormTypeService.getFormType(dft.getFormTypeId()));
    	result.setName(result.getFormType().getName());
    	result.setFormKind(dft.getKind());
        result.setStartDateAssign(dft.getPeriodStart());
        result.setEndDateAssign(dft.getPeriodEnd());
        if (!isControlUNP)
            result.setEnabled(taxType == dft.getTaxType());
    	return result;
    }

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
