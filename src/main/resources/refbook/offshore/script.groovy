/*
    blob_data.id = 'a396c647-2b0a-4124-9831-5b44e625e69e'
 */
package refbook.offshore

import au.com.bytecode.opencsv.CSVWriter
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.PagingParams
import groovy.transform.Field

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
    }
}

def createSpecificReportShortListCSV() {
    // получить данные
    def version = scriptSpecificReportHolder.version
    def filter = scriptSpecificReportHolder.filter
    def sortAttribute = scriptSpecificReportHolder.sortAttribute
    def isSortAscending = scriptSpecificReportHolder.isSortAscending
    def provider = refBookFactory.getDataProvider(519L)
    // количество записей - необходимо для пейджинга, иначе без пейджинга сортировка в пустых сортируемых полях отличается
    def count = provider.getRecordsCount(version, filter)
    PagingParams pagingParams = new PagingParams();
    pagingParams.setStartIndex(1);
    pagingParams.setCount(count);
    // записи из бд
    def records = provider.getRecords(version, pagingParams, filter, sortAttribute, isSortAscending)

    // записать в файл
    PrintWriter printWriter = new PrintWriter(scriptSpecificReportHolder.getFileOutputStream())
    BufferedWriter bufferedWriter = new BufferedWriter(printWriter)
    CSVWriter csvWriter = new CSVWriter(bufferedWriter, (char) ';', CSVWriter.NO_QUOTE_CHARACTER)

    // заголовок
    def header = ['rowNumber', 'countryShortName', 'countryFullName', 'digitalCode', 'Alpha2', 'Alpha3', 'Comments']
    csvWriter.writeNext(header.toArray() as String[])

    // данные
    def row = []
    def index = 0
    records.each { record ->
        index++

        // rowNumber
        row.add(index.toString())

        // countryShortName
        row.add(record?.SHORTNAME?.value)

        // countryFullName
        row.add(record?.NAME?.value)

        // digitalCode
        def record10 = getRecord10(version, record?.CODE?.value)
        row.add(record10?.CODE?.value)

        // Alpha2
        record10 = getRecord10(version, record?.CODE_2?.value)
        row.add(record10?.CODE_2?.value)

        // Alpha3
        record10 = getRecord10(version, record?.CODE_3?.value)
        row.add(record10?.CODE_3?.value)

        // Comments
        row.add(record?.COMMENT?.value)

        csvWriter.writeNext(row.toArray() as String[])
        row.clear()
    }
    csvWriter.close();

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

    // если не нашлось кода в справочике
    if (!periodCode) {
        Calendar c = Calendar.getInstance();
        c.setTime(version)
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