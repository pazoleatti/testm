package com.aplana.sbrf.taxaccounting.dao;

import java.util.List;
import java.util.Set;

import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;

/**
 * Dao для работы с {@link DepartmentDeclarationType информацией о назначении деклараций подразделениям}  
 * @author dsultanbekov
 */
public interface DepartmentDeclarationTypeDao {
	/**
	 * Возвращает информацию о декларациях, формируемых в указанном подразделении
	 * @param departmentId идентификатор подразделения
	 * @return список {@link DepartmentDeclarationType}, представляющий перечень деклараций, формируемых в подразделении,
	 * 	задаваемом departmentId 
	 */
	List<DepartmentDeclarationType> getDepartmentDeclarationTypes(int departmentId);

	/**
	 * Возвращает информацию о декларациях-потребителях, которые должны использовать информацию из данной налоговой формы в качестве источника
	 * @param sourceDepartmentId идентификатор подразделения формы-источника
	 * @param sourceFormTypeId вид налоговой формы-источника
	 * @param sourceKind тип налоговой формы-источника
	 * @return информация о декларациях-потребителях в виде списка {@link DepartmentDeclarationType}
	 */
	List<DepartmentDeclarationType> getDestinations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind);

	/**
	 * Возвращает идентификаторы всех подразделений, в которых есть декларации по данному виду налога.
	 * @param taxType тип налога
	 * @return набор идентификаторов подразделений
	 */
	Set<Integer> getDepartmentIdsByTaxType(TaxType taxType);

	/**
	 * Обновляет/добавляет список назначенных подразделению деклараций (с учётом вида и типа)
	 * @param departmentId
	 *            идентификатор подразделения формируемой налоговой формы
	 *            назначения
	 * @param departmentDeclarationTypes
	 *            новые данные для обновления/добавления
	 */
	void save(int departmentId, List<DepartmentDeclarationType> departmentDeclarationTypes);
}
