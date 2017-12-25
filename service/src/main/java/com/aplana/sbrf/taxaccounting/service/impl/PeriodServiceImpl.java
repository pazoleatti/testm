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
import net.sf.jasperreports.web.actions.ActionException;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Сервис работы с периодами
 * <p>
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
    public List<ReportPeriod> listByTaxPeriod(int taxPeriodId) {
        return reportPeriodDao.listByTaxPeriod(taxPeriodId);
    }

    @Override
    public DepartmentReportPeriod getLastReportPeriod(TaxType taxType, int departmentId) {
        // TODO: Нужно получить последний открытый для этого подразделения и типа налога.
        return null;
    }

    @Override
    @PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP')")
    public String open(DepartmentReportPeriod period) {
        List<LogEntry> logs = new ArrayList<LogEntry>();
        TaxPeriod taxPeriod = taxPeriodDao.getByTaxTypeAndYear(TaxType.NDFL, period.getReportPeriod().getTaxPeriod().getYear());
        if (taxPeriod == null) {
            taxPeriod = new TaxPeriod();
            taxPeriod.setYear(period.getReportPeriod().getTaxPeriod().getYear());
            taxPeriod.setId(taxPeriodDao.add(taxPeriod));
        }
        ReportPeriodType reportPeriodType = reportPeriodDao.getReportPeriodType(period.getReportPeriod().getDictTaxPeriodId());
        ReportPeriod reportPeriod = reportPeriodDao.getByTaxPeriodAndDict(taxPeriod.getId(), reportPeriodType.getId());
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
            reportPeriod = reportPeriodDao.get(reportPeriodDao.save(reportPeriod));
        }

        period.setIsActive(true);
        period.setReportPeriod(reportPeriod);
        for (int id : getAvailableDepartments(TaxType.NDFL, userService.getCurrentUser(), Operation.OPEN, null)) {
            period.setDepartmentId(id);
            saveOrOpen(period, logs);
        }
        return logEntryService.save(logs);
    }

    @Override
    @PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP')")
    public String close(Integer departmentReportPeriodId) {
        List<LogEntry> logs = new ArrayList<>();
        DepartmentReportPeriod drp = departmentReportPeriodDao.get(departmentReportPeriodId);
        if (drp == null) {
            throw new ServiceException("Период не найден. Возможно он был удалён. Попробуйте обновить страницу.");
        }

        DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
        int reportPeriodId = drp.getReportPeriod().getId();
        filter.setId(departmentReportPeriodId);
        filter.setReportPeriodIdList(Collections.singletonList(reportPeriodId));
        filter.setDepartmentIdList(getAvailableDepartments(TaxType.NDFL, userService.getCurrentUser(), Operation.CLOSE, drp.getDepartmentId()));
        if (drp.getCorrectionDate() == null) {
            filter.setIsCorrection(false);
        } else {
            filter.setCorrectionDate(drp.getCorrectionDate());
        }
        departmentReportPeriodService.updateActive(departmentReportPeriodService.getListIdsByFilter(filter), reportPeriodId, false);
        List<DepartmentReportPeriod> drpList = departmentReportPeriodService.getListByFilter(filter);
        for (DepartmentReportPeriod item : drpList) {
            if (!item.isActive()) {
                item.setReportPeriod(reportPeriodDao.get(item.getReportPeriod().getId()));
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
    public void saveOrOpen(DepartmentReportPeriod departmentReportPeriod, List<Integer> departmentIds, List<LogEntry> logs, boolean fullLogging) {
        Integer departmentId = departmentReportPeriod.getDepartmentId();
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
            departmentReportPeriodService.updateActive(reportIdsForUpdate, reportPeriodId, true);
        }

        if (logs != null) {
            if (fullLogging) {
                for (DepartmentReportPeriod period : departmentReportPeriodService.getListByFilter(filter)) {
                    int year = period.getReportPeriod().getTaxPeriod().getYear();
                    if (period.getCorrectionDate() == null) {
                        logOperation(logs, "Период " + "\"" + period.getReportPeriod().getName() + "\" "
                                        + period.getReportPeriod().getTaxPeriod().getYear()
                                        + " открыт для \"%s\"",
                                departmentService.getDepartment(period.getDepartmentId()).getName());
                    } else {
                        logOperation(logs, "Корректирующий период: " + period.getReportPeriod().getName()
                                        + " " + year + " открыт для \"%s\"",
                                departmentService.getDepartment(period.getDepartmentId()).getName());
                    }
                }
            } else {
                logs.add(new LogEntry(LogLevel.INFO,
                        String.format("Открыт период \"%s, %s\" для подразделений \"%s\"",
                                departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                                departmentReportPeriod.getReportPeriod().getName(),
                                departmentService.getDepartment(departmentId).getName())
                ));
            }
        }
    }

    @Override
    public TaxPeriod getTaxPeriod(int taxPeriodId) {
        return taxPeriodDao.get(taxPeriodId);
    }

    @Override
    public Calendar getStartDate(int reportPeriodId) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(reportPeriodDao.get(reportPeriodId).getStartDate());
        return cal;
    }

    @Override
    public Calendar getEndDate(int reportPeriodId) {
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
        if (backOrder) {
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
    public PeriodStatusBeforeOpen checkPeriodStatusBeforeOpen(TaxType taxType, int year, int departmentId, long dictionaryTaxPeriodId) {
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
        } else {
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
                    filter = new DepartmentReportPeriodFilter();
                    filter.setReportPeriodIdList(Collections.singletonList(reportPeriods.get(0).getId()));
                    filter.setDepartmentIdList(Collections.singletonList(departmentId));
                    filter.setIsCorrection(true);
                    departmentReportPeriodList = departmentReportPeriodService.getListByFilter(filter);
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
    public Set<ReportPeriod> getOpenForUser(TAUser user, TaxType taxType) {
        List<Integer> departments = departmentService.getTaxFormDepartments(user, taxType, null, null);
        if (user.hasRoles(taxType, TARole.N_ROLE_CONTROL_UNP, TARole.F_ROLE_CONTROL_UNP,
                TARole.N_ROLE_CONTROL_NS, TARole.F_ROLE_CONTROL_NS)) {
            return new LinkedHashSet<ReportPeriod>(getOpenPeriodsByTaxTypeAndDepartments(taxType, departments, false));
        } else if (user.hasRoles(taxType, TARole.N_ROLE_OPER, TARole.F_ROLE_OPER)) {
            return new LinkedHashSet<ReportPeriod>(getOpenPeriodsByTaxTypeAndDepartments(taxType, departments, false));
        } else {
            return Collections.EMPTY_SET;
        }
    }

    //http://conf.aplana.com/pages/viewpage.action?pageId=11389882#id-Формаспискапериодов-Удалениепериода
    @Override
    public void removeReportPeriod(TaxType taxType, Integer drpId, Logger logger, TAUserInfo userg) {
        removeReportPeriod(taxType, drpId, logger, userg, true);
    }

    @Override
    public String removeReportPeriod(Integer[] ids, TAUserInfo userInfo) {
        Logger logger = new Logger();
        for (Integer id : ids) {
            removeReportPeriod(TaxType.NDFL, id, logger, userInfo, true);
        }
        return logEntryService.save(logger.getEntries());
    }

    @Override
    public String removeReportPeriod(Integer id, TAUserInfo userInfo) {
        Logger logger = new Logger();
        removeReportPeriod(TaxType.NDFL, id, logger, userInfo, true);
        return logEntryService.save(logger.getEntries());
    }

    @Override
    public String editPeriod(DepartmentReportPeriod departmentReportPeriod, TAUserInfo user) {

        List<Integer> departmentIds = getAvailableDepartments(TaxType.NDFL, userService.getCurrentUser(), PeriodService.Operation.EDIT, departmentReportPeriod.getDepartmentId());
        List<LogEntry> logs = new ArrayList<>();

        DeclarationDataFilter filter = new DeclarationDataFilter();
        filter.setDepartmentIds(departmentIds);
        filter.setReportPeriodIds(Collections.singletonList(departmentReportPeriodService.get(departmentReportPeriod.getId()).getReportPeriod().getId()));
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
        return reportPeriodDao.getPeriodTypeById(id);
    }



    @Override
    public void setDeadline(DepartmentReportPeriodFilter filter) throws ActionException {
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        TAUser userInfo = userService.getCurrentUser();
        DepartmentReportPeriod period = departmentReportPeriodService.get(filter.getId());
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
            notification.setCreateDate(new LocalDateTime());
            notification.setDeadline(filter.getDeadline());
            notification.setReportPeriodId(period.getReportPeriod().getId());
            notification.setSenderDepartmentId(null);
            notification.setReceiverDepartmentId(department.getId());
            notification.setText(String.format(text,
                    userInfo.getName(), departmentService.getParentsHierarchy(department.getId()), TaxTypeCase.fromCode(TaxType.NDFL.getCode()).getGenitive(),
                    period.getReportPeriod().getName(), period.getReportPeriod().getTaxPeriod().getYear(), df.format(filter.getDeadline().toDate())));

            notifications.add(notification);
        }
        notificationService.saveList(notifications);
    }


    private void removeReportPeriod(TaxType taxType, Integer drpId, Logger logger, TAUserInfo user, boolean fullLogging) {
        //Проверка форм не относится к этой постановке
        List<Integer> departmentIds = new ArrayList<Integer>();
        DepartmentReportPeriod drp = departmentReportPeriodService.get(drpId);
        if (drp == null) {
            throw new ServiceException("Период не найден. Возможно он был уже удалён. Попробуйте обновить страницу.");
        }

        departmentIds = departmentService.getBADepartmentIds(user.getUser());

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
                departmentReportPeriodService.existLargeCorrection(drp.getDepartmentId(), reportPeriodId, drp.getCorrectionDate())) {
            logger.error("Удаление периода невозможно, т.к. существует более поздний корректирующий период!");
            return;
        } else if (drp.getCorrectionDate() == null) {
            //3 Существуют ли корректирующий период
            DepartmentReportPeriodFilter drpFilter = new DepartmentReportPeriodFilter();
            drpFilter.setIsCorrection(true);
            drpFilter.setReportPeriodIdList(Collections.singletonList(reportPeriodId));
            drpFilter.setDepartmentIdList(Collections.singletonList(drp.getDepartmentId()));
            List<Integer> corrIds = departmentReportPeriodService.getListIdsByFilter(drpFilter);
            if (!corrIds.isEmpty()) {
                logger.error("Удаление периода невозможно, т.к. для него существует корректирующий период!");
                return;
            }
        }
        if (logger.containsLevel(LogLevel.ERROR)) {
            return;
        }
        List<Integer> departments = getAvailableDepartments(taxType, user.getUser(), Operation.DELETE, drp.getDepartmentId());
        removePeriodWithLog(drp.getReportPeriod(), drp.getCorrectionDate(), drp.getDepartmentId(), departments, taxType, logger.getEntries(), fullLogging);
    }

    private void removePeriodWithLog(ReportPeriod reportPeriod, Date correctionDate, Integer departmentId, List<Integer> departmentIds, TaxType taxType, List<LogEntry> logs, boolean fullLogging) {
        DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
        filter.setCorrectionDate(correctionDate);
        filter.setDepartmentIdList(departmentIds);
        filter.setReportPeriodIdList(Collections.singletonList(reportPeriod.getId()));
        List<Integer> drpIds = departmentReportPeriodService.getListIdsByFilter(filter);
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
            if (reportPeriodDao.listByTaxPeriod(tp.getId()).isEmpty()) {
                taxPeriodDao.delete(tp.getId());
            }

            reportPeriodDao.remove(reportPeriod.getId());
        }
    }

    private void removePeriodWithLog(int reportPeriodId, Date correctionDate, Integer departmentId, List<Integer> departmentIds, TaxType taxType, List<LogEntry> logs, boolean fullLogging) {
        removePeriodWithLog(reportPeriodDao.get(reportPeriodId), correctionDate, departmentId, departmentIds, taxType, logs, fullLogging);
    }

    public List<Integer> getAvailableDepartments(TaxType taxType, TAUser user, Operation operation, Integer departmentId) {
        if (user.hasRoles(taxType, TARole.N_ROLE_CONTROL_UNP, TARole.F_ROLE_CONTROL_UNP)) {
            switch (taxType) {
                case NDFL:
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
            }
        } else if (user.hasRoles(taxType, TARole.N_ROLE_CONTROL_NS, TARole.F_ROLE_CONTROL_NS)) {
            switch (taxType) {
                case NDFL:
                    switch (operation) {
                        case FIND:
                            return departmentService.getTBDepartmentIds(user, taxType, true);
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
     *
     * @param reportPeriodId идентификатор отчетного период
     * @return список доступных месяцев
     */
    @Override
    public List<Months> getAvailableMonthList(int reportPeriodId) {

        RefBookDataProvider dataProvider = rbFactory.getDataProvider(RefBook.Id.PERIOD_CODE.getId());

        ReportPeriod reportPeriod = getReportPeriod(reportPeriodId);

        List<Months> monthsList = new ArrayList<Months>();
        monthsList.add(null);

        Map<String, RefBookValue> refBookValueMap = dataProvider.getRecordData(reportPeriod.getDictTaxPeriodId());

        monthsList.addAll(getReportPeriodMonthList(refBookValueMap));

        return monthsList;
    }

    /**
     * Получить упорядоченный список месяцев соответствующий
     *
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
    public List<ReportPeriod> getOpenPeriodsByTaxTypeAndDepartments(TaxType taxType, List<Integer> departmentList, boolean withoutCorrect) {
        return reportPeriodDao.getOpenPeriodsByTaxTypeAndDepartments(taxType, departmentList, withoutCorrect);
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
        if (thisReportPeriod.getOrder() == 1 && !reportPeriodList.isEmpty() && reportPeriodList.get(0).getId() == reportPeriodId) {
            List<TaxPeriod> taxPeriodList = taxPeriodDao.listByTaxType(TaxType.NDFL);
            for (int i = 0; i < taxPeriodList.size(); i++) {
                if (taxPeriodList.get(i).getId().equals(thisTaxPeriod.getId())) {
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
            for (int i = 1; i < reportPeriodList.size(); i++) {
                if (reportPeriodList.get(i).getId().equals(reportPeriodId)) {
                    return reportPeriodList.get(i - 1);
                }
            }
        }
        return null;
    }

    @Override
    public PagingResult<ReportPeriod> getCorrectPeriods(TaxType taxType, int departmentId, PagingParams pagingParams) {
        return reportPeriodDao.getCorrectPeriods(taxType, departmentId, pagingParams);
    }

    @Override
    public List<ReportPeriod> getComparativPeriods(TaxType taxType, int departmentId) {
        return reportPeriodDao.getComparativPeriods(taxType, departmentId);
    }

    @Override
    public void openCorrectionPeriod(TaxType taxType, ReportPeriod reportPeriod, int departmentId, Date term, TAUserInfo user, List<LogEntry> logs) {
        for (Integer depId : getAvailableDepartments(taxType, user.getUser(), Operation.OPEN, departmentId)) {
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
        DepartmentReportPeriod periodInDb = departmentReportPeriodDao.get(period.getId());
        if (periodInDb.getCorrectionDate() == null) {
            for (Integer depId : getAvailableDepartments(TaxType.NDFL, userService.getCurrentUser(), Operation.OPEN, period.getDepartmentId())) {
                period.setIsActive(true);
                period.setDepartmentId(depId);
                saveOrOpen(period, logs);
            }
        } else {
            editCorrectionPeriod(logs, period, periodInDb);
        }
        return logEntryService.save(logs);
    }

    @Override
    public PeriodStatusBeforeOpen checkPeriodStatusBeforeOpen(ReportPeriod reportPeriod, int departmentId, Date term) {
        DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
        filter.setDepartmentIdList(Collections.singletonList(departmentId));
        filter.setReportPeriodIdList(Collections.singletonList(reportPeriod.getId()));
        filter.setIsCorrection(true);
        List<DepartmentReportPeriod> drpList = departmentReportPeriodService.getListByFilter(filter);
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
        if (!onePeriod.isEmpty() && onePeriod.size() != 1) {
            throw new ServiceException("Найдено больше одного периода корректировки с заданной датой корректировки.");
        } else if (onePeriod.size() == 1 && onePeriod.get(0).isActive()) {
            return PeriodStatusBeforeOpen.CORRECTION_PERIOD_NOT_CLOSE;
        }

        return PeriodStatusBeforeOpen.NOT_EXIST;
    }

    @Override
    public String edit(DepartmentReportPeriod departmentReportPeriod, TAUserInfo userInfo) {
        List<LogEntry> logs = new ArrayList<LogEntry>();

        TaxPeriod taxPeriod = taxPeriodDao.getByTaxTypeAndYear(TaxType.NDFL, departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear());
        if (taxPeriod == null) {
            taxPeriod = new TaxPeriod();
            taxPeriod.setYear(departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear());
            taxPeriod.setId(taxPeriodDao.add(taxPeriod));
        }
        ReportPeriodType reportPeriodType = reportPeriodDao.getReportPeriodType(departmentReportPeriod.getReportPeriod().getDictTaxPeriodId());
        ReportPeriod reportPeriod = reportPeriodDao.getByTaxPeriodAndDict(taxPeriod.getId(), reportPeriodType.getId());
        if (reportPeriod == null) {
            reportPeriod = new ReportPeriod();
            reportPeriod.setName(reportPeriodType.getName());
            reportPeriod.setStartDate(reportPeriodType.getStartDate());
            reportPeriod.setEndDate(reportPeriodType.getEndDate());
            reportPeriod.setCalendarStartDate(reportPeriodType.getCalendarStartDate());
            reportPeriod.setTaxPeriod(taxPeriod);
            reportPeriod.setDictTaxPeriodId(reportPeriodType.getId());
            reportPeriod = reportPeriodDao.get(reportPeriodDao.save(reportPeriod));
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
    public void editCorrectionPeriod(List<LogEntry> logs, DepartmentReportPeriod newPeriod, DepartmentReportPeriod oldPeriod) {
        if ((oldPeriod.getReportPeriod().getId().equals(newPeriod.getReportPeriod().getId()))) { // изменилась только дата корректировки
            PeriodStatusBeforeOpen status = checkPeriodStatusBeforeOpen(oldPeriod.getReportPeriod(), newPeriod.getDepartmentId(), newPeriod.getCorrectionDate());
            if (status == PeriodStatusBeforeOpen.NOT_EXIST) {
                List<Integer> depIds = getAvailableDepartments(TaxType.NDFL, userService.getCurrentUser(), Operation.EDIT, newPeriod.getDepartmentId());
                DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
                filter.setCorrectionDate(oldPeriod.getCorrectionDate());
                filter.setReportPeriodIdList(Collections.singletonList(oldPeriod.getReportPeriod().getId()));
                filter.setDepartmentIdList(depIds);
                List<DepartmentReportPeriod> drps = departmentReportPeriodService.getListByFilter(filter);
                for (DepartmentReportPeriod drp : drps) {
                    departmentReportPeriodService.updateCorrectionDate(drp.getId(), newPeriod.getCorrectionDate());
                    logOperation(logs, "Корректирующий период с " + oldPeriod.getReportPeriod().getName() + " " + oldPeriod.getReportPeriod().getTaxPeriod().getYear()
                                    + " был изменён на " + newPeriod.getReportPeriod().getName() + " для \"%s\"",
                            departmentService.getDepartment(drp.getDepartmentId()).getName());//<соответствующий календарный год>** + <"ввод остатков" *>**  для <Наименование подразделения>"));
                }
            }
        } else {
            PeriodStatusBeforeOpen status = checkPeriodStatusBeforeOpen(newPeriod.getReportPeriod(), newPeriod.getDepartmentId(), newPeriod.getCorrectionDate());
            if (status == PeriodStatusBeforeOpen.NOT_EXIST) {
                List<Integer> depIds = getAvailableDepartments(TaxType.NDFL, userService.getCurrentUser(), Operation.EDIT, oldPeriod.getDepartmentId());
                removePeriodWithLog(oldPeriod.getReportPeriod(), oldPeriod.getCorrectionDate(), oldPeriod.getDepartmentId(), depIds, TaxType.NDFL, null, true);
                openCorrectionPeriod(newPeriod);
                for (Integer depId : depIds) {
                    logOperation(logs, "Корректирующий период с " + oldPeriod.getReportPeriod().getName() + " " + oldPeriod.getReportPeriod().getTaxPeriod().getYear() +
                                    " был изменён на " + newPeriod.getReportPeriod().getName() + " для \"%s\"",
                            departmentService.getDepartment(depId).getName());//<соответствующий календарный год>** + <"ввод остатков" *>**  для <Наименование подразделения>"));
                }
            }
        }
    }

    @Override
    public void edit(int reportPeriodId, int oldDepartmentId, long newDictTaxPeriodId, int newYear, TaxType taxType, TAUserInfo user,
                     int departmentId, List<LogEntry> logs) {
        ReportPeriod rp = getReportPeriod(reportPeriodId);
        RefBook refBook = rbFactory.get(PERIOD_CODE_REF_BOOK);
        RefBookDataProvider provider = rbFactory.getDataProvider(refBook.getId());
        Map<String, RefBookValue> dictTaxPeriod;
        try {
            dictTaxPeriod = provider.getRecordData(newDictTaxPeriodId);
        } catch (DaoException ex) {
            throw new ServiceException(ex.getMessage());
        }

        List<Integer> depIds = getAvailableDepartments(taxType, user.getUser(), Operation.EDIT, oldDepartmentId);
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
            DepartmentReportPeriod dRP = departmentReportPeriodService.getFirst(oldDepartmentId, reportPeriodId);
            boolean fullLogging = false;
            boolean departmentHasBeenChanged = oldDepartmentId != departmentId;
            removePeriodWithLog(reportPeriodId, null, oldDepartmentId, depIds, taxType, departmentHasBeenChanged ? logs : null, fullLogging);
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
                    logOperation(logs, "Корректирующий период с " + rp.getName() + " " + rp.getTaxPeriod().getYear()
                                    + " был изменён на " + newRp.getName() + " для \"%s\"",
                            departmentService.getDepartment(departmentReportPeriodService.get(drpId).getDepartmentId()).getName());//<соответствующий календарный год>** + <"ввод остатков" *>**  для <Наименование подразделения>"));
                }
            }
        } else {
            ReportPeriod rp = getReportPeriod(newReportPeriodId);
            PeriodStatusBeforeOpen status = checkPeriodStatusBeforeOpen(rp, newDepartmentId, newCorrectionDate);
            if (status == PeriodStatusBeforeOpen.NOT_EXIST) {
                List<Integer> depIds = getAvailableDepartments(taxType, user.getUser(), Operation.EDIT, oldDepartmentId);
                removePeriodWithLog(reportPeriodId, correctionDate, oldDepartmentId, depIds, taxType, null, true);
                ReportPeriod newRp = getReportPeriod(newReportPeriodId);
                openCorrectionPeriod(taxType, newRp, newDepartmentId, newCorrectionDate, user, null);
                for (Integer depId : depIds) {
                    logOperation(logs, "Корректирующий период с " + rp.getName() + " " + rp.getTaxPeriod().getYear() +
                                    " был изменён на " + newRp.getName() + " для \"%s\"",
                            departmentService.getDepartment(depId).getName());//<соответствующий календарный год>** + <"ввод остатков" *>**  для <Наименование подразделения>"));
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
    public PagingResult<ReportPeriodType> getPeriodType(PagingParams pagingParams) {
        return reportPeriodDao.getPeriodType(pagingParams);
    }

    @Override
    public List<ReportPeriod> getReportPeriodsByDate(TaxType taxType, Date startDate, Date endDate) {
        return reportPeriodDao.getReportPeriodsByDate(taxType, startDate, endDate);
    }


    @Override
    public boolean isFirstPeriod(int reportPeriodId) {
        ReportPeriod rp = getReportPeriod(reportPeriodId);
        Calendar sDate = Calendar.getInstance();
        sDate.setTime(rp.getCalendarStartDate());
        int month = sDate.get(Calendar.MONTH) + 1;
        return month == 1;
    }

    private void logOperation(List<LogEntry> logs, String message, String departmentName) {
        if (departmentName != null && !departmentName.trim().isEmpty()) {
            logs.add(new LogEntry(LogLevel.INFO, String.format(message, departmentName)));
        }
    }
}
