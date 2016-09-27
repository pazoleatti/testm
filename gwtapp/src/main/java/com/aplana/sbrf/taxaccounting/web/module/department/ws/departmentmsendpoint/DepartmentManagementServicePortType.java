package com.aplana.sbrf.taxaccounting.web.module.department.ws.departmentmsendpoint;

import javax.jws.WebService;

/**
 * Created by lhaziev on 27.09.2016.
 */
@WebService(endpointInterface="com.aplana.sbrf.taxaccounting.web.module.department.ws.departmentmsendpoint.DepartmentManagementService",
        targetNamespace = "http://taxaccounting.sbrf.aplana.com/DepartmentManagementService/",
        serviceName="DepartmentManagementService",
        portName="DepartmentManagementServicePort",
        wsdlLocation="META-INF/wsdl/DepartmentManagementService.wsdl")
public class DepartmentManagementServicePortType implements DepartmentManagementService {
    @Override
    public TaxDepartmentChanges getAllChanges() {
        TaxDepartmentChanges result = new TaxDepartmentChanges();
        result.setErrorCode("E0");
        TaxDepartmentChange taxDepartmentChange1 = new TaxDepartmentChange();
        taxDepartmentChange1.setId(1);
        taxDepartmentChange1.setOperationType(0);
        result.getTaxDepartmentChanges().add(taxDepartmentChange1);
        return result;
    }
}
