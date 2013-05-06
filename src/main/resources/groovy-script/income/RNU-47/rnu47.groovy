/**
 * Скрипт для РНУ-47 (rnu47.groovy).
 * Форма "(РНУ-47) Регистр налогового учёта «ведомость начисленной амортизации по основным средствам,
 * а также расходов в виде капитальных вложений»".
 *
 * Версия ЧТЗ: 57
 * Вопросы аналитикам по ЧТЗ: http://jira.aplana.com/browse/SBRFACCTAX-2383
 *
 * TODO:
 *      -   убрать тестовое заполнение таблицы после отладки
 *      -   не понятно, откуда брать данные помесячно (вопрос аналитикам)
 *      -   после того, как аналитики ответят на вопросы и код будет дописан - все провреить!
 *
 * @author vsergeev
 *
 * Графы:  *
 * 1    amortGroup               -   Амортизационные группы
 * 2    norm                     -   величина норматива
 * 3    sumCurrentQuarterTotal   -   в текущем квартале всего
 * 4    sumCurrentQuarterNorm    -   в текущем квартале по нормативу
 * 5    sumTaxPeriodTotal        -   с начала налогового периода всего
 * 6    sumTaxPeriodNorm         -   с начала налогового периода по нормативу
 * 7    amortQuarter             -   в текущем квартале
 * 8    amortTaxPeriod           -   с начала налогового периода
 *
 */
void fillTestData() {       //todo (vsergeev) убрать!!!
    Random rnd = new Random()
    for (def dataRow : formData.dataRows) {
        for (def colName : dataRow.keySet()) {
            final cell = dataRow.getCell(colName)
            if (cell.isEditable()) {
                dataRow.put(colName, new BigDecimal(rnd.nextInt(500)))
            }
        }
    }
}

switch (formDataEvent) {
    case FormDataEvent.CHECK :
        fillTestData()      //todo (vsergeev) убрать!!!
        logicalCheck()
        break
    case FormDataEvent.CALCULATE :
        if (logicalCheck()) {
            fillTestData()      //todo (vsergeev) убрать!!!
            logicalCheck()
            calc()
        }
        break
    case FormDataEvent.ADD_ROW :
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW :
        deleteRow()
        break
}

/**
 * расчет значений ячеек, заполняющихся автоматически (строка ИТОГО)
 */
void calc(){
    def totalRow = formData.get('R11')
    def controlTotalValues = getTotalValues()
    controlTotalValues.each { key, value ->
        totalRow.put(key, value)
    }
}

/**
 * логические проверки (таблица 149)
 */
void logicalCheck(){
    //  Проверка на заполнение поля «<Наименование поля>»
    if (requiredColsFilled()){
        groupRowsCheck()
        totalRowCheck()
    }
}

/**
 * Проверка итоговых значений по амортизационным группам
 */
boolean totalRowCheck() {
    boolean isValid = true
    def totalRow = formData.get('R11')
    def controlTotalValues = getTotalValues()
    controlTotalValues.keySet().each { key, value ->
        if (! totalRow.get(key).equals(value)) {
            isValid = false
        }
    }
    if (! isValid) {
        logger.error('Итоговые значения рассчитаны неверно!')
    }

    return isValid
}

/**
 *  Проверка суммы расходов в виде капитальных вложений с начала года
 *  Проверка суммы начисленной амортизации с начала года
 */
