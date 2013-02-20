package com.aplana.sbrf.taxaccounting.dao;

import java.util.List;

import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.service.FormDataSearchService;

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
	
    /**
     * Получить список видов налоговых форм для определенного департамента и с определенным типом налога
     * @param departmentId идентификатор департамента
     * @param taxType тип налога
     * @return Список видов налоговых форм для определенного департамента и с определенным типом налога
     * @deprecated этот метод нужно удалить после того, как будет удалён {@link FormDataSearchService#getAvailableFormTypes(int, TaxType)}
     */
	@Deprecated
    List<FormType> listAllByDepartmentIdAndTaxType(int departmentId, TaxType taxType);
	
}
