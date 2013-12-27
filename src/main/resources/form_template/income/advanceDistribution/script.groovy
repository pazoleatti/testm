package form_template.income.advanceDistribution

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import groovy.transform.Field

/**
 * Расчёт распределения авансовых платежей и налога на прибыль по обособленным подразделениям организации.
 * formTemplateId=500
 *
 * TODO:
 *      - консолидация не доделана потому что не готова нф "(Приложение 5) Сведения для расчета налога на прибыль".
 *
 * @author akadyrgulov
 * @author <a href="mailto:Ramil.Timerbaev@aplana.com">Тимербаев Рамиль</a>
 */
// графа 1  - number
// графа 2  - regionBank
// графа 3  - regionBankDivision
// графа 4  - kpp
// графа 5  - propertyPrice
// графа 6  - workersCount
// графа 7  - subjectTaxCredit
// графа 8  - calcFlag
// графа 9  - obligationPayTax
// графа 10 - baseTaxOf
// графа 11 - baseTaxOfRub
// графа 12 - subjectTaxStavka
// графа 13 - taxSum
// графа 14 - taxSumOutside
// графа 15 - taxSumToPay
// графа 16 - taxSumToReduction
// графа 17 - everyMontherPaymentAfterPeriod
// графа 18 - everyMonthForKvartalNextPeriod
// графа 19 - everyMonthForSecondKvartalNextPeriod
// графа 20 - everyMonthForThirdKvartalNextPeriod
// графа 21 - everyMonthForFourthKvartalNextPeriod

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK :
        logicalCheck()
        break
    case FormDataEvent.CALCULATE :
        logicalCheckBeforeCalc()
        calc()
        logicalCheckAfterCalc()
        break
    case FormDataEvent.ADD_ROW :
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW :
        if (currentDataRow.getAlias() == null) formDataService.getDataRowHelper(formData).delete(currentDataRow)
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
        logicalCheck()
        checkDeclaration()
        break
// после принятия из подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED :
        logicalCheck()
        // на стороне сервера будет выполнен compose
        break
// обобщить
    case FormDataEvent.COMPOSE :
        consolidation()
        calc()
        logicalCheckAfterCalc()
        // для сохранения изменении приемников
//        getData(formData).commit()
        break
}

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

// Все атрибуты
@Field
def allColumns = ['number', 'regionBank', 'fix', 'regionBankDivision', 'kpp', 'propertyPrice',
        'workersCount', 'subjectTaxCredit', 'calcFlag', 'obligationPayTax',
        'baseTaxOf', 'baseTaxOfRub', 'subjectTaxStavka', 'taxSum',
        'taxSumOutside', 'taxSumToPay', 'taxSumToReduction',
        'everyMontherPaymentAfterPeriod', 'everyMonthForKvartalNextPeriod',
        'everyMonthForSecondKvartalNextPeriod', 'everyMonthForThirdKvartalNextPeriod',
        'everyMonthForFourthKvartalNextPeriod']

// Редактируемые атрибуты
@Field
def editableColumns = ['regionBankDivision', 'propertyPrice', 'workersCount', 'subjectTaxCredit']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['number', 'regionBank', 'regionBankDivision',
        'kpp', 'propertyPrice', 'workersCount',
        'subjectTaxCredit', 'calcFlag', 'obligationPayTax',
        'baseTaxOf', 'baseTaxOfRub', 'subjectTaxStavka',
        'taxSum', 'taxSumOutside', 'taxSumToPay',
        'taxSumToReduction', 'everyMontherPaymentAfterPeriod', 'everyMonthForKvartalNextPeriod',
        'everyMonthForSecondKvartalNextPeriod', 'everyMonthForThirdKvartalNextPeriod', 'everyMonthForFourthKvartalNextPeriod'
]

// Группируемые атрибуты
@Field
def groupColumns = ['regionBankDivision', 'regionBank']

// Атрибуты для итогов
@Field
def totalColumns = ['propertyPrice', 'workersCount', 'subjectTaxCredit', 'baseTaxOf',
        'baseTaxOfRub', 'taxSum', 'taxSumOutside', 'taxSumToPay',
        'taxSumToReduction', 'everyMontherPaymentAfterPeriod',
        'everyMonthForKvartalNextPeriod', 'everyMonthForSecondKvartalNextPeriod',
        'everyMonthForThirdKvartalNextPeriod',
        'everyMonthForFourthKvartalNextPeriod']

// Текущая дата
@Field
def currentDate = new Date()

