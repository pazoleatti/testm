package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataFilter;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataSearchOrdering;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriodJournalItem;
import com.aplana.sbrf.taxaccounting.model.LockDataItem;
import com.aplana.sbrf.taxaccounting.model.MessageGenerator;
import com.aplana.sbrf.taxaccounting.model.State;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public void create(DepartmentReportPeriod departmentReportPeriod) {
        LOG.info(String.format("DepartmentReportPeriodServiceImpl.create. departmentReportPeriod: %s", departmentReportPeriod));
        departmentReportPeriodDao.create(departmentReportPeriod);
    }

    @Override
    public void create(DepartmentReportPeriod departmentReportPeriod, List<Integer> departmentIds) {
        Assert.notEmpty(departmentIds);
        LOG.info(String.format("DepartmentReportPeriodServiceImpl.create. departmentReportPeriod: %s; departmentIds: %s", departmentReportPeriod, departmentIds));
        departmentReportPeriodDao.create(departmentReportPeriod, departmentIds);
    }

    @Override
    public void create(final List<DepartmentReportPeriod> departmentReportPeriods, final Integer departmentId) {
        Assert.notEmpty(departmentReportPeriods);
        LOG.info(String.format("DepartmentReportPeriodServiceImpl.create. departmentReportPeriods.size: %s; departmentId: %s", departmentReportPeriods.size(), departmentId));
        departmentReportPeriodDao.create(departmentReportPeriods, departmentId);
    }

    @Override
    public void updateActive(int id, boolean active) {
        LOG.info(String.format("DepartmentReportPeriodServiceImpl.updateActive. id: %s; active: %s", id, active));
        departmentReportPeriodDao.updateActive(id, active);
    }

    @Override
    public void updateActive(List<Integer> ids, Integer reportPeriodId, boolean active) {
        LOG.info(String.format("DepartmentReportPeriodServiceImpl.updateActive. ids: %s; reportPeriodId: %s; active: %s", ids, reportPeriodId, active));
        if (ids == null || ids.isEmpty()) {
            throw new ServiceException(ERROR_BATCH_MESSAGE);
        }
        departmentReportPeriodDao.updateActive(ids, reportPeriodId, active);
    }

    @Override
    public void delete(List<Integer> ids) {
        LOG.info(String.format("DepartmentReportPeriodServiceImpl.delete. ids: %s", ids));
        departmentReportPeriodDao.delete(ids);
    }

    @Override
    public boolean isExistsByReportPeriodIdAndDepartmentId(int departmentId, int reportPeriodId) {
        return departmentReportPeriodDao.isExistsByReportPeriodIdAndDepartmentId(departmentId, reportPeriodId);
    }

    @Override
    public boolean isExistsByReportPeriodId(int reportPeriodId) {
        return departmentReportPeriodDao.isExistsByReportPeriodId(reportPeriodId);
    }

    @Override
    public DepartmentReportPeriod fetchLast(int departmentId, int reportPeriodId) {
        return departmentReportPeriodDao.fetchLast(departmentId, reportPeriodId);
    }

    @Override
    public boolean isLaterCorrectionPeriodExists(DepartmentReportPeriod departmentReportPeriod) {
        return departmentReportPeriodDao.isLaterCorrectionPeriodExists(departmentReportPeriod);
    }

    @Override
    public List<DepartmentReportPeriodJournalItem> fetchJournalItemByFilter(DepartmentReportPeriodFilter filter) {
        return departmentReportPeriodDao.fetchJournalItemByFilter(filter);
    }

    @Override
    public String checkHasNotAccepted(Integer id) {
        Logger logger = new Logger();

        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.fetchOne(id);
        if (departmentReportPeriod == null) {
            throw new ServiceException("Ошибка загрузки отчтетного периода подразделения с id " + id +
                    ". Период не существует или не найден.");
        }

        List<Integer> departments = departmentService.getAllChildrenIds(departmentReportPeriod.getDepartmentId());

        DeclarationDataFilter dataFilter = new DeclarationDataFilter();
        dataFilter.setDepartmentIds(departments);
        dataFilter.setReportPeriodIds(Arrays.asList(departmentReportPeriod.getReportPeriod().getId()));
        dataFilter.setFormState(State.CREATED);
        if (departmentReportPeriod.getCorrectionDate() != null) {
            dataFilter.setCorrectionTag(true);
            dataFilter.setCorrectionDate(departmentReportPeriod.getCorrectionDate());
        } else {
            dataFilter.setCorrectionTag(false);
        }

        List<DeclarationData> declarations = declarationDataService.getDeclarationData(dataFilter, DeclarationDataSearchOrdering.ID, false);
        dataFilter.setFormState(State.PREPARED);
        declarations.addAll(declarationDataService.getDeclarationData(dataFilter, DeclarationDataSearchOrdering.ID, false));
        for (DeclarationData dd : declarations) {
            DeclarationTemplate template = declarationTemplateService.get(dd.getDeclarationTemplateId());
            String msg = MessageGenerator.getFDMsg("Форма находится в состоянии отличном от \"Принята\":", template.getType().getName(), template.getDeclarationFormKind().getTitle(), departmentService.getDepartment(dd.getDepartmentId()).getName(),
                    null, dd.getManuallyCreated(), departmentReportPeriod.getReportPeriod().getName() + " " + departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                    departmentReportPeriod.getCorrectionDate(), null);
            logger.warn(msg);
        }

        return logEntryService.save(logger.getEntries());

    }

    @Override
    public DepartmentReportPeriod fetchOne(int id) {
        return departmentReportPeriodDao.fetchOne(id);
    }

    @Override
    public String checkHasBlockedDeclaration(Integer id) {
        Logger logger = new Logger();

        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.fetchOne(id);
        if (departmentReportPeriod == null) {
            throw new ServiceException("Ошибка загрузки отчетного периода подразделения с id " + id +
                    ". Период не существует или не найден.");
        }

        List<Integer> departments = departmentService.getAllChildrenIds(departmentReportPeriod.getDepartmentId());

        DeclarationDataFilter dataFilter = new DeclarationDataFilter();
        dataFilter.setDepartmentIds(departments);
        dataFilter.setReportPeriodIds(Arrays.asList(departmentReportPeriod.getReportPeriod().getId()));

        if (departmentReportPeriod.getCorrectionDate() != null) {
            dataFilter.setCorrectionTag(true);
            dataFilter.setCorrectionDate(departmentReportPeriod.getCorrectionDate());
        } else {
            dataFilter.setCorrectionTag(false);
        }

        List<DeclarationData> declarations = declarationDataService.getDeclarationData(dataFilter, DeclarationDataSearchOrdering.ID, false);

        Map<String, DeclarationData> keysBlocker = new HashMap<>(declarations.size());
        for (DeclarationData declarationData : declarations) {
            keysBlocker.put("DECLARATION_DATA_" + declarationData.getId(), declarationData);
        }
        List<LockDataItem> lockDataItems = new ArrayList<>();

        if (keysBlocker.size() > 0) {
            lockDataItems = lockDataService.fetchAllByKeySet(keysBlocker.keySet());
        }

        for (LockDataItem lockDataItem : lockDataItems) {
            DeclarationData dd = keysBlocker.get(lockDataItem.getKey());
            DeclarationTemplate template = declarationTemplateService.get(dd.getDeclarationTemplateId());
            String msg = MessageGenerator.getFDMsg("Форма № " + dd.getId() + " редактируется пользователем " + lockDataItem.getUser() + " ", template.getType().getName(), template.getDeclarationFormKind().getTitle(), departmentService.getDepartment(dd.getDepartmentId()).getName(),
                    null, dd.getManuallyCreated(), departmentReportPeriod.getReportPeriod().getName() + " " + departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                    departmentReportPeriod.getCorrectionDate(), null);
            logger.error(msg);
        }
        return logEntryService.save(logger.getEntries());
    }

}