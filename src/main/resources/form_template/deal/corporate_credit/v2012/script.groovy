package form_template.deal.corporate_credit.v2012

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook

import java.text.SimpleDateFormat

/**
 * 387 - Предоставление корпоративного кредита (12)
 *
 * похож на  software_development (Разработка, внедрение, поддержка и модификация программного обеспечения, приобретение лицензий)
 * похож на  trademark (Предоставление права пользования товарным знаком)
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
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
        calc()
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
// Консолидация
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
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
    def findForm = formDataService.find(formData.formType.id, formData.kind, formData.departmentReportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
    if (findForm != null) {
        logger.error('Формирование нового отчета невозможно, т.к. отчет с указанными параметрами уже сформирован.')
    }
}

/**
 * Логические проверки
 */
void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def startDate = reportPeriodService.getStartDate(formData.reportPeriodId)
    def dFrom = startDate.time
    startDate.add(Calendar.YEAR, 1)
    startDate.add(Calendar.DAY_OF_YEAR, -1)
    def dTo = startDate.time

    def int index = 1
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
                'countryName',   // Страна регистрации
                'sum',           // Сумма доходов Банка, руб.
                'docNumber',     // Номер договора
                'docDate',       // Дата договора
                'count',         // Количество
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
        // Проверка количества
        if (row.count != 1) {
            def msg = getColumnName(row, 'count')
            logger.warn("Строка $rowNum: В графе «$msg» может быть указано только значение «1»!")
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
        checkNSI(row, "fullNamePerson", "Организации-участники контролируемых сделок", 9)
        checkNSI(row, "countryName", "ОКСМ", 10)
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
    def int index = 1
    for (row in dataRows) {
        // Порядковый номер строки
        row.rowNumber = index++
        // В поле "Количество" подставляется значение «1»
        row.count = 1
        // Расчет поля "Цена"
        row.price = row.sum
        // Расчет поля "Стоимость"
        row.cost = row.sum

        // Расчет полей зависимых от справочников
        if (row.fullNamePerson != null) {
            def map = refBookService.getRecordData(9, row.fullNamePerson)
            row.inn = map.INN_KIO.stringValue
            row.countryName = map.COUNTRY.referenceValue
        } else {
            row.inn = null
            row.countryName = null
        }
    }
    dataRowHelper.update(dataRows);
}

/**
 * Консолидация
 */
void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def rows = new LinkedList<DataRow<Cell>>()
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        def source = formDataService.find(it.formTypeId, it.kind, it.departmentReportPeriodId, it.periodOrder, it.comparativePeriodId, it.accruing)
        if (source != null
                && source.state == WorkflowState.ACCEPTED
                && source.getFormType().getId() == formData.getFormType().getId()) {
            formDataService.getDataRowHelper(source).getAll().each { row ->
                if (row.getAlias() == null) {
                    rows.add(row)
                }
            }
        }
    }
    dataRowHelper.save(rows)
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
    try {
        if (!checkTableHead(xml, 3)) {
            logger.error('Заголовок таблицы не соответствует требуемой структуре!')
            return
        }
        addData(xml, 2)
    } catch (Exception e) {
        logger.error("" + e.message)
    }
}

/**
 * Проверить шапку таблицы.
 *
 * @param xml данные
 * @param headRowCount количество строк в шапке
 */
def checkTableHead(def xml, def headRowCount) {
    def colCount = 10
    // проверить количество строк и колонок в шапке
    if (xml.row.size() < headRowCount || xml.row[0].cell.size() < colCount) {
        return false

    }
    def result = (xml.row[0].cell[0] == 'Полное наименование юридического лица с указанием ОПФ' &&
            xml.row[2].cell[0] ==  'гр. 2' &&
            xml.row[0].cell[1] == 'ИНН/ КИО' &&
            xml.row[2].cell[1] ==  'гр. 3' &&
            xml.row[0].cell[2] == 'Страна регистрации' &&
            xml.row[2].cell[2] ==  'гр. 4' &&
            xml.row[0].cell[3] == 'Сумма доходов Банка, руб.' &&
            xml.row[2].cell[3] ==  'гр. 5' &&
            xml.row[0].cell[4] == 'Номер договора' &&
            xml.row[2].cell[4] ==  'гр. 6' &&
            xml.row[0].cell[5] == 'Дата договора' &&
            xml.row[2].cell[5] ==  'гр. 7' &&
            xml.row[0].cell[6] == 'Количество' &&
            xml.row[2].cell[6] ==  'гр. 8' &&
            xml.row[0].cell[7] == 'Цена' &&
            xml.row[2].cell[7] ==  'гр. 9' &&
            xml.row[0].cell[8] == 'Стоимость' &&
            xml.row[2].cell[8] ==  'гр. 10' &&
            xml.row[0].cell[9] == 'Дата совершения сделки' &&
            xml.row[2].cell[9] ==  'гр. 11')

    return result
}

