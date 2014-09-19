package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.Notification;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.*;

/**
 * Спринговая реализация таска "Генерация xlsm-файл" для вызова из дев-мода
 * @author Lhaziev
 */
@Component("TestXlsmGeneratorAsyncTaskSpring")
@Transactional
public class TestXlsmGeneratorAsyncTaskSpring implements AsyncTask {

    @Autowired
    private TAUserService userService;

    @Autowired
    private PrintingService printingService;

    @Autowired
    private BlobDataService blobDataService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private FormDataService formDataService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private LockDataService lockService;

    private static final String FILE_NAME = "Налоговый_отчет_";
    private static final String POSTFIX = ".xlsm";

    @Override
    public void execute(Map<String, Object> params) {
        String lock = (String) params.get(LOCKED_OBJECT.name());
        int userId = (Integer) params.get(USER_ID.name());
        if (lockService.checkLock(lock)) {
            //Если блокировка на объект задачи все еще существует, значит на нем можно выполнять бизнес-логику
            try {
                executeBusinessLogic(params);
            } catch (Exception e) {
                lockService.unlock(lock, userId);
                throw new RuntimeException("Не удалось выполнить задачи \"" + getAsyncTaskName() + "\". Выполняется откат транзакции. Произошла ошибка: " + e.getMessage(), e);
            }
            if (!lockService.checkLock(lock)) {
                //Если после выполнения бизнес логики, оказывается, что блокировки уже нет
                //Значит результаты нам уже не нужны - откатываем транзакцию и все изменения
                throw new RuntimeException("Результат выполнения задачи \"" + getAsyncTaskName() + "\" больше не актуален. Выполняется откат транзакции");
            }
            lockService.unlock(lock, userId);
        }
    }

    private void executeBusinessLogic(Map<String, Object> params) {
        int userId = (Integer)params.get(USER_ID.name());
        long formDataId = (Long)params.get("formDataId");
        boolean manual = (Boolean)params.get("manual");
        boolean isShowChecked = (Boolean)params.get("isShowChecked");

        Logger logger = new Logger();
        FormData formData = formDataService.getFormData(userService.getSystemUserInfo(), formDataId, manual, logger);

        byte[] xlsxBytes = printingService.generateExcel(userService.getUser(userId), formDataId, manual, isShowChecked);

        Notification notification = new Notification();
        notification.setCreateDate(new Date());
        notification.setDeadline(new Date(115,1,1));
        notification.setText("");
        notification.setReportPeriodId(formData.getReportPeriodId());
        notification.setReceiverDepartmentId(formData.getDepartmentId());
        notification.setSenderDepartmentId(userService.getUser(userId).getDepartmentId());
        notification.setText(""+params.toString());
        String uuid = blobDataService.create(new ByteArrayInputStream(xlsxBytes), FILE_NAME + formDataId + POSTFIX);
        reportService.create(formDataId, uuid, ReportType.EXCEL, isShowChecked, manual, false);
        //notificationService.save(notification);
    }

    protected String getAsyncTaskName() {
        return "Генерация xlsm-файла";
    }
}
