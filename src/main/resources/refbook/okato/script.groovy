package refbook.okato

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.ScriptStatus
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecord
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue

import javax.xml.namespace.QName
import javax.xml.stream.XMLInputFactory
import java.text.SimpleDateFormat

/**
 * Коды ОКАТО
 * blob_data.id = '99462c1e-1376-4fbe-8e31-eceb4ca470af'
 * ref_book_id = 3
 *
 * @author Stanislav Yasinskiy
 * @author Levykin
 */
switch (formDataEvent) {
    case FormDataEvent.IMPORT:
        importFromXML()
        break
}

// Получение строки для фильтрации записей по кодам ОКАТО
def getFilterString(def tempList) {
    def retVal = ''
    tempList?.each { okato ->
        retVal <<= "OKATO='$okato' or "
    }
    if (tempList != null && !tempList.isEmpty()) {
        retVal = retVal.substring(0, retVal.length() - 3)
    }
    return retVal
}

// Импорт записей из XML-файла
void importFromXML() {
    println("Import OKATO: Start " + System.currentTimeMillis())
    def reader
    def addByVersionMap = [:] // Обновляемые/Создаваемые (Версия -> Список значений)
    def delByVersionMap = [:] // Удаляемые (Версия -> Список значений)
    def dataProvider = refBookFactory.getDataProvider(3L)

    def sdf = new SimpleDateFormat('yyyy.MM.dd')
    try {
        def xmlFactory = XMLInputFactory.newInstance()
        xmlFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE)
        xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE)
        reader = xmlFactory.createXMLStreamReader(inputStream)

        def isTransactionStart = false // <transaction group="okato"> - событие импорта ОКАТО
        def isTableStart = false // <table name="OKATO"> - начало таблицы
        def isReplaceStart = false // <replace> - событие замены справочника
        def isRecordStart = false // <record> - запись
        def rolloutQN = QName.valueOf('rollout')
        def transactionQN = QName.valueOf('transaction')
        def tableQN = QName.valueOf('table')
        def fieldQN = QName.valueOf('field')
        def recordQN = QName.valueOf('record')
        def replaceQN = QName.valueOf('replace')
        def recordValueMap // Значение справочника
        def typeAkt // Атрибут 'TYPEAKT'
        def datAkt // Атрибут 'DATAKT'

        while (reader.hasNext()) {
            if (reader.startElement) {
                if (isTransactionStart
                        && isTableStart
                        && isReplaceStart
                        && isRecordStart
                        && reader.name.equals(fieldQN)) {
                    // Атрибуты записи
                    def name = reader.getAttributeValue(null, 'name')
                    if ('NAME1'.equals(name)) {
                        def refBookValue = new RefBookValue(RefBookAttributeType.STRING,
                                replaceQuotes(reader.getAttributeValue(null, 'value')))
                        recordValueMap.NAME = refBookValue
                    } else if ('KOD'.equals(name)) {
                        def refBookValue = new RefBookValue(RefBookAttributeType.STRING,
                                reader.getAttributeValue(null, 'value'))
                        recordValueMap.OKATO = refBookValue
                    } else if ('TYPEAKT'.equals(name)) {
                        typeAkt = reader.getAttributeValue(null, 'value')
                    } else if ('DATAKT'.equals(name)) {
                        datAkt = sdf.parse(reader.getAttributeValue(null, 'value'))
                    }
                } else if (reader.name.equals(recordQN)) {
                    isRecordStart = true
                    recordValueMap = [:]
                    typeAkt = null
                    datAkt = null
                } else if (reader.name.equals(rolloutQN)) {
                    def dateSet = sdf.parse(reader.getAttributeValue(null, 'dateSet'))
                    println("Import OKATO: " + sdf.format(dateSet) + " " + System.currentTimeMillis())
                } else if (reader.name.equals(transactionQN)
                        && 'okato'.equalsIgnoreCase(reader.getAttributeValue(null, 'group'))) {
                    isTransactionStart = true
                } else if (reader.name.equals(tableQN)
                        && 'okato'.equalsIgnoreCase(reader.getAttributeValue(null, 'name'))) {
                    isTableStart = true
                } else if (reader.name.equals(replaceQN)) {
                    isReplaceStart = true
                }
            } else if (reader.endElement) {
                if (reader.name.equals(recordQN)) {
                    def activeMap
                    if ('00'.equals(typeAkt)
                            || '02'.equals(typeAkt)
                            || '03'.equals(typeAkt)) {
                        // Список на добавление/обновление/включение
                        activeMap = addByVersionMap
                    } else if ('01'.equals(typeAkt)) {
                        // Список на исключение
                        activeMap = delByVersionMap
                    }
                    if (activeMap != null) {
                        if (activeMap.containsKey(datAkt)) {
                            activeMap.get(datAkt).put(recordValueMap.OKATO.stringValue, recordValueMap)
                        } else {
                            def recordMap = [:]
                            recordMap.put(recordValueMap.OKATO.stringValue, recordValueMap)
                            activeMap.put(datAkt, recordMap)
                        }
                    }
                    isRecordStart = false
                } else if (reader.name.equals(transactionQN)) {
                    isTransactionStart = false
                } else if (reader.name.equals(tableQN)) {
                    isTableStart = false
                } else if (reader.name.equals(replaceQN)) {
                    isReplaceStart = false
                }
            }
            reader.next()
        }
    } finally {
        reader?.close()
    }

    println("Import OKATO: Parse end " + System.currentTimeMillis())
    println("Import OKATO: Add versions count = ${addByVersionMap.size()}, Del versions count = ${delByVersionMap.size()}")

    // Сортировка версий
    def versions = []
    versions.addAll(addByVersionMap.keySet())
    versions.addAll(delByVersionMap.keySet())
    Collections.sort(versions)

    def hasChanges = false

    // По версиям
    versions.each { version ->
        // Добавляемые/обновляемые записи
        def addMap = addByVersionMap.get(version)
        // Удаляемые записи
        def delMap = delByVersionMap.get(version)

        println()
        println("Import OKATO: VERSION ${sdf.format(version)}")
        println("Import OKATO: Add/Upd candidate record count = ${addMap == null ? 0 : addMap.size()}, " +
                "Del candidate record count = ${delMap == null ? 0 : delMap.size()}")

        // Список записей для сохранения в текущей версии (updateRecords)
        def addList = []

        // Список записей для сохранения в текущей версии (insertRecords)
        def addNewList = []

        // Список записей для удаления в текущей версии (удаление записи updateRecordsVersionEnd)
        def delList = []

        // Список id записей для удаления в текущей версии (удаление версии deleteRecordVersions)
        def delRecList = []

        // Список id записей для изменения в текущей версии (refBookOkatoDao#updateValueNames)
        def updList = []

        // Коды окато, записи по которым будут изменены в текущей версии
        def okatoSet = [] as Set
        if (addMap != null) {
            okatoSet.addAll(addMap.keySet())
        }
        if (delMap != null) {
            okatoSet.addAll(delMap.keySet())
        }

        // Получение частями актуальных записей для сравнения с записями из ТФ
        def tempList = []
        def actualRecordList = []
        okatoSet.each { okato ->
            if (tempList.size() < 500) {
                tempList.add(okato)
            } else {
                actualRecordList.addAll(dataProvider.getRecords(version, null, getFilterString(tempList), null))
                tempList.clear()
            }
        }
        if (!tempList.isEmpty()) {
            actualRecordList.addAll(dataProvider.getRecords(version, null, getFilterString(tempList), null))
            tempList.clear()
        }

        println("Import OKATO: Current found record count = " + actualRecordList.size())

        // Построение Map для списка актуальных записей
        def actualRecordMap = [:]
        actualRecordList.each { actualMap ->
            actualRecordMap.put(actualMap.OKATO.stringValue, actualMap)
        }

        // Поиск добавляемых и поиск обновляемых среди добавляемых (при совпадении версий)
        addMap?.each { okato, value ->
            def actualValue = actualRecordMap.get(okato)
            def actualName = actualValue?.NAME?.stringValue
            if (actualName == null || !actualName.equals(value.NAME.stringValue)) {
                if (actualName == null) {
                    // Запись новая
                    addNewList.add(value)
                } else {
                    // Запись уже была
                    value.put(RefBook.RECORD_ID_ALIAS, actualValue.get(RefBook.RECORD_ID_ALIAS))
                    addList.add(value)
                }
            }
        }

        // Поиск удаляемых
        delMap?.keySet()?.each { okato ->
            def actualValue = actualRecordMap.get(okato)
            if (actualValue != null) {
                // Запись нашлась
                delList.add(actualValue)
            }
        }

        // Проверка добавляемых на совпадение версий
        def checkIdsAdd = [] as Set
        def checkIdsDel = [] as Set

        addList.each { map ->
            def okato = map.OKATO.stringValue
            def actualValue = actualRecordMap.get(okato)
            if (actualValue != null) {
                checkIdsAdd.add(actualValue.get(RefBook.RECORD_ID_ALIAS).numberValue)
            }
        }

        delList.each { map ->
            def okato = map.OKATO.stringValue
            def actualValue = actualRecordMap.get(okato)
            if (actualValue != null) {
                checkIdsDel.add(actualValue.get(RefBook.RECORD_ID_ALIAS).numberValue)
            }
        }

        if (!checkIdsAdd.isEmpty()) {
            // Получение версий для проверяемых записей
            def versionMap = dataProvider.getRecordsVersionStart(new ArrayList(checkIdsAdd))
            addList.each { map ->
                def okato = map.OKATO.stringValue
                def actualValue = actualRecordMap.get(okato)
                if (actualValue != null) {
                    def recordId = actualValue.get(RefBook.RECORD_ID_ALIAS).numberValue
                    def recVersion = versionMap.get(recordId)
                    if (recVersion.equals(version)) {
                        println("Import OKATO: Found update okato = " + okato)
                        // Не добавление новой версии, а обновление имеющеся
                        updList.add(map)
                    }
                }
            }
            // Обновляемые не должны добавляться
            addList.removeAll(updList)
        }

        if (!checkIdsDel.isEmpty()) {
            // Получение версий для проверяемых записей
            def versionMap = dataProvider.getRecordsVersionStart(new ArrayList(checkIdsDel))
            def delFromList = []
            delList.each { map ->
                def okato = map.OKATO.stringValue
                def actualValue = actualRecordMap.get(okato)
                if (actualValue != null) {
                    def recordId = actualValue.get(RefBook.RECORD_ID_ALIAS).numberValue
                    def recVersion = versionMap.get(recordId)
                    if (recVersion.equals(version)) {
                        println("Import OKATO: Found delete record okato = " + okato)
                        // Не удаление записи, а удаление версии
                        delRecList.add(recordId)
                        delFromList.add(map)
                    }
                }
            }

            // Удаляемые версии не должны удаляться как удаление элементов
            delList.removeAll(delFromList)
        }

        println("Import OKATO: Delete element count = ${delList.size()}, Delete version count = ${delRecList.size()},  Add element count = ${addList.size()}, Add version count = ${addNewList.size()}, " +
                "Upd element count = ${updList.size()}")

        if (!delList.isEmpty() || !delRecList.isEmpty() || !addList.isEmpty() || !addNewList.isEmpty() || !updList.isEmpty()) {
            println("Import OKATO: DB update/create begin " + System.currentTimeMillis())

            // Удаление записей, которые изменились в текущей версии и которые при импорте были отмечены как удаляемые
            if (!delList.isEmpty()) {
                def delIds = []
                for (def map : delList) {
                    delIds.add(map.get(RefBook.RECORD_ID_ALIAS).numberValue)
                }
                dataProvider.updateRecordsVersionEnd(logger, version, delIds)
            }

            // Удаление записей, которые изменились в текущей версии и которые при импорте были отмечены как удаляемые при этом версия совпадает с датой удаления
            if (!delRecList.isEmpty()) {
                dataProvider.deleteRecordVersions(logger, delRecList)
            }

            // Обновление записей, которые изменились, но дата совпадает с датой создания
            if (!updList.isEmpty()) {
                refBookOkatoDao.updateValueNames(version, updList)
            }

            // Добавление новых версии
            if (!addNewList.isEmpty()) {
                // Добавление порциями
                tempList = []
                addNewList.each { record ->
                    if (tempList.size() < 500) {
                        tempList.add(record)
                    } else {
                        dataProvider.insertRecords(version, tempList)
                        tempList.clear()
                    }
                }
                if (!tempList.isEmpty()) {
                    dataProvider.insertRecords(version, tempList)
                    tempList.clear()
                }
            }

            // Добавление элементов
            if (!addList.isEmpty()) {
                dataProvider.updateRecords(version, addList)
            }

            println("Import OKATO: DB update/create end " + System.currentTimeMillis())
            hasChanges = true
        }
        if (logger.containsLevel(LogLevel.ERROR)) {
            return
        }
    }
    println("Import OKATO: End " + System.currentTimeMillis())

    if (!hasChanges) {
        scriptStatusHolder.setScriptStatus(ScriptStatus.SKIP)
        scriptStatusHolder.setStatusMessage("Файл не содержит новые записи или изменения к существующим записям!")
    } else {
        if (!logger.containsLevel(LogLevel.ERROR)) {
            scriptStatusHolder.setScriptStatus(ScriptStatus.SUCCESS)
        }
    }
}