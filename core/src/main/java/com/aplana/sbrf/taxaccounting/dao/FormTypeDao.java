package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.FormType;

import java.util.List;

/**
 * Интерфейс DAO для работы с видами налоговых форм
 * @author dsultanbekov
 */
public interface FormTypeDao {
	/**
	 * Получить вид налоговой формы по идентификатору
	 * @param typeId идентификатор вида
	 * @return Объект, представляющий вид налоговой формы
	 * @throws DaoException если в БД нет записи с соответствующим ключом
	 */
	FormType getType(int typeId);
	
	
    List<FormType> listFormTypes();
}
