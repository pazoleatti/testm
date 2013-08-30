/**
 * РНУ-59
 * @author auldanov
 * @version 55
 *
 * Столбцы
 * 1. Номер сделки первая часть / вторая часть - tradeNumber
 * 2. Наименование ценной бумаги - securityName
 * 3. Код валюты - currencyCode Справочник
 * 4. Номинальная стоимость ценных бумаг (ед. вал.) - nominalPrice
 * 5. Дата первой части РЕПО - part1REPODate
 * 6. Дата второй части РЕПО - part2REPODate
 * 7. Стоимость приобретения, в т.ч. НКД, по первой части РЕПО (руб.коп.) - acquisitionPrice
 * 8. Стоимость реализации, в т.ч. НКД, по второй части РЕПО (руб.коп.) - salePrice
 * 9. Доходы (+) по сделке РЕПО (руб.коп.) - income
 * 10. Расходы (-) по сделке РЕПО (руб.коп.) - outcome
 * 11. Ставка Банка России (%) - rateBR
 * 12. Расходы по сделке РЕПО, рассчитанные с учётом ст. 269 НК РФ (руб.коп.) - outcome269st
 * 13. Расходы по сделке РЕПО, учитываемые для целей налогообложения (руб.коп.) - outcomeTax
 */

import java.text.SimpleDateFormat
import java.util.GregorianCalendar

// дата начала отчетного периода
Calendar getPeriodStartDate(){
    return reportPeriodService.getStartDate(formData.reportPeriodId)
}
Calendar getPeriodEndDate(){
    return reportPeriodService.getEndDate(formData.reportPeriodId)
}
// количество дней в году
def countDaysOfYear = (new GregorianCalendar()).isLeapYear(periodStartDate.get(Calendar.YEAR)) ? 365 : 366
// отчетная дата
Calendar getReportingDate(){
    def Calendar endDate = periodEndDate
    endDate.set(Calendar.DATE, endDate.get(Calendar.DATE) + 1)
    return endDate
}

/**
 * Выполнение действий по событиям
 *
 */
switch (formDataEvent){
// Инициирование Пользователем проверки данных формы в статусе «Создана», «Подготовлена», «Утверждена», «Принята»
    case FormDataEvent.CHECK:
        //1. Логические проверки значений налоговой формы
        logicalCheck()
        //2. Проверки соответствия НСИ
        checkNSI()
        break
// Инициирование Пользователем создания формы
    case FormDataEvent.CREATE:
        //1.    Проверка наличия и статуса формы, консолидирующей данные текущей налоговой формы, при создании формы.
        checkCreation()
        //2.    Логические проверки значений налоговой.
        //3.    Проверки соответствия НСИ.
        break
// Инициирование Пользователем перехода «Подготовить»
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:
        //1.    Проверка наличия и статуса формы, консолидирующей данные текущей налоговой формы, при переходе в статус «Подготовлена».
        //2.    Логические проверки значений налоговой формы.
        logicalCheck()
        //3.    Проверки соответствия НСИ.
        checkNSI()
        break
// Инициирование Пользователем  выполнение перехода «Утвердить»
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:
        //1.    Проверка наличия и статуса формы, консолидирующей данные текущей налоговой формы, при переходе в статус «Утверждена».
        //2.    Логические проверки значений налоговой формы.
        logicalCheck()
        //3.    Проверки соответствия НСИ.
        checkNSI()
        break
// Инициирование Пользователем  выполнение перехода «Принять»
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED:
        //1.    Проверка наличия и статуса формы, консолидирующей данные текущей налоговой формы, при переходе в статус «Принята».
        //2.    Логические проверки значений налоговой формы.
        logicalCheck()
        //3.    Проверки соответствия НСИ.
        checkNSI()
        break
// Инициирование Пользователем выполнения перехода «Отменить принятие»
    case FormDataEvent.MOVE_ACCEPTED_TO_APPROVED:
        //1.    Проверка наличия и статуса формы, консолидирующей данные текущей налоговой формы, при переходе «Отменить принятие».
        //2.    Логические проверки значений налоговой формы.
        logicalCheck()
        //3.    Проверки соответствия НСИ.
        checkNSI()
        break

// Событие добавить строку
    case FormDataEvent.ADD_ROW:
        addNewRow()
        break

// событие удалить строку
    case FormDataEvent.DELETE_ROW:
        deleteRow()
        break

    case FormDataEvent.CALCULATE:
        fillForm()
        logicalCheck()
        break
// после принятия из подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED :
        logicalCheck()
        break
// обобщить
    case FormDataEvent.COMPOSE :
        consolidation()
        fillForm()
        logicalCheck()
        // для сохранения изменений приемников
        getData(formData).commit()
        break
    // загрузить
    case FormDataEvent.IMPORT :
        importData()
        break
}



