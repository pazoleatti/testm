package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.AsyncTask;
import com.aplana.sbrf.taxaccounting.service.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.impl.fixed.RefBookConfigurationParam;
import com.aplana.sbrf.taxaccounting.service.RefBookScriptingService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.service.impl.TAAbstractScriptingServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.aplana.sbrf.taxaccounting.model.refbook.RefBook.Id.*;
import static com.aplana.sbrf.taxaccounting.model.refbook.RefBook.RECORD_PARENT_ID_ALIAS;

/**
 * Реализация фабрики провайдеров данных для справочников
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 11.07.13 11:22
 */
@Service("refBookFactory")
@Transactional
public class RefBookFactoryImpl implements RefBookFactory {

    @Autowired
    private AsyncManager asyncManager;

    @Autowired
    private TAUserService userService;

    private static final Log LOG = LogFactory.getLog(RefBookFactoryImpl.class);

    private static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

    // Список простых редактируемых версионируемых справочников
    private static final List<Long> simpleEditableRefBooks = Arrays.asList(new Long[]{
            TAX_PLACE_TYPE_CODE.getId(), COUNTRY.getId(), DETACH_TAX_PAY.getId(), MAKE_CALC.getId(), MARK_SIGNATORY_CODE.getId(),
            DOCUMENT_CODES.getId(), PERSON_ADDRESS.getId(), ID_DOC.getId(), TAXPAYER_STATUS.getId(), PERSON.getId(),
            ID_TAX_PAYER.getId(), DEDUCTION_TYPE.getId(), INCOME_CODE.getId(), REGION.getId(), PRESENT_PLACE.getId(),
            OKVED.getId(), REORGANIZATION.getId(), FILL_BASE.getId(), TARIFF_PAYER.getId(), HARD_WORK.getId(),
            KBK.getId(), PERSON_CATEGORY.getId(), NDFL.getId(), NDFL_DETAIL.getId(), NDFL_REFERENCES.getId(),

            // справочник ОКТМО отдельным списком идет, так как является версионируемым, но только для чтения
            // аналогично Справочник: "Признак кода вычета", реализован как нередактируемый
            OKTMO.getId(), DEDUCTION_MARK.getId()
    });

    @Autowired
    private RefBookDao refBookDao;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private RefBookScriptingService refBookScriptingService;
    @Autowired
    private LockDataService lockDataService;

    @Override
    public RefBook get(Long refBookId) {
        return refBookDao.get(refBookId);
    }

    @Override
    public List<RefBook> getAll(boolean onlyVisible) {
        //TODO: избавиться от лишнего аргумента null (Marat Fayzullin 10.02.2014)
        return onlyVisible ? refBookDao.getAllVisible(null) : refBookDao.getAll(null);
    }

    @Override
    public RefBook getByAttribute(Long attributeId) {
        return refBookDao.getByAttribute(attributeId);
    }

    @Override
    public RefBookDataProvider getDataProvider(Long refBookId) {
        RefBook refBook = get(refBookId);

        if (simpleEditableRefBooks.contains(refBookId)) {
            RefBookSimpleDataProvider dataProvider = (RefBookSimpleDataProvider) applicationContext.getBean("refBookSimpleDataProvider", RefBookDataProvider.class);
            dataProvider.setRefBook(refBook);
            return dataProvider;
        }

        if (DEPARTMENT.getId() == refBookId) {
            return applicationContext.getBean("refBookDepartment", RefBookDataProvider.class);
        }
        if (CONFIGURATION_PARAM.getId() == refBookId) {
            RefBookConfigurationParam dataProvider = applicationContext.getBean("refBookConfigurationParam", RefBookConfigurationParam.class);
            dataProvider.setRefBook(refBook);
            return dataProvider;
        }
        if (EMAIL_CONFIG.getId() == refBookId) {
            return applicationContext.getBean("refBookRefBookEmailConfig", RefBookEmailConfigProvider.class);
        }
        if (ASYNC_CONFIG.getId() == refBookId) {
            return applicationContext.getBean("refBookAsyncConfigProvider", RefBookAsyncConfigProvider.class);
        }
        if (refBook.getTableName() != null && !refBook.getTableName().isEmpty()) {
            RefBookSimpleReadOnly dataProvider = (RefBookSimpleReadOnly) applicationContext.getBean("refBookSimpleReadOnly", RefBookDataProvider.class);
            if (!refBook.getId().equals(RefBook.Id.CALENDAR.getId())) {
                dataProvider.setWhereClause("ID <> -1");
            }
            dataProvider.setRefBook(refBook);
            return dataProvider;
        } else {
            RefBookUniversal refBookUniversal = (RefBookUniversal) applicationContext.getBean("refBookUniversal", RefBookDataProvider.class);
            refBookUniversal.setRefBookId(refBookId);
            return refBookUniversal;
        }
    }

