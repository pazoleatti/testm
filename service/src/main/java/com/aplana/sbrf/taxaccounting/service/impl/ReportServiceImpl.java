package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.ReportDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
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
    public void createAudit(Integer userId, String blobDataId, ReportType type) {
        String uuid = reportDao.getAudit(userId, type);
        if (type == ReportType.ARCHIVE_AUDIT) {
            reportDao.deleteAudit(uuid);
        } else if (uuid != null){
            throw new ServiceException("Для этого пользователя уже есть отчет по ЖА, проверьте выгрузку.");
        }
        reportDao.createAudit(userId, blobDataId, type);
    }

    @Override
    public String getDec(TAUserInfo userInfo, long declarationDataId, DeclarationDataReportType type) {
        declarationDataAccessService.checkEvents(userInfo, declarationDataId, FormDataEvent.GET_LEVEL1);
        return reportDao.getDec(declarationDataId, type);
    }

    @Override
    public String getAudit(TAUserInfo userInfo, ReportType type) {
        Integer userId = (type == ReportType.ARCHIVE_AUDIT ? null : userInfo.getUser().getId());
        return reportDao.getAudit(userId, type);
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

    @Override
    public void deleteAudit(TAUserInfo userInfo, ReportType reportType) {
        reportDao.deleteAudit(userInfo.getUser().getId(), reportType);
    }

    @Override
    public void deleteAudit(String blobDataId) {
        reportDao.deleteAudit(blobDataId);
    }
}
