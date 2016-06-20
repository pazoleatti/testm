package form_template.deal.nondeliverable.v2012

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook

import java.text.SimpleDateFormat

/**
 * 392 - Беспоставочные срочные сделки (17)
 *
 * @author Dmitriy Levykin
 */
switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkCreation()
        break
    case FormDataEvent.CALCULATE:
        deleteAllStatic()
        sort()
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
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
        deleteAllStatic()
        sort()
        calc()
        addAllStatic()
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
        deleteAllStatic()
        sort()
        calc()
        addAllStatic()
        logicCheck()
        break
// Импорт
    case FormDataEvent.IMPORT:
        importData()
        if (!logger.containsLevel(LogLevel.ERROR)) {
            deleteAllStatic()
            sort()
            calc()
            addAllStatic()
            logicCheck()
        }
        break
}

def getAtributes() {
    [
            rowNum: ['rowNum', 'гр. 1', '№ п/п'],
            name: ['name', 'гр. 2', 'Полное наименование с указанием ОПФ'],
            innKio: ['innKio', 'гр. 3', 'ИНН/КИО'],
            country: ['country', 'гр. 4.1', 'Наименование страны регистрации'],
            countryCode: ['countryCode', 'гр. 4.2', 'Код страны по классификатору ОКСМ'],
            contractNum: ['contractNum', 'гр. 5', 'Номер договора'],
            contractDate: ['contractDate', 'гр. 6', 'Дата договора'],
            transactionNum: ['transactionNum', 'гр. 7', 'Номер сделки'],
            transactionDeliveryDate: ['transactionDeliveryDate', 'гр. 8', 'Дата заключения сделки'],
            transactionType: ['transactionType', 'гр. 9', 'Вид срочной сделки'],
            incomeSum: ['incomeSum', 'гр. 10', 'Сумма доходов Банка по данным бухгалтерского учета, руб.'],
            consumptionSum: ['consumptionSum', 'гр. 11', 'Сумма расходов Банка по данным бухгалтерского учета, руб.'],
            price: ['price', 'гр. 12', 'Цена (тариф) за единицу измерения, руб.'],
            cost: ['cost', 'гр. 13', 'Итого стоимость, руб.'],
            transactionDate: ['transactionDate', 'гр. 14', 'Дата совершения сделки']
    ]
}

def getGroupColumns() {
    ['name', 'innKio', 'contractNum', 'contractDate', 'transactionType']
}

