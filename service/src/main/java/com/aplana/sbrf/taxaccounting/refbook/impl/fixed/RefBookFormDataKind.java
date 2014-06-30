package com.aplana.sbrf.taxaccounting.refbook.impl.fixed;

import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * Фильтр принимает строку идентификаторов, разделенных запятой.
 */
@Service("refBookFormDataKind")
public class RefBookFormDataKind extends AbstractPermanentRefBook {

	public static final Long REF_BOOK_ID = 94L;
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
				FormDataKind item = FormDataKind.fromId(id.intValue());

				Map<String, RefBookValue> record = refBook.createRecord();
				record.get(RefBook.RECORD_ID_ALIAS).setValue(id);
				record.get(ATTRIBUTE_NAME).setValue(item.getName());
                records.add(record);
            }
        } else {
			for (FormDataKind item : FormDataKind.values()) {
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
