/**
 * Форма "Сводная форма начисленных расходов (расходы сложные)".
 *
 * @version 46
 */


if (formData.id != null) dataRowsHelper = formDataService.getDataRowHelper(formData)

switch (formDataEvent) {
    // создать
    case FormDataEvent.CREATE :
        checkCreation()
        break
    // расчитать
    case FormDataEvent.CALCULATE :
        checkAndCalc()
        break
    // обобщить
    case FormDataEvent.COMPOSE :
        consolidation()
        break
    // проверить
    case FormDataEvent.CHECK :
        checkAndCalc()
        break
    // утвердить
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :
        checkAndCalc()
        break
    // принять из утверждена
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED :
        checkAndCalc()
        break
    // вернуть из принята в утверждена
    case FormDataEvent.MOVE_ACCEPTED_TO_APPROVED :
        checkDeclarationBankOnCancelAcceptance()
        break
    // принять из создана
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :
        checkAndCalc()
        checkDeclarationBankOnAcceptance()
        break
    // вернуть из принята в создана
    case FormDataEvent.MOVE_ACCEPTED_TO_CREATED :
        checkDeclarationBankOnCancelAcceptance()
        break
    // после принятия из утверждена
    case FormDataEvent.AFTER_MOVE_APPROVED_TO_ACCEPTED :
        break
    // после вернуть из "Принята" в "Утверждена"
    case FormDataEvent.AFTER_MOVE_ACCEPTED_TO_APPROVED :
        checkDeclarationBankOnCancelAcceptance()
        break
}

// графа  1 - consumptionTypeId
// графа  2 - consumptionGroup
// графа  3 - consumptionTypeByOperation
// графа  4 - consumptionBuhSumAccountNumber
// графа  5 - consumptionBuhSumRnuSource
// графа  6 - consumptionBuhSumAccepted
// графа  7 - consumptionBuhSumPrevTaxPeriod
// графа  8 - consumptionTaxSumRnuSource
// графа  9 - consumptionTaxSumS
// графа 10 - rnuNo
// графа 11 - logicalCheck
// графа 12 - accountingRecords
// графа 13 - opuSumByEnclosure3
// графа 14 - opuSumByTableP
// графа 15 - opuSumTotal
// графа 16 - difference

/**
 * Проверить и расчитать.
 */
void checkAndCalc() {
    calculation()
}

/**
 * Расчет.
 */
void calculation() {
    def needExit = true
    if ((formDataEvent == FormDataEvent.COMPOSE && isBank()) || formDataEvent != FormDataEvent.COMPOSE) {
        needExit = false
    }
    if (needExit) {
        return
    }

//    formData.dataRows.each { row ->
//        ['consumptionBuhSumAccepted', 'consumptionBuhSumPrevTaxPeriod', 'consumptionTaxSumS'].each {
//            def cell = row.getCell(it)
//            if (cell.isEditable()) {
//                cell.setValue(1)
//            }
//        }
//    }

    /*
     * Проверка объязательных полей
     */
    def requiredColumns = ['consumptionBuhSumAccepted', 'consumptionBuhSumPrevTaxPeriod', 'consumptionTaxSumS']
    for (def row : dataRowsHelper.getAllCached()) {
        if (!checkRequiredColumns(row, requiredColumns, true)) {
            return
        }
    }

    /*
     * Расчет сумм
     */
    def totalRow1 = dataRowsHelper.getDataRow(dataRowsHelper.getAllCached(), 'R67')
    def totalRow2 = dataRowsHelper.getDataRow(dataRowsHelper.getAllCached(), 'R93')

    // суммы для графы 9
    ['consumptionTaxSumS'].each { alias ->
        totalRow1.getCell(alias).setValue(getSum(alias, 'R2', 'R66'))
        totalRow2.getCell(alias).setValue(getSum(alias, 'R69', 'R92'))
    }

    calculationControlGraphs()
    dataRowsHelper.save(dataRowsHelper.getAllCached())
}

/**
 * Расчет (контрольные графы).
 */
