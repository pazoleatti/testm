/**
 * Форма "Сводная форма начисленных расходов (расходы сложные)".
 */

import java.text.DecimalFormat

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
        checkAndCalc()
        break
    // проверить
    case FormDataEvent.CHECK :
        checkAndCalc()
        break
    // утвердить
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :
        checkAndCalc()
        checkOnApproval()
        break
    // принять из утверждена
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED :
        checkAndCalc()
        checkOnAcceptance()
        break
    // вернуть из принята в утверждена
    case FormDataEvent.MOVE_ACCEPTED_TO_APPROVED :
        checkDeclarationBankOnCancelAcceptance()
        checkOnCancelAcceptance()
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
        acceptance()
        break
    // после вернуть из "Принята" в "Утверждена"
    case FormDataEvent.AFTER_MOVE_ACCEPTED_TO_APPROVED :
        checkDeclarationBankOnCancelAcceptance()
        acceptance()
        break
}

// графа  1 - incomeTypeId
// графа  2 - incomeGroup
// графа  3 - incomeTypeByOperation
// графа  4 - incomeBuhSumAccountNumber
// графа  5 - incomeBuhSumRnuSource
// графа  6 - incomeBuhSumAccepted
// графа  7 - incomeBuhSumPrevTaxPeriod
// графа  8 - incomeTaxSumRnuSource
// графа  9 - incomeTaxSumS
// графа 10 - rnuNo
// графа 11 - logicalCheck
// графа 12 - opuSumByEnclosure2
// графа 13 - opuSumByTableD
// графа 14 - opuSumTotal
// графа 15 - opuSumByOpu
// графа 16 - difference

/**
 * Проверить и расчитать.
 */
void checkAndCalc() {
    checkRequiredFields()
    getDataFromSimpleOutcome()
    consolidationCalc()
    calculationBasicSum()
    calculationControlGraphs()
}

/**
 * Для перевода сводной налогой формы в статус "принят".
 *
 * @author rtimerbaev
 * @since 22.02.2013 12:50
 */
void acceptance() {
    departmentFormTypeService.getDestinations(formDataDepartment.id, formData.getFormType().getId(), FormDataKind.SUMMARY).each{
        formDataCompositionService.compose(formData, it.departmentId, it.formTypeId, it.kind, logger)
    }
}

/**
 * Расчет (основные графы).
 *
 * @since 19.03.2013 14:00
 */
void calculationBasicSum() {
    def needExit = true
    if ((formDataEvent == FormDataEvent.COMPOSE && isBank()) || formDataEvent != FormDataEvent.COMPOSE) {
        needExit = false
    }
    if (needExit) {
        return
    }

    // A1
    ['consumptionBuhSumAccepted', 'consumptionTaxSumS'].each {
        setSum('R2', 'R95', it)
    }

    // Б
    ['consumptionBuhSumPrevTaxPeriod', 'consumptionTaxSumS'].each {
        setSum('R96', 'R111', it)
    }

    // А2
    ['consumptionBuhSumAccepted', 'consumptionTaxSumS'].each {
        setSum('R113', 'R142', it)
    }

    // д
    setSum('R143', 'R148', 'consumptionTaxSumS')
}

/**
 * Расчет (контрольные графы).
 *
 * @author auldanov
 * @author rtimerbaev
 * @since 21.03.2013 14:00
 */
