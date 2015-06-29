package form_template.property.property_945_2.v2014

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * Данные о кадастровой стоимости объектов недвижимости для расчета налога на имущество
 * formTemplateId=611
 *
 * @author Bulat Kinzyabulatov
 */

// графа 1  - rowNum                    № пп
// графа -  - fix
// графа 2  - subject                   Код субъекта
// графа 3  - taxAuthority              Код НО
// графа 4  - kpp                       КПП
// графа 5  - oktmo                     Код ОКТМО
// графа 6  - address                   Адрес объекта
// графа 7  - sign                      Признак: здание - 1, помещение - 2
// графа 8  - cadastreNumBuilding       Кадастровый номер, здание
// графа 9  - cadastreNumRoom           Кадастровый номер, помещение
// графа 10 - cadastrePriceJanuary      Кадастровая стоимость, на 1 января
// графа 11 - cadastrePriceTaxFree      Кадастровая стоимость, в т.ч. необлагаемая налогом
// графа 12 - propertyRightBeginDate    Право собственности, дата возникновения
// графа 13 - propertyRightEndDate      Право собственности, дата прекращения
// графа 14 - taxBenefitCode            Код налоговой льготы
// графа 15 - benefitBasis              Основание льготы

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.AFTER_CREATE:
        addPrevDataRows()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        checkRegionId()
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.CHECK:
        checkRegionId()
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow != null && currentDataRow.getAlias() == null) {
            formDataService.getDataRowHelper(formData).delete(currentDataRow)
        }
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        checkRegionId()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        checkRegionId()
        importData()
        if (!logger.containsLevel(LogLevel.ERROR)) {
            calc()
            logicCheck()
            formDataService.saveCachedDataRows(formData, logger)
        }
        break
    case FormDataEvent.SORT_ROWS:
        sortFormDataRows()
        break
}

@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

// все атрибуты
@Field
def allColumns = ['rowNum', 'fix', 'subject', 'taxAuthority', 'kpp', 'oktmo', 'address', 'sign', 'cadastreNumBuilding',
        'cadastreNumRoom', 'cadastrePriceJanuary', 'cadastrePriceTaxFree', 'propertyRightBeginDate',
        'propertyRightEndDate', 'taxBenefitCode', 'benefitBasis']

// Редактируемые атрибуты
@Field
def editableColumns = ['subject', 'taxAuthority', 'kpp', 'oktmo', 'address', 'sign', 'cadastreNumBuilding',
                       'cadastreNumRoom', 'cadastrePriceJanuary', 'cadastrePriceTaxFree', 'propertyRightBeginDate',
                       'propertyRightEndDate', 'taxBenefitCode']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNum']

@Field
def nonEmptyColumns = ['subject', 'taxAuthority', 'kpp', 'oktmo', 'address', 'sign', 'cadastreNumBuilding',
                       'cadastrePriceJanuary', 'cadastrePriceTaxFree', 'propertyRightBeginDate']

@Field
def sortColumns = ['subject', 'taxAuthority', 'kpp', 'oktmo', 'sign', 'cadastreNumBuilding', 'cadastreNumRoom']

@Field
def groupColumns = ['taxAuthority', 'kpp']

@Field
def totalColumns = ['cadastrePriceJanuary', 'cadastrePriceTaxFree']

@Field
def startDate = null

@Field
def endDate = null

@Field
def isBalancePeriod

// Признак периода ввода остатков для отчетного периода подразделения
def isBalancePeriod() {
    if (isBalancePeriod == null) {
        def departmentReportPeriod = departmentReportPeriodService.get(formData.departmentReportPeriodId)
        isBalancePeriod = departmentReportPeriod.isBalance()
    }
    return isBalancePeriod
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = true) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache);
}

def getBenefitCode(def parentRecordId) {
    def recordId = getRefBookValue(203, parentRecordId).TAX_BENEFIT_ID.value
    return  getRefBookValue(202, recordId).CODE.value
}

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    }
    return startDate
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

