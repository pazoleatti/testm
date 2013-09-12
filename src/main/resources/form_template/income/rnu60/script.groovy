package form_template.income.rnu60

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.Column
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel

import java.text.SimpleDateFormat

/**
 * 6.41 (РНУ-60) Регистр налогового учёта закрытых сделок РЕПО с обязательством покупки по 2-й части
 * ЧТЗ http://conf.aplana.com/pages/viewpage.action?pageId=8588102 ЧТЗ_сводные_НФ_Ф2_Э1_т2.doc
 * @author ekuvshinov
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkCreation()
        break
    case FormDataEvent.CHECK:
        allCheck()
        break
    case FormDataEvent.CALCULATE:
        deleteAllStatic()
        sort()
        calc()
        addAllStatic()
        allCheck()
        break
    case FormDataEvent.ADD_ROW:
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW:
        deleteRow()
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
        allCheck()
        break
    // после принятия из подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED :
        allCheck()
        break
    // обобщить
    case FormDataEvent.COMPOSE :
        consolidation()
        deleteAllStatic()
        sort()
        calc()
        addAllStatic()
        if (allCheck()) {
            // для сохранения изменений приемников
            getData(formData).commit()
        }
        break
    case FormDataEvent.IMPORT :
        importData()
        break
}

/**

 № графы    ALIAS               Наименование поля                                                           Тип поля        Формат
 1.         tradeNumber         Номер сделки первая часть / вторая часть                                    Строка /41/
 2.         securityName        Наименование ценной бумаги                                                  Строка /255/
 3.         currencyCode        Код валюты                                                                  Строка /3/                      Должно содержать значение поля «Код валюты. Цифровой» справочника «Общероссийский классификатор валют»          Нет Да
 4.         nominalPrice        Номинальная стоимость ценных бумаг (ед. вал.)                               Число/17.2/
 5.         part1REPODate       Дата первой части РЕПО                                                      Дата            ДД.ММ.ГГГ
 6.         part2REPODate       Дата второй части РЕПО                                                      Дата            ДД.ММ.ГГГ
 7.         acquisitionPrice    Стоимость реализации, в т.ч. НКД, по первой части РЕПО (руб.коп.)           Число/17.2/
 8.         salePrice           Стоимость приобретения, в т.ч. НКД, по второй части РЕПО (руб.коп.)         Число/17.2/
 9.         income              Доходы (-) по сделке РЕПО (руб.коп.)                                        Число/17.2/
 10.        outcome             Расходы (+) по сделке РЕПО (руб.коп.)                                       Число/17.2/
 11.        rateBR              Ставка Банка России (%)                                                     Число/17.2/
 12.        outcome269st        Расходы по сделке РЕПО, рассчитанные с учётом ст. 269 НК РФ (руб.коп.)      Число/17.2/
 13.        outcomeTax          Расходы по сделке РЕПО, учитываемые для целей налогообложения (руб.коп.)    Число/17.2/

 */

