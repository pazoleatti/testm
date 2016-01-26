/*
    blob_data.id = 'a396c647-2b0a-4124-9831-5b44e625e69e'
 */
package refbook.offshore

import au.com.bytecode.opencsv.CSVWriter
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.PagingParams
import com.aplana.sbrf.taxaccounting.service.impl.print.formdata.FormDataXlsmReportBuilder
import com.aplana.sbrf.taxaccounting.service.impl.print.formdata.XlsxReportMetadata
import groovy.transform.Field
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.util.ClassUtils

/**
 * Скрипт справочника «Оффшорные зоны» (id = 519)
 *
 * @author Lhaziev
 */
switch (formDataEvent) {
    case FormDataEvent.SAVE:
        save()
        break
    case FormDataEvent.GET_SPECIFIC_REPORT_TYPES:
        specificReportType.add("Краткий список ОЗ (CSV)")
        specificReportType.add("Краткий список ОЗ (XLSM)")
        break
    case FormDataEvent.CREATE_SPECIFIC_REPORT:
        createSpecificReport()
        break
}

@Field
def refBookCache = [:]

def getRecord(def refBookId, def recordId) {
    if (refBookCache[getRefBookCacheKey(refBookId, recordId)] != null) {
        return refBookCache[getRefBookCacheKey(refBookId, recordId)]
    } else {
        def provider = refBookFactory.getDataProvider(refBookId)
        def value = provider.getRecordData(recordId)
        refBookCache.put(getRefBookCacheKey(refBookId, recordId), value)
        return value
    }
}

void save() {
    saveRecords.each {
        def Long code = it.CODE?.referenceValue
        def Long offshoreCode = it.OFFSHORE_CODE?.referenceValue
        it.CODE_2.setValue(code)
        it.CODE_3.setValue(code)
        if (code != null) {
            def record = getRecord(10L, code)
            it.SHORTNAME.setValue(record?.NAME.stringValue)
            it.NAME.setValue(record?.FULLNAME.stringValue)
        }
        it.OFFSHORE_NAME.setValue(offshoreCode)
    }
}

void createSpecificReport() {
    switch (scriptSpecificReportHolder.getSpecificReportType()) {
        case 'Краткий список ОЗ (CSV)' :
            createSpecificReportShortListCSV()
            break
        case 'Краткий список ОЗ (XLSM)' :
            createSpecificReportShortListXLSM()
            break
    }
}

// Краткий список ОЗ (CSV)
def createSpecificReportShortListCSV() {
    def version = scriptSpecificReportHolder.version

    // получить данные
    def records = getRecords()

    // записать в файл
    PrintWriter printWriter = new PrintWriter(scriptSpecificReportHolder.getFileOutputStream())
    BufferedWriter bufferedWriter = new BufferedWriter(printWriter)
    CSVWriter csvWriter = new CSVWriter(bufferedWriter, (char) ';', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.NO_ESCAPE_CHARACTER)

    // заголовок
    def header = ['rowNumber', 'countryShortName', 'countryFullName', 'digitalCode', 'Alpha2', 'Alpha3', 'Comments']
    csvWriter.writeNext(header.toArray() as String[])

    // данные
    def values = []
    def index = 0
    records.each { record ->
        index++

        // rowNumber
        values.add(index.toString())

        // countryShortName
        values.add(record?.SHORTNAME?.value)

        // countryFullName
        values.add(record?.NAME?.value)

        // digitalCode
        def record10 = getRecord10(version, record?.CODE?.value)
        values.add(record10?.CODE?.value)

        // Alpha2
        record10 = getRecord10(version, record?.CODE_2?.value)
        values.add(record10?.CODE_2?.value)

        // Alpha3
        record10 = getRecord10(version, record?.CODE_3?.value)
        values.add(record10?.CODE_3?.value)

        // Comments
        values.add(record?.COMMENT?.value)

        // добавить значения
        csvWriter.writeNext(values.toArray() as String[])
        values.clear()
    }
    csvWriter.close()

    // название файла
    def periodCode = getPeriodCode(version)
    def year = version.format('yyyy')
    def fileName = "offshore" + periodCode + year + ".csv"
    scriptSpecificReportHolder.setFileName(fileName)
}

