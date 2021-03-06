package com.aplana.sbrf.taxaccounting.dao.api;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Dao для работы с {@link DepartmentDeclarationType информацией о назначении деклараций подразделениям}  
 * @author dsultanbekov
 */
public interface DepartmentDeclarationTypeDao {

	/**
	 * Возвращает идентификаторы всех подразделений, в которых есть декларации по данному виду налога.
	 * @param taxType тип налога
	 * @return набор идентификаторов подразделений
	 */
	Set<Integer> getDepartmentIdsByTaxType(TaxType taxType);

    /**
     * Возвращает информацию о назначенных подразделению декларациях по заданному виду налога
     *
     * @param departmentId идентификатор подразделения
     * @param taxType      вид налога
     * @param periodStart  начало периода, в котором действуют назначения
     * @param periodEnd    окончание периода, в котором действуют назначения
     * @return список назначенных подразделению деклараций (с учётом вида и типа) по заданному виду налога
     */
    List<DepartmentDeclarationType> getByTaxType(int departmentId, TaxType taxType, Date periodStart, Date periodEnd);

    /**
     * Возвращает информацию о назначенных подразделению декларациях по заданному виду налога
     *
     * @param departmentId идентификатор подразделения
     * @param taxType      вид налога
     * @param periodStart  начало периода, в котором действуют назначения
     * @param periodEnd    окончание периода, в котором действуют назначения
     * @param queryParams  параметры пейджинга и фильтрации
     * @return список назначенных подразделению деклараций (с учётом вида и типа) по заданному виду налога
     */
    List<DepartmentDeclarationType> getByTaxType(int departmentId, TaxType taxType, Date periodStart, Date periodEnd,
                                                 QueryParams queryParams);

	/**
	 * Возвращает информацию о назначенных подразделению декларациях по заданному виду налога
	 * @param departmentId идентификатор подразделения
	 * @param taxType вид налога
	 * @return список назначенных подразделению деклараций (с учётом вида и типа) по заданному виду налога
	 */
    @Deprecated
	List<DepartmentDeclarationType> getByTaxType(int departmentId, TaxType taxType);
	
    /**
     * Добавляет назначения декларации
     */
    long save(int departmentId, int declarationTypeId);
    
    /**
     * Удаляет назначение декларации
     */
    void delete(Long id);

    List<Pair<DepartmentDeclarationType, Pair<Date, Date>>> findDestinationDTsForFormType(int typeId, @NotNull Date dateFrom, @NotNull Date dateTo);
    List<Pair<DepartmentDeclarationType, Pair<Date, Date>>> findSourceDTsForDeclaration(int typeId, @NotNull Date dateFrom, @NotNull Date dateTo);

    List<DepartmentDeclarationType> getDDTByDeclarationType(@NotNull Integer declarationTypeId);

    /**
     * Возвращает список назначенных деклараций для выбранного налога и подразделений
     *
     * @param departmentIds идентификаторы подразделений
     * @param taxType       идентификатор вида налога
     * @param queryParams   параметры пейджинга и фильтрации
     * @return список назначенных налоговых форм для выбранного налога и подразделений
     */
    List<DeclarationTypeAssignment> getAllDeclarationAssigned(List<Long> departmentIds, char taxType, QueryParams<TaxNominationColumnEnum> queryParams);

    /**
     * Получение списка видов налоговых форм, назначенных подразделениям
     *
     * @param departmentIds Идентификаторы подразделений
     * @param pagingParams  Параметры пагинации
     * @return Страница списка назначений {@link DeclarationTypeAssignment} видов форм выбранным подразделениям
     */
    List<DeclarationTypeAssignment> fetchDeclarationTypesAssignments(List<Long> departmentIds, PagingParams pagingParams);

    /**
     * Возвращает количество назначенных деклараций для выбранного налога и подразделений
     *
     * @param departmentsIds идентификаторы подразделений
     * @param taxType       идентификатор вида налога
     * @return список назначенных деклараций для выбранного налога и подразделений
     */
    int getAssignedDeclarationsCount(List<Long> departmentsIds, char taxType);

    /**
     * Возвращает количество назначенных деклараций для выбранного налога и подразделений
     *
     * @param departmentsIds Идентификаторы подразделений
     * @return Количество назначений налоговых форм подразделениям
     */
    int fetchDeclarationTypesAssignmentsCount(List<Long> departmentsIds);

    void savePerformers(final long ddtId, final List<Integer> performerIds);

    void deletePerformers(int ddtId);

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
}
