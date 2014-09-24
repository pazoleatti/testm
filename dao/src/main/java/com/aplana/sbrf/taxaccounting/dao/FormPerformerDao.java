package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.FormDataPerformer;

import java.util.Date;
import java.util.List;

/**
 * Интерфейс DAO для работы с информацией об @{link FormDataPerformer исполнителе налоговой формы}
 * @author dsultanbekov
 */
public interface FormPerformerDao {
	/**
	 * Сохранить информацию об исполнителе налоговой формы.
	 * @param formDataId идентификатор налоговой формы
     * @param manual признак версии ручного ввода
	 * @param performer информация об исполнетеле налоговой формы. Значение поля formDataId обязательно должно быть
	 * заполнено непустым значением, и данная карточка данных должна существовать в БД 
	 */
	void save(long formDataId, boolean manual, FormDataPerformer performer);
	
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

    /**
     * Получение списка id НФ в параметрах печатной формы которых
     * для формирования "Наименование подразделения, которое должно быть использовано в печатной форме" используется заданное подразделение
     * @param departmentId
     * @param dateFrom
     * @param dateTo
     * @return
     */
    List<Long> getFormDataId(final int departmentId, Date dateFrom, Date dateTo);
}
