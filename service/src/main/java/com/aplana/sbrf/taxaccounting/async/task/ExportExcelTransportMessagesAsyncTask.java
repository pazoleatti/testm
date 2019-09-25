package com.aplana.sbrf.taxaccounting.async.task;

/**
 * <b>{Description here REQUIRED}</b>
 * Created by <i><b>s.molokovskikh</i></b> on 25.09.19.
 */

import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessage;
import com.aplana.sbrf.taxaccounting.service.LockStateLogger;
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
public class ExportExcelTransportMessagesAsyncTask extends AbstractAsyncTask{


    @Autowired
    private TAUserService userService;

    @Autowired
    private TransportMessageService transportMessageService;

    @Autowired
    private AsyncManager asyncManager;

    @Autowired
    private PrintingService printingService;


    @Override
    protected BusinessLogicResult executeBusinessLogic(AsyncTaskData taskData, Logger logger) throws InterruptedException {
        Map<String, Object> params = taskData.getParams();
        List<Long> transportMessageIds = (List<Long>) params.get("transportMessageIds");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));
        List<TransportMessage> transportMessages = transportMessageService.findByIds(transportMessageIds, userInfo);

        printingService.generateExcelTransportMessages(transportMessages);
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
        return null;
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
