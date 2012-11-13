package com.aplana.sbrf.taxaccounting.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.WorkflowDao;
import com.aplana.sbrf.taxaccounting.dao.exсeption.DaoException;
import com.aplana.sbrf.taxaccounting.dao.mapper.WorkflowMapper;
import com.aplana.sbrf.taxaccounting.model.Workflow;

@Repository("workflowDao")
@Transactional(readOnly = true)
public class WorkflowDaoImpl implements WorkflowDao {
	@Autowired
	private WorkflowMapper workflowMapper;
	
	@Override
	public Workflow getWorkflow(int workflowId) {
		Workflow w = workflowMapper.getWorkflow(workflowId);
		if (w == null) {
			throw new DaoException("Объект жизненного цикла с id = " + workflowId + " не найден в БД");
		}
		w.setMoves(workflowMapper.getMoves(workflowId));
		return w;
	}
}
