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
 * Cкрипт справочника "Курсы валют" из КСШ
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
    def Map<String, Number> codeToRecordId = new HashMap<String, Number>() // Код → Id элемента

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
                    // Получение значений на максимальную дату актуальности
                    def maxActualDate = new GregorianCalendar(2114, Calendar.JANUARY, 1).getTime() // 01.01.2114
                    dataProvider.getRecords(maxActualDate, null, null, null).each {
                        if (it.CODE_NUMBER != null) {
                            codeToRecordId.put(it.CODE_NUMBER.referenceValue, it.get(RefBook.RECORD_ID_ALIAS).numberValue)
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
                if (codeToRecordId.containsKey(code)) {
                    recordsMap.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, codeToRecordId.get(code)))
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

    if (insertList.empty && updateList.empty) {
        throw new ServiceException("Сообщение не содержит значений, соответствующих загружаемым данным!")
    }
    if (!insertList.empty) {
        dataProvider.insertRecords(version, insertList)
    }
    if (!updateList.empty) {
        dataProvider.updateRecords(version, updateList)
    }
}