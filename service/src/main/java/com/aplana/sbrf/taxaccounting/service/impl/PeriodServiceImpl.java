package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Сервис работы с периодами
 * 
 * Только этот сервис должен использоваться для работы с отчетными и налоговыми периодами
 *
 */
@Service
@Transactional
public class PeriodServiceImpl implements PeriodService{
	
	private static final Long PERIOD_CODE_REFBOOK = 8L;

	@Autowired
	private ReportPeriodDao reportPeriodDao;

	@Autowired
	private TaxPeriodDao taxPeriodDao;

	@Autowired
	private DepartmentReportPeriodDao departmentReportPeriodDao;

	@Autowired
	private RefBookFactory rbFactory;

	@Autowired
	private SourceService departmentFormTypeService;

	@Autowired
	private DepartmentService departmentService;

	@Override
	public List<ReportPeriod> listByTaxPeriod(int taxPeriodId) {
		return reportPeriodDao.listByTaxPeriod(taxPeriodId);
	}

    @Override
    public DepartmentReportPeriod getLastReportPeriod(TaxType taxType, long departmentId) {
    	// TODO: Нужно получить последний открытый для этого подразделения и типа налога.
    	return null;
    }
    
    

	@Override
	public boolean isActivePeriod(int reportPeriodId, long departmentId) {
		DepartmentReportPeriod drp = departmentReportPeriodDao.get(reportPeriodId, departmentId);
		return drp != null && drp.isActive();
	}
	
	@Override
	public boolean isBalancePeriod(int reportPeriodId, long departmentId) {
		DepartmentReportPeriod drp = departmentReportPeriodDao.get(reportPeriodId, departmentId);
		return drp != null && drp.isBalance();
	}

	@Override
	public void open(int year, int dictionaryTaxPeriodId, TaxType taxType, TAUserInfo user,
	                 long departmentId, List<LogEntry> logs, boolean isBalance) {
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
			throw new ServiceException("Слишком много налоговых периодов");
		}

		TaxPeriod taxPeriod;

		if (taxPeriodList.isEmpty()) {
			taxPeriod = new TaxPeriod();
			taxPeriod.setStartDate(from.getTime());
			taxPeriod.setEndDate(to.getTime());
			taxPeriod.setTaxType(taxType);
			taxPeriodDao.add(taxPeriod);
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
			RefBook refBook = rbFactory.get(PERIOD_CODE_REFBOOK);
			RefBookDataProvider provider = rbFactory.getDataProvider(refBook.getId());
			Map<String, RefBookValue> record = provider.getRecordData(Long.valueOf(dictionaryTaxPeriodId));
			newReportPeriod = new ReportPeriod();
			newReportPeriod.setTaxPeriod(taxPeriod);
			newReportPeriod.setDictTaxPeriodId(dictionaryTaxPeriodId);
			
			String name = record.get("NAME").getStringValue();
			Number ord = record.get("ORD").getNumberValue();
			Number months = record.get("MONTHS").getNumberValue();
			if (name == null || name.isEmpty() || ord == null || months == null){
				throw new ServiceException("Не заполнен один из обязательных атрибутов справочника \"" + refBook.getName() + "\"");
			}
			newReportPeriod.setName(name);
			newReportPeriod.setOrder(ord.intValue()); 
			newReportPeriod.setMonths(months.intValue());
			reportPeriodDao.save(newReportPeriod);

		} else {
			newReportPeriod = reportPeriods.get(0);
		}

