package form_template.income.rnu46.v2012

import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import groovy.transform.Field

import java.math.RoundingMode
import java.text.SimpleDateFormat

/**
 * Скрипт для РНУ-46 (rnu46.groovy).
 * Форма "(РНУ-46) Регистр налогового учёта «карточка по учёту основных средств и капитальных вложений в неотделимые улучшения арендованного и полученного по договору безвозмездного пользования имущества»".
 * formTypeId=342
 *
 * @author rtimerbaev
 * @author Stanislav Yasinskiy
 * @author Dmitriy Levykin
 */

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

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        prevPeriodCheck()
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
        prevPeriodCheck()
        calc()
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow,
                isMonthBalance() ? balanceColumns : editableColumns,
                isMonthBalance() ? ['rowNumber', 'usefulLife'] : autoFillColumns )
        break
    case FormDataEvent.DELETE_ROW:
        formDataService.getDataRowHelper(formData).delete(currentDataRow)
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
        prevPeriodCheck()
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        formDataService.consolidationSimple(formData, formDataDepartment.id, logger)
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
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

// Редактируемые атрибуты
@Field
def editableColumns = ['invNumber', 'name', 'cost', 'amortGroup', 'monthsUsed', 'usefulLifeWithUsed',
        'specCoef', 'exploitationStart', 'rentEnd']

@Field
def balanceColumns = ['invNumber', 'name', 'cost', 'amortGroup', 'monthsUsed', 'usefulLifeWithUsed',
        'specCoef', 'cost10perMonth', 'cost10perTaxPeriod', 'cost10perExploitation', 'amortNorm', 'amortMonth',
        'amortTaxPeriod', 'amortExploitation', 'exploitationStart', 'usefullLifeEnd', 'rentEnd']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['rowNumber', 'invNumber', 'name', 'cost', 'amortGroup', 'monthsUsed',
        'usefulLifeWithUsed', 'specCoef', 'cost10perMonth', 'cost10perTaxPeriod', 'cost10perExploitation',
        'amortNorm', 'amortMonth', 'amortTaxPeriod', 'amortExploitation', 'exploitationStart', 'usefullLifeEnd']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber', 'cost10perMonth', 'cost10perTaxPeriod', 'cost10perExploitation',
        'amortNorm', 'amortMonth', 'usefulLife', 'amortTaxPeriod', 'amortExploitation', 'usefullLifeEnd']

//// Обертки методов

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache);
}

@Field
def formDataPrev = null // Форма предыдущего месяца
@Field
def dataRowHelperPrev = null // DataRowHelper формы предыдущего месяца
@Field
def isBalance = null
@Field
def format = new SimpleDateFormat('dd.MM.yyyy')
@Field
def check17 = format.parse('01.01.2006')
@Field
def lastDay2001 = format.parse('31.12.2001')
// Дата начала отчетного периода
@Field
def startDate = null
// Дата окончания отчетного периода
@Field
def endDate = null

//// Кастомные методы

// Поиск записи в справочнике по значению (для импорта)
def getRecordImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                    def boolean required = true) {
    if (value == null || value == '') {
        return null
    }
    return formDataService.getRefBookRecordImport(refBookId, recordCache, providerCache, refBookCache, alias, value,
            getMonthEndDate(), rowIndex, colIndex, logger, required)
}

// Получение формы предыдущего месяца
FormData getFormDataPrev() {
    if (formDataPrev == null) {
        formDataPrev = formDataService.getFormDataPrev(formData, formDataDepartment.id)
    }
    return formDataPrev
}

// Получение DataRowHelper формы предыдущего месяца
def getDataRowHelperPrev() {
    if (dataRowHelperPrev == null) {
        def formDataPrev = getFormDataPrev()
        if (!isMonthBalance() && formDataPrev != null && formDataPrev.state == WorkflowState.ACCEPTED) {
            dataRowHelperPrev = formDataService.getDataRowHelper(formDataPrev)
        }
    }
    return dataRowHelperPrev
}

