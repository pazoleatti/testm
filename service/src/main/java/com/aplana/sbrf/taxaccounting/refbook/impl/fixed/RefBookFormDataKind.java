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
 * Фильтр принимает строку идентификаторов, разделенных запятой, либо "LOWER(NAME) like %...%"
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
		if (filter != null && filter.trim().length() > 0) {
			String t = filter.trim().toLowerCase();
			if (t.contains(ATTRIBUTE_NAME.toLowerCase())) {
				int positionStart = t.indexOf('%');
				int positionEnd = t.lastIndexOf('%');
				return getNameFilteredRecords(t.substring(positionStart + 1, positionEnd));
			}
			return getIdFilteredRecords(t.split(","));
        } else {
			return getAllRecords();
        }
    }

	private PagingResult<Map<String, RefBookValue>> getAllRecords() {
		PagingResult<Map<String, RefBookValue>> records = new PagingResult<Map<String, RefBookValue>>();
		for (FormDataKind item : FormDataKind.values()) {
			Long id = Long.valueOf(item.getId());

			Map<String, RefBookValue> record = refBook.createRecord();
			record.get(RefBook.RECORD_ID_ALIAS).setValue(id);
			record.get(ATTRIBUTE_NAME).setValue(item.getName());
			records.add(record);
		}
		return records;
	}


	private PagingResult<Map<String, RefBookValue>> getIdFilteredRecords(String[] ids) {
		PagingResult<Map<String, RefBookValue>> records = new PagingResult<Map<String, RefBookValue>>();
		for (String s : ids) {
			Long id = Long.valueOf(s);
			FormDataKind item = FormDataKind.fromId(id.intValue());

			Map<String, RefBookValue> record = refBook.createRecord();
			record.get(RefBook.RECORD_ID_ALIAS).setValue(id);
			record.get(ATTRIBUTE_NAME).setValue(item.getName());
			records.add(record);
		}
		return records;
	}

	private PagingResult<Map<String, RefBookValue>> getNameFilteredRecords(String filter) {
		PagingResult<Map<String, RefBookValue>> records = new PagingResult<Map<String, RefBookValue>>();
		for (FormDataKind item : FormDataKind.values()) {
			if (!item.getName().toLowerCase().contains(filter)) {  //фильтрация. попадают в выборку только содержащие строку фильтра в названии
				continue;
			}
			Long id = Long.valueOf(item.getId());

			Map<String, RefBookValue> record = refBook.createRecord();
			record.get(RefBook.RECORD_ID_ALIAS).setValue(id);
			record.get(ATTRIBUTE_NAME).setValue(item.getName());
			records.add(record);
		}
		return records;
	}
}
