package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.service.ErrorService;
import com.aplana.sbrf.taxaccounting.model.error.ExceptionCause;
import com.aplana.sbrf.taxaccounting.model.error.ExceptionMessage;
import com.aplana.sbrf.taxaccounting.model.error.MessageType;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Service
public class ErrorServiceImpl implements ErrorService {

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
    public ExceptionMessage getExceptionMessage(MessageType messageType, String messageCode, Exception exception) {
        ExceptionMessage result = new ExceptionMessage(messageType, messageCode);
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
