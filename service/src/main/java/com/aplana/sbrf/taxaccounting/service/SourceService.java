package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Интерфейс сервиса для работы с привязкой департаментов к подразделениям
 */
@ScriptExposed
public interface SourceService {

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
     * Возвращает информацию о назначенных подразделению декларациях по заданному виду налога
     *
     * @param departmentId идентификатор подразделения
     * @param taxType      вид налога
     * @param periodStart  начало периода, в котором действуют назначения
     * @param periodEnd    окончание периода, в котором действуют назначения
     * @param queryParams  параметры пейджинга и фильтра
     * @return список назначенных подразделению деклараций (с учётом вида и типа) по заданному виду налога
     */
    List<DepartmentDeclarationType> getDDTByDepartment(int departmentId, TaxType taxType, Date periodStart, Date periodEnd,
                                                       QueryParams queryParams);

    /**
     * Возвращает информацию о декларациях-потребителях, которые должны использовать
     * информацию из данной налоговой формы в качестве источника
     *
     * @param sourceDepartmentId идентификатор подразделения формы-источника
     * @param sourceFormTypeId   вид налоговой формы-источника
     * @param sourceKind         тип налоговой формы-источника
     * @param periodStart        начало периода, в котором действуют назначения
     * @param periodEnd          окончание периода, в котором действуют назначения
     * @return информация о декларациях-потребителях в виде списка
     * {@link com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType}
     */
    List<DepartmentDeclarationType> getDeclarationDestinations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind, Date periodStart, Date periodEnd);

    /**
     * Возвращает список назначенных деклараций для выбранного налога и подразделений
     *
     * @param departmentIds идентификаторы подразделений
     * @param taxType       идентификатор вида налога
     * @param queryParams   параметры пейджинга и фильтра
     * @return список назначенных деклараций для выбранного налога и подразделений
     */
    List<DeclarationTypeAssignment> getAllDeclarationAssigned(List<Long> departmentIds, char taxType, QueryParams<TaxNominationColumnEnum> queryParams);

    /**
     * Добавляет декларации, назначенные подразделению
     *
     * @param departmentId  id подразделения
     * @param declarationId id вида декларации
     * @param performerIds  id исполнителей
     */
    void saveDDT(Long departmentId, int declarationId, List<Integer> performerIds);

    /**
     * Удаляет декларации, назначенные подразделению
     *
     * @param ids id на удаление
     */
    void deleteDDT(Collection<Long> ids);

    /**
     * Получить описание вида декларации по идентификатору
     *
     * @param declarationTypeId идентификатор вида декларации
     * @return описание вида декларации, с заданным идентификатором
     */
    DeclarationType getDeclarationType(int declarationTypeId);

    /**
     * Обновление исполнителя для назначенной формы
     */
    void updateDDTPerformers(int id, List<Integer> performerIds);

    /**
     * Находит назначенные виды деклараций, которые являются потребителями налоговой формы{@code typeId}
     *
     * @param typeId   вид налоговой формы потребителя
     * @param dateFrom дата начала действия
     * @param dateTo   дата окончания действия
     * @return список назначений
     */
    List<Pair<DepartmentDeclarationType, Pair<Date, Date>>> findDestinationDTsForFormType(int typeId, Date dateFrom, Date dateTo);

    /**
     * ПОлучение назначений деклараций
     *
     * @param declarationTypeId идентификатор {@link DeclarationType}
     * @return список
     */
    List<DepartmentDeclarationType> getDDTByDeclarationType(@NotNull Integer declarationTypeId);

    List<DeclarationType> allDeclarationTypeByTaxType(TaxType taxType);

    /**
     * Возвращает количество назначенных деклараций для выбранного налога и подразделений
     *
     * @param departmentsIds идентификаторы подразделений
     * @param taxType        идентификатор вида налога
     * @return список назначенных деклараций для выбранного налога и подразделений
     */
    int getAssignedDeclarationsCount(List<Long> departmentsIds, char taxType);

    /**
     * Добавляет информацию о консолидации(т.е. была ли она сделана).
     * Соответствие либо один-к-одному, либо один-ко-многим(т.е. одно в одном списке и сногов другом)
     *
     * @param tgtDeclarationId идентификатор НФ
     * @param srcFormDataIds   форма-источник с которой делалась консолидация для НФ
     */
    void addDeclarationConsolidationInfo(Long tgtDeclarationId, Collection<Long> srcFormDataIds);

    /**
     * Удалить записи о консолидации для текущего экземпляра
     *
     * @param targetDeclarationDataId идентификатор декларации
     */
    void deleteDeclarationConsolidateInfo(long targetDeclarationDataId);

    /**
     * Проверяет консолидирован ли источник с идентификатором sourceFormDataId для декларации с declarationId
     *
     * @param sourceFormDataId НФ источник
     * @return true если есть запись о консолидации
     */
    boolean isDeclarationSourceConsolidated(long declarationId, long sourceFormDataId);

    /**
     * Проставление признака неактуальности данных в НФ-приёмнике
     * http://conf.aplana.com/pages/viewpage.action?pageId=19662408
     *
     * @param sourceFormId идентификатор источника
     */
    void updateDDConsolidation(long sourceFormId);

    /**
     * Проверяет не изменились ли данные консолидации для декларации
     *
     * @param ddTargetId идентификатор декларации-приемника для проверки
     * @return true - данные актуальны
     */
    boolean isDDConsolidationTopical(long ddTargetId);

    /**
     * Возвращает список нф-источников для указанной декларации (включая несозданные)
     *
     * @param declarationDataId идентификатор налоговой формы приемника
     * @return список нф-источников
     */
    List<Relation> getDeclarationSourcesInfo(Long declarationDataId);

    /**
     * Возвращает список нф-источников для указанной декларации (включая несозданные)
     *
     * @param declarationDataId идентификатор налоговой формы источника
     * @return список нф-источников
     */
    List<Relation> getDeclarationDestinationsInfo(Long declarationDataId);
}
