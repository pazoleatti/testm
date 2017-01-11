package refbook.fias

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.refbook.impl.RefBookSimpleReadOnly
import com.github.junrar.rarfile.FileHeader
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
def BATCH_SIZE_MAX = 1000

void importData() {
    List<FileHeader> fileHeaders = archive.getFileHeaders();

    //Очистка данных перед импортом
    importFiasDataService.clearAll()

    //Строим карту Guid адресных объектов, заранее так как будет нужна иерархия
    def addrObjInputStream = getInputStream(fileHeaders, "AS_ADDROBJ_")
    def addressObjectGuidsMap = buildAddressObjectGuidsMap(addrObjInputStream);

    def houseGuidsMap = [:]

    //Начинаем заливать данные из таблиц справочника
    startImport(getInputStream(fileHeaders, "AS_OPERSTAT_"),
            QName.valueOf('OperationStatus'),
            RefBookSimpleReadOnly.FIAS_OPERSTAT_TABLE_NAME,
            { generatedId, attr ->
                operationStatusRowMapper(generatedId, attr)
            })

    startImport(getInputStream(fileHeaders, "AS_SOCRBASE_"),
            QName.valueOf('AddressObjectType'),
            RefBookSimpleReadOnly.FIAS_SOCRBASE_TABLE_NAME,
            { generatedId, attr ->
                addressObjectTypeRowMapper(generatedId, attr)
            })

    startImport(getInputStream(fileHeaders, "AS_ADDROBJ_"),
            QName.valueOf('Object'),
            RefBookSimpleReadOnly.FIAS_ADDR_OBJECT_TABLE_NAME,
            { generatedId, attr ->
                addressObjectRowMapper(addressObjectGuidsMap, attr) //здесь для получения id используем подготовленную карту
            })

    startImport(getInputStream(fileHeaders, "AS_HOUSE_"),
            QName.valueOf('House'),
            RefBookSimpleReadOnly.FIAS_HOUSE_TABLE_NAME,
            { generatedId, attr ->
                houseRowMapper(generatedId, addressObjectGuidsMap, houseGuidsMap, attr)
            })

    startImport(getInputStream(fileHeaders, "AS_HOUSEINT_"),
            QName.valueOf('HouseInterval'),
            RefBookSimpleReadOnly.FIAS_HOUSEINT_TABLE_NAME,
            { generatedId, attr ->
                houseIntervalRowMapper(generatedId, addressObjectGuidsMap, attr)
            })

    startImport(getInputStream(fileHeaders, "AS_ROOM_"),
            QName.valueOf('Room'),
            RefBookSimpleReadOnly.FIAS_ROOM_TABLE_NAME,
            { generatedId, attr ->
                roomRowMapper(generatedId, houseGuidsMap, attr)
            })

}

void startImport(fiasInputStream, importedElementName, tableName, rowMapper) {

    println "Fias data will be import now!"

    def time = System.currentTimeMillis()
    def rowBuffer = new ArrayList<Map<String, ?>>();

    //Используем StAX парсер для импорта
    def xmlFactory = XMLInputFactory.newInstance()
    xmlFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE)
    xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE)

    def reader = xmlFactory.createXMLEventReader(fiasInputStream)
    def i = 0;
    try {

        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent()

            //Обрабатываем все элементы импорта
            if (event.isStartElement()) {
                if (event.getName().equals(importedElementName)) {

                    Map attributeMap = getAttributesMap(event)
                    Map rowMap = rowMapper(i.longValue(), attributeMap)

                    rowBuffer.add(rowMap)
                    i++;
                    //Проверяем если обработали пакет строк размером BATCH_SIZE, то кидаем в сервис и очищаем список
                    if ((i % BATCH_SIZE_MAX) == 0) {
                        importFiasDataService.insertRecords(tableName, rowBuffer)
                        rowBuffer.clear();
                    }

                    //if ((i % 500000) == 0) {println "${i} rows process..."}

                }
            }
        }
        //Добавляем оставшиеся в списке записи
        importFiasDataService.insertRecords(tableName, rowBuffer);
        rowBuffer.clear();
    } finally {
        reader?.close()
    }

    println "Fias ${importedElementName} (${i} rows) import to ${tableName} end (" + (System.currentTimeMillis() - time) + " ms)";
}

