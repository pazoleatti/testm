package form_template.income.rnu70_2

import com.aplana.sbrf.taxaccounting.model.log.LogLevel

import java.text.DateFormat
import java.text.SimpleDateFormat

/**
 * Скрипт для РНУ-70.2.
 * (РНУ-70.2) Регистр налогового учёта уступки права требования до наступления, предусмотренного кредитным договором
 * срока погашения основного долга в отношении сделок уступки прав требования в пользу Взаимозависимых лиц и резидентов оффшорных зон
 * formTemplateId=357
 *
 * @author Stanislav Yasinskiy
 *
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkCreation()
        break
    case FormDataEvent.CHECK:
        logicalCheck()
        break
    case FormDataEvent.CALCULATE:
        deleteAllStatic()
        sort()
        calc()
        addAllStatic()
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
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED: // после принятия из подготовлена
        logicalCheck()
        break
}
/** Графы:
* 1    rowNumber                -      № пп
* 2    name                     -      Наименование контрагента
* 3    inn                      -      ИНН (его аналог)
* 4    number                   -      Номер
* 5    date                     -      Дата
* 6    cost                     -      Стоимость права требования
* 7    repaymentDate            -      Дата погашения основного долга
* 8    concessionsDate          -      Дата уступки права требования
* 9    income                   -      Доход (выручка) от уступки права требования
* 10   financialResult1         -      Финансовый результат уступки права требования
* 11   currencyDebtObligation   -      Валюта долгового обязательства
* 12   rateBR                   -      Ставка Банка России
* 13   interestRate             -      Ставка процента, установленная соглашением сторон
* 14   perc                     -      Проценты по долговому обязательству, рассчитанные с учётом ст. 269 НК РФ за
*                                      период от даты уступки права требования до даты платежа по договору
* 15   loss                     -      Убыток, превышающий проценты по долговому обязательству, рассчитанные с учётом
*                                      ст. 269 НК РФ за период от даты уступки  права требования до даты платежа по
*                                      договору
* 16   marketPrice              -      Рыночная цена прав требования для целей налогообложения
* 17   financialResult2         -      Финансовый результат, рассчитанный исходя из рыночной цены для целей налогообложения
* 18   maxLoss                  -      Максимальная сумма убытка, рассчитанного исходя из рыночной цены для целей налогообложения
* 19   financialResultAdjustment-      Корректировка финансового результата
**/

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
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def index = 0
    if (currentDataRow != null) {
        index = currentDataRow.getIndex()
        def row = currentDataRow
        while (row.getAlias() != null && index > 0) {
            row = getRows(dataRowHelper).get(--index)
        }
        if (index != currentDataRow.getIndex() && getRows(dataRowHelper).get(index).getAlias() == null) {
            index++
        }
    } else if (getRows(dataRowHelper).size() > 0) {
        for (int i = getRows(dataRowHelper).size() - 1; i >= 0; i--) {
            def row = getRows(dataRowHelper).get(i)
            if (!isFixedRow(row)) {
                index = getRows(dataRowHelper).indexOf(row) + 1
                break
            }
        }
    }
    dataRowHelper.insert(getNewRow(), index + 1)
}


def recalculateNumbers() {
    def index = 1
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    getRows(dataRowHelper).each { row ->
        if (!isFixedRow(row)) {
            row.rowNumber = index++
        }
    }
    dataRowHelper.save(getRows(dataRowHelper))
}

/**
 * Удалить строку.
 */
def deleteRow() {
    if (currentDataRow != null && currentDataRow.getAlias() == null) {
        def dataRowHelper = formDataService.getDataRowHelper(formData)
        dataRowHelper.delete(currentDataRow)
    }
}

/**
 * Алгоритмы заполнения полей формы
 */
void calc() {
    //расчет
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    for (def dataRow : dataRows) {
        dataRow.financialResult1 = getFinancialResult1(dataRow)
        dataRow.rateBR = getRateBR(dataRow, reportDate)
        dataRow.perc = getPerc(dataRow)
        dataRow.loss = getLoss(dataRow)
        dataRow.financialResult2 = getFinancialResult2(dataRow)
        dataRow.maxLoss = getMaxLoss(dataRow)
        dataRow.financialResultAdjustment = getFinancialResultAdjustment(dataRow)
    }
    dataRowHelper.update(dataRows)
}

/**
 * Проставляет статические строки
 */
