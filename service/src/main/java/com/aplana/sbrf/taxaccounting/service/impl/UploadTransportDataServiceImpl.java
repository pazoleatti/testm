package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.utils.FileWrapper;
import com.aplana.sbrf.taxaccounting.utils.ResourceUtils;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Dmitriy Levykin
 */
@Service
@Transactional
public class UploadTransportDataServiceImpl implements UploadTransportDataService {

    @Autowired
    private ConfigurationDao configurationDao;
    @Autowired
    private AuditService auditService;
    @Autowired
    private PeriodService periodService;
    @Autowired
    private RefBookExternalService refBookExternalService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private DepartmentFormTypeDao departmentFormTypeDao;
    @Autowired
    private FormTypeService formTypeService;

    // Сообщения при загрузке в каталоги
    final static String U1 = "Файл «%s» помещен в каталог загрузки «%s».";
    final static String U2 = "В каталоге загрузки ранее загруженный файл «%s» был заменен!";
    final static String U3 = "Загрузка транспортных файлов в каталог загрузки завершена.";
    final static String U4 = "Файл «%s» не загружен: %s!";
    final static String U5 = "Файл «%s» не загружен, т.к. имеет некорректный формат имени!";
    final static String U5_1 = " Код подразделения «%s» не существует в АС «Учет налогов»!";
    final static String U5_2 = " Код налоговой формы «%s» не существует в АС «Учет налогов»!";
    final static String U5_3 = " Код отчетного периода «%s» не существует в налоговом периоде %d года в АС «Учет налогов»!";
    final static String U6 = "Файл «%s» не загружен, т.к. текущий пользователь не имеет доступа к содержащейся в нем налоговой форме «%s» подразделения «%s»!";
    final static String U7 = "Не указан каталог загрузки в конфигурационных параметрах АС «Учет налогов»!";

    // Сообщения, которые не учтены в постановка
    final static String USER_NOT_FOUND_ERROR = "Не определен пользователь!";
    final static String ACCESS_DENIED_ERROR = "У пользователя нет прав для загрузки транспортных файлов!";
    final static String NO_FILE_NAME_ERROR = "Невозможно определить имя файла!";
    final static String EMPTY_INPUT_STREAM_ERROR = "Поток данных пуст!";

    // Константы
    final static String ZIP_ENCODING = "cp866";

    @Override
    public boolean uploadFile(TAUserInfo userInfo, int departmentId, String fileName, InputStream inputStream, Logger logger) {
        // Проверка прав
        if (userInfo == null) {
            logger.error(USER_NOT_FOUND_ERROR);
            logger.error(U3);
            return false;
        }

        if (!userInfo.getUser().hasRole(TARole.ROLE_OPER)
                && !userInfo.getUser().hasRole(TARole.ROLE_CONTROL)
                && !userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS)
                && !userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
            logger.error(ACCESS_DENIED_ERROR);
            logger.error(U3);
            return false;
        }

        if (fileName == null) {
            logger.error(NO_FILE_NAME_ERROR);
            logger.error(U3);
            return false;
        }

        if (inputStream == null) {
            logger.error(EMPTY_INPUT_STREAM_ERROR);
            logger.error(U3);
            return false;
        }

        // Список загруженных ТФ для ЖА
        List<String> fileNames = new LinkedList<String>();

        try {
            if (fileName.toLowerCase().endsWith(".zip")) {
                // Архив — извлекаем все содержимое
                ZipArchiveInputStream zais = new ZipArchiveInputStream(inputStream, ZIP_ENCODING);
                ArchiveEntry entry;
                try {
                    while ((entry = zais.getNextEntry()) != null) {
                        ConfigurationParam configurationParam = checkFormDataAccess(entry.getName(), logger);
                        if (configurationParam != null) {
                            int departmentConfId = departmentId;
                            if (configurationParam.isCommon()) {
                                departmentConfId = 0;
                            }
                            try {
                                if (configurationParam != null
                                        && copyFileFromStream(zais, getUploadPath(configurationParam, departmentConfId, logger),
                                        entry.getName(), logger)) {
                                    fileNames.add(entry.getName());
                                }
                            } catch (IOException e) {
                                // Ошибка копирования сущности из архива
                                logger.error(U4, e.getMessage());
                            }
                        }
                    }
                } catch (IOException e) {
                    // Ошибка копирования из архива
                    logger.error(U4, e.getMessage());
                } finally {
                    IOUtils.closeQuietly(zais);
                }
            } else {
                // Не архив
                ConfigurationParam configurationParam = checkFormDataAccess(fileName, logger);
                if (configurationParam != null) {
                    int departmentConfId = departmentId;
                    if (configurationParam.isCommon()) {
                        departmentConfId = 0;
                    }
                    try {
                        if (configurationParam != null
                                && copyFileFromStream(inputStream, getUploadPath(configurationParam, departmentConfId, logger),
                                fileName, logger)) {
                            fileNames.add(fileName);
                        }
                    } catch (IOException e) {
                        // Ошибка копирования файла
                        logger.error(U4, e.getMessage());
                    }
                }
            }
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

        // Если ничего не загрузили
        if (fileNames.isEmpty()) {
            logger.error(U3);
            return false;
        }

        logger.info(U3);

        // ЖА
        String msg = StringUtils.collectionToDelimitedString(fileNames, "; ");
        auditService.add(FormDataEvent.UPLOAD_TRANSPORT_FILE, userInfo, userInfo.getUser().getDepartmentId(), null,
                null, null, null, msg);

        return true;
    }

