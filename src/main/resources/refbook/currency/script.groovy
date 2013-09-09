package refbook.currency

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
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
    def refBook = refBookFactory.get(REFBOOK_ID)
    def SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd")
    def reader = null
    def Date version = null  //дата актуальности
    def boolean currencySector = false // флаг присутствия в секции с курсами
    def Map<String, RefBookValue> recordsMap // аттрибут и его значение
    def List<Map<String, RefBookValue>> insertList = new ArrayList<Map<String, RefBookValue>>() // новые записи
    def List<Map<String, RefBookValue>> updateList = new ArrayList<Map<String, RefBookValue>>() // измененные записи
    def Long code = null // код валюты
    def Double rate = null // курс валюты
    def RefBookAttributeType codeType = null // тип кода
    def RefBookAttributeType rateType = null // тип курса
    def Map<String, Number> recordsDB = new HashMap<String, Number>() // записи в БД

    refBook.attributes.each {
        if (it.alias.equals("CODE_NUMBER"))
            codeType = it.attributeType
        else if (it.alias.equals("RATE"))
            rateType = it.attributeType
    }

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
                        if (it.get("CODE_NUMBER") != null)
                            recordsDB.put(it.get("CODE_NUMBER").stringValue, it.get(RefBook.RECORD_ID_ALIAS).numberValue)
                    }
                }

                //Дошли до секции с курсами
                if (reader.getName().equals(QName.valueOf("CurrencyRates"))) {
                    currencySector = true
                }

                // Код валюты
                if (currencySector && reader.getName().equals(QName.valueOf("Code"))) {
                    code = reader.getElementText().toLong()
                }

                // Курс валюты
                if (currencySector && reader.getName().equals(QName.valueOf("Rate"))) {
                    rate = reader.getElementText().toDouble()
                }
            }

            // Запись в лист
            if (reader.endElement && reader.getName().equals(QName.valueOf("CurrencyRate"))) {
                recordsMap = new HashMap<String, RefBookValue>()
                recordsMap.put("CODE_NUMBER", new RefBookValue(codeType, code))
                recordsMap.put("RATE", new RefBookValue(rateType, rate))
                if (recordsDB.containsKey(code)) {
                    recordsMap.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, recordsDB.get(code)))
                    updateList.add(recordsMap)
                } else {
                    insertList.add(recordsMap)
                }
            }

            reader.next()
        }
    } finally {
        reader?.close()
    }

// TODO аккуратно проверить запись в бд
    if (!updateList.empty)
        dataProvider.updateRecords(version, updateList)
    if (!insertList.empty)
        dataProvider.insertRecords(version, insertList)

//дебаг
    println("version = " + version)
    println("insert record count = " + insertList.size())
    println("update record count = " + updateList.size())
//recordsList.each { map ->
//    println("==========================")
//    map.each {
//        println("attr = " + it.key + "; value = [s:" + it.value.getStringValue() + "; n:" + it.value.getNumberValue()
//                + "; d:" + it.value.getDateValue() + "; r:" + it.value.getReferenceValue()+"]")
//    }
//}
}