package com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.server;

import com.aplana.sbrf.taxaccounting.dao.impl.refbook.RefBookUtils;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.server.DepartmentParamAliases;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared.SaveDepartmentRefBookValuesAction;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared.SaveDepartmentRefBookValuesResult;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared.TableCell;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class SaveDepartmentRefBookValuesHandler extends AbstractActionHandler<SaveDepartmentRefBookValuesAction, SaveDepartmentRefBookValuesResult> {

    public SaveDepartmentRefBookValuesHandler() {
        super(SaveDepartmentRefBookValuesAction.class);
    }
    private static final String SUCCESS_INFO = "Настройки для \"%s\" в периоде с %s по %s успешно сохранены.";

    private static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

    @Autowired
    RefBookFactory rbFactory;
    @Autowired
    DepartmentReportPeriodService reportPeriodService;
    @Autowired
    SecurityService securityService;
    @Autowired
    PeriodService periodService;
    @Autowired
    LogEntryService logEntryService;
    @Autowired
    DepartmentService departmentService;
    @Autowired
    RefBookHelper refBookHelper;

    @Override
    public SaveDepartmentRefBookValuesResult execute(SaveDepartmentRefBookValuesAction action, ExecutionContext executionContext) throws ActionException {
        Logger logger = new Logger();
        logger.setTaUserInfo(securityService.currentUserInfo());
        SaveDepartmentRefBookValuesResult result = new SaveDepartmentRefBookValuesResult();
        RefBook slaveRefBook = rbFactory.get(action.getSlaveRefBookId());

        /** Специфичные проверки для настроек подразделений */
        Pattern innPattern = Pattern.compile(RefBookUtils.INN_JUR_PATTERN);
        Pattern kppPattern = Pattern.compile(RefBookUtils.KPP_PATTERN);
        Pattern taxOrganPattern = Pattern.compile(RefBookUtils.TAX_ORGAN_PATTERN);

        String inn = action.getNotTableParams().get("INN").getStringValue();
        if (inn != null && !inn.isEmpty()) {
            if (checkPattern(logger, false, null, "ИНН", inn, innPattern, RefBookUtils.INN_JUR_MEANING)){
                checkSumInn(logger, false, null, null, "ИНН", inn);
            }
        }

        int i = 1;
        for (Map<String, TableCell> row : action.getRows()) {
            String sig = row.get("SIGNATORY_ID").getDeRefValue();
            Integer signatoryId = (sig == null || sig.isEmpty()) ?
                    null :
                    Integer.parseInt(row.get("SIGNATORY_ID").getDeRefValue().trim());
            String approveDocName = row.get("APPROVE_DOC_NAME").getStringValue();
            String approveOrgName = row.get("APPROVE_ORG_NAME").getStringValue();

            if ((signatoryId != null && signatoryId == 1)
                    && ((approveDocName != null && !approveDocName.isEmpty())
                    ||  (approveOrgName != null && !approveOrgName.isEmpty()))
               ) {
                logger.error("Поля \"Наименование документа представителя\", " +
                        "\"Наименование организации представителя\" " +
                        "должны заполняться только в том случае, если " +
                        "поле \"Признак лица, подписавшего документ\" равно значению \"2\" (представитель налогоплательщика)");
                result.setHasFatalError(true);
                result.setErrorType(SaveDepartmentRefBookValuesResult.ERROR_TYPE.INCORRECT_FIELDS);
                break;
            }
            String taxOrganCode = row.get("TAX_ORGAN_CODE").getStringValue();
            String kpp = row.get("KPP").getStringValue();
            if (row.get("REORG_INN").getStringValue() != null && !row.get("REORG_INN").getStringValue().isEmpty()) {
                if (checkPattern(logger, true,  i, "ИНН реорганизованной организации",row.get("REORG_INN").getStringValue(), innPattern, RefBookUtils.INN_JUR_MEANING)){
                    checkSumInn(logger, true, taxOrganCode, kpp, "ИНН реорганизованной организации", row.get("REORG_INN").getStringValue());
                }
            }
            if (row.get("KPP").getStringValue() != null && !row.get("KPP").getStringValue().isEmpty()) {
                checkPattern(logger, true, i,"КПП",  row.get("KPP").getStringValue(), kppPattern, RefBookUtils.KPP_MEANING);
            }
            if (row.get("REORG_KPP").getStringValue() != null && !row.get("REORG_KPP").getStringValue().isEmpty()) {
                checkPattern(logger, true, i, "КПП реорганизованной организации", row.get("REORG_KPP").getStringValue(), kppPattern, RefBookUtils.KPP_MEANING);
            }
            if (row.get("TAX_ORGAN_CODE").getStringValue() != null && !row.get("TAX_ORGAN_CODE").getStringValue().isEmpty()) {
                checkPattern(logger, true, i, "Код налогового органа", row.get("TAX_ORGAN_CODE").getStringValue(), taxOrganPattern, RefBookUtils.TAX_ORGAN_MEANING);
            }
            i++;
        }

        if (logger.containsLevel(LogLevel.ERROR) && result.getErrorType() == SaveDepartmentRefBookValuesResult.ERROR_TYPE.NONE){
            result.setHasFatalError(true);
            result.setErrorType(SaveDepartmentRefBookValuesResult.ERROR_TYPE.COMMON_ERROR);
            prepareResult(result, logger, action);
            return result;
        }

        if (result.isHasFatalError()) {
            prepareResult(result, logger, action);
            return result;
        }

        /** Сохранение настроек подразделений */
        RefBookRecordVersion recordVersion = null;
        try {
            recordVersion = refBookHelper.saveOrUpdateDepartmentConfig(
                    action.getRecordId(), action.getRefBookId(), action.getSlaveRefBookId(), action.getReportPeriodId(),
                    DepartmentParamAliases.DEPARTMENT_ID.name(), action.getDepartmentId(),
                    convert(action.getNotTableParams()), convertRows(action.getRows()), logger);
        } catch (Exception e) {
            result.setHasFatalError(true);
            result.setErrorType(SaveDepartmentRefBookValuesResult.ERROR_TYPE.COMMON_ERROR);
            logger.error(e.getMessage());
        }
        if (logger.getMainMsg() != null) {
            result.setErrorMsg(logger.getMainMsg());
        }

        String departmentName = departmentService.getDepartment(action.getDepartmentId()).getName();
        if (!logger.containsLevel(LogLevel.ERROR)) {
            if (recordVersion != null) {
                if (recordVersion.getVersionEnd() != null) {
                    logger.info(String.format(SUCCESS_INFO, departmentName, sdf.get().format(recordVersion.getVersionStart()), sdf.get().format(recordVersion.getVersionEnd())));
                } else {
                    logger.info(String.format(SUCCESS_INFO, departmentName, sdf.get().format(recordVersion.getVersionStart()), "\"-\""));
                }
            }
        }

        prepareResult(result, logger, action);
        return result;
    }

    private void prepareResult(SaveDepartmentRefBookValuesResult result, Logger logger, SaveDepartmentRefBookValuesAction action){
        if (result.isHasFatalError()) {
            logger.clear(LogLevel.INFO);
        }
        if (action.getOldUUID() == null) {
            result.setUuid(logEntryService.save(logger.getEntries()));
        } else {
            result.setUuid(logEntryService.update(logger.getEntries(), action.getOldUUID()));
        }
    }

    @Override
    public void undo(SaveDepartmentRefBookValuesAction saveDepartmentRefBookValuesAction, SaveDepartmentRefBookValuesResult saveDepartmentRefBookValuesResult, ExecutionContext executionContext) throws ActionException {

    }

    private List<Map<String,RefBookValue>> convertRows(List<Map<String, TableCell>> rows) {
        List<Map<String,RefBookValue>> convertedRows = new ArrayList<Map<String,RefBookValue>>();
        for (Map<String, TableCell> row : rows) {
            Map<String,RefBookValue> convertedRow = convert(row);
            convertedRows.add(convertedRow);
        }
        return convertedRows;
    }

    private Map<String,RefBookValue> convert(Map<String, TableCell> row) {
        Map<String,RefBookValue> convertedRow = new HashMap<String, RefBookValue>();
        for (Map.Entry<String, TableCell> e : row.entrySet()) {
            RefBookValue value = null;
            if (e.getValue().getType() == null) {
                convertedRow.put(e.getKey(), null);
                continue;
            }
            switch (e.getValue().getType()) {
                case STRING:
                    value = new RefBookValue(e.getValue().getType(), e.getValue().getStringValue());
                    break;
                case DATE:
                    value = new RefBookValue(e.getValue().getType(), e.getValue().getDateValue());
                    break;
                case NUMBER:
                    value = new RefBookValue(e.getValue().getType(), e.getValue().getNumberValue());
                    break;
                case REFERENCE:
                    value = new RefBookValue(e.getValue().getType(), e.getValue().getRefValue());
                    break;
                default:
                    break;
            }
            convertedRow.put(e.getKey(), value);
        }
        return convertedRow;
    }

    private boolean checkPattern(Logger logger, boolean isTable, Integer index, String name, String value, Pattern pattern, String patternMeaning) {
        if (value != null && !pattern.matcher(value).matches()){
            if (isTable)
                logger.error("Строка %s: Поле \"%s\" заполнено неверно (%s)! Ожидаемый паттерн: \"%s\".", index, name, value, pattern.pattern());
            else
                logger.error("Поле \"%s\" заполнено неверно (%s)! Ожидаемый паттерн: \"%s\".", name, value, pattern.pattern());
            logger.error("Расшифровка паттерна \"%s\": %s.", pattern.pattern(), patternMeaning);
            return false;
        }
        return true;
    }

    private void checkSumInn(Logger logger, boolean isTable, String taxOrganCode, String kpp, String name, String value) {
        if (value != null && !RefBookUtils.checkControlSumInn(value)){
            if (isTable)
                logger.error("Код налогового органа \"%s\", КПП \"%s\": Вычисленное контрольное число по полю \"%s\" некорректно (%s).", taxOrganCode, kpp, name, value);
            else
                logger.error("Вычисленное контрольное число по полю \"%s\" некорректно (%s).", name, value);
        }
    }
}
