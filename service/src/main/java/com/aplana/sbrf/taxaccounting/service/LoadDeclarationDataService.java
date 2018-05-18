package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.AttachFileType;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

import java.io.File;
import java.io.InputStream;
import java.util.Date;

/**
 * Сервис загрузки ТФ деклараций
 *
 * @author lhaziev
 */
public interface LoadDeclarationDataService {

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
                               String fileName, File dataFile, AttachFileType attachFileType, Date createDateFile);
}
