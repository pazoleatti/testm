package com.aplana.sbrf.taxaccounting.service.print.tausers;

import com.aplana.sbrf.taxaccounting.model.TAUserView;
import com.aplana.sbrf.taxaccounting.service.impl.print.tausers.TAUsersReportBuilder;
import com.aplana.sbrf.taxaccounting.service.print.AbstractReportBuilderTest;
import org.apache.commons.lang3.time.FastDateFormat;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TAUsersReportBuilderTest extends AbstractReportBuilderTest {

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
    @Ignore
    public void createReportTest() throws Exception {
        TAUsersReportBuilder reportBuilder = new TAUsersReportBuilder(taUserList);

        String reportPath = null;
        try {
            reportPath = reportBuilder.createReport();

            String[][] rowsExpected = new String[][]{
                    {"", "", "", "Список пользователей"},
                    {"", "", "", FastDateFormat.getInstance("dd-MMM-yyyy").format(new Date())},
                    {""},
                    {"Полное имя пользователя", "Логин", "Электронная почта", "Признак активности", "Подразделение", "Роль"},
                    {"Контролер", "controlBank", "@sd", "Да", "Департамент", "Контролёр, Контролёр УНП"},
                    {"Контролер2", "controlBank", "@gmail.com", "Нет", "Департамент", "Контролёр, Контролёр УНП"},
                    {"Контролер", "controlBank", "@sd", "Да", "Департамент", "Контролёр, Контролёр УНП"}
            };
            assertEquals(toList(rowsExpected).toString(),
                    readExcelFile(reportPath).toString());
        } finally {
            if (reportPath != null) {
                File file = new File(reportPath);
                file.delete();
            }
        }
    }
}