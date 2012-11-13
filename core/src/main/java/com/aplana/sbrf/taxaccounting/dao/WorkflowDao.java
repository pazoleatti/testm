package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.dao.exсeption.DaoException;
import com.aplana.sbrf.taxaccounting.model.Workflow;

/**
 * DAO-интерфейс для получения информации по {@link Workflow жизненным циклам}
 * Интерфейс не должен использоватьс напрямую, а только из сервисного слоя, поэтому не вынесен в core
 */
public interface WorkflowDao {
	/**
	 * Получить объект Workflow по идентификатору
	 * @param workflowId идентификатор жизненного цикла
	 * @return объект жизненного цикла, если такого объекта нет, то выбрасывает исключение 
	 * @throws DaoException в случае, если не удаётся найти запись с данным идентификатором
	 */
	Workflow getWorkflow(int workflowId);
}