// Проверка НСИ
boolean checkNSI(def refBookId, def row, def alias) {
    return formDataService.checkNSI(refBookId, refBookCache, row, alias, logger, true)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Поиск записи в справочнике по значению (для расчетов) + по дате
def getRefBookRecord(def Long refBookId, def String alias, def String value, def Date day, def int rowIndex, def String cellName,
                     boolean required) {
    return formDataService.getRefBookRecord(refBookId, recordCache, providerCache, refBookCache, alias, value,
            day, rowIndex, cellName, logger, required)
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    // удалить фиксированные строки
    deleteAllAliased(dataRows)

    logicalCheckBeforeCalc()
    if (logger.containsLevel(LogLevel.ERROR)) {
        return
    }

    def propertyPriceSumm = getSumAll(dataRows, "propertyPrice")
    def workersCountSumm = getSumAll(dataRows, "workersCount")

    /** Распределяемая налоговая база за отчетный период. */
    def taxBase = roundValue(getTaxBase(), 0)
    /** Отчётный период. */
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

    /** Сумма налога на прибыль, выплаченная за пределами Российской Федерации в отчётном периоде. */
    def sumNal = 0
    def sumTaxRecords = getRefBookRecord(33, "DEPARTMENT_ID", "1", currentDate, -1, null, false)
    if (sumTaxRecords != null && !sumTaxRecords.isEmpty()) {
        sumNal = new BigDecimal(getValue(sumTaxRecords, 'SUM_TAX').doubleValue())
    }

    // расчет графы 2..4, 8..21
    for (row in dataRows) {
        // графа 2 - название подразделения
        row.regionBank = calc2(row)

        def incomeParam
        if (row.regionBankDivision != null) incomeParam = getRefBookRecord(33, "DEPARTMENT_ID", "$row.regionBankDivision", currentDate, -1, null, false)
        if (incomeParam == null || incomeParam.isEmpty()) {
            continue
        }
        // графа 4 - кпп
        row.kpp = incomeParam.get('record_id').getNumberValue()

        // графа 8 - Признак расчёта
        row.calcFlag = incomeParam.get('TYPE').getReferenceValue()

        // графа 9 - Обязанность по уплате налога
        row.obligationPayTax = incomeParam.get('OBLIGATION').getReferenceValue()

        // графа 10
        row.baseTaxOf = calc10(row, propertyPriceSumm, workersCountSumm)

        // графа 11
        row.baseTaxOfRub = calc11(row, taxBase)

        // графа 12
        row.subjectTaxStavka = row.kpp

        // графа 13..21
        calcColumnFrom13To21(row, sumNal, reportPeriod)
    }

    // Сортировка
    // отсортировать можно только после расчета графы regionBank
    dataRows.sort { a, b ->
        def regionBankA = getRefBookValue(30, a.regionBank)?.NAME?.stringValue
        def regionBankB = getRefBookValue(30, b.regionBank)?.NAME?.stringValue
        if (regionBankA == regionBankB) {
            def regionBankDivisionA = getRefBookValue(30, a.regionBankDivision)?.NAME?.stringValue
            def regionBankDivisionB = getRefBookValue(30, b.regionBankDivision)?.NAME?.stringValue
            return (regionBankDivisionA <=> regionBankDivisionB)
        }
        return (regionBankA <=> regionBankB)
    }

    // расчет графы 1 после сортировки
    def index = 0
    for (row in dataRows) {
        row.number = ++index
    }

    // добавить строку ЦА (скорректрированный) (графа 1..21)
    def caTotalRow = formData.createDataRow()
    caTotalRow.setAlias('ca')
    caTotalRow.fix = 'Центральный аппарат (скорректированный)'
    caTotalRow.getCell('fix').colSpan = 2
    setTotalStyle(caTotalRow)
    dataRows.add(caTotalRow)

    // добавить итого (графа 5..7, 10, 11, 13..21)
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.fix = 'Итого'
    totalRow.getCell('fix').colSpan = 2
    setTotalStyle(totalRow)
    calcTotalSum(dataRows, totalRow, totalColumns)
    dataRows.add(totalRow)

    // найти строку ЦА
    def caRow = findCA(dataRows)
    if (caRow != null) {
        // расчеты для строки ЦА (скорректированный) графы 1, 3..21
        // графа 1, 3..10, 12
        ['number', 'regionBankDivision', 'kpp', 'propertyPrice', 'workersCount', 'subjectTaxCredit',
                'calcFlag', 'obligationPayTax', 'baseTaxOf', 'subjectTaxStavka'].each { alias ->
            caTotalRow.getCell(alias).setValue(caRow.getCell(alias).getValue())
        }

        // графа 11
        caTotalRow.baseTaxOfRub = taxBase - totalRow.baseTaxOfRub + caRow.baseTaxOfRub

        // графа 13..21
        calcColumnFrom13To21(caTotalRow, sumNal, reportPeriod)
    }

    dataRowHelper.save(dataRows)
}

// графа 2 - название подразделения
def calc2(def row) {
    def departmentParam
    if (row.regionBankDivision != null) departmentParam = getRefBookRecord(30, "ID", "$row.regionBankDivision", currentDate, -1, null, false)
    if (departmentParam == null || departmentParam.isEmpty()) {
        return null
    }

    long centralId = 113 // ID Центрального аппарата.
    // У Центрального аппарата родительским подразделением должен быть он сам
    if (centralId == row.regionBankDivision) {
        return centralId
    } else {
        return departmentParam.get('PARENT_ID').getReferenceValue()
    }
}

def calc10(def row, def propertyPriceSumm, def workersCountSumm){
    if (row.propertyPrice != null && row.workersCount != null && propertyPriceSumm > 0 && workersCountSumm > 0) {
        return roundValue((row.propertyPrice / propertyPriceSumm * 100 + row.workersCount / workersCountSumm * 100) / 2, 8)
    }
    return null
}

def calc11(def row, def taxBase){
    if (row.baseTaxOf != null && taxBase != null) {
        return roundValue(taxBase * row.baseTaxOf / 100, 0)
    }
    return null
}

/**
 * Логические проверки.
 *
 * @param useLog нужно ли записывать в лог сообщения о незаполненности обязательных полей
 */
def logicalCheck() {
    logicalCheckBeforeCalc()
    logicalCheckAfterCalc()
}

void logicalCheckBeforeCalc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    def sumTaxRecords = getRefBookRecord(33, "DEPARTMENT_ID", "1", currentDate, -1, null, false)
    if (sumTaxRecords == null || sumTaxRecords.isEmpty() || getValue(sumTaxRecords, 'SUM_TAX') == null) {
        logger.error("В форме настроек подразделений (подразделение «УНП») не задано значение атрибута «Сумма налога на прибыль, выплаченная за пределами Российской Федерации в отчётном периоде»!")
    }

    def fieldNumber = 0

    dataRows.eachWithIndex { row, i ->
        if (isFixedRow(row)) {
            return
        }
        fieldNumber++
        def index = row.getIndex()
        def errorMsg = "Строка ${index}: "

        def departmentParam
        if (row.regionBankDivision != null) departmentParam = getRefBookRecord(30, "ID", "$row.regionBankDivision", currentDate, -1, null, false)
        if (departmentParam == null || departmentParam.isEmpty()) {
            logger.error(errorMsg + "Не найдено подразделение территориального банка!")
            return
        } else {
            long centralId = 113 // ID Центрального аппарата.
            // У Центрального аппарата родительским подразделением должен быть он сам
            if (centralId != row.regionBankDivision) {
                // графа 2 - название подразделения
                if (departmentParam.get('PARENT_ID')?.getReferenceValue()==null) {
                    logger.error(errorMsg + "Для подразделения территориального банка «${departmentParam.NAME.stringValue}» в справочнике «Подразделения» отсутствует значение наименования родительского подразделения!")
                }
            }
        }

        def incomeParam
        if (row.regionBankDivision != null) incomeParam = getRefBookRecord(33, "DEPARTMENT_ID", "$row.regionBankDivision", currentDate, -1, null, false)
        if (incomeParam == null || incomeParam.isEmpty()) {
            logger.error(errorMsg + "Строка $fieldNumber: Не найдены настройки подразделения!")
        } else {
            // графа 4 - кпп
            if (incomeParam?.get('record_id')?.getNumberValue() == null || incomeParam?.get('KPP')?.getStringValue() == null) {
                logger.error(errorMsg + "Для подразделения «${departmentParam.NAME.stringValue}» на форме настроек подразделений отсутствует значение атрибута «КПП»!")
            }

            // графа 8 - Признак расчёта
            if (incomeParam?.get('TYPE')?.getReferenceValue() == null) {
                logger.error(errorMsg + "Для подразделения «${departmentParam.NAME.stringValue}» на форме настроек подразделений отсутствует значение атрибута «Признак расчёта»!")
            }

            // графа 9 - Обязанность по уплате налога
            if (incomeParam?.get('OBLIGATION')?.getReferenceValue() == null) {
                logger.error(errorMsg + "Для подразделения «${departmentParam.NAME.stringValue}» на форме настроек подразделений отсутствует значение атрибута «Обязанность по уплате налога»!")
            }

            // графа 12
            if (incomeParam?.get('record_id')?.getNumberValue() == null || incomeParam?.get('TAX_RATE')?.getNumberValue() == null) {
                logger.error(errorMsg + "Для подразделения «${departmentParam.NAME.stringValue}» на форме настроек подразделений отсутствует значение атрибута «Ставка налога в бюджет субъекта (%%)»!")
            }
        }
    }
}