void calculationControlGraphs() {
    def needExit = true
    if ((formDataEvent == FormDataEvent.COMPOSE && isBank()) || formDataEvent != FormDataEvent.COMPOSE) {
        needExit = false
    }
    if (needExit) {
        return
    }
    def row
    def a, b, c

    // 95 строка
    row = formData.getDataRow('R95')
    // графа 11
    a = summ('consumptionBuhSumAccepted', 'R3', 'R92')
    b = getCellValue('R94', 'consumptionBuhSumAccepted')
    row.logicalCheck = ((BigDecimal) (a + b)).setScale(2, BigDecimal.ROUND_HALF_UP).toString()
    // графа 12
    a = summ('consumptionTaxSumS', 'R3', 'R92')
    b = getCellValue('R94', 'opuSumByEnclosure3')
    c = getCellValue('R93', 'opuSumByEnclosure3')
    row.opuSumByEnclosure3 = a + b + c
    // графа 13 = графа 11 - графа 6
    if (row.logicalCheck != null && row.consumptionBuhSumAccepted != null) {
        row.opuSumByTableP = toBigDecimal(row.logicalCheck) - row.consumptionBuhSumAccepted
    } else {
        row.opuSumByTableP = null
    }
    // графа 16 = графа 12 - графа 9
    if (row.opuSumByEnclosure3 != null && row.consumptionTaxSumS != null) {
        row.difference = row.opuSumByEnclosure3 - row.consumptionTaxSumS
    } else {
        row.difference = null
    }

    // 111 строка
    row = formData.getDataRow('R111')
    // графа 12
    a = summ('consumptionTaxSumS', 'R97', 'R109')
    b = getCellValue('R110', 'opuSumByEnclosure3')
    row.opuSumByEnclosure3 = ((BigDecimal) (a + b)).setScale(2, BigDecimal.ROUND_HALF_UP)
    // графа 13
    row.opuSumByTableP = getCellValue('R110', 'opuSumByTableP')
    // графа 14 = графа 13 - графа 7
    if (row.opuSumByTableP != null && row.consumptionBuhSumPrevTaxPeriod != null) {
        row.opuSumTotal = row.opuSumByTableP - row.consumptionBuhSumPrevTaxPeriod
    } else {
        row.opuSumTotal = null
    }
    // графа 16 = графа 12 - графа 9
    if (row.opuSumByEnclosure3 != null && row.consumptionTaxSumS != null) {
        row.difference = row.opuSumByEnclosure3 - row.consumptionTaxSumS
    } else {
        row.difference = null
    }

    // 142 строка
    row = formData.getDataRow('R142')
    // графа 11
    a = summ('consumptionBuhSumAccepted', 'R114', 'R139')
    b = toBigDecimal(getCell('R141', 'logicalCheck').getValue())
    row.logicalCheck = ((BigDecimal) (a + b)).setScale(2, BigDecimal.ROUND_HALF_UP).toString()
    // графа 12
    a = summ('consumptionTaxSumS', 'R114', 'R139')
    b = getCellValue('R141', 'opuSumByEnclosure3')
    c = getCellValue('R140', 'opuSumByEnclosure3')
    row.opuSumByEnclosure3 = a + b + c
    // графа 13 = графа 11 - графа 6
    if (row.logicalCheck != null && row.consumptionBuhSumAccepted != null) {
        row.opuSumByTableP = toBigDecimal(row.logicalCheck) - row.consumptionBuhSumAccepted
    } else {
        row.opuSumByTableP = null
    }
    // графа 16 = графа 12 - графа 9
    if (row.opuSumByEnclosure3 != null && row.consumptionTaxSumS != null) {
        row.difference = row.opuSumByEnclosure3 - row.consumptionTaxSumS
    } else {
        row.difference = null
    }

    //Для всех строк, для которых графа 11 является редактируемой, за исключением строк: 75 (21514), 76 (21515), 77(21518), 90(21657), 92(21659), 94, 141, 142
    ([3, 5] + (8..13) + [16] + [19,20,21] + (36..38) + (40..42) + [44] + (46..56) + [58] + (65..68) + [70, 80, 81, 83, 86, 87, 114, 116, 122, 123] + (131..134) + [139]).each {

        column6Range =  new ColumnRange('consumptionBuhSumAccepted', 0, formData.getDataRows().size() - 1)
        summ6Column = summ(formData, column6Range, {condRange ->
            return getCell('R' + it, 'consumptionTypeId').getValue() == condRange.getCell('consumptionTypeId').getValue() && condRange.getCell('consumptionBuhSumAccountNumber').getValue() != null
        })

        column7Range =  new ColumnRange('consumptionBuhSumPrevTaxPeriod', 0, formData.getDataRows().size() - 1)
        summ7Column = summ(formData, column7Range, {condRange ->
            return getCell('R' + it, 'consumptionTypeId').getValue() == condRange.getCell('consumptionTypeId').getValue()
        })

        summResult = ((BigDecimal)(getCellValue('R'+it, 'consumptionTaxSumS') - (summ6Column - summ7Column))).setScale(2, BigDecimal.ROUND_HALF_UP)
        getCell('R'+it, 'logicalCheck').setValue(
                summResult >= 0 ? summResult.toString() : 'Требуется объяснение'
        )
    }

    /**
     * Изменения в ЧТЗ 28.03.13
     * Строки выделились из вышестоящих
     */
    ((22..24)+[34,35]).each{
        column6Range =  new ColumnRange('consumptionBuhSumAccepted', 0, formData.getDataRows().size() - 1)
        summ6Column = summ(formData, column6Range, {condRange ->
            return getCell('R' + it, 'consumptionTypeId').getValue() == condRange.getCell('consumptionTypeId').getValue() && condRange.getCell('consumptionBuhSumAccountNumber').getValue() != null
        })

        column7Range =  new ColumnRange('consumptionBuhSumPrevTaxPeriod', 0, formData.getDataRows().size() - 1)
        summ7Column = summ(formData, column7Range, {condRange ->
            return getCell('R' + it, 'consumptionTypeId').getValue() == condRange.getCell('consumptionTypeId').getValue()
        })

        summResult = ((BigDecimal)(getCellValue('R'+it, 'consumptionTaxSumS') - (summ6Column - summ7Column))).setScale(2, BigDecimal.ROUND_HALF_UP)
        getCell('R'+it, 'logicalCheck').setValue(
                summResult <= 0 ? summResult.toString() : 'Требуется объяснение'
        )
    }

    // Сводная форма начисленных доходов уровня обособленного подразделения (доходы сложные)
    def formDataComplexIncome = FormDataService.find(302, formData.kind, formData.departmentId, formData.reportPeriodId)
    if (formDataComplexIncome != null) {

        // Строка 75, графа 11
        sum75 = (
        getCellValue('R75', 'consumptionTaxSumS')- (
        formDataComplexIncome.getDataRow('R13').getCell('incomeTaxSumS').getValue() ?: 0 -
                getCellValue('R72', 'consumptionTaxSumS') -
                getCellValue('R76', 'consumptionTaxSumS')
        )
        ).setScale(2, BigDecimal.ROUND_HALF_UP)

        getCell('R75', 'logicalCheck').setValue(sum75 == 0 ? '0' : 'Требуется объяснение')

        // Строка 76, графа 11
        sum76 = (
        getCellValue('R76', 'consumptionTaxSumS')- (
        formDataComplexIncome.getDataRow('R13').getCell('incomeTaxSumS').getValue() ?: 0 -
                getCellValue('R72', 'consumptionTaxSumS') -
                getCellValue('R76', 'consumptionTaxSumS')
        )
        ).setScale(2, BigDecimal.ROUND_HALF_UP)

        getCell('R76', 'logicalCheck').setValue(sum76 == 0 ? '0' : 'Требуется объяснение')

        // Строка 77, графа 11
        sum77 = (
        getCellValue('R77', 'consumptionTaxSumS')- (
        formDataComplexIncome.getDataRow('R14').getCell('incomeTaxSumS').getValue() ?: 0 +
                getCellValue('R73', 'consumptionTaxSumS')
        )
        ).setScale(2, BigDecimal.ROUND_HALF_UP)

        getCell('R77', 'logicalCheck').setValue(sum77 == 0 ? '0' : 'Требуется объяснение')

        // Строка 90, графа 11
        sum90 = (
        getCellValue('R90', 'consumptionTaxSumS')- (
        (formDataComplexIncome.getDataRow('R9').getCell('incomeTaxSumS').getValue() ?: 0) -
                (formDataComplexIncome.getDataRow('R10').getCell('incomeTaxSumS').getValue() ?: 0) +
                getCellValue('R92', 'consumptionTaxSumS')
        )
        ).setScale(2, BigDecimal.ROUND_HALF_UP)

        getCell('R90', 'logicalCheck').setValue(sum90 == 0 ? '0' : 'Требуется объяснение')

        // Строка 92, графа 11
        sum92 = (
        getCellValue('R92', 'consumptionTaxSumS')- (
        (formDataComplexIncome.getDataRow('R10').getCell('incomeTaxSumS').getValue() ?: 0) -
                (formDataComplexIncome.getDataRow('R9').getCell('incomeTaxSumS').getValue() ?: 0) +
                getCellValue('R90', 'consumptionTaxSumS')
        )
        ).setScale(2, BigDecimal.ROUND_HALF_UP)

        getCell('R92', 'logicalCheck').setValue(sum92 == 0 ? '0' : 'Требуется объяснение')
    } else {
        // Строка 75, графа 11
        getCell('R75', 'logicalCheck').setValue('0')

        // Строка 76, графа 11
        getCell('R76', 'logicalCheck').setValue('0')

        // Строка 77, графа 11
        getCell('R77', 'logicalCheck').setValue('0')

        // Строка 90, графа 11
        getCell('R90', 'logicalCheck').setValue('0')

        // Строка 92, графа 11
        getCell('R92', 'logicalCheck').setValue('0')
    }

    // получение нф «Сводная форма "Расшифровка видов расходов, учитываемых в простых РНУ" уровня обособленного подразделения» (расходы простые)
    def formDataSimpleConsumption = FormDataService.find(304, formData.kind, formData.departmentId, formData.reportPeriodId)

    /**
     * Для всех строк, для всех ячеек, обозначенных как вычисляемые.
     */
    ((3..25) + (34..59) + (65..70) + (80..84) + (86..88) + (114..117) + [122, 123] + (131..139)).each {
        // «графа 12» = сумма значений «графы 6» для тех строк, для которых значение «графы 4» равно значению «графы 4» текущей строки
        getCell('R' + it, 'opuSumByEnclosure3').setValue(
                summ(formData, new ColumnRange('consumptionBuhSumAccepted', 0, formData.getDataRows().size() - 1), {condRange ->
                    return getCell('R' + it, 'consumptionBuhSumAccountNumber').getValue() == condRange.getCell('consumptionBuhSumAccountNumber').getValue()
                })
        )

        /*
        * «графа 13» = сумма значений «графы 8» формы «Сводная форма 'Расшифровка видов расходов, учитываемых в простых РНУ' уровня обособленного подразделения»
        * (см. раздел 6.1.4) для тех строк, для которых значение «графы 4»  равно значению «графы 4» текущей строки текущей формы.
        */
        if (formDataSimpleConsumption != null) {
            getCell('R' + it, 'opuSumByTableP').setValue(
                    summ(formDataSimpleConsumption, new ColumnRange('rnu5Field5Accepted', 0, formDataSimpleConsumption.getDataRows().size() - 1), {condRange ->
                        return getCell('R' + it, 'consumptionBuhSumAccountNumber').getValue() == condRange.getCell('consumptionAccountNumber').getValue()
                    })
            )
        } else {
            getCell('R' + it, 'opuSumByTableP').setValue(0)
        }

        //«графа 14» = «графа12» + «графа13»
        getCell('R' + it, 'opuSumTotal').setValue(getCellValue('R' + it, 'opuSumByEnclosure3') + getCellValue('R' + it, 'opuSumByTableP'))

        // «графа15»
        def tmpValue15 = income102Dao.getIncome102(formData.reportPeriodId, getCell('R' + it, 'consumptionBuhSumAccountNumber').toString().substring(8), formData.departmentId)
        if (tmpValue15 == null || tmpValue15.totalSum == null) {
            logger.info("Нет информации в отчётах о прибылях и убытках")
            getCell('R' + it, 'opuSumByOpu').setValue(0)
        } else {
            getCell('R' + it, 'opuSumByOpu').setValue(tmpValue15.totalSum)
        }

        // «графа 16» = «графа14» - «графа15»
        getCell('R' + it, 'difference').setValue(
                getCellValue('R' + it, 'opuSumTotal') + getCellValue('R' + it, 'opuSumByOpu')
        )
    }

    (75..77).each {
        // «графа15»
        def tmpValue15 = income102Dao.getIncome102(formData.reportPeriodId, getCell('R' + it, 'consumptionBuhSumAccountNumber').toString().substring(8), formData.departmentId)
        if (tmpValue15 == null || tmpValue15.totalSum == null) {
            logger.info("Нет информации в отчётах о прибылях и убытках")
            getCell('R' + it, 'opuSumByOpu').setValue(0)
        } else {
            getCell('R' + it, 'opuSumByOpu').setValue(tmpValue15.totalSum)
        }
    }
}

