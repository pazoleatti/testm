package com.aplana.sbrf.taxaccounting.web.module.sudir.ws.assembler;

import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.web.module.sudir.ws.accountendpoint.*;
import com.aplana.sbrf.taxaccounting.web.module.sudir.ws.validation.ValidationService;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: avanteev
 */
public class ValidationServiceTest {

    private GenericAccountInfoAssembler assembler = new GenericAccountInfoAssembler();
    private ValidationService validationService = new ValidationService();

    @Test
    public void test() throws GenericAccountManagementException_Exception {
        TAUser user = new TAUser();
        user.setId(1);
        user.setActive(true);
        user.setName("fdjg");
        user.setLogin("sbrf");
        user.setDepartmentId(1);
        ArrayList<TARole> roles = new ArrayList<TARole>();
        roles.add(new TARole());
        user.setRoles(roles);
        List<GenericAccountInfo> accountInfos = assembler.desassembleUsers(Arrays.asList(user));
        validationService.validate(accountInfos.get(0));
    }

    @Test(expected = GenericAccountManagementException_Exception.class)
    public void testFail() throws GenericAccountManagementException_Exception {
        TAUser taUser = new TAUser();
        taUser.setId(1);
        taUser.setActive(true);
        taUser.setName("fdjg");
        taUser.setDepartmentId(1);
        ArrayList<TARole> roles = new ArrayList<TARole>();
        roles.add(new TARole());
        taUser.setRoles(roles);

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
        try {
            validationService.validate(gai);
        } catch (GenericAccountManagementException_Exception e) {
            e.printStackTrace();
            throw new GenericAccountManagementException_Exception(e.getLocalizedMessage(), e.getFaultInfo());
        }

    }
}
