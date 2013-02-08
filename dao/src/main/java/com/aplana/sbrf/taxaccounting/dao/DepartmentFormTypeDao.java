package com.aplana.sbrf.taxaccounting.dao;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;

/**
 * Интерфейс Dao для работы с привязкой департаментов к подразделениям
 * @author dsultanbekov
 */
public interface DepartmentFormTypeDao {
	/**
	 * Возвращает список идентфикаторов подразделений, формы которых являются источниками для налоговых форм заданного подразеления 
	 * по заданному виду налога.
	 * @param departmentId идентификатор подразделения
	 * @param taxType вид налога
	 * @return список идентфикатор подразделений
	 */
	List<Integer> getSourceDepartmentIds(int departmentId, TaxType taxType);

	/**
	 * Возвращает информацию об источниках, которые должны использоваться при формировании налоговой формы
	 * @param departmentId идентификатор подразделения формируемой налоговой формы
	 * @param formTypeId вид налоговой формы
	 * @param kind тип налоговой формы
	 * @return информация о формах-источниках в виде списка {@link DepartmentFormType}
	 */
	List<DepartmentFormType> getSources(int departmentId, int formTypeId, FormDataKind kind);
	
	/**
	 * Возвращает информацию о формах-источниках, которые должны использоваться при формировании декларации
	 * @param departmentId идентификатор декларации
	 * @param declarationTypeId идентификатор вида декларации
	 * @return информация о формах-источниках в виде списка {@link DepartmentFormType}
	 */
	List<DepartmentFormType> getDeclarationSources(int departmentId, int declarationTypeId);
	
	/**
	 * Возвращает информацию о формах-потребителях, которые должны использовать информацию из данной налоговой формы в качестве источника
	 * @param sourceDepartmentId идентификатор подразделения формы-источника
	 * @param sourceFormTypeId вид налоговой формы-источника
	 * @param sourceKind тип налоговой формы-источника
	 * @return информация о формах-потребителях в виде списка {@link DepartmentFormType}
	 */
	List<DepartmentFormType> getDestanations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind);
	
	/**
	 * Получить информацию о видах и типах налоговых форм с которыми можно работать в подразделении
	 * @param departmentId идентификатор подразделения
	 * @return список записей {@link DepartmentFormType}, представляющий информацию в типах и видах налоговых форм, с которыми можно работать
	 * 	в подразделении
	 */
	List<DepartmentFormType> getDepartmentFormTypes(int departmentId);
}
