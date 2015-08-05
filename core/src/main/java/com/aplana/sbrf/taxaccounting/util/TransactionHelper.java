package com.aplana.sbrf.taxaccounting.util;
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
	<T> T executeInNewTransaction(TransactionLogic<T> logic);

	/**
	 * Выполняет указанную логику в новой транзакции и откатывает все сделанные изменения
	 * @param logic код выполняемый в транзакции
	 */
	<T> T executeInNewReadOnlyTransaction(TransactionLogic<T> logic);
}
