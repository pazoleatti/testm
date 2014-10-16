package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
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
public class PeriodServiceImpl implements PeriodService {

	private static final Long PERIOD_CODE_REF_BOOK = 8L;

    @Autowired
	private ReportPeriodDao reportPeriodDao;

	@Autowired
	private TaxPeriodDao taxPeriodDao;

	@Autowired
	private DepartmentReportPeriodService departmentReportPeriodService;

	@Autowired
	private RefBookFactory rbFactory;

	@Autowired
	private DepartmentService departmentService;

	@Autowired
	private FormDataService formDataService;
    @Autowired
    private DeclarationDataService declarationDataService;
    @Autowired
    private DeclarationTemplateService declarationTemplateService;

	@Autowired
	private TAUserService userService;

	@Autowired
	private DeclarationDataSearchService declarationDataSearchService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private LockDataService lockDataService;

	@Override
	public List<ReportPeriod> listByTaxPeriod(int taxPeriodId) {
		return reportPeriodDao.listByTaxPeriod(taxPeriodId);
	}

    @Override
    public DepartmentReportPeriod getLastReportPeriod(TaxType taxType, int departmentId) {
    	// TODO: Нужно получить последний открытый для этого подразделения и типа налога.
    	return null;
    }

    @Override
	public void open(int year, long dictionaryTaxPeriodId, TaxType taxType, TAUserInfo user,
	                 int departmentId, List<LogEntry> logs, boolean isBalance, Date correctionDate) {
		TaxPeriod taxPeriod = taxPeriodDao.getByTaxTypeAndYear(taxType, year);
		if (taxPeriod == null) {
			taxPeriod = new TaxPeriod();
			taxPeriod.setTaxType(taxType);
			taxPeriod.setYear(year);
			taxPeriodDao.add(taxPeriod);
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
			RefBook refBook = rbFactory.get(PERIOD_CODE_REF_BOOK);
			RefBookDataProvider provider = rbFactory.getDataProvider(refBook.getId());
            Map<String, RefBookValue> record;
            try {
                record = provider.getRecordData(dictionaryTaxPeriodId);
            } catch (DaoException ex) {
                throw new ServiceException(ex.getMessage());
            }
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
		for (Integer depId : getAvailableDepartments(taxType, user.getUser(), Operation.OPEN, departmentId)) {
			DepartmentReportPeriod depRP = new DepartmentReportPeriod();
			depRP.setReportPeriod(newReportPeriod);
			depRP.setDepartmentId(depId);
			depRP.setActive(true);
			depRP.setBalance(isBalance);
			depRP.setCorrectionDate(correctionDate);
			saveOrOpen(depRP, logs);
		}
	}

	@Override
	public void close(TaxType taxType, int departmentReportPeriodId, List<LogEntry> logs, TAUserInfo user) {
        DepartmentReportPeriod drp = departmentReportPeriodService.get(departmentReportPeriodId);
		List<Integer> departments = departmentService.getAllChildrenIds(drp.getDepartmentId());

        int reportPeriodId = drp.getReportPeriod().getId();
        if (checkBeforeClose(departments, reportPeriodId, logs, user.getUser())) {
            DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
            filter.setReportPeriodIdList(Arrays.asList(reportPeriodId));
            filter.setDepartmentIdList(getAvailableDepartments(taxType, user.getUser(), Operation.CLOSE, drp.getDepartmentId()));
            if (drp.getCorrectionDate() == null)
                filter.setIsCorrection(false);
            else
                filter.setCorrectionDate(drp.getCorrectionDate());
            List<DepartmentReportPeriod> drpList = departmentReportPeriodService.getListByFilter(filter);
            departmentReportPeriodService.updateActive(departmentReportPeriodService.getListIdsByFilter(filter), false);
            for (DepartmentReportPeriod item : drpList){
                if (item.isActive())
                    continue;
                int year = item.getReportPeriod().getTaxPeriod().getYear();
                logs.add(new LogEntry(LogLevel.INFO, "Период" + " \"" + item.getReportPeriod().getName() + "\" " +
                        year + " " +
                        "закрыт для \"" +
                        departmentService.getDepartment(item.getDepartmentId()).getName() +
                        "\""));

            }
		}
        lockDataService.unlockAll(user);
	}

	private boolean checkBeforeClose(List<Integer> departments, int reportPeriodId, List<LogEntry> logs, TAUser user) {
		boolean allGood = true;
        List<FormData> formDataList = formDataService.find(departments, reportPeriodId);
        for (FormData fd : formDataList) {
            LockData lock = lockDataService.getLock(LockData.LOCK_OBJECTS.FORM_DATA.name() + "_" + fd.getId());
            if (lock != null) {
                logs.add(new LogEntry(LogLevel.WARNING,
                        "Форма " + fd.getFormType().getName() +
                                " " + fd.getKind().getName() +
                                " в подразделении " + departmentService.getDepartment(fd.getDepartmentId()).getName() +
                                " редактируется пользователем " + userService.getUser(lock.getUserId()).getName()));
                allGood = false;
            }
        }
		return allGood;
	}

	@Override
    public void saveOrOpen(DepartmentReportPeriod departmentReportPeriod, List<LogEntry> logs) {
        DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
        filter.setDepartmentIdList(Arrays.asList(departmentReportPeriod.getDepartmentId()));
        filter.setReportPeriodIdList(Arrays.asList(departmentReportPeriod.getReportPeriod().getId()));
        filter.setCorrectionDate(departmentReportPeriod.getCorrectionDate());

        List<DepartmentReportPeriod> departmentReportPeriodList = departmentReportPeriodService.getListByFilter(filter);
        DepartmentReportPeriod savedDepartmentReportPeriod = null;
        if (departmentReportPeriodList.size() == 1) {
            savedDepartmentReportPeriod = departmentReportPeriodList.get(0);
        }

		if (savedDepartmentReportPeriod == null) { //не существует
			departmentReportPeriodService.save(departmentReportPeriod);
		} else if (!savedDepartmentReportPeriod.isActive()) { // существует и не открыт
            departmentReportPeriodService.updateActive(savedDepartmentReportPeriod.getId(), true);
		} else { // уже открыт
			return;
		}
		if (logs != null) {
			int year = departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear();
            if (departmentReportPeriod.getCorrectionDate() == null) {
                logs.add(new LogEntry(LogLevel.INFO,
                        "Период " + "\"" + departmentReportPeriod.getReportPeriod().getName() + "\" "
                        + departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear() + " "
                        + (departmentReportPeriod.isBalance() ? "\"ввод остатков\"" : "") + " "
                        + " открыт для \"" + departmentService.getDepartment(departmentReportPeriod.getDepartmentId()).getName() + "\""
                ));
            } else {
                logs.add(new LogEntry(LogLevel.INFO, "Корректирующий период: " + departmentReportPeriod.getReportPeriod().getName()
                        + " " + year + " открыт для " + departmentService.getDepartment(departmentReportPeriod.getDepartmentId()).getName()));
            }
		}
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
	public boolean existForDepartment(int departmentId, int reportPeriodId) {
		return departmentReportPeriodService.existForDepartment(departmentId, reportPeriodId);
	}

	@Override
	public PeriodStatusBeforeOpen checkPeriodStatusBeforeOpen(TaxType taxType, int year, boolean balancePeriod, int departmentId, long dictionaryTaxPeriodId) {
		TaxPeriod taxPeriod = taxPeriodDao.getByTaxTypeAndYear(taxType, year);
		if (taxPeriod == null) {
			return PeriodStatusBeforeOpen.NOT_EXIST;
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
			if (existForDepartment(departmentId, reportPeriods.get(0).getId())) {
                DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
                filter.setReportPeriodIdList(Arrays.asList(reportPeriods.get(0).getId()));
                filter.setDepartmentIdList(Arrays.asList(departmentId));
                filter.setIsCorrection(false);
                List<DepartmentReportPeriod> departmentReportPeriodList = departmentReportPeriodService.getListByFilter(filter);

				DepartmentReportPeriod drp = null;
                if (departmentReportPeriodList.size() == 1) {
                    drp = departmentReportPeriodList.get(0);
                }

                if (drp != null) {
                    if (drp.isBalance() == balancePeriod) {
                        if (drp.isActive()) {
                            return PeriodStatusBeforeOpen.OPEN;
                        } else {
                            filter = new DepartmentReportPeriodFilter();
                            filter.setReportPeriodIdList(Arrays.asList(reportPeriods.get(0).getId()));
                            filter.setDepartmentIdList(Arrays.asList(departmentId));
                            filter.setIsCorrection(true);
                            departmentReportPeriodList = departmentReportPeriodService.getListByFilter(filter);
                            if (!departmentReportPeriodList.isEmpty()){
                                return PeriodStatusBeforeOpen.CORRECTION_PERIOD_ALREADY_EXIST;
                            }
                            return PeriodStatusBeforeOpen.CLOSE;
                        }
                    } else {
                        if (drp.isActive()) {
                            return PeriodStatusBeforeOpen.BALANCE_STATUS_CHANGED;
                        } else {
                            return PeriodStatusBeforeOpen.CLOSE;
                        }
                    }
                }
			}
			return PeriodStatusBeforeOpen.NOT_EXIST;
		}
	}

	@Override
	public Set<ReportPeriod> getOpenForUser(TAUser user, TaxType taxType) {
		List<Integer> departments = departmentService.getTaxFormDepartments(user, Arrays.asList(taxType), null, null);
		if (user.hasRole(TARole.ROLE_CONTROL_UNP)
				|| user.hasRole(TARole.ROLE_CONTROL_NS)
				|| user.hasRole(TARole.ROLE_CONTROL)
				) {
			return new LinkedHashSet<ReportPeriod>(getOpenPeriodsByTaxTypeAndDepartments(taxType, departments, false, false));
		} else if (user.hasRole(TARole.ROLE_OPER)) {
			return new LinkedHashSet<ReportPeriod>(getOpenPeriodsByTaxTypeAndDepartments(taxType, departments, true, false));
		} else {
			return Collections.EMPTY_SET;
		}
	}

    //http://conf.aplana.com/pages/viewpage.action?pageId=11389882#id-Формаспискапериодов-Удалениепериода
	@Override
	public void removeReportPeriod(TaxType taxType, int drpId, Logger logger, TAUserInfo user) {
        //Проверка форм не относится к этой постановке
        List<Integer> departmentIds = departmentService.getBADepartmentIds(user.getUser());
        DepartmentReportPeriod drp = departmentReportPeriodService.get(drpId);

        //Check forms
        List<FormData> formDatas = formDataService.find(departmentIds, drp.getReportPeriod().getId());
        for (FormData fd : formDatas) {
            logger.error("Форма %s %s в подразделении %s находится в удаляемом периоде!",
                    fd.getFormType().getName(), fd.getKind().getName(),
                    departmentService.getDepartment(fd.getDepartmentId()).getName());
        }

        DeclarationDataFilter filter = new DeclarationDataFilter();
        filter.setDepartmentIds(departmentIds);
        filter.setReportPeriodIds(Collections.singletonList(drp.getReportPeriod().getId()));
        List<Long> declarations = declarationDataSearchService.getDeclarationIds(filter, DeclarationDataSearchOrdering.ID, true);
        for (Long id : declarations) {
            DeclarationData dd = declarationDataService.get(id, user);
            DeclarationTemplate dt = declarationTemplateService.get(dd.getDeclarationTemplateId());
            logger.error("%s в подразделении %s находится в удаляемом периоде!", dt.getType().getName(), departmentService.getDepartment(dd.getDepartmentId()).getName());
        }

        int reportPeriodId = drp.getReportPeriod().getId();
        //2 Проверка вида периода
        if (drp.getCorrectionDate() != null &&
                departmentReportPeriodService.existLargeCorrection(drp.getDepartmentId(), reportPeriodId, drp.getCorrectionDate())){
            logger.error("Удаление периода невозможно, т.к. существует более поздний корректирующий период!");
            return;
        } else if (drp.getCorrectionDate() == null){
            //3 Существуют ли корректирующий период
            DepartmentReportPeriodFilter drpFilter = new DepartmentReportPeriodFilter();
            drpFilter.setIsCorrection(true);
            drpFilter.setReportPeriodIdList(Arrays.asList(reportPeriodId));
            drpFilter.setDepartmentIdList(Arrays.asList(drp.getDepartmentId()));
            List<Integer> corrIds = departmentReportPeriodService.getListIdsByFilter(drpFilter);
            if (!corrIds.isEmpty()){
                logger.error("Удаление периода невозможно, т.к. для него существует корректирующий период!");
                return;
            }
        }
        if (logger.containsLevel(LogLevel.ERROR)){
            return;
        }
		List<Integer> departments = getAvailableDepartments(taxType, user.getUser(), Operation.DELETE, drp.getDepartmentId());

		if (checkBeforeRemove(departments, drp.getReportPeriod().getId(), logger.getEntries())) {
			removePeriodWithLog(drp.getReportPeriod().getId(), drp.getCorrectionDate(), departments, taxType, logger.getEntries());
		}
	}

	private boolean checkBeforeRemove(List<Integer> departments, int reportPeriodId, List<LogEntry> logs) {
		boolean canRemove = true;
		Set<Integer> blockedBy = new HashSet<Integer>();
        List<FormData> formDataList = formDataService.find(departments, reportPeriodId);
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
	public void removePeriodWithLog(int reportPeriodId, Date correctionDate, List<Integer> departmentIds,  TaxType taxType, List<LogEntry> logs) {
        ReportPeriod rp = reportPeriodDao.get(reportPeriodId);
        DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
        filter.setCorrectionDate(correctionDate);
        filter.setDepartmentIdList(departmentIds);
        filter.setReportPeriodIdList(Arrays.asList(reportPeriodId));
        List<Integer> drpIds = departmentReportPeriodService.getListIdsByFilter(filter);
        departmentReportPeriodService.delete(drpIds);
		for (Integer id : departmentIds) {
            if (logs != null) {
                logs.add(new LogEntry(LogLevel.INFO,
                        "Период \"" + rp.getName() + "\" " +
                         rp.getTaxPeriod().getYear() + " удалён для " + "\"" + departmentService.getDepartment(id).getName() + "\""
                ));
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
	private List<Integer> getAvailableDepartments(TaxType taxType, TAUser user, Operation operation, int departmentId) {
		if (user.hasRole("ROLE_CONTROL_UNP")) {
			switch (taxType) {
				case INCOME:
				case VAT:
				case DEAL:
					switch (operation) {
						case FIND:
							List<Integer> dep = new ArrayList<Integer>();
							dep.add(departmentService.getBankDepartment().getId());
							return dep;
						case OPEN:
						case CLOSE:
						case DELETE:
                        case EDIT:
						case EDIT_DEADLINE:
								return departmentService.getBADepartmentIds(user);
					}
					break;
				case PROPERTY:
				case TRANSPORT:
					switch (operation) {
						case FIND:
							return Arrays.asList(departmentId);
						case OPEN:
						case CLOSE:
						case DELETE:
                        case EDIT:
						case EDIT_DEADLINE:
							return departmentService.getAllChildrenIds(departmentId);
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
							return departmentService.getTBDepartmentIds(user);
						case EDIT_DEADLINE:
							return departmentService.getBADepartmentIds(user);
					}
					break;
				case PROPERTY:
				case TRANSPORT:
					switch (operation) {
						case FIND:
							return departmentService.getTBDepartmentIds(user);
						case OPEN:
						case CLOSE:
						case DELETE:
                        case EDIT:
						case EDIT_DEADLINE:
							return departmentService.getBADepartmentIds(user);
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
    public List<Integer> getAvailableDepartmentsForClose(TaxType taxType, TAUser user, int departmentId) {
        return getAvailableDepartments(taxType, user, Operation.CLOSE, departmentId);
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

        Map<String, RefBookValue> refBookValueMap = dataProvider.getRecordData(reportPeriod.getDictTaxPeriodId());
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
    public ReportPeriod getPrevReportPeriod(int reportPeriodId) {
        // текущий отчетный период
        ReportPeriod thisReportPeriod = reportPeriodDao.get(reportPeriodId);
        // текущий налоговый период
        TaxPeriod thisTaxPeriod = thisReportPeriod.getTaxPeriod();
        // список отчетных периодов в текущем налоговом периоде
        List<ReportPeriod> reportPeriodList = reportPeriodDao.listByTaxPeriod(thisReportPeriod.getTaxPeriod().getId());

        /**
         *  если это первый отчетный период в данном налоговом периоде
         *  то возвращать последний отчетный период с предыдущего налогового периода
         */
        if (thisReportPeriod.getOrder() == 1 && reportPeriodList.size() > 0 && reportPeriodList.get(0).getId() == reportPeriodId){
            List<TaxPeriod> taxPeriodList = taxPeriodDao.listByTaxType(thisTaxPeriod.getTaxType());
            for (int i = 0; i < taxPeriodList.size(); i++){
                if (taxPeriodList.get(i).getId().equals(thisTaxPeriod.getId())){
                    if (i == 0) {
                        return null;
                    }
                    // получим список отчетных периодов для данного налогового периода
                    TaxPeriod prevTaxPeriod = taxPeriodList.get(i - 1);
                    // проверим что налоговые периоды по порядку
                    if (prevTaxPeriod.getYear() + 1 != thisTaxPeriod.getYear()) {
                        return null;
                    }
                    reportPeriodList = reportPeriodDao.listByTaxPeriod(prevTaxPeriod.getId());
                    // вернем последний отчетный период
                    return reportPeriodList.size() > 0 ? reportPeriodList.get(reportPeriodList.size() - 1) : null;
                }
            }
        } else {
            // не первый отчетный период в данном налоговом
            for (int i = 1; i < reportPeriodList.size(); i++){
                if (reportPeriodList.get(i).getId().equals(reportPeriodId)) {
                    return reportPeriodList.get(i - 1);
                }
            }
        }
        return null;
    }

    @Override
    public List<ReportPeriod> getCorrectPeriods(TaxType taxType, int departmentId) {
        return reportPeriodDao.getCorrectPeriods(taxType, departmentId);
    }

    @Override
    public void openCorrectionPeriod(TaxType taxType, ReportPeriod reportPeriod, int departmentId, Date term, TAUserInfo user, List<LogEntry> logs) {
        for (Integer depId : getAvailableDepartments(taxType, user.getUser(), Operation.OPEN, departmentId)) {
            DepartmentReportPeriod drp = new DepartmentReportPeriod();
            drp.setActive(true);
            drp.setReportPeriod(reportPeriod);
            drp.setDepartmentId(depId);
            drp.setBalance(false);
            drp.setCorrectionDate(term);
            saveOrOpen(drp,  logs);
        }

    }

    @Override
    public PeriodStatusBeforeOpen checkPeriodStatusBeforeOpen(ReportPeriod reportPeriod, int departmentId, Date term) {
        DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
        filter.setDepartmentIdList(Arrays.asList(departmentId));
        filter.setReportPeriodIdList(Arrays.asList(reportPeriod.getId()));
        filter.setIsCorrection(true);
        List<DepartmentReportPeriod> drpList = departmentReportPeriodService.getListByFilter(filter);
        for (DepartmentReportPeriod period : drpList) {
            if (period.getCorrectionDate().equals(term)) {
                if (!period.isActive()) {
                    return PeriodStatusBeforeOpen.CLOSE;
                } else {
                    return PeriodStatusBeforeOpen.OPEN;
                }
            } else if (period.getCorrectionDate().after(term)) {
                return PeriodStatusBeforeOpen.INVALID;

            }
        }
        if (!drpList.isEmpty()){
            //проверяет статус последнего по порядку корректирующего периода (сортировка в дао)
            DepartmentReportPeriod drpLast = drpList.get(drpList.size()-1);
            if (drpLast.isActive())
                return PeriodStatusBeforeOpen.CORRECTION_PERIOD_LAST_OPEN;
        }

        //Система проверяет статус периода корректировки (конкретный период ищем, т.е. д.б. одно значение)
        filter = new DepartmentReportPeriodFilter();
        filter.setIsCorrection(false);
        filter.setDepartmentIdList(Arrays.asList(departmentId));
        filter.setReportPeriodIdList(Arrays.asList(reportPeriod.getId()));
        List<DepartmentReportPeriod> onePeriod = departmentReportPeriodService.getListByFilter(filter);
        if (!onePeriod.isEmpty() && onePeriod.size()!=1){
            throw new ServiceException("Найдено больше одного периода корректировки с заданной датой корректировки.");
        } else if (onePeriod.isEmpty()){
            throw new ServiceException("Не найден корректирующий период.");
        } else if (onePeriod.size() == 1 && onePeriod.get(0).isActive()){
            return PeriodStatusBeforeOpen.CORRECTION_PERIOD_NOT_CLOSE;
        }

        return PeriodStatusBeforeOpen.NOT_EXIST;
    }

    @Override
    public void edit(int reportPeriodId, long newDictTaxPeriodId, int newYear, TaxType taxType, TAUserInfo user,
                     int departmentId, boolean isBalance, List<LogEntry> logs) {
        ReportPeriod rp = getReportPeriod(reportPeriodId);
        RefBook refBook = rbFactory.get(PERIOD_CODE_REF_BOOK);
        RefBookDataProvider provider = rbFactory.getDataProvider(refBook.getId());
        Map<String, RefBookValue> dictTaxPeriod;
        try {
            dictTaxPeriod = provider.getRecordData(newDictTaxPeriodId);
        } catch (DaoException ex) {
            throw new ServiceException(ex.getMessage());
        }

        String strBalance = isBalance ? " ввод остатков" : "";
        List<Integer> depIds = getAvailableDepartments(taxType, user.getUser(), Operation.EDIT, departmentId);
        if ((rp.getDictTaxPeriodId() == newDictTaxPeriodId) && (rp.getTaxPeriod().getYear() == newYear)) { // Изменился только ввод остатков

            DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
            filter.setReportPeriodIdList(Arrays.asList(reportPeriodId));
            filter.setIsCorrection(false);
            filter.setDepartmentIdList(depIds);
            departmentReportPeriodService.updateBalance(departmentReportPeriodService.getListIdsByFilter(filter), isBalance);
            for (Integer depId : depIds) {
                logs.add(new LogEntry(LogLevel.INFO,
                        "Период с " + rp.getName() + " " + rp.getTaxPeriod().getYear() +
                                " был изменён на " + rp.getName() + strBalance + " для " + departmentService.getDepartment(depId).getName()));//<соответствующий календарный год>** + <"ввод остатков" *>**  для <Наименование подразделения>"));
            }

        } else {
            removePeriodWithLog(reportPeriodId, null, depIds, taxType, null);
            open(newYear, newDictTaxPeriodId, taxType, user, departmentId, null, isBalance, null);
            for (Integer depId : depIds) {
                logs.add(new LogEntry(LogLevel.INFO,
                        "Период с " + rp.getName() + " " + rp.getTaxPeriod().getYear() + " был изменён на " +
                                dictTaxPeriod.get("NAME").getStringValue() + " " + newYear + strBalance + " для " + departmentService.getDepartment(depId).getName()));//<соответствующий календарный год>** + <"ввод остатков" *>**  для <Наименование подразделения>"));
            }
        }
    }

    @Override
    public void editCorrectionPeriod(int reportPeriodId, int newReportPeriodId, int oldDepartmentId, int newDepartmentId, TaxType taxType,
                                     Date correctionDate, Date newCorrectionDate, TAUserInfo user, List<LogEntry> logs) {
        if ((reportPeriodId == newReportPeriodId) && (oldDepartmentId == newDepartmentId)) { // изменилась только дата корректировки
            ReportPeriod rp = getReportPeriod(reportPeriodId);
            ReportPeriod newRp = getReportPeriod(newReportPeriodId);
            PeriodStatusBeforeOpen status = checkPeriodStatusBeforeOpen(rp, newDepartmentId, newCorrectionDate);
            if (status == PeriodStatusBeforeOpen.NOT_EXIST) {
                List<Integer> depIds = getAvailableDepartments(taxType, user.getUser(), Operation.EDIT, newDepartmentId);
                DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
                filter.setCorrectionDate(correctionDate);
                filter.setReportPeriodIdList(Arrays.asList(reportPeriodId));
                filter.setDepartmentIdList(depIds);
                List<Integer> drpIds = departmentReportPeriodService.getListIdsByFilter(filter);
                for (Integer drpId : drpIds) {
                    departmentReportPeriodService.updateCorrectionDate(drpId, newCorrectionDate);
                    logs.add(new LogEntry(LogLevel.INFO,
                            "Корректирующий период с " + rp.getName() + " " + rp.getTaxPeriod().getYear()
                                    + " был изменён на " + newRp.getName() + " для " +
                                    departmentService.getDepartment(departmentReportPeriodService.get(drpId).getDepartmentId()).getName()));//<соответствующий календарный год>** + <"ввод остатков" *>**  для <Наименование подразделения>"));

                }
            }
        } else {
            ReportPeriod rp = getReportPeriod(newReportPeriodId);
            PeriodStatusBeforeOpen status = checkPeriodStatusBeforeOpen(rp, newDepartmentId, newCorrectionDate);
            if (status == PeriodStatusBeforeOpen.NOT_EXIST) {
                List<Integer> depIds = getAvailableDepartments(taxType, user.getUser(), Operation.EDIT, oldDepartmentId);
                removePeriodWithLog(reportPeriodId, correctionDate, depIds, taxType, null);
                ReportPeriod newRp = getReportPeriod(newReportPeriodId);
                openCorrectionPeriod(taxType, newRp, newDepartmentId, newCorrectionDate, user, null);
                for (Integer depId : depIds) {
                    logs.add(new LogEntry(LogLevel.INFO,
                            "Корректирующий период с " + rp.getName() + " " + rp.getTaxPeriod().getYear() +
                                    " был изменён на " + newRp.getName() + " для " + departmentService.getDepartment(depId).getName()));//<соответствующий календарный год>** + <"ввод остатков" *>**  для <Наименование подразделения>"));
                }
            }
        }
    }

    @Override
    public List<DepartmentReportPeriod> getDRPByDepartmentIds(List<TaxType> taxTypes, List<Integer> departmentIds) {
        if (departmentIds == null || departmentIds.isEmpty()) {
            return new ArrayList<DepartmentReportPeriod>(0);
        }

        if (taxTypes == null) {
            taxTypes = Arrays.asList(TaxType.values());
        }

        DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
        filter.setDepartmentIdList(departmentIds);
        filter.setTaxTypeList(taxTypes);

        return departmentReportPeriodService.getListByFilter(filter);
    }

    @Override
    public ReportPeriod getByTaxTypedCodeYear(TaxType taxType, String code, int year) {
        return reportPeriodDao.getByTaxTypedCodeYear(taxType, code, year);
    }
}
