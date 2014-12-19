package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.source.*;

import java.util.Date;
import java.util.List;
import java.util.Map;


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
     * Получает список отчетных периодов, в которых существуют принятые экземпляры нф и которые находятся внутри указанного диапазона дат
     * @param source идентификатор назначения-приемника
     * @param periodStart начало диапазона
     * @param periodEnd окончание диапазона
     * @return информация о периодах в которых существуют принятые источники
     */
    List<String> findAcceptedInstances(Long source, Date periodStart, Date periodEnd);

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
}