boolean groupRowsCheck() {
    boolean isValid = true
    def groupRowsAliases = ['R0', 'R1', 'R2', 'R3', 'R4', 'R5', 'R6', 'R7', 'R8', 'R9', 'R10']
    for (def rowName : groupRowsAliases) {
        def row = formData.get(rowName)
        //2.		Проверка суммы расходов в виде капитальных вложений с начала года

        //2.1	графа 5 ? графа 3;
        final invalidCapitalForm = 'Неверная сумма расходов в виде капитальных вложений с начала года!'
        if (! row.sumTaxPeriodTotal >= row.sumCurrentQuarterTotal) {
            isValid = false
            logger.error(invalidCapitalForm)
        }
        //2.2	графа 5 = графа 3 + графа 5 за предыдущий месяц;
        // (если текущий отчетный период – январь, то слагаемое «по графе 5 за предыдущий месяц» в формуле считается равным «0.00»)
        if (! row.sumTaxPeriodTotal.equals(row.sumCurrentQuarterTotal + getSumTaxPeriodTotalFromPreviousMonth())) {
            isValid == false
            logger.error(invalidCapitalForm)
        }
        //2.3	графа 5 = ?графа 3 за все месяцы текущего года, начиная с января и включая текущий отчетный период.
        if (! row.sumTaxPeriodTotal.equals(getSumCurrentQuarterTotalForAllPeriods())) {
            isValid == false
            logger.error(invalidCapitalForm)
        }

        //3.    Проверка суммы начисленной амортизации с начала года
        final invalidAmortSumms = 'Неверная сумма начисленной амортизации с начала года!'
        //3.1.	графа 8 ? графа 7
        if (! row.amortTaxPeriod >= row.amortQuarter) {
            isValid = false
            logger.error(invalidAmortSumms)
        }
        //3.2   графа 8 = графа 7 + графа 8 за предыдущий месяц;
        //  (если текущий отчетный период – январь, то слагаемое «по графе 8 за предыдущий месяц» в формуле считается равным «0.00»)
        if (! row.amortTaxPeriod.equals(row.amortQuarter + getAmortTaxPeriodFromPreviousMonth())) {
            isValid = false;
            logger.error(invalidAmortSumms)
        }
        //3.3   графа 8 = ?графа 7 за все месяцы текущего года, начиная с января и включая текущий отчетный период.
        if (! row.amortTaxPeriod.equals(getAmortQuarterForAllPeriods())) {
            isValid = false
            logger.error(invalidAmortSumms)
        }
    }

    return isValid
}

/**
 * Проверяет
 * 1.  Обязательность заполнения поля графы (с 1 по 8) только для редактируемых ячеек
 * @return {@value true} если все обязательные поля заполнены, иначе {@value false}
 */
boolean requiredColsFilled() {
    def formIsValid = true

    for (def dataRow : formData.dataRows) {
        def parameterName = dataRow.amortGroup
        for (def colName : dataRow.keySet()) {
            final cell = dataRow.getCell(colName)
            if (cell.isEditable()) {
                if (isBlankOrNull(cell.getValue())){
                    logger.error("Поле \"$parameterName\" не заполнено!")
                    break
                }
            }
        }
    }

    return formIsValid
}

/**
 * возвращает значение графы 5 за предыдущий месяц
 * @return
 */
def getSumTaxPeriodTotalFromPreviousMonth() {
    return new BigDecimal(0)    //todo (vsergeev)  не понятно, откуда брать данные : http://jira.aplana.com/browse/SBRFACCTAX-2383
}

/**
 * возвращает значение графы 3 за все месяцы текущего года, включая текущий отчетный период
 * @return
 */
def getSumCurrentQuarterTotalForAllPeriods() {
    return new BigDecimal(0)    //todo (vsergeev)  не понятно, откуда брать данные : http://jira.aplana.com/browse/SBRFACCTAX-2383
}

/**
 * @return возвращает мапу с итоговыми суммами по графам (ключ - алиас графы, значение - итоговая сумма)
 */
def getTotalValues() {
    def groupRowsAliases = ['R0', 'R1', 'R2', 'R3', 'R4', 'R5', 'R6', 'R7', 'R8', 'R9', 'R10']
    def sumCurrentQuarterTotal = new BigDecimal(0)
    def sumCurrentQuarterNorm = new BigDecimal(0)
    def sumTaxPeriodTotal = new BigDecimal(0)
    def sumTaxPeriodNorm = new BigDecimal(0)
    def amortQuarter = new BigDecimal(0)
    def amortTaxPeriod = new BigDecimal(0)
    for (def colName : groupRowsAliases) {
        def row = fromData.get(colName)
        sumCurrentQuarterTotal += row.sumCurrentQuarterTotal
        sumCurrentQuarterNorm += row.sumCurrentQuarterNorm
        sumTaxPeriodTotal += row.sumTaxPeriodTotal
        sumTaxPeriodNorm += row.sumTaxPeriodNorm
        amortQuarter += row.amortQuarter
        amortTaxPeriod += row.amortTaxPeriod
    }

    return [
            'sumCurrentQuarterTotal': sumCurrentQuarterTotal,
            'sumCurrentQuarterNorm' : sumCurrentQuarterNorm,
            'sumTaxPeriodTotal': sumTaxPeriodTotal,
            'sumTaxPeriodNorm': sumTaxPeriodNorm,
            'amortQuarter': amortQuarter,
            'amortTaxPeriod': amortTaxPeriod
    ]
}

boolean isBlankOrNull(value) {
    value == null || value.equals('')
}

