package com.aplana.sbrf.taxaccounting.refbook.impl.fixed;

import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Фильтр принимает строку идентификаторов, разделенных запятой.
 */
@Service("refBookFormDataKind")
public class RefBookFormDataKind extends AbstractPermanentRefBook {

    public static final Long REF_BOOK_ID = 94L;

    @Override
    protected Map<Long, Map<String, String>> getRecords(String filter) {
        Map<Long, Map<String, String>> records = new HashMap<Long, Map<String, String>>();

        if (filter != null && !"".equals(filter)) {
            for (String s : filter.split(",")) {
                Map<String, String> attrs = new HashMap<String, String>();
                FormDataKind item = FormDataKind.fromId(Integer.valueOf(s));
                attrs.put("NAME", item.getName());
                records.put(Long.valueOf(item.getId()), attrs);

            }
        } else if (filter == null) {
            for (int i = 1; i <= FormDataKind.values().length; i++) {
                Map<String, String> attrs = new HashMap<String, String>();
                FormDataKind item = FormDataKind.fromId(i);
                attrs.put("NAME", item.getName());
                records.put(Long.valueOf(item.getId()), attrs);
            }
        }

        return records;
    }

    @Override
    public int getRecordsCount(Date version, String filter) {
        return getRecords(filter).size();
    }
}
