package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

import java.io.IOException;
import java.io.InputStream;

/**
 * Сервис загрузки транспортных файлов
 */
public interface TransportDataService {
    void uploadFile(TAUserInfo userInfo, ConfigurationParam folderParam, String fileName, InputStream inputStream, Logger logger) throws IOException;
}
