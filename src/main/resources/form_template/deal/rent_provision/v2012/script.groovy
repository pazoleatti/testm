package form_template.deal.rent_provision.v2012

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook

import java.math.RoundingMode
import java.text.SimpleDateFormat
/**
 * 376 - Предоставление нежилых помещений в аренду (1)
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
    ['jurName', 'incomeBankSum', 'outcomeBankSum', 'contractNum', 'contractDate', 'country',
            'region', 'city', 'settlement', 'count', 'transactionDate']
}

/**
 * Проверка при создании формы.
 */
void checkCreation() {
    def findForm = formDataService.find(formData.formType.id, formData.kind, formData.departmentReportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)

    if (findForm != null) {
        logger.error('Формирование нового отчета невозможно, т.к. отчет с указанными параметрами уже сформирован.')
    }
}

void addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def row = formData.createDataRow()
    def dataRows = dataRowHelper.getAllCached()
    def size = dataRows.size()
    def index = currentDataRow != null ? (currentDataRow.getIndex() + 1) : (size == 0 ? 1 : (size + 1))
    row.keySet().each {
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
    def startDate = reportPeriodService.getStartDate(formData.reportPeriodId)
    def dFrom = startDate.time
    startDate.add(Calendar.YEAR, 1)
    startDate.add(Calendar.DAY_OF_YEAR, -1)
    def dTo = startDate.time

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
        def outcomeBankSum = row.outcomeBankSum
        def transactionDate = row.transactionDate
        def contractDate = row.contractDate

        def bankSum = null
        def msgBankSum = getColumnName(row, 'incomeBankSum')
        if (row.incomeBankSum !=null && row.outcomeBankSum ==null){
            bankSum = row.incomeBankSum
        }
        if (row.outcomeBankSum !=null && row.incomeBankSum ==null) {
            bankSum = row.outcomeBankSum
            msgBankSum = getColumnName(row, 'outcomeBankSum')
        }

        //Наименования колонок
        def contractDateName = getColumnName(row, 'contractDate')
        def transactionDateName = getColumnName(row, 'transactionDate')
        def priceName = getColumnName(row, 'price')
        def countName = getColumnName(row, 'count')
        def costName = getColumnName(row, 'cost')

        // Корректность даты договора
        def dt = contractDate
        if (dt != null && (dt < dFrom || dt > dTo)) {
            logger.warn("Строка $rowNum: «$contractDateName» не может быть вне налогового периода!")
        }

        // Проверка цены
        def res = null
        if (bankSum != null && count != null && count != 0) {
            res = (bankSum / count).setScale(0, RoundingMode.HALF_UP)
        }
        if (bankSum == null || count == null || price != res) {
            logger.warn("Строка $rowNum: «$priceName» не равно отношению «$msgBankSum» и «$countName»!")
        }

        // Проверка доходности
        if (cost != bankSum) {
            logger.warn("Строка $rowNum: «$costName» не может отличаться от «$msgBankSum»!")
        }

        // Заполнение граф 5.1 и 5.2
        if (incomeBankSum == null && outcomeBankSum == null) {
            def msg1 = getColumnName(row, 'incomeBankSum')
            def msg2 = getColumnName(row, 'outcomeBankSum')
            logger.warn("Строка $rowNum: Должна быть заполнена графа «$msg1» или графа «$msg2»!")
        }
        if (incomeBankSum != null && outcomeBankSum != null) {
            def msg1 = getColumnName(row, 'incomeBankSum')
            def msg2 = getColumnName(row, 'outcomeBankSum')
            logger.warn("Строка $rowNum: Графа «$msg1» и графа «$msg2» не могут быть заполнены одновременно!")
        }

        // Корректность даты совершения сделки
        if (transactionDate < contractDate) {
            logger.warn("Строка $rowNum: «$transactionDateName» не может быть меньше «$contractDateName»!")
        }

        // Проверка стоимости
        if (price == null || count != null && cost != price * count) {
            logger.warn("Строка $rowNum: «$costName» не равна произведению «$countName» и «$priceName»!")
        }

        // Проверка заполненности одного из атрибутов
        if (row.city != null && !row.city.toString().isEmpty() && row.settlement != null && !row.settlement.toString().isEmpty()) {
            def cityName = getColumnName(row, 'city')
            def settleName = getColumnName(row, 'settlement')
            logger.warn("Строка $rowNum: Если заполнена графа «$cityName», то графа «$settleName» не должна быть заполнена!")
        }

        // Проверка заполнения региона
        if (row.country != null) {
            def country = refBookService.getStringValue(10, row.country, 'CODE')
            if (country != null) {
                def regionName = getColumnName(row, 'region')
                def countryName = getColumnName(row, 'country')
                if (country == '643' && row.region == null) {
                    logger.warn("Строка $rowNum: «$regionName» должен быть заполнен, т.к. в «$countryName» указан код 643!")
                } else if (country != '643' && row.region != null) {
                    logger.warn("Строка $rowNum: «$regionName» не должен быть заполнен, т.к. в «$countryName» указан код, отличный от 643!")
                }
            }
        }

        //Проверки соответствия НСИ
        checkNSI(row, "jurName", "Организации-участники контролируемых сделок", 9)
        checkNSI(row, "countryCode", "ОКСМ", 10)
        checkNSI(row, "country", "ОКСМ", 10)
        checkNSI(row, "region", "Коды субъектов Российской Федерации", 4)
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

        def bankSum = null
        if (row.incomeBankSum !=null && row.outcomeBankSum ==null)
            bankSum = row.incomeBankSum
        if (row.outcomeBankSum !=null && row.incomeBankSum ==null)
            bankSum = row.outcomeBankSum

        count = row.count

        // Расчет поля "Цена"
        if (bankSum != null && count != null && count != 0) {
            row.price = bankSum / count
        }
        // Расчет поля "Стоимость"
        row.cost = bankSum

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
        addData(xml)
    } catch (Exception e) {
        logger.error("" + e.message)
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
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def rows = new LinkedList()

    def indexRow = -1
    for (def row : xml.row) {
        indexRow++

        // пропустить шапку таблицы
        if (indexRow <= 2) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
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
        newRow.jurName = getRecordId(9, 'NAME', row.cell[indexCell].text(), date, cache, indexRow, indexCell, false)
        def map = newRow.jurName == null ? null : refBookService.getRecordData(9, newRow.jurName)
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

        // графа 5.1
        newRow.incomeBankSum = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 5.2
        newRow.outcomeBankSum = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 6
        newRow.contractNum = row.cell[indexCell].text()
        indexCell++

        // графа 7
        newRow.contractDate = getDate(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 8
        newRow.country = getRecordId(10, 'CODE', row.cell[indexCell].text(), date, cache, indexRow, indexCell, false)
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

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}

/**
 * Проверить шапку таблицы.
 *
 * @param xml данные
 * @param headRowCount количество строк в шапке
 */
def checkTableHead(def xml, def headRowCount) {
    def colCount = 14
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
            xml.row[2].cell[3] == 'гр. 5.1' &&

            xml.row[0].cell[4] == 'Сумма расходов Банка, руб.' &&
            xml.row[2].cell[4] == 'гр. 5.2' &&

            xml.row[0].cell[5] == 'Номер договора' &&
            xml.row[2].cell[5] == 'гр. 6' &&

            xml.row[0].cell[6] == 'Дата договора' &&
            xml.row[2].cell[6] == 'гр. 7' &&

            xml.row[0].cell[7] == 'Адрес местонахождения объекта недвижимости' &&
            xml.row[1].cell[7] == 'Страна (код)' &&
            xml.row[2].cell[7] == 'гр. 8' &&

            xml.row[1].cell[8] == 'Регион (код)' &&
            xml.row[2].cell[8] == 'гр. 9' &&

            xml.row[1].cell[9] == 'Город' &&
            xml.row[2].cell[9] == 'гр. 10' &&

            xml.row[1].cell[10] == 'Населенный пункт' &&
            xml.row[2].cell[10] == 'гр. 11' &&

            xml.row[0].cell[11] == 'Количество' &&
            xml.row[2].cell[11] == 'гр. 12' &&

            xml.row[0].cell[12] == 'Цена' &&
            xml.row[2].cell[12] == 'гр. 13' &&

            xml.row[0].cell[13] == 'Стоимость' &&
            xml.row[2].cell[13] == 'гр. 14' &&

            xml.row[0].cell[14] == 'Дата совершения сделки' &&
            xml.row[2].cell[14] == 'гр. 15')

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
        throw new Exception("Проверка файла: Строка ${indexRow + 3} столбец ${indexCell + 2} содержит недопустимый тип данных!")
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
        throw new Exception("Проверка файла: Строка ${indexRow + 3} столбец ${indexCell + 2} содержит недопустимый тип данных!")
    }
}

// Получить record_id по значению атрибута справочника
def getRecordId(
        def ref_id, String alias, String value, Date date,
        def cache, int indexRow, int indexCell, boolean mandatory = true) {
    String filter = "LOWER($alias) = LOWER('$value')"
    if (value == '') filter = "$alias is null"
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