// Скопировать строки за предыдущий отчетный период.
void addPrevDataRows() {
    if (isBalancePeriod()) {
        return
    }
    def prevFormData = formDataService.getFormDataPrev(formData)
    def prevDataRows = (prevFormData != null ? formDataService.getDataRowHelper(prevFormData)?.allSaved : null)

    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    if (reportPeriod.order == 1){
        def Date startDate = getReportPeriodStartDate()
        def Date endDate = getReportPeriodEndDate()
        prevDataRows = prevDataRows.findAll { row ->
            if (row.getAlias() != null) {
                return false
            }
            def rightBeginDate = row.propertyRightBeginDate
            def rightEndDate = row.propertyRightEndDate
            if (rightEndDate && rightEndDate < startDate || rightBeginDate > endDate) {
                return false
            } else {
                if (rightBeginDate < startDate) {
                    rightBeginDate = startDate
                }
                if (!rightEndDate || rightEndDate > endDate) {
                    rightEndDate = endDate
                }
                return !(rightBeginDate > endDate || rightEndDate < startDate)
            }
        }
    }
    if (prevDataRows) {
        def dataRows = formDataService.getDataRowHelper(formData).allCached
        def totalRow = getDataRow(dataRows, 'total')
        deleteAllAliased(prevDataRows)
        addFixedRows(prevDataRows, totalRow)
    }
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def totalRow = getDataRow(dataRows, 'total')

    deleteAllAliased(dataRows)

    if (formDataEvent != FormDataEvent.IMPORT) {
        sort(dataRows)
    }
    for (def row : dataRows) {
        def String basis = calcBasis(row.taxBenefitCode)
        row.benefitBasis = basis
    }

    addFixedRows(dataRows, totalRow)

    sortFormDataRows(false)
}

def calcBasis(def recordId) {
    if (recordId == null) {
        return null
    }
    def record = getRefBookValue(203, recordId)
    if (record == null) {
        return null
    }
    def section = record.SECTION.value ?: ''
    def item = record.ITEM.value ?: ''
    def subItem = record.SUBITEM.value ?: ''
    return String.format("%s%s%s", section.padLeft(4, '0'), item.padLeft(4, '0'), subItem.padLeft(4, '0'))
}

void addFixedRows(def dataRows, totalRow) {
    addAllAliased(dataRows, new CalcAliasRow() {
        @Override
        DataRow<Cell> calc(int i, List<DataRow<Cell>> rows) {
            return calcItog(i, dataRows)
        }
    }, groupColumns)

    calcTotalSum(dataRows, totalRow, totalColumns)
    dataRows.add(totalRow)
}

