package com.aplana.sbrf.taxaccounting.service.component.factory;

import com.aplana.sbrf.taxaccounting.model.jms.BaseMessage;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessage;

public interface TransportMessageFactory {

    TransportMessage createOutcomeMessageToEdo(BaseMessage baseIncomeXmlMessage, String messageBody);
}
