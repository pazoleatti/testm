package com.aplana.sbrf.taxaccounting.web.module.sudir.ws.assembler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.web.module.sudir.ws.accountendpoint.ArrayOfGenericAttribute;
import com.aplana.sbrf.taxaccounting.web.module.sudir.ws.accountendpoint.ArrayOfXsdString;
import com.aplana.sbrf.taxaccounting.web.module.sudir.ws.accountendpoint.GenericAccountInfo;
import com.aplana.sbrf.taxaccounting.web.module.sudir.ws.accountendpoint.GenericAttribute;
import com.aplana.sbrf.taxaccounting.web.module.sudir.ws.departmentendpoint.TaxAccDepartment;

public class GenericAccountInfoAssembler {
	
	private static Map<String, Integer> fieldNames = FieldNames.getFieldNamesMap();
	
	public TAUser assembleUser(GenericAccountInfo gai){
		TAUser user = new TAUser();
		for (GenericAttribute item : gai.getAttributes().getItem()) {
			switch (fieldNames.get(item.getName())) {
			case 0:
				user.setLogin(item.getValues().getItem().get(0));
				break;
			case 1:
				StringBuilder sb = new StringBuilder();
				for (String value : item.getValues().getItem()){
					sb.append(value);
					sb.append(" ");
				}
				user.setName(sb.toString().trim());
				break;
			case 2:
				user.setDepartmentId(Integer.valueOf(item.getValues().getItem().get(0)));
				
				break;
			case 3:
				user.setActive(Boolean.valueOf(item.getValues().getItem().get(0)));
				
				break;
			case 4:
				user.setEmail(item.getValues().getItem().get(0));
				
				break;
			case 5:
				List<TARole> listRoles = new ArrayList<TARole>();
				for (String value : item.getValues().getItem()){
					TARole role = new TARole();
					role.setAlias(value);
					listRoles.add(role);
				}
				user.setRoles(listRoles);
				
				break;
				
			}
		}
		return user;
	}
	
	public List<GenericAccountInfo> desassembleUsers(List<TAUser> users){
		List<GenericAccountInfo> listGenericAccountInfo = new ArrayList<GenericAccountInfo>();
		
		for (TAUser taUser : users) {
			GenericAccountInfo gai = new GenericAccountInfo();
			ArrayOfGenericAttribute aOfga = new ArrayOfGenericAttribute();
			GenericAttribute ga = new GenericAttribute();
			ArrayOfXsdString aOfxs = new ArrayOfXsdString();
			
			
			//NAME
			ga.setName(FieldNames.NAME.nameField());
			aOfxs.getItem().add(taUser.getName());
			ga.setValues(aOfxs);
			aOfga.getItem().add(ga);
			
			//ROLE_CODE
			ga = new GenericAttribute();
			aOfxs = new ArrayOfXsdString();
			ga.setName(FieldNames.ROLE_CODE.nameField());
			for(TARole role : taUser.getRoles()){
				aOfxs.getItem().add(role.getAlias());
			}
			ga.setValues(aOfxs);
			aOfga.getItem().add(ga);
			
			//LOGIN
			ga = new GenericAttribute();
			aOfxs = new ArrayOfXsdString();
			ga.setName(FieldNames.LOGIN.nameField());
			aOfxs.getItem().add(taUser.getLogin());
			ga.setValues(aOfxs);
			aOfga.getItem().add(ga);
			
			//DEPARTAMENT
			ga = new GenericAttribute();
			aOfxs = new ArrayOfXsdString();
			ga.setName(FieldNames.DEPARTAMENT_ID.nameField());
			aOfxs.getItem().add(Integer.toString(taUser.getDepartmentId()));
			ga.setValues(aOfxs);
			aOfga.getItem().add(ga);
			
			//IS_ACTIVE
			ga = new GenericAttribute();
			aOfxs = new ArrayOfXsdString();
			ga.setName(FieldNames.IS_ACTIVE.nameField());
			aOfxs.getItem().add(Boolean.toString(taUser.isActive()));
			ga.setValues(aOfxs);
			aOfga.getItem().add(ga);
			
			//EMAIL
			ga = new GenericAttribute();
			aOfxs = new ArrayOfXsdString();
			ga.setName(FieldNames.EMAIL.nameField());
			aOfxs.getItem().add(taUser.getEmail());
			ga.setValues(aOfxs);
			aOfga.getItem().add(ga);
			
			gai.setAttributes(aOfga);
			listGenericAccountInfo.add(gai);
		}
		
		return listGenericAccountInfo;
		
	}
	
	public List<TaxAccDepartment> desassembleDepartments(List<Department> listDepartments){
		List<TaxAccDepartment> listTaxAccDepartments = new ArrayList<TaxAccDepartment>();
		for (Department department : listDepartments) {
			TaxAccDepartment taxAccDepartment = new TaxAccDepartment();
			taxAccDepartment.setId(Integer.toString(department.getId()));
			taxAccDepartment.setName(department.getName());
			if(department.getParentId()!=null)
				taxAccDepartment.setParentId(Integer.toString(department.getParentId()));
			else
				taxAccDepartment.setParentId(Integer.toString(0));
			
			listTaxAccDepartments.add(taxAccDepartment);
		}
		
		return listTaxAccDepartments;
		
	}

}
