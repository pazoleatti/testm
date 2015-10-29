package refbook.metal_rate

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.ScriptStatus
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import com.aplana.sbrf.taxaccounting.model.log.Logger
import groovy.transform.Field

import javax.xml.namespace.QName
import javax.xml.stream.XMLInputFactory
import java.text.SimpleDateFormat

/**
 * blob_data.id = '99e90406-60f0-4a87-b6f0-7f127abf1182'
 *
 * Скрипт загрузки справочника "Курсы драгоценных металлов" из КСШ  (id = 90)
 *
 * @author Stanislav Yasinskiy
 */
switch (formDataEvent) {
    case FormDataEvent.IMPORT:
        importFromXML()
        break
}

@Field
def REFBOOK_ID = 90

@Field
def ATTR_LIST = "\"Дата начала актуальности\" = %s, \"Внутренний код\" = %s, \"Курс драгоценного металла (руб. за 1 грамм)\" = %s"

@Field
def EMPTY_DATA_ERROR = "Сообщение не содержит значений, соответствующих загружаемым данным!"

@Field
def recordCache = [:]
@Field
def refBookCache = [:]

@Field
def sdf = new SimpleDateFormat('dd.MM.yyyy')

/**
 * Аналог FormDataServiceImpl.getRefBookRecord(...) но ожидающий получения из справочника больше одной записи.
 * @return первая из найденных записей
 */
def getRecord(def refBookId, def filter, Date date) {
    if (refBookId == null) {
        return null
    }
    String dateStr = sdf.format(date)
    if (recordCache.containsKey(refBookId)) {
        Long recordId = recordCache.get(refBookId).get(dateStr + filter)
        if (recordId != null) {
            if (refBookCache != null) {
                def key = getRefBookCacheKey(refBookId, recordId)
                return refBookCache.get(key)
            } else {
                def retVal = new HashMap<String, RefBookValue>()
                retVal.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, recordId))
                return retVal
            }
        }
    } else {
        recordCache.put(refBookId, [:])
    }

    def provider = refBookFactory.getDataProvider(refBookId)

    def records = provider.getRecords(date, null, filter, null)
    // отличие от FormDataServiceImpl.getRefBookRecord(...)
    if (records.size() > 0) {
        def retVal = records.get(0)
        Long recordId = retVal.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue()
        recordCache.get(refBookId).put(dateStr + filter, recordId)
        if (refBookCache != null) {
            def key = getRefBookCacheKey(refBookId, recordId)
            refBookCache.put(key, retVal)
        }
        return retVal
    }
    return null
}

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

