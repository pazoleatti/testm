package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.ReportDao;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataReportType;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.FormDataReportType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataAccessService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@Transactional
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportDao reportDao;

    @Autowired
    private DeclarationDataAccessService declarationDataAccessService;

    @Override
    public void createDec(long declarationDataId, String blobDataId, DeclarationDataReportType type) {
        reportDao.createDec(declarationDataId, blobDataId, type);
    }

    @Override
    public String getDec(TAUserInfo userInfo, long declarationDataId, DeclarationDataReportType type) {
        declarationDataAccessService.checkEvents(userInfo, declarationDataId, FormDataEvent.GET_LEVEL1);
        return reportDao.getDec(declarationDataId, type);
    }

    @Override
    public void deleteDec(long formDataId) {
        reportDao.deleteDec(formDataId);
    }

    @Override
    public void deleteDec(Collection<Long> declarationDataId) {
        reportDao.deleteDec(declarationDataId);
    }

    @Override
    public void deleteDec(Collection<Long> declarationDataId, List<DeclarationDataReportType> ddReportTypes) {
        if (ddReportTypes != null && !ddReportTypes.isEmpty()) {
            reportDao.deleteDec(declarationDataId, ddReportTypes);
        }
    }

    @Override
    public void deleteDec(String blobDataId) {
        reportDao.deleteDec(blobDataId);
    }
}
