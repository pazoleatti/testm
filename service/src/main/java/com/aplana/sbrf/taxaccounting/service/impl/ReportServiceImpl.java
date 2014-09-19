package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.ReportDao;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    ReportDao reportDao;

    @Override
    public void create(long formDataId, String blobDataId, ReportType type, boolean checking, boolean manual, boolean absolute) {
        reportDao.create(formDataId, blobDataId, type, checking, manual, absolute);
    }

    @Override
    public String get(long formDataId, ReportType type, boolean checking, boolean manual, boolean absolute) {
        return reportDao.get(formDataId, type, checking, manual, absolute);
    }
}
