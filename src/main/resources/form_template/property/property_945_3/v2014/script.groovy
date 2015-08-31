package form_template.property.property_945_3.v2014

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import groovy.transform.Field

/**
 * Расчёт налога на имущество по средней/среднегодовой стоимости
 *
 * formTemplateId=613
 *
 * @author Bulat Kinzyabulatov
 */

// графа 1	rowNum	    	        № пп
//          fix
// графа 2	subject	    	        Код субъекта
// графа 3	taxAuthority	        Код НО
// графа 4	kpp	                    КПП
// графа 5	oktmo	                Код ОКТМО
// графа 6	priceAverage	        Средняя/среднегодовая стоимость имущества
// графа 7	taxBenefitCode	        Код налоговой льготы
// графа 8	benefitBasis	        Основание льготы
// графа 9	priceAverageTaxFree	    Средняя/Среднегодовая стоимость необлагаемого имущества
// графа 10	taxBase                 Налоговая база
// графа 11	taxBenefitCodeReduction Код налоговой льготы (понижение налоговой ставки)
// графа 12	benefitReductionBasis   Основание льготы
// графа 13	taxRate	                Налоговая ставка
// графа 14	taxSum	                Сумма налога (авансового платежа)
// графа 15	sumPayment	            Сумма авансовых платежей, исчисленная за отчетные периоды
// графа 16	taxBenefitCodeDecrease	Код налоговой льготы (в виде уменьшения суммы налога)
// графа 17	benefitDecreaseBasis	Основание льготы
// графа 18	sumDecrease	            Сумма уменьшения платежа
// графа 19	residualValue	        Остаточная стоимость основных средств

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        checkPrevForm()
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.CHECK:
        checkPrevForm()
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, null, null)
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow?.getAlias() == null) {
            formDataService.getDataRowHelper(formData)?.delete(currentDataRow)
        }
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        checkPrevForm()
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT:
        noImport(logger)
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
def allColumns = ["rowNum", "fix", "subject", "taxAuthority", "kpp", "oktmo", "priceAverage", "taxBenefitCode",
                 "benefitBasis", "priceAverageTaxFree", "taxBase", "taxBenefitCodeReduction", "benefitReductionBasis",
                 "taxRate", "taxSum", "sumPayment", "taxBenefitCodeDecrease", "benefitDecreaseBasis", "sumDecrease",
                 "residualValue"]

// Редактируемые атрибуты
@Field
def editableColumns = ["residualValue"]

@Field
def autoFillColumns = ["rowNum", "fix", "subject", "taxAuthority", "kpp", "oktmo", "priceAverage", "taxBenefitCode",
                       "benefitBasis", "priceAverageTaxFree", "taxBase", "taxBenefitCodeReduction", "benefitReductionBasis",
                       "taxRate", "taxSum", "sumPayment", "taxBenefitCodeDecrease", "benefitDecreaseBasis", "sumDecrease"]

@Field
def nonEmptyColumns = ["subject", "taxAuthority", "kpp", "oktmo", "priceAverage", "priceAverageTaxFree", "taxRate", "taxSum", 'residualValue']

@Field
def sortColumns = ['subject', 'taxAuthority', 'kpp', 'oktmo']

@Field
def groupColumns = ['taxAuthority', 'kpp']

@Field
def totalColumns = ['priceAverage', 'priceAverageTaxFree', 'taxBase', 'taxSum', 'sumPayment', 'sumDecrease', 'residualValue']

@Field
String TAX_AUTHORITY_APPROVED = 'Признаваемых объектом налогообложения'

@Field
String BENEFIT_PRICE = 'В т.ч. стоимость льготируемого имущества (всего):'

@Field
String WITHOUT_CATEGORY = 'Без категории'

// форма 945.5
@Field
def sourceFormTypeId = 615

@Field
def startDate = null

@Field
def endDate = null

@Field
ReportPeriod reportPeriod = null

@Field
def prevReportPeriods = null

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getCalendarStartDate(formData.reportPeriodId).time
    }
    return startDate
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

