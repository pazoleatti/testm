package com.aplana.sbrf.taxaccounting.script.service;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.Relation;
import com.aplana.sbrf.taxaccounting.service.ScriptExposed;

import java.util.Collection;
import java.util.List;

@ScriptExposed
public interface SourceService {

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
     * Возвращает список нф-приемников для указанной декларации (включая несозданные)
     *
     * @param sourceDeclaration декларация-источник
     * @return список нф-источников
     */
    List<Relation> getDestinationsInfo(DeclarationData sourceDeclaration);

    /**
     * Возвращает список НФ-источников для указанной НФ.
     *
     * @param destinationDeclaration НФ-приёмник
     * @return список объектов связей "источник-приёмник", где переданная НФ является приёмником.
     */
    List<Relation> getSourcesInfo(DeclarationData destinationDeclaration);
}
