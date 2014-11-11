package form_template.transport.vehicles_1.v2014

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import groovy.transform.Field

/**
 * Сведения о транспортных средствах, по которым уплачивается транспортный налог (new)
 * formTypeId=10704
 * formTemplateId=11762
 *
 * форма действует с 1 октября 2014
 *
 * TODO:
 *      - графа 25 - указать правильный справочник, после того как его добавят http://jira.aplana.com/browse/SBRFACCTAX-9316
 *      - включить логическую проверку 10 после того как будут добавлен справочник «Параметры представления деклараций по транспортному налогу» http://jira.aplana.com/browse/SBRFACCTAX-9316
 *      - включить логическую проверку 14 после того как будут дополнен справочник «Параметры налоговых льгот транспортного налога» http://jira.aplana.com/browse/SBRFACCTAX-9316
 *      - включить логическую проверку 16 после того как будет добавлен справочник «Повышающие коэффициенты транспортного налога» http://jira.aplana.com/browse/SBRFACCTAX-9316
 *      - включить логические проверки 17, 18, 19 после того как будет добавлен справочник «Средняя стоимость транспортных средств» http://jira.aplana.com/browse/SBRFACCTAX-9316
 *      - при импорте поправить графу 25, после того как будет добавлен справочник «Средняя стоимость транспортных средств» http://jira.aplana.com/browse/SBRFACCTAX-9316
 *      - при импорте не возможно получить значение для графы 23 http://jira.aplana.com/browse/SBRFACCTAX-9436
 *      - отладить метод copyData() при создании формы
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
                                // TODO (Ramil Timerbaev) справочник «Средняя стоимость транспортных средств» еще не добавлен http://jira.aplana.com/browse/SBRFACCTAX-9316
// графа 25 - version           - атрибут NNN - NAME - «Модель (версия)», справочник NN «Средняя стоимость транспортных средств»

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.AFTER_CREATE:
        if (formData.kind == FormDataKind.PRIMARY) {
            copyData()
        }
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
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
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


@Field
def copyColumns202 = ['taxBenefitCode', 'benefitStartDate', 'benefitEndDate']

// общее графы между фомрами 201 и 22
@Field
def commonBetween201and202 = ['codeOKATO', 'identNumber', 'regNumber', 'baseUnit']


// Соответствие задается совпадением граф 2, 4, 8, 12, 13
@Field
def columnsForEquals = ['codeOKATO', 'tsTypeCode', 'identNumber', 'taxBase', 'baseUnit']

//// Обертки методов

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
    return record.SECTION.value.padLeft(4, '0') + record.ITEM.value.padLeft(4, '0') + record.SUBITEM.value.padLeft(4, '0')
}

def logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def reportPeriod = getReportPeriod()
    def dFrom = getReportPeriodStartDate()
    def dTo = getReportPeriodEndDate()
    def String dFormat = "dd.MM.yyyy"

    // Проверенные строки (5-ая провека)
    def List<DataRow<Cell>> checkedRows = new ArrayList<DataRow<Cell>>()

    // регион из подразделения формы для проверки 10
    def regionId = getRefBookValue(30L, formData.departmentId)?.REGION_ID

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
        checkNonEmptyColumns(row, index ?: 0, nonEmptyColumns, logger, true)

        // 2. Проверка на соответствие дат при постановке (снятии) с учёта
        if (!(row.regDateEnd == null || (row.regDate != null && row.regDateEnd >= row.regDate))) {
            rowError(logger, row, errorMsg + 'Дата постановки (снятия) с учёта неверная!')
        }

        // 3. Проврека на наличие даты угона при указании даты возврата
        if (row.stealDateEnd != null && row.stealDateStart == null) {
            rowError(logger, row, errorMsg + 'Не заполнено поле «Дата угона»!')
        }

        // 4. Проверка на соответствие дат сведений об угоне
        if (row.stealDateStart != null && row.stealDateEnd != null && row.stealDateEnd < row.stealDateStart) {
            rowError(logger, row, errorMsg + 'Дата возврата ТС неверная!')
        }

        // 5. Проверка на наличие в списке ТС строк, для которых графы codeOKATO, identNumber, regNumber одинаковы
        if (!checkedRows.contains(row)) {
            def errorRows = ''
            for (def rowIn in dataRows) {
                if (rowIn.getAlias() == null && !checkedRows.contains(rowIn) && row != rowIn && isEquals(row, rowIn, columnsForEquals)) {
                    checkedRows.add(rowIn)
                    errorRows = ', ' + rowIn.getIndex()
                }
            }
            if (!''.equals(errorRows)) {
                rowError(logger, row, "Обнаружены строки $index$errorRows, у которых " +
                        "Код ОКТМО = ${getRefBookValue(96L, row.codeOKATO)?.CODE?.stringValue}, " +
                        "Код вида ТС = ${getRefBookValue(42L, row.tsTypeCode)?.CODE?.stringValue}, " +
                        "Идентификационный номер ТС = $row.identNumber, " +
                        "Налоговая база = $row.taxBase, " +
                        "Единица измерения налоговой базы по ОКЕИ = ${getRefBookValue(12L, row.baseUnit)?.CODE?.value} " +
                        "совпадают!")
            }
        }
        checkedRows.add(row)

        // 6. Проверка на наличие в списке ТС строк, период владения которых не пересекается с отчётным
        if (row.regDate != null && row.regDate > dTo || row.regDateEnd != null && row.regDateEnd < dFrom) {
            rowError(logger, row, errorMsg + 'Период регистрации ТС ('
                    + row.regDate.format(dFormat) + ' - ' + ((row.regDateEnd != null) ? row.regDateEnd.format(dFormat) : '...') + ') ' +
                    ' не пересекается с периодом (' + dFrom.format(dFormat) + " - " + dTo.format(dFormat) +
                    '), за который сформирована налоговая форма!')
        }

        // 8. Проверка года изготовления ТС
        if (row.year != null) {
            Calendar calenadarMake = Calendar.getInstance()
            calenadarMake.setTime(row.year)
            if (calenadarMake.get(Calendar.YEAR) > reportPeriod.taxPeriod.year) {
                rowError(logger, row, errorMsg + 'Год изготовления ТС не может быть больше отчетного года!')
            }
        }

        // 9. Проверка корректности заполнения «Графы 15»
        if (row.pastYear != calc15(row, reportPeriod.taxPeriod.year)) {
            def columnName = getColumnName(row, 'pastYear')
            rowError(logger, row, errorMsg + "Графа «$columnName» заполнена неверно!")
        }

        if (false) {
            def records = null
            if (row.codeOKATO != null) {
                // TODO (Ramil Timerbaev) справочник «Параметры представления деклараций по транспортному налогу» еще не добавлен http://jira.aplana.com/browse/SBRFACCTAX-9316
                // 10. Проверка наличия параметров представления декларации для кода ОКТМО
                def filter = "КодСубъектаРФпредставителяДекларации = $regionId and КодОКТМО = $row.codeOKATO"
                // справочник «Параметры представления деклараций по транспортному налогу»
                records = getProovider(0L).getRecords(dTo, null, filter, null);
                if (records == null || records.isEmpty()) {
                    rowError(logger, row, errorMsg + "Для выбранного кода ОКТМО отсутствует запись в справочнике «Параметры представления деклараций по транспортному налогу»!")
                }
            }

            // 14. Проверка допустимых значений «Графы 23»
            // TODO (Ramil Timerbaev) включить логическую проверку 14 после того как будут добавлен дополнен справочник «Параметры налоговых льгот транспортного налога» http://jira.aplana.com/browse/SBRFACCTAX-9316
            // Атрибут «Код субъекта РФ» равен хотя бы одному из значений, определенному согласно алгоритму раздела 1.1.1.5.4.1;
            if (row.codeOKATO != null && row.taxBenefitCode != null && records != null && !records.isEmpty()) {
                def oktmo = records.collect { it.кодОКТМО.value }.join(', ')
                def filter = "КодСубъектаРФпредставителяДекларации = $regionId and " +
                        "TAX_BENEFIT_ID in ('30200', '20200', '20210', '20220', '20230') and " +
                        "КодСубъектаРФ in ($oktmo)"
                // справочник «Параметры налоговых льгот транспортного налога»
                def records7 =  getProovider(7L).getRecords(dTo, null, filter, null);
                if (records7 == null || records7.isEmpty()) {
                    def columnName = getColumnName(row, 'taxBenefitCode')
                    rowError(logger, row, errorMsg + "Графа «$columnName» заполнена неверно!")
                }
            }
        }

        // 11. Поверка на соответствие дат использования льготы
        if (row.benefitEndDate != null && row.benefitStartDate != null && row.benefitEndDate < row.benefitStartDate) {
            rowError(logger, row, errorMsg + "Неверно указаны даты начала и окончания использования льготы!")
        }

        // 12. Проверка на заполнение периода использования льготы при указании кода налоговой льготы
        if (row.taxBenefitCode != null && row.benefitStartDate == null) {
            rowError(logger, row, errorMsg + "При указании кода налоговой льготы должен быть заполнен период использования льготы!")
        }

        // 13. Проверка на наличие в списке ТС строк, период использования льготы, которых, не пересекается с отчётным...
        if (row.benefitStartDate != null && row.benefitStartDate > dTo || row.benefitEndDate != null && row.benefitEndDate < dFrom) {
            rowError(logger, row, errorMsg + "Период использования льготы ТС ($row.benefitStartDate - $row.benefitEndDate) не пересекается с периодом ($dFrom - $dTo), за который сформирована налоговая форма!")
        }

        // 15. Проверка корректности заполнения «Графы 24»
        if (row.base != calc24(row)) {
            def columnName = getColumnName(row, 'base')
            rowError(logger, row, errorMsg + "Графа «$columnName» заполнена неверно!")
        }

        // TODO (Ramil Timerbaev) включить логическую проверку 16 после того как будет добавлен справочники «Средняя стоимость транспортных средств» и «Повышающие коэффициенты транспортного налога» http://jira.aplana.com/browse/SBRFACCTAX-9316
        if (false) {
            // 16. Проверка наличия повышающего коэффициента для ТС дороже 3 млн. руб.
            if (row.version != null) {
                def averageCost = getRefBookValue(NN, row.version)?.СредняяСтоимость?.value
                def filter = "СредняяСтоимость = $averageCost"
                // справочник «Повышающие коэффициенты транспортного налога»
                def records = getProovider(0L).getRecords(dTo, null, filter, null); // TODO (Ramil Timerbaev)
                if (records == null || !records.isEmpty()) {
                    rowError(logger, row, errorMsg + "Для средней стоимости выбранной модели (версии) из перечня, утвержденного на налоговый период, отсутствует запись в справочнике «Повышающие коэффициенты транспортного налога»!")
                }
            }
        }

        // 17. Проверка кода вида ТС (графа 4) по разделу «Наземные транспортные средства»
        // 18. Проверка кода вида ТС (графа 4) по разделу «Водные транспортные средства»
        // 19. Проверка кода вида ТС (графа 4) по разделу «Воздушные транспортные средства»
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
                rowError(logger, row, errorMsg + "Графа «$columnName» заполнена неверно!")
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

def copyRow201(def row) {
    def newRow = getNewRow()
    copyColumns201.each {
        newRow.getCell(it).setValue(row.getCell(it).value, null)
    }
    return newRow
}

// Получить новую строку с заданными стилями.
def getNewRow() {
    def newRow = formData.createDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).styleAlias = 'Редактируемая'
    }
    autoFillColumns.each {
        newRow.getCell(it).styleAlias = 'Автозаполняемая'
    }
    return newRow
}

// Получить строки для копирования за предыдущий отчетный период
def getPrevRowsForCopy(def reportPeriod, def dataRows) {
    if (reportPeriod != null) {
        def formDataPrev = formDataService.getLast(formData.formType.id, formData.kind, formDataDepartment.id, reportPeriod.id, reportPeriod.order)
        def dataRowsPrev = (formDataPrev != null ? formDataService.getDataRowHelper(formDataPrev)?.allCached : null)
        if (dataRowsPrev != null && !dataRowsPrev.isEmpty()) {
            dataRows = copyFromOursForm(dataRows, dataRowsPrev)
        } else {
            def formData201Old = formDataService.getLast(201, formData.kind, formDataDepartment.id, reportPeriod.id, reportPeriod.order)
            def dataRows201Old = (formData201Old != null ? formDataService.getDataRowHelper(formData201Old)?.allCached : null)

            def formData202Old = formDataService.getLast(202, formData.kind, formDataDepartment.id, reportPeriod.id, reportPeriod.order)
            def dataRows202Old = (formData202Old != null ? formDataService.getDataRowHelper(formData202Old)?.allCached : null)

            if (dataRows201Old != null && !dataRows201Old.isEmpty()) {
                dataRows = copyFromOldForm(dataRows, dataRows201Old, dataRows202Old)
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
        for (def rowOld in rowsOld) {
            if (isEquals(row, rowOld, columnsForEquals)) {
                need = false
                break
            }
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
 * @param dataRows202Old строки предыдущей формы 202
 */
