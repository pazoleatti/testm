package com.aplana.sbrf.taxaccounting.refbook.impl.fixed;

import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.impl.RefBookSimpleReadOnly;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Dmitriy Levykin
 */
public class RefBookConfigurationParamTest {
    private RefBookSimpleReadOnly refBookConfigurationParam;

    @Before
    public void init() {
        refBookConfigurationParam = new RefBookSimpleReadOnly();
        refBookConfigurationParam.setRefBookId(RefBookSimpleReadOnly.CONFIGURATION_PARAM_REF_BOOK_ID);
        refBookConfigurationParam.setTableName(RefBookSimpleReadOnly.CONFIGURATION_PARAM_TABLE_NAME);
    }

    // Без фильтра
    @Test
    public void getRecords1Test() {
        PagingResult<Map<String, RefBookValue>> records = refBookConfigurationParam.getRecords(new Date(), null, null, null);
        Assert.assertEquals(ConfigurationParam.values().length, records.size());
    }

    // С фильтром
    @Test
    public void getRecords2Test() {
        PagingResult<Map<String, RefBookValue>> records = refBookConfigurationParam.getRecords(new Date(), null,
                ConfigurationParam.FORM_UPLOAD_DIRECTORY.name() + ", " + ConfigurationParam.FORM_ERROR_DIRECTORY.name(), null);
        Assert.assertEquals(2, records.size());
        records = refBookConfigurationParam.getRecords(new Date(), null,
                ConfigurationParam.FORM_UPLOAD_DIRECTORY.name() + "," + ConfigurationParam.FORM_ARCHIVE_DIRECTORY.name(), null);
        Assert.assertEquals(2, records.size());
        records = refBookConfigurationParam.getRecords(new Date(), null,
                ConfigurationParam.FORM_UPLOAD_DIRECTORY.name(), null);
        Assert.assertEquals(1, records.size());
    }

    // С ошибкой в фильтре
    @Test(expected = IllegalArgumentException.class)
    public void getRecords3Test() {
        refBookConfigurationParam.getRecords(new Date(), null, "T1,T2", null);
    }
}