def getRecord10(def date, def recordId) {
    def records10 = getRecordsByRefbookId(10L, date)
    return records10?.find { it.record_id?.value == recordId }
}

@Field
def recordsMap = [:]

// Исользовать для небольших справочников
def getRecordsByRefbookId(long id, def date) {
    if (recordsMap[id] == null) {
        // получить записи из справончика
        def provider = refBookFactory.getDataProvider(10L)
        recordsMap[id] = provider.getRecords(date, null, null, null)
        if (recordsMap[id] == null) {
            recordsMap[id] = []
        }
    }
    return recordsMap[id]
}

/** Получить код периода по дате. */
def getPeriodCode(def date) {
    def provider8 = refBookFactory.getDataProvider(8L)
    def record8s = provider8.getRecords(date, null, "D = 1", null)
    def dayAndMonth = date.format('dd.MM')
    def tmpDate = Date.parse('dd.MM.yyyy', dayAndMonth + '.1970')
    def record8 = record8s.find { it?.CALENDAR_START_DATE?.value <= tmpDate && tmpDate <= it?.END_DATE?.value }
    def periodCode = record8?.CODE?.value

    // если не нашлось кода в справочнике
    if (!periodCode) {
        Calendar c = Calendar.getInstance()
        c.setTime(date)
        def month = c.get(Calendar.MONTH)
        if (month < 4) {
            periodCode = '21'
        } else if (month < 7) {
            periodCode = '31'
        } else if (month < 10) {
            periodCode = '33'
        } else {
            periodCode = '34'
        }
    }
    return periodCode
}

/** Получить отфильтрованные отсортированные данные для специфичных отчетов. */
def getRecords() {
    def version = scriptSpecificReportHolder.version
    def filter = scriptSpecificReportHolder.filter
    def sortAttribute = scriptSpecificReportHolder.sortAttribute
    def isSortAscending = scriptSpecificReportHolder.isSortAscending
    def provider = refBookFactory.getDataProvider(519L)
    // количество записей - необходимо для пейджинга, иначе без пейджинга сортировка в пустых сортируемых полях отличается
    def count = provider.getRecordsCount(version, filter)
    PagingParams pagingParams = new PagingParams()
    pagingParams.setStartIndex(1)
    pagingParams.setCount(count)
    // записи из бд
    return provider.getRecords(version, pagingParams, filter, sortAttribute, isSortAscending)
}

@Field
Workbook workBook = null
@Field
Sheet sheet = null

void createSpecificReportShortListXLSM() {
    def version = scriptSpecificReportHolder.version

    // получить данные
    def records = getRecords()

    // для работы с эксель
    String TEMPLATE = ClassUtils.classPackageAsResourcePath(FormDataXlsmReportBuilder.class)+ "/acctax.xlsm"
    InputStream templeteInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(TEMPLATE)
    workBook = WorkbookFactory.create(templeteInputStream)
    sheet = workBook.getSheetAt(0)
    def rowIndex = 0

    // очистить шаблон
    clearSheet()

    // заголовок
    def header = ['№', 'Краткое наименование', 'Полное наименование', 'Цифровой код', 'Буквенный код альфа-2', 'Буквенный код альфа-3', 'Справочно']
    addNewRowInXlsm(rowIndex, header, true)

    // задать ширину столбцов (в символах)
    def widths = [
            5,  // №
            37, // Краткое наименование
            37, // Полное наименование
            13, // Цифровой код
            13, // Буквенный код альфа-2
            13, // Буквенный код альфа-3
            57, // Справочно
    ]
    for (int i = 0; i < widths.size(); i++) {
        int width = widths[i] * 256 // умножить на 256, т.к. 1 единица ширины в poi = 1/256 ширины символа
        sheet.setColumnWidth(i, width)
    }

    // данные
    def values = []
    records.each { record ->
        rowIndex++

        // №
        values.add(rowIndex.toString())

        // Краткое наименование
        values.add(record?.SHORTNAME?.value)

        // Полное наименование
        values.add(record?.NAME?.value)

        // Цифровой код
        def record10 = getRecord10(version, record?.CODE?.value)
        values.add(record10?.CODE?.value)

        // Буквенный код альфа-2
        record10 = getRecord10(version, record?.CODE_2?.value)
        values.add(record10?.CODE_2?.value)

        // Буквенный код альфа-3
        record10 = getRecord10(version, record?.CODE_3?.value)
        values.add(record10?.CODE_3?.value)

        // Справочно
        values.add(record?.COMMENT?.value)

        // добавить значения
        addNewRowInXlsm(rowIndex, values)
        values.clear()
    }
    workBook.write(scriptSpecificReportHolder.getFileOutputStream())

    // название файла
    def periodCode = getPeriodCode(version)
    def year = version.format('yyyy')
    def fileName = "offshore" + periodCode + year + ".xlsm"
    scriptSpecificReportHolder.setFileName(fileName)
}