ReportPeriod getReportPeriod() {
    if (reportPeriod == null) {
        reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    }
    return reportPeriod
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache);
}

// Получить id записи справочника.
def getRefBookRecordId(Long refBookId, String alias, String value, Date recordDate) {
    if (refBookId == null) {
        return null
    }
    def record = formDataService.getRefBookRecord(refBookId, recordCache, providerCache, refBookCache,
            alias, value, getReportPeriodEndDate(), -1, null, logger, true)
    return record?.record_id?.value
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def ReportPeriod reportPeriod = getReportPeriod()
    def isTaxPeriod = reportPeriod.order == 4

    def totalRow = getDataRow(dataRows, 'total')
    deleteAllAliased(dataRows)
    sort(dataRows)

    for (def row : dataRows) {
        if (row.taxBenefitCode) {
            // графа 8
            row.benefitBasis = calcBasis(row.taxBenefitCode)
        }

        if (isTaxPeriod && row.priceAverage != null && row.priceAverageTaxFree != null) {
            // графа 10
            row.taxBase = row.priceAverage - row.priceAverageTaxFree
        }
        if (row.taxBenefitCodeReduction != null) {
            // графа 12
            row.benefitReductionBasis = calcBasis(row.taxBenefitCodeReduction)
        }
        // графа 13
        // Если «Графа 11» = «2012400», то «Графа 13» = Значение поля «Льготная ставка, %» справочника «Параметры налоговых льгот налога на имущество»
        if (row.taxBenefitCodeReduction != null && getBenefitCode(row.taxBenefitCodeReduction) == '2012400') {
            row.taxRate = getRefBookValue(203, row.taxBenefitCodeReduction).RATE.value
        } else if (row.subject != null) {// Иначе «Графа 13» = Значение поля «Ставка, %» справочника «Ставки налога на имущество»
            String filter = "DECLARATION_REGION_ID = " + formDataDepartment.regionId?.toString() + " and REGION_ID = " + row.subject?.toString()
            def records = refBookFactory.getDataProvider(201).getRecords(getReportPeriodEndDate(), null, filter, null)
            if (records.size() == 1) {
                row.taxRate = records.get(0).RATE.value
            } else {
                loggerError(null, "Строка ${row.getIndex()}: Для текущего субъекта в справочнике «Ставки налога на имущество» не найдена налоговая ставка!")
            }
        }
        // графа 14
        if (row.taxRate != null) {
            if (isTaxPeriod && row.taxBase != null) {
                row.taxSum = row.taxBase * row.taxRate / 100
            } else if (row.priceAverage != null && row.priceAverageTaxFree != null) {
                row.taxSum = ((row.priceAverage - row.priceAverageTaxFree) * (row.taxRate / 100))/4
            }
        }
        if (row.taxBenefitCodeDecrease != null) {
            // графа 17
            row.benefitDecreaseBasis = calcBasis(row.taxBenefitCodeDecrease)
            def record = getRefBookValue(203, row.taxBenefitCodeDecrease)
            def decreaseProc = record?.REDUCTION_PCT?.value
            def decreaseRub = record?.REDUCTION_SUM?.value
            // графа 18
            if (isTaxPeriod) {
                if (decreaseProc != null) {
                    if (row.taxSum != null && row.sumPayment != null) {
                        row.sumDecrease = (row.taxSum - row.sumPayment) * decreaseProc / 100
                    }
                } else if (decreaseRub != null) {
                    row.sumDecrease = decreaseRub
                }
            } else {
                if (decreaseProc != null) {
                    if (row.taxSum != null) {
                        row.sumDecrease = (row.taxSum * decreaseProc / 100) / 4
                    }
                } else if (decreaseRub != null) {
                    row.sumDecrease = decreaseRub / 4
                }
            }
        }
    }
    addFixedRows(dataRows, totalRow)
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

void sort(def dataRows) {
    dataRows.sort { def a, def b ->
        // графа 2  - subject (справочник)
        // графа 3  - taxAuthority
        // графа 4  - kpp
        // графа 5  - oktmo (справочник)

        def valuesA = [(a.subject ? getRefBookValue(4, a.subject)?.NAME?.value : null), a.taxAuthority, a.kpp,
                       (a.oktmo ? getRefBookValue(96, a.oktmo)?.CODE?.value : null)]
        def valuesB = [(b.subject ? getRefBookValue(4, b.subject)?.NAME?.value : null), b.taxAuthority, b.kpp,
                       (b.oktmo ? getRefBookValue(96, b.oktmo)?.CODE?.value : null)]

        for (int i = 0; i < 4; i++) {
            def valueA = valuesA[i]
            def valueB = valuesB[i]
            if (valueA != valueB) {
                return valueA <=> valueB
            }
        }
        return 0
    }
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    for (def row : dataRows) {
        def index = row.getIndex()
        def errorMsg = "Строка $index: "
        if (row.getAlias() == null) {
            checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)
            // проверка граф 8, 12, 17
            Map<String, String> codeBasisMap = ['taxBenefitCode' : 'benefitBasis',
                                                'taxBenefitCodeReduction' : 'benefitReductionBasis',
                                                'taxBenefitCodeDecrease' : 'benefitDecreaseBasis']
            codeBasisMap.each { codeColumn, basisColumn ->
                if (row[codeColumn]) {
                    if (row[basisColumn] != calcBasis(row[codeColumn])) {
                        loggerError(null, "Графа «${getColumnName(row, basisColumn)}» заполнена неверно!")
                    }
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
    checkTotalSum(dataRows, totalColumns, logger, true)
}

void consolidation() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def totalRow = getDataRow(dataRows, 'total')
    dataRows = []

    // собирается из 945.5 и 945.3
    def List<List> sourceGroups = getSourceRowsGroups()
    def Map<Integer, List> prevRowsMap = getPrevRowsMap()

    if (sourceGroups != null) {
        // сделаем их единым
        unite(dataRows, sourceGroups, prevRowsMap)
    }
    dataRows.add(totalRow)

    updateIndexes(dataRows)
}

def getSourceRowsGroups() {
    def sourceRows
    // получить источник 945.5
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind,
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        if (it.formTypeId == sourceFormTypeId){
            def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                sourceRows = formDataService.getDataRowHelper(source).allSaved
            }
        }
    }
    if (sourceRows == null) {
        return null
    }
    def Map<String, List> sourceGroups = [:]
    sourceRows.each { row ->
        String key = String.format("%s %s %s %s", getOwnerValue(row, 'subject').toString(), getOwnerValue(row, 'taxAuthority'), getOwnerValue(row, 'kpp'), getOwnerValue(row, 'oktmo'))
        if (sourceGroups.get(key) == null) {
            sourceGroups.put(key, new ArrayList())
        }
        sourceGroups.get(key).add(row)
    }
    // ключи нам не нужны, только значения
    return sourceGroups.values().asList()
}

def getPrevRowsMap() {
    def Map<Integer, List> prevRowsMap = [:]
    def ReportPeriod reportPeriod = getReportPeriod()
    // только для годичной получаем формы предыдущих периодов
    if (reportPeriod.order == 4) {
        def List<ReportPeriod> periodList = reportPeriodService.listByTaxPeriod(reportPeriod.taxPeriod.id)
        def List<ReportPeriod> errorPeriods = []
        periodList.each{ period ->
            if (period.order != 4) {
                def fd = formDataService.getLast(formData.formType.id, formData.kind, formData.departmentId, period.id, null, formData.comparativePeriodId, formData.accruing)
                if (fd != null && fd.state == WorkflowState.ACCEPTED) {
                    prevRowsMap.put(period.order, formDataService.getDataRowHelper(fd).allSaved)
                } else {
                    errorPeriods.add(period)
                }
            }
        }
        if (!errorPeriods.isEmpty()) {
            def formTypeName = formTypeService.get(formData.formType.id).name
            def periodAndYear = errorPeriods.collect { it.name }.join(', ') + " " + errorPeriods.get(0).taxPeriod.year
            loggerError(null, "Экземпляр налоговой формы «$formTypeName» в статусе «Принята» за $periodAndYear г. не существует! Расчеты не могут быть выполнены!")
        }
    }
    return prevRowsMap
}

/**
 * Собрать из групп источника 945.5 и строк форм 953.3
 *
 * @param dataRows строки текущей формы
 * @param sourceRowsGroups строки источников 945.5
 * @param prevRowsMap мапа с данными из предыдущих форм 945.3
 */
void unite(def dataRows, def sourceRowsGroups, def prevRowsMap) {
    // проходим по группам строк 945.5
    for (def groupRows : sourceRowsGroups) {
        // строка "Признаваемых объектом налогообложения"
        def taApprovedRow = null
        // первая строка с кодом 2012000
        def specialRow = null
        Map<DataRow, List<String>> rowCodesMap = [:]
        for (def DataRow row : groupRows) {
            // если "Без категории"/"Категория"
            if (! (row.title in [TAX_AUTHORITY_APPROVED, BENEFIT_PRICE])) {
                def categoryFilter = (row.title == WITHOUT_CATEGORY)? " and PARAM_DESTINATION = 0" : " and PARAM_DESTINATION = 1 and LOWER(ASSETS_CATEGORY) = LOWER('${row.title}')"
                String filter = "DECLARATION_REGION_ID = " + formDataDepartment.regionId?.toString() + " and REGION_ID = " + getOwnerValue(row, 'subject')?.toString() + categoryFilter
                def records = refBookFactory.getDataProvider(203).getRecords(getReportPeriodEndDate(), null, filter, null)
                if (records.size() == 0) {
                    benefitError(row)
                    continue
                }
                def benefitCodes = records.collect { getRefBookValue(202L, it.TAX_BENEFIT_ID.value).CODE.value }
                if (!specialRow && benefitCodes.contains('2012000')) {
                    specialRow = row
                }
                rowCodesMap.put(row, benefitCodes)
            } else if (row.title == TAX_AUTHORITY_APPROVED) {
                taApprovedRow = row
            }
        }
        if (!rowCodesMap.isEmpty()) {
            if (taApprovedRow != null) {
                def newRow = getNewRow()
                def benefitCodes = rowCodesMap.get(specialRow)
                updateRowCategory(newRow, specialRow, taApprovedRow, benefitCodes, specialRow != null)
                updateRowFromPrev(newRow, specialRow, prevRowsMap, benefitCodes)
                dataRows.add(newRow)
            }
            for (def rowCodesEntry : rowCodesMap) {
                def row = rowCodesEntry.key
                def benefitCodes = rowCodesEntry.value
                def newRow = getNewRow()
                updateRowCategory(newRow, row, null, benefitCodes, false)
                updateRowFromPrev(newRow, row, prevRowsMap, benefitCodes)
                dataRows.add(newRow)
            }
        }
    }
}

def getNewRow() {
    def newRow = formData.createDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    autoFillColumns.each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }
    return newRow
}

