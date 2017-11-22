package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class DepartmentReportPeriodServiceImpl implements DepartmentReportPeriodService {

	private static final Log LOG = LogFactory.getLog(DepartmentReportPeriodServiceImpl.class);

    private final static String ERROR_BATCH_MESSAGE = "Пустой список отчетных периодов";
	private final static String COMMON_ERROR_MESSAGE = "Ошибка при выполнении операции с отчетными периодами подразделения";

	@Autowired
    private DepartmentReportPeriodDao departmentReportPeriodDao;

    @Override
    public DepartmentReportPeriod get(int id) {
		try {
			return departmentReportPeriodDao.get(id);
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			LOG.error(COMMON_ERROR_MESSAGE, e);
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
    public int save(DepartmentReportPeriod departmentReportPeriod) {
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
			departmentReportPeriodDao.delete(ids);
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
}