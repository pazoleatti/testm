package form_template.deal.nondeliverable

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

/**
 * 392 - Беспоставочные срочные сделки (17)
 *
 * @author Dmitriy Levykin
 */
switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW:
        formDataService.getDataRowHelper(formData).delete(currentDataRow)
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
    case FormDataEvent.COMPOSE: // Консолидация
        formDataService.consolidationSimple(formData, formDataDepartment.id, logger)
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
        calc()
        logicCheck()
        break
}

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

// Редактируемые атрибуты
@Field
def editableColumns = ['name', 'contractNum', 'contractDate', 'transactionNum', 'transactionDeliveryDate',
        'transactionType', 'incomeSum', 'consumptionSum', 'transactionDate']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNum', 'innKio', 'country', 'countryCode', 'price', 'cost']

// Группируемые атрибуты
@Field
def groupColumns = ['name', 'innKio', 'contractNum', 'contractDate', 'transactionType']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['rowNum', 'name', 'innKio', 'country', 'countryCode', 'contractNum', 'contractDate',
        'transactionNum', 'transactionDeliveryDate', 'transactionType', 'price', 'cost', 'transactionDate']

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

// Текущая дата
@Field
def currentDate = new Date()

//// Обертки методов

// Проверка НСИ
boolean checkNSI(def refBookId, def row, def alias) {
    return formDataService.checkNSI(refBookId, refBookCache, row, alias, logger, false)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            reportPeriodEndDate, rowIndex, colIndex, logger, required)
}

// Поиск записи в справочнике по значению (для расчетов)
def getRecordId(def Long refBookId, def String alias, def String value, def int rowIndex, def String cellName,
                boolean required = true) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
            currentDate, rowIndex, cellName, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Получение xml с общими проверками
def getXML(def String startStr, def String endStr) {
    def fileName = (UploadFileName ? UploadFileName.toLowerCase() : null)
    if (fileName == null || fileName == '') {
        throw new ServiceException('Имя файла не должно быть пустым')
    }
    def is = ImportInputStream
    if (is == null) {
        throw new ServiceException('Поток данных пуст')
    }
    if (!fileName.endsWith('.xls')) {
        throw new ServiceException('Выбранный файл не соответствует формату xls!')
    }
    def xmlString = importService.getData(is, fileName, 'windows-1251', startStr, endStr)
    if (xmlString == null) {
        throw new ServiceException('Отсутствие значении после обработки потока данных')
    }
    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null) {
        throw new ServiceException('Отсутствие значении после обработки потока данных')
    }
    return xml
}

//// Кастомные методы

// Логические проверки
void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    if (dataRows.isEmpty()) {
        return
    }

    def dFrom = reportPeriodService.getStartDate(formData.reportPeriodId).time
    def dTo = reportPeriodService.getEndDate(formData.reportPeriodId).time

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = row.getIndex()

        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, false)

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
            def msg1 = row.getCell('consumptionSum').column.name
            def msg2 = row.getCell('incomeSum').column.name
            logger.warn("Строка $rowNum: Одна из граф «$msg1» и «$msg2» должна быть заполнена!")
        }

        // Корректность даты договора
        def dt = row.contractDate
        if (dt != null && (dt < dFrom || dt > dTo)) {
            def msg = row.getCell('contractDate').column.name
            logger.warn("Строка $rowNum: «$msg» не может быть вне налогового периода!")
        }

        // Корректность даты заключения сделки
        if (transactionDeliveryDate < contractDate) {
            def msg1 = row.getCell('transactionDeliveryDate').column.name
            def msg2 = row.getCell('contractDate').column.name
            logger.warn("Строка $rowNum: «$msg1» не может быть меньше «$msg2»!")
        }

        // Проверка доходов/расходов и стоимости
        if (incomeSum != null && consumptionSum == null && price != incomeSum) {
            def msg1 = row.getCell('price').column.name
            def msg2 = row.getCell('incomeSum').column.name
            logger.warn("Строка $rowNum: Графа «$msg1» должна быть равна «$msg2»!")
        }
        if (incomeSum == null && consumptionSum != null && price != consumptionSum) {
            def msg1 = row.getCell('price').column.name
            def msg2 = row.getCell('consumptionSum').column.name
            logger.warn("Строка $rowNum: Графа «$msg1» должна быть равна «$msg2»!")
        }
        if (incomeSum != null && consumptionSum != null &&
                (price == null
                        || consumptionSum == null
                        || incomeSum == null
                        || price != (consumptionSum - incomeSum).abs())) {
            def msg1 = row.getCell('price').column.name
            def msg2 = row.getCell('consumptionSum').column.name
            def msg3 = row.getCell('incomeSum').column.name
            logger.warn("Строка $rowNum: Графа «$msg1» должна быть равна разнице графы «$msg2» и графы «$msg3» по модулю!")
        }

        // Проверка стоимости сделки
        if (cost != price) {
            def msg1 = row.getCell('cost').column.name
            def msg2 = row.getCell('price').column.name
            logger.warn("Строка $rowNum: «$msg1» не может отличаться от «$msg2» сделки!")
        }

        // Корректность дат сделки
        if (transactionDate < transactionDeliveryDate) {
            def msg1 = row.getCell('transactionDate').column.name
            def msg2 = row.getCell('transactionDeliveryDate').column.name
            logger.warn("Строка $rowNum: «$msg1» не может быть меньше «$msg2»!")
        }

        // Проверки соответствия НСИ
        checkNSI(9, row, "name")
        checkNSI(10, row, "country")
        checkNSI(10, row, "countryCode")
    }

    checkItog(dataRows)
}

