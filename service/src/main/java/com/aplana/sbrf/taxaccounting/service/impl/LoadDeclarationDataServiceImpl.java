package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author lhaziev
 */
@Service
@Transactional
public class LoadDeclarationDataServiceImpl extends AbstractLoadTransportDataService implements LoadDeclarationDataService {

    private static final Log LOG = LogFactory.getLog(LoadDeclarationDataServiceImpl.class);
    private static final String LOCK_MSG = "Обработка данных транспортного файла не выполнена, " +
            "т.к. в данный момент выполняется изменение данных налоговой формы \"%s\" " +
            "для подразделения \"%s\" " +
            "в периоде \"%s\", " +
            "инициированное пользователем \"%s\" " +
            "в %s";
    private static final ThreadLocal<SimpleDateFormat> SDF_HH_MM_DD_MM_YYYY = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("HH:mm dd.MM.yyyy");
        }
    };

    @Autowired
    private DeclarationDataService declarationDataService;
    @Autowired
    private DeclarationTemplateService declarationTemplateService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private LockDataService lockDataService;
    @Autowired
    private DepartmentReportPeriodDao departmentReportPeriodDao;
    @Autowired
    private TAUserService userService;

    @Override
    public void importDeclarationData(Logger logger, TAUserInfo userInfo, DeclarationData declarationData, InputStream inputStream,
                                      String fileName, File dataFile, AttachFileType attachFileType, Date createDateFile) {
        LOG.info(String.format("LoadDeclarationDataServiceImpl.importDeclarationData. userInfo: %s; declarationData: %s; fileName: %s; attachFileType: %s",
                userInfo, declarationData, fileName, attachFileType));
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.fetchOne(declarationData.getDepartmentReportPeriodId());
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());
        DeclarationType declarationType = declarationTemplate.getType();
        String reportPeriodName = departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear() + " - "
                + departmentReportPeriod.getReportPeriod().getName();

        // Блокировка
        LockData lockData = lockDataService.lock(declarationDataService.generateAsyncTaskKey(declarationData.getId(), DeclarationDataReportType.IMPORT_TF_DEC),
                userInfo.getUser().getId(),
                declarationDataService.getDeclarationFullName(declarationData.getId(), DeclarationDataReportType.IMPORT_TF_DEC));
        if (lockData != null)
            throw new ServiceException(String.format(
                    LOCK_MSG,
                    declarationType.getName(),
                    departmentService.getDepartment(declarationData.getDepartmentId()).getName(),
                    reportPeriodName,
                    userService.getUser(lockData.getUserId()).getName(),
                    SDF_HH_MM_DD_MM_YYYY.get().format(lockData.getDateLock())
            ));

        try {
            // Скрипт загрузки ТФ + прикладываем файл к НФ
            try {
                declarationDataService.importDeclarationData(logger, userInfo, declarationData.getId(), inputStream,
                        fileName, FormDataEvent.IMPORT_TRANSPORT_FILE, null, dataFile, attachFileType, createDateFile);
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
        } finally {
            // Снимаем блокировку
            lockDataService.unlock(declarationDataService.generateAsyncTaskKey(declarationData.getId(), DeclarationDataReportType.IMPORT_TF_DEC), userInfo.getUser().getId());
        }
    }
}
