/**
 * Форма "Расшифровка видов расходов, учитываемых в простых РНУ (расходы простые)".
 *
 * @version 46
 */

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
    // проверить при переводе в утверждена
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :
        checkAndCalc()
        break
    // принять из утверждена
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED :
        checkAndCalc()
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
    // после вернуть из "Принята" в "Утверждена"
    case FormDataEvent.AFTER_MOVE_ACCEPTED_TO_APPROVED :
        checkDeclarationBankOnCancelAcceptance()
        break
}

// графа  1 - consumptionTypeId
// графа  2 - consumptionGroup
// графа  3 - consumptionTypeByOperation
// графа  4 - consumptionAccountNumber
// графа  5 - rnu7Field10Sum
// графа  6 - rnu7Field12Accepted
// графа  7 - rnu7Field12PrevTaxPeriod
// графа  8 - rnu5Field5Accepted
// графа  9 - logicalCheck
// графа 10 - accountingRecords
// графа 11 - opuSumByEnclosure2
// графа 12 - opuSumByTableP
// графа 13 - opuSumTotal
// графа 14 - difference

/**
 * Проверить и расчитать.
 */
void checkAndCalc() {
    calculationBasicSum()
}

/**
 * Вычисление сумм.
 */
void calculationBasicSum() {

//    formData.dataRows.each { row ->
//        ['rnu7Field10Sum', 'rnu7Field12Accepted',
//                'rnu7Field12PrevTaxPeriod', 'rnu5Field5Accepted'].each {
//            def cell = row.getCell(it)
//            if (cell.isEditable()) {
//                cell.setValue(1)
//            }
//        }
//    }

    /*
     * Проверка объязательных полей
     */
    def requiredColumns = ['rnu7Field10Sum', 'rnu7Field12Accepted', 'rnu7Field12PrevTaxPeriod', 'rnu5Field5Accepted']
    for (def row : formData.dataRows) {
        if (!checkRequiredColumns(row, requiredColumns, true)) {
            return
        }
    }

    /*
     * Расчет сумм
     */
    def row50001 = formData.getDataRow('R107')
    def row50002 = formData.getDataRow('R212')

    // суммы для графы 5..8
    ['rnu7Field10Sum', 'rnu7Field12Accepted',
            'rnu7Field12PrevTaxPeriod', 'rnu5Field5Accepted'].each { alias ->
        row50001.getCell(alias).setValue(getSum(alias, 'R2', 'R106'))
        row50002.getCell(alias).setValue(getSum(alias, 'R109', 'R211'))
    }

    calculationControlGraphs()
}

/**
 * Скрипт для заполнения контрольных полей.
 *
 * В текущей таблице нет 10й графы, следственно
 * нужно учесть что графы > 10 считаются "-1"
 *
 * @author rtimerbaev
 * @since 21.03.2013 13:00
 * @version 14 05.03.2013
 */