void logicalCheckAfterCalc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    def departmentRefDataProvider = refBookFactory.getDataProvider(30)

    for (def row : dataRows) {
        if (isFixedRow(row)) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка ${index}: "

        // 1. Обязательность заполнения поля графы 1..21
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // Проверки соответствия НСИ
        def departmentRecord = departmentRefDataProvider.getRecordData(row.regionBankDivision);
        // 1. Проверка совпадения наименования подразделения со справочным
        if (departmentRecord.isEmpty()) {
            logger.error(errorMsg + 'Неверное наименование подразделения!')
        }
    }
}

/**
 * Консолидация.
 */
void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    // удалить все строки и собрать из источников их строки
    dataRowHelper.clear()

    /** Идентификатор шаблона источников (Приложение 5). */
    def id = 372
    def newRow

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == id) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceDataRows = formDataService.getDataRowHelper(source).getAllCached()
                sourceDataRows.each { row ->
                    if (row.getAlias() == null || row.getAlias() == '') {
                        newRow = formData.createDataRow()

                        // TODO проверить консолидацию
                        newRow.regionBank =row.regionBank
                        newRow.regionBankDivision = row.regionBankDivision
                        newRow.kpp = row.kpp
                        newRow.propertyPrice = row.propertyPrice
                        newRow.workersCount = row.workersCount
                        newRow.subjectTaxCredit = row.subjectTaxCredit

                        dataRows.add(newRow)
                    }
                }
            }
        }
    }
    logger.info('Формирование консолидированной формы прошло успешно.')
}

