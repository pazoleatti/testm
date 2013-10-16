package form_template.income.rnu46

/**
 * Скрипт для РНУ-46 (rnu46.groovy).
 * Форма "(РНУ-46) Регистр налогового учёта «карточка по учёту основных средств и капитальных вложений в неотделимые улучшения арендованного и полученного по договору безвозмездного пользования имущества»".
 * formTemplateId=342
 *
 * @author rtimerbaev
 * @author Stanislav Yasinskiy
 */

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import java.text.SimpleDateFormat

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkCreation()
        break
    case FormDataEvent.CHECK:
        logicalCheck()
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicalCheck()
        break
    case FormDataEvent.ADD_ROW:
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW:
        deleteRow()
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
        logicalCheck()
        break
    case FormDataEvent.COMPOSE: // обобщить
        consolidation()
        calc()
        logicalCheck()
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

//Добавить новую строку
void addNewRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def row = formData.createDataRow()
    def dataRows = dataRowHelper.getAllCached()
    def size = dataRows.size()
    def index = currentDataRow != null ? (currentDataRow.getIndex() + 1) : (size == 0 ? 1 : (size + 1))
    // графа 2..7, 9, 17..19
    ['invNumber', 'name', 'cost', 'amortGroup', 'usefulLife', 'monthsUsed', 'usefulLifeWithUsed',
            'specCoef', 'exploitationStart', 'usefullLifeEnd', 'rentEnd'].each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    dataRowHelper.insert(row, index)
}

// Удалить строку
void deleteRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.delete(currentDataRow)
}

// Ресчет графы 6
def calc6(def map) {
    if (map != null) {
        return map.TERM.numberValue
    }
    return null
}

// Ресчет графы 8
def calc8(def row) {
    if (row.monthsUsed == null || row.usefulLife == null || row.specCoef == null)
        return null
    if (row.monthsUsed < row.usefulLife) {
        if (row.specCoef > 0) {
            return round((row.usefulLife - row.monthsUsed) / row.specCoef, 0)
        } else {
            return round(row.usefulLife - row.monthsUsed, 0)
        }
    }
    return row.usefulLifeWithUsed
}

// Ресчет графы 10
def calc10(def row, def map, def check17) {
    def tmp = null
    if (map != null) {
        if (map.GROUP.numberValue in [1, 2, 8..10] && row.cost != null) {
            tmp = row.cost * 10
        } else if (row.amortGroup in (3..7) && row.cost != null) {
            tmp = row.cost * 30
        } else if (row.exploitationStart != null && row.exploitationStart < check17) {
            tmp = 0
        }
    }
    return round(tmp)
}

// Ресчет граф 11, 15, 16
def calc11and15and16(def reportMonth, def row, def prevRow) {
    def values = [null, null, null]
    if (reportMonth == Calendar.JANUARY) {
        values[0] = row.cost10perMonth
        values[1] = row.amortMonth
        values[2] = row.amortMonth
    } else if (prevRow != null) {
        if (prevRow.cost10perTaxPeriod != null)
            values[0] = row.cost10perMonth + prevRow.cost10perTaxPeriod
        if (prevRow.amortTaxPeriod != null)
            values[1] = row.amortMonth + prevRow.amortTaxPeriod
        if (prevRow.amortExploitation != null)
            values[2] = row.amortMonth + prevRow.amortExploitation
    }
    return values
}

// Ресчет графы 12
def calc12(def row, def prevRow, def rpStartDate, def rpEndDate) {
    def val = null
    if (row.exploitationStart == null) {
        val = null
    } else if (rpStartDate < row.exploitationStart && row.exploitationStart < rpEndDate) {
        row.cost10perExploitation = row.cost10perMonth
    } else if (prevRow != null && prevRow.cost10perExploitation != null) {
        row.cost10perExploitation = row.cost10perMonth + prevRow.cost10perExploitation
    }
    return val
}

