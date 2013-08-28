package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.*;

import java.util.List;

/**
 * Интерфейс Dao для работы с источникам НФ
 *
 * @author dsultanbekov, sgoryachkin
 */
public interface DepartmentFormTypeDao {
    /**
     * Возвращает информацию о формах по подразделению
     *
     * @param departmentId id подразделения
     * @return список назначенных подразделению форм (с учётом вида и типа)
     */
    List<DepartmentFormType> get(int departmentId);

    /**
     * Возвращает информацию он назначенных подразделению формах по заданному виду налога
     *
     * @param departmentId идентификатор подразделения
     * @param taxType      вид налога
     * @return список назначенных подразделению форм (с учётом вида и типа) по заданному виду налога
     */
    List<DepartmentFormType> getByTaxType(int departmentId, TaxType taxType);

    /**
     * Возвращает информацию об источниках, которые должны использоваться при
     * формировании налоговой формы назначения с заданными параметрами
     *
     * @param departmentId идентификатор подразделения формируемой налоговой формы
     *                     назначения
     * @param formTypeId   вид налоговой формы
     * @param kind         тип налоговой формы
     * @return информация о формах-источниках в виде списка
     *         {@link DepartmentFormType}
     */
    List<DepartmentFormType> getFormSources(int departmentId, int formTypeId, FormDataKind kind);

    /**
     * Обновляет информацию об источниках формы
     *
     * @param departmentFormTypeId        идентификатор связки для которой нужно обновить источники
     * @param sourceDepartmentFormTypeIds идентификаторы форм-источников в виде списка
     */
    void saveFormSources(Long departmentFormTypeId, List<Long> sourceDepartmentFormTypeIds);

    /**
     * Возвращает информацию о всех налоговых формах, которые являются источниками
     * для налоговых форм или деклараций в заданном подразделении
     * Предполагается что метод будет использоваться для заполнения фильтра,
     * списком доступных для выбора департаментов, типов НФ, и видов НФ (kind)
     *
     * @param departmentId идентификатор подразделения
     * @param taxType      вид налога
     * @return информация о формах-источниках в виде списка
     *         {@link DepartmentFormType}
     */
    List<DepartmentFormType> getDepartmentSources(int departmentId, TaxType taxType);

    /**
     * Возвращает информацию о формах-потребителях, которые должны использовать
     * информацию из данной налоговой формы в качестве источника
     *
     * @param sourceDepartmentId идентификатор подразделения формы-источника
     * @param sourceFormTypeId   вид налоговой формы-источника
     * @param sourceKind         тип налоговой формы-источника
     * @return информация о формах-потребителях в виде списка
     *         {@link DepartmentFormType}
     */
    List<DepartmentFormType> getFormDestinations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind);

    /**
     * Возвращает информацию о декларациях-потребителях, которые должны использовать
     * информацию из данной налоговой формы в качестве источника
     *
     * @param sourceDepartmentId идентификатор подразделения формы-источника
     * @param sourceFormTypeId   вид налоговой формы-источника
     * @param sourceKind         тип налоговой формы-источника
     * @return информация о декларациях-потребителях в виде списка
     *         {@link DepartmentDeclarationType}
     */
    List<DepartmentDeclarationType> getDeclarationDestinations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind);

    /**
     * Возвращает информацию о формах-источниках, которые должны использоваться
     * при формировании декларации
     *
     * @param departmentId      идентификатор декларации
     * @param declarationTypeId идентификатор вида декларации
     * @return информация о формах-источниках в виде списка
     *         {@link DepartmentFormType}
     */
    List<DepartmentFormType> getDeclarationSources(int departmentId, int declarationTypeId);

    /**
     * Обновляет информацию об источниках для декларации
     *
     * @param declarationTypeId           идентификатор связки для которой нужно обновить источники
     * @param sourceDepartmentFormTypeIds идентификаторы деклараций-источников в виде списка
     */
    void saveDeclarationSources(final Long declarationTypeId, final List<Long> sourceDepartmentFormTypeIds);

    /**
     * Возвращает список назначенных налоговых форм для выбранного налога и подразделения
     *
     * @param departmentId идентификатор подразделения
     * @param taxType      идентификатор вида налога
     * @return список назначенных налоговых форм для выбранного налога и подразделения
     *         {@link com.aplana.sbrf.taxaccounting.model.FormTypeKind}
     */
    List<FormTypeKind> getFormAssigned(Long departmentId, char taxType);

    /**
     * Возвращает список назначенных налоговых форм для выбранного налога и подразделения
     *
     * @param departmentId идентификатор подразделения
     * @param taxType      идентификатор вида налога
     * @return список назначенных налоговых форм для выбранного налога и подразделения
     *         {@link com.aplana.sbrf.taxaccounting.model.FormTypeKind}
     */
    List<FormTypeKind> getDeclarationAssigned(Long departmentId, char taxType);

    /**
     * Добавляет налоговые формы, назначенные подразделению
     */
    void createDepartmentFormType(Long departmentId, int typeId, int formId);

    /**
     * Удаляет налоговые формы, назначенные подразделению
     */
    void deleteDepartmentFormType(Long id);

    /**
     * Добавляет декларации, назначенные подразделению
     */
    void createDepartmentDeclType(Long departmentId, int declarationId);

    /**
     * Удаляет декларации, назначенные подразделению
     */
    void deleteDepartmentDeclType(Long id);
}