    /**
     * Копирование файла из потока в каталог загрузки
     */
    private boolean copyFileFromStream(InputStream inputStream, String folderPath, String fileName, Logger logger)
            throws IOException {
        if (folderPath != null) {
            FileWrapper file = ResourceUtils.getSharedResource(folderPath + fileName, false);
            boolean exist = file.exists();
            OutputStream outputStream = file.getOutputStream();
            IOUtils.copy(inputStream, outputStream);
            IOUtils.closeQuietly(outputStream);
            logger.info(U1, fileName, folderPath);
            if (exist) {
                logger.info(U2, fileName);
            }
            return true;
        }
        return false;
    }

    /**
     * Проверка имени файла и проверка доступа к соответствующим НФ
     * http://conf.aplana.com/pages/viewpage.action?pageId=13111363
     */
    private ConfigurationParam checkFormDataAccess(String fileName, Logger logger) {
        boolean isDiasoftRefBook = refBookExternalService.isDiasoftFile(fileName);
        boolean isFormData = TransportDataParam.isValidName(fileName);

        if (isDiasoftRefBook) {
            // Справочники не проверяем
            return ConfigurationParam.DIASOFT_UPLOAD_DIRECTORY;
        }

        // Не справочники Diasoft и не ТФ НФ
        if (!isDiasoftRefBook && !isFormData) {
            logger.warn(U5, fileName);
            return null;
        }

        //// НФ

        // Параметры из имени файла
        TransportDataParam transportDataParam = TransportDataParam.valueOf(fileName);
        String formCode = transportDataParam.getFormCode();
        String reportPeriodCode = transportDataParam.getReportPeriodCode();
        Integer year = transportDataParam.getYear();
        Integer departmentCode = transportDataParam.getDepartmentCode();

        // Не задан код подразделения или код формы
        if (departmentCode == null || formCode == null || reportPeriodCode == null || year == null) {
            logger.warn(U5, fileName);
            return null;
        }

        // Указан несуществующий код налоговой формы
        FormType formType = formTypeService.getByCode(formCode);
        if (formType == null) {
            logger.warn(U5 + U5_2, fileName, formCode);
            return null;
        }

        // Указан несуществующий код подразделения
        Department formDepartment = departmentService.getDepartmentByCode(departmentCode);
        if (formDepartment == null) {
            logger.warn(U5 + U5_1, fileName, transportDataParam.getDepartmentCode());
            return null;
        }

        // Указан недопустимый код периода
        ReportPeriod reportPeriod = periodService.getByTaxTypedCodeYear(formType.getTaxType(), reportPeriodCode, year);
        if (reportPeriod == null) {
            logger.warn(U5 + U5_3, fileName, reportPeriodCode, year);
            return null;
        }

        // Назначение подразделению типа и вида НФ
        if (!departmentFormTypeDao.existAssignedForm(formDepartment.getId(), formType.getId(), FormDataKind.PRIMARY)) {
            logger.warn(U6, fileName, formType.getName(), formDepartment.getName());
            return null;
        }

        return ConfigurationParam.FORM_UPLOAD_DIRECTORY;
    }

    /**
     * Получение пути из конф. параметров
     */
    private String getUploadPath(ConfigurationParam configurationParam, int departmentId, Logger logger) {
        ConfigurationParamModel model = configurationDao.getByDepartment(departmentId);
        List<String> uploadPathList = model.get(configurationParam, departmentId);
        if (uploadPathList == null || uploadPathList.isEmpty()) {
            logger.error(U7);
            return null;
        }
        return uploadPathList.get(0);
    }
}
