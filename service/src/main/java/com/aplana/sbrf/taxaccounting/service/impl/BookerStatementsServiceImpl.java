package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.Income101;
import com.aplana.sbrf.taxaccounting.model.Income102;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.BookerStatementsService;
import com.aplana.sbrf.taxaccounting.service.ReportPeriodService;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Сервис для формы "Загрузка бухгалтерской отчётности из xls"
 *
 * @author Stanislav Yasinskiy
 */
@Service
@Transactional
public class BookerStatementsServiceImpl implements BookerStatementsService {
    private static long INCOME_101 = 50L;
    private static long INCOME_102 = 52L;

    private static String I_101_REPORT_PERIOD_ID = "REPORT_PERIOD_ID";
    private static String I_101_ACCOUNT = "ACCOUNT";
    private static String I_101_ACCOUNT_NAME = "ACCOUNT_NAME";
    private static String I_101_INCOME_DEBET_REMAINS = "INCOME_DEBET_REMAINS";
    private static String I_101_INCOME_CREDIT_REMAINS = "INCOME_CREDIT_REMAINS";
    private static String I_101_DEBET_RATE = "DEBET_RATE";
    private static String I_101_CREDIT_RATE = "CREDIT_RATE";
    private static String I_101_OUTCOME_DEBET_REMAINS = "OUTCOME_DEBET_REMAINS";
    private static String I_101_OUTCOME_CREDIT_REMAINS = "OUTCOME_CREDIT_REMAINS";
    private static String I_101_DEPARTMENT_ID = "DEPARTMENT_ID";

    private static String I_102_REPORT_PERIOD_ID = "REPORT_PERIOD_ID";
    private static String I_102_OPU_CODE = "OPU_CODE";
    private static String I_102_TOTAL_SUM = "TOTAL_SUM";
    private static String I_102_ITEM_NAME = "ITEM_NAME";
    private static String I_102_DEPARTMENT_ID = "DEPARTMENT_ID";

    // Ограничение по строкам для xls-файла
    private static long MAX_FILE_ROW = 10000L;

    private static String BAD_FILE_MSG = "Формат файла не соответствуют ожидаемому формату. Файл не может быть загружен.";

    @Autowired
    ReportPeriodService reportPeriodService;

    @Autowired
    RefBookFactory rbFactory;

    @Override
    public void importXML(InputStream stream, Integer periodId, int typeId, int departmentId) throws IOException, ServiceException {
        // Проверка того, что пользователем указан открытый отчетный период
        if (!reportPeriodService.isActivePeriod(periodId, departmentId)) {
            throw new ServiceException("Указан закрытый период. Файл не может быть загружен.");
        }

        if (typeId == 0) {
            RefBookDataProvider provider = rbFactory.getDataProvider(INCOME_101);
            List<Income101> list = importIncome101(stream);

            if (list != null && !list.isEmpty()) {
                List<Map<String, RefBookValue>> records = new LinkedList<Map<String, RefBookValue>>();

                for (Income101 item : list) {
                    Map<String, RefBookValue> map = new HashMap<String, RefBookValue>();
                    map.put(I_101_REPORT_PERIOD_ID, new RefBookValue(RefBookAttributeType.NUMBER, periodId));
                    map.put(I_101_ACCOUNT, new RefBookValue(RefBookAttributeType.STRING, item.getAccount()));
                    map.put(I_101_ACCOUNT_NAME, new RefBookValue(RefBookAttributeType.STRING, item.getAccountName()));
                    map.put(I_101_INCOME_DEBET_REMAINS, new RefBookValue(RefBookAttributeType.NUMBER, item.getIncomeDebetRemains()));
                    map.put(I_101_INCOME_CREDIT_REMAINS, new RefBookValue(RefBookAttributeType.NUMBER, item.getIncomeCreditRemains()));
                    map.put(I_101_DEBET_RATE, new RefBookValue(RefBookAttributeType.NUMBER, item.getDebetRate()));
                    map.put(I_101_CREDIT_RATE, new RefBookValue(RefBookAttributeType.NUMBER, item.getCreditRate()));
                    map.put(I_101_OUTCOME_DEBET_REMAINS, new RefBookValue(RefBookAttributeType.NUMBER, item.getOutcomeDebetRemains()));
                    map.put(I_101_OUTCOME_CREDIT_REMAINS, new RefBookValue(RefBookAttributeType.NUMBER, item.getOutcomeCreditRemains()));
                    map.put(I_101_DEPARTMENT_ID, new RefBookValue(RefBookAttributeType.REFERENCE, (long)departmentId));
                    records.add(map);
                }

                provider.updateRecords(new Date(), records);
            }
        } else {
            RefBookDataProvider provider = rbFactory.getDataProvider(INCOME_102);
            List<Income102> list = importIncome102(stream);

            if (list != null && !list.isEmpty()) {
                List<Map<String, RefBookValue>> records = new LinkedList<Map<String, RefBookValue>>();

                for (Income102 item : list) {
                    Map<String, RefBookValue> map = new HashMap<String, RefBookValue>();
                    map.put(I_102_REPORT_PERIOD_ID, new RefBookValue(RefBookAttributeType.NUMBER, periodId));
                    map.put(I_102_OPU_CODE, new RefBookValue(RefBookAttributeType.STRING, item.getOpuCode()));
                    map.put(I_102_TOTAL_SUM, new RefBookValue(RefBookAttributeType.NUMBER, item.getTotalSum()));
                    map.put(I_102_ITEM_NAME, new RefBookValue(RefBookAttributeType.STRING, item.getItemName()));
                    map.put(I_102_DEPARTMENT_ID, new RefBookValue(RefBookAttributeType.REFERENCE, (long) departmentId));
                    records.add(map);
                }

                provider.updateRecords(new Date(), records);
            }
        }
    }

