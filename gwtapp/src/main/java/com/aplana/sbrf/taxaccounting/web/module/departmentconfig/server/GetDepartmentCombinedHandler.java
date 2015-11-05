package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.server;

import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.DepartmentCombined;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.GetDepartmentCombinedAction;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.GetDepartmentCombinedResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Получение параметров подразделения и списка доступных налоговых периодов
 *
 * @author Dmitriy Levykin
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class GetDepartmentCombinedHandler extends AbstractActionHandler<GetDepartmentCombinedAction,
        GetDepartmentCombinedResult> {

    private static final Log LOG = LogFactory.getLog(GetDepartmentCombinedHandler.class);

    @Autowired
    private PeriodService reportService;

    @Autowired
    private RefBookFactory rbFactory;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;

    public GetDepartmentCombinedHandler() {
        super(GetDepartmentCombinedAction.class);
    }

    @Override
    public GetDepartmentCombinedResult execute(GetDepartmentCombinedAction action, ExecutionContext executionContext)
            throws ActionException {

        DepartmentCombined depCombined = new DepartmentCombined();

        RefBookDataProvider provider = null;

        Long parentRefBookId = null;

        GetDepartmentCombinedResult result = new GetDepartmentCombinedResult();
        result.setDepartmentCombined(depCombined);

        if (action.getDepartmentId() == null) {
            return result;
        }

        switch (action.getTaxType()) {
            case INCOME:
                parentRefBookId = RefBook.DEPARTMENT_CONFIG_INCOME;
                provider = rbFactory.getDataProvider(parentRefBookId);
                break;
            case TRANSPORT:
                parentRefBookId = RefBook.DEPARTMENT_CONFIG_TRANSPORT;
                provider = rbFactory.getDataProvider(parentRefBookId);
                break;
            case DEAL:
                parentRefBookId = RefBook.DEPARTMENT_CONFIG_DEAL;
                provider = rbFactory.getDataProvider(parentRefBookId);
                break;
            case VAT:
                parentRefBookId = RefBook.DEPARTMENT_CONFIG_VAT;
                provider = rbFactory.getDataProvider(parentRefBookId);
                break;
            case PROPERTY:
                parentRefBookId = RefBook.DEPARTMENT_CONFIG_PROPERTY;
                provider = rbFactory.getDataProvider(parentRefBookId);
                break;
        }

        if (parentRefBookId != null) {
            provider = rbFactory.getDataProvider(parentRefBookId);
        }

        Calendar calendarFrom = reportService.getEndDate(action.getReportPeriodId());

        String filter = DepartmentParamAliases.DEPARTMENT_ID.name() + " = " + action.getDepartmentId();
        //Берем -1 день, чтобы исключить возможность пересечения периодов актуальности для периодов 9 мес и год.
        //Т.к 9 мес имеет период 01.07-30.09 а год 01.10-31.12 и в api справочников при создании новой версии выполняется инкремент даты окончания + 1 день,
        //то возникает ошибка дубликата ключевых полей таблицы ref_book_record, т.к уже существует версия с полем version = 01.10.xxxx для 9 мес (фактически это фиктивная версия),
        //а сейчас мы пытаемся добавить запись с такой же датой начала для настроек на год
        PagingResult<Map<String, RefBookValue>> params = provider.getRecords(
                addDayToDate(calendarFrom.getTime(), -1), null, filter, null);

        if (!params.isEmpty()) {
            Map<String, RefBookValue> paramsMap = params.get(0);
            if (params.size() != 1) {
                String dt = new SimpleDateFormat("dd.MM.yyyy").format(calendarFrom.getTime());
                LOG.debug(String.format("Found more than one record on version = %s ref_book_id = %s department_id = %s map = %s",
						dt, parentRefBookId, action.getDepartmentId(), params));
                throw new ActionException("Найдено несколько записей для версии " + dt);
            }

            // Id записи
            depCombined.setRecordId(paramsMap.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue());

            // Общая часть
            depCombined.setDepartmentId(getList(paramsMap.get(DepartmentParamAliases.DEPARTMENT_ID.name()).getReferenceValue()));
            depCombined.setDictRegionId(getList(paramsMap.get(DepartmentParamAliases.DICT_REGION_ID.name()).getReferenceValue()));
            depCombined.setOktmo(getList(paramsMap.get(DepartmentParamAliases.OKTMO.name()).getReferenceValue()));
            depCombined.setInn(paramsMap.get(DepartmentParamAliases.INN.name()).getStringValue());
            depCombined.setKpp(paramsMap.get(DepartmentParamAliases.KPP.name()).getStringValue());
            depCombined.setTaxOrganCode(paramsMap.get(DepartmentParamAliases.TAX_ORGAN_CODE.name()).getStringValue());
            depCombined.setOkvedCode(getList(paramsMap.get(DepartmentParamAliases.OKVED_CODE.name()).getReferenceValue()));
            depCombined.setPhone(paramsMap.get(DepartmentParamAliases.PHONE.name()).getStringValue());

            depCombined.setReorgFormCode(getList(paramsMap.get(DepartmentParamAliases.REORG_FORM_CODE.name()).getReferenceValue()));
            depCombined.setReorgInn(paramsMap.get(DepartmentParamAliases.REORG_INN.name()).getStringValue());
            depCombined.setReorgKpp(paramsMap.get(DepartmentParamAliases.REORG_KPP.name()).getStringValue());

            depCombined.setName(paramsMap.get(DepartmentParamAliases.NAME.name()).getStringValue());

            // Общая частная часть
            depCombined.setSignatoryId(getList(paramsMap.get(DepartmentParamAliases.SIGNATORY_ID.name()).getReferenceValue()));
            depCombined.setSignatorySurname(paramsMap.get(DepartmentParamAliases.SIGNATORY_SURNAME.name()).getStringValue());
            depCombined.setSignatoryFirstname(paramsMap.get(DepartmentParamAliases.SIGNATORY_FIRSTNAME.name()).getStringValue());
            depCombined.setSignatoryLastname(paramsMap.get(DepartmentParamAliases.SIGNATORY_LASTNAME.name()).getStringValue());
            depCombined.setApproveDocName(paramsMap.get(DepartmentParamAliases.APPROVE_DOC_NAME.name()).getStringValue());
            depCombined.setApproveOrgName(paramsMap.get(DepartmentParamAliases.APPROVE_ORG_NAME.name()).getStringValue());
            depCombined.setTaxPlaceTypeCode(getList(paramsMap.get(DepartmentParamAliases.TAX_PLACE_TYPE_CODE.name()).getReferenceValue()));
            depCombined.setFormatVersion(paramsMap.get(DepartmentParamAliases.FORMAT_VERSION.name()).getStringValue());

            // Налог на прибыль
            if (action.getTaxType() == TaxType.INCOME) {
                Number sumTax = paramsMap.get(DepartmentParamAliases.SUM_TAX.name()).getNumberValue();
                depCombined.setSumTax(sumTax == null ? null : sumTax.longValue());
                Number sumDividends = paramsMap.get(DepartmentParamAliases.SUM_DIVIDENDS.name()).getNumberValue();
                depCombined.setSumDividends(sumDividends == null ? null : sumDividends.longValue());
                depCombined.setObligation(getList(paramsMap.get(DepartmentParamAliases.OBLIGATION.name()).getReferenceValue()));
                Number taxRate = paramsMap.get(DepartmentParamAliases.TAX_RATE.name()).getNumberValue();
                depCombined.setTaxRate(taxRate == null ? null : taxRate.doubleValue());
                depCombined.setType(getList(paramsMap.get(DepartmentParamAliases.TYPE.name()).getReferenceValue()));
                depCombined.setAdditionalName(paramsMap.get(DepartmentParamAliases.ADDITIONAL_NAME.name()).getStringValue());
            }

            // Транспортный налог
            if (action.getTaxType() == TaxType.TRANSPORT) {
                Number prepayment = paramsMap.get(DepartmentParamAliases.PREPAYMENT.name()).getNumberValue();
                depCombined.setPrepayment(prepayment == null ? false : prepayment.longValue() == 1L);
            }

            // НДС
            if (TaxType.VAT.equals(action.getTaxType())) {
                depCombined.setTaxOrganCodeProm((paramsMap.get(DepartmentParamAliases.TAX_ORGAN_CODE_PROM.name()).getStringValue()));
            }
        }

        // Если запись не нашлась, то готовим новую
        if (result.getDepartmentCombined().getDepartmentId() == null && action.getDepartmentId() != null) {
            result.getDepartmentCombined().setDepartmentId(getList(action.getDepartmentId().longValue()));
        }

        // Признак открытости
        DepartmentReportPeriodFilter departmentReportPeriodFilter = new DepartmentReportPeriodFilter();
        departmentReportPeriodFilter.setDepartmentIdList(Arrays.asList(action.getDepartmentId()));
        departmentReportPeriodFilter.setReportPeriodIdList(Arrays.asList(action.getReportPeriodId()));
        departmentReportPeriodFilter.setIsActive(true);
        List<DepartmentReportPeriod> departmentReportPeriodList = departmentReportPeriodService.getListByFilter(departmentReportPeriodFilter);
        DepartmentReportPeriod departmentReportPeriod = null;
        if (departmentReportPeriodList.size() == 1) {
            departmentReportPeriod = departmentReportPeriodList.get(0);
        }
        result.setReportPeriodActive(departmentReportPeriod != null && departmentReportPeriod.isActive());

        // Получение текстовых значений справочника
        Map<Long, String> rbTextValues = new HashMap<Long, String>();

        Logger logger = new Logger();

        if (depCombined.getDictRegionId() != null && !depCombined.getDictRegionId().isEmpty()) {
            getValueIgnoreEmptyResult(rbTextValues, parentRefBookId, 4L, 9L, depCombined.getDictRegionId().get(0), logger);
        }
        if (depCombined.getOktmo() != null && !depCombined.getOktmo().isEmpty()) {
            getValueIgnoreEmptyResult(rbTextValues, parentRefBookId, 96L, 840L, depCombined.getOktmo().get(0), logger);
        }
        if (depCombined.getOkvedCode() != null && !depCombined.getOkvedCode().isEmpty()) {
            getValueIgnoreEmptyResult(rbTextValues, parentRefBookId, 34L, 210L, depCombined.getOkvedCode().get(0), logger);
        }
        if (depCombined.getReorgFormCode() != null && !depCombined.getReorgFormCode().isEmpty()) {
            getValueIgnoreEmptyResult(rbTextValues, parentRefBookId, 5L, 13L, depCombined.getReorgFormCode().get(0), logger);
        }
        if (depCombined.getSignatoryId() != null && !depCombined.getSignatoryId().isEmpty()) {
            getValueIgnoreEmptyResult(rbTextValues, parentRefBookId, 35L, 213L, depCombined.getSignatoryId().get(0), logger);
        }
        if (depCombined.getTaxPlaceTypeCode() != null && !depCombined.getTaxPlaceTypeCode().isEmpty()) {
            getValueIgnoreEmptyResult(rbTextValues, parentRefBookId, 2L, 3L, depCombined.getTaxPlaceTypeCode().get(0), logger);
        }
        if (depCombined.getObligation() != null && !depCombined.getObligation().isEmpty()) {
            getValueIgnoreEmptyResult(rbTextValues, parentRefBookId, 25L, 110L, depCombined.getObligation().get(0), logger);
        }
        if (depCombined.getType() != null && !depCombined.getType().isEmpty()) {
            getValueIgnoreEmptyResult(rbTextValues, parentRefBookId, 26L, 120L, depCombined.getType().get(0), logger);
        }

        result.setRbTextValues(rbTextValues);

        // Запись ошибок в лог при наличии
        if (!logger.getEntries().isEmpty()) {
            result.setUuid(logEntryService.save(logger.getEntries()));
        }

        return result;
    }

    /**
     * Разыменование значения справочника с обработкой исключения, возникающего при отсутствии записи
     *
     * @param map             Id атрибута -> Разыменованное значение
     * @param parentRefBookId Id справочника формы настроек
     * @param refBookId       Id справочника
     * @param attributeId     Id атрибута
     * @param recordId        Id записи
     * @param logger          Логгер для передачи клиенту
     */
    private void getValueIgnoreEmptyResult(Map<Long, String> map, long parentRefBookId, long refBookId, long attributeId, long recordId, Logger logger) {
        RefBookValue value = rbFactory.getDataProvider(refBookId).getValue(recordId, attributeId);
        if (value == null) {
            logger.error(String.format("Ошибка получения значений для формы «%s»: " +
                    "Обнаружена ссылка на несуществующую запись справочника «%s», id = %d",
                    rbFactory.get(parentRefBookId).getName(), rbFactory.get(refBookId).getName(), recordId));
            throw new ServiceLoggerException("Ошибка при получении настроек подразделения", logEntryService.save(logger.getEntries()));
        }
        map.put(attributeId, getNumberValue(value));
    }

    /**
     * Разыменование числовых значений как строк и строк как строк
     */
    private String getNumberValue(RefBookValue value) {
        if (value == null) {
            return null;
        }
        if (value.getAttributeType() == RefBookAttributeType.STRING) {
            return value.getStringValue();
        }
        if (value.getNumberValue() == null) {
            return null;
        }
        return value.getNumberValue().toString();
    }

    @Override
    public void undo(GetDepartmentCombinedAction action, GetDepartmentCombinedResult result,
                     ExecutionContext executionContext) throws ActionException {
        // Не требуется
    }

    private List<Long> getList(Long value) {
        List<Long> list = null;
        if (value != null) {
            list = new ArrayList<Long>();
            list.add(value);
        }
        return list;
    }

    private Date addDayToDate(Date date, int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, days);
        return c.getTime();
    }
}