void updateRowCategory(def newRow, def row, def taApprovedRow, List<String> benefitCodes, def isSpecial) {
    def reportPeriod = getReportPeriod()
    def columns = []
    // столбцы, по которым проводить сумму
    switch (reportPeriod.order) {
        case 4: columns += ['cost11', 'cost12', 'cost13']
        case 3: columns += ['cost8', 'cost9', 'cost10']
        case 2: columns += ['cost5', 'cost6', 'cost7']
        case 1: columns += ['cost1', 'cost2', 'cost3', 'cost4']
    }
    def isTA = (taApprovedRow != null)
    ['subject', 'taxAuthority', 'kpp', 'oktmo'].each { newRow[it] = getOwnerValue((isTA? taApprovedRow : row), it)}
    // графа 6
    def sum = 0
    if (taApprovedRow || row) {
        sum = columns.sum { getOwnerValue((taApprovedRow ?: row), it) }
    }
    newRow.priceAverage = sum / columns.size()
    // row == null, только если отсутствует строка с кодом 2012000
    // графа 7
    newRow.taxBenefitCode = getBenefitRecordId(row, benefitCodes, '2012000', getReportPeriodEndDate())
    // графа 9
    if (isTA && !isSpecial) {
        newRow.priceAverageTaxFree = 0
    } else {
        newRow.priceAverageTaxFree = columns.sum { row[it] } / columns.size()
    }
    // графа 11
    newRow.taxBenefitCodeReduction = getBenefitRecordId(row, benefitCodes, '2012400', getReportPeriodEndDate())
    // графа 16
    newRow.taxBenefitCodeDecrease = getBenefitRecordId(row, benefitCodes, '2012500', getReportPeriodEndDate())
}