def getEditColumns() {
    ['name', 'contractNum', 'contractDate', 'transactionNum', 'transactionDeliveryDate',
            'transactionType', 'incomeSum', 'consumptionSum', 'transactionDate']
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
    def index = 0
    row.keySet().each {
        row.getCell(it).setStyleAlias('Автозаполняемая')
    }
    getEditColumns().each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    if (currentDataRow != null) {
        index = currentDataRow.getIndex()
        def pointRow = currentDataRow
        while (pointRow.getAlias() != null && index > 0) {
            pointRow = dataRows.get(--index)
        }
        if (index != currentDataRow.getIndex() && dataRows.get(index).getAlias() == null) {
            index++
        }
    } else if (size > 0) {
        for (int i = size - 1; i >= 0; i--) {
            def pointRow = dataRows.get(i)
            if (pointRow.getAlias() == null) {
                index = dataRows.indexOf(pointRow) + 1
                break
            }
        }
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

    def dataRows = dataRowHelper.getAllCached();
    def rowNum = 0
    for (row in dataRows) {
        rowNum++
        if (row.getAlias() != null) {
            continue
        }

        [
                'rowNum', // № п/п
                'name', // Полное наименование с указанием ОПФ
                'innKio', // ИНН/КИО
                'country', // Наименование страны регистрации
                'countryCode', // Код страны по классификатору ОКСМ
                'contractNum', // Номер договора
                'contractDate', // Дата договора
                'transactionNum', // Номер сделки
                'transactionDeliveryDate', // Дата заключения сделки
                'transactionType', // Вид срочной сделки
                'price', // Цена (тариф) за единицу измерения, руб.
                'cost', // Итого стоимость, руб.
                'transactionDate' // Дата совершения сделки
        ].each {
            if (row.getCell(it).value == null || row.getCell(it).value.toString().isEmpty()) {
                msg = row.getCell(it).column.name
                logger.warn("Строка $rowNum: Графа «$msg» не заполнена!")
            }
        }

        // Проверка доходов и расходов
        def consumptionSum = row.consumptionSum
        def incomeSum = row.incomeSum
        def price = row.price
        def transactionDeliveryDate = row.transactionDeliveryDate
        def contractDate = row.contractDate
        def cost = row.cost
        def transactionDate = row.transactionDate

        // В одной строке если не заполнена графа 11, то должна быть заполнена графа 12 и наоборот
        if (consumptionSum == null && incomeSum == null) {
            def msg1 = getColumnName(row, 'consumptionSum')
            def msg2 = getColumnName(row, 'incomeSum')
            logger.warn("Строка $rowNum: Одна из граф «$msg1» и «$msg2» должна быть заполнена!")
        }

        // Корректность даты договора
        def dt = row.contractDate
        if (dt != null && (dt < dFrom || dt > dTo)) {
            def msg = getColumnName(row, 'contractDate')
            logger.warn("Строка $rowNum: «$msg» не может быть вне налогового периода!")
        }

        // Корректность даты заключения сделки
        if (transactionDeliveryDate < contractDate) {
            def msg1 = getColumnName(row, 'transactionDeliveryDate')
            def msg2 = getColumnName(row, 'contractDate')
            logger.warn("Строка $rowNum: «$msg1» не может быть меньше «$msg2»!")
        }

        // Проверка доходов/расходов и стоимости
        if (incomeSum != null && consumptionSum == null && price != incomeSum) {
            def msg1 = getColumnName(row, 'price')
            def msg2 = getColumnName(row, 'incomeSum')
            logger.warn("Строка $rowNum: Графа «$msg1» должна быть равна «$msg2»!")
        }
        if (incomeSum == null && consumptionSum != null && price != consumptionSum) {
            def msg1 = getColumnName(row, 'price')
            def msg2 = getColumnName(row, 'consumptionSum')
            logger.warn("Строка $rowNum: Графа «$msg1» должна быть равна «$msg2»!")
        }
        if (incomeSum != null && consumptionSum != null &&
                (price == null
                        || consumptionSum == null
                        || incomeSum == null
                        || price != (consumptionSum - incomeSum).abs())) {
            def msg1 = getColumnName(row, 'price')
            def msg2 = getColumnName(row, 'consumptionSum')
            def msg3 = getColumnName(row, 'incomeSum')
            logger.warn("Строка $rowNum: Графа «$msg1» должна быть равна разнице графы «$msg2» и графы «$msg3» по модулю!")
        }

        // Проверка стоимости сделки
        if (cost != price) {
            def msg1 = getColumnName(row, 'cost')
            def msg2 = getColumnName(row, 'price')
            logger.warn("Строка $rowNum: «$msg1» не может отличаться от «$msg2» сделки!")
        }

        // Корректность дат сделки
        if (transactionDate < transactionDeliveryDate) {
            def msg1 = getColumnName(row, 'transactionDate')
            def msg2 = getColumnName(row, 'transactionDeliveryDate')
            logger.warn("Строка $rowNum: «$msg1» не может быть меньше «$msg2»!")
        }

        //Проверки соответствия НСИ
        checkNSI(row, "name", "Организации-участники контролируемых сделок", 9)
        checkNSI(row, "country", "ОКСМ", 10)
        checkNSI(row, "countryCode", "ОКСМ", 10)
    }

    //Проверки подитоговых сумм
    def testRows = dataRows.findAll { it -> it.getAlias() == null }
    //добавляем итоговые строки для проверки
    for (int i = 0; i < testRows.size(); i++) {
        def testRow = testRows.get(i)
        def nextRow = null

        if (i < testRows.size() - 1) {
            nextRow = testRows.get(i + 1)
        }

        if (testRow.getAlias() == null && nextRow == null || isDiffRow(testRow, nextRow, getGroupColumns())) {
            def itogRow = calcItog(i, testRows)
            testRows.add(++i, itogRow)
        }
    }

    def testItogRows = testRows.findAll { it -> it.getAlias() != null }
    def itogRows = dataRows.findAll { it -> it.getAlias() != null }

    if (testItogRows.size() > itogRows.size()) {            //если удалили итоговые строки

        for (int i = 0; i < dataRows.size(); i++) {
            def row = dataRows[i]
            def nextRow = dataRows[i + 1]
            if (row.getAlias() == null) {
                if (nextRow == null ||
                        nextRow.getAlias() == null && isDiffRow(row, nextRow, getGroupColumns())) {
                    def String groupCols = getValuesByGroupColumn(row)
                    if (groupCols != null) {
                        logger.error("Группа «$groupCols» не имеет строки подитога!")
                    }
                }
            }
        }

    } else if (testItogRows.size() < itogRows.size()) {
        //если удалили все обычные строки, значит где то 2 подряд подитог.строки

        for (int i = 0; i < dataRows.size(); i++) {
            if (dataRows[i].getAlias() != null) {
                if (i - 1 < -1 || dataRows[i - 1].getAlias() != null) {
                    logger.error("Строка ${dataRows[i].getIndex()}: Строка подитога не относится к какой-либо группе!")
                }
            }
        }
    } else {
        for (int i = 0; i < testItogRows.size(); i++) {
            def testItogRow = testItogRows[i]
            def realItogRow = itogRows[i]
            int itg = Integer.valueOf(testItogRow.getAlias().replaceAll("itg#", ""))
            if (dataRows[itg].getAlias() != null) {
                logger.error("Строка ${dataRows[i].getIndex()}: Строка подитога не относится к какой-либо группе!")
            } else {
                def String groupCols = getValuesByGroupColumn(dataRows[itg])
                def mes = "Строка ${realItogRow.getIndex()}: Неверное итоговое значение по группе «$groupCols» в графе"
                if (groupCols != null) {
                    if (testItogRow.price != realItogRow.price) {
                        logger.error(mes + " «${getAtributes().price[2]}»")
                    }
                    if (testItogRow.cost != realItogRow.cost) {
                        logger.error(mes + " «${getAtributes().cost[2]}»")
                    }
                }
            }
        }
    }
}

/**
 * Возвращает строку со значениями полей строки по которым идет группировка
 * ['name', 'innKio', 'contractNum', 'contractDate', 'transactionType']
 */
String getValuesByGroupColumn(DataRow row) {
    def sep = ", "
    StringBuilder builder = new StringBuilder()
    def map = row.name != null ? refBookService.getRecordData(9, row.name) : null
    if (map != null)
        builder.append(map.NAME.stringValue).append(sep)
    if (row.innKio != null)
        builder.append(row.innKio).append(sep)
    if (row.contractNum != null)
        builder.append(row.contractNum).append(sep)
    if (row.contractDate != null)
        builder.append(row.contractDate).append(sep)
    if (row.transactionType != null)
        builder.append(row.transactionType).append(sep)

    def String retVal = builder.toString()
    if (retVal.length() < 2)
        return null
    retVal.substring(0, retVal.length() - 2)
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
 * проверяет разные ли строки по значениям полей группировки
 * @param a первая  строка
 * @param b вторая строка
 * @return true - разные, false = одинаковые
 */
boolean isDiffRow(DataRow row, DataRow nextRow, def groupColumns) {
    def rez = false
    groupColumns.each { def n ->
        rez = rez || (row.get(n) != nextRow.get(n))
    }
    return rez
}

/**
 * Проставляет статические строки
 */
void addAllStatic() {
    if (!logger.containsLevel(LogLevel.ERROR)) {
        def dataRowHelper = formDataService.getDataRowHelper(formData)
        def dataRows = dataRowHelper.getAllCached()

        if (dataRows.size() < 1) {
            return
        }

        for (int i = 0; i < dataRows.size(); i++) {
            def row = dataRows.get(i)
            def nextRow = null

            if (i < dataRows.size() - 1) {
                nextRow = dataRows.get(i + 1)
            }

            if (row.getAlias() == null && nextRow == null || isDiffRow(row, nextRow, getGroupColumns())) {
                def itogRow = calcItog(i, dataRows)
                dataRowHelper.insert(itogRow, ++i + 1)
            }
        }
    }
}

/**
 * Расчет подитогового значения
 * @param i
 * @return
 */
def calcItog(int i, def dataRows) {
    def newRow = formData.createDataRow()

    newRow.getCell('itog').colSpan = 12
    newRow.itog = 'Подитог:'
    newRow.setAlias('itg#'.concat(i.toString()))
    newRow.getCell('fix').colSpan = 2

    // Расчеты подитоговых значений
    BigDecimal priceItg = 0, costItg = 0
    for (int j = i; j >= 0 && dataRows.get(j).getAlias() == null; j--) {
        row = dataRows.get(j)

        price = row.price
        cost = row.cost

        priceItg += price != null ? price : 0
        costItg += cost != null ? cost : 0
    }

    newRow.price = priceItg
    newRow.cost = costItg
    newRow
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
        // Графы 13 и 14 из 11 и 12
        incomeSum = row.incomeSum
        consumptionSum = row.consumptionSum

        if (incomeSum != null && consumptionSum == null) {
            row.price = incomeSum
            row.cost = row.price
        } else if (incomeSum == null && consumptionSum != null) {
            row.price = consumptionSum
            row.cost = row.price
        } else if (incomeSum != null && consumptionSum != null) {
            row.price = (consumptionSum - incomeSum).abs()
            row.cost = row.price
        }

        // Расчет полей зависимых от справочников
        if (row.name != null) {
            def map = refBookService.getRecordData(9, row.name)
            row.innKio = map.INN_KIO.stringValue
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
 * Сортировка строк по гр. 2, гр. 3, гр. 6, гр. 7, гр. 10
 */
void sort() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    dataRows.sort({ DataRow a, DataRow b ->
        sortRow(getGroupColumns(), a, b)
    })

    dataRowHelper.save(dataRows);
}

int sortRow(List<String> params, DataRow a, DataRow b) {
    for (String param : params) {
        aD = a.getCell(param).value
        bD = b.getCell(param).value

        if (aD != bD) {
            return aD<=>bD
        }
    }
    return 0
}

/**
 * Удаление всех статическиех строк "Подитог" из списка строк
 */
void deleteAllStatic() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    for (Iterator<DataRow> iter = dataRows.iterator() as Iterator<DataRow>; iter.hasNext();) {
        row = (DataRow) iter.next()
        if (row.getAlias() != null) {
            iter.remove()
            dataRowHelper.delete(row)
        }
    }
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

    def xmlString = importService.getData(is, fileName, 'windows-1251', 'Полное наименование с указанием ОПФ', 'Подитог:')
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
    def colCount = 13
    // проверить количество строк и колонок в шапке
    if (xml.row.size() < headRowCount || xml.row[0].cell.size() < colCount) {
        return false
    }

    def names = (
            xml.row[0].cell[0] == 'Полное наименование с указанием ОПФ' &&
                    xml.row[0].cell[1] == 'ИНН/ КИО' &&
                    xml.row[0].cell[2] == 'Наименование страны регистрации' &&
                    xml.row[0].cell[3] == 'Код страны регистрации по классификатору ОКСМ' &&
                    xml.row[0].cell[4] == 'Номер договора' &&
                    xml.row[0].cell[5] == 'Дата договора' &&
                    xml.row[0].cell[6] == 'Номер сделки' &&
                    xml.row[0].cell[7] == 'Дата заключения сделки' &&
                    xml.row[0].cell[8] == 'Вид срочной сделки' &&
                    xml.row[0].cell[9] == 'Сумма доходов Банка по данным бухгалтерского учета, руб.' &&
                    xml.row[0].cell[10] == 'Сумма расходов Банка по данным бухгалтерского учета, руб.' &&
                    xml.row[0].cell[11] == 'Цена (тариф) за единицу измерения, руб.' &&
                    xml.row[0].cell[12] == 'Итого стоимость, руб.' &&
                    xml.row[0].cell[13] == 'Дата совершения сделки'
    )
    if (!names) return false

    // пустая строка
    xml.row[1].cell.each { it ->
        if (it.text() != '') {
            if (it.text() == 'Вариант 2') {     //TODO (alivanov 9.09.13) выпилить проверку
            } else {
                return false
            }
        }
    }

    // строка с нумерацией граф
    def grafRow = (
            xml.row[2].cell[0] == 'гр. 2' &&
                    xml.row[2].cell[1] == 'гр. 3' &&
                    xml.row[2].cell[2] == 'гр. 4.1' &&
                    xml.row[2].cell[3] == 'гр. 4.2' &&
                    xml.row[2].cell[4] == 'гр. 5' &&
                    xml.row[2].cell[5] == 'гр. 6' &&
                    xml.row[2].cell[6] == 'гр. 7' &&
                    xml.row[2].cell[7] == 'гр. 8' &&
                    xml.row[2].cell[8] == 'гр. 9' &&
                    xml.row[2].cell[9] == 'гр. 10' &&
                    xml.row[2].cell[10] == 'гр. 11' &&
                    xml.row[2].cell[11] == 'гр. 12' &&
                    xml.row[2].cell[12] == 'гр. 13' &&
                    xml.row[2].cell[13] == 'гр. 14'
    )

    return grafRow
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
        newRow.name = getRecordId(9, 'NAME', row.cell[indexCell].text(), date, cache, indexRow, indexCell, false)
        def map = newRow.name == null ? null : refBookService.getRecordData(9, newRow.name)
        indexCell++

        // графа 3
        if (map != null) {
            def text = row.cell[indexCell].text()
            if ((text != null && !text.isEmpty() && !text.equals(map.INN_KIO.stringValue)) || ((text == null || text.isEmpty()) && map.INN_KIO.stringValue != null)) {
                logger.warn("Проверка файла: Строка ${indexRow + 2} столбец ${indexCell + 2} содержит значение, отсутствующее в справочнике «" + refBookFactory.get(9).getName()+"»!")
            }
        }
        indexCell++

        // графа 4.1
        if (map != null) {
            def text = row.cell[indexCell].text()
            map = refBookService.getRecordData(10, map.COUNTRY.referenceValue)
            if ((text != null && !text.isEmpty() && !text.equals(map.NAME.stringValue)) || ((text == null || text.isEmpty()) && map.NAME.stringValue != null)) {
                logger.warn("Проверка файла: Строка ${indexRow + 2} столбец ${indexCell + 2} содержит значение, отсутствующее в справочнике «" + refBookFactory.get(10).getName()+"»!")
            }
        }
        indexCell++

        // графа 4.2
        if (map != null) {
            def text = row.cell[indexCell].text()
            if ((text != null && !text.isEmpty() && !text.equals(map.CODE.stringValue)) || ((text == null || text.isEmpty()) && map.CODE.stringValue != null)) {
                logger.warn("Проверка файла: Строка ${indexRow + 2} столбец ${indexCell + 2} содержит значение, отсутствующее в справочнике «" + refBookFactory.get(10).getName()+"»!")
            }
        }
        indexCell++

        // графа 5
        newRow.contractNum = row.cell[indexCell].text()
        indexCell++

        // графа 6
        newRow.contractDate = getDate(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 7
        newRow.transactionNum = row.cell[indexCell].text()
        indexCell++

        // графа 8
        newRow.transactionDeliveryDate = getDate(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 9
        newRow.transactionType = row.cell[indexCell].text()
        indexCell++

        // графа 10
        newRow.incomeSum = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 11
        newRow.consumptionSum = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 12
        newRow.price = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 13
        newRow.cost = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 14
        newRow.transactionDate = getDate(row.cell[indexCell].text(), indexRow, indexCell)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}

def getNumber(def value, int indexRow, int indexCell) {
    try {
        return getNumber(value)
    } catch (Exception e) {
        throw new Exception("Проверка файла: Строка ${indexRow + 2} столбец ${indexCell + 2} содержит недопустимый тип данных!")
    }
}

def getRecordId(def ref_id, String code, String value, Date date, def cache, int indexRow, int indexCell, boolean mandatory = true) {
    def rez = getRecordId(ref_id, code, value, date, cache)
    if (rez == null) {
        def msg = "Проверка файла: Строка ${indexRow + 2} столбец ${indexCell + 2} содержит значение, отсутствующее в справочнике «" + refBookFactory.get(ref_id).getName()+"»!"
        if (mandatory) {
            throw new Exception(msg)
        } else {
            logger.warn(msg)
        }
    }
    return rez
}

def getDate(def value, int indexRow, int indexCell) {
    try {
        return getDate(value)
    } catch (Exception e) {
        throw new Exception("Проверка файла: Строка ${indexRow + 2} столбец ${indexCell + 2} содержит недопустимый тип данных!")
    }
}

/**
 * Получить числовое значение.
 *
 * @param value строка
 */
def getNumber(def value) {
    if (value == null) {
        return null
    }
    def tmp = value.trim()
    if ("".equals(tmp)) {
        return null
    }
    // поменять запятую на точку и убрать пробелы
    tmp = tmp.replaceAll(',', '.').replaceAll('[^\\d.,-]+', '')
    return new BigDecimal(tmp)
}

/**
 * Получить record_id элемента справочника.
 *
 * @param value
 */
def getRecordId(def ref_id, String alias, String value, Date date, def cache) {
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
    return null
}

/**
 * Получить дату по строковому представлению (формата дд.ММ.гггг)
 */
def getDate(def value) {
    if (value == null || value == '') {
        return null
    }
    return new SimpleDateFormat('dd.MM.yyyy').parse(value)
}