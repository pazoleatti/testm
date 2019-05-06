package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessage;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessageFilter;

import java.util.List;

public interface TransportMessageService {

    TransportMessage findById(Long id);

    String findMessageBodyById(Long id);

    List<TransportMessage> findByFilter(TransportMessageFilter filter);
}
