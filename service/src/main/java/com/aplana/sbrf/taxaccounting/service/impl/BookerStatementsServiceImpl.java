package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.Income101;
import com.aplana.sbrf.taxaccounting.model.Income102;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.BookerStatementsService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
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
    private static final long INCOME_101 = 50L;
    private static final long INCOME_102 = 52L;

    private static final String I_101_REPORT_PERIOD_ID = "REPORT_PERIOD_ID";
    private static final String I_101_ACCOUNT = "ACCOUNT";
    private static final String I_101_ACCOUNT_NAME = "ACCOUNT_NAME";
    private static final String I_101_INCOME_DEBET_REMAINS = "INCOME_DEBET_REMAINS";
    private static final String I_101_INCOME_CREDIT_REMAINS = "INCOME_CREDIT_REMAINS";
    private static final String I_101_DEBET_RATE = "DEBET_RATE";
    private static final String I_101_CREDIT_RATE = "CREDIT_RATE";
    private static final String I_101_OUTCOME_DEBET_REMAINS = "OUTCOME_DEBET_REMAINS";
    private static final String I_101_OUTCOME_CREDIT_REMAINS = "OUTCOME_CREDIT_REMAINS";
    private static final String I_101_DEPARTMENT_ID = "DEPARTMENT_ID";

    private static final String I_102_REPORT_PERIOD_ID = "REPORT_PERIOD_ID";
    private static final String I_102_OPU_CODE = "OPU_CODE";
    private static final String I_102_TOTAL_SUM = "TOTAL_SUM";
    private static final String I_102_ITEM_NAME = "ITEM_NAME";
    private static final String I_102_DEPARTMENT_ID = "DEPARTMENT_ID";

    // Ограничение по строкам для xls-файла
    private static final long MAX_FILE_ROW = 10000L;

    private static final String BAD_FILE_MSG = "Формат файла не соответствуют ожидаемому формату. Файл не может быть загружен.";
    private static final String IO_WORKBOOK_EXCEPTION = "Не могу прочитать загруженный Excel фаил";
    private static final String REPORT_PERIOD_CLOSED = "Указан закрытый период. Файл не может быть загружен.";
    private static final String REPORT_PERIOD_INVALID = "Отчетный период не указан";
    private static final String FILE_NULL = "Не указан фаил";

    @Autowired
    PeriodService reportPeriodService;

    @Autowired
    RefBookFactory rbFactory;

    @Override
    public void importXML(String realFileName, InputStream stream, Integer periodId, int typeId, int departmentId) {

        if (stream == null) {
            throw new ServiceException(FILE_NULL);
        }
        if (periodId == null) {
            throw new ServiceException(REPORT_PERIOD_INVALID);
        }
        if (realFileName == null || !getFileExtention(realFileName).equals("xls")) {
            throw  new ServiceException(BAD_FILE_MSG);
        }
        // Проверка того, что пользователем указан открытый отчетный период
        if (!reportPeriodService.isActivePeriod(periodId, departmentId)) {
            throw new ServiceException(REPORT_PERIOD_CLOSED);
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
                    map.put(I_101_DEPARTMENT_ID, new RefBookValue(RefBookAttributeType.REFERENCE, (long) departmentId));
                    records.add(map);
                }

                provider.updateRecords(new Date(), records);
            } else {
                // TODO что делать если в файле нет строк для записи в бд?
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
            } else {
                // TODO что делать если в файле нет строк для записи в бд?
            }
        }
    }

    // Проверка расширения Булата Кинзибулатова из com.aplana.sbrf.taxaccounting.web.mvc.BookerStatementsController.getFileExtention()
    private static String getFileExtention(String filename) {
        int dotPos = filename.lastIndexOf(".") + 1;
        return filename.substring(dotPos);
    }

    private List<Income101> importIncome101(InputStream stream) {
        List<Income101> list = new ArrayList<Income101>();
        HSSFWorkbook wb = null;
        try {
            wb = new HSSFWorkbook(stream);
        } catch (IOException e) {
            throw new ServiceException(IO_WORKBOOK_EXCEPTION);
        }
        Sheet sheet = wb.getSheetAt(0);
        Iterator<Row> it = sheet.iterator();
        boolean endOfFile = false;
        boolean hasHeader = false;
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
                hasHeader = true;
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
            // парсим каждую третью строку
            if (row.getRowNum() % 3 == 0) {
                boolean isValid = true;
                Income101 model = new Income101();
                endOfFile = true;
                while (cells.hasNext()) {
                    if (!isValid)
                        break;

                    Cell cell = row.getCell(cells.next().getColumnIndex(), Row.RETURN_BLANK_AS_NULL);

                    if (cell == null) {
                        continue;
                    }

                    try {
                        // заполняем модель для вставки в БД
                        switch (cell.getColumnIndex()) {
                            case 1:
                                endOfFile = false;
                                // игнорируем строки не соотнесённые с номерами счетов (разделы, главы)
                                String account = cell.getStringCellValue();
                                Pattern p = Pattern.compile("[0-9.]+");
                                Matcher m = p.matcher(account);
                                if (!m.matches()) {
                                    isValid = false;
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
                        throw getServiceException(cell.getColumnIndex(), INCOME_101);
                    }
                }
                if (!endOfFile && isValid) {
                    if (!isModelValid(model)) {
                        throw new ServiceException(BAD_FILE_MSG);
                    }
                    list.add(model);
                }
            }
        }
        if(!hasHeader){
            throw new ServiceException(BAD_FILE_MSG);
        }
        return list;
    }

    private List<Income102> importIncome102(InputStream stream) {
        // строки со следующими кодами игнорируем
        Set<String> excludeCode = new HashSet<String>();
        excludeCode.add("");
        excludeCode.add("10000");
        excludeCode.add("20000");
        // выходной лист с моделями для записи в бд
        List<Income102> list = new ArrayList<Income102>();
        HSSFWorkbook wb = null;
        try {
            wb = new HSSFWorkbook(stream);
        } catch (IOException e) {
            throw new ServiceException(IO_WORKBOOK_EXCEPTION);
        }
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

            // парсим с 18 строки
            if (row.getRowNum() < 9) {
                continue;
            }

            boolean isValid = true;
            Income102 model = new Income102();
            while (cells.hasNext()) {
                if (!isValid)
                    break;

                int index = cells.next().getColumnIndex();
                Cell cell = row.getCell(index, Row.RETURN_BLANK_AS_NULL);

                if (cell == null) {
                    if (index == 3) {
                        isValid = false;
                    }
                    continue;
                }

                try {
                    // заполняем модель для вставки в БД
                    switch (cell.getColumnIndex()) {
                        case 2:
                            model.setItemName(cell.getStringCellValue());
                            break;
                        case 3:
                            //Пропускаем строки с "плохим" кодом
                            String opCode = cell.getStringCellValue().trim();
                            if (opCode == null || excludeCode.contains(opCode.trim()) || opCode.startsWith("0")) {
                                isValid = false;
                                break;
                            }
                            model.setOpuCode(opCode.trim());
                            break;
                        case 6:
                            model.setTotalSum(cell.getNumericCellValue());
                            break;
                    }
                } catch (IllegalStateException e) {
                    throw getServiceException(cell.getColumnIndex(), INCOME_102);
                }
            }
            endOfFile = isEndOfFile102(model);
            if (!endOfFile && isValid) {
                if (!isModelValid(model)) {
                    throw new ServiceException(BAD_FILE_MSG);
                }
                list.add(model);
            }
        }
        return list;
    }

    private boolean isEndOfFile102(Income102 model) {
        return model.getOpuCode() == null
                && model.getTotalSum() == null &&
                model.getItemName() == null;
    }

    /**
     * @param model проверяемая модель
     * @return true если ячейки в столбцах, указанные в описании формата, не пустые (нет ни одной пустой ячейки), иначе - false
     */
    private boolean isModelValid(Income101 model) {
        return model.getAccount() != null && !model.getAccount().isEmpty()
                && model.getIncomeDebetRemains() != null
                && model.getIncomeCreditRemains() != null
                && model.getDebetRate() != null
                && model.getCreditRate() != null
                && model.getOutcomeDebetRemains() != null
                && model.getOutcomeCreditRemains() != null
                && model.getAccountName() != null && !model.getAccountName().isEmpty();
    }

    /**
     * @param model проверяемая модель
     * @return true если ячейки в столбцах, указанные в описании формата, не пустые (нет ни одной пустой ячейки), иначе - false
     */
    private boolean isModelValid(Income102 model) {
        return model.getOpuCode() != null && !model.getOpuCode().isEmpty()
                && model.getTotalSum() != null
                && model.getItemName() != null && !model.getItemName().isEmpty();
    }

    /**
     * Если тип загружаемых данных не соответствует объявленным
     *
     * @param columnIndex индекс колонки (для определения текста ошибки)
     * @param typeID      тип бух отчетности
     */
    private ServiceException getServiceException(int columnIndex, long typeID) {
        String colName = "";
        if (typeID == INCOME_101) {
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
