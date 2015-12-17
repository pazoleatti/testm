package com.aplana.sbrf.taxaccounting.dao.api;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * Интерфейс Dao для работы с источникам НФ
 *
 * @author dsultanbekov, sgoryachkin
 */
public interface DepartmentFormTypeDao {

    /**
     * Получает назначения по идентификаторам
     * @param ids списо идентификаторов
     */
    List<DepartmentFormType> getByListIds(List<Long> ids);

    /**
     * Возвращает информацию о формах по подразделению
     *
     * @param departmentId id подразделения
     * @return список назначенных подразделению форм (с учётом вида и типа)
     */
    List<DepartmentFormType> getByDepartment(int departmentId);

    /**
     * Возвращает информацию он назначенных подразделению формах по заданному виду налога
     *
     * @param departmentId идентификатор подразделения
     * @param taxType      вид налога
     * @param periodStart  начало периода, в котором действуют назначения
     * @param periodEnd    окончание периода, в котором действуют назначения
     * @return список назначенных подразделению форм (с учётом вида и типа) по заданному виду налога
     */
    List<DepartmentFormType> getByTaxType(int departmentId, TaxType taxType, Date periodStart, Date periodEnd);

    /**
     * Возвращает информацию он назначенных подразделению формах по заданному виду налога
     *
     * @param departmentId идентификатор подразделения
     * @param taxType      вид налога
     * @param periodStart  начало периода, в котором действуют назначения
     * @param periodEnd    окончание периода, в котором действуют назначения
     * @param queryParams  параметры пейджинга и фильтрации
     * @return список назначенных подразделению форм (с учётом вида и типа) по заданному виду налога
     */
    List<DepartmentFormType> getByTaxType(int departmentId, TaxType taxType, Date periodStart, Date periodEnd,
                                          QueryParams queryParams);

    /**
     * Возвращает информацию он назначенных подразделению формах по заданному виду налога
     *
     * @param departmentId идентификатор подразделения
     * @param taxType      вид налога
     * @return список назначенных подразделению форм (с учётом вида и типа) по заданному виду налога
     */
    @Deprecated
    List<DepartmentFormType> getByTaxType(int departmentId, TaxType taxType);

    /**
     * Возвращает информацию о формах по заданному виду налога и исполнителю
     *
     * @param performerDepId    идентификатор подразделения исполнителя
     * @param taxTypes           вид налога
     * @return список назначенных подразделению форм (с учётом вида и типа) по заданному виду налога
     */
    List<Long> getByPerformerId(int performerDepId, List<TaxType> taxTypes, List<FormDataKind> kinds);

    /**
     * Возвращает назначения по по заданному типу налога и исполнителю
     *
     * @param performerDepId    идентификатор подразделения исполнителя
     * @param taxTypes           вид налога
     * @return список назначенных подразделению форм (с учётом вида и типа) по заданному виду налога
     */
    List<Long> getDFTByPerformerId(int performerDepId, List<TaxType> taxTypes, List<FormDataKind> kinds);

    /**
     * Возвращает типы НФ:
     * 1) типы НФ, для которых подразделение узазано в качестве исполнителя;
     * 2) типы НФ, которые назначены формам из 1) в качестве источников;
     * 3) типы НФ, которые назначены формам из 2) в качестве источников.
     * (из http://conf.aplana.com/pages/viewpage.action?pageId=11382061 пункты 3.b.iii, 3.b.iv, 3.b.v)
     *
     * @param performerDepId    идентификатор подразделения исполнителя
     * @param taxType           вид налога
     * @return список назначенных подразделению форм (с учётом вида и типа) по заданному виду налога
     */
    List<Long> getFormTypeBySource(int performerDepId, TaxType taxType, List<FormDataKind> kinds);

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
     *         {@link DepartmentFormType}
     */
    List<DepartmentFormType> getFormSources(int departmentId, int formTypeId, FormDataKind kind, Date periodStart, Date periodEnd);

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
     * @param queryParams  параметры пейджинга и фильтрации
     * @return информация о формах-источниках в виде списка
     * {@link DepartmentFormType}
     */
    List<DepartmentFormType> getFormSources(int departmentId, int formTypeId, FormDataKind kind, Date periodStart,
                                            Date periodEnd, QueryParams queryParams);

