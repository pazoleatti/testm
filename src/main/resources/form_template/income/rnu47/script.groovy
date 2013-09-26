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
 * 2    amortGroup               -   Амортизационные группы
 * 3    sumCurrentPeriodTotal    -   За отчётный месяц
 * 4    sumTaxPeriodTotal        -   С начала налогового периода
 * 5    amortPeriod              -   За отчётный месяц
 * 6    amortTaxPeriod           -   С начала налогового периода
 *
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkCreation()
        break
    case FormDataEvent.CHECK :
        def rnu46FormData = getRnu46FormData()
        if (rnu46FormData==null) {
            logger.error("Отсутствуют данные РНУ-46!")
            return
        }
        logicalCheck()
        break
    case FormDataEvent.CALCULATE :
        def rnu46FormData = getRnu46FormData()
        if (rnu46FormData==null) {
            logger.error("Отсутствуют данные РНУ-46!")
            return
        }
        calc()
        !hasError() && logicalCheck()
        break
    case FormDataEvent.ADD_ROW :
        break
    case FormDataEvent.DELETE_ROW :
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
 * Получить данные за формы "(РНУ-46) Регистр налогового учёта «карточка по учёту основных средств и капитальных вложений в неотделимые улучшения арендованного и полученного по договору безвозмездного пользования имущества»"
 */
def getRnu46FormData(){
    def formData46 = formDataService.find(342, formData.kind, formDataDepartment.id, formData.reportPeriodId)
    if (formData46!=null) {
        return getData(formData46)
    }
    return null
}

/**
 * расчет значений ячеек, заполняющихся автоматически
 */
void calc(){
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = getRows(dataRowHelper)

    // расчет для первых 11 строк
    def row1_11 = calcRows1_11()
    // расчет для строк 12-13
    def totalValues = getTotalValues()

    dataRows.eachWithIndex{row, index->
        if (index<11) {
            row1_11[index].each{k,v->
                row[k] = v
            }
        } else if (index==12 || index==13) {
            row.cost10perMonth = totalValues[index].cost10perMonth
            row.cost10perTaxPeriod = totalValues[index].cost10perTaxPeriod
        }
    }
    save(dataRowHelper)
}

/**
 * расчет строк 1-11
 */
void calcRows1_11(){
    def rnu46FormData = getRnu46FormData()
    def rnu46Rows = getRows(rnu46FormData)
    def groupList = 0..10
    def value = [:]
    groupList.each{group ->
        value[group] = calc3_6(rnu46Rows, group)
    }
}

/**
 * расчет строк 12-13
 */
def getTotalValues(){
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = getRows(dataRowHelper)
    def group12 = ['R1', 'R2', 'R8', 'R9', 'R10']
    def group13 = ['R3', 'R4', 'R5', 'R6', 'R7']
    def value = [:]

    // расчет для строк 12-13
    dataRows.eachWithIndex{row, index->
        if (group12.contains(row.getAlias())) {
            value[12].sumCurrentPeriodTotal = (value[12].sumCurrentPeriodTotal?:new BigDecimal(0))+row.sumCurrentPeriodTotal
            value[12].sumTaxPeriodTotal = (value[12].sumCurrentPeriodTotal?:new BigDecimal(0))+row.sumCurrentPeriodTotal
        } else if (group13.contains(row.getAlias())) {
            value[13].sumCurrentPeriodTotal = (value[13].sumCurrentPeriodTotal?:new BigDecimal(0))+row.sumCurrentPeriodTotal
            value[13].sumTaxPeriodTotal = (value[13].sumTaxPeriodTotal?:new BigDecimal(0))+row.sumTaxPeriodTotal
        }
    }
    return value
}
/**
 * расчет столбцов 3-6 для строк 1-11
 */
def calc3_6(def rows, def group) {
    def value = [
                    sumCurrentPeriodTotal: 0,
                    sumTaxPeriodTotal: 0,
                    amortPeriod: 0,
                    amortTaxPeriod: 0
                ]
    rows.each{row ->
        if (refBookService.getNumberValue(71,row.amortGroup,'GROUP')==group) {
            value.sumCurrentPeriodTotal += row.cost10perMonth
            value.sumTaxPeriodTotal += row.cost10perTaxPeriod
            value.amortPeriod += row.amortMonth
            value.amortTaxPeriod += row.amortTaxPeriod
        }
    }
    return value
}

