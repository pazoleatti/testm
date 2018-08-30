package com.aplana.sbrf.taxaccounting.service.impl.refbook;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.action.DepartmentConfigFetchingAction;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import com.aplana.sbrf.taxaccounting.service.refbook.DepartmentConfigService;
import com.aplana.sbrf.taxaccounting.utils.SimpleDateUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.model.refbook.RefBook.Id.*;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Service
public class DepartmentConfigServiceImpl implements DepartmentConfigService {
    protected static final Log LOG = LogFactory.getLog(DepartmentConfigServiceImpl.class);

    @Autowired
    private CommonRefBookService commonRefBookService;
    @Autowired
    private RefBookFactory refBookFactory;
    @Autowired
    private LogEntryService logEntryService;

    @Override
    public PagingResult<DepartmentConfig> fetchDepartmentConfigs(DepartmentConfigFetchingAction action, PagingParams pagingParams) {
        StringBuilder departmentConfigFilter = new StringBuilder().append(DepartmentConfigDetailAliases.DEPARTMENT_ID).append(" = ").append(action.getDepartmentId());
        if (!isEmpty(action.getKpp())) {
            departmentConfigFilter.append(" AND LOWER(KPP) like '%").append(action.getKpp().toLowerCase()).append("%'");
        }
        if (!isEmpty(action.getOktmo())) {
            departmentConfigFilter.append(" AND LOWER(OKTMO.CODE) like '%").append(action.getOktmo().toLowerCase()).append("%'");
        }
        if (!isEmpty(action.getTaxOrganCode())) {
            departmentConfigFilter.append(" AND LOWER(TAX_ORGAN_CODE) like '%").append(action.getTaxOrganCode().toLowerCase()).append("%'");
        }
        RefBook refBook = commonRefBookService.get(NDFL_DETAIL.getId());
        RefBookDataProvider provider = refBookFactory.getDataProvider(NDFL_DETAIL.getId());
        PagingResult<Map<String, RefBookValue>> records = provider.getRecords(
                action.getRelevanceDate(), pagingParams, departmentConfigFilter.toString(), refBook.getAttribute("OKTMO"));
        return new PagingResult<>(convertToDepartmentConfigs(records), records.getTotalCount());
    }

    public DepartmentConfig fetch(long id) {
        RefBookDataProvider provider = refBookFactory.getDataProvider(NDFL_DETAIL.getId());
        Map<String, RefBookValue> record = provider.getRecordData(id);
        return convertToDepartmentConfig(record, null);
    }

    public List<DepartmentConfig> fetchByKppAndOktmo(String kpp, String oktmoCode) {
        String filter = "LOWER(KPP) like '%" + kpp.toLowerCase() + "%'" +
                " AND LOWER(OKTMO.CODE) like '%" + oktmoCode.toLowerCase() + "%'";
        RefBookDataProvider provider = refBookFactory.getDataProvider(NDFL_DETAIL.getId());
        PagingResult<Map<String, RefBookValue>> records = provider.getRecords(
                null, null, filter, null);
        return convertToDepartmentConfigs(records);
    }

