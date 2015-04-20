package form_template.transport.vehicles.v2014

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import groovy.transform.Field

/**
 * Сведения о транспортных средствах, по которым уплачивается транспортный налог
 * formTypeId=201
 * formTemplateId=204
 *
 * форма действует с 1 октября 2014
 *
 * @author ivildanov
 * @author Stanislav Yasinskiy
 */

// графа 1  - rowNumber
// графа    - fix
// графа 2  - codeOKATO         - атрибут 840 - CODE - «Код», справочник 96 «Общероссийский классификатор территорий муниципальных образований»
// графа 3  - regionName        - зависит от графы 2 - атрибут 841 - NAME - «Наименование», справочник 96 «Общероссийский классификатор территорий муниципальных образований»
// графа 4  - tsTypeCode        - атрибут 422 - CODE - «Код вида ТС», справочник 42 «Коды видов транспортных средств»
// графа 5  - tsType            - зависит от графы 4 - атрибут 423 - NAME - «Наименование вида транспортного средства», справочник 42 «Коды видов транспортных средств»
// графа 6  - model
// графа 7  - ecoClass          - атрибут 400 - CODE - «Код экологического класса», справочник 40 «Экологические классы»
// графа 8  - identNumber
// графа 9  - regNumber
// графа 10 - regDate
// графа 11 - regDateEnd
// графа 12 - taxBase
// графа 13 - baseUnit          - атрибут 57 - CODE - «Код единицы измерения», справочник 12 «Коды единиц измерения налоговой базы на основании ОКЕИ»
// графа 14 - year
// графа 15 - pastYear
// графа 16 - stealDateStart
// графа 17 - stealDateEnd
// графа 18 - share
// графа 19 - costOnPeriodBegin
// графа 20 - costOnPeriodEnd
// графа 21 - benefitStartDate
// графа 22 - benefitEndDate
// графа 23 - taxBenefitCode    - атрибут 19 - TAX_BENEFIT_ID - «Код налоговой льготы», справочник 7 «Параметры налоговых льгот транспортного налога»
// графа 24 - base
// графа 25 - version           - атрибут 2082 - MODEL - «Модель (версия)», справочник 208 «Средняя стоимость транспортных средств»

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.AFTER_CREATE:
        if (formData.kind == FormDataKind.PRIMARY) {
            checkRegionId()
            copyData()
        }
        break
    case FormDataEvent.CALCULATE:
        checkRegionId()
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK:
        checkRegionId()
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        addRow()
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow != null && currentDataRow.getAlias() == null) {
            formDataService.getDataRowHelper(formData)?.delete(currentDataRow)
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
    case FormDataEvent.COMPOSE:
        checkRegionId()
        consolidation()
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        checkRegionId()
        importData()
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        checkRegionId()
        importTransportData()
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

// Редактируемые атрибуты (графа 2, 4, 6..14, 16..23, 25)
@Field
def editableColumns = ['codeOKATO', 'tsTypeCode', 'model', 'ecoClass', 'identNumber', 'regNumber',
                       'regDate', 'regDateEnd', 'taxBase', 'baseUnit', 'year', 'stealDateStart', 'stealDateEnd',
                       'share', 'costOnPeriodBegin', 'costOnPeriodEnd', 'benefitStartDate', 'benefitEndDate', 'taxBenefitCode', 'version']

// Проверяемые на пустые значения атрибуты (графа 1..6, 8..10, 12..15, 18. Графа 1, 3, 5 исключены)
@Field
def nonEmptyColumns = [/*'rowNumber',*/ 'codeOKATO', /*'regionName',*/ 'tsTypeCode', /*'tsType',*/ 'model',
                       'identNumber', 'regNumber', 'regDate', 'taxBase', 'baseUnit', 'year', 'pastYear', 'share']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber']

// дата начала отчетного периода
@Field
def start = null

// дата окончания отчетного периода
@Field
def endDate = null

// Признак периода ввода остатков
@Field
def isBalancePeriod

// отчетный период формы
@Field
def currentReportPeriod = null

// список алиасов подразделов
@Field
def sections = ['A', 'B', 'C']

@Field
def copyColumns = ['codeOKATO', 'tsTypeCode', 'model', 'ecoClass', 'identNumber', 'regNumber', 'regDate',
                   'regDateEnd', 'taxBase', 'baseUnit', 'year', 'pastYear', 'stealDateStart', 'stealDateEnd', 'share',
                   'costOnPeriodBegin', 'costOnPeriodEnd', 'benefitStartDate', 'benefitEndDate', 'taxBenefitCode', 'base', 'version']

@Field
def copyColumns201 = ['codeOKATO', 'tsTypeCode', 'identNumber', 'model', 'ecoClass', 'regNumber',
                      'baseUnit', 'year', 'regDate', 'regDateEnd', 'stealDateStart', 'stealDateEnd']

// Соответствие задается совпадением граф 2, 4, 8, 12, 13
@Field
def columnsForEquals = ['codeOKATO', 'tsTypeCode', 'identNumber', 'taxBase', 'baseUnit']

//// Обертки методов

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    if (recordId == null) {
        return null
    }
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                    def boolean required = true) {
    if (value == null || value == '') {
        return null
    }
    return formDataService.getRefBookRecordImport(refBookId, recordCache, providerCache, refBookCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Признак периода ввода остатков для отчетного периода подразделения
def isBalancePeriod() {
    if (isBalancePeriod == null) {
        def departmentReportPeriod = departmentReportPeriodService.get(formData.departmentReportPeriodId)
        isBalancePeriod = departmentReportPeriod.isBalance()
    }
    return isBalancePeriod
}

//// Кастомные методы

def addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def index
    if (currentDataRow == null || currentDataRow.getIndex() == -1) {
        index = getDataRow(dataRows, 'B').getIndex()
    } else {
        index = currentDataRow.getIndex() + 1
    }
    dataRowHelper.insert(getNewRow(), index)
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def currentYear = getReportPeriod().taxPeriod.year
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        // графа 15
        row.pastYear = calc15(row, currentYear)

        // графа 24
        row.base = calc24(row)
    }

    dataRowHelper.save(dataRows)

    sortFormDataRows()
}

