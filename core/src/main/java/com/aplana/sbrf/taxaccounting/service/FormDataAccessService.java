package com.aplana.sbrf.taxaccounting.service;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataAccessParams;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.WorkflowMove;


/**
 * Интерфейс, реализующий логику по проверке прав доступа
 * @author dsultanbekov
 *
 */
public interface FormDataAccessService {
	/**
	 * Проверка прав на чтение данных
	 * @param userId идентификатор пользователя, для которого проверяются права
	 * @param formDataId идентификатор карточки данных по налоговой форме
	 * @return true - если права на чтение есть, false - в противном случае
	 */
	boolean canRead(int userId, long formDataId);
	
	/**
	 * Проверка прав на редактирование данных
	 * @param userId идентификатор пользователя, для которого проверяются права
	 * @param formDataId идентификатор карточки данных по налоговой форме
	 * @return true - если права на редактирование есть, false - в противном случае
	 */	
	boolean canEdit(int userId, long formDataId);
	
	/**
	 * Проверка того, что у пользователя есть права на создание карточек налоговых форм
	 * Данный метод просто проверяет, что пользователь имеет подходящую роль 
	 * и его подразделение может работать с данными налоговыми формами.
	 * Проверки, связанные с бизнес-логикой (например то, что за один период в подразделении должна существовать
	 * только одна форма заданного типа и т.п. должны проверяться отдельно).
	 * @param userId идентификатор пользователя
	 * @param formTemplateId идентификатор налоговой формы (шаблона)
	 * @param kind тип налоговой формы, который создаётся
	 * @param departmentId идентификатор подразделения, в котором создаётся форма
	 * @return true - если у пользователя есть права, false - в противном случае
	 */
	boolean canCreate(int userId, int formTemplateId, FormDataKind kind, int departmentId);
	
	/**
	 * Проверка того, что у пользователя есть права на удаление карточки с данными налоговой формы
	 * @param userId идентификатор пользователя
	 * @param formDataId идентификатор карточки с данными формы
	 * @return true - если у пользователя есть права на удаление, false - в противном случае
	 */
	boolean canDelete(int userId, long formDataId);
	
	/**
	 * Получить список переходов, которые данный пользователь может выполнить над данным объектом {@link FormData}
	 * @param userId идентификатор пользователя
	 * @param formDataId идентификатор записи данных формы
	 * @return список переходов жизненного цикла, которые может выполнить текущий пользователь над данным объектом {@link FormData}
	 */
	List<WorkflowMove> getAvailableMoves(int userId, long formDataId);
	
	/**
	 * Получить объект, содержащий {@link FormDataAccessParams параметры доступа} пользователя к объекту {@link FormData}
	 * Если для одного и того же объекта FormData нужно получить значения нескольких флагов доступа, то
	 * использование этого метода более предпочтительно, так как его реализация более эффективно запрашивает данные
	 * из БД
	 * @param userId идентификатор пользователя
	 * @param formDataId идентификатор записи данных формы
	 * @return объект, содержащий информацию о правах доступа пользователя к данной налоговой форме
	 */
	FormDataAccessParams getFormDataAccessParams(int userId, long formDataId);
}
