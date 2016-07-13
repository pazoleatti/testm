package refbook.income101

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.Income101
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider
import groovy.transform.Field
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet

/**
 * Оборотная ведомость (Форма 0409101-СБ)
 * ref_book_id = 50
 */

switch (formDataEvent) {
    case FormDataEvent.IMPORT_TRANSPORT_FILE :
        importData()
        break
}

@Field
def REF_BOOK_ID = 50L

@Field
def BAD_FILE_MSG = 'Формат файла не соответствуют ожидаемому формату. Файл не может быть загружен.'
@Field
def INCORRECT_NAME_MSG = 'Выбранный файл не соответствует формату xls. Файл не может быть загружен.'

@Field
def ATTRIBUTE_ACCOUNT_NO = 'Номер счета'
@Field
def ATTRIBUTE_NAME = 'Название'
@Field
def ATTRIBUTE_INCOME_REMAINS = 'Входящие остатки'
@Field
def ATTRIBUTE_REPORT_PERIOD_TURN = 'Обороты за отчетный период'
@Field
def ATTRIBUTE_OUTCOME_REMAINS = 'Исходящие остатки'
@Field
def ON_DEBET = 'по дебету'
@Field
def ON_CREDIT = 'по кредиту'
@Field
def ATTRIBUTE_INCOME_REMAINS_ON_DEBET = 'Входящие остатки по дебету'
@Field
def ATTRIBUTE_INCOME_REMAINS_ON_CREDIT = 'Входящие остатки по кредиту'
@Field
def ATTRIBUTE_REPORT_PERIOD_TURN_ON_DEBET = 'Обороты за отчетный период по дебету'
@Field
def ATTRIBUTE_REPORT_PERIOD_TURN_ON_CREDIT = 'Обороты за отчетный период по кредиту'
@Field
def ATTRIBUTE_OUTCOME_REMAINS_ON_DEBET = 'Исходящие остатки по дебету'
@Field
def ATTRIBUTE_OUTCOME_REMAINS_ON_CREDIT = 'Исходящие остатки по кредиту'

// импорт записей из экселя.
void importData() {
    if (fileName == null || !fileName.endsWith('xls')) {
        throw new ServiceException(INCORRECT_NAME_MSG)
    }
    List<Income101> list = importIncome101(inputStream)
    if (list != null && !list.isEmpty()) {
        List<Map<String, RefBookValue>> records = new LinkedList<Map<String, RefBookValue>>()
        for (Income101 item : list) {
            if (item.getAccountName() != null && item.getAccountName().length() > 255) {
                logger.error("В строке с \"Номер счета = %s\" превышена максимальная длина строки значения поля  \"Название счета\"!", item.getAccount())
            }
            Map<String, RefBookValue> map = new HashMap<String, RefBookValue>()
            map.put('ACCOUNT', new RefBookValue(RefBookAttributeType.STRING, item.getAccount()))
            map.put('ACCOUNT_NAME', new RefBookValue(RefBookAttributeType.STRING, item.getAccountName()))
            map.put('INCOME_DEBET_REMAINS', new RefBookValue(RefBookAttributeType.NUMBER, item.getIncomeDebetRemains()))
            map.put('INCOME_CREDIT_REMAINS', new RefBookValue(RefBookAttributeType.NUMBER, item.getIncomeCreditRemains()))
            map.put('DEBET_RATE', new RefBookValue(RefBookAttributeType.NUMBER, item.getDebetRate()))
            map.put('CREDIT_RATE', new RefBookValue(RefBookAttributeType.NUMBER, item.getCreditRate()))
            map.put('OUTCOME_DEBET_REMAINS', new RefBookValue(RefBookAttributeType.NUMBER, item.getOutcomeDebetRemains()))
            map.put('OUTCOME_CREDIT_REMAINS', new RefBookValue(RefBookAttributeType.NUMBER, item.getOutcomeCreditRemains()))
            map.put('ACCOUNT_PERIOD_ID', new RefBookValue(RefBookAttributeType.REFERENCE, accountPeriodId.longValue()))
            records.add(map)
        }
        RefBookDataProvider provider = refBookFactory.getDataProvider(REF_BOOK_ID)
        if (!logger.containsLevel(LogLevel.ERROR)) {
            provider.updateRecords(null, null, records)
        }
    } else {
        throw new ServiceException('Файл не содержит данных. Файл не может быть загружен.')
    }
}

