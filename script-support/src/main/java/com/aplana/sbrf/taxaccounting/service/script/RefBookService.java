package com.aplana.sbrf.taxaccounting.service.script;


import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;
import com.aplana.sbrf.taxaccounting.util.TransactionLogic;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;


@ScriptExposed
public interface RefBookService {

    /**
     * Запись справочника по Id
     */
    Map<String, RefBookValue> getRecordData(Long refBookId, Long recordId);

    /**
     * Строковое значение атрибута записи справочника
     */
    String getStringValue(Long refBookId, Long recordId, String alias);

    /**
     * Числовое значение атрибута записи справочника
     */
    Number getNumberValue(Long refBookId, Long recordId, String alias);

    /**
     * Датированное значение атрибута записи справочника
     */
    Date getDateValue(Long refBookId, Long recordId, String alias);

    /**
     * Разыменование строк НФ
     */
    @SuppressWarnings("unused")
    public void dataRowsDereference(Logger logger, Collection<DataRow<Cell>> dataRows, List<Column> columns);
    /**
     * Выполняет указанную логику в новой транзакции
     * @param logic код выполняемый в транзакции
     */
    void executeInNewTransaction(TransactionLogic logic);

    /**
     * Выполняет указанную логику в новой транзакции. Вовращает результат
     * @param logic код выполняемый в транзакции
     */
    <T> T returnInNewTransaction(TransactionLogic<T> logic);
}
