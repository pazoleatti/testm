package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriodJournalItem;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataSearchService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.LockDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

@Service
@Transactional
public class DepartmentReportPeriodServiceImpl implements DepartmentReportPeriodService {

    /**
     * Логгер для {@link DepartmentReportPeriodServiceImpl}
     */
    private static final Log LOG = LogFactory.getLog(DepartmentReportPeriodServiceImpl.class);

    private final static String ERROR_BATCH_MESSAGE = "Пустой список отчетных периодов";

    @Autowired
    private DepartmentReportPeriodDao departmentReportPeriodDao;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private DeclarationDataSearchService declarationDataService;
    @Autowired
    private DeclarationTemplateService declarationTemplateService;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private LockDataService lockDataService;

    @Override
    public List<DepartmentReportPeriod> fetchAllByFilter(DepartmentReportPeriodFilter departmentReportPeriodFilter) {
        return departmentReportPeriodDao.fetchAllByFilter(departmentReportPeriodFilter);
    }

    @Override
    public DepartmentReportPeriod fetchOneByFilter(DepartmentReportPeriodFilter departmentReportPeriodFilter) {
        List<DepartmentReportPeriod> departmentReportPeriods = departmentReportPeriodDao.fetchAllByFilter(departmentReportPeriodFilter);
        if (departmentReportPeriods.size() > 1) {
            throw new IllegalArgumentException();
        } else if (!departmentReportPeriods.isEmpty()) {
            return departmentReportPeriods.get(0);
        }
        return null;
    }

    @Override
    public List<Integer> fetchAllIdsByFilter(DepartmentReportPeriodFilter departmentReportPeriodFilter) {
        return departmentReportPeriodDao.fetchAllIdsByFilter(departmentReportPeriodFilter);
    }

    @Override
    @Transactional
    public void create(DepartmentReportPeriod departmentReportPeriod) {
        LOG.info(String.format("DepartmentReportPeriodServiceImpl.create. departmentReportPeriod: %s", departmentReportPeriod));
        departmentReportPeriodDao.create(departmentReportPeriod);
    }

    @Override
    @Transactional
    public void create(DepartmentReportPeriod departmentReportPeriod, List<Integer> departmentIds) {
        Assert.notEmpty(departmentIds);
        LOG.info(String.format("DepartmentReportPeriodServiceImpl.create. departmentReportPeriod: %s; departmentIds: %s", departmentReportPeriod, departmentIds));
        departmentReportPeriodDao.create(departmentReportPeriod, departmentIds);
    }

    @Override
    @Transactional
    public void merge(final List<DepartmentReportPeriod> departmentReportPeriods, final Integer departmentId) {
        if (departmentReportPeriods != null && !departmentReportPeriods.isEmpty()) {
            LOG.info(String.format("DepartmentReportPeriodServiceImpl.merge. departmentReportPeriods.size: %s; departmentId: %s", departmentReportPeriods.size(), departmentId));
            departmentReportPeriodDao.merge(departmentReportPeriods, departmentId);
        }
    }

    @Override
    @Transactional
    public void updateActive(int id, boolean active) {
        LOG.info(String.format("DepartmentReportPeriodServiceImpl.updateActive. id: %s; active: %s", id, active));
        departmentReportPeriodDao.updateActive(id, active);
    }

    @Override
    @Transactional
    public void updateActive(List<Integer> ids, Integer reportPeriodId, boolean active) {
        LOG.info(String.format("DepartmentReportPeriodServiceImpl.updateActive. ids: %s; reportPeriodId: %s; active: %s", ids, reportPeriodId, active));
        if (ids == null || ids.isEmpty()) {
            throw new ServiceException(ERROR_BATCH_MESSAGE);
        }
        departmentReportPeriodDao.updateActive(ids, reportPeriodId, active);
    }

    @Override
    @Transactional
    public void delete(List<Integer> ids) {
        LOG.info(String.format("DepartmentReportPeriodServiceImpl.delete. ids: %s", ids));
        departmentReportPeriodDao.delete(ids);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isExistsByReportPeriodIdAndDepartmentId(int departmentId, int reportPeriodId) {
        return departmentReportPeriodDao.isExistsByReportPeriodIdAndDepartmentId(departmentId, reportPeriodId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isExistsByReportPeriodId(int reportPeriodId) {
        return departmentReportPeriodDao.isExistsByReportPeriodId(reportPeriodId);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentReportPeriod fetchLast(int departmentId, int reportPeriodId) {
        return departmentReportPeriodDao.fetchLast(departmentId, reportPeriodId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isLaterCorrectionPeriodExists(DepartmentReportPeriod departmentReportPeriod) {
        return departmentReportPeriodDao.isLaterCorrectionPeriodExists(departmentReportPeriod);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentReportPeriodJournalItem> fetchJournalItemByFilter(DepartmentReportPeriodFilter filter) {
        return departmentReportPeriodDao.fetchJournalItemByFilter(filter);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentReportPeriod fetchOne(int id) {
        return departmentReportPeriodDao.fetchOne(id);
    }

}