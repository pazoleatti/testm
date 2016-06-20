package form_template.deal.nondeliverable.v2013

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

/**
 * 2392 - Беспоставочные срочные сделки (17)
 *
 * formTemplateId=2392
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
        formDataService.saveCachedDataRows(formData, logger)
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
        formDataService.consolidationSimple(formData, logger, userInfo)
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT:
        importData()
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.SORT_ROWS:
        sortFormDataRows()
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
def nonEmptyColumns = ['name', 'contractNum', 'contractDate', 'price', 'cost', 'transactionDate']

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

//// Обертки методов

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            reportPeriodEndDate, rowIndex, colIndex, logger, required)
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
    if (!fileName.endsWith('.xls') && !fileName.endsWith('.xlsx') && !fileName.endsWith('.xlsm')) {
        throw new ServiceException('Выбранный файл не соответствует формату xls/xlsx/xlsm!')
    }
    def xmlString = importService.getData(is, fileName, 'windows-1251', startStr, endStr)
    if (xmlString == null) {
        throw new ServiceException('Отсутствие значения после обработки потока данных')
    }
    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null) {
        throw new ServiceException('Отсутствие значения после обработки потока данных')
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

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = row.getIndex()

        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)

        def incomeSumCell = row.getCell('incomeSum')
        def outcomeSumCell = row.getCell('consumptionSum')
        def price = row.price
        def transactionDeliveryDate = row.transactionDeliveryDate
        def contractDate = row.contractDate
        def cost = row.cost
        def transactionDate = row.transactionDate
        def msgIn = incomeSumCell.column.name
        def msgOut = outcomeSumCell.column.name

        // Проверка доходов и расходов
        if (outcomeSumCell.value == null && incomeSumCell.value  == null) {
            rowError(logger, row, "Строка $rowNum: Графа «$msgIn» должна быть заполнена, если не заполнена графа «$msgOut»!")
        }

        // Корректность даты заключения сделки
        if (transactionDeliveryDate < contractDate) {
            def msg1 = row.getCell('transactionDeliveryDate').column.name
            def msg2 = row.getCell('contractDate').column.name
            rowError(logger, row, "Строка $rowNum: Значение графы «$msg1» должно быть не меньше значения графы «$msg2»!")
        }

        // Проверка доходов/расходов и стоимости
        def msgPrice = row.getCell('price').column.name
        if (incomeSumCell.value != null && outcomeSumCell.value != null) {
            if ((row.price ?: 0).abs() != (incomeSumCell.value - outcomeSumCell.value).abs())
                rowError(logger, row, "Строка $rowNum: Значение графы «$msgPrice» должно быть равно разнице значений граф «$msgIn» и «$msgOut» по модулю!")
        } else if (incomeSumCell.value != null) {
            if (row.price != incomeSumCell.value)
                rowError(logger, row, "Строка $rowNum: Значение графы «$msgPrice» должно быть равно значению графы «$msgIn»!")
        } else if (outcomeSumCell.value != null) {
            if (row.price != outcomeSumCell.value)
                rowError(logger, row, "Строка $rowNum: Значение графы «$msgPrice» должно быть равно значению графы «$msgOut»!")
        }

        // Проверка стоимости сделки
        if (cost != price) {
            def msg1 = row.getCell('cost').column.name
            def msg2 = row.getCell('price').column.name
            rowError(logger, row, "Строка $rowNum: Значение графы «$msg1» должно быть равно значению графы «$msg2»!")
        }

        // Корректность дат сделки
        if (transactionDate < transactionDeliveryDate) {
            def msg1 = row.getCell('transactionDate').column.name
            def msg2 = row.getCell('transactionDeliveryDate').column.name
            rowError(logger, row, "Строка $rowNum: «$msg1» должно быть не меньше значения графы «$msg2»!")
        }
    }

    if (formData.kind == FormDataKind.CONSOLIDATED) {
        checkItog(dataRows)
    }
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

    // Сортировка для группировки
    sortRows(dataRows, groupColumns)

    for (row in dataRows) {
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
    }

    // Добавление подитов
    if (formData.kind == FormDataKind.CONSOLIDATED) {
        addAllAliased(dataRows, new CalcAliasRow() {
            @Override
            DataRow<Cell> calc(int i, List<DataRow<Cell>> rows) {
                return calcItog(i, dataRows)
            }
        }, groupColumns)
    }

    dataRowHelper.allCached = dataRows
    sortFormDataRows(false)
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
    def tmpRow = formData.createDataRow()
    def xml = getXML('Общие сведения о контрагенте - юридическом лице', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 15, 3)

    def headerMapping = [
            (xml.row[1].cell[2]): tmpRow.getCell('name').column.name,
            (xml.row[1].cell[3]): tmpRow.getCell('innKio').column.name,
            (xml.row[1].cell[4]): tmpRow.getCell('country').column.name,
            (xml.row[1].cell[5]): tmpRow.getCell('countryCode').column.name,
            (xml.row[1].cell[6]): tmpRow.getCell('contractNum').column.name,
            (xml.row[1].cell[7]): tmpRow.getCell('contractDate').column.name,
            (xml.row[1].cell[8]): tmpRow.getCell('transactionNum').column.name,
            (xml.row[1].cell[9]): tmpRow.getCell('transactionDeliveryDate').column.name,
            (xml.row[1].cell[10]): tmpRow.getCell('transactionType').column.name,
            (xml.row[1].cell[11]): tmpRow.getCell('incomeSum').column.name,
            (xml.row[1].cell[12]): tmpRow.getCell('consumptionSum').column.name,
            (xml.row[1].cell[13]): tmpRow.getCell('price').column.name,
            (xml.row[1].cell[14]): tmpRow.getCell('cost').column.name,
            (xml.row[1].cell[16]): tmpRow.getCell('transactionDate').column.name,
            (xml.row[2].cell[2]): 'гр. 2',
            (xml.row[2].cell[3]): 'гр. 3',
            (xml.row[2].cell[4]): 'гр. 4.1',
            (xml.row[2].cell[5]): 'гр. 4.2',
            (xml.row[2].cell[16]): 'гр. 14'
    ]
    (6..14).each {
        headerMapping.put(xml.row[2].cell[it], 'гр. ' + (it - 1))
    }
    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def int xmlIndexRow = -1
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    def rows = []
    def int rowIndex = 1

    for (def row : xml.row) {
        xmlIndexRow++
        def int xlsIndexRow = xmlIndexRow + rowOffset

        // пропустить шапку таблицы
        if (xmlIndexRow <= headRowCount) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        // Пропуск итоговых строк
        if (row.cell[1].text() != null && row.cell[1].text() != "") {
            continue
        }

        def newRow = formData.createDataRow()
        newRow.setIndex(rowIndex++)
        newRow.setImportIndex(xlsIndexRow)
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }
        autoFillColumns.each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }

        def int xmlIndexCol = 0

        // графа 1
        xmlIndexCol++

        // графа fix
        xmlIndexCol++

        // графа 2
        newRow.name = getRecordIdImport(9, 'NAME', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, true)
        def map = getRefBookValue(9, newRow.name)
        xmlIndexCol++

        // графа 3
        if (map != null) {
            formDataService.checkReferenceValue(9, row.cell[xmlIndexCol].text(), map.INN_KIO?.stringValue, xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        }
        xmlIndexCol++

        // графа 4.1
        if (map != null) {
            map = getRefBookValue(10, map.COUNTRY?.referenceValue)
            if (map != null) {
                formDataService.checkReferenceValue(10, row.cell[xmlIndexCol].text(), map.NAME?.stringValue, xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            }
        }
        xmlIndexCol++

        // графа 4.2
        if (map != null) {
            formDataService.checkReferenceValue(10, row.cell[xmlIndexCol].text(), map.CODE?.stringValue, xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        }
        xmlIndexCol++

        // графа 5
        newRow.contractNum = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 6
        newRow.contractDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 7
        newRow.transactionNum = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 8
        newRow.transactionDeliveryDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 9
        newRow.transactionType = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 10
        newRow.incomeSum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 11
        newRow.consumptionSum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 12
        newRow.price = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 13
        newRow.cost = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа fix
        xmlIndexCol++

        // графа 14
        newRow.transactionDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)

        rows.add(newRow)
    }

    dataRowHelper.allCached = rows
}

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, getSubTotalRows(dataRows), null, true)
    if(saveInDB){
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows);
    }
}

// Получение подитоговых строк
def getSubTotalRows(def dataRows) {
    return dataRows.findAll { it.getAlias() != null}
}