package form_template.property.property_945_2.v2014

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.Department
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
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
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
        calc()
        logicCheck()
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

// Признак периода ввода остатков. Отчетный период является периодом ввода остатков.
def isBalancePeriod() {
    if (isBalancePeriod == null) {
        isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)
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
    def prevFormData = formDataService.getFormDataPrev(formData, formDataDepartment.id)
    def prevDataRows = (prevFormData != null ? formDataService.getDataRowHelper(prevFormData)?.allCached : null)

    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    if (reportPeriod.order == 1){
        def Date startDate = getReportPeriodStartDate()
        def Date endDate = getReportPeriodEndDate()
        prevDataRows = prevDataRows.findAll { row ->
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
                return (rightBeginDate > endDate || rightEndDate < startDate)
            }
        }
    }
    if (prevDataRows) {
        def dataRowHelper = formDataService.getDataRowHelper(formData)
        dataRowHelper.save(prevDataRows)
    }
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def totalRow = getDataRow(dataRows, 'total')

    deleteAllAliased(dataRows)

    if (formDataEvent != FormDataEvent.IMPORT) {
        sort(dataRows)
    }
    for (def row : dataRows) {
        def basis = calcBasis(row.taxBenefitCode)
        row.benefitBasis = basis
    }

    addFixedRows(dataRows, totalRow)

    dataRowHelper.save(dataRows)
}

def calcBasis(def recordId) {
    if (recordId == null) {
        return null
    }
    def record = getRefBookValue(203, recordId)
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
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

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
            if (!isBalancePeriod() && formDataService.getFormDataPrev(formData, formDataDepartment.id) == null) {
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
                String filter = "DECLARATION_REGION_ID = " + formDataDepartment.regionId?.toString() + " and REGION_ID = " + row.subject?.toString() + " and RECORD_ID = " + row.taxBenefitCode + " and PARAM_DESTINATION = 2"
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
                filter = "DECLARATION_REGION_ID = " + formDataDepartment.regionId?.toString() + " and REGION_ID = " + row.subject?.toString() + " and LOWER(TAX_ORGAN_CODE) = LOWER('" + row.taxAuthority + "') and LOWER(KPP) = LOWER('" + row.kpp + "') and OKTMO = " + row.oktmo?.toString()
                records = refBookFactory.getDataProvider(200).getRecords(getReportPeriodEndDate(), null, filter, null)
                if (records.size() == 0) {
                    rowWarning(logger, row, errorMsg + "Текущие параметры представления декларации (Код субъекта, Код НО, КПП, Код ОКТМО) не предусмотрены (в справочнике «Параметры представления деклараций по налогу на имущество» отсутствует такая запись)!")
                }
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

void importData() {
    def tempRow = formData.createDataRow()
    def xml = getXML(ImportInputStream, importService, UploadFileName, getColumnName(tempRow, 'rowNum'), null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 15, 3)

    def headerMapping = [
            (xml.row[0].cell[0]): getColumnName(tempRow, 'rowNum'),
            (xml.row[0].cell[2]): getColumnName(tempRow, 'subject'),
            (xml.row[0].cell[3]): getColumnName(tempRow, 'taxAuthority'),
            (xml.row[0].cell[4]): getColumnName(tempRow, 'kpp'),
            (xml.row[0].cell[5]): getColumnName(tempRow, 'oktmo'),
            (xml.row[0].cell[6]): getColumnName(tempRow, 'address'),
            (xml.row[0].cell[7]): getColumnName(tempRow, 'sign'),
            (xml.row[0].cell[8]): 'Кадастровый номер',
            (xml.row[0].cell[10]): 'Кадастровая стоимость',
            (xml.row[0].cell[12]): 'Право собственности',
            (xml.row[0].cell[14]): getColumnName(tempRow, 'taxBenefitCode'),
            (xml.row[0].cell[15]): getColumnName(tempRow, 'benefitBasis'),
            (xml.row[1].cell[8]): 'Здание',
            (xml.row[1].cell[9]): 'Помещение',
            (xml.row[1].cell[10]): 'на 1 января',
            (xml.row[1].cell[11]): 'в т.ч. необлагаемая налогом',
            (xml.row[1].cell[12]): 'Дата возникновения',
            (xml.row[1].cell[13]): 'Дата прекращения',
            (xml.row[2].cell[0]): '1'
    ]
    (2..15).each { index ->
        headerMapping.put((xml.row[2].cell[index]), index.toString())
    }
    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

void addData(def xml, int headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def xmlIndexRow = -1
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    def rows = []
    def int rowIndex = 1

    def totalRow = getDataRow(dataRows, 'total')

    for (def row : xml.row) {
        xmlIndexRow++
        def int xlsIndexRow = xmlIndexRow + rowOffset

        /* Пропуск строк шапок */
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

        // графа 1
        // графа fix
        // графа 2
        newRow.subject = getRecordIdImport(4, 'CODE', row.cell[2].text(), xlsIndexRow, 2 + colOffset)
        // графа 3
        newRow.taxAuthority = row.cell[3].text()
        // графа 4
        newRow.kpp = row.cell[4].text()
        // графа 5
        newRow.oktmo = getRecordIdImport(96, 'CODE', row.cell[5].text(), xlsIndexRow, 5 + colOffset)
        // графа 6
        newRow.address = row.cell[6].text()
        // графа 7
        newRow.sign = parseNumber(row.cell[7].text(), xlsIndexRow, 7 + colOffset, logger, true)
        // графа 8
        newRow.cadastreNumBuilding = row.cell[8].text()
        // графа 9
        newRow.cadastreNumRoom = row.cell[9].text()
        // графа 10
        newRow.cadastrePriceJanuary = parseNumber(row.cell[10].text(), xlsIndexRow, 10 + colOffset, logger, true)
        // графа 11
        newRow.cadastrePriceTaxFree = parseNumber(row.cell[11].text(), xlsIndexRow, 11 + colOffset, logger, true)
        // графа 12
        newRow.propertyRightBeginDate = parseDate(row.cell[12].text(), 'dd.MM.yyyy', xlsIndexRow, 12 + colOffset, logger, true)
        // графа 13
        newRow.propertyRightEndDate = parseDate(row.cell[13].text(), 'dd.MM.yyyy', xlsIndexRow, 13 + colOffset, logger, true)
        // графа 15
        newRow.benefitBasis = row.cell[15].text()
        // графа 14
        // TODO может как-то попроще
        String filter = "CODE = '" + row.cell[14].text() + "'"
        def records202 = refBookFactory.getDataProvider(202).getRecords(getReportPeriodEndDate(), null, filter, null)
        for (def record202 : records202) {
            filter = "DECLARATION_REGION_ID = " + formDataDepartment.regionId?.toString() + " and REGION_ID = " + newRow.subject?.toString() + " and TAX_BENEFIT_ID = " + record202.record_id.value + " and PARAM_DESTINATION = 2"
            def records = refBookFactory.getDataProvider(203).getRecords(getReportPeriodEndDate(), null, filter, null)
            def taxRecordId = records.find { calcBasis(it?.record_id?.value) == newRow.benefitBasis }?.record_id?.value
            if (taxRecordId) {
                newRow.taxBenefitCode = taxRecordId
            }
        }

        rows.add(newRow)
    }
    rows.add(totalRow)
    dataRowHelper.save(rows)
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
