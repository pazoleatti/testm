package com.aplana.sbrf.taxaccounting.web.module.sources.server.assembler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.service.DepartmentFormTypeService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.DepartmentFormTypeShared;

@Component
public class DeparmentFormTypeAssembler {
	
	@Autowired
	private DepartmentFormTypeService departmentFormTypeService;
	
	@Autowired
	private DepartmentService departmentService;
	
    public List<DepartmentFormTypeShared> assemble(List<DepartmentFormType> departmentFormTypes){
    	List<DepartmentFormTypeShared> departmentFormTypeShareds = new ArrayList<DepartmentFormTypeShared>();
    	int i = 1;
    	for (DepartmentFormType departmentFormType : departmentFormTypes) {
    		DepartmentFormTypeShared departmentFormTypeShared = assemble(departmentFormType);
    		departmentFormTypeShared.setIndex(i++);
			departmentFormTypeShareds.add(departmentFormTypeShared);
		}
    	
		Map<Integer, FormType> formTypes = new HashMap<Integer, FormType>();
		for (DepartmentFormType departmentFormType : departmentFormTypes) {
			formTypes.put(departmentFormType.getFormTypeId(),
					departmentFormTypeService.getFormType(departmentFormType.getFormTypeId()));
		}
		Collections.sort(departmentFormTypes, new DepartmentFormTypeComparator(formTypes));
		
    	return departmentFormTypeShareds;
    }
    
    private DepartmentFormTypeShared assemble(DepartmentFormType departmentFormType){
    	DepartmentFormTypeShared result = new DepartmentFormTypeShared();    	
    	result.setDepartmentName(departmentService.getDepartment(departmentFormType.getDepartmentId()).getName());
    	result.setId(departmentFormType.getId());
    	result.setFormTypeName(departmentFormTypeService.getFormType(departmentFormType.getFormTypeId()).getName());
    	result.setKind(departmentFormType.getKind());
    	return result;
    }

}
