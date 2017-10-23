package com.aplana.sbrf.taxaccounting.service;

/**
 * Интерфейс, описывающий бизнес-логику, которая должна быть выполнена в рамках одной транзакции
 * @author dloshkarev
 */
public interface TransactionLogic<T> {
    T execute();
}
