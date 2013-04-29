package com.aplana.sbrf.taxaccounting.service.script;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;
import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;
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
     * @deprecated источники сейчас деляться на getFormSources и getDeclarationSources
	 */
    @Deprecated
	List<DepartmentFormType> getSources(int departmentId, int formTypeId, FormDataKind kind);
	
	/**
	 * Возвращает информацию о формах-потребителях, которые должны использовать информацию из данной налоговой формы в качестве источника
	 * @param sourceDepartmentId идентификатор подразделения формы-источника
	 * @param sourceFormTypeId вид налоговой формы-источника
	 * @param sourceKind тип налоговой формы-источника
	 * @return информация о формах-потребителях в виде списка {@link DepartmentFormType}
	 */
    @Deprecated
	List<DepartmentFormType> getDestinations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind);

    /**
     * Возвращает информацию об источниках, которые должны использоваться при
     * формировании налоговой формы назначения с заданными параметрами
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
    List<DepartmentFormType> getFormSources(int departmentId, int formTypeId, FormDataKind kind);

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
    List<DepartmentFormType> getFormDestinations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind);

    /**
     * Возвращает информацию о всех налоговых формах, которые являются источниками
     * для налоговых форм или деклараций в заданном подразделении
     * Предполагается что метод будет использоваться для заполнения фильтра,
     * списком доступных для выбора департаментов, типов НФ, и видов НФ (kind)
     *
     * @param departmentId
     *            идентификатор подразделения
     * @param taxType
     *            вид налога
     * @return информация о формах-источниках в виде списка
     *         {@link DepartmentFormType}
     */
    List<DepartmentFormType> getDepartmentSources(int departmentId, TaxType taxType);

    /**
     * Возвращает информацию о декларациях-потребителях, которые должны использовать
     * информацию из данной налоговой формы в качестве источника
     *
     * @param sourceDepartmentId
     *            идентификатор подразделения формы-источника
     * @param sourceFormTypeId
     *            вид налоговой формы-источника
     * @param sourceKind
     *            тип налоговой формы-источника
     * @return информация о декларациях-потребителях в виде списка
     *         {@link com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType}
     */
    List<DepartmentDeclarationType> getDeclarationDestinations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind);

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
    List<DepartmentFormType> getDeclarationSources(int departmentId, int declarationTypeId);

}