/**
 * Скрипт для проверки создания.
 *
 * @author rtimerbaev
 * @since 22.02.2013 12:30
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
 * Проверка при осуществлении перевода формы в статус "Принята".
 *
 * @since 21.03.2013 17:00
 */
void checkOnAcceptance() {
    if (isBank()) {
        return
    }
    departmentFormTypeService.getDestinations(formData.getDepartmentId(), formData.getFormType().getId(), FormDataKind.SUMMARY).each { department ->
        if (department.formTypeId == formData.getFormType().getId()) {
            def form = FormDataService.find(department.formTypeId, department.kind, department.departmentId, formData.reportPeriodId)
            // если форма существует и статус "принята"
            if (form != null && form.getState() == WorkflowState.ACCEPTED) {
                logger.error('Принятие сводной налоговой формы невозможно, т.к. уже подготовлена сводная налоговая форма Банка.')
            }
        }
    }
}

/**
 * Проверка, наличия и статуса сводной формы уровня Банка  при осуществлении перевода формы в статус "Утверждена".
 *
 * @author auldanov
 * @since 21.03.2013 17:00
 */
void checkOnApproval() {
    if (isBank()) {
        return
    }
    departmentFormTypeService.getDestinations(formData.getDepartmentId(), formData.getFormType().getId(), FormDataKind.SUMMARY).each { department ->
        if (department.formTypeId == formData.getFormType().getId()) {
            def form = FormDataService.find(department.formTypeId, department.kind, department.departmentId, formData.reportPeriodId)
            // если форма существует и статус "принята"
            if (form != null && form.getState() == WorkflowState.ACCEPTED) {
                logger.error('Утверждение сводной налоговой формы невозможно, т.к. уже подготовлена сводная налоговая форма Банка.')
            }
        }
    }
}

