package form_template.income.rnu108

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field
import java.math.RoundingMode

/*
 * (РНУ-108) Регистр налогового учёта расходов, связанных с приобретением услуг у Взаимозависимых лиц и резидентов оффшорных зон и подлежащих корректировке в связи с применением цен, не соответствующих рыночному уровню
 * formTemplateId=395
 *
 * @author akadyrgulov
 * @author Stanislav Yasinskiy
 * @author bkinzyabulatov
 *
 * графа 1 - rowNumber
 * графа 2 - personName
 * графа 3 - inn
 * графа 4 - date
 * графа 5 - code
 * графа 6 - docNumber
 * графа 7 - docDate
 * графа 8 - contractNumber
 * графа 9 - contractDate
 * графа 10 - priceService
 * графа 11 - priceMarket
 * графа 12 - factSum
 * графа 13 - correctKoef
 * графа 14 - marketSum
 * графа 15 - deviatSum
 * графа 16 - code2
*/

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow?.getAlias() == null) {
            formDataService.getDataRowHelper(formData).delete(currentDataRow)
        }
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE :
        formDataService.consolidationSimple(formData, formDataDepartment.id, logger)
        calc()
        logicCheck()
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
def allColumns = ["rowNumber", "fix", "personName", "inn", "date", "code",
        "docNumber", "docDate", "contractNumber", "contractDate", "priceService",
        "priceMarket", "factSum", "correctKoef", "marketSum", "deviatSum", "code2"]

// Поля, для которых подсчитываются итоговые значения
@Field
def totalColumns = ['deviatSum']

// Редактируемые атрибуты
@Field
def editableColumns = ['personName', 'date', 'code', 'docNumber', 'docDate', 'contractNumber',
        'contractDate', 'priceService', 'priceMarket', 'correctKoef']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ["factSum", "marketSum", "deviatSum", "code2"]

// Группируемые атрибуты
@Field
def groupColumns = ['personName']

@Field
def nonEmptyColumns = ['rowNumber', 'personName', 'inn', 'date', 'code',
        'docNumber', 'docDate', 'contractNumber', 'contractDate', 'priceService',
        'priceMarket', 'factSum', 'marketSum', 'deviatSum']

boolean checkNSI(def refBookId, def row, def alias) {
    return formDataService.checkNSI(refBookId, refBookCache, row, alias, logger, false)
}

def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache);
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    // Удаление подитогов
    deleteAllAliased(dataRows)

    // Сортировка
    dataRows.sort{ getRefBookValue(9, it.personName)?.NAME?.stringValue }

    // Номер последний строки предыдущей формы
    def i = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'rowNumber')

    for (row in dataRows) {
        row.with{
            rowNumber = ++i
            inn = calc3(row)
            factSum = calc12(row)
            marketSum = calc14(row)
            deviatSum = calc15(row)
        }
    }

    // Добавить строки итогов/подитогов
    addAllAliased(dataRows, new CalcAliasRow() {
        @Override
        DataRow<Cell> calc(int ind, List<DataRow<Cell>> rows) {
            return calcItog(ind, rows)
        }
    }, groupColumns)
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.fix = 'Итого'
    totalRow.getCell('fix').colSpan = 14
    allColumns.each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, totalRow, totalColumns)

    dataRows.add(totalRow)
    dataRowHelper.save(dataRows)
}

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    def dFrom = reportPeriodService.getStartDate(formData.reportPeriodId)?.time
    def dTo = reportPeriodService.getEndDate(formData.reportPeriodId)?.time

    if (!dataRows.isEmpty()) {
        def i = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'rowNumber')

        for (def row : dataRows) {
            if (row?.getAlias()!=null) {
                hasTotal = true
                continue
            }

            def index = row.getIndex()
            def errorMsg = "Строка $index: "

            checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

            // 2. Проверка на уникальность поля «№ пп»
            if (++i != row.rowNumber) {
                logger.error(errorMsg + 'Нарушена уникальность номера по порядку!')
            }

            // 3. Проверка даты совершения операции и границ отчётного периода
            if (row.date != null && (row.date < dFrom || dTo < row.date)) {
                logger.error(errorMsg + "Дата совершения операции вне границ отчётного периода!")
            }

            def values = [:]
            values.with{
                inn = calc3(row)
                factSum = calc12(row)
                marketSum = calc14(row)
                deviatSum = calc15(row)
            }
            checkCalc(row, autoFillColumns, values, logger, true)

            if (row.inn != values.inn){
                logger.error(errorMsg + "Неверное значение графы ${getColumnName(row, 'inn')}!")
            }

            //Проверки соответствия НСИ
            checkNSI(9, row, "personName")
            checkNSI(28, row, "code")
        }

        def testRows = dataRows.findAll{ it -> it.getAlias() == null }

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
                return getRefBookValue(9, dataRow.personName)?.NAME?.stringValue
            }
        }, new CheckGroupSum() {
            @Override
            String check(DataRow<Cell> row1, DataRow<Cell> row2) {
                if (row1.personName != row2.personName) {
                    return getColumnName(row1, 'personName')
                }
                return null
            }
        })
        testRows = dataRows.findAll{ it -> it.getAlias() == null || it.getAlias() == 'total'}
        checkTotalSum(testRows, totalColumns, logger, true)
    }
}

// Расчет подитогового значения
DataRow<Cell> calcItog(def int i, def List<DataRow<Cell>> dataRows) {
    def newRow = formData.createDataRow()

    newRow.getCell('fix').colSpan = 14
    newRow.fix = 'Итого по ' + getRefBookValue(9, dataRows.get(i).personName)?.NAME?.stringValue
    newRow.setAlias('total#'.concat(i.toString()))
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }

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

def calc3(def row) {
    def tmp
    if (row.personName != null) {
        tmp = getRefBookValue(9, row.personName)?.INN_KIO?.stringValue
    } else {
        tmp = null
    }
    return tmp
}

def calc12(def row) {
    return row.priceService
}

def calc14(def row) {
    def tmp
    // TODO Если цена за весь объем работ
    if (true) {
        tmp = row.priceMarket
    } else if (true && row.priceService != null && row.priceMarket != null) { // TODO Если за единицу товара
        tmp = row.priceService * row.priceMarket
    } else {
        tmp = null
    }
    return tmp?.setScale(2, RoundingMode.HALF_UP)
}

def calc15(def row){
    def tmp
    if (row.marketSum != null && row.factSum != null)
        tmp = (row.marketSum - row.factSum).abs()
    return tmp?.setScale(2, RoundingMode.HALF_UP)
}