void addAllStatic() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    // добавить строку "итого"
    def totalRow = getCalcTotalRow()
    data.insert(totalRow, dataRows.size() + 1)


    // посчитать "Итого по <Наименование контрагента>"
    def totalRows = getCalcTotalName()

    // добавить "Итого по <Наименование контрагента>" в таблицу
    def i = 1
    totalRows.each { index, row ->
        dataRowHelper.insert(row, index + i++)
    }
}

/**
 * Логические проверки
 */
boolean logicalCheck() {
    return requiredColsFilled() && checkRowsData()
}

/**
 * Проверки, не связанные с обязательностью заполнения полей
 */
boolean checkRowsData() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    boolean isValid = true

    for (def dataRow : dataRows) {
        if (isFixedRow(dataRow)) continue
        //  Проверка даты погашения основного долга «Графа 7» >= «Графа 8»
        if (dataRow.repaymentDate.before(dataRow.concessionsDate)) {
            isValid = false
            logger.error('Неверно указана дата погашения основного долга')
        }

        //  Проверка даты совершения операции «Графа 8» <= дата окончания отчётного периода
        if (reportDate.before(dataRow.concessionsDate)) {
            isValid = false
            logger.error('Неверно указана дата погашения основного долга')
        }

        //  Арифметическая проверка граф 10, 14, 15,17-19
        //  Графы 10, 14, 15,17-19 должны содержать значение, полученное согласно алгоритмам, описанным в разделе «Алгоритмы заполнения полей формы».
        if (dataRow.financialResult1 != getFinancialResult1(dataRow)) {
            isValid = false
            logger.error("Неверно рассчитана графа «${dataRow.getCell('financialResult1').column.name}»!")
        }

        if (dataRow.perc != getPerc(dataRow)) {
            isValid = false
            logger.error("Неверно рассчитана графа «${dataRow.getCell('perc').column.name}»!")
        }

        if (dataRow.loss != getLoss(dataRow)) {
            isValid = false
            logger.error("Неверно рассчитана графа «${dataRow.getCell('loss').column.name}»!")
        }

        if (dataRow.financialResult2 != getFinancialResult2(dataRow)) {
            isValid = false
            logger.error("Неверно рассчитана графа «${dataRow.getCell('financialResult2').column.name}»!")
        }

        if (dataRow.maxLoss != getMaxLoss(dataRow)) {
            isValid = false
            logger.error("Неверно рассчитана графа «${dataRow.getCell('maxLoss').column.name}»!")
        }

        if (dataRow.financialResultAdjustment != getFinancialResultAdjustment(dataRow)) {
            isValid = false
            logger.error("Неверно рассчитана графа «${dataRow.getCell('financialResultAdjustment').column.name}»!")
        }

        //Проверка принадлежности даты графы 8 отчетному периоду
        if (!isInPeriod(dataRow.concessionsDate)) {
            isValid = false
            logger.error('«Графа 8» не принадлежит отчетному периоду')
        }
    }


    for (def dataRow : dataRows) {
        //Проверка итоговых значений "Итого по <Наименование контрагента>"
        if (dataRow.getAlias() != null && dataRow.getAlias() != 'total') {
            srow = getCalcTotalNameRow(dataRow)
            for (def col : totalColumns) {
                if (dataRow[col] != srow[col]) {
                    isValid = false
                    logger.error("Итоговые значения по «" + dataRow.name + "» рассчитаны неверно!")
                    break
                }
            }
        }

        //Проверка итоговых значений по всей форме
        if (dataRow.getAlias() == 'total') {
            def totalRow = getCalcTotalRow()
            for (def col : totalColumns) {
                if (totalRow[col] != dataRow[col]) {
                    isValid = false
                    logger.error("Итоговые значения рассчитаны неверно!")
                    break
                }
            }
        }
    }

    return isValid
}

/**
 * Проверка на заполнение полей
 * Обязательность заполнения поля графы 1-12, 14, 15
 */
boolean requiredColsFilled() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    boolean isValid = true
    def requiredColsNames = ['name', 'inn', 'number', 'date', 'cost', 'repaymentDate', 'concessionsDate', 'income',
            'financialResult1', 'currencyDebtObligation', 'perc', 'loss']//, 'rateBR'
    for (def dataRow : dataRows) {
        if (!isFixedRow(dataRow)) {
            for (def colName : requiredColsNames) {
                if (isBlankOrNull(dataRow.get(colName))) {
                    isValid = false
                    def fieldName = dataRow.getCell(colName).column.name
                    logger.error("Поле «$fieldName» не заполнено!")
                }
            }
        }
    }

    return isValid
}

/**
 * вычисляем значение графы 10
 */
def getFinancialResult1(def dataRow) {
    if (dataRow.income == null || dataRow.cost == null)
        return null
    return dataRow.income - dataRow.cost
}

