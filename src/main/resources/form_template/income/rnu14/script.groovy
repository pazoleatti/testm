package form_template.income.rnu14
/**
 * Скрипт для УНП (unp.groovy).
 * Форма "УНП"("РНУ-14 - Регистр налогового учёта нормируемых расходов").
 *
 * @version 1
 *
 * TODO:
 *      -
 *
 * @author lhaziev
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        checkCreation()
        break
    case FormDataEvent.CHECK :
        logicalCheck(true)
        break
    case FormDataEvent.CALCULATE :
        calc()
        logicalCheck(false)
        break
    case FormDataEvent.ADD_ROW :
        // addNewRow()
        break
    case FormDataEvent.DELETE_ROW :
        // deleteRow()
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
//        consolidation()
        calc()
        logicalCheck(false)
        break
}

// графа 1  - rowNumber
// графа 2  - mode
// графа 3  - sum
// графа 4  - normBase
// графа 5  - normCoef
// графа 6  - limitSum
// графа 7  - inApprovedNprms
// графа 8  - overApprovedNprms


/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {
    /*
     * Проверка объязательных полей.
     */

    // список проверяемых столбцов (графа 3..4)
    def requiredColumns = ['normBase']

    for (def row : formData.dataRows) {
        if (!isTotal(row) && !checkRequiredColumns(row, requiredColumns, true)) {
            return
        }
    }

    def col = ['21270', '21410', '20698', '20700', '20690']
    def koeffNormBase = [4/100, 1/100, 6/100, 12/100, 15000]

    for (def row : formData.dataRows) {
        def rowA = getTotalRowFromRNU(col[getIndex(row)])
        if (rowA!=null) {
            // 3 - графа 8 строки А + (графа 5 строки А – графа 6 строки А)
            row.sum = rowA.rnu5Field5Accepted?:0 + rowA.rnu7Field10Sum?:0  - rowA.rnu7Field12Accepted?:0
            // 6
            row.limitSum = koeffNormBase[getIndex(row)] * row.normBase
            def diff6_3 = row.limitSum - row.sum
            // 7 - 1. ЕСЛИ («графа 6» – «графа 3») ≥ 0, то «графа 3»;
            //     2. ЕСЛИ («графа 6» – «графа 3») < 0, то «графа 6»;
            if (diff6_3>=0) {
                row.inApprovedNprms = row.sum
            } else {
                row.inApprovedNprms = row.limitSum
            }
            // 8 - 1. ЕСЛИ («графа 6» – «графа 3») ≥ 0, то 0;
            //     2. ЕСЛИ («графа 6» – «графа 3») < 0, то «графа 3» - «графа 6».
            if (diff6_3>=0) {
                row.overApprovedNprms = 0
            } else {
                row.overApprovedNprms = -diff6_3
            }
        }
    }
}

/**
 * Логические проверки.
 *
 * @param useLog нужно ли записывать в лог сообщения о незаполненности обязательных полей
 */
def logicalCheck(def useLog) {

    // список проверяемых столбцов (графа 4)
    def requiredColumns = ['normBase']
    for (def row : formData.dataRows) {
        // 1. Обязательность заполнения полей графы 4
        if (!checkRequiredColumns(row, requiredColumns, useLog)) {
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
    logger.info('Формирование консолидированной формы прошло успешно.')
}

/**
 * Проверки при переходе "Отменить принятие".
 */
void checkOnCancelAcceptance() {
    def departments = departmentFormTypeService.getFormDestinations(formData.getDepartmentId(),
            formData.getFormType().getId(), formData.getKind());
    def department = departments.getAt(0);
    if (department != null) {
        def form = FormDataService.find(department.formTypeId, department.kind, department.departmentId, formData.reportPeriodId)

        if (form != null && (form.getState() == WorkflowState.PREPARED || form.getState() == WorkflowState.ACCEPTED)) {
            logger.error("Нельзя отменить принятие налоговой формы, так как уже принята вышестоящая налоговая форма")
        }
    }
}

/**
 * Принять.
 */
void acceptance() {
    if (!logicalCheck(true)) {
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
    if (reportPeriod != null && reportPeriod.isBalancePeriod()) {
        logger.error('Налоговая форма не может быть в периоде ввода остатков.')
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
 * Проверка пустое ли значение.
 */
def isEmpty(def value) {
    return value == null || value == ''
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
 * Получить строку из сводной налоговой формы "Расходы, учитываемые в простых РНУ", у которой графа 1 ("КНУ") = knu
 * @params knu КНУ
 */
def getTotalRowFromRNU(def knu) {
    def formDataRNU = FormDataService.find(304, FormDataKind.SUMMARY, formDataDepartment.id, formData.reportPeriodId)
    if (formDataRNU != null) {
        for (def row : formDataRNU.dataRows) {
            if (row.consumptionTypeId == knu) {
                return row
            }
        }
    }
    return null
}