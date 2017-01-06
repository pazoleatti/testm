package refbook.fias

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.refbook.impl.RefBookSimpleReadOnly
import groovy.transform.Field

import javax.xml.namespace.QName
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.events.Attribute
import javax.xml.stream.events.XMLEvent

/**
 * Импорт справочника адресообразующих объектов
 */
switch (formDataEvent) {
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importData()
        break
}

@Field
def BATCH_SIZE_MAX = 100

void importData() {

    println "Fias data will be import now!"

    def time = System.currentTimeMillis()
    def rowBuffer = new ArrayList<Map<String, ?>>();

    def tableName;
    def importedElementName;
    def rowMapper;

    def xmlFactory = XMLInputFactory.newInstance()
    xmlFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE)
    xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE)

    def reader = xmlFactory.createXMLEventReader(inputStream)

    try {
        def i = 0;
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent()

            //Обрабатываем все элементы импорта
            if (event.isStartElement()) {
                if (event.getName().equals(importedElementName)) {

                    Map attributeMap = getAttributesMap(event)
                    rowBuffer.add(rowMapper(attributeMap))
                    i++;
                    //Проверяем если обработали пакет строк размером BATCH_SIZE, то кидаем в сервис и очищаем список
                    if ((i % BATCH_SIZE_MAX) == 0) {
                        importFiasDataService.insertRecords(tableName, rowBuffer)
                        rowBuffer.clear();
                    }
                    continue;
                }

                //В зависимости от корневого элемента определяем какие элементы будем разбирать и куда их загружать
                if (event.getName().equals(QName.valueOf('OperationStatuses'))) {
                    tableName = RefBookSimpleReadOnly.FIAS_OPERSTAT_TABLE_NAME
                    importedElementName = QName.valueOf('OperationStatus')
                    rowMapper = { attr ->
                        operationStatusRowMapper(attr)
                    }
                } else if (event.getName().equals(QName.valueOf('AddressObjectTypes'))) {
                    tableName = RefBookSimpleReadOnly.FIAS_SOCRBASE_TABLE_NAME
                    importedElementName = QName.valueOf('AddressObjectType')
                    rowMapper = { attr ->
                        addressObjectTypeRowMapper(attr)
                    }
                } else if (event.getName().equals(QName.valueOf('AddressObjects'))) {
                    tableName = RefBookSimpleReadOnly.FIAS_ADDR_OBJECT_TABLE_NAME
                    importedElementName = QName.valueOf('Object')
                    rowMapper = { attr ->
                        addressObjectRowMapper(attr)
                    }
                } else if (event.getName().equals(QName.valueOf('Houses'))) {
                    tableName = RefBookSimpleReadOnly.FIAS_HOUSE_TABLE_NAME
                    importedElementName = QName.valueOf('House')
                    rowMapper = { attr ->
                        houseRowMapper(attr)
                    }
                } else if (event.getName().equals(QName.valueOf('HouseIntervals'))) {
                    tableName = RefBookSimpleReadOnly.FIAS_HOUSEINT_TABLE_NAME
                    importedElementName = QName.valueOf('HouseInterval')
                    rowMapper = { attr ->
                        houseIntervalRowMapper(attr)
                    }
                } else if (event.getName().equals(QName.valueOf('Rooms'))) {
                    tableName = RefBookSimpleReadOnly.FIAS_ROOM_TABLE_NAME
                    importedElementName = QName.valueOf('Room')
                    rowMapper = { attr ->
                        roomRowMapper(attr)
                    }
                }
            }
        }
        //Добавляем оставшиеся в списке записи
        importFiasDataService.insertRecords(tableName, rowBuffer);
        rowBuffer.clear();
    } finally {
        reader?.close()
    }

    println "Fias ${importedElementName} import to ${tableName} end (" + (System.currentTimeMillis() - time) + " ms)";

}


Integer getInteger(String val) {
    if (val != null && !val.isEmpty()) {
        return val.toInteger()
    }
    return null
}

Integer getLong(String val) {
    if (val != null && !val.isEmpty()) {
        return val.toLong()
    }
    return null
}


Map getAttributesMap(event) {
    def result = [:]
    Iterator iterator = event.getAttributes();
    while (iterator.hasNext()) {
        Attribute attribute = (Attribute) iterator.next();
        QName name = attribute.getName();
        String value = attribute.getValue();
        result.put(name, value);
    }
    return result;
}

//---fias_operstat---
Map operationStatusRowMapper(attrMap) {
    def Map recordsMap = new HashMap<String, ?>();
    recordsMap.put('OPERSTATID', getLong(attrMap.get(QName.valueOf('OPERSTATID'))))
    recordsMap.put('NAME', attrMap.get(QName.valueOf('NAME')))
    return recordsMap;
}

Map addressObjectTypeRowMapper(attrMap) {
    def Map recordsMap = new HashMap<String, ?>();
    recordsMap.put('SCNAME', attrMap.get(QName.valueOf('SCNAME')))
    recordsMap.put('SOCRNAME', attrMap.get(QName.valueOf('SOCRNAME')))
    recordsMap.put('KOD_T_ST', attrMap.get(QName.valueOf('KOD_T_ST')))
    return recordsMap;
}

