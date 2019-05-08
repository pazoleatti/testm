package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.TransportMessageDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessage;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessageFilter;
import com.aplana.sbrf.taxaccounting.service.TransportMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class TransportMessageServiceImpl implements TransportMessageService {

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
    public List<TransportMessage> findByFilter(TransportMessageFilter filter, PagingParams pagingParams) {
        return transportMessageDao.findByFilter(filter, pagingParams);
    }
}