package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.ObjectLockDao;
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
import com.aplana.sbrf.taxaccounting.service.*;
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
	private DepartmentService departmentService;

	@Autowired
	private ObjectLockDao objectLockDao;

	@Autowired
	private FormDataDao formDataDao;

	@Autowired
	private TAUserService userService;

	@Autowired
	private DeclarationDataSearchService declarationDataSearchService;

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
	                 long departmentId, List<LogEntry> logs, boolean isBalance, Date correctionDate, boolean isCorrection) {
		List<TaxPeriod> taxPeriods = taxPeriodDao.listByTaxTypeAndYear(taxType, year);
		TaxPeriod taxPeriod;
		if (taxPeriods.size() > 1) {
			// Что-то пошло не так
			throw new ServiceException("На " + year + " год найдено несколько налоговых периодов");
		} else if (taxPeriods.isEmpty()) {
			taxPeriod = new TaxPeriod();
			taxPeriod.setTaxType(taxType);
			taxPeriod.setYear(year);
			taxPeriodDao.add(taxPeriod);
		} else {
			taxPeriod = taxPeriods.get(0);
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

			if (name == null || name.isEmpty() || ord == null
					|| record.get("START_DATE").getDateValue() == null || record.get("END_DATE").getDateValue() == null) {
				throw new ServiceException("Не заполнен один из обязательных атрибутов справочника \"" + refBook.getName() + "\"");
			}
			Calendar start = new GregorianCalendar();
			start.setTime(record.get("START_DATE").getDateValue());
			start.set(Calendar.YEAR, year);
			Date startDate = start.getTime();

			Calendar end = new GregorianCalendar();
			end.setTime(record.get("END_DATE").getDateValue());
			end.set(Calendar.YEAR, year);
			Date endDate = end.getTime();

			newReportPeriod.setName(name);
			newReportPeriod.setOrder(ord.intValue());
			newReportPeriod.setStartDate(startDate);
			newReportPeriod.setEndDate(endDate);

			// TODO: установить правильный calendar_start_date http://conf.aplana.com/pages/viewpage.action?pageId=9570811 (Marat Fayzullin 2014-01-22)
			newReportPeriod.setCalendarStartDate(startDate);

			reportPeriodDao.save(newReportPeriod);
		} else {
			newReportPeriod = reportPeriods.get(0);
		}
		for (Department dep : getAvailableDepartments(taxType, user.getUser(), Operation.OPEN, (int)departmentId)) {
			DepartmentReportPeriod depRP = new DepartmentReportPeriod();
			depRP.setReportPeriod(newReportPeriod);
			depRP.setDepartmentId(Long.valueOf(dep.getId()));
			depRP.setActive(true);
			depRP.setBalance(isBalance);
			depRP.setHasCorrectPeriod(isCorrection);
			depRP.setCorrectPeriod(correctionDate);
			saveOrUpdate(depRP, logs);
		}
	}

	@Override
	public void close(TaxType taxType, int reportPeriodId, long departmentId, List<LogEntry> logs, TAUserInfo user) {

		List<Integer> departments = new ArrayList<Integer>();
		for (Department dep : departmentService.getAllChildren((int) departmentId)) {
			departments.add(dep.getId());
		}

		if (checkBeforeClose(departments, reportPeriodId, logs)) {
			for (Department dep : getAvailableDepartments(taxType, user.getUser(), Operation.CLOSE, (int)departmentId)) {
				closePeriodWithLog(reportPeriodId, dep.getId(), logs);
			}
		}

	}

	private boolean checkBeforeClose(List<Integer> departments, int reportPeriodId, List<LogEntry> logs) {
		boolean allGood = true;
		for (Integer id : departments) {
			for (FormData formData : formDataDao.find(id, reportPeriodId)) {
                //TODO dloshkarev: можно сразу получать список а не выполнять запросы в цикле
				ObjectLock<Long> lock = objectLockDao.getObjectLock(formData.getId(), FormData.class);
				if (lock != null) {
					logs.add(new LogEntry(LogLevel.WARNING,
							"Форма " + formData.getFormType().getName() +
							" " + formData.getKind().getName() +
							" в подразделение " + departmentService.getDepartment(id).getName() +
							" редактируется пользователем " + userService.getUser(lock.getUserId()).getName()));
					allGood = false;
				}
			}

		}
		return allGood;
	}

	private void closePeriodWithLog(int reportPeriodId, long departmentId, List<LogEntry> logs) {
		DepartmentReportPeriod period = departmentReportPeriodDao.get(reportPeriodId, departmentId);
		if (period != null && period.isActive()) {
			departmentReportPeriodDao.updateActive(reportPeriodId, departmentId, false);
			ReportPeriod reportPeriod = reportPeriodDao.get(reportPeriodId);
			int year = period.getReportPeriod().getTaxPeriod().getYear();
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
			int year = departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear();
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
	public List<DepartmentReportPeriod> listByDepartmentIdAndTaxType(long departmentId, TaxType taxType) {
		return departmentReportPeriodDao.getByDepartmentAndTaxType(departmentId, taxType);
	}

	@Override
	public List<TaxPeriod> listByTaxType(TaxType taxType) {
		return taxPeriodDao.listByTaxType(taxType);
	}

	@Override
	public TaxPeriod getTaxPeriod(int taxPeriodId) {
		return taxPeriodDao.get(taxPeriodId);
	}

    @Override
    public Calendar getStartDate(int reportPeriodId){
		Calendar cal = new GregorianCalendar();
		cal.setTime(reportPeriodDao.get(reportPeriodId).getStartDate());
		return cal;
	}

    @Override
    public Calendar getEndDate(int reportPeriodId){
		Calendar cal = new GregorianCalendar();
		cal.setTime(reportPeriodDao.get(reportPeriodId).getEndDate());
		return cal;
    }

    @Override
    public Calendar getReportDate(int reportPeriodId) {
        Calendar cal = getEndDate(reportPeriodId);
		cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + 1);
        cal.set(Calendar.DATE, 1);
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

    @Override
    public Calendar getMonthStartDate(int reportPeriodId, int periodOrder) {
		Date date = getReportPeriod(reportPeriodId).getStartDate();
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + periodOrder - 1);
        return cal;
    }

    @Override
    public Calendar getMonthEndDate(int reportPeriodId, int periodOrder) {
		Date date = getReportPeriod(reportPeriodId).getStartDate();
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + periodOrder - 1);
		cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DATE));
		return cal;
    }

    @Override
    public Calendar getMonthReportDate(int reportPeriodId, int periodOrder) {
        Calendar cal = getMonthStartDate(reportPeriodId, periodOrder);
		cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + 1);
		cal.set(Calendar.DATE, 1);
        return cal;
    }


	@Override
	public boolean existForDepartment(Integer departmentId, long reportPeriodId) {
		return departmentReportPeriodDao.existForDepartment(departmentId, reportPeriodId);
	}

	@Override
	public PeriodStatusBeforeOpen checkPeriodStatusBeforeOpen(TaxType taxType, int year, boolean balancePeriod, long departmentId, long dictionaryTaxPeriodId) {
		List<TaxPeriod> taxPeriods = taxPeriodDao.listByTaxTypeAndYear(taxType, year);
		TaxPeriod taxPeriod;
		if (taxPeriods.size() > 1) {
			// Что-то пошло не так
			throw new ServiceException("На " + year + " год найдено несколько налоговых периодов");
		} else if (taxPeriods.isEmpty()) {
			return PeriodStatusBeforeOpen.NOT_EXIST;
		} else {
			taxPeriod = taxPeriods.get(0);
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

		if (reportPeriods.isEmpty()) {
			return PeriodStatusBeforeOpen.NOT_EXIST;
		} else if (reportPeriods.size() > 1) {
			throw new ServiceException("На " + year + " год найдено несколько отчетных периодов");
		}
		else {
			if (existForDepartment((int) departmentId, reportPeriods.get(0).getId())) {
				DepartmentReportPeriod drp = departmentReportPeriodDao.get(reportPeriods.get(0).getId(), departmentId);
				if (drp.isBalance() == balancePeriod) {
					if (drp.isActive()) {
						return PeriodStatusBeforeOpen.OPEN;
					} else {
						return PeriodStatusBeforeOpen.CLOSE;
					}
				} else {
					return PeriodStatusBeforeOpen.BALANCE_STATUS_CHANGED;
				}
			}
			return PeriodStatusBeforeOpen.NOT_EXIST;
		}
	}

	@Override
	public void removeReportPeriod(TaxType taxType, int reportPeriodId, long departmentId, List<LogEntry> logs, TAUserInfo user) {
		List<Integer> departments = new ArrayList<Integer>();
		List<Department> avalDeps = getAvailableDepartments(taxType, user.getUser(), Operation.DELETE, (int) departmentId);
		for (Department dep : avalDeps) {
			departments.add(dep.getId());
		}

		if (checkBeforeRemove(departments, reportPeriodId, logs)) {
			removePeriodWithLog(reportPeriodId, departments, logs);
		}
	}

	private boolean checkBeforeRemove(List<Integer> departments, int reportPeriodId, List<LogEntry> logs) {
		boolean canRemove = true;
		Set<Integer> blockedBy = new HashSet<Integer>();
		for (Integer dep : departments) {
			DeclarationDataFilter filter = new DeclarationDataFilter();
			filter.setDepartmentIds(Arrays.asList(new Integer[]{dep}));
			filter.setReportPeriodIds(Arrays.asList(new Integer[]{reportPeriodId}));
			filter.setSearchOrdering(DeclarationDataSearchOrdering.ID);
			if (!declarationDataSearchService.search(filter).isEmpty()) {
				blockedBy.add(dep);
				canRemove = false;
				continue;
			}

			if (!formDataDao.find(dep, reportPeriodId).isEmpty()) {
				blockedBy.add(dep);
				canRemove = false;
				continue;
			}
		}

		if (!canRemove) {
			StringBuilder msg = new StringBuilder(
					"Перед удалением периода необходимо удалить все налоговые формы и декларации в подразделениях, " +
							"для которых удаляется период! На текущий момент не удалены налоговые формы / декларации " +
							"в следующих подразделениях: "
			);
			for (Integer dep : blockedBy) {
                //TODO dloshkarev: можно сразу получать список а не выполнять запросы в цикле
				msg.append(departmentService.getDepartment(dep).getName() + "; ");
			}
			logs.add(new LogEntry(LogLevel.ERROR, msg.toString()));
		}

		return canRemove;
	}

	private void removePeriodWithLog(int reportPeriodId, List<Integer> departmentId, List<LogEntry> logs) {
		for (Integer id : departmentId) {
			departmentReportPeriodDao.delete(reportPeriodId, id);
            //TODO dloshkarev: можно сразу получать список а не выполнять запросы в цикле
			ReportPeriod rp = reportPeriodDao.get(reportPeriodId);
			logs.add(new LogEntry(LogLevel.INFO,
					rp.getName() + " " + rp.getTaxPeriod().getYear() + " удалён для подразделения " + departmentService.getDepartment(id).getName()));
		}

		boolean canRemoveReportPeriod = true;
		for (Department dep : departmentService.listAll()) {
			if (existForDepartment(dep.getId(), reportPeriodId)) {
				System.out.println("Exist for " + dep.getId());
				canRemoveReportPeriod = false;
				break;
			}
		}

		if (canRemoveReportPeriod) {
			System.out.println("Remove for " + reportPeriodDao);
			reportPeriodDao.remove(reportPeriodId);
		}

	}

	private enum Operation {
		FIND, // Поиск периода
		OPEN, // Открытие периода
		CLOSE, // Закрытие периода
		DELETE, // Удаление периода
		EDIT_DEADLINE, // Изменение срока сдачи отчетности в периоде
		EDIT // Редактирование периода
	}
	private List<Department> getAvailableDepartments(TaxType taxType, TAUser user, Operation operation, int departmentId) {
		if (user.hasRole("ROLE_CONTROL_UNP")) {
			switch (taxType) {
				case INCOME:
				case VAT:
				case DEAL:
					switch (operation) {
						case FIND:
							List<Department> dep = new ArrayList<Department>();
							dep.add(departmentService.getBankDepartment());
							return dep;
						case OPEN:
						case CLOSE:
						case DELETE:
						case EDIT_DEADLINE:
								return departmentService.getBADepartments(user);
					}
					break;
				case PROPERTY:
				case TRANSPORT:
					switch (operation) {
						case FIND:
							List<Department> dep = new ArrayList<Department>();
							dep.add(departmentService.getDepartment(departmentId));
							return dep;
						case OPEN:
						case CLOSE:
						case DELETE:
						case EDIT_DEADLINE:
							return departmentService.getAllChildren(departmentId);
					}
					break;
			}
		} else if  (user.hasRole("ROLE_CONTROL_NS")) {
			switch (taxType) {
				case INCOME:
				case VAT:
				case DEAL:
					switch (operation) {
						case FIND:
							return departmentService.getTBDepartments(user);
						case EDIT_DEADLINE:
							return departmentService.getBADepartments(user);
					}
					break;
				case PROPERTY:
				case TRANSPORT:
					switch (operation) {
						case FIND:
							return departmentService.getTBDepartments(user);
						case OPEN:
						case CLOSE:
						case DELETE:
						case EDIT_DEADLINE:
							return departmentService.getBADepartments(user);
					}
					break;
			}
		}
		return Collections.EMPTY_LIST;
	}


    @Override
    public List<ReportPeriod> getPeriodsByTaxTypeAndDepartments(TaxType taxType, List<Integer> departmentList) {
        return reportPeriodDao.getPeriodsByTaxTypeAndDepartments(taxType, departmentList);
    }

    @Override
    public List<Months> getMonthList(int reportPeriodId) {

        ReportPeriod reportPeriod = getReportPeriod(reportPeriodId);

        List<Months> monthsList = new ArrayList<Months>();
        monthsList.add(null);

        if (reportPeriodId != 0) {
            Calendar startDate = getStartDate(reportPeriodId);
            Calendar endDate = getEndDate(reportPeriodId);

            int startMonth = startDate.get(Calendar.MONTH);
            int endMonth = endDate.get(Calendar.MONTH);

            switch (reportPeriod.getTaxPeriod().getTaxType()) {
                case INCOME: {
                    // Первый квартал
                    if (startMonth == 0 && endMonth == 2) {
                        monthsList.add(Months.JANUARY);
                        monthsList.add(Months.FEBRUARY);
                        monthsList.add(Months.MARCH);
                        // Второй квартал
                    } else if (startMonth == 3 && startMonth == 5) {
                        monthsList.add(Months.APRIL);
                        monthsList.add(Months.MAY);
                        monthsList.add(Months.JUNE);
                        // Третий квартал
                    } else if (startMonth == 6 && endMonth == 8) {
                        monthsList.add(Months.JULY);
                        monthsList.add(Months.AUGUST);
                        monthsList.add(Months.SEPTEMBER);
                        // Четвертый квартал
                    } else if (startMonth == 9 && endMonth == 11) {
                        monthsList.add(Months.OCTOBER);
                        monthsList.add(Months.NOVEMBER);
                        monthsList.add(Months.DECEMBER);
                        // Полугодие
                    } else if (startMonth == 0 && endMonth == 5) {
                        monthsList.add(Months.JANUARY);
                        monthsList.add(Months.FEBRUARY);
                        monthsList.add(Months.MARCH);
                        monthsList.add(Months.APRIL);
                        monthsList.add(Months.MAY);
                        monthsList.add(Months.JUNE);
                        // 9 месяцев
                    } else if (startMonth == 0 && endMonth == 8) {
                        monthsList.add(Months.JANUARY);
                        monthsList.add(Months.FEBRUARY);
                        monthsList.add(Months.MARCH);
                        monthsList.add(Months.APRIL);
                        monthsList.add(Months.MAY);
                        monthsList.add(Months.JUNE);
                        monthsList.add(Months.JULY);
                        monthsList.add(Months.AUGUST);
                        monthsList.add(Months.SEPTEMBER);
                        // Год
                    } else if (startMonth == 0 && endMonth == 11) {
                        monthsList.add(Months.JANUARY);
                        monthsList.add(Months.FEBRUARY);
                        monthsList.add(Months.MARCH);
                        monthsList.add(Months.APRIL);
                        monthsList.add(Months.MAY);
                        monthsList.add(Months.JUNE);
                        monthsList.add(Months.JULY);
                        monthsList.add(Months.AUGUST);
                        monthsList.add(Months.SEPTEMBER);
                        monthsList.add(Months.OCTOBER);
                        monthsList.add(Months.NOVEMBER);
                        monthsList.add(Months.DECEMBER);
                    }
                    break;
                }

                case TRANSPORT: {
                    // Первый квартал
                    if (startMonth == 0 && endMonth == 2) {
                        monthsList.add(Months.JANUARY);
                        monthsList.add(Months.FEBRUARY);
                        monthsList.add(Months.MARCH);
                        // Второй квартал
                    } else if (startMonth == 3 && startMonth == 5) {
                        monthsList.add(Months.APRIL);
                        monthsList.add(Months.MAY);
                        monthsList.add(Months.JUNE);
                        // Третий квартал
                    } else if (startMonth == 6 && endMonth == 8) {
                        monthsList.add(Months.JULY);
                        monthsList.add(Months.AUGUST);
                        monthsList.add(Months.SEPTEMBER);
                        // Четвертый квартал
                    } else if (startMonth == 9 && endMonth == 11) {
                        monthsList.add(Months.OCTOBER);
                        monthsList.add(Months.NOVEMBER);
                        monthsList.add(Months.DECEMBER);
                        // Полугодие
                    }
                }
            }
        }
        return monthsList;
    }
}