/**
 * Получить курс валюты
 */
def getRateBR(def dataRow, def date) {
    if (dataRow.currencyDebtObligation != null) {
        def refCourseDataProvider = refBookFactory.getDataProvider(22)
        def res = refCourseDataProvider.getRecords(date, null, 'CODE_NUMBER=' + dataRow.currencyDebtObligation, null);
        return (!res.getRecords().isEmpty()) ? res.getRecords().get(0).RATE.getNumberValue() : 0//Правильнее null, такой ситуации быть не должно, она должна отлавливаться проверками НСИ
    } else {
        return null;
    }
}

/**
 * вычисляем значение для графы 14
 */
BigDecimal getPerc(def dataRow) {
    if (dataRow.financialResult1 < 0 && dataRow.concessionsDate!=null) {
        final DateFormat dateFormat = new SimpleDateFormat('dd.MM.yyyy')
        final firstJan2010 = dateFormat.parse('01.01.2010')
        final thirtyFirstDec2013 = dateFormat.parse('31.12.2013')
        final firstJan2011 = dateFormat.parse('01.01.2011')

        BigDecimal x

        final repaymentDateDuration = getRepaymentDateDuration(dataRow)
        if (isRoublel(dataRow)) {
            if (dataRow.concessionsDate.after(firstJan2010) && dataRow.concessionsDate.before(thirtyFirstDec2013)) {
                if (dataRow.rateBR * 1.8 <= dataRow.interestRate) {
                    x = getXByRateBR(dataRow, repaymentDateDuration, 1.8)
                } else {
                    x = getXByInterestRate(dataRow, repaymentDateDuration)
                }
                if (dataRow.interestRate == 0) x = getXByRateBR(dataRow, repaymentDateDuration, 1.8)
            } else {
                x = getXByRateBR(dataRow, repaymentDateDuration, 1.1)
            }
        } else {
            if (dataRow.concessionsDate.after(firstJan2011) && dataRow.concessionsDate.before(thirtyFirstDec2013)) {
                x = getXByRateBR(dataRow, repaymentDateDuration, 0.8)
            } else {
                x = getXByIncomeOnly(dataRow, repaymentDateDuration, 0.15)
            }
        }

        if (x.abs() > dataRow.financialResult1.abs()) {
            return dataRow.financialResult1
        } else {
            return x.setScale(2, BigDecimal.ROUND_HALF_UP)
        }
    } else {
        return new BigDecimal(0)
    }
}

/**
 * вычисляем значение для графы 15
 */
def getLoss(def dataRow) {
    if (dataRow.cost < 0 && dataRow.financialResult1!=null && dataRow.perc!=null) {
        return dataRow.financialResult1.abs() - dataRow.perc
    } else {
        return new BigDecimal(0)
    }
}

/**
 * вычисляем значение для графы 17
 */
def getFinancialResult2(def dataRow) {
    return dataRow.marketPrice - dataRow.cost
}

/**
 * вычисляем значение для графы 18
 */
def getMaxLoss(def dataRow) {
    if (dataRow.financialResult1 < 0) {
        final DateFormat dateFormat = new SimpleDateFormat('dd.MM.yyyy')
        final firstJan2010 = dateFormat.parse('01.01.2010')
        final thirtyFirstDec2013 = dateFormat.parse('31.12.2013')
        final firstJan2011 = dateFormat.parse('01.01.2011')

        BigDecimal x

        final repaymentDateDuration = getRepaymentDateDuration(dataRow)
        if (isRoublel(dataRow)) {
            if (dataRow.concessionsDate.after(firstJan2010) && dataRow.concessionsDate.before(thirtyFirstDec2013)) {
                if (dataRow.rateBR * 1.8 <= dataRow.interestRate) {
                    x = dataRow.marketPrice * dataRow.rateBR * 1.8 * repaymentDateDuration
                } else {
                    x = dataRow.marketPrice * dataRow.interestRate * repaymentDateDuration
                }
                if (dataRow.interestRate == 0) x = dataRow.marketPrice * dataRow.rateBR * repaymentDateDuration
            } else {
                x = dataRow.marketPrice * dataRow.rateBR * 1.1 * repaymentDateDuration
            }
        } else {
            if (dataRow.concessionsDate.after(firstJan2011) && dataRow.concessionsDate.before(thirtyFirstDec2013)) {
                x = dataRow.marketPrice * dataRow.rateBR * 0.8 * repaymentDateDuration
            } else {
                x = dataRow.marketPrice * 0.15 * repaymentDateDuration
            }
        }

        if (x.abs() > dataRow.financialResult1.abs()) {
            return dataRow.financialResult1
        } else {
            return x.setScale(2, BigDecimal.ROUND_HALF_UP)
        }
    } else {
        return new BigDecimal(0)
    }
}