/**
 * Добавление новой строки
 */
def addNewRow(){
    def newRow = formData.createDataRow()
    def data = getData(formData)

    // Графы 1-10 Заполняется вручную
    ['tradeNumber', 'securityName', 'currencyCode', 'nominalPrice', 'part1REPODate', 'part2REPODate', 'acquisitionPrice', 'salePrice'].each{ column ->
        newRow.getCell(column).setEditable(true)
        newRow.getCell(column).setStyleAlias('Редактируемая')
    }
    def index = 0
    if (currentDataRow!=null){
        index = currentDataRow.getIndex()
        def row = currentDataRow
        while(row.getAlias()!=null && index>0){
            row = getRows(data).get(--index)
        }
        if(index!=currentDataRow.getIndex() && getRows(data).get(index).getAlias()==null){
            index++
        }
    }else if (getRows(data).size()>0) {
        for(int i = getRows(data).size()-1;i>=0;i--){
            def row = getRows(data).get(i)
            if(row.getAlias()==null){
                index = getRows(data).indexOf(row)+1
                break
            }
        }
    }
    data.insert(newRow,index+1)
}

/**
 * Удаление строки
 */
def deleteRow(){
    //def row = (DataRow)additionalParameter
    def row = currentDataRow
    if (!(row.getAlias() in ['totalByCode', 'total'])){
        // удаление строки
        getData(formData).delete(row)
    }

// пересчет номеров строк таблицы
//    def i = 1;
//    getData(formData).getAllCached().each{rowItem->
//        rowItem.rowNumber = i++
//    }
}

/**
 * Проверки соответствия НСИ.
 */
def checkNSI() {
    def data = getData(formData)
    if (!getRows(data).isEmpty()) {

        for (DataRow row : getRows(data)) {
            if (isTotalRow(row)) {
                continue
            }

            // 1. Проверка кода валюты со справочным (графа 3)
            if (row.currencyCode!=null && getCurrency(row.currencyCode)==null) {
                logger.warn('Неверный код валюты!')
            }

            // 2. Проверка соответствия ставки рефинансирования ЦБ (графа 11) коду валюты (графа 3)
            def col11 = roundTo2(calculateColumn11(row, row.part2REPODate))
            if (col11!=null && col11!=row.rateBR) {
                logger.error('Неверно указана ставка Банка России!')
                return false
            }
        }
    }
    return true
}

/**
 * Заполнение полей формы
 * 6.40.2.3 Алгоритмы заполнения полей формы
 */
