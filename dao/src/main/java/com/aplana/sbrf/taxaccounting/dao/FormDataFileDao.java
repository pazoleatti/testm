package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.FormDataFile;
import java.util.List;

/**
 * Реализация DAO для работы с информацией о {@link com.aplana.sbrf.taxaccounting.model.FormDataFile файлах НФ}
 */
public interface FormDataFileDao {

    /**
     * Получение данных по файлам НФ
     * @param formDataId
     * @return
     */
    List<FormDataFile> getFiles(long formDataId);

    /**
     * Сохранение набора файлов НФ
     * @param files
     */
    void saveFiles(final long formDataId, List<FormDataFile> files);
}