/**
 * вычисляем значение для графы 19
 */
def getFinancialResultAdjustment(def dataRow) {
    if (dataRow.financialResult1 >= 0 && dataRow.financialResult2 > 0) {
        return (dataRow.financialResult2 - dataRow.financialResult1).abs().setScale(2, BigDecimal.ROUND_HALF_UP)
    } else {
        return -(dataRow.maxLoss - dataRow.loss).setScale(2, BigDecimal.ROUND_HALF_UP)
    }
}


boolean isBlankOrNull(value) {
    value == null || value.equals('')
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

/**
 * Проверка валюты на рубли
 */
def isRoublel(def dataRow) {
    return refBookService.getStringValue(15, dataRow.currencyDebtObligation, 'CODE') == '643'
}

def getRepaymentDateDuration(dataRow) {
    if (dataRow.repaymentDate == null || dataRow.concessionsDate == null)
        return null
    return (dataRow.repaymentDate - dataRow.concessionsDate) / getCountDaysInYear(new Date())    //(«графа 7» - «графа 8»)/365(366)
}

/**
 * Получить количество дней в году по указанной дате.
 */
def getCountDaysInYear(def date) {
    if (date == null) {
        return 0
    }
    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    def year = date.format('yyyy')
    def end = format.parse("31.12.$year")
    def begin = format.parse("01.01.$year")
    return end - begin + 1
}

// вспомогательная функция для расчета графы 14
// «Графа 14» = «Графа 9» * «Графа 13» * («Графа 7» - «Графа 8») / 365(366);
BigDecimal getXByInterestRate(def dataRow, def repaymentDateDuration) {
    if (dataRow.income == null || dataRow.interestRate == null || repaymentDateDuration== null)
        return null
    x2 = dataRow.income * dataRow.interestRate * repaymentDateDuration
    return x2.setScale(2, BigDecimal.ROUND_HALF_UP)
}

// вспомогательная функция для расчета графы 14
// «Графа 14» = «Графа 9» * «Графа 12» * index * («Графа 7» – «Графа 8») / 365(366)
BigDecimal getXByRateBR(def dataRow, def repaymentDateDuration, BigDecimal index) {
    if (dataRow.income == null || dataRow.rateBR == null || repaymentDateDuration== null || index== null)
        return null
    x = dataRow.income * dataRow.rateBR * index * repaymentDateDuration
    return x.setScale(2, BigDecimal.ROUND_HALF_UP)
}

// вспомогательная функция для расчета графы 14
// «Графа 14» = «Графа 9» * 15% * («Графа 7» - «Графа 8») / 365(366);
BigDecimal getXByIncomeOnly(def dataRow, def repaymentDateDuration, BigDecimal index) {
    if (dataRow.income == null || repaymentDateDuration== null || index== null)
        return null
    x = dataRow.income * index * repaymentDateDuration
    return x.setScale(2, BigDecimal.ROUND_HALF_UP)
}

boolean isInPeriod(Date date) {
    if ( reportDate == null || date== null)
        return null
    return reportDate.after(date) && reportPeriodService.getStartDate(formData.reportPeriodId).getTime().before(date)
}

/**
 * @return массив имен ячеек, доступных для редактирования
 */
def getEditableColsNames() {
    return ['name', 'inn', 'number', 'date', 'cost', 'repaymentDate', 'concessionsDate', 'income',
            'currencyDebtObligation', 'rateBR', 'interestRate', 'marketPrice']
}

/**
 * Графы для которых надо вычислять итого и итого по эмитенту (графа 9, 10, 14, 15, 19 )
 */
def getTotalColumns() {
    return ['income', 'financialResult1', 'perc', 'loss', 'financialResultAdjustment']
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

/**
 * Получить новую стролу с заданными стилями.
 */
def getNewRow() {
    def newRow = formData.createDataRow()
    // графа 1..10
    getEditableColsNames().each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    return newRow
}

/**
 * Получение первого rowNumber по name
 * @param alias
 * @param data
 * @return
 */
def getRowNumber(def alias, def data) {
    for (def row : getRows(data)) {
        if (row.name == alias) {
            return row.rowNumber.toString()
        }
    }
}

/**
 * Устаносить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    ['name', 'inn', 'number', 'date', 'cost', 'repaymentDate', 'concessionsDate', 'income',
            'financialResult1', 'currencyDebtObligation', 'rateBR', 'interestRate', 'perc', 'loss',
            'marketPrice', 'financialResult2', 'maxLoss', 'financialResultAdjustment'].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
        row.getCell(it).editable = false
    }
}

/**
 * Проверка является ли строка итоговой.
 */
def isFixedRow(def row) {
    return row != null && row.getAlias() != null && row.getAlias().contains('total')
}

/**
 * Получить отчетную дату.
 */
def getReportDate() {
    def tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
    return (tmp ? tmp.getTime() + 1 : null)
}

/**
 * Отсорировать данные (по графе 2, 5, 4).
 */
void sort() {
    formDataService.getDataRowHelper(formData).getAllCached().sort({ DataRow a, DataRow b ->
        if (a.name == b.name && a.date == b.date) {
            return a.number <=> b.number
        }
        if (a.name == b.name) {
            return a.date <=> b.date
        }
        return a.name <=> b.name
    })
    recalculateNumbers()
}

/**
 * Получить итоговую строку с суммами.
 */
def getCalcTotalRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    // создаем строку
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.name = "Всего"

    // заполняем начальными данными-нулями
    for (column in totalColumns) {
        totalRow.getCell(column).value = new BigDecimal(0)
    }

    // ищем снизу вверх итоговую строку по эмитету
    for (int j = dataRowHelper.getAllCached().size() - 1; j >= 0; j--) {
        DataRow<Cell> srow = dataRowHelper.getAllCached().get(j)
        if ((srow.getAlias() == null)) {
            for (column in totalColumns) {
                if (srow.get(column) != null) {
                    totalRow.getCell(column).value = totalRow.getCell(column).value + (BigDecimal) srow.get(column)
                }
            }
        }
    }
    setTotalStyle(totalRow)
    return totalRow
}

/**
 * Посчитать все итоговые строки "Итого по <Наименование контрагента>"
 */
def getCalcTotalName() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def totalRows = [:]
    getRows(dataRowHelper).eachWithIndex { row, i ->
        if (!isFixedRow(row)) {
            DataRow<Cell> nextRow = getRow(i + 1)
            // если код расходы поменялся то создать новую строку "Итого по <Наименование контрагента>"
            if (nextRow?.name != row.name || i == (getRows(dataRowHelper).size() - 2)) {
                totalRows.put(i + 1, getCalcTotalNameRow(row))
            }
        }
    }
    return totalRows
}

