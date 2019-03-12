package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.AsyncTaskTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.RefBookUtils;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskType;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskTypeData;
import com.aplana.sbrf.taxaccounting.model.Configuration;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParamGroup;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParamModel;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.ConfigurationService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.utils.FileWrapper;
import com.aplana.sbrf.taxaccounting.utils.ResourceUtils;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Service("configurationService")
@Transactional
public class ConfigurationServiceImpl implements ConfigurationService {
    private static final int INN_JUR_LENGTH = 10;
    private static final int COMMON_PARAM_DEPARTMENT_ID = 0;
    private static final int UPLOAD_REFBOOK_ASYNC_TASK_LIMIT = 1500000;
    private static final String SBERBANK_INN_DEFAULT = "7707083893";
    private static final String NO_CODE_DEFAULT = "9979";
    private static final String SHOW_TIMING_DEFAULT = "0";
    private static final String LIMIT_IDENT_DEFAULT = "0.65";
    private static final String ENABLE_IMPORT_PERSON_DEFAULT = "1";
    private static final String CONSOLIDATION_DATA_SELECTION_DEPTH_DEFAULT = "3";
    private static final String REPORT_PERIOD_YEAR_MIN = "2003";
    private static final String REPORT_PERIOD_YEAR_MAX = "2100";
    private static final String DECLARATION_ROWS_BULK_EDIT_MAX_COUNT_DEFAULT = "200";
    private static final String ASYNC_MODE_DEFAULT = "0";
    private static final String READ_ERROR = "«%s»: Отсутствует доступ на чтение!";
    private static final String WRITE_ERROR = "«%s»: Отсутствует доступ на запись!";
    private static final String READ_INFO = "«%s»: Присутствует доступ на чтение!";
    private static final String WRITE_INFO = "«%s»: Присутствует доступ на запись!";
    private static final String NO_CODE_ERROR = "Код НО (пром.) (\"%s\") не найден в справочнике \"Налоговые инспекции\"";
    private static final String INN_JUR_ERROR = "Введен некорректный номер ИНН «%s»";
    private static final String TASK_LIMIT_FIELD = "Ограничение на выполнение задачи";
    private static final String SHORT_QUEUE_LIMIT = "Ограничение на выполнение задачи в очереди быстрых задач";
    private static final String LIMIT_IDENT_ERROR = "Значение параметра должно быть от \"0\" до \"1\" и иметь не более 2-х знаков после запятой";
    private static final String ONLY_0_1_AVAILABLE_ERROR = "Допустимые значения параметра \"0\" и \"1\"";
    private static final String ASYNC_PARAM_NOT_NUMBER_ERROR = "%s: Значение параметра \"%s\" (%d) должно быть числовым (больше нуля)!";
    private static final String ASYNC_PARAM_TOO_MUCH_VALUE_ERROR = "%s: Значение параметра \"%s\" (%d) должно быть числовым меньше или равно 1 500 000!";
    private static final String ASYNC_PARAM_INTERVAL_ERROR = "%s: Значение параметра \"Ограничение на выполнение задания\" (%d) " +
            "должно быть больше значения параметра \"Ограничение на выполнение задания в очереди быстрых заданий\" (%d)";
    private static final String UPLOAD_REFBOOK_ASYNC_TASK = "UploadRefBookAsyncTask";
    private static final String ASYNC_PARAM_TASK_LIMIT = "Ограничение на выполнение задания";
    private static final String ASYNC_PARAM_SHORT_QUEUE_LIMIT = "Ограничение на выполнение задания в очереди быстрых заданий";
    private static final String SEARCH_WITHOUT_ERROR_MESSAGE = "Проверка выполнена, ошибок не найдено";
    private static final String EDIT_ASYNC_PARAM_MESSAGE = "%s. Изменён параметр \"%s\" для задания \"%s\": %s.";
    private static final String CONSOLIDATION_DATA_SELECTION_DEPTH_ERROR_MESSAGE = "Для параметра \"Горизонт отбора данных для консолидации, годы\" должно быть указано целое количество лет от 1 до 99";
    private static final String REPORT_PERIOD_YEAR_ERROR = "Для параметра \"%s\" должно быть указано значение от 0001 до 9999";
    private static final String NEGATIVE_INTEGER_ERROR = "Для параметра \"%s\" должно быть указано целое число ≥ 0"; // >= 0
    private static final String NOT_POSITIVE_INTEGER_ERROR = "Значение параметра \"%s\" должно быть целым числом, большим нуля"; // > 0
    private static final String REPORT_PERIOD_YEAR_MIN_HIGHER_MAX_ERROR = "Минимальное значение отчетного года\" должно быть не больше \"Максимального значения отчетного года";
    private static final String REPORT_PERIOD_YEAR_MAX_LOWER_MIN_ERROR = "Максимальное значение отчетного года\" должно быть не меньше \"Минимального значения отчетного года";


