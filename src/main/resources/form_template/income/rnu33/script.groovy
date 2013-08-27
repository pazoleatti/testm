/**
 * Скрипт для РНУ-33 (rnu33.groovy).
 * Форма "(РНУ-33) Регистр налогового учёта процентного дохода и финансового результата от реализации (выбытия) ГКО".
 *
 * @version 68
 *
 * TODO:
 *      - нет условии в проверках соответствия НСИ (потому что нету справочников)
 *		- проверка 5 не сделана, потому что про предыдущие месяцы пока не прояснилось
 *		- неясность с алгоритмом заполнения строки «Итого за текущий месяц»
 *              (после каких строк считать или по каким значениям группировать строки).
 *              Временно сгруппировал по графе 4 "Выпуск"
 *		- по какому полю группировать?
 *	    - заполнение графы 15 не доописано
 *	    - нет проверок заполнения полей перед логической проверкой
 *
 * @author rtimerbaev
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        checkCreation()
        break
    case FormDataEvent.CHECK :
        logicalCheck(true)
        checkNSI()
        break
    case FormDataEvent.CALCULATE :
        calc()
        logicalCheck(false)
        checkNSI()
        break
    case FormDataEvent.ADD_ROW :
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW :
        deleteRow()
        break
// проверка при "подготовить"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :
        checkOnPrepareOrAcceptance('Подготовка')
        break
// проверка при "принять"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED :
        checkOnPrepareOrAcceptance('Принятие')
        break
// проверка при "вернуть из принята в подготовлена"
    case FormDataEvent.MOVE_ACCEPTED_TO_PREPARED :
        checkOnCancelAcceptance()
        break
// после принятия из подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED :
        acceptance()
        break
// обобщить
    case FormDataEvent.COMPOSE :
        consolidation()
        calc()
        logicalCheck(false)
        checkNSI()
        break
}

// графа 1  - rowNumber
// графа 2  - code
// графа 3  - valuablePaper
// графа 4  - issue
// графа 5  - purchaseDate
// графа 6  - implementationDate
// графа 7  - bondsCount
// графа 8  - purchaseCost
// графа 9  - costs
// графа 10 - marketPriceOnDateAcquisitionInPerc
// графа 11 - marketPriceOnDateAcquisitionInRub
// графа 12 - taxPrice
// графа 13 - redemptionVal
// графа 14 - exercisePrice
// графа 15 - exerciseRuble
// графа 16 - marketPricePercent
// графа 17 - marketPriceRuble
// графа 18 - exercisePriceRetirement
// графа 19 - costsRetirement
// графа 20 - allCost
// графа 21 - parPaper
// графа 22 - averageWeightedPricePaper
// графа 23 - issueDays
// графа 24 - tenureSkvitovannymiBonds
// графа 25 - interestEarned
// графа 26 - profitLoss
// графа 27 - excessOfTheSellingPrice

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def newRow = formData.createDataRow()
    formData.dataRows.add(getIndex(currentDataRow) + 1, newRow)

    // графа 2..17, 19, 21..23
    ['code', 'valuablePaper', 'issue', 'purchaseDate', 'implementationDate',
            'bondsCount', 'purchaseCost', 'costs', 'marketPriceOnDateAcquisitionInPerc',
            'marketPriceOnDateAcquisitionInRub', 'redemptionVal', 'exercisePrice',
            'exerciseRuble', 'marketPricePercent', 'marketPriceRuble', 'costsRetirement',
            'parPaper', 'averageWeightedPricePaper', 'issueDays'].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
}

/**
 * Удалить строку.
 */
