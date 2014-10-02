package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
import net.sf.jasperreports.engine.query.JRXPathQueryExecuterFactory;
import net.sf.jasperreports.engine.util.JRXmlUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.*;
import java.util.*;

import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.*;

/**
 * Спринговая реализация таска "Генерация xlsx-файла" для вызова из дев-мода
 * @author Lhaziev
 */
@Component("TestXlsxGeneratorAsyncTaskSpring")
@Transactional
public class TestXlsxGeneratorAsyncTaskSpring implements AsyncTask {
    protected final Log log = LogFactory.getLog(getClass());

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TAUserService userService;

    @Autowired
    private DeclarationDataService declarationDataService;

    @Autowired
    private BlobDataService blobDataService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;

    @Autowired
    private LockDataService lockService;

    @Override
    public void execute(Map<String, Object> params) {
        String lock = (String) params.get(LOCKED_OBJECT.name());
        Date lockDateEnd = (Date) params.get(LOCK_DATE_END.name());
        try {
            if (lockService.isLockExists(lock, lockDateEnd)) {
                //Если блокировка на объект задачи все еще существует, значит на нем можно выполнять бизнес-логику
                try {
                    executeBusinessLogic(params);
                } catch (Exception e) {
                    if (lockService.isLockExists(lock, lockDateEnd)) {
                        lockService.unlock(lock, (Integer) params.get(USER_ID.name()), true);
                    }
                    throw e; // TODO с каким сообщением???
                }
                if (!lockService.isLockExists(lock, lockDateEnd)) {
                    //Если после выполнения бизнес логики, оказывается, что блокировки уже нет
                    //Значит результаты нам уже не нужны - откатываем транзакцию и все изменения
                    throw new RuntimeException("Результат выполнения задачи \"" + getAsyncTaskName() + "\" больше не актуален. Выполняется откат транзакции");
                }

                //Получаем список пользователей, для которых надо сформировать оповещение
                try {
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
                } finally {
                    lockService.unlock(lock, (Integer) params.get(USER_ID.name()), true);
                }
            }
        } catch (Exception e) {
            log.error("Не удалось выполнить асинхронную задачу", e);
        }
    }

    private void executeBusinessLogic(Map<String, Object> params) {
        long declarationDataId = (Long)params.get("declarationDataId");
        int userId = (Integer)params.get(USER_ID.name());
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));

        reportService.createDec(declarationDataId, blobDataService.create(new ByteArrayInputStream(declarationDataService.getXlsxData(declarationDataId, userInfo)), ""), ReportType.EXCEL_DEC);
    }

    protected String getNotificationMsg(Map<String, Object> params) {
        int userId = (Integer)params.get(USER_ID.name());
        long declarationDataId = (Long)params.get("declarationDataId");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));

        DeclarationData declaration = declarationDataService.get(declarationDataId, userInfo);
        Department department = departmentService.getDepartment(declaration.getDepartmentId());
        DepartmentReportPeriod reportPeriod = departmentReportPeriodService.get(declaration.getDepartmentReportPeriodId());
        return String.format("Сформирован %s отчет налоговой формы: Период: \"%s, %s\", Подразделение: \"%s\", Тип: \"%s\", Вид: \"%s\".", ReportType.EXCEL.getName(), reportPeriod.getReportPeriod().getTaxPeriod().getYear(), reportPeriod.getReportPeriod().getName(), department.getName(), "''", "''");
    }

    protected String getAsyncTaskName() {
        return "Генерация xlsx-файла";
    }
}