def copyFromOldForm(def dataRows, dataRows201Old, dataRows202Old) {
    def rowsOld = []
    rowsOld.addAll(dataRows)

    def dFrom = getReportPeriodStartDate()
    def dTo = getReportPeriodEndDate()

    def section = null
    def sectionRows = [:]
    sections.each {
        sectionRows[it] = []
    }

    def sectionMap = ['50000' : 'A', '40200' : 'B', '40100' : 'C']

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
        for (def rowOld in rowsOld) {
            if (isEquals(row, rowOld, ['codeOKATO', 'identNumber', 'baseUnit'])) {
                need = false
                break
            }
        }
        if (need) {
            row.setIndex(rowsOld.size())
            newRow = copyRow(row, copyColumns201)
            def tsTypeCode = getParentTsTypeCode(row.tsTypeCode)
            sectionRows[sectionMap[tsTypeCode]].add(newRow)
            rowsOld.add(newRow)
        }
    }
    sections.each {
        dataRows.addAll(getLastRowIndexInSection(dataRows, it), sectionRows[it])
        updateIndexes(dataRows)
    }

    // получение данных из 202
    for (def row : dataRows202Old) {
        if ((row.benefitEndDate != null && row.benefitEndDate < dFrom) || (row.benefitStartDate > dTo)) {
            continue
        }

        // эта часть вроде как лишняя
        def benefitEndDate = row.benefitEndDate
        if (benefitEndDate == null || benefitEndDate > dTo) {
            benefitEndDate = dTo
        }
        def benefitStartDate = row.benefitStartDate
        if (benefitStartDate < dFrom) {
            benefitStartDate = dFrom
        }
        if (benefitStartDate > dTo || benefitEndDate < dFrom) {
            continue
        }

        // находим среди заполненых строк соответствующие тукущей строке
        for (def rowOur in dataRows) {
            if (isEquals(row, rowOur, commonBetween201and202)) {
                // строка из 201 соответствует строке из 202
                copyRow(row, copyColumns202)
                copyColumns202.each {
                    rowOur.getCell(it).setValue(row.getCell(it).value, null)
                }
                break
            }
        }
    }
    return dataRows
}


