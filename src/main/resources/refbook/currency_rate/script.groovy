/*
    blob_data.id = '99e90406-60f0-4a87-b6f0-7f127abf1fbb'
 */
package refbook.currency_rate

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecord
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue

import javax.xml.namespace.QName
import javax.xml.stream.XMLInputFactory
import java.text.SimpleDateFormat

/**
 * скрипт справочника "Курсы валют" из КСШ
 *
 * @author Stanislav Yasinskiy
 */
switch (formDataEvent) {
    case FormDataEvent.IMPORT:
        importFromXML()
        break
}

void importFromXML() {
    def final REFBOOK_ID = 22
    def dataProvider = refBookFactory.getDataProvider(REFBOOK_ID)
    def SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd")
    def reader = null
    def Date version = null  // дата актуальности
    def boolean currencySector = false // флаг присутствия в секции с курсами
    def Map<String, RefBookValue> recordsMap // аттрибут и его значение
    def List<Map<String, RefBookValue>> insertList = new ArrayList<Map<String, RefBookValue>>() // новые записи
    def List<Map<String, RefBookValue>> updateList = new ArrayList<Map<String, RefBookValue>>() // измененные записи
    def Long code = null // код валюты
    def Double rate = null // курс валюты
    def BigDecimal lotSize = 1
    def Map<String, Number> recordsDB = new HashMap<String, Number>() // записи в БД

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
                    dataProvider.getRecords(version, null, null, null).records.each {
                        if (it.get("CODE_NUMBER") != null) {
                            recordsDB.put(it.get("CODE_NUMBER").referenceValue, it.get(RefBook.RECORD_ID_ALIAS).numberValue)
                        }
                    }
                }

                // Дошли до секции с курсами
                if (reader.getName().equals(QName.valueOf("CurrencyRates"))) {
                    currencySector = true
                }

                // Код валюты
                if (currencySector && reader.getName().equals(QName.valueOf("Code"))) {
                    def String val = reader.getElementText()
                    def records = refBookFactory.getDataProvider(15).getRecords(version, null, "LOWER(CODE) = LOWER('$val')", null)
                    if (records.size() > 0) {
                        code = records.get(0).record_id.numberValue
                    } else {
                        code = null
                        println("В справочнике «Общероссийский классификатор валют» отсутствует элемент с кодом '$val'")
                    }
                }

                // LotSize
                if (currencySector && reader.getName().equals(QName.valueOf("LotSize"))) {
                    def String val = reader.getElementText()?.trim()
                    def tmp = val.replaceAll(",", ".").replace(" ", "")
                    if (tmp.matches("-?\\d+(\\.\\d+)?")) {
                        lotSize = new BigDecimal(tmp)
                    } else {
                        throw new ServiceException("Ошибка получения значения атрибута «LotSize», равного \"$tmp\"")
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
                recordsMap.put("RATE", new RefBookValue(RefBookAttributeType.NUMBER, rate/lotSize))
                if (recordsDB.containsKey(code)) {
                    recordsMap.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, recordsDB.get(code)))
                    updateList.add(recordsMap)
                } else {
                    insertList.add(recordsMap)
                }
                lotSize = 1
            }

            reader.next()
        }
    } finally {
        reader?.close()
    }

    if (updateList.empty && insertList.empty) {
        throw new ServiceException("Сообщение не содержит значений, соответствующих загружаемым данным")
    }

    if (!updateList.empty) {
        updateList.each { map ->
            dataProvider.updateRecordVersion(logger, map.get(RefBook.RECORD_ID_ALIAS).numberValue, version, null, map)
        }
    }
    if (!insertList.empty) {
        def recordList = []
        insertList.each { map ->
            def rbRecord = new RefBookRecord()
            rbRecord.setValues(map)
            recordList.add(rbRecord)
        }
        dataProvider.createRecordVersion(logger, version, null, recordList)
    }
}