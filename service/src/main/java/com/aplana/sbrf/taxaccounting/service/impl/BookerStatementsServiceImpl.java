package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.BookerStatementsDao;
import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.Income101;
import com.aplana.sbrf.taxaccounting.model.Income102;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.BookerStatementsService;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
    @Autowired
    ReportPeriodDao reportPeriodDao;
    @Autowired
    BookerStatementsDao bookerStatementsDao;

    @Override
    public void importXML(InputStream stream, Integer periodID, int typeID) throws IOException {
        // Проверка того, что пользователем указан открытый отчетный период
        if (!reportPeriodDao.get(periodID).isActive()) {
            throw new ServiceException("Указан закрытый период. Файл не может быть загружен.");
        }

        // TODO SBRFACCTAX-3451
        if (typeID == 1) {
            List<Income101> list = importIncome101(stream);
            // bookerStatementsDao.create101(list, periodID);
        } else {
            List<Income102> list = importIncome102(stream);
            // bookerStatementsDao.create102(list, periodID);
        }
    }

    private List<Income101> importIncome101(InputStream stream) throws IOException {
        String errMsg = "Формат файла не соответствуют ожидаемому формату. Файл не может быть загружен.";
        List<Income101> list = new ArrayList<Income101>();
        HSSFWorkbook wb = new HSSFWorkbook(stream);
        Sheet sheet = wb.getSheetAt(0);
        Iterator<Row> it = sheet.iterator();
        while (it.hasNext()) {
            Row row = it.next();
            Iterator<Cell> cells = row.iterator();

            //debug
            if (row.getRowNum() > 100)
                break;

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
                        throw new ServiceException(errMsg);
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
                        throw new ServiceException(errMsg);
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
                while (cells.hasNext()) {
                    if (!isValid)
                        break;
                    Cell cell = cells.next();
                    try {
                        // заполняем модель для вставки в БД
                        switch (cell.getColumnIndex()) {
                            case 1:
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
                        throw getServiceException(cell.getColumnIndex(), 1);
                    }
                }
                if (isValid) {
                    if (!isModelValid(model)) {
                        throw new ServiceException(errMsg);
                    }
                    list.add(model);
                }
            }
        }

        //debug
        System.out.println("list.size() = " + list.size());
        for (Income101 m : list) {
            System.out.println(m.getAccount());
            System.out.println(m.getIncomeDebetRemains());
            System.out.println(m.getIncomeCreditRemains());
            System.out.println(m.getDebetRate());
            System.out.println(m.getCreditRate());
            System.out.println(m.getOutcomeCreditRemains());
            System.out.println(m.getOutcomeDebetRemains());
            System.out.println(m.getAccountName());
            System.out.println("----------------------------");
        }
        return list;
    }

    private List<Income102> importIncome102(InputStream stream) throws IOException {
        List<Income102> list = new ArrayList<Income102>();
        HSSFWorkbook wb = new HSSFWorkbook(stream);
        Sheet sheet = wb.getSheetAt(0);
        Iterator<Row> it = sheet.iterator();
        while (it.hasNext()) {
            Row row = it.next();
            Iterator<Cell> cells = row.iterator();

            //debug
            if (row.getRowNum() > 100)
                break;

            // парсим с 18 строки
            if (row.getRowNum() < 9) {
                continue;
            }
            // TODO какие строки парсим (SBRFACCTAX-3444)
            if (row.getRowNum() % 3 == 0) {
                boolean isValid = true;
                Income102 model = new Income102();
                while (cells.hasNext()) {
                    if (!isValid)
                        break;
                    Cell cell = cells.next();
                    try {
                        // заполняем модель для вставки в БД
                        switch (cell.getColumnIndex()) {
                            case 2:
                                // TODO SBRFACCTAX-3444
                                // model.setItemName(cell.getStringCellValue());
                                break;
                            case 3:
                                model.setOpuCode(cell.getStringCellValue());
                                break;
                            case 6:
                                model.setTotalSum(cell.getNumericCellValue());
                                break;
                        }
                    } catch (IllegalStateException e) {
                        throw getServiceException(cell.getColumnIndex(), 1);
                    }
                }
                if (isValid) {
                    if (!isModelValid(model)) {
                        throw new ServiceException("Формат файла не соответствуют ожидаемому формату. Файл не может быть загружен.");
                    }
                    list.add(model);
                }
            }
        }

        //debug
        System.out.println("list.size() = " + list.size());
        for (Income102 m : list) {
            System.out.println(m.getOpuCode());
            System.out.println(m.getTotalSum());
            System.out.println(m.getItemName());
            System.out.println("----------------------------");
        }
        return list;
    }

    /**
     * @param model проверяемая модель
     * @return true если ячейки в столбцах, указанные в описании формата, не пустые (нет ни одной пустой ячейки), иначе - false
     */
    private boolean isModelValid(Income101 model) {
        return model.getAccount() != null
                && model.getIncomeDebetRemains() != null
                && model.getIncomeCreditRemains() != null
                && model.getDebetRate() != null
                && model.getCreditRate() != null
                && model.getOutcomeDebetRemains() != null
                && model.getOutcomeCreditRemains() != null
                && model.getAccountName() != null;
    }

    /**
     * @param model проверяемая модель
     * @return true если ячейки в столбцах, указанные в описании формата, не пустые (нет ни одной пустой ячейки), иначе - false
     */
    private boolean isModelValid(Income102 model) {
        return model.getOpuCode() != null
                && model.getTotalSum() != null;
        // && model.getItemName() != null;
    }

    /**
     * Если тип загружаемых данных не соответствует объявленным
     *
     * @param columnIndex индекс колонки (для определения текста ошибки)
     * @param typeID      тип бух отчетности
     */
    private ServiceException getServiceException(int columnIndex, int typeID) {
        String colName = "";
        if (typeID == 1) {
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
        return new ServiceException("Данные столбца " + colName + " файла не соответствуют ожидаемому типу данных. Файл не может быть загружен.");
    }
}
