package com.aplana.sbrf.taxaccounting.mock;

import com.aplana.sbrf.taxaccounting.service.TransactionHelper;
import com.aplana.sbrf.taxaccounting.service.TransactionLogic;

/**
 * Тестовая реализация TransactionHelper, просто выполняющая переданную логику.
 */
public class TransactionHelperStub implements TransactionHelper {
    @Override
    public <T> T executeInNewTransaction(TransactionLogic<T> logic) {
        return logic.execute();
    }

    @Override
    public <T> T executeInNewReadOnlyTransaction(TransactionLogic<T> logic) {
        return logic.execute();
    }
}
