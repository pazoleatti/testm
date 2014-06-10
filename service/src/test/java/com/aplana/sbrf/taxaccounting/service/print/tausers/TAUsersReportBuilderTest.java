package com.aplana.sbrf.taxaccounting.service.print.tausers;

import com.aplana.sbrf.taxaccounting.model.*;
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

    private List<TAUserView> taUserList = new ArrayList<TAUserView>();

    @Before
    public void setUp() {
        TAUserView userView = new TAUserView();

        userView.setName("Контролер");
        userView.setActive(false);
        userView.setEmail("@sd");
        userView.setDepId(1);
        userView.setLogin("controlBank");
        userView.setRoles("Контролёр, Контролёр УНП");
        userView.setDepName("Департамент");

        taUserList.add(userView);
        taUserList.add(userView);
        taUserList.add(userView);

    }

    @Test
    public void createReportTest() throws IOException {
        TAUsersReportBuilder report = new TAUsersReportBuilder(taUserList);
        report.createReport();
    }
}
