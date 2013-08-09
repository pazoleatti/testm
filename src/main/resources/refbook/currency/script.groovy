package refbook.currency

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue

import javax.xml.namespace.QName
import javax.xml.stream.XMLInputFactory
import java.text.SimpleDateFormat

/**
 * загрузка справочника "Курсы валют" из КСШ
 *
 * @author Stanislav Yasinskiy
 */

def SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd")
def reader = null

def Date version = null  //дата актуальности
def boolean currencySector = false // флаг присутствия в секции с курсами
def Map<String, RefBookValue> recordsMap // аттрибут и его значение
def List<Map<String, RefBookValue>> recordsList = new ArrayList<Map<String, RefBookValue>>() // данные для записи в бд
def Long code = null // код валюты
def Double rate = null // курс валюты

try {

    def XMLInputFactory factory = XMLInputFactory.newInstance();
    // TODO падает из-за DTD (без следующей строки кода)
    factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
    reader = factory.createXMLStreamReader(inputStream)

    while (reader.hasNext()) {
        if (reader.startElement) {

            // Версия справочника
            if (reader.getName().equals(QName.valueOf("StartDateTime"))) {
                version = sdf.parse(reader.getElementText())
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
            recordsMap.put("CODE_NUMBER", new RefBookValue(RefBookAttributeType.REFERENCE, code))
            recordsMap.put("RATE", new RefBookValue(RefBookAttributeType.NUMBER, rate))
            recordsList.add(recordsMap)
        }

        reader.next()
    }
} finally {
    reader?.close()
}

// TODO аккуратно проверить запись в бд
//refBookDataProvider.updateRecords(version, recordsList)

//дебаг
println("version = " + version)
println("record count = " + recordsList.size())
//recordsList.each { map ->
//    println("==========================")
//    map.each {
//        println("attr = " + it.key + "; value = [s:" + it.value.getStringValue() + "; n:" + it.value.getNumberValue()
//                + "; d:" + it.value.getDateValue() + "; r:" + it.value.getReferenceValue()+"]")
//    }
//}