List<Income101> importIncome101(InputStream stream) {
    // выходной лист с моделями для записи в бд
    def result = []
    HSSFWorkbook workbook
    try {
        workbook = new HSSFWorkbook(stream)
    } catch (IOException e) {
        throw new ServiceException(BAD_FILE_MSG)
    }
    Sheet sheet = workbook.getSheetAt(0)
    Iterator<Row> rows = sheet.iterator()
    boolean endOfFile = false
    boolean hasHeader = false
    while (rows.hasNext() && !endOfFile) {
        Row row = rows.next()
        Iterator<Cell> cells = row.iterator()

        // проверка шапки таблицы (строка 10)
        if (row.getRowNum() == 9) {
            while (cells.hasNext()) {
                Cell cell = cells.next()
                if ((cell.getCellType() != Cell.CELL_TYPE_STRING) &&
                        (cell.getCellType() != Cell.CELL_TYPE_BLANK)) {
                    throw new ServiceException(BAD_FILE_MSG)
                }
                int colNum = cell.getColumnIndex()
                String colName = cell.getStringCellValue().trim()
                if ((colNum == 1 && !ATTRIBUTE_ACCOUNT_NO.equals(colName)) ||
                        (colNum == 2 && !ATTRIBUTE_NAME.equals(colName)) ||
                        (colNum == 4 && !ATTRIBUTE_INCOME_REMAINS.equals(colName)) ||
                        (colNum == 6 && !ATTRIBUTE_REPORT_PERIOD_TURN.equals(colName)) ||
                        (colNum == 8 && !ATTRIBUTE_OUTCOME_REMAINS.equals(colName))) {
                    throw new ServiceException(BAD_FILE_MSG)
                }
            }
        }
        // проверка шапки таблицы (строка 12)
        if (row.getRowNum() == 11) {
            hasHeader = true
            while (cells.hasNext()) {
                Cell cell = cells.next()
                int colNum = cell.getColumnIndex()
                String colName = cell.getStringCellValue().trim()
                if (((colNum == 4 || colNum == 6 || colNum == 8) && !ON_DEBET.equals(colName)) ||
                        ((colNum == 5 || colNum == 7 || colNum == 9) && !ON_CREDIT.equals(colName))) {
                    throw new ServiceException(BAD_FILE_MSG)
                }
            }
        }

        // данные начинаются с 18 строки
        if (row.getRowNum() < 18) {
            continue
        }
        // парсим каждую третью строку
        if (row.getRowNum() % 3 == 0) {
            boolean isValid = true
            Income101 model = new Income101()
            endOfFile = true
            while (cells.hasNext()) {
                if (!isValid) {
                    break
                }
                Cell cell = row.getCell(cells.next().getColumnIndex(), Row.RETURN_BLANK_AS_NULL)
                // первая ячейка не должна быть пустой
                if (cell == null) {
                    throw new ServiceException(BAD_FILE_MSG)
                }
                if (cell.getColumnIndex() > 1 && endOfFile) {
                    break
                }
                try {
                    // заполняем модель для вставки в БД
                    switch (cell.getColumnIndex()) {
                        case 1 :
                            endOfFile = false
                            // игнорируем строки не соотнесённые с номерами счетов (разделы, главы)
                            String account = cell.getStringCellValue()
                            if (!account?.matches("[0-9.]+")) {
                                isValid = false
                            }
                            model.setAccount(cell.getStringCellValue())
                            break
                        case 2 :
                            model.setAccountName(cell.getStringCellValue())
                            break
                        case 4 :
                            model.setIncomeDebetRemains(cell.getNumericCellValue())
                            break
                        case 5 :
                            model.setIncomeCreditRemains(cell.getNumericCellValue())
                            break
                        case 6 :
                            model.setDebetRate(cell.getNumericCellValue())
                            break
                        case 7 :
                            model.setCreditRate(cell.getNumericCellValue())
                            break
                        case 8 :
                            model.setOutcomeDebetRemains(cell.getNumericCellValue())
                            break
                        case 9 :
                            model.setOutcomeCreditRemains(cell.getNumericCellValue())
                            break
                    }
                } catch (IllegalStateException e) {
                    throw getServiceException(cell.getColumnIndex())
                }
            }
            if (!endOfFile && isValid) {
                if (!isModelValid(model)) {
                    throw new ServiceException(BAD_FILE_MSG)
                }
                result.add(model)
            }
        }
    }
    if (!hasHeader) {
        throw new ServiceException(BAD_FILE_MSG)
    }
    return result
}

/**
 * Проверить строку данных.
 *
 * @return true - если ячейки не пустые (нет ни одной пустой ячейки), иначе - false
 */
boolean isModelValid(Income101 model) {
    return model.getAccount() != null && !model.getAccount().isEmpty() &&
            model.getIncomeDebetRemains() != null &&
            model.getIncomeCreditRemains() != null &&
            model.getDebetRate() != null &&
            model.getCreditRate() != null &&
            model.getOutcomeDebetRemains() != null &&
            model.getOutcomeCreditRemains() != null &&
            model.getAccountName() != null && !model.getAccountName().isEmpty()
}

/**
 * Если тип загружаемых данных не соответствует объявленным.
 *
 * @param columnIndex индекс колонки (для определения текста ошибки)
 */
ServiceException getServiceException(int columnIndex) {
    String colName = ''
    switch (columnIndex) {
        case 1 :
            colName = ATTRIBUTE_ACCOUNT_NO
            break
        case 2 :
            colName = ATTRIBUTE_NAME
            break
        case 4 :
            colName = ATTRIBUTE_INCOME_REMAINS_ON_DEBET
            break
        case 5 :
            colName = ATTRIBUTE_INCOME_REMAINS_ON_CREDIT
            break
        case 6 :
            colName = ATTRIBUTE_REPORT_PERIOD_TURN_ON_DEBET
            break
        case 7 :
            colName = ATTRIBUTE_REPORT_PERIOD_TURN_ON_CREDIT
            break
        case 8 :
            colName = ATTRIBUTE_OUTCOME_REMAINS_ON_DEBET
            break
        case 9 :
            colName = ATTRIBUTE_OUTCOME_REMAINS_ON_CREDIT
            break
    }
    return new ServiceException("Данные столбца '$colName' файла не соответствуют ожидаемому типу данных. Файл не может быть загружен.")
}