package com.aplana.sbrf.taxaccounting.service.script;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

/**
 * Интерфейс сервиса для работы с привязкой департаментов к подразделениям
 * @author auldanov
 */
@ScriptExposed
public interface DepartmentFormTypeService {
	
	/**
	 * Возвращает информацию об источниках, которые должны использоваться при формировании налоговой формы
	 * @param departmentId идентификатор подразделения формируемой налоговой формы
	 * @param formTypeId вид налоговой формы
	 * @param kind тип налоговой формы
	 * @return информация о формах-источниках в виде списка {@link DepartmentFormType}
	 */
	List<DepartmentFormType> getSources(int departmentId, int formTypeId, FormDataKind kind);
	
	/**
	 * Возвращает информацию о формах-потребителях, которые должны использовать информацию из данной налоговой формы в качестве источника
	 * @param sourceDepartmentId идентификатор подразделения формы-источника
	 * @param sourceFormTypeId вид налоговой формы-источника
	 * @param sourceKind тип налоговой формы-источника
	 * @return информация о формах-потребителях в виде списка {@link DepartmentFormType}
	 */
	List<DepartmentFormType> getDestination(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind);
	
}
