package com.aplana.sbrf.taxaccounting.model;

/**
 * Интерфейс, который должен реализовываться объектами, обрабатываемыми по жизненному циклу 
 */
public interface WorkflowObject {
	/**
	 * Идентификатор жизненного цикла, по которому обрабатывается объект
	 */
	int getWorkflowId();
	/**
	 * Текущая стадия жизненного цикла
	 */
	int getStateId();
	/**
	 * Установить стадию жизненного цикла
	 */
	void setStateId(int stateId);
}