def deleteRow() {
    formData.dataRows.remove(currentDataRow)
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {
    /*
     * Проверка объязательных полей.
     */

    // список проверяемых столбцов (графа 2..17, 19, 21..23)
    def requiredColumns = ['code', 'valuablePaper', 'issue', 'purchaseDate', 'implementationDate',
            'bondsCount', 'purchaseCost', 'costs', 'marketPriceOnDateAcquisitionInPerc',
            'marketPriceOnDateAcquisitionInRub', 'redemptionVal', 'exercisePrice',
            'exerciseRuble', 'marketPricePercent', 'marketPriceRuble', 'costsRetirement',
            'parPaper', 'averageWeightedPricePaper', 'issueDays']

    for (def row : formData.dataRows) {
        if (!isTotal(row) && !checkRequiredColumns(row, requiredColumns, true)) {
            return
        }
    }

    /*
     * Расчеты.
     */

    // удалить строку "итого" и "Итого за текущий месяц"
    def delRow = []
    formData.dataRows.each { row ->
        if (isTotal(row)) {
            delRow += row
        }
    }
    delRow.each { row ->
        formData.dataRows.remove(getIndex(row))
    }
    if (formData.dataRows.isEmpty()) {
        return
    }

    // отсортировать/группировать
    formData.dataRows.sort { it.issue } // TODO (Ramil Timerbaev) уточнить по какому полю группировать

    def tmp

    formData.dataRows.eachWithIndex { row, index ->
        // графа 1
        row.rowNumber = index + 1

        // графа 18
        if (row.code == 1 ||
                ((row.code == 2 || row.code == 5) && row.exercisePrice > row.marketPricePercent && row.exerciseRuble > row.marketPriceRuble) ||
                ((row.code == 2 || row.code == 5) && row.marketPricePercent == 0 && row.marketPriceRuble == 0)) {
            tmp = row.exerciseRuble
        } else if (row.code == 4) {
            tmp = row.redemptionVal
        } else if ((row.code == 2 || row.code == 5) && row.exercisePrice < row.marketPricePercent && row.exerciseRuble < row.marketPriceRuble) {
            tmp = row.marketPriceRuble
        } else {
            // TODO (Ramil Timerbaev) иначе что?
            tmp = 0
        }
        row.exercisePriceRetirement = tmp

        // графа 20
        row.allCost = row.purchaseCost + row.costs + row.costsRetirement

        // графа 24
        row.tenureSkvitovannymiBonds = row.implementationDate - row.purchaseDate

        // графа 25
        def column22 = (row.parPaper - row.averageWeightedPricePaper) * row.tenureSkvitovannymiBonds * row.bondsCount / row.issueDays
        row.interestEarned = round(column22, 0)

        // графа 23
        row.profitLoss = row.exercisePriceRetirement - row.allCost - Math.abs(row.interestEarned)

        // графа 27
        row.excessOfTheSellingPrice = (row.code != 4 ? row.exercisePriceRetirement - row.exerciseRuble : 0)
    }

    // графы для которых надо вычислять итого и итого по эмитенту (графа 7..9, 13, 15, 17..20, 25..27)
    def totalColumns = ['bondsCount', 'purchaseCost', 'costs', 'redemptionVal',
            'exerciseRuble', 'marketPriceRuble', 'exercisePriceRetirement', 'costsRetirement',
            'allCost', 'interestEarned', 'profitLoss', 'excessOfTheSellingPrice']

    // добавить строку "Итого за текущий отчётный (налоговый) период"
    def totalRow = formData.createDataRow()
    formData.dataRows.add(totalRow)
    totalRow.setAlias('total')
    totalRow.getCell('valuablePaper').setColSpan(4)
    totalRow.getCell('valuablePaper').setValue('Итого за текущий отчётный (налоговый) период')
    setTotalStyle(totalRow)
    totalColumns.each { alias ->
        totalRow.getCell(alias).setValue(getSum(alias))
    }

    // посчитать "Итого за текущий месяц"
    def totalRows = [:]
    def sums = [:]
    tmp = null
    totalColumns.each {
        sums[it] = 0
    }
    formData.dataRows.eachWithIndex { row, i ->
        if (!isTotal(row)) {
            if (tmp == null) {
                tmp = row.issue // TODO (Ramil Timerbaev) уточнить по какому полю группировать
            }
            // если код расходы поменялся то создать новую строку "Итого за текущий месяц"
            if (tmp != row.issue) { // TODO (Ramil Timerbaev) уточнить по какому полю группировать
                totalRows.put(i, getNewRow(tmp, totalColumns, sums))
                totalColumns.each {
                    sums[it] = 0
                }
            }
            // если строка последняя то сделать для ее кода расхода новую строку "Итого за текущий месяц"
            if (i == formData.dataRows.size() - 2) {
                totalColumns.each {
                    sums[it] += row.getCell(it).getValue()
                }
                totalRows.put(i + 1, getNewRow(row.issue, totalColumns, sums))
                totalColumns.each {
                    sums[it] = 0
                }
            }
            totalColumns.each {
                sums[it] += row.getCell(it).getValue()
            }
            tmp = row.issue // TODO (Ramil Timerbaev) уточнить по какому полю группировать
        }
    }
    // добавить "Итого за текущий месяц" в таблицу
    def i = 0
    totalRows.each { index, row ->
        formData.dataRows.add(index + i, row)
        i = i + 1
    }
}

/**
 * Логические проверки.
 *
 * @param useLog нужно ли записывать в лог сообщения о незаполненности обязательных полей
 */
def logicalCheck(def useLog) {
    if (!formData.dataRows.isEmpty()) {
        def i = 1

        // список проверяемых столбцов (графа 2..17, 19, 21..23)
        def requiredColumns = ['code', 'valuablePaper', 'issue', 'purchaseDate', 'implementationDate',
                'bondsCount', 'purchaseCost', 'costs', 'marketPriceOnDateAcquisitionInPerc',
                'marketPriceOnDateAcquisitionInRub', 'redemptionVal', 'exercisePrice',
                'exerciseRuble', 'marketPricePercent', 'marketPriceRuble', 'costsRetirement',
                'parPaper', 'averageWeightedPricePaper', 'issueDays']
        // суммы строки общих итогов
        def totalSums = [:]
        // графы для которых надо вычислять итого и итого по эмитенту (графа 7..9, 13, 15, 17..20, 25..27)
        def totalColumns = ['bondsCount', 'purchaseCost', 'costs', 'redemptionVal',
                'exerciseRuble', 'marketPriceRuble', 'exercisePriceRetirement', 'costsRetirement',
                'allCost', 'interestEarned', 'profitLoss', 'excessOfTheSellingPrice']
        // признак наличия итоговых строк
        def hasTotal = false
        // список групп кодов классификации для которых надо будет посчитать суммы
        def totalGroupsName = []

        def tmp

        for (def row : formData.dataRows) {
            if (isTotal(row)) {
                hasTotal = true
                continue
            }

            // TODO (Ramil Timerbaev) нет проверок заполнения полей перед логической проверкой
            // . Обязательность заполнения полей (графа 2..17, 19, 21..23)
            if (!checkRequiredColumns(row, requiredColumns, useLog)) {
                return false
            }

            // 1. Проверка рыночной цены в процентах к номиналу (графа 13, 15)
            if (row.redemptionVal > 0 && row.marketPricePercent != 100) {
                logger.error('Неверно указана цена в процентах при погашении!')
                return false
            }

            // 2. Проверка рыночной цены в рублях к номиналу (графа 13, 17)
            if (row.redemptionVal > 0 && row. redemptionVal != row.marketPriceRuble) {
                logger.error('Неверно указана цена в рублях при погашении!')
                return false
            }

            // 3. Проверка определения срока короткой позиции (графа 2, 24)
            if (row.code == 5 && row.tenureSkvitovannymiBonds >= 0) {
                logger.error('Неверно определен срок короткой позиции!')
                return false
            }

            // 4. Проверка определения процентного дохода по короткой позиции (графа 2, 25)
            if (row.code == 5 && row.interestEarned >= 0) {
                logger.error('Неверно определен процентный доход по короткой позиции!')
                return false
            }

            // 5. Проверка наличия данных предыдущих месяцев
            // Наличие экземпляров отчетов за предыдущие месяцы с начала текущего отчётного (налогового) периода
            // TODO (Ramil Timerbaev) про предыдущие месяцы пока не прояснилось
            if (false) {
                // TODO (Ramil Timerbaev) поправить сообщение
                logger.error('Экземпляр за период(ы) <Дата начала отчетного периода1> - <Дата окончания отчетного периода1>, <Дата начала отчетного периода N> - <Дата окончания отчетного периода N> не существует (отсутствуют первичные данные для расчёта)')
                return false
            }

            // 6. Арифметическая проверка графы 18
            if (row.code == 1 ||
                    (row.code in [2, 5] && row.exercisePrice > row.marketPricePercent && row.exerciseRuble > row.marketPriceRuble) ||
                    (row.code in [2, 5] && row.marketPricePercent == 0 && row.marketPriceRuble)) {
                tmp = row.exerciseRuble
            } else if (row.code == 4) {
                tmp = row.redemptionVal
            } else if (row.code in [2, 5] &&
                    row.exercisePrice < row.marketPricePercent &&
                    row.exerciseRuble < row.marketPriceRuble) {
                tmp = row.marketPriceRuble
            }
            if (row.exercisePriceRetirement != tmp) {
                logger.warn('Неверное значение поля «Цена реализации (выбытия) для целей налогообложения (руб.коп.)»!')
            }

            // 7. Арифметическая проверка графы 20
            tmp = row.purchaseCost + row.costs + row.costsRetirement
            if (row.allCost != tmp) {
                logger.warn('Неверное значение поля «Всего расходы (руб.коп.)»!')
            }

            // 8. Арифметическая проверка графы 24
            if (row.tenureSkvitovannymiBonds != row.implementationDate - row.purchaseDate) {
                logger.warn('Неверное значение поля «Показатели для расчёта процентного дохода за время владения сквитованными облигациями.Срок владения сквитованными облигациями (дни)»!')
            }

            // 9. Арифметическая проверка графы 25
            tmp = round((row.parPaper - row.averageWeightedPricePaper) * row.tenureSkvitovannymiBonds * row.bondsCount / row.issueDays, 2)
            if (row.interestEarned != tmp) {
                logger.warn('Неверное значение поля «Процентный доход, полученный за время владения сквитованными облигациями (руб.коп.)»!')
            }

            // 10. Арифметическая проверка графы 27
            tmp = row.exercisePriceRetirement - row.allCost - Math.abs(row.interestEarned)
            if (row.profitLoss != tmp) {
                logger.warn('Неверное значение поля «Прибыль (+), убыток (-) от реализации (погашения) за вычетом процентного дохода (руб.коп.)»!')
            }

            // 11. Арифметическая проверка графы 24
            tmp = row.exercisePriceRetirement - row.exerciseRuble
            if ((row.code != 4 && row.excessOfTheSellingPrice != tmp) ||
                    (row.code == 4 && row.excessOfTheSellingPrice != 0)) {
                logger.warn('Неверное значение поля «Превышение цены реализации для целей налогообложения над ценой реализации (руб.коп.)»!')
            }

            // 12. Проверка итоговых значений за текущий месяц
            if (!totalGroupsName.contains(row.issue)) {
                totalGroupsName.add(row.issue)
            }

            // 13. Проверка итоговых значений за текущий отчётный (налоговый) период - подсчет сумм для общих итогов
            totalColumns.each { alias ->
                if (totalSums[alias] == null) {
                    totalSums[alias] = 0
                }
                totalSums[alias] += (row.getCell(alias).getValue() ?: 0)
            }

            // 14. Проверка на уникальность поля «№ пп» (графа 1)
            if (i != row.rowNumber) {
                logger.error('Нарушена уникальность номера по порядку!')
                return false
            }
            i += 1
        }

        if (hasTotal) {
            def totalRow = formData.getDataRow('total')

            // 12. Проверка итоговых значений за текущий месяц
            for (def codeName : totalGroupsName) {
                def row = formData.getDataRow('total' + codeName)
                for (def alias : totalColumns) {
                    if (calcSumByCode(codeName, alias) != row.getCell(alias).getValue()) {
                        logger.error('Итоговые значения за текущий месяц рассчитаны неверно!')
                        return false
                    }
                }
            }

            // 13. Проверка итоговых значений за текущий отчётный (налоговый) период
            for (def alias : totalColumns) {
                if (totalSums[alias] != totalRow.getCell(alias).getValue()) {
                    logger.error('Итоговые значения за текущий отчётный (налоговый) период рассчитаны неверно!')
                    return false
                }
            }
        }
    }
    return true
}

/**
 * Проверки соответствия НСИ.
 */
def checkNSI() {
    // 1. Проверка актуальности поля «Код сделки» (графа 2)
    if (false) {
        logger.warn('')
    }

    // 2. Проверка актуальности поля «Признак ценной бумаги» (графа 3)
    if (false) {
        logger.warn('')
    }
    return true
}

/**
 * Проверка наличия и статуса консолидированной формы при осуществлении перевода формы в статус "Подготовлена"/"Принята".
 */
void checkOnPrepareOrAcceptance(def value) {
    departmentFormTypeService.getFormDestinations(formDataDepartment.id,
            formData.getFormType().getId(), formData.getKind()).each() { department ->
        if (department.formTypeId == formData.getFormType().getId()) {
            def form = FormDataService.find(department.formTypeId, department.kind, department.departmentId, formData.reportPeriodId)
            // если форма существует и статус "принята"
            if (form != null && form.getState() == WorkflowState.ACCEPTED) {
                logger.error("$value первичной налоговой формы невозможно, т.к. уже подготовлена консолидированная налоговая форма.")
            }
        }
    }
}

/**
 * Консолидация.
 */
void consolidation() {
    // удалить все строки и собрать из источников их строки
    formData.dataRows.clear()

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = FormDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                source.getDataRows().each { row->
                    if (row.getAlias() == null || row.getAlias() == '') {
                        formData.dataRows.add(row)
                    }
                }
            }
        }
    }
    logger.info('Формирование консолидированной формы прошло успешно.')
}

