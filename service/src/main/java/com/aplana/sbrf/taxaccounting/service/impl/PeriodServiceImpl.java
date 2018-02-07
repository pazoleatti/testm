package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.cache.CacheManagerDecorator;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
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
import com.aplana.sbrf.taxaccounting.utils.SimpleDateUtils;
import net.sf.jasperreports.web.actions.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Сервис работы с периодами
 * Только этот сервис должен использоваться для работы с отчетными и налоговыми периодами
 */
@Service
@Transactional
public class PeriodServiceImpl implements PeriodService {

    private static final Long PERIOD_CODE_REF_BOOK = RefBook.Id.PERIOD_CODE.getId();

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
    private TAUserService userService;

    @Autowired
    private DeclarationDataSearchService declarationDataSearchService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private DepartmentReportPeriodDao departmentReportPeriodDao;

    @Autowired
    private DeclarationDataService declarationDataService;

    @Autowired
    private DeclarationTemplateService declarationTemplateService;

    @Autowired
    private CacheManagerDecorator cacheManagerDecorator;


    private static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

    @Override
    public List<ReportPeriod> fetchAllByTaxPeriod(int taxPeriodId) {
        return reportPeriodDao.fetchAllByTaxPeriod(taxPeriodId);
    }

    @Override
    @PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP')")
    public String open(DepartmentReportPeriod period) {
        List<LogEntry> logs = new ArrayList<LogEntry>();
        TaxPeriod taxPeriod = taxPeriodDao.fetchAllByYear(period.getReportPeriod().getTaxPeriod().getYear());
        if (taxPeriod == null) {
            taxPeriod = new TaxPeriod();
            taxPeriod.setYear(period.getReportPeriod().getTaxPeriod().getYear());
            taxPeriod.setId(taxPeriodDao.create(taxPeriod));
        }
        ReportPeriodType reportPeriodType = reportPeriodDao.getReportPeriodTypeById(period.getReportPeriod().getDictTaxPeriodId());
        ReportPeriod reportPeriod = reportPeriodDao.fetchOneByTaxPeriodAndDict(taxPeriod.getId(), reportPeriodType.getId());
        if (reportPeriod == null) {
            reportPeriod = new ReportPeriod();
            reportPeriod.setName(reportPeriodType.getName());

            // Устанавливаем дату начала, окончания и календарную дату начала периода
            // в соответствии с типом отчетного периода из справочника
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            Calendar start = Calendar.getInstance();
            start.setTime(reportPeriodType.getStartDate());
            start.set(Calendar.YEAR, period.getReportPeriod().getTaxPeriod().getYear());

            Calendar end = Calendar.getInstance();
            end.setTime(reportPeriodType.getEndDate());
            end.set(Calendar.YEAR, period.getReportPeriod().getTaxPeriod().getYear());

            Calendar calendarDate = Calendar.getInstance();
            calendarDate.setTime(reportPeriodType.getCalendarStartDate());
            calendarDate.set(Calendar.YEAR, period.getReportPeriod().getTaxPeriod().getYear());

            if (gregorianCalendar.isLeapYear(period.getReportPeriod().getTaxPeriod().getYear())) {
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
            reportPeriod.setStartDate(start.getTime());
            reportPeriod.setEndDate(end.getTime());
            reportPeriod.setCalendarStartDate(calendarDate.getTime());
            reportPeriod.setTaxPeriod(taxPeriod);
            reportPeriod.setDictTaxPeriodId(reportPeriodType.getId());
            reportPeriod = reportPeriodDao.fetchOne(reportPeriodDao.create(reportPeriod));
        }

        period.setIsActive(true);
        period.setReportPeriod(reportPeriod);
        saveOrOpen(period, getAvailableDepartments(userService.getCurrentUser(), Operation.OPEN), logs);
        return logEntryService.save(logs);
    }

    @Override
    @PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP')")
    public String close(Integer departmentReportPeriodId) {
        List<LogEntry> logs = new ArrayList<>();
        DepartmentReportPeriod drp = departmentReportPeriodDao.fetchOne(departmentReportPeriodId);
        if (drp == null) {
            throw new ServiceException("Период не найден. Возможно он был удалён. Попробуйте обновить страницу.");
        }

        DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
        int reportPeriodId = drp.getReportPeriod().getId();
        filter.setId(departmentReportPeriodId);
        filter.setReportPeriodIdList(Collections.singletonList(reportPeriodId));
        filter.setDepartmentIdList(getAvailableDepartments(userService.getCurrentUser(), Operation.CLOSE));
        filter.setIsActive(drp.isActive());
        if (drp.getCorrectionDate() == null) {
            filter.setIsCorrection(false);
        } else {
            filter.setIsCorrection(true);
            filter.setCorrectionDate(drp.getCorrectionDate());
        }
        departmentReportPeriodService.updateActive(departmentReportPeriodService.fetchAllIdsByFilter(filter), reportPeriodId, false);
        filter.setIsActive(null);
        List<DepartmentReportPeriod> drpList = departmentReportPeriodService.fetchAllByFilter(filter);
        for (DepartmentReportPeriod item : drpList) {
            if (!item.isActive()) {
                item.setReportPeriod(reportPeriodDao.fetchOne(item.getReportPeriod().getId()));
                int year = item.getReportPeriod().getTaxPeriod().getYear();
                logs.add(new LogEntry(LogLevel.INFO, "Период" + " \"" + item.getReportPeriod().getName() + "\" " +
                        year + " " +
                        "закрыт для \"" +
                        departmentService.getDepartment(item.getDepartmentId()).getName() +
                        "\""));
            }
        }
        String uuid = logEntryService.save(logs);
        for (DepartmentReportPeriod item : drpList) {
            cacheManagerDecorator.evict(CacheConstants.DEPARTMENT_REPORT_PERIOD, item.getId());
        }
        return uuid;
    }

    @Override
    public void saveOrOpen(DepartmentReportPeriod departmentReportPeriod, List<Integer> departmentIds, List<LogEntry> logs) {
        DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
        filter.setDepartmentIdList(Collections.singletonList(departmentService.getBankDepartment().getId()));
        filter.setReportPeriodIdList(Collections.singletonList(departmentReportPeriod.getReportPeriod().getId()));
        filter.setCorrectionDate(departmentReportPeriod.getCorrectionDate());

        List<DepartmentReportPeriod> departmentReportPeriodList = departmentReportPeriodService.fetchAllByFilter(filter);
        DepartmentReportPeriod savedDepartmentReportPeriod = null;
        if (departmentReportPeriodList.size() == 1) {
            savedDepartmentReportPeriod = departmentReportPeriodList.get(0);
        }

        if (savedDepartmentReportPeriod == null) { //не существует
            departmentReportPeriodService.create(departmentReportPeriod, departmentIds);
        } else if (!savedDepartmentReportPeriod.isActive()) { // существует и не открыт
            filter.setDepartmentIdList(departmentIds);
            departmentReportPeriodService.updateActive(departmentReportPeriodService.fetchAllIdsByFilter(filter), departmentReportPeriod.getReportPeriod().getId(), true);
        } else { // уже открыт
            return;
        }

        if (logs != null) {
            int year = departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear();
            for (Department department : departmentService.fetchAllDepartmentByIds(departmentIds)) {
                if (departmentReportPeriod.getCorrectionDate() == null) {
                    logOperation(logs, "Период " + "\"" + departmentReportPeriod.getReportPeriod().getName() + "\" "
                                    + departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear() +
                                    " открыт для \"%s\"",
                            department.getName());
                } else {
                    logOperation(logs, "Корректирующий период: " + departmentReportPeriod.getReportPeriod().getName()
                                    + " " + year + " открыт для \"%s\"",
                            department.getName());

                }
            }
        }


    }

    @Override
    public void saveOrOpen(DepartmentReportPeriod departmentReportPeriod, List<LogEntry> logs) {
        DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
        filter.setDepartmentIdList(Collections.singletonList(departmentReportPeriod.getDepartmentId()));
        filter.setReportPeriodIdList(Collections.singletonList(departmentReportPeriod.getReportPeriod().getId()));
        filter.setCorrectionDate(departmentReportPeriod.getCorrectionDate());

        List<DepartmentReportPeriod> departmentReportPeriodList = departmentReportPeriodService.fetchAllByFilter(filter);
        DepartmentReportPeriod savedDepartmentReportPeriod = null;
        if (departmentReportPeriodList.size() == 1) {
            savedDepartmentReportPeriod = departmentReportPeriodList.get(0);
        }

        if (savedDepartmentReportPeriod == null) { //не существует
            departmentReportPeriodService.create(departmentReportPeriod);
        } else if (!savedDepartmentReportPeriod.isActive()) { // существует и не открыт
            departmentReportPeriodService.updateActive(savedDepartmentReportPeriod.getId(), true);
        } else { // уже открыт
            return;
        }
        if (logs != null) {
            int year = departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear();
            if (departmentReportPeriod.getCorrectionDate() == null) {
                logOperation(logs, "Период " + "\"" + departmentReportPeriod.getReportPeriod().getName() + "\" "
                                + departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear() +
                                " открыт для \"%s\"",
                        departmentService.getDepartment(departmentReportPeriod.getDepartmentId()).getName());
            } else {
                logOperation(logs, "Корректирующий период: " + departmentReportPeriod.getReportPeriod().getName()
                                + " " + year + " открыт для \"%s\"",
                        departmentService.getDepartment(departmentReportPeriod.getDepartmentId()).getName());

            }
        }
    }

    @Override
    public Calendar getStartDate(int reportPeriodId) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(reportPeriodDao.fetchOne(reportPeriodId).getStartDate());
        return cal;
    }

