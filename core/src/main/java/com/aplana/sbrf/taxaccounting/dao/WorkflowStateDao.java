package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.WorkflowState;

public interface WorkflowStateDao {
	
	/**
	 * Получить объект, представляющий стадию жизненного цикла
	 * @param stateId идентификатор стадии ЖЦ
	 * @return объект стадии жизненного цикла
	 * @throws DaoException если объект с заданным id не найден
	 */
	WorkflowState getState(int workflowStateId);
}
