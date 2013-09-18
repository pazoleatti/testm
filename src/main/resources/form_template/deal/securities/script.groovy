package form_template.deal.securities

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook

import java.math.RoundingMode
import java.text.SimpleDateFormat

/**
 * 381 - Приобретение и реализация ценных бумаг (долей в уставном капитале)
 *
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
// Импорт
    case FormDataEvent.IMPORT:
        importData()
        if (!logger.containsLevel(LogLevel.ERROR)) {
            calc()
            logicCheck()
        }
        break
}

// графа  1 - rowNumber         // № п/п
// графа  2 - fullNamePerson    // Полное наименование юридического лица с указанием ОПФ
// графа  3 - inn               // ИНН/КИО
// графа  4 - countryCode       // Код страны регистрации по классификатору ОКСМ
// графа  5 - dealSign          // Признак сделки, совершенной в РПС
// графа  6 - incomeSum         // Сумма доходов (стоимость реализации) Банка, руб.
// графа  7 - outcomeSum        // Сумма расходов (стоимость приобретения) Банка, руб.
// графа  8 - docNumber         // Номер договора
// графа  9 - docDate           // Дата договора
// графа 10 - okeiCode          // Код единицы измерения по ОКЕИ
// графа 11 - count             // Количество
// графа 12 - price             // Цена
// графа 13 - cost              // Стоимость
// графа 14 - dealDate          // Дата совершения сделки


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
    ['fullNamePerson', 'dealSign', 'incomeSum', 'outcomeSum', 'docNumber', 'docDate', 'okeiCode', 'count', 'dealDate'].each {
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

    for (row in dataRowHelper.getAllCached()) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = row.getIndex()
        def docDateCell = row.getCell('docDate')
        def okeiCodeCell = row.getCell('okeiCode')
        [
                'rowNumber',        // № п/п
                'fullNamePerson',// Полное наименование юридического лица с указанием ОПФ
                'inn',           // ИНН/КИО
                'countryCode',   // Код страны регистрации по классификатору ОКСМ
                'docNumber',     // Номер договора
                'docDate',       // Дата договора
                'okeiCode',      // Код единицы измерения по ОКЕИ
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
        // Проверка доходов и расходов
        def incomeSumCell = row.getCell('incomeSum')
        def outcomeSumCell = row.getCell('outcomeSum')
        def msgIn = incomeSumCell.column.name
        def msgOut = outcomeSumCell.column.name
        if (incomeSumCell.value != null && outcomeSumCell.value != null) {
            logger.warn("Строка $rowNum: «$msgIn» и «$msgOut» не могут быть одновременно заполнены!")
        }
        if (incomeSumCell.value == null && outcomeSumCell.value == null) {
            logger.warn("Строка $rowNum: Одна из граф «$msgIn» и «$msgOut» должна быть заполнена!")
        }
        // Проверка выбранной единицы измерения
        def okei =  row.okeiCode!= null ? refBookService.getRecordData(12, row.okeiCode).CODE.stringValue : null
        if (okei != '796' && okei != '744') {
            def msg = okeiCodeCell.column.name
            logger.warn("Строка $rowNum: В графе «$msg» могут быть указаны только следующие элементы: шт., процент!")
        }

        //  Корректность даты договора
        def dt = docDateCell.value
        if (dt != null && (dt < dFrom || dt > dTo)) {
            def msg = docDateCell.column.name
            logger.warn("Строка $rowNum: «$msg» не может быть вне налогового периода!")
        }

        // Проверка цены
        def sumCell = row.incomeSum != null ? row.getCell('incomeSum') : row.getCell('outcomeSum')
        def countCell = row.getCell('count')
        def priceCell = row.getCell('price')
        if (okei == '796' && countCell.value != null && countCell.value != 0
                && priceCell.value != (sumCell.value / countCell.value).setScale(2, RoundingMode.HALF_UP)) {
            def msg1 = priceCell.column.name
            def msg2 = sumCell.column.name
            def msg3 = countCell.column.name
            logger.warn("Строка $rowNum: «$msg1» не равно отношению «$msg2» и «$msg3»!")
        } else if (okei == '744' && priceCell.value != sumCell.value) {
            def msg1 = priceCell.column.name
            def msg2 = sumCell.column.name
            logger.warn("Строка $rowNum: «$msg1» не равно «$msg2»!")
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
        checkNSI(row, "countryCode", "ОКСМ", 10)
        checkNSI(row, "dealSign", "Признак сделки, совершенной в РПС", 36)
        checkNSI(row, "okeiCode", "Коды единиц измерения на основании ОКЕИ", 12)
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
    for (row in dataRows) {
        // Порядковый номер строки
        if (row.getIndex() != null) {
            row.rowNumber = row.getIndex()
        }
        // Расчет поля "Цена"
        def priceValue = row.incomeSum != null ? row.incomeSum : row.outcomeSum
        def okei =  row.okeiCode!= null ? refBookService.getStringValue(12, row.okeiCode, 'CODE') : null
        if (okei == '744') {
            row.price = priceValue
        } else if (okei == '796' && row.count != 0 && row.count != null) {
            row.price = priceValue / row.count
        } else {
            row.price = null
        }
        // Расчет поля "Стоимость"
        row.cost = priceValue

        // Расчет полей зависимых от справочников
        if (row.fullNamePerson != null) {
            def map = refBookService.getRecordData(9, row.fullNamePerson)
            row.inn = map.INN_KIO.numberValue
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
    def dataRows = dataRowHelper.getAllCached()
    dataRows.clear()

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
        logger.error('Отсутствие значения после обработки потока данных')
        return
    }
    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null) {
        logger.error('Ошибка в обработке данных!')
        return
    }

    // добавить данные в форму
    try {
        if (!checkTableHead(xml, 3)) {
            logger.error('Заголовок таблицы не соответствует требуемой структуре!')
            return
        }
        addData(xml)
    } catch (Exception e) {
        logger.error("Ошибка при заполнении данных с файла! " + e.message)
    }
}

/**
 * Проверить шапку таблицы.
 *
 * @param xml данные
 * @param headRowCount количество строк в шапке
 */