void importFromXML() {
    def dataProvider = refBookFactory.getDataProvider(REFBOOK_ID)
    def SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd")
    def reader = null
    def Date version = null  //дата актуальности
    def boolean rateSector = false // флаг присутствия в секции с курсами
    def Map<String, RefBookValue> recordsMap // аттрибут и его значение
    def List<Map<String, RefBookValue>> insertList = new ArrayList<Map<String, RefBookValue>>() // Новые элементы
    def List<Map<String, RefBookValue>> updateList = new ArrayList<Map<String, RefBookValue>>() // Измененные элементы
    def String code = null // код драг. металла
    def BigDecimal rate = null // курс драг. металла
    int count = 0
    Logger logNoRecordInRefbookList = new Logger()
    Logger logExistList = new Logger()
    Logger logUpdateList = new Logger()
    Logger logCreateList = new Logger()

    def fileRecords = []
    try {
        try {
            def XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE)
            factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE)
            reader = factory.createXMLStreamReader(inputStream)

            while (reader.hasNext()) {
                if (reader.startElement) {

                    // Версия справочника
                    if (reader.getName().equals(QName.valueOf("StartDateTime"))) {
                        version = sdf.parse(reader.getElementText())
                    }

                    // Дошли до секции с курсами
                    if (reader.getName().equals(QName.valueOf("PreciousMetalRate"))) {
                        rateSector = true
                    }

                    // Код драг. металла
                    if (rateSector && reader.getName().equals(QName.valueOf("Code"))) {
                        code = reader.getElementText()
                    }

                    // Курс драг. металла
                    if (rateSector && reader.getName().equals(QName.valueOf("Rate"))) {
                        rate = new BigDecimal(reader.getElementText())
                    }
                }

                // Запись в лист
                if (reader.endElement && reader.getName().equals(QName.valueOf("PreciousMetalRate"))) {
                    count++
                    def record = getRecord(17, "LOWER(INNER_CODE) = LOWER('$code')", version)
                    if (record != null) {
                        recordsMap = new HashMap<String, RefBookValue>()
                        recordsMap.put("CODE", new RefBookValue(RefBookAttributeType.REFERENCE, record.record_id.numberValue))
                        recordsMap.put("RATE", new RefBookValue(RefBookAttributeType.NUMBER, rate))
                        fileRecords.add(recordsMap)
                    } else {
                        logNoRecordInRefbookList.warn(ATTR_LIST, sdf.format(version), code, rate)
                    }
                    rateSector = false
                    rate = null
                    code = null
                }
                reader.next()
            }
        } finally {
            reader?.close()
        }

        scriptStatusHolder.setTotalCount(count)
        if (fileRecords.empty) {
            logger.warn(EMPTY_DATA_ERROR)
            return
        }

        // Получение идентификаторов строк
        def filterStr = ''
        fileRecords.each { record ->
            filterStr += ((record == fileRecords.getAt(0)) ? "" : " or ") + " CODE = " + record.CODE.referenceValue
        }

        def recordIds = dataProvider.getUniqueRecordIds(null, filterStr)

        // Получение записей
        def existRecords = [:]
        if (recordIds != null && !recordIds.empty) {
            def size = recordIds.size()
            def delta = 1000
            for (int start = 0; start < size; start += delta) {
                def end = ((start + delta > size) ? size : (start + delta)) - 1
                def subExistRecords = dataProvider.getRecordData(recordIds[start..end])
                existRecords.putAll(subExistRecords)
            }
        }

        // CODE → Запись
        def existMap = [:]
        existRecords.each { key, record ->
            existMap.put(record.CODE.referenceValue, record)
        }

        fileRecords.each { record ->
            def existRecord = existMap[record.CODE.referenceValue]
            if (existRecord != null) {
                if ((existRecord["RATE"].getNumberValue() - record["RATE"].getNumberValue()) != 0) {
                    record.put(RefBook.RECORD_ID_ALIAS, existRecord[RefBook.RECORD_ID_ALIAS])
                    updateList.add(record)
                    logUpdateList.info(ATTR_LIST + "(предыдущее значение %s)", sdf.format(version), getRecord(17, record.CODE.referenceValue)["INNER_CODE"], record["RATE"], existRecord["RATE"])
                } else {
                    logExistList.warn(ATTR_LIST, sdf.format(version), getRecord(17, record.CODE.referenceValue)["INNER_CODE"], record["RATE"])
                }
            } else {
                insertList.add(record)
                logCreateList.info(ATTR_LIST, sdf.format(version), getRecord(17, record.CODE.referenceValue)["INNER_CODE"], record["RATE"])
            }
        }
    } finally {
        if (!logNoRecordInRefbookList.getEntries().isEmpty()) {
            logger.warn("Не созданы следующие записи справочника (в справочнике \"Коды драгоценных металлов\" на дату актуальности отсутствует элемент с кодом):")
            logger.getEntries().addAll(logNoRecordInRefbookList.getEntries())
        }
        if (!logExistList.getEntries().isEmpty()) {
            logger.warn("Не созданы следующие записи справочника (уже существуют):")
            logger.getEntries().addAll(logExistList.getEntries())
        }
        if (!logUpdateList.getEntries().isEmpty()) {
            logger.info("Обновлено значение курса по следующим записям справочника:")
            logger.getEntries().addAll(logUpdateList.getEntries())
        }
        if (!logCreateList.getEntries().isEmpty()) {
            logger.info("Созданы следующие записи справочника:")
            logger.getEntries().addAll(logCreateList.getEntries())
        }
    }

    if (!insertList.empty) {
        dataProvider.insertRecordsWithoutLock(userInfo, version, insertList)
    }
    if (!updateList.empty) {
        dataProvider.updateRecordsWithoutLock(userInfo, version, updateList)
    }
    scriptStatusHolder.setSuccessCount(insertList.size() + updateList.size())
}