// Проверки подитоговых сумм
void checkItog(def dataRows) {
    def testRows = dataRows.findAll { it -> it.getAlias() == null }
    addAllAliased(testRows, new CalcAliasRow() {
        @Override
        DataRow<Cell> calc(int i, List<DataRow<Cell>> rows) {
            return calcItog(i, testRows)
        }
    }, groupColumns)
    // Рассчитанные строки итогов
    def testItogRows = testRows.findAll { it -> it.getAlias() != null }
    // Имеющиеся строки итогов
    def itogRows = dataRows.findAll { it -> it.getAlias() != null }

    checkItogRows(dataRows, testItogRows, itogRows, groupColumns, logger, new GroupString() {
        @Override
        String getString(DataRow<Cell> row) {
            return getValuesByGroupColumn(row)
        }
    }, new CheckGroupSum() {
        @Override
        String check(DataRow<Cell> row1, DataRow<Cell> row2) {
            if (row1.price != row2.price) {
                return getColumnName(row1, 'price')
            }
            if (row1.cost != row2.cost) {
                return getColumnName(row1, 'cost')
            }
            return null
        }
    })
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    if (dataRows.isEmpty()) {
        return
    }

    // Удаление подитогов
    deleteAllAliased(dataRows)

    // Сортировка
    sortRows(dataRows, groupColumns)

    def index = 1
    for (row in dataRows) {
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
        def map = getRefBookValue(9, row.name)
        row.innKio = map?.INN_KIO?.stringValue
        row.country = map?.COUNTRY?.referenceValue
        row.countryCode = map?.COUNTRY?.referenceValue
    }

    // Добавление подитов
    addAllAliased(dataRows, new CalcAliasRow() {
        @Override
        DataRow<Cell> calc(int i, List<DataRow<Cell>> rows) {
            return calcItog(i, dataRows)
        }
    }, groupColumns)

    // Если нет сортировки и подитогов, то dataRowHelper.update(dataRows)
    dataRowHelper.save(dataRows)
}

// Расчет подитогового значения
DataRow<Cell> calcItog(def int i, def List<DataRow<Cell>> dataRows) {
    def newRow = formData.createDataRow()

    newRow.getCell('itog').colSpan = 12
    newRow.itog = 'Подитог:'
    newRow.setAlias('itg#'.concat(i.toString()))
    newRow.getCell('fix').colSpan = 2

    // Расчеты подитоговых значений
    def BigDecimal priceItg = 0, costItg = 0
    for (int j = i; j >= 0 && dataRows.get(j).getAlias() == null; j--) {
        def row = dataRows.get(j)
        def price = row.price
        def cost = row.cost

        priceItg += price != null ? price : 0
        costItg += cost != null ? cost : 0
    }
    newRow.price = priceItg
    newRow.cost = costItg

    return newRow
}

// Возвращает строку со значениями полей строки по которым идет группировка
String getValuesByGroupColumn(DataRow row) {
    def sep = ", "
    StringBuilder builder = new StringBuilder()
    def map = getRefBookValue(9, row.name)
    if (map != null)
        builder.append(map.NAME?.stringValue).append(sep)
    if (row.contractNum != null)
        builder.append(row.contractNum).append(sep)
    if (row.contractDate != null)
        builder.append(row.contractDate).append(sep)
    if (row.transactionType != null)
        builder.append(row.transactionType).append(sep)

    def String retVal = builder.toString()
    if (retVal.length() < 2)
        return null
    return retVal.substring(0, retVal.length() - 2)
}