void updateRowFromPrev(def newRow, def row, def prevRowsMap, List<String> benefitCodes) {
    // только для годовых форм
    if (reportPeriod.order != 4) {
        return
    }
    def ReportPeriod reportPeriod = getReportPeriod()
    def List<ReportPeriod> periodList = reportPeriodService.listByTaxPeriod(reportPeriod.taxPeriod.id)
    def sum = 0
    // если используется "Без категории"/"Категория К"
    if (row != null) {
        if (! row.title in [TAX_AUTHORITY_APPROVED, BENEFIT_PRICE]) {
            for (Map.Entry entry : prevRowsMap) {
                def order = entry.key
                def dataRows = entry.value
                def record1 = getBenefitRecordId(row, benefitCodes, '2012000', getRecordDate(periodList, order))
                def record2 = getBenefitRecordId(row, benefitCodes, '2012400', getRecordDate(periodList, order))
                def record3 = getBenefitRecordId(row, benefitCodes, '2012500', getRecordDate(periodList, order))
                def selectedRow = dataRows.find{
                    it.subject == getOwnerValue(row, 'subject') && it.taxAuthority == getOwnerValue(row, 'taxAuthority') &&
                            it.kpp == getOwnerValue(row, 'kpp') && it.oktmo == getOwnerValue(row, 'oktmo') &&
                            it.taxBenefitCode == record1 && it.taxBenefitCodeReduction == record2 && it.taxBenefitCodeDecrease == record3
                }
                if (selectedRow) {
                    // суммируем 14-ые графы строк
                    sum += selectedRow.taxSum
                } else {
                    sourceRowError(newRow, row, periodList, order)
                }
            }
        } else {
            for (Map.Entry entry : prevRowsMap) {
                def order = entry.key
                def dataRows = entry.value
                // ищем строки по совпадению 4-х граф
                def selectedRow = dataRows.find{
                    it.subject == getOwnerValue(row, 'subject') && it.taxAuthority == getOwnerValue(row, 'taxAuthority') &&
                            it.kpp == getOwnerValue(row, 'kpp') && it.oktmo == getOwnerValue(row, 'oktmo')
                }
                if (selectedRow) {
                    // суммируем 14-ые графы строк
                    sum += selectedRow.taxSum
                } else {
                    sourceRowError(newRow, row, periodList, order)
                }
            }
        }
    }
    newRow.sumPayment = sum
}