Map addressObjectRowMapper(attrMap) {
    def Map recordsMap = new HashMap<String, ?>();
    recordsMap.put('AOGUID', attrMap.get(QName.valueOf('AOGUID')))
    recordsMap.put('FORMALNAME', attrMap.get(QName.valueOf('FORMALNAME')))
    recordsMap.put('REGIONCODE', attrMap.get(QName.valueOf('REGIONCODE')))
    recordsMap.put('AUTOCODE', attrMap.get(QName.valueOf('AUTOCODE')))
    recordsMap.put('AREACODE', attrMap.get(QName.valueOf('AREACODE')))
    recordsMap.put('CITYCODE', attrMap.get(QName.valueOf('CITYCODE')))
    recordsMap.put('CTARCODE', attrMap.get(QName.valueOf('CTARCODE')))
    recordsMap.put('PLACECODE', attrMap.get(QName.valueOf('PLACECODE')))
    recordsMap.put('PLANCODE', attrMap.get(QName.valueOf('PLANCODE')))
    recordsMap.put('STREETCODE', attrMap.get(QName.valueOf('STREETCODE')))
    recordsMap.put('EXTRCODE', attrMap.get(QName.valueOf('EXTRCODE')))
    recordsMap.put('SEXTCODE', attrMap.get(QName.valueOf('SEXTCODE')))
    recordsMap.put('LIVESTATUS', getInteger(attrMap.get(QName.valueOf('LIVESTATUS'))))
    recordsMap.put('CENTSTATUS', getInteger(attrMap.get(QName.valueOf('CENTSTATUS'))))
    recordsMap.put('OPERSTATUS', getInteger(attrMap.get(QName.valueOf('OPERSTATUS'))))
    recordsMap.put('CURRSTATUS', getInteger(attrMap.get(QName.valueOf('CURRSTATUS'))))
    recordsMap.put('DIVTYPE', getInteger(attrMap.get(QName.valueOf('DIVTYPE'))))
    recordsMap.put('OFFNAME', attrMap.get(QName.valueOf('OFFNAME')))
    recordsMap.put('POSTALCODE', attrMap.get(QName.valueOf('POSTALCODE')))
    recordsMap.put('PARENTGUID', attrMap.get(QName.valueOf('PARENTGUID')))
    return recordsMap;
}


Map houseRowMapper(attrMap) {
    def Map recordsMap = new HashMap<String, ?>();
    recordsMap.put('HOUSEGUID', attrMap.get(QName.valueOf('HOUSEGUID')))
    recordsMap.put('AOGUID', attrMap.get(QName.valueOf('AOGUID')))
    recordsMap.put('ESTSTATUS', getInteger(attrMap.get(QName.valueOf('ESTSTATUS'))))
    recordsMap.put('STRSTATUS', getInteger(attrMap.get(QName.valueOf('STRSTATUS'))))
    recordsMap.put('STATSTATUS', getInteger(attrMap.get(QName.valueOf('STATSTATUS'))))
    recordsMap.put('DIVTYPE', getInteger(attrMap.get(QName.valueOf('DIVTYPE'))))
    recordsMap.put('POSTALCODE', attrMap.get(QName.valueOf('POSTALCODE')))
    recordsMap.put('HOUSENUM', attrMap.get(QName.valueOf('HOUSENUM')))
    recordsMap.put('BUILDNUM', attrMap.get(QName.valueOf('BUILDNUM')))
    recordsMap.put('STRUCNUM', attrMap.get(QName.valueOf('STRUCNUM')))
    return recordsMap;
}

Map houseIntervalRowMapper(attrMap) {
    def Map recordsMap = new HashMap<String, ?>();
    recordsMap.put('INTGUID', attrMap.get(QName.valueOf('INTGUID')))
    recordsMap.put('AOGUID', attrMap.get(QName.valueOf('AOGUID')))
    recordsMap.put('INTSTART', getInteger(attrMap.get(QName.valueOf('INTSTART'))))
    recordsMap.put('INTEND', getInteger(attrMap.get(QName.valueOf('INTEND'))))
    recordsMap.put('INTSTATUS', getInteger(attrMap.get(QName.valueOf('INTSTATUS'))))
    recordsMap.put('COUNTER', getInteger(attrMap.get(QName.valueOf('COUNTER'))))
    recordsMap.put('POSTALCODE', attrMap.get(QName.valueOf('POSTALCODE')))
    return recordsMap;
}

Map roomRowMapper(attrMap) {
    def Map recordsMap = new HashMap<String, ?>();
    recordsMap.put('ROOMGUID', attrMap.get(QName.valueOf('ROOMGUID')))
    recordsMap.put('HOUSEGUID', attrMap.get(QName.valueOf('HOUSEGUID')))
    recordsMap.put('REGIONCODE', attrMap.get(QName.valueOf('REGIONCODE')))
    recordsMap.put('FLATNUMBER', attrMap.get(QName.valueOf('FLATNUMBER')))
    recordsMap.put('FLATTYPE', getInteger(attrMap.get(QName.valueOf('FLATTYPE'))))
    recordsMap.put('LIVESTATUS', getInteger(attrMap.get(QName.valueOf('LIVESTATUS'))))
    recordsMap.put('ROOMNUMBER', attrMap.get(QName.valueOf('ROOMNUMBER')))
    recordsMap.put('ROOMTYPEID', getInteger(attrMap.get(QName.valueOf('ROOMTYPEID'))))
    recordsMap.put('POSTALCODE', attrMap.get(QName.valueOf('POSTALCODE')))
    return recordsMap;
}





