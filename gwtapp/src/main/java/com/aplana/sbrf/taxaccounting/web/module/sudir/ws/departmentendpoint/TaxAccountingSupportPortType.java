package com.aplana.sbrf.taxaccounting.web.module.sudir.ws.departmentendpoint;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.web.module.sudir.ws.assembler.GenericAccountInfoAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.jws.WebService;
import java.util.List;


@WebService(endpointInterface="com.aplana.sbrf.taxaccounting.web.module.sudir.ws.departmentendpoint.TaxAccountingSupportingData",
			targetNamespace="http://taxaccounting.sbt.ru/SupportingData/",
			serviceName="TaxAccountingSupportingData",
			portName="TaxAccountingSupportingDataSOAP",
			wsdlLocation="META-INF/wsdl/TaxAccountingSupportingData.wsdl")
public class TaxAccountingSupportPortType extends SpringBeanAutowiringSupport{

	@Autowired
	private DepartmentService departmentService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private TAUserService userService;
	
	private GenericAccountInfoAssembler gais = new GenericAccountInfoAssembler(); 
	
	public List<TaxAccDepartment> getDepartments() {
        List<TaxAccDepartment> taxAccDepartmentList = gais.desassembleDepartments(departmentService.getDepartmentForSudir());
        TAUserInfo userInfo = userService.getSystemUserInfo();
        auditService.add(FormDataEvent.EXTERNAL_INTERACTION, userInfo, userInfo.getUser().getDepartmentId(),
                null, null, null, null, "Успешный обмен данными с вебсервисом СУДИР.", null);
		return taxAccDepartmentList;
	}

}