def getBenefitRecordId(DataRow row, List<String> benefitCodes, String benefitCode, def recordDate) {
    if (row && benefitCodes && benefitCodes.contains(benefitCode)) {
        def benefitId = getRefBookRecordId(202, 'CODE', benefitCode, recordDate)
        def categoryFilter = (row.title == WITHOUT_CATEGORY)? " and PARAM_DESTINATION = 0" : " and PARAM_DESTINATION = 1 and LOWER(ASSETS_CATEGORY) = LOWER('${row.title}')"
        String filter = "DECLARATION_REGION_ID = " + formDataDepartment.regionId?.toString() + " and REGION_ID = " + getOwnerValue(row, 'subject')?.toString() + categoryFilter + " and TAX_BENEFIT_ID = " + benefitId.toString()
        def records = refBookFactory.getDataProvider(203).getRecords(recordDate, null, filter, null)
        if (records.size() == 1) {
            return records.get(0).record_id.value
        } else {
            return null
        }
    }
    return null
}

def getRecordDate(def periodList, def order) {
    return periodList.find { it.order == order }
}

void loggerError(def row, def msg) {
    rowError(logger, row, msg)
}

void benefitError(def row) {
    def subjectCode = getRefBookValue(4, getOwnerValue(row, 'subject')).CODE.value
    def catFilter = (row.title == WITHOUT_CATEGORY) ? "без категории " : ("с категорией «" + row.title + "» ")
    loggerError(null, "Для кода субъекта " + subjectCode + " не предусмотрена налоговая  льгота " + catFilter + " (в справочнике «Параметры налоговых льгот налога на имущество» отсутствует необходимая запись)!")
}

