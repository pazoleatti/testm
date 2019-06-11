package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.LogBusinessDao;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.LogBusiness;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.dto.LogBusinessDTO;
import com.aplana.sbrf.taxaccounting.service.LogBusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service("logBusinessService")
@Transactional(readOnly = true)
public class LogBusinessServiceImpl implements LogBusinessService {

    @Autowired
    private LogBusinessDao logBusinessDao;

    @Override
    @Transactional
    public void logFormEvent(Long declarationId, FormDataEvent event, String logId, String note, TAUserInfo userInfo) {
        LogBusiness log = makeLogBusiness(event, logId, note, userInfo.getUser());
        log.setDeclarationDataId(declarationId);
        logBusinessDao.create(log);
    }

    @Override
    @Transactional
    public void logPersonEvent(Long personId, FormDataEvent event, String note, TAUserInfo userInfo) {
        LogBusiness log = makeLogBusiness(event, null, note, userInfo.getUser());
        log.setPersonId(personId);
        logBusinessDao.create(log);
    }

    @Override
    @Transactional
    public void create(LogBusiness logBusiness) {
        logBusinessDao.create(logBusiness);
    }

    @Override
    public Date getMaxLogDateByDeclarationIdAndEvent(long declarationId, FormDataEvent event) {
        return logBusinessDao.getMaxLogDateByDeclarationIdAndEvent(declarationId, event);
    }

    @Override
    public List<LogBusinessDTO> findAllByDeclarationId(long declarationId, PagingParams pagingParams) {
        return logBusinessDao.findAllByDeclarationId(declarationId, pagingParams);
    }

    @Override
    @PreAuthorize("hasPermission(#personId, 'com.aplana.sbrf.taxaccounting.model.refbook.RegistryPerson', T(com.aplana.sbrf.taxaccounting.permissions.PersonPermission).VIEW_VIP_DATA)")
    public PagingResult<LogBusinessDTO> findAllByPersonId(long personId, PagingParams pagingParams) {
        return logBusinessDao.findAllByPersonId(personId, pagingParams);
    }

    private LogBusiness makeLogBusiness(FormDataEvent event, String logId, String note, TAUser user) {
        LogBusiness log = new LogBusiness();
        log.setEvent(event);
        log.setUser(user);
        log.setLogDate(new Date());
        log.setLogId(logId);
        log.setNote(note);
        return log;
    }
}