    @Override
    @PreAuthorize("hasPermission(#departmentConfig, T(com.aplana.sbrf.taxaccounting.permissions.DepartmentConfigPermission).CREATE)")
    public ActionResult create(DepartmentConfig departmentConfig, TAUserInfo userInfo) {
        LOG.info("create: kpp=" + departmentConfig.getKpp() + ", oktmo=" + departmentConfig.getOktmo().getCode());
        ActionResult result = new ActionResult();
        Logger logger = new Logger();
        logger.setTaUserInfo(userInfo);
        try {
            departmentConfig.setStartDate(SimpleDateUtils.toStartOfDay(departmentConfig.getStartDate()));
            departmentConfig.setEndDate(SimpleDateUtils.toStartOfDay(departmentConfig.getEndDate()));
            create(departmentConfig, logger);
        } catch (ServiceException e) {
            LOG.error(e.getMessage());
            return new ActionResult(logEntryService.save(logger.getEntries()))
                    .error(e.getMessage());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return new ActionResult(logEntryService.save(logger.getEntries()))
                    .error(getCreateFailedMessage(departmentConfig));
        }
        if (logger.containsLevel(LogLevel.ERROR)) {
            LOG.error("failed");
            return new ActionResult(logEntryService.save(logger.getEntries()))
                    .error(getCreateFailedMessage(departmentConfig));
        }
        logger.info("Для подразделения \"%s\" создана настройка КПП: \"%s\", ОКТМО: \"%s\", настройка действительна с \"%s\" по \"%s\"",
                departmentConfig.getDepartment().getName(), departmentConfig.getKpp(), departmentConfig.getOktmo().getCode(),
                FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getStartDate()),
                departmentConfig.getEndDate() == null ? "-" : FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getEndDate()));
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Transactional
    public void create(DepartmentConfig departmentConfig, Logger logger) {
        List<DepartmentConfig> relatedDepartmentConfigs = fetchByKppAndOktmo(departmentConfig.getKpp(), departmentConfig.getOktmo().getCode());
        if (!relatedDepartmentConfigs.isEmpty()) {
            departmentConfig.setRecordId(relatedDepartmentConfigs.get(0).getRecordId());
        }
        checkDepartmentConfig(departmentConfig, relatedDepartmentConfigs);
        RefBookDataProvider provider = refBookFactory.getDataProvider(NDFL_DETAIL.getId());
        provider.createRecordVersion(logger, departmentConfig.getStartDate(), departmentConfig.getEndDate(), new ArrayList<>(singletonList(convertToRefBookRecord(departmentConfig))));
    }

    private String getCreateFailedMessage(DepartmentConfig departmentConfig) {
        return String.format("Ошибка при создании настройки подразделения для \"%s\" с параметрами: КПП: \"%s\", ОКТМО: \"%s\", дата начала действия настройки: " +
                        "\"%s\". Обратитесь к Администратору Системы или повторите операцию позднее.",
                departmentConfig.getDepartment().getName(), departmentConfig.getKpp(), departmentConfig.getOktmo().getCode(),
                FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getStartDate()));
    }

    @Override
    @PreAuthorize("hasPermission(#departmentConfig, T(com.aplana.sbrf.taxaccounting.permissions.DepartmentConfigPermission).UPDATE)")
    public ActionResult update(DepartmentConfig departmentConfig, TAUserInfo userInfo) {
        LOG.info("update: id=" + departmentConfig.getId() + ", kpp=" + departmentConfig.getKpp() + ", oktmo=" + departmentConfig.getOktmo().getCode());
        ActionResult result = new ActionResult();
        Logger logger = new Logger();
        logger.setTaUserInfo(userInfo);
        try {
            departmentConfig.setStartDate(SimpleDateUtils.toStartOfDay(departmentConfig.getStartDate()));
            departmentConfig.setEndDate(SimpleDateUtils.toStartOfDay(departmentConfig.getEndDate()));
            update(departmentConfig, logger);
        } catch (ServiceException e) {
            LOG.error(e.getMessage());
            return new ActionResult(logEntryService.save(logger.getEntries()))
                    .error(e.getMessage());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return new ActionResult(logEntryService.save(logger.getEntries()))
                    .error(getUpdateFailedMessage(departmentConfig));
        }
        if (logger.containsLevel(LogLevel.ERROR)) {
            LOG.error("failed");
            return new ActionResult(logEntryService.save(logger.getEntries()))
                    .error(getUpdateFailedMessage(departmentConfig));
        }
        logger.info("В подразделении \"%s\" изменена настройка КПП: \"%s\", ОКТМО: \"%s\", настройка действительна с \"%s\" по \"%s\"",
                departmentConfig.getDepartment().getName(), departmentConfig.getKpp(), departmentConfig.getOktmo().getCode(),
                FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getStartDate()),
                departmentConfig.getEndDate() == null ? "-" : FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getEndDate()));
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Transactional
    public void update(DepartmentConfig departmentConfig, Logger logger) {
        checkDepartmentConfig(departmentConfig, fetchByKppAndOktmo(departmentConfig.getKpp(), departmentConfig.getOktmo().getCode()));
        RefBookDataProvider provider = refBookFactory.getDataProvider(NDFL_DETAIL.getId());
        provider.updateRecordVersions(logger, departmentConfig.getStartDate(), departmentConfig.getEndDate(), new HashSet<>(singletonList(convertToMap(departmentConfig))));
    }

    private String getUpdateFailedMessage(DepartmentConfig departmentConfig) {
        return String.format("Ошибка при сохранении настройки подразделения для \"%s\" с параметрами: КПП: \"%s\", ОКТМО: \"%s\", дата начала действия настройки: " +
                        "\"%s\". Обратитесь к Администратору Системы или повторите операцию позднее.",
                departmentConfig.getDepartment().getName(), departmentConfig.getKpp(), departmentConfig.getOktmo().getCode(),
                FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getStartDate()));
    }

    @Override
    @PreAuthorize("hasPermission(#ids, 'com.aplana.sbrf.taxaccounting.model.refbook.DepartmentConfig', T(com.aplana.sbrf.taxaccounting.permissions.DepartmentConfigPermission).DELETE)")
    public ActionResult delete(List<Long> ids, TAUserInfo userInfo) {
        LOG.info("delete: ids=" + ids);
        ActionResult result = new ActionResult();
        Logger logger = new Logger();
        for (long id : ids) {
            LOG.info("delete: id=" + id);
            Logger localLogger = new Logger();
            localLogger.setTaUserInfo(userInfo);
            DepartmentConfig departmentConfig = fetch(id);
            try {
                delete(departmentConfig, localLogger);
            } catch (ServiceException e) {
                LOG.error(e.getMessage());
                localLogger.error(e.getMessage());
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
                localLogger.error(getDeleteFailedMessage(departmentConfig, e.getMessage()));
            }
            if (localLogger.containsLevel(LogLevel.ERROR)) {
                LOG.error("failed");
                localLogger.error(getDeleteFailedMessage(departmentConfig, ""));
            } else {
                localLogger.info("В подразделении \"%s\" удалена настройка КПП: \"%s\", ОКТМО: \"%s\", период актуальности с \"%s\" по \"%s\"",
                        departmentConfig.getDepartment().getName(), departmentConfig.getKpp(), departmentConfig.getOktmo().getCode(),
                        FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getStartDate()),
                        departmentConfig.getEndDate() == null ? "-" : FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getEndDate()));
            }
            logger.getEntries().addAll(localLogger.getEntries());
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Transactional
    public void delete(DepartmentConfig departmentConfig, Logger logger) {
        RefBookDataProvider provider = refBookFactory.getDataProvider(NDFL_DETAIL.getId());
        provider.deleteRecordVersions(logger, new ArrayList<Long>(singletonList(departmentConfig.getId())));
    }

    private String getDeleteFailedMessage(DepartmentConfig departmentConfig, String error) {
        return String.format("Возникла ошибка при удалении настройки подразделения \"%s\", КПП: \"%s\", ОКТМО: \"%s\", период актуальности с " +
                        "\"%s\" по \"%s\". %s",
                departmentConfig.getDepartment().getName(), departmentConfig.getKpp(), departmentConfig.getOktmo().getCode(),
                FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getStartDate()),
                departmentConfig.getEndDate() == null ? "-" : FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getEndDate()),
                error);
    }

    private void checkDepartmentConfig(DepartmentConfig departmentConfig, List<DepartmentConfig> relatedDepartmentConfigs) {
        Date minDate = null, maxDate = new Date(0);
        for (DepartmentConfig relatedDepartmentConfig : relatedDepartmentConfigs) {
            if (departmentConfig.getId() == null || !departmentConfig.getId().equals(relatedDepartmentConfig.getId())) {
                // Проверка пересечения существующей с исходной
                if (!(relatedDepartmentConfig.getEndDate() != null && departmentConfig.getStartDate().after(relatedDepartmentConfig.getEndDate())
                        || departmentConfig.getStartDate().before(relatedDepartmentConfig.getStartDate())
                        && departmentConfig.getEndDate() != null && departmentConfig.getEndDate().before(relatedDepartmentConfig.getStartDate()))) {
                    throw new ServiceException("Для настройки подразделения КПП: \"%s\", ОКТМО: \"%s\", период актуальности: с \"%s\" по \"%s\" в подразделении: \"%s\" " +
                            "уже существует настройка с такими же КПП и ОКТМО и пересекающимся периодом актуальности: с \"%s\" по \"%s\".",
                            departmentConfig.getKpp(), departmentConfig.getOktmo().getCode(),
                            FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getStartDate()),
                            departmentConfig.getEndDate() == null ? "-" : FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getEndDate()),
                            departmentConfig.getDepartment().getName(),
                            FastDateFormat.getInstance("dd.MM.yyyy").format(relatedDepartmentConfig.getStartDate()),
                            relatedDepartmentConfig.getEndDate() == null ? "-" : FastDateFormat.getInstance("dd.MM.yyyy").format(relatedDepartmentConfig.getEndDate()));
                }
                if (minDate == null || relatedDepartmentConfig.getStartDate().before(minDate)) {
                    minDate = relatedDepartmentConfig.getStartDate();
                }
                if (maxDate != null && (relatedDepartmentConfig.getEndDate() == null || relatedDepartmentConfig.getEndDate().after(maxDate))) {
                    maxDate = relatedDepartmentConfig.getEndDate();
                }
            }
        }
        if (!(minDate == null || DateUtils.addDays(minDate, -1).equals(departmentConfig.getEndDate())
                || maxDate != null && DateUtils.addDays(maxDate, 1).equals(departmentConfig.getStartDate()))) {
            throw new ServiceException("Между периодом актуальности настройки подразделения КПП: \"%s\", ОКТМО: \"%s\" период актуальности с \"%s\" по \"%s\" " +
                    "в подразделении: \"%s\" и другими настройками с такими же КПП и ОКТМО  имеется временной разрыв более 1 календарного дня.",
                    departmentConfig.getKpp(), departmentConfig.getOktmo().getCode(),
                    FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getStartDate()),
                    departmentConfig.getEndDate() == null ? "-" : FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getEndDate()),
                    departmentConfig.getDepartment().getName());
        }
    }

    private List<DepartmentConfig> convertToDepartmentConfigs(List<Map<String, RefBookValue>> records) {
        List<DepartmentConfig> result = new ArrayList<>();
        // Создаем кэши для исключения избыточных запросов к БД
        Map<Long, Map<Long, Object>> cache = new HashMap<>();
        for (Map<String, RefBookValue> record : records) {
            result.add(convertToDepartmentConfig(record, cache));
        }
        return result;
    }

    private DepartmentConfig convertToDepartmentConfig(Map<String, RefBookValue> record, Map<Long, Map<Long, Object>> cache) {
        DepartmentConfig result = new DepartmentConfig();
        RefBookDataProvider provider = refBookFactory.getDataProvider(NDFL_DETAIL.getId());
        Long departmentId = record.get(DepartmentConfigDetailAliases.DEPARTMENT_ID.name()).getReferenceValue();
        Long presentPlaceId = record.get(DepartmentConfigDetailAliases.PRESENT_PLACE.name()).getReferenceValue();
        Long oktmoId = record.get(DepartmentConfigDetailAliases.OKTMO.name()).getReferenceValue();
        Long reorganizationId = record.get(DepartmentConfigDetailAliases.REORG_FORM_CODE.name()).getReferenceValue();
        Long signatoryMarkId = record.get(DepartmentConfigDetailAliases.SIGNATORY_ID.name()).getReferenceValue();

        result.setId(record.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue());
        result.setRecordId(record.get(RefBook.BUSINESS_ID_ALIAS).getNumberValue().longValue());
        RefBookRecordVersion version = provider.getRecordVersionInfo(result.getId());
        if (record.get(RefBook.RECORD_SORT_ALIAS) != null) {
            result.setRowOrd(record.get(RefBook.RECORD_SORT_ALIAS).getNumberValue().intValue());
        }
        result.setStartDate(version.getVersionStart());
        result.setEndDate(version.getVersionEnd());
        result.setDepartment((RefBookDepartment) getRecordWithCache(DEPARTMENT.getId(), departmentId, cache));
        result.setKpp(record.get(DepartmentConfigDetailAliases.KPP.name()).getStringValue());
        result.setOktmo((RefBookOktmo) getRecordWithCache(OKTMO.getId(), oktmoId, cache));
        result.setTaxOrganCode(record.get(DepartmentConfigDetailAliases.TAX_ORGAN_CODE.name()).getStringValue());
        result.setPresentPlace((RefBookPresentPlace) getRecordWithCache(PRESENT_PLACE.getId(), presentPlaceId, cache));
        result.setName(record.get(DepartmentConfigDetailAliases.NAME.name()).getStringValue());
        result.setPhone(record.get(DepartmentConfigDetailAliases.PHONE.name()).getStringValue());
        result.setSignatoryMark((RefBookSignatoryMark) getRecordWithCache(MARK_SIGNATORY_CODE.getId(), signatoryMarkId, cache));
        result.setSignatorySurName(record.get(DepartmentConfigDetailAliases.SIGNATORY_SURNAME.name()).getStringValue());
        result.setSignatoryFirstName(record.get(DepartmentConfigDetailAliases.SIGNATORY_FIRSTNAME.name()).getStringValue());
        result.setSignatoryLastName(record.get(DepartmentConfigDetailAliases.SIGNATORY_LASTNAME.name()).getStringValue());
        result.setApproveDocName(record.get(DepartmentConfigDetailAliases.APPROVE_DOC_NAME.name()).getStringValue());
        result.setApproveOrgName(record.get(DepartmentConfigDetailAliases.APPROVE_ORG_NAME.name()).getStringValue());
        result.setReorganization((RefBookReorganization) getRecordWithCache(REORGANIZATION.getId(), reorganizationId, cache));
        result.setReorgInn(record.get(DepartmentConfigDetailAliases.REORG_INN.name()).getStringValue());
        result.setReorgKpp(record.get(DepartmentConfigDetailAliases.REORG_KPP.name()).getStringValue());
        return result;
    }

    private RefBookRecord convertToRefBookRecord(DepartmentConfig departmentConfig) {
        RefBookRecord refBookRecord = new RefBookRecord();
        refBookRecord.setUniqueRecordId(departmentConfig.getId());
        refBookRecord.setRecordId(departmentConfig.getRecordId());
        refBookRecord.setValues(convertToMap(departmentConfig));
        return refBookRecord;
    }

    private Map<String, RefBookValue> convertToMap(DepartmentConfig departmentConfig) {
        Map<String, RefBookValue> values = new HashMap<>();
        values.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, departmentConfig.getId()));
        values.put(RefBook.BUSINESS_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, departmentConfig.getRecordId()));
        values.put(DepartmentConfigDetailAliases.DEPARTMENT_ID.name(), new RefBookValue(RefBookAttributeType.REFERENCE, departmentConfig.getDepartment().getId().longValue()));
        values.put(DepartmentConfigDetailAliases.KPP.name(), new RefBookValue(RefBookAttributeType.STRING, departmentConfig.getKpp()));
        values.put(DepartmentConfigDetailAliases.OKTMO.name(), new RefBookValue(RefBookAttributeType.REFERENCE, departmentConfig.getOktmo().getId().longValue()));
        values.put(DepartmentConfigDetailAliases.TAX_ORGAN_CODE.name(), new RefBookValue(RefBookAttributeType.STRING, departmentConfig.getTaxOrganCode()));
        values.put(DepartmentConfigDetailAliases.PRESENT_PLACE.name(), new RefBookValue(RefBookAttributeType.REFERENCE, departmentConfig.getPresentPlace().getId().longValue()));
        values.put(DepartmentConfigDetailAliases.NAME.name(), new RefBookValue(RefBookAttributeType.STRING, departmentConfig.getName()));
        values.put(DepartmentConfigDetailAliases.PHONE.name(), new RefBookValue(RefBookAttributeType.STRING, departmentConfig.getPhone()));
        values.put(DepartmentConfigDetailAliases.SIGNATORY_ID.name(), new RefBookValue(RefBookAttributeType.REFERENCE, departmentConfig.getSignatoryMark().getId().longValue()));
        values.put(DepartmentConfigDetailAliases.SIGNATORY_SURNAME.name(), new RefBookValue(RefBookAttributeType.STRING, departmentConfig.getSignatorySurName()));
        values.put(DepartmentConfigDetailAliases.SIGNATORY_FIRSTNAME.name(), new RefBookValue(RefBookAttributeType.STRING, departmentConfig.getSignatoryFirstName()));
        values.put(DepartmentConfigDetailAliases.SIGNATORY_LASTNAME.name(), new RefBookValue(RefBookAttributeType.STRING, departmentConfig.getSignatoryLastName()));
        values.put(DepartmentConfigDetailAliases.APPROVE_DOC_NAME.name(), new RefBookValue(RefBookAttributeType.STRING, departmentConfig.getApproveDocName()));
        values.put(DepartmentConfigDetailAliases.APPROVE_ORG_NAME.name(), new RefBookValue(RefBookAttributeType.STRING, departmentConfig.getApproveOrgName()));
        values.put(DepartmentConfigDetailAliases.REORG_FORM_CODE.name(), new RefBookValue(RefBookAttributeType.REFERENCE, departmentConfig.getReorganization() == null ? null : departmentConfig.getReorganization().getId().longValue()));
        values.put(DepartmentConfigDetailAliases.REORG_INN.name(), new RefBookValue(RefBookAttributeType.STRING, departmentConfig.getReorgInn()));
        values.put(DepartmentConfigDetailAliases.REORG_KPP.name(), new RefBookValue(RefBookAttributeType.STRING, departmentConfig.getReorgKpp()));
        return values;
    }

    private <T extends RefBookSimple> T getRecordWithCache(Long refBookId, Long id, Map<Long, Map<Long, Object>> cache) {
        T value = null;
        if (id != null) {
            Map<Long, Object> recordCache = null;
            if (cache != null) {
                recordCache = cache.get(refBookId);
                if (recordCache == null) {
                    recordCache = new HashMap<>();
                    cache.put(refBookId, recordCache);
                }
                value = (T) recordCache.get(id);
            }
            if (value == null) {
                value = commonRefBookService.fetchRecord(refBookId, id);
                if (recordCache != null) {
                    recordCache.put(id, value);
                }
            }
        }
        return value;
    }

    /**
     * Константы соответствующие названиям аттрибутов в справочнике настроек подразделений
     */
    public enum DepartmentConfigDetailAliases {
        DEPARTMENT_ID,
        TAX_ORGAN_CODE,
        KPP,
        PRESENT_PLACE,
        NAME,
        OKTMO,
        PHONE,
        REORG_FORM_CODE,
        REORG_INN,
        REORG_KPP,
        SIGNATORY_ID,
        SIGNATORY_SURNAME,
        SIGNATORY_FIRSTNAME,
        SIGNATORY_LASTNAME,
        APPROVE_DOC_NAME,
        APPROVE_ORG_NAME
    }
}