/**
 * Проверка наличия декларации для текущего department
 */
void checkDeclaration() {
    declarationType = 2    // Тип декларации которую проверяем (Налог на прибыль)
    declaration = declarationService.find(declarationType, formData.getDepartmentId(), formData.getReportPeriodId())
    if (declaration != null && declaration.isAccepted()) {
        logger.error("Декларация банка находиться в статусе принята")
    }
}

/*
 * Вспомогательные методы.
 */

/**
 * Проверка является ли строка фиксированной.
 */
def isFixedRow(def row) {
    return row != null && row.getAlias() != null
}

/**
 * Проверка пустое ли значение.
 */
def isEmpty(def value) {
    return value == null || value == '' || value == 0
}

/**
 * Установить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    allColumns.each {
        row.getCell(it).setStyleAlias('Итоговая')
    }
}

/**
 * Получить сумму столбца (за исключением значении фиксированных строк).
 *
 * @param data данные нф (helper)
 * @param columnAlias алилас столбца по которому считать сумму
 * @return
 */
def getSum(def dataRows, def columnAlias) {
    def from = 0
    def to = dataRows.size() - 2
    if (from > to) {
        return 0
    }
    return summ(formData, dataRows, new ColumnRange(columnAlias, from, to))
}

/**
 * Получить сумму столбца (сумма значении всех строк).
 */
def getSumAll(def dataRows, def columnAlias) {
    def from = 0
    def to = dataRows.size() - 1
    if (from > to) {
        return 0
    }
    return summ(formData, dataRows, new ColumnRange(columnAlias, from, to))
}

/**
 * Получить данные формы.
 *
 * @param formData форма
 */
def getData(def formData) {
    if (formData != null && formData.id != null) {
        return formDataService.getDataRowHelper(formData)
    }
    return null
}

/**
 * Вставить новыую строку в конец нф.
 *
 * @param data данные нф
 * @param row строка
 */
void insert(def data, def row) {
    data.insert(row, data.getAllCached().size() + 1)
}

/**
 * Расчитываем распределяемая налоговая база за отчётный период.
 *
 * @author ekuvshinov
 */
