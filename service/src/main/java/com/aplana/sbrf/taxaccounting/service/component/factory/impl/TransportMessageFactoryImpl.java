package com.aplana.sbrf.taxaccounting.service.component.factory.impl;

import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.Subsystem;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.jms.BaseMessage;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessage;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessageState;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessageType;
import com.aplana.sbrf.taxaccounting.service.ConfigurationService;
import com.aplana.sbrf.taxaccounting.service.component.factory.TransportMessageFactory;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransportMessageFactoryImpl implements TransportMessageFactory {

    @Autowired
    private ConfigurationService configurationService;

    public TransportMessage createOutcomeMessageToEdo(BaseMessage baseIncomeXmlMessage, String messageBody) {
        TransportMessage transportMessage = new TransportMessage();
        transportMessage.setDateTime(LocalDateTime.fromDateFields(baseIncomeXmlMessage.getDateTime()));
        transportMessage.setBody(messageBody);
        transportMessage.setState(TransportMessageState.SENT);
        transportMessage.setType(TransportMessageType.OUTGOING);
        transportMessage.setMessageUuid(baseIncomeXmlMessage.getUuid());
        transportMessage.setReceiverSubsystem(new Subsystem(getConfigIntValue(ConfigurationParam.TARGET_SUBSYSTEM_ID)));
        transportMessage.setSenderSubsystem(new Subsystem(getConfigIntValue(ConfigurationParam.NDFL_SUBSYSTEM_ID)));
        return transportMessage;
    }

    private int getConfigIntValue(ConfigurationParam param) {
        Integer result = configurationService.getParamIntValue(param);
        if (result == null) {
            throw new ServiceException(String.format("не задан конфигурационный параметр: \"%s\"", param.getCaption()));
        }
        return result;
    }
}
