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
	 * Возвращает информацию о декларациях, формируемых в указанном подразделении
	 * @param departmentId идентификатор подразделения
	 * @return список {@link DepartmentDeclarationType}, представляющий перечень деклараций, формируемых в подразделении,
	 * 	задаваемом departmentId
	 */
	@Deprecated
	List<DepartmentDeclarationType> getDepartmentDeclarationTypes(int departmentId);

	/**
	 * Возвращает информацию о декларациях-потребителях, которые должны использовать информацию из данной налоговой формы в качестве источника
	 * @param sourceDepartmentId идентификатор подразделения формы-источника
	 * @param sourceFormTypeId вид налоговой формы-источника
	 * @param sourceKind тип налоговой формы-источника
	 * @return информация о декларациях-потребителях в виде списка {@link DepartmentDeclarationType}
	 */
	@Deprecated
	List<DepartmentDeclarationType> getDestinations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind);

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
