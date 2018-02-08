package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.service.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class DepartmentReportPeriodServiceImpl implements DepartmentReportPeriodService {

    /**
     * Логгер для {@link DepartmentReportPeriodServiceImpl}
     */
    private static final Log LOG = LogFactory.getLog(DepartmentReportPeriodServiceImpl.class);

    private final static String ERROR_BATCH_MESSAGE = "Пустой список отчетных периодов";
    private final static String COMMON_ERROR_MESSAGE = "Ошибка при выполнении операции с отчетными периодами подразделения";

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
    private NotificationService notificationService;
    @Autowired
    private LockDataService lockDataService;

    @Override
    public List<DepartmentReportPeriod> fetchAllByFilter(DepartmentReportPeriodFilter departmentReportPeriodFilter) {
        try {
            return departmentReportPeriodDao.fetchAllByFilter(departmentReportPeriodFilter);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(COMMON_ERROR_MESSAGE, e);
            throw new ServiceException(COMMON_ERROR_MESSAGE, e);
        }
    }

    @Override
    public List<Integer> fetchAllIdsByFilter(DepartmentReportPeriodFilter departmentReportPeriodFilter) {
        try {
            return departmentReportPeriodDao.fetchAllIdsByFilter(departmentReportPeriodFilter);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(COMMON_ERROR_MESSAGE, e);
            throw new ServiceException(COMMON_ERROR_MESSAGE, e);
        }
    }

    @Override
    public void create(DepartmentReportPeriod departmentReportPeriod) {
        try {
            departmentReportPeriodDao.create(departmentReportPeriod);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(COMMON_ERROR_MESSAGE, e);
            throw new ServiceException(COMMON_ERROR_MESSAGE, e);
        }
    }

    @Override
    public void create(DepartmentReportPeriod departmentReportPeriod, List<Integer> departmentIds) {
        if (departmentIds == null || departmentIds.isEmpty())
            throw new ServiceException(ERROR_BATCH_MESSAGE);
        try {
            for (Integer departmentId : departmentIds) {
                departmentReportPeriod.setDepartmentId(departmentId);
                departmentReportPeriodDao.create(departmentReportPeriod);
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(COMMON_ERROR_MESSAGE, e);
            throw new ServiceException(COMMON_ERROR_MESSAGE, e);
        }
    }

    @Override
    public void updateActive(int id, boolean active) {
        try {
            departmentReportPeriodDao.updateActive(id, active);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(COMMON_ERROR_MESSAGE, e);
            throw new ServiceException(COMMON_ERROR_MESSAGE, e);
        }
    }

    @Override
    public void updateActive(List<Integer> ids, Integer reportPeriodId, boolean active) {
        if (ids == null || ids.isEmpty())
            throw new ServiceException(ERROR_BATCH_MESSAGE);
        try {
            departmentReportPeriodDao.updateActive(ids, reportPeriodId, active);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(COMMON_ERROR_MESSAGE, e);
            throw new ServiceException(COMMON_ERROR_MESSAGE, e);
        }
    }

    @Override
    public void delete(List<Integer> ids) {
        try {
            departmentReportPeriodDao.delete(ids);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(COMMON_ERROR_MESSAGE, e);
            throw new ServiceException(COMMON_ERROR_MESSAGE, e);
        }
    }

    @Override
    public boolean checkExistForDepartment(int departmentId, int reportPeriodId) {
        try {
            return departmentReportPeriodDao.checkExistForDepartment(departmentId, reportPeriodId);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(COMMON_ERROR_MESSAGE, e);
            throw new ServiceException(COMMON_ERROR_MESSAGE, e);
        }
    }

    @Override
    public DepartmentReportPeriod fetchLast(int departmentId, int reportPeriodId) {
        try {
            return departmentReportPeriodDao.fetchLast(departmentId, reportPeriodId);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(COMMON_ERROR_MESSAGE, e);
            throw new ServiceException(COMMON_ERROR_MESSAGE, e);
        }
    }

    @Override
    public DepartmentReportPeriod fetchFirst(int departmentId, int reportPeriodId) {
        try {
            return departmentReportPeriodDao.fetchFirst(departmentId, reportPeriodId);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(COMMON_ERROR_MESSAGE, e);
            throw new ServiceException(COMMON_ERROR_MESSAGE, e);
        }
    }

    @Override
    public boolean checkExistLargeCorrection(int departmentId, int reportPeriodId, Date correctionDate) {
        try {
            return departmentReportPeriodDao.checkExistLargeCorrection(departmentId, reportPeriodId, correctionDate);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(COMMON_ERROR_MESSAGE, e);
            throw new ServiceException(COMMON_ERROR_MESSAGE, e);
        }
    }

    @Override
    public List<DepartmentReportPeriodJournalItem> fetchJournalItemByFilter(DepartmentReportPeriodFilter filter) {
        List<DepartmentReportPeriodJournalItem> page = departmentReportPeriodDao.fetchJournalItemByFilter(filter);
        for (DepartmentReportPeriodJournalItem item : page) {
            Notification notification = notificationService.fetchOne(item.getReportPeriodId(), null, item.getDepartmentId());
            if (notification != null) {
                item.setDeadline(notification.getDeadline());
            }
        }
        return page;
    }

    @Override
    public String checkHasNotAccepted(Integer id) {
        Logger logger = new Logger();

        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.fetchOne(id);
        if (departmentReportPeriod == null) {
            throw new ServiceException(COMMON_ERROR_MESSAGE, "Ошибка загрузки отчтетного периода подразделения с id " + id +
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
            throw new ServiceException(COMMON_ERROR_MESSAGE, "Ошибка загрузки отчтетного периода подразделения с id " + id +
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

        if (keysBlocker.size() > 0){
            lockDataItems = lockDataService.fetchAllByKeySet(keysBlocker.keySet());
        }

        for (LockDataItem lockDataItem : lockDataItems) {
            DeclarationData dd = keysBlocker.get(lockDataItem.getKey());
            String msg = "Налоговая форма: №: " +
                    dd.getId() + ", Вид: " +
                    "\"" + declarationTemplateService.get(dd.getDeclarationTemplateId()).getType().getName() + "\"" +
                    ", Подразделение: " +
                    "\"" + departmentService.getDepartment(dd.getDepartmentId()).getName() + "\"" +
                    ", редактируется пользователем " + lockDataItem.getUser();

            logger.error(msg);
        }
        return logEntryService.save(logger.getEntries());
    }

}