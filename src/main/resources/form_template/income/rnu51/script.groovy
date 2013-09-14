package form_template.income.rnu51

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel

import java.text.SimpleDateFormat

/**
 * 6.5	(РНУ-51) Регистр налогового учёта финансового результата от реализации (выбытия) ОФЗ
 *
 * TODO:
 *      - неясности как рассчитывать графу 16 и 17
 */

switch (formDataEvent) {
    case FormDataEvent.CHECK:
        allCheck()
        break
    case FormDataEvent.CREATE:
        checkCreation()
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
        recalculateNumbers()
        break
    case FormDataEvent.DELETE_ROW:
        deleteRow()
        recalculateNumbers()
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
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED:
        allCheck()
        break
    // обобщить
    case FormDataEvent.COMPOSE:
        consolidation()
        deleteAllStatic()
        sort()
        calc()
        addAllStatic()
        allCheck()
        // для сохранения изменений приемников
        getData(formData).commit()
        break
    case FormDataEvent.IMPORT :
        importData()
        if (!hasError()) {
            deleteAllStatic()
            sort()
            calc()
            addAllStatic()
            if (!hasError()) {
                logicalCheck()
                checkNSI()
                if (!hasError()) {
                    logger.info('Закончена загрузка файла ' + UploadFileName)
                }
            }
        }
        break
    case FormDataEvent.MIGRATION :
        importData()
        if (!hasError()) {
            def total = getCalcTotalRow()
            def data = getData(formData)
            insert(data, total)
            logger.info('Закончена загрузка файла ' + UploadFileName)
        }
        break
}

/**


 № графы	ALIAS                   Тип поля                Наименование поля
 0          fix                     Строка 255              Скрытое поле для строки итого
 1.		    rowNumber               Число/15/               № пп	            		                                                    Натуральное число( ≥1)
 2.		    tradeNumber             Число/1/                Код сделки			                                                            Справочник, Может принимать значения: 1 ИЛИ 2 ИЛИ 4 ИЛИ 5 1 - сделки на ОРЦБ, кроме переговорных; 2 - переговорные сделки на ОРЦБ; 4 - погашение, в т.ч. частичное; 5 - операции, связанные с открытием-закрытием короткой позиции.
 3.		    singSecurirty           Строка /1/	            Признак ценной бумаги	                                                        Справочник, Может принимать значения «+» или «-»
 4.		    issue                   Строка /255/            Выпуск
 5.		    acquisitionDate         Дата	ДД.ММ.ГГГГ      Дата приобретения, закрытия короткой позиции
 6.		    saleDate                Дата	ДД.ММ.ГГГГ      Дата реализации, погашения, прочего выбытия, открытия короткой позиции
 7.		    amountBonds             Число/15/               Количество облигаций (шт.)
 8.		    acquisitionPrice        Число/17.2/             Цена приобретения (руб.коп.)
 9.		    costOfAcquisition       Число/17.2/	            Расходы по приобретению (руб.коп.)
 10.	    marketPriceInPerc       Число/18.3/             Рыночная цена на дату приобретения. В % к номиналу
 11.	    marketPriceInRub        Число/17.2/             Рыночная цена на дату приобретения. В рублях и коп.
 12.	    acquisitionPriceTax     Число/17.2/             Цена приобретения для целей налогообложения (руб.коп.)
 13.	    redemptionValue         Число/17.2/             Стоимость погашения (руб.коп.)			                                        ≥ 0
 14.	    priceInFactPerc         Число/18.3/             Фактическая цена реализации. В % к номиналу
 15.	    priceInFactRub          Число/17.2/             Фактическая цена реализации. В рублях и коп.
 16.	    marketPriceInPerc1      Число/18.3/	            Рыночная цена на дату реализации. В % к номиналу
 17.	    marketPriceInRub1       Число/17.2/	            Рыночная цена на дату реализации. В рублях и коп.
 18.	    salePriceTax            Число/17.2/             Цена реализации (выбытия) для целей налогообложения (руб.коп.)
 19.	    expensesOnSale          Число/17.2/             Расходы по реализации (выбытию) (руб.коп.)
 20.	    expensesTotal           Число/17.2/             Всего расходы (руб. коп.)
 21.	    profit                  Число/17.2/	            Прибыль (+), убыток (-) от реализации (погашения) (руб.коп.)
 22.	    excessSalePriceTax      Число/17.2/	            Превышение цены реализации для целей налогообложения над ценой реализации (руб.коп.)

 */

