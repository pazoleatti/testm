import groovy.time.TimeCategory

import java.text.DateFormat
import java.text.SimpleDateFormat

/**
 * Скрипт для РНУ-70 (rnu70.groovy).
 * (РНУ-70) Регистр налогового учёта уступки права требования до наступления предусмотренного кредитным договором
 * срока погашения основного долга
 *
 * Версия ЧТЗ: 57
 *
 * вопросы аналитикам: http://jira.aplana.com/browse/SBRFACCTAX-2488
 *
 * TODO:
 *          -   узнать, как определить дату конца отчетного периода
 *          -   следить за SBRFACCTAX-2376 и добавить проверку кодов валют!
 *
 * @author vsergeev
 *
 * Графы:
 * 1    rowNumber                -      № пп
 * 2    debtor                   -      Должник
 * 3    cost                     -      Стоимость права требования, (руб.)
 * 4    repaymentDate            -      Дата погашения основного долга
 * 5    concessionsDate          -      Дата уступки права требования
 * 6    income                   -      Доход (выручка) от уступки права требования (руб.)
 * 7    financialResult          -      Финансовый результат уступки права требования (руб.)
 * 8    currencyDebtObligation   -      Валюта долгового обязательства
 * 9    rateBR                   -      Ставка Банка России
 * 10   interestRate             -      Ставка процента, установленная соглашением сторон
 * 11   perc                     -      Проценты по долговому обязательству, рассчитанные с учётом ст. 269 НК РФ за
 *                                      период от даты уступки права требования до даты платежа по договору
 * 12   loss                     -      Убыток, превышающий проценты по долговому обязательству, рассчитанные с учётом
 *                                      ст. 269 НК РФ за период от даты уступки  права требования до даты платежа по
 *                                      договору
 */
switch (formDataEvent) {
    case FormDataEvent.CHECK :
        logicalCheck()
        break
    case FormDataEvent.CALCULATE :
        if (logicalCheck(false)) {
            calc()
        }
        break
    case FormDataEvent.ADD_ROW :
        addNewRowAction()
        break
    case FormDataEvent.DELETE_ROW :
        deleteRow()
        break
}

/**
 * действие при нажатии на кнопку "добавить строку"
 */
void addNewRowAction() {
    def totalAmountRow = tryFindTotalAmountRow()
    if (totalAmountRow != null) {                               //добавляем новую строку перед ИТОГО
        def tmpValues = [                                       //запоминаем существующие значения
                'cost' : totalAmountRow.cost,
                'income' : totalAmountRow.income,
                'financialResult' : totalAmountRow.financialResult,
                'perc' : totalAmountRow.perc,
                'loss' : totalAmountRow.loss
        ]
        formData.deleteDataRow(totalAmountRow)
        addNewRowAction()
        def newTotalAmountRow = addNewRow(true)
        newTotalAmountRow.cost = tmpValues.cost                 //подставляем старые значения
        newTotalAmountRow.income = tmpValues.income
        newTotalAmountRow.financialResult = tmpValues.financialResult
        newTotalAmountRow.perc = tmpValues.perc
        newTotalAmountRow.loss = tmpValues.loss
    } else {                                                    //добавляем новую строку в конец таблицы
        def newRow = addNewRow()
        newRow.number = formData.dataRows.size()
    }
}

/**
 * добавляем новую строку непосредственно в таблицу
 * @param isTotalAmountRow = {@value true когда нужно получить строку итогов}
 * @return добавленный DataRow
 */
private def addNewRow(boolean isTotalAmountRow = false) {
    def newRow
    if (! isTotalAmountRow) {                       //полуаем просто новую строку, ячейки разрешены для редактирования
        newRow = formData.appendDataRow()
        def editableColsNames = getEditableColsNames()
        editableColsNames.each{ value ->
            newRow.getCell(value).editable = true
        }
    } else {                                        //получаем строку для ИТОГО
        newRow = formData.appendDataRow('total')
        newRow.debtor = 'Итого'
    }

    return newRow
}

/**
 * Алгоритмы заполнения полей формы
 */
void calc() {
    for (def dataRow : formData.getDataRows()) {
        // графа 7
        dataRow.financialResult = getFinancialResult(dataRow)
        dataRow.rateBR = getRateBR(dataRow)
        dataRow.perc = getPerc(dataRow)
        dataRow.loss = getLoss(dataRow)
    }
}