/**
 * Добавить новую строку в эксель и заполнить данными.
 *
 * @param sheet лист эксель
 * @param rowIndex номер строк (0..n)
 * @param values список строковых значении
 */
void addNewRowInXlsm(int rowIndex, def values, def isHeader = false) {
    Row newRow = sheet.createRow(rowIndex)
    def cellIndex = 0
    for (String value : values) {
        Cell cell = newRow.createCell(cellIndex)
        cell.setCellValue(value)

        // стили
        CellStyle cellStyle = getCellStyle(isHeader ? 'header' : 'data')
        cell.setCellStyle(cellStyle)
        cellIndex++
    }
}

/** Очистить шаблон, т.к в нем есть значения, поименованные ячейки, стили и т.д. */
void clearSheet() {
    // убрать объединения
    def count = sheet.getNumMergedRegions()
    for (int i = count - 1; i >= 0; i--) {
        sheet.removeMergedRegion(i)
    }

    // очистить ячейки
    def needAddRow = true
    int maxColumnNum = 0
    for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++) {
        Row row = sheet.getRow(i)
        if (!row) {
            continue
        }
        if (row.getLastCellNum() > maxColumnNum) {
            maxColumnNum = row.getLastCellNum()
        }
        sheet.removeRow(row)
        // добавить новую строку что б не удалился макрос после удаления всех строк
        if (needAddRow) {
            Row tmp = sheet.createRow(i)
            tmp.createCell(2).setCellValue(' ')
            needAddRow = false
        }
    }
    // поправить ширину столбцов
    int width = (sheet.getDefaultColumnWidth() + 1) * 256
    for (int i = 0; i <= maxColumnNum; i++) {
        sheet.setColumnWidth(i, width)
    }

    // удалить именованные ячейки
    def cellNames = [
            XlsxReportMetadata.RANGE_DATE_CREATE,
            XlsxReportMetadata.RANGE_REPORT_CODE,
            XlsxReportMetadata.RANGE_REPORT_PERIOD,
            XlsxReportMetadata.RANGE_REPORT_NAME,
            XlsxReportMetadata.RANGE_SUBDIVISION,
            // XlsxReportMetadata.RANGE_POSITION, // используется макросом
            XlsxReportMetadata.RANGE_SUBDIVISION_SIGN,
            XlsxReportMetadata.RANGE_FIO,
    ]
    cellNames.each { name ->
        workBook.removeName(name)
    }
}

@Field
def cellStyleMap = [:]

CellStyle getCellStyle(String alias) {
    if (cellStyleMap.containsKey(alias)) {
        return cellStyleMap.get(alias)
    }
    CellStyle style = workBook.createCellStyle()
    // рамки
    style.setBorderRight(CellStyle.BORDER_THIN)
    style.setBorderLeft(CellStyle.BORDER_THIN)
    style.setBorderBottom(CellStyle.BORDER_THIN)
    style.setBorderTop(CellStyle.BORDER_THIN)

    switch (alias) {
        case 'header' : // шапка
            style.setAlignment(CellStyle.ALIGN_CENTER)
            Font font = workBook.createFont()
            font.setBoldweight(Font.BOLDWEIGHT_BOLD)
            style.setFont(font)
            break
        case 'data' : // данные
            style.setAlignment(CellStyle.ALIGN_LEFT)
            break
    }
    style.setWrapText(true)

    cellStyleMap.put(alias, style)
    return style
}