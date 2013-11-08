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
        logicalCheck()
        break
}

// графа 1  - amount
// графа 2  - incomePrev
// графа 3  - incomeShortPosition
// графа 4  - totalPercIncome

/**
 * Логические проверки.
 */
def logicalCheck() {
    def data = getData(formData)
    if (getRows(data).isEmpty()) {
        return true
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
        def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (source != null && source.state == WorkflowState.ACCEPTED) {
            def sourceData = getData(source)
            if (it.formTypeId == formData.getFormType().getId()) {
            //if (formData.kind == FormDataKind.CONSOLIDATED) {
                // Консолидация данных из первичной рну-38.2 в консолидированную рну-38.2.
                getRows(sourceData).each { row ->
                    newRows.add(row)
                }
            } else {
                // Консолидация данных из первичной рну-38.1 в первичную рну-38.2.
                def totalRow = getRowByAlias(sourceData, 'total')
                def newRow = formData.createDataRow()
                // графа 1..4
                newRow.amount = totalRow.amount
                newRow.incomePrev = totalRow.incomePrev
                newRow.incomeShortPosition = totalRow.incomeShortPosition
                newRow.totalPercIncome = newRow.incomePrev + newRow.incomeShortPosition
                newRows.add(newRow)
            }
        }
    }
    if (!newRows.isEmpty()) {
        data.insert(newRows, 1)
        updateIndexes(data)
    }
    save(data)
    if (formData.kind == FormDataKind.CONSOLIDATED) {
        logger.info('Формирование консолидированной формы прошло успешно.')
    } else {
        logger.info('Формирование первичной формы РНУ-38.2 прошло успешно.')
    }
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
    return getRowByAlias(dataRNU_38_1, 'total')
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

/**
 * Получить строку по алиасу.
 *
 * @param data хелпер
 * @param alias алиас
 */
def getRowByAlias(def data, def alias) {
    if (alias == null || alias == '') {
        return null
    }
    if (data != null) {
        for (def row : getRows(data)) {
            if (alias.equals(row.getAlias())) {
                return row
            }
        }
    }
    return null
}

/**
 * Поправить индексы, потому что они после вставки не пересчитываются.
 */
void updateIndexes(def data) {
    getRows(data).eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
}