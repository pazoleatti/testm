package com.aplana.sbrf.taxaccounting.common.service;

import com.aplana.sbrf.taxaccounting.common.model.EventType;
import com.aplana.sbrf.taxaccounting.common.model.UserInfo;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Спринговая реализация общего сервиса. Нужна для того, чтобы все работало в dev-моде.
 *
 * @author dloshkarev
 */
@Service
public class EventAuditServiceImpl implements EventAuditService {

    @Autowired
    private AuditService auditService;

    @Autowired
    private TAUserService userService;

    @Override
    public void addAuditLog(EventType event, UserInfo userInfo, String note) throws CommonServiceException {
        try {
            FormDataEvent taxEvent = EventRelationsContainer.get(event);
            TAUserInfo taUserInfo = getTaUserInfo(userInfo);
            auditService.add(taxEvent, taUserInfo, taUserInfo.getUser().getDepartmentId(), null, null, null, null, note, null, null);
        } catch (RuntimeException e) {
            throw new CommonServiceException(e.getMessage(), e.getCause());
        } catch (Exception e) {
            throw new CommonServiceException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public void addAuditLog(EventType event, UserInfo userInfo, Integer departmentId, Integer reportPeriodId,
                            String declarationType, String formType, Integer formKindId, String note, String blobDataId,
                            Integer formTypeId) throws CommonServiceException {
        try {
            FormDataEvent taxEvent = EventRelationsContainer.get(event);
            auditService.add(taxEvent, getTaUserInfo(userInfo), departmentId, reportPeriodId, declarationType,
                    formType, formKindId, note, blobDataId, formTypeId);
        } catch (RuntimeException e) {
            throw new CommonServiceException(e.getMessage(), e.getCause());
        } catch (Exception e) {
            throw new CommonServiceException(e.getMessage(), e.getCause());
        }
    }

    private TAUserInfo getTaUserInfo(UserInfo userInfo) {
        TAUserInfo taUserInfo = new TAUserInfo();
        taUserInfo.setIp(userInfo.getUserIp());
        taUserInfo.setUser(userService.getUser((int) userInfo.getUserId()));
        return taUserInfo;
    }


}
