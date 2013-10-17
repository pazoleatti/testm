package refbook.organization

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
/**
 * Cкрипт справочника «Организации - участники контролируемых сделок»
 *
 * @author Stanislav Yasinskiy
 */
switch (formDataEvent) {
    case FormDataEvent.IMPORT:
        importFromXLS()
        break
}

void importFromXLS() {

    if (inputStream == null) {
        logger.error('Поток данных пуст')
        return
    }

    def xmlString = importService.getData(inputStream, ".xls", 'windows-1251', 'Наименование организации', null)
    if (xmlString == null) {
        logger.error('Отсутствие значении после обработки потока данных')
        return
    }

    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null) {
        logger.error('Отсутствие значении после обработки потока данных')
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
}

// Проверить шапку таблицы
def checkTableHead(def xml) {
    def colCount = 11
    if (xml.row[0].cell.size() < colCount) {
        return false
    }
    def cells = xml.row[0].cell
    return cells[0] == 'Наименование организации' &&
            cells[1] == 'Страна регистрации' &&
            cells[2] == 'Регистрационный номер организации в стране ее регистрации (инкорпорации)' &&
            cells[3] == 'Код налогоплательщика в стране регистрации (инкорпорации) или его аналог ' &&
            cells[4] == 'Адрес организации' &&
            cells[5] == 'ИНН / КИО' &&
            cells[6] == 'КПП' &&
            cells[7] == 'Сведения об организации' &&
            cells[8] == 'Резидент оффшорной зоны' &&
            cells[9] == 'Дополнительная информация' &&
            cells[10] == 'Освобождена от налога на прибыль либо является резидентом Сколково'
}

/**
 * Импорт данных
 */
def addData(def xml) {

    def List<Map<String, RefBookValue>> insertList = new LinkedList<Map<String, RefBookValue>>()
    def List<Map<String, RefBookValue>> updateList = new ArrayList<Map<String, RefBookValue>>()

    def cache = [:]
    def date = new Date()
    def indexRow = 0
    for (def row : xml.row) {
        indexRow++
        // Шапка
        if (row == xml.row[0]) {
            continue
        }
        def Map recordsMap = new HashMap<String, RefBookValue>()
        indexCell = 0
        def recordID = getRecordId(9, 'NAME', row.cell[indexCell].text(), date, cache, 0, 0)

        recordsMap.put("NAME",          new RefBookValue(RefBookAttributeType.STRING,       row.cell[indexCell++].text()))
        recordsMap.put("COUNTRY",       new RefBookValue(RefBookAttributeType.REFERENCE,    getRecordId(10, 'NAME', row.cell[indexCell].text(), date, cache, indexRow, indexCell++)))
        recordsMap.put("REG_NUM",       new RefBookValue(RefBookAttributeType.STRING,       row.cell[indexCell++].text()))
        recordsMap.put("TAXPAYER_CODE", new RefBookValue(RefBookAttributeType.STRING,       row.cell[indexCell++].text()))
        recordsMap.put("ADDRESS",       new RefBookValue(RefBookAttributeType.STRING,       row.cell[indexCell++].text()))
        recordsMap.put("INN_KIO",       new RefBookValue(RefBookAttributeType.STRING,       row.cell[indexCell++].text()))
        recordsMap.put("KPP",           new RefBookValue(RefBookAttributeType.NUMBER,       getNumber(row.cell[indexCell++].text())))
        recordsMap.put("ORGANIZATION",  new RefBookValue(RefBookAttributeType.REFERENCE,    getRecordId(70, 'CODE', row.cell[indexCell].text(), date, cache, indexRow, indexCell++)))
        recordsMap.put("OFFSHORE",      new RefBookValue(RefBookAttributeType.REFERENCE,    getRecordId(38, 'CODE', row.cell[indexCell].text(), date, cache, indexRow, indexCell++)))
        recordsMap.put("DOP_INFO",      new RefBookValue(RefBookAttributeType.STRING,       row.cell[indexCell++].text()))
        recordsMap.put("SKOLKOVO",      new RefBookValue(RefBookAttributeType.REFERENCE,    getRecordId(38, 'CODE', row.cell[indexCell].text(), date, cache, indexRow, indexCell)))

        if (recordID != null) {
            recordsMap.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, recordID))
            updateList.add(recordsMap)
        } else {
            insertList.add(recordsMap)
        }
    }

    def refDataProvider = refBookFactory.getDataProvider(9)
    if (updateList.size() > 0)
        refDataProvider.updateRecords(new Date(), updateList)
    if (insertList.size() > 0)
        refDataProvider.insertRecords(new Date(), insertList)
}

def getNumber(def value) {
    if (value == null || "".equals(value.trim())) {
        return null
    }

    def tr =  value.trim()

    if (!tr.matches("-?\\d+(.\\d+)?")) {
        return null
    }

    return new BigDecimal(tr)
}

def getRecordId(def ref_id, String alias, String value, Date date, def cache, int indexRow, int indexCell) {
    String filter = "LOWER($alias) = LOWER('$value')"
    if (cache[ref_id] != null) {
        if (cache[ref_id][filter] != null) return cache[ref_id][filter]
    } else {
        cache[ref_id] = [:]
    }
    def refDataProvider = refBookFactory.getDataProvider(ref_id)
    def records = refDataProvider.getRecords(date, null, filter, null).getRecords()
    if (records.size() == 1) {
        cache[ref_id][filter] = (records.get(0).record_id.toString() as Long)
        return cache[ref_id][filter]
    }
    if (indexRow != 0 && alias!=null && "".equals(alias.trim()))
        throw new Exception("Строка ${indexRow -1} столбец ${indexCell+1} содержит значение, отсутствующее в справочнике!")
    return null
}