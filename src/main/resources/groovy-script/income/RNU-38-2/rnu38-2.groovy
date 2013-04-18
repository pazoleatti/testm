/**
 * Скрипт для РНУ-38.2 (rnu38-2.groovy).
 * Форма "РНУ-38.2) Регистр налогового учёта начисленного процентного дохода по ОФЗ, по которым открыта короткая позиция. Отчёт 2".
 *
 * TODO:
 *      - уточнить получение данных за предыдущий период
 *
 * @author rtimerbaev
 */

switch (formDataEvent) {
    case FormDataEvent.CHECK :
    case FormDataEvent.CALCULATE :
        calc()
        logicalCheck()
        break
    case FormDataEvent.ADD_ROW :
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW :
        break
}

// графа 1  - amount
// графа 2  - incomePrev
// графа 3  - incomeShortPosition
// графа 4  - totalPercIncome

/**
 * Добавить новую строку.
 */
def addNewRow() {
    if (formData.dataRows.size == 0) {
        formData.appendDataRow()
    }
    setOrder()
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {
    /** (РНУ-38.1) Регистр налогового учёта начисленного процентного дохода по ОФЗ, по которым открыта короткая позиция. Отчёт 1. */
    def formDataRNU_38_1 = FormDataService.find(334, FormDataKind.PRIMARY, formDataDepartment.id, formData.reportPeriodId)
    if (formDataRNU_38_1 == null) {
        /*
          // TODO (Ramil Timerbaev) костыль, потом убать
          formData.dataRows.each { row ->
              row.amount = 1
              row.incomePrev = 1
              row.incomeShortPosition = 1
              row.totalPercIncome = row.incomePrev + row.incomeShortPosition
          }
          // конец
          */
        return
    }

    def totalRow = formDataRNU_38_1.getDataRow('total')

    /*
      * Расчеты.
      */

    formData.dataRows.each { row ->
        // графа 1
        row.amount = totalRow.amount

        // графа 2
        row.incomePrev = totalRow.incomePrev

        // графа 3
        row.incomeShortPosition = totalRow.incomeShortPosition

        // графа 4
        row.totalPercIncome = row.incomePrev + row.incomeShortPosition
    }
}

/**
 * Логические проверки.
 */
void logicalCheck() {
    /*
	 * Проверка объязательных полей.
	 */
    formData.dataRows.each { row ->
        def colNames = []
        // Список проверяемых столбцов (графа 1..4)
        ['amount', 'incomePrev', 'incomeShortPosition', 'totalPercIncome'].each {
            if (row.getCell(it).getValue() == null) {
                colNames.add('"' + row.getCell(it).getColumn().getName() + '"')
            }
        }
        if (!colNames.isEmpty()) {
            def errorMsg = colNames.join(', ')
            def index = row.getOrder()
            logger.error("В строке $index не заполнены колонки : $errorMsg.")
        }
    }
}

/*
 * Вспомогательные методы.
 */

/**
 * Поправить значания order.
 */
void setOrder() {
    formData.dataRows.eachWithIndex { row, index ->
        row.setOrder(index + 1)
    }
}