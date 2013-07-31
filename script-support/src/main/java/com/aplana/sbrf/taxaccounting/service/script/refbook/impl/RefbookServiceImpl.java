package com.aplana.sbrf.taxaccounting.service.script.refbook.impl;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.service.script.refbook.RefBookService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service("refbookService")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RefBookServiceImpl implements RefBookService {

    @Autowired  // Получать через фабрику com.aplana.sbrf.taxaccounting.refbook.RefBookFactory потому что будет много RefBookDataProvider а не один универсальный
    private RefBookDataProvider provider;

    @Override
    public Map<String, RefBookValue> getRecordData(Long refBookId, Long recordId) {
		//provider.setRefBookId(refBookId);//TODO: (Marat Fayzullin) временная заглушка
        return provider.getRecordData(recordId);
    }

    @Override
    public String getStringValue(Long refBookId, Long recordId, String alias) {
        RefBookValue refBookValue = getValue(refBookId, recordId, alias);
        return refBookValue != null ? refBookValue.getStringValue() : null;
    }

    @Override
    public Number getNumberValue(Long refBookId, Long recordId, String alias) {
        RefBookValue refBookValue = getValue(refBookId, recordId, alias);
        return refBookValue != null ? refBookValue.getNumberValue() : null;
    }

    @Override
    public Date getDateValue(Long refBookId, Long recordId, String alias) {
        RefBookValue refBookValue = getValue(refBookId, recordId, alias);
        return refBookValue != null ? refBookValue.getDateValue() : null;
    }

    private RefBookValue getValue(Long refBookId, Long recordId, String alias) {
        if (refBookId == null || recordId == null || alias == null || alias.isEmpty())
            return null;

        Map<String, RefBookValue> map = getRecordData(refBookId, recordId);

        if (map == null || map.isEmpty() || !map.containsKey(alias))
            return null;

        return map.get(alias);
    }
}