def getTaxBase() {
    def departmentId = formData.departmentId
    def reportPeriodId = formData.reportPeriodId

    // доходы сложные
    def formDataComplexIncome = formDataService.find(302, FormDataKind.SUMMARY, departmentId, reportPeriodId)
    def dataComplexIncome = getData(formDataComplexIncome)

    // доходы простые
    def formDataSimpleIncome = formDataService.find(301, FormDataKind.SUMMARY, departmentId, reportPeriodId)
    def dataSimpleIncome = getData(formDataSimpleIncome)

    // расходы сложные
    def formDataComplexConsumption = formDataService.find(303, FormDataKind.SUMMARY, departmentId, reportPeriodId)
    def dataComplexConsumption = getData(formDataComplexConsumption)

    // расходы простые
    def formDataSimpleConsumption = formDataService.find(304, FormDataKind.SUMMARY, departmentId, reportPeriodId)
    def dataSimpleConsumption = getData(formDataSimpleConsumption)

    BigDecimal taxBase = 0
    // доходы сложные
    if (dataComplexIncome != null) {
        for (row in dataComplexIncome.getAllCached()) {
            // Если ячейка не объеденена то она должна быть в списке
            String khy = row.getCell('incomeTypeId').hasValueOwner() ? row.getCell('incomeTypeId').getValueOwner().value : row.getCell('incomeTypeId').value

            BigDecimal incomeTaxSumS = (BigDecimal)(row.getCell('incomeTaxSumS').hasValueOwner() ? row.getCell('incomeTaxSumS').getValueOwner().value : row.getCell('incomeTaxSumS').value)
            incomeTaxSumS = incomeTaxSumS ?: 0
            //k1
            if (khy in ['10633', '10634', '10650', '10670']) {
                taxBase += incomeTaxSumS
            }
            //k5
            if (khy in ['10855', '10880', '10900']) {
                taxBase += incomeTaxSumS
            }
            //k6
            if (khy in ['10850']) {
                taxBase += incomeTaxSumS
            }
            //k7
            if (khy in ['11180', '11190', '11200', '11210', '11220', '11230', '11240', '11250', '11260']) {
                taxBase += incomeTaxSumS
            }
            //k8
            if (khy in ['11405', '11410', '11415', '13040', '13045', '13050', '13055', '13060', '13065', '13070', '13090', '13100', '13110', '13120', '13250', '13650', '13655', '13660', '13665', '13670', '13675', '13680', '13685', '13690', '13695', '13700', '13705', '13710', '13715', '13720', '13780', '13785', '13790', '13940', '13950', '13960', '13970', '13980', '13990', '14140', '14170', '14180', '14190', '14200', '14210', '14220', '14230', '14240', '14250', '14260', '14270', '14280', '14290' ]) {
                taxBase += incomeTaxSumS
            }
            //k13
            if (khy in ['10840']) {
                taxBase += incomeTaxSumS
            }
            //k15
            if (khy in ['10860']) {
                taxBase += incomeTaxSumS
            }
            //k16
            if (khy in ['10870']) {
                taxBase += incomeTaxSumS
            }
            //k19
            if (khy in ['10890']) {
                taxBase += incomeTaxSumS
            }
            //k21
            if (khy in ['13655', '13660', '13665', '13675', '13680', '13685', '13690', '13695', '13705', '13710', '13780', '13785', '13790' ]) {
                taxBase -= incomeTaxSumS
            }
        }
    }
    // Доходы простые
    if (dataSimpleIncome != null) {
        for (row in dataSimpleIncome.getAllCached()) {
            String khy = row.getCell('incomeTypeId').hasValueOwner() ? row.getCell('incomeTypeId').getValueOwner().value : row.getCell('incomeTypeId').value

            // графа 8
            BigDecimal rnu4Field5Accepted = (BigDecimal)(row.getCell('rnu4Field5Accepted').hasValueOwner() ? row.getCell('rnu4Field5Accepted').getValueOwner().value : row.getCell('rnu4Field5Accepted').value)
            rnu4Field5Accepted = rnu4Field5Accepted ?: 0

            // графа 5
            BigDecimal rnu6Field10Sum = (BigDecimal)(row.getCell('rnu6Field10Sum').hasValueOwner() ? row.getCell('rnu6Field10Sum').getValueOwner().value : row.getCell('rnu6Field10Sum').value)
            rnu6Field10Sum = rnu6Field10Sum ?: 0

            // графа 6
            BigDecimal rnu6Field12Accepted = (BigDecimal)(row.getCell('rnu6Field12Accepted').hasValueOwner() ? row.getCell('rnu6Field12Accepted').getValueOwner().value : row.getCell('rnu6Field12Accepted').value)
            rnu6Field12Accepted = rnu6Field12Accepted ?: 0
            //k2
            if (khy in ['10001', '10006', '10041', '10300', '10310', '10320', '10330', '10340', '10350', '10360', '10370', '10380', '10390', '10450', '10460', '10470', '10480', '10490', '10571', '10580', '10590', '10600', '10610', '10630', '10631', '10632', '10640', '10680', '10690', '10740', '10744', '10748', '10752', '10756', '10760', '10770', '10790', '10800', '11140', '11150', '11160', '11170', '11320', '11325', '11330', '11335', '11340', '11350', '11360', '11370', '11375']) {
                taxBase += rnu4Field5Accepted
            }
            //k3
            if (khy in ['10001', '10006', '10300', '10310', '10320', '10330', '10340', '10350', '10360', '10470', '10480', '10490', '10571', '10590', '10610', '10640', '10680', '10690', '11340', '11350', '11370', '11375' ]) {
                taxBase += rnu6Field10Sum
            }
            //k4
            if (khy in ['10001', '10006', '10300', '10310', '10320', '10330', '10340', '10350', '10360', '10470', '10480', '10490', '10571', '10590', '10610', '10640', '10680', '10690', '11340', '11350', '11370', '11375']) {
                taxBase -= rnu6Field12Accepted
            }
            //k9
            if (khy in ['11380', '11385', '11390', '11395', '11400', '11420', '11430', '11840', '11850', '11855', '11860', '11870', '11880', '11930', '11970', '12000', '12010', '12030', '12050', '12070', '12090', '12110', '12130', '12150', '12170', '12190', '12210', '12230', '12250', '12270', '12290', '12320', '12340', '12360', '12390', '12400', '12410', '12420', '12430', '12830', '12840', '12850', '12860', '12870', '12880', '12890', '12900', '12910', '12920', '12930', '12940', '12950', '12960', '12970', '12980', '12985', '12990', '13000', '13010', '13020', '13030', '13035', '13080', '13150', '13160', '13170', '13180', '13190', '13230', '13240', '13290', '13300', '13310', '13320', '13330', '13340', '13400', '13410', '13725', '13730', '13920', '13925', '13930', '14000', '14010', '14020', '14030', '14040', '14050', '14060', '14070', '14080', '14090', '14100', '14110', '14120', '14130', '14150', '14160' ]) {
                taxBase += rnu4Field5Accepted
            }
            //k10
            if (khy in ['11860', '11870', '11880', '11930', '11970', '12000', '13930', '14020', '14030', '14040', '14050', '14060', '14070', '14080', '14090', '14100', '14110', '14130', '14150', '14160']) {
                taxBase += rnu6Field10Sum
            }
            //k11
            if (khy in ['11860', '11870', '11880', '11930', '11970', '12000', '13930', '14020', '14030', '14040', '14050', '14060', '14070', '14080', '14090', '14100', '14110', '14130', '14150', '14160']) {
                taxBase -= rnu6Field12Accepted
            }
            //k12
            if (khy in ['13130', '13140' ]) {
                taxBase -= rnu4Field5Accepted
            }
            //k22
            if (khy in ['14000', '14010' ]) {
                taxBase -= rnu4Field5Accepted
            }
        }
    }
    // Расходы сложные
    if (dataComplexConsumption != null) {
        for (row in dataComplexConsumption.getAllCached()) {
            String khy = row.getCell('consumptionTypeId').hasValueOwner() ? row.getCell('consumptionTypeId').getValueOwner().value : row.getCell('consumptionTypeId').value

            // 9
            BigDecimal consumptionTaxSumS = (BigDecimal)(row.getCell('consumptionTaxSumS').hasValueOwner() ? row.getCell('consumptionTaxSumS').getValueOwner().value : row.getCell('consumptionTaxSumS').value)
            consumptionTaxSumS = consumptionTaxSumS ?: 0

            //k14
            if (khy in ['21780']) {
                taxBase += consumptionTaxSumS
            }
            //k17
            if (khy in ['21500']) {
                taxBase += consumptionTaxSumS
            }
            //k18
            if (khy in ['21510']) {
                taxBase += consumptionTaxSumS
            }
            //k20
            if (khy in ['21390']) {
                taxBase += consumptionTaxSumS
            }
            //k23
            if (khy in ['20320', '20321', '20470', '20750', '20755', '20760', '20765', '20770', '20775', '20780', '20785', '21210', '21280', '21345', '21355', '21365', '21370', '21375', '21380' ]) {
                taxBase -= consumptionTaxSumS
            }
            //k27
            if (khy in ['21450', '21740', '21750']) {
                taxBase -= consumptionTaxSumS
            }
            //k28
            if (khy in ['21770']) {
                taxBase -= consumptionTaxSumS
            }
            //k29
            if (khy in ['21662', '21664', '21666', '21668', '21670', '21672', '21674', '21676', '21678', '21680']) {
                taxBase -= consumptionTaxSumS
            }
            //k30
            if (khy in ['21520', '21530']) {
                taxBase -= consumptionTaxSumS
            }
            //k31
            if (khy in ['22500', '22505', '22585', '22590', '22595', '22660', '22664', '22668', '22670', '22690', '22695', '22700', '23120', '23130', '23140', '23240']) {
                taxBase -= consumptionTaxSumS
            }
            //k35
            if (khy in ['22492']) {
                taxBase += consumptionTaxSumS
            }
            //k36
            if (khy in ['23150']) {
                taxBase += consumptionTaxSumS
            }
            //k37
            if (khy in ['23160']) {
                taxBase += consumptionTaxSumS
            }
            //k38
            if (khy in ['23170']) {
                taxBase += consumptionTaxSumS
            }
            //k39
            if (khy in ['21760']) {
                taxBase -= consumptionTaxSumS
            }
            //k40
            if (khy in ['21460']) {
                taxBase -= consumptionTaxSumS
            }
            //k41
            if (khy in ['21470']) {
                taxBase -= consumptionTaxSumS
            }
            //k42
            if (khy in ['21385']) {
                taxBase -= consumptionTaxSumS
            }
        }
    }
    // Расходы простые
    if (dataSimpleConsumption != null) {
        for (row in dataSimpleConsumption.getAllCached()) {
            String khy = row.getCell('consumptionTypeId').hasValueOwner() ? row.getCell('consumptionTypeId').getValueOwner().value : row.getCell('consumptionTypeId').value

            // 8
            BigDecimal rnu5Field5Accepted = (BigDecimal)(row.getCell('rnu5Field5Accepted').hasValueOwner() ? row.getCell('rnu5Field5Accepted').getValueOwner().value : row.getCell('rnu5Field5Accepted').value)
            rnu5Field5Accepted = rnu5Field5Accepted ?: 0

            // 5
            BigDecimal rnu7Field10Sum = (BigDecimal)(row.getCell('rnu7Field10Sum').hasValueOwner() ? row.getCell('rnu7Field10Sum').getValueOwner().value : row.getCell('rnu7Field10Sum').value)
            rnu7Field10Sum = rnu7Field10Sum ?: 0

            // 6
            BigDecimal rnu7Field12Accepted = (BigDecimal)(row.getCell('rnu7Field12Accepted').hasValueOwner() ? row.getCell('rnu7Field12Accepted').getValueOwner().value : row.getCell('rnu7Field12Accepted').value)
            rnu7Field12Accepted = rnu7Field12Accepted ?: 0

            //k24
            if (khy in ['20291', '20300', '20310', '20330', '20332', '20334', '20336', '20338', '20339', '20340', '20360', '20364', '20368', '20370', '20430', '20434', '20438', '20440', '20442', '20446', '20448', '20450', '20452', '20454', '20456', '20458', '20460', '20464', '20468', '20475', '20480', '20485', '20490', '20500', '20510', '20520', '20530', '20540', '20550', '20690', '20694', '20698', '20700', '20710', '20810', '20812', '20814', '20816', '20820', '20825', '20830', '20840', '20850', '20860', '20870', '20880', '20890', '20920', '20940', '20945', '20950', '20960', '20970', '21020','21025', '21030', '21050', '21055', '21060', '21065', '21080', '21130', '21140', '21150', '21154', '21158', '21170', '21270', '21290', '21295', '21300', '21305', '21310', '21315', '21320', '21325', '21340', '21350', '21360', '21400', '21405', '21410', '21580', '21590', '21600', '21610', '21620', '21660', '21700', '21710', '21720', '21730', '21790', '21800', '21810']) {
                taxBase -= rnu5Field5Accepted
            }
            //k25
            if (khy in ['20300', '20360', '20370', '20430', '20434', '20438', '20440', '20442', '20446', '20448', '20450', '20452', '20454', '20456', '20458', '20460', '20464', '20468', '20475', '20480', '20485', '20490', '20500',  '20530',  '20540', '20550', '20690', '20694', '20698', '20700', '20710', '20810', '20812', '20814', '20816', '20825', '20830', '20840', '20850', '20870', '20880', '20890', '20950', '20960', '20970', '21020', '21025', '21030', '21050', '21055', '21060', '21065', '21080',  '21130', '21140', '21150', '21154', '21158', '21170', '21400', '21405', '21410', '21580', '21590', '21620', '21660', '21700', '21710', '21730', '21790', '21800', '21810']) {
                taxBase -= rnu7Field10Sum
            }
            //k26
            if (khy in ['20300', '20360', '20370', '20430', '20434', '20438', '20440', '20442', '20446', '20448', '20450', '20452', '20454', '20456', '20458', '20460', '20464', '20468', '20475', '20480', '20485', '20490', '20500',  '20530',  '20540', '20550', '20690', '20694', '20698', '20700', '20710', '20810', '20812', '20814', '20816', '20825', '20830', '20840', '20850', '20870', '20880', '20890', '20950', '20960', '20970', '21020', '21025', '21030', '21050', '21055', '21060', '21065', '21080',  '21130', '21140', '21150', '21154', '21158', '21170', '21400', '21405', '21410', '21580', '21590', '21620', '21660', '21700', '21710', '21730', '21790', '21800', '21810']) {
                taxBase += rnu7Field12Accepted
            }
            //k32
            if (khy in ['22000', '22010', '22020', '22030', '22040', '22050', '22060', '22070', '22080', '22090', '22100', '22110', '22120', '22130', '22140', '22150', '22160', '22170', '22180', '22190', '22200', '22210', '22220', '22230', '22240', '22250', '22260', '22270', '22280', '22290', '22300', '22310', '22320', '22330', '22340', '22350', '22360', '22370', '22380', '22385', '22390', '22395', '22400', '22405', '22410', '22415', '22420', '22425', '22430', '22435', '22440', '22445', '22450', '22455', '22460', '22465', '22470', '22475', '22480', '22485', '22490', '22496', '22498', '22530', '22534', '22538', '22540', '22544', '22548', '22550', '22560', '22565', '22570', '22575', '22580', '22600', '22610', '22640', '22680', '22710', '22715', '22720', '22750', '22760', '22800', '22810', '22840', '22850', '22860', '22870', '23040', '23050', '23100', '23110', '23200', '23210', '23220', '23230', '23250', '23260', '23270', '23280' ]) {
                taxBase -= rnu5Field5Accepted
            }
            //k33
            if (khy in ['22570', '22575', '22580', '22720', '22750', '22760', '22800', '22810', '23200', '23210', '23230', '23250', '23260', '23270', '23280']) {
                taxBase -= rnu7Field10Sum
            }
            //k34
            if (khy in ['22570', '22575', '22580', '22720', '22750', '22760', '22800', '22810', '23200', '23210', '23230', '23250', '23260', '23270', '23280']) {
                taxBase += rnu7Field12Accepted
            }
        }
    }
    // taxBase = распределяемая налоговая база за отчётный период
    return taxBase
}