def isEquals(def row1, def row2, def columns) {
    for (def column : columns) {
        if (row1[column] != null && row1[column].equals(row2[column])) {
            return false
        }
    }
    return true
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
            (xml.row[1].cell[16]): 'Дата начала розыска',
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
            formDataService.checkReferenceValue(96, row.cell[xmlIndexCol].text(), record?.NAME?.value, xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        }
        xmlIndexCol++

        // графа 4 - атрибут 422 - CODE - «Код вида ТС», справочник 42 «Коды видов транспортных средств»
        // http://jira.aplana.com/browse/SBRFACCTAX-8572 исправить загрузку Кода Вида ТС (убираю пробелы)
        record = getRecordImport(42, 'CODE', row.cell[xmlIndexCol].text().replace(' ', ''), xlsIndexRow, xmlIndexCol + colOffset, true)
        newRow.tsTypeCode = record?.record_id?.value
        xmlIndexCol++

        // графа 5 - зависит от графы 4 - атрибут 423 - NAME - «Наименование вида транспортного средства», справочник 42 «Коды видов транспортных средств»
        if (record != null) {
            formDataService.checkReferenceValue(42, row.cell[xmlIndexCol].text(), record?.NAME?.value, xlsIndexRow, xmlIndexCol + colOffset, logger, true)
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

        // TODO (Ramil Timerbaev) вернуть когда поправится баг http://jira.aplana.com/browse/SBRFACCTAX-9436
        // графа 23 - атрибут 19 - TAX_BENEFIT_ID - «Код налоговой льготы», справочник 7 «Параметры налоговых льгот транспортного налога»
        // def recordId = getRecordIdImport(6, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        // newRow.taxBenefitCode = getRecordIdImport(7, 'TAX_BENEFIT_ID', recordId.toString(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 24
        newRow.base = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // TODO (Ramil Timerbaev) вернуть когда будет готов справочник «Средняя стоимость транспортных средств» http://jira.aplana.com/browse/SBRFACCTAX-9316
        // графа 25 - атрибут NNN - NAME - «Модель (версия)», справочник NN «Средняя стоимость транспортных средств»
        // newRow.version = getRecordIdImport(NN, 'NAME', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

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
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 25, 0)
    addTransportData(xml)
}

