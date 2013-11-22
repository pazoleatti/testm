/*
    blob_data.id = '8891efea-5d2d-4f0e-bc63-6349f354b48d'
 */
package refbook.region

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue

import javax.xml.namespace.QName
import javax.xml.stream.XMLInputFactory
import java.text.SimpleDateFormat
import groovy.transform.Field

/**
 * Cкрипт справочника «Коды субъектов Российской Федерации»
 * // TODO уточнить тексты сообщении при ошибках
 *
 * @author lhaziev
 */
switch (formDataEvent) {
    case FormDataEvent.IMPORT:
        importFromXML()
        break
}


@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

@Field
def currentDate = new Date()

@Field
def lstOkatoDefinition = [
        "Ненецкий автономный округ":            "1110",
        "Ханты-Мансийский автономный округ":    "71100",
        "Ямало - Ненецкий автономный округ":    "71140"
]

void importFromXML() {
    def dataProvider = refBookFactory.getDataProvider(4L)
    def dataProviderOKATO = refBookFactory.getDataProvider(3L)
    def refBook = refBookFactory.get(4L)
    def SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd")
    def reader = null

    boolean isRegion = false
    def Map<String, RefBookValue> recordsMap = new HashMap<String, RefBookValue>() // аттрибут и его значение
    def List<Map<String, RefBookValue>> insertList = new ArrayList<Map<String, RefBookValue>>() // данные для записи в бд
    def List<Map<String, RefBookValue>> updateList = new ArrayList<Map<String, RefBookValue>>() // данные для обновления в бд
    def List<Long> deleteList = new ArrayList<Long>() // данные для удаления из бд

    def Map<String, Model> mapper = new HashMap<String, Model>() // соответствие имён аттрибутов в бд и xml

    refBook.attributes.each {
        if (it.alias.equals("CODE")) {
            mapper.put("CODE", new Model(it.attributeType, "CODE"))
        }
        else if (it.alias.equals("NAME")) {
            mapper.put("NAME", new Model(it.attributeType, "NAME"))
        }
        else if (it.alias.equals("OKATO_DEFINITION")) {
            mapper.put("OKATO", new Model(it.attributeType, "OKATO_DEFINITION"))
        }
        else if (it.alias.equals("OKATO")) {
            mapper.put("OKATO_FULL", new Model(it.attributeType, "OKATO"))
        }
    }

    int recordtype = 0
    try {
        def XMLInputFactory factory = XMLInputFactory.newInstance()
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE)
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE)
        reader = factory.createXMLStreamReader(inputStream)

        while (reader.hasNext()) {
            // проверка названия таблицы
            if (isRegion) {
            } else if (reader.startElement && reader.getName().equals(QName.valueOf("table")) && (reader.getAttributeValue(null, "name").toString()=="PB_region")) {
                isRegion = true
            } else {
                isRegion = false
                reader.next()
                continue
            }

            if (reader.startElement) {
                if (reader.getName().equals(QName.valueOf("insert"))) {
                    recordtype = 1
                } else if (reader.getName().equals(QName.valueOf("update"))) {
                    recordtype = 2
                } else if (reader.getName().equals(QName.valueOf("delete"))) {
                    recordtype = 3
                } else if (reader.getName().equals(QName.valueOf("replace"))) {
                    recordtype = 4
                }

                // Список значений для вставки в бд
                if (reader.getName().equals(QName.valueOf("field"))) {
                    def name = reader.getAttributeValue(null, "name")
                    def value = reader.getAttributeValue(null, "value")
                    def map = mapper.get(name)
                    if (map != null) {
                        def RefBookValue refBookValue = null
                        if (map.type.equals(RefBookAttributeType.STRING)) {
                            if (name=="CODE"){
                                if (value?.size()==1) value='0'+value
                            }
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

            if (reader.endElement && reader.getName().equals(QName.valueOf("table"))) {
                isRegion = false
                reader.next()
                continue
            }
            // Запись в лист
            else if (reader.endElement && reader.getName().equals(QName.valueOf("record"))) {
                switch (recordtype) {
                    case 1:
                        def recordID = getRecordId(dataProvider, 'CODE', recordsMap.get("CODE").toString(), currentDate)
                        if (recordID==null) {
                            def OKATO = getOKATO(recordsMap, dataProviderOKATO)
                            if (OKATO!=null) {
                                def map = mapper.get("OKATO_FULL")
                                recordsMap.put(map.name,  OKATO)
                                insertList.add(recordsMap)
                            } else {
                                logger.error("Для элемента ${recordsMap.get("NAME")?.toString()} справочника «Коды субъектов Российской Федерации» в справочнике «Коды ОКАТО» не найдено соответствующее значение.")
                                return
                            }
                        } else {
                            // TODO уточнить сообщения
                            logger.error("Добавление элемента справочника: Элемент ${recordsMap.get("NAME")?.toString()} существует в справочнике «Коды субъектов Российской Федерации».")
                            return
                        }
                        break
                    case 2:
                        def recordID = getRecordId(dataProvider, 'CODE', recordsMap.get("CODE").toString(), currentDate)
                        if (recordID!=null) {
                            def OKATO = getOKATO(recordsMap, dataProviderOKATO)
                            if (OKATO!=null) {
                                def map = mapper.get("OKATO_FULL")
                                recordsMap.put(map.name,  OKATO)
                                recordsMap.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, recordID))
                                updateList.add(recordsMap)
                            } else {
                                logger.error("В справочнике «Коды субъектов Российской Федерации» элемент «${recordsMap.get("NAME")?.toString()}» уже существует.")
                                return
                            }
                        } else {
                            // TODO уточнить сообщения
                            logger.error("Редактирование элемента справочника: В справочнике «Коды субъектов Российской Федерации» не существует элемента «${recordsMap.get("NAME")?.toString()}».")
                            return
                        }
                        break
                    case 3:
                        def recordID = getRecordId(dataProvider, 'CODE', recordsMap.get("CODE").toString(), currentDate)
                        if (recordID!=null) {
                            deleteList.add(recordID)
                        } else {
                            // TODO уточнить сообщения
                            logger.error("Удаления элемента справочника: В справочнике «Коды субъектов Российской Федерации» не существует элемента «${recordsMap.get("NAME")?.toString()}».")
                            return
                        }
                        break
                    case 4:
                        break
                }
                recordsMap = new HashMap<String, RefBookValue>()
            } else if (reader.endElement && reader.getName().equals(QName.valueOf("insert"))) {
                if (!insertList.empty) {
                    dataProvider.insertRecords(currentDate, insertList)
                    insertList.clear()
                }
            } else if (reader.endElement && reader.getName().equals(QName.valueOf("update"))) {
                if (!updateList.empty) {
                    dataProvider.updateRecords(currentDate, updateList)
                    updateList.clear()
                }
            } else if (reader.endElement && reader.getName().equals(QName.valueOf("delete"))) {
                if (!deleteList.empty) {
                    dataProvider.deleteRecords(currentDate, deleteList)
                    deleteList.clear()
                }
            } else if (reader.endElement && reader.getName().equals(QName.valueOf("replace"))) {
            }
            reader.next()
        }
    } finally {
        reader?.close()
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

def getOKATO(def recordsMap, def dataProviderOKATO) {
    def OKATO_DEFINITION = recordsMap.get("OKATO_DEFINITION")?.toString()
    if (OKATO_DEFINITION==null || OKATO_DEFINITION==""){
        OKATO_DEFINITION = lstOkatoDefinition[recordsMap.get("NAME")?.toString()]
        recordsMap.put("OKATO_DEFINITION", new RefBookValue(RefBookAttributeType.STRING, OKATO_DEFINITION))
    }
    if (OKATO_DEFINITION!=null && OKATO_DEFINITION!=""){
        def recordID = getRecordId(dataProviderOKATO, 'OKATO', OKATO_DEFINITION+"0"*(11-OKATO_DEFINITION.size()), currentDate)
        if (recordID!=null) {
            return new RefBookValue(RefBookAttributeType.REFERENCE, recordID)
        } else {
            return null
        }
    }
    return new RefBookValue(RefBookAttributeType.STRING, "")
}

def getRecordId(def dataProvider, String alias, String value, Date date) {
    String filter = "LOWER($alias) = LOWER('$value')"
    def records = dataProvider.getRecords(date, null, filter, null)?.getRecords()
    if (records?.size() == 1) {
        return (records.get(0).record_id.toString() as Long)
    }
    return null
}