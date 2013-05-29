/**
 * Скрипт для РНУ-46 (rnu46.groovy).
 * Форма "(РНУ-46) Регистр налогового учёта «карточка по учёту основных средств и капитальных вложений в неотделимые улучшения арендованного и полученного по договору безвозмездного пользования имущества»".
 *
 * @version 59
 *
 * TODO:
 *      - нет условии в проверках соответствия НСИ (потому что нету справочников)
 *		- получение значений за предыдущий месяц, за предыдущие месяцы
 *		- проверка уникальности инвентарного номера
 *
 * @author rtimerbaev
 */

import java.text.SimpleDateFormat

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
// графа 2  - invNumber
// графа 3  - name
// графа 4  - cost
// графа 5  - amortGroup
// графа 6  - usefulLife
// графа 7  - monthsUsed
// графа 8  - usefulLifeWithUsed
// графа 9  - specCoef
// графа 10 - cost10perMonth
// графа 11 - cost10perTaxPeriod
// графа 12 - cost10perExploitation
// графа 13 - amortNorm
// графа 14 - amortMonth
// графа 15 - amortTaxPeriod
// графа 16 - amortExploitation
// графа 17 - exploitationStart
// графа 18 - usefullLifeEnd
// графа 19 - rentEnd

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def newRow = formData.createDataRow()
    formData.dataRows.add(getIndex(currentDataRow) + 1, newRow)

    // графа 2..7, 9, 17..19
    ['invNumber', 'name', 'cost', 'amortGroup', 'usefulLife', 'monthsUsed',
            'specCoef', 'exploitationStart', 'usefullLifeEnd', 'rentEnd'].each {
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

    // список проверяемых столбцов (графа 2..7, 9, 17..19)
    def requiredColumns = ['invNumber', 'name', 'cost', 'amortGroup', 'usefulLife', 'monthsUsed',
            'specCoef', 'exploitationStart', 'usefullLifeEnd', 'rentEnd']

    for (def row : formData.dataRows) {
        if (!checkRequiredColumns(row, requiredColumns, true)) {
            return
        }
    }

    /*
     * Расчеты.
     */

    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    def lastDay2001 = format.parse('31.12.2001')

    def tmp = null
    formData.dataRows.eachWithIndex { row, index ->
        // графа 1
        row.rowNumber = index + 1

        // графа 8
        // TODO (Ramil Timerbaev) спросить у аналитика
        if (row.specCoef > 0) {
            row.usefulLifeWithUsed = (row.usefulLife - row.monthsUsed) / row.specCoef
        } else {
            row.usefulLifeWithUsed = 0 // TODO (Ramil Timerbaev) не описано в чтз
        }

        // графа 10
        tmp = 0
        if (row.amortGroup in ['1', '2', '8', '9', '10']) {
            tmp = row.cost * 0.1
        } else if (row.amortGroup in ('3'..'7')) {
            tmp = row.cost * 0.3
        }
        row.cost10perMonth = tmp

        // графа 12
        // TODO (Ramil Timerbaev) getFromOld() = 12 графа предыдущего месяца
        row.cost10perExploitation = getFromOld() + row.cost10perMonth

        // графа 13
        if (row.usefulLifeWithUsed != 0) {
            row.amortNorm = (1 / row.usefulLifeWithUsed) * 100
        } else {
            row.amortNorm = 0
        }

        // графа 14
        // TODO (Ramil Timerbaev) требуется пояснение относительно этой формулы
        if (row.usefullLifeEnd > lastDay2001) {
            row.amortMonth = 0
            // row.amortMonth = (row.cost (на начало месяца) - row.cost10perExploitation - row.amortExploitation (на начало месяца)) / (row.usefullLifeEnd - последнее число предыдущего месяца)
        } else {
            row.amortMonth = row.cost / 84
        }

        // графа 11, 15, 16
        if (isFirstMonth()) {
            row.cost10perTaxPeriod = row.cost10perMonth
            row.amortTaxPeriod = row.amortMonth
            row.amortExploitation = row.amortMonth
        } else {
            // TODO (Ramil Timerbaev) getFromOld() = 11 графа предыдущего месяца
            row.cost10perTaxPeriod = getFromOld() + row.cost10perMonth

            // TODO (Ramil Timerbaev) getFromOld() = 15 графа предыдущего месяца
            row.amortTaxPeriod = getFromOld() + row.amortMonth

            // TODO (Ramil Timerbaev) getFromOld() = 16 графа предыдущего месяца
            row.amortExploitation = getFromOld() + row.amortMonth
        }
    }
}

