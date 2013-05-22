/**
 * Форма "Расшифровка видов расходов, учитываемых в простых РНУ (расходы простые)".
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
    // проверить при переводе в утверждена
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
}

// графа  1 - consumptionTypeId
// графа  2 - consumptionGroup
// графа  3 - consumptionTypeByOperation
// графа  4 - ptionAccountNumber
// графа  5 - rnu7Field10Sum
// графа  6 - rnu7Field12Accepted
// графа  7 - rnu7Field12PrevTaxPeriod
// графа  8 - rnu5Field5Accepted
// графа  9 - rnu5Field5PrevTaxPeriod
// графа 10 - logicalCheck
// графа 11 - opuSumByEnclosure2
// графа 12 - opuSumByTableP
// графа 13 - opuSumTotal
// графа 14 - opuSumByOpu
// графа 15 - difference

/**
 * Проверить и расчитать.
 */
void checkAndCalc() {
    checkRequiredFields()
    calculationBasicSum()
    calculationControlGraphs()
}

/**
 * Скрипт для перевода сводной налогой формы в статус "принят".
 *
 * @author rtimerbaev
 * @since 13.02.2013 12:00
 */
void acceptance() {
    departmentFormTypeService.getDestinations(formDataDepartment.id, formData.getFormType().getId(), FormDataKind.SUMMARY).each() {
        formDataCompositionService.compose(formData, it.departmentId, it.formTypeId, it.kind, logger)
    }
}

/**
 * Вычисление сумм.
 *
 * @since 15.03.2013 15:00
 */
