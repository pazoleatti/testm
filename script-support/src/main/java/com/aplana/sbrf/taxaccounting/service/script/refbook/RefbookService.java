package com.aplana.sbrf.taxaccounting.service.script.refbook;


import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

import java.util.Date;
import java.util.Map;


@ScriptExposed
public interface RefbookService {

    Map<String, RefBookValue> getRecordData(Long refBookId, Long recordId);

    String getStringValue(Long refBookId, Long recordId, String alias);

    Number getNumberValue(Long refBookId, Long recordId, String alias);

    Date getDateValue(Long refBookId, Long recordId, String alias);
}
