package com.aplana.sbrf.taxaccounting.service.print.refbook;

import com.aplana.sbrf.taxaccounting.service.impl.print.refbook.RefBookCSVReportBuilder;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class RefBookCSVReportBuilderTest extends AbstractRefBookReportBuilderTest {

    @Test
    public void buildLinearRefBookReportTest() throws Exception {
        RefBookCSVReportBuilder reportBuilder = new RefBookCSVReportBuilder(linearRefBook, linearAttributes, linearRecords,
                version, "ася", true, sortAttribute);

        String reportPath = null;
        try {
            reportPath = reportBuilder.createReport();

            String[][] rowsExpected = new String[][]{
                    {"Справочник: Линейный справочник"},
                    {"Дата актуальности: 01.01.2018"},
                    {"Параметр поиска: ася(по точному совпадению)"},
                    {""},
                    {"Наименование", "Число", "Дата начала актуальности", "Дата окончания актуальности"},
                    {"Вася", "111", "01.01.2017", "01.01.2019"},
                    {"Дася", "222", "01.01.2017", "01.01.2019"},
                    {"Мася", "333", "01.01.2017", "01.01.2019"}
            };
            assertEquals(toList(rowsExpected).toString(),
                    readCsvFile(reportPath, null).toString());
        } finally {
            if (reportPath != null) {
                File file = new File(reportPath);
                file.delete();
            }
        }
    }

    @Test
    public void buildHierRefBookReportTest() throws Exception {
        RefBookCSVReportBuilder reportBuilder = new RefBookCSVReportBuilder(hierRefBook, hierAttributes, hierRecords,
                version, "ася", true, sortAttribute);

        String reportPath = null;
        try {
            reportPath = reportBuilder.createReport();

            String[][] rowsExpected = new String[][]{
                    {"Справочник: Иерархический справочник"},
                    {"Дата актуальности: 01.01.2018"},
                    {"Параметр поиска: ася(по точному совпадению)"},
                    {""},
                    {"Наименование", "Число", "Дата начала актуальности", "Дата окончания актуальности", "Код родительского подразделения", "Уровень"},
                    {"Вася", "111", "01.01.2017", "01.01.2019", "", "1"},
                    {"Дася", "222", "01.01.2017", "01.01.2019", "\tВася", "2"},
                    {"Мася", "333", "01.01.2017", "01.01.2019", "\tВася", "2"}
            };
            assertEquals(toList(rowsExpected).toString(),
                    readCsvFile(reportPath, null).toString());
        } finally {
            if (reportPath != null) {
                File file = new File(reportPath);
                file.delete();
            }
        }
    }
}