/**
 * Проверки при переходе "Отменить принятие".
 */
void checkOnCancelAcceptance() {
    List<DepartmentFormType> departments = departmentFormTypeService.getFormDestinations(formData.getDepartmentId(),
            formData.getFormType().getId(), formData.getKind());
    DepartmentFormType department = departments.getAt(0);
    if (department != null) {
        FormData form = FormDataService.find(department.formTypeId, department.kind, department.departmentId, formData.reportPeriodId)

        if (form != null && (form.getState() == WorkflowState.PREPARED || form.getState() == WorkflowState.ACCEPTED)) {
            logger.error("Нельзя отменить принятие налоговой формы, так как уже принята вышестоящая налоговая форма")
        }
    }
}

/**
 * Принять.
 */
void acceptance() {
    if (!logicalCheck(true) || !checkNSI()) {
        return
    }
    departmentFormTypeService.getFormDestinations(formDataDepartment.id,
            formData.getFormType().getId(), formData.getKind()).each() {
        formDataCompositionService.compose(formData, it.departmentId, it.formTypeId, it.kind, logger)
    }
}

/**
 * Проверка при создании формы.
 */
void checkCreation() {
    // отчётный период
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

    //проверка периода ввода остатков
    if (reportPeriod != null && reportPeriod.reportPeriodService.isBalancePeriod(reportPeriod.id, formData.departmentId)) {
        logger.error('Налоговая форма не может создаваться в периоде ввода остатков.')
        return
    }

    def findForm = FormDataService.find(formData.formType.id,
            formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }
}