/**
 * Получаем поток для чтения файла из rar-архива
 * @param fileHeaders заголовок файла в архиве
 * @param prefix префикс имени файла
 * @return Pipe streams
 */
def getInputStream(List<FileHeader> fileHeaders, prefix) {

    def fileHeader = getFileHeader(fileHeaders, prefix)

    if (fileHeader == null) {
        throw new ServiceException("В архиве выбранном для загрузки данных ФИАС, отсутствует XML файл с префиксом ${prefix}")
    }
    return archive.getInputStream(fileHeader)
}

/**
 * Получаем имя файла в архиве по префиксу
 * @param fileHeaders заголовки файлов в архиве
 * @param prefix префикс для выбора заголовка файла
 * @return заголовок файла
 */
def getFileHeader(fileHeaders, prefix) {
    for (FileHeader fileHeader : fileHeaders) {
        if (fileHeader.getFileNameString().startsWith(prefix)) {
            return fileHeader
        }
    }
    return null
}


Integer getInteger(String val) {
    if (val != null && !val.isEmpty()) {
        return val.toInteger()
    }
    return null
}

Integer getInteger(String val, defaultValue) {
    if (val != null && !val.isEmpty()) {
        return val.toInteger()
    }
    return defaultValue
}

Integer getLong(String val) {
    if (val != null && !val.isEmpty()) {
        return val.toLong()
    }
    return null
}

/**
 * Функция возвращает карту атрибут значение
 */
Map getAttributesMap(event) {
    def result = [:]
    Iterator iterator = event.getAttributes()
    while (iterator.hasNext()) {
        Attribute attribute = (Attribute) iterator.next()
        QName name = attribute.getName()
        String value = attribute.getValue()
        result.put(name, value)
    }
    return result;
}

Map operationStatusRowMapper(generatedId, attrMap) {
    def Map recordsMap = new HashMap<String, Object>()
    //меняем OPERSTATID на целочисленный идентификатор
    recordsMap.put('ID', generatedId)
    recordsMap.put('NAME', attrMap.get(QName.valueOf('NAME')))
    return recordsMap;
}


Map addressObjectTypeRowMapper(generatedId, attrMap) {
    def Map recordsMap = new HashMap<String, Object>()
    recordsMap.put('ID', generatedId) //добавляем ID для использования в справочниках
    recordsMap.put('SCNAME', attrMap.get(QName.valueOf('SCNAME')))
    recordsMap.put('SOCRNAME', attrMap.get(QName.valueOf('SOCRNAME')))
    recordsMap.put('KOD_T_ST', attrMap.get(QName.valueOf('KOD_T_ST')))
    return recordsMap;
}


Map addressObjectRowMapper(addressObjectGuidsMap, attrMap) {
    def Map recordsMap = new HashMap<String, Object>();
    def addressObjectGuid = attrMap.get(QName.valueOf('AOGUID'))
    def parentGuid = attrMap.get(QName.valueOf('PARENTGUID'))

    //меняем AOGUID и PARENTGUID на целочисленный идентификатор
    recordsMap.put('ID', addressObjectGuidsMap.get(addressObjectGuid))
    recordsMap.put('PARENTGUID', addressObjectGuidsMap.get(parentGuid))
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
    //Поле тип адресации. Хотя поле обязательное в выгрузке его нет, ставим значение 0 - не определено
    recordsMap.put('DIVTYPE', getInteger(attrMap.get(QName.valueOf('DIVTYPE')), 0))
    recordsMap.put('OFFNAME', attrMap.get(QName.valueOf('OFFNAME')))
    recordsMap.put('POSTALCODE', attrMap.get(QName.valueOf('POSTALCODE')))


    return recordsMap;
}


