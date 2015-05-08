package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.ReportDao;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataAccessService;
import com.aplana.sbrf.taxaccounting.service.FormDataAccessService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportDao reportDao;

    @Autowired
    private BlobDataService blobDataService;

    @Autowired
    private FormDataAccessService formDataAccessService;

    @Autowired
    private DeclarationDataAccessService declarationDataAccessService;

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
    public void delete(long formDataId, Boolean manual) {
        reportDao.delete(formDataId, manual);
    }

    @Override
    public void createDec(long declarationDataId, String blobDataId, ReportType type) {
        reportDao.createDec(declarationDataId, blobDataId, type);
    }

    @Override
    public void createAudit(int userId, String blobDataId, ReportType type) {
        reportDao.createAudit(userId, blobDataId, type);
    }

    @Override
    public String getDec(TAUserInfo userInfo, long declarationDataId, ReportType type) {
        declarationDataAccessService.checkEvents(userInfo, declarationDataId, FormDataEvent.GET_LEVEL1);
        return reportDao.getDec(declarationDataId, type);
    }

    @Override
    public String getAudit(TAUserInfo userInfo, ReportType type) {
        return reportDao.getAudit(userInfo.getUser().getId(), type);
    }

    @Override
    public void deleteDec(long formDataId) {
        reportDao.deleteDec(formDataId);
    }

    @Override
    public void deleteDec(String blobDataId) {
        reportDao.deleteDec(blobDataId);
        blobDataService.delete(blobDataId);
    }

    @Override
    public void deleteAudit(TAUserInfo userInfo, ReportType reportType) {
        reportDao.deleteAudit(userInfo.getUser().getId(), reportType);
    }

    @Override
    public void deleteAudit(String blobDataId) {
        reportDao.deleteAudit(blobDataId);
        blobDataService.delete(blobDataId);
    }
}
