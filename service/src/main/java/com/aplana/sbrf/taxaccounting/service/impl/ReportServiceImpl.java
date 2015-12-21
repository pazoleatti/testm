package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.ReportDao;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.FormDataReportType;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataAccessService;
import com.aplana.sbrf.taxaccounting.service.FormDataAccessService;
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
    private FormDataAccessService formDataAccessService;

    @Autowired
    private DeclarationDataAccessService declarationDataAccessService;

    @Override
    public void create(long formDataId, String blobDataId, FormDataReportType type, boolean checking, boolean manual, boolean absolute) {
        reportDao.create(formDataId, blobDataId, type.getReportName(), checking, manual, absolute);
    }

    @Override
    public String get(TAUserInfo userInfo, long formDataId, FormDataReportType type, boolean checking, boolean manual, boolean absolute) {
        formDataAccessService.canRead(userInfo, formDataId);
        return reportDao.get(formDataId, type.getReportName(), checking, manual, absolute);
    }


    @Override
    public void delete(long formDataId, Boolean manual) {
        reportDao.delete(formDataId, manual);
    }

    @Override
    public void createDec(long declarationDataId, String blobDataId, ReportType type) {
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
    public String getDec(TAUserInfo userInfo, long declarationDataId, ReportType type) {
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
    public void deleteDec(Collection<Long> declarationDataId, List<ReportType> reportTypes) {
        if (reportTypes != null && !reportTypes.isEmpty()) {
            reportDao.deleteDec(declarationDataId, reportTypes);
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
