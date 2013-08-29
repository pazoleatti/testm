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
    def columns = ['rowNumber', 'tradeNumber', 'singSecurirty', 'issue', 'acquisitionDate', 'saleDate', 'amountBonds',
            'acquisitionPrice', 'costOfAcquisition', 'marketPriceInPerc', 'marketPriceInRub', 'acquisitionPriceTax',
            'redemptionValue', 'priceInFactPerc', 'priceInFactRub', 'marketPriceInPerc1', 'marketPriceInRub1',
            'salePriceTax', 'expensesOnSale', 'expensesTotal', 'profit', 'excessSalePriceTax']

    for (DataRow row in data) {
        if (row.getAlias() == null) {

            if (!checkRequiredColumns(row, columns)) {
                return
            }

            // 1.Проверка цены приобретения для целей налогообложения (графа 12)
            if (row.acquisitionPrice > row.marketPriceInRub && row.acquisitionPriceTax != row.marketPriceInRub) {
                logger.error("Неверно определена цена приобретения для целей налогообложения")
                return
            }

            if (row.acquisitionPrice <= row.marketPriceInRub && row.acquisitionPriceTax != row.acquisitionPrice) {
                logger.error("Неверно определена цена приобретения для целей налогообложения")
                return
            }

            // 2.Проверка рыночной цены в процентах при погашении (графа 16)
            if (row.redemptionValue != null && row.redemptionValue > 0 && !(row.marketPriceInPerc1 == 100)) {
                logger.error("Неверно указана рыночная цена в процентах при погашении!")
                return
            }

            // 3.Проверка рыночной цены в рублях при погашении (графа 17)
            if (row.redemptionValue != null && row.redemptionValue > 0 && !(row.marketPriceInRub1 == row.redemptionValue)) {
                logger.error("Неверно указана рыночная цена в рублях при погашении!")
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
                logger.error('Неверно определена цена реализации для целей налогообложения по сделкам на ОРЦБ!')
                return
            }

            /**
             * Проверка цены реализации для целей налогообложения при погашении (графа 18)
             * Если «графа 2» = 4, то «графа 18» = «графа 13»
             * 1
             * Неверно определена цена реализации для целей налогообложения при погашении!
             */
            if (code == 4 && row.salePriceTax != row.redemptionValue) {
                logger.error('Неверно определена цена реализации для целей налогообложения при погашении!')
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
                logger.error('Неверно определена цена реализации для целей налогообложения по переговорным сделкам на ОРЦБ и сделкам, связанным с открытием-закрытием короткой позиции!')
                return
            }

            /**
             * Проверка итоговой суммы расходов (графа 20)
             * «графа 20» = «графа 9» + «графа 12» + «графа 19»
             * 1
             * Неверно определены расходы!
             */
            if (row.expensesTotal != (row.costOfAcquisition ?: 0) + (row.acquisitionPriceTax ?: 0) + (row.expensesOnSale ?: 0)) {
                logger.error('Неверно определены расходы!')
                return
            }

            /**
             * Проверка суммы финансового результата (графа 21)
             * «графа 21» = «графа 18» - «графа 20»
             * 1
             * Неверно определен финансовый результат реализации (выбытия)!
             */
            if (row.profit != (row.salePriceTax ?: 0) - (row.expensesTotal ?: 0)) {
                logger.error('Неверно определен финансовый результат реализации (выбытия)!')
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
                logger.error('Неверно определено превышение цены реализации для целей налогообложения над фактической ценой реализации!')
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
                    logger.error("Неверно рассчитана графа " + row.getCell(check).column.name.replace('%', '') + "!")
                    return
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
        if (data.size()>=2) {
            DataRow realItogoKvartal = getRealItogoKvartal()
            itogoKvartal
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
            itogo
            for (String alias in itogoSum) {
                if (realItogo.getCell(alias).value != itogo.getCell(alias).value) {
                    logger.error("Итоговые значения за текущий отчётный (налоговый) период рассчитаны неверно!")
                    return
                }
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
        while(row.getAlias()!=null && index>=0){
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

DataRow<Cell> getItogo() {
    DataRow<Cell> itogo = formData.createDataRow()
    itogo.setAlias("itogo")
    itogo.getCell('fix').colSpan = 7
    itogo.fix = "Итого за текущий отчетный (налоговый) период"
    sumColumns = ['amountBonds', 'acquisitionPrice', 'costOfAcquisition', 'marketPriceInRub', 'acquisitionPriceTax', 'redemptionValue', 'priceInFactRub',
            'marketPriceInRub1', 'salePriceTax', 'expensesOnSale', 'expensesTotal', 'profit', 'excessSalePriceTax']
    itogoKvartal
    prevItogoKvartal
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
        return getData(formPrev).getDataRow(getData(formPrev).getAllCahced(),'itogoKvartal')
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

/**
 * Если «графа 13» > 0, то «графа 16» = 100
 * @param row
 * @return
 */
BigDecimal calc16(DataRow row) {
    BigDecimal result = null
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
    BigDecimal result = null
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
    getData(formData).insert(itogoKvartal,getData(formData).getAllCached().size()+1)
    getData(formData).insert(itogo,getData(formData).getAllCached().size()+1)
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
        if (row.getAlias()==null) {
            if (row.tradeNumber!=null && getCode(row.tradeNumber)==null){
                logger.warn('Код сделки в справочнике отсутствует!');
            }
            if (row.singSecurirty!=null && getSign(row.singSecurirty)==null){
                logger.warn('Признак ценной бумаги в справочнике отсутствует!');
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
        def index = getRows(data).indexOf(row) + 1
        def errorMsg = colNames.join(', ')
        logger.error("В строке $index не заполнены колонки : $errorMsg.")
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
            deleteAllStatic()
            sort()
            calc()
            addAllStatic()
            allCheck()
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
    def index
    def refDataProvider61 = refBookFactory.getDataProvider(61)
    def refDataProvider62 = refBookFactory.getDataProvider(62)

    def data = getData(formData)
    data.clear()

    // TODO (Ramil Timerbaev) Проверка корректности данных
    for (def row : xml.exemplar.table.detail.record) {
        index = 1

        def newRow = getNewRow()

        // графа 1
        newRow.rowNumber = getNumber(row.field[index].@value.text())
        index++

        // графа 2 - справочник 61 "Коды сделок"
        tmp = null
        if (row.field[index].@value.text() != null && row.field[index].@value.text().trim() != '') {
            tmp = getRecordId(refDataProvider61, 'CODE', getNumber(row.field[index].@value.text()))
        }
        newRow.tradeNumber = tmp
        index++

        // графа 3 - справочник 62 "Признаки ценных бумаг"
        tmp = null
        if (row.field[index].@value.text() != null && row.field[index].@value.text().trim() != '') {
            tmp = getRecordId(refDataProvider62, 'CODE', row.field[index].@value.text())
        }
        newRow.singSecurirty = tmp
        index++

        // графа 4
        newRow.issue = row.field[index].@value.text()
        index++

        // графа 5
        newRow.acquisitionDate = getDate(row.field[index].@value.text())
        index++

        // графа 6
        newRow.saleDate = getDate(row.field[index].@value.text())
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
    // проверка итоговых данных
    if (xml.exemplar.table.total.record.size() > 1 && !getRows(data).isEmpty()) {
        // графы 7-9, 11-13, 15, 17-22
        // TODO (Ramil Timerbaev) убрал нередактируемые вычисляемые графы (их итоги)
        def columnsAlias = ['amountBonds': 7, 'acquisitionPrice': 8, 'costOfAcquisition': 9,
                'marketPriceInRub': 11, /*'acquisitionPriceTax': 12,*/ 'redemptionValue': 13,
                'priceInFactRub': 15, /*'marketPriceInRub1': 17, 'salePriceTax': 18,*/ 'expensesOnSale': 19 /*,
                'expensesTotal': 20, 'profit': 21, 'excessSalePriceTax': 22*/]

        index = 0
        for (def row : xml.exemplar.table.total.record) {
            index++
            def totalRow = (index == 1 ? getItogoKvartal() : getItogoKvartal())

            // сравнить посчитанные суммы итогов с итогами из транспортного файла (графы 7-9, 11-13, 15, 17-22)
            def exit = false
            columnsAlias.each { alias, i ->
                if (!exit && totalRow.getCell(alias).getValue() != getNumber(row.field[i].@value.text())) {
                    logger.error('Итоговые значения неправильные.')
                    exit = true
                }
            }
            if (exit) {
                return
            }
        }
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
 * Получить идентификатор записи из справочника.
 *
 * @param provider справочник
 * @param searchByAlias алиас атрибута справочника по которому ищется запись
 * @param value значение по которому ищется запись
 */
def getRecordId(def provider, def searchByAlias, def value) {
    def records = provider.getRecords(new Date(), null, searchByAlias + " = '" + value + "'", null);
    if (records != null && !records.getRecords().isEmpty()) {
        return records.getRecords().get(0).get('record_id').getNumberValue()
    }
    return null
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