/**
 * Посчитать значение графы 13..21.
 *
 * @param row строка
 * @param sumNal значение из настроек подраздления "Сумма налога на прибыль, выплаченная за пределами Российской Федерации в отчётном периоде"
 * @param reportPeriod отчетный период
 */
void calcColumnFrom13To21(def row, def sumNal, def reportPeriod) {
    def tmp

    // графа 13
    row.taxSum = (row.baseTaxOfRub > 0 ? roundValue(row.baseTaxOfRub * getTaxRateAttribute(row.subjectTaxStavka) / 100, 0) : 0)

    // графа 14
    row.taxSumOutside = roundValue(sumNal * row.baseTaxOf / 100, 0)

    // графа 15
    row.taxSumToPay = (row.taxSum > row.subjectTaxCredit + row.taxSumOutside ?
        row.taxSum - (row.subjectTaxCredit + row.taxSumOutside) : 0)

    // графа 16
    row.taxSumToReduction = (row.taxSum < row.subjectTaxCredit + row.taxSumOutside ?
        (row.subjectTaxCredit + row.taxSumOutside) - row.taxSum: 0)

    // графа 19
    row.everyMonthForSecondKvartalNextPeriod = (reportPeriod.order == 1 ? row.taxSum : 0)

    // графа 20
    row.everyMonthForThirdKvartalNextPeriod =
        (reportPeriod.order == 2 ? row.taxSum - row.everyMonthForSecondKvartalNextPeriod : 0)

    // графа 21
    row.everyMonthForFourthKvartalNextPeriod =
        (reportPeriod.order == 3 ? row.taxSum - row.everyMonthForThirdKvartalNextPeriod : 0)

    // графа 17 и 18 расчитывается в конце потому что требует значения графы 19, 20, 21
    // графа 17
    switch (reportPeriod.order) {
        case 1:
            tmp = row.everyMonthForSecondKvartalNextPeriod
            break
        case 2:
            tmp = row.everyMonthForThirdKvartalNextPeriod
            break
        case 3:
            tmp = row.everyMonthForFourthKvartalNextPeriod
            break
        default:
            // налоговый период
            tmp = 0
    }
    row.everyMontherPaymentAfterPeriod = tmp

    // графа 18
    row.everyMonthForKvartalNextPeriod = (reportPeriod.order == 3 ? row.everyMontherPaymentAfterPeriod : 0)
}

