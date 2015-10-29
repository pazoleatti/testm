package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.Relation;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;
import com.aplana.sbrf.taxaccounting.model.source.*;

import java.util.*;


/**
 * Дао для работы с версионируемыми связками источников-приемников
 *
 * @author Denis Loshkarev
 */
public interface SourceDao {
    /**
     * Возвращает указанные связки источников-приемников, периоды которых пересекаются в определенном периоде
     *
     * @param sourcePairs пары источников-приемников, версии которых будут искаться в указанном периоде
     * @param periodStart начало нового периода
     * @param periodEnd   конец нового периода
     * @param excludedPeriodStart начало нового периода, который будет исключен из проверки
     * @param excludedPeriodEnd   конец нового периода, который будет исключен из проверки
     * @param declaration признак того, что выполняется назначение источников для декларации
     * @return список пересекающихся пар "приемник-источник" + их периоды
     */
    Map<SourcePair, List<SourceObject>> getIntersections(List<SourcePair> sourcePairs, Date periodStart, Date periodEnd,
                                                         Date excludedPeriodStart, Date excludedPeriodEnd, boolean declaration);

    /**
     * Получает информацию о возможных зацикливаниях в источниках приемниках
     * Назначение источников-приемников может выполняться в двух режимах:
     * 1. Назначение нескольких приемников для одного источника
     * 2. Назначение нескольких источников для одного приемника
     * Соответственно в обоих случая получается отношение 1 к М
     *
     * @param sourcePairs пары источников-приемников
     * @param periodStart              начало периода, в течение которого будет выполняться поиск зацикливаний
     * @param periodEnd                окончание периода, в течение которого будет выполняться поиск зацикливаний
     * @return список из двух пар:
     * 1. Входная пара назначений для которых был обнаружен цикл
     * 2. Пара (идентификатор назначения-причины зацикливания и идентификатор его назначения-приемника)
     */
    Map<SourcePair, SourcePair> getLoops(List<SourcePair> sourcePairs, Date periodStart, Date periodEnd);

    /**
     * Удаляет указанные связки источников-приемников
     *
     * @param sources связки источников-приемников
     * @param declaration признак того, что выполняется удаление источников для декларации
     */
    void deleteAll(List<SourceObject> sources, boolean declaration);

    /**
     * Создает новые связки источников-приемников
     * @param sources связки источников-приемников
     * @param declaration признак того, что выполняется назначение источников для декларации
     */
    void createAll(List<SourceObject> sources, boolean declaration);

    /**
     * Удаляет указанные связки источников-приемников
     *
     * @param sources связки источников-приемников
     * @param periodStart новая дата начала действия
     * @param periodEnd новая дата окончания действия
     * @param declaration признак того, что выполняется обновление источников для декларации
     */
    void updateAll(List<SourceObject> sources, Date periodStart, Date periodEnd, boolean declaration);

    /**
     * Возвращает названия назначений по их идентификаторам
     *
     * @param sourceIds список идентификаторов назначений
     * @return ключ - Идентификатор назначения; значение - Название назначения
     */
    Map<Long, String> getSourceNames(List<Long> sourceIds);

    /**
     * Проверяет, существует ли список назначенных подразделению форм (с учётом вида и типа)
     *
     * @param departmentFormTypeIds список идентификаторов назначений
     * @return список идентификаторов назначений, которые все еще существуют
     */
    List<Long> checkDFTExistence(List<Long> departmentFormTypeIds);

    /**
     * Проверяет, существует ли список назначенных подразделению деклараций (с учётом вида и типа)
     *
     * @param departmentDeclarationTypeIds список идентификаторов назначений
     * @return список идентификаторов назначений, которые все еще существуют
     */
    List<Long> checkDDTExistence(List<Long> departmentDeclarationTypeIds);

    /**
     * Возвращает информацию о промежуточных периодах, в которых указанное назначение не действует
     * http://conf.aplana.com/pages/viewpage.action?pageId=12321547
     * @param sourcePair связка источник-приемник
     * @param newPeriodStart начало нового периода
     * @param newPeriodEnd конец нового периода
     * @return список промежуточных периодов
     */
    List<SourceObject> getEmptyPeriods(SourcePair sourcePair, Date newPeriodStart, Date newPeriodEnd);

    /**
     * Получает список нф/деклараций для которых указанная нф является источником при консолидаци и которые находятся внутри указанного диапазона дат
     * @param source идентификатор назначения-источника
     * @param periodStart начало диапазона
     * @param periodEnd окончание диапазона
     * @param declaration признак того, что экземпляры-приемники надо искать только среди деклараций
     * @return
     */
    List<ConsolidatedInstance> findConsolidatedInstances(long source, long destination, Date periodStart, Date periodEnd, boolean declaration);

    /**
     * Возвращает названия подразделений для указанных источников
     * @param sources пары id источника - название подразделения
     * @return пары id источника - название подразделения
     */
    Map<Long, String> getDepartmentNamesBySource(List<Long> sources);

    /**
     * Возвращает id подразделения по его назначению НФ
     * @param departmentFormTypeId id назначения формы подразделению
     * @return id подразделения
     */
    Integer getDepartmentIdByDepartmentFormType(long departmentFormTypeId);

