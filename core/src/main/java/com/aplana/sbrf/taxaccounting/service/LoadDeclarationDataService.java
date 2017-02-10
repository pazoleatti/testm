package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Сервис загрузки ТФ деклараций
 *
 * @author lhaziev
 */
public interface LoadDeclarationDataService {
    /**
     * Загрузка файла
     * @param logger
     * @param userInfo
     * @param fileName
     * @param inputStream
     * @param lock
     */
    void uploadFile(Logger logger, TAUserInfo userInfo, String fileName, InputStream inputStream, String lock);

    /**
     *
     * @param logger
     * @param userInfo
     * @param declarationData
     * @param inputStream
     * @param fileName
     * @param dataFile
     * @param attachFileType
     */
    void importDeclarationData(Logger logger, TAUserInfo userInfo, DeclarationData declarationData, InputStream inputStream,
                               String fileName, File dataFile, AttachFileType attachFileType);
}