/*
 * Вспомогательные методы.
 */

/**
 * Проверка является ли строка итоговой.
 */
def isTotal(def row) {
    return row != null && row.getAlias() != null && row.getAlias().contains('total')
}

/**
 * Поправить значания order.
 */
void setOrder() {
    formData.dataRows.eachWithIndex { row, index ->
        row.setOrder(index + 1)
    }
}

/**
 * Получить сумму столбца.
 */
def getSum(def columnAlias) {
    return summ(formData, new ColumnRange(columnAlias, 0, formData.dataRows.size() - 2))
}

/**
 * Получить новую строку.
 */
def getNewRow(def alias, def totalColumns, def sums) {
    def newRow = formData.createDataRow()
    newRow.setAlias('total' + alias)
    newRow.valuablePaper = 'Итого за текущий месяц'
    newRow.getCell('valuablePaper').setColSpan(4)
    setTotalStyle(newRow)
    totalColumns.each {
        newRow.getCell(it).setValue(sums[it])
    }
    return newRow
}

/**
 * Устаносить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    ['rowNumber', 'code', 'valuablePaper', 'issue', 'purchaseDate',
            'implementationDate', 'bondsCount', 'purchaseCost', 'costs',
            'marketPriceOnDateAcquisitionInPerc', 'marketPriceOnDateAcquisitionInRub', 'taxPrice',
            'redemptionVal', 'exercisePrice', 'exerciseRuble',
            'marketPricePercent', 'marketPriceRuble', 'exercisePriceRetirement',
            'costsRetirement', 'allCost', 'parPaper', 'averageWeightedPricePaper',
            'issueDays', 'tenureSkvitovannymiBonds', 'interestEarned',
            'profitLoss', 'excessOfTheSellingPrice'].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
}

/**
 * Посчитать сумму указанного графа для строк с общим значением
 *
 * @param value значение общее для всех строк суммирования
 * @param alias название графа
 */
def calcSumByCode(def value, def alias) {
    def sum = 0
    formData.dataRows.each { row ->
        if (!isTotal(row) && row.issue == value) {
            sum += (row.getCell(alias).getValue() ?: 0)
        }
    }
    return sum
}

/**
 * Получить номер строки в таблице.
 */
def getIndex(def row) {
    formData.dataRows.indexOf(row)
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

    columns.each {
        if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
            def name = row.getCell(it).getColumn().getName().replace('%', '%%')
            colNames.add('"' + name + '"')
        }
    }
    if (!colNames.isEmpty()) {
        if (!useLog) {
            return false
        }
        def index = getIndex(row) + 1
        def errorMsg = colNames.join(', ')
        if (index != null) {
            logger.error("В строке \"№ пп\" равной $index не заполнены колонки : $errorMsg.")
        } else {
            index = getIndex(row) + 1
            logger.error("В строке $index не заполнены колонки : $errorMsg.")
        }
        return false
    }
    return true
}