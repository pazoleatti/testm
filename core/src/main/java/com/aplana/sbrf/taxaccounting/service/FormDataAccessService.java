package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.FormDataKind;


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
	 * @param formId идентификатор налоговой формы (шаблона)
	 * @param kind тип налоговой формы, который создаётся
	 * @param departmentId идентификатор подразделения, в котором создаётся форма
	 * @return true - если у пользователя есть права, false - в противном случае
	 */
	boolean canCreate(int userId, int formId, FormDataKind kind, int departmentId);
	
	/**
	 * Проверка того, что у пользователя есть права на удаление карточки с данными налоговой формы
	 * @param userId идентификатор пользователя
	 * @param formDataId идентификатор карточки с данными формы
	 * @return true - если у пользователя есть права на удаление, false - в противном случае
	 */
	boolean canDelete(int userId, long formDataId);	
}
