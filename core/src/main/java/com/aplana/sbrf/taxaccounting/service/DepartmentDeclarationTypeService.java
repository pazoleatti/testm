package com.aplana.sbrf.taxaccounting.service;

import java.util.List;
import java.util.Set;

import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;

/**
 * Интерфейс сервиса для работы с привязкой департаментов к подразделениям
 * 
 * @deprecated Нужно использовать SourceService и перенести всё отсюда туда.
 * 
 */
@Deprecated
public interface DepartmentDeclarationTypeService {

	/**
	 * Возвращает идентификаторы всех подразделений, в которых есть декларации по данному виду налога.
	 * @param taxType тип налога
	 * @return набор идентификаторов подразделений
	 */
	@Deprecated
	Set<Integer> getDepartmentIdsByTaxType(TaxType taxType);
	
	/**
	 * Получить список всех видов деклараций по типу налога
	 * @return список видов деклараций
	 */
	@Deprecated
	List<DeclarationType> listAllByTaxType(TaxType taxType);
}