    @Autowired
    private ConfigurationDao configurationDao;
    @Autowired
    private RefBookFactory refBookFactory;
    @Autowired
    private AuditService auditService;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private AsyncTaskTypeDao asyncTaskTypeDao;

    //Значение конфигурационных параметров по умолчанию
    private List<Configuration> defaultCommonParams() {
        List<Configuration> defaultCommonConfig = new ArrayList<>();
        defaultCommonConfig.add(new Configuration(ConfigurationParam.SBERBANK_INN.getCaption(), COMMON_PARAM_DEPARTMENT_ID, SBERBANK_INN_DEFAULT));
        defaultCommonConfig.add(new Configuration(ConfigurationParam.NO_CODE.getCaption(), COMMON_PARAM_DEPARTMENT_ID, NO_CODE_DEFAULT));
        defaultCommonConfig.add(new Configuration(ConfigurationParam.SHOW_TIMING.getCaption(), COMMON_PARAM_DEPARTMENT_ID, SHOW_TIMING_DEFAULT));
        defaultCommonConfig.add(new Configuration(ConfigurationParam.LIMIT_IDENT.getCaption(), COMMON_PARAM_DEPARTMENT_ID, LIMIT_IDENT_DEFAULT));
        defaultCommonConfig.add(new Configuration(ConfigurationParam.ENABLE_IMPORT_PERSON.getCaption(), COMMON_PARAM_DEPARTMENT_ID, ENABLE_IMPORT_PERSON_DEFAULT));
        defaultCommonConfig.add(new Configuration(ConfigurationParam.CONSOLIDATION_DATA_SELECTION_DEPTH.getCaption(), COMMON_PARAM_DEPARTMENT_ID, CONSOLIDATION_DATA_SELECTION_DEPTH_DEFAULT));
        defaultCommonConfig.add(new Configuration(ConfigurationParam.REPORT_PERIOD_YEAR_MIN.getCaption(), COMMON_PARAM_DEPARTMENT_ID, REPORT_PERIOD_YEAR_MIN));
        defaultCommonConfig.add(new Configuration(ConfigurationParam.REPORT_PERIOD_YEAR_MAX.getCaption(), COMMON_PARAM_DEPARTMENT_ID, REPORT_PERIOD_YEAR_MAX));
        defaultCommonConfig.add(new Configuration(ConfigurationParam.DECLARATION_ROWS_BULK_EDIT_MAX_COUNT.getCaption(), COMMON_PARAM_DEPARTMENT_ID, DECLARATION_ROWS_BULK_EDIT_MAX_COUNT_DEFAULT));
        defaultCommonConfig.add(new Configuration(ConfigurationParam.ASYNC_SERIAL_MODE.getCaption(), COMMON_PARAM_DEPARTMENT_ID, ASYNC_MODE_DEFAULT));
        // Веса идентификации
        defaultCommonConfig.add(new Configuration(ConfigurationParam.WEIGHT_LAST_NAME.getCaption(), COMMON_PARAM_DEPARTMENT_ID, "5"));
        defaultCommonConfig.add(new Configuration(ConfigurationParam.WEIGHT_FIRST_NAME.getCaption(), COMMON_PARAM_DEPARTMENT_ID, "10"));
        defaultCommonConfig.add(new Configuration(ConfigurationParam.WEIGHT_MIDDLE_NAME.getCaption(), COMMON_PARAM_DEPARTMENT_ID, "5"));
        defaultCommonConfig.add(new Configuration(ConfigurationParam.WEIGHT_BIRTHDAY.getCaption(), COMMON_PARAM_DEPARTMENT_ID, "10"));
        defaultCommonConfig.add(new Configuration(ConfigurationParam.WEIGHT_CITIZENSHIP.getCaption(), COMMON_PARAM_DEPARTMENT_ID, "1"));
        defaultCommonConfig.add(new Configuration(ConfigurationParam.WEIGHT_INP.getCaption(), COMMON_PARAM_DEPARTMENT_ID, "15"));
        defaultCommonConfig.add(new Configuration(ConfigurationParam.WEIGHT_INN.getCaption(), COMMON_PARAM_DEPARTMENT_ID, "10"));
        defaultCommonConfig.add(new Configuration(ConfigurationParam.WEIGHT_INN_FOREIGN.getCaption(), COMMON_PARAM_DEPARTMENT_ID, "10"));
        defaultCommonConfig.add(new Configuration(ConfigurationParam.WEIGHT_SNILS.getCaption(), COMMON_PARAM_DEPARTMENT_ID, "15"));
        defaultCommonConfig.add(new Configuration(ConfigurationParam.WEIGHT_TAX_PAYER_STATUS.getCaption(), COMMON_PARAM_DEPARTMENT_ID, "1"));
        defaultCommonConfig.add(new Configuration(ConfigurationParam.WEIGHT_DUL.getCaption(), COMMON_PARAM_DEPARTMENT_ID, "10"));
        defaultCommonConfig.add(new Configuration(ConfigurationParam.WEIGHT_ADDRESS.getCaption(), COMMON_PARAM_DEPARTMENT_ID, "1"));
        defaultCommonConfig.add(new Configuration(ConfigurationParam.WEIGHT_ADDRESS_INO.getCaption(), COMMON_PARAM_DEPARTMENT_ID, "1"));

        return defaultCommonConfig;
    }

