package com.aplana.sbrf.taxaccounting.refbook.impl.fixed;

import com.aplana.sbrf.taxaccounting.model.AuditFieldList;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * Список полей для журнала аудита
 */
@Service("refBookAuditFieldList")
public class RefBookAuditFieldList extends AbstractPermanentRefBook {

    public static final Long REF_BOOK_ID = 104L;
    public static final String ATTRIBUTE_NAME = "NAME";

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
                Long id = Long.valueOf(s);
                AuditFieldList item = AuditFieldList.fromId(id.intValue());

                Map<String, RefBookValue> record = refBook.createRecord();
                record.get(RefBook.RECORD_ID_ALIAS).setValue(id);
                record.get(ATTRIBUTE_NAME).setValue(item.getName());
                records.add(record);
            }
        } else {
            for (AuditFieldList item : AuditFieldList.values()) {
                Long id = Long.valueOf(item.getId());

                Map<String, RefBookValue> record = refBook.createRecord();
                record.get(RefBook.RECORD_ID_ALIAS).setValue(id);
                record.get(ATTRIBUTE_NAME).setValue(item.getName());
                records.add(record);
            }
        }
        return records;
    }

}
