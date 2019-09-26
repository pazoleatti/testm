package com.aplana.sbrf.taxaccounting.async.task;

/**
 * <b>{Description here REQUIRED}</b>
 * Created by <i><b>s.molokovskikh</i></b> on 25.09.19.
 */

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessage;
import com.aplana.sbrf.taxaccounting.service.PrintingService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.service.TransportMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Формирование файла Excel по транспортным сообщениям Обмена с ФП АС Учет налогов
 */
@Component("ExportExcelTransportMessagesAsyncTask")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ExportExcelTransportMessagesAsyncTask extends AbstractAsyncTask {


    @Autowired
    private TAUserService userService;

    @Autowired
    private TransportMessageService transportMessageService;

    @Autowired
    private PrintingService printingService;


    @Override
    protected BusinessLogicResult executeBusinessLogic(AsyncTaskData taskData, Logger logger) throws InterruptedException {
        Map<String, Object> params = taskData.getParams();
        List<Long> transportMessageIds = (List<Long>) params.get("transportMessageIds");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));
        List<TransportMessage> transportMessages = transportMessageService.findByIds(transportMessageIds, userInfo);

        String uuid = printingService.generateExcelTransportMessages(transportMessages);
        return new BusinessLogicResult(true, NotificationType.REF_BOOK_REPORT, uuid);
    }

    @Override
    protected String getNotificationMsg(AsyncTaskData taskData) {
        return "Сформирован список транспортных сообщений обмена с ФП АС Учет налогов";
    }

    @Override
    protected String getErrorMsg(AsyncTaskData taskData, boolean unexpected) {
        return "Произошла непредвиденная ошибка при формировании списка по транспортным сообщениям Обмена" +
                " с ФП АС Учет налогов";
    }

    @Override
    protected AsyncQueue checkTaskLimit(String taskDescription, TAUserInfo user, Map<String, Object> params, Logger logger) throws AsyncTaskException {
        AsyncTaskTypeData taskTypeData = asyncTaskTypeDao.findById(getAsyncTaskType().getId());
        if (taskTypeData == null) {
            throw new AsyncTaskException(String.format("Cannot find task parameters for \"%s\"", taskDescription));
        }

        List<Long> transportMessageIds = (List<Long>) params.get("transportMessageIds");
        int value = transportMessageService.findByIds(transportMessageIds, user).size();

        Long taskLimit = taskTypeData.getTaskLimit();
        if (taskLimit != null && taskLimit != 0 && value != 0 && taskLimit < value) {
            String errorText = "Количество отобранных для выгрузки в файл записей превышает пороговое значение = "
                    + taskLimit + " строк";
            throw new ServiceException(errorText, taskDescription);
        } else {
            return AsyncQueue.SHORT;
        }
    }

    @Override
    protected AsyncTaskType getAsyncTaskType() {
        return AsyncTaskType.EXPORT_TRANSPORT_MESSAGES;
    }


    @Override
    public String createDescription(TAUserInfo userInfo, Map<String, Object> params) {
        return getAsyncTaskType().getDescription();
    }
}
