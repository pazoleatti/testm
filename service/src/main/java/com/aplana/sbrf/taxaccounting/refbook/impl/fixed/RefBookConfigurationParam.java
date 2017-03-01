package com.aplana.sbrf.taxaccounting.refbook.impl.fixed;

import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
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
    @SuppressWarnings("unused") // https://jira.codehaus.org/browse/SONARJAVA-117
    private void init() {
        refBook = refBookFactory.get(REF_BOOK_ID);
    }

    @Override
    protected PagingResult<Map<String, RefBookValue>> getRecords(String filter) {
        PagingResult<Map<String, RefBookValue>> records = new PagingResult<Map<String, RefBookValue>>();
        if (filter != null && !filter.trim().isEmpty()) {
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

    @Override
    public Map<Long, Map<String, RefBookValue>> getRecordDataWhere(String where) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<Long, Map<String, RefBookValue>> getRecordDataVersionWhere(String where, Date version) {
        throw new UnsupportedOperationException();
    }

    @Override
	public Map<Long, RefBookValue> dereferenceValues(Long attributeId, Collection<Long> recordIds) {
		throw new UnsupportedOperationException();
	}

    @Override
    public List<String> getMatchedRecords(List<RefBookAttribute> attributes, List<Map<String, RefBookValue>> records, Integer accountPeriodId) {
        throw new UnsupportedOperationException();
    }

}
