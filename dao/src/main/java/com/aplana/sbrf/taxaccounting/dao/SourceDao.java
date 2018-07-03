package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.Relation;
import com.aplana.sbrf.taxaccounting.model.source.ConsolidatedInstance;
import com.aplana.sbrf.taxaccounting.model.source.SourceObject;
import com.aplana.sbrf.taxaccounting.model.source.SourcePair;

import java.util.*;


/**
 * Дао для работы с версионируемыми связками источников-приемников
 *
 * @author Denis Loshkarev
 */
public interface SourceDao {

    /**
     * Удаляет указанные связки источников-приемников
     *
     * @param sources связки источников-приемников
     */
    void deleteAll(List<SourceObject> sources);

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
     * Получить данные об источниках
     * @param targetId  идентификатор целевой НФ
     * @return список объектов хранения данных источников/приемников
     */
    List<Relation> getSourcesInfo(long targetId);

    /**
     * Получить данные о приемниках
     * @param sourceId  идентификатор  НФ источника
     * @return список объектов хранения данных источников/приемников
     */
    List<Relation> getDestinationsInfo(long sourceId);
}
