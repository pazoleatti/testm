package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.BookerStatementsSearchDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookIncome101Dao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookIncome102Dao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecord;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.*;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

/**
 * Сервис для форм бухгалтерской отчётности
 *
 * @author Stanislav Yasinskiy
 */
@Service
@Transactional
public class BookerStatementsServiceImpl implements BookerStatementsService {

    private static final String I_101_ACCOUNT = "ACCOUNT";
    private static final String I_101_ACCOUNT_NAME = "ACCOUNT_NAME";
    private static final String I_101_INCOME_DEBET_REMAINS = "INCOME_DEBET_REMAINS";
    private static final String I_101_INCOME_CREDIT_REMAINS = "INCOME_CREDIT_REMAINS";
    private static final String I_101_DEBET_RATE = "DEBET_RATE";
    private static final String I_101_CREDIT_RATE = "CREDIT_RATE";
    private static final String I_101_OUTCOME_DEBET_REMAINS = "OUTCOME_DEBET_REMAINS";
    private static final String I_101_OUTCOME_CREDIT_REMAINS = "OUTCOME_CREDIT_REMAINS";
    private static final String I_101_ACCOUNT_PERIOD_ID = "ACCOUNT_PERIOD_ID";

    private static final String I_102_OPU_CODE = "OPU_CODE";
    private static final String I_102_TOTAL_SUM = "TOTAL_SUM";
    private static final String I_102_ITEM_NAME = "ITEM_NAME";
    private static final String I_102_ACCOUNT_PERIOD_ID = "ACCOUNT_PERIOD_ID";

    private static final String NO_DATA_FILE_MSG = "Файл не содержит данных. Файл не может быть загружен.";
    private static final String ACCOUNT_PERIOD_INVALID = "Период не указан.";
    private static final String DEPARTMENTID_INVALID = "Подразделение не указано.";
    private static final String ACCOUNT_LOG = "Вид бух. отчетности - %s";

    @Autowired
    PeriodService reportPeriodService;

    @Autowired
    RefBookFactory rbFactory;

    @Autowired
    AuditService auditService;

    @Autowired
    BookerStatementsSearchDao bookerStatementsSearchDao;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private RefBookScriptingService refBookScriptingService;

    @Autowired
    private RefBookFactory refBookFactory;

    @Override
    public void importData(String realFileName, InputStream stream, Integer accountPeriodId, int typeId, Integer departmentId, TAUserInfo userInfo) {

        if (stream == null) {
            throw new ServiceException(NO_DATA_FILE_MSG);
        }
        if (departmentId == null) {
            throw new ServiceException(DEPARTMENTID_INVALID);
        }
        if (accountPeriodId == null) {
            throw new ServiceException(ACCOUNT_PERIOD_INVALID);
        }

        // Обращение к скрипту
        Map<String, Object> additionalParameters = new HashMap<String, Object>();
        additionalParameters.put("inputStream", stream);
        additionalParameters.put("fileName", realFileName);
        additionalParameters.put("accountPeriodId", accountPeriodId);

        Logger logger = new Logger();

        Long refBookId = (typeId == 0 ? RefBookIncome101Dao.REF_BOOK_ID : RefBookIncome102Dao.REF_BOOK_ID);
        String formTypeName = String.format(ACCOUNT_LOG, typeId == 0 ? BookerStatementsType.INCOME101.getName() : BookerStatementsType.INCOME102.getName());

        //Выполняем логику скрипта
        refBookScriptingService.executeScript(userInfo, refBookId, FormDataEvent.IMPORT_TRANSPORT_FILE,
                logger, additionalParameters);
        IOUtils.closeQuietly(stream);

        //Получение имени отчетного периода
        RefBookDataProvider dataProvider = refBookFactory.getDataProvider(107l);
        Map<String, RefBookValue> refBookValueMap = dataProvider.getRecordData(accountPeriodId.longValue());
        String date = String.valueOf(refBookValueMap.get("YEAR").getNumberValue());
        dataProvider = refBookFactory.getDataProvider(106L);
        refBookValueMap = dataProvider.getRecordData(refBookValueMap.get("ACCOUNT_PERIOD_ID").getReferenceValue());
        String name = refBookValueMap.get("NAME").getStringValue();

        String msg = String.format("Импорт бухгалтерской отчётности: %s", realFileName);
        auditService.add(FormDataEvent.IMPORT, userInfo, date + " " + name, departmentId, null, formTypeName, null, msg, null);
    }