// Признак периода ввода остатков. Отчетный период является периодом ввода остатков и месяц первый в периоде.
def isMonthBalance() {
    if (isBalance == null) {
        // Отчётный период
        if (!reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId) || formData.periodOrder == null) {
            isBalance = false
        } else {
            isBalance = (formData.periodOrder - 1) % 3 == 0
        }
    }
    return isBalance
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    if (dataRows.isEmpty()) {
        return
    }

    // Принятый отчет за предыдущий месяц
    def dataPrev = null
    if (!isMonthBalance() && formData.kind == FormDataKind.PRIMARY) {
        dataPrev = getDataRowHelperPrev()
    }

    // Сквозная нумерация с начала года
    def rowNumber = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'rowNumber')

    for (def row in dataRows) {
        // Графа 1
        row.rowNumber = ++rowNumber

        if (isMonthBalance() || formData.kind != FormDataKind.PRIMARY) {
            // Для периода ввода остатков расчитывается только порядковый номер
            continue;
        }

        def map = row.amortGroup == null ? null : getRefBookValue(71, row.amortGroup)

        // Строка из предыдущей формы с тем же инвентарным номером
        def prevRow = getPrevRow(dataPrev, row)

        // Графа 8
        row.usefulLifeWithUsed = calc8(row)

        // Графа 10
        row.cost10perMonth = calc10(row, map)

        // графа 12
        row.cost10perExploitation = calc12(row, prevRow)

        // графа 18
        row.usefullLifeEnd = calc18(row)

        // графа 14
        row.amortMonth = calc14(row, prevRow)

        // Графа 11, 15, 16
        def calc11and15and16 = calc11and15and16(row, prevRow)
        row.cost10perTaxPeriod = calc11and15and16[0]
        row.amortTaxPeriod = calc11and15and16[1]
        row.amortExploitation = calc11and15and16[2]

        // графа 13
        row.amortNorm = calc13(row)
    }
    dataRowHelper.update(dataRows);
}

// Ресчет графы 8
BigDecimal calc8(def row) {
    if (row.monthsUsed == null || row.amortGroup == null || row.specCoef == null) {
        return null
    }
    def map = getRefBookValue(71, row.amortGroup)
    def term = map.TERM.numberValue
    if (row.monthsUsed < term) {
        if (row.specCoef > 0) {
            return round((term - row.monthsUsed) / row.specCoef, 0)
        } else {
            return round(term - row.monthsUsed, 0)
        }
    }
    return row.usefulLifeWithUsed
}

// Ресчет графы 10
BigDecimal calc10(def row, def map) {
    def Integer group = map?.GROUP?.numberValue
    def result = null
    if ([1, 2, 8, 9, 10].contains(group) && row.cost != null) {
        result = round(row.cost * 0.1)
    } else if ((3..7).contains(group) && row.cost != null) {
        result = round(row.cost * 0.3)
    } else if (row.exploitationStart != null && row.exploitationStart < check17) {
        result = 0
    }
    return result
}

// Ресчет граф 11, 15, 16
BigDecimal[] calc11and15and16(def row, def prevRow) {
    def BigDecimal[] values = new BigDecimal[3]
    def reportMonth = formData.periodOrder

    // Calendar.JANUARY == 0, FormData нумерация с 1
    if (reportMonth == (Calendar.JANUARY + 1)) {
        values[0] = row.cost10perMonth
        values[1] = row.amortMonth
        values[2] = (prevRow != null ? prevRow.amortExploitation : 0)
    } else if (prevRow != null) {
        if (row.cost10perMonth != null && prevRow.cost10perTaxPeriod != null) {
            values[0] = row.cost10perMonth + prevRow.cost10perTaxPeriod
        }
        if (row.amortMonth != null && prevRow.amortTaxPeriod != null) {
            values[1] = row.amortMonth + prevRow.amortTaxPeriod
        }
        if (row.amortMonth != null && prevRow.amortExploitation != null) {
            values[2] = row.amortMonth + prevRow.amortExploitation
        }
    }
    return values
}

