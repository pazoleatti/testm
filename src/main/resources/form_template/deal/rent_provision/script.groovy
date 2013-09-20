package form_template.deal.rent_provision

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook

import java.math.RoundingMode
import java.text.SimpleDateFormat

/**
 * 376 - Предоставление нежилых помещений в аренду
 *
 * @author Dmitriy Levykin
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
    case FormDataEvent.IMPORT:
        importData()
        if (!logger.containsLevel(LogLevel.ERROR)) {
            calc()
            logicCheck()
        }
        break
}

def getEditColumns() {
    ['jurName', 'incomeBankSum', 'contractNum', 'contractDate', 'country',
            'region', 'city', 'settlement', 'count', 'transactionDate']
}

/**
 * Проверка при создании формы.
 */
void checkCreation() {
    def findForm = formDataService.find(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Формирование нового отчета невозможно, т.к. отчет с указанными параметрами уже сформирован.')
    }
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
    getEditColumns().each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    dataRowHelper.insert(row, index)
}

void deleteRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.delete(currentDataRow)
}

/**
 * Логические проверки
 */
void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    // Налоговый период
    def taxPeriod = reportPeriodService.get(formData.reportPeriodId).taxPeriod

    def dFrom = taxPeriod.getStartDate()
    def dTo = taxPeriod.getEndDate()

    int index = 1

    for (row in dataRowHelper.getAllCached()) {
        if (row.getAlias() != null) {
            continue
        }

        def rowNum = index++

        [
                'rowNum', // № п/п
                'jurName', // Полное наименование юридического лица с указанием ОПФ
                'innKio', // ИНН/КИО
                'countryCode', // Код страны по классификатору ОКСМ
                'incomeBankSum', // Сумма доходов Банка, руб.
                'contractNum', // Номер договора
                'contractDate', // Дата договора
                'country', // Адрес местонахождения объекта недвижимости (Страна)
                'count', // Количество
                'price', // Цена
                'cost', // Стоимость
                'transactionDate' // Дата совершения сделки
        ].each {
            if (row.getCell(it).value == null || row.getCell(it).value.toString().isEmpty()) {
                msg = row.getCell(it).column.name
                logger.warn("Строка $rowNum: Графа «$msg» не заполнена!")
            }
        }

        def count = row.count
        def price = row.price
        def cost = row.cost
        def incomeBankSum = row.incomeBankSum
        def transactionDate = row.transactionDate
        def contractDate = row.contractDate

        //Наименования колонок
        def contractDateName = row.getCell('contractDate').column.name
        def transactionDateName = row.getCell('transactionDate').column.name
        def priceName = row.getCell('price').column.name
        def incomeBankSumName = row.getCell('incomeBankSum').column.name
        def countName = row.getCell('count').column.name
        def costName = row.getCell('cost').column.name

        // Корректность даты договора
        def dt = contractDate
        if (dt != null && (dt < dFrom || dt > dTo)) {
            logger.warn("Строка $rowNum: «$contractDateName» не может быть вне налогового периода!")
        }

        // Проверка цены
        def res = null

        if (incomeBankSum != null && count != null && count != 0) {
            res = (incomeBankSum / count).setScale(0, RoundingMode.HALF_UP)
        }

        if (incomeBankSum == null || count == null || price != res) {
            logger.warn("Строка $rowNum: «$priceName» не равно отношению «$incomeBankSumName» и «$countName»!")
        }

        // Проверка доходности
        if (cost != incomeBankSum) {
            logger.warn("Строка $rowNum: «$costName» не может отличаться от «$incomeBankSumName»!")
        }

        // Корректность даты совершения сделки
        if (transactionDate < contractDate) {
            logger.warn("Строка $rowNum: «$transactionDateName» не может быть меньше «$contractDateName»!")
        }

        // Проверка стоимости
        if (price == null || count != null && cost != price * count) {
            logger.warn("Строка $rowNum: «$costName» не равна произведению «$countName» и «$priceName»!")
        }

        //Проверки соответствия НСИ
        checkNSI(row, "jurName", "Организации-участники контролируемых сделок",9)
        checkNSI(row, "countryCode", "ОКСМ",10)
        checkNSI(row, "country", "ОКСМ",10)
        checkNSI(row, "region", "Коды субъектов Российской Федерации",4)
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
 * Расчеты. Алгоритмы заполнения полей формы.
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
        row.rowNum = index++

        incomeBankSum = row.incomeBankSum
        count = row.count
        // Расчет поля "Цена"
        if (count != null && count != 0) {
            row.price = incomeBankSum / count
        }
        // Расчет поля "Стоимость"
        row.cost = row.incomeBankSum

        // Расчет полей зависимых от справочников
        if (row.jurName != null) {
            def map = refBookService.getRecordData(9, row.jurName)
            row.innKio = map.INN_KIO.stringValue
            row.countryCode = map.COUNTRY.referenceValue
        } else {
            row.innKio = null
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
        if (source != null
                && source.state == WorkflowState.ACCEPTED
                && source.getFormType().getId() == formData.getFormType().getId()) {
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
        if (indexRow <= 2) {
            continue
        }

        if ((row.cell.find{it.text()!=""}.toString())=="") {
            break
        }

        def newRow = formData.createDataRow()
        getEditColumns().each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        def indexCell = 0
        // графа 1
        newRow.rowNum = indexRow - 2

        // графа 2
        newRow.jurName = getRecordId(9, 'NAME', row.cell[indexCell].text(), date, cache, indexRow, indexCell)
        indexCell++


        // графа 3
        indexCell++

        // графа 4
        indexCell++

        // графа 5
        newRow.incomeBankSum = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 6
        newRow.contractNum = row.cell[indexCell].text()
        indexCell++

        // графа 7
        newRow.contractDate = getDate(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 8
        newRow.country = getRecordId(10, 'CODE', row.cell[indexCell].text(), date, cache, indexRow, indexCell)
        indexCell++

        // графа 9
        newRow.region = getRecordId(4, 'CODE', row.cell[indexCell].text(), date, cache, indexRow, indexCell, false)
        indexCell++

        // графа 10
        newRow.city = row.cell[indexCell].text()
        indexCell++

        // графа 11
        newRow.settlement = row.cell[indexCell].text()
        indexCell++

        // графа 12
        newRow.count = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 13
        indexCell++

        // графа 14
        indexCell++

        // графа 15
        newRow.transactionDate = getDate(row.cell[indexCell].text(), indexRow, indexCell)

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
    def colCount = 13
    // проверить количество строк и колонок в шапке
    if (xml.row.size() < headRowCount || xml.row[0].cell.size() < colCount) {
        return false
    }
    def result = (
            xml.row[0].cell[0] == 'Полное наименование юридического лица с указанием ОПФ' &&
            xml.row[2].cell[0] == 'гр. 2' &&

            xml.row[0].cell[1] == 'ИНН/ КИО' &&
            xml.row[2].cell[1] == 'гр. 3' &&

            xml.row[0].cell[2] == 'Код страны по классификатору ОКСМ' &&
            xml.row[2].cell[2] == 'гр. 4' &&

            xml.row[0].cell[3] == 'Сумма доходов Банка, руб.' &&
            xml.row[2].cell[3] == 'гр. 5' &&

            xml.row[0].cell[4] == 'Номер договора' &&
            xml.row[2].cell[4] == 'гр. 6' &&

            xml.row[0].cell[5] == 'Дата договора' &&
            xml.row[2].cell[5] == 'гр. 7' &&

            xml.row[0].cell[6] == 'Адрес местонахождения объекта недвижимости ' &&
            xml.row[1].cell[6] == 'Страна (код)' &&
            xml.row[2].cell[6] == 'гр. 8' &&

            xml.row[1].cell[7] == 'Регион (код)' &&
            xml.row[2].cell[7] == 'гр. 9' &&

            xml.row[1].cell[8] == 'Город' &&
            xml.row[2].cell[8] == 'гр. 10' &&

            xml.row[1].cell[9] == 'Населенный пункт' &&
            xml.row[2].cell[9] == 'гр. 11' &&

            xml.row[0].cell[10] == 'Количество' &&
            xml.row[2].cell[10] == 'гр. 12' &&

            xml.row[0].cell[11] == 'Цена' &&
            xml.row[2].cell[11] == 'гр. 13' &&

            xml.row[0].cell[12] == 'Стоимость' &&
            xml.row[2].cell[12] == 'гр. 14' &&

            xml.row[0].cell[13] == 'Дата совершения сделки' &&
            xml.row[2].cell[13] == 'гр. 15')

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

/**
 * Получить record_id элемента справочника.
 *
 * @param value
 */
def getRecordId(def ref_id, String alias, String value, Date date, def cache, int indexRow, int indexCell, boolean mandatory=true) {
    String filter = alias + "= '" + value + "'"
    if (value=='') filter = "$alias is null"
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
    } else if (mandatory || value!='') {
        throw new Exception("Строка ${indexRow + 3} столбец ${indexCell + 2} содержит значение, отсутствующее в справочнике!")
    }
    return null
}