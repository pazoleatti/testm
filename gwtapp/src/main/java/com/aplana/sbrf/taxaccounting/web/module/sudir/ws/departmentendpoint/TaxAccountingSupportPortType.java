package com.aplana.sbrf.taxaccounting.web.module.sudir.ws.departmentendpoint;

import java.util.List;

import javax.jws.WebService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.module.sudir.ws.assembler.GenericAccountInfoAssembler;


@WebService(endpointInterface="com.aplana.sbrf.taxaccounting.web.module.sudir.ws.departmentendpoint.TaxAccountingSupportingData",
			targetNamespace="http://taxaccounting.sbt.ru/SupportingData/",
			serviceName="TaxAccountingSupportingData",
			portName="TaxAccountingSupportingDataSOAP",
			wsdlLocation="META-INF/wsdl/TaxAccountingSupportingData.wsdl")
public class TaxAccountingSupportPortType extends SpringBeanAutowiringSupport{

	@Autowired
	private DepartmentService departmentService;
	
	private GenericAccountInfoAssembler gais = new GenericAccountInfoAssembler(); 
	
	public List<TaxAccDepartment> getDepartments() {
		return gais.desassembleDepartments(departmentService.listDepartments());
	}

}