/**
 * Логические проверки
 */
boolean logicalCheck() {
    boolean isValid = true

    if (requiredColsFilled()){
        checkRowsData()
        nsiCheck()
    }
}

/**
 * Проверки, не связанные с обязательностью заполнения полей
 */
boolean checkRowsData() {
    boolean isValid = true

    for (def dataRow : formData.getDataRows()) {

        //  Проверка даты погашения основного долга «графа 4» >= «графа 5»
        //  Проверка даты совершения операции «графа 5» <= дата окончания отчётного периода
        if (! dataRow.repaymentDate.before(dataRow.concessionsDate) || isBeforePeriodEnd(dataRow.concessionsDate)) {
            isValid = false
            logger.error('Неверно указана дата погашения основного долга')
        }
        //  Проверка финансового результата	графа 7
        if (! dataRow.financialResult.equals(getFinancialResult(dataRow))) {
            isValid = false
            logger.error('Неверно рассчитана графа 7')      //todo (vsergeev) в ЧТЗ две одинаковые проверки с разными сообщениями. которое брать? в вопросах аналитикам
        }

        if (getFinancialResult < 0) {
            if (! Math.abs(dataRow.financialResult).equals(dataRow.perc + dataRow.loss)) {
                isValid = false
                logger.error('Неверно рассчитаны проценты и убыток от уступки права требования')
            }
        } else {
            if (! dataRow.perc == 0 || ! dataRow.loss == 0) {
                isValid = false
                logger.error('При уступке с прибылью проценты и убыток не рассчитываются')
            }
        }

        if (! dataRow.rateBR.equals(getRateBR(dataRow))) {
            isValid = false
            logger.error('Неверно рассчитана графа 9')
        }

        if (! dataRow.perc.equals(getPerc(dataRow))) {
            isValid = false
            logger.error('Неверно рассчитана графа 11')
        }

        if (! dataRow.loss.equals(getLoss(dataRow))){
            isValid = false
            logger.error('Неверно рассчитана графа 12')
        }

        if (! isinPeriod(dataRow.concessionsDate)) {
            isValid = false
            logger.error('Графа 5 не принадлежит отчетному периоду')
        }

    }

    //todo (vsergeev) добавить проверку итоговых значений!

    return isValid
}

/**
 * Проверка на заполнение полей
 * Обязательность заполнения поля графы 1-8, 11-12
 */
boolean requiredColsFilled() {
    boolean isValid = true
    def requiredColsNames = ['rowNumber', 'debtor', 'cost', 'repaymentDate', 'concessionsDate', 'income',
            'financialResult', 'currencyDebtObligation', 'perc', 'loss']
    for (def dataRow : formData.getDataRows()) {
        if (! dataRow.getAlias().equals('total')) {
            def fieldNumber = row.get('number')
            for (def colName : requiredColsNames) {
                if (isBlankOrNull(dataRow.get(colName))) {
                    isValid = false
                    logger.error("Поле $fieldNumber не заполнено!")
                    break
                }
            }
        }
    }

    return isValid
}

/**
 * вычисляем значение графы 7
 */
def getFinancialResult(def dataRow) {
    return dataRow.income - dataRow.cost
}

/**
 * вычисляем значение для графы 9
 */
def getRateBR(def dataRow) {
    if (getFinancialResult(dataRow) < 0) {
        if (isRoublel()) {
            //todo (vsergeev) в вопросах аналитикам уточнить про формат ввода в графу 9 и про то, откуда брать установленную Банком России ставку рефинансирования, действующую на дату, указанную в «графе 5»
        }
    } else {
        return null
    }
}

/**
 * вычисляем значение для графы 11
 */
