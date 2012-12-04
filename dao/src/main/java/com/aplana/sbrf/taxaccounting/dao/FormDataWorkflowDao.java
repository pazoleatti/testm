package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.WorkflowState;
import com.aplana.sbrf.taxaccounting.service.FormDataService;

/**
 * DAO-интерфейс, реализующий функционал по изменению стадии жизненного цикла карточек данных по налоговым формам
 * В этот интерфейс вынесены методы, которые не должны быть доступны из публичных интерфейсов проекта core,
 * в первую очередь - методы по изменению стадии жизненного цикла
 * 
 * Эти методы не выполняют никаких бизнес-проверок, поэтому вызывать их напрямую из кода нельзя - лучше
 * воспользоваться соответствующим сервисом ({@link FormDataService})
 */
public interface FormDataWorkflowDao {
	/**
	 * Изменить статус карточки данных по налоговой форме
	 * Карточка должна быть сохранена, и иметь идентификатор
	 * @param formDataId - идентификатор данных
	 * @param workflowState - состояние, в которое нужно перевести налоговую форму
	 */
	void changeFormDataState(long formDataId, WorkflowState workflowState);
}