def calc15(def row, def currentYear) {
    if (row.year == null) {
        return null
    }
    return currentYear - row.year.format('yyyy')?.toInteger()
}

def calc24(row) {
    if (row.taxBenefitCode == null) {
        return null
    }
    def record = getRefBookValue(7L, row.taxBenefitCode)
    // дополнить 0 слева если значении меньше четырех
    def section = record.SECTION.value ?: ''
    def item = record.ITEM.value ?: ''
    def subItem = record.SUBITEM.value ?: ''
    return String.format("%s%s%s", section.padLeft(4, '0'), item.padLeft(4, '0'), subItem.padLeft(4, '0'))
}

def logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def reportPeriod = getReportPeriod()
    def dFrom = getReportPeriodStartDate()
    def dTo = getReportPeriodEndDate()
    def String dFormat = "dd.MM.yyyy"

    def rowMap = getRowEqualsMap(dataRows, columnsForEquals)

    // регион из подразделения формы для проверки 10
    def regionId = formDataDepartment.regionId

    def sectionTsTypeCodeMap = ['A' : '50000', 'B' : '40200', 'C' : '40100']
    def sectionTsTypeCode = null

    for (def row in dataRows) {
        if (row.getAlias() != null) {
            sectionTsTypeCode = sectionTsTypeCodeMap[row.getAlias()]
            continue
        }

        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Проверка на заполнение поля
        checkNonEmptyColumns(row, index ?: 0, nonEmptyColumns, logger, !isBalancePeriod())

        // 2. Проверка на соответствие дат при постановке (снятии) с учёта
        if (!(row.regDateEnd == null || (row.regDate != null && row.regDateEnd >= row.regDate))) {
            loggerError(row, errorMsg + 'Дата постановки (снятия) с учёта неверная!')
        }

        // 3. Проврека на наличие даты угона при указании даты возврата
        if (row.stealDateEnd != null && row.stealDateStart == null) {
            loggerError(row, errorMsg + 'Не заполнено поле «Дата угона»!')
        }

        // 4. Проверка на соответствие дат сведений об угоне
        if (row.stealDateStart != null && row.stealDateEnd != null && row.stealDateEnd < row.stealDateStart) {
            loggerError(row, errorMsg + 'Дата возврата ТС неверная!')
        }

        // 6. Проверка на наличие в списке ТС строк, период владения которых не пересекается с отчётным
        if (row.regDate != null && row.regDate > dTo || row.regDateEnd != null && row.regDateEnd < dFrom) {
            loggerError(row, errorMsg + 'Период регистрации ТС ('
                    + row.regDate.format(dFormat) + ' - ' + ((row.regDateEnd != null) ? row.regDateEnd.format(dFormat) : '...') + ') ' +
                    ' не пересекается с периодом (' + dFrom.format(dFormat) + " - " + dTo.format(dFormat) +
                    '), за который сформирована налоговая форма!')
        }

        // 8. Проверка года изготовления ТС
        if (row.year != null) {
            Calendar calenadarMake = Calendar.getInstance()
            calenadarMake.setTime(row.year)
            if (calenadarMake.get(Calendar.YEAR) > reportPeriod.taxPeriod.year) {
                loggerError(row, errorMsg + 'Год изготовления ТС не может быть больше отчетного года!')
            }
        }

        if (row.share != null){
            def columnName = getColumnName(row, 'share')
            if (!(row.share ==~ /\d{1,10}\/\d{1,10}/)) {
                loggerError(row, errorMsg + "Графа «$columnName» должна быть заполнена согласно формату «(от 1 до 10 знаков)/(от 1 до 10 знаков)»!")
            } else {
                def partArray = row.share.split('/')
                if (partArray[1] ==~ /0{1,10}/) {
                    logger.error(errorMsg + "Деление на ноль в графе «$columnName»!")
                }
            }
        }

        // 9. Проверка корректности заполнения «Графы 15»
        if (row.pastYear != calc15(row, reportPeriod.taxPeriod.year)) {
            def columnName = getColumnName(row, 'pastYear')
            loggerError(row, errorMsg + "Графа «$columnName» заполнена неверно!")
        }

        if (row.codeOKATO != null) {
            // 10. Проверка наличия параметров представления декларации для кода ОКТМО
            def filter = "DECLARATION_REGION_ID = $regionId and OKTMO = $row.codeOKATO"
            // справочник «Параметры представления деклараций по транспортному налогу»
            def records = getProvider(210L).getRecords(dTo, null, filter, null)
            if (records == null || records.isEmpty()) {
                loggerError(row, errorMsg + "Для выбранного кода ОКТМО отсутствует запись в справочнике «Параметры представления деклараций по транспортному налогу»!")
            }

            // 15. Проверка допустимых значений «Графы 23»
            if (row.taxBenefitCode != null && records != null && !records.isEmpty()) {
                def record7 = getRefBookValue(7L, row.taxBenefitCode)
                def record6 = getRefBookValue(6L, record7?.TAX_BENEFIT_ID?.value)
                def isError
                if (record6 == null) {
                    isError = true
                } else {
                    def code = record6?.CODE?.value
                    isError = !(code in ["30200" , "20200" , "20210" , "20220" , "20230"])
                }
                if (isError) {
                    def columnName = getColumnName(row, 'taxBenefitCode')
                    loggerError(row, errorMsg + "Графа «$columnName» заполнена неверно!")
                }
            }
        }

        // 11. Поверка на соответствие дат использования льготы
        if (row.benefitEndDate != null && row.benefitStartDate != null && row.benefitEndDate < row.benefitStartDate) {
            loggerError(row, errorMsg + "Неверно указаны даты начала и окончания использования льготы!")
        }

        // 12. Проверка на заполнение периода использования льготы при указании кода налоговой льготы
        if (row.taxBenefitCode != null && row.benefitStartDate == null) {
            loggerError(row, errorMsg + "При указании кода налоговой льготы должен быть заполнен период использования льготы!")
        }

        // 13. Проверка на заполнение кода налоговой льготы при указании периода использования льгот
        if (row.benefitStartDate != null && row.taxBenefitCode == null) {
            loggerError(row, errorMsg + "При указании периода использования льготы должен быть заполнен код налоговой льготы!")
        }

        // 14. Проверка на наличие в списке ТС строк, период использования льготы, которых, не пересекается с отчётным...
        if (row.benefitStartDate != null && row.benefitStartDate > dTo || row.benefitEndDate != null && row.benefitEndDate < dFrom) {
            loggerError(row, errorMsg + "Период использования льготы ТС (${row.benefitStartDate.format(dFormat)} - ${row.benefitEndDate.format(dFormat)}) не пересекается с периодом (${dFrom.format(dFormat)} - ${dTo.format(dFormat)}), за который сформирована налоговая форма!")
        }

        // 16. Проверка корректности заполнения «Графы 24»
        if (row.base != calc24(row)) {
            def columnName = getColumnName(row, 'base')
            loggerError(row, errorMsg + "Графа «$columnName» заполнена неверно!")
        }

        // 17. Проверка наличия повышающего коэффициента для ТС дороже 3 млн. руб.
        if (row.version != null) {
            def averageCost = getRefBookValue(208L, row.version)?.AVG_COST?.value
            def filter = "AVG_COST = $averageCost"
            // справочник «Повышающие коэффициенты транспортного налога»
            def records = getProvider(209L).getRecords(dTo, null, filter, null)
            if (records == null || records.isEmpty()) {
                loggerError(row, errorMsg + "Для средней стоимости выбранной модели (версии) из перечня, утвержденного на налоговый период, отсутствует запись в справочнике «Повышающие коэффициенты транспортного налога»!")
            }
        }

        // 18. Проверка кода вида ТС (графа 4) по разделу «Наземные транспортные средства»
        // 19. Проверка кода вида ТС (графа 4) по разделу «Водные транспортные средства»
        // 20. Проверка кода вида ТС (графа 4) по разделу «Воздушные транспортные средства»
        if (row.tsTypeCode != null) {
            // проверить выбрана ли верхушка деревьев видов ТС
            def tsTypeCode = getRefBookValue(42L, row.tsTypeCode)?.CODE?.value
            def hasError = tsTypeCode in sectionTsTypeCodeMap.values()
            if (!hasError) {
                // проверить корень дерева видов ТС на соответстиве
                def perentTsTypeCode = getParentTsTypeCode(row.tsTypeCode)
                if (perentTsTypeCode != null) {
                    hasError = sectionTsTypeCode != perentTsTypeCode
                }
            }
            if (hasError) {
                def columnName = getColumnName(row, 'tsTypeCode')
                loggerError(row, errorMsg + "Графа «$columnName» заполнена неверно!")
            }
        }
    }

    // 5. Проверка на наличие в списке ТС строк, для которых графы "Код ОКТМО", "Вид ТС",
    // "Идентификационный номер ТС", "Налоговая база", "Единица измерения налоговой базы по ОКЕИ" одинаковы
    rowMap.each { key, rowList ->
        if (rowList.size() > 1) {
            def errorRows = ''
            rowList.each { row ->
                errorRows = errorRows + ', ' + row.getIndex()
            }
            if (!''.equals(errorRows)) {
                def row = rowList[0]
                loggerError(row, "Обнаружены строки $errorRows, у которых " +
                        "Код ОКТМО = ${getRefBookValue(96L, row.codeOKATO)?.CODE?.stringValue ?: '\"\"'}, " +
                        "Код вида ТС = ${getRefBookValue(42L, row.tsTypeCode)?.CODE?.stringValue ?: '\"\"'}, " +
                        "Идентификационный номер ТС = ${row.identNumber ?: '\"\"'}, " +
                        "Налоговая база = ${row.taxBase?:'\"\"'}, " +
                        "Единица измерения налоговой базы по ОКЕИ = ${getRefBookValue(12L, row.baseUnit)?.CODE?.value ?: '\"\"'} " +
                        "совпадают!")
            }
        }
    }

    // 7. Проверка наличия формы предыдущего периода
    def prevReportPeriod = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
    if (prevReportPeriod != null) {
        def str = ''
        if (reportPeriod.order == 4) {
            // 3 квартал
            str += checkPrevPeriod(prevReportPeriod)
            // 2 квартал
            prevReportPeriod = reportPeriodService.getPrevReportPeriod(prevReportPeriod.id)
            str += checkPrevPeriod(prevReportPeriod)
            // 1 квартал
            prevReportPeriod = reportPeriodService.getPrevReportPeriod(prevReportPeriod.id)
            str += checkPrevPeriod(prevReportPeriod)
        } else {
            str = checkPrevPeriod(prevReportPeriod)
        }
        if (str.length() > 2) {
            logger.warn("Данные ТС из предыдущих отчётных периодов не были скопированы. В Системе " +
                    "не создавались формы за следующие периоды: " + str.substring(0, str.size() - 2) + ".")
        }
    }
}