BigDecimal getPerc(def dataRow) {
    if (getFinancialResult(dataRow) < 0) {
        final DateFormat dateFormat = new SimpleDateFormat('dd.MM.yyyy')        //todo (vsergeev) попробовать вынести форматтер и даты в константы
        final firstJan2010 = dateFormat.parse('01.01.2010')
        final thirtyFirstDec2013 = dateFormat.parse('31.12.2013')
        final thirtyJun2010 = dateFormat.parse('30.06.2010')
        final firstJan2011 = dateFormat.parse('01.01.2011')

        BigDecimal x

        final repaymentDateDuration = getRepaymentDateDuration(dataRow)
        if (isRoublel()) {
            x = getXByRateBR(dataRow, repaymentDateDuration, 1.1)
            if (dataRow.concessionsDate.after(firstJan2010) && dataRow.concessionsDate.before(thirtyFirstDec2013)) {
                if (dataRow.rateBR * 1.8 <= dataRow.interestRate) {
                    x = getXByRateBR(dataRow, repaymentDateDuration, 1.1)
                } else {
                    x = getXByInterestRate(dataRow, repaymentDateDuration)
                }
            }
            if (dataRow.concessionsDate.after(firstJan2010) && dataRow.concessionsDate.before(thirtyJun2010)) {       //todo (vsergeev) примечания k217 и k255 ЧТЗ (одинаковые) - дополнить условия
                x = getXByRateBR(dataRow, repaymentDateDuration, 2)
            }
        } else {
            x = getXByIncomeOnly(dataRow, repaymentDateDuration, 0.15)      //todo (vsergeev) в вопросах аналитикам узнать про проценты (15% как писать в формуле)
            if (dataRow.concessionsDate.after(firstJan2011) && dataRow.concessionsDate.before(thirtyFirstDec2013)) {
                x = getXByRateBR(dataRow, repaymentDateDuration, 0.8)
            }
        }

        if (Math.abs(x) > Math.abs(dataRow.financialResult)) {
            return Math.abs(dataRow.financialResult)
        } else {
            return x
        }
    } else {
        return new BigDecimal(0)
    }
}

/**
 * вычисляем значение для графы 12
 */
def getLoss(def DataRow) {
    if (getFinancialResult(DataRow) < 0) {
        return Math.abs(dataRow.financialResult) - dataRow.perc
    } else {
        return dataRow.loss = new BigDecimal(0)
    }
}

boolean isBlankOrNull(value) {
    value == null || value.equals('')
}

boolean isBeforePeriodEnd(def date) {
    return true     //todo (vsergeev) узнать, как определить дату конца отчетного периода
}

boolean nsiCheck() {
    //todo (vsergeev) следить за SBRFACCTAX-2376 и добавить проверку кодов валют!
    return true
}

boolean isRoublel() {
    return true     //todo (vsergeev) следить за SBRFACCTAX-2376 и добавить проверку рубля это или нет!
}

def getRepaymentDateDuration(dataRow) {
    return TimeCategory(dataRow.repaymentDate, dataRow.concessionsDate).days / getCountOfDaysInReportYear()    //(«графа 4» - «графа 5»
}

int getCountOfDaysInReportYear() {
    def reportPeriod = ReportPeriodService.get(formData.getReportPeriodId())
    def taxPeriod = TaxPeriodService.get(reportPeriod.getTaxPeriodId())
    Calendar calendar = Calendar.getInstance()
    calendar.setTime(taxPeriod.getStartDate())
    final year = calendar.get(Calendar.YEAR)

    return (year % 4 == 0 && year % 400 != 0 ) ? 366 : 365
}

BigDecimal getXByInterestRate(def dataRow, def repaymentDateDuration) {
    x2 = dataRow.income * dataRow.interestRate * repaymentDateDuration
    return x2.setScale(2, BigDecimal.ROUND_HALF_UP)
}

BigDecimal getXByRateBR(def dataRow, def repaymentDateDuration, int index) {
    x = dataRow.income * dataRow.rateBR * index * repaymentDateDuration
    return x.setScale(2, BigDecimal.ROUND_HALF_UP)
}

BigDecimal getXByIncomeOnly(def dataRow, def repaymentDateDuration, int index) {
    x = dataRow.income * 0.15 * repaymentDateDuration
    return x.setScale(2, BigDecimal.ROUND_HALF_UP)
}

boolean isInPeriod(Date date) {
    return true     //todo (vsergeev) узнать, как определить даты отчетного периода!
}

def tryFindTotalAmountRow() {
    return formData.dataRows.find {
        isTotalRow(it)
    }
}

boolean isTotalRow(dataRow) {
    dataRow.getAlias().equals('total')
}

/**
 * @return массив имен ячеек, доступных для редактирования
 */
def getEditableColsNames() {
    return ['debtor', 'cost', 'repaymentDate', 'concessionsDate', 'income']
}