// Ресчет графы 12
BigDecimal calc12(def row, def prevRow) {
    def startDate = getReportPeriodStartDate()
    def endDate = getReportPeriodEndDate()

    if (prevRow == null || row.exploitationStart == null) {
        return 0 as BigDecimal
    }
    if (startDate < row.exploitationStart && row.exploitationStart < endDate) {
        return row.cost10perMonth
    }
    if (prevRow != null && row.cost10perMonth != null && prevRow.cost10perExploitation != null) {
        return row.cost10perMonth + prevRow.cost10perExploitation
    }
    return 0 as BigDecimal
}

// Ресчет графы 13
BigDecimal calc13(def row) {
    if (row == null || row.usefulLifeWithUsed == null || row.usefulLifeWithUsed == 0) {
        return null
    }
    return round(100 / row.usefulLifeWithUsed, 0)
}

// Ресчет графы 14
BigDecimal calc14(def row, def prevRow) {
    def endDate = getReportPeriodStartDate() - 1
    if (prevRow == null || row.usefullLifeEnd == null || row.cost10perExploitation == null || row.cost == null
            || prevRow?.cost == null || prevRow?.amortExploitation == null) {
        return 0 as BigDecimal
    }
    if (row.usefullLifeEnd > lastDay2001) {
        def date1 = Long.valueOf(row.usefullLifeEnd.format("MM")) +  Long.valueOf(row.usefullLifeEnd.format("yyyy"))*12
        def date2 = Long.valueOf(endDate.format("MM")) +  Long.valueOf(endDate.format("yyyy"))*12
        if ((date1 - date2) == 0) {
            return 0 as BigDecimal
        }
        return round((prevRow.cost - row.cost10perExploitation - prevRow.amortExploitation) / (date1 - date2))
    }
    return round(row.cost / 84)
}

