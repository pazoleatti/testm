package com.aplana.sbrf.taxaccounting.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.WorkflowStateDao;
import com.aplana.sbrf.taxaccounting.dao.exсeption.DaoException;
import com.aplana.sbrf.taxaccounting.dao.mapper.WorkflowStateMapper;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;

@Repository("workflowStateDao")
@Transactional(readOnly = true)
public class WorkflowStateDaoImpl implements WorkflowStateDao {
	@Autowired
	private WorkflowStateMapper workflowStateMapper;
	
	@Override
	public WorkflowState getState(int stateId) {
		WorkflowState state = workflowStateMapper.getState(stateId);
		if (state == null) {
			throw new DaoException("Объект состояния жизненного цикла с id = " + stateId + " не найден в БД");
		}
		return state;
	}
}
