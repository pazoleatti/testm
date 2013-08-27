package com.aplana.sbrf.taxaccounting.service;

import java.util.List;
import java.util.Set;

import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;

/**
 * Интерфейс сервиса для работы с привязкой департаментов к подразделениям
 */
public interface DepartmentDeclarationTypeService {

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
	 * Возвращает информацию о назначенных подразделению декларациях по заданному виду налога
	 * @param departmentId идентификатор подразделения
	 * @param taxType вид налога
	 * @return список назначенных подразделению деклараций (с учётом вида и типа) по заданному виду налога
	 */
	List<DepartmentDeclarationType> getByTaxType(int departmentId, TaxType taxType);

	/**
	 * Обновляет/добавляет список назначенных подразделению деклараций (с учётом вида и типа)
	 * @param departmentId
	 *            идентификатор подразделения формируемой налоговой формы
	 *            назначения
	 * @param departmentDeclarationTypes
	 *            новые данные для обновления/добавления
	 */
	void save(int departmentId, List<DepartmentDeclarationType> departmentDeclarationTypes);
	
	/**
	 * Получить описание вида декларации по идентификатору
	 * @param declarationTypeId идентификатор вида декларации
	 * @return описание вида декларации, с заданным идентификатором
	 * @throws DaoException если в БД нет такой записи
	 */
	DeclarationType getDeclarationType(int declarationTypeId);
	
	/**
	 * Получить список всех видов деклараций по типу налога
	 * @return список видов деклараций
	 */
	List<DeclarationType> listAllByTaxType(TaxType taxType);
}
