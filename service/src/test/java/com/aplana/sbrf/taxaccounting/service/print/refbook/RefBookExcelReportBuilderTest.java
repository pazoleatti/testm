package com.aplana.sbrf.taxaccounting.service.print.refbook;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.service.impl.print.refbook.RefBookExcelReportBuilder;
import com.aplana.sbrf.taxaccounting.service.impl.refbook.BatchIterator;
import org.junit.Test;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class RefBookExcelReportBuilderTest extends AbstractRefBookReportBuilderTest {

    class RecordsFetcher implements BatchIterator {
        private Iterator<Map<String, RefBookValue>> iterator = linearRecords.iterator();

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Map<String, RefBookValue> getNextRecord() {
            return iterator.next();
        }
    }

    @Test
    public void buildLinearRefBookReportTest() throws Exception {
        RefBookExcelReportBuilder reportBuilder = new RefBookExcelReportBuilder(linearRefBook, linearAttributes,
                version, "ася", true, sortAttribute, new RecordsFetcher());

        String reportPath = null;
        try {
            reportPath = reportBuilder.createReport();

            String[][] rowsExpected = new String[][]{
                    {"Справочник: Линейный справочник"},
                    {"Дата актуальности: 01.01.2018"},
                    {"Параметр поиска: \"ася\" (по точному совпадению)"},
                    {""},
                    {"Наименование", "Число", "Дата начала актуальности", "Дата окончания актуальности"},
                    {"Вася", "111.0", "01-янв-2017", "01-янв-2019"},
                    {"Дася", "222.0", "01-янв-2017", "01-янв-2019"},
                    {"Мася", "333.0", "01-янв-2017", "01-янв-2019"}
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

    @Test
    public void buildHierRefBookReportTest() throws Exception {
        RefBookExcelReportBuilder reportBuilder = new RefBookExcelReportBuilder(hierRefBook, hierAttributes,
                version, "ася", true, sortAttribute, hierRecords);

        String reportPath = null;
        try {
            reportPath = reportBuilder.createReport();

            String[][] rowsExpected = new String[][]{
                    {"Справочник: Иерархический справочник"},
                    {"Дата актуальности: 01.01.2018"},
                    {"Параметр поиска: \"ася\" (по точному совпадению)"},
                    {""},
                    {"Наименование", "Число", "Дата начала актуальности", "Дата окончания актуальности", "Код родительского подразделения", "Уровень"},
                    {"Вася", "111.0", "01-янв-2017", "01-янв-2019", "", "1.0"},
                    {"Дася", "222.0", "01-янв-2017", "01-янв-2019", "Вася", "2.0"},
                    {"Мася", "333.0", "01-янв-2017", "01-янв-2019", "Вася", "2.0"}
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