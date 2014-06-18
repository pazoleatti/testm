package com.aplana.sbrf.taxaccounting.util;

/**
 * Интерфейс, описывающий бизнес-логику, которая должна быть выполнена в рамках одной транзакции
 * @author dloshkarev
 */
public interface TransactionLogic<T> {
    public void execute();
    public T executeWithReturn();
}
