package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.*;

/**
 * Спринговая реализация таска "Генерация xlsm-файл" для вызова из дев-мода
 * @author Lhaziev
 */
@Component("TestXlsmGeneratorAsyncTaskSpring")
@Transactional
public class TestXlsmGeneratorAsyncTaskSpring implements AsyncTask {
    protected final Log log = LogFactory.getLog(getClass());

    @Autowired
    private TAUserService userService;

    @Autowired
    private PrintingService printingService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private FormDataAccessService formDataAccessService;

    @Autowired
    private FormDataService formDataService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;

    @Autowired
    private LockDataService lockService;

    @Override
    public void execute(Map<String, Object> params) {
        String lock = (String) params.get(LOCKED_OBJECT.name());
        try {
            if (lockService.isLockExists(lock)) {
                //Если блокировка на объект задачи все еще существует, значит на нем можно выполнять бизнес-логику
                executeBusinessLogic(params);
                if (!lockService.isLockExists(lock)) {
                    //Если после выполнения бизнес логики, оказывается, что блокировки уже нет
                    //Значит результаты нам уже не нужны - откатываем транзакцию и все изменения
                    throw new RuntimeException("Результат выполнения задачи \"" + getAsyncTaskName() + "\" больше не актуален. Выполняется откат транзакции");
                }
                //Получаем список пользователей, для которых надо сформировать оповещение
                String msg = getNotificationMsg(params);
                if (msg != null && !msg.isEmpty()) {
                    List<Integer> waitingUsers = lockService.getUsersWaitingForLock(lock);
                    if (!waitingUsers.isEmpty()) {
                        List<Notification> notifications = new ArrayList<Notification>();
                        for (Integer userId : waitingUsers) {
                            Notification notification = new Notification();
                            notification.setUserId(userId);
                            notification.setCreateDate(new Date());
                            notification.setText(msg);
                            notifications.add(notification);
                        }
                        //Создаем оповещение для каждого пользователя из списка
                        notificationService.saveList(notifications);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Не удалось выполнить асинхронную задачу", e);
        } finally {
            //Снимаем блокировку
            lockService.unlock(lock, (Integer) params.get(USER_ID.name()));
        }
    }

    private void executeBusinessLogic(Map<String, Object> params) {
        int userId = (Integer)params.get(USER_ID.name());
        long formDataId = (Long)params.get("formDataId");
        boolean manual = (Boolean)params.get("manual");
        boolean isShowChecked = (Boolean)params.get("isShowChecked");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));

        formDataAccessService.canRead(userInfo, formDataId);
        String uuid = printingService.generateExcel(userInfo, formDataId, manual, isShowChecked);
        reportService.create(formDataId, uuid, ReportType.EXCEL, isShowChecked, manual, false);
    }

    protected String getAsyncTaskName() {
        return "Генерация xlsm-файла";
    }

    protected String getNotificationMsg(Map<String, Object> params) {
        int userId = (Integer)params.get(USER_ID.name());
        long formDataId = (Long)params.get("formDataId");
        boolean manual = (Boolean)params.get("manual");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));

        Logger logger = new Logger();
        FormData formData = formDataService.getFormData(userInfo, formDataId, manual, logger);
        Department department = departmentService.getDepartment(formData.getDepartmentId());
        DepartmentReportPeriod reportPeriod = departmentReportPeriodService.get(formData.getDepartmentReportPeriodId());
        Integer periodOrder = formData.getPeriodOrder();
        if (periodOrder == null){
            return String.format("Сформирован %s отчет налоговой формы: Период: \"%s, %s\", Подразделение: \"%s\", Тип: \"%s\", Вид: \"%s\", Версия: \"%s\".", ReportType.EXCEL.getName(), reportPeriod.getReportPeriod().getTaxPeriod().getYear(), reportPeriod.getReportPeriod().getName(), department.getName(), formData.getKind().getName(), formData.getFormType().getName(), manual ? "ручного ввода" : "автоматическая");
        } else {
            return String.format("Сформирован %s отчет налоговой формы: Период: \"%s, %s\", Месяц: \"%s\", Подразделение: \"%s\", Тип: \"%s\", Вид: \"%s\", Версия: \"%s\".", ReportType.EXCEL.getName(), reportPeriod.getReportPeriod().getTaxPeriod().getYear(), reportPeriod.getReportPeriod().getName(), Formats.getRussianMonthNameWithTier(formData.getPeriodOrder()), department.getName(), formData.getKind().getName(), formData.getFormType().getName(), manual ? "ручного ввода" : "автоматическая");
        }
    }
}
