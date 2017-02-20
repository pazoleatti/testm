package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.DeclarationDataFile;

import java.util.List;

/**
 * DAO интерфейс для работы с информацией о {@link com.aplana.sbrf.taxaccounting.model.DeclarationDataFile файлах НФ(declaration)}
 */
public interface DeclarationDataFileDao {

    /**
     * Получение данных по файлам НФ(declaration)
     * @param declarationDataId
     * @return
     */
    List<DeclarationDataFile> getFiles(long declarationDataId);

    /**
     * Сохранение набора файлов НФ(declaration)
     * @param files
     */
    void saveFiles(final long declarationDataId, List<DeclarationDataFile> files);

    /**
     * Сохраняет отдельный файл
     */
    void saveFile(DeclarationDataFile file);

    /**
     * Находит файл с максимальным "весом"
     * https://conf.aplana.com/pages/viewpage.action?pageId=27184983
     */
    DeclarationDataFile findFileWithMaxWeight(Long declarationDataId);
}