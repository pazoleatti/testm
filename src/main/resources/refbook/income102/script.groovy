package refbook.income102

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.Income102
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider
import groovy.transform.Field
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet

/**
 * Отчет о прибылях и убытках (Форма 0409102-СБ)
 * ref_book_id = 52
 */

switch (formDataEvent) {
    case FormDataEvent.IMPORT_TRANSPORT_FILE :
        importData()
        break
}

@Field
def REF_BOOK_ID = 52L

@Field
def BAD_FILE_MSG = 'Формат файла не соответствуют ожидаемому формату. Файл не может быть загружен.'
@Field
def INCORRECT_NAME_MSG = 'Выбранный файл не соответствует формату xls. Файл не может быть загружен.'

@Field
def ATTRIBUTE_ARTICLE_NAME = 'Наименование статей'
@Field
def ATTRIBUTE_SYMBOLS = 'Символы'
@Field
def ATTRIBUTE_TOTAL = 'Всего'

// импорт записей из экселя.
void importData() {
    if (fileName == null || !fileName.endsWith('xls')) {
        throw new ServiceException(INCORRECT_NAME_MSG)
    }
    List<Income102> list = importIncome102(inputStream)
    if (list != null && !list.isEmpty()) {
        List<Map<String, RefBookValue>> records = new LinkedList<Map<String, RefBookValue>>()
        for (Income102 item : list) {
            Map<String, RefBookValue> map = new HashMap<String, RefBookValue>()
            map.put('OPU_CODE', new RefBookValue(RefBookAttributeType.STRING, item.getOpuCode()))
            map.put('TOTAL_SUM', new RefBookValue(RefBookAttributeType.NUMBER, item.getTotalSum()))
            map.put('ITEM_NAME', new RefBookValue(RefBookAttributeType.STRING, item.getItemName()))
            map.put('ACCOUNT_PERIOD_ID', new RefBookValue(RefBookAttributeType.REFERENCE, accountPeriodId.longValue()))
            records.add(map)
        }
        RefBook refBook = refBookFactory.get(REF_BOOK_ID)
        RefBookDataProvider provider = refBookFactory.getDataProvider(REF_BOOK_ID)
        List<String> matchedRecords = provider.getMatchedRecords(refBook.getAttributes(), records, accountPeriodId)
        if (matchedRecords.size() > 0) {
            logger.error("Нарушена уникальность кодов ОПУ. Файл не может быть загружен.")
            logger.error("Следующие коды ОПУ указаны в форме более одного раза:")
            logger.error(matchedRecords.join(', '))
            return
        }
        for (Income102 item : list) {
            if (item.getItemName() != null && item.getItemName().length() > 255) {
                logger.error("В строке с \"Символ = %s\" превышена максимальная длина строки значения поля  \"Наименование статьи\"!", item.getOpuCode())
            }
        }
        if (!logger.containsLevel(LogLevel.ERROR)) {
            provider.updateRecords(null, null, records)
        }
    } else {
        throw new ServiceException('Файл не содержит данных. Файл не может быть загружен.')
    }
}

List<Income102> importIncome102(InputStream stream) {
    // строки со следующими кодами игнорируем
    def excludeCode = [ '', '0']

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
    def endOfFile = false
    while (rows.hasNext() && !endOfFile) {
        Row row = rows.next()
        Iterator<Cell> cells = row.iterator()

        // проверка шапки таблицы (строка 10)
        if (row.getRowNum() == 9) {
            while (cells.hasNext()) {
                Cell cell = cells.next()
                if (cell.getCellType() != Cell.CELL_TYPE_STRING && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
                    throw new ServiceException(BAD_FILE_MSG)
                }
                int colNum = cell.getColumnIndex()
                String colName = cell.getStringCellValue().trim()
                if ((colNum == 2 && !ATTRIBUTE_ARTICLE_NAME.equals(colName)) ||
                        (colNum == 3 && !ATTRIBUTE_SYMBOLS.equals(colName)) ||
                        (colNum == 6 && !ATTRIBUTE_TOTAL.equals(colName))) {
                    throw new ServiceException(BAD_FILE_MSG)
                }
            }
        }

        // данные начинаются с 17 строки
        if (row.getRowNum() < 16) {
            continue
        }

        boolean isValid = true
        Income102 model = new Income102()
        while (cells.hasNext()) {
            if (!isValid) {
                break
            }
            int index = cells.next().getColumnIndex()
            Cell cell = row.getCell(index, Row.RETURN_BLANK_AS_NULL)
            if (cell == null) {
                if (index == 3) {
                    isValid = false
                }
                continue
            }
            try {
                // заполняем модель для вставки в БД
                switch (cell.getColumnIndex()) {
                    case 2 :
                        model.setItemName(cell.getStringCellValue())
                        break
                    case 3 :
                        // пропуск строки с "плохим" кодом
                        String opCode = cell.getStringCellValue().trim()
                        if (opCode == null || excludeCode.contains(opCode.trim())) {
                            isValid = false
                            break
                        }
                        model.setOpuCode(opCode.trim())
                        break
                    case 6 :
                        model.setTotalSum(cell.getNumericCellValue())
                        break
                }
            } catch (IllegalStateException e) {
                throw getServiceException(cell.getColumnIndex())
            }
        }
        endOfFile = isEndOfFile102(model)
        if (!endOfFile && isValid) {
            if (!isModelValid(model)) {
                throw new ServiceException(BAD_FILE_MSG)
            }
            result.add(model)
        }
    }
    return result
}

boolean isEndOfFile102(Income102 model) {
    return model.getOpuCode() == null && model.getTotalSum() == null && model.getItemName() == null
}

/**
 * Проверить строку данных.
 *
 * @return true - если ячейки не пустые (нет ни одной пустой ячейки), иначе - false
 */
boolean isModelValid(Income102 model) {
    return model.getOpuCode() != null && !model.getOpuCode().isEmpty() &&
            model.getTotalSum() != null &&
            model.getItemName() != null && !model.getItemName().isEmpty()
}

/**
 * Если тип загружаемых данных не соответствует объявленным.
 *
 * @param columnIndex индекс колонки (для определения текста ошибки)
 */
ServiceException getServiceException(int columnIndex) {
    String colName = ''
    switch (columnIndex) {
        case 2 :
            colName = ATTRIBUTE_ARTICLE_NAME
            break
        case 3 :
            colName = ATTRIBUTE_SYMBOLS
            break
        case 6 :
            colName = ATTRIBUTE_TOTAL
            break
    }
    return new ServiceException("Данные столбца '$colName' файла не соответствуют ожидаемому типу данных. Файл не может быть загружен.")
}