    @Override
    public ConfigurationParamModel getCommonConfig(TAUserInfo userInfo) {
        return getCommonConfigUnsafe();
    }

    @Override
    public ConfigurationParamModel getCommonConfigUnsafe() {
        return configurationDao.fetchAllAsModelByGroup(ConfigurationParamGroup.COMMON_PARAM);
    }

    @Override
    public List<Configuration> getEmailConfig() {
        return null; // TODO используется в методе из EmailServiceImpl, который нигде не используется
    }

    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).VIEW_ADMINISTRATION_CONFIG) || " +
            "hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).EDIT_GENERAL_PARAMS)")
    public ConfigurationParamModel fetchAllByDepartment(Integer departmentId, TAUserInfo userInfo) {
        return configurationDao.fetchAllByDepartment(departmentId);
    }

    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).EDIT_ADMINISTRATION_CONFIG)")
    public String checkConfigParam(Configuration param, TAUserInfo userInfo) {
        Logger logger = checkConfigurationParam(param);
        if (logger.containsLevel(LogLevel.ERROR) || logger.containsLevel(LogLevel.WARNING)) {
            return logEntryService.save(logger.getEntries());
        } else {
            logger.clear();
            logger.info(SEARCH_WITHOUT_ERROR_MESSAGE);
            return logEntryService.save(logger.getEntries());
        }
    }

    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).VIEW_ADMINISTRATION_CONFIG)")
    public PagingResult<Configuration> fetchEmailParams(PagingParams pagingParams, TAUserInfo userInfo) {
        return configurationDao.fetchEmailParams(pagingParams);
    }

    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).EDIT_ADMINISTRATION_CONFIG)")
    public String checkFileSystemAccess(Configuration param, TAUserInfo userInfo) {
        Logger logger = checkConfigurationParam(param);
        return logEntryService.save(logger.getEntries());
    }

    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).EDIT_GENERAL_PARAMS)")
    public void resetCommonParams(TAUserInfo userInfo) {
        configurationDao.update(defaultCommonParams());
    }

    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).VIEW_ADMINISTRATION_CONFIG) ")
    public PagingResult<AsyncTaskTypeData> fetchAsyncParams(PagingParams pagingParams, TAUserInfo userInfo) {
        return asyncTaskTypeDao.findAll(pagingParams);
    }

    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).VIEW_ADMINISTRATION_CONFIG) || " +
            "hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).VIEW_TAXES_GENERAL)")
    public PagingResult<Configuration> fetchCommonParams(PagingParams pagingParams, ConfigurationParamGroup configurationParamGroup, TAUserInfo userInfo) {
        return configurationDao.fetchAllByGroupAndPaging(configurationParamGroup, pagingParams);
    }

    @Override
    public Configuration fetchByEnum(ConfigurationParam param) {
        return configurationDao.fetchByEnum(param);
    }

    @Override
    public Integer getParamIntValue(ConfigurationParam param) {
        Configuration configuration = configurationDao.fetchByEnum(param);
        if (configuration == null) return null;
        try {
            return NumberUtils.createInteger(configuration.getValue());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).VIEW_ADMINISTRATION_CONFIG) || " +
            "hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).VIEW_TAXES_GENERAL)")
    public Map<String, Configuration> fetchAllByEnums(List<ConfigurationParam> params, TAUserInfo userInfo) {
        return fetchAllByEnums(params);
    }

    @Override
    public Map<String, Configuration> fetchAllByEnums(List<ConfigurationParam> params) {
        List<Configuration> configurations = configurationDao.fetchAllByEnums(params);
        return Maps.uniqueIndex(configurations, new Function<Configuration, String>() {
            @Override
            public String apply(Configuration configuration) {
                return configuration.getCode();
            }
        });
    }

    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).EDIT_ADMINISTRATION_CONFIG)")
    public String create(Configuration commonParam, TAUserInfo userInfo) {
        Logger logger = new Logger();
        if (commonParam != null) {
            ConfigurationParam param = ConfigurationParam.valueOf(commonParam.getCode());
            configurationDao.createCommonParam(commonParam);
            String message = ConfigurationParamGroup.COMMON.getCaption() + ". Добавлен параметр \"" + param.getCaption() + "\": " + commonParam.getValue();
            auditService.add(FormDataEvent.EDIT_CONFIG_PARAMS, userInfo, userInfo.getUser().getDepartmentId(), null, null, null,
                    null, null, message, null);
            logger.info(message);
        }
        return logEntryService.save(logger.getEntries());
    }

    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).VIEW_ADMINISTRATION_CONFIG)")
    public PagingResult<Configuration> fetchNonCreatedCommonParams(PagingParams pagingParams, TAUserInfo userInfo) {
        List<Configuration> result = new ArrayList<>();
        for (ConfigurationParam param : ConfigurationParam.getParamsByGroup(ConfigurationParamGroup.COMMON)) {
            result.add(new Configuration(param.name(), param.getCaption()));
            for (Configuration commonParam : configurationDao.fetchAllByGroupAndPaging(ConfigurationParamGroup.COMMON, pagingParams)) {
                if (param.name().equals(commonParam.getCode())) {
                    result.remove(result.size() - 1);
                    break;
                }
            }
        }
        return new PagingResult<>(result, result.size());
    }

    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).EDIT_ADMINISTRATION_CONFIG)")
    public String remove(List<String> codes, TAUserInfo userInfo) {
        Logger logger = new Logger();
        List<ConfigurationParam> params = new ArrayList<>();
        for (String code : codes) {
            ConfigurationParam param = ConfigurationParam.valueOf(code);
            params.add(param);
            String message = ConfigurationParamGroup.COMMON.getCaption() + ". Удален параметр \"" + param.getCaption();
            auditService.add(FormDataEvent.EDIT_CONFIG_PARAMS, userInfo,
                    userInfo.getUser().getDepartmentId(), null, null, null, null, null, message, null);
            logger.info(message);
        }
        configurationDao.removeCommonParam(params);
        return logEntryService.save(logger.getEntries());
    }

    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).EDIT_ADMINISTRATION_CONFIG) || " +
            "hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).EDIT_GENERAL_PARAMS)")
    public String updateCommonParam(Configuration commonParam, TAUserInfo userInfo) {
        Logger logger = new Logger();
        checkCommonParam(commonParam, logger);
        if (!logger.containsLevel(LogLevel.ERROR)) {
            configurationDao.update(commonParam);
            String message = ConfigurationParamGroup.COMMON.getCaption() + ". Изменён параметр \"" + commonParam.getDescription() + "\": " + commonParam.getValue();
            auditService.add(FormDataEvent.EDIT_CONFIG_PARAMS, userInfo,
                    userInfo.getUser().getDepartmentId(), null, null, null, null, null, message, null);
            logger.info(message);
        }
        return logEntryService.save(logger.getEntries());
    }

    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).EDIT_ADMINISTRATION_CONFIG)")
    public String updateAsyncParam(AsyncTaskTypeData asyncParam, TAUserInfo userInfo) {
        Logger logger = new Logger();
        AsyncTaskTypeData oldAsyncParam = asyncTaskTypeDao.findById(asyncParam.getId());
        checkAsyncParam(asyncParam, logger);
        if (logger.containsLevel(LogLevel.ERROR)) {
            return logEntryService.save(logger.getEntries());
        }
        asyncTaskTypeDao.updateLimits(asyncParam.getId(), asyncParam.getShortQueueLimit(), asyncParam.getTaskLimit());

        String message = String.format(EDIT_ASYNC_PARAM_MESSAGE,
                ConfigurationParamGroup.ASYNC.getCaption(),
                (!Objects.equals(asyncParam.getTaskLimit(), oldAsyncParam.getTaskLimit()) ? TASK_LIMIT_FIELD : (!Objects.equals(asyncParam.getShortQueueLimit(), oldAsyncParam.getShortQueueLimit()) ? SHORT_QUEUE_LIMIT : "")),
                oldAsyncParam.getName(),
                (!Objects.equals(asyncParam.getTaskLimit(), oldAsyncParam.getTaskLimit()) ? (asyncParam.getTaskLimit() != null ? asyncParam.getTaskLimit() : "") :
                        (!Objects.equals(asyncParam.getShortQueueLimit(), oldAsyncParam.getShortQueueLimit()) ? (asyncParam.getShortQueueLimit() != null ? asyncParam.getShortQueueLimit() : "") : "")));

        auditService.add(FormDataEvent.EDIT_CONFIG_PARAMS, userInfo,
                userInfo.getUser().getDepartmentId(), null, null, null, null, null, message, null);
        logger.info(message);

        if (!Objects.equals(asyncParam.getTaskLimit(), oldAsyncParam.getTaskLimit()) && !Objects.equals(asyncParam.getShortQueueLimit(), oldAsyncParam.getShortQueueLimit())) {

            message = String.format(EDIT_ASYNC_PARAM_MESSAGE,
                    ConfigurationParamGroup.ASYNC.getCaption(),
                    (!Objects.equals(asyncParam.getShortQueueLimit(), oldAsyncParam.getShortQueueLimit()) ? SHORT_QUEUE_LIMIT : ""),
                    oldAsyncParam.getName(),
                    (!Objects.equals(asyncParam.getShortQueueLimit(), oldAsyncParam.getShortQueueLimit()) ? (asyncParam.getShortQueueLimit() != null ? asyncParam.getShortQueueLimit() : "") : ""));

            auditService.add(FormDataEvent.EDIT_CONFIG_PARAMS, userInfo,
                    userInfo.getUser().getDepartmentId(), null, null, null, null, null, message, null);
            logger.info(message);
        }

        return logEntryService.save(logger.getEntries());
    }

    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).EDIT_ADMINISTRATION_CONFIG)")
    public String updateEmailParam(Configuration emailParam, TAUserInfo userInfo) {
        Logger logger = new Logger();
        configurationDao.updateEmailParam(emailParam);
        String message = ConfigurationParamGroup.EMAIL.getCaption() + ". Изменён параметр \"" + emailParam.getCode() + "\": " + emailParam.getValue();
        auditService.add(FormDataEvent.EDIT_CONFIG_PARAMS, userInfo,
                userInfo.getUser().getDepartmentId(), null, null, null, null, null, message, null);
        logger.info(message);

        return logEntryService.save(logger.getEntries());
    }

    @Override
    public Map<String, String> fetchAuthEmailParamsMap() {
        List<Configuration> configurations = configurationDao.fetchAuthEmailParams();
        Map<String, String> result = new HashMap<>();
        for (Configuration configuration : configurations) {
            result.put(configuration.getCode(), configuration.getValue());
        }
        return result;
    }


    /**
     * Проверка параметров асинхронных задач на валидность. Если одна из проверок не пройдена, ошибка записывается в logger
     *
     * @param asyncParam параметр
     * @param logger     логгер
     */
    private void checkAsyncParam(AsyncTaskTypeData asyncParam, Logger logger) {
        if (asyncParam.getTaskLimit() != null && asyncParam.getTaskLimit() < 1) {
            logger.error(ASYNC_PARAM_NOT_NUMBER_ERROR, asyncParam.getName(), ASYNC_PARAM_TASK_LIMIT, asyncParam.getTaskLimit());
            return;
        }
        if (asyncParam.getShortQueueLimit() != null && asyncParam.getShortQueueLimit() < 1) {
            logger.error(ASYNC_PARAM_NOT_NUMBER_ERROR, asyncParam.getName(), ASYNC_PARAM_SHORT_QUEUE_LIMIT, asyncParam.getShortQueueLimit());
            return;
        }
        if (asyncParam.getTaskLimit() != null && asyncParam.getShortQueueLimit() != null && asyncParam.getHandlerBean().equals(UPLOAD_REFBOOK_ASYNC_TASK) &&
                (asyncParam.getTaskLimit() > UPLOAD_REFBOOK_ASYNC_TASK_LIMIT || asyncParam.getShortQueueLimit() > UPLOAD_REFBOOK_ASYNC_TASK_LIMIT)) {
            logger.error(ASYNC_PARAM_TOO_MUCH_VALUE_ERROR, asyncParam.getName(),
                    asyncParam.getTaskLimit() > UPLOAD_REFBOOK_ASYNC_TASK_LIMIT ? ASYNC_PARAM_TASK_LIMIT : ASYNC_PARAM_SHORT_QUEUE_LIMIT,
                    asyncParam.getTaskLimit() > UPLOAD_REFBOOK_ASYNC_TASK_LIMIT ? asyncParam.getTaskLimit() : asyncParam.getShortQueueLimit());
            return;
        }
        if (asyncParam.getTaskLimit() != null && asyncParam.getShortQueueLimit() != null && asyncParam.getShortQueueLimit() >= asyncParam.getTaskLimit()) {
            logger.error(ASYNC_PARAM_INTERVAL_ERROR, asyncParam.getName(), asyncParam.getTaskLimit(), asyncParam.getShortQueueLimit());
        }
        if (AsyncTaskType.EXCEL_PERSONS.getId() == asyncParam.getId()) {
            if (asyncParam.getTaskLimit() != null && asyncParam.getTaskLimit() > 1_000_000) {
                logger.error("Количество выгружаемых в файл Excel строк не может быть более 1 000 000");
            }
        }
    }

    /**
     * Проверка параметра на пренадлежность к {@link ConfigurationParamGroup#COMMON_PARAM}
     *
     * @param config проверяемый параметр
     * @param logger логгер
     */
    private void checkCommonParam(Configuration config, Logger logger) {
        if (config.getCode().equals(ConfigurationParam.SBERBANK_INN.name())) {
            checkSberbankInn(config.getValue(), logger);
        } else if (config.getCode().equals(ConfigurationParam.NO_CODE.name())) {
            checkNoCode(config.getValue(), logger);
        } else if (config.getCode().equals(ConfigurationParam.LIMIT_IDENT.name())) {
            checkLimitIdent(config.getValue(), logger);
        } else if (config.getCode().equals(ConfigurationParam.SHOW_TIMING.name()) ||
                config.getCode().equals(ConfigurationParam.ENABLE_IMPORT_PERSON.name())) {
            checkDiscreteValue(config.getValue(), logger);
        } else if (config.getCode().equals(ConfigurationParam.CONSOLIDATION_DATA_SELECTION_DEPTH.name())) {
            checkConsolidationDataSelectionDepth(config.getValue(), logger);
        } else if (config.getCode().equals(ConfigurationParam.REPORT_PERIOD_YEAR_MIN.name()) ||
                config.getCode().equals(ConfigurationParam.REPORT_PERIOD_YEAR_MAX.name())) {
            checkReportPeriodYear(config, logger);
        } else if (isIdentificationWeightParam(config)) {
            checkIdentificationWeightParam(config, logger);
        } else if (config.getCode().equals(ConfigurationParam.DECLARATION_ROWS_BULK_EDIT_MAX_COUNT.name())) {
            checkPositiveIntParam(config, logger);
        } else if (config.getCode().equals(ConfigurationParam.ASYNC_SERIAL_MODE.name())) {
            checkDiscreteValue(config.getValue(), logger);
        }
    }

    private boolean isIdentificationWeightParam(Configuration config) {
        List<ConfigurationParam> params = Arrays.asList(
                ConfigurationParam.WEIGHT_LAST_NAME, ConfigurationParam.WEIGHT_FIRST_NAME, ConfigurationParam.WEIGHT_MIDDLE_NAME,
                ConfigurationParam.WEIGHT_BIRTHDAY, ConfigurationParam.WEIGHT_CITIZENSHIP, ConfigurationParam.WEIGHT_INP,
                ConfigurationParam.WEIGHT_INN, ConfigurationParam.WEIGHT_INN_FOREIGN, ConfigurationParam.WEIGHT_SNILS,
                ConfigurationParam.WEIGHT_TAX_PAYER_STATUS, ConfigurationParam.WEIGHT_DUL, ConfigurationParam.WEIGHT_ADDRESS,
                ConfigurationParam.WEIGHT_ADDRESS_INO
        );
        for (ConfigurationParam param : params) {
            if (config.getCode().equalsIgnoreCase(param.name())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Проверка параметра веса идентификации
     *
     * @param configuration значение параметра
     * @param logger        логгер
     */
    private void checkIdentificationWeightParam(Configuration configuration, Logger logger) {
        String value = configuration.getValue();
        boolean valid = isNotNegativeInt(value);
        if (!valid) {
            logger.error(NEGATIVE_INTEGER_ERROR, configuration.getDescription());
        }
    }

    private boolean isNotNegativeInt(String value) {
        try {
            return (Integer.valueOf(value) >= 0);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Проверка параметра - целого положительного числа.
     */
    private void checkPositiveIntParam(Configuration configuration, Logger logger) {
        String value = configuration.getValue();
        boolean valid = isPositiveInt(value);
        if (!valid) {
            logger.error(NOT_POSITIVE_INTEGER_ERROR, configuration.getDescription());
        }
    }

    private boolean isPositiveInt(String value) {
        try {
            return (Integer.valueOf(value) > 0);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Проверка значения параметра на 0 или 1
     *
     * @param value  значение параметра
     * @param logger логгер
     */
    private void checkDiscreteValue(String value, Logger logger) {
        if (!"0".equals(value) && !"1".equals(value)) {
            logger.error(ONLY_0_1_AVAILABLE_ERROR);
        }
    }

    /**
     * Проверка значения параметра {@link ConfigurationParam#LIMIT_IDENT}
     *
     * @param value  значение параметра
     * @param logger логгер
     */
    private void checkLimitIdent(String value, Logger logger) {
        try {
            BigDecimal decimal = new BigDecimal(value);
            if ((decimal.precision() > 2) || (decimal.doubleValue() < 0) || (decimal.doubleValue() > 1)) {
                logger.error(LIMIT_IDENT_ERROR);
            }
        } catch (NumberFormatException e) {
            logger.error(LIMIT_IDENT_ERROR);
        }
    }

    private void checkConsolidationDataSelectionDepth(String value, Logger logger) {
        try {
            Integer intValue = Integer.valueOf(value);
            if (intValue < 1 || intValue > 99) {
                logger.error(CONSOLIDATION_DATA_SELECTION_DEPTH_ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            logger.error(CONSOLIDATION_DATA_SELECTION_DEPTH_ERROR_MESSAGE);
        }
    }

    /**
     * Проверка значения параметра {@link ConfigurationParam#NO_CODE}
     *
     * @param noCode код параметра
     * @param logger логгер
     */
    private void checkNoCode(String noCode, Logger logger) {
        RefBookDataProvider taxInspectionDataProvider = refBookFactory.getDataProvider(RefBook.Id.TAX_INSPECTION.getId());
        if (taxInspectionDataProvider.getRecordsCount(new Date(), "code = '" + noCode + "'") == 0) {
            logger.error(NO_CODE_ERROR, noCode);
        }
    }

    /**
     * Проверка значения параметра {@link ConfigurationParam#SBERBANK_INN}
     *
     * @param inn    ИНН
     * @param logger логгер
     */
    private void checkSberbankInn(String inn, Logger logger) {
        if (inn.length() != INN_JUR_LENGTH || !RefBookUtils.checkControlSumInn(inn)) {
            logger.error(INN_JUR_ERROR, inn);
        }
    }

    private void checkReportPeriodYear(Configuration configuration, Logger logger) {
        try {
            Integer intValue = Integer.valueOf(configuration.getValue());
            if (intValue < 0 || intValue > 9999) {
                logger.error(REPORT_PERIOD_YEAR_ERROR, configuration.getDescription());
            }
            if (configuration.getCode().equals(ConfigurationParam.REPORT_PERIOD_YEAR_MIN.name())) {
                Integer maxValue = Integer.valueOf(configurationDao.fetchByEnum(ConfigurationParam.REPORT_PERIOD_YEAR_MAX).getValue());
                if (intValue > maxValue) {
                    logger.error(REPORT_PERIOD_YEAR_MIN_HIGHER_MAX_ERROR);
                }
            } else {
                Integer minValue = Integer.valueOf(configurationDao.fetchByEnum(ConfigurationParam.REPORT_PERIOD_YEAR_MIN).getValue());
                if (intValue < minValue) {
                    logger.error(REPORT_PERIOD_YEAR_MAX_LOWER_MIN_ERROR);
                }
            }
        } catch (NumberFormatException e) {
            logger.error(REPORT_PERIOD_YEAR_ERROR, configuration.getDescription());
        }
    }

    /**
     * Проверяет конфигурационный параметр на наличие ошибок в значении или доступе к папке
     *
     * @param param конфигурационный параметр
     * @return объект {@link Logger}
     */
    private Logger checkConfigurationParam(Configuration param) {
        Logger logger = new Logger();
        if (param == null) {
            return logger;
        }

        ConfigurationParam configParam = ConfigurationParam.valueOf(param.getCode());
        Boolean isFolder = configParam.isFolder();
        // у папок smb в конце должен быть слеш (иначе возникенет ошибка при configurationParam.isFolder() == true и configurationParam.hasReadCheck() == true)
        if (isFolder == null) {
            FileWrapper keyResourceFolder = ResourceUtils.getSharedResource(param.getValue(), false);
            if (keyResourceFolder.isFile()) {
                isFolder = false;
            } else if (keyResourceFolder.isDirectory()) {
                isFolder = true;
                param.setValue(param.getValue() + "/");
            } else {
                isFolder = false;
            }
        } else if (isFolder) {
            param.setValue(param.getValue() + "/");
        }
        // Проверка значения параметра "Проверять ЭП"
        if (configParam.equals(ConfigurationParam.SIGN_CHECK)) {
            checkDiscreteValue(param.getValue(), logger);
        }
        if (configParam.hasReadCheck() && (isFolder
                && !FileWrapper.canReadFolder(param.getValue()) || !isFolder
                && !FileWrapper.canReadFile(param.getValue()))) {
            // Доступ на чтение
            logger.error(READ_ERROR, param.getValue());
        } else if (configParam.hasWriteCheck() && (isFolder
                && !FileWrapper.canWriteFolder(param.getValue()) || !isFolder
                && !FileWrapper.canWriteFile(param.getValue()))) {
            // Доступ на запись
            logger.error(WRITE_ERROR, param.getValue());
        } else {
            if (configParam.hasReadCheck()) {
                logger.info(READ_INFO, param.getValue());
            } else if (configParam.hasWriteCheck()) {
                logger.info(WRITE_INFO, param.getValue());
            }
        }
        return logger;
    }
}
