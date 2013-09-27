package refbook.organization

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
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
    println("importFromXLS")

    if (inputStream == null) {
        logger.error('Поток данных пуст')
        return
    }

    def xmlString = importService.getData(inputStream, ".xls", 'windows-1251', 'Наименование организации', null)
    if (xmlString == null) {
        logger.error('Отсутствие значении после обработки потока данных')
        return
    }

    // println(xmlString)

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

    // def final Long refBookID = 9L
//    def dataProvider = refBookFactory.getDataProvider(refBookID)
//    def RefBook refBook = refBookFactory.get(refBookID)
//    def SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd")

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

    for (def row : xml.row) {
        // Шапка
        if (row == xml.row[0]) {
            continue
        }

        def Map recordsMap = new HashMap<String, RefBookValue>()

//        recordsMap.put("NAME", new RefBookValue(RefBookAttributeType.STRING, row.cell[0].text()))
//        recordsMap.put("COUNTRY", new RefBookValue(RefBookAttributeType.REFERENCE,))
//        recordsMap.put("REG_NUM", new RefBookValue(RefBookAttributeType.STRING,))
//        recordsMap.put("TAXPAYER_CODE", new RefBookValue(RefBookAttributeType.STRING,))
//        recordsMap.put("ADDRESS", new RefBookValue(RefBookAttributeType.STRING,))
//        recordsMap.put("INN_KIO", new RefBookValue(RefBookAttributeType.STRING,))
//        recordsMap.put("KPP", new RefBookValue(RefBookAttributeType.NUMBER,))
//        recordsMap.put("ORGANIZATION", new RefBookValue(RefBookAttributeType.NUMBER,))
//        recordsMap.put("OFFSHORE", new RefBookValue(RefBookAttributeType.REFERENCE,))
//        recordsMap.put("DOP_INFO", new RefBookValue(RefBookAttributeType.STRING,))
//        recordsMap.put("SKOLKOVO", new RefBookValue(RefBookAttributeType.REFERENCE,))

        insertList.add(recordsMap)
    }

   //  dataProvider.updateRecords(new Date(), insertList)

}