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
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.aplana.sbrf.taxaccounting.model.TaxType.ETR;

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
    private FormDataSearchService formDataSearchService;
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

    private static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

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
		DepartmentReportPeriod depRP = new DepartmentReportPeriod();
		depRP.setReportPeriod(newReportPeriod);
		depRP.setActive(true);
		depRP.setBalance(isBalance);
		depRP.setCorrectionDate(correctionDate);
		saveOrOpen(depRP, getAvailableDepartments(taxType, user.getUser(), Operation.OPEN, departmentId), logs);
	}

	@Override
	public void close(TaxType taxType, int departmentReportPeriodId, List<LogEntry> logs, TAUserInfo user) {
        DepartmentReportPeriod drp = departmentReportPeriodService.get(departmentReportPeriodId);
        if (drp == null) {
            throw new ServiceException("Период не найден. Возможно он был удалён. Попробуйте обновить страницу.");
        }
		List<Integer> departments = departmentService.getAllChildrenIds(drp.getDepartmentId());

        int reportPeriodId = drp.getReportPeriod().getId();
        if (checkBeforeClose(departments, reportPeriodId, logs, user.getUser())) {
            DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
            filter.setReportPeriodIdList(Collections.singletonList(reportPeriodId));
            filter.setDepartmentIdList(getAvailableDepartments(taxType, user.getUser(), Operation.CLOSE, drp.getDepartmentId()));
            if (drp.getCorrectionDate() == null)
                filter.setIsCorrection(false);
            else
                filter.setCorrectionDate(drp.getCorrectionDate());
            departmentReportPeriodService.updateActive(departmentReportPeriodService.getListIdsByFilter(filter), reportPeriodId, false);
            List<DepartmentReportPeriod> drpList = departmentReportPeriodService.getListByFilter(filter);
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
	}

	private boolean checkBeforeClose(List<Integer> departments, int reportPeriodId, List<LogEntry> logs, TAUser user) {
		boolean allGood = true;
        List<FormData> formDataList = formDataService.find(departments, reportPeriodId);
        for (FormData fd : formDataList) {
            Pair<ReportType, LockData> lockType = formDataService.getLockTaskType(fd.getId());
            if (lockType != null) {
                logs.add(new LogEntry(LogLevel.ERROR,
                        "Форма " + fd.getFormType().getName() +
                                " " + fd.getKind().getTitle() +
                                " в подразделении " + departmentService.getDepartment(fd.getDepartmentId()).getName() +
                                " заблокирована пользователем " + userService.getUser(lockType.getSecond().getUserId()).getName() +
                                " (запущена операция \"" + formDataService.getTaskName(lockType.getFirst(), fd.getId(), userService.getSystemUserInfo()) +"\")")
                );
                allGood = false;
            }
        }
		return allGood;
	}

	@Override
    public void saveOrOpen(DepartmentReportPeriod departmentReportPeriod, List<LogEntry> logs) {
        DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
        filter.setDepartmentIdList(Collections.singletonList(departmentReportPeriod.getDepartmentId()));
        filter.setReportPeriodIdList(Collections.singletonList(departmentReportPeriod.getReportPeriod().getId()));
        filter.setCorrectionDate(departmentReportPeriod.getCorrectionDate());

        List<DepartmentReportPeriod> departmentReportPeriodList = departmentReportPeriodService.getListByFilter(filter);
        DepartmentReportPeriod savedDepartmentReportPeriod = null;
        if (departmentReportPeriodList.size() == 1) {
            savedDepartmentReportPeriod = departmentReportPeriodList.get(0);
        }

		if (savedDepartmentReportPeriod == null) { //не существует
			departmentReportPeriodService.save(departmentReportPeriod);
		} else if (!savedDepartmentReportPeriod.isActive()) { // существует и не открыт
            departmentReportPeriodService.updateActive(savedDepartmentReportPeriod.getId(), true, departmentReportPeriod.isBalance());
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
	public void saveOrOpen(DepartmentReportPeriod departmentReportPeriod, List<Integer> departmentIds, List<LogEntry> logs) {
		List<Integer> reportIdsForUpdate = new ArrayList<Integer>();
		List<Integer> departmentIdsForSave = new ArrayList<Integer>();
		Integer reportPeriodId = departmentReportPeriod.getReportPeriod().getId();
		DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
		filter.setReportPeriodIdList(Collections.singletonList(reportPeriodId));
		filter.setCorrectionDate(departmentReportPeriod.getCorrectionDate());

		filter.setDepartmentIdList(departmentIds);
		List<DepartmentReportPeriod> departmentReportPeriodList = departmentReportPeriodService.getListByFilter(filter);

		departmentIdsForSave.addAll(departmentIds);
		for (DepartmentReportPeriod depPeriod : departmentReportPeriodList) {
			reportIdsForUpdate.add(depPeriod.getId());
			departmentIdsForSave.remove(depPeriod.getDepartmentId());
		}

		if (!departmentIdsForSave.isEmpty()) {
			departmentReportPeriodService.save(departmentReportPeriod, departmentIdsForSave);
		}
		if (!reportIdsForUpdate.isEmpty()) {
			departmentReportPeriodService.updateActive(reportIdsForUpdate, reportPeriodId, true, departmentReportPeriod.isBalance());
		}

        if (logs != null) {
            for (DepartmentReportPeriod period : departmentReportPeriodService.getListByFilter(filter)) {
                int year = period.getReportPeriod().getTaxPeriod().getYear();
                if (period.getCorrectionDate() == null) {
                    logs.add(new LogEntry(LogLevel.INFO,
                            "Период " + "\"" + period.getReportPeriod().getName() + "\" "
                                    + period.getReportPeriod().getTaxPeriod().getYear() + " "
                                    + (period.isBalance() ? "\"ввод остатков\"" : "") + " "
                                    + " открыт для \"" + departmentService.getDepartment(period.getDepartmentId()).getName() + "\""
                    ));
                } else {
                    logs.add(new LogEntry(LogLevel.INFO, "Корректирующий период: " + period.getReportPeriod().getName()
                            + " " + year + " открыт для " + departmentService.getDepartment(period.getDepartmentId()).getName()));
                }
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
                filter.setReportPeriodIdList(Collections.singletonList(reportPeriods.get(0).getId()));
                filter.setDepartmentIdList(Collections.singletonList(departmentId));
                filter.setIsCorrection(false);
                List<DepartmentReportPeriod> departmentReportPeriodList = departmentReportPeriodService.getListByFilter(filter);

				DepartmentReportPeriod drp = null;
                if (departmentReportPeriodList.size() == 1) {
                    drp = departmentReportPeriodList.get(0);
                }

                if (drp != null) {

                    if (drp.isActive()) {
                        if (drp.isBalance() == balancePeriod) {
                            return PeriodStatusBeforeOpen.OPEN;
                        } else {
                            return PeriodStatusBeforeOpen.BALANCE_STATUS_CHANGED;
                        }
                    } else {
                        if (drp.isBalance() != balancePeriod) {
                            return PeriodStatusBeforeOpen.BALANCE_STATUS_CHANGED;
                        }
                        filter = new DepartmentReportPeriodFilter();
                        filter.setReportPeriodIdList(Collections.singletonList(reportPeriods.get(0).getId()));
                        filter.setDepartmentIdList(Collections.singletonList(departmentId));
                        filter.setIsCorrection(true);
                        departmentReportPeriodList = departmentReportPeriodService.getListByFilter(filter);
                        if (!departmentReportPeriodList.isEmpty()){
                            return PeriodStatusBeforeOpen.CORRECTION_PERIOD_ALREADY_EXIST;
                        }
                        return PeriodStatusBeforeOpen.CLOSE;
                    }
                }
			}
			return PeriodStatusBeforeOpen.NOT_EXIST;
		}
	}

	@Override
	public Set<ReportPeriod> getOpenForUser(TAUser user, TaxType taxType) {
		List<Integer> departments = departmentService.getTaxFormDepartments(user, Collections.singletonList(taxType), null, null);
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
        List<Integer> departmentIds = new ArrayList<Integer>();
        DepartmentReportPeriod drp = departmentReportPeriodService.get(drpId);
        if (drp == null) {
            throw new ServiceException("Период не найден. Возможно он был уже удалён. Попробуйте обновить страницу.");
        }

        switch (taxType) {
            case PROPERTY:
            case TRANSPORT:
            case LAND:
                departmentIds = departmentService.getAllChildrenIds(drp.getDepartmentId());
                break;
            case INCOME:
            case DEAL:
            case VAT:
            case MARKET:
            case ETR:
                departmentIds = departmentService.getBADepartmentIds(user.getUser());
                break;
        }
        //Check forms
        FormDataFilter dataFilter = new FormDataFilter();
        if (drp.getCorrectionDate() != null) {
            dataFilter.setCorrectionTag(true);
            dataFilter.setCorrectionDate(drp.getCorrectionDate());
        } else {
            dataFilter.setCorrectionTag(false);
        }
        dataFilter.setDepartmentIds(departmentIds);
		// Проверка на наличие форм в периоде
        dataFilter.setReportPeriodIds(Collections.singletonList(drp.getReportPeriod().getId()));
        List<FormData> formDatas = formDataSearchService.findDataByFilter(dataFilter);
        for (FormData fd : formDatas) {
            logger.error("Форма \"%s\" \"%s\" в подразделении \"%s\" находится в удаляемом периоде!",
                    fd.getFormType().getName(), fd.getKind().getTitle(),
                    departmentService.getDepartment(fd.getDepartmentId()).getName());
        }

		if (taxType.equals(ETR)) {
			// Проверка на наличие форм в периоде сравнения
			dataFilter.setReportPeriodIds(null);
			dataFilter.setComparativePeriodId(Collections.singletonList(drp.getReportPeriod().getId()));
			formDatas = formDataSearchService.findDataByFilter(dataFilter);
			for (FormData fd : formDatas) {
				logger.error("Форма \"%s\" \"%s\" в подразделении \"%s\" находится в удаляемом периоде!",
						fd.getFormType().getName(), fd.getKind().getTitle(),
						departmentService.getDepartment(fd.getDepartmentId()).getName());
			}
		}

        DeclarationDataFilter filter = new DeclarationDataFilter();
        filter.setDepartmentIds(departmentIds);
        filter.setReportPeriodIds(Collections.singletonList(drp.getReportPeriod().getId()));
        if (drp.getCorrectionDate() != null) {
            filter.setCorrectionTag(true);
            filter.setCorrectionDate(drp.getCorrectionDate());
        } else {
            filter.setCorrectionTag(false);
        }
        List<Long> declarations = declarationDataSearchService.getDeclarationIds(filter, DeclarationDataSearchOrdering.ID, true);
        for (Long id : declarations) {
            DeclarationData dd = declarationDataService.get(id, user);
            DeclarationTemplate dt = declarationTemplateService.get(dd.getDeclarationTemplateId());
            logger.error("\"%s\" в подразделении \"%s\" находится в удаляемом периоде!", dt.getType().getName(), departmentService.getDepartment(dd.getDepartmentId()).getName());
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
            drpFilter.setReportPeriodIdList(Collections.singletonList(reportPeriodId));
            drpFilter.setDepartmentIdList(Collections.singletonList(drp.getDepartmentId()));
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

        removePeriodWithLog(drp.getReportPeriod().getId(), drp.getCorrectionDate(), departments, taxType, logger.getEntries());
	}

	private void removePeriodWithLog(int reportPeriodId, Date correctionDate, List<Integer> departmentIds,  TaxType taxType, List<LogEntry> logs) {
        ReportPeriod rp = reportPeriodDao.get(reportPeriodId);
        DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
        filter.setCorrectionDate(correctionDate);
        filter.setDepartmentIdList(departmentIds);
        filter.setReportPeriodIdList(Collections.singletonList(reportPeriodId));
        List<Integer> drpIds = departmentReportPeriodService.getListIdsByFilter(filter);
        departmentReportPeriodService.delete(drpIds);
		for (Integer id : departmentIds) {
            if (logs != null) {
                logs.add(new LogEntry(LogLevel.INFO,
                        "Период удалён для " + "\"" + departmentService.getDepartment(id).getName() + "\""
                ));
            }
		}

        if (logs != null) {
            logs.add(new LogEntry(LogLevel.INFO,
                    "Удален период \"" + rp.getName() + "\" " + rp.getTaxPeriod().getYear() +
                            (correctionDate == null ? "" : " с датой сдачи корректировки " + sdf.get().format(correctionDate))
                    ));
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

	public List<Integer> getAvailableDepartments(TaxType taxType, TAUser user, Operation operation, int departmentId) {
		if (user.hasRole("ROLE_CONTROL_UNP")) {
			switch (taxType) {
				case INCOME:
				case VAT:
				case DEAL:
				case MARKET:
                case ETR: //TODO
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
                case LAND:
					switch (operation) {
						case FIND:
							return Collections.singletonList(departmentId);
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
				case MARKET:
                case ETR: //TODO
                    switch (operation) {
						case FIND:
							return departmentService.getTBDepartmentIds(user);
						case EDIT_DEADLINE:
							return departmentService.getBADepartmentIds(user);
					}
					break;
				case PROPERTY:
				case TRANSPORT:
                case LAND:
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

        monthsList.addAll(getReportPeriodMonthList(refBookValueMap));

        return monthsList;
    }

    /**
     * Получить упорядоченный список месяцев соответствующий
     * @param refBookValueMap
     * @return список месяцев в налоговом периоде
     */
    private List<Months> getReportPeriodMonthList(Map<String, RefBookValue> refBookValueMap) {
        int start;
        int end;

        List<Months> list = new ArrayList<Months>();

        GregorianCalendar startDate = new GregorianCalendar();
        startDate.setTime(refBookValueMap.get("CALENDAR_START_DATE").getDateValue());
        start = startDate.get(Calendar.MONTH);

        GregorianCalendar endDate = new GregorianCalendar();
        endDate.setTime(refBookValueMap.get("END_DATE").getDateValue());
        end = endDate.get(Calendar.MONTH);

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
        if (thisReportPeriod.getOrder() == 1 && !reportPeriodList.isEmpty() && reportPeriodList.get(0).getId() == reportPeriodId){
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
                    return !reportPeriodList.isEmpty() ? reportPeriodList.get(reportPeriodList.size() - 1) : null;
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
    public List<ReportPeriod> getComparativPeriods(TaxType taxType, int departmentId) {
        return reportPeriodDao.getComparativPeriods(taxType, departmentId);
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
        filter.setDepartmentIdList(Collections.singletonList(departmentId));
        filter.setReportPeriodIdList(Collections.singletonList(reportPeriod.getId()));
        filter.setIsCorrection(true);
        List<DepartmentReportPeriod> drpList = departmentReportPeriodService.getListByFilter(filter);
        if (!drpList.isEmpty()) {
            DepartmentReportPeriod drpLast = drpList.get(drpList.size()-1);
            //7А. Система проверяет наличие корректирующего периода для выбранного подразделения, созданного для выбранного периода корректировки.
            for (int i = 0; i<drpList.size(); i++) {
                DepartmentReportPeriod period = drpList.get(i);
                //7А.1А.1 Система проверяет найденный период (с совпадающим сроком подачи корректировки).
                if (period.getCorrectionDate().equals(term)) {
                    // Период открыт/закрыт и является/не является последним по порядку из корректирующих периодов.
                    if (!period.isActive()) {
                        return i == drpList.size()-1 ? PeriodStatusBeforeOpen.CLOSE : PeriodStatusBeforeOpen.INVALID;
                    } else {
                        return i == drpList.size()-1 ? PeriodStatusBeforeOpen.OPEN : PeriodStatusBeforeOpen.INVALID;
                    }
                } else if (period.getCorrectionDate().after(term)) {
                    return PeriodStatusBeforeOpen.INVALID;
    
                }
            }
        
            if (drpLast.isActive())
                return PeriodStatusBeforeOpen.CORRECTION_PERIOD_LAST_OPEN;
        }

        //Система проверяет статус периода корректировки (конкретный период ищем, т.е. д.б. одно значение)
        filter = new DepartmentReportPeriodFilter();
        filter.setIsCorrection(false);
        filter.setDepartmentIdList(Collections.singletonList(departmentId));
        filter.setReportPeriodIdList(Collections.singletonList(reportPeriod.getId()));
        List<DepartmentReportPeriod> onePeriod = departmentReportPeriodService.getListByFilter(filter);
        if (!onePeriod.isEmpty() && onePeriod.size()!=1){
            throw new ServiceException("Найдено больше одного периода корректировки с заданной датой корректировки.");
        } else if (onePeriod.size() == 1 && onePeriod.get(0).isActive()){
            return PeriodStatusBeforeOpen.CORRECTION_PERIOD_NOT_CLOSE;
        } else if (!onePeriod.get(0).isActive() && onePeriod.get(0).isBalance()) {
            return PeriodStatusBeforeOpen.CLOSE_AND_BALANCE;
        }

        return PeriodStatusBeforeOpen.NOT_EXIST;
    }

    @Override
    public void edit(int reportPeriodId, int oldDepartmentId, long newDictTaxPeriodId, int newYear, TaxType taxType, TAUserInfo user,
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

        String strBalance = isBalance ? " \"ввод остатков\"" : "";
        List<Integer> depIds = getAvailableDepartments(taxType, user.getUser(), Operation.EDIT, oldDepartmentId);
        if ((oldDepartmentId == departmentId) && (rp.getDictTaxPeriodId() == newDictTaxPeriodId) && (rp.getTaxPeriod().getYear() == newYear)) { // Изменился только ввод остатков
            DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
            filter.setReportPeriodIdList(Collections.singletonList(reportPeriodId));
            filter.setIsCorrection(false);
            filter.setDepartmentIdList(depIds);
            departmentReportPeriodService.updateBalance(departmentReportPeriodService.getListIdsByFilter(filter), isBalance);
            for (Integer depId : depIds) {
                logs.add(new LogEntry(LogLevel.INFO,
                        "Период с " + rp.getName() + " " + rp.getTaxPeriod().getYear() + (!isBalance ? " \"ввод остатков\"" : "") +
                                " был изменён на " + rp.getName() + strBalance + " для " + departmentService.getDepartment(depId).getName()));//<соответствующий календарный год>** + <"ввод остатков" *>**  для <Наименование подразделения>"));
            }

        } else {
            DepartmentReportPeriod dRP = departmentReportPeriodService.getFirst(oldDepartmentId, reportPeriodId);
            removePeriodWithLog(reportPeriodId, null, depIds, taxType, null);
            open(newYear, newDictTaxPeriodId, taxType, user, departmentId, null, isBalance, null);
            for (Integer depId : depIds) {
                logs.add(new LogEntry(LogLevel.INFO,
                        "Период с " + rp.getName() + " " + rp.getTaxPeriod().getYear() + (dRP.isBalance() ? " \"ввод остатков\"" : "") + " был изменён на " +
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
                filter.setReportPeriodIdList(Collections.singletonList(reportPeriodId));
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

    @Override
    public List<ReportPeriod> getReportPeriodsByDate(TaxType taxType, Date startDate, Date endDate) {
        return reportPeriodDao.getReportPeriodsByDate(taxType, startDate, endDate);
    }

    @Override
    public List<ReportPeriod> getReportPeriodsByDateAndDepartment(TaxType taxType, int depId, Date startDate, Date endDate) {
        return reportPeriodDao.getReportPeriodsByDateAndDepartment(taxType, depId, startDate, endDate);
    }

    @Override
    public boolean isFirstPeriod(int reportPeriodId) {
        ReportPeriod rp = getReportPeriod(reportPeriodId);
        Calendar sDate = Calendar.getInstance();
        sDate.setTime(rp.getCalendarStartDate());
        int month = sDate.get(Calendar.MONTH) + 1;
        return month == 1;
    }
}
