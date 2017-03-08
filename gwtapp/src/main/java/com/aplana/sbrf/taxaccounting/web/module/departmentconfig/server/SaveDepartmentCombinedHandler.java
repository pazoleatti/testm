package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.server;

import com.aplana.sbrf.taxaccounting.dao.impl.refbook.RefBookUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.DepartmentCombined;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.SaveDepartmentCombinedAction;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.SaveDepartmentCombinedResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Dmitriy Levykin
 */
@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class SaveDepartmentCombinedHandler extends AbstractActionHandler<SaveDepartmentCombinedAction,
        SaveDepartmentCombinedResult> {

    private static final String SUCCESS_INFO = "Настройки для \"%s\" в периоде с %s по %s успешно сохранены.";
    private static final ThreadLocal<SimpleDateFormat> SDF = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

    @Autowired
    private PeriodService periodService;
    @Autowired
    private RefBookFactory rbFactory;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;

    public SaveDepartmentCombinedHandler() {
        super(SaveDepartmentCombinedAction.class);
    }

    @Override
    public SaveDepartmentCombinedResult execute(SaveDepartmentCombinedAction action, ExecutionContext executionContext)
            throws ActionException {
        SaveDepartmentCombinedResult result = new SaveDepartmentCombinedResult();

        DepartmentCombined depCombined = action.getDepartmentCombined();

        if (depCombined != null
                && depCombined.getDepartmentId() != null
                && !depCombined.getDepartmentId().isEmpty()
                && action.getTaxType() != null
                && action.getReportPeriodId() != null) {

            DepartmentReportPeriodFilter departmentReportPeriodFilter = new DepartmentReportPeriodFilter();
            departmentReportPeriodFilter.setDepartmentIdList(Arrays.asList(depCombined.getDepartmentId().get(0).intValue()));
            departmentReportPeriodFilter.setReportPeriodIdList(Arrays.asList(action.getReportPeriodId()));
            List<DepartmentReportPeriod> departmentReportPeriodList = departmentReportPeriodService.getListByFilter(departmentReportPeriodFilter);
            if (departmentReportPeriodList.isEmpty()) {
                throw new ActionException("Не найден отчетный период!");
            }

            Long refBookId = null;
            switch (action.getTaxType()) {
                case NDFL:
                    refBookId = RefBook.Id.NDFL.getId();
                    break;
                case PFR:
                    refBookId = RefBook.Id.FOND.getId();
                    break;
            }
            RefBookDataProvider provider = rbFactory.getDataProvider(refBookId);
            RefBook refBook = rbFactory.get(refBookId);

            ReportPeriod period = periodService.getReportPeriod(action.getReportPeriodId());

            Map<String, RefBookValue> paramsMap = new HashMap<String, RefBookValue>();
            // Id записи
            paramsMap.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, depCombined.getRecordId()));

            // Общая часть
            paramsMap.put(DepartmentParamAliases.DEPARTMENT_ID.name(), new RefBookValue(RefBookAttributeType.REFERENCE, getFirstLong(depCombined.getDepartmentId())));
            paramsMap.put(DepartmentParamAliases.DICT_REGION_ID.name(), new RefBookValue(RefBookAttributeType.REFERENCE, getFirstLong(depCombined.getDictRegionId())));
            paramsMap.put(DepartmentParamAliases.OKTMO.name(), new RefBookValue(RefBookAttributeType.REFERENCE, getFirstLong(depCombined.getOktmo())));
            paramsMap.put(DepartmentParamAliases.INN.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getInn()));
            paramsMap.put(DepartmentParamAliases.KPP.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getKpp()));
            paramsMap.put(DepartmentParamAliases.TAX_ORGAN_CODE.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getTaxOrganCode()));
            paramsMap.put(DepartmentParamAliases.OKVED_CODE.name(), new RefBookValue(RefBookAttributeType.REFERENCE, getFirstLong(depCombined.getOkvedCode())));
            paramsMap.put(DepartmentParamAliases.PHONE.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getPhone()));

            paramsMap.put(DepartmentParamAliases.REORG_FORM_CODE.name(), new RefBookValue(RefBookAttributeType.REFERENCE, getFirstLong(depCombined.getReorgFormCode())));
            paramsMap.put(DepartmentParamAliases.REORG_INN.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getReorgInn()));
            paramsMap.put(DepartmentParamAliases.REORG_KPP.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getReorgKpp()));

            paramsMap.put(DepartmentParamAliases.NAME.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getName()));

            // Общая частная часть
            paramsMap.put(DepartmentParamAliases.SIGNATORY_ID.name(), new RefBookValue(RefBookAttributeType.REFERENCE, getFirstLong(depCombined.getSignatoryId())));
            paramsMap.put(DepartmentParamAliases.SIGNATORY_SURNAME.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getSignatorySurname()));
            paramsMap.put(DepartmentParamAliases.SIGNATORY_FIRSTNAME.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getSignatoryFirstname()));
            paramsMap.put(DepartmentParamAliases.SIGNATORY_LASTNAME.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getSignatoryLastname()));
            paramsMap.put(DepartmentParamAliases.APPROVE_DOC_NAME.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getApproveDocName()));
            paramsMap.put(DepartmentParamAliases.APPROVE_ORG_NAME.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getApproveOrgName()));
            paramsMap.put(DepartmentParamAliases.TAX_PLACE_TYPE_CODE.name(), new RefBookValue(RefBookAttributeType.REFERENCE, getFirstLong(depCombined.getTaxPlaceTypeCode())));
            paramsMap.put(DepartmentParamAliases.FORMAT_VERSION.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getFormatVersion()));

            // Налог на прибыль
            if (action.getTaxType() == TaxType.INCOME) {
                paramsMap.put(DepartmentParamAliases.SUM_TAX.name(), new RefBookValue(RefBookAttributeType.NUMBER, depCombined.getSumTax()));
                paramsMap.put(DepartmentParamAliases.SUM_DIVIDENDS.name(), new RefBookValue(RefBookAttributeType.NUMBER, depCombined.getSumDividends()));
                paramsMap.put(DepartmentParamAliases.OBLIGATION.name(), new RefBookValue(RefBookAttributeType.REFERENCE, getFirstLong(depCombined.getObligation())));
                paramsMap.put(DepartmentParamAliases.TAX_RATE.name(), new RefBookValue(RefBookAttributeType.NUMBER, depCombined.getTaxRate()));
                paramsMap.put(DepartmentParamAliases.TYPE.name(), new RefBookValue(RefBookAttributeType.REFERENCE, getFirstLong(depCombined.getType())));
                paramsMap.put(DepartmentParamAliases.ADDITIONAL_NAME.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getAdditionalName()));
            }

            Logger logger = new Logger();

            logger.setTaUserInfo(securityService.currentUserInfo());
            RefBookRecord record = new RefBookRecord();
            record.setValues(paramsMap);
            record.setRecordId(depCombined.getRecordId());

            // Проверка значения атрибута на соответствие паттерну
            Pattern innPattern = Pattern.compile(RefBookUtils.INN_JUR_PATTERN);
            Pattern kppPattern = Pattern.compile(RefBookUtils.KPP_PATTERN);
            Pattern taxOrganPattern = Pattern.compile(RefBookUtils.TAX_ORGAN_PATTERN);

            if (depCombined.getReorgInn() != null && !depCombined.getReorgInn().isEmpty()) {
                if (checkPattern(logger, "ИНН реорганизованной организации", depCombined.getReorgInn(), innPattern, RefBookUtils.INN_JUR_MEANING)){
                    checkSumInn(logger, "ИНН реорганизованной организации", depCombined.getReorgInn());
                }
            }
            if (depCombined.getReorgKpp() != null && !depCombined.getReorgKpp().isEmpty()) {
                checkPattern(logger, "КПП реорганизованной организации", depCombined.getReorgKpp(), kppPattern, RefBookUtils.KPP_MEANING);
            }
            if (depCombined.getInn() != null && !depCombined.getInn().isEmpty()) {
                if (checkPattern(logger, "ИНН", depCombined.getInn(), innPattern, RefBookUtils.INN_JUR_MEANING)){
                    checkSumInn(logger, "ИНН", depCombined.getInn());
                }
            }
            if (depCombined.getKpp() != null && !depCombined.getKpp().isEmpty()) {
                checkPattern(logger, "КПП", depCombined.getKpp(), kppPattern, RefBookUtils.KPP_MEANING);
            }

            if (depCombined.getTaxOrganCode() != null && !depCombined.getTaxOrganCode().isEmpty()) {
                checkPattern(logger, "Код налогового органа", depCombined.getTaxOrganCode(), taxOrganPattern, RefBookUtils.TAX_ORGAN_MEANING);
            }

            if (logger.containsLevel(LogLevel.ERROR)){
                result.setHasError(true);
                if (action.getOldUUID() == null) {
                    result.setUuid(logEntryService.save(logger.getEntries()));
                } else {
                    result.setUuid(logEntryService.update(logger.getEntries(), action.getOldUUID()));
                }
                return result;
            }

            // Проверка необходимости редактирования
            boolean needEdit = false;

            // Поиск версий настроек для указанного подразделения. Если они есть - создаем новую версию с существующим record_id, иначе создаем новый record_id (по сути элемент справочника)
            String filter = DepartmentParamAliases.DEPARTMENT_ID.name() + " = " + action.getDepartmentCombined().getDepartmentId().get(0);
            List<Pair<Long, Long>> recordPairs = provider.checkRecordExistence(null, filter);
            if (!recordPairs.isEmpty()) {
                //Проверяем, к одному ли элементу относятся версии
                Set<Long> recordIdSet = new HashSet<Long>();
                for (Pair<Long, Long> pair : recordPairs) {
                    recordIdSet.add(pair.getSecond());
                }

                if (recordIdSet.size() > 1) {
                    throw new ActionException("Версии настроек, отобраные по фильтру, относятся к разным подразделениям");
                }

                // Существуют версии настроек для указанного подразделения
                record.setRecordId(recordPairs.get(0).getSecond());
            }

            // Проверяем, нужно ли обновление существующих настроек
            recordPairs = provider.checkRecordExistence(period.getCalendarStartDate(), filter);
            if (!recordPairs.isEmpty()) {
                needEdit = true;
                // Запись нашлась
                if (recordPairs.size() != 1) {
                    throw new ActionException("Найдено несколько настроек для подразделения");
                }
                depCombined.setRecordId(recordPairs.get(0).getFirst());
            }

            RefBookRecordVersion recordVersion;
            Date versionTo = provider.getEndVersion(record.getRecordId(), period.getCalendarStartDate());
            if (!needEdit) {
                List<Long> newRecordIds = provider.createRecordVersion(logger, period.getCalendarStartDate(), versionTo, Arrays.asList(record));
                recordVersion = provider.getRecordVersionInfo(newRecordIds.get(0));
            } else {
                provider.updateRecordVersion(logger, depCombined.getRecordId(), period.getCalendarStartDate(), versionTo, paramsMap);
                recordVersion = provider.getRecordVersionInfo(depCombined.getRecordId());
            }
            if (logger.getMainMsg() != null) {
                result.setErrorMsg(logger.getMainMsg());
            }

            String departmentName = departmentService.getDepartment(action.getDepartment()).getName();
            if (!logger.containsLevel(LogLevel.ERROR)) {
                if (recordVersion.getVersionEnd() != null) {
                    logger.info(String.format(SUCCESS_INFO, departmentName, SDF.get().format(recordVersion.getVersionStart()), SDF.get().format(recordVersion.getVersionEnd())));
                } else {
                    logger.info(String.format(SUCCESS_INFO, departmentName, SDF.get().format(recordVersion.getVersionStart()), "\"-\""));
                }
            }

            if (action.getOldUUID() == null) {
                result.setUuid(logEntryService.save(logger.getEntries()));
            } else {
                result.setUuid(logEntryService.update(logger.getEntries(), action.getOldUUID()));
            }

            if (logger.containsLevel(LogLevel.ERROR)) {
                result.setHasError(true);
            }
        }
        return result;
    }

    @Override
    public void undo(SaveDepartmentCombinedAction action, SaveDepartmentCombinedResult result,
                     ExecutionContext executionContext) throws ActionException {
        // Не требуется
    }

    private Long getFirstLong(List<Long> list) {
        return list != null && !list.isEmpty() ? list.get(0) : null;
    }

    private boolean checkPattern(Logger logger, String name, String value, Pattern pattern, String patternMeaning) {
        if (value != null && !pattern.matcher(value).matches()){
            logger.error("Поле \"%s\" заполнено неверно (%s)! Ожидаемый паттерн: \"%s\".", name, value, pattern.pattern());
            logger.error("Расшифровка паттерна \"%s\": %s.", pattern.pattern(), patternMeaning);
            return false;
        }
        return true;
    }

    private void checkSumInn(Logger logger, String name, String value) {
        if (value != null && !RefBookUtils.checkControlSumInn(value)){
            logger.error("Вычисленное контрольное число по полю \"%s\" некорректно (%s).", name, value);
        }
    }
}