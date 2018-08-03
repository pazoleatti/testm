package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.action.OpenCorrectionPeriodAction;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.result.ReportPeriodResult;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.utils.SimpleDateUtils;
import net.sf.jasperreports.web.actions.ActionException;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.Collections.singletonList;

/**
 * Сервис работы с периодами
 * Только этот сервис должен использоваться для работы с отчетными и налоговыми периодами
 */
@Service
@Transactional
public class PeriodServiceImpl implements PeriodService {
    private static final Log LOG = LogFactory.getLog(PeriodServiceImpl.class);

    @Autowired
    private ReportPeriodDao reportPeriodDao;

    @Autowired
    private ReportPeriodService reportPeriodService;

    @Autowired
    private TaxPeriodDao taxPeriodDao;

    @Autowired
    private TaxPeriodService taxPeriodService;

    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;

    @Autowired
    private DepartmentDao departmentDao;

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
    private DeclarationDataDao declarationDataDao;

    @Autowired
    private DeclarationTemplateService declarationTemplateService;

    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).OPEN_DEPARTMENT_REPORT_PERIOD)")
    public String open(DepartmentReportPeriod departmentReportPeriod, TAUserInfo userInfo) {
        LOG.info(String.format("open period: %s", departmentReportPeriod));
        ReportPeriod reportPeriod = null;
        try {
            Logger logger = new Logger();
            TaxPeriod taxPeriod = taxPeriodService.fetchOrCreate(departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear());
            ReportPeriodType reportPeriodType = reportPeriodDao.getReportPeriodTypeById(departmentReportPeriod.getReportPeriod().getDictTaxPeriodId());
            reportPeriod = reportPeriodService.fetchOrCreate(taxPeriod, reportPeriodType);
            departmentReportPeriod.setIsActive(true);
            departmentReportPeriod.setReportPeriod(reportPeriod);

            open(departmentReportPeriod, logger);
            return logEntryService.save(logger.getEntries());
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException("Ошибка при открытии периода \"%s:%s\" для подразделения \"%s\" и всех дочерних подразделений. Обратитесь к администратору.",
                    departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                    reportPeriod != null ? reportPeriod.getName() : "null",
                    departmentDao.getDepartment(departmentReportPeriod.getDepartmentId()).getName());
        }
    }

    @Override
    @PreAuthorize("hasPermission(#action.departmentReportPeriodId, 'com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod', T(com.aplana.sbrf.taxaccounting.permissions.DepartmentReportPeriodPermission).OPEN_CORRECT)")
    public String openCorrectionPeriod(OpenCorrectionPeriodAction action) {
        LOG.info(String.format("openCorrectionPeriod for departmentReportPeriodId: %s", action.getDepartmentReportPeriodId()));
        DepartmentReportPeriod mainDrp = departmentReportPeriodDao.fetchOne(action.getDepartmentReportPeriodId());
        DepartmentReportPeriod correctionPeriod = new DepartmentReportPeriod();
        correctionPeriod.setReportPeriod(mainDrp.getReportPeriod());
        correctionPeriod.setDepartmentId(mainDrp.getDepartmentId());
        correctionPeriod.setCorrectionDate(SimpleDateUtils.toStartOfDay(action.getCorrectionDate()));
        correctionPeriod.setIsActive(true);
        try {
            Logger logger = new Logger();
            DepartmentReportPeriod drpLast = departmentReportPeriodService.fetchLast(correctionPeriod.getDepartmentId(), correctionPeriod.getReportPeriod().getId());
            if (drpLast.getCorrectionDate() != null && drpLast.isActive()) {
                throw new ServiceException("%s не может быть открыт, т.к уже открыт другой корректирующий период!",
                        periodWithDescription(correctionPeriod));
            }
            if (departmentReportPeriodService.isLaterCorrectionPeriodExists(correctionPeriod)) {
                throw new ServiceException("%s не может быть открыт, т.к. для него существует более поздние корректирующие периоды!",
                        periodWithDescription(correctionPeriod));
            }

            open(correctionPeriod, logger);
            return logEntryService.save(logger.getEntries());
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException("Ошибка при открытии корректирующего периода \"%s:%s\" с периодом сдачи корректировки %s для подразделения \"%s\" и всех дочерних подразделений. Обратитесь к администратору.",
                    correctionPeriod.getReportPeriod().getTaxPeriod().getYear(),
                    correctionPeriod.getReportPeriod().getName(),
                    FastDateFormat.getInstance("dd.MM.yyyy").format(correctionPeriod.getCorrectionDate()),
                    departmentDao.getDepartment(correctionPeriod.getDepartmentId()).getName());
        }
    }

    @Override
    public void openForNewDepartment(int departmentId) {
        LOG.info(String.format("openForNewDepartment, departmentId: %s", departmentId));
        if (departmentDao.existDepartment(departmentId)) {
            Department terBank = departmentDao.getParentTB(departmentId);
            if (terBank != null) {
                DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
                filter.setDepartmentId(terBank.getId());
                List<DepartmentReportPeriod> departmentReportPeriods = departmentReportPeriodService.fetchAllByFilter(filter);
                departmentReportPeriodService.merge(departmentReportPeriods, departmentId);
            }
        }
    }

    private void open(DepartmentReportPeriod departmentReportPeriod, Logger logger) {
        LOG.info(String.format("open departmentReportPeriod: %s", departmentReportPeriod));

        DepartmentReportPeriod savedDepartmentReportPeriod = departmentReportPeriodService.fetchOneByFilter(filterForSinglePeriod(departmentReportPeriod));
        if (savedDepartmentReportPeriod != null) {
            throw new ServiceException("%s уже существует и %s для подразделения \"%s\" и всех дочерних подразделений",
                    periodWithDescription(savedDepartmentReportPeriod),
                    savedDepartmentReportPeriod.isActive() ? "открыт" : "закрыт",
                    departmentDao.getDepartment(departmentReportPeriod.getDepartmentId()).getName());
        }

        if (logger == null || !logger.containsLevel(LogLevel.ERROR)) {
            List<Integer> departmentIds = departmentDao.fetchAllChildrenIds(departmentReportPeriod.getDepartmentId());
            departmentReportPeriodService.create(departmentReportPeriod, departmentIds);

            if (logger != null) {
                logger.info("%s открыт для \"%s\" и всех дочерних подразделений",
                        periodWithDescription(departmentReportPeriod), departmentDao.getDepartment(departmentReportPeriod.getDepartmentId()).getName());
            }
        }
    }

    @Override
    @PreAuthorize("hasPermission(#departmentReportPeriodId, 'com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod', T(com.aplana.sbrf.taxaccounting.permissions.DepartmentReportPeriodPermission).REOPEN)")
    public String reopen(Integer departmentReportPeriodId) {
        LOG.info(String.format("reopen departmentReportPeriodId: %s", departmentReportPeriodId));
        Logger logger = new Logger();
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.fetchOne(departmentReportPeriodId);
        try {
            reopen(departmentReportPeriod, logger);
            return logEntryService.save(logger.getEntries());
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException("Ошибка при открытии периода %s для подразделения \"%s\" и всех дочерних подразделений. Обратитесь к администратору.",
                    periodDescription(departmentReportPeriod),
                    departmentDao.getDepartment(departmentReportPeriod.getDepartmentId()).getName());
        }
    }

    private void reopen(DepartmentReportPeriod departmentReportPeriod, Logger logger) {
        if (departmentReportPeriod.isActive()) {
            throw new ServiceException("Период %s уже открыт для подразделения \"%s\" и всех дочерних подразделений",
                    periodDescription(departmentReportPeriod), departmentDao.getDepartment(departmentReportPeriod.getDepartmentId()).getName());
        }
        if (departmentReportPeriod.getCorrectionDate() != null && departmentReportPeriodService.isLaterCorrectionPeriodExists(departmentReportPeriod)) {
            // Корректирующий период невозможно переоткрыть из-за наличия корректирующих периодов с более поздней датой корректировки
            throw new ServiceException("Период %s не может быть переоткрыт, т.к. для него существуют более поздние корректирующие периоды!", periodDescription(departmentReportPeriod));
        } else if (departmentReportPeriod.getCorrectionDate() == null) {
            // Основной период невозможно переоткрыть из-за наличия корректирующих периодов
            DepartmentReportPeriodFilter filter = filterForSinglePeriod(departmentReportPeriod);
            filter.setIsCorrection(true);
            List<Integer> corrIds = departmentReportPeriodService.fetchAllIdsByFilter(filter);
            if (!corrIds.isEmpty()) {
                throw new ServiceException("Период %s не может быть переоткрыт, т.к. для него созданы корректирующие периоды!", periodDescription(departmentReportPeriod));
            }
        }

        if (!logger.containsLevel(LogLevel.ERROR)) {
            List<Integer> drpIds = departmentReportPeriodService.fetchAllIdsByFilter(filterForAllPeriods(departmentReportPeriod));
            departmentReportPeriodService.updateActive(drpIds, departmentReportPeriod.getReportPeriod().getId(), true);

            logger.info("Период %s переоткрыт для подразделения \"%s\" и всех дочерних подразделений",
                    periodDescription(departmentReportPeriod), departmentDao.getDepartment(departmentReportPeriod.getDepartmentId()).getName());
        }
    }

    @Override
    @PreAuthorize("hasPermission(#departmentReportPeriodId, 'com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod', T(com.aplana.sbrf.taxaccounting.permissions.DepartmentReportPeriodPermission).CLOSE)")
    public String close(Integer departmentReportPeriodId) {
        LOG.info(String.format("close departmentReportPeriodId: %s", departmentReportPeriodId));
        Logger logger = new Logger();
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.fetchOne(departmentReportPeriodId);
        try {
            close(departmentReportPeriod, logger);
            return logEntryService.save(logger.getEntries());
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException("Ошибка при закрытии периода %s для подразделения \"%s\" и всех дочерних подразделений. Обратитесь к администратору.",
                    periodDescription(departmentReportPeriod),
                    departmentDao.getDepartment(departmentReportPeriod.getDepartmentId()).getName());
        }
    }

    private void close(DepartmentReportPeriod departmentReportPeriod, Logger logger) {
        if (!departmentReportPeriod.isActive()) {
            throw new ServiceException("Период %s не может быть закрыт для подразделения \"%s\" и всех дочерних подразделений, поскольку он уже закрыт",
                    periodDescription(departmentReportPeriod), departmentDao.getDepartment(departmentReportPeriod.getDepartmentId()).getName());
        }

        if (!logger.containsLevel(LogLevel.ERROR)) {
            List<Integer> drpIds = departmentReportPeriodService.fetchAllIdsByFilter(filterForAllPeriods(departmentReportPeriod));
            departmentReportPeriodService.updateActive(drpIds, departmentReportPeriod.getReportPeriod().getId(), false);

            logger.info("Период %s закрыт для подразделения \"%s\" и всех дочерних подразделений",
                    periodDescription(departmentReportPeriod), departmentDao.getDepartment(departmentReportPeriod.getDepartmentId()).getName());
        }
    }

    @Override
    @PreAuthorize("hasPermission(#departmentReportPeriodId, 'com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod', T(com.aplana.sbrf.taxaccounting.permissions.DepartmentReportPeriodPermission).DELETE)")
    public String delete(Integer departmentReportPeriodId) {
        LOG.info(String.format("delete id: %s", departmentReportPeriodId));
        Logger logger = new Logger();
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.fetchOne(departmentReportPeriodId);
        try {
            delete(departmentReportPeriod, logger);
            return logEntryService.save(logger.getEntries());
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException("Ошибка при удалении периода %s для подразделения \"%s\" и всех дочерних подразделений. Обратитесь к администратору.",
                    periodDescription(departmentReportPeriod),
                    departmentDao.getDepartment(departmentReportPeriod.getDepartmentId()).getName());
        }
    }

    private void delete(DepartmentReportPeriod departmentReportPeriod, Logger logger) {
        List<Long> declarations = declarationDataSearchService.getDeclarationIds(declarationFilterForAllPeriods(departmentReportPeriod), DeclarationDataSearchOrdering.ID, true);
        if (!declarations.isEmpty()) {
            for (Long id : declarations) {
                DeclarationData dd = declarationDataDao.get(id);
                DeclarationTemplate dt = declarationTemplateService.get(dd.getDeclarationTemplateId());
                logger.error("Форма \"%s\" № %s существует в подразделении \"%s\" в периоде %s.",
                        dt.getType().getName(), dd.getId(), departmentDao.getDepartment(dd.getDepartmentId()).getName(),
                        periodDescription(departmentReportPeriod));
            }
            throw new ServiceLoggerException("Период %s не может быть удалён, т.к. в нём существуют налоговые или отчетные формы. Перечень форм приведен в списке уведомлений.",
                    logEntryService.save(logger.getEntries()), periodDescription(departmentReportPeriod));
        }
        if (departmentReportPeriod.getCorrectionDate() != null && departmentReportPeriodService.isLaterCorrectionPeriodExists(departmentReportPeriod)) {
            // Корректирующий период невозможно удалить из-за наличия корректирующих периодов с более поздней датой корректировки
            throw new ServiceException("Удаление корректирующего периода %s для подразделения \"%s\" и всех дочерних подразделений невозможно, т.к. для него существует более поздний корректирующий период.",
                    periodDescription(departmentReportPeriod), departmentDao.getDepartment(departmentReportPeriod.getDepartmentId()).getName());
        } else if (departmentReportPeriod.getCorrectionDate() == null) {
            // Основной период невозможно удалить из-за наличия корректирующих периодов
            DepartmentReportPeriodFilter filter = filterForSinglePeriod(departmentReportPeriod);
            filter.setIsCorrection(true);
            List<Integer> corrIds = departmentReportPeriodService.fetchAllIdsByFilter(filter);
            if (!corrIds.isEmpty()) {
                throw new ServiceException("Удаление периода %s для подразделения \"%s\" и всех дочерних подразделений невозможно, т.к. для него существует корректирующий период.",
                        periodDescription(departmentReportPeriod), departmentDao.getDepartment(departmentReportPeriod.getDepartmentId()).getName());
            }
        }

        if (!logger.containsLevel(LogLevel.ERROR)) {
            doDelete(departmentReportPeriod, logger);
        }
    }

    private void doDelete(DepartmentReportPeriod departmentReportPeriod, Logger logger) {
        ReportPeriod reportPeriod = departmentReportPeriod.getReportPeriod();

        List<Integer> drpIds = departmentReportPeriodService.fetchAllIdsByFilter(filterForAllPeriods(departmentReportPeriod));
        departmentReportPeriodService.delete(drpIds);

        if (logger != null) {
            logger.info("Период %s удалён для подразделения \"%s\" и всех дочерних подразделений",
                    periodDescription(departmentReportPeriod), departmentDao.getDepartment(departmentReportPeriod.getDepartmentId()).getName());
        }

        notificationService.deleteByReportPeriod(reportPeriod.getId());

        if (!departmentReportPeriodService.isExistsByReportPeriodId(reportPeriod.getId())) {
            // Неиспользующияся ReportPeriod удаляем
            reportPeriodDao.remove(reportPeriod.getId());

            if (reportPeriodDao.fetchAllByTaxPeriod(reportPeriod.getTaxPeriod().getId()).isEmpty()) {
                // Неиспользующияся TaxPeriod удаляем
                taxPeriodDao.delete(reportPeriod.getTaxPeriod().getId());
            }
        }
    }

    @Override
    public ReportPeriod fetchReportPeriod(int reportPeriodId) {
        return reportPeriodDao.fetchOne(reportPeriodId);
    }

    @Override
    public boolean existForDepartment(int departmentId, int reportPeriodId) {
        return departmentReportPeriodService.isExistsByReportPeriodIdAndDepartmentId(departmentId, reportPeriodId);
    }

    @Override
    public Set<ReportPeriod> getOpenReportPeriodForUser(TAUser user) {
        List<Integer> departments = departmentService.getTaxFormDepartments(user);
        if (user.hasRoles(TaxType.NDFL, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_OPER)) {
            return new LinkedHashSet<>(getOpenPeriodsByDepartments(departments, false));
        } else {
            return Collections.EMPTY_SET;
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
        DepartmentReportPeriod period = departmentReportPeriodDao.fetchOne(filter.getId());
        String text = "%s назначил подразделению %s новый срок сдачи отчетности для %s в периоде %s %s года: %s";
        List<Notification> notifications = new ArrayList<>();
        if (filter.getDeadline() == null) {
            throw new ActionException("Дата сдачи отчетности должна быть указана!");
        }
        List<Department> departments = new ArrayList<>();
        if (filter.isWithChild()) {
            departments.addAll(departmentService.getAllChildren(filter.getDepartmentId()));
        } else {
            departments.add(departmentDao.getDepartment(filter.getDepartmentId()));
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
    public ReportPeriod getByDictCodeAndYear(String code, int year) {
        return reportPeriodDao.getByTaxTypedCodeYear(code, year);
    }

    @Override
    public List<ReportPeriodType> getPeriodType() {
        return reportPeriodDao.getPeriodType();
    }

    @Override
    public List<ReportPeriodResult> fetchActiveByDepartment(Integer departmentId) {
        return reportPeriodDao.fetchActiveByDepartment(departmentId);
    }

    private String periodDescription(DepartmentReportPeriod departmentReportPeriod) {
        return String.format("\"%s:%s%s\"",
                departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                departmentReportPeriod.getReportPeriod().getName(),
                departmentReportPeriod.getCorrectionDate() != null ? " (корр. " + FastDateFormat.getInstance("dd.MM.yyyy").format(departmentReportPeriod.getCorrectionDate()) + ")" : "");
    }

    private String periodWithDescription(DepartmentReportPeriod departmentReportPeriod) {
        return String.format("%s \"%s:%s\"%s",
                departmentReportPeriod.getCorrectionDate() == null ? "Период" : "Корректирующий период",
                departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                departmentReportPeriod.getReportPeriod().getName(),
                departmentReportPeriod.getCorrectionDate() == null ? "" :
                        " с периодом сдачи корректировки " + FastDateFormat.getInstance("dd.MM.yyyy").format(departmentReportPeriod.getCorrectionDate()));
    }

    private DepartmentReportPeriodFilter filterFor(DepartmentReportPeriod departmentReportPeriod) {
        DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
        filter.setReportPeriodIdList(singletonList(departmentReportPeriod.getReportPeriod().getId()));
        filter.setIsCorrection(departmentReportPeriod.getCorrectionDate() != null);
        filter.setCorrectionDate(departmentReportPeriod.getCorrectionDate());
        return filter;
    }

    private DepartmentReportPeriodFilter filterForSinglePeriod(DepartmentReportPeriod departmentReportPeriod) {
        DepartmentReportPeriodFilter filter = filterFor(departmentReportPeriod);
        filter.setDepartmentId(departmentReportPeriod.getDepartmentId());
        return filter;
    }

    private DepartmentReportPeriodFilter filterForAllPeriods(DepartmentReportPeriod departmentReportPeriod) {
        DepartmentReportPeriodFilter filter = filterFor(departmentReportPeriod);
        filter.setDepartmentIdList(departmentDao.fetchAllChildrenIds(departmentReportPeriod.getDepartmentId()));
        return filter;
    }

    private DeclarationDataFilter declarationFilterForAllPeriods(DepartmentReportPeriod departmentReportPeriod) {
        DeclarationDataFilter filter = new DeclarationDataFilter();
        filter.setDepartmentIds(departmentDao.fetchAllChildrenIds(departmentReportPeriod.getDepartmentId()));
        filter.setReportPeriodIds(singletonList(departmentReportPeriod.getReportPeriod().getId()));
        filter.setCorrectionTag(departmentReportPeriod.getCorrectionDate() != null);
        filter.setCorrectionDate(departmentReportPeriod.getCorrectionDate());
        return filter;
    }
}