    @Override
    public String getSearchQueryStatement(String query, Long refBookId) {
        return getSearchQueryStatement(query, refBookId, false);
    }

    @Override
    public String getSearchQueryStatement(String query, Long refBookId, boolean exactSearch) {
        if (query == null || query.isEmpty()) {
            return null;
        }

        String q = StringUtils.cleanString(query);
        q = q.toLowerCase().replaceAll("\'", "\\\\\'");
        StringBuilder resultSearch = new StringBuilder();
        RefBook refBook = get(refBookId);
        for (RefBookAttribute attribute : refBook.getAttributes()) {
            if (attribute.getAlias().equals(RECORD_PARENT_ID_ALIAS) || attribute.getAlias().equals("IS_ACTIVE")) {
                continue;
            }

            if (resultSearch.length() > 0) {
                resultSearch.append(" or ");
            }

            switch (attribute.getAttributeType()) {
                case STRING:
                    resultSearch
                            .append("LOWER(")
                            .append(attribute.getAlias())
                            .append(")");
                    break;
                case NUMBER:
                    resultSearch
                            .append("TO_CHAR(")
                            .append(attribute.getAlias())
                            .append(")");
                    break;
                case DATE:
                    resultSearch
                            .append("TRUNC(")
                            .append(attribute.getAlias())
                            .append(") = TO_DATE('")
                            .append(q)
                            .append("')");
                    break;
                case REFERENCE:
                    if (isSimpleRefBool(refBookId)) {
                        String fullAlias = getStackAlias(attribute);
                        switch (getLastAttribute(attribute).getAttributeType()) {
                            case STRING:
                                resultSearch
                                        .append("LOWER(")
                                        .append(fullAlias)
                                        .append(")");
                                break;
                            case NUMBER:
                                resultSearch
                                        .append("TO_CHAR(")
                                        .append(fullAlias)
                                        .append(")");
                                break;
                            case DATE:
                                resultSearch.append(fullAlias);
                                break;
                            default:
                                throw new RuntimeException("Unknown RefBookAttributeType");
                        }
                    } else {
                        resultSearch
                                .append("TO_CHAR(")
                                .append(attribute.getAlias())
                                .append(")");
                    }
                    break;
                default:
                    throw new RuntimeException("Unknown RefBookAttributeType");

            }

            if (attribute.getAttributeType() != RefBookAttributeType.DATE) {
                if (exactSearch) {
                    resultSearch
                            .append(" like ")
                            .append("'")
                            .append(q)
                            .append("'");
                } else {
                    resultSearch
                            .append(" like ")
                            .append("'%")
                            .append(q)
                            .append("%'");
                }
            }
        }

        return resultSearch.toString();
    }

