package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.AttachFileType;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import org.joda.time.LocalDateTime;

import java.io.File;
import java.io.InputStream;

/**
 * Сервис загрузки ТФ деклараций
 *
 * @author lhaziev
 */
public interface LoadDeclarationDataService {
    /**
     * Загрузка файла
     *
     * @param logger
     * @param userInfo
     * @param fileName
     * @param inputStream
     * @param lock
     */
    String uploadFile(Logger logger, TAUserInfo userInfo, String fileName, InputStream inputStream, String lock);

    /**
     * @param logger
     * @param userInfo
     * @param declarationData
     * @param inputStream
     * @param fileName
     * @param dataFile
     * @param attachFileType
     * @param createDateFile
     */
    void importDeclarationData(Logger logger, TAUserInfo userInfo, DeclarationData declarationData, InputStream inputStream,
                               String fileName, File dataFile, AttachFileType attachFileType, LocalDateTime createDateFile);
}
