package refbook.vehicles_average_cost

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.ScriptStatus
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import groovy.transform.Field

import java.text.SimpleDateFormat

/**
 * blob_data.id = '31bac73f-dc1b-49c1-b278-cb42d2588600'
 *
 * Скрипт справочника "Средняя стоимость транспортных средств"
 *
 * @author Bulat Kinzyabulatov
 */
switch (formDataEvent) {
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importFromXls()
        break
}

@Field
def REFBOOK_ID = 208

@Field
def providerCache = [:]

@Field
def EMPTY_DATA_ERROR = "Сообщение не содержит значений, соответствующих загружаемым данным!"

void importFromXls() {

    if (inputStream == null) {
        logger.error('Поток данных пуст')
        return
    }

    try {
        def xmlString = importService.getData(inputStream, ".xlsx", 'windows-1251', 'Средняя стоимость', null)
    } catch (Exception e) {
        logger.error("Неверная структура загружаемого файла")
        return
    }
    if (xmlString == null || xmlString.isEmpty()) {
        logger.error('Отсутствие значений после обработки потока данных')
        return
    }

    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null || xml.empty) {
        logger.error('Отсутствие значений после обработки потока данных')
        return
    }

    try {
        if (!checkTableHead(xml)) {
            logger.error('Заголовок таблицы не соответствует требуемой структуре!')
            return
        }
        addData(xml)
    } catch (Exception e) {
        logger.error(e.message)
    }

    if (!logger.containsLevel(LogLevel.ERROR)) {
        scriptStatusHolder.setScriptStatus(ScriptStatus.SUCCESS)
    }
}

// Проверить шапку таблицы
def checkTableHead(def xml) {
    def colCount = 3
    if (xml.row[0].cell.size() < colCount) {
        return false
    }
    def cells = xml.row[0].cell
    return cells[0] == 'Средняя стоимость' &&
            cells[1] == 'Марка' &&
            cells[2] == 'Модель (Версия)'
}

// Импорт данных
def addData(def xml) {
    def SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy")

    def Map<Date, ArrayList<Map<String, RefBookValue>>> insertVersionMap = new HashMap<Date, ArrayList<Map<String, RefBookValue>>>()
    def Map<Date, ArrayList<Map<String, RefBookValue>>> updateVersionMap = new HashMap<Date, ArrayList<Map<String, RefBookValue>>>()

    def recordIdCache = [:]
    def date = new Date()
    def indexRow = 0
    def dataProvider = formDataService.getRefBookProvider(refBookFactory, REFBOOK_ID, providerCache)
    for (def row : xml.row) {
        indexRow++
        // Шапка
        if (row == xml.row[0]) {
            continue
        }
        def Map recordsMap = new HashMap<String, RefBookValue>()
        def recordIds = dataProvider.getUniqueRecordIds(null, "LOWER(BREND) = LOWER('${row.cell[1].text()}') AND LOWER(MODEL) = LOWER('${row.cell[2].text()}')")
        def version
        def recordId
        if (row.cell[3].text()) {
            version = sdf.parse(row.cell[3].text())
        } else {
            if (recordIds.empty) {
                version = sdf.parse("01.03.${date[Calendar.YEAR]}")
            } else {
                def maxRecordVersion = dataProvider.getRecordsVersionStart(recordIds)?.max { it.value }
                def lastVersion = maxRecordVersion?.value
                if (lastVersion) {
                    version = sdf.parse("01.03.${lastVersion[Calendar.YEAR] + 1}")
                }
                recordId = maxRecordVersion.key
            }
        }

        if (version != null) {
            def avgCostRecordId = getRecordId(211, 'CODE', row.cell[0].text(), date, recordIdCache, 0, 0)
            if (recordId) {
                if (dataProvider.getRecordData(recordId)?.AVG_COST?.value == avgCostRecordId) {
                    continue
                }
                recordsMap.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, recordId))
            }
            recordsMap.put("AVG_COST", new RefBookValue(RefBookAttributeType.REFERENCE, avgCostRecordId))
            recordsMap.put("BREND", new RefBookValue(RefBookAttributeType.STRING, row.cell[1].text()))
            recordsMap.put("MODEL", new RefBookValue(RefBookAttributeType.STRING, row.cell[2].text()))

            if (recordIds.size() == 1) {
                addToMapListMap(updateVersionMap, version, recordsMap)
            } else {
                addToMapListMap(insertVersionMap, version, recordsMap)
            }
        }
    }

    if (insertVersionMap.empty && updateVersionMap.empty) {
        logger.warn(EMPTY_DATA_ERROR)
        return
    }
    if (!insertVersionMap.empty) {
        for (def insertEntry : insertVersionMap) {
            dataProvider.insertRecordsWithoutLock(userInfo, insertEntry.key, insertEntry.value)
        }
    }
    if (!updateVersionMap.empty) {
        for (def updateEntry : updateVersionMap) {
            dataProvider.updateRecordsWithoutLock(userInfo, updateEntry.key, updateEntry.value)
        }
    }
}

def getRecordId(def ref_id, String alias, String value, Date date, def recordIdCache, int indexRow, int indexCell) {
    String filter = "LOWER($alias) = LOWER('$value')"
    if (recordIdCache[ref_id] != null) {
        if (recordIdCache[ref_id][filter] != null) return recordIdCache[ref_id][filter]
    } else {
        recordIdCache[ref_id] = [:]
    }
    def refDataProvider = refBookFactory.getDataProvider(ref_id)
    def records = refDataProvider.getRecords(date, null, filter, null)
    if (records.size() == 1) {
        recordIdCache[ref_id][filter] = (records.get(0).record_id.toString() as Long)
        return recordIdCache[ref_id][filter]
    }
    if (indexRow != 0 && alias!=null && "".equals(alias.trim()))
        throw new Exception("Строка ${indexRow -1} столбец ${indexCell+1} содержит значение, отсутствующее в справочнике «" + refBookFactory.get(ref_id).getName()+"»!")
    return null
}

void addToMapListMap (def mapListMap, def key, def innerMap) {
    if (mapListMap.get(key) != null) {
        mapListMap.get(key).add(innerMap)
    } else {
        mapListMap.put(key, [innerMap])
    }

}