/**
 * Посчитать строку "Итого по <Наименование контрагента>"
 */
def getCalcTotalNameRow(def row) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def tRow = getPrevRowWithoutAlias(row)
    def newRow = formData.createDataRow()
    newRow.setAlias('total' + getRowNumber(tRow.name, dataRowHelper))
    newRow.name = 'Итого по ' + tRow.name
    setTotalStyle(newRow)
    for (column in totalColumns) {
        newRow.getCell(column).value = new BigDecimal(0)
    }

    // идем от текущей позиции вверх и ищем нужные строки
    for (int j = dataRowHelper.getAllCached().indexOf(tRow); j >= 0; j--) {
        srow = getRow(j)

        if (srow.getAlias() == null) {
            if (getRow(j).name != tRow.name) {
                break
            }

            for (column in totalColumns) {
                if (srow.get(column) != null) {
                    newRow.getCell(column).value = newRow.getCell(column).value + (BigDecimal) srow.get(column)
                }
            }
        }
    }
    return newRow
}

/**
 * Удаление всех статическиех строк "Итого" из списка строк
 */
void deleteAllStatic() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    for (Iterator<DataRow> iter = dataRows.iterator() as Iterator<DataRow>; iter.hasNext();) {
        row = (DataRow) iter.next()
        if (row.getAlias() != null) {
            iter.remove()
            dataRowHelper.delete(row)
        }
    }
}

/**
 * Ищем вверх по форме первую строку без альяса
 */
DataRow getPrevRowWithoutAlias(DataRow row) {
    int pos = formDataService.getDataRowHelper(formData).getAllCached().indexOf(row)
    for (int i = pos; i >= 0; i--) {
        if (getRow(i).getAlias() == null) {
            return getRow(i)
        }
    }
    throw new IllegalArgumentException()
}

/**
 * Получение строки по номеру
 * @author ivildanov
 */
DataRow<Cell> getRow(int i) {
    def data = formDataService.getDataRowHelper(formData)
    if ((i < data.getAllCached().size()) && (i >= 0)) {
        return data.getAllCached().get(i)
    } else {
        return null
    }
}