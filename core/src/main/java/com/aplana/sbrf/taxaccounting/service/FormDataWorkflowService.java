package com.aplana.sbrf.taxaccounting.service;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.WorkflowMove;

/**
 * Сервис для выполнения работ, связанных с жизненным циклом над данными {@link FormData налоговых форм} 
 * @author dsultanbekov
 */
public interface FormDataWorkflowService {
	/**
	 * Получить список переходов, которые данный пользователь может выполнить над данным объектом {@link FormData}
	 * @param userId идентификатор пользователя
	 * @param formDataId идентификатор записи данных формы
	 * @return список переходов жизненного цикла, которые может выполнить текущий пользователь над данным объектом {@link FormData}
	 */
	List<WorkflowMove> getAvailableMoves(int userId, long formDataId);
}