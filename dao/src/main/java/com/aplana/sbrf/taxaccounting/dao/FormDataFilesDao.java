package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.AsyncTaskTypeData;
import com.aplana.sbrf.taxaccounting.model.FormDataFile;

import java.util.List;


/**
 * DAO-Интерфейс для работы с типами асинхронных задач
 */
public interface FormDataFilesDao {

    /**
     * Получение данных по id НФ
     * @param formDataId
     * @return
     */
    List<FormDataFile> getFiles(long formDataId);

    /**
     *
     * @param files
     */
    void saveFiles(final long formDataId, List<FormDataFile> files);

}
