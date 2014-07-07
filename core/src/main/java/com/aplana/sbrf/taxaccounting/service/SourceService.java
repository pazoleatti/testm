package com.aplana.sbrf.taxaccounting.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.source.SourceClientData;
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
     * @param periodStart  начало периода, в котором действуют назначения
     * @param periodEnd    окончание периода, в котором действуют назначения
     * @return информация о формах-источниках в виде списка
     *         {@link com.aplana.sbrf.taxaccounting.model.DepartmentFormType}
     */
    List<DepartmentFormType> getDFTSourcesByDepartment(int departmentId, TaxType taxType, Date periodStart, Date periodEnd);

    /**
     * Возвращает типы НФ назначения для подразделения с заданным исполнителем
     *
     * @param performerDepId идентификатор подразделения исполнителя
     * @param taxType        вид налога
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
     * @param performerDepId идентификатор подразделения исполнителя
     * @param taxType        вид налога
     * @return список типов НФ
     */
    List<Long> getDFTFormTypeBySource(int performerDepId, TaxType taxType, List<FormDataKind> kinds);

    /**
     * Возвращает НФ назначения для подразделения
     *
     * @param departmentId идентификатор подразделения
     * @param taxType      вид налога
     * @param periodStart  начало периода, в котором действуют назначения
     * @param periodEnd    окончание периода, в котором действуют назначения
     * @return информация о формах-источниках в виде списка
     *         {@link com.aplana.sbrf.taxaccounting.model.DepartmentFormType}
     */
    List<DepartmentFormType> getDFTByDepartment(int departmentId, TaxType taxType, Date periodStart, Date periodEnd);

    /**
     * Возвращает информацию о назначенных подразделению декларациях по заданному виду налога
     *
     * @param departmentId идентификатор подразделения
     * @param taxType      вид налога
     * @param periodStart  начало периода, в котором действуют назначения
     * @param periodEnd    окончание периода, в котором действуют назначения
     * @return список назначенных подразделению деклараций (с учётом вида и типа) по заданному виду налога
     */
    List<DepartmentDeclarationType> getDDTByDepartment(int departmentId, TaxType taxType, Date periodStart, Date periodEnd);

    /**
     * Возвращает информацию об источниках, которые должны использоваться при
     * формировании налоговой формы назначения с заданными параметрами
     *
     * @param departmentId идентификатор подразделения формируемой налоговой формы
     *                     назначения
     * @param formTypeId   вид налоговой формы
     * @param kind         тип налоговой формы
     * @param periodStart  начало периода, в котором действуют назначения
     * @param periodEnd    окончание периода, в котором действуют назначения
     * @return информация о формах-источниках в виде списка
     *         {@link com.aplana.sbrf.taxaccounting.model.DepartmentFormType}
     */
    List<DepartmentFormType> getDFTSourcesByDFT(int departmentId, int formTypeId, FormDataKind kind, Date periodStart, Date periodEnd);

    /**
     * Возвращает информацию об источниках, которые должны использоваться при
     * формировании налоговой формы назначения с заданными параметрами
     *
     * @param departmentId
     * @param formTypeId
     * @param kind
     * @param reportPeriodId
     * @return
     */
    List<DepartmentFormType> getDFTSourcesByDFT(int departmentId, int formTypeId, FormDataKind kind, int reportPeriodId);

    /**
     * Возвращает НФ назначения (DFT), которые являются источником для ДЕ назначения (DDT)
     * (в данном случае DDT и связка департамента и типа декларации - одно и тоже)
     *
     * @param departmentId      идентификатор декларации
     * @param declarationTypeId идентификатор вида декларации
     * @param periodStart  начало периода, в котором действуют назначения
     * @param periodEnd    окончание периода, в котором действуют назначения
     * @return информация о формах-источниках в виде списка
     *         {@link com.aplana.sbrf.taxaccounting.model.DepartmentFormType}
     */
    List<DepartmentFormType> getDFTSourceByDDT(int departmentId, int declarationTypeId, Date periodStart, Date periodEnd);

    /**
     * Возвращает информацию о декларациях-потребителях, которые должны использовать
     * информацию из данной налоговой формы в качестве источника
     *
     * @param sourceDepartmentId идентификатор подразделения формы-источника
     * @param sourceFormTypeId   вид налоговой формы-источника
     * @param sourceKind         тип налоговой формы-источника
     * @param periodStart  начало периода, в котором действуют назначения
     * @param periodEnd    окончание периода, в котором действуют назначения
     * @return информация о декларациях-потребителях в виде списка
     *         {@link com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType}
     */
    List<DepartmentDeclarationType> getDeclarationDestinations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind, Date periodStart, Date periodEnd);

    /**
     * Возвращает информацию о декларациях-потребителях, которые должны использовать
     * информацию из данной налоговой формы в качестве источника
     *
     * @param sourceDepartmentId идентификатор подразделения формы-источника
     * @param sourceFormTypeId   вид налоговой формы-источника
     * @param sourceKind         тип налоговой формы-источника
     * @param reportPeriodId  отчетный период
     * @return информация о декларациях-потребителях в виде списка
     *         {@link com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType}
     */
    List<DepartmentDeclarationType> getDeclarationDestinations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind, int reportPeriodId);

    /**
     * Обновляет информацию об источниках для декларации
     *
     * @param declarationTypeId           идентификатор связки для которой нужно обновить источники
     * @param sourceDepartmentFormTypeIds идентификаторы деклараций-источников в виде списка
     */
    @Deprecated
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
     * @param departmentId id подразделения
     * @param typeId       id типа налоговой формы
     * @param formId       id вида налоговой формы
     * @param performerId  id исполнителя
     */
    void saveDFT(Long departmentId, int typeId, int formId, Integer performerId);

    /**
     * Удаляет налоговые формы, назначенные подразделению
     *
     * @param ids id на удаление
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
     * @param ids id на удаление
     */
    void deleteDDT(Collection<Long> ids);

    /**
     * Получить вид налоговой формы по идентификатору
     *
     * @param formTypeId идентификатор вида
     * @return Объект, представляющий вид налоговой формы
     */
    FormType getFormType(int formTypeId);

    /**
     * Получить описание вида декларации по идентификатору
     *
     * @param declarationTypeId идентификатор вида декларации
     * @return описание вида декларации, с заданным идентификатором
     */
    DeclarationType getDeclarationType(int declarationTypeId);

    /**
     * Получить все существующие виды налоговых форм по виду налога
     *
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
     * @param periodStart  начало периода, в котором действуют назначения
     * @param periodEnd    окончание периода, в котором действуют назначения
     * @return информация о формах-потребителях в виде списка
     *         {@link com.aplana.sbrf.taxaccounting.model.DepartmentFormType}
     */
    List<DepartmentFormType> getFormDestinations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind, Date periodStart, Date periodEnd);

    /**
     * Возвращает информацию о формах-потребителях, которые должны использовать
     * информацию из данной налоговой формы в качестве источника
     *
     * @param sourceDepartmentId идентификатор подразделения формы-источника
     * @param sourceFormTypeId   вид налоговой формы-источника
     * @param sourceKind         тип налоговой формы-источника
     * @param reportPeriodId  отчетный период
     * @return информация о формах-потребителях в виде списка
     *         {@link com.aplana.sbrf.taxaccounting.model.DepartmentFormType}
     */
    List<DepartmentFormType> getFormDestinations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind, int reportPeriodId);


    /**
     * Проверяет существование формы назначения для позразделения с id = departmentId
     * c идентификатором вида typeId и идентификатором типа kindId
     *
     * @param departmentId
     * @param typeId
     * @param kind
     * @return true - существует форма, false в противном случае
     */
    boolean existAssignedForm(int departmentId, int typeId, FormDataKind kind);

    /**
     * Проверяет существование форм-приемников в статусе "Принята" в указанном отчетном периоде
     *
     * @param sourceDepartmentId идентификатор подразделения формы-источника
     * @param sourceFormTypeId   вид налоговой формы-источника
     * @param sourceKind         тип налоговой формы-источника
     * @param reportPeriodId     идентификатор отчетного периода
     * @param periodStart  начало периода, в котором действуют назначения
     * @param periodEnd    окончание периода, в котором действуют назначения
     * @return список найденных пар "вид приёмника-подразделение приёмника"
     */
    List<Pair<String, String>> existAcceptedDestinations(int sourceDepartmentId, int sourceFormTypeId,
                                                         FormDataKind sourceKind, Integer reportPeriodId,
                                                         Date periodStart, Date periodEnd);

    /**
     * Обновление исполнителя для назначенной формы
     */
    void updatePerformer(int id, Integer performerId);

    List<DeclarationType> allDeclarationTypeByTaxType(TaxType taxType);

    /**
     * Создает новые назначения источников-приемников
     *
     * @param sourceClientData данные связок источников-приемников
     */
    void createSources(Logger logger, SourceClientData sourceClientData);

    /**
     * Удаляет указанные назначения источников-приемников
     *
     * @param sourceClientData данные связок источников-приемников
     */
    void deleteSources(Logger logger, SourceClientData sourceClientData);

    /**
     * Обновляет указанные назначения источников-приемников
     *
     * @param sourceClientData данные связок источников-приемников
     */
    void updateSources(Logger logger, SourceClientData sourceClientData);
}