// Расчет подитогового значения
DataRow<Cell> calcItog(def int i, def List<DataRow<Cell>> dataRows) {
    def newRow = formData.createDataRow()

    newRow.getCell('fix').colSpan = 5
    def row = dataRows.get(i)
    newRow.fix = 'Итого по НО ' + row.taxAuthority + ' и КПП ' + row.kpp
    newRow.setAlias('total#'.concat(i.toString()))
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }

    totalColumns.each {
        newRow[it] = 0
    }
    for (int j = i; j >= 0 && dataRows.get(j).getAlias() == null; j--) {
        row = dataRows.get(j)
        totalColumns.each {
            newRow[it] += (row[it] ?: 0)
        }
    }
    return newRow
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // строки для сравнения
    def rowsToCompare = dataRows.clone()
    rowsToCompare.removeAll { e ->
        e.getAlias() != null
    }
    // мапа индексов строк "идентичных" текущей
    def Map<Integer, List<Integer>> foundEqualsMap = [:]
    for (def row : dataRows) {
        def index = row.getIndex()
        def errorMsg = "Строка $index: "
        if (row.getAlias() == null) {

            // Проверка на заполнение поля
            def Integer sign = row.sign
            def columns = (sign == 2)? (nonEmptyColumns + 'cadastreNumRoom') : nonEmptyColumns
            checkNonEmptyColumns(row, index, columns, logger, true)
            // Проверка допустимых значений «Графы 7»
            if (!(sign in [1, 2])) {
                loggerError(row, errorMsg + "Графа «${getColumnName(row, 'sign')}» заполнена неверно")
            }
            // Проверка наличия кадастрового номера при указании признака здания
            if (sign == 2 && row.cadastreNumRoom == null) {
                loggerError(row, errorMsg + "При установке признака «Помещение - 2» графа «${getColumnName(row, 'cadastreNumRoom')}» должна быть заполнена!")
            }
            // Проверка необлагаемой налогом кадастровой стоимости
            if (row.cadastrePriceJanuary < row.cadastrePriceTaxFree) {
                loggerError(row, errorMsg + "Необлагаемая налогом кадастровая стоимость не может быть больше общей кадастровой стоимости!")
            }
            // Проверка наличия формы предыдущего периода
            if (!isBalancePeriod() && formDataService.getFormDataPrev(formData) == null) {
                logger.warn("Данные о кадастровой стоимости из предыдущего отчетного периода не были скопированы. В Системе не создавалась первичная налоговая форма «${formData.formType.name}» за предыдущий отчетный период!")
            }

            // Проверка наличия в списке объектов недвижимости строк, для которых графы 3, 4, 5, 7, 8, 9 одинаковы
            rowsToCompare.remove(row)
            for (def anotherRow : rowsToCompare) {
                def compareColumns = ['taxAuthority', 'kpp', 'oktmo', 'sign', 'cadastreNumBuilding', 'cadastreNumRoom']
                def boolean rowEqual = true
                for (def column in compareColumns) {
                    if (row[column] != anotherRow[column]) {
                        rowEqual = false
                        break
                    }
                }
                if (rowEqual) {
                    if (!foundEqualsMap[index]) {
                        def list = new ArrayList()
                        list.add(index)
                        foundEqualsMap.put(index, list)
                    }
                    foundEqualsMap[index].add(anotherRow.getIndex())
                }
            }
            if (foundEqualsMap[index]) {
                loggerError(null, "Обнаружены строки ${foundEqualsMap[index].join(', ')}, у которых совпадают значения Граф 3, 4, 5, 7, 8, 9!")
                rowsToCompare.removeAll { e -> e.getIndex() in foundEqualsMap[index] }
            }
            // Проверка допустимых значений «Графы 14»
            if (formDataDepartment.regionId && row.subject && row.taxBenefitCode) {
                def String filter = String.format("DECLARATION_REGION_ID %s " +
                        "and REGION_ID = %s " +
                        "and RECORD_ID = %s " +
                        "and PARAM_DESTINATION = 2",
                        formDataDepartment.regionId == null ? "is null" : "=" + formDataDepartment.regionId.toString(),
                        row.subject?.toString(),
                        row.taxBenefitCode)
                def records = refBookFactory.getDataProvider(203).getRecords(getReportPeriodEndDate(), null, filter, null)
                if (records.size() == 0 || !(getBenefitCode(row.taxBenefitCode)?.toString() in ["2012000", "2012400", "2012500"])) {
                    loggerError(row, errorMsg + "Графа «${getColumnName(row, 'taxBenefitCode')}» заполнена неверно!")
                }
            }
            // Проверка основания льготы (в справочнике «Параметры налоговых льгот по налогу на имущество» для льготы, выбранной в «Графе 14», основание может быть изменено)
            if (row.benefitBasis != calcBasis(row.taxBenefitCode)) {
                loggerError(row, errorMsg + "Графа «${getColumnName(row, 'benefitBasis')}» заполнена неверно!")
            }
            // Проверка даты возникновения права собственности
            if (row.propertyRightBeginDate < getReportPeriodStartDate() || row.propertyRightBeginDate > getReportPeriodEndDate()) {
                loggerError(row, errorMsg + "Дата возникновения права собственности должна попадать в интервал с ${getReportPeriodStartDate().format('dd.MM.yyyy')} до ${getReportPeriodEndDate().format('dd.MM.yyyy')} (включительно)!")
            }
            // Проверка периода действия права собственности
            if (row.propertyRightEndDate != null && row.propertyRightEndDate < row.propertyRightBeginDate) {
                loggerError(row, errorMsg + "Дата возникновения права собственности не может быть больше даты прекращения!")
            }
            // Проверка существования выбранных параметров декларации
            if (row.subject && row.taxAuthority && row.kpp && row.oktmo) {
                def String filter = String.format("DECLARATION_REGION_ID %s " +
                        "and REGION_ID = %s " +
                        "and LOWER(TAX_ORGAN_CODE) = LOWER('%s') " +
                        "and LOWER(KPP) = LOWER('%s') " +
                        "and OKTMO = %s",
                        formDataDepartment.regionId == null ? "is null" : "=" + formDataDepartment.regionId.toString(),
                        row.subject?.toString(),
                        row.taxAuthority,
                        row.kpp,
                        row.oktmo?.toString())
                records = refBookFactory.getDataProvider(200).getRecords(getReportPeriodEndDate(), null, filter, null)
                if (records != null && records.size() == 0) {
                    rowWarning(logger, row, errorMsg + "Текущие параметры представления декларации (Код субъекта, Код НО, КПП, Код ОКТМО) не предусмотрены (в справочнике «Параметры представления деклараций по налогу на имущество» отсутствует такая запись)!")
                }
            }
            // Проверка диапазона дат
            if (row.propertyRightBeginDate) {
                checkDateValid(logger, row, 'propertyRightBeginDate', row.propertyRightBeginDate, !isBalancePeriod())
            }
            if (row.propertyRightEndDate) {
                checkDateValid(logger, row, 'propertyRightEndDate', row.propertyRightEndDate, !isBalancePeriod())
            }
        } else {
            // Проверка итоговых значений
            if (row.getAlias() != 'total' && row.getAlias().indexOf('total') != -1) {
                srow = calcItog(dataRows.indexOf(row) - 1, dataRows)

                for (def column in totalColumns) {
                    // если итог не совпал
                    if (row[column] != srow[column]) {
                        loggerError(row, errorMsg + "Итоговые значения рассчитаны неверно в графе «${getColumnName(row, column)}»!")
                    }
                }
            }
        }
    }
    // Проверка итоговых значений по всей форме
    checkTotalSum(dataRows, totalColumns, logger, !isBalancePeriod())
}