// Сгруппировать строки в карту по общим колонкам
def getRowEqualsMap(def dataRows, def columns) {
    def result = [:]
    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        StringBuilder keySb = new StringBuilder()
        boolean skipRow = false
        for (column in columns) {
            if (row[column] != null) {
                keySb.append(row[column]).append("#")
            } else {
                skipRow = true
            }
        }
        if (skipRow) {
            continue
        }
        String keyString = keySb.toString()
        if (result[keyString] == null) {
            result[keyString] = []
        }
        result[keyString].add(row)
    }
    return result
}

void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // удалить нефиксированные строки
    deleteNotFixedRows(dataRows)

    // собрать из источников строки и разместить соответствующим разделам
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind,
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        if (it.formTypeId == formData.formType.id) {
            def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceDataRows = formDataService.getDataRowHelper(source).allCached
                // копирование данных по разделам
                sections.each { section ->
                    copyRows(sourceDataRows, dataRows, section)
                }
            }
        }
    }
    dataRowHelper.save(dataRows)
}

def String checkPrevPeriod(def reportPeriod) {
    if (reportPeriod != null) {
        // ищем форму нового типа иначе две формы старого
        if (formDataService.getLast(formData.formType.id, formData.kind, formDataDepartment.id, reportPeriod.id, formData.periodOrder) == null) {
            return reportPeriod.name + " " + reportPeriod.taxPeriod.year + ", "
        }
    }
    return ''
}

