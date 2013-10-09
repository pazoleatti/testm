package form_template.income.rnu38_1

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException

/**
 * Форма "РНУ-38.1" "Регистр налогового учёта начисленного процентного дохода по ОФЗ, по которым открыта короткая позиция. Отчёт 1".
 *
 * TODO:
 *		- уточнить как получать дату последнего и отчетного дня для месяца в методе getLastDayReportPeriod() getReportDate()
 *
 * @author ivildanov
 *
 * Графы
 * 1  -  series
 * 2  -  amount
 * 3  -  shortPositionDate
 * 4  -  maturityDate
 * 5  -  incomeCurrentCoupon
 * 6  -  currentPeriod
 * 7  -  incomePrev
 * 8  -  incomeShortPosition
 * 9  -  totalPercIncome
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        //Проверка наличия и статуса формы, консолидирующей данные текущей налоговой формы, при создании формы.
        checkCreation()
        break
    case FormDataEvent.ADD_ROW:
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW:
        deleteRow()
        break
    case FormDataEvent.CHECK: // Инициирование Пользователем проверки данных формы в статусе «Создана», «Подготовлена», «Утверждена», «Принята»
        logicalCheck()
        break
    case FormDataEvent.CALCULATE:
        fillForm() && logicalCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
        logicalCheck()
        break
    case FormDataEvent.COMPOSE:
        consolidation()
        fillForm() && logicalCheck()
        break
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

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def data = getData(formData)

    def index = 0
    if (currentDataRow != null) {
        index = currentDataRow.getIndex()
        def row = currentDataRow
        while (row.getAlias() != null && index > 0) {
            row = getRows(data).get(--index)
        }
        if (index != currentDataRow.getIndex() && getRows(data).get(index).getAlias() == null) {
            index++
        }
    } else if (getRows(data).size() > 0) {
        for (int i = getRows(data).size() - 1; i >= 0; i--) {
            def row = getRows(data).get(i)
            if (!isTotalRow(row)) {
                index = getRows(data).indexOf(row) + 1
                break
            }
        }
    }
    data.insert(getNewRow(), index + 1)
}

/**
 * Удалить строку.
 */
def deleteRow() {
    if (!isTotalRow(currentDataRow)) {
        def data = getData(formData)
        data.delete(currentDataRow)
    } else {
        logger.error('Невозможно удалить фиксированную строку!')
    }
}

/**
 * Логические проверки
 */
def logicalCheck() {
    def data = getData(formData)

    if (getRows(data).isEmpty() || (getRows(data).size() == 1 && getRows(data).get(0).getAlias() == 'total')) {
        logger.error('Отсутствуют данные')
        return false
    }

    /*
     * Проверка обязательных полей.
     */
    // список проверяемых столбцов (графа 1..6, 9)
    def requiredColumns = ['series', 'amount', 'shortPositionDate', 'maturityDate',
            'incomeCurrentCoupon', 'currentPeriod', 'totalPercIncome']
    for (def row : getRows(data)) {
        if (!isTotalRow(row) && !checkRequiredColumns(row, requiredColumns)) {
            return false
        }
    }

    def reportDay = getReportDate()
    def lastDay = getLastDayReportPeriod()

    for (def row : getRows(data)) {
        if (!isTotalRow(row)) {
            index = getIndex(row) + 1
            errorMsg = "В строке $index "

            //  1.	Проверка даты открытия короткой позиции
            if (row.shortPositionDate > reportDay) {
                logger.error(errorMsg + 'неверно указана дата приобретения (открытия короткой позиции)!')
                return false
            }

            // 2. Проверка даты погашения
            if (row.maturityDate > reportDay) {
                logger.error(errorMsg + 'неверно указана дата погашения предыдущего купона!')
                return false
            }

            // 3. Арифметические проверки графы 7..9
            List checks = ['incomePrev', 'incomeShortPosition', 'totalPercIncome']
            def value = formData.createDataRow()
            value.incomePrev = calc7(row, lastDay)
            value.incomeShortPosition = calc8(row, lastDay)
            value.totalPercIncome = calc9(row)

            for (String alias in checks) {
                if (row.getCell(alias).value != value.get(alias)) {
                    def name = getColumnName(row, alias)
                    logger.error(errorMsg + "неверно рассчитана графа \"$name\"! (${row.getCell(alias).value} != ${value.get(alias)})")
                    return false
                }
            }
        }
    }

    // 4. Проверка итоговых значений по всей форме
    def sumColumns = ['amount', 'incomePrev', 'totalPercIncome', 'incomeShortPosition']
    def sums = [:]
    sumColumns.each { alias ->
        sums[alias] = 0
    }
    getRows(data).each { row ->
        if (!isTotalRow(row)) {
            sumColumns.each { alias ->
                sums[alias] += (row.getCell(alias).getValue() ?: 0)
            }
        }
    }
    def totalRow = getRowByAlias(data, 'total')
    for (def alias : sumColumns) {
        if (totalRow.getCell(alias).getValue() != sums[alias]) {
            logger.error('Итоговые значения рассчитаны неверно!')
            return false
        }
    }

    return true
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
def fillForm() {
    def data = getData(formData)

    /*
     * Проверка обязательных полей.
     */
    // список проверяемых столбцов (графа 1..6)
    def requiredColumns = ['series', 'amount', 'shortPositionDate', 'maturityDate',
            'incomeCurrentCoupon', 'currentPeriod']
    for (def row : getRows(data)) {
        if (!isTotalRow(row) && !checkRequiredColumns(row, requiredColumns)) {
            return false
        }
    }

    // сортировка
    sort(data)

    /*
     * Заполнение графы 7..9
     * Вычисление ИТОГО для граф 2,7-9
     */

    def total2 = 0, total7 = 0, total8 = 0, total9 = 0
    def lastDay = getLastDayReportPeriod()

    getRows(data).each { row ->
        if (!isTotalRow(row)) {
            // графа 7
            row.incomePrev = calc7(row, lastDay)

            // графа 8
            row.incomeShortPosition = calc8(row, lastDay)

            // графа 9
            row.totalPercIncome = calc9(row)

            // подсчет ИТОГО
            total2 = total2 + (row.amount ?: 0)
            total7 = total7 + (row.incomePrev ?: 0)
            total8 = total8 + (row.incomeShortPosition ?: 0)
            total9 = total9 + (row.totalPercIncome ?: 0)
        }
    }

    def totalRow = getRowByAlias(data, 'total')
    totalRow.amount = total2
    totalRow.incomePrev = total7
    totalRow.incomeShortPosition = total8
    totalRow.totalPercIncome = total9

    save(data)
    return true
}

/**
 * Консолидация.
 */
void consolidation() {
    def data = getData(formData)

    // удалить все строки (кроме итоговой) и собрать из источников их строки
    def totalRow = getRowByAlias(data, 'total')
    data.clear()
    data.insert(totalRow, 1)
    def newRows = []

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceData = getData(source)
                getRows(sourceData).each { row ->
                    if (row.getAlias() == null || row.getAlias() == '') {
                        newRows.add(row)
                    }
                }
            }
        }
    }
    if (!newRows.isEmpty()) {
        data.insert(newRows, 1)
    }
    logger.info('Формирование консолидированной формы прошло успешно.')
}

