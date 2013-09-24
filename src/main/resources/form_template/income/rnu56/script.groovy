/**
 * Скрипт для РНУ-56 (rnu56.groovy).
 * Форма "(РНУ-56) Регистр налогового учёта процентного дохода по процентным векселям сторонних эмитентов".
 *
 * @version 59
 *
 * TODO:
 *      - нет уcловии в проверках соответствия НСИ (потому что нету справочников)
 *		- уточнить чтз про графу 17, в нф всего графов 15
 *		- уточнить про вычисление 14ой графы, последний блок, по предыдущим условиям туда никогда не попадет
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

// графа 1  - number
// графа 2  - bill
// графа 3  - buyDate
// графа 4  - currency
// графа 5  - nominal
// графа 6  - price
// графа 7  - maturity
// графа 8  - termDealBill
// графа 9  - percIncome
// графа 10 - implementationDate
// графа 11 - sum
// графа 12 - discountInCurrency
// графа 13 - discountInRub
// графа 14 - sumIncomeinCurrency
// графа 15 - sumIncomeinRuble

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def newRow = formData.createDataRow()
    formData.dataRows.add(getIndex(currentDataRow) + 1, newRow)

    // графа 2..7, 10, 11
    ['bill', 'buyDate', 'currency', 'nominal', 'price',
            'maturity', 'implementationDate', 'sum'].each {
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
     * Проверка обязательных полей.
     */

    // список проверяемых столбцов (графа 2..7)
    def requiredColumns = ['bill', 'buyDate', 'currency', 'nominal', 'price', 'maturity']

    for (def row : formData.dataRows) {
        if (!isTotal(row) && !checkRequiredColumns(row, requiredColumns, true)) {
            return
        }
    }

    /*
     * Расчеты
     */

    // удалить строку "итого"
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

    def tmp

    /** Отчетная дата. */
    def reportDate = getReportDate()

    /** Начальная дата отчетного периода. */
    tmp = reportPeriodService.getStartDate(formData.reportPeriodId)
    def reportDateStart = (tmp ? tmp.getTime() : null)

    /** Количество дней владения векселем в отчетном периоде. */
    def countsDays = 1

    /** Курс банка России. */
    def rate

    formData.dataRows.eachWithIndex { row, i ->
        // графа 1
        row.number = i + 1

        // графа 8
        row.termDealBill = round(row.buyDate - row.maturity + 1, 0)

        // графа 9
        row.percIncome = row.nominal - row.price

        // графа 12
        row.discountInCurrency = row.sum - row.price

        // графа 13
        // TODO (Ramil Timerbaev) уточнить чтз про графу 17, в нф всего графов 15
        row.discountInRub = round(row.discountInCurrency * getRate(row.currency), 2)

        // графа 14
        if (row.implementationDate == null && row.sum == null) {
            countsDays = (row.buyDate >= reportDateStart ?
                reportDate - row.buyDate + 1 : reportDate - reportDateStart)
            if (row.termDealBill != 0) {
                tmp = row.percIncome / row.termDealBill * countsDays
            } else {
                def index = getIndex(row)
                logger.warn("Невозможно вычислить графу 10 в строке $index. Деление на ноль. Количество дней владения векселем в отчётном периоде равно 0.")
            }
        } else {
            tmp = (row.implementationDate != null && row.sum == null ? row.percIncome : row.discountInCurrency) -
                    getCalcPrevColumn(row.bill, 'sumIncomeinCurrency')
        }
        row.sumIncomeinCurrency = round(tmp, 2)

        // графа 15
        if (row.implementationDate == null && row.sum == null) {
            rate = getRate(reportDate)
            tmp = row.sumIncomeinCurrency * rate
        } else if (row.implementationDate != null && row.sum == null) { // последнее условие поменял местами
            tmp = row.discountInRub - getCalcPrevColumn10(row.bill, 'sumIncomeinRuble')
        } else if (row.implementationDate != null) {
            rate = getRate(row.implementationDate)
            tmp = row.sumIncomeinCurrency * rate
        }
        row.sumIncomeinRuble = round(tmp, 2)
    }

    // итого (графа 13, 15)
    def totalRow = formData.createDataRow()
    formData.dataRows.add(totalRow)
    totalRow.setAlias('total')
    totalRow.bill = 'Итого'
    setTotalStyle(totalRow)
    ['discountInRub', 'sumIncomeinRuble'].each { alias ->
        totalRow.getCell(alias).setValue(getSum(alias))
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

        def requiredColumns = ['number', 'bill', 'buyDate', 'currency', 'nominal',
                'price', 'maturity', 'termDealBill', 'percIncome',
                'implementationDate', 'sum', 'discountInCurrency',
                'discountInRub', 'sumIncomeinCurrency', 'sumIncomeinRuble']

        // суммы строки общих итогов
        def totalSums = [:]

        // графы для которых надо вычислять итого (графа 13, 15)
        def totalColumns = ['discountInRub', 'sumIncomeinRuble']

        // признак наличия итоговых строк
        def hasTotal = false

        /** Дата начала отчетного периода. */
        def tmp = reportPeriodService.getStartDate(formData.reportPeriodId)
        def a = (tmp ? tmp.getTime() : null)

        /** Дата окончания отчетного периода. */
        tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
        def b = (tmp ? tmp.getTime() : null)

        /** Отчетная дата. */
        def reportDate = getReportDate()

        def cell
        def hasError
        for (def row : formData.dataRows) {
            if (isTotal(row)) {
                hasTotal = true
                continue
            }

            if (!checkRequiredColumns(row, requiredColumns, useLog)) {
                return false
            }

            // TODO (Ramil Timerbaev)
            // 2. Проверка даты приобретения и границ отчетного периода (графа 3)
            if (row.buyDate > b) {
                logger.error('Дата приобретения вне границ отчетного периода!')
                return false
            }

            // 3. Проверка даты реализации (погашения)  и границ отчетного периода (графа 7)
            if (row.implementationDate < a || b < row.implementationDate) {
                logger.error('Дата реализации (погашения) вне границ отчетного периода!')
                return false
            }

            // 4. Проверка на уникальность поля «№ пп» (графа 1) (в рамках текущего года)
            if (i != row.number) {
                logger.error('Нарушена уникальность номера по порядку!')
                return false
            }
            i = i + 1

            // 5. Проверка на нулевые значения (графа 11)
            // TODO (Ramil Timerbaev)
            if (row.sum != null && false) {
                logger.error('Поле ”<Наименование поля>” при отсутствии сумм не заполняется!')
                return false
            }

            // 6. Проверка на нулевые значения (графа 12..15)
            hasError = false
            ['discountInCurrency', 'discountInRub', 'sumIncomeinCurrency',
                    'sumIncomeinRuble'].each {
                if (row.getCell(it).getValue() == 0) {
                    hasError = true
                }
            }
            if (hasError) {
                logger.error('Все суммы по операции нулевые!')
                return false
            }

            // 7. Проверка на наличие данных предыдущих отчетных периодов для заполнения графы 14 и графы 15
            // TODO (Ramil Timerbaev)
            if (false) {
                logger.error("Экземпляр за период(ы) <Дата начала отчетного периода1> - <Дата окончания отчетного периода1>, <Дата начала отчетного периода N> - <Дата окончания отчетного периода N> не существует (отсутствуют первичные данные для расчёта)!")
                return false
            }

            // 8. Проверка корректности расчёта дисконта
            if (row.sum - row.price <= 0 && (row.discountInCurrency != 0 || row.discountInRub != 0)) {
                logger.error('Расчёт дисконта некорректен!')
                return false
            }

            // 9. Проверка на неотрицательные значения (графа 12, 13)
            hasError = false
            ['discountInCurrency', 'discountInRub'].each {
                cell = row.getCell(it)
                if (cell.getValue() != null && cell.getValue() < 0) {
                    def name = cell.getColumn().getName()
                    logger.error("Значение графы \"$name\"  отрицательное!")
                    hasError = true
                }
            }
            if (hasError) {
                return false
            }

            // 10. Арифметическая проверка графы 8
            if (row.termDealBill != round(row.buyDate - row.maturity + 1, 0)) {
                logger.warn('Неверно рассчитана графа «Возможный срок обращения векселя, дней»!')
            }

            // 11. Арифметическая проверка графы 9
            if (row.percIncome != row.nominal - row.price) {
                logger.warn('Неверно рассчитана графа «Заявленный процентный доход (дисконт), ед. валюты»!')
            }

            // 12. Арифметическая проверка графы 12
            if (row.discountInCurrency != row.sum - row.price) {
                logger.warn('Неверно рассчитана графа «Фактически поступившая сумма дисконта в валюте»!')
            }

            // 13. Арифметическая проверка графы 13
            // TODO (Ramil Timerbaev) уточнить чтз про графу 17, в нф всего графов 15
            if (row.discountInRub != round(row.discountInCurrency * getRate(row.currency), 2)) {
                logger.warn('Неверно рассчитана графа «Фактически поступившая сумма дисконта в рублях по курсу Банка России»!')
            }

            // 14. Арифметическая проверка графы 14
            if (row.implementationDate == null && row.sum == null) {
                countsDays = (row.buyDate >= reportDateStart ?
                    reportDate - row.buyDate + 1 : reportDate - reportDateStart)
                if (row.termDealBill != 0) {
                    tmp = row.percIncome / row.termDealBill * countsDays
                } else {
                    def index = getIndex(row)
                    logger.warn("Невозможно вычислить графу 10 в строке $index. Деление на ноль. Количество дней владения векселем в отчётном периоде равно 0.")
                }
            } else {
                tmp = (row.implementationDate != null && row.sum == null ? row.percIncome : row.discountInCurrency) -
                        getCalcPrevColumn(row.bill, 'sumIncomeinCurrency')
            }
            if (row.sumIncomeinCurrency != round(tmp, 2)) {
                logger.warn('Неверно рассчитана графа «Сумма начисленного процентного дохода за отчётный период в валюте»!')
            }

            // 15. Арифметическая проверка графы 15
            if (row.implementationDate == null && row.sum == null) {
                rate = getRate(reportDate)
                tmp = row.sumIncomeinCurrency * rate
            } else if (row.implementationDate != null && row.sum == null) { // последнее условие поменял местами
                tmp = row.discountInRub - getCalcPrevColumn10(row.bill, 'sumIncomeinRuble')
            } else if (row.implementationDate != null) {
                rate = getRate(row.implementationDate)
                tmp = row.sumIncomeinCurrency * rate
            }
            if (row.sumIncomeinRuble != round(tmp, 2)) {
                logger.warn('Неверно рассчитана графа «Сумма начисленного процентного дохода за отчётный период в рублях по курсу Банка России»!')
            }

            // 16. Проверка итогового значений по всей форме - подсчет сумм для общих итогов
            totalColumns.each { alias ->
                if (totalSums[alias] == null) {
                    totalSums[alias] = 0
                }
                totalSums[alias] += (row.getCell(alias).getValue() ?: 0)
            }
        }

        if (hasTotal) {
            def totalRow = formData.getDataRow('total')

            // 16. Проверка итогового значений по всей форме (графа 13, 15)
            for (def alias : totalColumns) {
                if (totalSums[alias] != totalRow.getCell(alias).getValue()) {
                    logger.error('Итоговые значения рассчитаны неверно!')
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
    if (!formData.dataRows.isEmpty()) {
        for (def row : formData.dataRows) {
            if (isTotal(row)) {
                continue
            }

            // 1. Проверка кода валюты со справочным (графа 4)
            if (false) {
                logger.warn('Неверный код валюты!')
            }
        }
    }
    return true
}

/**
 * Проверка при создании формы.
 */
void checkCreation() {
    // отчётный период
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

    //проверка периода ввода остатков
    if (reportPeriod != null && reportPeriodService.isBalancePeriod(reportPeriod.id, formData.departmentId)) {
        logger.error('Налоговая форма не может создаваться в периоде ввода остатков.')
        return
    }

    def findForm = FormDataService.find(formData.formType.id,
            formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }
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
 * Проверка пустое ли значение.
 */
def isEmpty(def value) {
    return value == null || value == '' || value == 0
}

/**
 * Устаносить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    ['number', 'bill', 'buyDate', 'currency', 'nominal',
            'price', 'maturity', 'termDealBill', 'percIncome',
            'implementationDate', 'sum', 'discountInCurrency',
            'discountInRub', 'sumIncomeinCurrency', 'sumIncomeinRuble'].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
}

/**
 * Получить номер строки в таблице.
 */
def getIndex(def row) {
    formData.dataRows.indexOf(row)
}

// TODO (Ramil Timerbaev) учесть графу 3 при суммировании
/**
 * Cумма ранее начисленного процентного дохода по векселю до отчётного периода
 * (сумма граф 14 из РНУ-56 предыдущих отчётных (налоговых) периодов)
 * выбирается по графе 2 с даты приобретения (графа 3) по дату начала отчетного периода.
 *
 * @param bill вексель
 * @param sumColumnName название графы, по которой суммировать данные
 */
def getCalcPrevColumn(def bill, def sumColumnName) {
    def formDataOld = getFormDataOld()
    def sum = 0
    if (formDataOld == null) {
        return 0
    }
    formDataOld.dataRows.each {
        if (bill == row.bill) {
            sum += getValue(row.getCell(sumColumnName).getValue())
        }
    }
    return sum
}

/**
 * Получить данные за предыдущий отчетный период
 */
def getFormDataOld() {
    // предыдущий отчётный период
    def reportPeriodOld = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)

    // РНУ-55 за предыдущий отчетный период
    def formDataOld = null
    if (reportPeriodOld != null) {
        formDataOld = FormDataService.find(formData.formType.id, formData.kind, formDataDepartment.id, reportPeriodOld.id)
    }

    return formDataOld
}

/**
 * Получить значение или ноль, если значения нет.
 */
def getValue(def value) {
    return (value != null ? value : 0)
}

/**
 * Получить курс банка России на указанную дату.
 */
def getRate(def date) {
    // TODO (Ramil Timerbaev) откуда брать?
    return 1
}

/**
 * Получить сумму столбца.
 */
def getSum(def columnAlias) {
    def from = 0
    def to = formData.dataRows.size() - 2
    if (from > to) {
        return 0
    }
    return summ(formData, new ColumnRange(columnAlias, from, to))
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
        if (!isEmpty(index)) {
            logger.error("В строке \"№ пп\" равной $index не заполнены колонки : $errorMsg.")
        } else {
            index = getIndex(row) + 1
            logger.error("В строке $index не заполнены колонки : $errorMsg.")
        }
        return false
    }
    return true
}

/**
 * Получить отчетную дату.
 */
def getReportDate() {
    def tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
    return (tmp ? tmp.getTime() + 1 : null)
}