void sourceRowError(def newRow, def row, def periodList, def order) {
    def reportPeriod = periodList.find { it.order == order }
    def subject = getRefBookValue(4, getOwnerValue(row, 'subject')).CODE.value
    def oktmo = getRefBookValue(96, getOwnerValue(row, 'oktmo')).CODE.value
    def benefit = newRow.taxBenefitCode ? ((getRefBookValue(203, newRow.taxBenefitCode).CODE?.value ?: "\"\"") + '/' + calcBasis(newRow.taxBenefitCode)) : "\"\""
    def benefitReduction = newRow.taxBenefitCodeReduction ? ((getRefBookValue(203, newRow.taxBenefitCodeReduction).CODE?.value ?: "\"\"") + '/' + calcBasis(newRow.taxBenefitCodeReduction)) : "\"\""
    def benefitDecrease = newRow.taxBenefitCodeDecrease ? ((getRefBookValue(203, newRow.taxBenefitCodeDecrease).CODE?.value ?: "\"\"") + '/' + calcBasis(newRow.taxBenefitCodeDecrease)) : "\"\""
    loggerError(null, "В налоговой форме-источнике «${formData.formType.name}» за ${reportPeriod.name} " +
            "${reportPeriod.taxPeriod.year} отсутствует строка со значениями граф «Код субъекта» = ${subject}, " +
            "«Код НО» = ${getOwnerValue(row, 'taxAuthority')}, «КПП» = ${getOwnerValue(row, 'kpp')}, «Код ОКТМО» = ${oktmo}, " +
            "«Код налоговой льготы и основание» = $benefit, " +
            "«Код налоговой льготы и основание (понижение налоговой ставки)» = $benefitReduction, " +
            "«Код налоговой льготы и основание (в виде уменьшения суммы налога)» = $benefitDecrease!")
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

def getBenefitCode(def parentRecordId) {
    def recordId = getRefBookValue(203, parentRecordId).TAX_BENEFIT_ID.value
    return  getRefBookValue(202, recordId).CODE.value
}

def checkPrevForm() {
    // 2. Проверка заполнения атрибута «Регион» подразделения текущей формы (справочник «Подразделения»)
    if (formDataDepartment.regionId == null) {
        throw new Exception("Атрибут «Регион» подразделения текущей налоговой формы не заполнен (справочник «Подразделения»)!")
    }

    // 1. Проверить существование и принятость форм, а также наличие данных в них.
    if (getReportPeriod()?.order != 4) {
        return
    }
    def reportPeriods = getPrevReportPeriods()

    for (def reportPeriod : reportPeriods) {
        formDataService.checkFormExistAndAccepted(formData.formType.id, FormDataKind.SUMMARY, formDataDepartment.id, reportPeriod.id, false, logger, true)
    }
}

/** Получить предыдущие преиоды за год. */
def getPrevReportPeriods() {
    if (prevReportPeriods == null) {
        prevReportPeriods = []
        def yearStartDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
        // получить периоды за год
        def reportPeriods = reportPeriodService.getReportPeriodsByDate(TaxType.PROPERTY, yearStartDate, getReportPeriodEndDate())
        for (def reportPeriod : reportPeriods) {
            if (reportPeriod.id == formData.reportPeriodId) {
                continue
            }
            prevReportPeriods.add(reportPeriod)
        }
    }
    return prevReportPeriods
}