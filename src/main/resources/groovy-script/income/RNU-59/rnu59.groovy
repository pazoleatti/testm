/**
 * РНУ-59
 * @author auldanov
 * @version 55
 *
 * Столбцы
 * 1. Номер сделки первая часть / вторая часть - tradeNumber
 * 2. Наименование ценной бумаги - securityName
 * 3. Код валюты - currencyCode
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
import java.util.GregorianCalendar

// дата начала отчетного периода
Calendar periodStartDate = reportPeriodService.getStartDate(formData.reportPeriodId)
Calendar periodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId)
// количество дней в году
def countDaysOfYear = (new GregorianCalendar()).isLeapYear(periodStartDate.get(Calendar.YEAR)) ? 365 : 366
// отчетная дата
Calendar reportingDate = periodEndDate
reportingDate.set(Calendar.DATE, reportingDate.get(Calendar.DATE) + 1)

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
        //NCICheck()
        break
// Инициирование Пользователем создания формы
    case FormDataEvent.CREATE:
        //1.	Проверка наличия и статуса формы, консолидирующей данные текущей налоговой формы, при создании формы.
        //2.	Логические проверки значений налоговой.
        //3.	Проверки соответствия НСИ.
        break
// Инициирование Пользователем перехода «Подготовить»
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:
        //1.	Проверка наличия и статуса формы, консолидирующей данные текущей налоговой формы, при переходе в статус «Подготовлена».
        //2.	Логические проверки значений налоговой формы.
        logicalCheck()
        //3.	Проверки соответствия НСИ.
        break
// Инициирование Пользователем  выполнение перехода «Утвердить»
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:
        //1.	Проверка наличия и статуса формы, консолидирующей данные текущей налоговой формы, при переходе в статус «Утверждена».
        //2.	Логические проверки значений налоговой формы.
        //3.	Проверки соответствия НСИ.
        break
// Инициирование Пользователем  выполнение перехода «Принять»
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED:
        //1.	Проверка наличия и статуса формы, консолидирующей данные текущей налоговой формы, при переходе в статус «Принята».
        //2.	Логические проверки значений налоговой формы.
        //3.	Проверки соответствия НСИ.
        break
// Инициирование Пользователем выполнения перехода «Отменить принятие»
    case FormDataEvent.MOVE_ACCEPTED_TO_APPROVED:
        //1.	Проверка наличия и статуса формы, консолидирующей данные текущей налоговой формы, при переходе «Отменить принятие».
        //2.	Логические проверки значений налоговой формы.
        //3.	Проверки соответствия НСИ.
        break

// Событие добавить строку
    case FormDataEvent.ADD_ROW:
        addNewRow()
        break

// событие удалить строку
    case FormDataEvent.DELETE_ROW:
        break

    case FormDataEvent.CALCULATE:
        logicalCheck()
        fillForm()
        break
// после принятия из подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED :
        logicalCheck()
        break
// обобщить
    case FormDataEvent.COMPOSE :
        consolidation()
        logicalCheck()
        fillForm()
        break
}


/**
 * Добавление новой строки
 */
def addNewRow(){
    def newRow = formData.createDataRow()
    formData.dataRows.add(formData.dataRows.size() > 0 ? formData.dataRows.size() - 1 : 0, newRow )

    // Графы 1-10 Заполняется вручную
    ['tradeNumber', 'securityName', 'currencyCode', 'nominalPrice', 'part1REPODate', 'part2REPODate', 'acquisitionPrice', 'salePrice', 'income', 'outcome'].each{ column ->
        newRow.getCell(column).setEditable(true)
    }
}

/**
 * Удаление строки
 */
def deleteRow(){
    def row = (DataRow)additionalParameter
    if (!(row.getAlias() in ['totalByCode', 'total'])){
        // удаление строки
        formData.deleteDataRow(row)
    }

    // пересчет номеров строк таблицы
    def i = 1;
    formData.dataRows.each{rowItem->
        rowItem.rowNumber = i++
    }
}

/**
 * Заполнение полей формы
 * 6.40.2.3	Алгоритмы заполнения полей формы
 */
