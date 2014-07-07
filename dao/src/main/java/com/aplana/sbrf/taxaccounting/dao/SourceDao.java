package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.source.DeclarationDataInfo;
import com.aplana.sbrf.taxaccounting.model.source.FormDataInfo;
import com.aplana.sbrf.taxaccounting.model.source.SourceObject;
import com.aplana.sbrf.taxaccounting.model.source.SourcePair;
import com.aplana.sbrf.taxaccounting.model.util.Pair;

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
    List<Long> checkDepartmentFormTypesExistence(List<Long> departmentFormTypeIds);

    /**
     * Получает список экземпляров налоговых форм, созданных в указанном периоде с определенными назначениями
     * http://conf.aplana.com/pages/viewpage.action?pageId=12321547
     *
     * @param periodStart начало периода
     * @param periodEnd окончание периода
     * @param departmentFormTypes список назначений нф подразделениям
     * @return информация об экземплярах нф
     */
    List<FormDataInfo> findForms(Date periodStart, Date periodEnd, List<Long> departmentFormTypes);

    /**
     * Получает список экземпляров деклараций, созданных в указанном периоде с определенными назначениями
     * http://conf.aplana.com/pages/viewpage.action?pageId=12321547
     *
     * @param periodStart начало периода
     * @param periodEnd окончание периода
     * @param destinationIds список назначений деклараций подразделениям
     * @return информация об экземплярах нф
     */
    List<DeclarationDataInfo> findDeclarations(Date periodStart, Date periodEnd, List<Long> destinationIds);
}
