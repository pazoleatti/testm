package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.service.ErrorService;
import com.aplana.sbrf.taxaccounting.model.error.ExceptionCause;
import com.aplana.sbrf.taxaccounting.model.error.ExceptionMessage;
import com.aplana.sbrf.taxaccounting.model.error.MessageType;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Service
public class ErrorServiceImpl implements ErrorService {

    @Autowired
    private LogEntryService logEntryService;

    /**
     * Перегоняет из StackTraceElement[] в Set<String> с учётом настройки по количеству строк стэка, отображаемого на клиенте
     *
     * @param stack              стэк
     * @param countStackRowsSend количеству строк стэка, отображаемого на клиенте
     * @return набор строк
     */
    private Set<String> stackTraceElementArrayToStringList(StackTraceElement[] stack, Integer countStackRowsSend) {
        Set<String> result = new HashSet<String>();
        Integer i = 0;
        for (StackTraceElement element : stack) {
            result.add(element.toString());
            i++;
            if ((i >= countStackRowsSend) && (countStackRowsSend > -1)) {
                break;
            }
        }
        return result;
    }

    @Override
    public ExceptionMessage getExceptionMessage(Exception exception) {
        ExceptionMessage result = new ExceptionMessage(500);
        if (exception instanceof ServiceLoggerException) {
            result.getAdditionInfo().put("uuid", ((ServiceLoggerException) exception).getUuid());
            result.setMessageType(MessageType.MULTI_ERROR);
        } else if (exception instanceof ServiceException){
            result.setMessageType(MessageType.BUSINESS_ERROR);
        } else {
            result.setMessageType(MessageType.ERROR);
        }
        result.getAdditionInfo().put("serverDate", new Date());

        result.getExceptionCause().add(
                new ExceptionCause(
                        stackTraceElementArrayToStringList(exception.getStackTrace(), exception.getStackTrace().length),
                        exception.getMessage(),
                        ((Throwable) exception).getClass().toString()
                )
        );
        return result;
    }
}