def fillForm(){

    def data = getData(formData)
    // удаляем строку итого
    for(def i=0;i<data.getAllCached().size();i++){
        def row = data.getAllCached().get(i)
        if (row.getAlias() == "total") {
            data.delete(row)
        }
    }

    // строка для Итого
    def newRow = formData.createDataRow()
    newRow.alias = "total"
    newRow.securityName = "Итого"
    setTotalStyle(newRow)

    // проставим 0ми
    ['nominalPrice', 'acquisitionPrice', 'salePrice', 'income', 'outcome', 'outcome269st', 'outcomeTax'].each{ alias ->
        newRow[alias] = 0
    }

    data.getAllCached().each{ DataRow row ->
        /**
         * Табл. 199 Алгоритмы заполнения полей формы «Регистр налогового учёта закрытых сделок РЕПО с обязательством продажи по 2-й части»
         */

        // графа 9, 10
        // A=«графа8» - «графа7»
        BigDecimal a = (row.salePrice?:0) - (row.acquisitionPrice?:0)
        // B=ОКРУГЛ(A;2),
        BigDecimal b = roundTo2(a)
        // C= ОКРУГЛ(ABS(A);2),
        BigDecimal c = roundTo2(a.abs())
        /**
        *    Если  .A>0, то
             «графа 9» = B
             «графа 10» = 0
             Иначе Если  A<0
             «графа 9» = 0
             «графа 10» = С
             Иначе
             «графа 9»= «графа 10» = 0
         */
        if (a.compareTo(0) > 0){
            row.income = b
            row.outcome = 0
        } else if (a.compareTo(0) < 0){
            row.income = 0
            row.outcome = c
        }   else{
            row.income = 0
            row.outcome = 0
        }


        // Графа 11
        row.rateBR = roundTo2(calculateColumn11(row,row.part2REPODate))

        // графа 12
        row.outcome269st = roundTo2(calculateColumn12(row))

        // Графа 13
        row.outcomeTax = roundTo2(calculateColumn13(row))

        // экономим на итерациях, подсчитаем сумму для граф 7-10, 12-13
        newRow.nominalPrice += row.nominalPrice ?:0
        newRow.acquisitionPrice += row.acquisitionPrice ?:0
        newRow.salePrice += row.salePrice ?:0
        newRow.income += row.income ?:0
        newRow.outcome += row.outcome ?:0
        newRow.outcome269st += row.outcome269st ?:0
        newRow.outcomeTax += row.outcomeTax ?:0
    }

    data.save(data.getAllCached())

    if (data.getAllCached().size()>0) {
// вставка строки итого
        data.insert(newRow, data.getAllCached().size()+1);
    }
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
 * 6.40.2.4.1 Логические проверки
 */
def logicalCheck(){

    def nominalPrice = 0
    def acquisitionPrice = 0
    def salePrice = 0
    def income = 0
    def outcome = 0
    def outcome269st = 0
    def outcomeTax = 0

    def data = getData(formData)
    data.getAllCached().each{ row ->
        if (!isTotalRow(row)) {
            // Обязательность заполнения поля графы 12 и 13. Текст ошибки - Поле “Наименование поля” не заполнено!
            ['outcome269st', 'outcomeTax'].each{ alias ->
                if (row[alias] == null){
                    logger.error('Поле '+row.getCell(alias).getColumn().getName()+' не заполнено!')
                }
            }

            // графа 5 заполнена и «графа 5» ≤ «отчётная дата». Текст ошибки - Неверно указана дата первой части сделки! SBRFACCTAX-2575
            if (!(row.part1REPODate != null && (row.part1REPODate.compareTo(reportingDate.getTime())  <= 0))){
                logger.error('Неверно указана дата первой части сделки!')
            }

            // графа 6 заполнена и графа 6 в рамках отчётного периода. Текст ошибки - Неверно указана дата второй части сделки!
            if (!(row.part2REPODate != null && (row.part2REPODate.compareTo(periodStartDate.getTime()) >=0 && row.part2REPODate.compareTo(periodEndDate.getTime()) <=0))){
                logger.error('Неверно указана дата второй части сделки!')
            }

            // если«графа 9» = 0 ИЛИ  «графа 10» = 0. = Задвоение финансового результата!
            if (!(row.income == 0 || row.outcome == 0)){
                logger.error("Задвоение финансового результата!")
            }

            // если «графа 10» = 0, то «графа 12» = 0 и «графа 13» = 0
            if (row.outcome == 0 && !(row.outcome269st == 0 && row.outcomeTax == 0)){
                logger.error("Задвоение финансового результата!")
            }

            //  «графа 9» = «графа 8» - «графа 7», при условии («графа 8» - «графа 7») > 0. = Неверно определены доходы
            def price = row.salePrice?:0
            def acqPrice = row.acquisitionPrice?:0
            if (price - acqPrice > 0 && !(price - acqPrice == row.income)){
                logger.warn('Неверно определены доходы')
            }

            // «графа 10» =|«графа 8» - «графа 7»|, при условии («графа 8» - «графа 7») < 0.  = Неверно определены расходы
            if ((price - acqPrice) < 0 && !(row.outcome == (price - acqPrice).abs())){
                logger.warn('Неверно определены расходы')
            }

            // Арифметическая проверка графы 11
            def col11 = roundTo2(calculateColumn11(row,row.part2REPODate))
            if (col11 != null && col11 != row.rateBR){
                logger.error('Неверно рассчитана графа «Ставка Банка России (%%)»!')
            }

            // Арифметическая проверка графы 12
            def col12 = roundTo2(calculateColumn12(row))
            if (col12 != null && col12 != row.outcome269st){
                logger.error('Неверно рассчитана графа «Расходы по сделке РЕПО, рассчитанные с учётом ст. 269 НК РФ (руб.коп.)»!')
            }

            // Арифметическая проверка графы 13
            def col13 = roundTo2(calculateColumn13(row))
            if (col13 != null && col13 != row.outcomeTax){
                logger.error('Неверно рассчитана графа «Расходы по сделке РЕПО, учитываемые для целей налогообложения (руб.коп.)»!')
            }
            // экономим на итерациях, подсчитаем сумму для граф 4,7-10, 12-13, суммы нужны для проверок
            nominalPrice += row.nominalPrice ?:0
            acquisitionPrice += row.acquisitionPrice ?:0
            salePrice += row.salePrice ?:0
            income += row.income ?:0
            outcome += row.outcome ?:0
            outcome269st += row.outcome269st ?:0
            outcomeTax += row.outcomeTax ?:0
        }
    }

    // Проверка итоговых значений по всей форме
    for(def dataRow:data.getAllCached()){
        if (isTotalRow(dataRow)){
            def totalRow = data.getDataRow(data.getAllCached(),"total")
            if (totalRow != null && totalRow.nominalPrice != nominalPrice ||
                    totalRow.acquisitionPrice != acquisitionPrice ||
                    totalRow.salePrice != salePrice ||
                    totalRow.income != income ||
                    totalRow.outcome != outcome ||
                    totalRow.outcome269st != outcome269st ||
                    totalRow.outcomeTax != outcomeTax){
                logger.error('Итоговые значения рассчитаны неверно!')
            }
        }
    }

}

/**
 * Проверка является ли строка итоговой (любой итоговой, т.е. по коду, либо основной)
 */
def isTotalRow(row){
    row.getAlias()=='total'
}

/**
 * Метод возвращает значение для графы 11
 * Логика выделена в отдельный метод так как
 * логика используется при расчетах и при логических проверкат
 * @param row
 * @param rateDate
 */
def calculateColumn11(DataRow row, def rateDate){
    def currency = getCurrency(row.currencyCode)
    def rate = getRate(rateDate)
    // Если «графа 10» = 0, то « графа 11» не заполняется; && Если «графа 3» не заполнена, то « графа 11» не заполняется
    if (!isTotalRow(row) && row.outcome != 0 && row.currencyCode != null){
        // Если «графа 3» = 810, то «графа 11» = ставка рефинансирования Банка России из справочника «Ставки рефинансирования ЦБ РФ» на дату «графа 6»,
        if (currency == '810')    {
            return rate
        } else{ // Если «графа 3» ≠ 810), то
            // Если «графа 6» принадлежит периоду с 01.09.2008 по 31.12.2009 (включительно), то «графа 11» = 22;
            if (inPeriod(rateDate, '01.09.2008', '31.12.2009')){
                return 22
            } else if (inPeriod(rateDate, '01.01.2011', '31.12.2012')){
                // Если «графа 6» принадлежит периоду с 01.01.2011 по 31.12.2012 (включительно), то
                // графа 11 = ставка рефинансирования Банка России из справочника «Ставки рефинансирования ЦБ РФ»  на дату «графа 6»;
                return rate
            } else{
                //Если  «графа 6» не принадлежит отчётным периодам с 01.09.2008 по 31.12.2009 (включительно), с 01.01.2011 по 31.12.2012 (включительно)),
                //то  «графа 11» = 15.
                return 15
            }
        }
    }
}

/**
 * Количество дней в году за который делаем
 * @return
 */
int getCountDaysOfYear() {
    Calendar periodStartDate = reportPeriodService.getStartDate(formData.reportPeriodId)
    return countDaysOfYear = (new GregorianCalendar()).isLeapYear(periodStartDate.get(Calendar.YEAR)) ? 365 : 366
}

/**
 * Метод возвращает значение для графы 12
 * Логика выделена в отдельный метод так как
 * логика используется при расчетах и при логических проверкат
 * @param row
 */
def calculateColumn12(DataRow row){
    Date date01_09_2008 = new Date(1220227200000)       // 1220227200000 - 01.09.2008 00:00 GMT
    Date date31_12_2009 = new Date(1262217600000)       // 1262217600000 - 31.12.2009 00:00 GMT
    Date date01_01_2011 = new Date(1293840000000)       // 1293840000000 - 01.01.2011 00:00 GMT
    Date date31_12_2012 = new Date(1356912000000)       // 1356912000000 - 31.12.2012 00:00 GMT
    Date date01_01_2010 = new Date(1262282400000)       // 1262282400000 - 01.01.2010 00:00 GMT
    Date date30_06_2010 = new Date(1277834400000)       // 1277834400000 - 30.06.2010 00:00 GMT
    Date date01_11_2009 = new Date(1257012000000)       // 1257012000000 - 01.11.2009 00:00 GMT
    // Если «графа 10» > 0 И«графа 3» = 810, то:
    if (row.outcome > 0 && getCurrency(row.currencyCode) == '810'){
        if (row.part2REPODate.compareTo(date01_09_2008) >= 0 && row.part2REPODate.compareTo(date31_12_2009) <=0){
            // 1.   Если «графа 6» принадлежит периоду с 01.09.2008 по 31.12.2009, то:
            // «графа 12» = («графа 7» × «графа 11» × 1,5) × ((«графа 6» - «графа 5») / 365 (366)) / 100;
            return (row.acquisitionPrice * row.rateBR * 1.5) * ((row.part2REPODate - row.part1REPODate) / countDaysOfYear) / 100
        } else if (row.part2REPODate.compareTo(date01_01_2010) >= 0 && row.part2REPODate.compareTo(date30_06_2010) <=0 && row.part1REPODate.compareTo(date01_11_2009) <= 0){
            // 2.   Если «графа 6» принадлежит периоду с 01.01.2010 по 30.06.2010 И «графа 5» < 01.11.2009, то:
            // «графа 12» = («графа 7» × «графа 11» × 2) × ((«графа 6» - «графа 5») / 365 (366)) / 100;
            return (row.acquisitionPrice * row.rateBR * 2) * ((row.part2REPODate - row.part1REPODate) / countDaysOfYear) / 100
        } else if (row.part2REPODate.compareTo(date01_01_2010) >= 0 && row.part2REPODate.compareTo(date31_12_2012)){
            // 3.   Если «графа 6» принадлежит периоду с 01.01.2010 по 31.12.2012, то:
            // «графа 12» = («графа 7» × «графа 11» × 1,8) × ((«графа6» - «графа5») / 365(366)) / 100.
            return (row.acquisitionPrice * row.rateBR * 1.1) * ((row.part2REPODate - row.part1REPODate) / countDaysOfYear) / 100
        } else{
            // 4.   Иначе:
            //«графа 12» = («графа 7» × «графа 11» × 1,1) х ((«графа 6» - «графа 5») / 365 (366)) / 100;
            return (row.acquisitionPrice * row.rateBR * 1.1) * ((row.part2REPODate - row.part1REPODate) / countDaysOfYear) / 100
        }
    } else if (row.outcome != null && row.outcome > 0 && getCurrency(row.currencyCode) != '810'){ // Если «графа 10» > 0 И «графа 3» ≠ 810, то:
        if (row.part2REPODate.compareTo(date01_01_2011) >= 0 && row.part2REPODate.compareTo(date31_12_2012)){
            //Если «графа 6» принадлежит периоду с 01.01.2011 по 31.12.2012, то:
            // «графа 12» = («графа 7» × «графа 11» × 0,8) × ((«графа 6» - «графа 5») / 365 (366)) / 100.
            // При этом, если «графа 6» = «графе 5», то («графа 6» - «графа 5») =1
            def diff65 = row.part2REPODate - row.part1REPODate
            diff65 = diff65 == 0 ? 1:diff65
            return (row.acquisitionPrice?:0 * row.rateBR?:0 * 0.8) * (diff65 / countDaysOfYear) / 100
        } else {
            // Иначе
            // «графа 12» = («графа 7» × «графа 11») × ((«графа 6» - «графа 5») / 365 (366)) / 100;
            return (row.acquisitionPrice?:0 * row.rateBR?:0) * ((row.part2REPODate?:0 - row.part1REPODate?:0) / countDaysOfYear) / 100
        }
    } else if (row.outcome == 0){
        //  Если «графа 10» = 0, то «графа 12» = 0
        return 0
    }
}

/**
 * Метод возвращает значение для графы 13
 * Логика выделена в отдельный метод так как
 * логика используется при расчетах и при логических проверкат
 * @param row
 */
def calculateColumn13(DataRow row){
    if (row.outcome > 0){
        // Если «графа 10» > 0, то:
        if (row.outcome <= row.rateBR){
            // Если «графа 10» ≤ «графа 12», то:  «графа 13» = «графа 10»
            row.outcomeTax = row.outcome
        }else{
            // 2.   Если «графа 10» > «графа 12», то: «графа 13» = «графа 12»
            row.outcomeTax = row.rateBR
        }
    }else if (row.outcome == 0){
        // Если «графа 10» = 0, то «графа 13» = 0
        row.rateBR = 0
    }
}

/**
 * Консолидация.
 */
void consolidation() {
    // удалить все строки и собрать из источников их строки
    getData(formData).clear()

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                getData(source).getAllCached().each { row->
                    if (row.getAlias() == null || row.getAlias() == '') {
                        data.insert(row, getRows(data).size+1);
                    }
                }
            }
        }
    }
    logger.info('Формирование консолидированной формы прошло успешно.')
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
 * Получить строки формы.
 *
 * @param formData форма
 */
