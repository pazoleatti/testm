/*
    blob_data.id = '8891efea-5d2d-4f0e-bc63-6349f354b48d'
    Коды субъектов Российской Федерации
 */
package refbook.region

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecord
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import groovy.transform.Field

import javax.xml.namespace.QName
import javax.xml.stream.XMLInputFactory
import java.text.SimpleDateFormat

/**
 * Cкрипт справочника «Коды субъектов Российской Федерации»
 *
 * @author Levykin
 */
switch (formDataEvent) {
    case FormDataEvent.IMPORT:
        importFromXML()
        break
}

@Field
def lstOkatoDefinition = ['Ненецкий автономный округ': '1110',
        'Ханты-Мансийский автономный округ - Югра': '71100',
        'Ямало-Ненецкий автономный округ': '71140']

// Получение строки для фильтрации записей по кодам ОКАТО
def getFilterString(def tempList) {
    def retVal = ''
    tempList?.each { okato ->
        retVal <<= "OKATO='${okato.stringValue.padRight(11,"0")}' or "
    }
    if (tempList != null && !tempList.isEmpty()) {
        retVal = retVal.substring(0, retVal.length() - 3)
    }
    return retVal
}

// Импорт записей из XML-файла
void importFromXML() {
    println("Import Region: Start " + System.currentTimeMillis())
    // Текущая дата - используется для ссылки на записи справочника "ОКАТО"
    // "01.01.2012" Версия для добавляемых записей
    def actualDate = new GregorianCalendar(2015, Calendar.JANUARY, 1).getTime()
    // def actualDate = new GregorianCalendar(2012, Calendar.JANUARY, 1).getTime()
    println("Import Region: Import date = " + new SimpleDateFormat("dd.MM.yyyy").format(actualDate))
    def dataProvider = refBookFactory.getDataProvider(4L)
    def dataProviderOKATO = refBookFactory.getDataProvider(3L)
    def reader = null

    def addRecordList = [] // Список записей для сохранения

    try {
        def factory = XMLInputFactory.newInstance()
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE)
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE)
        reader = factory.createXMLStreamReader(inputStream)

        def isReplaceStart = false // <replace>
        def isTableStart = false // <table name="PB_region"> - начало таблицы
        def isRecordStart = false // <record> - запись

        def tableQN = QName.valueOf('table')
        def fieldQN = QName.valueOf('field')
        def recordQN = QName.valueOf('record')
        def replaceQN = QName.valueOf('replace')

        def recordValueMap // Значение справочника

        while (reader.hasNext()) {
            if (reader.startElement) {
                if (isTableStart
                        && isRecordStart
                        && isReplaceStart
                        && reader.name.equals(fieldQN)) {
                    // Атрибуты записи
                    def name = reader.getAttributeValue(null, 'name')
                    if ('NAME'.equals(name)) {
                        def refBookValue = new RefBookValue(RefBookAttributeType.STRING,
                                reader.getAttributeValue(null, 'value'))
                        recordValueMap.put('NAME', refBookValue)
                    } else if ('CODE'.equals(name)) {
                        def refBookValue = new RefBookValue(RefBookAttributeType.STRING,
                                reader.getAttributeValue(null, 'value').padLeft(2, '0'))
                        recordValueMap.put('CODE', refBookValue)
                    } else if ('OKATO'.equals(name)) {
                        if ('false'.equals(reader.getAttributeValue(null, 'null'))) {
                            // ОКАТО задан
                            def refBookValue = new RefBookValue(RefBookAttributeType.STRING,
                                    reader.getAttributeValue(null, 'value'))
                            recordValueMap.put('OKATO_DEFINITION', refBookValue)
                        }
                    }
                } else if (reader.name.equals(recordQN)) {
                    isRecordStart = true
                    recordValueMap = [:]
                } else if (reader.name.equals(tableQN)
                        && 'PB_region'.equalsIgnoreCase(reader.getAttributeValue(null, 'name'))) {
                    isTableStart = true
                } else if (reader.name.equals(replaceQN)) {
                    isReplaceStart = true
                }
            } else if (reader.endElement) {
                if (reader.name.equals(recordQN)) {
                    addRecordList.add(recordValueMap)
                    isRecordStart = false
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

    // Список требуемых кодов ОКАТО
    def okatoList = []

    if (!addRecordList.isEmpty()) {
        println("File record count = " + addRecordList.size())
        // Подстановка ссылок на коды ОКАТО
        addRecordList.each { map ->
            if (!map.containsKey('OKATO_DEFINITION')) {
                def name = map.get('NAME').stringValue
                for (def lstName : lstOkatoDefinition.keySet()) {
                    if (lstName.equalsIgnoreCase(name)) {
                        map.OKATO_DEFINITION = new RefBookValue(RefBookAttributeType.STRING,
                                lstOkatoDefinition.get(lstName))
                    }
                }
            }
            if (map.containsKey('OKATO_DEFINITION')) {
                okatoList.add(map.OKATO_DEFINITION)
            }
        }

        println("Import Region: okatoList.size = " + okatoList.size())
        // println("Import Region: okatoList = " + okatoList)

        // Поиск записей ОКАТО
        def actualOkatoRecordMap = [:]
        if (!okatoList.isEmpty()) {
            // Актуальные записи ОКАТО
            def actualOkatoRecordList = dataProviderOKATO.getRecords(actualDate, null, getFilterString(okatoList), null)
            println("Import Region: Current OKATO found record count = " + actualOkatoRecordList?.size())

            // Построение Map для списка актуальных записей
            actualOkatoRecordList.each { actualMap ->
                actualOkatoRecordMap.put(actualMap.OKATO.stringValue, actualMap)
            }
        }

        // Подстановка ссылок на ОКАТО
        for (def map : addRecordList) {
            def okato = map.OKATO_DEFINITION?.stringValue
            if (okato != null && okato != '') {
                okato = okato.padRight(11,'0')
                def actuaOkatolMap = actualOkatoRecordMap.get(okato)

                if (actuaOkatolMap == null) {
                    logger.error("Для элемента «${map.NAME.stringValue}» ТФ «Коды субъектов Российской Федерации», " +
                            "в справочнике «Коды ОКАТО» не найдено значение, соответствующее коду «$okato». " +
                            "Загрузка справочника не выполнена.")
                    return
                } else {
                    map.OKATO = new RefBookValue(RefBookAttributeType.REFERENCE,
                            actuaOkatolMap.get(RefBook.RECORD_ID_ALIAS).numberValue)
                }
            }
        }

        // Подстановка ссылок на ОКТМО
        // TODO (пока нет в ЧТЗ) http://conf.aplana.com/pages/viewpage.action?pageId=9572224

        // Список записей для добавления
        def addList = []

        // Список id записей для удаления
        def delList = []

        // Список id записей для обновления
        def updList = []

        // Актуальные записи Регионов
        def actualRegionRecordList = dataProvider.getRecords(actualDate, null, null, null)

        println("Import Region: Current Region found record count = " + actualRegionRecordList?.size())

        // Код -> RECORD_ID
        def recIdMap = [:]

        // Построение Map для списка актуальных записей
        def actualRegionRecordMap = [:]
        actualRegionRecordList.each { actualMap ->
            actualRegionRecordMap.put(actualMap.CODE.stringValue, actualMap)
            recIdMap.put(actualMap.CODE.stringValue, actualMap.get(RefBook.RECORD_ID_ALIAS).numberValue)
        }

        def addRecordMap = [:]

        // Сравнение
        addRecordList.each { map ->
            def code = map.CODE.stringValue
            def actualMap = actualRegionRecordMap.get(code)
            if (actualMap == null) {
                // Запись новая
                addList.add(map)
            } else {
                // Запись обновляемая
                if (map.NAME.stringValue != actualMap.NAME.stringValue
                        || map.OKATO_DEFINITION?.stringValue != actualMap.OKATO_DEFINITION?.stringValue
                        || map.OKATO?.stringValue != actualMap.OKATO?.stringValue) {
                    // TODO проверять атрибуты ОКТМО после добавления в ЧТЗ
                    addList.add(map)
                }
            }
            addRecordMap.put(code, map)
        }

        // Проверка добавляемых на совпадение версий
        def checkIds = []
        addList.each { map ->
            def code = map.CODE.stringValue
            def actualValue = actualRegionRecordMap.get(code)
            if (actualValue != null) {
                checkIds.add(actualValue.get(RefBook.RECORD_ID_ALIAS).numberValue)
            }
        }

        if (!checkIds.isEmpty()) {
            // Получение версий для проверяемых записей
            def versionMap = dataProvider.getRecordsVersionStart(checkIds)
            addList.each { map ->
                def code = map.CODE.stringValue
                def actualValue = actualRegionRecordMap.get(code)
                if (actualValue != null) {
                    def recordId = actualValue.get(RefBook.RECORD_ID_ALIAS).numberValue
                    def recVersion = versionMap.get(recordId)
                    if (recVersion.equals(actualDate)) {
                        println("Import Region: Found update code = " + code)
                        updList.add(map)
                    }
                }
            }

            // Обновляемые не должны добавляться
            addList.removeAll(updList)
        }

        // Поиск неактуальных (есть в справочнике, но нет в файле)
        actualRegionRecordList.each { actualMap ->
            if (!addRecordMap.containsKey(actualMap.CODE.stringValue)) {
                delList.add(actualMap.get(RefBook.RECORD_ID_ALIAS).numberValue)
            }
        }

        println("Import Region: Delete count = ${delList.size()}, Add count = ${addList.size()}, " +
                "Update count = ${updList.size()}")

        // Сохранение изменений в БД
        if (!delList.isEmpty()) {
            dataProvider.updateRecordsVersionEnd(logger, actualDate, delList)
        }
        if (!addList.isEmpty()) {
            def addCreateRecordList = []
            addList.each { map ->
                def rbRecord = new RefBookRecord()
                rbRecord.setRecordId(recIdMap.get(map.CODE.stringValue))
                rbRecord.setValues(map)
                addCreateRecordList.add(rbRecord)
            }
            dataProvider.createRecordVersion(logger, actualDate, null, addCreateRecordList)
        }
        if (!updList.isEmpty()) {
            updList.each { map ->
                dataProvider.updateRecordVersion(logger, recIdMap.get(map.CODE.stringValue), actualDate, null, map)
            }
        }
    }

    // TODO ОКТМО (пока нет в ЧТЗ) http://conf.aplana.com/pages/viewpage.action?pageId=9572224

    println("Import Region: End " + System.currentTimeMillis())
    if (!logger.containsLevel(LogLevel.ERROR)) {
        logger.info("Импорт успешно выполнен.")
    }
}
