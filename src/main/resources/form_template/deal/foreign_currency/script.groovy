package form_template.deal.foreign_currency

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogLevel

import java.text.SimpleDateFormat

/**
 * 390 - Купля-продажа иностранной валюты
 * (похож на nondeliverable " Беспоставочные срочные сделки")
 *
 * @author Stanislav Yasinskiy
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkUniq()
        break
    case FormDataEvent.CALCULATE:
        deleteAllStatic()
        calc()
        addAllStatic()
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
    case FormDataEvent.IMPORT:
        importData()
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
    def index = currentDataRow != null ? currentDataRow.getIndex() : (size == 0 ? 1 : size)
    ['fullName', 'docNum', 'docDate', 'dealNumber', 'dealDate', 'currencyCode',
            'countryDealCode', 'incomeSum', 'outcomeSum', 'dealDoneDate'].each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    dataRowHelper.insert(row, index)
}

/**
 * Проверяет уникальность в отчётном периоде и вид
 * (не был ли ранее сформирован отчет, параметры которого совпадают с параметрами, указанными пользователем )
 */
void checkUniq() {
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
    for (row in dataRowHelper.getAllCached()) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = row.getIndex()
        def docDateCell = row.getCell('docDate')
        [
                'rowNumber',        // № п/п
                'fullName',         // Полное наименование с указанием ОПФ
                'inn',              // ИНН/КИО
                'countryName',      // Наименование страны регистрации
                'countryCode',      // Код страны по классификатору ОКСМ
                'docNum',           // Номер договора
                'docDate',          // Дата договора
                'dealNumber',       // Номер сделки
                'dealDate',         // Дата заключения сделки
                'currencyCode',     // Код валюты расчетов по сделке
                'countryDealCode',  // Код страны происхождения предмета сделки по классификатору ОКСМ
                'price',            // Цена (тариф) за единицу измерения, руб.
                'total',            // Итого стоимость, руб.
                'dealDoneDate'      // Дата совершения сделки
        ].each {
            def rowCell = row.getCell(it)
            if (rowCell.value == null || rowCell.value.toString().isEmpty()) {
                def msg = rowCell.column.name
                logger.warn("Графа «$msg» в строке $rowNum не заполнена!")
            }
        }
        // Проверка заполнения доходов и расходов Банка
        def incomeSumCell = row.getCell('incomeSum')
        def outcomeSumCell = row.getCell('outcomeSum')
        def msgIn = incomeSumCell.column.name
        def msgOut = outcomeSumCell.column.name
        if (incomeSumCell.value != null && outcomeSumCell.value != null) {
            logger.warn("«$msgIn» и «$msgOut» в строке $rowNum не могут быть одновременно заполнены!")
        }
        if (incomeSumCell.value == null && outcomeSumCell.value == null) {
            logger.warn("Одна из граф «$msgIn» и «$msgOut» в строке $rowNum должна быть заполнена!")
        }
        //  Корректность даты договора
        def taxPeriod = reportPeriodService.get(formData.reportPeriodId).taxPeriod

        def dFrom = taxPeriod.getStartDate()
        def dTo = taxPeriod.getEndDate()
        def dt = docDateCell.value
        if (dt != null && (dt < dFrom || dt > dTo)) {
            def msg = docDateCell.column.name
            if (dt > dTo) {
                logger.warn("«$msg» в строке $rowNum не может быть больше даты окончания отчётного периода!")
            }
            if (dt < dFrom) {
                logger.warn("«$msg» в строке $rowNum не может быть меньше даты начала отчётного периода!")
            }
        }
        // Корректность даты заключения сделки
        def dealDateCell = row.getCell('dealDate')
        if (docDateCell.value > dealDateCell.value) {
            def msg1 = dealDateCell.column.name
            def msg2 = docDateCell.column.name
            logger.warn("«$msg1» не может быть меньше «$msg2» в строке $rowNum!")
        }
        // Проверка заполнения стоимости сделки
        if (row.total != row.price) {
            def msg1 = row.getCell('price').column.name
            def msg2 = row.getCell('total').column.name
            logger.warn("«$msg1» не может отличаться от «$msg2» в строке $rowNum!")
        }
        // Корректность дат сделки  dealDate - 9гр, dealDoneDate - 16гр
        def dealDoneDate = row.getCell('dealDoneDate')
        if (dealDateCell.value > dealDoneDate.value) {
            def msg1 = dealDoneDate.column.name
            def msg2 = dealDateCell.column.name
            logger.warn("«$msg1» не может быть меньше «$msg2» в строке $rowNum!")
        }
        //Проверки соответствия НСИ
        checkNSI(row, "fullName", "Организации-участники контролируемых сделок", 9)
        checkNSI(row, "countryName", "ОКСМ", 10)
        checkNSI(row, "countryCode", "ОКСМ", 10)
        checkNSI(row, "countryDealCode", "ОКСМ", 10)
        checkNSI(row, "currencyCode", "Единый справочник валют", 15)
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
        logger.warn("В справочнике «$msg» не найден элемент графы «$msg2», указанный в строке $rowNum!")
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
        if (row.getAlias() != null) {
            continue
        }
        // Порядковый номер строки
        row.rowNumber = index++
        // Расчет поля "Цена"
        row.price = row.incomeSum != null ? row.incomeSum : row.outcomeSum
        // Расчет поля "Итого"
        row.total = row.price

        // Расчет полей зависимых от справочников
        if (row.fullName != null) {
            def map = refBookService.getRecordData(9, row.fullName)
            row.inn = map.INN_KIO.numberValue
            row.countryCode = map.COUNTRY.referenceValue
            row.countryName = map.COUNTRY.referenceValue
        } else {
            row.inn = null
            row.countryCode = null
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
 * Удаление всех статическиех строк "Подитог" из списка строк
 */
void deleteAllStatic() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    for (Iterator<DataRow> iter = dataRows.iterator() as Iterator<DataRow>; iter.hasNext();) {
        def row = (DataRow) iter.next()
        if (row.getAlias() != null) {
            dataRowHelper.delete(row)
        }
    }
}

/**
 * Проставляет статические строки
 */
void addAllStatic() {
    if (!logger.containsLevel(LogLevel.ERROR)) {

        def dataRowHelper = formDataService.getDataRowHelper(formData)
        def dataRows = dataRowHelper.getAllCached()
        def newRow = formData.createDataRow()

        newRow.setAlias('itg')
        newRow.itog = 'Подитог:'
        newRow.getCell('itog').colSpan = 11

        // Расчеты подитоговых значений
        def BigDecimal priceItg = 0, totalItg = 0
        for (row in dataRows) {

            def price = row.price
            def total = row.total

            priceItg += price != null ? price : 0
            totalItg += total != null ? total : 0
        }

        newRow.price = priceItg
        newRow.total = totalItg

        dataRowHelper.insert(newRow, dataRows.size() + 1)
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

    if (!fileName.contains('.xls')) {
        logger.error('Формат файла должен быть *.xls')
        return
    }

    def xmlString = importService.getData(is, fileName, 'windows-1251', 'Полное наименование с указанием ОПФ', 'Подитог:')
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
        if (!checkTableHead(xml, 4)) {
            logger.error('Заголовок таблицы не соответствует требуемой структуре!')
            return
        }
        addData(xml, 3)
//        logicCheck()
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
    def colCount = 17
    // проверить количество строк и колонок в шапке
    if (xml.row.size() < headRowCount || xml.row[0].cell.size() < colCount) {
        return false

    }
    def result = (
    xml.row[0].cell[0] == 'Полное наименование с указанием ОПФ' &&
            xml.row[2].cell[0] == '2' &&
            xml.row[3].cell[0] == 'гр. 2' &&
            xml.row[0].cell[1] == 'ИНН/ КИО' &&
            xml.row[2].cell[1] == '3' &&
            xml.row[3].cell[1] == 'гр. 3' &&
            xml.row[0].cell[2] == 'Наименование страны регистрации' &&
            xml.row[2].cell[2] == '4' &&
            xml.row[3].cell[2] == 'гр. 4.1' &&
            xml.row[0].cell[3] == 'Код страны регистрации по классификатору ОКСМ' &&
            xml.row[2].cell[3] == '5' &&
            xml.row[3].cell[3] == 'гр. 4.2' &&
            xml.row[0].cell[4] == 'Номер договора' &&
            xml.row[2].cell[4] == '6' &&
            xml.row[3].cell[4] == 'гр. 5' &&
            xml.row[0].cell[5] == 'Дата договора' &&
            xml.row[2].cell[5] == '7' &&
            xml.row[3].cell[5] == 'гр. 6' &&
            xml.row[0].cell[6] == 'Номер сделки' &&
            xml.row[2].cell[6] == '8' &&
            xml.row[3].cell[6] == 'гр. 7' &&
            xml.row[0].cell[7] == 'Дата  заключения сделки' &&
            xml.row[2].cell[7] == '9' &&
            xml.row[3].cell[7] == 'гр. 8' &&
            xml.row[0].cell[8] == 'Код валюты по сделке' &&
            xml.row[2].cell[8] == '10' &&
            xml.row[3].cell[8] == 'гр. 9' &&
            xml.row[0].cell[10] == 'Код страны происхождения предмета сделки по классификатору ОКСМ' &&
            xml.row[2].cell[10] == '11' &&
            xml.row[3].cell[10] == 'гр. 10' &&
            xml.row[0].cell[12] == 'Сумма доходов Банка по данным бухгалтерского учета, руб.' &&
            xml.row[2].cell[12] == '12' &&
            xml.row[3].cell[12] == 'гр. 11' &&
            xml.row[0].cell[13] == 'Сумма расходов Банка по данным бухгалтерского учета, руб.' &&
            xml.row[2].cell[13] == '13' &&
            xml.row[3].cell[13] == 'гр. 12' &&
            xml.row[0].cell[14] == 'Цена (тариф) за единицу измерения, руб.' &&
            xml.row[2].cell[14] == '14' &&
            xml.row[3].cell[14] == 'гр. 13' &&
            xml.row[0].cell[15] == 'Итого стоимость, руб.' &&
            xml.row[2].cell[15] == '15' &&
            xml.row[3].cell[15] == 'гр. 14' &&
            xml.row[0].cell[16] == 'Дата совершения сделки' &&
            xml.row[2].cell[16] == '16' &&
            xml.row[3].cell[16] == 'гр. 15')

    return result
}

/**
 * Заполнить форму данными.
 *
 * @param xml данные
 */
def addData(def xml, int headRowCount) {
    Date date = new Date()

    def cache = [:]
    def data = formDataService.getDataRowHelper(formData)
    data.clear()

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
        ['fullName', 'docNum', 'docDate', 'dealNumber', 'dealDate', 'currencyCode',
                'countryDealCode', 'incomeSum', 'outcomeSum', 'dealDoneDate'].each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        def indexCell = 0
        // графа 1
        newRow.rowNumber = indexRow - headRowCount

        // графа 2
        newRow.fullName = getRecordId(9, 'NAME', row.cell[indexCell].text(), date, cache, indexRow, indexCell)
        indexCell++

        // графа 3
//        newRow.inn =
        indexCell++

        // графа 4.1
//        newRow.countryName =
        indexCell++

        // графа 4.2
//        newRow.countryCode =
        indexCell++

        // графа 5
        newRow.docNum = row.cell[indexCell].text()
        indexCell++

        // графа 6
        newRow.docDate = getDate(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 7
        newRow.dealNumber = row.cell[indexCell].text()
        indexCell++

        // графа 8
        newRow.dealDate = getDate(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 9
        newRow.currencyCode = getRecordId(15, 'CODE', row.cell[indexCell].text(), date, cache, indexRow, indexCell)
        indexCell++
        indexCell++

        // графа 10
        newRow.countryDealCode = getRecordId(10, 'CODE', row.cell[indexCell].text(), date, cache, indexRow, indexCell)
        indexCell++
        indexCell++

        // графа 11
        newRow.incomeSum = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 12
        newRow.outcomeSum = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 13
        indexCell++

        // графа 14
        indexCell++

        // графа 15
        newRow.dealDoneDate = getDate(row.cell[indexCell].text(), indexRow, indexCell)
        data.insert(newRow, indexRow - headRowCount)
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
        throw new Exception("Строка ${indexRow + 6} столбец ${indexCell + 2} содержит недопустимый тип данных!")
    }
}

/**
 * Получить record_id элемента справочника.
 *
 * @param value
 */
def getRecordId(def ref_id, String code, String value, Date date, def cache, int indexRow, int indexCell) {
    String filter = code + "= '" + value + "'"
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
    throw new Exception("Строка ${indexRow + 6} столбец ${indexCell + 2} содержит значение, отсутствующее в справочнике!")
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
        throw new Exception("Строка ${indexRow + 6} столбец ${indexCell + 2} содержит недопустимый тип данных!")
    }
}
