package refbook.okato

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue

import javax.xml.namespace.QName
import javax.xml.stream.XMLInputFactory
import java.text.SimpleDateFormat

/**
 * скрипт справочника ОКАТО
 *
 * @author Stanislav Yasinskiy
 */


switch (formDataEvent) {
    case FormDataEvent.IMPORT:
        importFromXML()
        break
}

void importFromXML() {
    def dataProvider = refBookFactory.getDataProvider(3)
    def refBook = refBookFactory.get(3)
    def SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd")
    def reader = null
    def Date version = null  //дата актуальности
    def Map<String, RefBookValue> recordsMap = new HashMap<String, RefBookValue>() // аттрибут и его значение
    def List<Map<String, RefBookValue>> recordsList = new ArrayList<Map<String, RefBookValue>>() // данные для записи в бд
    def Map<String, Model> mapper = new HashMap<String, Model>() // соответствие имён аттрибутов в бд и xml
    def final INSERT_SIZE = 1000 // размер одной порции данных // 10000 Превышает таймаут

    refBook.attributes.each {
        if (it.alias.equals("NAME"))
            mapper.put("NAME1", new Model(it.attributeType, "NAME"))
        else if (it.alias.equals("OKATO"))
            mapper.put("KOD", new Model(it.attributeType, "OKATO"))
    }

    try {
        def XMLInputFactory factory = XMLInputFactory.newInstance();
        // TODO падает из-за DTD (без следующей строки кода)
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        reader = factory.createXMLStreamReader(inputStream)

        println("begin")

        while (reader.hasNext()) {

            if (reader.startElement) {

                // Версия справочника
                if (reader.getName().equals(QName.valueOf("rollout"))) {
                    version = sdf.parse(reader.getAttributeValue(null, "dateSet"))
                }


                // Список значений для вставки в бд
                if (reader.getName().equals(QName.valueOf("field"))) {
                    def name = reader.getAttributeValue(null, "name")
                    def value = reader.getAttributeValue(null, "value")
                    def map = mapper.get(name)
                    if (map != null) {
                        def RefBookValue refBookValue = null
                        if (map.type.equals(RefBookAttributeType.STRING)) {
                            refBookValue = new RefBookValue(map.type, value)
                        } else if (map.type.equals(RefBookAttributeType.NUMBER) || map.type.equals(RefBookAttributeType.REFERENCE)) {
                            refBookValue = new RefBookValue(map.type, value.toLong())
                        } else if (map.type.equals(RefBookAttributeType.DATE)) {
                            refBookValue = new RefBookValue(map.type, sdf.parse(value))
                        }
                        recordsMap.put(map.name, refBookValue)
                    }
                }
            }

            // Запись в лист
            if (reader.endElement && reader.getName().equals(QName.valueOf("record"))) {
                recordsList.add(recordsMap)
                recordsMap = new HashMap<String, RefBookValue>()
                if (recordsList.size() >= INSERT_SIZE) {
                    println("recordsList.size() = " + recordsList.size())
                    dataProvider.insertRecords(version, recordsList)
                    recordsList.clear()
                }
            }

            reader.next()
        }
    } finally {
        reader?.close()
    }
    dataProvider.deleteAllRecords(version)
    dataProvider.insertRecords(version, recordsList)

//дебаг
    println("version = " + version)
    println("record count = " + recordsList.size())

/*recordsList.each { map ->
    println("==========================")
    map.each {
        println("attr = " + it.key + "; value = [s:" + it.value.getStringValue() + "; n:" + it.value.getNumberValue()
                + "; d:" + it.value.getDateValue() + "; r:" + it.value.getReferenceValue() + "]")
    }
} */
}

class Model {
    RefBookAttributeType type
    String name

    Model(RefBookAttributeType type, String name) {
        this.type = type
        this.name = name
    }
}
