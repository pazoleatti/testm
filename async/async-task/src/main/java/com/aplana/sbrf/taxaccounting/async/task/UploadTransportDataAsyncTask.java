package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.*;

import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.LOCKED_OBJECT;
import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.LOCK_DATE;
import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.USER_ID;

/**
 * Реализация таска "Загрузка ТФ с локального компьютера"
 * @author Lhaziev
 */
public abstract class UploadTransportDataAsyncTask extends AbstractAsyncTask {

    // private final Log log = LogFactory.getLog(getClass());

    @Autowired
    TAUserService userService;

    @Autowired
    RefBookFactory refBookFactory;

    @Autowired
    private LockDataService lockDataService;

    @Autowired
    private BlobDataService blobDataService;

    @Autowired
    private UploadTransportDataService uploadTransportDataService;

    @Autowired
    private LoadFormDataService loadFormDataService;

    @Autowired
    private LoadRefBookDataService loadRefBookDataService;

    @Autowired
    private LockDataService lockService;

    @Override
    protected ReportType getReportType() {
        return ReportType.UPLOAD_TF;
    }

    @Override
    public BalancingVariants checkTaskLimit(Map<String, Object> params) {
        return BalancingVariants.LONG;
    }

    @Override
    protected void executeBusinessLogic(Map<String, Object> params, Logger logger) {
        int userId = (Integer)params.get(USER_ID.name());
        String uuidFile = (String)params.get("uuidFile");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));
        final String lock = (String) params.get(LOCKED_OBJECT.name());
        final Date lockDate = (Date) params.get(LOCK_DATE.name());

        BlobData blobData = blobDataService.get(uuidFile);
        String key = LockData.LockObjects.CONFIGURATION_PARAMS.name() + "_" + UUID.randomUUID().toString().toLowerCase();
        lockDataService.lock(key, userInfo.getUser().getId(),
                LockData.DescriptionTemplate.CONFIGURATION_PARAMS.getText(),
                lockDataService.getLockTimeout(LockData.LockObjects.CONFIGURATION_PARAMS));
        try {

            // Загрузка в каталог
            UploadResult uploadResult = uploadTransportDataService.uploadFile(userInfo, blobData.getName(),
                    blobData.getInputStream(), logger);

            // Загрузка из каталога
            if (!uploadResult.getDiasoftFileNameList().isEmpty()) {
                // Diasoft
                lockService.updateState(lock, lockDate, "Импорт справочников \"Diasoft\"");
                loadRefBookDataService.importRefBookDiasoft(userInfo, uploadResult.getDiasoftFileNameList(), logger, lock);
            }
            if (!uploadResult.getAvgCostFileNameList().isEmpty()) {
                lockService.updateState(lock, lockDate, "Импорт справочника \"Средняя стоимость транспортных средств\"");
                loadRefBookDataService.importRefBookAvgCost(userInfo, uploadResult.getAvgCostFileNameList(), logger, lock);
            }

            if (!uploadResult.getFormDataFileNameList().isEmpty()) {
                // НФ
                // Пересечение списка доступных приложений и списка загруженных приложений
                List<Integer> departmentList = new ArrayList(CollectionUtils.intersection(
                        loadFormDataService.getTB(userInfo, logger), uploadResult.getFormDataDepartmentList()));

                lockService.updateState(lock, lockDate, "Импорт налоговых форм");
                loadFormDataService.importFormData(userInfo, departmentList, uploadResult.getFormDataFileNameList(), logger, lock);
            }
        } catch (IOException e) {
            throw new ServiceException("Ошибка при работе с файлом", e);
        } finally {
            try {
                blobDataService.delete(blobData.getUuid());
            } catch (Exception e) {}
            lockDataService.unlock(key, userInfo.getUser().getId());
        }
    }

    @Override
    protected String getAsyncTaskName() {
        return "Загрузка ТФ с локального компьютера";
    }

    @Override
    protected String getNotificationMsg(Map<String, Object> params) {
        return "Загрузка ТФ с локального компьютера завершена";
    }

    @Override
    protected String getErrorMsg(Map<String, Object> params) {
        return "Произошла непредвиденная ошибка при загрузка ТФ с локального компьютера";
    }
}