/**
 * Проверки при переходе "Отменить принятие".
 */
void checkOnCancelAcceptance() {
    if (isBank()) {
        return
    }
    List<DepartmentFormType> departments = departmentFormTypeService.getFormDestinations(formData.getDepartmentId(), formData.getFormType().getId(), FormDataKind.SUMMARY);
    DepartmentFormType department = departments.getAt(0);
    if (department != null) {
        FormData bank = FormDataService.find(department.formTypeId, department.kind, department.departmentId, formData.reportPeriodId)

        if (bank != null && (bank.getState() == WorkflowState.APPROVED || bank.getState() == WorkflowState.ACCEPTED)) {
            logger.error("Нельзя отменить принятие налоговой формы, так как уже принята вышестоящая налоговая форма")
        }
    }
}

/**
 * Проверка обязательных полей.
 *
 * @author rtimerbaev
 * @since 20.03.2013 18:40
 */
void checkRequiredFields() {
    if (formDataEvent == FormDataEvent.COMPOSE) {
        return
    }

    // 6, 7, 9 графы
    [
            'consumptionBuhSumAccepted' : ((3..25) + (34..59) + (65..70) + (80..84) + (86..88) + (114..117) + [122, 123] + (131..139)),
            'consumptionBuhSumPrevTaxPeriod' : ((3..25) + (34..52) + (55..59) + (65..70) + (80..84) + (86..88) + [122, 123] + (131..139)),
            'consumptionTaxSumS' : ([3, 5] + (9..13) + [16] + (19..24) + (26..38) + [40, 41, 42, 44] + (46..56) + [58] + (60..68) + (70..81) + [83, 85, 86, 87] + (89..92) + (97..109) + [114, 116] + (118..134) + [139] + (144..147))
    ].each() { colAlias, items ->
        def errorMsg = ''
        def colName = formData.getDataRow('R1').getCell(colAlias).getColumn().getName()
        // разделы
        def sectionA1 = '', sectionB1 = '', sectionA2 = '', sectionD = ''
        items.each { item->
            def row = formData.getDataRow('R' + item)
            if (row.getCell(colAlias) != null && (row.getCell(colAlias).getValue() == null || ''.equals(row.getCell(colAlias).getValue()))) {
                switch (item) {
                    case (3..94) :
                        sectionA1 += getEmptyCellIncomeType(row)
                        break
                    case (97..110) :
                        sectionB1 += getEmptyCellIncomeType(row)
                        break
                    case (114..141) :
                        sectionA2 += getEmptyCellIncomeType(row)
                        break
                    case (144..147) :
                        sectionD += getEmptyCellIncomeType(row)
                        break
                }
            }
        }

        errorMsg += addSector(errorMsg, sectionA1, '"А1"')
        errorMsg += addSector(errorMsg, sectionB1, '"Б1"')
        errorMsg += addSector(errorMsg, sectionA2, '"А2"')
        errorMsg += addSector(errorMsg, sectionD, '"Д"')

        if (!''.equals(errorMsg)) {
            logger.error("Не заполнены ячейки колонки \"$colName\" в разделе: $errorMsg.")
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
    formData.getDataRows().each{ row ->
        ['consumptionBuhSumAccepted', 'consumptionBuhSumPrevTaxPeriod', 'consumptionTaxSumS'].each { it ->
            row.getCell(it).setValue(null)
        }
    }
    // получить консолидированные формы из источников
    departmentFormTypeService.getSources(formDataDepartment.id, formData.getFormType().getId(), FormDataKind.SUMMARY).each {
        def child = FormDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (child != null && child.state == WorkflowState.ACCEPTED
                && child.formType.id == 303) {
            child.getDataRows().eachWithIndex() { row, i ->
                def rowResult = formData.getDataRows().get(i)
                ['consumptionBuhSumAccepted', 'consumptionBuhSumPrevTaxPeriod', 'consumptionTaxSumS'].each {
                    if (row.getCell(it).getValue() != null && !row.getCell(it).hasValueOwner()) {
                        rowResult.getCell(it).setValue(summ(rowResult.getCell(it), row.getCell(it)))
                    }
                }
            }
        }
    }
    logger.info('Формирование сводной формы уровня Банка прошло успешно.')
}

/**
 * Получения данных из простых расходов (консолидация).
 * 6.1.3.8.4	Алгоритмы консолидации данных.
 *
 * @author auldanov
 * @author rtimerbaev
 * @since 22.02.2013 12:40
 */
void consolidationCalc() {
    if (isBank()) {
        return
    }
    // очищаем данные
    [93, 110, 140].each {
        formData.getDataRow('R' + it).consumptionBuhSumPrevTaxPeriod = null
        formData.getDataRow('R' + it).consumptionTaxSumS = null
    }
    [94, 141].each {
        formData.getDataRow('R' + it).consumptionBuhSumAccepted = null
        formData.getDataRow('R' + it).consumptionTaxSumS = null
    }

    // получение нф расходов простых
    departmentFormTypeService.getSources(formDataDepartment.id, formData.getFormType().getId(), FormDataKind.SUMMARY).each {
        def child = FormDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (child != null && child.state != WorkflowState.CREATED && child.formType.id == 304) {
            // 50001 - 93 строка
            copyFor500x('R85', 'R93', child)
            // 50002 - 110 строка
            copyFor500x('R89', 'R110', child)
            // 50011 - 140 строка
            copyFor500x('R194', 'R140', child)

            // 70001 - 94 строка
            copyFor700x('R86', 'R94', child)
            // 70011 - 141 строка
            copyFor700x('R195', 'R141', child)
        }
    }
}

/**
 * Получения данных из простых расходов.
 * 6.1.3.8.3.1	Логические проверки.
 *
 * @author auldanov
 * @author rtimerbaev
 * @since 21.03.2013 13:30
 */
void getDataFromSimpleOutcome() {
    // получение нф расходов простых
    def fromForm = FormDataService.find(304, formData.kind, formData.departmentId, formData.reportPeriodId)

    // 50001 - 93 строка
    copyFor500x('R85', 'R93', 'R3', 'R84', fromForm)
    // 50002 - 110 строка
    copyFor500x('R89', 'R110', 'R88', 'R88', fromForm)
    // 50011 - 140 строка
    copyFor500x('R194', 'R140', 'R92', 'R193', fromForm)


    // 70001 - 94 строка
    copyFor700x('R86', 'R94', 'R3', 'R84', fromForm)
    // 70011 - 141 строка
    copyFor700x('R195', 'R141', 'R92', 'R193', fromForm)
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
 * Функция суммирует диапазон строк определенного столбца и вставляет указаную сумму в последнюю ячейку.
 */
def setSum(String rowFromAlias, String rowToAlias, String columnAlias) {
    int rowFrom = formData.getDataRowIndex(rowFromAlias)
    int rowTo = formData.getDataRowIndex(rowToAlias)
    def sumRow = formData.getDataRow(rowToAlias)

    sumRow[columnAlias] = ((BigDecimal) summ(formData, new ColumnRange(columnAlias, rowFrom + 1, rowTo - 1))).setScale(2, BigDecimal.ROUND_HALF_UP)
}


/**
 * Суммирует ячейки второго диапазона только для тех строк, для которых выполняется условие фильтрации. В данном
 * случае под условием фильтрации подразумевается равенство значений строк первого диапазона заранее заданному
 * значению. Является аналогом Excel функции 'СУММЕСЛИ' в нотации 'СУММЕСЛИ(диапазон, критерий, диапазон_суммирования)'
 * @see <a href='http://office.microsoft.com/ru-ru/excel-help/HP010342932.aspx?CTT=1'>СУММЕСЛИ(диапазон, критерий, [диапазон_суммирования])</a>
 *
 * @param formData таблица данных
 * @param conditionRange диапазон по которому осуществляется отбор строк (фильтрация)
 * @param filterValue значение фильтра
 * @param summRange диапазон суммирования
 * @return сумма ячеек
 */
double summ(FormData formData, Range summRange, filter) {
    Rect summRect = summRange.getRangeRect(formData)
    Rect condRange = summRange.getRangeRect(formData)
    //noinspection GroovyAssignabilityCheck
    if (!summRect.isSameSize(condRange))
        throw new IllegalArgumentException(NOT_SAME_RANGES)

    double sum = 0
    List<DataRow> summRows = formData.getDataRows()
    List<Column> summCols = formData.getFormColumns()
    List<DataRow> condRows = formData.getDataRows()
    List<Column> condCols = formData.getFormColumns()
    for (int i = 0; i < condRange.getHeight(); i++) {
        for (int j = 0; j < condRange.getWidth(); j++) {
            Object condValue = condRows.get(condRange.y1 + i).get(condCols.get(condRange.x1 + j).getAlias())
            if (condValue != null && condValue != 'Требуется объяснение' && condValue != '' && filter(condRows.get(condRange.y1 + i))) {
                BigDecimal summValue = (BigDecimal) summRows.get(summRect.y1 + i).get(summCols.get(summRect.x1 + j).getAlias())
                if (summValue != null) {
                    sum += summValue.doubleValue()
                }
            }
        }
    }
    return sum;
}

/**
 * Обертка предназначенная для прямых вызовов функции без formData.
 */
DataRow getDataRow(String rowAlias) {
    return formData.getDataRow(rowAlias)
}
/**
 * Прямое получения значения ячейки по столбцу и колонке, значение Null воспринимается как 0.
 */
BigDecimal getCellValue(String row, String column) {
    if (getDataRow(row).getCell(column) == null) {
        throw new Exception('Не найдена ячейка')
    }
    return getDataRow(row).getCell(column).getValue() ?: 0;
}

/**
 * Прямое получения ячейки по столбцу и колонке.
 */
Cell getCell(String row, String column) {
    return getDataRow(row).getCell(column)
}

double summ(String columnName, String fromRowA, String toRowA) {
    def result = summ(formData, new ColumnRange(columnName,
            formData.getDataRowIndex(fromRowA), formData.getDataRowIndex(toRowA)))
    return result != null ? result : 0;
}

/**
 * Получить число из строки.
 */
def toBigDecimal(String value) {
    if (value == null) {
        return new BigDecimal(0)
    }
    def result
    try {
        result = new BigDecimal(Double.parseDouble(value))
    } catch (NumberFormatException e){
        result = new BigDecimal(0)
    }
    return result
}

/**
 * Получить разделить между названиями разделов.
 */
def getSectionSeparator(def value1, def value2) {
    return ((!''.equals(value1)) && !''.equals(value2) ? ', ' : '')
}

/**
 * Получить код строки в которой есть незаполненная ячейка.
 */
def getEmptyCellIncomeType(def row) {
    return (row.consumptionTypeId != null ? row.consumptionTypeId : 'пусто') + ', '
}

/**
 * Удалить последнюю запятую.
 */
def deleteLastSeparator(String values) {
    return values.substring(0, values.length() - 2)
}

/**
 * Добавить в сообщение коды незаполненных ячеек.
 *
 * @param errorMsg сообщение
 * @param values список незаполненных полей в виде строки (перечислены через запятую)
 * @param sectorName название раздела
 */
def addSector(def errorMsg, def values, def sectorName) {
    if (values != null && !''.equals(values)) {
        return getSectionSeparator(errorMsg, values) + sectorName + ' (' + deleteLastSeparator(values) + ')'
    } else {
        return ''
    }
}

/**
 * Копирует данные для строк 500x.
 *
 * @param fromRowA псевдоним строки нф откуда копируем данные
 * @param toRowA псевдоним текущей строки куда копируем данные
 */
void copyFor500x(String fromRowA, String toRowA, def fromForm) {
    // строка для заполнения из текущей нф
    toRow = formData.getDataRow(toRowA)
    // строка для копирования из другой нф
    fromRow = fromForm.getDataRow(fromRowA)

    // 7 графа
    toRow.consumptionBuhSumPrevTaxPeriod = summ(toRow.getCell('consumptionBuhSumPrevTaxPeriod'), fromRow.getCell('rnu5Field5PrevTaxPeriod'))
    // 9 графа
    toRow.consumptionTaxSumS = summ(toRow.getCell('consumptionTaxSumS'), fromRow.getCell('rnu5Field5Accepted'))
}

/**
 * Копирует данные для строк 700x.
 *
 * @param fromRowA псевдоним строки нф откуда копируем данные
 * @param toRowA псевдоним текущей строки куда копируем данные
 */
void copyFor700x(String fromRowA, String toRowA, def fromForm) {
    // строка для заполнения из текущей нф
    toRow = formData.getDataRow(toRowA)
    // строка для копирования из другой нф
    fromRow = fromForm.getDataRow(fromRowA)

    // 6 графа
    toRow.consumptionBuhSumAccepted = summ(toRow.getCell('consumptionBuhSumAccepted'), fromRow.getCell('rnu7Field12Accepted'))
    // 9 графа
    toRow.consumptionTaxSumS = summ(toRow.getCell('consumptionTaxSumS'), fromRow.getCell('rnu7Field10Sum'))
}

/**
 * Копирует данные для строк 500x.
 *
 * @param fromRowA псевдоним строки нф откуда копируем данные
 * @param toRowA псевдоним текущей строки куда копируем данные
 * @param fromRowSum псевдоним строки от которой считать сумму
 * @param toRowSum псевдоним строки до которой считать сумму
 */
void copyFor500x(String fromRowA, String toRowA, String fromRowSum, String toRowSum, def fromForm) {
    // строка для заполнения из текущей нф
    toRow = formData.getDataRow(toRowA)

    if (fromForm != null) {
        // строка для копирования из другой нф
        fromRow = fromForm.getDataRow(fromRowA)

        // 12 графа
        toRow.opuSumByEnclosure3 = summ(fromForm, new ColumnRange('rnu5Field5Accepted',
                fromForm.getDataRowIndex(fromRowSum), fromForm.getDataRowIndex(toRowSum)))
        // 13 графа
        toRow.opuSumByTableP = summ(fromForm, new ColumnRange('rnu5Field5PrevTaxPeriod',
                fromForm.getDataRowIndex(fromRowSum), fromForm.getDataRowIndex(toRowSum)))
    } else {
        // 12 графа
        toRow.opuSumByEnclosure3 = 0
        // 13 графа
        toRow.opuSumByTableP = 0
    }

    // 14 графа = графа 13 - графа 7
    if (toRow.opuSumByTableP != null && toRow.consumptionBuhSumPrevTaxPeriod != null) {
        toRow.opuSumTotal = toRow.opuSumByTableP - toRow.consumptionBuhSumPrevTaxPeriod
    } else {
        toRow.opuSumTotal = null
    }
    // 16 графа = графа 12 - графа 9
    if (toRow.opuSumByEnclosure3 != null && toRow.consumptionTaxSumS != null) {
        toRow.difference = toRow.opuSumByEnclosure3 - toRow.consumptionTaxSumS
    } else {
        toRow.difference = null
    }
}

/**
 * Копирует данные для строк 700x.
 *
 * @param fromRowA псевдоним строки нф откуда копируем данные
 * @param toRowA псевдоним текущей строки куда копируем данные
 * @param fromRowSum псевдоним строки от которой считать сумму
 * @param toRowSum псевдоним строки до которой считать сумму
 */
void copyFor700x(String fromRowA, String toRowA, String fromRowSum, String toRowSum, def fromForm) {
    // строка для заполнения из текущей нф
    toRow = formData.getDataRow(toRowA)

    if (fromForm != null) {
        // строка для копирования из другой нф
        fromRow = fromForm.getDataRow(fromRowA)

        // 11 графа
        def tmp = (BigDecimal) summ(fromForm, new ColumnRange('rnu7Field12Accepted', fromForm.getDataRowIndex(fromRowSum), fromForm.getDataRowIndex(toRowSum)))
        toRow.logicalCheck = new DecimalFormat("#0.##").format(tmp).replace(',', '.')
        // 12 графа
        toRow.opuSumByEnclosure3 = summ(fromForm, new ColumnRange('rnu7Field10Sum',
                fromForm.getDataRowIndex(fromRowSum), fromForm.getDataRowIndex(toRowSum)))
    } else {
        // 11 графа
        toRow.logicalCheck = '0'
        // 12 графа
        toRow.opuSumByEnclosure3 = 0
    }

    // 13 графа = графа 11 - графа 6
    if (toRow.logicalCheck != null && toRow.consumptionBuhSumAccepted != null) {
        toRow.opuSumByTableP = toBigDecimal(toRow.logicalCheck) - toRow.consumptionBuhSumAccepted
    } else {
        toRow.opuSumByTableP = null
    }
    // 16 графа = графа 12 - графа 9
    if (toRow.opuSumByEnclosure3 != null && toRow.consumptionTaxSumS != null) {
        toRow.difference = toRow.opuSumByEnclosure3 - toRow.consumptionTaxSumS
    } else {
        toRow.difference = null
    }
}