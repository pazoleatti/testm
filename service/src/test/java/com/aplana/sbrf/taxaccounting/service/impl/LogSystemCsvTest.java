package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.LogSearchResultItem;
import com.aplana.sbrf.taxaccounting.service.impl.print.logsystem.LogSystemCsvBuilder;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * User: avanteev
 */

public class LogSystemCsvTest {
    private static final int SIZE = 100;

    @Test
    public void csvTest() throws IOException {
        ArrayList<LogSearchResultItem> list = new ArrayList<LogSearchResultItem>(SIZE);
        for (int i=0; i<SIZE; i++){
            LogSearchResultItem item = new LogSearchResultItem();
            item.setIp("127.0.0.1");
            item.setDeclarationTypeName("Declaration name");
            item.setDepartmentName("Department name");
            item.setEvent(FormDataEvent.ADD_DEPARTMENT);
            item.setFormKind(FormDataKind.ADDITIONAL);
            item.setFormTypeId(1);
            item.setFormTypeName("FormType name");
            item.setLogDate(new Date());
            item.setNote("");
            item.setReportPeriodName("Report name");
            item.setRoles("");
            item.setUser("user");
            item.setUserDepartmentName("User department name");

            list.add(item);
        }
        File file = null;
        try{
            LogSystemCsvBuilder builder = new LogSystemCsvBuilder(list);
            file = builder.createBlobDataFile();
            System.out.println(file.getAbsolutePath());
        } finally {
            assert file != null;
            if (!file.delete()){
                System.out.println("Файл не удалился");
            }
        }
    }
}
