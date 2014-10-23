package com.aplana.sbrf.taxaccounting.common.service;

import com.aplana.sbrf.taxaccounting.common.model.EventType;
import com.aplana.sbrf.taxaccounting.common.model.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ejb.*;
import javax.interceptor.Interceptors;

/**
 * EJB-реализация сервиса аудита событий
 *
 * @author dloshkarev
 */
@Local(EventAuditServiceLocal.class)
@Remote(EventAuditServiceRemote.class)
@Stateless
@Interceptors(EventAuditInterceptor.class)
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class EventAuditServiceBean implements EventAuditService {

    @Autowired
    private EventAuditService eventAuditService;

    @Override
    public void addAuditLog(EventType event, UserInfo userInfo, String note) throws CommonServiceException {
        eventAuditService.addAuditLog(event, userInfo, note);
    }

    @Override
    public void addAuditLog(EventType event, UserInfo userInfo, Integer departmentId, Integer reportPeriodId,
                            String declarationType, String formType, Integer formKindId, String note,
                            String blobDataId, Integer formTypeId) throws CommonServiceException {
        eventAuditService.addAuditLog(event, userInfo, departmentId, reportPeriodId, declarationType, formType,
                formKindId, note, blobDataId, formTypeId);
    }
}
