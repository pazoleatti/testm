package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.security.TAUser;

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
	 * Проверка прав на чтение данных
	 * @param user объект, представляющий пользователя, для которого проверяются права
	 * @param formData объект с данными по налоговой формы, для которого проверяются права 
	 * @return true - если права на чтение есть, false - в противном случае
	 */	
	boolean canRead(TAUser user, FormData formData);
	
	/**
	 * Проверка прав на редактирование данных
	 * @param userId идентификатор пользователя, для которого проверяются права
	 * @param formDataId идентификатор карточки данных по налоговой форме
	 * @return true - если права на редактирование есть, false - в противном случае
	 */	
	boolean canEdit(int userId, long formDataId);
	
	/**
	 * Проверка прав на редактирование данных 
	 * @param user объект, представляющий пользователя, для которого проверяются права
	 * @param formData объект с данными по налоговой формы, для которого проверяются права 
	 * @return true - если права на редактирование есть, false - в противном случае
	 */
	boolean canEdit(TAUser user, FormData formData);
}
