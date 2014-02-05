/*
    blob_data.id = '8891efea-5d2d-4f0e-bc63-6349f354b48d'
    Коды субъектов Российской Федерации
 */
package refbook.region

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.log.Logger
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import groovy.transform.Field

import javax.xml.namespace.QName
import javax.xml.stream.XMLInputFactory
import java.text.SimpleDateFormat

/**
 * Cкрипт справочника «Коды субъектов Российской Федерации»
 * TODO требуются доработки в БД
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
        'Ханты - Мансийский автономный округ': '71100',
        'Ямало - Ненецкий автономный округ': '71140']

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
    println("Import Region: Start " + System.currentTimeMillis())
    // Текущая дата - используется для ссылки на записи справочника "ОКАТО"
    def actualDate =  new GregorianCalendar(2012, Calendar.JANUARY, 1) // "01.01.2012" Версия для добавляемых записей
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
                map.OKATO_DEFINITION.value = map.OKATO_DEFINITION.stringValue.padRight(11, '0')
                okatoList.add(map.OKATO_DEFINITION)
            }
        }

        println("Import Region: okatoList.size = " + okatoList.size())
        println("Import Region: okatoList = " + okatoList)

        // Поиск записей ОКАТО
        if (!okatoList.isEmpty()) {
            // Актуальные записи ОКАТО
            def actualOkatoRecordList = dataProviderOKATO.getRecords(actualDate, null, getFilterString(okatoList), null)

            println("Import Region: Current OKATO found record count = " + actualOkatoRecordList?.size())

            // Построение Map для списка актуальных записей
            def actualOkatoRecordMap = [:]
            actualOkatoRecordList.each { actualMap ->
                actualOkatoRecordMap.put(actualMap.OKATO, actualMap)
            }

            // Список записей для сохранения
            def addList = []

            // Список id записей для удаления
            def delList = []

            // Список id записей для обновления
            def updList = []

            // Подстановка ссылок на ОКАТО
            for (def map : addRecordList) {
                def okato = map.OKATO_DEFINITION?.stringValue
                if (okato != null) {
                    def actuaOkatolMap = actualOkatoRecordMap.get(okato)

                    if (actuaOkatolMap == null) {
                        logger.error("Для элемента «${map.NAME.stringValue}» ТФ «Коды субъектов Российской Федерации», " +
                                "в справочнике «Коды ОКАТО» не найдено соответствующее значение. " +
                                "Загрузка справочника не выполнена.")
                        return
                    } else {
                        map.OKATO = new RefBookValue(RefBookAttributeType.REFERENCE,
                                actuaOkatolMap.get(RefBook.RECORD_ID_ALIAS).numberValue)
                    }
                }
            }

            // TODO Подстановка ссылок на ОКТМО

            // Актуальные записи Регионов
            def actualRegionRecordList = dataProvider.getRecords(actualDate, null, null, null)

            println("Import Region: Current Region found record count = " + actualRegionRecordList?.size())

            // Построение Map для списка актуальных записей
            def actualRegionRecordMap = [:]
            actualRegionRecordList.each { actualMap ->
                actualRegionRecordMap.put(actualMap.CODE, actualMap)
            }

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
                            || map?.OKATO_DEFINITION?.stringValue != actualMap.OKATO_DEFINITION?.stringValue
                            || map.OKATO.stringValue != actualMap.OKATO?.stringValue) { // TODO проверять новые атрибуты
                        // Новая запись отличается от имеющейся
                        if (true) { // TODO Условие (Нужна функция. Делает Д. Лошкарев.)
                            delList.add(actualMap)
                            addList.add(map)
                        } else {
                            updList.add(map)
                        }
                    }
                }
            }

            println(">>>>>>>> delList = ${delList.size()} addList = ${addList.size()} updList = ${updList.size()}")

            // Сохранение изменений в БД
            if (!delList.isEmpty()) {
                // dataProvider.updateRecordsVersionEnd(logger, actualDate, delList)
            }
            if (!addList.isEmpty()) {
                // dataProvider.createRecordVersion(logger, null, actualDate, null, addList)
            }
            if (!updList.isEmpty()) {
                // TODO Метод обновления записей
            }
        }
    }

    // TODO ОКТМО

    println("Import Region: End " + System.currentTimeMillis())
    if (!logger.containsLevel(LogLevel.ERROR)) {
        logger.info("Импорт успешно выполнен.")
    }
}