    @Override
    public String getSearchQueryStatementWithAdditionalStringParameters(Map<String, String> parameters, String searchPattern, Long refBookId, boolean exactSearch) {
        if (searchPattern != null && !searchPattern.isEmpty()) {
            String q = StringUtils.cleanString(searchPattern);
            q = q.toLowerCase().replaceAll("\'", "\\\\\'");
            StringBuilder resultSearch = new StringBuilder();
            RefBook refBook = get(refBookId);
            for (RefBookAttribute attribute : refBook.getAttributes()) {
                if (attribute.getAlias().equals(RECORD_PARENT_ID_ALIAS) || attribute.getAlias().equals("IS_ACTIVE") || parameters.containsKey(attribute.getAlias())) {
                    continue;
                }

                if (resultSearch.length() > 0) {
                    resultSearch.append(" or ");
                }

                switch (attribute.getAttributeType()) {
                    case STRING:
                        resultSearch
                                .append("LOWER(")
                                .append(attribute.getAlias())
                                .append(")");
                        break;
                    case NUMBER:
                        resultSearch
                                .append("TO_CHAR(")
                                .append(attribute.getAlias())
                                .append(")");
                        break;
                    case DATE:
                        resultSearch
                                .append("TRUNC(")
                                .append(attribute.getAlias())
                                .append(") = TO_DATE('")
                                .append(q)
                                .append("')");
                        break;
                    case REFERENCE:
                        if (isSimpleRefBool(refBookId)) {
                            String fullAlias = getStackAlias(attribute);
                            switch (getLastAttribute(attribute).getAttributeType()) {
                                case STRING:
                                    resultSearch
                                            .append("LOWER(")
                                            .append(fullAlias)
                                            .append(")");
                                    break;
                                case NUMBER:
                                    resultSearch
                                            .append("TO_CHAR(")
                                            .append(fullAlias)
                                            .append(")");
                                    break;
                                case DATE:
                                    resultSearch.append(fullAlias);
                                    break;
                                default:
                                    throw new RuntimeException("Unknown RefBookAttributeType");
                            }
                        } else {
                            resultSearch
                                    .append("TO_CHAR(")
                                    .append(attribute.getAlias())
                                    .append(")");
                        }
                        break;
                    default:
                        throw new RuntimeException("Unknown RefBookAttributeType");

                }

                if (attribute.getAttributeType() != RefBookAttributeType.DATE) {
                    if (exactSearch) {
                        resultSearch
                                .append(" like ")
                                .append("'")
                                .append(q)
                                .append("'");
                    } else {
                        resultSearch
                                .append(" like ")
                                .append("'%")
                                .append(q)
                                .append("%'");
                    }
                }
                if (!parameters.isEmpty()) {
                    resultSearch.append(" and ")
                            .append(buildQueryFromParams(parameters, exactSearch));
                }
            }
            return resultSearch.toString();
        } else {
            if (parameters.isEmpty()) {
                return null;
            } else {
                return buildQueryFromParams(parameters, exactSearch).toString();
            }
        }
    }

    private StringBuilder buildQueryFromParams(Map<String, String> parameters, boolean exactSearch) {
        StringBuilder queryBuilder = new StringBuilder();

        for (Map.Entry<String, String> param : parameters.entrySet()) {
            if (queryBuilder.length() > 0) {
                queryBuilder.append(" and ");
            }
            String q = StringUtils.cleanString(param.getValue());
            q = q.toLowerCase().replaceAll("\'", "\\\\\'");
            queryBuilder.append("LOWER(")
                    .append(param.getKey())
                    .append(")");
            if (exactSearch) {
                queryBuilder
                        .append(" like ")
                        .append("'")
                        .append(q)
                        .append("'");
            } else {
                queryBuilder
                        .append(" like ")
                        .append("'%")
                        .append(q)
                        .append("%'");
            }
        }
        return queryBuilder;
    }

    /**
     * Метод возврадет полный алиас для ссылочного атрибута вида
     * user.city.name
     *
     * @param attribute
     * @return
     */
    private String getStackAlias(RefBookAttribute attribute) {
        switch (attribute.getAttributeType()) {
            case STRING:
            case DATE:
            case NUMBER:
                return attribute.getAlias();
            case REFERENCE:
                RefBook rb = get(attribute.getRefBookId());
                RefBookAttribute nextAttribute = rb.getAttribute(attribute.getRefBookAttributeId());
                return attribute.getAlias() + "." + getStackAlias(nextAttribute);
            default:
                throw new RuntimeException("Unknown RefBookAttributeType");
        }
    }