/**
 * Найти строку ЦА
 */
def findCA(def dataRows) {
    def resultRow = null
    long centralId = 113 // ID Центрального аппарата
    if (dataRows != null) {
        for (def row : dataRows) {
            if (row.regionBank == centralId) {
                resultRow = row
            }
        }
    }
    return resultRow
}

/**
 * Получить значение атрибута строки справочника.
 *
 * @param record строка справочника
 * @param alias алиас
 */
def getValue(def record, def alias) {
    def value = record.get(alias)
    switch (value.getAttributeType()) {
        case RefBookAttributeType.DATE :
            return value.getDateValue()
        case RefBookAttributeType.NUMBER :
            return value.getNumberValue()
        case RefBookAttributeType.STRING :
            return value.getStringValue()
        case RefBookAttributeType.REFERENCE :
            return value.getReferenceValue()
    }
    return null
}

/**
 * Округляет число до требуемой точности.
 *
 * @param value округляемое число
 * @param precision точность округления, знаки после запятой
 * @return округленное число
 */
def roundValue(BigDecimal value, def precision) {
    value.setScale(precision, BigDecimal.ROUND_HALF_UP)
}

/**
 * Получить атрибут 200 - "Ставка налога" справочник 33 - "Параметры подразделения по налогу на прибыль".
 *
 * @param id идентификатор записи справочника
 */
def getTaxRateAttribute(def id) {
    return getRefBookValue(33, id)?.TAX_RATE?.getNumberValue()
}