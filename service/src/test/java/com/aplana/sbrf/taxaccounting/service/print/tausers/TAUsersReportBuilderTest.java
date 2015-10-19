package com.aplana.sbrf.taxaccounting.service.print.tausers;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.impl.print.tausers.TAUsersReportBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
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
        userView.setActive(true);
        userView.setEmail("@sd");
        userView.setDepId(1);
        userView.setLogin("controlBank");
        userView.setRoles("Контролёр, Контролёр УНП");
        userView.setDepName("Департамент");

        TAUserView userView2 = new TAUserView();
        userView2.setName("Контролер2");
        userView2.setActive(false);
        userView2.setEmail("@gmail.com");
        userView2.setDepId(1);
        userView2.setLogin("controlBank");
        userView2.setRoles("Контролёр, Контролёр УНП");
        userView2.setDepName("Департамент");

        taUserList.add(userView);
        taUserList.add(userView2);
        taUserList.add(userView);

    }

    @Test
    public void createReportTest() throws IOException {
        TAUsersReportBuilder report = new TAUsersReportBuilder(taUserList);
        String reportPath = null;
        try {
            reportPath = report.createReport();
        } finally {
            assert reportPath != null;
            File file = new File(reportPath);
            file.delete();
        }
    }
}