/**
 * Проверки соответствия НСИ.
 */
def checkNSI() {
    // 1. Проверка амортизационной группы (графа 5)
    if (false) {
        logger.error('Амортизационная группа не существует!')
        return false
    }

    // 2. Проверка срока полезного использования (графа 6)
    if (false) {
        logger.error('Срок полезного использования указан неверно!')
        return false
    }
    return true
}

/**
 * Логические проверки.
 *
 * @param useLog нужно ли записывать в лог сообщения о незаполненности обязательных полей
 */
def logicalCheck(def useLog) {
    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    def lastDay2001 = format.parse('31.12.2001')

    // список проверяемых столбцов (графа 1..18)
    def columns = ['rowNumber', 'invNumber', 'name', 'cost', 'amortGroup',
            'usefulLife', 'monthsUsed', 'usefulLifeWithUsed', 'specCoef',
            'cost10perMonth', 'cost10perTaxPeriod', 'cost10perExploitation',
            'amortNorm', 'amortMonth', 'amortTaxPeriod', 'amortExploitation',
            'exploitationStart', 'usefullLifeEnd', 'rentEnd']

    // последнее число предыдущего месяца
    tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
    def lastDayPrevMonth = (tmp ? tmp.getTime() : null)

    def hasError
    for (def row : formData.dataRows) {
        // 1. Обязательность заполнения поля (графа 1..18)
        if (!checkRequiredColumns(row, columns, useLog)) {
            return
        }

        // 2. Проверка на уникальность поля «инвентарный номер» (графа 2)
        // TODO (Ramil Timerbaev) Как должна производиться эта проверка?
        if (false) {
            logger.warn('Инвентарный номер не уникальный!')
        }

        // 3. Проверка на нулевые значения (графа 9, 10, 11, 13, 14, 15)
        if (row.specCoef == 0 &&
                row.cost10perMonth == 0 &&
                row.cost10perTaxPeriod == 0 &&
                row.amortNorm &&
                row.amortMonth == 0 &&
                row.amortTaxPeriod) {
            logger.error('Все суммы по операции нулевые!')
            return false
        }

        // 4. Проверка суммы расходов в виде капитальных вложений с начала года (графа 10, 9, 10 (за прошлый месяц), 9 (за предыдущие месяцы текущего года))
        if (row.cost10perMonth >= row.specCoef &&
                // TODO (Ramil Timerbaev) getFromOld() = 10 графа предыдущего месяца
                row.cost10perMonth == row.specCoef + getFromOld() &&
                // TODO (Ramil Timerbaev) getFromOld() = сумма графы 9 всех предыдущих месяцев
                row.cost10perMonth == getFromOld()) {
            logger.error('Неверная сумма расходов в виде капитальных вложений с начала года!')
            return false
        }

        // 5. Проверка суммы начисленной амортизации с начала года (графа 14, 13, 14 (за прошлый месяц), 13 (за предыдущие месяцы текущего года))
        if (row.amortMonth < row.amortNorm ||
                // TODO (Ramil Timerbaev) getFromOld() = 14 графа предыдущего месяца
                row.amortMonth != row.amortNorm + getFromOld() ||
                // TODO (Ramil Timerbaev) getFromOld() = сумма графы 13 всех предыдущих месяцев
                row.amortMonth != getFromOld()) {
            logger.error('Неверная сумма начисленной амортизации с начала года!')
            return false
        }

        // 6. Арифметическая проверка графы 8
        hasError = false
        if (row.specCoef < 0 || row.usefulLifeWithUsed != (row.usefulLife - row.monthsUsed) / row.specCoef) {
            hasError = true
        } else if (row.specCoef == 0) {
            hasError = true
        } else {
            // row.usefulLifeWithUsed = 0 // TODO (Ramil Timerbaev) не описано в чтз
        }
        if (hasError) {
            logger.error('Неверное значение графы «Срок полезного использования с учётом срока эксплуатации предыдущими собственниками (арендодателями, ссудодателями) либо установленный самостоятельно, (мес.)»!')
            return false
        }

        // 7. Арифметическая проверка графы 10
        hasError = false
        if (row.amortGroup in ['1', '2', '8', '9', '10'] && row.cost10perMonth != row.cost * 0.1) {
            hasError = true
        } else if (row.amortGroup in ('3'..'7') && row.cost10perMonth != row.cost * 0.3) {
            hasError = true
        }
        if (hasError) {
            logger.error('Неверное значение графы «10%% (30%%) от первоначальной стоимости, включаемые в расходы.За месяц»!')
            return false
        }

        // 8. Арифметическая проверка графы 11
        hasError = false
        if (isFirstMonth() && row.cost10perTaxPeriod != row.cost10perMonth) {
            hasError = true

            // TODO (Ramil Timerbaev) getFromOld() = 11 графа предыдущего месяца
        } else if (!isFirstMonth() && row.cost10perTaxPeriod != getFromOld() + row.cost10perMonth) {
            hasError = true
        }
        if (hasError) {
            logger.error('Неверное значение графы «10%% (30%%) от первоначальной стоимости, включаемые в расходы.с начала налогового периода»!')
            return false
        }

        // 9. Арифметическая проверка графы 12
        // TODO (Ramil Timerbaev) getFromOld() = 12 графа предыдущего месяца
        if (row.cost10perExploitation != getFromOld() + row.cost10perMonth) {
            logger.error('Неверное значение графы «10%% (30%%) от первоначальной стоимости, включаемые в расходы.с даты ввода в эксплуатацию»!')
            return false
        }

        // 10. Арифметическая проверка графы 13
        hasError = false
        if (row.usefulLifeWithUsed != 0 && row.amortNorm != (1 / row.usefulLifeWithUsed) * 100) {
            hasError = true
        } else if (row.usefulLifeWithUsed == 0) {
            // hasError = true // TODO (Ramil Timerbaev) уточнить
        }
        if (hasError) {
            logger.error('Неверное значение графы «Норма амортизации (процентов в мес.)»!')
            return false
        }

        // 11. Арифметическая проверка графы 14
        hasError = false

        // TODO (Ramil Timerbaev) требуется пояснение относительно этой формулы
        // row.amortMonth = (row.cost (на начало месяца) - row.cost10perExploitation - row.amortExploitation (на начало месяца)) / (row.usefullLifeEnd - последнее число предыдущего месяца)
        def tmp = (row.amortMonth != (row.cost - row.cost10perExploitation - row.amortExploitation) / (row.usefullLifeEnd - lastDayPrevMonth))
        if (row.usefullLifeEnd > lastDay2001 && tmp) {
            hasError = true
        } else if (row.usefullLifeEnd <= lastDay2001 && row.amortMonth != row.cost / 84) {
            hasError = true
        }
        // TODO (Ramil Timerbaev) убрать && false
        if (hasError && false) {
            logger.error('Неверно рассчитана графа «Сумма начисленной амортизации.за месяц»!')
            return false
        }

        // 12. Арифметическая проверка графы 15
        hasError = false
        if (isFirstMonth() && row.amortTaxPeriod != row.amortMonth) {
            hasError = true

            // TODO (Ramil Timerbaev) getFromOld() = 15 графа предыдущего месяца
        } else if (!isFirstMonth() && row.amortTaxPeriod != getFromOld() + row.amortMonth) {
            hasError = true
        }
        if (hasError) {
            logger.error('Неверное значение графы «Сумма начисленной амортизации.с начала налогового периода»!')
            return false
        }

        // 13. Арифметическая проверка графы 16
        hasError = false
        if (isFirstMonth() && row.amortExploitation != row.amortMonth) {
            hasError = true

            // TODO (Ramil Timerbaev) getFromOld() = 16 графа предыдущего месяца
        } else if (!isFirstMonth() && row.amortExploitation != getFromOld() + row.amortMonth) {
            hasError = true
        }
        if (hasError) {
            logger.error('Неверное значение графы «Сумма начисленной амортизации.с даты ввода в эксплуатацию»!')
            return false
        }
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
                    formData.dataRows.add(row)
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
 * Получить значение из предыдущего месяца.
 */
def getFromOld() {
    // TODO (Ramil Timerbaev)
    /*
    // предыдущий отчётный период
    def reportPeriodOld = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)

    // РНУ-25 за предыдущий отчетный период
    def formDataOld = null
    if (reportPeriodOld != null) {
        formDataOld = FormDataService.find(formData.formType.id, formData.kind, formDataDepartment.id, reportPeriodOld.id)
    }
    */
    return 0
}

/**
 * Первый ли это месяц (январь)
 */
def isFirstMonth() {
    // отчётный период
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

    if (reportPeriod != null && reportPeriod.getOrder() == 1) {
        return true
    }
    return false
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