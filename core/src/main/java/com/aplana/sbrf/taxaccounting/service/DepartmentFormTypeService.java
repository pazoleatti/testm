package com.aplana.sbrf.taxaccounting.service;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;
import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.FormTypeKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;

/**
 * Интерфейс сервиса для работы с привязкой департаментов к подразделениям
 */
public interface DepartmentFormTypeService {

    /**
     * Возвращает информацию об источниках, которые должны использоваться при формировании налоговой формы
     *
     * @param departmentId идентификатор подразделения формируемой налоговой формы
     * @param formTypeId   вид налоговой формы
     * @param kind         тип налоговой формы
     * @return информация о формах-источниках в виде списка {@link com.aplana.sbrf.taxaccounting.model.DepartmentFormType}
     * @deprecated источники сейчас деляться на getFormSources и getDeclarationSources
     */
    @Deprecated
    List<DepartmentFormType> getSources(int departmentId, int formTypeId, FormDataKind kind);

    /**
     * Возвращает информацию о формах-потребителях, которые должны использовать информацию из данной налоговой формы в качестве источника
     *
     * @param sourceDepartmentId идентификатор подразделения формы-источника
     * @param sourceFormTypeId   вид налоговой формы-источника
     * @param sourceKind         тип налоговой формы-источника
     * @return информация о формах-потребителях в виде списка {@link com.aplana.sbrf.taxaccounting.model.DepartmentFormType}
     */
    @Deprecated
    List<DepartmentFormType> getDestinations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind);

    /**
     * Возвращает информацию об источниках, которые должны использоваться при
     * формировании налоговой формы назначения с заданными параметрами
     *
     * @param departmentId идентификатор подразделения формируемой налоговой формы
     *                     назначения
     * @param formTypeId   вид налоговой формы
     * @param kind         тип налоговой формы
     * @return информация о формах-источниках в виде списка
     *         {@link com.aplana.sbrf.taxaccounting.model.DepartmentFormType}
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
     * Возвращает информацию о формах-потребителях, которые должны использовать
     * информацию из данной налоговой формы в качестве источника
     *
     * @param sourceDepartmentId идентификатор подразделения формы-источника
     * @param sourceFormTypeId   вид налоговой формы-источника
     * @param sourceKind         тип налоговой формы-источника
     * @return информация о формах-потребителях в виде списка
     *         {@link com.aplana.sbrf.taxaccounting.model.DepartmentFormType}
     */
    List<DepartmentFormType> getFormDestinations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind);

    /**
     * Возвращает информацию о всех налоговых формах, которые являются источниками
     * для налоговых форм или деклараций в заданном подразделении
     * Предполагается что метод будет использоваться для заполнения фильтра,
     * списком доступных для выбора департаментов, типов НФ, и видов НФ (kind)
     *
     * @param departmentId идентификатор подразделения
     * @param taxType      вид налога
     * @return информация о формах-источниках в виде списка
     *         {@link com.aplana.sbrf.taxaccounting.model.DepartmentFormType}
     */
    List<DepartmentFormType> getDepartmentFormSources(int departmentId, TaxType taxType);

    /**
     * Возвращает информацию о всех формах-потребителях
     *
     * @param departmentId идентификатор подразделения
     * @param taxType      вид налога
     * @return информация о формах-источниках в виде списка
     *         {@link com.aplana.sbrf.taxaccounting.model.DepartmentFormType}
     */
    List<DepartmentFormType> getDepartmentFormDestinations(int departmentId, TaxType taxType);

    /**
     * Возвращает информацию о декларациях-потребителях, которые должны использовать
     * информацию из данной налоговой формы в качестве источника
     *
     * @param sourceDepartmentId идентификатор подразделения формы-источника
     * @param sourceFormTypeId   вид налоговой формы-источника
     * @param sourceKind         тип налоговой формы-источника
     * @return информация о декларациях-потребителях в виде списка
     *         {@link com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType}
     */
    List<DepartmentDeclarationType> getDeclarationDestinations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind);

    /**
     * Возвращает информацию о формах-источниках, которые должны использоваться
     * при формировании декларации
     *
     * @param departmentId      идентификатор декларации
     * @param declarationTypeId идентификатор вида декларации
     * @return информация о формах-источниках в виде списка
     *         {@link com.aplana.sbrf.taxaccounting.model.DepartmentFormType}
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
     * Возвращает список назначенных деклараций для выбранного налога и подразделения
     *
     * @param departmentId идентификатор подразделения
     * @param taxType      идентификатор вида налога
     * @return список назначенных деклараций для выбранного налога и подразделения
     *         {@link com.aplana.sbrf.taxaccounting.model.FormTypeKind}
     */
    List<FormTypeKind> getDeclarationAssigned(Long departmentId, char taxType);

    /**
     * Добавляет налоговые формы, назначенные подразделению
     *
     * @param departmentId id подразделения
     * @param typeId       id типа налоговой формы
     * @param formId       id вида налоговой формы
     */
    void saveForm(Long departmentId, int typeId, int formId);

    /**
     * Удаляет налоговые формы, назначенные подразделению
     *
     * @param id id на удаление
     */
    void deleteForm(Long id);

    /**
     * Добавляет декларации, назначенные подразделению
     *
     * @param departmentId  id подразделения
     * @param declarationId id вида декларации
     */
    void saveDeclaration(Long departmentId, int declarationId);

    /**
     * Удаляет декларации, назначенные подразделению
     *
     * @param id id на удаление
     */
    void deleteDeclaration(Long id);
    
	/**
	 * Получить вид налоговой формы по идентификатору
	 * @param typeId идентификатор вида
	 * @return Объект, представляющий вид налоговой формы
	 * @throws DaoException если в БД нет записи с соответствующим ключом
	 */
	FormType getFormType(int formTypeId);
	
    /**
     * Получить все существующие виды налоговых форм по виду налога
     * @param taxType вид налога
     * @return список всех существующих видов налоговых форм по виду налога
     */
	List<FormType> listAllByTaxType(TaxType taxType);
    
}