void calculationBasicSum() {
    ['rnu5Field5Accepted', 'rnu5Field5PrevTaxPeriod'].each {
        setSum('R2', 'R85', it)
    }

    ['rnu7Field10Sum', 'rnu7Field12Accepted'].each {
        setSum('R2', 'R86', it)
    }

    ['rnu5Field5Accepted', 'rnu5Field5PrevTaxPeriod'].each {
        setSum('R87', 'R89', it)
    }

    ['rnu5Field5Accepted', 'rnu5Field5PrevTaxPeriod'].each {
        setSum('R91', 'R194', it)
    }

    ['rnu7Field10Sum', 'rnu7Field12Accepted'].each {
        setSum('R92', 'R195', it)
    }
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
    // графа 10
    ([10] + (13..15) + (23..25) + (30..33) + [35, 36, 39, 52, 55, 56, 63] +
            (70..73) + [76, 77, 81, 82, 88, 155] + (166..176) + [178] + (180..183)).each {
        setColumn9Equals0(formData.getDataRow('R' + it))
    }
    [37, 38].each {
        setColumn9Less0(formData.getDataRow('R' + it))
    }

    // получение данных из расходов сложных (303) для вычисления 11 графы
    def formData303 = FormDataService.find(303, FormDataKind.SUMMARY, formData.departmentId, formData.reportPeriodId)
    if (formData303 != null) {
        ((3..84) + [88] + (92..192)).each() {
            def row = formData.getDataRow('R' + it)

            // графа 11
            if (row.consumptionAccountNumber != null) {
                def from = 0
                def to = formData303.getDataRows().size() - 1
                ColumnRange columnRange4 = new ColumnRange('consumptionBuhSumAccountNumber', from, to)
                ColumnRange columnRange6 = new ColumnRange('consumptionBuhSumAccepted', from, to)
                row.opuSumByEnclosure2 = summIfEquals(formData303, columnRange4, row.consumptionAccountNumber, columnRange6)
            }
        }
    } else {
        // если источников нет то зануляем поля в которые должны были перетянуться данные
        ((3..84) + [88] + (92..192)).each() {
            def row = formData.getDataRow('R' + it)

            // графа 11
            row.opuSumByEnclosure2 = 0
        }
    }

    // строка 193 графа 15
    def row193 = formData.getDataRow('R193')
    def bVal15 = new StringBuffer(row193.consumptionAccountNumber)
    bVal15.delete(4,5)
    def data193 = income101Dao.getIncome101(formData.reportPeriodId, bVal15.toString(), formData.departmentId)
    if (data193 == null)
        logger.warn("Не найдены соответствующие данные в оборотной ведомости")
    row193.opuSumByOpu = data193 ? data193.creditRate: 0
    // строка 193 графа 16 («графа 16»= «графа 15»-«графа 6»)
    row193.difference = (row193.opuSumByOpu ?:0) - (row193.rnu7Field12Accepted ?:0)

    // вычислять только для заданных строк
    ((3..84) + [88] + (92..192)).each() {
        def row = formData.getDataRow('R' + it)

        // графа 12
        if (row.consumptionAccountNumber != null) {
            def from = 0
            def to = formData.getDataRows().size() - 1
            ColumnRange columnRange4 = new ColumnRange('consumptionAccountNumber', from, to)
            ColumnRange columnRange8 = new ColumnRange('rnu5Field5Accepted', from, to)
            row.opuSumByTableP = summIfEquals(formData, columnRange4, row.consumptionAccountNumber, columnRange8)
        }

        // графа 13
        if (row.opuSumByEnclosure2 != null && row.opuSumByTableP != null) {
            row.opuSumTotal = row.opuSumByEnclosure2 + row.opuSumByTableP
        } else {
            row.opuSumTotal = null
        }

        // графа 14
        def bVal = new StringBuffer(row.consumptionAccountNumber)
        bVal.delete(1,8)
        def dataThisRow = income102Dao.getIncome102(formData.reportPeriodId, bVal.toString(), formData.departmentId)
        if (dataThisRow == null)
            logger.warn("Не найдены соответствующие данные в отчете о прибылях и убытках")
        row.opuSumByOpu = dataThisRow ? dataThisRow.creditRate : 0

        // графа 15
        if (row.opuSumTotal != null && row.opuSumByOpu != null) {
            row.difference = row.opuSumTotal - row.opuSumByOpu
        } else {
            row.difference = null
        }
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
 * Проверки наличия декларации Банка при принятии нф (checkDeclarationBankOnAcceptance.groovy).
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
 * Проверка при осуществлении перевода формы в статус "Принята".
 *
 * @author auldanov
 * @since 21.03.2013 17:00
 */
void checkOnAcceptance() {
    departmentFormTypeService.getDestinations(formData.getDepartmentId(), formData.getFormType().getId(), FormDataKind.SUMMARY).each { department ->
        if (department.formTypeId == formData.getFormType().getId()) {
            def form = FormDataService.find(department.formTypeId, department.kind, department.departmentId, formData.reportPeriodId)
            // если форма существует и статус "принята"
            if (form != null && form.getState() == WorkflowState.ACCEPTED) {
                logger.error('Принятие сводной налоговой формы "Расшифровка видов расходов, учитываемых в простых РНУ (расходы простые)" невозможно, т.к. уже подготовлена сводная налоговая форма Банка.')
            }
        }
    }
}

/**
 * Проверка, наличия и статуса сводной формы уровня Банка при осуществлении перевода формы в статус "Утверждена".
 *
 * @author auldanov
 * @since 21.03.2013 17:00
 */
void checkOnApproval() {
    departmentFormTypeService.getDestinations(formData.getDepartmentId(), formData.getFormType().getId(), FormDataKind.SUMMARY).each { department ->
        if (department.formTypeId == formData.getFormType().getId()) {
            def form = FormDataService.find(department.formTypeId, department.kind, department.departmentId, formData.reportPeriodId)
            // если форма существует и статус "принята"
            if (form != null && form.getState() == WorkflowState.ACCEPTED) {
                logger.error('Утверждение сводной налоговой формы "Расшифровка видов расходов, учитываемых в простых РНУ (расходы простые)" невозможно, т.к. уже подготовлена сводная налоговая форма Банка.')
            }
        }
    }
}

/**
 * Проверки при переходе "Отменить принятие".
 */
void checkOnCancelAcceptance() {
    if (!isTerBank()) {
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
 * @since 20.03.2013 18:30
 */
void checkRequiredFields() {
    // 5-9 графы
    [
            'rnu7Field10Sum' : ([17, 18, 22, 28, 29, 34] + (48..51) + [53, 54] + (57..62) + [74, 75, 78, 79, 80, 184, 185, 186]),
            'rnu7Field12Accepted' : ([17, 18, 22, 28, 29, 34] + (48..51) + [53, 54] + (57..62) + [74, 75, 78, 79, 80, 184, 185, 186, 193]),
            'rnu7Field12PrevTaxPeriod' : ([17, 18, 22, 28, 29, 34] + (48..51) + [53, 54] + (57..62) + [74, 75, 78, 79, 80, 184, 185, 186]),
            'rnu5Field5Accepted' : ((3..84) + [88] + (92..191)),
            'rnu5Field5PrevTaxPeriod' : ([10, 13, 14, 15, 23, 24, 25] + (30..33) + (35..39) + [52, 55, 56, 63] + (70..73) + [76, 77, 81, 82, 88, 155] + (166..176) + [178] + (180..183))
    ].each() { colAlias, items ->
        def errorMsg = ''
        def colName = formData.getDataRow('R1').getCell(colAlias).getColumn().getName()
        // разделы
        def sectionA1 = '', sectionB1 = '', sectionA2 = ''
        items.each { item->
            def row = formData.getDataRow('R' + item)
            if (row.getCell(colAlias) != null && (row.getCell(colAlias).getValue() == null || ''.equals(row.getCell(colAlias).getValue()))) {
                switch (item) {
                    case (3..84) :
                        sectionA1 += getEmptyCellType(row)
                        break
                    case 88 :
                        sectionB1 += getEmptyCellType(row)
                        break
                    case (92..193) :
                        sectionA2 += getEmptyCellType(row)
                        break
                }
            }
        }
        errorMsg += addSector(errorMsg, sectionA1, '"А1"')
        errorMsg += addSector(errorMsg, sectionB1, '"Б1"')
        errorMsg += addSector(errorMsg, sectionA2, '"А2"')

        if (!''.equals(errorMsg)) {
            logger.error("Не заполнены ячейки колонки \"$colName\" в разделе: $errorMsg.")
        }
    }
}

/**
 * Скрипт для консолидации (consolidation.groovy).
 *
 * @author rtimerbaev
 * @since 21.02.2013 13:50
 */
void consolidation() {
    if (!isBank()) {
        return
    }
    // очистить форму
    formData.getDataRows().each{ row ->
        ['rnu7Field10Sum', 'rnu7Field12Accepted', 'rnu7Field12PrevTaxPeriod',
                'rnu5Field5Accepted', 'rnu5Field5PrevTaxPeriod', 'logicalCheck'].each{ alias->
            row.getCell(alias).setValue(null)
        }
    }

    // получить консолидированные формы в дочерних подразделениях в текущем налоговом периоде
    departmentFormTypeService.getSources(formDataDepartment.id, formData.getFormType().getId(), FormDataKind.SUMMARY).each {
        def child = FormDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (child != null && child.state == WorkflowState.ACCEPTED) {
            child.getDataRows().eachWithIndex() { row, i ->
                def rowResult = formData.getDataRows().get(i)
                ['rnu7Field10Sum', 'rnu7Field12Accepted', 'rnu7Field12PrevTaxPeriod',
                        'rnu5Field5Accepted', 'rnu5Field5PrevTaxPeriod'].each {
                    if (row.getCell(it).getValue() != null) {
                        rowResult.getCell(it).setValue(summ(rowResult.getCell(it), row.getCell(it)))
                    }
                }
            }
        }
    }
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

    sumRow[columnAlias] = summ(formData, new ColumnRange(columnAlias, rowFrom + 1, rowTo - 1))
}

/**
 * Пустое ли значение.
 */
boolean isEquals(Double value1, Double value2) {
    def eps = 1e-8
    return value1 != null && (Math.abs(value1 - value2) < eps)
}

/**
 * Установить значение для 10ого столбца (если 9ый столбец = 0).
 *
 * @param row строка
 */
void setColumn9Equals0(def row) {
    if (row != null && row.rnu5Field5PrevTaxPeriod != null) {
        def result = (Double) round(row.rnu5Field5PrevTaxPeriod, 2)
        row.logicalCheck = isEquals(result, 0) ? result.toString() : 'Требуется объяснение'
    } else {
        row.logicalCheck = null
    }
}

/**
 * Установить значение для 10ого столбца (если 9ый столбец < 0).
 *
 * @param row строка
 */
void setColumn9Less0(def row) {
    if (row != null) {
        def result = round((row.rnu5Field5Accepted?:0) - (row.rnu7Field12Accepted?:0), 2)
        row.logicalCheck = result < 0 ? 'Требуется объяснение' : (new DecimalFormat("#0.##").format(result)).toString().replace(",", ".")
    }
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
def getEmptyCellType(def row) {
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