    @Override
    public void create(Logger logger, Integer year, Long periodId, int typeId, Integer departmentId, TAUserInfo userInfo) {

        RefBookDataProvider provider;
        if (typeId == BookerStatementsType.INCOME101.getId()) {
            provider = rbFactory.getDataProvider(RefBookIncome101Dao.REF_BOOK_ID);
        } else {
            provider = rbFactory.getDataProvider(RefBookIncome102Dao.REF_BOOK_ID);
        }

        Date date = getStartDate();
        List<Long> ids = rbFactory.getDataProvider(107L).getUniqueRecordIds(null,
                " account_period_id = " + periodId + " and year = " + year + " and department_id = " + departmentId);
        if (!ids.isEmpty()) {
            List<Long> ids101 = provider.getUniqueRecordIds(null, " account_period_id = " + ids.get(0));
            if (!ids101.isEmpty()) {
                logger.error("Бухгалтерская отчётность с заданными параметрами уже существует");
            }
        } else {
            Map<String, RefBookValue> values = new HashMap<String, RefBookValue>();
            values.put("YEAR", new RefBookValue(RefBookAttributeType.NUMBER, BigDecimal.valueOf(year)));
            values.put("ACCOUNT_PERIOD_ID", new RefBookValue(RefBookAttributeType.REFERENCE, periodId));
            values.put("DEPARTMENT_ID", new RefBookValue(RefBookAttributeType.REFERENCE, Long.valueOf(departmentId)));

            RefBookRecord record = new RefBookRecord();
            record.setValues(values);

            ids = rbFactory.getDataProvider(107L).createRecordVersion(logger, date, null, Arrays.asList(record));
        }

        if (logger.containsLevel(LogLevel.ERROR)) {
            return;
        }

        List<Map<String, RefBookValue>> records = new LinkedList<Map<String, RefBookValue>>();
        Map<String, RefBookValue> map = new HashMap<String, RefBookValue>();
        if (typeId == 0) {
            map.put(I_101_ACCOUNT, new RefBookValue(RefBookAttributeType.STRING, "-1"));
            map.put(I_101_ACCOUNT_NAME, new RefBookValue(RefBookAttributeType.STRING, null));
            map.put(I_101_INCOME_DEBET_REMAINS, new RefBookValue(RefBookAttributeType.NUMBER, null));
            map.put(I_101_INCOME_CREDIT_REMAINS, new RefBookValue(RefBookAttributeType.NUMBER, null));
            map.put(I_101_DEBET_RATE, new RefBookValue(RefBookAttributeType.NUMBER, null));
            map.put(I_101_CREDIT_RATE, new RefBookValue(RefBookAttributeType.NUMBER, null));
            map.put(I_101_OUTCOME_DEBET_REMAINS, new RefBookValue(RefBookAttributeType.NUMBER, null));
            map.put(I_101_OUTCOME_CREDIT_REMAINS, new RefBookValue(RefBookAttributeType.NUMBER, null));
            map.put(I_101_ACCOUNT_PERIOD_ID, new RefBookValue(RefBookAttributeType.REFERENCE, ids.get(0)));
            records.add(map);
            provider.updateRecords(userInfo, date, records);

        } else {
            map.put(I_102_OPU_CODE, new RefBookValue(RefBookAttributeType.STRING, "-1"));
            map.put(I_102_TOTAL_SUM, new RefBookValue(RefBookAttributeType.NUMBER, null));
            map.put(I_102_ITEM_NAME, new RefBookValue(RefBookAttributeType.STRING, null));
            map.put(I_102_ACCOUNT_PERIOD_ID, new RefBookValue(RefBookAttributeType.REFERENCE, ids.get(0)));
            records.add(map);
            provider.updateRecords(userInfo, date, records);
        }
    }

    @Override
    public PagingResult<BookerStatementsSearchResultItem> findDataByFilter(BookerStatementsFilter bookerStatementsFilter, TAUser tAUser) {
        if (tAUser.hasRole(TARole.ROLE_CONTROL_NS)) {
            List<Integer> departmentIds = departmentService.getBADepartmentIds(tAUser);
            if (bookerStatementsFilter.getDepartmentIds() != null && !bookerStatementsFilter.getDepartmentIds().isEmpty()) {
                bookerStatementsFilter.getDepartmentIds().retainAll(departmentIds);
                if (bookerStatementsFilter.getDepartmentIds().isEmpty()) {
                    bookerStatementsFilter.setDepartmentIds(departmentIds);
                }
            } else {
                bookerStatementsFilter.setDepartmentIds(departmentIds);
            }
        }
        return bookerStatementsSearchDao.findPage(bookerStatementsFilter, bookerStatementsFilter.getSearchOrdering(),
                bookerStatementsFilter.isAscSorting(), new PagingParams(bookerStatementsFilter.getStartIndex(),
                        bookerStatementsFilter.getCountOfRecords()));
    }

    private Date getStartDate() {
        return new GregorianCalendar(2012, Calendar.JANUARY, 1).getTime();
    }
}