def allCheck() {
    return !hasError() && logicalCheck() && checkNSI()
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
 * 6.41.3.2.1   Логические проверки
 * Табл. 209 Логические проверки формы «Регистр налогового учёта закрытых сделок РЕПО с обязательством покупки по 2-й части»
 */
def logicalCheck() {
    def data = getData(formData)
    for (row in getRows(data)) {
        if (row.getAlias() == null) {

            def index = row.tradeNumber
            def errorMsg
            if (index!=null && index!='') {
                errorMsg = "В строке \"Номер сделки\" равной $index "
            } else {
                index = row.getIndex()
                errorMsg = "В строке $index "
            }

            // 1. Проверка на заполнение поля «<Наименование поля>»
            if (!checkRequiredColumns(row,['outcome269st', 'outcomeTax'])){
                return false
            }
            // 2. Проверка даты первой части РЕПО
            if (row.part1REPODate != null && reportDate.time.before((Date) row.part1REPODate)) {
                logger.error(errorMsg + "неверно указана дата первой части сделки!")
                return false
            }
            // 3. Проверка даты второй части РЕПО
            if (row.part2REPODate != null
                    && (reportPeriodService.getStartDate(formData.reportPeriodId).time.after((Date) row.part2REPODate) || reportPeriodService.getEndDate(formData.reportPeriodId).time.before((Date) row.part2REPODate)
            )) {
                logger.error(errorMsg + "неверно указана дата второй части сделки!")
                return false
            }

            // 4. Проверка финансового  результата на основе http://jira.aplana.com/browse/SBRFACCTAX-2870
            if (row.outcome > 0 && row.income > 0) {
                logger.error(errorMsg + "задвоение финансового результата!")
                return false
            }

            // 6. Проверка финансового результата
            if (row.outcome == 0 && !(row.outcome269st == 0 && row.outcomeTax == 0)) {
                logger.error(errorMsg + "задвоение финансового результата!")
                return false
            }

            // 7. Проверка финансового результата
            BigDecimal temp = (row.salePrice ?: 0) - (row.acquisitionPrice ?: 0)
            if (temp < 0 && !(temp.abs() == row.income)) {
                logger.warn(errorMsg + "неверно определены доходы")
            }

            // 8. Проверка финансового результата
            if (temp > 0 && !(temp == row.outcome)) {
                logger.warn(errorMsg + "неверно определены расходы")
            }

            // 9. Арифметические проверки граф 9, 10, 11, 12, 13
            List checks = ['income', 'outcome', 'rateBR', 'outcome269st', 'outcomeTax']
            Map<String, BigDecimal> value = [:]
            value.put('income', calc9(row))
            value.put('outcome', calc10(row))
            value.put('rateBR', calc11(row,row.part2REPODate))
            value.put('outcome269st', calc12(row))
            value.put('outcomeTax', calc13(row))
            for (String check in checks) {
                if (row.getCell(check).value != value.get(check)) {
                    logger.error(errorMsg + "неверно рассчитана графа " + row.getCell(check).column.name.replace('%', '%%') + "!")
                    return false
                }
            }
        }
    }
    // 10. Проверка итоговых значений по всей форме
    List itogoSum = ['nominalPrice', 'acquisitionPrice', 'salePrice', 'income', 'outcome', 'outcome269st', 'outcomeTax']
    DataRow realItogo = getRealItogo()
    if (realItogo!=null) {
        DataRow itogo = getItogo()
        for (String alias in itogoSum) {
            if (realItogo.getCell(alias).value != itogo.getCell(alias).value) {
                logger.error("Итоговые значения рассчитаны неверно!")
                return false
            }
        }
    }
    return true
}

/**
 * Проверить заполненность обязательных полей.
 *
 * @param row строка
 * @param columns список обязательных графов
 * @return true - все хорошо, false - есть незаполненные поля
 */
def checkRequiredColumns(def row, def columns) {
    def colNames = []

    columns.each {
        if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
            def name = getColumnName(row, it)
            colNames.add('"' + name + '"')
        }
    }
    if (!colNames.isEmpty()) {
        def index = row.tradeNumber
        def errorMsg = colNames.join(', ')
        if (index!=null && index!='') {
            logger.error("В строке \"Номер сделки\" равной $index не заполнены колонки : $errorMsg.")
        } else {
            index = row.getIndex()
            logger.error("В строке $index не заполнены колонки : $errorMsg.")
        }
        return false
    }
    return true
}

/**
 * Получить название графы по псевдониму.
 *
 * @param row строка
 * @param alias псевдоним графы
 */
def getColumnName(def row, def alias) {
    if (row != null && alias != null) {
        return row.getCell(alias).getColumn().getName().replace('%', '%%')
    }
    return ''
}

/**
 * Проверки соответствия НСИ.
 */
def checkNSI() {
    def data = getData(formData)
    if (!getRows(data).isEmpty()) {

        for (def row : getRows(data)) {
            if (isItogoRow(row)) {
                continue
            }

            // 1. Проверка кода валюты со справочным (графа 3)
            if (row.currencyCode!=null && getCurrency(row.currencyCode)==null) {
                logger.warn('Неверный код валюты!')
            }

            // 2. Проверка соответствия ставки рефинансирования ЦБ (графа 11) коду валюты (графа 3)
            def col11 = roundTo2(calc11(row, row.part2REPODate))
            if (col11!=null && col11!=row.rateBR) {
                logger.error('Неверно указана ставка Банка России!')
                return false
            }
        }
    }
    return true
}

Calendar getReportDate() {
    Calendar periodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId)
    Calendar reportingDate = periodEndDate
    reportingDate.set(Calendar.DATE, reportingDate.get(Calendar.DATE) + 1)
    return reportingDate
}

