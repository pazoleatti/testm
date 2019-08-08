package com.aplana.sbrf.taxaccounting.service.jms.transport;

import com.aplana.sbrf.taxaccounting.model.exception.ConfigurationParameterAbsentException;

public interface MessageSender {
    void sendMessage(String fileName) throws ConfigurationParameterAbsentException;
}
