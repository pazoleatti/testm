package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.ReportDao;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.FormDataAccessService;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    ReportDao reportDao;

    @Autowired
    FormDataAccessService formDataAccessService;

    @Override
    public void create(long formDataId, String blobDataId, ReportType type, boolean checking, boolean manual, boolean absolute) {
        reportDao.create(formDataId, blobDataId, type, checking, manual, absolute);
    }

    @Override
    public String get(TAUserInfo userInfo, long formDataId, ReportType type, boolean checking, boolean manual, boolean absolute) {
        formDataAccessService.canRead(userInfo, formDataId);
        return reportDao.get(formDataId, type, checking, manual, absolute);
    }

    @Override
    public void delete(long formDataId) {
        reportDao.delete(formDataId);
    }
}
