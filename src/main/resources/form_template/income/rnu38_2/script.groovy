package form_template.income.rnu38_2

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState

/**
 * Форма "РНУ-38.2 Регистр налогового учёта начисленного процентного дохода по ОФЗ, по которым открыта короткая позиция. Отчёт 2".
 *
 * @version 59
 *
 * TODO:
 *      - импорт и миграция
 *
 * @author rtimerbaev
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        checkCreation()
        break
    case FormDataEvent.CHECK :
        logicalCheck()
        break
    case FormDataEvent.CALCULATE :
        calc() && logicalCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
        logicalCheck()
        break
    case FormDataEvent.COMPOSE :
        consolidation()
        calc() && logicalCheck()
        break
}

// графа 1  - amount
// графа 2  - incomePrev
// графа 3  - incomeShortPosition
// графа 4  - totalPercIncome

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
def calc() {
    // если нф консолидированная, то не надо проверять данные из рну 38.1
    if (formData.kind == FormDataKind.CONSOLIDATED) {
        return true
    }
    def data = getData(formData)
    def totalRow = getTotalRowFromRNU38_1()
    if (totalRow == null) {
        for (def row : getRows(data)) {
            // графа 1
            row.amount = null

            // графа 2
            row.incomePrev = null

            // графа 3
            row.incomeShortPosition = null

            // графа 4
            row.totalPercIncome = null
        }
        return true
    }

    /*
     * Расчеты.
     */

    for (def row : getRows(data)) {
        // графа 1
        row.amount = totalRow.amount

        // графа 2
        row.incomePrev = totalRow.incomePrev

        // графа 3
        row.incomeShortPosition = totalRow.incomeShortPosition

        // графа 4
        row.totalPercIncome = row.incomePrev + row.incomeShortPosition
    }

    save(data)
    return true
}

/**
 * Логические проверки.
 */
def logicalCheck() {
    def data = getData(formData)
    if (getRows(data).isEmpty()) {
        logger.error('Отсутствуют данные')
        return false
    }
    def row = getRows(data).get(0)

    // 1. Обязательность заполнения поля графы 1..4
    def requiredColumns = ['amount', 'incomePrev', 'incomeShortPosition', 'totalPercIncome']
    if (!checkRequiredColumns(row, requiredColumns)) {
        return false
    }

    // если нф консолидированная, то не надо проверять данные из рну 38.1
    if (formData.kind == FormDataKind.CONSOLIDATED) {
        return true
    }

    def totalRow = getTotalRowFromRNU38_1()
    if (totalRow == null) {
        logger.error('Отсутствует РНУ-38.1.')
        return false
    }

    index = getIndex(row) + 1
    errorMsg = "В строке $index "

    // 2. Арифметическая проверка графы 1
    if (row.amount != totalRow.amount) {
        logger.warn(errorMsg + 'неверно рассчитана графа «Количество (шт.)»!')
    }

    // 3. Арифметическая проверка графы 2
    if (row.incomePrev != totalRow.incomePrev) {
        logger.warn(errorMsg + 'неверно рассчитана графа «Доход с даты погашения предыдущего купона (руб.коп.)»!')
    }

    // 4. Арифметическая проверка графы 3
    if (row.incomeShortPosition != totalRow.incomeShortPosition) {
        logger.warn(errorMsg + 'неверно рассчитана графа «Доход с даты открытия короткой позиции, (руб.коп.)»!')
    }

    // 5. Арифметическая проверка графы 4
    if (row.totalPercIncome != row.totalPercIncome) {
        logger.warn(errorMsg + 'неверно рассчитана графа «Всего процентный доход (руб.коп.)»!')
    }
    return true
}

/**
 * Консолидация.
 */
void consolidation() {
    def data = getData(formData)

    // удалить все строки и собрать из источников их строки
    data.clear()
    def newRows = []

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceData = getData(source)
                getRows(sourceData).each { row ->
                    newRows.add(row)
                }
            }
        }
    }
    if (!newRows.isEmpty()) {
        data.insert(newRows, 1)
    }
    logger.info('Формирование консолидированной формы прошло успешно.')
}

/**
 * Проверка при создании формы.
 */
void checkCreation() {
    def isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)

    //проверка периода ввода остатков
    if (isBalancePeriod) {
        logger.error('Налоговая форма не может создаваться в периоде ввода остатков.')
        return
    }

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
 * Получить итоговую строку из нф (РНУ-38.1) Регистр налогового учёта начисленного процентного дохода по ОФЗ, по которым открыта короткая позиция. Отчёт 1.
 */
def getTotalRowFromRNU38_1() {
    def formDataRNU_38_1 = formDataService.find(334, formData.kind, formDataDepartment.id, formData.reportPeriodId)
    def dataRNU_38_1 = getData(formDataRNU_38_1)
    if (dataRNU_38_1 != null) {
        for (def row : getRows(dataRNU_38_1)) {
            if (row.getAlias() == 'total') {
                return row
            }
        }
    }
    return null
}

/**
 * Получить список строк формы.
 *
 * @param data данные нф (helper)
 */
def getRows(def data) {
    return data.getAllCached();
}

/**
 * Сохранить измененные значения нф.
 *
 * @param data данные нф (helper)
 */
void save(def data) {
    data.save(getRows(data))
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
 * Получить номер строки в таблице (0..n).
 *
 * @param row строка
 */
def getIndex(def row) {
    return row.getIndex() - 1
}

/**
 * Проверить заполненость обязательных полей.
 *
 * @param row строка
 * @param columns список обязательных графов
 * @return true - все хорошо, false - есть незаполненные поля
 */
def checkRequiredColumns(def row, def columns) {
    def colNames = []
    columns.each {
        if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
            def name = getColumnName(row, it)
            colNames.add('"' + name + '"')
        }
    }
    if (!colNames.isEmpty()) {
        def index = getIndex(row) + 1
        def errorMsg = colNames.join(', ')
        logger.error("В строке $index не заполнены колонки : $errorMsg.")
        return false
    }
    return true
}