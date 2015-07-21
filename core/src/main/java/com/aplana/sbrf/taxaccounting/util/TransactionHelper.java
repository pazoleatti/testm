package com.aplana.sbrf.taxaccounting.util;

import org.springframework.transaction.TransactionStatus;

/**
 * Класс для расширенной работы с транзакциями в jta-режиме.
 * Изначально вебсфера + jta не поддерживают приостановку транзакций, как например в режиме PROPAGATION_REQUIRES_NEW
 * Для решения этой проблемы, транзакция создается вручную
 * @author dloshkarev
 */
@ScriptExposed
public interface TransactionHelper {
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

	/**
	 * Стартует новую транзакцию
	 * @return
	 */
	TransactionStatus startTransaction();

	/**
	 * Подтверждает транзакцию
	 * @param status информация о транзакции
	 */
	void commit(TransactionStatus status);

	/**
	 * Откатывает транзакцию
	 * @param status информация о транзакции
	 */
	void rollback(TransactionStatus status);
}