// Ресчет графы 18
Date calc18(def row) {
    if (row.exploitationStart == null || row.usefulLifeWithUsed == null) {
        return null
    }
    def Calendar tmpCal = Calendar.getInstance()
    tmpCal.setTime(row.exploitationStart)
    tmpCal.add(Calendar.MONTH, row.usefulLifeWithUsed.intValue() + 1)
    tmpCal.set(Calendar.DAY_OF_MONTH, 0)
    return tmpCal.getTime()
}

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).getAllCached()

    // Алиасы граф для арифметической проверки
    def arithmeticCheckAlias = ['usefulLifeWithUsed', 'cost10perMonth', 'cost10perTaxPeriod', 'amortTaxPeriod',
            'amortExploitation', 'cost10perExploitation', 'amortNorm', 'amortMonth']

    // Для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]

    // Инвентарные номера
    def Set<String> invSet = new HashSet<String>()
    def inventoryNumbersOld = []
    def dataRowHelperOld = null
    if (formData.kind == FormDataKind.PRIMARY && !isMonthBalance()) {
        dataRowHelperOld = getDataRowHelperPrev()
        if (dataRowHelperOld) {
            dataRowHelperOld.allCached.each { row ->
                inventoryNumbersOld.add(row.invNumber)
            }
        }
    }

    for (def row : dataRows) {
        def map = null
        if (row.amortGroup != null) {
            map = getRefBookValue(71, row.amortGroup)
        }

        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Проверка на заполнение (графа 1..18)
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, !isMonthBalance())

        // 2. Проверка на уникальность поля «инвентарный номер»
        if (invSet.contains(row.invNumber)) {
            loggerError(row, errorMsg + "Инвентарный номер не уникальный!")
        } else {
            invSet.add(row.invNumber)
        }

        // 5. Проверка на нулевые значения (графа 9, 10, 11, 13, 14, 15)
        if (row.specCoef == 0 &&
                row.cost10perMonth == 0 &&
                row.cost10perTaxPeriod == 0 &&
                row.amortNorm &&
                row.amortMonth == 0 &&
                row.amortTaxPeriod) {
            loggerError(row, errorMsg + 'Все суммы по операции нулевые!')
        }

        if (formData.kind == FormDataKind.PRIMARY) {

            def prevRow = getPrevRow(dataRowHelperOld, row)
            def prevSum = getYearSum(['cost10perMonth', 'amortMonth'], row)

            // 6. Проверка суммы расходов в виде капитальных вложений с начала года
            if (prevRow == null ||
                    row.cost10perTaxPeriod == null ||
                    row.cost10perMonth == null ||
                    prevRow.cost10perTaxPeriod == null ||
                    row.cost10perTaxPeriod < row.cost10perMonth ||
                    row.cost10perTaxPeriod != row.cost10perMonth + prevRow.cost10perTaxPeriod ||
                    row.cost10perTaxPeriod != prevSum.cost10perMonth) {
                loggerError(row, errorMsg + 'Неверная сумма расходов в виде капитальных вложений с начала года!')
            }

            // 7. Проверка суммы начисленной амортизации с начала года
            if (prevRow == null ||
                    row.amortTaxPeriod == null ||
                    row.amortMonth == null ||
                    row.amortTaxPeriod < row.amortMonth ||
                    row.amortTaxPeriod != row.amortMonth + prevRow.amortTaxPeriod ||
                    row.amortTaxPeriod != prevSum.amortMonth) {
                loggerError(row, errorMsg + 'Неверная сумма начисленной амортизации с начала года!')
            }

            // 8. Арифметические проверки расчета граф 8, 10-16, 18
            needValue['usefulLifeWithUsed'] = calc8(row)
            needValue['cost10perMonth'] = calc10(row, map)
            needValue['cost10perExploitation'] = calc12(row, prevRow)
            needValue['amortNorm'] = calc13(row)
            needValue['amortMonth'] = calc14(row, prevRow)
            def calc11and15and16 = calc11and15and16(row, prevRow)
            needValue['cost10perTaxPeriod'] = calc11and15and16[0]
            needValue['amortTaxPeriod'] = calc11and15and16[1]
            needValue['amortExploitation'] = calc11and15and16[2]

            // Для проверок
            // logger.info("10 = " + needValue['cost10perMonth'] + " : " + row.cost10perMonth)
            // logger.info("11 = " + needValue['cost10perTaxPeriod'] + " : " + row.cost10perTaxPeriod)
            // logger.info("12 = " + needValue['cost10perExploitation'] + " : " + row.cost10perExploitation)

            // logger.info("14 = " + needValue['amortMonth'] + " : " + row.amortMonth)
            // logger.info("15 = " + needValue['amortTaxPeriod'] + " : " + row.amortTaxPeriod)
            // logger.info("16 = " + needValue['amortExploitation'] + " : " + row.amortExploitation)

            checkCalc(row, arithmeticCheckAlias, needValue, logger, !isMonthBalance())

            if (row.usefullLifeEnd != calc18(row)) {
                loggerError(row, errorMsg + "Неверное значение графы: ${getColumnName(row, 'usefullLifeEnd')}!")
            }
        }
    }

    // 10. Проверки существования необходимых экземпляров форм
    if (formData.kind == FormDataKind.PRIMARY && !isMonthBalance()) {
        for (def inv in invSet) {
            if (!inventoryNumbersOld.contains(inv)) {
                logger.warn('Отсутствуют данные за прошлые отчетные периоды!')
                break
            }
        }
    }
}
// Округление
def BigDecimal round(BigDecimal value, def int precision = 2) {
    return value?.setScale(precision, RoundingMode.HALF_UP)
}

