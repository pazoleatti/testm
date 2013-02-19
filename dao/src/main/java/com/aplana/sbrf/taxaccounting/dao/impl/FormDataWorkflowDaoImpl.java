package com.aplana.sbrf.taxaccounting.dao.impl;

import java.util.Date;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.FormDataWorkflowDao;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;

/**
 * Реализация DAO для изменения стадии жизненного цикла у FormDao
 */
@Repository
public class FormDataWorkflowDaoImpl extends AbstractDao implements FormDataWorkflowDao {
	@Override
	@Transactional(readOnly=false)
	public void changeFormDataState(long formDataId, WorkflowState workflowState, Date acceptedDate) {
		int rows = getJdbcTemplate().update("update form_data set state=?, accepted_date=? where id = ?", workflowState.getId(), acceptedDate, formDataId);
		if (rows == 0) {
			throw new DaoException("Не удалось изменить состояние у записи с id = " + formDataId + ", возможно идентификатор неверен");
		}
	}
}