    private List<Income101> importIncome101(InputStream stream) throws IOException, ServiceException{
        List<Income101> list = new ArrayList<Income101>();
        HSSFWorkbook wb = new HSSFWorkbook(stream);
        Sheet sheet = wb.getSheetAt(0);
        Iterator<Row> it = sheet.iterator();
        boolean endOfFile = false;
        long rowCounter = 1L;
        while (it.hasNext() && !endOfFile) {
            if (rowCounter++ > MAX_FILE_ROW) {
                throw new ServiceException(BAD_FILE_MSG);
            }

            Row row = it.next();
            Iterator<Cell> cells = row.iterator();

            // проверка ячеек в строке 10
            if (row.getRowNum() == 9) {
                while (cells.hasNext()) {
                    Cell cell = cells.next();
                    int colNum = cell.getColumnIndex();
                    String colName = cell.getStringCellValue().trim();
                    if ((colNum == 1 && !colName.equals("Номер счета"))
                            || (colNum == 2 && !colName.equals("Название"))
                            || (colNum == 4 && !colName.equals("Входящие остатки"))
                            || (colNum == 6 && !colName.equals("Обороты за отчетный период"))
                            || (colNum == 8 && !colName.equals("Исходящие остатки")))
                        throw new ServiceException(BAD_FILE_MSG);
                }
            }
            // проверка ячеек в строке 12
            if (row.getRowNum() == 11) {
                while (cells.hasNext()) {
                    Cell cell = cells.next();
                    int colNum = cell.getColumnIndex();
                    String colName = cell.getStringCellValue().trim();
                    if (((colNum == 4 || colNum == 6 || colNum == 8) && !colName.equals("по дебету"))
                            || ((colNum == 5 || colNum == 7 || colNum == 9) && !colName.equals("по кредиту")))
                        throw new ServiceException(BAD_FILE_MSG);
                }
            }
            // парсим с 18 строки
            if (row.getRowNum() < 18) {
                continue;
            }
            // можно ориентироваться на не пустые ячейки столбца B, начиная с 19 строки
            Income101 model = new Income101();
            endOfFile = true;
            boolean skipRow = false;
            for(int cn=0;cn<row.getLastCellNum() && !skipRow;cn++){
                Cell cell = row.getCell(cn,Row.RETURN_BLANK_AS_NULL);
                if (cell!=null) {
                    try {
                        // заполняем модель для вставки в БД
                        switch (cell.getColumnIndex()) {
                            case 1:
                                endOfFile = false;
                                // игнорируем строки не соотнесённые с номерами счетов (разделы, главы)
                                String account = cell.getStringCellValue();
                                Pattern p = Pattern.compile("[0-9.]+");
                                Matcher m = p.matcher(account);
                                if(account==null || account.isEmpty() || !m.matches()){
                                    skipRow = true;
                                    break;
                                }
                                model.setAccount(cell.getStringCellValue());
                                break;
                            case 2:
                                model.setAccountName(cell.getStringCellValue());
                                break;
                            case 4:
                                model.setIncomeDebetRemains(cell.getNumericCellValue());
                                break;
                            case 5:
                                model.setIncomeCreditRemains(cell.getNumericCellValue());
                                break;
                            case 6:
                                model.setDebetRate(cell.getNumericCellValue());
                                break;
                            case 7:
                                model.setCreditRate(cell.getNumericCellValue());
                                break;
                            case 8:
                                model.setOutcomeDebetRemains(cell.getNumericCellValue());
                                break;
                            case 9:
                                model.setOutcomeCreditRemains(cell.getNumericCellValue());
                                break;
                        }
                    } catch (IllegalStateException e) {
                        throw getServiceException(cell.getColumnIndex(), 1);
                    }
                }
            }
            if (!endOfFile && !skipRow) {
                if (!isModelValid(model)) {
                    throw new ServiceException(BAD_FILE_MSG);
                }
                list.add(model);
            }
        }
        return list;
    }