void calculationControlGraphs() {
    def needExit = true
    if ((formDataEvent == FormDataEvent.COMPOSE && isBank()) || formDataEvent != FormDataEvent.COMPOSE) {
        needExit = false
    }
    if (needExit) {
        return
    }

    def message = 'ТРЕБУЕТСЯ ОБЪЯСНЕНИЕ'
    def tmp
    def value
    def formDataSimple = getFormDataSimple()
    def income102NotFound = []
    for (def row : dataRowsHelper.getAllCached()) {
        // исключить итоговые строки
        if (row.getAlias() in ['R67', 'R93']) {
            continue
        }
        if (!isEmpty(row.consumptionTaxSumS) && !isEmpty(row.consumptionBuhSumAccepted) &&
                !isEmpty(row.consumptionBuhSumPrevTaxPeriod)) {
            // ОКРУГЛ( «графа9»-(Сумма 6-Сумма 7);2),
            sum6 = 0
            sum7 = 0
            for (rowSum in dataRowsHelper.getAllCached()) {
                String knySum
                String kny
                if (rowSum.getCell('consumptionTypeId').hasValueOwner()) {
                    knySum = rowSum.getCell('consumptionTypeId').valueOwner.value
                } else {
                    knySum = rowSum.getCell('consumptionTypeId').value
                }
                if (row.getCell('consumptionTypeId').hasValueOwner()) {
                    kny = row.getCell('consumptionTypeId').valueOwner.value
                } else {
                    kny = row.getCell('consumptionTypeId').value
                }
                if (kny == knySum) {
                    sum6 += rowSum.consumptionBuhSumAccepted
                    sum7 += rowSum.consumptionBuhSumPrevTaxPeriod
                }
            }
            tmp = round(row.consumptionTaxSumS - (sum6 - sum7), 2)
            value = ((BigDecimal) tmp).setScale(2, BigDecimal.ROUND_HALF_UP)
            row.logicalCheck = (tmp < 0 ? message : value.toString())
        }

        if (!isEmpty(row.consumptionBuhSumAccepted) && !isEmpty(row.consumptionBuhSumPrevTaxPeriod)) {
            // графа 13
            if (row.getAlias() in ['R3', 'R11']) {
                tmp = calcColumn6(['R3', 'R11'])
            } else {
                tmp = row.consumptionBuhSumAccepted
            }
            row.opuSumByEnclosure3 = tmp

            // графа 14
            row.opuSumByTableP = getSumFromSimple(formDataSimple, 'consumptionAccountNumber',
                    'rnu5Field5Accepted', row.consumptionBuhSumAccountNumber)

            // графа 15
            def income102 = income102Dao.getIncome102(formData.reportPeriodId, row.accountingRecords)
            if (income102 == null || income102.isEmpty()) {
                income102NotFound += getIndex(row)
                tmp = 0
            } else {
                tmp = (income102[0] != null ? income102[0].getTotalSum() : 0)
            }
            row.opuSumTotal = tmp

            // графа 16
            row.difference = (getValue(row.opuSumByEnclosure3) + getValue(row.opuSumByTableP)) - getValue(row.opuSumTotal)
        }
    }

    if (!income102NotFound.isEmpty()) {
        def rows = income102NotFound.join(', ')
        logger.warn("Не найдены соответствующие данные в отчете о прибылях и убытках для строк: $rows")
    }
}

/**
 * Скрипт для проверки создания.
 *
 * @author rtimerbaev
 * @since 22.02.2013 12:30
 */
void checkCreation() {
    def findForm = formDataService.find(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }

    if (formData.kind != FormDataKind.SUMMARY) {
        logger.error("Нельзя создавать форму с типом ${formData.kind?.name}")
    }
}

/**
 * Проверки наличия декларации Банка при принятии нф.
 *
 * @author rtimerbaev
 * @since 21.03.2013 11:00
 */
void checkDeclarationBankOnAcceptance() {
    if (isTerBank()) {
        return
    }
    departmentFormTypeService.getDeclarationDestinations(formData.getDepartmentId(), formData.getFormType().getId(), FormDataKind.SUMMARY).each { department ->
        def bank = declarationService.find(2, department.departmentId, formData.reportPeriodId)
        if (bank != null && bank.accepted) {
            logger.error('Принятие налоговой формы невозможно, т.к. уже принята декларация Банка.')
        }
    }
}

/**
 * Проверки наличия декларации Банка при отмене принятия нф.
 *
 * @author rtimerbaev
 * @since 21.03.2013 11:00
 */
void checkDeclarationBankOnCancelAcceptance() {
    if (isTerBank()) {
        return
    }
    departmentFormTypeService.getDeclarationDestinations(formData.getDepartmentId(), formData.getFormType().getId(), FormDataKind.SUMMARY).each { department ->
        def bank = declarationService.find(2, department.departmentId, formData.reportPeriodId)
        if (bank != null && bank.accepted) {
            logger.error('Отмена принятия налоговой формы невозможно, т.к. уже принята декларация Банка.')
        }
    }
}

/**
 * Скрипт для консолидации.
 *
 * @author rtimerbaev
 * @since 22.02.2013 15:30
 */
void consolidation() {
    if (isTerBank()) {
        return
    }
    // очистить форму
    dataRowsHelper.getAllCached().each { row ->
        ['consumptionBuhSumAccepted', 'consumptionBuhSumPrevTaxPeriod', 'consumptionTaxSumS'].each { it ->
            row.getCell(it).setValue(null)
        }
    }

    def needCalc = false

    // получить консолидированные формы из источников
    departmentFormTypeService.getSources(formDataDepartment.id, formData.getFormType().getId(), FormDataKind.SUMMARY).each {
        def child = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (child != null && child.state == WorkflowState.ACCEPTED && child.formType.id == 303) {
            needCalc = true
            for (def row : formDataService.getDataRowHelper(child).getAllCached()) {
                if (row.getAlias() == null) {
                    continue
                }
                def rowResult = dataRowsHelper.getDataRow(dataRowsHelper.getAllCached(), row.getAlias())
                ['consumptionBuhSumAccepted', 'consumptionBuhSumPrevTaxPeriod', 'consumptionTaxSumS'].each {
                    if (row.getCell(it).getValue() != null && !row.getCell(it).hasValueOwner()) {
                        rowResult.getCell(it).setValue(summ(rowResult.getCell(it), row.getCell(it)))
                    }
                }
            }
        }
    }
    if (needCalc) {
        checkAndCalc()
    }
    logger.info('Формирование сводной формы уровня Банка прошло успешно.')
    dataRowsHelper.save(dataRowsHelper.allCached)
    dataRowsHelper.commit()
}


