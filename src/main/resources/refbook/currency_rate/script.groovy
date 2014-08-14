package refbook.currency_rate

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
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
def REFBOOK_ID = 22

@Field
def EMPTY_DATA_ERROR = "Сообщение не содержит значений, соответствующих загружаемым данным!"

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

        def currencyDataProvider = refBookFactory.getDataProvider(15)

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
                    def records = currencyDataProvider.getRecords(version, null, "LOWER(CODE) = LOWER('$val')", null)
                    if (records.size() > 0) {
                        code = records.get(0).record_id.numberValue
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
    def filterStr = ''
    fileRecords.each { record ->
        filterStr += ((record == fileRecords.getAt(0)) ? "" : " or ") + " CODE_NUMBER = " + record.CODE_NUMBER.referenceValue
    }

    def recordIds = dataProvider.getUniqueRecordIds(null, filterStr)

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
        dataProvider.insertRecords(version, insertList)
    }
    if (!updateList.empty) {
        dataProvider.updateRecords(version, updateList)
    }
    scriptStatusHolder.setSuccessCount(insertList.size() + updateList.size())
}