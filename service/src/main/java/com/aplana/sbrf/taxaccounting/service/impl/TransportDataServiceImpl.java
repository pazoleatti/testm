package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.TransportDataService;
import com.aplana.sbrf.taxaccounting.utils.FileWrapper;
import com.aplana.sbrf.taxaccounting.utils.ResourceUtils;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

@Service
public class TransportDataServiceImpl implements TransportDataService {

    @Autowired
    private ConfigurationDao configurationDao;

    @Autowired
    private AuditService auditService;

    final static String USER_NOT_FOUND_ERROR = "Не определен пользователь!";
    final static String ACCESS_DENIED_ERROR = "У пользователя нет прав для загрузки транспортных файлов!";
    final static String NO_FILE_NAME_ERROR = "Невозможно определить имя файла!";
    final static String EMPTY_INPUT_STREAM_ERROR = "Поток данных пуст!";
    final static String NO_CATALOG_ERROR = "Не указан каталог загрузки в конфигурационных параметрах АС «Учет налогов»!";

    @Override
    public void uploadFile(TAUserInfo userInfo, ConfigurationParam folderParam, String fileName, InputStream inputStream, Logger logger) throws IOException {
        // Проверка прав
        if (userInfo == null) {
            logger.error(USER_NOT_FOUND_ERROR);
            return;
        }

        if (!userInfo.getUser().hasRole(TARole.ROLE_OPER)
                && !userInfo.getUser().hasRole(TARole.ROLE_CONTROL)
                && !userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS)
                && !userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
            logger.error(ACCESS_DENIED_ERROR);
            return;
        }

        if (fileName == null) {
            logger.error(NO_FILE_NAME_ERROR);
            return;
        }

        if (inputStream == null) {
            logger.error(EMPTY_INPUT_STREAM_ERROR);
            return;
        }

        // Конфигурационные параметры
        ConfigurationParamModel model = configurationDao.loadParams();
        List<String> uploadPathList = model.get(folderParam);

        if (uploadPathList == null || uploadPathList.isEmpty()) {
            logger.error(NO_CATALOG_ERROR);
            return;
        }

        List<String> fileNames = new LinkedList<String>();

        if (fileName.toLowerCase().endsWith(".zip")) {
            // Архив — извлекаем все содержимое
            ZipArchiveInputStream zais = new ZipArchiveInputStream(inputStream, "cp866");
            ArchiveEntry entry;
            while ((entry = zais.getNextEntry()) != null) {
                fileNames.add(entry.getName());
                copyFileFromStream(zais, uploadPathList.get(0), entry.getName(), logger);
            }
            IOUtils.closeQuietly(zais);
        } else {
            // Не архив
            copyFileFromStream(inputStream, uploadPathList.get(0), fileName, logger);
            fileNames.add(fileName);
        }
        IOUtils.closeQuietly(inputStream);

        // ЖА
        String msg = StringUtils.collectionToDelimitedString(fileNames, "; ");
        auditService.add(FormDataEvent.IMPORT, userInfo, userInfo.getUser().getDepartmentId(), null, null, null, null, msg);
    }

    private void copyFileFromStream(InputStream inputStream, String folderPath, String fileName, Logger logger) throws IOException {
        FileWrapper file = ResourceUtils.getSharedResource(folderPath + fileName, false);
        OutputStream outputStream = file.getOutputStream();
        IOUtils.copy(inputStream, outputStream);
        IOUtils.closeQuietly(outputStream);
        logger.info("Файл «" + fileName + "» помещен в каталог загрузки «" + folderPath + "».");
    }
}
