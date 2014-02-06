package com.aplana.sbrf.taxaccounting.refbook.impl.fixed;

import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service("refBookFormDataKind")
public class RefBookFormDataKind extends AbstractPermanentRefBook {

    public static final Long REF_BOOK_ID = 94L;

    @Override
    protected Map<Long, Map<String, String>> getRecords(String filter) {
        Map<Long, Map<String, String>> records = new HashMap<Long, Map<String, String>>();

        for (FormDataKind item: FormDataKind.values()){
            Map<String, String> attrs = new HashMap<String, String>();
            attrs.put("NAME", item.getName());
            records.put(Long.valueOf(item.getId()), attrs);
        }

        return records;
    }
}
