package form_template.deal.trademark

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent

import java.text.SimpleDateFormat
/**
 * 379 - Предоставление права пользования товарным знаком (4)
 *
 * похож на  software_development (Разработка, внедрение, поддержка и модификация программного обеспечения, приобретение лицензий)
 * похож на corporate_credit (Предоставление корпоративного кредита)
 * @author Stanislav Yasinskiy
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkCreation()
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        addRow()
        break
    case FormDataEvent.DELETE_ROW:
        deleteRow()
        break
// После принятия из Утверждено
    case FormDataEvent.AFTER_MOVE_APPROVED_TO_ACCEPTED:
        logicCheck()
        break
// После принятия из Подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED:
        logicCheck()
        break
// Консолидация
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT :
        importData()
        if (!logger.containsLevel(LogLevel.ERROR)) {
            calc()
            logicCheck()
        }
        break
}

void deleteRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.delete(currentDataRow)
}

void addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def row = formData.createDataRow()
    def dataRows = dataRowHelper.getAllCached()
    def size = dataRows.size()
    def index = currentDataRow != null ? (currentDataRow.getIndex()+1) : (size == 0 ? 1 : (size+1))
    row.keySet().each{
        row.getCell(it).setStyleAlias('Автозаполняемая')
    }
    ['fullNamePerson', 'sum', 'docNumber', 'docDate', 'dealDate'].each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    dataRowHelper.insert(row, index)
}

/**
 * Проверяет уникальность в отчётном периоде и вид
 * (не был ли ранее сформирован отчет, параметры которого совпадают с параметрами, указанными пользователем )
 */
void checkCreation() {
    def findForm = formDataService.find(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId)
    if (findForm != null) {
        logger.error('Формирование нового отчета невозможно, т.к. отчет с указанными параметрами уже сформирован.')
    }
}

/**
 * Логические проверки
 */
void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def taxPeriod = reportPeriodService.get(formData.reportPeriodId).taxPeriod
    def dFrom = taxPeriod.getStartDate()
    def dTo = taxPeriod.getEndDate()

    int index = 1
    for (row in dataRowHelper.getAllCached()) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = index++
        def docDateCell = row.getCell('docDate')
        [
                'rowNumber',     // № п/п
                'fullNamePerson',// Полное наименование юридического лица с указанием ОПФ
                'inn',           // ИНН/КИО
                'countryCode',   // Код страны регистрации по классификатору ОКСМ
                'sum',           // Сумма расходов Банка, руб.
                'docNumber',     // Номер договора
                'docDate',       // Дата договора
                'price',         // Цена
                'cost',          // Стоимость
                'dealDate'       // Дата совершения сделки
        ].each {
            def rowCell = row.getCell(it)
            if (rowCell.value == null || rowCell.value.toString().isEmpty()) {
                def msg = rowCell.column.name
                logger.warn("Строка $rowNum: Графа «$msg» не заполнена!")
            }
        }

        //  Корректность даты договора
        def dt = docDateCell.value
        if (dt != null && (dt < dFrom || dt > dTo)) {
            def msg = docDateCell.column.name
            logger.warn("Строка $rowNum: «$msg» не может быть вне налогового периода!")
        }

        // Проверка доходов
        def sumCell = row.getCell('sum')
        def priceCell = row.getCell('price')
        def costCell = row.getCell('cost')
        def msgSum = sumCell.column.name
        if (priceCell.value != sumCell.value) {
            def msg = priceCell.column.name
            logger.warn("Строка $rowNum: «$msg» не может отличаться от «$msgSum»!")
        }
        if (costCell.value != sumCell.value) {
            def msg = costCell.column.name
            logger.warn("Строка $rowNum: «$msg» не может отличаться от «$msgSum»!")
        }
        // Корректность даты совершения сделки
        def dealDateCell = row.getCell('dealDate')
        if (docDateCell.value > dealDateCell.value) {
            def msg1 = dealDateCell.column.name
            def msg2 = docDateCell.column.name
            logger.warn("Строка $rowNum: «$msg1» не может быть меньше «$msg2»!")
        }
        //Проверки соответствия НСИ
        checkNSI(row, "fullNamePerson", "Организации-участники контролируемых сделок",9)
        checkNSI(row, "countryCode", "ОКСМ",10)
    }
}

/**
 * Проверка соответствия НСИ
 */