/**
 * Проставляет статические строки
 */
void addAllStatic() {
    def data=getData(formData)
    if (getRows(data).size()>0) {
        data.insert(itogo,getRows(data).size()+1)
    }
}

/**
 * Получает строку итого
 * @return
 */
DataRow<Cell> getItogo() {
    DataRow<Cell> itogo = formData.createDataRow()
    itogo.setAlias('itogo')
    itogo.securityName = "Итого"
    List itogoSum = ['nominalPrice', 'acquisitionPrice', 'salePrice', 'income', 'outcome', 'outcome269st', 'outcomeTax']
    itogoSum.each { name ->
        itogo.getCell(name).value = 0
    }
    for (DataRow row in getRows(getData(formData))) {
        if (row.getAlias() == null) {
            for (String name in itogoSum) {
                itogo.getCell(name).value += row.getCell(name).value ?: 0
            }
        }
    }
    setTotalStyle(itogo)
    return itogo
}

/**
 * Установить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    ['tradeNumber', 'securityName', 'currencyCode', 'nominalPrice', 'part1REPODate', 'part2REPODate', 'acquisitionPrice', 'salePrice', 'income', 'outcome', 'rateBR', 'outcome269st', 'outcomeTax'].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
}

BigDecimal calc9(DataRow row) {
    BigDecimal result
    BigDecimal a = (row.salePrice ?: 0) - (row.acquisitionPrice ?: 0)
    BigDecimal c = a.abs().setScale(2, BigDecimal.ROUND_HALF_UP)

    /**
     * Если  .A>0, то
     «графа 9» = 0
     «графа 10» = B
     Иначе Если  A<0
     «графа 9» = C
     «графа 10» = 0
     Иначе
     «графа 9»= «графа 10» = 0

     где
     A=«графа8» - «графа7»
     B=ОКРУГЛ(A;2),
     C= ОКРУГЛ(ABS(A);2),

     ABS() – операция получения модуля(абсолютного значения)  числа.

     Реализация немного проще и логичней чем в аналитике, но она даёт теже самые результаты
     вообщем оптимизированный вариант

     */
    if (a < 0) {
        result = c
    } else {
        result = 0
    }
    return roundTo2(result)
}

/**
 * Хелпер для округления чисел
 * @param value
 * @param newScale
 * @return
 */
BigDecimal roundTo2(BigDecimal value) {
    if (value != null) {
        return value.setScale(2, BigDecimal.ROUND_HALF_UP)
    } else {
        return value
    }
}

BigDecimal calc10(DataRow row) {
    BigDecimal result
    BigDecimal a = (row.salePrice ?: 0) - (row.acquisitionPrice ?: 0)
    BigDecimal b = a.setScale(2, BigDecimal.ROUND_HALF_UP)

    /**
     * Если  .A>0, то
     «графа 9» = 0
     «графа 10» = B
     Иначе Если  A<0
     «графа 9» = C
     «графа 10» = 0
     Иначе
     «графа 9»= «графа 10» = 0

     где
     A=«графа8» - «графа7»
     B=ОКРУГЛ(A;2),
     C= ОКРУГЛ(ABS(A);2),

     ABS() – операция получения модуля(абсолютного значения)  числа.

     Реализация немного проще и логичней чем в аналитике, но она даёт теже самые результаты
     вообщем оптимизированный вариант

     */

    if (a > 0) {
        result = b
    } else {
        result = 0
    }
    return roundTo2(result)
}

/**
 * Метод возвращает значение для графы 11
 * Логика выделена в отдельный метод так как
 * логика используется при расчетах и при логических проверкат
 * @param row
 * @param rateDate
 */