    @Override
    public Calendar getEndDate(int reportPeriodId) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(reportPeriodDao.fetchOne(reportPeriodId).getEndDate());
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
    public ReportPeriod fetchReportPeriod(int reportPeriodId) {
        return reportPeriodDao.fetchOne(reportPeriodId);
    }


    @Override
    public Calendar getMonthStartDate(int reportPeriodId, int periodOrder) {
        Date date = fetchReportPeriod(reportPeriodId).getStartDate();
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + periodOrder - 1);
        return cal;
    }

    @Override
    public Calendar getMonthEndDate(int reportPeriodId, int periodOrder) {
        Date date = fetchReportPeriod(reportPeriodId).getStartDate();
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
        return departmentReportPeriodService.checkExistForDepartment(departmentId, reportPeriodId);
    }

    @Override
    public PeriodStatusBeforeOpen checkPeriodStatus(int year, int departmentId, long dictionaryTaxPeriodId) {
        TaxPeriod taxPeriod = taxPeriodDao.fetchAllByYear(year);
        if (taxPeriod == null) {
            return PeriodStatusBeforeOpen.NOT_EXIST;
        }

        List<ReportPeriod> reportPeriods = fetchAllByTaxPeriod(taxPeriod.getId());
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
        } else {
            if (existForDepartment(departmentId, reportPeriods.get(0).getId())) {
                DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
                filter.setReportPeriodIdList(Collections.singletonList(reportPeriods.get(0).getId()));
                filter.setDepartmentIdList(Collections.singletonList(departmentId));
                filter.setIsCorrection(false);
                List<DepartmentReportPeriod> departmentReportPeriodList = departmentReportPeriodService.fetchAllByFilter(filter);

                DepartmentReportPeriod drp = null;
                if (departmentReportPeriodList.size() == 1) {
                    drp = departmentReportPeriodList.get(0);
                }

                if (drp != null) {
                    filter = new DepartmentReportPeriodFilter();
                    filter.setReportPeriodIdList(Collections.singletonList(reportPeriods.get(0).getId()));
                    filter.setDepartmentIdList(Collections.singletonList(departmentId));
                    filter.setIsCorrection(true);
                    departmentReportPeriodList = departmentReportPeriodService.fetchAllByFilter(filter);
                    if (!departmentReportPeriodList.isEmpty()) {
                        return PeriodStatusBeforeOpen.CORRECTION_PERIOD_ALREADY_EXIST;
                    }
                    return drp.isActive() ? PeriodStatusBeforeOpen.OPEN : PeriodStatusBeforeOpen.CLOSE;
                }
            }
            return PeriodStatusBeforeOpen.NOT_EXIST;
        }
    }

    @Override
    public Set<ReportPeriod> getOpenReportPeriodForUser(TAUser user) {
        List<Integer> departments = departmentService.getTaxFormDepartments(user, TaxType.NDFL, null, null);
        if (user.hasRoles(TaxType.NDFL, TARole.N_ROLE_CONTROL_UNP, TARole.F_ROLE_CONTROL_UNP,
                TARole.N_ROLE_CONTROL_NS, TARole.F_ROLE_CONTROL_NS, TARole.N_ROLE_OPER, TARole.F_ROLE_OPER)) {
            return new LinkedHashSet<>(getOpenPeriodsByDepartments(departments, false));
        } else {
            return Collections.EMPTY_SET;
        }
    }

    @Override
    public void removeReportPeriod(Integer drpId, Logger logger, TAUserInfo userg) {
        removeReportPeriod(drpId, logger, userg, true);
    }

    @Override
    public String removeReportPeriod(Integer id, TAUserInfo userInfo) {
        Logger logger = new Logger();
        removeReportPeriod(id, logger, userInfo, true);
        return logEntryService.save(logger.getEntries());
    }

    @Override
    public String editPeriod(DepartmentReportPeriod departmentReportPeriod, TAUserInfo user) {

        List<Integer> departmentIds = getAvailableDepartments(userService.getCurrentUser(), PeriodService.Operation.EDIT);
        List<LogEntry> logs = new ArrayList<>();

        DeclarationDataFilter filter = new DeclarationDataFilter();
        filter.setDepartmentIds(departmentIds);
        filter.setReportPeriodIds(Collections.singletonList(departmentReportPeriodService.fetchOne(departmentReportPeriod.getId()).getReportPeriod().getId()));
        List<DeclarationData> declarations = declarationDataSearchService.getDeclarationData(filter, DeclarationDataSearchOrdering.ID, true);
        for (DeclarationData dd : declarations) {
            DeclarationTemplate dt = declarationTemplateService.get(dd.getDeclarationTemplateId());
            logs.add(new LogEntry(LogLevel.ERROR, "\"" + dt.getType().getName() + "\" в подразделении \"" +
                    departmentService.getDepartment(dd.getDepartmentId()).getName() + "\" находится в редактируемом периоде!"));
        }

        if (!declarations.isEmpty()) {
            return logEntryService.save(logs);
        } else {
            return edit(departmentReportPeriod, user);
        }


    }

    @Override
    public ReportPeriodType getPeriodTypeById(Long id) {
        return reportPeriodDao.getReportPeriodTypeById(id);
    }


    @Override
    public void updateDeadline(DepartmentReportPeriodFilter filter) throws ActionException {
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        TAUser userInfo = userService.getCurrentUser();
        DepartmentReportPeriod period = departmentReportPeriodService.fetchOne(filter.getId());
        String text = "%s назначил подразделению %s новый срок сдачи отчетности для %s в периоде %s %s года: %s";
        List<Notification> notifications = new ArrayList<>();
        if (filter.getDeadline() == null) {
            throw new ActionException("Дата сдачи отчетности должна быть указана!");
        }
        List<Department> departments = new ArrayList<>();
        if (filter.isWithChild()) {
            departments.addAll(departmentService.getAllChildren(filter.getDepartmentId()));
        } else {
            departments.add(departmentService.getDepartment(filter.getDepartmentId()));
        }
        for (Department department : departments) {
            Notification notification = new Notification();
            notification.setCreateDate(new Date());
            notification.setDeadline(filter.getDeadline());
            notification.setReportPeriodId(period.getReportPeriod().getId());
            notification.setSenderDepartmentId(null);
            notification.setReceiverDepartmentId(department.getId());
            notification.setText(String.format(text,
                    userInfo.getName(), departmentService.getParentsHierarchy(department.getId()), TaxTypeCase.fromCode(TaxType.NDFL.getCode()).getGenitive(),
                    period.getReportPeriod().getName(), period.getReportPeriod().getTaxPeriod().getYear(), df.format(filter.getDeadline())));

            notifications.add(notification);
        }
        notificationService.create(notifications);
    }


    private void removeReportPeriod(Integer drpId, Logger logger, TAUserInfo user, boolean fullLogging) {
        //Проверка форм не относится к этой постановке
        DepartmentReportPeriod drp = departmentReportPeriodService.fetchOne(drpId);
        if (drp == null) {
            throw new ServiceException("Период не найден. Возможно он был уже удалён. Попробуйте обновить страницу.");
        }

        List<Integer> departmentIds = departmentService.getBADepartmentIds(user.getUser());

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
                departmentReportPeriodService.checkExistLargeCorrection(drp.getDepartmentId(), reportPeriodId, drp.getCorrectionDate())) {
            logger.error("Удаление периода невозможно, т.к. существует более поздний корректирующий период!");
            return;
        } else if (drp.getCorrectionDate() == null) {
            //3 Существуют ли корректирующий период
            DepartmentReportPeriodFilter drpFilter = new DepartmentReportPeriodFilter();
            drpFilter.setIsCorrection(true);
            drpFilter.setReportPeriodIdList(Collections.singletonList(reportPeriodId));
            drpFilter.setDepartmentIdList(Collections.singletonList(drp.getDepartmentId()));
            List<Integer> corrIds = departmentReportPeriodService.fetchAllIdsByFilter(drpFilter);
            if (!corrIds.isEmpty()) {
                logger.error("Удаление периода невозможно, т.к. для него существует корректирующий период!");
                return;
            }
        }
        if (logger.containsLevel(LogLevel.ERROR)) {
            return;
        }
        List<Integer> departments = getAvailableDepartments(user.getUser(), Operation.DELETE);
        removePeriodWithLog(drp.getReportPeriod(), drp.getCorrectionDate(), drp.getDepartmentId(), departments, logger.getEntries(), fullLogging);
    }

    private void removePeriodWithLog(ReportPeriod reportPeriod, Date correctionDate, Integer departmentId, List<Integer> departmentIds, List<LogEntry> logs, boolean fullLogging) {
        DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
        filter.setCorrectionDate(correctionDate);
        filter.setDepartmentIdList(departmentIds);
        filter.setReportPeriodIdList(Collections.singletonList(reportPeriod.getId()));
        List<Integer> drpIds = departmentReportPeriodService.fetchAllIdsByFilter(filter);
        departmentReportPeriodService.delete(drpIds);
        if (fullLogging) {
            for (Integer id : departmentIds) {
                if (logs != null) {
                    logOperation(logs, "Период удалён для " + "\"%s\"",
                            departmentService.getDepartment(id).getName());
                }
            }
            if (logs != null) {
                logs.add(new LogEntry(LogLevel.INFO,
                        "Удален период \"" + reportPeriod.getName() + "\" " + reportPeriod.getTaxPeriod().getYear() +
                                (correctionDate == null ? "" : " с датой сдачи корректировки " + sdf.get().format(correctionDate))
                ));
            }
        } else {
            if (logs != null) {
                logs.add(new LogEntry(LogLevel.INFO,
                        String.format("Удален период \"%s, %s\" для подразделений \"%s\"",
                                reportPeriod.getTaxPeriod().getYear(), reportPeriod.getName(),
                                departmentService.getDepartment(departmentId).getName())
                ));
            }
        }

        notificationService.deleteByReportPeriod(reportPeriod.getId());

        boolean canRemoveReportPeriod = true;
        for (Department dep : departmentService.listAll()) {
            if (existForDepartment(dep.getId(), reportPeriod.getId())) {
                canRemoveReportPeriod = false;
                break;
            }
        }

        if (canRemoveReportPeriod) {
            TaxPeriod tp = reportPeriod.getTaxPeriod();
            if (reportPeriodDao.fetchAllByTaxPeriod(tp.getId()).isEmpty()) {
                taxPeriodDao.delete(tp.getId());
            }

            reportPeriodDao.remove(reportPeriod.getId());
        }
    }

    private void removePeriodWithLog(int reportPeriodId, Date correctionDate, Integer departmentId, List<Integer> departmentIds, List<LogEntry> logs, boolean fullLogging) {
        removePeriodWithLog(reportPeriodDao.fetchOne(reportPeriodId), correctionDate, departmentId, departmentIds, logs, fullLogging);
    }

    public List<Integer> getAvailableDepartments(TAUser user, Operation operation) {
        if (user.hasRoles(TaxType.NDFL, TARole.N_ROLE_CONTROL_UNP, TARole.F_ROLE_CONTROL_UNP)) {
            switch (operation) {
                case FIND:
                    List<Integer> dep = new ArrayList<>();
                    dep.add(departmentService.getBankDepartment().getId());
                    return dep;
                case OPEN:
                case CLOSE:
                case DELETE:
                case EDIT:
                case EDIT_DEADLINE:
                    return departmentService.getBADepartmentIds(user);
            }
        } else if (user.hasRoles(TaxType.NDFL, TARole.N_ROLE_CONTROL_NS, TARole.F_ROLE_CONTROL_NS)) {

            switch (operation) {
                case FIND:
                    return departmentService.getTBDepartmentIds(user, TaxType.NDFL, true);
                case EDIT_DEADLINE:
                    return departmentService.getBADepartmentIds(user);
            }

        }
        return Collections.EMPTY_LIST;
    }


    @Override
    public List<ReportPeriod> getPeriodsByDepartments(List<Integer> departmentList) {
        return reportPeriodDao.fetchAllByDepartments(departmentList);
    }


    @Override
    public List<ReportPeriod> getOpenPeriodsByDepartments(List<Integer> departmentList, boolean withoutCorrect) {
        return reportPeriodDao.getOpenPeriodsAndDepartments(departmentList, withoutCorrect);
    }

    @Override
    public List<ReportPeriod> getCorrectPeriods(int departmentId) {
        return reportPeriodDao.getCorrectPeriods(departmentId);
    }


    @Override
    public void openCorrectionPeriod(ReportPeriod reportPeriod, int departmentId, Date term, TAUserInfo user, List<LogEntry> logs) {
        for (Integer depId : getAvailableDepartments(user.getUser(), Operation.OPEN)) {
            DepartmentReportPeriod drp = new DepartmentReportPeriod();
            drp.setIsActive(true);
            drp.setReportPeriod(reportPeriod);
            drp.setDepartmentId(depId);
            drp.setCorrectionDate(term);
            saveOrOpen(drp, logs);
        }

    }

    @Override
    public String openCorrectionPeriod(DepartmentReportPeriod period) {
        List<LogEntry> logs = new ArrayList<LogEntry>();
        DepartmentReportPeriod periodInDb = departmentReportPeriodDao.fetchOne(period.getId());
        period.setCorrectionDate(SimpleDateUtils.toStartOfDay(period.getCorrectionDate()));

        if (checkPeriodStatusBeforeOpen(period.getReportPeriod(), period.getDepartmentId(), period.getCorrectionDate()).equals(PeriodStatusBeforeOpen.CORRECTION_PERIOD_LAST_OPEN)){
            logs.add(new LogEntry(LogLevel.ERROR, "Корректирующий период с датой корректировки "  + new SimpleDateFormat("dd.MM.yyyy").format(period.getCorrectionDate())
                    +"  не может быть открыт, т.к. открыт более ранний корректирующий период!"));
            return logEntryService.save(logs);
        }

        // Поиск последнего отчетного периода данного подразделения для того же отчетного периода
        DepartmentReportPeriod lastDrp = departmentReportPeriodService.fetchLast(periodInDb.getDepartmentId(), periodInDb.getReportPeriod().getId());

        // Открываемый корректирующий период должен быть самым поздним
        if(lastDrp.getCorrectionDate() == null || lastDrp.getCorrectionDate().before(period.getCorrectionDate())) {
            if (periodInDb.getCorrectionDate() == null) {
                for (Integer depId : getAvailableDepartments(userService.getCurrentUser(), Operation.OPEN)) {
                    period.setIsActive(true);
                    period.setDepartmentId(depId);
                    saveOrOpen(period, logs);
                }
            }
        } else {
            throw new ServiceException("Корректирующий период с датой корректировки " + sdf.get().format(period.getCorrectionDate()) + " не может быть открыт, т.к. существует более поздний корректирующий период!");
        }

        return logEntryService.save(logs);
    }

    @Override
    public PeriodStatusBeforeOpen checkPeriodStatusBeforeOpen(ReportPeriod reportPeriod, int departmentId, Date term) {
        DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
        filter.setDepartmentId(departmentId);
        filter.setReportPeriod(reportPeriod);
        filter.setIsCorrection(true);
        List<DepartmentReportPeriod> drpList = departmentReportPeriodService.fetchAllByFilter(filter);
        if (!drpList.isEmpty()) {
            DepartmentReportPeriod drpLast = drpList.get(drpList.size() - 1);
            //7А. Система проверяет наличие корректирующего периода для выбранного подразделения, созданного для выбранного периода корректировки.
            for (int i = 0; i < drpList.size(); i++) {
                DepartmentReportPeriod period = drpList.get(i);
                //7А.1А.1 Система проверяет найденный период (с совпадающим сроком подачи корректировки).
                if (period.getCorrectionDate().equals(term)) {
                    // Период открыт/закрыт и является/не является последним по порядку из корректирующих периодов.
                    if (!period.isActive()) {
                        return i == drpList.size() - 1 ? PeriodStatusBeforeOpen.CLOSE : PeriodStatusBeforeOpen.INVALID;
                    } else {
                        return i == drpList.size() - 1 ? PeriodStatusBeforeOpen.OPEN : PeriodStatusBeforeOpen.INVALID;
                    }
                } else if (period.getCorrectionDate().after(term)) {
                    return drpLast.isActive() ? PeriodStatusBeforeOpen.CORRECTION_PERIOD_BEFORE_OPEN : PeriodStatusBeforeOpen.INVALID;

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
        List<DepartmentReportPeriod> onePeriod = departmentReportPeriodService.fetchAllByFilter(filter);
        if (!onePeriod.isEmpty() && onePeriod.size() != 1) {
            throw new ServiceException("Найдено больше одного периода корректировки с заданной датой корректировки.");
        } else if (onePeriod.size() == 1 && onePeriod.get(0).isActive()) {
            return PeriodStatusBeforeOpen.CORRECTION_PERIOD_NOT_CLOSE;
        }

        return PeriodStatusBeforeOpen.NOT_EXIST;
    }

    @Override
    public String edit(DepartmentReportPeriod departmentReportPeriod, TAUserInfo userInfo) {
        List<LogEntry> logs = new ArrayList<>();

        TaxPeriod taxPeriod = taxPeriodDao.fetchAllByYear(departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear());
        if (taxPeriod == null) {
            taxPeriod = new TaxPeriod();
            taxPeriod.setYear(departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear());
            taxPeriod.setId(taxPeriodDao.create(taxPeriod));
        }
        ReportPeriodType reportPeriodType = reportPeriodDao.getReportPeriodTypeById(departmentReportPeriod.getReportPeriod().getDictTaxPeriodId());
        ReportPeriod reportPeriod = reportPeriodDao.fetchOneByTaxPeriodAndDict(taxPeriod.getId(), reportPeriodType.getId());
        if (reportPeriod == null) {
            reportPeriod = new ReportPeriod();
            reportPeriod.setName(reportPeriodType.getName());
            reportPeriod.setStartDate(reportPeriodType.getStartDate());
            reportPeriod.setEndDate(reportPeriodType.getEndDate());
            reportPeriod.setCalendarStartDate(reportPeriodType.getCalendarStartDate());
            reportPeriod.setTaxPeriod(taxPeriod);
            reportPeriod.setDictTaxPeriodId(reportPeriodType.getId());
            reportPeriod = reportPeriodDao.fetchOne(reportPeriodDao.create(reportPeriod));
        }
        departmentReportPeriod.setReportPeriod(reportPeriod);

        removeReportPeriod(departmentReportPeriod.getId(), userInfo);
        open(departmentReportPeriod);
        logs.add(new LogEntry(LogLevel.INFO,
                String.format("Период изменён на \"%s, %s\" для подразделений \"%s\"",
                        reportPeriod.getTaxPeriod().getYear(), reportPeriod.getName(),
                        departmentService.getBankDepartment().getName())));


        return logEntryService.save(logs);

    }

    @Override
    public void edit(int reportPeriodId, int oldDepartmentId, long newDictTaxPeriodId, int newYear, TAUserInfo user,
                     int departmentId, List<LogEntry> logs) {
        ReportPeriod rp = fetchReportPeriod(reportPeriodId);
        RefBook refBook = rbFactory.get(PERIOD_CODE_REF_BOOK);
        RefBookDataProvider provider = rbFactory.getDataProvider(refBook.getId());
        Map<String, RefBookValue> dictTaxPeriod;
        try {
            dictTaxPeriod = provider.getRecordData(newDictTaxPeriodId);
        } catch (DaoException ex) {
            throw new ServiceException(ex.getMessage());
        }

        List<Integer> depIds = getAvailableDepartments(user.getUser(), Operation.EDIT);
        if ((oldDepartmentId == departmentId) && (rp.getDictTaxPeriodId() == newDictTaxPeriodId) && (rp.getTaxPeriod().getYear() == newYear)) { // Изменился только ввод остатков
            DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
            filter.setReportPeriodIdList(Collections.singletonList(reportPeriodId));
            filter.setIsCorrection(false);
            filter.setDepartmentIdList(depIds);
            logs.add(new LogEntry(LogLevel.INFO,
                    String.format("Период \"%s, %s\" изменён на \"%s, %s\" для подразделений \"%s\"",
                            rp.getTaxPeriod().getYear(), rp.getName(),
                            rp.getTaxPeriod().getYear(), rp.getName(),
                            departmentService.getDepartment(oldDepartmentId).getName())));

        } else {
            boolean departmentHasBeenChanged = oldDepartmentId != departmentId;
            removePeriodWithLog(reportPeriodId, null, oldDepartmentId, depIds, departmentHasBeenChanged ? logs : null, false);
            DepartmentReportPeriod open = new DepartmentReportPeriod();
            open.setDepartmentId(departmentId);
            open.setReportPeriod(new ReportPeriod());
            open.getReportPeriod().setTaxPeriod(new TaxPeriod());
            open.getReportPeriod().setDictTaxPeriodId(newDictTaxPeriodId);
            open.getReportPeriod().getTaxPeriod().setYear(newYear);
            open(open);
            if (!departmentHasBeenChanged) {
                logs.add(new LogEntry(LogLevel.INFO,
                        String.format("Период \"%s, %s\" изменён на \"%s, %s\" для подразделений \"%s\"",
                                rp.getTaxPeriod().getYear(), rp.getName(),
                                newYear, dictTaxPeriod.get("NAME").getStringValue(),
                                departmentService.getDepartment(oldDepartmentId).getName())));
            }
        }
    }

    @Override
    public List<DepartmentReportPeriod> getDRPByDepartmentIds(List<Integer> departmentIds) {
        if (departmentIds == null || departmentIds.isEmpty()) {
            return new ArrayList<>(0);
        }

        DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
        filter.setDepartmentIdList(departmentIds);

        return departmentReportPeriodService.fetchAllByFilter(filter);
    }

    @Override
    public ReportPeriod getByDictCodeAndYear(String code, int year) {
        return reportPeriodDao.getByTaxTypedCodeYear(code, year);
    }

    @Override
    public List<ReportPeriodType> getPeriodType() {
        return reportPeriodDao.getPeriodType();
    }

    @Override
    public List<ReportPeriod> getReportPeriodsByDate(Date startDate, Date endDate) {
        return reportPeriodDao.getReportPeriodsByDate(startDate, endDate);
    }

    private void logOperation(List<LogEntry> logs, String message, String departmentName) {
        if (departmentName != null && !departmentName.trim().isEmpty()) {
            logs.add(new LogEntry(LogLevel.INFO, String.format(message, departmentName)));
        }
    }
}