// Алгоритм копирования данных из форм предыдущего периода при создании формы.
// Также получение данных из старых форм "Сведения о транспортных средствах, по которым уплачивается транспортный налог"
// и "Сведения о льготируемых транспортных средствах, по которым уплачивается транспортный налог"
def copyData() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def reportPeriod = getReportPeriod()
    def prevReportPeriod = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
    if (prevReportPeriod == null) {
        return
    }

    if (reportPeriod.order == 4) {
        // 3 квартал
        dataRows = getPrevRowsForCopy(prevReportPeriod, dataRows)
        // 2 квартал
        prevReportPeriod = reportPeriodService.getPrevReportPeriod(prevReportPeriod.id)
        dataRows = getPrevRowsForCopy(prevReportPeriod, dataRows)
        // 1 квартал
        prevReportPeriod = reportPeriodService.getPrevReportPeriod(prevReportPeriod.id)
        dataRows = getPrevRowsForCopy(prevReportPeriod, dataRows)
    } else {
        dataRows = getPrevRowsForCopy(prevReportPeriod, dataRows)
    }

    if (dataRows.size() > 3) {
        dataRowHelper.save(dataRows)
    }
}

def copyRow(def row, def columns) {
    def newRow = getNewRow()
    columns.each {
        newRow.getCell(it).setValue(row.getCell(it).value, null)
    }
    return newRow
}

// Получить новую строку с заданными стилями.
def getNewRow() {
    def newRow = formData.createDataRow()
    if (isBalancePeriod()) {
        (editableColumns + ['pastYear', 'base']).each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).styleAlias = 'Редактируемая'
        }
    } else {
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).styleAlias = 'Редактируемая'
        }
        autoFillColumns.each {
            newRow.getCell(it).styleAlias = 'Автозаполняемая'
        }
    }
    return newRow
}

// Получить строки для копирования за предыдущий отчетный период
def getPrevRowsForCopy(def reportPeriod, def dataRows) {
    if (reportPeriod != null) {
        def formData201 = formDataService.getLast(201, formData.kind, formDataDepartment.id, reportPeriod.id, null)
        if (formData201 != null && formData201.formTemplateId == 201) {
            // получить нет формы за предыдущий отчетный период, то попытаться найти старую форму 201
            def dataRows201 = (formData201 != null ? formDataService.getDataRowHelper(formData201)?.allCached : null)

            if (dataRows201 != null && !dataRows201.isEmpty()) {
                dataRows = copyFromOldForm(dataRows, dataRows201)
            }
        } else {
            // получить форму за предыдущий отчетный период
            def dataRowsPrev = (formData201 != null ? formDataService.getDataRowHelper(formData201)?.allCached : null)
            if (dataRowsPrev != null && !dataRowsPrev.isEmpty()) {
                dataRows = copyFromOursForm(dataRows, dataRowsPrev)
            }
        }
    }
    return dataRows
}

/**
 * Скопировать строки из аналогичной предыдущей формы.
 *
 * @param dataRows строки текущей формы
 * @param dataRowsPrev строки предыдущей формы
 */
def copyFromOursForm(def dataRows, def dataRowsPrev) {
    def rowsOld = []
    rowsOld.addAll(dataRows)

    def dFrom = getReportPeriodStartDate()
    def dTo = getReportPeriodEndDate()

    def section = null
    def sectionRows = [:]
    sections.each {
        sectionRows[it] = []
    }

    def rowOldMap = getRowEqualsMap(rowsOld, columnsForEquals)

    for (def row : dataRowsPrev) {
        if (row.getAlias() != null) {
            section = row.getAlias()
            continue
        }
        // если период владения ТСом не пересекается с текущим периодом формы, то пропустить эту строку
        if ((row.regDateEnd != null && row.regDateEnd < dFrom) || (row.regDate > dTo)) {
            continue
        }

        // эта часть вроде как лишняя
        def regDateEnd = row.regDateEnd
        if (regDateEnd == null || regDateEnd > dTo) {
            regDateEnd = dTo
        }
        def regDate = row.regDate
        if (regDate < dFrom) {
            regDate = dFrom
        }
        if (regDate > dTo || regDateEnd < dFrom) {
            continue
        }

        // исключаем дубли
        def need = true
        // формируем ключ карты

        def keySb = new StringBuilder()
        for (column in columnsForEquals) {
            // если графа пустая, то считаем что не дубль
            if (row[column] == null) {
                keySb = null
                break
            } else {
                keySb.append(row[column]).append("#")
            }
        }
        if (keySb != null && rowOldMap[keySb.toString()] != null){
            need = false
        }
        if (need) {
            row.setIndex(rowsOld.size())
            def newRow = copyRow(row, copyColumns)
            rowsOld.add(newRow)
            sectionRows[section].add(newRow)
        }
    }
    sections.each {
        dataRows.addAll(getLastRowIndexInSection(dataRows, it), sectionRows[it])
        updateIndexes(dataRows)
    }
    return dataRows
}

/**
 * Скопировать строки из устаревших предыдущих форм
 * "Сведения о транспортных средствах, по которым уплачивается транспортный налог" и
 * "Сведения о льготируемых транспортных средствах, по которым уплачивается транспортный налог".
 *
 * @param dataRows строки текущей формы
 * @param dataRows201Old строки предыдущей формы 201
 */
