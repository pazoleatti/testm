package form_template.deal.interbank_credits

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook

import java.text.SimpleDateFormat

/**
 * 389 - Предоставление межбанковских кредитов
 *
 * (похож на letter_of_credit "Предоставление инструментов торгового финансирования и непокрытых аккредитивов")
 * (похож на  guarantees "Предоставление гарантий")
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
// импорт из xls
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
    dataRowHelper.save(dataRowHelper.getAllCached())
}

void addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def row = formData.createDataRow()
    def dataRows = dataRowHelper.getAllCached()
    def size = dataRows.size()
    def index = currentDataRow != null ? (currentDataRow.getIndex()+1) : (size == 0 ? 1 : (size+1))
    [       'fullName',
            'docNumber',
            'docDate',
            'dealNumber',
            'dealDate',
            'sum',
            'dealDoneDate'
    ].each {
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
    def rowNum = 0

    for (row in  dataRowHelper.getAllCached()) {
        rowNum++
        if (row.getAlias() != null) {
            continue
        }
        def docDateCell = row.getCell('docDate')
        def dealDateCell = row.getCell('dealDate')
        [
                'rowNumber',        // № п/п
                'fullName',      // Полное наименование юридического лица с указанием ОПФ
                'inn',           // ИНН/КИО
                'countryName',   // Наименование страны регистрации
                'countryCode',   // Код страны регистрации по классификатору ОКСМ
                'docNumber',     // Номер договора
                'docDate',       // Дата договора
                'dealNumber',    // Номер сделки
                'dealDate',      // Дата сделки
                'count',         // Количество
                'sum',           // Сумма доходов Банка по данным бухгалтерского учета, руб.
                'price',         // Цена (тариф) за единицу измерения без учета НДС, акцизов и пошлины, руб.
                'total',         // Итого стоимость без учета НДС, акцизов и пошлин, руб.
                'dealDoneDate'   // Дата совершения сделки
        ].each {
            def rowCell = row.getCell(it)
            if (rowCell.value == null || rowCell.value.toString().isEmpty()) {
                def msg = rowCell.column.name
                logger.warn("Строка $rowNum: Графа «$msg» не заполнена!")
            }
        }
        // Проверка количества
        if (row.count != 1) {
            def msg = row.getCell('count').column.name
            logger.warn("Строка $rowNum: В графе «$msg» может быть указано только значение «1»!")
        }
        //  Корректность даты договора
        def dt = docDateCell.value
        if (dt != null && (dt < dFrom || dt > dTo)) {
            def msg = docDateCell.column.name
            logger.warn("Строка $rowNum: «$msg» не может быть вне налогового периода!")
        }
        // Корректность даты заключения сделки
        if (docDateCell.value > dealDateCell.value) {
            def msg1 = dealDateCell.column.name
            def msg2 = docDateCell.column.name
            logger.warn("Строка $rowNum: «$msg1» не может быть меньше «$msg2»!")
        }
        // Проверка доходности
        def sumCell = row.getCell('sum')
        def priceCell = row.getCell('price')
        def totalCell = row.getCell('total')
        def msgSum = sumCell.column.name
        if (priceCell.value != sumCell.value) {
            def msg = priceCell.column.name
            logger.warn("Строка $rowNum: «$msg» не может отличаться от «$msgSum»!")
        }
        if (totalCell.value != sumCell.value) {
            def msg = totalCell.column.name
            logger.warn("Строка $rowNum: «$msg» не может отличаться от «$msgSum»!")
        }
        // Корректность даты совершения сделки
        def dealDoneDateCell = row.getCell('dealDoneDate')
        if (dealDoneDateCell.value < dealDateCell.value) {
            def msg1 = dealDoneDateCell.column.name
            def msg2 = dealDateCell.column.name
            logger.warn("Строка $rowNum: «$msg1» не может быть меньше «$msg2»!")
        }
        //Проверки соответствия НСИ
        checkNSI(row, "fullName", "Организации-участники контролируемых сделок", 9)
        checkNSI(row, "countryName", "ОКСМ", 10)
        checkNSI(row, "countryCode", "ОКСМ", 10)
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
        if (row.getAlias() != null) {
            continue
        }
        // Порядковый номер строки
        row.rowNumber = index++
        // Расчет поля " Количество"
        row.count = 1
        // Расчет поля "Цена"
        row.price = row.sum
        // Расчет поля "Итого"
        row.total = row.sum

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
//        logicCheck()
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
    Date date = new Date()

    def cache = [:]
    def data = formDataService.getDataRowHelper(formData)
    data.clear()

    def indexRow = -1
    for (def row : xml.row) {
        indexRow++

        // пропустить шапку таблицы
        if (indexRow <= 3) {
            continue
        }

        if ((row.cell.find{it.text()!=""}.toString())=="") {
            break
        }

        def newRow = formData.createDataRow()
        [
                'fullName',      // Полное наименование юридического лица с указанием ОПФ
                'docNumber',     // Номер договора
                'docDate',       // Дата договора
                'dealNumber',    // Номер сделки
                'dealDate',      // Дата сделки
                'sum',           // Сумма доходов Банка по данным бухгалтерского учета, руб.
                'dealDoneDate'   // Дата совершения сделки
        ].each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        def indexCell = 0
        // графа 1
        newRow.rowNumber = indexRow - 3

        // графа 2
        newRow.fullName = getRecordId(9, 'NAME', row.cell[indexCell].text(), date, cache, indexRow, indexCell)
        indexCell++

        // графа 3
        indexCell++

        // графа 4
        indexCell++

        // графа 5
        indexCell++

        // графа 6
        newRow.docNumber = row.cell[indexCell].text()
        indexCell++

        // графа 7

        newRow.docDate = getDate(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 8
        newRow.dealNumber = row.cell[indexCell].text()
        indexCell++

        // графа 9
        newRow.dealDate = getDate(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 10
        indexCell++
        indexCell++

        // графа 11
        newRow.sum = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++
        indexCell++

        // графа 12
        indexCell++
        indexCell++

        // графа 13
        indexCell++

        // графа 14
        newRow.dealDoneDate = getDate(row.cell[indexCell].text(), indexRow, indexCell)

        data.insert(newRow, indexRow - 3)
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
    def result = (
        xml.row[0].cell[0] == 'Полное наименование юридического лица с указанием ОПФ' &&
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

        xml.row[0].cell[7] == 'Дата заключения сделки' &&
        xml.row[2].cell[7] == '9' &&
        xml.row[3].cell[7] == 'гр. 8' &&

        xml.row[0].cell[8] == 'Количество' &&
        xml.row[2].cell[8] == '10' &&
        xml.row[3].cell[8] == 'гр. 9' &&

        xml.row[0].cell[10] == 'Сумма доходов Банка по данным бухгалтерского учета, руб.' &&
        xml.row[2].cell[10] == '11' &&
        xml.row[3].cell[10] == 'гр.10' &&

        xml.row[0].cell[12] == 'Цена (тариф) за единицу измерения без учета НДС, акцизов и пошлины, руб.' &&
        xml.row[2].cell[12] == '12' &&
        xml.row[3].cell[12] == 'гр. 11' &&

        xml.row[0].cell[14] == 'Итого стоимость без учета НДС, акцизов и пошлины, руб.' &&
        xml.row[2].cell[14] == '13' &&
        xml.row[3].cell[14] == 'гр. 12' &&

        xml.row[0].cell[15] == 'Дата совершения сделки' &&
        xml.row[2].cell[15] == '14' &&
        xml.row[3].cell[15] == 'гр. 13')
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
def getRecordId(def ref_id, String code, String value, Date date, def cache, int indexRow, int indexCell) {
    String filter = code + " = '"+ value+"'"
    if (cache[ref_id]!=null) {
        if (cache[ref_id][filter]!=null) return cache[ref_id][filter]
    } else {
        cache[ref_id] = [:]
    }
    def refDataProvider = refBookFactory.getDataProvider(ref_id)
    def records = refDataProvider.getRecords(date, null, filter, null)
    if (records.size() == 1){
        cache[ref_id][filter] = records.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
        return cache[ref_id][filter]
    }
    throw new Exception("Строка ${indexRow+3} столбец ${indexCell+2} содержит значение, отсутствующее в справочнике!")
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