def fillForm(){

    // удаляем строку итого
    def iterator = formData.dataRows.iterator()
    while (iterator.hasNext()){
        def row = iterator.next()
        if (row.getAlias() == "total"){
            iterator.remove()
        }
    }

    // строка для Итого
    def newRow = formData.createDataRow()
    newRow.alias = "total"
    newRow.securityName = "Итого"

    // проставим 0ми
    ['nominalPrice', 'acquisitionPrice', 'salePrice', 'income', 'outcome', 'outcome269st', 'outcomeTax'].each{ alias ->
        newRow[alias] = 0
    }

    formData.dataRows.each{ row ->
        /**
         * Табл. 199 Алгоритмы заполнения полей формы «Регистр налогового учёта закрытых сделок РЕПО с обязательством продажи по 2-й части»
         */

        // графа 9, 10
        // A=«графа8» - «графа7»
        def a = row.salePrice - row.acquisitionPrice
        // B=ОКРУГЛ(A;2),
        def b = a.setScale(2, BigDecimal.ROUND_HALF_UP)
        // C= ОКРУГЛ(ABS(A);2),
        def c = a.abs().setScale(2, BigDecimal.ROUND_HALF_UP)

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
        row.rateBR = calculateColumn11(row)

        // графа 12
        row.outcome269st = calculateColumn12(row)

        // Графа 13
        row.outcomeTax = calculateColumn13(row)

        // экономим на итерациях, подсчитаем сумму для граф 7-10, 12-13
        newRow.acquisitionPrice += row.acquisitionPrice ?:0
        newRow.salePrice += row.salePrice ?:0
        newRow.income += row.income ?:0
        newRow.outcome += row.outcome ?:0
        newRow.outcome269st += row.outcome269st ?:0
        newRow.outcomeTax += row.outcomeTax ?:0

    }

    // вставка строки итого
    formData.dataRows.add(newRow)
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

    formData.dataRows.each{ row ->
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
        if (row.salePrice -  row.acquisitionPrice > 0 && !(row.salePrice -  row.acquisitionPrice == row.income)){
            logger.warn('Неверно определены доходы')
        }

        // «графа 10» =|«графа 8» - «графа 7»|, при условии («графа 8» - «графа 7») < 0.  = Неверно определены расходы
        if ((row.salePrice -  row.acquisitionPrice) < 0 && !(row.outcome == (row.salePrice -  row.acquisitionPrice).abs())){
            logger.warn('Неверно определены расходы')
        }

        // Арифметическая проверка графы 11
        def col11 = calculateColumn11(row)
        if (col11 != null && col11 != row.rateBR){
            logger.error('Неверно рассчитана графа «Ставка Банка России (%)»!')
        }

        // Арифметическая проверка графы 12
        def col12 = calculateColumn12(row)
        if (col12 != null && col12 != row.outcome269st){
            logger.error('Неверно рассчитана графа «Расходы по сделке РЕПО, рассчитанные с учётом ст. 269 НК РФ (руб.коп.)»!')
        }

        // Арифметическая проверка графы 13
        def col13 = calculateColumn13(row)
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

    // Проверка итоговых значений по всей форме
    def totalRow = formData.getDataRow("total")
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

/**
 * Проверка является ли строка итововой (любой итоговой, т.е. по коду, либо основной)
 */
def isTotalRow(row){
    row.getAlias()=='total'
}

/**
 * Метод возвращает значение для графы 11
 * Логика выделена в отдельный метод так как
 * логика используется при расчетах и при логических проверкат
 * @param row
 */
def calculateColumn11(DataRow row){
    // Если «графа 10» = 0, то « графа 11» не заполняется; && Если «графа 3» не заполнена, то « графа 11» не заполняется
    if (!isTotalRow(row) && row.outcome != 0 && row.currencyCode != null){
        // Если «графа 3» = 810, то «графа 11» = ставка рефинансирования Банка России из справочника «Ставки рефинансирования ЦБ РФ» на дату «графа 6»,
        if (row.currencyCode == 810)    {
            // TODO справочника «Ставки рефинансирования ЦБ РФ» еще нет
            return '';
        } else{ // Если «графа 3» ≠ 810), то
            // Если «графа 6» принадлежит периоду с 01.09.2008 по 31.12.2009 (включительно), то «графа 11» = 22;
            if (row.part2REPODate.compareTo(date01092008) >= 0 && compareTo(date31122009) <= 0){
                return 22
            } else if (row.part2REPODate.compareTo(date01012011) >= 0 && compareTo(date31122012) <= 0){
                // Если «графа 6» принадлежит периоду с 01.01.2011 по 31.12.2012 (включительно), то
                // графа 11 = ставка рефинансирования Банка России из справочника «Ставки рефинансирования ЦБ РФ»  на дату «графа 6»;
                // TODO справочник не готов
                return ''
            } else{
                //Если  «графа 6» не принадлежит отчётным периодам с 01.09.2008 по 31.12.2009 (включительно), с 01.01.2011 по 31.12.2012 (включительно)),
                //то  «графа 11» = 15.
                return 15
            }
        }
    }
}

/**
 * Метод возвращает значение для графы 12
 * Логика выделена в отдельный метод так как
 * логика используется при расчетах и при логических проверкат
 * @param row
 */
def calculateColumn12(DataRow row){
    // Если «графа 10» > 0 И«графа 3» = 810, то:
    if (row.outcome > 0 && row.currencyCode == 810){
        if (row.part2REPODate.compareTo(date01092008) >= 0 && row.part2REPODate.compareTo(date31122009) <=0){
            // 1.	Если «графа 6» принадлежит периоду с 01.09.2008 по 31.12.2009, то:
            // «графа 12» = («графа 7» × «графа 11» × 1,5) × ((«графа 6» - «графа 5») / 365 (366)) / 100;
            return (row.acquisitionPrice * rateBR * 1.5) * ((row.part2REPODate - row.part1REPODate) / countDaysOfYear) / 100
        } else if (row.part2REPODate.compareTo(date01012010) >= 0 && row.part2REPODate.compareTo(date30062010) <=0 && row.part1REPODate.compareTo(date01112009) <= 0){
            // 2.	Если «графа 6» принадлежит периоду с 01.01.2010 по 30.06.2010 И «графа 5» < 01.11.2009, то:
            // «графа 12» = («графа 7» × «графа 11» × 2) × ((«графа 6» - «графа 5») / 365 (366)) / 100;
            return (row.acquisitionPrice * rateBR * 2) * ((row.part2REPODate - row.part1REPODate) / countDaysOfYear) / 100
        } else if (row.part2REPODate.compareTo(date01012010) >= 0 && row.part2REPODate.compareTo(date31122012)){
            // 3.	Если «графа 6» принадлежит периоду с 01.01.2010 по 31.12.2012, то:
            // «графа 12» = («графа 7» × «графа 11» × 1,8) × ((«графа6» - «графа5») / 365(366)) / 100.
            return (row.acquisitionPrice * rateBR * 1.1) * ((row.part2REPODate - row.part1REPODate) / countDaysOfYear) / 100
        } else{
            // 4.	Иначе:
            //«графа 12» = («графа 7» × «графа 11» × 1,1) х ((«графа 6» - «графа 5») / 365 (366)) / 100;
            return (row.acquisitionPrice * rateBR * 1.1) * ((row.part2REPODate - row.part1REPODate) / countDaysOfYear) / 100
        }
    } else if (row.outcome != null && row.outcome > 0 && row.currencyCode != 810){ // Если «графа 10» > 0 И «графа 3» ≠ 810, то:
        if (row.part2REPODate.compareTo(date01012011) >= 0 && row.part2REPODate.compareTo(date31122012)){
            //Если «графа 6» принадлежит периоду с 01.01.2011 по 31.12.2012, то:
            // «графа 12» = («графа 7» × «графа 11» × 0,8) × ((«графа 6» - «графа 5») / 365 (366)) / 100.
            // При этом, если «графа 6» = «графе 5», то («графа 6» - «графа 5») =1
            def diff65 = row.part2REPODate - row.part1REPODate
            diff65 = diff65 == 0 ? 1:diff65
            return (row.acquisitionPrice?:0 * row.rateBR?:0 * 0.8) * (diff65 / countDaysOfYear) / 100
        } else {
            // Иначе
            // «графа 12» = («графа 7» × «графа 11») × ((«графа 6» - «графа 5») / 365 (366)) / 100;
            return (row.acquisitionPrice?:0 * rateBR?:0) * ((row.part2REPODate?:0 - row.part1REPODate?:0) / countDaysOfYear) / 100
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
            // 2.	Если «графа 10» > «графа 12», то: «графа 13» = «графа 12»
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
    formData.dataRows.clear()

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = FormDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                source.getDataRows().each { row->
                    if (row.getAlias() == null || row.getAlias() == '') {
                        formData.dataRows.add(row)
                    }
                }
            }
        }
    }
    logger.info('Формирование консолидированной формы прошло успешно.')
}


