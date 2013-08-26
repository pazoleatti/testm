package com.aplana.sbrf.taxaccounting.service.impl;

import java.util.*;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.DepartmentFormTypeService;
import com.aplana.sbrf.taxaccounting.service.ReportPeriodService;

/**
 * Сервис работы с периодами
 * 
 * Только этот сервис должен использоваться для работы с отчетными и налоговыми периодами
 *
 */
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

	@Autowired
	private DepartmentService departmentService;

	@Override
	public ReportPeriod get(int reportPeriodId) {
		return reportPeriodDao.get(reportPeriodId);
	}

	@Override
	public List<ReportPeriod> listByTaxPeriod(int taxPeriodId) {
		return reportPeriodDao.listByTaxPeriod(taxPeriodId);
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
    public DepartmentReportPeriod getLastReportPeriod(TaxType taxType, long departmentId) {
    	// TODO: Нужно получить последний открытый для этого подразделения.
    	return null;
    }

	@Override
	public boolean checkOpened(int reportPeriodId, long departmentId) {
		// TODO
		throw new UnsupportedOperationException("Не реализован метод проверки открытости периода TODO");
	}

	@Override
	public void open(int year, int dictionaryTaxPeriodId, TaxType taxType, TAUserInfo user, long departmentId) {
		Calendar from = Calendar.getInstance();
		from.set(Calendar.YEAR, year);
		from.set(Calendar.MONTH, Calendar.JANUARY);
		from.set(Calendar.DAY_OF_MONTH, 1);

		Calendar to = Calendar.getInstance();
		to.set(Calendar.YEAR, year);
		to.set(Calendar.MONTH, Calendar.DECEMBER);
		to.set(Calendar.DAY_OF_MONTH, 31);

		List<TaxPeriod> taxPeriodList = taxPeriodDao.listByTaxTypeAndDate(taxType, from.getTime(), to.getTime());
		if (taxPeriodList.size() > 1) {
			throw new ServiceException("Слишком много TaxPeriod'ов"); //TODO
		}

		TaxPeriod taxPeriod;

		if (taxPeriodList.isEmpty()) {
			taxPeriod = new TaxPeriod();
			taxPeriod.setStartDate(from.getTime());
			taxPeriod.setEndDate(to.getTime());
			taxPeriod.setTaxType(taxType);
		} else {
			taxPeriod = taxPeriodList.get(0);
		}


		List<ReportPeriod> reportPeriods = listByTaxPeriod(taxPeriod.getId());
		if (!reportPeriods.isEmpty()) {
			Iterator<ReportPeriod> it = reportPeriods.iterator();
			while (it.hasNext()) {
				if (it.next().getDictTaxPeriodId() != dictionaryTaxPeriodId) {
					it.remove();
				}
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
			newReportPeriod.setOrder(4);
			newReportPeriod.setMonths(4);//TODO заполнять из справочника
			reportPeriodDao.add(newReportPeriod);

		} else {
			newReportPeriod = reportPeriods.get(0);
		}

		if ((taxType == TaxType.INCOME) || (taxType == TaxType.VAT)) {
			// Сохраняем для всех
			for(Department dep : departmentService.listAll()) {

				DepartmentReportPeriod depRP = new DepartmentReportPeriod();
				depRP.setReportPeriod(newReportPeriod);
				depRP.setDepartmentId(Long.valueOf(dep.getId()));
				depRP.setActive(true);
				saveOrUpdate(depRP);
			}
		} else {
			// Сохраняем для пользователя
			DepartmentReportPeriod departmentReportPeriod = new DepartmentReportPeriod();
			departmentReportPeriod.setActive(true); //TODO
			departmentReportPeriod.setBalance(false); //TODO
			departmentReportPeriod.setDepartmentId(Long.valueOf(user.getUser().getDepartmentId()));
			departmentReportPeriod.setReportPeriod(newReportPeriod);
			saveOrUpdate(departmentReportPeriod);

			// Сохраняем для источников
			List<DepartmentFormType> departmentFormTypes = new ArrayList<DepartmentFormType>();
			departmentFormTypes.addAll(departmentFormTypeService.getDepartmentFormSources((int) departmentId, taxType));

			for (DepartmentFormType dft : departmentFormTypes) {
				DepartmentReportPeriod depRP = new DepartmentReportPeriod();
				depRP.setReportPeriod(newReportPeriod);
				depRP.setDepartmentId(Long.valueOf(dft.getDepartmentId()));
				depRP.setActive(true);
				saveOrUpdate(depRP);
			}

		}
	}

	private void saveOrUpdate(DepartmentReportPeriod departmentReportPeriod) {
		if (departmentReportPeriodDao.get(departmentReportPeriod.getReportPeriod().getId(),
				departmentReportPeriod.getDepartmentId()) == null) {

			departmentReportPeriodDao.save(departmentReportPeriod);
		} else {
			departmentReportPeriodDao.updateActive(departmentReportPeriod.getReportPeriod().getId(),
				departmentReportPeriod.getDepartmentId(), true);
		}
	}

	@Override
	public List<DepartmentReportPeriod> listByDepartmentId(long departmentId) {
		return departmentReportPeriodDao.getByDepartment(departmentId);
	}

	@Override
	public List<TaxPeriod> listByTaxType(TaxType taxType) {
		return taxPeriodDao.listByTaxType(taxType);
	}

	@Override
	public TaxPeriod getTaxPeriod(int taxPeriodId) {
		return taxPeriodDao.get(taxPeriodId);
	}
	
    public Calendar getStartDate(int reportPeriodId){
        ReportPeriod reportPeriod = reportPeriodDao.get(reportPeriodId);
        TaxPeriod taxPeriod = taxPeriodDao.get(reportPeriod.getTaxPeriodId());
        // календарь
        Calendar cal = Calendar.getInstance();
        cal.setTime(taxPeriod.getStartDate());

        // для налога на прибыль, периоды вложены в друг дгруга, и начало всегда совпадает
        if (taxPeriod.getTaxType() != TaxType.INCOME){
            // получим отчетные периоды для данного налогового периода
            List<ReportPeriod> reportPeriodList = reportPeriodDao.listByTaxPeriod(reportPeriod.getTaxPeriodId());
            // смещение относительно налогового периода
            int months = 0;
            for (ReportPeriod cReportPeriod: reportPeriodList){
                // если достигли текущего то выходим из цикла
                if (cReportPeriod.getId() == reportPeriod.getId()){
                    break;
                }
                // смещение в месяцах
                months += cReportPeriod.getMonths();
            }
            cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + months);
        }

        return cal;
    }
}
