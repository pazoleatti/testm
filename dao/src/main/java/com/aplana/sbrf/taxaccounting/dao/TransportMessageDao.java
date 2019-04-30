package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessage;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessageFilter;

import java.util.List;

/**
 * Доступ к таблице TRANSPORT_MESSAGE.
 */
public interface TransportMessageDao {

    TransportMessage findById(Long id);

    String findMessageBodyById(Long id);

    List<TransportMessage> findByFilter(TransportMessageFilter filter, PagingParams pagingParams);
}
