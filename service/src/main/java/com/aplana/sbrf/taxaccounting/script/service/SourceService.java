package com.aplana.sbrf.taxaccounting.script.service;

import com.aplana.sbrf.taxaccounting.service.ScriptExposed;

import java.util.Collection;

@ScriptExposed
public interface SourceService {

    /**
     * Добавляет информацию о консолидации(т.е. была ли она сделана).
     * Соответствие либо один-к-одному, либо один-ко-многим(т.е. одно в одном списке и сногов другом)
     * @param tgtDeclarationId идентификатор НФ
     * @param srcFormDataIds форма-источник с которой делалась консолидация для НФ
     */
    void addDeclarationConsolidationInfo(Long tgtDeclarationId, Collection<Long> srcFormDataIds);

    /**
     * Удалить записи о консолидации для текущего экземпляра
     * @param targetDeclarationDataId идентификатор декларации
     */
    void deleteDeclarationConsolidateInfo(long targetDeclarationDataId);
}