def calc11(DataRow row, def rateDate) {
    if (row.currencyCode == null) {
        return null
    }
    def currency = getCurrency(row.currencyCode)
    def rate = getRate(rateDate)
    // Если «графа 10» = 0, то « графа 11» не заполняется; && Если «графа 3» не заполнена, то « графа 11» не заполняется
    if (!isItogoRow(row) && row.outcome != 0 && row.currencyCode != null){
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

BigDecimal calc12(DataRow row) {
    if (row.currencyCode == null) {
        return null
    }
    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    Date date01_11_2009 = format.parse('01.11.2009')
    BigDecimal result = null
    def countDaysInYear = getCountDaysInYear()
    def currency = getCurrency(row.currencyCode)
    if (row.outcome != null && row.outcome > 0) {
        long difference = (row.part2REPODate.getTime() - row.part1REPODate.getTime()) / (1000 * 60 * 60 * 24) // необходимо получить кол-во в днях
        difference = difference == 0 ? 1 : difference   // Эти вычисления для того чтобы получить разницу в днях, если она нулевая считаем равной 1 так написано в чтз
        if (currency == '810') {
            if (inPeriod(row.part2REPODate, '01.09.2008', '31.12.2009')) {
                /*
                a.  Если «графа 6» принадлежит периоду с 01.09.2008 по 31.12.2009, то:
                    «графа 12» = («графа 7» ? «графа 11» ? 1,5) ? ((«графа6» - «графа5») / 365 (366)) / 100;

                 */
                result = ((row.acquisitionPrice ?: 0) * (row.rateBR ?: 0) * 1.5) * (difference / countDaysInYear) / 100
            } else if (inPeriod(row.part2REPODate, '01.01.2010', '30.06.2010') && row.part1REPODate.compareTo(date01_11_2009) < 0) {
                /*
                b.  Если «графа 6» принадлежит периоду с 01.01.2010 по 30.06.2010 и одновременно сделка открыта до 01.11.2009 («графа 5» < 01.11.2009 г.), то
                    «графа 12» = («графа 7» ? «графа 11» ? 2) ? ((«графа 6» - «графа 5») / 365 (366)) / 100;
                 */
                result = ((row.acquisitionPrice ?: 0) * (row.rateBR ?: 0) * 2) * (difference / countDaysInYear) / 100
            } else if (inPeriod(row.part2REPODate, '01.01.2010', '31.12.2012')) {
                /*
                c.  Если «графа 6» принадлежит периоду с 01.01.2010 по 31.12.2012, то:
                    «графа 12» = («графа 7» ? «графа 11» ? 1,8) ? ((«графа6» - «графа5») / 365(366)) / 100.
                 */
                result = ((row.acquisitionPrice ?: 0) * (row.rateBR ?: 0) * 1.8) * (difference / countDaysInYear) / 100
            } else {
                /*
                d.  Иначе
                    «графа 12» = («графа 7» ? «графа 11» ? 1,1) ? ((«графа 6» -« графа 5») / 365 (366)) / 100;.
                 */
                result = ((row.acquisitionPrice ?: 0) * (row.rateBR ?: 0) * 1.1) * (difference / countDaysInYear) / 100
            }
        } else {
            result = ((row.acquisitionPrice ?: 0) * (row.rateBR ?: 0)) * (difference / countDaysInYear) / 100
            if (inPeriod(row.part2REPODate, '01.01.2011', '31.12.2012')) {
                result = ((row.acquisitionPrice ?: 0) * (row.rateBR ?: 0) * 0.8) * (difference / countDaysInYear) / 100
            }
        }
    }
    if (row.outcome != null && row.outcome == 0) {
        result = 0
    }

    return roundTo2(result)
}

BigDecimal calc13(DataRow row) {
    BigDecimal result = null
    if (row.outcome > 0) {
        if (row.outcome <= row.outcome269st) {
            result = row.outcome
        }
        if (row.outcome > row.outcome269st) {
            result = row.outcome269st
        }
    }
    if (row.outcome == 0) {
        result = 0
    }
    return result
}

/**
 * Табл. 207 Алгоритмы заполнения полей формы «Регистр налогового учёта закрытых сделок РЕПО с обязательством покупки по 2-й части»
 */
void calc() {
    def data = getData(formData)
    for (DataRow row in getRows(data)) {
        if (row.getAlias() == null) {
            row.income = calc9(row)
            row.outcome = calc10(row)
            row.rateBR = calc11(row,row.part2REPODate)
            row.outcome269st = calc12(row)
            row.outcomeTax = calc13(row)
        }
    }
    data.save(getRows(data));

}

/**
 * Количество дней в году за который делаем
 * @return
 */
int getCountDaysInYear() {
    Calendar periodStartDate = reportPeriodService.getStartDate(formData.reportPeriodId)
    return countDaysOfYear = (new GregorianCalendar()).isLeapYear(periodStartDate.get(Calendar.YEAR)) ? 365 : 366
}

/**
 * Сортирует форму в соответвие с требованиями 6.11.2.1 Перечень полей формы
 */
void sort() {
    getRows(getData(formData)).sort({ DataRow a, DataRow b ->
        if (a.part1REPODate == b.part1REPODate) {
            return a.tradeNumber <=> b.tradeNumber
        }
        return a.part1REPODate <=> b.part1REPODate
    })
}

/**
 * Удаляет строку из формы
 */
void deleteRow() {
    if (currentDataRow != null && currentDataRow.getAlias() == null) {
        getData(formData).delete(currentDataRow)
    }
}

/**
 * Удаляет все статические строки(ИТОГО) во всей форме
 */
void deleteAllStatic() {
    def data = getData(formData)
    for(def i=0;i<getRows(data).size();i++){
        def row = getRows(data).get(i)
        if (row.getAlias() == "itogo") {
            data.delete(row)
        }
    }
}

DataRow getRealItogo(){
    def data = getData(formData)
    for(def i=0;i<getRows(data).size();i++){
        def row = getRows(data).get(i)
        if (row.getAlias() == "itogo") {
            return row;
        }
    }
}

/**
 * Вставка строки в случае если форма генирует динамически строки итого (на основе данных введённых пользователем)
 */
void addNewRow() {
    def data = getData(formData)
    DataRow<Cell> newRow = getNewRow()
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
 * Консолидация.
 */
void consolidation() {
    def data = getData(formData)
    // удалить все строки и собрать из источников их строки
    data.clear()

    departmentFormTypeService.getFormSources(formData.departmentId, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                getRows(getData(source)).each { row->
                    if (row.getAlias() == null || row.getAlias() == '') {
                        data.insert(row,getRows(data).size()+1)
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
    def cached = data.getAllCached()
    return cached
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
    return  refBookService.getStringValue(15,currencyCode,'CODE')
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
 * Проверка является ли строка итоговой (любой итоговой, т.е. по коду, либо основной)
 */
def isItogoRow(row){
    row.getAlias()=='itogo'
}

/**
 * Получение импортируемых данных.
 * Транспортный файл формата xml.
 */
void importData() {
    def fileName = (UploadFileName ? UploadFileName.toLowerCase() : null)
    if (fileName == null || fileName == '') {
        logger.error('Имя файла не должно быть пустым')
        return
    }
    if (!fileName.contains('.xml')) {
        logger.error('Формат файла должен быть *.xml')
        return
    }

    def is = ImportInputStream
    if (is == null) {
        logger.error('Поток данных пуст')
        return
    }

    def xmlString = importService.getData(is, fileName)
    if (xmlString == null || xmlString == '') {
        logger.error('Отсутствие значении после обработки потока данных')
        return
    }

    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null) {
        logger.error('Отсутствие значении после обработки потока данных')
        return
    }

    try {
        // добавить данные в форму
        def totalLoad = addData(xml)

        // рассчитать, проверить и сравнить итоги
        if (totalLoad != null) {
            checkTotalRow(totalLoad)
        } else {
            logger.error("Нет итоговой строки.")
        }
    } catch(Exception e) {
        logger.error('Во время загрузки данных произошла ошибка! ' + e.message)
    }

    if (!hasError()) {
        logger.info('Закончена загрузка файла ' + fileName)
    }

        // добавить данные в форму
        def totalLoad = addData(xml)
        if (totalLoad!=null) {

        } else {
            logger.error("Нет итоговой строки.")
        }

}

/**
 * Заполнить форму данными.
 *
 * @param xml данные
 */
def addData(def xml) {
    def date = new Date()
    def cache = [:]
    def data = getData(formData)
    data.clear()
    def index

    for (def row : xml.exemplar.table.detail.record) {
        index = 0
        def newRow = getNewRow()

        // графа 1
        newRow.tradeNumber = row.field[index].text()
        index++

        // графа 2
        newRow.securityName = row.field[index].text()
        index++

        // графа 3 - справочник 15, атрибут 64
        newRow.currencyCode = getRecordId(15, 'CODE', row.field[index].text(), date, cache)
        index++

        // графа 4
        newRow.nominalPrice = getNumber(row.field[index].@value.text())
        index++

        // в транспортном файле порядок колонок по другому (графа 1, 2, 3, 4, 7, 8, 5, 6, 9, 10, 11, 12, 13)
        // графа 7
        newRow.acquisitionPrice = getNumber(row.field[index].@value.text())
        index++

        // графа 8
        newRow.salePrice = getNumber(row.field[index].@value.text())
        index++

        // графа 5
        newRow.part1REPODate = getDate(row.field[index].@value.text())
        index++

        // графа 6
        newRow.part2REPODate = getDate(row.field[index].@value.text())
        index++

        // графа 9
        newRow.income = getNumber(row.field[index].@value.text())
        index++

        // графа 10
        newRow.outcome = getNumber(row.field[index].@value.text())
        index++

        // графа 11
        newRow.rateBR = getNumber(row.field[index].@value.text())
        index++

        // графа 12
        newRow.outcome269st = getNumber(row.field[index].@value.text())
        index++

        // графа 13
        newRow.outcomeTax = getNumber(row.field[index].@value.text())

        insert(data, newRow)
    }

    // итоговая строка
    if (xml.exemplar.table.total.record.field.size() > 0) {
        def row = xml.exemplar.table.total.record[0]
        def totalRow = formData.createDataRow()

        // графа 4
        totalRow.nominalPrice = getNumber(row.field[3].@value.text())
        index++

        // графа 7
        totalRow.acquisitionPrice = getNumber(row.field[4].@value.text())
        index++

        // графа 8
        totalRow.salePrice = getNumber(row.field[5].@value.text())
        index++

        // графа 9
        totalRow.income = getNumber(row.field[8].@value.text())
        index++

        // графа 10
        totalRow.outcome = getNumber(row.field[9].@value.text())
        index++

        // графа 12
        totalRow.outcome269st = getNumber(row.field[11].@value.text())
        index++

        // графа 13
        totalRow.outcomeTax = getNumber(row.field[12].@value.text())

        return totalRow
    } else {
        return null
    }
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
 * Вставить новыую строку в конец нф.
 *
 * @param data данные нф
 * @param row строка
 */
void insert(def data, def row) {
    data.insert(row, getRows(data).size() + 1)
}

/**
 * Получить новую стролу с заданными стилями.
 */
def getNewRow() {
    def row = formData.createDataRow()
    [
            'tradeNumber', 'securityName', 'currencyCode', 'nominalPrice', 'part1REPODate', 'part2REPODate', 'acquisitionPrice', 'salePrice'
    ].each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    return row
}

/**
 * Получить идентификатор записи из справочника.
 *
 * @param value
 */
def getRecordId(def ref_id, String code, String value, Date date, def cache) {
    String filter = code + " like '" + value.replaceAll(' ', '') + "%'"
    if (cache[ref_id]!=null) {
        if (cache[ref_id][filter]!=null) return cache[ref_id][filter]
    } else {
        cache[ref_id] = [:]
    }
    def refDataProvider = refBookFactory.getDataProvider(ref_id)
    def records = refDataProvider.getRecords(date, null, filter, null).getRecords()
    if (records.size() == 1){
        cache[ref_id][filter] = (records.get(0).record_id.toString() as Long)
        return cache[ref_id][filter]
    }
    logger.error("Не удалось определить элемент справочника!")
    return null;
}

/**
 * Получить дату по строковому представлению (формата дд.ММ.гггг)
 */
def getDate(def value) {
    if (value == null || value == '') {
        return null
    }
    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    return format.parse(value)
}

/**
 * Рассчитать, проверить и сравнить итоги.
 *
 * @param totalRow итоговая строка из транспортного файла
 */
void checkTotalRow(def totalRow) {
    deleteAllStatic()
    sort()
    calc()
    addAllStatic()
    if (!hasError() && allCheck()) {
        def data = getData(formData)
        def totalColumns = [4: 'nominalPrice', 7: 'acquisitionPrice', 8: 'salePrice',
                9: 'income', 10: 'outcome', 12: 'outcome269st', 13: 'outcomeTax']

        def totalCalc = null
        for (def row : getRows(data)) {
            if (isItogoRow(row)) {
                totalCalc = row
                break
            }
        }
        if (totalCalc != null) {
            totalColumns.each { index, columnAlias ->
                if (totalCalc[columnAlias] != totalRow[columnAlias]) {
                    logger.error("Итоговая сумма в графе $index в транспортном файле некорректна")
                }
            }
        }
    }
}

/**
 * Имеются ли фатальные ошибки.
 */
def hasError() {
    return logger.containsLevel(LogLevel.ERROR)
}