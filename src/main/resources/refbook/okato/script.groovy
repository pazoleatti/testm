package refbook.okato

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue

import javax.xml.namespace.QName
import javax.xml.stream.XMLInputFactory
import java.text.SimpleDateFormat

/**
 * загрузка справочника ОКАТО
 *
 * @author Stanislav Yasinskiy
 */

def SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd")
def reader = null

def Date version = null  //дата актуальности
def Map<String, RefBookAttributeType> attributeTypeMap = new HashMap<String, RefBookAttributeType>()  // аттрибуты и их типы
def Map<String, RefBookValue> recordsMap = new HashMap<String, RefBookValue>() // аттрибут и его значение
def List<Map<String, RefBookValue>> recordsList = new ArrayList<Map<String, RefBookValue>>() // данные для записи в бд

try {

    def XMLInputFactory factory = XMLInputFactory.newInstance();
    // TODO падает из-за DTD (без следующей строки кода)
    factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
    reader = factory.createXMLStreamReader(inputStream)

    while (reader.hasNext()) {
        if (reader.startElement) {

            // Версия справочника
            if (reader.getName().equals(QName.valueOf("rollout"))) {
                version = sdf.parse(reader.getAttributeValue(null, "dateSet"))
            }

            // Список аттрибутов с типом
            if (reader.getName().equals(QName.valueOf("fielddesc"))) {
                def String type = reader.getAttributeValue(null, "type")
                def RefBookAttributeType refBookAttributeType = null
                if (type.equals("string"))
                    refBookAttributeType = RefBookAttributeType.STRING
                else if (type.equals("numeric"))
                    refBookAttributeType = RefBookAttributeType.NUMBER
                else if (type.equals("date"))
                    refBookAttributeType = RefBookAttributeType.DATE
                attributeTypeMap.put(reader.getAttributeValue(null, "name"), refBookAttributeType)
            }

            // Список значений для вставки в бд
            if (reader.getName().equals(QName.valueOf("field"))) {
                def name = reader.getAttributeValue(null, "name")
                def value = reader.getAttributeValue(null, "value")
                def RefBookAttributeType type = attributeTypeMap.get(name)
                def RefBookValue refBookValue = null
                if (reader.getAttributeValue(null, "null").toBoolean()) {
                    refBookValue = new RefBookValue(type, null)
                } else if (type.equals(RefBookAttributeType.STRING)) {
                    refBookValue = new RefBookValue(type, value)
                } else if (type.equals(RefBookAttributeType.NUMBER)) {
                    refBookValue = new RefBookValue(type, value.toLong())
                } else if (type.equals(RefBookAttributeType.DATE)) {
                    refBookValue = new RefBookValue(type, sdf.parse(value))
                }
                recordsMap.put(name, refBookValue)
            }
        }

        // Запись в лист
        if (reader.endElement && reader.getName().equals(QName.valueOf("record"))) {
                // TODO тут тормозит - попробывать отдавать в бд порциями
                recordsList.add(recordsMap)
                recordsMap = new HashMap<String, RefBookValue>()
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
println("attribute count = " + attributeTypeMap.size())
println("record count = " + recordsList.size())
recordsList.each { map ->
println("==========================")
map.each {
println("attr = " + it.key + "; value = [s:" + it.value.getStringValue() + "; n:" + it.value.getNumberValue()
        + "; d:" + it.value.getDateValue() + "; r:" + it.value.getReferenceValue()+"]")
}}