// Получение импортируемых данных.
void importData() {
    def xml = getXML('Полное наименование с указанием ОПФ', 'Подитог:')

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 13, 3)

    def headerMapping = [
            (xml.row[0].cell[1]): 'ИНН/ КИО',
            (xml.row[0].cell[2]): 'Наименование страны регистрации',
            (xml.row[0].cell[3]): 'Код страны регистрации по классификатору ОКСМ',
            (xml.row[0].cell[4]): 'Номер договора',
            (xml.row[0].cell[5]): 'Дата договора',
            (xml.row[0].cell[6]): 'Номер сделки',
            (xml.row[0].cell[7]): 'Дата заключения сделки',
            (xml.row[0].cell[8]): 'Вид срочной сделки',
            (xml.row[0].cell[9]): 'Сумма доходов Банка по данным бухгалтерского учета, руб.',
            (xml.row[0].cell[10]): 'Сумма расходов Банка по данным бухгалтерского учета, руб.',
            (xml.row[0].cell[11]): 'Цена (тариф) за единицу измерения, руб.',
            (xml.row[0].cell[12]): 'Итого стоимость, руб.',
            (xml.row[0].cell[13]): 'Дата совершения сделки',
            (xml.row[2].cell[0]): 'гр. 2',
            (xml.row[2].cell[1]): 'гр. 3',
            (xml.row[2].cell[2]): 'гр. 4.1',
            (xml.row[2].cell[3]): 'гр. 4.2',
            (xml.row[2].cell[4]): 'гр. 5',
            (xml.row[2].cell[5]): 'гр. 6',
            (xml.row[2].cell[6]): 'гр. 7',
            (xml.row[2].cell[7]): 'гр. 8',
            (xml.row[2].cell[8]): 'гр. 9',
            (xml.row[2].cell[9]): 'гр. 10',
            (xml.row[2].cell[10]): 'гр. 11',
            (xml.row[2].cell[11]): 'гр. 12',
            (xml.row[2].cell[12]): 'гр. 13',
            (xml.row[2].cell[13]): 'гр. 14'
    ]

    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def int xmlIndexRow = -1
    def int xlsIndexRow = 0
    def int rowOffset = 3
    def int colOffset = 2

    def rows = new LinkedList<DataRow<Cell>>()

    for (def row : xml.row) {
        xmlIndexRow++
        xlsIndexRow = xmlIndexRow + rowOffset

        // пропустить шапку таблицы
        if (xmlIndexRow <= headRowCount) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        def newRow = formData.createDataRow()
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }
        autoFillColumns.each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }

        def int xmlIndexCol = 0

        // графа 1
        newRow.rowNum = xmlIndexRow - 2

        // графа 2
        newRow.name = getRecordIdImport(9, 'NAME', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        def map = getRefBookValue(9, newRow.name)
        xmlIndexCol++

        // графа 3
        if (map != null) {
            def text = row.cell[xmlIndexCol].text()
            if ((text != null && !text.isEmpty() && !text.equals(map.INN_KIO?.stringValue)) || ((text == null || text.isEmpty()) && map.INN_KIO?.stringValue != null)) {
                logger.warn("Строка ${xlsIndexRow} столбец ${xmlIndexCol + colOffset} содержит значение, отсутствующее в справочнике!")
            }
        }
        xmlIndexCol++

        // графа 4.1
        if (map != null) {
            def text = row.cell[xmlIndexCol].text()
            map = getRefBookValue(10, map.COUNTRY?.referenceValue)
            if (map != null) {
                if ((text != null && !text.isEmpty() && !text.equals(map.NAME?.stringValue)) || ((text == null || text.isEmpty()) && map.NAME?.stringValue != null)) {
                    logger.warn("Строка ${xlsIndexRow} столбец ${xmlIndexCol + colOffset} содержит значение, отсутствующее в справочнике!")
                }
            }
        }
        xmlIndexCol++

        // графа 4.2
        if (map != null) {
            def text = row.cell[xmlIndexCol].text()
            if ((text != null && !text.isEmpty() && !text.equals(map.CODE?.stringValue)) || ((text == null || text.isEmpty()) && map.CODE?.stringValue != null)) {
                logger.warn("Строка ${xlsIndexRow} столбец ${xmlIndexCol + colOffset} содержит значение, отсутствующее в справочнике!")
            }
        }
        xmlIndexCol++

        // графа 5
        newRow.contractNum = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 6
        newRow.contractDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 7
        newRow.transactionNum = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 8
        newRow.transactionDeliveryDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 9
        newRow.transactionType = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 10
        newRow.incomeSum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 11
        newRow.consumptionSum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 12
        newRow.price = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 13
        newRow.cost = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 14
        newRow.transactionDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}