/** Вывести сообщение. В периоде ввода остатков сообщения должны быть только НЕфатальными. */
void loggerError(def row, def msg) {
    if (isBalancePeriod()) {
        rowWarning(logger, row, msg)
    } else {
        rowError(logger, row, msg)
    }
}

void sort(def dataRows) {
    dataRows.sort { def a, def b ->
        // графа 2  - subject (справочник)
        // графа 3  - taxAuthority
        // графа 4  - kpp
        // графа 5  - oktmo
        // графа 7  - sign
        // графа 8  - cadastreNumBuilding
        // графа 9  - cadastreNumRoom

        def valuesA = [(a.subject ? getRefBookValue(4, a.subject)?.NAME?.value : null), a.taxAuthority, a.kpp,
                       (a.oktmo ? getRefBookValue(96, a.oktmo)?.CODE?.value : null), a.sign, a.cadastreNumBuilding, a.cadastreNumRoom]
        def valuesB = [(b.subject ? getRefBookValue(4, b.subject)?.NAME?.value : null), b.taxAuthority, b.kpp,
                       (b.oktmo ? getRefBookValue(96, b.oktmo)?.CODE?.value : null), b.sign, b.cadastreNumBuilding, b.cadastreNumRoom]

        for (int i = 0; i < 7; i++) {
            def valueA = valuesA[i]
            def valueB = valuesB[i]
            if (valueA != valueB) {
                return valueA <=> valueB
            }
        }
        return 0
    }
}

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, getSubTotalRows(dataRows), getTotalRow(dataRows), true)
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows);
    }
}

// Получение подитоговых строк
def getSubTotalRows(def dataRows) {
    return dataRows.findAll { it.getAlias() != null && !it.getAlias().equals('total')}
}

// Получение подитоговых строк
def getTotalRow(def dataRows) {
    return dataRows.find { it.getAlias() != null && it.getAlias().equals('total')}
}

