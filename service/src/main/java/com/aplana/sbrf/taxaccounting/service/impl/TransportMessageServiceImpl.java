package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.TransportMessageDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessage;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessageFilter;
import com.aplana.sbrf.taxaccounting.service.TransportMessageService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransportMessageServiceImpl implements TransportMessageService {

    private static final Log LOG = LogFactory.getLog(TransportMessageServiceImpl.class);

    @Autowired
    private TransportMessageDao transportMessageDao;

    @Override
    public TransportMessage findById(Long id) {
        return transportMessageDao.findById(id);
    }

    @Override
    public String findMessageBodyById(Long id) {
        return transportMessageDao.findMessageBodyById(id);
    }

    @Override
    public PagingResult<TransportMessage> findByFilter(TransportMessageFilter filter, PagingParams pagingParams) {
        return transportMessageDao.findByFilter(filter, pagingParams);
    }

    @Override
    public void create(TransportMessage transportMessage) {
        transportMessageDao.create(transportMessage);
        LOG.info("Сохранено транспортное сообщение: " + transportMessage);
    }

    @Override
    public void update(TransportMessage transportMessage) {
        transportMessageDao.update(transportMessage);
    }
}
