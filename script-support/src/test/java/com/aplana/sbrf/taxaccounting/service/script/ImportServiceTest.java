package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.service.script.impl.ImportServiceImpl;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertTrue;

/**
 * @author Ramil Timerbaev
 */
public class ImportServiceTest {

    private static ImportService service = new ImportServiceImpl();

    /**
     * Получить данные xls.
     */
    private InputStream getInputStream(String name) {
        String fileName = getClass().getResource(name).getFile();
        File file = new File(fileName);
        if (!file.exists()) {
            return null;
        }
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    @Test
    public void getAllData() throws IOException {
        assertTrue(service.getData(getInputStream("rnu25ImportTest.xls"), "fileName.xls") != null);
    }

    @Test
    public void getTableData() throws IOException {
        assertTrue(service.getData(getInputStream("rnu25ImportTest.xls"), "fileName.xls", "windows-1251",
                "Государственный регистрационный номер", "Общий итог") != null);
    }

    @Test
    public void getAllDataWithBadCellFormula() throws IOException {
        assertTrue(service.getData(getInputStream("937_1withBadCellFormula.xls"), "fileName.xls") != null);
    }
}
