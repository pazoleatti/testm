package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.DepartmentFormTypeService;
import com.aplana.sbrf.taxaccounting.service.ReportPeriodService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ReportPeriodServiceImpl implements ReportPeriodService{

	@Autowired
	private ReportPeriodDao reportPeriodDao;

	@Autowired
	private TaxPeriodDao taxPeriodDao;

	@Autowired
	private DepartmentReportPeriodDao departmentReportPeriodDao;

	@Autowired
	private RefBookDao refBookDao;

	@Autowired
	private RefBookFactory rbFactory;

	@Autowired
	private DepartmentFormTypeService departmentFormTypeService;

	@Override
	public ReportPeriod get(int reportPeriodId) {
		return reportPeriodDao.get(reportPeriodId);
	}

	@Override
	public List<ReportPeriod> listByTaxPeriod(int taxPeriodId) {
		return reportPeriodDao.listByTaxPeriod(taxPeriodId);
	}

	@Override
	public List<ReportPeriod> listByTaxPeriodAndDepartment(int taxPeriodId, long departmentId) {
		return reportPeriodDao.listByTaxPeriodAndDepartmentId(taxPeriodId, departmentId);
	}

	@Override
	public void closePeriod(int reportPeriodId) {
		reportPeriodDao.changeActive(reportPeriodId, false);
	}

	@Override
	public void openPeriod(int reportPeriodId) {
		reportPeriodDao.changeActive(reportPeriodId, true);
	}

	@Override
	public int add(ReportPeriod reportPeriod) {
		return reportPeriodDao.add(reportPeriod);
	}

    @Override
    public ReportPeriod getLastReportPeriod(TaxType taxType, long departmentId) {
        return reportPeriodDao.getLastReportPeriod(taxType, departmentId);
    }

	@Override
	public boolean checkOpened(int reportPeriodId, long departmentId) {
		// TODO
		throw new UnsupportedOperationException("Не реализован метод проверки открытости периода TODO");
	}

	@Override
	public void open(ReportPeriod reportPeriod, int year, int dictionaryTaxPeriodId, TaxType taxType, TAUserInfo user, long departmentId) {
		Calendar date = Calendar.getInstance();
		date.set(Calendar.YEAR, year);
		date.set(Calendar.MONTH, Calendar.JANUARY);
		date.set(Calendar.DAY_OF_MONTH, 1);
//		TaxType taxType = taxPeriodDao.get(reportPeriod.getTaxPeriodId()).getTaxType();
		List<TaxPeriod> taxPeriodList = taxPeriodDao.listByTaxTypeAndDate(taxType, date.getTime(), date.getTime());
		if (taxPeriodList.size() > 1) {
			throw new ServiceException("Слишком много TaxPeriod'ов"); //TODO
		}

		TaxPeriod taxPeriod;

		if (taxPeriodList.isEmpty()) {
			taxPeriod = new TaxPeriod();
			taxPeriod.setStartDate(date.getTime());
			date.set(Calendar.MONTH, Calendar.DECEMBER);
			date.set(Calendar.DAY_OF_MONTH, 31);
			taxPeriod.setEndDate(date.getTime());
			taxPeriod.setTaxType(taxType);
		} else {
			taxPeriod = taxPeriodList.get(0);
		}

		List<ReportPeriod> reportPeriods = listByTaxPeriod(taxPeriod.getId());
		for (ReportPeriod rp : reportPeriods) {
			if (rp.getDictTaxPeriodId() != dictionaryTaxPeriodId) {
				reportPeriods.remove(rp); //TODO
			}
		}

		ReportPeriod newReportPeriod;
		if (reportPeriods.isEmpty()) {
			RefBookDataProvider provider = rbFactory.getDataProvider(8L);
			Map<String, RefBookValue> record = provider.getRecordData(Long.valueOf(dictionaryTaxPeriodId));
			newReportPeriod = new ReportPeriod();
			newReportPeriod.setTaxPeriodId(taxPeriod.getId());
			newReportPeriod.setDictTaxPeriodId(dictionaryTaxPeriodId);
			newReportPeriod.setName(record.get("NAME").getStringValue());
			newReportPeriod.setOrder(record.get("ORD").getNumberValue().intValue());
			newReportPeriod.setMonths(4);//TODO заполнять из справочника

		} else {
			newReportPeriod = reportPeriods.get(0); //TODO
		}

		if (user.getUser().getDepartmentId() == departmentId) {
			DepartmentReportPeriod departmentReportPeriod = new DepartmentReportPeriod();
			departmentReportPeriod.setActive(true); //TODO
			departmentReportPeriod.setBalance(false); //TODO
			departmentReportPeriod.setDepartmentId(Long.valueOf(user.getUser().getDepartmentId()));
			departmentReportPeriod.setReportPeriod(newReportPeriod);
		}

	}

	@Override
	public List<DepartmentReportPeriod> listByDepartmentId(long departmentId) {
		return departmentReportPeriodDao.getByDepartment(departmentId);
	}

	private static ReportPeriod findPeriodInListByPeriodDict(List<ReportPeriod> reportPeriods, int dictionaryTaxPeriodId) {
		for (ReportPeriod reportPeriod : reportPeriods) {
			if (reportPeriod.getDictTaxPeriodId() == dictionaryTaxPeriodId) {
				return reportPeriod;
			}
		}
		return null;
	}
}