// Поиск строки из предыдущей формы с тем же инвентарным номером
def getPrevRow(def dataPrev, def row) {
    if (dataPrev != null) {
        for (def rowPrev : dataPrev.getAll()) {
            if (rowPrev.invNumber == row.invNumber) {
                return rowPrev
            }
        }
    }
    return null
}

// Получение суммы по графе всех предыдущих принятых форм и по графе текущей формы
def getYearSum(def aliases, def rowCurrent) {
    def retVal = [:]

    for (def alias : aliases) {
        retVal[alias] = 0
    }

    // Налоговый период
    def taxPeriod = reportPeriodService.get(formData.reportPeriodId).taxPeriod

    // Сумма в текущей форме
    for (def alias : aliases) {
        def val = rowCurrent.get(alias)
        retVal[alias] += val == null ? 0 : val
    }
    // Сумма в предыдущих формах
    for (def month = formData.periodOrder - 1; month >= 1; month--) {
        def prevFormData = formDataService.findMonth(formData.formType.id, formData.kind, formData.departmentId,
                taxPeriod.id, month)
        if (prevFormData != null && prevFormData.state == WorkflowState.ACCEPTED) {
            def row = getPrevRow(formDataService.getDataRowHelper(prevFormData), rowCurrent)
            if (row) {
                for (def alias : aliases) {
                    def val = row.get(alias)
                    retVal[alias] += val == null ? 0 : val
                }
            }
        }
    }
    return retVal
}