    /**
     * Метод возвращает последний не ссылочный атрибут по цепочке
     * ссылок
     *
     * @param attribute ссылочный атрибут для которого нужно получить последний не ссылочный атрибут
     * @return
     */
    private RefBookAttribute getLastAttribute(RefBookAttribute attribute) {
        if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)) {
            RefBook rb = getByAttribute(attribute.getRefBookAttributeId());
            RefBookAttribute nextAttribute = rb.getAttribute(attribute.getRefBookAttributeId());

            return getLastAttribute(nextAttribute);
        } else {
            return attribute;
        }
    }

    /**
     * Находится ли справочник в стандартной структуре
     *
     * @param refBookId
     * @return
     */
    private boolean isSimpleRefBool(Long refBookId) {
        // TODO Левыкин: нереализованный метод?
        return true;
    }

    @Override
    public String getRefBookDescription(DescriptionTemplate descriptionTemplate, Long refBookId) {
        RefBook refBook = get(refBookId);
        switch (descriptionTemplate) {
            case REF_BOOK_EDIT:
                return String.format(descriptionTemplate.getText(), refBook.getName());
            default:
                throw new ServiceException("Неверный тип шаблона(%s)", descriptionTemplate);
        }
    }

    @Override
    public List<String> getSpecificReportTypes(long refBookId, TAUserInfo userInfo, Logger logger) {
        Map<String, Object> params = new HashMap<String, Object>();
        List<String> specificReportTypes = new ArrayList<String>();
        params.put("specificReportType", specificReportTypes);
        refBookScriptingService.executeScript(userInfo, refBookId, FormDataEvent.GET_SPECIFIC_REPORT_TYPES, logger, params);
        if (logger.containsLevel(LogLevel.ERROR)) {
            logger.warn("Возникли ошибки при получении списка отчетов");
        }
        Iterator<String> iterator = specificReportTypes.iterator();
        while (iterator.hasNext()) {
            String specificReportType = iterator.next();
            if (AsyncTaskType.EXCEL_REF_BOOK.getName().equals(specificReportType) ||
                    AsyncTaskType.CSV_REF_BOOK.getName().equals(specificReportType)) {
                iterator.remove();
                LOG.error(String.format("Нельзя переопределить стандартный отчет: %s.", specificReportType));
            }
        }
        return specificReportTypes;
    }

    @Override
    public String generateTaskKey(long refBookId) {
        return LockData.LockObjects.REF_BOOK.name() + "_" + refBookId;
    }

    @Override
    public String getRefBookLockDescription(LockData lockData, long refBookId) {
        if (lockData.getTaskId() != null) {
            //Заблокировано асинхроннной задачей
            AsyncTaskData lockTaskData = asyncManager.getLightTaskData(lockData.getTaskId());
            return String.format(AsyncTask.LOCK_CURRENT,
                    sdf.get().format(lockData.getDateLock()),
                    userService.getUser(lockData.getUserId()).getName(),
                    lockTaskData.getDescription());
        } else {
            //Заблокировано редактированием
            return String.format(AsyncTask.LOCK_CURRENT,
                    sdf.get().format(lockData.getDateLock()),
                    userService.getUser(lockData.getUserId()).getName(),
                    getRefBookDescription(DescriptionTemplate.REF_BOOK_EDIT, refBookId));
        }
    }

    @Override
    public boolean getEventScriptStatus(long refBookId, FormDataEvent event) {
        String script = refBookScriptingService.getScript(refBookId);
        if (script != null && !script.isEmpty()) {
            return TAAbstractScriptingServiceImpl.canExecuteScript(script, event);
        } else {
            return false;
        }
    }

    @Override
    public Map<FormDataEvent, Boolean> getEventScriptStatus(long refBookId) {
        List<FormDataEvent> formDataEventList = Arrays.asList(FormDataEvent.ADD_ROW, FormDataEvent.IMPORT);
        Map<FormDataEvent, Boolean> eventScriptStatus = new HashMap<FormDataEvent, Boolean>();
        String script = refBookScriptingService.getScript(refBookId);
        if (script != null && !script.isEmpty()) {
            for (FormDataEvent event : formDataEventList) {
                eventScriptStatus.put(event, TAAbstractScriptingServiceImpl.canExecuteScript(script, event));
            }
        } else {
            for (FormDataEvent event : formDataEventList) {
                eventScriptStatus.put(event, false);
            }
        }
        return eventScriptStatus;
    }

}
