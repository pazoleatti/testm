package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.permissions.DepartmentReportPeriodPermission;
import com.aplana.sbrf.taxaccounting.permissions.DepartmentReportPeriodPermissionSetter;
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
	private DepartmentReportPeriodPermissionSetter departmentReportPeriodPermissionSetter;
	@Autowired
	private NotificationService notificationService;
	@Autowired
	private LockDataService lockDataService;


    @Override
    public List<DepartmentReportPeriod> getListByFilter(DepartmentReportPeriodFilter departmentReportPeriodFilter) {
		try {
			return departmentReportPeriodDao.getListByFilter(departmentReportPeriodFilter);
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			LOG.error(COMMON_ERROR_MESSAGE, e);
			throw new ServiceException(COMMON_ERROR_MESSAGE, e);
		}
    }

    @Override
    public List<Integer> getListIdsByFilter(DepartmentReportPeriodFilter departmentReportPeriodFilter) {
		try {
        	return departmentReportPeriodDao.getListIdsByFilter(departmentReportPeriodFilter);
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			LOG.error(COMMON_ERROR_MESSAGE, e);
			throw new ServiceException(COMMON_ERROR_MESSAGE, e);
		}
    }

    @Override
    public DepartmentReportPeriod save(DepartmentReportPeriod departmentReportPeriod) {
		try {
        	return departmentReportPeriodDao.save(departmentReportPeriod);
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			LOG.error(COMMON_ERROR_MESSAGE, e);
			throw new ServiceException(COMMON_ERROR_MESSAGE, e);
		}
    }

	@Override
	public void save(DepartmentReportPeriod departmentReportPeriod, List<Integer> departmentIds) {
		if (departmentIds == null || departmentIds.isEmpty())
			throw new ServiceException(ERROR_BATCH_MESSAGE);
		try {
			departmentReportPeriodDao.save(departmentReportPeriod, departmentIds);
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
    public void updateActive(List<Integer> ids, Integer report_period_id, boolean active) {
        if (ids == null || ids.isEmpty())
            throw new ServiceException(ERROR_BATCH_MESSAGE);
		try {
			departmentReportPeriodDao.updateActive(ids, report_period_id, active);
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			LOG.error(COMMON_ERROR_MESSAGE, e);
			throw new ServiceException(COMMON_ERROR_MESSAGE, e);
		}
    }

    @Override
    public void updateCorrectionDate(int id, Date correctionDate) {
		try {
			departmentReportPeriodDao.updateCorrectionDate(id, correctionDate);
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			LOG.error(COMMON_ERROR_MESSAGE, e);
			throw new ServiceException(COMMON_ERROR_MESSAGE, e);
		}
    }

    @Override
    public void delete(int id) {
		try {
			departmentReportPeriodDao.delete(id);
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
			for(Integer id : ids) {
				departmentReportPeriodDao.delete(id);
			}
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			LOG.error(COMMON_ERROR_MESSAGE, e);
			throw new ServiceException(COMMON_ERROR_MESSAGE, e);
		}
    }

    @Override
    public boolean existForDepartment(int departmentId, int reportPeriodId) {
		try {
			return departmentReportPeriodDao.existForDepartment(departmentId, reportPeriodId);
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			LOG.error(COMMON_ERROR_MESSAGE, e);
			throw new ServiceException(COMMON_ERROR_MESSAGE, e);
		}
    }

    @Override
    public Integer getCorrectionNumber(int id) {
		try {
			return departmentReportPeriodDao.getCorrectionNumber(id);
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			LOG.error(COMMON_ERROR_MESSAGE, e);
			throw new ServiceException(COMMON_ERROR_MESSAGE, e);
		}
    }

    @Override
    public DepartmentReportPeriod getLast(int departmentId, int reportPeriodId) {
		try {
			return departmentReportPeriodDao.getLast(departmentId, reportPeriodId);
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			LOG.error(COMMON_ERROR_MESSAGE, e);
			throw new ServiceException(COMMON_ERROR_MESSAGE, e);
		}
    }

    @Override
    public DepartmentReportPeriod getFirst(int departmentId, int reportPeriodId) {
        try {
            return departmentReportPeriodDao.getFirst(departmentId, reportPeriodId);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
			LOG.error(COMMON_ERROR_MESSAGE, e);
            throw new ServiceException(COMMON_ERROR_MESSAGE, e);
        }
    }

    @Override
    public boolean existLargeCorrection(int departmentId, int reportPeriodId, Date correctionDate) {
		try {
			return departmentReportPeriodDao.existLargeCorrection(departmentId, reportPeriodId, correctionDate);
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			LOG.error(COMMON_ERROR_MESSAGE, e);
			throw new ServiceException(COMMON_ERROR_MESSAGE, e);
		}
    }

	@Override
	public List<DepartmentReportPeriodJournalItem> findAll(DepartmentReportPeriodFilter filter) {
		List<DepartmentReportPeriodJournalItem> page =  departmentReportPeriodDao.findAll(filter);
		for (DepartmentReportPeriodJournalItem item : page){
			DepartmentReportPeriod period = new DepartmentReportPeriod();
			period.setIsActive(item.getIsActive());
			departmentReportPeriodPermissionSetter.setPermissions(period, DepartmentReportPeriodPermission.EDIT, DepartmentReportPeriodPermission.OPEN,
					DepartmentReportPeriodPermission.DELETE, DepartmentReportPeriodPermission.CLOSE, DepartmentReportPeriodPermission.OPEN_CORRECT,
					DepartmentReportPeriodPermission.DEADLINE);
			item.setPermissions(period.getPermissions());
			Notification notification = notificationService.get(item.getReportPeriodId(), null, item.getDepartmentId());
			if (notification != null) {
				item.setDeadline(notification.getDeadline());
			}
		}
		return page;
	}

	@Override
	public String checkHasNotAccepted(Integer id) {
		Logger logger = new Logger();

		DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.get(id);
		if (departmentReportPeriod == null){
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
			String msg = "Налоговая форма: №: " +
					dd.getId() + ", Вид: " +
					"\"" + declarationTemplateService.get(dd.getDeclarationTemplateId()).getType().getName() + "\"" +
					", Подразделение: " +
					"\"" + departmentService.getDepartment(dd.getDepartmentId()).getName() + "\"" +
					", находится в состоянии отличном от \"Принята\"";

			logger.warn(msg);
		}

		return logEntryService.save(logger.getEntries());

	}

	@Override
	public DepartmentReportPeriod get(int id) {
		return departmentReportPeriodDao.get(id);
	}

	@Override
	public String checkHasBlockedDeclaration(Integer id){
		Logger logger = new Logger();

		DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.get(id);
		if (departmentReportPeriod == null){
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
		for (DeclarationData declarationData: declarations){
			keysBlocker.put("DECLARATION_DATA_" + declarationData.getId(), declarationData);
		}
		List<LockDataItem> lockDataItems =  lockDataService.getLocksByKeySet(keysBlocker.keySet());

		for (LockDataItem lockDataItem : lockDataItems){
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