    /**
     * Возвращает информацию о всех налоговых формах, которые являются источниками
     * для налоговых форм или деклараций в заданном подразделении
     * Предполагается что метод будет использоваться для заполнения фильтра,
     * списком доступных для выбора департаментов, типов НФ, и видов НФ (kind)
     *
     * @param departmentId идентификатор подразделения
     * @param taxType      вид налога
     * @param periodStart  начало периода, в котором действуют назначения
     * @param periodEnd    окончание периода, в котором действуют назначения
     * @return информация о формах-источниках в виде списка
     *         {@link DepartmentFormType}
     */
    List<DepartmentFormType> getDepartmentSources(int departmentId, TaxType taxType, Date periodStart, Date periodEnd);

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
     *         {@link DepartmentFormType}
     */
    List<DepartmentFormType> getFormDestinations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind, Date periodStart, Date periodEnd);

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
    @Deprecated
    List<DepartmentFormType> getFormDestinations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind);

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
     *         {@link DepartmentDeclarationType}
     */
    List<DepartmentDeclarationType> getDeclarationDestinations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind, Date periodStart, Date periodEnd);

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
    @Deprecated
    List<DepartmentDeclarationType> getDeclarationDestinations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind);


    /**
     * Возвращает информацию о формах-источниках, которые должны использоваться
     * при формировании декларации
     *
     * @param departmentId      идентификатор декларации
     * @param declarationTypeId идентификатор вида декларации
     * @param periodStart       начало периода, в котором действуют назначения
     * @param periodEnd         окончание периода, в котором действуют назначения
     * @return информация о формах-источниках в виде списка
     * {@link DepartmentFormType}
     */
    List<DepartmentFormType> getDeclarationSources(int departmentId, int declarationTypeId, Date periodStart, Date periodEnd);

    /**
     * Возвращает информацию о формах-источниках, которые должны использоваться
     * при формировании декларации
     *
     * @param departmentId      идентификатор декларации
     * @param declarationTypeId идентификатор вида декларации
     * @param periodStart       начало периода, в котором действуют назначения
     * @param periodEnd         окончание периода, в котором действуют назначения
     * @param queryParams       параметры пейджинга и фильтрации
     * @return информация о формах-источниках в виде списка
     * {@link DepartmentFormType}
     */
    List<DepartmentFormType> getDeclarationSources(int departmentId, int declarationTypeId, Date periodStart, Date periodEnd,
                                                   QueryParams queryParams);

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
     * Возвращает список назначенных налоговых форм для выбранного налога и подразделений
     *
     * @param departmentIds идентификаторы подразделений
     * @param taxType       идентификатор вида налога
     * @param queryParams   параметры пейджинга и фильтрации
     * @return список назначенных налоговых форм для выбранного налога и подразделений
     */
    List<FormTypeKind> getAllFormAssigned(List<Long> departmentIds, char taxType, QueryParams<TaxNominationColumnEnum> queryParams);

    /**
     * TODO - возможно нужно переместить в {@link com.aplana.sbrf.taxaccounting.dao.api.DepartmentDeclarationTypeDao}
     * Возвращает список назначенных налоговых форм для выбранного налога и подразделения
     *
     * @param departmentId идентификатор подразделения
     * @param taxType      идентификатор вида налога
     * @return список назначенных налоговых форм для выбранного налога и подразделения
     *         {@link com.aplana.sbrf.taxaccounting.model.FormTypeKind}
     */
    List<FormTypeKind> getDeclarationAssigned(Long departmentId, char taxType);

    /**
     * Добавляет назначенные НФ
     */
    long save(int departmentId, int typeId, int kindId);

    /**
     * Сохраняет исполнителей для назначения
     * @param dftId идентификатор назначения
     * @param performerIds идентификаторы подразделений-исполнителей
     */
    void savePerformers(long dftId, List<Integer> performerIds);

    /**
     * Удаляет всех исполнителей для назначения
     */
    void deletePerformers(int dftId);

    /**
     * Удаляет назначение НФ
     */
    void delete(Long id);

    /**
     * Удаляет назначение НФ
     */
    void delete(List<Long> ids);

    /**
     * Проверяет существование формы назначения для позразделения с id = departmentId
     * c идентификатором вида typeId и идентификатором типа kindId
     *
     * @param departmentId позразделения
     * @param typeId вид
     * @param kind тип
     * @return true - существует форма, false в противном случае
     */
    boolean existAssignedForm(int departmentId, int typeId, FormDataKind kind);

    /**
     * Проверяет существование форм-приемников в статусе "Принята" в указанном отчетном периоде
     * @param sourceDepartmentId идентификатор подразделения формы-источника
     * @param sourceFormTypeId   вид налоговой формы-источника
     * @param sourceKind         тип налоговой формы-источника
     * @param reportPeriodId     идентификатор отчетного периода
     * @param periodStart  начало периода, в котором действуют назначения
     * @param periodEnd    окончание периода, в котором действуют назначения
     * @return приемники существуют?
     */
    List<Pair<String, String>> existAcceptedDestinations(int sourceDepartmentId, int sourceFormTypeId,
                                                         FormDataKind sourceKind, Integer departmentReportPeriodId,
                                                         Date periodStart, Date periodEnd);

    /**
     * Находим приемники, у которых этот макет является источником
     * @param typeId идентификатор макета
     * @param dateFrom дата начала периода поиска приемников
     * @param dateTo дата окончания периода
     * @return список пар подразделений с началом периода
     */
    List<Pair<DepartmentFormType, Pair<Date, Date>>> findDestinationsForFormType(int typeId, @NotNull Date dateFrom, @NotNull Date dateTo);

    /**
     * Находим источники, у которых этот макет является приемником
     * @param typeId идентификатор макета
     * @param dateFrom дата начала периода поиска источников
     * @param dateTo дата окончания периода
     * @return список пар подразделений с началом периода
     */
    List<Pair<DepartmentFormType, Pair<Date, Date>>> findSourcesForFormType(int typeId, @NotNull Date dateFrom, @NotNull Date dateTo);

    /**
     * Получает назначения подразделений типов НФ
     * @param formTypeId {@link FormType}
     * @return список
     */
    List<DepartmentFormType> getDFTByFormType(@NotNull Integer formTypeId);

    /**
     * Возвращает количество назначенных налоговых форм для выбранного налога и подразделений
     *
     * @param departmentsIds идентификаторы подразделений
     * @param taxType идентификатор вида налога
     * @return список назначенных налоговых форм для выбранного налога и подразделений
     */
    int getAssignedFormsCount(List<Long> departmentsIds, char taxType);
}