void addTransportData(def xml) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def int rnuIndexRow = 2
    def int colOffset = 1
    def rows = []
    def sectionNumber = 0
    def mapRows = [:]

    for (def row : xml.row) {
        rnuIndexRow++
        def int xlsIndexRow = rnuIndexRow + rowOffset

        // Пропуск строк шапки
        if (rnuIndexRow <= headRowCount) {
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
            formDataService.checkReferenceValue(96, row.cell[xmlIndexCol].text(), record?.NAME?.value, xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        }
        xmlIndexCol++

        // графа 4 - атрибут 422 - CODE - «Код вида ТС», справочник 42 «Коды видов транспортных средств»
        // http://jira.aplana.com/browse/SBRFACCTAX-8572 исправить загрузку Кода Вида ТС (убираю пробелы)
        record = getRecordImport(42, 'CODE', row.cell[xmlIndexCol].text().replace(' ', ''), xlsIndexRow, xmlIndexCol + colOffset, true)
        newRow.tsTypeCode = record?.record_id?.value
        xmlIndexCol++

        // графа 5 - зависит от графы 4 - атрибут 423 - NAME - «Наименование вида транспортного средства», справочник 42 «Коды видов транспортных средств»
        if (record != null) {
            formDataService.checkReferenceValue(42, row.cell[xmlIndexCol].text(), record?.NAME?.value, xlsIndexRow, xmlIndexCol + colOffset, logger, true)
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

        // TODO (Ramil Timerbaev) вернуть когда поправится баг http://jira.aplana.com/browse/SBRFACCTAX-9436
        // графа 23 - атрибут 19 - TAX_BENEFIT_ID - «Код налоговой льготы», справочник 7 «Параметры налоговых льгот транспортного налога»
        // def recordId = getRecordIdImport(6, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        // newRow.taxBenefitCode = getRecordIdImport(7, 'TAX_BENEFIT_ID', recordId.toString(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 24
        newRow.base = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // TODO (Ramil Timerbaev) вернуть когда будет готов справочник «Средняя стоимость транспортных средств» http://jira.aplana.com/browse/SBRFACCTAX-9316
        // графа 25 - атрибут NNN - NAME - «Модель (версия)», справочник NN «Средняя стоимость транспортных средств»
        // newRow.version = getRecordIdImport(NN, 'NAME', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // Техническое поле(группа)
        def sectionAlias = row.cell[xmlIndexCol].text()

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
    def ids = getProovider(42L).getParentsHierarchy(tsTypeCode)
    if (ids != null && !ids.isEmpty()) {
        return getRefBookValue(42L, ids.get(0))?.CODE?.value
    }
    return null
}

def getProovider(def id) {
    return formDataService.getRefBookProvider(refBookFactory, id, providerCache)
}