/**
 * логические проверки (таблица 149)
 */
void logicalCheck(){
    //  Проверка на заполнение поля «<Наименование поля>»
    if (true){//requiredColsFilled()){
        groupRowsCheck()
        totalRowCheck()
    }
}

/**
 * Проверка итоговых значений по амортизационным группам
 */
boolean totalRowCheck() {
    boolean isValid = true

    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = getRows(dataRowHelper)

    def totalValues = getTotalValues()
    dataRows.eachWithIndex{row, index->
        if ( (index==12 || index==13) &&
             (row.cost10perMonth==totalValues[index].cost10perMonth &&
              row.cost10perTaxPeriod==totalValues[index].cost10perTaxPeriod) ) isValid = false
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
    def formDataOld = getFormDataOld()

    for (def rowName : groupRowsAliases) {
        def row = formData.get(rowName)
        //2.		Проверка суммы расходов в виде капитальных вложений с начала года

        //2.1	графа 4 ? графа 3;
        final invalidCapitalForm = 'Неверная сумма расходов в виде капитальных вложений с начала года!'
        if (! row.sumTaxPeriodTotal >= row.sumCurrentPeriodTotal) {
            isValid = false
            logger.error(invalidCapitalForm)
        } else
        //2.2	графа 4 = графа 3 + графа  за предыдущий месяц;
        // (если текущий отчетный период – январь, то слагаемое «по графе 4 за предыдущий месяц» в формуле считается равным «0.00»)
        if (! row.sumTaxPeriodTotal.equals(row.sumCurrentPeriodTotal + getSumTaxPeriodTotalFromPreviousMonth())) {
            isValid = false
            logger.error(invalidCapitalForm)
        } else
        //2.3	графа 4 = ?графа 3 за все месяцы текущего года, начиная с января и включая текущий отчетный период.
        if (! row.sumTaxPeriodTotal.equals(getSumCurrentQuarterTotalForAllPeriods())) {
            isValid = false
            logger.error(invalidCapitalForm)
        }

        //3.    Проверка суммы начисленной амортизации с начала года
        final invalidAmortSumms = 'Неверная сумма начисленной амортизации с начала года!'
        //3.1.	графа 6 ? графа 5
        if (! row.amortTaxPeriod >= row.amortPeriod) {
            isValid = false
            logger.error(invalidAmortSumms)
        } else
        //3.2   графа 6 = графа 5 + графа 6 за предыдущий месяц;
        //  (если текущий отчетный период – январь, то слагаемое «по графе 6 за предыдущий месяц» в формуле считается равным «0.00»)
        if (! row.amortTaxPeriod.equals(row.amortPeriod + getAmortTaxPeriodFromPreviousMonth())) {
            isValid = false;
            logger.error(invalidAmortSumms)
        } else
        //3.3   графа 6 = ?графа 5 за все месяцы текущего года, начиная с января и включая текущий отчетный период.
        if (! row.amortTaxPeriod.equals(getAmortQuarterForAllPeriods())) {
            isValid = false
            logger.error(invalidAmortSumms)
        }
    }

    return isValid
}

/**
 * Получить данные за предыдущий отчетный период
 */
def getFormDataOld() {
    // предыдущий отчётный период
    def reportPeriodOld = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)

    // РНУ-47 за предыдущие отчетные периоды
    def formDataOld = null
    if (reportPeriodOld != null) {
        formDataOld = formDataService.find(formData.formType.id, formData.kind, formDataDepartment.id, reportPeriodOld.id)
    }

    return formDataOld
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

boolean isBlankOrNull(value) {
    value == null || value.equals('')
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
 * Получить строку по алиасу.
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
 * Имеются ли фатальные ошибки.
 */
def hasError() {
    return logger.containsLevel(LogLevel.ERROR)
}