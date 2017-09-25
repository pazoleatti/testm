package com.aplana.sbrf.taxaccounting.model.error;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;

/**
 * Класс - обертка для отправки сообщений об ошибке на клиент
 */
public class ExceptionMessage {

    public ExceptionMessage(int messageCode) {
        this.messageCode = messageCode;
    }

    /**
     * Тип сообщения
     */
    private MessageType messageType;
    /**
     * Текст сообщения
     */
    private int messageCode;
    /**
     * Набор класс-обёрток для отображения на клиенте одного исключения
     */
    private Set<ExceptionCause> exceptionCause = Sets.newHashSet();
    /**
     * Дополнительная информация
     */
    private Map<String, Object> additionInfo = Maps.newHashMap();

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public int getMessageCode() {
        return messageCode;
    }

    public void setMessageCode(int messageCode) {
        this.messageCode = messageCode;
    }

    public Set<ExceptionCause> getExceptionCause() {
        return exceptionCause;
    }

    public void setExceptionCause(Set<ExceptionCause> exceptionCause) {
        this.exceptionCause = exceptionCause;
    }

    public Map<String, Object> getAdditionInfo() {
        return additionInfo;
    }

    public void setAdditionInfo(Map<String, Object> additionInfo) {
        this.additionInfo = additionInfo;
    }
}
