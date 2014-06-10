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

    @Autowired
    private NotificationService notificationService;

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
	                 long departmentId, List<LogEntry> logs, boolean isBalance, Date correctionDate) {
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
			Map<String, RefBookValue> record = provider.getRecordData((long) dictionaryTaxPeriodId);
			newReportPeriod = new ReportPeriod();
			newReportPeriod.setTaxPeriod(taxPeriod);
			newReportPeriod.setDictTaxPeriodId(dictionaryTaxPeriodId);

			String name = record.get("NAME").getStringValue();

			if (name == null || name.isEmpty()
					|| record.get("START_DATE").getDateValue() == null
					|| record.get("END_DATE").getDateValue() == null
					|| record.get("CALENDAR_START_DATE").getDateValue() == null) {
				throw new ServiceException("Не заполнен один из обязательных атрибутов справочника \"" + refBook.getName() + "\"");
			}
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
			Calendar start = Calendar.getInstance();
			start.setTime(record.get("START_DATE").getDateValue());
			start.set(Calendar.YEAR, year);

			Calendar end = Calendar.getInstance();
			end.setTime(record.get("END_DATE").getDateValue());
			end.set(Calendar.YEAR, year);

			Calendar calendarDate = Calendar.getInstance();
			calendarDate.setTime(record.get("CALENDAR_START_DATE").getDateValue());
			calendarDate.set(Calendar.YEAR, year);

            if (gregorianCalendar.isLeapYear(year)) {
                if (start.get(Calendar.MONTH) == Calendar.FEBRUARY && start.get(Calendar.DATE) == 28) {
                    start.set(Calendar.DATE, 29);
                }
                if (end.get(Calendar.MONTH) == Calendar.FEBRUARY && end.get(Calendar.DATE) == 28) {
                    end.set(Calendar.DATE, 29);
                }
                if (calendarDate.get(Calendar.MONTH) == Calendar.FEBRUARY && calendarDate.get(Calendar.DATE) == 28) {
                    calendarDate.set(Calendar.DATE, 29);
                }
            }

			newReportPeriod.setName(name);
			newReportPeriod.setStartDate(start.getTime());
			newReportPeriod.setEndDate(end.getTime());
			newReportPeriod.setCalendarStartDate(calendarDate.getTime());

			reportPeriodDao.save(newReportPeriod);
		} else {
			newReportPeriod = reportPeriods.get(0);
		}
		for (Department dep : getAvailableDepartments(taxType, user.getUser(), Operation.OPEN, (int)departmentId)) {
			DepartmentReportPeriod depRP = new DepartmentReportPeriod();
			depRP.setReportPeriod(newReportPeriod);
			depRP.setDepartmentId((long) dep.getId());
			depRP.setActive(true);
			depRP.setBalance(isBalance);
			depRP.setCorrectPeriod(correctionDate);
			saveOrUpdate(depRP, null, logs);
		}
	}

	@Override
	public void close(TaxType taxType, int reportPeriodId, long departmentId, Date correctionDate, List<LogEntry> logs, TAUserInfo user) {

		List<Integer> departments = new ArrayList<Integer>();
		for (Department dep : departmentService.getAllChildren((int) departmentId)) {
			departments.add(dep.getId());
		}

		if (checkBeforeClose(departments, reportPeriodId, logs)) {
			for (Department dep : getAvailableDepartments(taxType, user.getUser(), Operation.CLOSE, (int)departmentId)) {
				closePeriodWithLog(reportPeriodId, dep.getId(), correctionDate, logs);
			}
		}

	}

	private boolean checkBeforeClose(List<Integer> departments, int reportPeriodId, List<LogEntry> logs) {
		boolean allGood = true;
        List<FormData> formDataList = formDataDao.find(departments, reportPeriodId);
        for (FormData fd : formDataList) {
            ObjectLock<Long> lock = objectLockDao.getObjectLock(fd.getId(), FormData.class);
            if (lock != null) {
                logs.add(new LogEntry(LogLevel.WARNING,
                        "Форма " + fd.getFormType().getName() +
                                " " + fd.getKind().getName() +
                                " в подразделение " + departmentService.getDepartment(fd.getDepartmentId()).getName() +
                                " редактируется пользователем " + userService.getUser(lock.getUserId()).getName()));
                allGood = false;
            }
        }
		return allGood;
	}

	private void closePeriodWithLog(int reportPeriodId, long departmentId, Date correctionDate, List<LogEntry> logs) {
		DepartmentReportPeriod period = departmentReportPeriodDao.get(reportPeriodId, departmentId, correctionDate);
		if (period != null && period.isActive()) {
			departmentReportPeriodDao.updateActive(reportPeriodId, departmentId, correctionDate, false);
			ReportPeriod reportPeriod = reportPeriodDao.get(reportPeriodId);
			int year = period.getReportPeriod().getTaxPeriod().getYear();
			logs.add(new LogEntry(LogLevel.INFO, "Период" + " \"" + reportPeriod.getName() + "\" " +
					"за " + year + " год " +
					"закрыт для подразделения \"" +
					departmentService.getDepartment((int) departmentId).getName() +
					"\""));
		}
	}

	public void saveOrUpdate(DepartmentReportPeriod departmentReportPeriod, Date correctionDate, List<LogEntry> logs) {
		DepartmentReportPeriod dp = departmentReportPeriodDao.get(departmentReportPeriod.getReportPeriod().getId(),
				departmentReportPeriod.getDepartmentId(), departmentReportPeriod.getCorrectPeriod());
        String balance;
        if (departmentReportPeriod.isBalance()) {
            balance = "ввод остатков ";
        } else {
            balance = "";
        }
		if (dp == null) { //не существует
			departmentReportPeriodDao.save(departmentReportPeriod);
		} else if (!dp.isActive()) { // существует и не открыт
			departmentReportPeriodDao.updateActive(departmentReportPeriod.getReportPeriod().getId(),
				departmentReportPeriod.getDepartmentId(), correctionDate, true);
		} else { // уже открыт
			return;
		}
		if (logs != null) {
			int year = departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear();
            if (departmentReportPeriod.getCorrectPeriod() == null) {
                logs.add(new LogEntry(LogLevel.INFO, "\"" + departmentReportPeriod.getReportPeriod().getName() + "\" " +
                        " за " + year + " год " + balance
                        + "открыт для \" " +
                        departmentService.getDepartment(departmentReportPeriod.getDepartmentId().intValue()).getName() + "\""));
            } else {
                logs.add(new LogEntry(LogLevel.INFO, "Корректирующий период: " + departmentReportPeriod.getReportPeriod().getName()
                        + " " + year + " открыт для " + departmentService.getDepartment(departmentReportPeriod.getDepartmentId().intValue()).getName()));
            }
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
	public Set<ReportPeriod> getOpenForUser(TAUser user, TaxType taxType) {
		List<Integer> departments = departmentService.getTaxFormDepartments(user, Arrays.asList(taxType));
		getPeriodsByTaxTypeAndDepartments(taxType, departments);
		if (user.hasRole(TARole.ROLE_CONTROL_UNP)
				|| user.hasRole(TARole.ROLE_CONTROL_NS)
				|| user.hasRole(TARole.ROLE_CONTROL)
				) {
			return new LinkedHashSet<ReportPeriod>(getOpenPeriodsByTaxTypeAndDepartments(taxType, departments, false, true));
		} else if (user.hasRole(TARole.ROLE_OPER)) {
			return new LinkedHashSet<ReportPeriod>(getOpenPeriodsByTaxTypeAndDepartments(taxType, departments, true, true));
		} else {
			return Collections.EMPTY_SET;
		}
	}

	@Override
	public void removeReportPeriod(TaxType taxType, int reportPeriodId, Date correctionDate, long departmentId, List<LogEntry> logs, TAUserInfo user) {
		List<Integer> departments = new ArrayList<Integer>();
		List<Department> avalDeps = getAvailableDepartments(taxType, user.getUser(), Operation.DELETE, (int) departmentId);
		for (Department dep : avalDeps) {
			departments.add(dep.getId());
		}

		if (checkBeforeRemove(departments, reportPeriodId, logs)) {
			removePeriodWithLog(reportPeriodId, correctionDate, departments, taxType, logs);
		}
	}

	private boolean checkBeforeRemove(List<Integer> departments, int reportPeriodId, List<LogEntry> logs) {
		boolean canRemove = true;
		Set<Integer> blockedBy = new HashSet<Integer>();
        List<FormData> formDataList = formDataDao.find(departments, reportPeriodId);
        if (!formDataList.isEmpty()) {
            for (FormData fd : formDataList) {
                blockedBy.add(fd.getDepartmentId());
            }
            canRemove = false;
        }
		for (Integer dep : departments) {
			DeclarationDataFilter filter = new DeclarationDataFilter();
			filter.setDepartmentIds(Arrays.asList(dep));
			filter.setReportPeriodIds(Arrays.asList(reportPeriodId));
			filter.setSearchOrdering(DeclarationDataSearchOrdering.ID);
			if (!declarationDataSearchService.search(filter).isEmpty()) {
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
				msg.append(departmentService.getDepartment(dep).getName()).append("; ");
			}
			logs.add(new LogEntry(LogLevel.ERROR, msg.toString()));
		}

		return canRemove;
	}

    @Override
	public void removePeriodWithLog(int reportPeriodId, Date correctionDate, List<Integer> departmentId,  TaxType taxType, List<LogEntry> logs) {
		for (Integer id : departmentId) {
            DepartmentReportPeriod drp = departmentReportPeriodDao.get(reportPeriodId, id.longValue(), correctionDate);
            if (drp == null) {
                continue;
            }
            Long drpId = drp.getId();
			departmentReportPeriodDao.delete(drpId);
            //TODO dloshkarev: можно сразу получать список а не выполнять запросы в цикле
			ReportPeriod rp = reportPeriodDao.get(reportPeriodId);
            if (logs != null) {
                logs.add(new LogEntry(LogLevel.INFO,
                        rp.getName() + " " + rp.getTaxPeriod().getYear() + " удалён для " + departmentService.getDepartment(id).getName()));
            }
		}

        notificationService.deleteByReportPeriod(reportPeriodId);

		boolean canRemoveReportPeriod = true;
		for (Department dep : departmentService.listAll()) {
			if (existForDepartment(dep.getId(), reportPeriodId)) {
				canRemoveReportPeriod = false;
				break;
			}
		}

		if (canRemoveReportPeriod) {

            TaxPeriod tp = reportPeriodDao.get(reportPeriodId).getTaxPeriod();
            if (reportPeriodDao.listByTaxPeriod(tp.getId()).isEmpty()) {
                taxPeriodDao.delete(tp.getId());
            }

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
                        case EDIT:
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
                        case EDIT:
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
                        case EDIT:
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

    /**
     * Возвращает список доступных месяцев для указанного отчетного периода.
     * @param reportPeriodId идентификатор отчетного период
     * @return список доступных месяцев
     */
    @Override
    public List<Months> getAvailableMonthList(int reportPeriodId) {

        RefBookDataProvider dataProvider = rbFactory.getDataProvider(8L);

        ReportPeriod reportPeriod = getReportPeriod(reportPeriodId);

        List<Months> monthsList = new ArrayList<Months>();
        monthsList.add(null);

        Map<String, RefBookValue> refBookValueMap = dataProvider.getRecordData((long) reportPeriod.getDictTaxPeriodId());
        // Код налогового периода
        String code = refBookValueMap.get("CODE").getStringValue();

        monthsList.addAll(getReportPeriodMonthList(code));

        return monthsList;
    }

    /**
     * Получить упорядоченный список месяцев соответствующий налоговому периоду с кодом code.
     * @param code код налогового периода
     * @return список месяцев в налоговом периоде.
     */
    private List<Months> getReportPeriodMonthList(String code) {

        int start = 0;
        int end = 0;

        List<Months> list = new ArrayList<Months>();

        if (code.equals("21")) {
            start = 0;
            end = 2;
        } else if (code.equals("22")) {
            start = 3;
            end = 5;
        } else if (code.equals("23")) {
            start = 6;
            end = 8;
        } else if (code.equals("24")) {
            start = 9;
            end = 11;
        } else if (code.equals("31")) {
            start = 0;
            end = 5;
        } else if (code.equals("33")) {
            start = 0;
            end = 8;
        } else if (code.equals("34")) {
            start = 0;
            end = 11;
        }

        for (int i = start; i <= end; ++i) {
            list.add(Months.values()[i]);
        }

        return list;
    }

	@Override
	public List<ReportPeriod> getOpenPeriodsByTaxTypeAndDepartments(TaxType taxType, List<Integer> departmentList,
                                                                    boolean withoutBalance, boolean withoutCorrect) {
		return reportPeriodDao.getOpenPeriodsByTaxTypeAndDepartments(taxType, departmentList, withoutBalance, withoutCorrect);
	}

	@Override
	public boolean isPeriodOpen(int departmentId, long reportPeriodId) {
		return departmentReportPeriodDao.isPeriodOpen(departmentId, reportPeriodId);
	}

    @Override
    public ReportPeriod getPrevReportPeriod(int reportPeriodId) {
        // текущий отчетный период
        ReportPeriod thisReportPeriod = reportPeriodDao.get(reportPeriodId);
        // текущий налоговый период
        TaxPeriod thisTaxPeriod = thisReportPeriod.getTaxPeriod();
        // список отчетных периодов в текущем налоговом периоде
        List<ReportPeriod> reportPeriodlist = reportPeriodDao.listByTaxPeriod(thisReportPeriod.getTaxPeriod().getId());

        /**
         *  если это первый отчетный период в данном налоговом периоде
         *  то возвращать последний отчетный период с предыдущего налогово периода
         */
        if (reportPeriodlist.size() > 0 && reportPeriodlist.get(0).getId() == reportPeriodId){
            List<TaxPeriod> taxPeriodlist = taxPeriodDao.listByTaxType(thisTaxPeriod.getTaxType());
            for (int i = 0; i < taxPeriodlist.size(); i++){
                if (taxPeriodlist.get(i).getId().equals(thisTaxPeriod.getId())){
                    if (i == 0) {
                        return null;
                    }
                    // получим список отчетных периодов для данного налогового периода
                    reportPeriodlist = reportPeriodDao.listByTaxPeriod(taxPeriodlist.get(i - 1).getId());
                    // вернем последний отчетный период
                    return reportPeriodlist.size() > 0 ? reportPeriodlist.get(reportPeriodlist.size() - 1) : null;
                }
            }
        } else {
            // не первый отчетный период в данном налоговом
            for (int i = 0; i < reportPeriodlist.size(); i++){
                if (reportPeriodlist.get(i).getId().equals(reportPeriodId)) {
                    return reportPeriodlist.get(i - 1);
                }
            }
        }
        return null;
    }

    @Override
    public List<ReportPeriod> getCorrectPeriods(TaxType taxType, int departmentId) {
        List<ReportPeriod> correctPeriods = reportPeriodDao.getCorrectPeriods(taxType, departmentId);
        switch (taxType) {
            case INCOME:
            case TRANSPORT:
            case DEAL:
                RefBookDataProvider dataProvider = rbFactory.getDataProvider(8L);
                for (Iterator<ReportPeriod> it = correctPeriods.iterator(); it.hasNext(); ) {
                    ReportPeriod rp = it.next();

                    Map<String, RefBookValue> refBookValueMap = dataProvider.getRecordData((long) rp.getDictTaxPeriodId());
                    // Код налогового периода
                    String code = refBookValueMap.get("CODE").getStringValue();
                    if (!code.equals("34")) {
                        it.remove();
                    }
                }
                break;
        }
        return correctPeriods;
    }

    @Override
    public void openCorrectionPeriod(TaxType taxType, ReportPeriod reportPeriod, long departmentId, Date term, TAUserInfo user, List<LogEntry> logs) {
        for (Department dep : getAvailableDepartments(taxType, user.getUser(), Operation.OPEN, (int)departmentId)) {
            DepartmentReportPeriod drp = new DepartmentReportPeriod();
            drp.setActive(true);
            drp.setReportPeriod(reportPeriod);
            drp.setDepartmentId((long) dep.getId());
            drp.setBalance(false);
            drp.setCorrectPeriod(term);
            saveOrUpdate(drp, term, logs);
        }

    }

    @Override
    public PeriodStatusBeforeOpen checkPeriodStatusBeforeOpen(ReportPeriod reportPeriod, long departmentId, Date term) {
        List<DepartmentReportPeriod> drp = departmentReportPeriodDao.getDepartmentCorrectionPeriods(departmentId,reportPeriod.getId());
        for (DepartmentReportPeriod period : drp) {
            if (period.getCorrectPeriod().equals(term)) {
                if (!period.isActive()) {
                    return PeriodStatusBeforeOpen.CLOSE;
                } else {
                    return PeriodStatusBeforeOpen.OPEN;
                }
            } else if (period.getCorrectPeriod().after(term)) {
                return PeriodStatusBeforeOpen.INVALID;

            }
        }
        return PeriodStatusBeforeOpen.NOT_EXIST;
    }

    @Override
    public void edit(int reportPeriodId, int newDictTaxPeriodId, int newYear, TaxType taxType, TAUserInfo user,
                     long departmentId, boolean isBalance, List<LogEntry> logs) {
        ReportPeriod rp = getReportPeriod(reportPeriodId);


        List<Department> deps = getAvailableDepartments(taxType, user.getUser(), Operation.EDIT, (int) departmentId);
        if ((rp.getDictTaxPeriodId() == newDictTaxPeriodId) && (rp.getTaxPeriod().getYear() == newYear)) { // Изменился только ввод остатков

            for (Department dep : deps) {
                departmentReportPeriodDao.changeBalance(reportPeriodId, dep.getId(), isBalance);
                logs.add(new LogEntry(LogLevel.INFO,
                        "Период с " + rp.getName() + " " + rp.getTaxPeriod().getYear() + " был изменён на " + rp.getName() + " для " + dep.getName()));//<соответствующий календарный год>** + <"ввод остатков" *>**  для <Наименование подразделения>"));
            }

        } else {
            List<Integer> depIds = new ArrayList<Integer>();
            for (Department dep : deps) {
                depIds.add(dep.getId());
            }
            removePeriodWithLog(reportPeriodId, null, depIds, taxType, null);
            open(newYear, newDictTaxPeriodId, taxType, user, departmentId, null, isBalance, null);
            for (Department dep : deps) {
                logs.add(new LogEntry(LogLevel.INFO,
                        "Период с " + rp.getName() + " " + rp.getTaxPeriod().getYear() + " был изменён на " + newDictTaxPeriodId + " для " + dep.getName()));//<соответствующий календарный год>** + <"ввод остатков" *>**  для <Наименование подразделения>"));
            }
        }
    }

    @Override
    public void editCorrectionPeriod(int reportPeriodId, int newReportPeriodId, long departmentId, TaxType taxType,
                                     Date correctionDate, Date newCorrectionDate, TAUserInfo user, List<LogEntry> logs) {
        if (reportPeriodId == newReportPeriodId) { // изменилась только дата корректировки
            ReportPeriod rp = getReportPeriod(reportPeriodId);
            ReportPeriod newRp = getReportPeriod(newReportPeriodId);
            PeriodStatusBeforeOpen status = checkPeriodStatusBeforeOpen(rp, departmentId, newCorrectionDate);
            if (status == PeriodStatusBeforeOpen.NOT_EXIST) {
                List<Department> deps = getAvailableDepartments(taxType, user.getUser(), Operation.EDIT, (int) departmentId);
                for (Department dep : deps) {
                    DepartmentReportPeriod drp = departmentReportPeriodDao.get(reportPeriodId, (long)dep.getId(), correctionDate);
                    departmentReportPeriodDao.updateCorrectionDate(drp.getId(), newCorrectionDate);
                    logs.add(new LogEntry(LogLevel.INFO,
                            "Корректирующий период с " + rp.getName() + " " + rp.getTaxPeriod().getYear() + " был изменён на " + newRp.getName() + " для " + dep.getName()));//<соответствующий календарный год>** + <"ввод остатков" *>**  для <Наименование подразделения>"));

                }
            }
        } else {
            ReportPeriod rp = getReportPeriod(newReportPeriodId);
            PeriodStatusBeforeOpen status = checkPeriodStatusBeforeOpen(rp, departmentId, newCorrectionDate);
            if (status == PeriodStatusBeforeOpen.NOT_EXIST) {
                List<Department> deps = getAvailableDepartments(taxType, user.getUser(), Operation.EDIT, (int) departmentId);
                List<Integer> depIds = new ArrayList<Integer>();
                for (Department dep : deps) {
                    depIds.add(dep.getId());
                }
                removePeriodWithLog(reportPeriodId, correctionDate, depIds, taxType, null);
                ReportPeriod newRp = getReportPeriod(newReportPeriodId);
                openCorrectionPeriod(taxType, newRp, departmentId, newCorrectionDate, user, null);
                for (Department dep : deps) {
                    logs.add(new LogEntry(LogLevel.INFO,
                            "Корректирующий период с " + rp.getName() + " " + rp.getTaxPeriod().getYear() + " был изменён на " + newRp.getName() + " для " + dep.getName()));//<соответствующий календарный год>** + <"ввод остатков" *>**  для <Наименование подразделения>"));
                }



            }
        }

    }
}
