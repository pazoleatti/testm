package com.aplana.sbrf.taxaccounting.service.print.tausers;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TAUserFull;
import com.aplana.sbrf.taxaccounting.service.impl.print.tausers.TAUsersReportBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: avanteev
 */
public class TAUsersReportBuilderTest {

    private List<TAUserFull> taUserList = new ArrayList<TAUserFull>();

    @Before
    public void setUp() {
        TAUserFull  userFull = new TAUserFull();
        TAUser user = new TAUser();
        TARole role = new TARole();
        TARole role1 = new TARole();
        role.setAlias("ROLE_CONTROL");
        role1.setAlias("ROLE_CONTROL_UNP");
        List<TARole> roleList = new ArrayList<TARole>();
        roleList.add(role);
        roleList.add(role1);

        user.setActive(false);
        user.setEmail("@sd");
        user.setDepartmentId(1);
        user.setLogin("controlBank");
        user.setRoles(roleList);
        user.setName("Контролер");
        userFull.setUser(user);

        Department dep = new Department();
        dep.setName("Департамент");
        userFull.setDepartment(dep);

        taUserList.add(userFull);
        taUserList.add(userFull);
        taUserList.add(userFull);

    }

    @Test
    public void createReportTest() throws IOException {
        TAUsersReportBuilder report = new TAUsersReportBuilder(taUserList);
        report.createReport();
    }
}