void logicalCheck() {
    def data = getData(formData).getAllCached()

    // Проверока наличия итоговой строки
    if (!data.isEmpty() && !checkAlias(data, 'itogo')) {
        logger.error('Итоговые значения не рассчитаны')
        return
    }

    def columns = ['rowNumber', 'tradeNumber', 'singSecurirty', 'issue', 'acquisitionDate', 'saleDate', 'amountBonds',
            'acquisitionPrice', 'costOfAcquisition', 'marketPriceInPerc', 'marketPriceInRub', 'acquisitionPriceTax',
            'redemptionValue', 'priceInFactPerc', 'priceInFactRub', 'marketPriceInPerc1', 'marketPriceInRub1',
            'salePriceTax', 'expensesOnSale', 'expensesTotal', 'profit', 'excessSalePriceTax']

    for (DataRow row in data) {
        if (row.getAlias() == null) {

            if (!checkRequiredColumns(row, columns)) {
                return
            }

            def index = row.rowNumber
            def errorMsg
            if (index!=null && index!='') {
                errorMsg = "В строке \"№ пп\" равной $index "
            } else {
                index = row.getIndex()
                errorMsg = "В строке $index "
            }

            // 1.Проверка цены приобретения для целей налогообложения (графа 12)
            if (row.acquisitionPrice > row.marketPriceInRub && row.acquisitionPriceTax != row.marketPriceInRub) {
                logger.error(errorMsg + "неверно определена цена приобретения для целей налогообложения")
                return
            }

            if (row.acquisitionPrice <= row.marketPriceInRub && row.acquisitionPriceTax != row.acquisitionPrice) {
                logger.error(errorMsg + "неверно определена цена приобретения для целей налогообложения")
                return
            }

            // 2.Проверка рыночной цены в процентах при погашении (графа 16)
            if (row.redemptionValue != null && row.redemptionValue > 0 && !(row.marketPriceInPerc1 == 100)) {
                logger.error(errorMsg + "Неверно указана рыночная цена в процентах при погашении!")
                return
            }

            // 3.Проверка рыночной цены в рублях при погашении (графа 17)
            if (row.redemptionValue != null && row.redemptionValue > 0 && !(row.marketPriceInRub1 == row.redemptionValue)) {
                logger.error(errorMsg + "неверно указана рыночная цена в рублях при погашении!")
                return
            }

            /**
             * Проверка цены реализации (выбытия) для целей налогообложения (графа 18)
             * Если «графа 2» = 1 или 2 или 5, и при этом «графа 14» > «графа 16» и «графа 15» > «графа 17», то «графа 18» = «графа 15»
             */
            def code = getCode(row.tradeNumber)
            if ((code == 1 || code == 2 || code == 5) && row.priceInFactPerc > row.marketPriceInPerc1
                    && row.priceInFactRub > row.marketPriceInRub1 && row.salePriceTax != row.priceInFactRub
            ) {
                logger.error(errorMsg + "неверно определена цена реализации для целей налогообложения по сделкам на ОРЦБ!")
                return
            }

            /**
             * Проверка цены реализации для целей налогообложения при погашении (графа 18)
             * Если «графа 2» = 4, то «графа 18» = «графа 13»
             * 1
             * Неверно определена цена реализации для целей налогообложения при погашении!
             */
            if (code == 4 && row.salePriceTax != row.redemptionValue) {
                logger.error(errorMsg + "неверно определена цена реализации для целей налогообложения при погашении!")
                return
            }

            /**
             * Проверка цены реализации для целей налогообложения по переговорным сделкам на ОРЦБ и сделкам, связанным с открытием-закрытием короткой позиции (графа 18)
             * Если «графа 2» = 2 или 5, и («графа 14» < «графа 16», и «графа 15» < «графа 17», то «графа 18» = «графа 17»)
             * 1
             * Неверно определена цена реализации для целей налогообложения по переговорным сделкам на ОРЦБ и сделкам, связанным с открытием-закрытием короткой позиции!
             */
            if ((code == 2 || code == 5) && row.tradeNumber < row.marketPriceInPerc1
                    && row.priceInFactRub < row.marketPriceInRub1 && row.salePriceTax != row.marketPriceInRub1) {
                logger.error(errorMsg + "неверно определена цена реализации для целей налогообложения по переговорным сделкам на ОРЦБ и сделкам, связанным с открытием-закрытием короткой позиции!")
                return
            }

            /**
             * Проверка итоговой суммы расходов (графа 20)
             * «графа 20» = «графа 9» + «графа 12» + «графа 19»
             * 1
             * Неверно определены расходы!
             */
            if (row.expensesTotal != (row.costOfAcquisition ?: 0) + (row.acquisitionPriceTax ?: 0) + (row.expensesOnSale ?: 0)) {
                logger.error(errorMsg + "неверно определены расходы!")
                return
            }

            /**
             * Проверка суммы финансового результата (графа 21)
             * «графа 21» = «графа 18» - «графа 20»
             * 1
             * Неверно определен финансовый результат реализации (выбытия)!
             */
            if (row.profit != (row.salePriceTax ?: 0) - (row.expensesTotal ?: 0)) {
                logger.error(errorMsg + "неверно определен финансовый результат реализации (выбытия)!")
                return
            }

            /**
             * Проверка превышения цены реализации для целей налогообложения над фактической ценой реализации (графа 22)
             1.	если «графа 2» ≠ 4, то «графа 22» = «графа 18» - «графа 15»
             2.	если «графа 2» = 4, то «графа 22» = 0
             3.	значение «графы 22» ≥ 0
             1
             Неверно определено превышение цены реализации для целей налогообложения над фактической ценой реализации!
             */
            if ((code != 4 && row.excessSalePriceTax != (row.salePriceTax ?: 0) - (row.priceInFactRub ?: 0))
                    || (code == 4 && row.excessSalePriceTax != 0)
                    || row.excessSalePriceTax < 0
            ) {
                logger.error(errorMsg + "неверно определено превышение цены реализации для целей налогообложения над фактической ценой реализации!")
                return
            }

            /**
             * Арифметическая проверка графы 12, 16, 17, 18, 20, 21, 22	•
             * Графы 12, 16, 17, 18, 20, 21, 22 всех строк формы, кроме итоговых, содержат значения, рассчитанные согласно алгоритмам расчета  из Табл. 172
             * 0
             * Неверно рассчитана графа «<Наименование графы>»!
             */
            List checks = ['acquisitionPriceTax', 'marketPriceInPerc1', 'marketPriceInRub1', 'salePriceTax', 'expensesTotal', 'profit', 'excessSalePriceTax']
            Map<String, BigDecimal> value = [:]
            value.put('acquisitionPriceTax', calc12(row))
            value.put('marketPriceInPerc1', calc16(row))
            value.put('marketPriceInRub1', calc17(row))
            value.put('salePriceTax', calc18(row))
            value.put('expensesTotal', calc20(row))
            value.put('profit', calc21(row))
            value.put('excessSalePriceTax', calc22(row))
            for (String check in checks) {
                if (row.getCell(check).value != value.get(check)) {
                    logger.error(errorMsg + "неверно рассчитана графа " + row.getCell(check).column.name.replace('%', '%%') + "!")
                    return
                }
            }
        }
    }

    /**
     * Проверка корректности расчета итоговых значений за текущий квартал
     * Графы 7-9, 11-13, 15, 17-22  строки «Итого за текущий квартал» содержат значения, рассчитанные согласно алгоритму расчета из Табл. 173
     * 1
     * Итоговые значения за текущий квартал рассчитаны неверно!
     */
    List itogoSum = ['amountBonds', 'acquisitionPrice', 'costOfAcquisition', 'marketPriceInRub',
            'acquisitionPriceTax', 'redemptionValue', 'priceInFactRub', 'priceInFactRub', 'salePriceTax',
            'expensesOnSale', 'expensesTotal', 'profit', 'excessSalePriceTax']
    def itogoKvartal = getItogoKvartal()
    def prevItogoKvartal = getPrevItogoKvartal()
    if (data.size()>=2) {
        DataRow realItogoKvartal = getRealItogoKvartal()
        for (String alias in itogoSum) {
            if (realItogoKvartal.getCell(alias).value != itogoKvartal.getCell(alias).value) {
                logger.error("Итоговые значения за текущий квартал рассчитаны неверно!")
                return
            }
        }
    }

    /**
     * Проверка корректности расчета итоговых значений за текущий отчётный (налоговый) период
     * Графы 7-9, 11-13, 15, 17-22  строки «Итого за текущий отчётный (налоговый) период» содержат значения, рассчитанные согласно алгоритму расчета из Табл. 174
     * 1
     * Итоговые значения за текущий отчётный (налоговый) период рассчитаны неверно!
     */
    if (data.size()>=1) {
        DataRow realItogo = getRealItogo()
        def itogo = getItogo(itogoKvartal, prevItogoKvartal)
        for (String alias in itogoSum) {
            if (realItogo.getCell(alias).value != itogo.getCell(alias).value) {
                logger.error("Итоговые значения за текущий отчётный (налоговый) период рассчитаны неверно!")
                return
            }
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

def recalculateNumbers(){
    def index = 1
    def data = getData(formData)
    getRows(data).each{row->
        if(row.getAlias()==null){
            row.rowNumber = index++
        }
    }
    data.save(getRows(data))
}

DataRow<Cell> getItogo(def itogoKvartal, def prevItogoKvartal) {
    DataRow<Cell> itogo = formData.createDataRow()
    itogo.setAlias("itogo")
    itogo.getCell('fix').colSpan = 7
    itogo.fix = "Итого за текущий отчетный (налоговый) период"
    sumColumns = ['amountBonds', 'acquisitionPrice', 'costOfAcquisition', 'marketPriceInRub', 'acquisitionPriceTax', 'redemptionValue', 'priceInFactRub',
            'marketPriceInRub1', 'salePriceTax', 'expensesOnSale', 'expensesTotal', 'profit', 'excessSalePriceTax']
    for (String alias in sumColumns) {
        if (prevItogoKvartal != null) {
            if (itogo.getCell(alias).value == null) {
                itogo.getCell(alias).value = prevItogoKvartal.getCell(alias).value
            } else {
                itogo.getCell(alias).value += prevItogoKvartal.getCell(alias).value
            }
        }
        if (itogoKvartal != null) {
            if (itogo.getCell(alias).value == null) {
                itogo.getCell(alias).value = itogoKvartal.getCell(alias).value
            } else {
                itogo.getCell(alias).value += itogoKvartal.getCell(alias).value
            }
        }
    }
    setTotalStyle(itogo)
    return itogo
}

/**
 * Получает строку итого за квартал в преведущей НФ(ПРИНЯТОЙ) или null если формы
 * @return
 */
DataRow<Cell> getPrevItogoKvartal() {
    FormData formPrev = getFormPrev()
    if (formPrev != null && formPrev.state == WorkflowState.ACCEPTED) {
        return getData(formPrev).getDataRow(getRows(getData(formPrev)),'itogoKvartal')
    }
    return null
}

FormData getFormPrev() {
    ReportPeriod period = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
    FormData formPrev = null
    if (period != null) {
        formPrev = formDataService.find(formData.formType.id, formData.kind, formData.departmentId, period.id)
    }
    return formPrev
}


DataRow<Cell> getItogoKvartal() {
    DataRow<Cell> itogo = formData.createDataRow()
    itogo.setAlias("itogoKvartal")
    itogo.getCell('fix').colSpan = 7
    itogo.fix = "Итого за текущий квартал"
    sumColumns = ['amountBonds', 'acquisitionPrice', 'costOfAcquisition', 'marketPriceInRub', 'acquisitionPriceTax', 'redemptionValue', 'priceInFactRub',
            'marketPriceInRub1', 'salePriceTax', 'expensesOnSale', 'expensesTotal', 'profit', 'excessSalePriceTax']
    for (DataRow row in getData(formData).getAllCached()) {
        if (row.getAlias() == null) {
            for (String alias in sumColumns) {
                if (itogo.getCell(alias).value == null) {
                    itogo.getCell(alias).value = row.getCell(alias).value ?: 0
                } else {
                    itogo.getCell(alias).value += row.getCell(alias).value ?: 0
                }
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
    ['rowNumber', 'fix', 'tradeNumber', 'singSecurirty', 'issue', 'acquisitionDate', 'saleDate', 'amountBonds',
            'acquisitionPrice', 'costOfAcquisition', 'marketPriceInPerc', 'marketPriceInRub', 'acquisitionPriceTax',
            'redemptionValue', 'priceInFactPerc', 'priceInFactRub', 'marketPriceInPerc1', 'marketPriceInRub1',
            'salePriceTax', 'expensesOnSale', 'expensesTotal', 'profit', 'excessSalePriceTax'].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
}

/*
 * TODO (Ramil Timerbaev) из чтз:
 * ВОПРОС:  уточнить,  какое значение нужно указать, если «Графа 13»=0?
 * Этот вопрос актуален и для графы 17. ....
 */
/**
 * Если «графа 13» > 0, то «графа 16» = 100
 * @param row
 * @return
 */
BigDecimal calc16(DataRow row) {
    BigDecimal result = row.marketPriceInPerc1 // TODO (Ramil Timerbaev) вместо null поставил значение 16 графы
    if (row.redemptionValue > 0) {
        result = 100
    }
    if (result != null) result = roundTo2(result, 3)
    return result
}

/**
 * Если «графа 13» > 0, то «графа 17» = «графа 13»
 * @param row
 * @return
 */
BigDecimal calc17(DataRow row) {
    BigDecimal result = row.marketPriceInRub1 // TODO (Ramil Timerbaev) вместо null поставил значение 17 графы
    if (row.redemptionValue > 0) {
        result = row.redemptionValue
    }
    if (result != null) result = roundTo2(result, 3)
    return result
}
/**
 Если «графа 2» = 1 или 2 или 5, И («графа 14» > «графа 16» И «графа 15» > «графа 17»), то «графа 18» = «графа 15»
 Если «графа 2» = 4, то «графа 18» = «графа 13»
 Если «графа 2» = 2 или 5, И  («графа 14» < «графа 16» И «графа 15» < «графа 17»), то «графа 18» = «графа 17»
 * @param row
 * @return
 */
BigDecimal calc18(DataRow row) {
    BigDecimal result = null
    def code = getCode(row.tradeNumber)
    if ((code == 1 || code == 2 || code == 5) && (row.priceInFactPerc > row.marketPriceInPerc1 && row.priceInFactRub > row.marketPriceInRub1)) {
        result = row.priceInFactRub
    }
    if (code == 4) {
        result = row.redemptionValue
    }
    if ((code == 2 || code == 5) && (row.priceInFactPerc < row.marketPriceInPerc1 && row.priceInFactRub < row.marketPriceInRub1)) {
        result = row.marketPriceInRub1
    }
    if (result != null) result = roundTo2(result, 3)
    return result
}

/**
 «графа 20» = «графа 9» + «графа 12» + «графа 19»
 * @param row
 * @return
 */
BigDecimal calc20(DataRow row) {
    BigDecimal result = (row.costOfAcquisition ?: 0) + (row.acquisitionPriceTax ?: 0) + (row.expensesOnSale ?: 0)
    return roundTo2(result, 3)
}

/**
 «графа 21» = «графа 18» -«графа 20»
 * @param row
 * @return
 */
BigDecimal calc21(DataRow row) {
    BigDecimal result = (row.salePriceTax ?: 0) - (row.expensesTotal ?: 0)
    return roundTo2(result, 3)
}

/**
 Если «графа 2» = 4 то
 «графа 22» = 0
 Если «графа 2» ≠4, то
 «графа 22» = «графа 18» -«графа 15»
 * @param row
 * @return
 */
BigDecimal calc22(DataRow row) {
    BigDecimal result = null
    if (getCode(row.tradeNumber) == 4) {
        result = 0
    } else {
        result = (row.salePriceTax ?: 0) - (row.priceInFactRub ?: 0)
    }
    return roundTo2(result, 3)
}

void calc() {
    def data = getData(formData)
    for (DataRow row in getData(formData).getAllCached()) {
        if (row.getAlias() == null) {
            row.rowNumber = calc1(row)
            row.acquisitionPriceTax = calc12(row)
            row.marketPriceInPerc1 = calc16(row)
            row.marketPriceInRub1 = calc17(row)
            row.salePriceTax = calc18(row)
            row.expensesTotal = calc20(row)
            row.profit = calc21(row)
            row.excessSalePriceTax = calc22(row)
        }
    }
    data.save(data.getAllCached());
}

/**
 * 1.	Если «графа 8» > «графа 11», то
 2.	«графа 12» = графа 11
 •	Если «графа 8» ≤ «графа 11», то
 •	«графа 12» = «графа 8»

 * @param row
 * @return
 */
BigDecimal calc12(DataRow row) {
    BigDecimal result = null
    if (row.acquisitionPrice > row.marketPriceInRub) {
        result = row.marketPriceInRub
    } else {
        result = row.acquisitionPrice
    }
    if (result != null) result = roundTo2(result, 2)
    return result
}

/**
 * Удаляет все статические строки(ИТОГО) во всей форме
 */
void deleteAllStatic() {
    def data = getData(formData)
    def rItogo = getRealItogo()
    if (rItogo!=null) {
        data.delete(rItogo)
    }
    def rItogoKvartal = getRealItogoKvartal()
    if (rItogoKvartal!=null) {
        data.delete(rItogoKvartal)
    }
}

/**
 * Сортирует форму в соответвие с требованиями 6.11.2.1	Перечень полей формы
 */
void sort() {
    getRows(getData(formData)).sort({ DataRow a, DataRow b ->
        def codeA = getCode(a.tradeNumber)
        def codeB = getCode(b.tradeNumber)
        if (codeA == codeB && a.singSecurirty == b.singSecurirty) {
            return a.issue <=> b.issue
        }
        if (codeA == codeB) {
            return a.singSecurirty <=> b.singSecurirty
        }
        return codeA <=> codeB
    })
}

void addAllStatic() {
    def data = getData(formData)
    if (getRows(data).isEmpty()) {
        return
    }
    def itogoKvartal = getItogoKvartal()
    def prevItogoKvartal = getPrevItogoKvartal()
    data.insert(itogoKvartal, data.getAllCached().size() + 1)

    data.insert(getItogo(itogoKvartal, prevItogoKvartal), data.getAllCached().size() + 1)
}

void allCheck() {
    logicalCheck()
    checkNSI()
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

void checkNSI() {
    getRows(getData(formData)).each{ DataRow row ->

        def index = row.rowNumber
        def errorMsg
        if (index!=null && index!='') {
            errorMsg = "В строке \"№ пп\" равной $index "
        } else {
            index = row.getIndex()
            errorMsg = "В строке $index "
        }
        if (row.getAlias()==null) {
            if (row.tradeNumber!=null && getCode(row.tradeNumber)==null){
                logger.warn(errorMsg + "код сделки в справочнике отсутствует!");
            }
            if (row.singSecurirty!=null && getSign(row.singSecurirty)==null){
                logger.warn(errorMsg + "признак ценной бумаги в справочнике отсутствует!");
            }
        }
    }
}

/**
 * Проверить заполненость обязательных полей.
 *
 * @param row строка
 * @param columns список обязательных графов
 */
def checkRequiredColumns(def row, def columns) {
    def data = getData(formData)
    def colNames = []

    def cell
    columns.each {
        cell = row.getCell(it)
        if (cell.isEditable() && (cell.getValue() == null || row.getCell(it).getValue() == '')) {
            def name = getColumnName(row, it)
            colNames.add('"' + name + '"')
        }
    }
    if (!colNames.isEmpty()) {
        def index = row.rowNumber
        def errorMsg = colNames.join(', ')
        if (!isEmpty(index)) {
            logger.error("В строке \"№ пп\" равной $index не заполнены колонки : $errorMsg.")
        } else {
            index = getRows(data).indexOf(row) + 1
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
 * Хелпер для округления чисел
 * @param value
 * @param newScale
 * @return
 */
BigDecimal roundTo2(BigDecimal value, int newScale) {
    if (value != null) {
        return value.setScale(newScale, BigDecimal.ROUND_HALF_UP)
    } else {
        return value
    }
}

/**
 * @todo block http://jira.aplana.com/browse/SBRFACCTAX-2548 пока можно использовать алгоритм ниже
 * @param row
 * @return
 */
BigDecimal calc1(DataRow row) {
    return getData(formData).getAllCached().indexOf(row) + 1
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
                getData(source).getAllCached().each { DataRow row ->
                    if (row.getAlias() == null || row.getAlias() == '') {
                        data.insert(row, getRows(data).size()+1)
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

DataRow getRealItogo(){
    def data = getData(formData)
    for(def i=0;i<getRows(data).size();i++){
        def row = getRows(data).get(i)
        if (row.getAlias() == "itogo") {
            return row;
        }
    }
}

DataRow getRealItogoKvartal(){
    def data = getData(formData)
    for(def i=0;i<getRows(data).size();i++){
        def row = getRows(data).get(i)
        if (row.getAlias() == "itogoKvartal") {
            return row;
        }
    }
}

def getRows(def data){
    return data.getAllCached()
}

/**
 * Получить код сделки
 */
def getCode(def code) {
    return  refBookService.getNumberValue(61,code,'CODE')
}

/**
 * Получить признак ценной бумаги
 */
def getSign(def sign) {
    return  refBookService.getStringValue(62,sign,'CODE')
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

        // расчетать, проверить и сравнить итоги
        if (totalLoad != null) {
            checkTotalRow(totalLoad)
        } else {
            logger.error("Нет итоговой строки.")
        }
    } catch(Exception e) {
        logger.error('Во время загрузки данных произошла ошибка! ' + e.toString())
    }
}

/**
 * Заполнить форму данными.
 *
 * @param xml данные
 */
def addData(def xml) {
    def tmp
    def index
    def data = getData(formData)
    data.clear()
    def cache = [:]
    def date = new Date()
    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')

    for (def row : xml.exemplar.table.detail.record) {
        index = 0

        def newRow = getNewRow()

        // графа 1
        newRow.rowNumber = getNumber(row.field[index].@value.text())
        index++

        // графа 2 - справочник 61 "Коды сделок"
        tmp = null
        if (row.field[index].@value.text() != null && row.field[index].@value.text().trim() != '') {
            tmp = getRecordId(61, 'CODE', getNumber(row.field[index].@value.text()), new Date(), cache)
        }
        newRow.tradeNumber = tmp
        index++

        // графа 3 - справочник 62 "Признаки ценных бумаг"
        tmp = null
        if (row.field[index].text() != null && row.field[index].text().trim() != '') {
            tmp = getRecordId(62, 'CODE', row.field[index].text(), date, cache)
        }
        newRow.singSecurirty = tmp
        index++

        // графа 4
        newRow.issue = row.field[index].text()
        index++

        // графа 5
        newRow.acquisitionDate = getDate(row.field[index].@value.text(), format)
        index++

        // графа 6
        newRow.saleDate = getDate(row.field[index].@value.text(), format)
        index++

        // графа 7
        newRow.amountBonds = getNumber(row.field[index].@value.text())
        index++

        // графа 8
        newRow.acquisitionPrice = getNumber(row.field[index].@value.text())
        index++

        // графа 9
        newRow.costOfAcquisition = getNumber(row.field[index].@value.text())
        index++

        // графа 10
        newRow.marketPriceInPerc = getNumber(row.field[index].@value.text())
        index++

        // графа 11
        newRow.marketPriceInRub = getNumber(row.field[index].@value.text())
        index++

        // графа 12
        newRow.acquisitionPriceTax = getNumber(row.field[index].@value.text())
        index++

        // графа 13
        newRow.redemptionValue = getNumber(row.field[index].@value.text())
        index++

        // графа 14
        newRow.priceInFactPerc = getNumber(row.field[index].@value.text())
        index++

        // графа 15
        newRow.priceInFactRub = getNumber(row.field[index].@value.text())
        index++

        // графа 16
        newRow.marketPriceInPerc1 = getNumber(row.field[index].@value.text())
        index++

        // графа 17
        newRow.marketPriceInRub1 = getNumber(row.field[index].@value.text())
        index++

        // графа 18
        newRow.salePriceTax = getNumber(row.field[index].@value.text())
        index++

        // графа 19
        newRow.expensesOnSale = getNumber(row.field[index].@value.text())
        index++

        // графа 20
        newRow.expensesTotal = getNumber(row.field[index].@value.text())
        index++

        // графа 21
        newRow.profit = getNumber(row.field[index].@value.text())
        index++

        // графа 22
        newRow.excessSalePriceTax = getNumber(row.field[index].@value.text())

        insert(data, newRow)
    }

    // итоговая строка
    if (xml.exemplar.table.total.record.size() > 1) {
        def row = xml.exemplar.table.total.record[0]
        def total = formData.createDataRow()

        // графа 7
        total.amountBonds = getNumber(row.field[6].@value.text())
        index++

        // графа 8
        total.acquisitionPrice = getNumber(row.field[7].@value.text())
        index++

        // графа 9
        total.costOfAcquisition = getNumber(row.field[8].@value.text())
        index++

        // графа 11
        total.marketPriceInRub = getNumber(row.field[10].@value.text())
        index++

        // графа 12
        total.acquisitionPriceTax = getNumber(row.field[11].@value.text())
        index++

        // графа 13
        total.redemptionValue = getNumber(row.field[12].@value.text())
        index++

        // графа 15
        total.priceInFactRub = getNumber(row.field[14].@value.text())
        index++

        // графа 17
        total.marketPriceInRub1 = getNumber(row.field[16].@value.text())
        index++

        // графа 18
        total.salePriceTax = getNumber(row.field[17].@value.text())
        index++

        // графа 19
        total.expensesOnSale = getNumber(row.field[18].@value.text())
        index++

        // графа 20
        total.expensesTotal = getNumber(row.field[19].@value.text())
        index++

        // графа 21
        total.profit = getNumber(row.field[20].@value.text())
        index++

        // графа 22
        total.excessSalePriceTax = getNumber(row.field[21].@value.text())

        return total
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

    // графа 2..11, 13..15, 19
    ['tradeNumber', 'singSecurirty', 'issue', 'acquisitionDate', 'saleDate', 'amountBonds',
            'acquisitionPrice', 'costOfAcquisition', 'marketPriceInPerc', 'marketPriceInRub',
            'redemptionValue', 'priceInFactPerc', 'priceInFactRub', 'expensesOnSale'].each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    return row
}

/**
 * Получить дату по строковому представлению (формата дд.ММ.гггг)
 */
def getDate(def value, def format) {
    if (value == null || value == '') {
        return null
    }
    return format.parse(value)
}

/**
 * Проверить существования строки по алиасу.
 *
 * @param list строки нф
 * @param rowAlias алиас
 * @return <b>true</b> - строка с указанным алиасом есть, иначе <b>false</b>
 */
def checkAlias(def list, def rowAlias) {
    if (rowAlias == null || rowAlias == "" || list == null || list.isEmpty()) {
        return false
    }
    for (def row : list) {
        if (row.getAlias() == rowAlias) {
            return true
        }
    }
    return false
}

/**
 * Расчетать, проверить и сравнить итоги.
 *
 * @param totalRow итоговая строка из транспортного файла
 */
void checkTotalRow(def totalRow) {
    // графы 7-9, 11-13, 15, 17-22
    def totalColumns = [7: 'amountBonds', 8 : 'acquisitionPrice', 9 : 'costOfAcquisition',
            11 : 'marketPriceInRub', 12 : 'acquisitionPriceTax', 13 : 'redemptionValue',
            15 : 'priceInFactRub', 17 : 'marketPriceInRub1', 18 : 'salePriceTax', 19 : 'expensesOnSale',
            20 : 'expensesTotal', 21 : 'profit', 22 : 'excessSalePriceTax']
    def totalCalc = getCalcTotalRow()
    def errorColums = []
    if (totalCalc != null) {
        totalColumns.each { index, columnAlias ->
            if (totalRow[columnAlias] != null && totalCalc[columnAlias] != totalRow[columnAlias]) {
                errorColums.add(index)
            }
        }
    }
    if (!errorColums.isEmpty()) {
        def columns = errorColums.join(', ')
        logger.error("Итоговая сумма в графе $columns в транспортном файле некорректна")
    }
}

/**
 * Имеются ли фатальные ошибки.
 */
def hasError() {
    return logger.containsLevel(LogLevel.ERROR)
}

/**
 * Получить итоговую строку с суммами.
 */
def getCalcTotalRow() {
    def totalColumns = sumColumns = ['amountBonds', 'acquisitionPrice', 'costOfAcquisition', 'marketPriceInRub',
            'acquisitionPriceTax', 'redemptionValue', 'priceInFactRub', 'marketPriceInRub1', 'salePriceTax',
            'expensesOnSale', 'expensesTotal', 'profit', 'excessSalePriceTax']
    def totalRow = formData.createDataRow()

    totalRow.setAlias('itogo')
    totalRow.getCell('fix').colSpan = 7
    totalRow.fix = "Итого за текущий отчетный (налоговый) период"
    setTotalStyle(totalRow)
    def data = getData(formData)
    totalColumns.each { alias ->
        totalRow.getCell(alias).setValue(getSum(data, alias))
    }
    return totalRow
}

/**
 * Получить сумму столбца.
 */
def getSum(def data, def columnAlias) {
    def from = 0
    def to = getRows(data).size() - 1
    if (from > to) {
        return 0
    }
    return summ(formData, getRows(data), new ColumnRange(columnAlias, from, to))
}

/**
 * Получить id справочника.
 *
 * @param ref_id идентификатор справончика
 * @param code атрибут справочника
 * @param value значение для поиска
 * @param date дата актуальности
 * @param cache кеш
 * @return
 */
def getRecordId(def ref_id, String code, def value, Date date, def cache) {
    String filter = code + " = '" + value + "'"
    if (cache[ref_id]!=null) {
        if (cache[ref_id][filter] != null) {
            return cache[ref_id][filter]
        }
    } else {
        cache[ref_id] = [:]
    }
    def refDataProvider = refBookFactory.getDataProvider(ref_id)
    def records = refDataProvider.getRecords(date, null, filter, null).getRecords()
    if (records.size() == 1){
        cache[ref_id][filter] = (records.get(0).record_id.toString() as Long)
        return cache[ref_id][filter]
    }
    logger.error("Не удалось найти запись в справочнике (id=$ref_id) с атрибутом $code равным $value!")
    return null
}