void calculationControlGraphs() {
    def message = 'ТРЕБУЕТСЯ ОБЪЯСНЕНИЕ'
    def tmp
    def value
    def formDataComplex = getFormDataComplex()
    def income102NotFound = []
    for (def row : formData.dataRows) {
        // исключить итоговые строки
        if (row.getAlias() in ['R107', 'R212']) {
            continue
        }
        if (!isEmpty(row.rnu7Field10Sum) && !isEmpty(row.rnu7Field12Accepted) &&
                !isEmpty(row.rnu7Field12PrevTaxPeriod)) {
            // графы 9 = ОКРУГЛ(«графа 5» - («графа 6» - «графа 7»); 2)
            tmp = round(row.rnu7Field10Sum - (row.rnu7Field10Sum - row.rnu7Field12Accepted), 2)
            value = ((BigDecimal) tmp).setScale(2, BigDecimal.ROUND_HALF_UP)
            row.logicalCheck = (tmp < 0 ? message : value.toString())
        }

        // графа 11
        row.opuSumByEnclosure2 = getSumFromComplex(formDataComplex,
                'consumptionBuhSumAccountNumber', 'consumptionBuhSumAccepted', row.consumptionAccountNumber)

        // графа 12
        if (row.getAlias() in ['R105', 'R209']) {
            tmp = calcColumn6(['R105', 'R209'])
        } else if (row.getAlias() in ['R106', 'R211']) {
            tmp = calcColumn6(['R106', 'R211'])
        } else if (row.getAlias() in ['R104', 'R208']) {
            tmp = calcColumn6(['R104', 'R208'])
        } else {
            tmp = row.rnu5Field5Accepted
        }
        row.opuSumByTableP = tmp

        // графа 13
        def income102 = income102Dao.getIncome102(formData.reportPeriodId, row.accountingRecords, formData.departmentId)
        if (income102 == null || income102.isEmpty()) {
            income102NotFound += getIndex(row)
            tmp = 0
        } else {
            tmp = (income102[0] != null ? income102[0].getTotalSum() : 0)
        }
        row.opuSumTotal = tmp

        // графа 14
        row.difference = (getValue(row.opuSumByEnclosure2) + getValue(row.opuSumByTableP)) - getValue(row.opuSumTotal)
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
 * @since 21.02.2013 13:40
 */
void checkCreation() {
    def findForm = FormDataService.find(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId)

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
    if (!isBank()) {
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
    if (!isBank()) {
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
 * @since 21.02.2013 13:50
 */
void consolidation() {
    if (isTerBank()) {
        return
    }
    // очистить форму
    formData.getDataRows().each { row ->
        ['rnu7Field10Sum', 'rnu7Field12Accepted', 'rnu7Field12PrevTaxPeriod', 'rnu5Field5Accepted'].each { alias->
            row.getCell(alias).setValue(null)
        }
    }

    def needCalc = false

    // получить консолидированные формы в дочерних подразделениях в текущем налоговом периоде
    departmentFormTypeService.getSources(formDataDepartment.id, formData.getFormType().getId(), FormDataKind.SUMMARY).each {
        def child = FormDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (child != null && child.state == WorkflowState.ACCEPTED && child.formType.id == 304) {
            needCalc = true
            for (def row : child.getDataRows()) {
                if (row.getAlias() == null) {
                    continue
                }
                def rowResult = formData.getDataRow(row.getAlias())
                ['rnu7Field10Sum', 'rnu7Field12Accepted', 'rnu7Field12PrevTaxPeriod', 'rnu5Field5Accepted'].each {
                    if (row.getCell(it).getValue() != null) {
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

/**
 * Получить сумму диапазона строк определенного столбца.
 */
def getSum(String columnAlias, String rowFromAlias, String rowToAlias) {
    def from = formData.getDataRowIndex(rowFromAlias) + 1
    def to = formData.getDataRowIndex(rowToAlias) - 1
    if (from > to) {
        return 0
    }
    return summ(formData, new ColumnRange(columnAlias, from, to))
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
 * Получить номер строки в таблице.
 */
def getIndex(def row) {
    formData.dataRows.indexOf(row)
}

/**
 * Проверка пустое ли значение.
 */
def isEmpty(def value) {
    return value == null || value == ''
}

/**
 * Получить сумму значений из расходов сложных.
 *
 * @param data данные формы
 * @param columnAliasCheck алиас графы, по которой отбираются строки для суммирования
 * @param columnAliasSum алиас графы, значения которой суммируются
 * @param value значение, по которому отбираются строки для суммирования
 */
def getSumFromComplex(data, columnAliasCheck, columnAliasSum, value) {
    def sum = 0
    if (data != null && (columnAliasCheck != null || columnAliasCheck != '') && value != null) {
        for (def row : data.dataRows) {
            if (row.getCell(columnAliasCheck).getValue() == value) {
                sum += (row.getCell(columnAliasSum).getValue() ?: 0)
            }
        }
    }
    return sum
}

/**
 * Получить значение для графы 12. Сумма значении графы 6 указанных строк
 *
 * @param aliasRows список алиасов значения которых надо просуммировать
 */
def calcColumn6(def aliasRows) {
    def sum = 0
    aliasRows.each { alias ->
        sum += formData.getDataRow(alias).rnu7Field12Accepted
    }
    return sum
}

/**
 * Получить данные формы "расходы сложные" (id = 303)
 */
def getFormDataComplex() {
    return FormDataService.find(303, formData.kind, formDataDepartment.id, formData.reportPeriodId)
}

/**
 * Получить значение или ноль.
 *
 * @param value значение которое надо проверить
 */
def getValue(def value) {
    return value ?: 0
}