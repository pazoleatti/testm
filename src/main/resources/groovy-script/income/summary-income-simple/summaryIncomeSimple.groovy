/**
 * Форма "Расшифровка видов доходов, учитываемых в простых РНУ (доходы простые)".
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

// графа  1 - incomeTypeId
// графа  2 - incomeGroup
// графа  3 - incomeTypeByOperation
// графа  4 - accountNo
// графа  5 - rnu6Field10Sum
// графа  6 - rnu6Field10Field2
// графа  7 - rnu6Field12Accepted
// графа  8 - rnu6Field12PrevTaxPeriod
// графа  9 - rnu4Field5Accepted
// графа 10 - rnu4Field5PrevTaxPeriod
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
    calculationBasicSum()
    calculationControlGraphs()
}

/**
 * Для перевода сводной налогой формы в статус "принят".
 *
 * @author rtimerbaev
 * @since 07.02.2013 13:10
 */
void acceptance() {
    departmentFormTypeService.getDestinations(formDataDepartment.id, formData.getFormType().getId(), FormDataKind.SUMMARY).each{
        formDataCompositionService.compose(formData, it.departmentId, it.formTypeId, it.kind, logger)
    }
}

/**
 * Расчет основных граф НФ.
 *
 * @author auldanov
 */
void calculationBasicSum() {
    // Расчет сумм
    ["rnu4Field5Accepted", "rnu4Field5PrevTaxPeriod"].each {
        setSum('R2', 'R87', it)
    }

    ["rnu6Field10Sum", "rnu6Field10Field2", "rnu6Field12Accepted"].each {
        setSum('R2', 'R88', it)
    }

    ["rnu4Field5Accepted", "rnu4Field5PrevTaxPeriod"].each {
        setSum('R89', 'R94',  it)
    }

    ["rnu6Field10Sum", "rnu6Field12Accepted"].each {
        setSum('R89', 'R95', it)
    }

    ["rnu4Field5Accepted", "rnu4Field5PrevTaxPeriod"].each {
        setSum('R97', 'R210', it)
    }

    ["rnu6Field10Sum", "rnu6Field10Field2", "rnu6Field12Accepted"].each {
        setSum('R97', 'R211', it)
    }

    ["rnu4Field5Accepted", "rnu4Field5PrevTaxPeriod"].each {
        setSum('R212', 'R215', it)
    }

    ["rnu4Field5Accepted", "rnu4Field5PrevTaxPeriod"].each {
        setSum('R216', 'R218', it)
    }

    setSum('R219', 'R222', "rnu4Field5Accepted")
}

/**
 * Расчет контрольных граф НФ (calculationControlGraphs.groovy).
 *
 * @author auldanov
 * @author rtimerbaev
 * @since 20.03.2013 19:10
 * @version 14 (05.03.2013)
 */
