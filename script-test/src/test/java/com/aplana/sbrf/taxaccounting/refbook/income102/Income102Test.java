package com.aplana.sbrf.taxaccounting.refbook.income102;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.refbook.impl.RefBookUniversal;
import com.aplana.sbrf.taxaccounting.util.RefBookScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.util.*;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * Отчет о прибылях и убытках (Форма 0409102-СБ).
 * id = 52
 */
public class Income102Test extends RefBookScriptTestBase {

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(Income102Test.class);
    }

    @Before
    public void mockServices() {
    }

    @Test
    public void importFile() throws ParseException {
        testHelper.setAccountPeriodId(1);

        // для работы со справочником
        long refbookId = 52L;
        RefBookUniversal provider = mock(RefBookUniversal.class);
        provider.setRefBookId(refbookId);
        when(testHelper.getRefBookFactory().getDataProvider(refbookId)).thenReturn(provider);
        List<String> matchedRecords = new ArrayList<String>();
        when(provider.getMatchedRecords(anyList(), anyList(), anyInt())).thenReturn(matchedRecords);
        RefBook refBook = new RefBook();
        when(testHelper.getRefBookFactory().get(anyLong())).thenReturn(refBook);

        List<LogEntry> entries = testHelper.getLogger().getEntries();
        List<String> fileNames = new ArrayList<String>();
        String msg;
        String fileName;
        int i;
        int times = 0;

        // ошибочное имя файла
        fileName = "test-file-name";
        testHelper.setFileName(fileName);
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);
        i = 0;
        msg = "Выбранный файл не соответствует формату xls/xlsx. Файл не может быть загружен.";
        Assert.assertEquals(msg, subMsg(entries.get(i++).getMessage()));
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        verify(provider, never()).updateRecords(any(TAUserInfo.class), any(Date.class), anyList());
        testHelper.getLogger().clear();

        // пустой файл
        // пропуск строк с символом 0
        fileNames.clear();
        fileNames.add("importFile - empty.xls");
        fileNames.add("importFile - zero.xls");
        msg = "Файл не содержит данных. Файл не может быть загружен.";
        for (String name : fileNames) {
            testHelper.setFileName(name);
            testHelper.setImportFileInputStream(getCustomInputStream(name));
            testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);
            i = 0;
            Assert.assertEquals(msg, subMsg(entries.get(i++).getMessage()));
            Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
            verify(provider, never()).updateRecords(any(TAUserInfo.class), any(Date.class), anyList());
            testHelper.getLogger().clear();
        }

        // в шапке не строковые типы
        // в шапке неправильные надписи
        // не возможно прочесть файл xls/xlsx
        fileNames.clear();
        fileNames.add("importFile - bad format.xls");
        fileNames.add("importFile - bad header.xls");
        fileNames.add("importFile - bad header type.xls");
        msg = "Формат файла не соответствуют ожидаемому формату. Файл не может быть загружен.";
        for (String name : fileNames) {
            testHelper.setFileName(name);
            testHelper.setImportFileInputStream(getCustomInputStream(name));
            testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);
            i = 0;
            Assert.assertEquals(msg, subMsg(entries.get(i++).getMessage()));
            Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
            verify(provider, never()).updateRecords(any(TAUserInfo.class), any(Date.class), anyList());
            testHelper.getLogger().clear();
        }

        // нарушена уникальность кодов ОПУ
        matchedRecords.add("1");
        matchedRecords.add("2");
        fileName = "importFile.xls";
        testHelper.setFileName(fileName);
        testHelper.setImportFileInputStream(getCustomInputStream(fileName));
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);
        i = 0;
        Assert.assertEquals("Нарушена уникальность кодов ОПУ. Файл не может быть загружен.", entries.get(i++).getMessage());
        Assert.assertEquals("Следующие коды ОПУ указаны в форме более одного раза:", entries.get(i++).getMessage());
        Assert.assertEquals("1, 2", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        verify(provider, never()).updateRecords(any(TAUserInfo.class), any(Date.class), anyList());
        matchedRecords.clear();
        testHelper.getLogger().clear();

        // превышена максимальная длина строки
        fileName = "importFile - long name.xls";
        testHelper.setFileName(fileName);
        testHelper.setImportFileInputStream(getCustomInputStream(fileName));
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);
        i = 0;
        msg = "В строке с \"Символ = 1\" превышена максимальная длина строки значения поля  \"Наименование статьи\"!";
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        verify(provider, never()).updateRecords(any(TAUserInfo.class), any(Date.class), anyList());
        testHelper.getLogger().clear();

        // неправильный тип данных в столбцах
        Map<String, String> fileNameMap = new HashMap<String, String>();
        fileNameMap.put("importFile - bad type name.xls", "Наименование статей");
        fileNameMap.put("importFile - bad type symbols.xls", "Символы");
        fileNameMap.put("importFile - bad type total.xls", "Всего");
        for (String name : fileNameMap.keySet()) {
            String columnName = fileNameMap.get(name);
            testHelper.setFileName(name);
            testHelper.setImportFileInputStream(getCustomInputStream(name));
            testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);
            i = 0;
            msg = String.format("Данные столбца '%s' файла не соответствуют ожидаемому типу данных. Файл не может быть загружен.", columnName);
            Assert.assertEquals(msg, subMsg(entries.get(i++).getMessage()));
            Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
            verify(provider, never()).updateRecords(any(TAUserInfo.class), any(Date.class), anyList());
            testHelper.getLogger().clear();
        }

        // нормальная загрузка xlsx
        fileName = "importFile.xlsx";
        testHelper.setFileName(fileName);
        testHelper.setImportFileInputStream(getCustomInputStream(fileName));
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);
        // ошибок быть не должно и сохранение должно выполниться один раз
        checkLogger();
        verify(provider, times(++times)).updateRecords(any(TAUserInfo.class), any(Date.class), anyList());
        testHelper.getLogger().clear();

        // нормальная загрузка xls
        fileName = "importFile.xls";
        testHelper.setFileName(fileName);
        testHelper.setImportFileInputStream(getCustomInputStream(fileName));
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);
        // ошибок быть не должно и сохранение должно выполниться один раз
        checkLogger();
        verify(provider, times(++times)).updateRecords(any(TAUserInfo.class), any(Date.class), anyList());
        testHelper.getLogger().clear();
    }

    /** Получить часть сообщения без надписи "Ошибка исполнения [N]: ". Используется для сообщении сформированных исплючениями. */
    private String subMsg(String msg) {
        int position = msg.indexOf("]: ") + 3;
        return msg.substring(position);
    }
}
