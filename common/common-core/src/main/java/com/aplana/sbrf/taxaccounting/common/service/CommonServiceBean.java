package com.aplana.sbrf.taxaccounting.common.service;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ejb.*;
import javax.interceptor.Interceptors;

/**
 * EJB-реализация общего сервиса
 * @author dloshkarev
 */
@Local(CommonServiceLocal.class)
@Remote(CommonServiceRemote.class)
@Stateless
@Interceptors(CommonInterceptor.class)
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class CommonServiceBean implements CommonService {

    @Autowired
    private CommonService commonService;

    @Override
    public void addAuditLog(FormDataEvent event, TAUserInfo userInfo, Integer departmentId, Integer reportPeriodId,
                            String declarationType, String formType, Integer formKindId, String note, String blobDataId) {
        commonService.addAuditLog(event, userInfo, departmentId, reportPeriodId,
                declarationType, formType, formKindId, note, blobDataId);
    }
}