/*
 * Вспомогательные методы.
 */

/**
 * Проверка является ли строка фиксированной.
 */
def isTotalRow(def row) {
    return row != null && row.getAlias() == 'total'
}

/**
 * 	Посчитать значение графы 7.
 *
 * @param row строка
 * @param lastDay последний день отчетного месяца
 */
def calc7(def row, def lastDay) {
    if (row.maturityDate > row.shortPositionDate) {
        checkDivision(row.currentPeriod == 0, getIndex(row) + 1)
        return roundValue((row.incomeCurrentCoupon * (lastDay - row.maturityDate) / row.currentPeriod), 2) * row.amount
    } else {
        return null
    }
}

/**
 * 	Посчитать значение графы 8.
 *
 * @param row строка
 * @param lastDay последний день отчетного месяца
 */
def calc8(def row, def lastDay) {
    if (row.maturityDate <= row.shortPositionDate) {
        checkDivision(row.currentPeriod == 0, getIndex(row) + 1)
        return roundValue((row.incomeCurrentCoupon * (lastDay - row.shortPositionDate) / row.currentPeriod), 2) * row.amount
    } else {
        return null
    }
}

/**
 * 	Посчитать значение графы 9.
 *
 * @param row строка
 */
def calc9(def row) {
    return (row.incomePrev ?: 0) + (row.incomeShortPosition ?: 0)
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
 * Вставить новую строку в конец нф.
 *
 * @param data данные нф
 * @param row строка
 */
void insert(def data, def row) {
    data.insert(row, getRows(data).size() + 2)
}

/**
 * Удалить строку из нф
 *
 * @param data данные нф (helper)
 * @param row строка для удаления
 */
void deleteRow(def data, def row) {
    data.delete(row)
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
    row.getIndex() - 1
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
 * Получить новую стролу с заданными стилями.
 */
def getNewRow() {
    def newRow = formData.createDataRow()

    // Графы 1-6 Заполняется вручную
    ['series', 'amount', 'shortPositionDate', 'maturityDate', 'incomeCurrentCoupon', 'currentPeriod'].each { column ->
        newRow.getCell(column).setEditable(true)
        newRow.getCell(column).styleAlias = 'Редактируемая'
    }
    return newRow
}

/**
 * Округляет число до требуемой точности.
 *
 * @param value округляемое число
 * @param precision точность округления, знаки после запятой
 * @return округленное число
 */
def roundValue(def value, int precision) {
    if (value != null) {
        return ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
}

/**
 * Проверка деления на ноль.
 *
 * @param division делитель
 * @param index номер строки
 */
void checkDivision(def division, def index) {
    if (division == 0) {
        throw new ServiceLoggerException("Деление на ноль в строке $index.", logger.getEntries())
    }
}

// TODO (Ramil Timerbaev) уточнить как получать дату последнего дня для месяца
/**
 * Получить последний день отчетного периода (месяца)
 */
def getLastDayReportPeriod() {
    def last = reportPeriodService.getEndDate(formData.reportPeriodId)
    return (last ? last.time : null)
}

/**
 * Получить отчетную дату.
 */
def getReportDate() {
    def reportDay = reportPeriodService.getReportDate(formData.reportPeriodId)
    return (reportDay ? reportDay.time : null)
}

/**
 * Получить строку по алиасу.
 *
 * @param data данные нф
 * @param alias алиас
 */
def getRowByAlias(def data, def alias) {
    if (alias == null || alias == '' || data == null) {
        return null
    }
    for (def row : getRows(data)) {
        if (alias.equals(row.getAlias())) {
            return row
        }
    }
    return null
}

/**
 * Отсорировать данные (по графе 1).
 *
 * @param data данные нф (хелпер)
 */
void sort(def data) {
    def rows = getRows(data)
    rows.subList(0, rows.size() - 1).sort {
        it.series
    }
}