// Получение импортируемых данных
void importData() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, '№ пп', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 18, 2)

    def headerMapping = [
            (xml.row[0].cell[0]): '№ пп',
            (xml.row[0].cell[1]): 'Инв. номер',
            (xml.row[0].cell[2]): 'Наименование объекта',
            (xml.row[0].cell[3]): 'Первоначальная стоимость',
            (xml.row[0].cell[4]): 'Амортизационная группа',
            (xml.row[0].cell[5]): 'Срок полезного использования, (мес.)',
            (xml.row[0].cell[6]): 'Количество месяцев эксплуатации предыдущими собственниками (арендодателями, ссудодателями)',
            (xml.row[0].cell[7]): 'Срок полезного использования с учётом срока эксплуатации предыдущими собственниками (арендодателями, ссудодателями) либо установленный самостоятельно, (мес.)',
            (xml.row[0].cell[8]): 'Специальный коэффициент',
            (xml.row[0].cell[9]): '10% (30%) от первоначальной стоимости, включаемые в расходы',
            (xml.row[0].cell[12]): 'Норма амортизации (% в мес.)',
            (xml.row[0].cell[13]): 'Сумма начисленной амортизации',
            (xml.row[0].cell[16]): 'Дата ввода в эксплуатацию',
            (xml.row[0].cell[17]): 'Дата истечения срока полезного использования',
            (xml.row[0].cell[18]): 'Дата истечения срока договора аренды / договора безвозмездного пользования',
            (xml.row[1].cell[9]): 'За месяц',
            (xml.row[1].cell[10]): 'с начала налогового периода',
            (xml.row[1].cell[11]): 'с даты ввода в эксплуатацию',
            (xml.row[1].cell[13]): 'за месяц',
            (xml.row[1].cell[14]): 'с начала налогового периода',
            (xml.row[1].cell[15]): 'с даты ввода в эксплуатацию',
            (xml.row[2].cell[0]): '1',
            (xml.row[2].cell[1]): '2',
            (xml.row[2].cell[2]): '3',
            (xml.row[2].cell[3]): '4',
            (xml.row[2].cell[4]): '5',
            (xml.row[2].cell[5]): '6',
            (xml.row[2].cell[6]): '7',
            (xml.row[2].cell[7]): '8',
            (xml.row[2].cell[8]): '9',
            (xml.row[2].cell[9]): '10',
            (xml.row[2].cell[10]): '11',
            (xml.row[2].cell[11]): '12',
            (xml.row[2].cell[12]): '13',
            (xml.row[2].cell[13]): '14',
            (xml.row[2].cell[14]): '15',
            (xml.row[2].cell[15]): '16',
            (xml.row[2].cell[16]): '17',
            (xml.row[2].cell[17]): '18',
            (xml.row[2].cell[18]): '19'
    ]

    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def xmlIndexRow = -1 // Строки xml, от 0
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    def rows = []
    def int rowIndex = 1  // Строки НФ, от 1

    for (def row : xml.row) {
        xmlIndexRow++
        def int xlsIndexRow = xmlIndexRow + rowOffset

        // Пропуск строк шапки
        if (xmlIndexRow <= headRowCount) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        // Нет итогов
        def xmlIndexCol = 0

        def newRow = formData.createDataRow()
        newRow.setIndex(rowIndex++)
        newRow.setImportIndex(xlsIndexRow)
        if (isMonthBalance()){
            balanceColumns.each {
                newRow.getCell(it).editable = true
                newRow.getCell(it).setStyleAlias('Редактируемая')
            }
            newRow.getCell('rowNumber').setStyleAlias('Автозаполняемая')
            newRow.getCell('usefulLife').setStyleAlias('Автозаполняемая')
        }else{
            editableColumns.each {
                newRow.getCell(it).editable = true
                newRow.getCell(it).setStyleAlias('Редактируемая')
            }
            autoFillColumns.each {
                newRow.getCell(it).setStyleAlias('Автозаполняемая')
            }
        }

        // графа 1
        newRow.rowNumber = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++
        // графа 2
        newRow.invNumber = row.cell[xmlIndexCol].text()
        xmlIndexCol++
        // графа 3
        newRow.name = row.cell[xmlIndexCol].text()
        xmlIndexCol++
        // графа 4
        newRow.cost = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++
        // графа 5 - атрибут 643 - GROUP - "Группа", справочник 71 "Амортизационные группы"
        def record71 = getRecordImport(71, 'GROUP', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        newRow.amortGroup = record71?.record_id?.value
        xmlIndexCol++
        // графа 6
        if (record71 != null) {
            // графа 6 - зависит от графы 5 - атрибут 645 - TERM - "Срок полезного использования (месяцев)", справочник 71 "Амортизационные группы"
            formDataService.checkReferenceValue(71, row.cell[xmlIndexCol].text(), record71?.TERM?.value?.toString(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        }
        xmlIndexCol++
        // графа 7
        newRow.monthsUsed = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++
        // графа 8
        newRow.usefulLifeWithUsed = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++
        // графа 9
        newRow.specCoef = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++
        // графа 10
        newRow.cost10perMonth = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++
        // графа 11
        newRow.cost10perTaxPeriod = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++
        // графа 12
        newRow.cost10perExploitation = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++
        // графа 13
        newRow.amortNorm = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++
        // графа 14
        newRow.amortMonth = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++
        // графа 15
        newRow.amortTaxPeriod = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++
        // графа 16
        newRow.amortExploitation = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++
        // графа 17
        newRow.exploitationStart = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++
        // графа 18
        newRow.usefullLifeEnd = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++
        // графа 19
        newRow.rentEnd = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}

// Начальная дата отчетного периода (для ежемесячной формы)
def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getMonthStartDate(formData.reportPeriodId, formData.periodOrder).time
    }
    return startDate
}

// Конечная дата отчетного периода (для ежемесячной формы)
def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getMonthEndDate(formData.reportPeriodId, formData.periodOrder).time
    }
    return endDate
}

def loggerError(def row, def msg) {
    if (isMonthBalance()) {
        rowWarning(logger, row, msg)
    } else {
        rowError(logger, row, msg)
    }
}

void prevPeriodCheck() {
    if (!isMonthBalance() && formData.kind == FormDataKind.PRIMARY) {
        formDataService.checkMonthlyFormExistAndAccepted(formData.formType.id, FormDataKind.PRIMARY, formData.departmentId, formData.reportPeriodId, formData.periodOrder, true, logger, true)
    }
}