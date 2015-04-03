package refbook.currency_rate

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import com.aplana.sbrf.taxaccounting.model.util.Pair
import groovy.transform.Field

import javax.xml.namespace.QName
import javax.xml.stream.XMLInputFactory
import java.text.SimpleDateFormat

/**
 * blob_data.id = '99e90406-60f0-4a87-b6f0-7f127abf1fbb'
 *
 * Cкрипт справочника "Курсы валют" из КСШ (id = 22)
 *
 * @author Stanislav Yasinskiy
 */
switch (formDataEvent) {
    case FormDataEvent.IMPORT:
        importFromXML()
        break
}

@Field
int REFBOOK_ID = 22

@Field
def EMPTY_DATA_ERROR = "Сообщение не содержит значений, соответствующих загружаемым данным!"

//// Кэши и константы
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

void importFromXML() {
    def dataProvider = refBookFactory.getDataProvider(REFBOOK_ID)
    def SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd")
    def reader = null
    def Date version = null  // дата актуальности
    def boolean currencySector = false // флаг присутствия в секции с курсами
    def Map<String, RefBookValue> recordsMap // аттрибут и его значение
    def List<Map<String, RefBookValue>> insertList = new ArrayList<Map<String, RefBookValue>>() // Новые элементы
    def List<Map<String, RefBookValue>> updateList = new ArrayList<Map<String, RefBookValue>>() // Измененные элементы
    def Long code = null // код валюты
    def BigDecimal rate = null // курс валюты
    def BigDecimal lotSize = 1 // Атрибут LotSize
    // def Map<String, Number> codeToRecordId = new HashMap<String, Number>() // Код → Id элемента

    def fileRecords = []
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
                if (reader.getName().equals(QName.valueOf("CurrencyRates"))) {
                    currencySector = true
                }

                // Код валюты
                if (currencySector && reader.getName().equals(QName.valueOf("Code"))) {
                    def String val = reader.getElementText()
                    def record = getRecord(15, "LOWER(CODE) = LOWER('$val')", version)
                    if (record != null) {
                        code = record.record_id.numberValue
                    } else {
                        code = null
                        logger.warn("В справочнике «Общероссийский классификатор валют» отсутствует элемент с кодом '$val'")
                    }
                }

                // LotSize
                if (currencySector && reader.getName().equals(QName.valueOf("LotSize"))) {
                    def String val = reader.getElementText()?.trim()
                    def tmp = val.replaceAll(",", ".").replace(" ", "")
                    if (tmp.matches("-?\\d+(\\.\\d+)?")) {
                        lotSize = new BigDecimal(tmp)
                    } else {
                        logger.warn("Ошибка получения значения атрибута «LotSize», равного \"$tmp\"")
                    }
                }

                // Курс валюты
                if (currencySector && reader.getName().equals(QName.valueOf("Rate"))) {
                    rate = new BigDecimal(reader.getElementText())
                }
            }

            // Запись в лист
            if (reader.endElement && reader.getName().equals(QName.valueOf("CurrencyRate")) && code != null) {
                recordsMap = new HashMap<String, RefBookValue>()
                recordsMap.put("CODE_NUMBER", new RefBookValue(RefBookAttributeType.REFERENCE, code))
                recordsMap.put("NAME", new RefBookValue(RefBookAttributeType.REFERENCE, code))
                recordsMap.put("CODE_LETTER", new RefBookValue(RefBookAttributeType.REFERENCE, code))
                recordsMap.put("RATE", new RefBookValue(RefBookAttributeType.NUMBER, rate / lotSize))
                fileRecords.add(recordsMap)
                lotSize = 1
            }
            reader.next()
        }
    } finally {
        reader?.close()
    }

    if (fileRecords.empty) {
        logger.warn(EMPTY_DATA_ERROR)
        return
    }

    // Получение идентификаторов строк
    StringBuilder filterVersion = new StringBuilder("")
    fileRecords.eachWithIndex { record, index ->
        filterVersion.append((index == 0) ? "" : " or ").append(" CODE_NUMBER = ").append(record.CODE_NUMBER.referenceValue)
    }

    //Получаем те записи, актуальные на дату version
    def recordIds = dataProvider.getUniqueRecordIds(version, filterVersion.toString())

    // Получение записей
    def existRecords = [:]
    if (recordIds != null && !recordIds.empty) {
        existRecords = dataProvider.getRecordData(recordIds)
    }

    // CODE_NUMBER → Запись
    def existMap = [:]
    existRecords.each { key, record ->
        existMap.put(record.CODE_NUMBER.referenceValue, record)
    }

    // на невероятный случай загрузки курса до первой версии, чтобы корректно нашел запись
    def preVersionFileRecords = fileRecords.findAll { record ->
        existMap[record.CODE_NUMBER.referenceValue] == null
    }

    if (preVersionFileRecords != null && !preVersionFileRecords.isEmpty()) {
        def filterPreVersion = new StringBuilder("")
        preVersionFileRecords.eachWithIndex { record, index ->
            filterPreVersion.append((index == 0) ? "" : " or ").append(" CODE_NUMBER = ").append(record.CODE_NUMBER.referenceValue)
        }
        // ищем без привязки к версии
        def preVersionRecordIds = dataProvider.getUniqueRecordIds(null, filterPreVersion.toString())

        def preVersionRecords = [:]
        if (preVersionRecordIds != null && !preVersionRecordIds.empty) {
            preVersionRecords = dataProvider.getRecordData(preVersionRecordIds)
        }
        preVersionRecords.each { key, record ->
            existMap.put(record.CODE_NUMBER.referenceValue, record)
        }
    }

    fileRecords.each { record ->
        def existRecord = existMap[record.CODE_NUMBER.referenceValue]
        if (existRecord != null) {
            record.put(RefBook.RECORD_ID_ALIAS, existRecord[RefBook.RECORD_ID_ALIAS])
            updateList.add(record)
        } else {
            insertList.add(record)
        }
    }

    if (insertList.empty && updateList.empty) {
        logger.warn(EMPTY_DATA_ERROR)
        return
    }
    if (!insertList.empty) {
        dataProvider.insertRecordsWithoutLock(userInfo, version, insertList)
    }
    if (!updateList.empty) {
        dataProvider.updateRecordsWithoutLock(userInfo, version, updateList)
    }
    scriptStatusHolder.setSuccessCount(insertList.size() + updateList.size())
}