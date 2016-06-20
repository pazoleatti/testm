package form_template.deal.bonds_trade.v2012

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook

import java.math.RoundingMode
import java.text.SimpleDateFormat

/**
 * 384 - Реализация и приобретение ценных бумаг (9)
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
    def index = currentDataRow != null ? currentDataRow.getIndex() : size
    row.keySet().each {
        row.getCell(it).setStyleAlias('Автозаполняемая')
    }
    [
            'transactionDeliveryDate',
            'contraName',
            'transactionMode',
            'transactionSumCurrency',
            'currency',
            'courseCB',
            'transactionSumRub',
            'contractNum',
            'contractDate',
            'transactionDate',
            'bondRegCode',
            'bondCount',
            'transactionType'
    ].each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    dataRowHelper.insert(row, index + 1)
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

    def rowNum = 0
    for (row in dataRowHelper.getAllCached()) {
        rowNum++
        if (row.getAlias() != null) {
            continue;
        }

        [
                'rowNum', // № п/п
                'transactionDeliveryDate', // Дата сделки (поставки)
                'contraName', // Наименование контрагента и ОПФ
                'transactionMode', // Режим переговорных сделок
                'innKio', // ИНН/КИО контрагента
                'contraCountry', // Страна местонахождения контрагента
                'contraCountryCode', // Код страны местонахождения контрагента
                'transactionSumCurrency', // Сумма сделки (с учетом НКД), в валюте расчетов
                'currency', // Валюта расчетов по сделке
                'courseCB', // Курс ЦБ РФ
                'transactionSumRub', // Сумма сделки (с учетом НКД), руб.
                'contractNum', // Номер договора
                'contractDate', // Дата договора
                'transactionDate', // Дата заключения сделки
                'bondRegCode', // Регистрационный код ценной бумаги
                'bondCount', // Количество бумаг по сделке, шт.
                'priceOne', // Цена за 1 шт., руб.
                'transactionType' // Тип сделки
        ].each {
            if (row.getCell(it).value == null || row.getCell(it).value.toString().isEmpty()) {
                def msg = row.getCell(it).column.name
                logger.warn("Строка $rowNum: Графа «$msg» не заполнена!")
            }
        }

        def transactionDeliveryDate = row.transactionDeliveryDate
        def transactionDate = row.transactionDate
        def transactionSumRub = row.transactionSumRub
        def bondCount = row.bondCount
        def priceOne = row.priceOne
        def courseCB = row.courseCB
        def transactionSumCurrency = row.transactionSumCurrency
        def contractDate = row.contractDate

        // Корректность даты сделки
        if (transactionDeliveryDate < transactionDate) {
            def msg1 = getColumnName(row, 'transactionDeliveryDate')
            def msg2 = getColumnName(row, 'transactionDate')
            logger.warn("Строка $rowNum: «$msg1» не может быть меньше «$msg2»!")
        }

        // Проверка конверсии
        if (courseCB == null || transactionSumCurrency == null || transactionSumRub != (courseCB * transactionSumCurrency).setScale(0, RoundingMode.HALF_UP)) {
            def msg1 = getColumnName(row, 'transactionSumRub')
            def msg2 = getColumnName(row, 'courseCB')
            def msg3 = getColumnName(row, 'transactionSumCurrency')
            logger.warn("Строка $rowNum: «$msg1» не соответствует «$msg2» с учетом данных «$msg3»!")
        }

        // Корректность даты договора
        def dt = contractDate
        if (dt != null && (dt < dFrom || dt > dTo)) {
            def msg = getColumnName(row, 'contractDate')
            logger.warn("Строка $rowNum: «$msg» в строке $rowNum не может быть вне налогового периода!")
        }

        // Корректность даты заключения сделки
        if (transactionDeliveryDate < contractDate) {
            def msg1 = getColumnName(row, 'transactionDate')
            def msg2 = getColumnName(row, 'contractDate')
            logger.warn("Строка $rowNum: «$msg1» не может быть меньше «$msg2»!")
        }

        // Проверка цены сделки
        def res = null

        if (transactionSumRub != null && bondCount != null) {
            res = (transactionSumRub / bondCount).setScale(0, RoundingMode.HALF_UP)
        }

        if (transactionSumRub == null || bondCount == null || priceOne != res) {
            def msg1 = getColumnName(row, 'priceOne')
            def msg2 = getColumnName(row, 'transactionSumRub')
            def msg3 = getColumnName(row, 'bondCount')
            logger.warn("Строка $rowNum: «$msg1» не равно отношению «$msg2» и «$msg3»!")
        }

        //Проверки соответствия НСИ
        checkNSI(row, "contraName", "Организации-участники контролируемых сделок", 9)
        checkNSI(row, "contraCountry", "ОКСМ", 10)
        checkNSI(row, "contraCountryCode", "ОКСМ", 10)
        checkNSI(row, "transactionMode", "Режим переговорных сделок", 14)
        checkNSI(row, "transactionType", "Типы сделок", 16)
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
        // Расчет поля "Цена за 1 шт., руб."
        transactionSumRub = row.transactionSumRub
        bondCount = row.bondCount

        if (transactionSumRub != null && bondCount != null && bondCount != 0) {
            row.priceOne = transactionSumRub / bondCount;
        }

        // Расчет полей зависимых от справочников
        if (row.contraName != null) {
            def map = refBookService.getRecordData(9, row.contraName)
            row.innKio = map.INN_KIO.stringValue
            row.contraCountry = map.COUNTRY.referenceValue
            row.contraCountryCode = map.COUNTRY.referenceValue
        } else {
            row.innKio = null
            row.contraCountry = null
            row.contraCountryCode = null
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

    def xmlString = importService.getData(is, fileName, 'windows-1251', 'Сокращенная форма\nДанные для расчета сумм доходов по сделкам', null)
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
        if (!checkTableHead(xml, 2)) {
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
        [
                'transactionDeliveryDate', // Дата сделки (поставки)
                'contraName', // Наименование контрагента и ОПФ
                'transactionMode', // Режим переговорных сделок
                'transactionSumCurrency', // Сумма сделки (с учетом НКД), в валюте расчетов
                'currency', // Валюта расчетов по сделке
                'courseCB', // Курс ЦБ РФ
                'transactionSumRub', // Сумма сделки (с учетом НКД), руб.
                'contractNum', // Номер договора
                'contractDate', // Дата договора
                'transactionDate', // Дата заключения сделки
                'bondRegCode', // Регистрационный код ценной бумаги
                'bondCount', // Количество бумаг по сделке, шт.
                'transactionType' // Тип сделки
        ].each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        def indexCell = 0
        // графа 1
        newRow.rowNum = indexRow - 3

        // графа 2
        newRow.transactionDeliveryDate = getDate(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 3
        newRow.contraName = getRecordId(9, 'NAME', row.cell[indexCell].text(), date, cache, indexRow, indexCell, false)
        def map = newRow.contraName == null ? null : refBookService.getRecordData(9, newRow.contraName)
        indexCell++

        // графа 4
        newRow.transactionMode = getRecordId(14, 'MODE', row.cell[indexCell].text(), date, cache, indexRow, indexCell, false)
        indexCell++

        // графа 5
        if (map != null) {
            def text = row.cell[indexCell].text()
            if ((text != null && !text.isEmpty() && !text.equals(map.INN_KIO.stringValue)) || ((text == null || text.isEmpty()) && map.INN_KIO.stringValue != null)) {
                logger.warn("Проверка файла: Строка ${indexRow + 3} столбец ${indexCell + 2} содержит значение, отсутствующее в справочнике «" + refBookFactory.get(9).getName()+"»!")
            }
        }
        indexCell++

        // графа 6.1
        if (map != null) {
            def text = row.cell[indexCell].text()
            map = refBookService.getRecordData(10, map.COUNTRY.referenceValue)
            if ((text != null && !text.isEmpty() && !text.equals(map.NAME.stringValue)) || ((text == null || text.isEmpty()) && map.NAME.stringValue != null)) {
                logger.warn("Проверка файла: Строка ${indexRow + 3} столбец ${indexCell + 2} содержит значение, отсутствующее в справочнике «" + refBookFactory.get(10).getName()+"»!")
            }
        }
        indexCell++

        // графа 6.2
        if (map != null) {
            def text = row.cell[indexCell].text()
            if ((text != null && !text.isEmpty() && !text.equals(map.CODE.stringValue)) || ((text == null || text.isEmpty()) && map.CODE.stringValue != null)) {
                logger.warn("Проверка файла: Строка ${indexRow + 3} столбец ${indexCell + 2} содержит значение, отсутствующее в справочнике «" + refBookFactory.get(10).getName()+"»!")
            }
        }
        indexCell++

        // графа 8
        newRow.transactionSumCurrency = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 9
        newRow.currency = getRecordId(15, 'CODE', row.cell[indexCell].text(), date, cache, indexRow, indexCell, false)
        indexCell++

        // графа 10
        newRow.courseCB = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 11
        newRow.transactionSumRub = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 12
        newRow.contractNum = row.cell[indexCell].text()
        indexCell++

        // графа 13
        newRow.contractDate = getDate(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 14
        newRow.transactionDate = getDate(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 15
        newRow.bondRegCode = row.cell[indexCell].text()
        indexCell++

        // графа 16
        newRow.bondCount = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 17
        indexCell++

        // графа 18
        newRow.transactionType = getRecordId(16, 'CODE', row.cell[indexCell].text(), date, cache, indexRow, indexCell, false)

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
    def colCount = 17
    // проверить количество строк и колонок в шапке
    if (xml.row.size() < headRowCount || xml.row[0].cell.size() < colCount) {
        return false
    }
    def result = (
            xml.row[0].cell[0] == 'Сокращенная форма\nДанные для расчета сумм доходов по сделкам' &&
                    xml.row[1].cell[0] == 'Дата сделки (поставки)' &&
                    xml.row[2].cell[0] == 'гр. 2' &&

                    xml.row[1].cell[1] == 'Наименование контрагента и ОПФ' &&
                    xml.row[2].cell[1] == 'гр. 3' &&

                    xml.row[1].cell[2] == 'Режим переговорных сделок' &&
                    xml.row[2].cell[2] == 'гр. 4' &&

                    xml.row[1].cell[3] == 'ИНН/ КИО контрагента' &&
                    xml.row[2].cell[3] == 'гр. 5' &&

                    xml.row[1].cell[4] == 'Страна местонахождения контрагента' &&
                    xml.row[2].cell[4] == 'гр. 6.1' &&

                    xml.row[1].cell[5] == 'Код страны местонахождения контрагента' &&
                    xml.row[2].cell[5] == 'гр. 6.2' &&

                    xml.row[1].cell[6] == 'Сумма сделки (с учетом НКД), в валюте расчетов' &&
                    xml.row[2].cell[6] == 'гр. 7.1' &&

                    xml.row[1].cell[7] == 'Валюта расчетов по сделке' &&
                    xml.row[2].cell[7] == 'гр. 7.2' &&

                    xml.row[1].cell[8] == 'Курс ЦБ РФ' &&
                    xml.row[2].cell[8] == 'гр. 7.3' &&

                    xml.row[1].cell[9] == 'Сумма сделки (с учетом НКД), руб.' &&
                    xml.row[2].cell[9] == 'гр. 7.4' &&

                    xml.row[0].cell[10] == 'Номер договора' &&
                    xml.row[2].cell[10] == 'гр. 8' &&

                    xml.row[0].cell[11] == 'Дата договора' &&
                    xml.row[2].cell[11] == 'гр. 9' &&

                    xml.row[0].cell[12] == 'Дата (заключения) сделки' &&
                    xml.row[2].cell[12] == 'гр. 10' &&

                    xml.row[0].cell[13] == 'Регистрационный код ценной бумаги' &&
                    xml.row[2].cell[13] == 'гр. 11' &&

                    xml.row[0].cell[14] == 'Количество бумаг по сделке, шт.' &&
                    xml.row[2].cell[14] == 'гр. 12' &&

                    xml.row[0].cell[15] == 'Цена за 1 шт., руб.' &&
                    xml.row[2].cell[15] == 'гр. 13' &&

                    xml.row[0].cell[16] == 'Тип сделки' &&
                    xml.row[2].cell[16] == 'гр. 14')
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
        throw new Exception("Проверка файла: Строка ${indexRow + 2} столбец ${indexCell + 2} содержит недопустимый тип данных!")
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
        throw new Exception("Проверка файла: Строка ${indexRow + 2} столбец ${indexCell + 2} содержит недопустимый тип данных!")
    }
}

/**
 * Получить record_id элемента справочника.
 *
 * @param value
 */
def getRecordId(
        def ref_id, String alias, String value, Date date,
        def cache, int indexRow, int indexCell, boolean mandatory = true) {
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
    def msg = "Проверка файла: Строка ${indexRow + 2} столбец ${indexCell + 2} содержит значение, отсутствующее в справочнике «" + refBookFactory.get(ref_id).getName()+"»!"
    if (mandatory) {
        throw new Exception(msg)
    } else {
        logger.warn(msg)
    }
    return null
}