def copyFromOldForm(def dataRows, dataRows201Old) {
    def rowsOld = []
    rowsOld.addAll(dataRows)
    def tmpRows = []

    def dFrom = getReportPeriodStartDate()
    def dTo = getReportPeriodEndDate()

    def sectionRows = [:]
    sections.each {
        sectionRows[it] = []
    }

    def sectionMap = ['50000' : 'A', '40200' : 'B', '40100' : 'C']

    def dublColumns = ['codeOKATO', 'identNumber', 'baseUnit']
    def rowOldMap = getRowEqualsMap(rowsOld, dublColumns)

    // получение данных из 201
    for (def row : dataRows201Old) {
        if ((row.regDateEnd != null && row.regDateEnd < dFrom) || (row.regDate > dTo)) {
            continue
        }

        // эта часть вроде как лишняя
        def regDateEnd = row.regDateEnd
        if (regDateEnd == null || regDateEnd > dTo) {
            regDateEnd = dTo
        }
        def regDate = row.regDate
        if (regDate < dFrom) {
            regDate = dFrom
        }
        if (regDate > dTo || regDateEnd < dFrom) {
            continue
        }

        // исключаем дубли
        def need = true
        // формируем ключ карты
        def keySb = new StringBuilder()
        for (column in dublColumns) {
            // если графа пустая, то считаем что не дубль
            if (row[column] == null) {
                keySb = null
                break
            } else {
                keySb.append(row[column]).append("#")
            }
        }
        if (keySb != null && rowOldMap[keySb.toString()] != null){
            need = false
        }
        if (need) {
            newRow = copyRow(row, copyColumns201)
            def tsTypeCode = getRefBookValue(42L, row.tsTypeCode)?.CODE?.value
            if (!(tsTypeCode in sectionMap.keySet())) {
                tsTypeCode = getParentTsTypeCode(row.tsTypeCode)
            }
            if (tsTypeCode != null && tsTypeCode in sectionMap.keySet()) {
                sectionRows[sectionMap[tsTypeCode]].add(newRow)
                rowsOld.add(newRow)
                tmpRows.add(newRow)
            }
        }
    }
    sections.each {
        dataRows.addAll(getLastRowIndexInSection(dataRows, it), sectionRows[it])
        updateIndexes(dataRows)
    }

    return dataRows
}