    /**
     * Возвращает id подразделения по его назначению декларации
     * @param departmentDeclarationTypeId id назначения декларации подразделению
     * @return id подразделения
     */
    Integer getDepartmentIdByDepartmentDeclarationType(long departmentDeclarationTypeId);

    /**
     * Обновляет информацию о консолидации(т.е. была ли она сделана).
     * @param tgtDeclarationId идентификатор декларации
     * @param srcFormDataIds форма-источник с которой делалась консолидация для НФ
     */
    void addDeclarationConsolidationInfo(Long tgtDeclarationId, Collection<Long> srcFormDataIds);

    /**
     * Удалить записи о консолидации для текущего экземпляра
     * @param targetDeclarationDataId идентификатор декларации
     */
    void deleteDeclarationConsolidateInfo(long targetDeclarationDataId);

    /**
     * Проверяет консолидирован ли источник с идентификатором sourceFormDataId для декларации с declarationId
     * @param sourceFormDataId
     * @return
     */
    boolean isDeclarationSourceConsolidated(long declarationId, long sourceFormDataId);

    /**
     * Обновляет информацию о консолидации(т.е. была ли она сделана).
     * @param tgtFormDataId идентификатор НФ
     * @param srcFormDataIds форма-источник с которой делалась консолидация для НФ
     */
    void addFormDataConsolidationInfo(Long tgtFormDataId, Collection<Long> srcFormDataIds);

    /**
     * Удаляет данные о консолидации.
     * Данные удаляются из таблиц FORM_DATA_CONSOLIDATION
     * @param tgtFormDataIds идентификаторы НФ
     */
    void deleteFormDataConsolidationInfo(Collection<Long> tgtFormDataIds);

    boolean isFDSourceConsolidated(long formDataId, long sourceFormDataId);

    /**
     * Проставление признака неактуальности данных в НФ-приёмнике
     * @param sourceFormId идентификатор экземпляра-источника
     */
    void updateFDConsolidationInfo(long sourceFormId);

    /**
     * Проставление признака неактуальности консолидации данных для списка пар источник-приемник
     * @param instances идентификаторы пар источник-приемник
     * @param declaration признак того, что источник - декларация
     */
    void updateConsolidationInfo(Set<ConsolidatedInstance> instances, boolean declaration);

    /**
     * Проставление признака неактуальности данных в НФ/декларации-приёмнике
     * @param sourceFormId идентификатор источника
     */
    int updateDDConsolidationInfo(long sourceFormId);

    /**
     * Проверяет не изменились ли данные консолидации для НФ
     * @param fdTargetId идентификатор нф-приемника для проверки
     * @return false если есть хоть одна строка где НФ-источник равна null
     */
    boolean isFDConsolidationTopical(long fdTargetId);

    /**
     * Проверяет не изменились ли данные консолидации для декларации
     * @param ddTargetId идентификатор декларации-приемника для проверки
     * @return false если есть хоть одна строка где НФ-источник равна null
     */
    boolean isDDConsolidationTopical(long ddTargetId);

    /**
     * Возвращает список нф-источников для указанной нф (включая несозданные)
     * @param destinationFormData нф-приемник
     * @param light true - заполнятся только текстовые данные для GUI и сообщений
     * @param excludeIfNotExist true - исключить несозданные источники
     * @param stateRestriction ограничение по состоянию для созданных экземпляров
     * @return список нф-источников
     */
    List<Relation> getSourcesInfo(FormData destinationFormData, boolean light, boolean excludeIfNotExist, WorkflowState stateRestriction);

    /**
     * Возвращает список нф-приемников для указанной нф (включая несозданные)
     * @param sourceFormData нф-источник
     * @param light true - заполнятся только текстовые данные для GUI и сообщений
     * @param excludeIfNotExist true - исключить несозданные приемники
     * @param stateRestriction ограничение по состоянию для созданных экземпляров
     * @return список нф-приемников
     */
    List<Relation> getDestinationsInfo(FormData sourceFormData, boolean light, boolean excludeIfNotExist, WorkflowState stateRestriction);

    /**
     * Возвращает список нф-источников для указанной декларации (включая несозданные)
     * @param sourceFormData нф-источник
     * @param light true - заполнятся только текстовые данные для GUI и сообщений
     * @param excludeIfNotExist true - исключить несозданные приемники
     * @param stateRestriction ограничение по состоянию для созданных экземпляров
     * @return список нф-источников
     */
    List<Relation> getDeclarationDestinationsInfo(FormData sourceFormData, boolean light, boolean excludeIfNotExist, WorkflowState stateRestriction);

    /**
     * Возвращает список нф-источников для указанной декларации (включая несозданные)
     * @param declaration декларация-приемник
     * @param light true - заполнятся только текстовые данные для GUI и сообщений
     * @param excludeIfNotExist true - исключить несозданные источники
     * @param stateRestriction ограничение по состоянию для созданных экземпляров
     * @return список нф-источников
     */
    List<Relation> getDeclarationSourcesInfo(DeclarationData declaration, boolean light, boolean excludeIfNotExist, WorkflowState stateRestriction);
}
