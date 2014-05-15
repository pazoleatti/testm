package com.aplana.sbrf.taxaccounting.service;

import java.util.Collection;
import java.util.List;

import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;
import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.FormTypeKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.util.Pair;

/**
 * Интерфейс сервиса для работы с привязкой департаментов к подразделениям
 */
public interface SourceService {


    /**
     * Возвращает НФ назначения, которые являются источниками
     * для налоговых форм или деклараций в заданном подразделении
     * Предполагается что метод будет использоваться для заполнения фильтра,
     * списком доступных для выбора департаментов, типов НФ, и видов НФ (kind)
     *
     * @param departmentId идентификатор подразделения
     * @param taxType      вид налога
     * @return информация о формах-источниках в виде списка
     *         {@link com.aplana.sbrf.taxaccounting.model.DepartmentFormType}
     */
    List<DepartmentFormType> getDFTSourcesByDepartment(int departmentId, TaxType taxType);
    
    /**
     * Возвращает НФ назначения для подразделения
     *
     * @param departmentId идентификатор подразделения
     * @param taxType      вид налога
     * @return информация о формах-источниках в виде списка
     *         {@link com.aplana.sbrf.taxaccounting.model.DepartmentFormType}
     */
    List<DepartmentFormType> getDFTByDepartment(int departmentId, TaxType taxType);

    /**
     * Возвращает типы НФ назначения для подразделения с заданным исполнителем
     *
     * @param performerDepId    идентификатор подразделения исполнителя
     * @param taxType           вид налога
     * @return список типов НФ
     */
    List<Long> getDFTByPerformerDep(int performerDepId, TaxType taxType, List<FormDataKind> kinds);

    /**
     * Возвращает типы НФ:
     * 1) типы НФ, для которых подразделение узазано в качестве исполнителя;
     * 2) типы НФ, которые назначены формам из 1) в качестве источников;
     * 3) типы НФ, которые назначены формам из 2) в качестве источников.
     * (из http://conf.aplana.com/pages/viewpage.action?pageId=11382061 пункт 3.b.iii, 3.b.iv, 3.b.v)
     *
     * @param performerDepId    идентификатор подразделения исполнителя
     * @param taxType           вид налога
     * @return список типов НФ
     */
    List<Long> getDFTFormTypeBySource(int performerDepId, TaxType taxType, List<FormDataKind> kinds);
	/**
	 * Возвращает информацию о назначенных подразделению декларациях по заданному виду налога
	 * 
	 * @param departmentId идентификатор подразделения
	 * @param taxType вид налога
	 * @return список назначенных подразделению деклараций (с учётом вида и типа) по заданному виду налога
	 */
	List<DepartmentDeclarationType> getDDTByDepartment(int departmentId, TaxType taxType);
    
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
    List<DepartmentFormType> getDFTSourcesByDFT(int departmentId, int formTypeId, FormDataKind kind);
    
    /**
     * Возвращает НФ назначения (DFT), которые являются источником для ДЕ назначения (DDT) 
     * (в данном случае DDT и связка департамента и типа декларации - одно и тоже)
     *
     * @param departmentId      идентификатор декларации
     * @param declarationTypeId идентификатор вида декларации
     * @return информация о формах-источниках в виде списка
     *         {@link com.aplana.sbrf.taxaccounting.model.DepartmentFormType}
     */
    List<DepartmentFormType> getDFTSourceByDDT(int departmentId, int declarationTypeId);
    
    /**
     * Обновляет информацию об источниках формы
     *
     * @param leftDpartmentFormTypeId     идентификатор связки из левой части формы. Т.е тот, для которого назначают множество источников/приемников
     * @param rightDepartmentFormTypeIds идентификаторы связок из справой части формы. Т.е те, которые являются множеством источников/приемников для связки из левой части
     */
    void saveFormSources(Long leftDpartmentFormTypeId, List<Long> rightDepartmentFormTypeIds);

 

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
     * Обновляет информацию об источниках для декларации
     *
     * @param declarationTypeId           идентификатор связки для которой нужно обновить источники
     * @param sourceDepartmentFormTypeIds идентификаторы деклараций-источников в виде списка
     */
    void saveDeclarationSources(final Long declarationTypeId, final List<Long> sourceDepartmentFormTypeIds);

 

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
     * Возвращает список назначенных налоговых форм для выбранного налога и подразделения
     *
     * @param departmentId идентификатор подразделения
     * @param taxType      идентификатор вида налога
     * @return список назначенных налоговых форм для выбранного налога и подразделения
     *         {@link com.aplana.sbrf.taxaccounting.model.FormTypeKind}
     */
    List<FormTypeKind> getFormAssigned(Long departmentId, char taxType);

    /**
     * Добавляет налоговые формы, назначенные подразделению
     *
     * @param departmentId id подразделения
     * @param typeId       id типа налоговой формы
     * @param formId       id вида налоговой формы
     */
    void saveDFT(Long departmentId, int typeId, int formId);

    /**
     * Добавляет налоговые формы, назначенные подразделению
     *
     *
     * @param departmentId id подразделения
     * @param typeId       id типа налоговой формы
     * @param formId       id вида налоговой формы
     * @param performerId  id исполнителя
     */
    void saveDFT(Long departmentId, int typeId, int formId, Integer performerId);

    /**
     * Удаляет налоговые формы, назначенные подразделению
     *
     * @param id id на удаление
     */
    void deleteDFT(Collection<Long> ids);

    /**
     * Добавляет декларации, назначенные подразделению
     *
     * @param departmentId  id подразделения
     * @param declarationId id вида декларации
     */
    void saveDDT(Long departmentId, int declarationId);

    /**
     * Удаляет декларации, назначенные подразделению
     *
     * @param id id на удаление
     */
    void deleteDDT(Collection<Long> ids);
    
	/**
	 * Получить вид налоговой формы по идентификатору
	 * @param typeId идентификатор вида
	 * @return Объект, представляющий вид налоговой формы
	 * @throws DaoException если в БД нет записи с соответствующим ключом
	 */
	FormType getFormType(int formTypeId);
	
	/**
	 * Получить описание вида декларации по идентификатору
	 * @param declarationTypeId идентификатор вида декларации
	 * @return описание вида декларации, с заданным идентификатором
	 * @throws DaoException если в БД нет такой записи
	 */
	DeclarationType getDeclarationType(int declarationTypeId);
	
    /**
     * Получить все существующие виды налоговых форм по виду налога
     * @param taxType вид налога
     * @return список всех существующих видов налоговых форм по виду налога
     */
	List<FormType> listAllByTaxType(TaxType taxType);
	
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
     * Проверяет существование формы назначения для позразделения с id = departmentId
     * c идентификатором вида typeId и идентификатором типа kindId
     *
     * @param departmentId
     * @param typeId
     * @param kindId
     * @return true - существует форма, false в противном случае
     */
    boolean existAssignedForm(int departmentId, int typeId, FormDataKind kind);

    /**
     * Проверяет существование форм-приемников в статусе "Принята" в указанном отчетном периоде
     * @param sourceDepartmentId идентификатор подразделения формы-источника
     * @param sourceFormTypeId   вид налоговой формы-источника
     * @param sourceKind         тип налоговой формы-источника
     * @param reportPeriodId     идентификатор отчетного периода
     * @return список найденных пар "вид приёмника-подразделение приёмника"
     */
    List<Pair<String, String>> existAcceptedDestinations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind, Integer reportPeriodId);

    /**
     * Обновление исполнителя для назначенной формы
     */
    void updatePerformer(int id, Integer performerId);

	List<DeclarationType> allDeclarationTypeByTaxType(TaxType taxType);
}