// Проверка заполнения атрибута «Регион» подразделения текущей формы (справочник «Подразделения»)
void checkRegionId() {
    if (formDataDepartment.regionId == null) {
        throw new Exception("Атрибут «Регион» подразделения текущей налоговой формы не заполнен (справочник «Подразделения»)!")
    }
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 15
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = getColumnName(tmpRow, 'rowNum')
    String TABLE_END_VALUE = null
    int INDEX_FOR_SKIP = 1

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset' : 0, 'colOffset' : 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT, tmpRow)
    if (logger.containsLevel(LogLevel.ERROR)) {
        return;
    }
    // освобождение ресурсов для экономии памяти
    headerValues.clear()
    headerValues = null

    def fileRowIndex = paramsMap.rowOffset
    def colOffset = paramsMap.colOffset
    paramsMap.clear()
    paramsMap = null

    def rowIndex = 0
    def rows = []
    def allValuesCount = allValues.size()

    // формирвание строк нф
    for (def i = 0; i < allValuesCount; i++) {
        rowValues = allValues[0]
        fileRowIndex++
        // все строки пустые - выход
        if (!rowValues) {
            allValues.remove(rowValues)
            rowValues.clear()
            break
        }
        // Пропуск итоговых строк
        if (rowValues[INDEX_FOR_SKIP] && rowValues[INDEX_FOR_SKIP].contains("Итого по НО ")) {
            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }
        // простая строка
        rowIndex++
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
        rows.add(newRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    showMessages(rows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        def formTemplate = formDataService.getFormTemplate(formData.formType.id, formData.reportPeriodId)
        def templateRows = formTemplate.rows
        rows.add(getDataRow(templateRows, 'total'))

        updateIndexes(rows)
        formDataService.getDataRowHelper(formData).allCached = rows
    }
}

/**
 * Проверить шапку таблицы
 *
 * @param headerRows строки шапки
 * @param colCount количество колонок в таблице
 * @param rowCount количество строк в таблице
 * @param tmpRow вспомогательная строка для получения названии графов
 */
void checkHeaderXls(def headerRows, def colCount, rowCount, def tmpRow) {
    if (headerRows.isEmpty()) {
        throw new ServiceException(WRONG_HEADER_ROW_SIZE)
    }
    checkHeaderSize(headerRows[headerRows.size() - 1].size(), headerRows.size(), colCount, rowCount)

    def headerMapping = [
            (headerRows[0][0]) : getColumnName(tmpRow, 'rowNum'),
            (headerRows[0][2]) : getColumnName(tmpRow, 'subject'),
            (headerRows[0][3]) : getColumnName(tmpRow, 'taxAuthority'),
            (headerRows[0][4]) : getColumnName(tmpRow, 'kpp'),
            (headerRows[0][5]) : getColumnName(tmpRow, 'oktmo'),
            (headerRows[0][6]) : getColumnName(tmpRow, 'address'),
            (headerRows[0][7]) : getColumnName(tmpRow, 'sign'),
            (headerRows[0][8]) : 'Кадастровый номер',
            (headerRows[0][10]): 'Кадастровая стоимость',
            (headerRows[0][12]): 'Право собственности',
            (headerRows[0][14]): getColumnName(tmpRow, 'taxBenefitCode'),
            (headerRows[0][15]): getColumnName(tmpRow, 'benefitBasis'),
            (headerRows[1][8]) : 'Здание',
            (headerRows[1][9]) : 'Помещение',
            (headerRows[1][10]): 'на 1 января',
            (headerRows[1][11]): 'в т.ч. необлагаемая налогом',
            (headerRows[1][12]): 'Дата возникновения',
            (headerRows[1][13]): 'Дата прекращения',
            (headerRows[2][0]) : '1'
    ]
    (2..15).each { index ->
        headerMapping.put((headerRows[2][index]), index.toString())
    }
    checkHeaderEquals(headerMapping, logger)
}

/**
 * Получить новую строку нф по значениям из экселя.
 *
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 */
def getNewRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex) {
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }

    // графа 1
    // графа fix
    // графа 2
    newRow.subject = getRecordIdImport(4, 'CODE', values[2], fileRowIndex, 2 + colOffset)
    // графа 3
    newRow.taxAuthority = values[3]
    // графа 4
    newRow.kpp = values[4]
    // графа 5
    newRow.oktmo = getRecordIdImport(96, 'CODE', values[5], fileRowIndex, 5 + colOffset)
    // графа 6
    newRow.address = values[6]
    // графа 7
    newRow.sign = parseNumber(values[7], fileRowIndex, 7 + colOffset, logger, true)
    // графа 8
    newRow.cadastreNumBuilding = values[8]
    // графа 9
    newRow.cadastreNumRoom = values[9]
    // графа 10
    newRow.cadastrePriceJanuary = parseNumber(values[10], fileRowIndex, 10 + colOffset, logger, true)
    // графа 11
    newRow.cadastrePriceTaxFree = parseNumber(values[11], fileRowIndex, 11 + colOffset, logger, true)
    // графа 12
    newRow.propertyRightBeginDate = parseDate(values[12], 'dd.MM.yyyy', fileRowIndex, 12 + colOffset, logger, true)
    // графа 13
    newRow.propertyRightEndDate = parseDate(values[13], 'dd.MM.yyyy', fileRowIndex, 13 + colOffset, logger, true)
    // графа 15
    newRow.benefitBasis = values[15]
    // графа 14
    // TODO может как-то попроще
    String filter = "CODE = '" + values[14] + "'"
    def records202 = refBookFactory.getDataProvider(202).getRecords(getReportPeriodEndDate(), null, filter, null)
    for (def record202 : records202) {
        filter = "DECLARATION_REGION_ID = " + formDataDepartment.regionId?.toString() + " and REGION_ID = " + newRow.subject?.toString() + " and TAX_BENEFIT_ID = " + record202.record_id.value + " and PARAM_DESTINATION = 2"
        def records = refBookFactory.getDataProvider(203).getRecords(getReportPeriodEndDate(), null, filter, null)
        def taxRecordId = records.find { calcBasis(it?.record_id?.value) == newRow.benefitBasis }?.record_id?.value
        if (taxRecordId) {
            newRow.taxBenefitCode = taxRecordId
        }
    }

    return newRow
}