def checkTableHead(def xml, def headRowCount) {
    def colCount = 13
    // проверить количество строк и колонок в шапке
    if (xml.row.size() < headRowCount || xml.row[0].cell.size() < colCount) {
        return false
    }
    def result = (xml.row[0].cell[0] == 'Полное наименование юридического лица с указанием ОПФ' &&
            xml.row[1].cell[0] == '' &&
            xml.row[2].cell[0] == 'гр. 2' &&
            xml.row[0].cell[1] == 'ИНН/ КИО' &&
            xml.row[1].cell[1] == '' &&
            xml.row[2].cell[1] == 'гр. 3' &&
            xml.row[0].cell[2] == 'Код страны по классификатору ОКСМ' &&
            xml.row[1].cell[2] == '' &&
            xml.row[2].cell[2] == 'гр. 4' &&
            xml.row[0].cell[3] == 'Признак сделки, совершенной в РПС' &&
            xml.row[1].cell[3] == '' &&
            xml.row[2].cell[3] == 'гр. 5' &&
            xml.row[0].cell[4] == 'Сумма доходов (стоимость реализации) Банка, руб.' &&
            xml.row[1].cell[4] == '' &&
            xml.row[2].cell[4] == 'гр. 6' &&
            xml.row[0].cell[5] == 'Сумма расходов (стоимость приобретения) Банка, руб.' &&
            xml.row[1].cell[5] == '' &&
            xml.row[2].cell[5] == 'гр. 7' &&
            xml.row[0].cell[6] == 'Номер договора' &&
            xml.row[1].cell[6] == '' &&
            xml.row[2].cell[6] == 'гр. 8' &&
            xml.row[0].cell[7] == 'Дата договора' &&
            xml.row[1].cell[7] == '' &&
            xml.row[2].cell[7] == 'гр. 9' &&
            xml.row[0].cell[8] == 'Код единицы измерения по ОКЕИ' &&
            xml.row[1].cell[8] == '' &&
            xml.row[2].cell[8] == 'гр. 10' &&
            xml.row[0].cell[9] == 'Количество' &&
            xml.row[1].cell[9] == '' &&
            xml.row[2].cell[9] == 'гр. 11' &&
            xml.row[0].cell[10] == 'Цена' &&
            xml.row[1].cell[10] == '' &&
            xml.row[2].cell[10] == 'гр. 12' &&
            xml.row[0].cell[11] == 'Стоимость' &&
            xml.row[1].cell[11] == '' &&
            xml.row[2].cell[11] == 'гр. 13' &&
            xml.row[0].cell[12] == 'Дата совершения сделки' &&
            xml.row[1].cell[12] == '' &&
            xml.row[2].cell[12] == 'гр. 14')
    return result
}

/**
 * Заполнить форму данными.
 *
 * @param xml данные
 */
def addData(def xml) {
    Date date = new Date()

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

        if ((row.cell.find {it.text() != ""}.toString()) == "") {
            break
        }

        def newRow = formData.createDataRow()
        ['fullNamePerson', 'dealSign', 'incomeSum', 'outcomeSum', 'docNumber', 'docDate', 'okeiCode', 'count', 'dealDate'].each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        def indexCell = 0
        // графа 1
        newRow.rowNumber = indexRow - 2

        // графа 2
        newRow.fullNamePerson = getRecordId(9, 'NAME', row.cell[indexCell].text(), date, cache, indexRow, indexCell)
        indexCell++

        // графа 3
        //newRow.inn = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 4
        //newRow.countryCode = getRecordId(10, 'CODE', row.cell[indexCell].text(), date, cache, indexRow, indexCell)
        indexCell++

        // графа 5
        newRow.dealSign = getRecordId(36, 'SIGN', row.cell[indexCell].text(), date, cache, indexRow, indexCell)
        indexCell++

        // графа 6
        newRow.incomeSum = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 7
        newRow.outcomeSum = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 8
        newRow.docNumber = row.cell[indexCell].text()
        indexCell++

        // графа 9
        newRow.docDate = getDate(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 10
        newRow.okeiCode = getRecordId(12, 'CODE', row.cell[indexCell].text(), date, cache, indexRow, indexCell)
        indexCell++

        // графа 11
        newRow.count = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 12
        newRow.price = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 13
        newRow.cost = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 14
        newRow.dealDate = getDate(row.cell[indexCell].text(), indexRow, indexCell)

        data.insert(newRow, indexRow - 2)
    }
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
        throw new Exception("Строка ${indexRow - 2} столбец ${indexCell + 2} содержит недопустимый тип данных!")
    }
}

/**
 * Получить record_id элемента справочника.
 *
 * @param value
 */
def getRecordId(def ref_id, String alias, String value, Date date, def cache, int indexRow, int indexCell) {
    String filter;
    if (value == null || value.equals("")) {
        filter = alias + " is null"
    } else {
        filter = alias + "= '" + value + "'"
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
    throw new Exception("Строка ${indexRow - 2} столбец ${indexCell + 2} содержит значение, отсутствующее в справочнике!")
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
        throw new Exception("Строка ${indexRow - 2} столбец ${indexCell + 2} содержит недопустимый тип данных!")
    }
}