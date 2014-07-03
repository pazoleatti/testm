package com.aplana.sbrf.taxaccounting.web.module.sources.server.assembler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.CurrentAssign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;

@Component
public class DeparmentFormTypeAssembler {
	
	@Autowired
	private SourceService departmentFormTypeService;
	
	@Autowired
	private DepartmentService departmentService;
	
    public List<CurrentAssign> assemble(List<DepartmentFormType> departmentFormTypes){
    	List<CurrentAssign> currentAssigns = new ArrayList<CurrentAssign>();
    	for (DepartmentFormType departmentFormType : departmentFormTypes) {
			currentAssigns.add(assemble(departmentFormType));
		}
    	
		Map<Integer, FormType> formTypes = new HashMap<Integer, FormType>();
		for (DepartmentFormType departmentFormType : departmentFormTypes) {
			formTypes.put(departmentFormType.getFormTypeId(),
					departmentFormTypeService.getFormType(departmentFormType.getFormTypeId()));
		}
		Collections.sort(departmentFormTypes, new DepartmentFormTypeComparator(formTypes));
		
    	return currentAssigns;
    }
    
    private CurrentAssign assemble(DepartmentFormType departmentFormType){
    	CurrentAssign result = new CurrentAssign();
    	result.setDepartmentName(departmentService.getParentsHierarchy(departmentFormType.getDepartmentId()));
    	result.setId(departmentFormType.getId());
        result.setFormType(departmentFormTypeService.getFormType(departmentFormType.getFormTypeId()));
    	result.setName(result.getFormType().getName());
    	result.setFormKind(departmentFormType.getKind());
        result.setStartDateAssign(departmentFormType.getPeriodStart());
        result.setEndDateAssign(departmentFormType.getPeriodEnd());
    	return result;
    }

}
