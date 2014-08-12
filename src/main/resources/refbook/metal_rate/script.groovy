package refbook.metal_rate

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.ScriptStatus
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import groovy.transform.Field

import javax.xml.namespace.QName
import javax.xml.stream.XMLInputFactory
import java.text.SimpleDateFormat

/**
 * blob_data.id = '99e90406-60f0-4a87-b6f0-7f127abf1182'
 *
 * Скрипт загрузки справочника "Курсы драгоценных металлов" из КСШ  (id = 90)
 *
 * @author Stanislav Yasinskiy
 */
switch (formDataEvent) {
    case FormDataEvent.IMPORT:
        importFromXML()
        break
}

@Field
def REFBOOK_ID = 90

@Field
def EMPTY_DATA_ERROR = "Сообщение не содержит значений, соответствующих загружаемым данным!"

void importFromXML() {
    def dataProvider = refBookFactory.getDataProvider(REFBOOK_ID)
    def SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd")
    def reader = null
    def Date version = null  //дата актуальности
    def boolean rateSector = false // флаг присутствия в секции с курсами
    def Map<String, RefBookValue> recordsMap // аттрибут и его значение
    def List<Map<String, RefBookValue>> insertList = new ArrayList<Map<String, RefBookValue>>() // Новые элементы
    def List<Map<String, RefBookValue>> updateList = new ArrayList<Map<String, RefBookValue>>() // Измененные элементы
    def Long code = null // код драг. металла
    def BigDecimal rate = null // курс драг. металла

    def fileRecords = []
    try {
        def XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE)
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE)
        reader = factory.createXMLStreamReader(inputStream)

        def metalCodeDataProvider = refBookFactory.getDataProvider(17)

        while (reader.hasNext()) {
            if (reader.startElement) {

                // Версия справочника
                if (reader.getName().equals(QName.valueOf("StartDateTime"))) {
                    version = sdf.parse(reader.getElementText())
                }

                // Дошли до секции с курсами
                if (reader.getName().equals(QName.valueOf("PreciousMetalRates"))) {
                    rateSector = true
                }

                // Код драг. металла
                if (rateSector && reader.getName().equals(QName.valueOf("Code"))) {
                    def String val = reader.getElementText()
                    def records = metalCodeDataProvider.getRecords(version, null, "LOWER(INNER_CODE) = LOWER('$val')", null)
                    if (records.size() > 0) {
                        code = records.get(0).record_id.numberValue
                    } else {
                        code = null
                        println("В справочнике «Коды драгоценных металлов» отсутствует элемент с кодом '$val'")
                    }
                }

                // Курс драг. металла
                if (rateSector && reader.getName().equals(QName.valueOf("Rate"))) {
                    rate = new BigDecimal(reader.getElementText())
                }
            }

            // Запись в лист
            if (reader.endElement && reader.getName().equals(QName.valueOf("PreciousMetalRate")) && code != null) {
                recordsMap = new HashMap<String, RefBookValue>()
                recordsMap.put("CODE", new RefBookValue(RefBookAttributeType.REFERENCE, code))
                recordsMap.put("RATE", new RefBookValue(RefBookAttributeType.NUMBER, rate))
                fileRecords.add(recordsMap)
            }
            reader.next()
        }
    } finally {
        reader?.close()
    }

    if (fileRecords.empty) {
        logger.warn(EMPTY_DATA_ERROR)
        return
    }

    // Получение идентификаторов строк
    def filterStr = ''
    fileRecords.each { record ->
        filterStr += ((record == fileRecords.getAt(0)) ? "" : " or ") + " CODE = " + record.CODE.referenceValue
    }

    def recordIds = dataProvider.getUniqueRecordIds(null, filterStr)

    // Получение записей
    def existRecords = [:]
    if (recordIds != null && !recordIds.empty) {
        existRecords = dataProvider.getRecordData(recordIds)
    }

    // CODE → Запись
    def existMap = [:]
    existRecords.each { key, record ->
        existMap.put(record.CODE.referenceValue, record)
    }

    fileRecords.each { record ->
        def existRecord = existMap[record.CODE.referenceValue]
        if (existRecord != null) {
            record.put(RefBook.RECORD_ID_ALIAS, existRecord[RefBook.RECORD_ID_ALIAS])
            updateList.add(record)
        } else {
            insertList.add(record)
        }
    }

    if (insertList.empty && updateList.empty) {
        logger.warn(EMPTY_DATA_ERROR)
        return
    }
    if (!insertList.empty) {
        dataProvider.insertRecords(version, insertList)
    }
    if (!updateList.empty) {
        dataProvider.updateRecords(version, updateList)
    }
    scriptStatusHolder.successCount = insertList.size + updateList.size
}