void calculationControlGraphs() {
    /**
     * Выполнена в виде хелпера, учитывая принцип DRY.
     *
     * v0.1
     */

    def specialNotation = 'Требуется объяснение'

    // графа 11 [1]
    ([3, 4, 7, 8, 9, 10, 12] + (13..30) + (33..36) + [38, 40, 44, 47, 62, 63, 85, 92, 93, 104, 114, 115, 197, 200, 201, 202, 204, 205]).each {
        def column5 = getCellValue('R' + it, 'rnu6Field10Sum')
        def column7 = getCellValue('R' + it, 'rnu6Field12Accepted')
        def column8 = getCellValue('R' + it, 'rnu6Field12PrevTaxPeriod')
        summ = ((BigDecimal)(column5 - (column7 - column8))).setScale(2, BigDecimal.ROUND_HALF_UP)
        getCell('R' + it, 'logicalCheck').setValue(summ >= 0 ? summ.toString() : specialNotation)
    }

    // графа 11 [2]
    ([31,32, 37, 56, 61] + (64..70) + (78..84) + [90, 91] + (98..103) + (105..109) + [111, 112, 113] + (116..164) + (170..196) + [198, 199] + [203, 213, 214, 217]).each {
        summ = ((BigDecimal)(getCellValue('R' + it, 'rnu4Field5PrevTaxPeriod'))).setScale(2, BigDecimal.ROUND_HALF_UP)
        getCell('R' + it, 'logicalCheck').setValue(summ != 0 ? specialNotation : summ.toString())
    }

    // графа 11 [3]
    ([5,6, 11, 39, 41, 42, 43, 45, 46] + (48..55) + (57..60) + (71..77) + [86, 110, 206, 207, 208]).each {
        summ = ((BigDecimal)(getCellValue('R' + it, 'rnu6Field10Sum') - getCellValue('R' + it, 'rnu4Field5PrevTaxPeriod'))).setScale(2, BigDecimal.ROUND_HALF_UP)
        getCell('R' + it, 'logicalCheck').setValue(summ >= 0 ? summ.toString() : specialNotation)
    }

    // получение данных из доходов сложных (302) для вычисления 12 графы
    def formData302 = FormDataService.find(302, FormDataKind.SUMMARY, formData.departmentId, formData.reportPeriodId)
    if (formData302 != null) {
        ((3..86) + (90..93) + (98..164) + (170..208) + [213, 214, 217]).each {
            // графа 12
            ColumnRange columnRange6 = new ColumnRange('incomeBuhSumAccepted', 0, formData302.getDataRows().size() - 1)
            Double sum6column = summ(formData302, columnRange6, columnRange6, { condRange ->
                return getCell('R' + it, 'accountNo').getValue() == condRange.getCell('incomeBuhSumAccountNumber').getValue()
            })
            getCell('R' + it, 'opuSumByEnclosure2').setValue(sum6column)


        }
    } else {
        // если источников нет то зануляем поля в которые должны были перетянуться данные
        ((3..86) + (90..93) + (98..164) + (170..208) + [213, 214, 217]).each {
            // графа 12
            getCell('R' + it, 'opuSumByEnclosure2').setValue(0)
        }
    }


    // графы 13, 14, 15, 16
    ((3..86) + (90..93) + (98..164) + (170..208) + [213, 214, 217]).each {
        // графа 13
        columnRange9 = new ColumnRange('rnu4Field5Accepted', 0, formData.getDataRows().size() - 1)
        Double sum9column = summ(formData, columnRange9, columnRange9, { condRange ->
            return getCell('R' + it, 'accountNo').getValue() == condRange.getCell('accountNo').getValue()
        })
        getCell('R' + it, 'opuSumByTableD').setValue(sum9column)

        // графа 14
        getCell('R' + it, 'opuSumTotal').setValue(
                getCellValue('R' + it, 'opuSumByEnclosure2') + getCellValue('R' + it, 'opuSumByTableD')
        )

        // графа 15
        def bVal = new StringBuffer(getCell('R'+it, 'accountNo').value)
        bVal.delete(1, 8)
        def data = income102Dao.getIncome102(formData.reportPeriodId, bVal.toString(), formData.departmentId)
        if (data == null)
            logger.warn("Не найдены соответствующие данные в отчете о прибылях и убытках")
        getCell('R'+it, 'opuSumByOpu').setValue(data ? data.creditRate : 0)


        // графа 16
        getCell('R' + it, 'difference').setValue(
                getCellValue('R' + it, 'opuSumTotal') - getCellValue('R' + it, 'opuSumByOpu')
        )
    }

    // Графа 15 и 16
    (165..169).each {
        // графа 15
        def bVal = new StringBuffer(getCell('R'+it, 'accountNo').value)
        bVal.delete(4,5)
        def data = income101Dao.getIncome101(formData.reportPeriodId, bVal.toString(), formData.departmentId)
        if (data == null)
            logger.warn("Не найдены соответствующие данные в оборотной ведомости")
        getCell('R'+it, 'opuSumByOpu').setValue(data ? data.debetRate : 0)
        // графа 16
        getCell('R' + it, 'difference').setValue(
                getCellValue('R' + it, 'opuSumByOpu') - getCellValue('R' + it, 'rnu4Field5Accepted')
        )
    }


    // Графа 15 Строка 209
    bVal = new StringBuffer(getCell('R209', 'accountNo').value)
    bVal.delete(4,5)
    def data209x15 = income101Dao.getIncome101(formData.reportPeriodId, bVal.toString(), formData.departmentId)
    if (data209x15 == null)
        logger.warn("Не найдены соответствующие данные в оборотной ведомости")
    getCell('R209', 'opuSumByOpu').setValue(data209x15 ? data209x15.debetRate : 0)
    // Графа 16  Строка 209 («графа 16» = «графа 15» - «графа 7»)
    getCell('R209', 'difference').setValue(
            getCellValue('R209', 'opuSumByOpu') - getCellValue('R209', 'rnu6Field12Accepted')
    )

    // Графа 15 (Строка 220)
    bVal = new StringBuffer(getCell('R220', 'accountNo').value)
    bVal.delete(4, 5)
    def data220x15 = income101Dao.getIncome101(formData.reportPeriodId, bVal.toString(), formData.departmentId)
    if (data220x15 == null)
        logger.warn("Не найдены соответствующие данные в оборотной ведомости")
    getCell('R220', 'opuSumByOpu').setValue(data220x15 ? data220x15.creditRate : 0)
    /*
     * Графа 16 строка 220
     * «графа 16» = «графа 15» - (А + Б)
     * А – значение «графы 9» для строки 220
     * Б – значение «графы 9» для строки 221
     */
    def column15 = getCellValue('R220', 'opuSumByOpu')
    def a = getCellValue('R220', 'rnu4Field5Accepted')
    def b = getCellValue('R221', 'rnu4Field5Accepted')
    getCell('R220', 'difference').setValue(column15 - (a + b))
}