    private List<Income102> importIncome102(InputStream stream) throws IOException, ServiceException {
        // строки со следующими кодами игнорируем
        Set<String> excludeCode = new HashSet<String>();
        excludeCode.add("");
        excludeCode.add("10000");
        excludeCode.add("20000");
        // выходной лист с моделями для записи в бд
        List<Income102> list = new ArrayList<Income102>();
        HSSFWorkbook wb = new HSSFWorkbook(stream);
        Sheet sheet = wb.getSheetAt(0);
        Iterator<Row> it = sheet.iterator();
        boolean endOfFile = false;
        long rowCounter = 1L;
        while (it.hasNext() && !endOfFile) {
            if (rowCounter++ > MAX_FILE_ROW) {
                throw new ServiceException(BAD_FILE_MSG);
            }

            Row row = it.next();

            // парсим с 18 строки
            if (row.getRowNum() < 18) {
                continue;
            }

            boolean skipRow = false;
            endOfFile = true;
            Income102 model = new Income102();
            for(int cn=0;cn<row.getLastCellNum() && !skipRow;cn++){
                Cell cell = row.getCell(cn,Row.RETURN_BLANK_AS_NULL);
                if (cell!=null) {
                    try {
                        // заполняем модель для вставки в БД
                        switch (cell.getColumnIndex()) {
                            case 2:
                                model.setItemName(cell.getStringCellValue());
                                break;
                            case 3:
                                endOfFile = false;
                                //Пропускаем строки с "плохим" кодом
                                String opCode = cell.getStringCellValue();
                                Pattern p = Pattern.compile("0[0-9]+");
                                Matcher m = p.matcher(opCode.trim());
                                if (opCode == null || excludeCode.contains(opCode.trim()) || m.matches()) {
                                    skipRow = true;
                                    break;
                                }
                                model.setOpuCode(opCode.trim());
                                break;
                            case 6:
                                model.setTotalSum(cell.getNumericCellValue());
                                break;
                        }
                    } catch (IllegalStateException e) {
                        throw getServiceException(cell.getColumnIndex(), 1);
                    }
                }
            }
            if (!endOfFile && !skipRow) {
                if (!isModelValid(model)) {
                    throw new ServiceException(BAD_FILE_MSG);
                }
                list.add(model);
            }
        }
        return list;
    }

    /**
     * @param model проверяемая модель
     * @return true если ячейки в столбцах, указанные в описании формата, не пустые (нет ни одной пустой ячейки), иначе - false
     */
    private boolean isModelValid(Income101 model) {
        return model.getAccount() != null &&  !model.getAccount().isEmpty()
                && model.getIncomeDebetRemains() != null
                && model.getIncomeCreditRemains() != null
                && model.getDebetRate() != null
                && model.getCreditRate() != null
                && model.getOutcomeDebetRemains() != null
                && model.getOutcomeCreditRemains() != null
                && model.getAccountName() != null  &&  !model.getAccountName().isEmpty();
    }

    /**
     * @param model проверяемая модель
     * @return true если ячейки в столбцах, указанные в описании формата, не пустые (нет ни одной пустой ячейки), иначе - false
     */
    private boolean isModelValid(Income102 model) {
        return model.getOpuCode() != null
                && model.getTotalSum() != null &&
                model.getItemName() != null;
    }

    /**
     * Если тип загружаемых данных не соответствует объявленным
     *
     * @param columnIndex индекс колонки (для определения текста ошибки)
     * @param typeID      тип бух отчетности
     */
    private ServiceException getServiceException(int columnIndex, int typeID) {
        String colName = "";
        if (typeID == 0) {
            switch (columnIndex) {
                case 1:
                    colName = "Номер счета";
                    break;
                case 2:
                    colName = "Название";
                    break;
                case 4:
                    colName = "Входящие остатки по дебету";
                    break;
                case 5:
                    colName = "Входящие остатки по кредиту";
                    break;
                case 6:
                    colName = "Обороты за отчетный период по дебету";
                    break;
                case 7:
                    colName = "Обороты за отчетный период по кредиту";
                    break;
                case 8:
                    colName = "Исходящие остатки по дебету";
                    break;
                case 9:
                    colName = "Исходящие остатки по кредиту";
                    break;
            }
        } else {
            switch (columnIndex) {
                case 2:
                    colName = "Наименование статьи";
                    break;
                case 3:
                    colName = "Код ОПУ";
                    break;
                case 6:
                    colName = "Сумма";
                    break;
            }
        }
        return new ServiceException("Данные столбца '" + colName + "' файла не соответствуют ожидаемому типу данных. Файл не может быть загружен.");
    }
}