def getReportPeriodStartDate() {
    if (!start) {
        start = reportPeriodService.getStartDate(formData.reportPeriodId).time
    }
    return start
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

def getReportPeriod() {
    if (currentReportPeriod == null) {
        currentReportPeriod = reportPeriodService.get(formData.reportPeriodId)
    }
    return currentReportPeriod
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    def xml = getXML(ImportInputStream, importService, UploadFileName, getColumnName(tmpRow, 'rowNumber'), null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 25, 2)

    def headerMapping = [
            (xml.row[0].cell[0]) : getColumnName(tmpRow, 'rowNumber'),
            (xml.row[0].cell[2]) : getColumnName(tmpRow, 'codeOKATO'),
            (xml.row[0].cell[3]) : getColumnName(tmpRow, 'regionName'),
            (xml.row[0].cell[4]) : getColumnName(tmpRow, 'tsTypeCode'),
            (xml.row[0].cell[5]) : getColumnName(tmpRow, 'tsType'),
            (xml.row[0].cell[6]) : getColumnName(tmpRow, 'model'),
            (xml.row[0].cell[7]) : getColumnName(tmpRow, 'ecoClass'),
            (xml.row[0].cell[8]) : getColumnName(tmpRow, 'identNumber'),
            (xml.row[0].cell[9]) : getColumnName(tmpRow, 'regNumber'),
            (xml.row[0].cell[10]): getColumnName(tmpRow, 'regDate'),
            (xml.row[0].cell[11]): getColumnName(tmpRow, 'regDateEnd'),
            (xml.row[0].cell[12]): getColumnName(tmpRow, 'taxBase'),
            (xml.row[0].cell[13]): getColumnName(tmpRow, 'baseUnit'),
            (xml.row[0].cell[14]): getColumnName(tmpRow, 'year'),
            (xml.row[0].cell[15]): getColumnName(tmpRow, 'pastYear'),
            (xml.row[0].cell[16]): 'Сведения об угоне',
            (xml.row[1].cell[16]): 'Дата начала розыска ТС',
            (xml.row[1].cell[17]): 'Дата возврата ТС',
            (xml.row[0].cell[18]): getColumnName(tmpRow, 'share'),
            (xml.row[0].cell[19]): getColumnName(tmpRow, 'costOnPeriodBegin'),
            (xml.row[0].cell[20]): getColumnName(tmpRow, 'costOnPeriodEnd'),
            (xml.row[0].cell[21]): getColumnName(tmpRow, 'benefitStartDate'),
            (xml.row[0].cell[22]): getColumnName(tmpRow, 'benefitEndDate'),
            (xml.row[0].cell[23]): getColumnName(tmpRow, 'taxBenefitCode'),
            (xml.row[0].cell[24]): getColumnName(tmpRow, 'base'),
            (xml.row[0].cell[25]): getColumnName(tmpRow, 'version'),
            (xml.row[2].cell[0]) : '1'
    ]

    (2..25).each { index ->
        headerMapping.put((xml.row[2].cell[index]), index.toString())
    }
    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def xmlIndexRow = -1 // Строки xml, от 0
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    def int rowIndex = 1  // Строки НФ, от 1
    def sectionNumber = 0
    def sectionAlias = null
    def mapRows = [:]

    def dTo = getReportPeriodEndDate()
    def regionId = formDataDepartment.regionId

    for (def row : xml.row) {
        xmlIndexRow++
        def int xlsIndexRow = xmlIndexRow + rowOffset

        // Пропуск строк шапки
        if (xmlIndexRow <= headRowCount) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        // если это начало раздела, то запомнить его название и обрабатывать следующую строку
        def firstValue = row.cell[1].text()
        if (firstValue != null && firstValue != '') {
            sectionAlias = sections[sectionNumber]
            sectionNumber++
            mapRows.put(sectionAlias, [])
            continue
        }

        def newRow = getNewRow()
        newRow.setIndex(rowIndex++)
        newRow.setImportIndex(xlsIndexRow)

        def int xmlIndexCol = 1

        // графа 1
        xmlIndexCol++

        // графа 2 - атрибут 840 - CODE - «Код», справочник 96 «Общероссийский классификатор территорий муниципальных образований»
        def record = getRecordImport(96, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        newRow.codeOKATO = record?.record_id?.value
        xmlIndexCol++

        // графа 3 - зависит от графы 2 - атрибут 841 - NAME - «Наименование», справочник 96 «Общероссийский классификатор территорий муниципальных образований»
        if (record != null) {
            formDataService.checkReferenceValue(96, row.cell[xmlIndexCol].text(), record?.NAME?.value, xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        }
        xmlIndexCol++

        // графа 4 - атрибут 422 - CODE - «Код вида ТС», справочник 42 «Коды видов транспортных средств»
        // http://jira.aplana.com/browse/SBRFACCTAX-8572 исправить загрузку Кода Вида ТС (убираю пробелы)
        record = getRecordImport(42, 'CODE', row.cell[xmlIndexCol].text().replace(' ', ''), xlsIndexRow, xmlIndexCol + colOffset, true)
        newRow.tsTypeCode = record?.record_id?.value
        xmlIndexCol++

        // графа 5 - зависит от графы 4 - атрибут 423 - NAME - «Наименование вида транспортного средства», справочник 42 «Коды видов транспортных средств»
        if (record != null) {
            formDataService.checkReferenceValue(42, row.cell[xmlIndexCol].text(), record?.NAME?.value, xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        }
        xmlIndexCol++

        // графа 6
        newRow.model = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 7
        newRow.ecoClass = getRecordIdImport(40, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 8
        newRow.identNumber = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 9
        newRow.regNumber = row.cell[xmlIndexCol].text().replace(' ', '')
        xmlIndexCol++

        // графа 10
        newRow.regDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 11
        newRow.regDateEnd = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 12
        newRow.taxBase = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 13 - атрибут 57 - CODE - «Код единицы измерения», справочник 12 «Коды единиц измерения налоговой базы на основании ОКЕИ»
        newRow.baseUnit = getRecordIdImport(12, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 14
        def yearStr = row.cell[xmlIndexCol].text()
        if (yearStr != null) {
            if (yearStr.contains(".")) {
                newRow.year = parseDate(yearStr, "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            } else {
                def yearNum = parseNumber(yearStr, xlsIndexRow, xmlIndexCol + colOffset, logger, true)
                if (yearNum != null && yearNum != 0) {
                    newRow.year = new GregorianCalendar(yearNum as Integer, Calendar.JANUARY, 1).getTime()
                }
            }
        }
        xmlIndexCol++

        // графа 15
        newRow.pastYear = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 16
        newRow.stealDateStart = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 17
        newRow.stealDateEnd = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 18
        newRow.share = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 19
        newRow.costOnPeriodBegin = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 20
        newRow.costOnPeriodEnd = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 21
        newRow.benefitStartDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 22
        newRow.benefitEndDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 23 - атрибут 19 - TAX_BENEFIT_ID - «Код налоговой льготы», справочник 7 «Параметры налоговых льгот транспортного налога»
        if (row.cell[xmlIndexCol].text()) {
            def recordId = getRecordIdImport(6L, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
            def filter = "TAX_BENEFIT_ID = $recordId and DECLARATION_REGION_ID = $regionId"
            def columnName = getColumnName(newRow, 'taxBenefitCode')
            newRow.taxBenefitCode = getRefBookRecordIdImport(7L, dTo, filter, columnName, xlsIndexRow, xmlIndexCol + colOffset, false)
        }
        xmlIndexCol++

        // графа 24
        newRow.base = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 25 - атрибут 2082 - MODEL - «Модель (версия)», справочник 208 «Средняя стоимость транспортных средств»
        newRow.version = getRecordIdImport(208L, 'MODEL', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        mapRows[sectionAlias].add(newRow)
    }

    deleteNotFixedRows(dataRows)

    // копирование данных по разделам
    sections.each { section ->
        def copyRows = mapRows[section]
        if (copyRows != null && !copyRows.isEmpty()) {
            def insertIndex = getDataRow(dataRows, section).getIndex()
            dataRows.addAll(insertIndex, copyRows)
            // поправить индексы, потому что они после вставки не пересчитываются
            updateIndexes(dataRows)
        }
    }

    dataRowHelper.save(dataRows)
}

void importTransportData() {
    int COLUMN_COUNT = 25
    int TOTAL_ROW_COUNT = 0
    int ROW_MAX = 1000
    def DEFAULT_CHARSET = "cp866"
    char SEPARATOR = '|'
    char QUOTE = '\0'

    checkBeforeGetXml(ImportInputStream, UploadFileName)

    if (!UploadFileName.endsWith(".rnu")) {
        logger.error(WRONG_RNU_FORMAT)
    }

    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    deleteExtraRows(dataRows)
    dataRowHelper.save(dataRows)

    InputStreamReader isr = new InputStreamReader(ImportInputStream, DEFAULT_CHARSET)
    CSVReader reader = new CSVReader(isr, SEPARATOR, QUOTE)

    String[] rowCells
    int countEmptyRow = 0	// количество пустых строк
    int fileRowIndex = 0    // номер строки в файле
    int rowIndex = 0        // номер строки в НФ
    int totalRowCount = 0   // счетчик кол-ва итогов
    def total = null		// итоговая строка со значениями из тф для добавления
    def mapRows = [:]

    while ((rowCells = reader.readNext()) != null) {
        fileRowIndex++

        def isEmptyRow = (rowCells.length == 1 && rowCells[0].length() < 1)
        if (isEmptyRow) {
            if (countEmptyRow > 0) {
                // если встретилась вторая пустая строка, то дальше только строки итогов и ЦП
                totalRowCount++
                // итоговая строка тф
                total = getNewRow(reader.readNext(), COLUMN_COUNT, ++fileRowIndex, ++rowIndex)
                break
            }
            countEmptyRow++
            continue
        }

        // если еще не было пустых строк, то это первая строка - заголовок (пропускается)
        // обычная строка
        if (countEmptyRow != 0 && !addRow(mapRows, rowCells, COLUMN_COUNT, fileRowIndex, ++rowIndex)) {
            break
        }

        // периодически сбрасываем строки
        if (getNewRowCount(mapRows) > ROW_MAX) {
            insertRows(dataRowHelper, mapRows)
            mapRows.clear()
        }
    }
    reader.close()

    // проверка итоговой строки
    if (TOTAL_ROW_COUNT != 0 && totalRowCount != TOTAL_ROW_COUNT) {
        logger.error(ROW_FILE_WRONG, fileRowIndex)
    }

    if (getNewRowCount(mapRows) != 0) {
        insertRows(dataRowHelper, mapRows)
    }
}

/** Добавляет строку в текущий буфер строк. */
boolean addRow(def mapRows, String[] rowCells, def columnCount, def fileRowIndex, def rowIndex) {
    if (rowCells == null) {
        return true
    }
    def newRow = getNewRow(rowCells, columnCount, fileRowIndex, rowIndex)
    if (newRow == null) {
        return false
    }
    // определить раздел по техническому полю и добавить строку в нужный раздел
    sectionIndex = pure(rowCells[26])
    if (mapRows[sectionIndex] == null) {
        mapRows[sectionIndex] = []
    }
    mapRows[sectionIndex].add(newRow)
    return true
}

/**
 * Получить новую строку нф по строке из тф (*.rnu).
 *
 * @param rowCells список строк со значениями
 * @param columnCount количество колонок
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 *
 * @return вернет строку нф или null, если количество значений в строке тф меньше
 */
def getNewRow(String[] rowCells, def columnCount, def fileRowIndex, def rowIndex) {
    def newRow = getNewRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    if (rowCells.length != columnCount + 2) {
        rowError(logger, newRow, String.format(ROW_FILE_WRONG, fileRowIndex))
        return null
    }

    def int colOffset = 1
    def int colIndex = 1

    def regionId = formDataDepartment.regionId

    // графа 1
    colIndex++

    // графа 2 - атрибут 840 - CODE - «Код», справочник 96 «Общероссийский классификатор территорий муниципальных образований»
    def record = getRecordImport(96, 'CODE', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)
    newRow.codeOKATO = record?.record_id?.value
    colIndex++

    // графа 3 - зависит от графы 2 - атрибут 841 - NAME - «Наименование», справочник 96 «Общероссийский классификатор территорий муниципальных образований»
    if (record != null) {
        formDataService.checkReferenceValue(96, pure(rowCells[colIndex]), record?.NAME?.value, fileRowIndex, colIndex + colOffset, logger, false)
    }
    colIndex++

    // графа 4 - атрибут 422 - CODE - «Код вида ТС», справочник 42 «Коды видов транспортных средств»
    // http://jira.aplana.com/browse/SBRFACCTAX-8572 исправить загрузку Кода Вида ТС (убираю пробелы)
    record = getRecordImport(42, 'CODE', pure(rowCells[colIndex]).replace(' ', ''), fileRowIndex, colIndex + colOffset, false)
    newRow.tsTypeCode = record?.record_id?.value
    colIndex++

    // графа 5 - зависит от графы 4 - атрибут 423 - NAME - «Наименование вида транспортного средства», справочник 42 «Коды видов транспортных средств»
    if (record != null) {
        formDataService.checkReferenceValue(42, pure(rowCells[colIndex]), record?.NAME?.value, fileRowIndex, colIndex + colOffset, logger, false)
    }
    colIndex++

    // графа 6
    newRow.model = pure(rowCells[colIndex])
    colIndex++

    // графа 7
    newRow.ecoClass = getRecordIdImport(40, 'CODE', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 8
    newRow.identNumber = pure(rowCells[colIndex])
    colIndex++

    // графа 9
    newRow.regNumber = pure(rowCells[colIndex]).replace(' ', '')
    colIndex++

    // графа 10
    newRow.regDate = parseDate(pure(rowCells[colIndex]), "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 11
    newRow.regDateEnd = parseDate(pure(rowCells[colIndex]), "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 12
    newRow.taxBase = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 13 - атрибут 57 - CODE - «Код единицы измерения», справочник 12 «Коды единиц измерения налоговой базы на основании ОКЕИ»
    newRow.baseUnit = getRecordIdImport(12, 'CODE', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 14
    newRow.year = parseDate(pure(rowCells[colIndex]), "yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 15
    newRow.pastYear = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 16
    newRow.stealDateStart = parseDate(pure(rowCells[colIndex]), "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 17
    newRow.stealDateEnd = parseDate(pure(rowCells[colIndex]), "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 18
    newRow.share = pure(rowCells[colIndex])
    colIndex++

    // графа 19
    newRow.costOnPeriodBegin = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 20
    newRow.costOnPeriodEnd = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 21
    newRow.benefitStartDate = parseDate(pure(rowCells[colIndex]), "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 22
    newRow.benefitEndDate = parseDate(pure(rowCells[colIndex]), "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 23 - атрибут 19 - TAX_BENEFIT_ID - «Код налоговой льготы», справочник 7 «Параметры налоговых льгот транспортного налога»
    if (pure(rowCells[colIndex])) {
        def recordId = getRecordIdImport(6L, 'CODE', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)
        if(recordId != null) {
            String filter = "TAX_BENEFIT_ID = $recordId and DECLARATION_REGION_ID = $regionId"
            String columnName = getColumnName(newRow, 'taxBenefitCode')
            newRow.taxBenefitCode = getRefBookRecordIdImport(7L, getReportPeriodEndDate(), filter, columnName, fileRowIndex, colIndex + colOffset, false)
        }
    }
    colIndex++

    // графа 24
    newRow.base = pure(rowCells[colIndex])
    colIndex++

    // графа 25 - атрибут 2082 - MODEL - «Модель (версия)», справочник 208 «Средняя стоимость транспортных средств»
    newRow.version = getRecordIdImport(208L, 'MODEL', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)

    return newRow
}

String pure(String cell) {
    return StringUtils.cleanString(cell)?.intern()
}

// Удалить нефиксированные строки
void deleteExtraRows(def dataRows) {
    def deleteRows = []
    dataRows.each { row ->
        if (!(row.getAlias() in ['A', 'B', 'C'])) {
            deleteRows.add(row)
        }
    }
    if (!deleteRows.isEmpty()) {
        dataRows.removeAll(deleteRows)
        updateIndexes(dataRows)
    }
}

/** Получить количество новых строк в мапе во всех разделах. */
def getNewRowCount(def mapRows) {
    return mapRows.entrySet().sum { entry -> entry.value.size() }
}

/** Вставить данные в нф по разделам. */
def insertRows(def dataRowHelper, def mapRows) {
    sections.each { section ->
        def copyRows = mapRows[section]
        if (copyRows != null && !copyRows.isEmpty()) {
            def dataRows = dataRowHelper.allCached
            def insertIndex = getLastRowIndexInSection(dataRows, section)
            dataRowHelper.insert(copyRows, insertIndex + 1)

            // поправить индексы, потому что они после вставки не пересчитываются
            updateIndexes(dataRows)
        }
    }
}

// Сортировка групп и строк
void sortFormDataRows() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    for (def section : sections) {
        def firstRow = getDataRow(dataRows, section)
        def from = firstRow.getIndex()
        def to = getLastRowIndexInSection(dataRows, section)
        def sectionRows = (from < to ? dataRows[from..(to - 1)] : [])

        // Массовое разыменовывание граф НФ
        def columnNameList = firstRow.keySet().collect { firstRow.getCell(it).getColumn() }
        refBookService.dataRowsDereference(logger, sectionRows, columnNameList)

        sortRowsSimple(sectionRows)
    }
    dataRowHelper.saveSort()
}

/**
 * Получить номер последнего элемента раздела.
 *
 * @param dataRows все строки
 * @param section алиас раздела
 */
def getLastRowIndexInSection(def dataRows, def section) {
    if (section == 'A') {
        return getDataRow(dataRows, 'B').getIndex() - 1
    } else if (section == 'B') {
        return getDataRow(dataRows, 'C').getIndex() - 1
    } else {
        dataRows.get(dataRows.size() - 1).getIndex()
    }
}

// Поправить индексы.
void updateIndexes(def dataRows) {
    dataRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
}

// Удалить нефиксированные строки
void deleteNotFixedRows(def dataRows) {
    def deleteRows = dataRows.findAll { row -> row.getAlias() == null }
    if (!deleteRows.isEmpty()) {
        dataRows.removeAll(deleteRows)
        updateIndexes(dataRows)
    }
}

/**
 * Копировать заданный диапозон строк из источника в приемник.
 *
 * @param sourceDataRows строки источника
 * @param destinationDataRows строки приемника
 * @param section псевдоним строки с которой копировать строки (НЕ включительно)
 */
void copyRows(def sourceDataRows, def destinationDataRows, def section) {
    def from = getDataRow(sourceDataRows, section).getIndex()
    def to = getLastRowIndexInSection(sourceDataRows, section)
    if (from >= to) {
        return
    }
    def copyRows = sourceDataRows.subList(from, to)
    destinationDataRows.addAll(from, copyRows)
    // поправить индексы, потому что они после вставки не пересчитываются
    updateIndexes(destinationDataRows)
}

/**
 * Получить "Код вида ТС" родителя.
 *
 * @param tsTypeCode id на справочник 42 «Коды видов транспортных средств»
 */
def getParentTsTypeCode(def tsTypeCode) {
    // справочник 42 «Коды видов транспортных средств»
    def ids = getProvider(42L).getParentsHierarchy(tsTypeCode)
    if (ids != null && !ids.isEmpty()) {
        return getRefBookValue(42L, ids.get(0))?.CODE?.value
    }
    return null
}

def getProvider(def id) {
    return formDataService.getRefBookProvider(refBookFactory, id, providerCache)
}

@Field
String REF_BOOK_NOT_FOUND_IMPORT_ERROR_NEW = "Проверка файла: Строка %d, столбец %s: В справочнике «%s» не найдена запись для графы «%s» актуальная на дату %s!";

@Field
String REF_BOOK_TOO_MANY_FOUND_IMPORT_ERROR_NEW = "Проверка файла: Строка %d, столбец %s: В справочнике «%s» найдено более одной записи для графы «%s» актуальная на дату %s!";

/**
 * Получить id записи из справочника по фильтру.
 * Не используется унифицированный метод formDataService.getRefBookRecordIdImport потому что в нем нет возможности
 * искать запись по фильтру.
 *
 * @param refBookId идентификатор справочника
 * @param date дата актуальности записи
 * @param filter фильтр для поиска
 * @param columnName название графы формы для которого ищется значение
 * @param rowIndex номер строки в файле
 * @param colIndex номер колонки в файле
 * @param required фатальность
 */
Long getRefBookRecordIdImport(Long refBookId, Date date, String filter, String columnName,
                              int rowIndex, int colIndex, boolean required) {
    if (refBookId == null) {
        return null
    }
    def records = getProvider(refBookId).getRecords(date, null, filter, null)
    if (records != null && records.size() == 1) {
        return records.get(0).record_id.value
    }

    def tooManyValue = (records != null && records.size() > 1)
    RefBook rb = refBookFactory.get(refBookId)

    String msg = String.format(tooManyValue ? REF_BOOK_TOO_MANY_FOUND_IMPORT_ERROR_NEW : REF_BOOK_NOT_FOUND_IMPORT_ERROR_NEW,
            rowIndex, getXLSColumnName(colIndex), rb.getName(), columnName, date.format('dd.MM.yyyy'))
    if (required) {
        throw new ServiceException("%s", msg)
    } else {
        logger.warn("%s", msg)
    }
    return null
}

// Проверка заполнения атрибута «Регион» подразделения текущей формы (справочник «Подразделения»)
void checkRegionId() {
    if (formDataDepartment.regionId == null) {
        throw new Exception("Атрибут «Регион» подразделения текущей налоговой формы не заполнен (справочник «Подразделения»)!")
    }
}

def loggerError(def row, def msg) {
    if (isBalancePeriod()) {
        rowWarning(logger, row, msg)
    } else {
        rowError(logger, row, msg)
    }
}
