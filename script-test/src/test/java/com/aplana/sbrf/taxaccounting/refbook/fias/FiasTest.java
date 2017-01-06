package com.aplana.sbrf.taxaccounting.refbook.fias;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.service.script.ImportFiasDataService;
import com.aplana.sbrf.taxaccounting.util.RefBookScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;

/**
 * Тесты для скрипта загрузки справочников фиас, проверяется что скрипт отрабатывает на тестовых данных без ошибок
 *
 * @author Andrey Drunk
 */
public class FiasTest extends RefBookScriptTestBase {

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(FiasTest.class);
    }

    @Before
    public void mockService() {
        ImportFiasDataService importFiasDataService = testHelper.getImportFiasDataService();
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                String table = (String) args[0];
                List<Map<String, ?>> rowsList = (List<Map<String, ?>>) args[1];
                System.out.println("Test " + rowsList.size() + " rows insert to " + table);
                return null;
            }
        }).when(importFiasDataService).insertRecords(anyString(), anyList());
    }

    @Test
    public void importOperationStatusesFileTest() throws FileNotFoundException {
        String fileName = "AS_OPERSTAT.XML";
        InputStream addrObjectInputStream = getCustomInputStream(fileName);
        testHelper.setFileName(fileName);
        testHelper.setImportFileInputStream(addrObjectInputStream);
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);

        //Проверка логгера на наличие ошибок
        checkLogger();

        testHelper.getLogger().clear();
    }

    @Test
    public void importAddressObjectTypesFileTest() throws FileNotFoundException {
        String fileName = "AS_SOCRBASE.XML";
        InputStream addrObjectInputStream = getCustomInputStream(fileName);
        testHelper.setFileName(fileName);
        testHelper.setImportFileInputStream(addrObjectInputStream);
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);

        //Проверка логгера на наличие ошибок
        checkLogger();

        testHelper.getLogger().clear();
    }

    @Test
    public void importAddressObjectsFileTest() throws FileNotFoundException {

        //InputStream addrObjectInputStream = new FileInputStream("D:\\sbrf1\\fias_xml\\fias_xml\\AS_ADDROBJ_20161222_3bcfa426-87dd-4f2c-b469-14a550858d24.XML");
        String fileName = "AS_ADDROBJ.XML";
        InputStream addrObjectInputStream = getCustomInputStream(fileName);
        testHelper.setFileName(fileName);
        testHelper.setImportFileInputStream(addrObjectInputStream);
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);

        //Проверка логгера на наличие ошибок
        checkLogger();

        testHelper.getLogger().clear();
    }


    @Test
    public void importHousesFileTest() throws FileNotFoundException {
        String fileName = "AS_HOUSE.XML";
        InputStream addrObjectInputStream = getCustomInputStream(fileName);
        testHelper.setFileName(fileName);
        testHelper.setImportFileInputStream(addrObjectInputStream);
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);

        //Проверка логгера на наличие ошибок
        checkLogger();

        testHelper.getLogger().clear();
    }

    @Test
    public void importHousesIntervalsFileTest() throws FileNotFoundException {
        String fileName = "AS_HOUSEINT.XML";
        InputStream addrObjectInputStream = getCustomInputStream(fileName);
        testHelper.setFileName(fileName);
        testHelper.setImportFileInputStream(addrObjectInputStream);
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);

        //Проверка логгера на наличие ошибок
        checkLogger();

        testHelper.getLogger().clear();
    }

    @Test
    public void importRoomsFileTest() throws FileNotFoundException {
        String fileName = "AS_ROOM.XML";
        InputStream addrObjectInputStream = getCustomInputStream(fileName);
        testHelper.setFileName(fileName);
        testHelper.setImportFileInputStream(addrObjectInputStream);
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);

        //Проверка логгера на наличие ошибок
        checkLogger();

        testHelper.getLogger().clear();
    }


}