Map houseRowMapper(generatedId, addressObjectGuidMap, houseGuidMap, attrMap) {
    def Map recordsMap = new HashMap<String, Object>()
    def houseGuid = attrMap.get(QName.valueOf('HOUSEGUID'))

    //сохраняем HOUSEGUID и соответсвующий ему идентификатор в карту для использования в room
    houseGuidMap.put(houseGuid, generatedId)

    //меняем HOUSEGUID на целочисленный идентификатор
    recordsMap.put('ID', generatedId)
    recordsMap.put('AOGUID', addressObjectGuidMap.get(attrMap.get(QName.valueOf('AOGUID'))))

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

Map houseIntervalRowMapper(generatedId, addressObjectGuidMap, attrMap) {
    def Map recordsMap = new HashMap<String, Object>();

    //меняем INTGUID на целочисленный идентификатор
    recordsMap.put('ID', generatedId)
    recordsMap.put('AOGUID', addressObjectGuidMap.get(attrMap.get(QName.valueOf('AOGUID'))))

    recordsMap.put('INTSTART', getInteger(attrMap.get(QName.valueOf('INTSTART'))))
    recordsMap.put('INTEND', getInteger(attrMap.get(QName.valueOf('INTEND'))))
    recordsMap.put('INTSTATUS', getInteger(attrMap.get(QName.valueOf('INTSTATUS'))))
    recordsMap.put('COUNTER', getInteger(attrMap.get(QName.valueOf('COUNTER'))))
    recordsMap.put('POSTALCODE', attrMap.get(QName.valueOf('POSTALCODE')))
    return recordsMap;
}

Map roomRowMapper(generatedId, houseGuidMap, attrMap) {
    def Map recordsMap = new HashMap<String, Object>();

    //меняем ROOMGUID на целочисленный идентификатор
    recordsMap.put('ID', generatedId)
    recordsMap.put('HOUSEGUID', houseGuidMap.get(attrMap.get(QName.valueOf('HOUSEGUID'))))

    recordsMap.put('REGIONCODE', attrMap.get(QName.valueOf('REGIONCODE')))
    recordsMap.put('FLATNUMBER', attrMap.get(QName.valueOf('FLATNUMBER')))
    recordsMap.put('FLATTYPE', getInteger(attrMap.get(QName.valueOf('FLATTYPE'))))
    recordsMap.put('LIVESTATUS', getInteger(attrMap.get(QName.valueOf('LIVESTATUS'))))
    recordsMap.put('ROOMNUMBER', attrMap.get(QName.valueOf('ROOMNUMBER')))
    recordsMap.put('ROOMTYPEID', getInteger(attrMap.get(QName.valueOf('ROOMTYPEID'))))
    recordsMap.put('POSTALCODE', attrMap.get(QName.valueOf('POSTALCODE')))
    return recordsMap;
}

/**
 * Построение карты с идентификаторами адресных объектов
 * @param fiasInputStream xml-файл импорта
 * @return карта соответствия guid из фиас к целочисленному идентификатору
 */
def buildAddressObjectGuidsMap(fiasInputStream) {

    println "Start build address object guid's map!"

    def result = [:]
    def time = System.currentTimeMillis()
    def objectName = QName.valueOf('Object')
    def aoguidAttrName = QName.valueOf('AOGUID')

    def xmlFactory = XMLInputFactory.newInstance()
    xmlFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE)
    xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE)

    def reader = xmlFactory.createXMLEventReader(fiasInputStream)

    try {
        def i = 0;
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent()
            if (event.isStartElement() && event.getName().equals(objectName)) {
                Map attrMap = getAttributesMap(event)
                def guid = attrMap.get(aoguidAttrName)
                //if ((i % 500000) == 0) {println "${i} rows process..."}
                result.put(guid, i.longValue())
                i++;
            }
        }
    } finally {
        reader?.close()
    }

    println "Addres object guid's map buid end (" + (System.currentTimeMillis() - time) + " ms)";

    return result
}