/**
 * Заполнить форму данными.
 *
 * @param xml данные
 */
def addData(def xml, int headRowCount) {
    Date date = reportPeriodService.get(formData.reportPeriodId).taxPeriod.getEndDate()

    def cache = [:]
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def rows = new LinkedList()

    def indexRow = -1
    for (def row : xml.row) {
        indexRow++

        // пропустить шапку таблицы
        if (indexRow <= headRowCount) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        def newRow = formData.createDataRow()
        ['fullNamePerson', 'sum', 'docNumber', 'docDate', 'dealDate'].each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        def indexCell = 0
        // графа 1
        newRow.rowNumber = indexRow - headRowCount

        // графа 2
        newRow.fullNamePerson = getRecordId(9, 'NAME', row.cell[indexCell].text(), date, cache, indexRow, indexCell, false)
        def map = newRow.fullNamePerson == null ? null : refBookService.getRecordData(9, newRow.fullNamePerson)
        indexCell++

        // графа 3
        if (map != null) {
            def text = row.cell[indexCell].text()
            if ((text != null && !text.isEmpty() && !text.equals(map.INN_KIO.stringValue)) || ((text == null || text.isEmpty()) && map.INN_KIO.stringValue != null)) {
                logger.warn("Проверка файла: Строка ${indexRow+3} столбец ${indexCell+2} содержит значение, отсутствующее в справочнике «" + refBookFactory.get(9).getName()+"»!")
            }
        }
        indexCell++

        // графа 4
        if (map != null) {
            def text = row.cell[indexCell].text()
            map = refBookService.getRecordData(10, map.COUNTRY.referenceValue)
            if ((text != null && !text.isEmpty() && !text.equals(map.CODE.stringValue)) || ((text == null || text.isEmpty()) && map.CODE.stringValue != null)) {
                logger.warn("Проверка файла: Строка ${indexRow+3} столбец ${indexCell+2} содержит значение, отсутствующее в справочнике «" + refBookFactory.get(10).getName()+"»!")
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
//        newRow.count =
        indexCell++

        // графа 9
//        newRow.price =
        indexCell++

        // графа 10
//        newRow.cost =
        indexCell++

        // графа 11
        newRow.dealDate = getDate(row.cell[indexCell].text(), indexRow, indexCell)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
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
        throw new Exception("Проверка файла: Строка ${indexRow + 3} столбец ${indexCell + 2} содержит недопустимый тип данных!")
    }
}

/**
 * Получить record_id элемента справочника.
 *
 * @param value
 */
def getRecordId(def ref_id, String alias, String value, Date date, def cache, int indexRow, int indexCell, boolean mandatory = true) {
    String filter;
    if (value == null || value.equals("")) {
        filter = alias + " is null"
    } else {
        filter = "LOWER($alias) = LOWER('$value')"
    }
    if (cache[ref_id] != null) {
        if (cache[ref_id][filter] != null) return cache[ref_id][filter]
    } else {
        cache[ref_id] = [:]
    }
    def refDataProvider = refBookFactory.getDataProvider(ref_id)
    def records = refDataProvider.getRecords(date, null, filter, null)
    if (records.size() == 1) {
        cache[ref_id][filter] = records.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
        return cache[ref_id][filter]
    }
    def msg = "Проверка файла: Строка ${indexRow + 3} столбец ${indexCell + 2} содержит значение, отсутствующее в справочнике «" + refBookFactory.get(ref_id).getName()+"»!"
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
        throw new Exception("Проверка файла: Строка ${indexRow + 3} столбец ${indexCell + 2} содержит недопустимый тип данных!")
    }
}