package form_template.income.rnu14

import com.aplana.sbrf.taxaccounting.model.exception.ServiceException

/**
 * Форма "УНП" ("РНУ-14 - Регистр налогового учёта нормируемых расходов")
 * formTemplateId=321
 *
 * @author lhaziev
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkCreation()
        break
    case FormDataEvent.CHECK:
        logicalCheck(true)
        break
    case FormDataEvent.CALCULATE:
        prevPeriodCheck()
        calc()
        logicalCheck(false)
        break
// проверка при "подготовить"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:
        checkOnPrepareOrAcceptance('Подготовка')
        break
// проверка при "принять"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED:
        checkOnPrepareOrAcceptance('Принятие')
        break
// проверка при "вернуть из принята в подготовлена"
    case FormDataEvent.MOVE_ACCEPTED_TO_PREPARED:
        checkOnCancelAcceptance()
        break
// обобщить
    case FormDataEvent.COMPOSE:
        calc()
        logicalCheck(false)
        break
}

// графа 1  - knu
// графа 2  - mode
// графа 3  - sum
// графа 4  - normBase
// графа 5  - normCoef
// графа 6  - limitSum
// графа 7  - inApprovedNprms
// графа 8  - overApprovedNprms

void prevPeriodCheck() {
    if (getFormDataOutcomeSimple() == null) {
        throw new ServiceException("Не найден экземпляр Сводной формы «Расходы, учитываемые в простых РНУ» за текущий отчетный период!")
    }
    if (getFormDataSimple() == null) {
        throw new ServiceException("Не найден экземпляр Сводной формы «Доходы, учитываемые в простых РНУ»!")
    }
    if (getFormDataComplex() == null) {
        throw new ServiceException("Не найден экземпляр Сводной формы «Сводная форма начисленных доходов»!")
    }
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {
    def data = getData(formData)
    /*
     * Проверка обязательных полей.
     */

    // список проверяемых столбцов (графа 3..4)
    def requiredColumns = ['normBase']

    for (def row : getRows(data)) {
        if (!checkRequiredColumns(row, requiredColumns, true)) {
            return
        }
    }

    def col = getCol()
    def koeffNormBase = [4 / 100, 1 / 100, 6 / 100, 12 / 100, 15000]

    def formDataComplex = getFormDataComplex()
    def formDataSimple = getFormDataSimple()
    def knuComplex = getKnuComplex()
    def knuSimpleRNU4 = getKnuSimpleRNU4()
    def knuSimpleRNU6 = getKnuSimpleRNU6()
    for (def row : getRows(data)) {
        def rowA = getTotalRowFromRNU(col[getIndex(row)])
        if (rowA != null) {
            // 3 - графа 8 строки А + (графа 5 строки А – графа 6 строки А)
            row.sum = (rowA.rnu5Field5Accepted?:0) + (rowA.rnu7Field10Sum?:0)  - (rowA.rnu7Field12Accepted?:0)
            // 4 - сумма по всем (графа 8 строки B + (графа 5 строки B – графа 6 строки B)),
            // КНУ которых совпадает со значениями в colBase (или colTax если налоговый период)
            if (getRows(data).indexOf(row)!=4 && getRows(data).indexOf(row)!=1) {//не 5-я и 2-я строка
                def normBase = 0
                /** Отчётный период. */
                def reportPeriod = reportPeriodService.get(formData.reportPeriodId)
                /** Признак налоговый ли это период. */
                def isTaxPeriod = (reportPeriod != null && reportPeriod.order == 4)
                for(def knu:(isTaxPeriod?knuTax:knuBase)){
                    def rowB = getTotalRowFromRNU(knu)
                    if (rowB!=null){
                        normBase += (rowB.rnu5Field5Accepted?:0) + (rowB.rnu7Field10Sum?:0)  - (rowB.rnu7Field12Accepted?:0)
                    }
                }
                row.normBase = normBase
            } else if (getRows(data).indexOf(row)==1){//2-я строка(сложнее)
                def normBase = 0
                //Сумма значений по графе 9 (столбец «Доход по данным налогового учёта. Сумма») в сложных доходах где КНУ = ...
                //объединил
                for(def rowComplex:getRows(getData(formDataComplex))){
                    if (rowComplex.incomeTypeId in knuComplex) {
                        normBase += rowComplex.incomeTaxSumS
                    }
                }
                //простые доходы
                for(def rowSimple:getRows(getData(formDataSimple))){
                    //+ Сумма значений по графе 8 (столбец «РНУ-4 (графа 5) сумма») в простых доходах
                    if (rowSimple.incomeTypeId in knuSimpleRNU4) {
                        normBase += rowSimple.rnu4Field5Accepted
                    }
                    //+ Сумма значений по графе 5 (столбец «РНУ-6 (графа 10) сумма»)
                    //- Сумма значений по графе 6 (столбец «РНУ-6 (графа 12). Сумма»)
                    // КНУ одни, поэтому объединил
                    if (rowSimple.incomeTypeId in knuSimpleRNU6) {
                        normBase += (rowSimple.rnu6Field10Sum - rowSimple.rnu6Field12Accepted)
                    }
                }
                row.normBase = normBase
            }
            // 6
            if (row.normBase!=null) {
                row.limitSum = koeffNormBase[getIndex(row)] * row.normBase
            }
            def diff6_3 = row.limitSum - row.sum
            if (diff6_3 != null) {
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
    data.save(getRows(data));
}

/**
 * Логические проверки.
 *
 * @param useLog нужно ли записывать в лог сообщения о незаполненности обязательных полей
 */
def logicalCheck(def useLog) {
    def data = getData(formData)
    // список проверяемых столбцов (графа 4)
    def requiredColumns = ['normBase']
    for (def row : getRows(data)) {
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
            def form = formDataService.find(department.formTypeId, department.kind, department.departmentId, formData.reportPeriodId)
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
        def form = formDataService.find(department.formTypeId, department.kind, department.departmentId, formData.reportPeriodId)

        if (form != null && (form.getState() == WorkflowState.PREPARED || form.getState() == WorkflowState.ACCEPTED)) {
            logger.error("Нельзя отменить принятие налоговой формы, так как уже принята вышестоящая налоговая форма")
        }
    }
}

/**
 * Проверка при создании формы.
 */
void checkCreation() {
    def findForm = formDataService.find(formData.formType.id,
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
    getRows(getData(formData)).indexOf(row)
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
        if (row.getCell(it).editable && (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue()))) {
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
            logger.error("В строке под номером $index не заполнены колонки : $errorMsg.")
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
    def formDataRNU = getFormDataOutcomeSimple()
    if (formDataRNU != null) {
        def dataRNU = getData(formDataRNU)
        for (def row : getRows(dataRNU)) {
            if (row.consumptionTypeId == knu) {
                return row
            }
        }
    }
    return null
}

// Получить данные формы "расходы простые" (id = 304)
def getFormDataOutcomeSimple() {
    return formDataService.find(304, FormDataKind.SUMMARY, formDataDepartment.id, formData.reportPeriodId)
}

// Получить данные формы "доходы сложные" (id = 302)
def getFormDataComplex() {
    return formDataService.find(302, FormDataKind.SUMMARY, formDataDepartment.id, formData.reportPeriodId)
}

// Получить данные формы "доходы простые" (id = 301)
def getFormDataSimple() {
    return formDataService.find(301, FormDataKind.SUMMARY, formDataDepartment.id, formData.reportPeriodId)
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
 * Получить строки формы.
 *
 * @param formData форма
 */
def getRows(def data) {
    def cached = data.getAllCached()
    return cached
}

def getCol() {
    return ['21270', '21410', '20698', '20700', '20690']
}

def getKnuBase() {
    return ['20480', '20485', '20490', '20500', '20505', '20530']
}

def getKnuTax() {
    return ['20480', '20485', '20490', '20500', '20505', '20530', '20510', '20520']
}

def getKnuComplex() {
    return ['10633', '10634', '10650', '10670', '10855', '10880', '10900', '10850',
            '11180', '11190', '11200', '11210', '11220', '11230', '11240', '11250',
            '11260', '10840', '10860', '10870', '10890']
}

def getKnuSimpleRNU4() {
    return ['10001', '10006', '10041', '10300', '10310', '10320', '10330', '10340',
            '10350', '10360', '10370', '10380', '10390', '10450', '10460', '10470',
            '10480', '10490', '10571', '10580', '10590', '10600', '10610', '10630',
            '10631', '10632', '10640', '10680', '10690', '10740', '10744', '10748',
            '10752', '10756', '10760', '10770', '10790', '10800', '11140', '11150',
            '11160', '11170', '11320', '11325', '11330', '11335', '11340', '11350',
            '11360', '11370', '11375']
}

def getKnuSimpleRNU6() {
    return ['10001', '10006', '10300', '10310', '10320', '10330', '10340', '10350',
            '10360', '10470', '10480', '10490', '10571', '10590', '10610', '10640',
            '10680', '10690', '11340', '11350', '11370', '11375']
}