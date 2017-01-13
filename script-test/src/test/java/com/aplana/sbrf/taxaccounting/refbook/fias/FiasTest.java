package com.aplana.sbrf.taxaccounting.refbook.fias;

import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.service.script.ImportFiasDataService;
import com.aplana.sbrf.taxaccounting.util.RefBookScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import net.sf.sevenzipjbinding.ArchiveFormat;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.util.ArrayList;
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
                String tableName = (String) args[0];
                List<Map<String, Object>> rowsList = (List<Map<String, Object>>) args[1];
                //System.out.println("Test " + rowsList.size() + " rows insert to " + tableName);
                //createInserts(tableName, rowsList);
                return null;
            }
        }).when(importFiasDataService).insertRecords(anyString(), anyList());
    }

    public IInArchive getFiasArchive(String name) throws Exception {
        return getFiasArchive(new File(FiasTest.class.getResource(name).toURI()));
    }

    public IInArchive getFiasArchive(File file) throws Exception {
        IInArchive archive = null;
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
            archive = SevenZip.openInArchive(ArchiveFormat.RAR, // null - autodetect
                    new RandomAccessFileInStream(
                            randomAccessFile));
        } catch (Exception e) {
            throw new Exception("Ошибка чтения файла fias_xml.rar", e);
        }
        return archive;
    }


    @Test
    public void importFiasXmlRar() throws Exception {

        //Tecт с архивом с сайта ФНС
        //IInArchive archive = getFiasArchive(new File("D:\\sbrf1\\fias_xml.rar"));

        IInArchive archive = getFiasArchive("fias_xml.rar");
        testHelper.setFiasArchive(archive);
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);

        printLog();

        //Проверка логгера на наличие ошибок
        checkLogger();

        testHelper.getLogger().clear();
    }

    @Test
    public void importBadFiasXmlRar() throws Exception {
        testHelper.setFiasArchive(getFiasArchive("fias_xml_bad.rar"));
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);

        //должна быть ошибка
        checkLoggerError();

        testHelper.getLogger().clear();
    }

    protected void checkLoggerError() {
        if (testHelper.getLogger().containsLevel(LogLevel.ERROR)) {
            printLog();
        }
        Assert.assertTrue("Logger contains error level messages.", testHelper.getLogger().containsLevel(LogLevel.ERROR));
    }


    private void createInserts(String tableName, List<Map<String, Object>> rowsList) {
        for (Map<String, Object> row : rowsList) {
            //вставка в БД
            System.out.println(createInsert(tableName, row));
        }
    }


    private String createInsert(String tableName, Map<String, Object> row) {
        String[] columns = row.keySet().toArray(new String[]{});
        Object[] values = row.values().toArray(new Object[]{});
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("insert into ");
        sqlStatement.append(tableName);
        sqlStatement.append("(").append(SqlUtils.getColumnsToString(columns, null)).append(")");
        sqlStatement.append(" VALUES ");
        sqlStatement.append("(").append(valuesToString(values)).append(")");
        return sqlStatement.toString() + ";";
    }

    private String valuesToString(Object[] values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            Object val = values[i];
            if (val == null) {
                sb.append("null");
            } else if (val instanceof String) {
                sb.append("'" + val + "'");
            } else {
                sb.append(val.toString());
            }
            if (i < values.length - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }


}
