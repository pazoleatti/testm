package form_template.income.rnu71_2.v1970

import com.aplana.sbrf.taxaccounting.model.*
import groovy.transform.Field

import java.math.RoundingMode

/**
 * Форма "(РНУ-71.2) Регистр налогового учёта уступки права требования после предусмотренного кредитным договором срока погашения основного долга в отношении сделок уступки прав требования в пользу Взаимозависимых лиц и резидентов оффшорных зон"
 * formTemplateId=503
 * TODO графа 10, 18
 * @author bkinzyabulatov
 *
 * Графа 1  rowNumber               № пп
 * Графа 2  contragent              Наименование контрагента
 * Графа 3  inn                     ИНН (его аналог)
 * Графа 4  assignContractNumber    Договор цессии. Номер
 * Графа 5  assignContractDate      Договор цессии. Дата
 * Графа 6  amount                  Стоимость права требования
 * Графа 7  amountForReserve        Стоимость права требования, списанного за счёт резервов
 * Графа 8  repaymentDate           Дата погашения основного долга
 * Графа 9  dateOfAssignment        Дата уступки права требования
 * Графа 10 income                  Доход (выручка) от уступки права требования
 * Графа 11 result                  Финансовый результат уступки права требования
 * Графа 12 part2Date               Дата отнесения на расходы второй половины убытка
 * Графа 13 lossThisQuarter         Убыток, относящийся к расходам текущего квартала
 * Графа 14 lossNextQuarter         Убыток, относящийся к расходам следующего квартала
 * Графа 15 lossThisTaxPeriod       Убыток, относящийся к расходам текущего отчётного (налогового) периода, но полученный в предыдущем квартале
 * Графа 16 taxClaimPrice           Рыночная цена прав требования для целей налогообложения
 * Графа 17 correctThisPrev         Корректировка финансового результата в отношении убытка, относящегося к расходам текущего налогового периода, но полученного в предыдущем налоговом периоде
 * Графа 18 correctThisThis         Корректировка финансового результата в отношении убытка, относящегося к расходам текущего налогового периода и полученного в текущем налоговом периоде (прибыли, полученной в текущем налоговом периоде)
 * Графа 19 correctThisNext         Корректировка финансового результата в отношении убытка, относящегося полученного в текущем налоговом периоде, но относящегося к расходам следующего налогового периода
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW:
        if (!currentDataRow?.getAlias()?.contains('itg')) {
            formDataService.getDataRowHelper(formData).delete(currentDataRow)
        }
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        formDataService.consolidationSimple(formData, formDataDepartment.id, logger)
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        noImport(logger)
        break
}

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

@Field
def isBalancePeriod

// Все поля
@Field
def allColumns = ["rowNumber", "contragent", "inn", "assignContractNumber", "assignContractDate",
        "amount", "amountForReserve", "repaymentDate", "dateOfAssignment", "income",
        "result", "part2Date", "lossThisQuarter", "lossNextQuarter", "lossThisTaxPeriod",
        "taxClaimPrice", "correctThisPrev", "correctThisThis", "correctThisNext"]

// Поля, для которых подсчитываются итоговые значения
@Field
def totalColumns = ["income", "result", "lossThisQuarter", "lossNextQuarter", "lossThisTaxPeriod",
        "correctThisPrev", "correctThisThis", "correctThisNext"]

// Редактируемые атрибуты
@Field
def editableColumns = ["contragent", "inn", "assignContractNumber", "assignContractDate",
        "amount", "amountForReserve", "repaymentDate", "dateOfAssignment", "income"]

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ["result", "lossThisQuarter", "lossNextQuarter", "lossThisTaxPeriod",
        "taxClaimPrice", "correctThisPrev", "correctThisThis", "correctThisNext"]

// Группируемые атрибуты
@Field
def groupColumns = ['contragent']

@Field
def sortColumns = ["contragent", "assignContractDate", "assignContractNumber"]

// TODO Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ["rowNumber", "contragent", "inn", "assignContractNumber", "assignContractDate",
        "amount", "amountForReserve", "repaymentDate", "dateOfAssignment", "income",
        "result"]

//// Кастомные методы
void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def dFrom = reportPeriodService.getStartDate(formData.getReportPeriodId())?.time
    def dTo = reportPeriodService.getEndDate(formData.getReportPeriodId())?.time

    def dataRowsPrev
    if (!isBalancePeriod() && formData.kind == FormDataKind.PRIMARY) {
        dataRowsPrev = getDataRowsPrev()
    }

    // Номер последний строки предыдущей формы
    def i = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'rowNumber')

    for (def DataRow row : dataRows) {
        //проверка и пропуск итогов
        if (row?.getAlias()?.contains('itg')) {
            continue
        }

        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, !isBalancePeriod())

        if (!isBalancePeriod() && formData.kind == FormDataKind.PRIMARY) {
            def values = [:]
            def rowPrev = getRowPrev(dataRowsPrev, row)
            values.with {
                result = calc11(row)
                part2Date = calc12(row)
                lossThisQuarter = calc13(row, dTo)
                lossNextQuarter = calc14(row, dTo)
                lossThisTaxPeriod = calc15(row, rowPrev, dFrom, dTo)
                taxClaimPrice = calc16(row)
                correctThisPrev = calc17(row, rowPrev, dFrom, dTo)
                correctThisThis = calc18(row, dTo)
                correctThisNext = calc19(row, dTo)
            }
            checkCalc(row, autoFillColumns, values, logger, true)

            if (row.part2Date != values.part2Date) {
                logger.error(errorMsg + "Неверное значение графы ${getColumnName(row, 'part2Date')}!")
            }
        }
        if (!(row.repaymentDate in (dFrom..dTo))) {
            loggerError(errorMsg + "Дата совершения операции вне границ отчетного периода!")
        }
        if (++i != row.rowNumber) {
            loggerError(errorMsg + 'Нарушена уникальность номера по порядку!')
        }
        if (row.income == 0 && row.lossThisQuarter == 0 && row.lossNextQuarter == 0) {
            loggerError(errorMsg + "Все суммы по операции нулевые!")
        }
        if (row.dateOfAssignment < row.repaymentDate) {
            loggerError(errorMsg + "Неверно указана дата погашения основного долга!")
        }
        if (row.amount > 0 && row.income > 0 && row.repaymentDate == null &&
                row.dateOfAssignment == null && row.lossThisTaxPeriod != null) {
            loggerError(errorMsg + "В момент уступки права требования «Графа 15» не заполняется!")
        }
        if (row.lossThisTaxPeriod > 0 &&
                ((row.amount == null && row.result != null) ||
                        row.lossThisQuarter != null ||
                        row.lossNextQuarter != null)) {
            loggerError(errorMsg + "В момент отнесения второй половины убытка на расходы графы кроме графы 15 и графы 12 не заполняются!")
        }
    }
    def testRows = dataRows.findAll { it -> it.getAlias() == null }

    addAllAliased(testRows, new CalcAliasRow() {
        @Override
        DataRow<Cell> calc(int ind, List<DataRow<Cell>> rows) {
            return calcItog(ind, rows)
        }
    }, groupColumns)
    // Рассчитанные строки итогов
    def testItogRows = testRows.findAll { it -> it.getAlias() != null }
    // Имеющиеся строки итогов
    def itogRows = dataRows.findAll { it -> it.getAlias() != null }

    checkItogRows(testRows, testItogRows, itogRows, groupColumns, logger, new GroupString() {
        @Override
        String getString(DataRow<Cell> dataRow) {
            return dataRow.contragent
        }
    }, new CheckGroupSum() {
        @Override
        String check(DataRow<Cell> row1, DataRow<Cell> row2) {
            if (row1.contragent != row2.contragent) {
                return getColumnName(row1, 'contragent')
            }
            return null
        }
    })
    def sumRowList = dataRows.findAll { it -> it.getAlias() == null || it.getAlias() == 'itg' }
    checkTotalSum(sumRowList, totalColumns, logger, !isBalancePeriod())
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // Удаление подитогов
    deleteAllAliased(dataRows)
    // Сортировка
    sortRows(dataRows, sortColumns)

    // Номер последний строки предыдущей формы
    def index = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'rowNumber')

    if (!isBalancePeriod() && formData.kind == FormDataKind.PRIMARY) {
        def dataRowsPrev = getDataRowsPrev()
        def dFrom = reportPeriodService.getStartDate(formData.getReportPeriodId())?.time
        def dTo = reportPeriodService.getEndDate(formData.getReportPeriodId())?.time

        // Расчет ячеек
        for (def row : dataRows) {
            def rowPrev = getRowPrev(dataRowsPrev, row)
            row.with {
                rowNumber = ++index
                result = calc11(row)
                part2Date = calc12(row)
                lossThisQuarter = calc13(row, dTo)
                lossNextQuarter = calc14(row, dTo)
                lossThisTaxPeriod = calc15(row, rowPrev, dFrom, dTo)
                taxClaimPrice = calc16(row)
                correctThisPrev = calc17(row, rowPrev, dFrom, dTo)
                correctThisThis = calc18(row, dTo)
                correctThisNext = calc19(row, dTo)
            }
        }
    } else {
        for (def row : dataRows) {
            row.rowNumber = ++index
        }
    }

    // Добавить строки итогов/подитогов
    addAllAliased(dataRows, new CalcAliasRow() {
        @Override
        DataRow<Cell> calc(int i, List<DataRow<Cell>> rows) {
            return calcItog(i, rows)
        }
    }, groupColumns)
    def totalRow = formData.createDataRow()
    totalRow.setAlias('itg')
    totalRow.contragent = 'Итого'
    allColumns.each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, totalRow, totalColumns)
    dataRows.add(totalRow)
    dataRowHelper.save(dataRows)
}

// Расчет подитогового значения
DataRow<Cell> calcItog(def int i, def List<DataRow<Cell>> dataRows) {
    def newRow = formData.createDataRow()

    newRow.getCell('contragent').colSpan = 6
    newRow.contragent = 'Итого по ' + (dataRows.get(i).contragent ?: '')
    newRow.setAlias('itg#'.concat(i.toString()))
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }

    // Расчеты подитоговых значений
    def sums = [:]
    totalColumns.each {
        sums[it] = 0
    }
    for (int j = i; j >= 0 && dataRows.get(j).getAlias() == null; j--) {
        row = dataRows.get(j)

        totalColumns.each {
            sums[it] += row[it] != null ? row[it] : 0
        }
    }

    totalColumns.each {
        newRow[it] = sums[it]
    }
    return newRow
}

def getRowPrev(def dataRowsPrev, def row) {
    if (dataRowsPrev != null) {
        for (def rowPrev in dataRowsPrev) {
            if ((row.contragent == rowPrev.contragent &&
                    row.inn == rowPrev.inn &&
                    row.assignContractNumber == rowPrev.assignContractNumber &&
                    row.assignContractDate == rowPrev.assignContractDate)) {
                return rowPrev
            }
        }
    }
}

def getDataRowsPrev() {
    def formDataPrev = formDataService.getFormDataPrev(formData, formData.departmentId)
    formDataPrev = formDataPrev?.state == WorkflowState.ACCEPTED ? formDataPrev : null
    if (formDataPrev == null) {
        logger.error("Не найдены экземпляры РНУ-71.2 за прошлый отчетный период!")
    } else {
        return formDataService.getDataRowHelper(formDataPrev)?.allCached
    }
    return null
}

def BigDecimal calc11(def row) {
    if (row.income != null && row.amount != null && row.amountForReserve != null) {
        return (row.income - (row.amount - row.amountForReserve)).setScale(2, RoundingMode.HALF_UP)
    }
}

def Date calc12(def row) {
    return row.dateOfAssignment ? (row.dateOfAssignment + 45) : null //не заполняется
}

def BigDecimal calc13(def row, def endDate) {
    def tmp
    if (row.result != null && row.result < 0) {
        if (row.part2Date != null && endDate != null) {
            if (row.part2Date <= endDate) {
                tmp = row.result
            } else {
                tmp = row.result * 0.5
            }
        }
    }
    return tmp?.setScale(2, RoundingMode.HALF_UP)
}

def BigDecimal calc14(def row, def endDate) {
    def tmp
    if (row.result != null && row.result < 0) {
        if (row.part2Date != null && endDate != null) {
            if (row.part2Date <= endDate) {
                tmp = BigDecimal.ZERO
            } else {
                tmp = row.result * 0.5
            }
        }
    }
    return tmp?.setScale(2, RoundingMode.HALF_UP)
}

def BigDecimal calc15(def row, def rowPrev, def startDate, def endDate) {
    def tmp
    if (startDate != null && endDate != null) {
        def period = (startDate..endDate)
        if (row.dateOfAssignment != null && row.part2Date != null && !(row.dateOfAssignment in period) && (row.part2Date in period)) {
            tmp = rowPrev?.lossNextQuarter
        }
    }
    return tmp?.setScale(2, RoundingMode.HALF_UP)
}

def BigDecimal calc16(def row) {
    if (row.lossThisTaxPeriod != null && row.amount != null && row.amountForReserve != null) {
        return (row.lossThisTaxPeriod - (row.amount - row.amountForReserve)).setScale(2, RoundingMode.HALF_UP)
    }
}

def BigDecimal calc17(def row, def rowPrev, def startDate, def endDate) {
    if (row.dateOfAssignment != null && startDate != null && row.part2Date != null && endDate != null &&
            row.result != null && row.dateOfAssignment < startDate && row.part2Date in (startDate..endDate)) {
        if (row.result < 0) {
            return rowPrev.correctThisNext
        }
    }
}

//TODO уточнить
def BigDecimal calc18(def row, def endDate) {
    def tmp
    if (row.result != null) {
        if (row.result < 0) {
            if (row.part2Date != null && endDate != null) {
                if (row.part2Date < endDate) {
                    if (row.taxClaimPrice != null) {
                        tmp = row.taxClaimPrice.abs() - row.result.abs()
                    } else {
                        tmp = null
                    }
                }
                if (row.part2Date > endDate) {
                    if (row.taxClaimPrice != null) {
                        tmp = (row.taxClaimPrice.abs() - row.result.abs()) * 0.5
                    } else {
                        tmp = null
                    }
                }
            } else {
                tmp = null
            }
        } else {
            if (row.taxClaimPrice != null) {
                if (row.taxClaimPrice > row.result) {
                    tmp = row.taxClaimPrice - row.result
                }
            } else {
                tmp = null
            }
        }
    } else {
        tmp = null
    }
    return tmp?.setScale(2, RoundingMode.HALF_UP)
}

def BigDecimal calc19(def row, def endDate) {
    def tmp
    if (row.result != null) {
        if (row.result < 0) {
            if (row.part2Date != null && endDate != null) {
                if (row.part2Date < endDate) {
                    tmp = BigDecimal.ZERO
                }
                if (row.part2Date > endDate) {
                    if (row.taxClaimPrice != null) {
                        tmp = (row.taxClaimPrice.abs() - row.result.abs()) * 0.5
                    } else {
                        tmp = null
                    }
                }
            } else {
                tmp = null
            }
        } else {
            if (row.taxClaimPrice != null) {
                if (row.taxClaimPrice > row.result) {
                    tmp = null
                }
            } else {
                tmp = null
            }
        }
    }
    return tmp?.setScale(2, RoundingMode.HALF_UP)
}

// Признак периода ввода остатков. Отчетный период является периодом ввода остатков.
def isBalancePeriod() {
    if (isBalancePeriod == null) {
        isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)
    }
    return isBalancePeriod
}

def loggerError(def msg) {
    if (isBalancePeriod()) {
        logger.warn(msg)
    } else {
        logger.error(msg)
    }
}