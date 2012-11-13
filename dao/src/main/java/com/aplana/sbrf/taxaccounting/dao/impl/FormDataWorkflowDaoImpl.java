package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.FormDataWorkflowDao;
import com.aplana.sbrf.taxaccounting.dao.exсeption.DaoException;

/**
 * Реализация DAO для изменения стадии жизненного цикла у FormDao
 */
public class FormDataWorkflowDaoImpl extends AbstractDao implements FormDataWorkflowDao {
	@Override
	public void changeFormDataState(int formDataId, int stateId) {
		int rows = getJdbcTemplate().update("update form_data set state_id = ? where id = ?", stateId, formDataId);
		if (rows == 0) {
			throw new DaoException("Не удалось изменить состояние у записи с id = " + formDataId + ", возможно идентификатор неверен");
		}
	}
}