def getRows(def data) {
    return data.getAllCached()
}

/**
 * Хелпер для округления чисел
 * @param value
 * @return
 */
BigDecimal roundTo2(BigDecimal value) {
    if (value != null) {
        return value.setScale(2, BigDecimal.ROUND_HALF_UP)
    } else {
        return value
    }
}

/**
 * Проверить попадает ли указанная дата в период
 */
def inPeriod(def date, def from, to) {
    if (date == null) {
        return false
    }
    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    def dateFrom = format.parse(from)
    def dateTo = format.parse(to)
    return (dateFrom < date && date <= dateTo)
}

/**
 * Получить ставку рефинансирования ЦБ РФ
 * @param date
 */
def getRate(def date) {
    if (date!=null) {
        def refDataProvider = refBookFactory.getDataProvider(23)
        def res = refDataProvider.getRecords(date, null, null, null);
        return res.getRecords().get(0).RATE.getNumberValue()
    } else {
        return null;
    }
}

/**
 * Получить цифровой код валюты
*/
def getCurrency(def currencyCode) {
    return refBookService.getStringValue(15,currencyCode,'CODE')

}

/**
 * Устаносить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    ['tradeNumber', 'securityName', 'currencyCode', 'nominalPrice',
            'part1REPODate', 'part2REPODate', 'acquisitionPrice', 'salePrice',
            'income', 'outcome', 'rateBR', 'outcome269st', 'outcomeTax'].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
}

/**
 * Получение импортируемых данных.
 * Транспортный файл формата xml.
 */
