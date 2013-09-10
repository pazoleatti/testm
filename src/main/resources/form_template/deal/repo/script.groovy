package form_template.deal.repo

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent

import java.text.SimpleDateFormat

/**
 * 383 - Сделки РЕПО
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
    case FormDataEvent.IMPORT:
        importData()
        break
}

/**
 * Проверка при создании формы.
 */
void checkCreation() {
    def findForm = formDataService.find(formData.formType.id, formData.kind, formData.departmentId,
            formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Формирование нового отчета невозможно, т.к. отчет с указанными параметрами уже сформирован.')
    }
}

void addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def row = formData.createDataRow()
    def dataRows = dataRowHelper.getAllCached()
    def size = dataRows.size()
    def index = currentDataRow != null ? currentDataRow.getIndex() : (size == 0 ? 1 : size)
    ['jurName', 'contractNum', 'contractDate', 'transactionNum', 'transactionDeliveryDate', 'dealsMode',
            'date1', 'date2', 'percentIncomeSum', 'percentConsumptionSum', 'priceFirstCurrency', 'currencyCode',
            'courseCB', 'priceFirstRub', 'transactionDate'].each {
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

    for (row in dataRowHelper.getAllCached()) {
        if (row.getAlias() != null) {
            continue
        }

        def rowNum = row.getIndex()
        [
                'rowNum', // № п/п
                'jurName', // Полное наименование юридического лица с указанием ОПФ
                'innKio', // ИНН/КИО
                'country', // Наименование страны регистрации
                'countryCode', // Код страны по классификатору ОКСМ
                'contractNum', // Номер договора
                'contractDate', // Дата договора
                'transactionNum', // Номер сделки
                'transactionDeliveryDate', // Дата заключения сделки
                'dealsMode', // Режим переговорных сделок
                'date1', // Дата исполнения 1-ой части сделки
                'date2', // Дата исполнения 2-ой части сделки
                'percentIncomeSum', // Сумма процентного дохода (руб.)
                'percentConsumptionSum', // Сумма процентного расхода (руб.)
                'priceFirstCurrency', // Цена 1-ой части сделки, ед. валюты
                'currencyCode', // Код валюты расчетов по сделке
                'courseCB', // Курс ЦБ РФ
                'priceFirstRub', // Цена 1-ой части сделки, руб.
                'transactionDate' // Дата совершения сделки
        ].each {
            if (row.getCell(it).value == null || row.getCell(it).value.toString().isEmpty()) {
                def msg = row.getCell(it).column.name
                logger.warn("Графа «$msg» в строке $rowNum не заполнена!")
            }
        }

        def contractDate = row.contractDate
        def transactionDate = row.transactionDate
        def transactionDeliveryDate = row.transactionDeliveryDate
        def percentIncomeSum = row.percentIncomeSum
        def percentConsumptionSum = row.percentConsumptionSum

        // Заполнение граф 13 и 14
        if (percentIncomeSum == null && percentConsumptionSum == null) {
            def msg1 = row.getCell('percentIncomeSum').column.name
            def msg2 = row.getCell('percentConsumptionSum').column.name
            logger.warn("Должна быть заполнена графа «$msg1» или графа «$msg2» в строке $rowNum!")
        }
        if (percentIncomeSum != null && percentConsumptionSum != null) {
            def msg1 = row.getCell('percentIncomeSum').column.name
            def msg2 = row.getCell('percentConsumptionSum').column.name
            logger.warn("Графа  «$msg1» и графа «$msg2» в строке $rowNum не могут быть заполнены одновременно!")
        }

        // Корректность даты договора
        def dt = contractDate
        if (dt != null && (dt < dFrom || dt > dTo)) {
            def msg = row.getCell('contractDate').column.name

            if (dt > dTo) {
                logger.warn("«$msg» не может быть больше даты окончания отчётного периода в строке $rowNum!")
            }

            if (dt < dFrom) {
                logger.warn("«$msg» не может быть меньше даты начала отчётного периода в строке $rowNum!")
            }
        }

        // Корректность даты (заключения) сделки
        if (transactionDeliveryDate < contractDate) {
            def msg1 = row.getCell('transactionDate').column.name
            def msg2 = row.getCell('contractDate').column.name
            logger.warn("«$msg1» не может быть меньше «$msg2» в строке $rowNum!")
        }

        // Корректность даты исполнения 1–ой части сделки
        def dt1 = row.date1
        if (dt1 != null && (dt1 < dFrom || dt1 > dTo)) {
            def msg = row.getCell('date1').column.name

            if (dt1 > dTo) {
                logger.warn("«$msg» не может быть больше даты окончания отчётного периода в строке $rowNum!")
            }

            if (dt1 < dFrom) {
                logger.warn("«$msg» не может быть меньше даты начала отчётного периода в строке $rowNum!")
            }
        }

        // Корректность даты совершения сделки
        if (transactionDeliveryDate < transactionDate) {
            def msg1 = row.getCell('transactionDeliveryDate').column.name
            def msg2 = row.getCell('transactionDate').column.name
            logger.warn("«$msg1» не может быть меньше «$msg2» в строке $rowNum!")
        }

        //Проверки соответствия НСИ
        checkNSI(row, "jurName", "Организации-участники контролируемых сделок", 9)
        checkNSI(row, "country", "ОКСМ", 10)
        checkNSI(row, "countryCode", "ОКСМ", 10)
        checkNSI(row, "dealsMode", "Режим переговорных сделок", 14)
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
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    for (row in dataRows) {
        // Порядковый номер строки
        row.rowNum = row.getIndex()
        // Расчет полей зависимых от справочников
        if (row.jurName != null) {
            def map = refBookService.getRecordData(9, row.jurName)
            row.innKio = map.INN_KIO.numberValue
            row.country = map.COUNTRY.referenceValue
            row.countryCode = map.COUNTRY.referenceValue
        } else {
            row.innKio = null
            row.country = null
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

    if (!fileName.contains('.xls')) {
        logger.error('Формат файла должен быть *.xls')
        return
    }

    def xmlString = importService.getData(is, fileName, 'windows-1251', 'Полное наименование с указанием ОПФ', null)
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
        addData(xml,2)
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

        if ((row.cell.find{it.text()!=""}.toString())=="") {
            break
        }

        def newRow = formData.createDataRow()
        ['jurName', 'contractNum', 'contractDate', 'transactionNum', 'transactionDeliveryDate', 'dealsMode',
                'date1', 'date2', 'percentIncomeSum', 'percentConsumptionSum', 'priceFirstCurrency', 'currencyCode',
                'courseCB', 'priceFirstRub', 'transactionDate'].each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        def indexCell = 0
        // графа 1
        newRow.rowNum = indexRow - headRowCount

        // графа 2
        newRow.jurName = getRecordId(9, 'NAME', row.cell[indexCell].text(), date, cache, indexRow, indexCell)
        indexCell++

        // графа 3
//        newRow.innKio =
        indexCell++

        // графа 4
//        newRow.country =
        indexCell++

        // графа 5
        //newRow.countryCode =
        indexCell++

        // графа 6
        newRow.contractNum = row.cell[indexCell].text()
        indexCell++

        // графа 7
        newRow.contractDate = getDate(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 8
        newRow.transactionNum = row.cell[indexCell].text()
        indexCell++

        // графа 9
        newRow.transactionDeliveryDate = getDate(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 10
        newRow.dealsMode = getRecordId(14, 'MODE', row.cell[indexCell].text(), date, cache, indexRow, indexCell)
        indexCell++

        // графа 11
        newRow.date1 = getDate(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 12
        newRow.date2 = getDate(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 13
        newRow.percentIncomeSum = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 14
        newRow.percentConsumptionSum = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 15
        newRow.priceFirstCurrency = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 16
        newRow.currencyCode = getRecordId(15, 'CODE', row.cell[indexCell].text(), date, cache, indexRow, indexCell)
        indexCell++

        // графа 17
        newRow.courseCB = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 18
        newRow.priceFirstRub = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 19
        newRow.transactionDate = getDate(row.cell[indexCell].text(), indexRow, indexCell)

        data.insert(newRow, indexRow - headRowCount)
    }
}


/**
 * Проверить шапку таблицы.
 *
 * @param xml данные
 * @param headRowCount количество строк в шапке
 */
def checkTableHead(def xml, int headRowCount) {
    def colCount = 18
    // проверить количество строк и колонок в шапке
    if (xml.row.size() < headRowCount || xml.row[0].cell.size() < colCount) {
        return false
    }
    def result = (xml.row[0].cell[0] == 'Полное наименование с указанием ОПФ' &&
            xml.row[1].cell[0] ==  '2' &&
            xml.row[2].cell[0] ==  'Гр. 2' &&
            xml.row[0].cell[1] == 'ИНН/ КИО' &&
            xml.row[1].cell[1] ==  '3' &&
            xml.row[2].cell[1] ==  'Гр. 3' &&
            xml.row[0].cell[2] == 'Наименование страны регистрации' &&
            xml.row[1].cell[2] ==  '4' &&
            xml.row[2].cell[2] ==  'Гр. 4.1' &&
            xml.row[0].cell[3] == 'Код страны регистрации по классификатору ОКСМ' &&
            xml.row[1].cell[3] ==  '5' &&
            xml.row[2].cell[3] ==  'Гр. 4.2' &&
            xml.row[0].cell[4] == 'Номер договора' &&
            xml.row[1].cell[4] ==  '6' &&
            xml.row[2].cell[4] ==  'Гр. 5' &&
            xml.row[0].cell[5] == 'Дата договора' &&
            xml.row[1].cell[5] ==  '7' &&
            xml.row[2].cell[5] ==  'Гр. 6' &&
            xml.row[0].cell[6] == 'Номер сделки' &&
            xml.row[1].cell[6] ==  '8' &&
            xml.row[2].cell[6] ==  'Гр. 7' &&
            xml.row[0].cell[7] == 'Дата (заключения) сделки ' &&
            xml.row[1].cell[7] ==  '9' &&
            xml.row[2].cell[7] ==  'Гр. 8' &&
            xml.row[0].cell[8] == 'Режим переговорных сделок' &&
            xml.row[1].cell[8] ==  '10' &&
            xml.row[2].cell[8] ==  'Гр. 9' &&
            xml.row[0].cell[9] == 'Дата исполнения  1-ой части сделки ' &&
            xml.row[1].cell[9] ==  '11' &&
            xml.row[2].cell[9] ==  'Гр. 10.1' &&
            xml.row[0].cell[10] == 'Дата исполнения  2-ой части сделки ' &&
            xml.row[1].cell[10] ==  '12' &&
            xml.row[2].cell[10] ==  'Гр. 10.2' &&
            xml.row[0].cell[11] == 'Сумма процентного дохода (руб.)' &&
            xml.row[1].cell[11] ==  '13' &&
            xml.row[2].cell[11] ==  'Гр. 11.1' &&
            xml.row[0].cell[12] == 'Сумма процентного расхода (руб.)' &&
            xml.row[1].cell[12] ==  '14' &&
            xml.row[2].cell[12] ==  'Гр. 11.2' &&
            xml.row[0].cell[13] == 'Цена 1-ой части сделки, ед. валюты' &&
            xml.row[1].cell[13] ==  '15' &&
            xml.row[2].cell[13] ==  'Гр. 12' &&
            xml.row[0].cell[14] == 'Код валюты расчетов по сделке' &&
            xml.row[1].cell[14] ==  '16' &&
            xml.row[2].cell[14] ==  'Гр. 13' &&
            xml.row[0].cell[15] == 'Курс ЦБ РФ' &&
            xml.row[1].cell[15] ==  '17' &&
            xml.row[2].cell[15] ==  'Гр. 14' &&
            xml.row[0].cell[16] == 'Цена 1-ой части сделки, руб.' &&
            xml.row[1].cell[16] ==  '18' &&
            xml.row[2].cell[16] ==  'Гр. 15' &&
            xml.row[0].cell[17] == 'Дата совершения сделки' &&
            xml.row[1].cell[17] ==  '19' &&
            xml.row[2].cell[17] ==  'Гр. 16')
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
    String filter = code + "= '"+ value+"'"
    if (value=='') filter = "$code is null"
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