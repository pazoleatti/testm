package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.FormDataPerformer;

/**
 * Интерфейс DAO для работы с информацией об @{link FormDataPerformer исполнителе налоговой формы}
 * @author dsultanbekov
 */
public interface FormPerformerDao {
	/**
	 * Сохранить информацию об исполнителе налоговой формы.
	 * @param formDataId идентификатор налоговой формы
	 * @param performer информация об исполнетеле налоговой формы. Значение поля {@link FormDataPerformer#getFormDataId()} обязательно должно быть
	 * заполнено непустым значением, и данная карточка данных должна существовать в БД 
	 */
	void save(long formDataId, FormDataPerformer performer);
	
	/**
	 * Получить информацию об исполнителе налоговой формы 
	 * @param formDataId идентификатор налоговой формы
	 * @return объект, представляющий информацию об исполнителе или null, если для данной налоговой формы не задана
	 * информация об исполнителе
	 */
	FormDataPerformer get(long formDataId);
	
	/**
	 * Стереть информацию об исполнителе налоговой формы
	 * @param formDataId идентификатор налоговой формы
	 */
	void clear(long formDataId);
}