void importData() {
    def fileName = (UploadFileName ? UploadFileName.toLowerCase() : null)
    if (fileName == null || fileName == '' || !fileName.contains('.xml')) {
        return
    }

    def is = ImportInputStream
    if (is == null) {
        return
    }

    def xmlString = importService.getData(is, fileName)
    if (xmlString == null || xmlString == '') {
        return
    }

    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null) {
        return
    }

    // сохранить начальное состояние формы
    def data = getData(formData)
    def rowsOld = getRows(data)
    try {
        // добавить данные в форму
        addData(xml)

        // расчитать и проверить
        if (!logger.containsLevel(LogLevel.ERROR)) {
            fillForm()
            logicalCheck()
            checkNSI()
        }
    } catch(Exception e) {
        logger.error('Во время загрузки данных произошла ошибка! ' + e.toString())
    }
    // откатить загрузку если есть ошибки
    if (logger.containsLevel(LogLevel.ERROR)) {
        data.clear()
        data.insert(rowsOld, 1)
    } else {
        logger.info('Данные загружены')
    }
    data.commit()
}

/**
 * Заполнить форму данными.
 *
 * @param xml данные
 */
void addData(def xml) {
    def tmp
    def newRows = []
    def index
    def refDataProvider = refBookFactory.getDataProvider(15)

    for (def row : xml.exemplar.table.detail.record) {
        index = 0

        def newRow = getNewRow()

        // графа 1
        newRow.tradeNumber = row.field[index].@value.text()
        index++

        // графа 2
        newRow.securityName = row.field[index].@value.text()
        index++

        // графа 3 - справочник 15 "Общероссийский классификатор валют"
        tmp = null
        if (row.field[index].@value.text() != null &&
                row.field[index].@value.text().trim() != '') {
            def records = refDataProvider.getRecords(new Date(), null, "CODE = '" + row.field[index].@value.text() + "'", null);
            if (records != null && !records.getRecords().isEmpty()) {
                tmp = records.getRecords().get(0).get('record_id').getNumberValue()
            }
        }
        newRow.currencyCode = tmp
        index++

        // графа 4
        newRow.nominalPrice = getNumber(row.field[index].@value.text())
        index++

        // графа 5
        newRow.part1REPODate = getDate(row.field[index].@value.text())
        index++

        // графа 6
        newRow.part2REPODate = getDate(row.field[index].@value.text())
        index++

        // графа 7
        newRow.acquisitionPrice = getNumber(row.field[index].@value.text())
        index++

        // графа 8
        newRow.salePrice = getNumber(row.field[index].@value.text())
        index++

        // графа 9
        newRow.income = getNumber(row.field[index].@value.text())
        index++

        // графа 10
        newRow.outcome = getNumber(row.field[index].@value.text())
        index++

        // графа 11
        newRow.rateBR= getNumber(row.field[index].@value.text())
        index++

        // графа 12
        newRow.outcome269st = getNumber(row.field[index].@value.text())
        index++

        // графа 13
        newRow.outcomeTax = getNumber(row.field[index].@value.text())

        newRows.add(newRow)
    }
    // проверка итоговых данных
    if (xml.exemplar.table.total.record.field.size() > 0 && !newRows.isEmpty()) {
        def totalRow = formData.createDataRow()

        totalRow.nominalPrice = 0
        totalRow.acquisitionPrice = 0
        totalRow.salePrice = 0
        totalRow.income = 0
        totalRow.outcome = 0
        totalRow.outcome269st = 0
        totalRow.outcomeTax = 0

        newRows.each { row ->
            totalRow.nominalPrice += (row.nominalPrice != null ? row.nominalPrice : 0)
            totalRow.acquisitionPrice += (row.acquisitionPrice != null ? row.acquisitionPrice : 0)
            totalRow.salePrice += (row.salePrice != null ? row.salePrice : 0)
            totalRow.income += (row.income != null ? row.income : 0)
            totalRow.outcome += (row.outcome != null ? row.outcome : 0)
            totalRow.outcome269st += (row.outcome269st != null ? row.outcome269st : 0)
            totalRow.outcomeTax += (row.outcomeTax != null ? row.outcomeTax : 0)
        }

        for (def row : xml.exemplar.table.total.record) {
            // графа 4, 7, 8, 9, 10, 12, 13
            if (totalRow.nominalPrice != getNumber(row.field[3].@value.text()) ||
                    totalRow.acquisitionPrice != getNumber(row.field[6].@value.text()) ||
                    totalRow.salePrice!= getNumber(row.field[7].@value.text()) ||
                    totalRow.income != getNumber(row.field[8].@value.text()) ||
                    totalRow.outcome != getNumber(row.field[9].@value.text()) ||
                    totalRow.outcome269st != getNumber(row.field[11].@value.text()) ||
                    totalRow.outcomeTax != getNumber(row.field[12].@value.text())
            ) {
                logger.error('Итоговые значения неправильные.')
                return
            }
        }
    }
    def data = getData(formData)
    data.clear()
    data.insert(newRows, 1)
    data.commit()
}

/**
 * Получить числовое значение.
 *
 * @param value строка
 */
def getNumber(def value) {
    if (value == null) {
        return null
    }
    def tmp = value.trim()
    if ("".equals(tmp)) {
        return null
    }
    // поменять запятую на точку и убрать пробелы
    tmp = tmp.replaceAll(',', '.').replaceAll('[^\\d.,-]+', '')
    return new BigDecimal(tmp)
}

/**
 * Получить дату по строковому представлению (формата дд.ММ.гггг)
 */
def getDate(def value) {
    if (isEmpty(value)) {
        return null
    }
    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    return format.parse(value)
}

/**
 * Проверка пустое ли значение.
 */
def isEmpty(def value) {
    return value == null || value == '' || value == 0
}

/**
 * Получить новую строку с заданными стилями.
 */
def getNewRow() {
    def row = formData.createDataRow()

    // Графы 1-10 Заполняется вручную
    ['tradeNumber', 'securityName', 'currencyCode', 'nominalPrice', 'part1REPODate', 'part2REPODate', 'acquisitionPrice', 'salePrice'].each{ column ->
        row.getCell(column).setEditable(true)
        row.getCell(column).setStyleAlias('Редактируемая')
    }
    return row
}