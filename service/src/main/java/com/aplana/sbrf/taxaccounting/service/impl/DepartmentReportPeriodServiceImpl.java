package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.permissions.DepartmentReportPeriodPermission;
import com.aplana.sbrf.taxaccounting.permissions.DepartmentReportPeriodPermissionSetter;
import com.aplana.sbrf.taxaccounting.service.*;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class DepartmentReportPeriodServiceImpl implements DepartmentReportPeriodService {

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

	@Override
    public DepartmentReportPeriod get(int id) {
		try {
			return departmentReportPeriodDao.get(id);
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new ServiceException(COMMON_ERROR_MESSAGE, e);
		}
    }

    @Override
    public List<DepartmentReportPeriod> getListByFilter(DepartmentReportPeriodFilter departmentReportPeriodFilter) {
		try {
			return departmentReportPeriodDao.getListByFilter(departmentReportPeriodFilter);
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new ServiceException(COMMON_ERROR_MESSAGE, e);
		}
    }

    @Override
    public List<Long> getListIdsByFilter(DepartmentReportPeriodFilter departmentReportPeriodFilter) {
		try {
        	return departmentReportPeriodDao.getListIdsByFilter(departmentReportPeriodFilter);
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
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
			throw new ServiceException(COMMON_ERROR_MESSAGE, e);
		}
	}

    @Override
    public void updateActive(long id, boolean active) {
		try {
			departmentReportPeriodDao.updateActive(id, active);
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new ServiceException(COMMON_ERROR_MESSAGE, e);
		}
    }

    @Override
    public void updateActive(List<Long> ids, Integer report_period_id, boolean active) {
        if (ids == null || ids.isEmpty())
            throw new ServiceException(ERROR_BATCH_MESSAGE);
		try {
			departmentReportPeriodDao.updateActive(ids, report_period_id, active);
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new ServiceException(COMMON_ERROR_MESSAGE, e);
		}
    }

    @Override
    public void updateCorrectionDate(Long id, LocalDateTime correctionDate) {
		try {
			departmentReportPeriodDao.updateCorrectionDate(id, correctionDate);
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new ServiceException(COMMON_ERROR_MESSAGE, e);
		}
    }

    @Override
    public void delete(long id) {
		try {
			departmentReportPeriodDao.delete(id);
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new ServiceException(COMMON_ERROR_MESSAGE, e);
		}
    }

    @Override
    public void delete(List<Long> ids) {
		try {
			for(Long id : ids) {
				departmentReportPeriodDao.delete(id);
			}
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
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
            throw new ServiceException(COMMON_ERROR_MESSAGE, e);
        }
    }

    @Override
    public boolean existLargeCorrection(int departmentId, int reportPeriodId, LocalDateTime correctionDate) {
		try {
			return departmentReportPeriodDao.existLargeCorrection(departmentId, reportPeriodId, correctionDate);
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new ServiceException(COMMON_ERROR_MESSAGE, e);
		}
    }

	@Override
	public PagingResult<DepartmentReportPeriodJournalItem> findAll(DepartmentReportPeriodFilter filter, PagingParams pagingParams) {
		PagingResult<DepartmentReportPeriodJournalItem> page =  departmentReportPeriodDao.findAll(filter, pagingParams);
		for (DepartmentReportPeriodJournalItem item : page){
			DepartmentReportPeriod period = new DepartmentReportPeriod();
			if (item.getIsActive() == 1){
				period.setActive(true);
			}else {
				period.setActive(false);
			}
			departmentReportPeriodPermissionSetter.setPermissions(period, DepartmentReportPeriodPermission.EDIT, DepartmentReportPeriodPermission.OPEN,
					DepartmentReportPeriodPermission.DELETE, DepartmentReportPeriodPermission.CLOSE, DepartmentReportPeriodPermission.OPEN_CORRECT,
					DepartmentReportPeriodPermission.DEADLINE);
			item.setPermissions(period.getPermissions());
		}
		return page;
	}

	@Override
	public String checkHasNotAccepted(Long id) {
		Logger logger = new Logger();

		DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.findOne(id);
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
			dataFilter.setCorrectionDate(departmentReportPeriod.getCorrectionDate().toDate());
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
	public DepartmentReportPeriod findOne(Long departmentRPId) {
		return departmentReportPeriodDao.findOne(departmentRPId);
	}


}