/**
 * Скрипт для проверки создания.
 *
 * @author rtimerbaev
 * @since 21.02.2013 12:30
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
 * Проверка при осуществлении перевода формы в статус "Принята".
 *
 * @since 21.03.2013 17:00
 */
void checkOnAcceptance() {
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
                logger.error('Утверждение сводной налоговой формы невозможно, т.к. уже подготовлена сводная налоговая форма Банка.')
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
 * @since 20.03.2013 18:10
 */
void checkRequiredFields() {
    // 5-10 графы
    [
            'rnu6Field10Sum' : ((3..30) + (33..36) + (38..55) + (57..60) + [62, 63] + (71..77) + [85, 86, 92, 93, 104, 110, 114, 115, 197] + (200..202) + (204..208)),
            'rnu6Field10Field2' : ((3..16) + [19] + (22..30) + (33..36) + (38..55) + (57..60) + [62, 63] + (71..77) + [85, 86, 110, 197] + (200..202) + (206..208)),
            'rnu6Field12Accepted' : ([3, 4] + (7..10) + (12..30) + (33..36) + [38, 40, 44, 47, 62, 63, 87, 92, 93, 104, 114, 115, 197] + (200..202) + [204, 205, 209]),
            'rnu6Field12PrevTaxPeriod' : ([3, 4] + (7..10) + (12..30) + (33..36) + [38, 40, 44, 47, 62, 63, 87, 92, 93, 104, 114, 115, 197] + (200..202) + [204, 205]),
            'rnu4Field5Accepted' : ((3..88) + (90..93) + (98..208) + [213, 214, 217, 220, 221]),
            'rnu4Field5PrevTaxPeriod' : ([5, 6, 11, 31, 32, 37, 39, 41, 42, 43, 45, 46] + (48..61) + (64..84) + [86, 90, 91] + (98..103) + (105..113) + (116..164) + (170..196) + [198, 199, 203, 206, 207, 208, 213, 214, 217])
    ].each { colAlias, items ->
        def errorMsg = ''
        def colName = formData.getDataRow('R1').getCell(colAlias).getColumn().getName()
        // разделы
        def sectionA1 = '', sectionB1 = '', sectionA2 = '', sectionB2 = '', sectionD = '', sectionE = ''
        items.each { item->
            def row = formData.getDataRow('R' + item)
            if (row.getCell(colAlias) != null && (row.getCell(colAlias).getValue() == null || ''.equals(row.getCell(colAlias).getValue()))) {
                switch (item) {
                    case (3..86) :
                        sectionA1 += getEmptyCellType(row)
                        break
                    case (90..93) :
                        sectionB1 += getEmptyCellType(row)
                        break
                    case (98..209) :
                        sectionA2 += getEmptyCellType(row)
                        break
                    case (213..214) :
                        sectionB2 += getEmptyCellType(row)
                        break
                    case 217 :
                        sectionD += getEmptyCellType(row)
                        break
                    case (220..221) :
                        sectionE += getEmptyCellType(row)
                        break
                }
            }
        }

        errorMsg += addSector(errorMsg, sectionA1, '"А1"')
        errorMsg += addSector(errorMsg, sectionB1, '"Б1"')
        errorMsg += addSector(errorMsg, sectionA2, '"А2"')
        errorMsg += addSector(errorMsg, sectionB2, '"Б2"')
        errorMsg += addSector(errorMsg, sectionD, '"Д"')
        errorMsg += addSector(errorMsg, sectionE, '"Е"')

        if (!''.equals(errorMsg)) {
            logger.error("Не заполнены ячейки колонки \"$colName\" в разделе: $errorMsg.")
        }
    }
}

/**
 * Скрипт для консолидации.
 *
 * @author rtimerbaev
 * @since 08.02.2013 13:00
 */
void consolidation() {
    if (!isTerBank()) {
        return
    }
    // очистить форму
    formData.getDataRows().each{ row ->
        ['rnu6Field10Sum', 'rnu6Field10Field2', 'rnu6Field12Accepted', 'rnu6Field12PrevTaxPeriod', 'rnu4Field5Accepted', 'rnu4Field5PrevTaxPeriod'].each{ alias->
            row.getCell(alias).setValue(null)
        }
    }
    // получить данные из источников
    departmentFormTypeService.getSources(formDataDepartment.id, formData.getFormType().getId(), FormDataKind.SUMMARY).each {
        def child = FormDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (child != null && child.state == WorkflowState.ACCEPTED) {
            child.getDataRows().eachWithIndex() { row, i ->
                def rowResult = formData.getDataRows().get(i)
                ['rnu6Field10Sum', 'rnu6Field10Field2', 'rnu6Field12Accepted',
                        'rnu6Field12PrevTaxPeriod', 'rnu4Field5Accepted', 'rnu4Field5PrevTaxPeriod'].each {
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

// обертка предназначенная для прямых вызовов функции без formData
DataRow getDataRow(String rowAlias) {
    return formData.getDataRow(rowAlias)
}

// обертка предназначенная для прямых вызовов функции без formData
int getDataRowIndex(String rowAlias) {
    return formData.getDataRowIndex(rowAlias)
}

// прямое получения ячейки по столбцу и колонке
Cell getCell(String row, String column) {
    return getDataRow(row).getCell(column)
}

// обертка предназначенная для прямых вызовов функции без formData
BigDecimal summ(ColumnRange cr) {
    return summ(formData, cr, cr, {return true;})
}

// прямое получения значения ячейки по столбцу и колонке, значение Null воспринимается как 0
BigDecimal getCellValue(String row, String column) {
    if (getDataRow(row).getCell(column) == null) {
        throw new Exception('Не найдена ячейка')
    }
    return getDataRow(row).getCell(column).getValue() ?: 0;
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
double summ(FormData formData, Range conditionRange, Range summRange, filter) {

    Rect summRect = summRange.getRangeRect(formData)
    Rect condRange = conditionRange.getRangeRect(formData)
    if (!summRect.isSameSize(condRange))
        throw new IllegalArgumentException(NOT_SAME_RANGES)

    double sum = 0;
    List<DataRow> summRows = formData.getDataRows()
    List<Column> summCols = formData.getFormColumns()
    List<DataRow> condRows = formData.getDataRows()
    List<Column> condCols = formData.getFormColumns()
    for (int i = 0; i < condRange.getHeight(); i++) {
        for (int j = 0; j < condRange.getWidth(); j++) {
            Object condValue = condRows.get(condRange.y1 + i).get(condCols.get(condRange.x1 + j).getAlias());
            if (condValue != null && condValue != 'Требуется объяснение' && condValue != '' && filter(condRows.get(condRange.y1 + i))) {
                BigDecimal summValue = (BigDecimal) summRows.get(summRect.y1 + i).get(summCols.get(summRect.x1 + j).getAlias());
                if (summValue != null) {
                    sum += summValue.doubleValue()
                }
            }
        }
    }
    return sum;
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
    return (row.incomeTypeId != null ? row.incomeTypeId : 'пусто') + ', '
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