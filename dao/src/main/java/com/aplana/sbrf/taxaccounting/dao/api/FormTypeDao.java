package com.aplana.sbrf.taxaccounting.dao.api;

import java.util.List;

import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.TaxType;

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
	
	/**
	 * Получить полный список видов налоговых форм
	 * @return список видов налоговых форм
	 */
    List<FormType> listFormTypes();

    /**
     * Получить все существующие виды налоговых форм по виду налога
     * @param taxType вид налога
     * @return список всех существующих видов налоговых форм по виду налога
     */
	List<FormType> listAllByTaxType(TaxType taxType);
	
}