// Ресчет графы 13
def calc12(def row) {
    if (row == null || row.usefulLifeWithUsed == null || row.usefulLifeWithUsed == 0)
        return null
    return round((1 / row.usefulLifeWithUsed) * 100, 0)
}

// Ресчет графы 14
def calc14(def row, def prevRow, def lastDay2001) {
    def val
    if (row.usefullLifeEnd == null || row.cost10perExploitation == null || row.cost == null
            || prevRow.cost == null || prevRow.amortExploitation == null || (row.usefullLifeEnd - lastDayPrevMonth) == 0)
        val = null
    else if (row.usefullLifeEnd > lastDay2001) {
        val = (prevRow.cost - row.cost10perExploitation - prevRow.amortExploitation) / (row.usefullLifeEnd - lastDayPrevMonth)
    } else {
        val = row.cost / 84
    }
    return round(val)
}

// Ресчет графы 18
def calc18(def row) {
    if (row.exploitationStart == null || row.usefulLifeWithUsed == null)
        return null
    def Calendar tmpCal = Calendar.getInstance()
    tmpCal.setTime(row.exploitationStart)
    tmpCal.add(Calendar.MONTH, Integer.valueOf(row.usefulLifeWithUsed.toString()))
    tmpCal.set(Calendar.DAY_OF_MONTH, tmpCal.getMaximum(Calendar.DAY_OF_MONTH))
    return tmpCal.getTime()
}

// Расчеты. Алгоритмы заполнения полей формы.
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    if (dataRows.isEmpty()) {
        return
    }

    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    def lastDay2001 = format.parse('31.12.2001')
    def check17 = format.parse('01.01.2006')
    /** Дата начала отчетного периода. */
    def tmpDate = reportPeriodService.getStartDate(formData.reportPeriodId)
    def rpStartDate = (tmpDate ? tmpDate.getTime() : null)
    /** Дата окончания отчетного периода. */
    tmpDate = reportPeriodService.getEndDate(formData.reportPeriodId)
    def rpEndDate = (tmpDate ? tmpDate.getTime() : null)
    // Отчетная дата
    def reportDate = getReportDate()
    def Calendar reportDateC = Calendar.getInstance()
    reportDateC.setTime(reportDate)
    def reportMounth = reportDateC.get(Calendar.MONTH)
    // Отчет за предыдущий месяц
    def formDataOld = getFormDataOld()
    def dataOld = formDataOld != null ? formDataService.getDataRowHelper(formDataOld) : null
    // индекс
    def index = 0

    for (def row in dataRows) {
        def map = null
        if (row.amortGroup != null) {
            map = refBookService.getRecordData(71, row.amortGroup)
        }

        prevRow = getPrevRow(dataOld, row)

        // графа 1
        row.rowNumber = ++index

        // графа 6
        row.usefulLife = calc6(map)

        // графа 8
        row.usefulLifeWithUsed = calc8(row)

        // графа 10
        row.cost10perMonth = calc10(row, map, check17)

        calc11and15and16 = calc11and15and16(reportMounth, row, prevRow)
        // графа 11
        row.cost10perExploitation = calc11and15and16[0]
        // графа 15
        row.amortTaxPeriod = calc11and15and16[1]
        // графа 16
        row.amortExploitation = calc11and15and16[2]

        // графа 12
        row.cost10perExploitation = calc12(row, prevRow, rpStartDate, rpEndDate)

        // графа 13
        row.amortNorm = calc12(row)

        // графа 14
        row.amortMonth = calc14(row, prevRow, lastDay2001)

        // графа 18
        row.usefullLifeEnd = calc18(row)
    }

    dataRowHelper.update(dataRows);
}

/**
 * Логические проверки.
 */
def logicalCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    if (!dataRows.isEmpty()) {
        // список проверяемых столбцов (графа 1..18)
        def requiredColumns = ['rowNumber', 'invNumber', 'name', 'cost', 'amortGroup',
                'usefulLife', 'monthsUsed', 'usefulLifeWithUsed', 'specCoef',
                'cost10perMonth', 'cost10perTaxPeriod', 'cost10perExploitation',
                'amortNorm', 'amortMonth', 'amortTaxPeriod', 'amortExploitation',
                'exploitationStart', 'usefullLifeEnd']
        // Инвентарные номера
        def List<String> invList = new ArrayList<String>()
        for (def row in dataRows) {
            // 1. Проверка на заполнение поля «<Наименование поля>»
            if (!checkRequiredColumns(row, requiredColumns)) {
                return false
            }
            // 3. Проверка на уникальность поля «инвентарный номер»
            if (invList.contains(row.invNumber)) {
                logger.error("Инвентарный номер не уникальный!")
                return false
            } else {
                invList.add(row.invNumber)
            }
        }

        // Отчет за предыдущий месяц
        def formDataOld = getFormDataOld()
        // 4. Проверки существования необходимых экземпляров форм
        if (formDataOld == null) {
            // TODO что делать если это самая первая форма?
            logger.warn('Отсутствуют данные за прошлые отчетные периоды!')
            //return false
        }
        def dataOld = formDataOld != null ? formDataService.getDataRowHelper(formDataOld) : null

        SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
        def check17 = format.parse('01.01.2006')
        def lastDay2001 = format.parse('31.12.2001')

        // последнее число предыдущего месяца
        def tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
        def lastDayPrevMonth = (tmp ? tmp.getTime() : null)

        /** Дата начала отчетного периода. */
        def tmpDate = reportPeriodService.getStartDate(formData.reportPeriodId)
        def rpStartDate = (tmpDate ? tmpDate.getTime() : null)

        /** Дата окончания отчетного периода. */
        tmpDate = reportPeriodService.getEndDate(formData.reportPeriodId)
        def rpEndDate = (tmpDate ? tmpDate.getTime() : null)

        // Отчетная дата
        def reportDate = getReportDate()
        def Calendar reportDateC = Calendar.getInstance()
        reportDateC.setTime(reportDate)
        def reportMounth = reportDateC.get(Calendar.MONTH)

        for (def row : dataRows) {
            def map = null
            if (row.amortGroup != null) {
                map = refBookService.getRecordData(71, row.amortGroup)
            }

            prevRow = getPrevRow(dataOld, row)

            // 5. Проверка на нулевые значения (графа 9, 10, 11, 13, 14, 15)
            if (row.specCoef == 0 &&
                    row.cost10perMonth == 0 &&
                    row.cost10perTaxPeriod == 0 &&
                    row.amortNorm &&
                    row.amortMonth == 0 &&
                    row.amortTaxPeriod) {
                logger.error('Все суммы по операции нулевые!')
                return false
            }

            // 6. Проверка суммы расходов в виде капитальных вложений с начала года
            // TODO что делать с проверкой если нет prevRow
            prevRowcost10perTaxPeriod = prevRow!= null ? prevRow.cost10perTaxPeriod : -1
            if (row.cost10perTaxPeriod >= row.cost10perMonth &&
                    row.cost10perTaxPeriod == row.cost10perTaxPeriod + prevRowcost10perTaxPeriod &&
                    row.cost10perTaxPeriod == prevRowcost10perTaxPeriod) {
                logger.error('Неверная сумма расходов в виде капитальных вложений с начала года!')
                return false
            }

            // 7. Проверка суммы начисленной амортизации с начала года
            // TODO что делать с проверкой если нет prevRow
            prevRowAmortTaxPeriod = prevRow!= null ? prevRow.amortTaxPeriod : -1
            if (row.amortTaxPeriod < row.amortMonth &&
                    row.amortTaxPeriod == row.cost10perTaxPeriod + prevRowAmortTaxPeriod &&
                    row.amortTaxPeriod == prevRowAmortTaxPeriod) {
                logger.error('Неверная сумма начисленной амортизации с начала года!')
                return false
            }

            // 8. Арифметические проверки расчета граф 8, 10-16, 18
            calc11and15and16 = calc11and15and16(reportMounth, row, prevRow)
            if (check(row.getCell('usefulLifeWithUsed'), calc8(row)) ||
                    check(row.getCell('cost10perMonth'), calc10(row, map, check17)) ||
                    check(row.getCell('cost10perExploitation'), calc11and15and16[0]) ||
                    check(row.getCell('amortTaxPeriod'), calc11and15and16[1]) ||
                    check(row.getCell('amortExploitation'), calc11and15and16[2]) ||
                    check(row.getCell('cost10perExploitation'), calc12(row, prevRow, rpStartDate, rpEndDate)) ||
                    check(row.getCell('amortNorm'), calc12(row)) ||
                    check(row.getCell('amortMonth'), calc14(row, prevRow, lastDay2001)) ||
                    check(row.getCell('usefullLifeEnd'), calc18(row))) {
                return false
            }

            // Проверки соответствия НСИ.
            if (!checkNSI(row, "amortGroup", "Амортизационные группы", 71)) {
                return false
            }
        }
    }
    return true
}

