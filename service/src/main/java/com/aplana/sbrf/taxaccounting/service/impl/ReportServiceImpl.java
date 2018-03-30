package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.ReportDao;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataReportType;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@Transactional
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportDao reportDao;

    @Override
    public void createDec(long declarationDataId, String blobDataId, DeclarationDataReportType type) {
        reportDao.createDec(declarationDataId, blobDataId, type);
    }

    @Override
    @PreAuthorize("hasPermission(#declarationDataId, 'com.aplana.sbrf.taxaccounting.model.DeclarationData', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission).VIEW)")
    public String getDec(long declarationDataId, DeclarationDataReportType type) {
        return reportDao.getDec(declarationDataId, type);
    }

    @Override
    public void deleteDec(long formDataId) {
        reportDao.deleteDec(formDataId);
    }

    @Override
    public void deleteDec(Collection<Long> declarationDataIds) {
        reportDao.deleteDec(declarationDataIds);
    }

    @Override
    public void deleteDec(long declarationDataId, DeclarationDataReportType type) {
        reportDao.deleteDec(declarationDataId, type);
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

    @Override
    public void deleteNotXmlDec(long declarationDataId) {
        reportDao.deleteNotXmlDec(declarationDataId);
    }
}