void checkNSI(DataRow<Cell> row, String alias, String msg, Long id) {
    def cell = row.getCell(alias)
    if (cell.value != null && refBookService.getRecordData(id, cell.value) == null) {
        def msg2 = cell.column.name
        def rowNum = row.getIndex()
        logger.warn("Строка $rowNum: В справочнике «$msg» не найден элемент «$msg2»!")
    }
}

/**
 * Алгоритмы заполнения полей формы.
 */
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    dataRows.eachWithIndex { row, index ->
        // Порядковый номер строки
        row.rowNumber = index + 1
        // Расчет поля "Цена"
        row.price = row.sum
        // Расчет поля "Стоимость"
        row.cost = row.sum

        // Расчет полей зависимых от справочников
        if (row.fullNamePerson != null) {
            def map = refBookService.getRecordData(9, row.fullNamePerson)
            row.inn = map.INN_KIO.stringValue
            row.countryCode = map.COUNTRY.referenceValue
        } else {
            row.inn = null
            row.countryCode = null
        }
    }
    dataRowHelper.update(dataRows);
}

/**
 * Консолидация
 */
void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.clear()

    int index = 1;
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (source != null && source.state == WorkflowState.ACCEPTED) {
            formDataService.getDataRowHelper(source).getAllCached().each { row ->
                if (row.getAlias() == null) {
                    dataRowHelper.insert(row, index++)
                }
            }
        }
    }
}

/**
 * Получение импортируемых данных.
 */
void importData() {
    def fileName = (UploadFileName ? UploadFileName.toLowerCase() : null)
    if (fileName == null || fileName == '') {
        logger.error('Имя файла не должно быть пустым')
        return
    }

    def is = ImportInputStream
    if (is == null) {
        logger.error('Поток данных пуст')
        return
    }

    if (!fileName.endsWith('.xls')) {
        logger.error('Выбранный файл не соответствует формату xls!')
        return
    }

    def xmlString = importService.getData(is, fileName, 'windows-1251', 'Полное наименование юридического лица с указанием ОПФ', null)
    if (xmlString == null) {
        logger.error('Отсутствие значении после обработки потока данных')
        return
    }
    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null) {
        logger.error('Отсутствие значении после обработки потока данных')
        return
    }

    // добавить данные в форму
    try{
        if (!checkTableHead(xml, 3)) {
            logger.error('Заголовок таблицы не соответствует требуемой структуре!')
            return
        }
        addData(xml)
        if (!logger.containsLevel(LogLevel.ERROR)) {
            calc()
            logicCheck()
        }
    } catch(Exception e) {
        logger.error(""+e.message)
    }
}

/**
 * Заполнить форму данными.
 *
 * @param xml данные
 */
