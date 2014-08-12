package com.aplana.sbrf.taxaccounting.refbook.impl.fixed;

import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Перечень конфигурационных параметров
 */
@Service("refBookConfigurationParam")
public class RefBookConfigurationParam extends AbstractPermanentRefBook {

    public static final Long REF_BOOK_ID = 105L;
    public static final String ATTRIBUTE_NAME = "NAME";
    public static final String ATTRIBUTE_CODE = "CODE";

    @Autowired
    private RefBookFactory refBookFactory;

    private RefBook refBook;

    @PostConstruct
    private void init() {
        refBook = refBookFactory.get(REF_BOOK_ID);
    }

    @Override
    protected PagingResult<Map<String, RefBookValue>> getRecords(String filter) {
        PagingResult<Map<String, RefBookValue>> records = new PagingResult<Map<String, RefBookValue>>();
        if (filter != null && filter.trim().length() > 0) {
            for (String s : filter.trim().split(",")) {
                ConfigurationParam item = ConfigurationParam.valueOf(s.trim());
                Map<String, RefBookValue> record = refBook.createRecord();
                record.get(ATTRIBUTE_CODE).setValue(item.name());
                record.get(ATTRIBUTE_NAME).setValue(item.getCaption());
                record.get(RefBook.RECORD_ID_ALIAS).setValue(BigDecimal.valueOf(item.ordinal()));
                records.add(record);
            }
        } else {
            for (ConfigurationParam item : ConfigurationParam.values()) {
                Map<String, RefBookValue> record = refBook.createRecord();
                record.get(ATTRIBUTE_CODE).setValue(item.name());
                record.get(ATTRIBUTE_NAME).setValue(item.getCaption());
                record.get(RefBook.RECORD_ID_ALIAS).setValue(BigDecimal.valueOf(item.ordinal()));
                records.add(record);
            }
        }
        return records;
    }
}
