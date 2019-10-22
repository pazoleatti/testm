package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.action.OpenCorrectionPeriodAction;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookFormType;
import com.aplana.sbrf.taxaccounting.model.result.*;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookFormTypeService;
import com.aplana.sbrf.taxaccounting.utils.DepartmentReportPeriodFormatter;
import com.aplana.sbrf.taxaccounting.utils.SimpleDateUtils;
import net.sf.jasperreports.web.actions.ActionException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

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

    private static final String YEAR_PERIOD_CODE = "34";
    private static final String YEAR_REORG_PERIOD_CODE = "90";

    public static final FastDateFormat DD_MM_YYYY_DATE_FORMAT = FastDateFormat.getInstance("dd.MM.yyyy");

    @Autowired
    private ReportPeriodDao reportPeriodDao;

    @Autowired
    private ReportPeriodService reportPeriodService;

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
    private DeclarationDataDao declarationDataDao;

    @Autowired
    private DeclarationTemplateService declarationTemplateService;

    @Autowired
    private LockDataService lockDataService;

    @Autowired
    private RefBookFormTypeService refBookFormTypeService;

    @Autowired
    private DepartmentReportPeriodFormatter departmentReportPeriodFormatter;

    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).OPEN_DEPARTMENT_REPORT_PERIOD)")
    public OpenPeriodResult open(DepartmentReportPeriod departmentReportPeriod, TAUserInfo userInfo) {
        LOG.info(String.format("open period: %s", departmentReportPeriod));
        ReportPeriod reportPeriod = null;
        Logger logger = new Logger();
        try {
            TaxPeriod taxPeriod = reportPeriodDao.fetchOrCreateTaxPeriod(departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear());
            ReportPeriodType reportPeriodType = reportPeriodDao.getReportPeriodTypeById(departmentReportPeriod.getReportPeriod().getDictTaxPeriodId());
            reportPeriod = reportPeriodService.fetchOrCreate(
                    taxPeriod, reportPeriodType, departmentReportPeriod.getReportPeriod().getReportPeriodTaxFormTypeId());
            departmentReportPeriod.setIsActive(true);
            departmentReportPeriod.setReportPeriod(reportPeriod);

            open(departmentReportPeriod, logger);
            return new OpenPeriodResult(logEntryService.save(logger.getEntries()));
        } catch (ServiceException e) {
            LOG.error(e.getMessage());
            return new OpenPeriodResult(logEntryService.save(logger.getEntries()))
                    .error(e.getMessage());
        } catch (Exception e) {
            throw new ServiceException(String.format("Ошибка при открытии периода \"%s\" для подразделения \"%s\" " +
                            "и всех дочерних подразделений. Обратитесь к администратору.",
                    departmentReportPeriodFormatter.getPeriodDescription(departmentReportPeriod),
                    departmentDao.getDepartment(departmentReportPeriod.getDepartmentId()).getName()), e);
        }
    }

    @Override
    @PreAuthorize("hasPermission(#action.departmentReportPeriodId, 'com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod', T(com.aplana.sbrf.taxaccounting.permissions.DepartmentReportPeriodPermission).OPEN_CORRECT)")
    public OpenPeriodResult openCorrectionPeriod(OpenCorrectionPeriodAction action) {
        LOG.info(String.format("openCorrectionPeriod for departmentReportPeriodId: %s", action.getDepartmentReportPeriodId()));
        DepartmentReportPeriod mainDrp = departmentReportPeriodService.fetchOne(action.getDepartmentReportPeriodId());
        DepartmentReportPeriod correctionPeriod = new DepartmentReportPeriod();
        correctionPeriod.setReportPeriod(mainDrp.getReportPeriod());
        correctionPeriod.setDepartmentId(mainDrp.getDepartmentId());
        correctionPeriod.setCorrectionDate(SimpleDateUtils.toStartOfDay(action.getCorrectionDate()));
        correctionPeriod.setIsActive(true);
        Logger logger = new Logger();
        try {
            openCorrectionPeriod(correctionPeriod, logger);
            return new OpenPeriodResult(logEntryService.save(logger.getEntries()));
        } catch (ServiceException e) {
            LOG.error(e.getMessage());
            return new OpenPeriodResult(logEntryService.save(logger.getEntries()))
                    .error(e.getMessage());
        } catch (Exception e) {
            throw new ServiceException(String.format("Ошибка при открытии корректирующего периода \"%s\" с периодом " +
                            "сдачи корректировки %s для подразделения \"%s\" и всех дочерних подразделений. " +
                            "Обратитесь к администратору.",
                    departmentReportPeriodFormatter.getPeriodDescriptionWithoutCorrectionDate(correctionPeriod),
                    DD_MM_YYYY_DATE_FORMAT.format(correctionPeriod.getCorrectionDate()),
                    departmentDao.getDepartment(correctionPeriod.getDepartmentId()).getName()), e);
        }
    }

    private void openCorrectionPeriod(DepartmentReportPeriod correctionPeriod, Logger logger) {
        DepartmentReportPeriod drpLast = departmentReportPeriodService.fetchLast(correctionPeriod.getDepartmentId(), correctionPeriod.getReportPeriod().getId());
        if (drpLast.getCorrectionDate() != null && drpLast.isActive() && !drpLast.getCorrectionDate().equals(correctionPeriod.getCorrectionDate())) {
            throw new ServiceException("%s не может быть открыт, т.к уже открыт другой корректирующий период!",
                    periodWithCustomCorrectionDateDescription(correctionPeriod));
        }
        if (departmentReportPeriodService.isLaterCorrectionPeriodExists(correctionPeriod)) {
            throw new ServiceException("%s не может быть открыт, т.к. для него существует более поздние корректирующие периоды!",
                    periodWithCustomCorrectionDateDescription(correctionPeriod));
        }

        open(correctionPeriod, logger);
    }

    @Override
    public void openForNewDepartment(int departmentId) {
        LOG.info(String.format("openForNewDepartment, departmentId: %s", departmentId));
        if (departmentDao.existDepartment(departmentId)) {
            Department terBank = departmentDao.getParentTB(departmentId);
            if (terBank != null && terBank.getId() != departmentId) {
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
                    periodWithCustomCorrectionDateDescription(savedDepartmentReportPeriod),
                    savedDepartmentReportPeriod.isActive() ? "открыт" : "закрыт",
                    departmentDao.getDepartment(departmentReportPeriod.getDepartmentId()).getName());
        }

        if (!logger.containsLevel(LogLevel.ERROR)) {
            List<Integer> departmentIds = departmentDao.findAllChildrenIdsById(departmentReportPeriod.getDepartmentId());
            departmentReportPeriodService.create(departmentReportPeriod, departmentIds);

            logger.info("%s открыт для подразделения \"%s\" и всех дочерних подразделений",
                    periodWithCustomCorrectionDateDescription(departmentReportPeriod), departmentDao.getDepartment(departmentReportPeriod.getDepartmentId()).getName());
        }
    }

    @Override
    @PreAuthorize("hasPermission(#departmentReportPeriodId, 'com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod', T(com.aplana.sbrf.taxaccounting.permissions.DepartmentReportPeriodPermission).REOPEN)")
    public ReopenPeriodResult reopen(Integer departmentReportPeriodId) {
        LOG.info(String.format("reopen departmentReportPeriodId: %s", departmentReportPeriodId));
        Logger logger = new Logger();
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.fetchOne(departmentReportPeriodId);
        try {
            reopen(departmentReportPeriod, logger);
            return new ReopenPeriodResult(logEntryService.save(logger.getEntries()));
        } catch (ServiceException e) {
            LOG.error(e.getMessage());
            return new ReopenPeriodResult(logEntryService.save(logger.getEntries()))
                    .error(e.getMessage());
        } catch (Exception e) {
            throw new ServiceException(String.format("Ошибка при переоткрытии периода %s для подразделения \"%s\" и всех дочерних подразделений. Обратитесь к администратору.",
                    periodDescription(departmentReportPeriod),
                    departmentDao.getDepartment(departmentReportPeriod.getDepartmentId()).getName()), e);
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
    public ClosePeriodResult close(Integer departmentReportPeriodId, boolean skipHasNotAcceptedCheck) {
        LOG.info(String.format("close departmentReportPeriodId: %s", departmentReportPeriodId));
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.fetchOne(departmentReportPeriodId);
        Logger logger = new Logger();
        try {
            close(departmentReportPeriod, skipHasNotAcceptedCheck, logger);
            return new ClosePeriodResult(logEntryService.save(logger.getEntries()));
        } catch (ServiceException e) {
            LOG.error(e.getMessage());
            return new ClosePeriodResult(logEntryService.save(logger.getEntries()))
                    .error(e.getMessage())
                    .fatal(logger.getEntries().isEmpty() || logger.containsLevel(LogLevel.ERROR));
        } catch (Exception e) {
            throw new ServiceException(String.format("Ошибка при закрытии периода %s для подразделения \"%s\" и всех дочерних подразделений. Обратитесь к администратору.",
                    periodDescription(departmentReportPeriod),
                    departmentDao.getDepartment(departmentReportPeriod.getDepartmentId()).getName()), e);
        }
    }

    private void close(DepartmentReportPeriod departmentReportPeriod, boolean skipHasNotAcceptedCheck, Logger logger) {
        if (!departmentReportPeriod.isActive()) {
            throw new ServiceException("Период %s не может быть закрыт для подразделения \"%s\" и всех дочерних подразделений, поскольку он уже закрыт",
                    periodDescription(departmentReportPeriod), departmentDao.getDepartment(departmentReportPeriod.getDepartmentId()).getName());
        }

        checkHasBlockedDeclaration(departmentReportPeriod, logger);
        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceException("Период %s не может быть закрыт для подразделения \"%s\" и всех дочерних подразделений, т.к. в нём существуют заблокированные налоговые или отчетные формы. Перечень форм приведен в списке уведомлений",
                    periodDescription(departmentReportPeriod),
                    departmentDao.getDepartment(departmentReportPeriod.getDepartmentId()).getName());
        }

        if (!skipHasNotAcceptedCheck) {
            checkHasNotAccepted(departmentReportPeriod, logger);
            if (logger.containsLevel(LogLevel.WARNING)) {
                throw new ServiceException("В периоде %s существуют налоговые или отчетные формы в состоянии отличном от \"Принято\". Перечень форм приведен в списке уведомлений. Все равно закрыть период?",
                        periodDescription(departmentReportPeriod));
            }
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
    public DeletePeriodResult delete(Integer departmentReportPeriodId) {
        LOG.info(String.format("delete id: %s", departmentReportPeriodId));
        Logger logger = new Logger();
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.fetchOne(departmentReportPeriodId);
        try {
            delete(departmentReportPeriod, logger);
            return new DeletePeriodResult(logEntryService.save(logger.getEntries()));
        } catch (ServiceException e) {
            LOG.error(e.getMessage());
            return new DeletePeriodResult(logEntryService.save(logger.getEntries()))
                    .error(e.getMessage());
        } catch (Exception e) {
            throw new ServiceException(String.format("Ошибка при удалении периода %s для подразделения \"%s\" и всех дочерних подразделений. Обратитесь к администратору.",
                    periodDescription(departmentReportPeriod),
                    departmentDao.getDepartment(departmentReportPeriod.getDepartmentId()).getName()), e);
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
            throw new ServiceException("Период %s не может быть удалён, т.к. в нём существуют налоговые или отчетные формы. Перечень форм приведен в списке уведомлений.",
                    periodDescription(departmentReportPeriod));
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
                reportPeriodDao.removeTaxPeriod(reportPeriod.getTaxPeriod().getId());
            }
        }
    }

    private void checkHasBlockedDeclaration(DepartmentReportPeriod departmentReportPeriod, Logger logger) {
        List<Integer> departments = departmentService.getAllChildrenIds(departmentReportPeriod.getDepartmentId());

        DeclarationDataFilter dataFilter = new DeclarationDataFilter();
        dataFilter.setDepartmentIds(departments);
        dataFilter.setReportPeriodIds(Arrays.asList(departmentReportPeriod.getReportPeriod().getId()));
        dataFilter.setCorrectionTag(departmentReportPeriod.getCorrectionDate() != null);
        dataFilter.setCorrectionDate(departmentReportPeriod.getCorrectionDate());

        List<DeclarationData> declarations = declarationDataSearchService.getDeclarationData(dataFilter, DeclarationDataSearchOrdering.ID, false);

        Map<String, DeclarationData> keysBlocker = new HashMap<>(declarations.size());
        for (DeclarationData declarationData : declarations) {
            keysBlocker.put("DECLARATION_DATA_" + declarationData.getId(), declarationData);
        }
        List<LockDataDTO> lockDataItems = new ArrayList<>();

        if (keysBlocker.size() > 0) {
            lockDataItems = lockDataService.fetchAllByKeyPrefixSet(keysBlocker.keySet());
        }

        for (LockDataDTO lockDataItem : lockDataItems) {
            long declarationId = lockDataService.getDeclarationIdByLockKey(lockDataItem.getKey());
            DeclarationData dd = null;
            for (DeclarationData declarationData : declarations) {
                if (declarationData.getId() == declarationId) {
                    dd = declarationData;
                    break;
                }
            }

            DeclarationTemplate template = declarationTemplateService.get(dd.getDeclarationTemplateId());
            logger.error("Форма \"%s\" № %s, Подразделении: \"%s\", Период: \"%s\" заблокирована.",
                    template.getType().getName(), dd.getId(), departmentService.getDepartment(dd.getDepartmentId()).getName(),
                    periodDescription(departmentReportPeriod));
        }
    }

    private boolean checkHasNotAccepted(DepartmentReportPeriod departmentReportPeriod, Logger logger) {
        boolean result = false;
        List<Integer> departments = departmentService.getAllChildrenIds(departmentReportPeriod.getDepartmentId());

        DeclarationDataFilter dataFilter = new DeclarationDataFilter();
        dataFilter.setDepartmentIds(departments);
        dataFilter.setReportPeriodIds(Arrays.asList(departmentReportPeriod.getReportPeriod().getId()));
        dataFilter.setFormStates(Arrays.asList(State.CREATED.getId(), State.PREPARED.getId()));
        if (departmentReportPeriod.getCorrectionDate() != null) {
            dataFilter.setCorrectionTag(true);
            dataFilter.setCorrectionDate(departmentReportPeriod.getCorrectionDate());
        } else {
            dataFilter.setCorrectionTag(false);
        }

        List<DeclarationData> declarations = declarationDataSearchService.getDeclarationData(dataFilter, DeclarationDataSearchOrdering.ID, false);
        for (DeclarationData dd : declarations) {
            DeclarationTemplate template = declarationTemplateService.get(dd.getDeclarationTemplateId());
            logger.warn("Форма \"%s\" № %s существует в подразделении \"%s\" в периоде %s.",
                    template.getType().getName(), dd.getId(), departmentService.getDepartment(dd.getDepartmentId()).getName(),
                    periodDescription(departmentReportPeriod));
            result = true;
        }
        return result;
    }

    @Override
    public ReportPeriod fetchReportPeriod(int reportPeriodId) {
        return reportPeriodDao.fetchOne(reportPeriodId);
    }

    @Override
    public List<ReportPeriod> findAll() {
        return reportPeriodDao.findAll();
    }

    @Override
    public List<ReportPeriod> findAllFor2NdflFL() {
        return reportPeriodDao.findAllFor2NdflFL();
    }

    @Override
    public boolean existForDepartment(int departmentId, int reportPeriodId) {
        return departmentReportPeriodService.isExistsByReportPeriodIdAndDepartmentId(departmentId, reportPeriodId);
    }

    @Override
    public List<ReportPeriod> findAllActive(TAUser user) {
        List<Integer> departmentIds = departmentService.findAllAvailableIds(user);
        return reportPeriodDao.findAllActive(departmentIds);
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
    public List<ReportPeriod> getCorrectPeriods(int departmentId) {
        return reportPeriodDao.getCorrectPeriods(departmentId);
    }

    @Override
    public List<ReportPeriodType> getPeriodType() {
        return reportPeriodDao.getPeriodType();
    }

    @Override
    public List<ReportPeriodResult> fetchActiveByDepartment(Integer departmentId) {
        return reportPeriodDao.fetchActiveByDepartment(departmentId);
    }

    @Override
    public String createLogPeriodFormatById(List<Long> idList, Integer logLevelType) {

        if (ObjectUtils.isEmpty(idList)) {
            return StringUtils.EMPTY;
        }

        List<LogPeriodResult> maxPeriodList = new ArrayList<>();

        for (Long id : idList) {
            LogPeriodResult logPeriodResult = getFirstInList(reportPeriodDao.createLogPeriodFormatById(id, logLevelType));
            if (!ObjectUtils.isEmpty(logPeriodResult))
                maxPeriodList.add(logPeriodResult);
        }

        if (maxPeriodList.isEmpty()) {
            return StringUtils.EMPTY;
        }

        StringBuilder periodBuilder = new StringBuilder();
        Collections.sort(maxPeriodList, new LogPeriodResult.CompDate(true));
        LogPeriodResult logPeriodResult = getFirstInList(maxPeriodList);
        periodBuilder.append(periodBuilder)
                .append(logPeriodResult.getYear())
                .append(": ")
                .append(logPeriodResult.getName())
                .append("; ")
                .append(getLogPeriodResultCorrectingString(logPeriodResult));

        return periodBuilder.toString();
    }


    /**
     * Строка корректировки
     *
     * @param logPeriodResult
     * @return
     */
    private String getLogPeriodResultCorrectingString(LogPeriodResult logPeriodResult) {
        if (ObjectUtils.isEmpty(logPeriodResult.getCorrectionDate())) {
            return StringUtils.EMPTY;
        }
        return "(корр. " + DD_MM_YYYY_DATE_FORMAT.format(logPeriodResult.getCorrectionDate()) + ")";
    }

    /**
     * Получить первый элемент в списке
     *
     * @param list
     * @param <T>
     * @return
     */
    private <T> T getFirstInList(List<T> list) {
        return list.size() > 0 ? list.get(0) : null;
    }

    private String periodDescription(DepartmentReportPeriod departmentReportPeriod) {
        return String.format("\"%s\"", departmentReportPeriodFormatter.getPeriodDescription(departmentReportPeriod));
    }

    private String periodWithCustomCorrectionDateDescription(DepartmentReportPeriod departmentReportPeriod) {
        return String.format("%s \"%s\"%s",
                departmentReportPeriod.getCorrectionDate() == null ? "Период" : "Корректирующий период",
                departmentReportPeriodFormatter.getPeriodDescriptionWithoutCorrectionDate(departmentReportPeriod),
                departmentReportPeriod.getCorrectionDate() == null ? StringUtils.EMPTY :
                        " с периодом сдачи корректировки " + DD_MM_YYYY_DATE_FORMAT.format(departmentReportPeriod.getCorrectionDate()));
    }

    @Override
    public boolean isYearPeriodType(ReportPeriod reportPeriod) {
        long dictTaxPeriodId = reportPeriod.getDictTaxPeriodId();
        ReportPeriodType periodType = getPeriodTypeById(dictTaxPeriodId);
        return isYearPeriodType(periodType);
    }

    @Override
    public boolean isYearPeriodType(ReportPeriodType periodType) {
        return YEAR_PERIOD_CODE.equals(periodType.getCode()) || YEAR_REORG_PERIOD_CODE.equals(periodType.getCode());
    }

    @Override
    public boolean is6NdflOr2Ndfl1TaxFormType(ReportPeriod reportPeriod) {
        Integer reportPeriodTaxFormTypeId = reportPeriod.getReportPeriodTaxFormTypeId();
        return RefBookFormType.NDFL_2_1.getId().intValue() == reportPeriodTaxFormTypeId
                || RefBookFormType.NDFL_6.getId().intValue() == reportPeriodTaxFormTypeId;
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
        filter.setDepartmentIdList(departmentDao.findAllChildrenIdsById(departmentReportPeriod.getDepartmentId()));
        return filter;
    }

    private DeclarationDataFilter declarationFilterForAllPeriods(DepartmentReportPeriod departmentReportPeriod) {
        DeclarationDataFilter filter = new DeclarationDataFilter();
        filter.setDepartmentIds(departmentDao.findAllChildrenIdsById(departmentReportPeriod.getDepartmentId()));
        filter.setReportPeriodIds(singletonList(departmentReportPeriod.getReportPeriod().getId()));
        filter.setCorrectionTag(departmentReportPeriod.getCorrectionDate() != null);
        filter.setCorrectionDate(departmentReportPeriod.getCorrectionDate());
        return filter;
    }
}
