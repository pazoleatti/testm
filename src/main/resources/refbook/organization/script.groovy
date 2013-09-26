package refbook.organization

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook

import javax.xml.namespace.QName
import javax.xml.stream.XMLInputFactory
import java.text.SimpleDateFormat

/**
 * скрипт справочника Организации-участники контролируемых сделок
 *
 * @author Stanislav Yasinskiy
 */
switch (formDataEvent) {
    case FormDataEvent.IMPORT:
        importFromXML()
        break
}

// TODO доделать под xml
void importFromXML() {
    def final Long refBookID = 9L
    def dataProvider = refBookFactory.getDataProvider(refBookID)
    def RefBook refBook = refBookFactory.get(refBookID)
    def SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd")
    def reader = null
    def Date version = null  //дата актуальности
    def Map<String, RefBookValue> recordsMap = new HashMap<String, RefBookValue>() // аттрибут и его значение
    def List<Map<String, RefBookValue>> recordsList = new ArrayList<Map<String, RefBookValue>>() // данные для записи в бд
    def Map<String, Model> mapper = new HashMap<String, Model>() // соответствие имён аттрибутов в бд и xml
    def final INSERT_SIZE = 100 // размер одной порции данных // 10000 Превышает таймаут

    mapper.put("TODO1", getModel(refBook, 'NAME'))
    mapper.put("TODO2", getModel(refBook, 'SKOLKOVO'))
    mapper.put("TODO3", getModel(refBook, 'DOP_INFO'))
    mapper.put("TODO4", getModel(refBook, 'OFFSHORE'))
    mapper.put("TODO5", getModel(refBook, 'ORGANIZATION'))
    mapper.put("TODO6", getModel(refBook, 'INN_KIO'))
    mapper.put("TODO7", getModel(refBook, 'ADDRESS'))
    mapper.put("TODO8", getModel(refBook, 'TAXPAYER_CODE'))
    mapper.put("TODO9", getModel(refBook, 'REG_NUM'))
    mapper.put("TODO0", getModel(refBook, 'COUNTRY'))

    // Признак обновления записей
    def isUpdateMode = false;

    try {
        def XMLInputFactory factory = XMLInputFactory.newInstance()
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE)
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE)
        reader = factory.createXMLStreamReader(inputStream)

        while (reader.hasNext()) {
            if (reader.startElement) {

                // Версия справочника
                if (reader.getName().equals(QName.valueOf("rollout"))) {
                    version = sdf.parse(reader.getAttributeValue(null, "dateSet"))

                    List<Date> versionList = dataProvider.getVersions(version, version)
                    isUpdateMode = versionList.contains(version)

                    // Если версии не совпадают, прежние записи отмечаются как удаленные
                    if (!isUpdateMode) {
                        dataProvider.deleteAllRecords(version)
                    }
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
                // recordsMap.put("ID", new RefBookValue(RefBookAttributeType.NUMBER, counter++))
                recordsList.add(recordsMap)
                recordsMap = new HashMap<String, RefBookValue>()
                if (recordsList.size() >= INSERT_SIZE) {
                    writeRecords(dataProvider, isUpdateMode, version, recordsList)
                    recordsList.clear()
                }
            }

            reader.next()
        }
    } finally {
        reader?.close()
    }

    writeRecords(dataProvider, isUpdateMode, version, recordsList)

    if (isUpdateMode) {
        refBookOkatoDao.clearParentId(version)
    }

    refBookOkatoDao.updateParentId(version)
}

// Добавление или обновление порции записей
private void writeRecords(def dataProvider, boolean isUpdateMode, Date version,
                          List<Map<String, RefBookValue>> recordsList) {
    if (!isUpdateMode) {
        // Добавление новых записей
        dataProvider.insertRecords(version, recordsList)
    } else {
        // Обновление атрибутов
        List<Map<String, RefBookValue>> notFoundList = refBookOkatoDao.updateValueNames(version, recordsList)
        // Добавление новых записей
        if (!notFoundList.isEmpty()) {
            dataProvider.insertRecords(version, notFoundList)
        }
    }
}

class Model {
    RefBookAttributeType type
    String name

    Model(RefBookAttributeType type, String name) {
        this.type = type
        this.name = name
    }
}

Model getModel(RefBook refBook, String name) {
    new Model(refBook.getAttribute(name).attributeType, name)
}