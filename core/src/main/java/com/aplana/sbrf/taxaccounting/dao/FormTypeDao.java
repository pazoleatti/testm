package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.TaxType;

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


	/**
	 * Получить список видов налоговых форм с определенным типом налога
	 * @param taxType тип налога
	 * @return Список видов налоговых форм с определенным типом налога
	 */
	List<FormType> listAllByTaxType(TaxType taxType);

    /**
     * Получить список видов налоговых форм для определенного департамента и с определенным типом налога
     * @param departmentId идентификатор департамента
     * @param taxType тип налога
     * @return Список видов налоговых форм для определенного департамента и с определенным типом налога
     */
    List<FormType> listAllByDepartmentIdAndTaxType(int departmentId, TaxType taxType);
}