		if ((taxType == TaxType.INCOME) || (taxType == TaxType.VAT) || (taxType == TaxType.DEAL)) {
			if ((user.getUser().hasRole("ROLE_CONTROL_UNP") || (user.getUser().hasRole("ROLE_CONTROL")))
					&& (user.getUser().getDepartmentId() == Department.ROOT_BANK_ID)
					&& (departmentId == Department.ROOT_BANK_ID)) {
				for(Department dep : departmentService.listAll()) {
					DepartmentReportPeriod depRP = new DepartmentReportPeriod();
					depRP.setReportPeriod(newReportPeriod);
					depRP.setDepartmentId(Long.valueOf(dep.getId()));
					depRP.setActive(true);
					depRP.setBalance(isBalance);
					saveOrUpdate(depRP, logs);
				}
			} else {
				throw new ServiceException("Невозможно открыть период с такими параметрами");
			}
		} else if ((taxType == TaxType.TRANSPORT) || (taxType == TaxType.PROPERTY)) {
			if ((user.getUser().hasRole("ROLE_CONTROL") && (user.getUser().getDepartmentId() == departmentId))
					|| user.getUser().hasRole("ROLE_CONTROL_UNP")) {
				// Сохраняем для выбраного
				DepartmentReportPeriod departmentReportPeriod = new DepartmentReportPeriod();
				departmentReportPeriod.setActive(true);
				departmentReportPeriod.setBalance(isBalance);
				departmentReportPeriod.setDepartmentId(departmentId);
				departmentReportPeriod.setReportPeriod(newReportPeriod);
				saveOrUpdate(departmentReportPeriod, logs);

				// Сохраняем для источников
				List<DepartmentFormType> departmentFormTypes = new ArrayList<DepartmentFormType>();
				departmentFormTypes.addAll(departmentFormTypeService.getDFTSourcesByDepartment((int) departmentId, taxType));

				for (DepartmentFormType dft : departmentFormTypes) {
					DepartmentReportPeriod depRP = new DepartmentReportPeriod();
					depRP.setReportPeriod(newReportPeriod);
					depRP.setDepartmentId(Long.valueOf(dft.getDepartmentId()));
					depRP.setActive(true);
					depRP.setBalance(isBalance);
					saveOrUpdate(depRP, logs);
				}
			} else if (user.getUser().hasRole("ROLE_CONTROL") && (user.getUser().getDepartmentId() != departmentId)) {
				DepartmentReportPeriod departmentReportPeriod = new DepartmentReportPeriod();
				departmentReportPeriod.setActive(true);
				departmentReportPeriod.setBalance(isBalance);
				departmentReportPeriod.setDepartmentId(departmentId);
				departmentReportPeriod.setReportPeriod(newReportPeriod);
				saveOrUpdate(departmentReportPeriod, logs);
			} else {
				throw new ServiceException("Невозможно открыть период с такими параметрами");
			}
		} else {
			throw new ServiceException("Вид налога не поддерживается");
		}

	}

	@Override
	public void close(TaxType taxType, int reportPeriodId, long departmentId, List<LogEntry> logs) {
		if ((taxType == TaxType.INCOME) || (taxType == TaxType.VAT)) {
			for(Department dep : departmentService.listAll()) { //Закрываем для всех
				closePeriodWithLog(reportPeriodId, dep.getId(), logs);
			}
		} else {
			closePeriodWithLog(reportPeriodId, departmentId, logs);
			List<DepartmentFormType> departmentFormTypes = new ArrayList<DepartmentFormType>();
			departmentFormTypes.addAll(departmentFormTypeService.getDFTSourcesByDepartment((int) departmentId, taxType));
			for (DepartmentFormType dft : departmentFormTypes) {
				closePeriodWithLog(reportPeriodId, dft.getDepartmentId(), logs);
			}
		}
	}

	private void closePeriodWithLog(int reportPeriodId, long departmentId, List<LogEntry> logs) {
		DepartmentReportPeriod period = departmentReportPeriodDao.get(reportPeriodId, departmentId);
		if (period != null && period.isActive()) {
			departmentReportPeriodDao.updateActive(reportPeriodId, departmentId, false);
			ReportPeriod reportPeriod = reportPeriodDao.get(reportPeriodId);
			int year = period.getReportPeriod().getYear();
			logs.add(new LogEntry(LogLevel.INFO, "Период" + " \"" + reportPeriod.getName() + "\" " +
					"за " + year + " год " +
					"закрыт для подразделения \"" +
					departmentService.getDepartment((int) departmentId).getName() +
					"\""));
		}
	}

	private void saveOrUpdate(DepartmentReportPeriod departmentReportPeriod, List<LogEntry> logs) {
		DepartmentReportPeriod dp = departmentReportPeriodDao.get(departmentReportPeriod.getReportPeriod().getId(),
				departmentReportPeriod.getDepartmentId());
		if (dp == null) { //не существует
			departmentReportPeriodDao.save(departmentReportPeriod);
		} else if (!dp.isActive()) { // существует и не открыт
			departmentReportPeriodDao.updateActive(departmentReportPeriod.getReportPeriod().getId(),
				departmentReportPeriod.getDepartmentId(), true);
		} else { // уже открыт
			return;
		}
		if (logs != null) {
			int year = departmentReportPeriod.getReportPeriod().getYear();
			logs.add(new LogEntry(LogLevel.INFO,"Создан период" + " \"" + departmentReportPeriod.getReportPeriod().getName() + "\" " +
					" за " + year + " год "
					+ "для подразделения \" " +
					departmentService.getDepartment(departmentReportPeriod.getDepartmentId().intValue()).getName()+ "\""));
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

    /**
     * Возвращает дату начала отчетного периода
     * @param reportPeriodId
     * @return
     */
    @Override
    public Calendar getStartDate(int reportPeriodId){
        ReportPeriod reportPeriod = reportPeriodDao.get(reportPeriodId);
        TaxPeriod taxPeriod = reportPeriod.getTaxPeriod();
        // календарь
        Calendar cal = Calendar.getInstance();
        cal.setTime(taxPeriod.getStartDate());

        // для налога на прибыль, периоды вложены в друг дгруга, и начало всегда совпадает
        // В МУКС только один период
        if (taxPeriod.getTaxType() != TaxType.INCOME && taxPeriod.getTaxType() != TaxType.DEAL &&
                taxPeriod.getTaxType() != TaxType.TRANSPORT){
            // получим отчетные периоды для данного налогового периода
            List<ReportPeriod> reportPeriodList = reportPeriodDao.listByTaxPeriod(reportPeriod.getTaxPeriod().getId());
            // смещение относительно налогового периода
            int months = 0;
            for (ReportPeriod cReportPeriod: reportPeriodList){
                // если достигли текущего то выходим из цикла
                if (cReportPeriod.getId().equals(reportPeriod.getId())){
                    break;
                }
                // смещение в месяцах
                months += cReportPeriod.getMonths();
            }
            cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + months);
        }

        return cal;
    }

    /**
     * Возвращает дату конца отчетного периода
     * @param reportPeriodId
     * @return
     */
    @Override
    public Calendar getEndDate(int reportPeriodId){
        ReportPeriod reportPeriod = reportPeriodDao.get(reportPeriodId);
        TaxPeriod taxPeriod = taxPeriodDao.get(reportPeriod .getTaxPeriod().getId());
        // календарь
        Calendar cal = new GregorianCalendar();
        cal.clear();
        cal.set(Calendar.YEAR, reportPeriod.getYear());

        // для налога на прибыль, периоды вложены в друг дгруга
        if (taxPeriod.getTaxType() == TaxType.INCOME || taxPeriod.getTaxType() == TaxType.DEAL){
            // Calendar.MONTH = 0 это январь
            cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + reportPeriod.getMonths() - 1);
        }
        else{
            // получим отчетные периоды для данного налогового периода
            List<ReportPeriod> reportPeriodList = reportPeriodDao.listByTaxPeriod(reportPeriod.getTaxPeriod().getId());
            // смещение относительно налогового периода
            int months = 0;
            for (int i = 0; i < reportPeriodList.size(); i++) {
                ReportPeriod cReportPeriod = reportPeriodList.get(i);
                // если достигли текущего то выходим из цикла
                if (cReportPeriod.getId().equals(reportPeriod.getId())){
                    months += cReportPeriod.getMonths();
                    break;
                }
                // смещение в месяцах
                months += cReportPeriod.getMonths();
            }
            // Calendar.MONTH = 0 это январь
            cal.set(Calendar.MONTH, months - 1);
        }

        cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        return cal;
    }

    @Override
    public Calendar getReportDate(int reportPeriodId) {
        Calendar cal = getEndDate(reportPeriodId);
        cal.set(Calendar.DATE, cal.get(Calendar.DATE) + 1);
        return cal;
    }

	@Override
	public ReportPeriod getReportPeriod(int reportPeriodId) {
		return reportPeriodDao.get(reportPeriodId);
	}

	@Override
	public TaxPeriod getLastTaxPeriod(TaxType taxType) {
		return taxPeriodDao.getLast(taxType);
	}

	@Override
	public List<ReportPeriod> getAllPeriodsByTaxType(TaxType taxType, boolean backOrder) {
		// TODO Оптимизировать!!!!
		List<ReportPeriod> reportPeriods = new ArrayList<ReportPeriod>();
		List<TaxPeriod> taxPeriods = taxPeriodDao.listByTaxType(taxType);
		if (backOrder){
			Collections.reverse(taxPeriods);
		}
		for (TaxPeriod taxPeriod : taxPeriods) {
			reportPeriods.addAll(reportPeriodDao.listByTaxPeriod(taxPeriod.getId()));
		}
		return reportPeriods;
	}
}
