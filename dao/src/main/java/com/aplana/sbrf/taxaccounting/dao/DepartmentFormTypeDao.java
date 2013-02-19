package com.aplana.sbrf.taxaccounting.dao;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;

/**
 * Интерфейс Dao для работы с источникам НФ
 * 
 * @author dsultanbekov, sgoryachkin
 */
public interface DepartmentFormTypeDao {
	/**
	 * Возвращает информацию о формах по подразделению
	 * 
	 * @param departamentId
	 * @return
	 */
	List<DepartmentFormType> get(int departamentId);
	
	/**
	 * Возвращает информацию о формах по подразделению и типу налога
	 * 
	 * @param departmentId
	 * @param taxType
	 * @return
	 */
	List<DepartmentFormType> get(int departmentId, TaxType taxType);

	/**
	 * Возвращает информацию об источниках, которые должны использоваться при
	 * формировании налоговой формы назначения с заданными параметрами (эта
	 * форма назначения по идее должна быть только одна)
	 * 
	 * @param departmentId
	 *            идентификатор подразделения формируемой налоговой формы
	 *            назначения
	 * @param formTypeId
	 *            вид налоговой формы
	 * @param kind
	 *            тип налоговой формы
	 * @return информация о формах-источниках в виде списка
	 *         {@link DepartmentFormType}
	 */
	List<DepartmentFormType> getFormSources(int departmentId, int formTypeId,
			FormDataKind kind);

	/**
	 * Возвращает информацию об источниках, которые должны использоваться при
	 * формировании налоговых форм назначения заданного подразеления.
	 * Предпологается что метод будет использоваться для проверки прав.
	 * 
	 * @param departmentId
	 *            идентификатор подразделения формируемой налоговой формы
	 *            назначения
	 * @return
	 */
	List<DepartmentFormType> getFormSources(int departmentId);

	/**
	 * Возвращает информацию об источниках, которые должны использоваться при
	 * формировании налоговых форм назначения заданного подразеления по
	 * заданному виду налога. Предпологается что метод будет использоваться для
	 * заполнения фильтра, списком доступных для выбора департаментов, типоф НФ,
	 * и видов НФ (kind)
	 * 
	 * @param departmentId
	 *            идентификатор подразделения
	 * @param taxType
	 *            вид налога
	 * @return список идентфикатор подразделений
	 */
	List<DepartmentFormType> getFormSources(int departmentId, TaxType taxType);

	/**
	 * Возвращает информацию о формах-потребителях, которые должны использовать
	 * информацию из данной налоговой формы в качестве источника
	 * 
	 * @param sourceDepartmentId
	 *            идентификатор подразделения формы-источника
	 * @param sourceFormTypeId
	 *            вид налоговой формы-источника
	 * @param sourceKind
	 *            тип налоговой формы-источника
	 * @return информация о формах-потребителях в виде списка
	 *         {@link DepartmentFormType}
	 */
	List<DepartmentFormType> getFormDestinations(int sourceDepartmentId,
			int sourceFormTypeId, FormDataKind sourceKind);

	/**
	 * Возвращает информацию о формах-источниках, которые должны использоваться
	 * при формировании декларации
	 * 
	 * @param departmentId
	 *            идентификатор декларации
	 * @param declarationTypeId
	 *            идентификатор вида декларации
	 * @return информация о формах-источниках в виде списка
	 *         {@link DepartmentFormType}
	 */
	List<DepartmentFormType> getDeclarationSources(int departmentId,
			int declarationTypeId);


}