boolean check(def cell, def value) {
    if (cell.value != value) {
        logger.error("Неверно рассчитана графа «" + cell.column.name + "»!")
        return false
    }
    return true
}

/**
 * Проверка соответствия НСИ
 */
boolean checkNSI(DataRow<Cell> row, String alias, String msg, Long id) {
    def cell = row.getCell(alias)
    if (cell.value != null && refBookService.getRecordData(id, cell.value) == null) {
        def msg2 = cell.column.name
        def rowNum = row.getIndex()
        logger.warn("Строка $rowNum: В справочнике «$msg» не найден элемент «$msg2»!")
        return false
    }
    return true
}

// Консолидация
void consolidation() {
    // удалить все строки и собрать из источников их строки
    def rows = new LinkedList<DataRow<Cell>>()
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                formDataService.getDataRowHelper(source).getAllCached().each { row ->
                    if (row.getAlias() == null || row.getAlias() == '') {
                        rows.add(row)
                    }
                }
            }
        }
    }
    formDataService.getDataRowHelper(formData).save(rows)
    logger.info('Формирование консолидированной формы прошло успешно.')
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

    def findForm = formDataService.find(formData.formType.id,
            formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }
}

//Получить номер строки в таблице
def getIndex(def row) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.getAllCached().indexOf(row)
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

    def cell
    columns.each {
        cell = row.getCell(it)
        if (cell.getValue() == null || row.getCell(it).getValue() == '') {
            def name = row.getCell(it).getColumn().getName().replace('%', '%%')
            colNames.add('"' + name + '"')
        }
    }
    if (!colNames.isEmpty()) {
        def index = row.rowNumber
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

def round(def value, def int precision = 2) {
    if (value == null) {
        return null
    }
    return value.setScale(precision, RoundingMode.HALF_UP)
}

//Получить отчетную дату
def getReportDate() {
    def tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
    return (tmp ? tmp.getTime() + 1 : null)
}

// Получить данные за предыдущий месяц
def getFormDataOld() {
    // предыдущий отчётный период
    def reportPeriodOld = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)

    // РНУ-55 за предыдущий отчетный период
    def formDataOld = null
    if (reportPeriodOld != null) {
        formDataOld = formDataService.find(formData.formType.id, formData.kind, formDataDepartment.id, reportPeriodOld.id)
    }

    return formDataOld
}

// Получить значение за предыдущий отчетный период для графы 11, 12, 14, 15, 16
def getPrevRow(def dataOld, def row) {
    if (dataOld != null)
        for (def rowOld : dataOld.getAllCached()) {
            if (rowOld.invNumber == row.invNumber) {
                return rowOld
            }
        }
    return null
}