/*
 * Вспомогательные методы.
 */

/**
 * Проверка на банк.
 */
def isBank() {
    boolean isBank = true
    departmentFormTypeService.getDestinations(formData.departmentId, formData.formTemplateId, FormDataKind.SUMMARY).each {
        if (it.departmentId != formData.departmentId) {
            isBank = false
        }
    }
    return isBank
}

/**
 * Проверка на террбанк.
 */
def isTerBank() {
    boolean isTerBank = false
    departmentFormTypeService.getFormDestinations(formData.departmentId, formData.formTemplateId, FormDataKind.SUMMARY).each {
        if (it.departmentId != formData.departmentId) {
            isTerBank = true
        }
    }
    return isTerBank
}

double summ(String columnName, String fromRowA, String toRowA) {
    def from = dataRowsHelper.getDataRowIndex(dataRowsHelper.getAllCached(), fromRowA)
    def to = dataRowsHelper.getDataRowIndex(dataRowsHelper.getAllCached(), toRowA)
    if (from > to) {
        return 0
    }
    def result = summ(formData, dataRowsHelper.getAllCached(), new ColumnRange(columnName, from, to))
    return result ?: 0;
}

/**
 * Проверить заполненость обязательных полей.
 *
 * @param row строка
 * @param columns список обязательных графов
 * @param useLog нужно ли записывать сообщения в лог
 * @return true - все хорошо, false - есть незаполненные поля
 */
def checkRequiredColumns(def row, def columns, def useLog) {
    def colNames = []

    def cell
    columns.each {
        cell = row.getCell(it)
        if (cell.isEditable() && (cell.getValue() == null || row.getCell(it).getValue() == '')) {
            def name = getColumnName(row, it)
            colNames.add('"' + name + '"')
        }
    }
    if (!colNames.isEmpty()) {
        if (!useLog) {
            return false
        }
        def index = getIndex(row) + 1
        def errorMsg = colNames.join(', ')
        logger.error("В строке $index не заполнены колонки : $errorMsg.")
        return false
    }
    return true
}

/**
 * Получить сумму диапазона строк определенного столбца.
 */
def getSum(String columnAlias, String rowFromAlias, String rowToAlias) {
    def from = dataRowsHelper.getDataRowIndex(dataRowsHelper.getAllCached(), rowFromAlias)
    def to = dataRowsHelper.getDataRowIndex(dataRowsHelper.getAllCached(), rowToAlias)
    if (from > to) {
        return 0
    }
    return summ(formData, dataRowsHelper.getAllCached(), new ColumnRange(columnAlias, from, to))
}

/**
 * Получить название графы по псевдониму.
 *
 * @param row строка
 * @param alias псевдоним графы
 */
def getColumnName(def row, def alias) {
    if (row != null && alias != null) {
        return row.getCell(alias).getColumn().getName().replace('%', '%%')
    }
    return ''
}

/**
 * Получить значение или ноль.
 *
 * @param value значение которое надо проверить
 */
def getValue(def value) {
    return value ?: 0
}

/**
 * Получить номер строки в таблице.
 */
def getIndex(def row) {
    dataRowsHelper.getAllCached().indexOf(row)
}

/**
 * Проверка пустое ли значение.
 */
def isEmpty(def value) {
    return value == null || value == ''
}

/**
 * Получить значение для графы 13. Сумма значении графы 6 указанных строк
 *
 * @param aliasRows список алиасов значения которых надо просуммировать
 */
def calcColumn6(def aliasRows) {
    def sum = 0
    aliasRows.each { alias ->
        sum += dataRowsHelper.getDataRow(dataRowsHelper.getAllCached(), alias).consumptionBuhSumAccepted
    }
    return sum
}

/**
 * Получить данные формы "расходы простые" (id = 304)
 */
def getFormDataSimple() {
    return formDataService.find(304, formData.kind, formDataDepartment.id, formData.reportPeriodId)
}

/**
 * Получить сумму значений из расходов простых.
 *
 * @param data данные формы
 * @param columnAliasCheck алиас графы, по которой отбираются строки для суммирования
 * @param columnAliasSum алиас графы, значения которой суммируются
 * @param value значение, по которому отбираются строки для суммирования
 */
def getSumFromSimple(data, columnAliasCheck, columnAliasSum, value) {
    def sum = 0
    if (data != null && (columnAliasCheck != null || columnAliasCheck != '') && value != null) {
        for (def row : formDataService.getDataRowHelper(data).getAllCached()) {
            if (row.getCell(columnAliasCheck).getValue() == value) {
                sum += (row.getCell(columnAliasSum).getValue() ?: 0)
            }
        }
    }
    return sum
}