def addData(def xml) {
    Date date = reportPeriodService.get(formData.reportPeriodId).taxPeriod.getEndDate()

    def cache = [:]
    def data = formDataService.getDataRowHelper(formData)
    data.clear()

    def indexRow = -1
    for (def row : xml.row) {
        indexRow++

        // пропустить шапку таблицы
        if (indexRow <= 2) {
            continue
        }

        if ((row.cell.find{it.text()!=""}.toString())=="") {
            break
        }

        def newRow = formData.createDataRow()
        ['fullNamePerson', 'sum', 'docNumber', 'docDate', 'dealDate'].each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        def indexCell = 0

        // графа 1
        newRow.rowNumber = indexRow - 2

        // графа 2
        newRow.fullNamePerson = getRecordId(9, 'NAME', row.cell[indexCell].text(), date, cache, indexRow, indexCell, false)
        def map = newRow.fullNamePerson == null ? null : refBookService.getRecordData(9, newRow.fullNamePerson)
        indexCell++

        // графа 3
        if (map != null) {
            def String text = row.cell[indexCell].text()
            if ((text != null && !text.isEmpty() && !text.equals(map.INN_KIO.stringValue)) || ((text == null || text.isEmpty()) && map.INN_KIO.stringValue != null)) {
                logger.warn("Строка ${indexRow + 3} столбец ${indexCell + 2} содержит значение, отсутствующее в справочнике!")
            }
        }
        indexCell++

        // графа 4
        if (map != null) {
            def text = row.cell[indexCell].text()
            map = refBookService.getRecordData(10, map.COUNTRY.referenceValue)
            if ((text != null && !text.isEmpty() && !text.equals(map.CODE.stringValue)) || ((text == null || text.isEmpty()) && map.CODE.stringValue != null)) {
                logger.warn("Строка ${indexRow + 3} столбец ${indexCell + 2} содержит значение, отсутствующее в справочнике!")
            }
        }
        indexCell++

        // графа 5
        newRow.sum = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 6
        newRow.docNumber = row.cell[indexCell].text()
        indexCell++

        // графа 7
        newRow.docDate = getDate(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 8
        indexCell++

        // графа 9
        indexCell++

        // графа 10
        newRow.dealDate = getDate(row.cell[indexCell].text(), indexRow, indexCell)

        data.insert(newRow, indexRow - 2)
    }
}

/**
 * Проверить шапку таблицы.
 *
 * @param xml данные
 * @param headRowCount количество строк в шапке
 */
def checkTableHead(def xml, def headRowCount) {
    def colCount = 9
    // проверить количество строк и колонок в шапке
    if (xml.row.size() < headRowCount || xml.row[0].cell.size() < colCount) {
        return false
    }
    def result = (xml.row[0].cell[0] == 'Полное наименование юридического лица с указанием ОПФ' &&
            xml.row[1].cell[0] ==  '' &&
            xml.row[2].cell[0] ==  'гр. 2' &&
            xml.row[0].cell[1] == 'ИНН/ КИО' &&
            xml.row[1].cell[1] ==  '' &&
            xml.row[2].cell[1] ==  'гр. 3' &&
            xml.row[0].cell[2] == 'Код страны по классификатору ОКСМ' &&
            xml.row[1].cell[2] ==  '' &&
            xml.row[2].cell[2] ==  'гр. 4' &&
            xml.row[0].cell[3] == 'Сумма доходов Банка, руб.' &&
            xml.row[1].cell[3] ==  '' &&
            xml.row[2].cell[3] ==  'гр. 5' &&
            xml.row[0].cell[4] == 'Номер договора' &&
            xml.row[1].cell[4] ==  '' &&
            xml.row[2].cell[4] ==  'гр. 6' &&
            xml.row[0].cell[5] == 'Дата договора' &&
            xml.row[1].cell[5] ==  '' &&
            xml.row[2].cell[5] ==  'гр. 7' &&
            xml.row[0].cell[6] == 'Цена' &&
            xml.row[1].cell[6] ==  '' &&
            xml.row[2].cell[6] ==  'гр. 8' &&
            xml.row[0].cell[7] == 'Стоимость' &&
            xml.row[1].cell[7] ==  '' &&
            xml.row[2].cell[7] ==  'гр. 9' &&
            xml.row[0].cell[8] == 'Дата совершения сделки')
            xml.row[1].cell[8] ==  '' &&
            xml.row[2].cell[8] ==  'гр. 10'
    return result
}

/**
 * Получить числовое значение.
 *
 * @param value строка
 */
def getNumber(def value, int indexRow, int indexCell) {
    if (value == null) {
        return null
    }
    def tmp = value.trim()
    if ("".equals(tmp)) {
        return null
    }
    // поменять запятую на точку и убрать пробелы
    tmp = tmp.replaceAll(',', '.').replaceAll('[^\\d.,-]+', '')
    try {
        return new BigDecimal(tmp)
    } catch (Exception e) {
        throw new Exception("Строка ${indexRow+3} столбец ${indexCell+2} содержит недопустимый тип данных!")
    }
}

/**
 * Получить record_id элемента справочника.
 *
 * @param value
 */
def getRecordId(def ref_id, String alias, String value, Date date, def cache, int indexRow, int indexCell, boolean mandatory = true) {
    String filter = "LOWER($alias) = LOWER('$value')"
    if (cache[ref_id]!=null) {
        if (cache[ref_id][filter]!=null) return cache[ref_id][filter]
    } else {
        cache[ref_id] = [:]
    }
    def refDataProvider = refBookFactory.getDataProvider(ref_id)
    def records = refDataProvider.getRecords(date, null, filter, null).getRecords()
    if (records.size() == 1){
        cache[ref_id][filter] = (records.get(0).record_id.toString() as Long)
        return cache[ref_id][filter]
    }
    def msg = "Строка ${indexRow + 3} столбец ${indexCell + 2} содержит значение, отсутствующее в справочнике!"
    if (mandatory) {
        throw new Exception(msg)
    } else {
        logger.warn(msg)
    }
    return null
}


/**
 * Получить дату по строковому представлению (формата дд.ММ.гггг)
 */
def getDate(def value, int indexRow, int indexCell) {
    if (value == null || value == '') {
        return null
    }
    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    try {
        return format.parse(value)
    } catch (Exception e) {
        throw new Exception("Строка ${indexRow+3} столбец ${indexCell+2} содержит недопустимый тип данных!")
    }
}