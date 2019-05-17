package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataFileDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.utils.ZipUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Service
public class LoadDeclarationDataServiceImpl extends AbstractLoadTransportDataService implements LoadDeclarationDataService {

    private static final Log LOG = LogFactory.getLog(LoadDeclarationDataServiceImpl.class);

    @Autowired
    private DepartmentReportPeriodDao departmentReportPeriodDao;
    @Autowired
    private DeclarationDataFileDao declarationDataFileDao;

    @Autowired
    private BlobDataService blobDataService;
    @Autowired
    private DeclarationDataService declarationDataService;
    @Autowired
    private DeclarationDataScriptingService declarationDataScriptingService;
    @Autowired
    private DeclarationTemplateService declarationTemplateService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private LockDataService lockDataService;
    @Autowired
    private LogBusinessService logBusinessService;
    @Autowired
    private ReportService reportService;
    @Autowired
    private TAUserService userService;

    @Autowired
    private RefBookFactory refBookFactory;


    @Override
    @Transactional
    @PreAuthorize("hasPermission(#declaration.id, 'com.aplana.sbrf.taxaccounting.model.DeclarationData', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission).IMPORT_EXCEL)")
    public void importXmlTransportFile(File xmlTransportFile, String xmlFileName, DeclarationData declaration, TAUserInfo userInfo, Logger logger) {

        TAUser user = userInfo.getUser();
        long declarationId = declaration.getId();

        LOG.info(String.format("LoadDeclarationDataServiceImpl: Import transport file. user: %s; declaration: %s; fileName: %s",
                user, declaration, xmlFileName));

        setLock(declaration, user);
        try {
            LOG.info(String.format("Сохранение XML-файла в базе данных для налоговой формы %s", declarationId));
            String fileUuid = archiveAndSaveFile(xmlTransportFile, xmlFileName);

            // Добавляем ТФ к отчётам декларации, таблица declaration_report
            reportService.attachReportToDeclaration(declarationId, fileUuid, DeclarationReportType.XML_DEC);
            // Добавляем ТФ к файлам декларации, таблица declaration_data_file
            attachFileToDeclaration(fileUuid, declaration, user);

            // Парсим xml и импортируем данные в базу с помощью primary_rnu_ndfl.groovy
            Map<String, Object> additionalParameters = new HashMap<>();
            additionalParameters.put("dataFile", xmlTransportFile);
            if (!declarationDataScriptingService.executeScript(userInfo, declaration, FormDataEvent.IMPORT_TRANSPORT_FILE, logger, additionalParameters)) {
                throw new ServiceException("Импорт данных не предусмотрен");
            }

            String note = "Форма создана из файла: " + xmlFileName;
            logBusinessService.create(new LogBusiness().declarationDataId(declaration.getId()).event(FormDataEvent.CREATE_FROM_XML)
                    .logId(logger.getLogId()).logDate(declaration.getCreatedDate()).user(userInfo.getUser()));
            auditService.add(FormDataEvent.IMPORT_TRANSPORT_FILE, userInfo, declaration, note, null);
        } finally {
            unlock(declaration, user);
        }
    }

    // добавление файла к декларации (раздел "Файлы и комментарии" декларации, таблица declaration_data_file)
    private void attachFileToDeclaration(String fileUuid, DeclarationData declaration, TAUser user) {
        RefBookDataProvider provider = refBookFactory.getDataProvider(RefBook.Id.ATTACH_FILE_TYPE.getId());
        Long fileTypeId = provider.getUniqueRecordIds(new Date(), "code = " + AttachFileType.TRANSPORT_FILE.getCode() + "").get(0);

        DeclarationDataFile declarationDataFile = new DeclarationDataFile();
        declarationDataFile.setDeclarationDataId(declaration.getId());
        declarationDataFile.setUuid(fileUuid);
        declarationDataFile.setUserName(user.getName());
        declarationDataFile.setUserDepartmentName(departmentService.getParentsHierarchyShortNames(user.getDepartmentId()));
        declarationDataFile.setFileTypeId(fileTypeId);
        declarationDataFileDao.create(declarationDataFile);
    }

    /**
     * Архивация и сохранение файла
     *
     * @return uuid файла в blob_data
     */
    private String archiveAndSaveFile(File file, String fileName) {
        File zipFile = null;
        String zipFileUuid;
        try {
            zipFile = ZipUtils.archive(file, fileName);

            Date creationDateToday = new Date();
            zipFileUuid = blobDataService.create(zipFile, trimFileExtension(fileName) + ".zip", creationDateToday);
        } catch (IOException e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        } finally {
            deleteTempFile(zipFile);
        }
        return zipFileUuid;
    }

    private String trimFileExtension(String filename) {
        int dotPos = filename.lastIndexOf('.');
        if (dotPos < 0) {
            return filename;
        }
        return filename.substring(0, dotPos);
    }

    private void deleteTempFile(File tempFile) {
        if (tempFile != null && !tempFile.delete()) {
            LOG.warn(String.format("Временный файл %s не удален", tempFile.getAbsolutePath()));
        }
    }

    private void setLock(DeclarationData declaration, TAUser user) {
        String asyncTaskKey = declarationDataService.generateAsyncTaskKey(declaration.getId(), AsyncTaskType.IMPORT_TF_DEC);
        String declarationFullName = declarationDataService.getDeclarationFullName(declaration.getId(), AsyncTaskType.IMPORT_TF_DEC);

        LockData lockData = lockDataService.lock(asyncTaskKey, user.getId(), declarationFullName);
        if (lockData != null) {
            throw new ServiceException(createDataLockedErrorMessage(declaration, lockData));
        }
    }

    // генерация сообщения при заблокированных данных
    private String createDataLockedErrorMessage(DeclarationData declaration, LockData lockData) {

        String lockedByUser = userService.getUser(lockData.getUserId()).getName();
        String lockedOnDate = FastDateFormat.getInstance("HH:mm dd.MM.yyyy").format(lockData.getDateLock());

        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.fetchOne(declaration.getDepartmentReportPeriodId());
        ReportPeriod reportPeriod = departmentReportPeriod.getReportPeriod();
        String reportPeriodName = reportPeriod.getTaxPeriod().getYear() + " - " + reportPeriod.getName();

        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declaration.getDeclarationTemplateId());
        String declarationTypeName = declarationTemplate.getType().getName();

        String departmentName = departmentService.getDepartment(declaration.getDepartmentId()).getName();

        return String.format(
                "Обработка данных транспортного файла не выполнена, " +
                        "т.к. в данный момент выполняется изменение данных налоговой формы \"%s\" " +
                        "для подразделения \"%s\" " +
                        "в периоде \"%s\", " +
                        "инициированное пользователем \"%s\" " +
                        "в %s",
                declarationTypeName,
                departmentName,
                reportPeriodName,
                lockedByUser,
                lockedOnDate
        );
    }

    private void unlock(DeclarationData declaration, TAUser user) {
        String asyncTaskKey = declarationDataService.generateAsyncTaskKey(declaration.getId(), AsyncTaskType.IMPORT_TF_DEC);
        lockDataService.unlock(asyncTaskKey, user.getId());
    }
}
