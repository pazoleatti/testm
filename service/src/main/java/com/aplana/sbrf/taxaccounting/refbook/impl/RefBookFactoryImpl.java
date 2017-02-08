package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.impl.fixed.RefBookAuditFieldList;
import com.aplana.sbrf.taxaccounting.refbook.impl.fixed.RefBookConfigurationParam;
import com.aplana.sbrf.taxaccounting.service.RefBookScriptingService;
import com.aplana.sbrf.taxaccounting.service.impl.TAAbstractScriptingServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.aplana.sbrf.taxaccounting.model.refbook.RefBook.RECORD_PARENT_ID_ALIAS;
import static com.aplana.sbrf.taxaccounting.model.refbook.RefBook.Id.*;

/**
 * Реализация фабрики провайдеров данных для справочников
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 11.07.13 11:22
 */
@Service("refBookFactory")
@Transactional
public class RefBookFactoryImpl implements RefBookFactory {

    private static final Log LOG = LogFactory.getLog(RefBookFactoryImpl.class);

	private static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("dd.MM.yyyy");
		}
	};

	// Список простых нередактируемых справочников
	private static final List<Long> simpleReadOnlyRefBooks = Arrays.asList(new Long[]{
			USER.getId(), SEC_ROLE.getId(), DEPARTMENT_TYPE.getId(), ASNU.getId(),
			DECLARATION_DATA_KIND_REF_BOOK.getId(), DECLARATION_DATA_TYPE_REF_BOOK.getId(),
            DECLARATION_TEMPLATE.getId(), DOC_STATE.getId(), TAX_INSPECTION.getId(),
			TAXPAYER_STATUS.getId(), NDFL_RATE.getId()
	});
	// Список простых редактируемых версионируемых справочников
	private static final List<Long> simpleEditableRefBooks = Arrays.asList(new Long[]{
			NDFL.getId(), NDFL_DETAIL.getId(),
            FOND.getId(), FOND_DETAIL.getId(),
            FIAS_OPERSTAT.getId(), FIAS_SOCRBASE.getId(),
            FIAS_ADDR_OBJECT.getId(), FIAS_HOUSE.getId(),
			FIAS_HOUSEINT.getId(), FIAS_ROOM.getId(), REORGANIZATION.getId(),
			REGION.getId(), NDFL.getId(), NDFL_DETAIL.getId(),
			PERSON.getId(), ID_DOC.getId(), PERSON_ADDRESS.getId(), ID_TAX_PAYER.getId(),

            OKATO.getId()
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

		if (simpleReadOnlyRefBooks.contains(refBookId)) {
			RefBookSimpleReadOnly dataProvider = (RefBookSimpleReadOnly) applicationContext.getBean("refBookSimpleReadOnly", RefBookDataProvider.class);
			dataProvider.setRefBook(refBook);
			return dataProvider;
		}
		if (simpleEditableRefBooks.contains(refBookId)) {
			RefBookSimpleDataProvider dataProvider = (RefBookSimpleDataProvider) applicationContext.getBean("refBookSimpleDataProvider", RefBookDataProvider.class);
			dataProvider.setRefBook(refBook);
			return dataProvider;
		}

        if (DEPARTMENT.getId() == refBookId) {
            return applicationContext.getBean("refBookDepartment", RefBookDataProvider.class);
        }
		if(RefBookOktmoProvider.OKTMO_REF_BOOK_ID.equals(refBookId)) {  //  Справочник "ОКТМО"
            RefBookOktmoProvider dataProvider = (RefBookOktmoProvider) applicationContext.getBean("RefBookOktmoProvider", RefBookDataProvider.class);
            dataProvider.setRefBookId(refBookId);
			dataProvider.setTableName(RefBookOktmoProvider.OKTMO_TABLE_NAME);
            return dataProvider;
		}
		if (CONFIGURATION_PARAM.getId() == refBookId) {
            RefBookConfigurationParam dataProvider = applicationContext.getBean("refBookConfigurationParam", RefBookConfigurationParam.class);
            dataProvider.setRefBook(refBook);
            return dataProvider;
		}
		if (AUDIT_FIELD.getId() == refBookId) {
            RefBookAuditFieldList dataProvider = applicationContext.getBean("refBookAuditFieldList", RefBookAuditFieldList.class);
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
        if (query == null || query.isEmpty()){
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

            if (resultSearch.length() > 0){
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
                    if (isSimpleRefBool(refBookId)){
                        String fullAlias = getStackAlias(attribute);
                        switch (getLastAttribute(attribute).getAttributeType()){
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

    /**
     * Метод возврадет полный алиас для ссылочного атрибута вида
     * user.city.name
     *
     * @param attribute
     * @return
     */
    private String getStackAlias(RefBookAttribute attribute){
        switch (attribute.getAttributeType()) {
            case STRING:
            case DATE:
            case NUMBER:
                return attribute.getAlias();
            case REFERENCE:
                RefBook rb = get(attribute.getRefBookId());
                RefBookAttribute nextAttribute = rb.getAttribute(attribute.getRefBookAttributeId());
                return attribute.getAlias()+"."+getStackAlias(nextAttribute);
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
    private RefBookAttribute getLastAttribute(RefBookAttribute attribute){
        if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)){
            RefBook rb = getByAttribute(attribute.getRefBookAttributeId());
            RefBookAttribute nextAttribute = rb.getAttribute(attribute.getRefBookAttributeId());

            return getLastAttribute(nextAttribute);
        } else{
            return attribute;
        }
    }

    /**
     * Находится ли справочник в стандартной структуре
     *
     * @param refBookId
     * @return
     */
    private boolean isSimpleRefBool(Long refBookId){
        // TODO Левыкин: нереализованный метод?
        return true;
    }

    @Override
    public String getTaskName(ReportType reportType, Long refBookId, String specificReportType) {
        RefBook refBook = get(refBookId);
        switch (reportType) {
            case EXCEL_REF_BOOK:
            case CSV_REF_BOOK:
            case IMPORT_REF_BOOK:
            case EDIT_REF_BOOK:
                return String.format(reportType.getDescription(), refBook.getName());
            case SPECIFIC_REPORT_REF_BOOK:
                return String.format(reportType.getDescription(), specificReportType, refBook.getName());
            default:
                throw new ServiceException("Неверный тип отчета(%s)", reportType.getName());
        }
    }

    @Override
    public String getTaskFullName(ReportType reportType, Long refBookId, Date version, String filter, String specificReportType) {
        RefBook refBook = get(refBookId);
        switch (reportType) {
            case EXCEL_REF_BOOK:
            case CSV_REF_BOOK:
                return String.format("Формирование отчета справочника \"%s\" в %s-формате : Версия: %s, Фильтр: \"%s\"", refBook.getName(), reportType.getName(), sdf.get().format(version), filter);
            case SPECIFIC_REPORT_REF_BOOK:
                return String.format("Формирование специфического отчета \"%s\" справочника \"%s\": Версия: %s, Фильтр: \"%s\"", specificReportType, refBook.getName(), sdf.get().format(version), filter);
            case IMPORT_REF_BOOK:
                return String.format("Загрузка данных из файла в справочник \"%s\"", refBook.getName());
            default:
                throw new ServiceException("Неверный тип отчета(%s)", reportType.getName());
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
            if (ReportType.EXCEL_REF_BOOK.getName().equals(specificReportType) ||
                    ReportType.CSV_REF_BOOK.getName().equals(specificReportType)) {
                iterator.remove();
                LOG.error(String.format("Нельзя переопределить стандартный отчет: %s.", specificReportType));
            }
        }
        return specificReportTypes;
    }

    @Override
    public Pair<ReportType, LockData> getLockTaskType(long refBookId) {
        ReportType[] reportTypes = {ReportType.IMPORT_REF_BOOK, ReportType.EDIT_REF_BOOK};
        for (ReportType reportType : reportTypes) {
            LockData lockData = lockDataService.getLock(generateTaskKey(refBookId, reportType));
            if (lockData != null)
                return new Pair<ReportType, LockData>(reportType, lockData);
        }
        return null;
    }

    @Override
    public String generateTaskKey(long refBookId, ReportType reportType) {
        return LockData.LockObjects.REF_BOOK.name() + "_" + refBookId + "_" + reportType.getName();
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
