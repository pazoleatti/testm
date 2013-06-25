import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import com.aplana.sbrf.taxaccounting.model.WorkflowState

/**
 * 6.5	(РНУ-51) Регистр налогового учёта финансового результата от реализации (выбытия) ОФЗ
 */

switch (formDataEvent) {
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
        addNewRowInFormData()
        break
    case FormDataEvent.DELETE_ROW:
        deleteRow()
        break
}

/**


 № графы	ALIAS                   Тип поля                Наименование поля
 0          fix                     Строка 255              Скрытое поле для строки итого
 1.		    rowNumber               Число/15/               № пп	            		                                                    Натуральное число( ≥1)
 2.		    tradeNumber             Число/1/                Код сделки			                                                            Может принимать значения: 1 ИЛИ 2 ИЛИ 4 ИЛИ 5 1 - сделки на ОРЦБ, кроме переговорных; 2 - переговорные сделки на ОРЦБ; 4 - погашение, в т.ч. частичное; 5 - операции, связанные с открытием-закрытием короткой позиции.
 3.		    singSecurirty           Строка /1/	            Признак ценной бумаги	                                                        Может принимать значения «+» или «-»
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
    for (DataRow row in formData.dataRows) {
        if (row.getAlias() == null) {
            // 1.Проверка цены приобретения для целей налогообложения (графа 12)
            if (row.acquisitionPrice > row.marketPriceInRub && row.acquisitionPriceTax == row.marketPriceInRub) {
                logger.error("Неверно определена цена приобретения для целей налогообложения")
            }

            // 2.Проверка рыночной цены в процентах при погашении (графа 16)
            if (row.redemptionValue != null && row.redemptionValue > 0 && !(row.marketPriceInPerc1 == 100)) {
                logger.error("Неверно указана рыночная цена в % при погашении!")
            }

            // 3.Проверка рыночной цены в рублях при погашении (графа 17)
            if (row.redemptionValue != null && row.redemptionValue > 0 && !(row.marketPriceInRub1 == row.redemptionValue)) {
                logger.error("Неверно указана рыночная цена в рублях при погашении!")
            }

            /**
             * Проверка цены реализации (выбытия) для целей налогообложения (графа 18)
             * Если «графа 2» = 1 или 2 или 5, и при этом «графа 14» > «графа 16» и «графа 15» > «графа 17», то «графа 18» = «графа 15»
             */
            if ((row.tradeNumber == 1 || row.tradeNumber == 2 || row.tradeNumber == 5) && row.priceInFactPerc > row.marketPriceInPerc1
                    && row.priceInFactRub > row.marketPriceInRub1 && row.salePriceTax != row.priceInFactRub
            ) {
                logger.error('Неверно определена цена реализации для целей налогообложения по сделкам на ОРЦБ!')
            }

            /**
             * Проверка цены реализации для целей налогообложения при погашении (графа 18)
             * Если «графа 2» = 4, то «графа 18» = «графа 13»
             * 1
             * Неверно определена цена реализации для целей налогообложения при погашении!
             */
            if (row.tradeNumber == 4 && row.salePriceTax != row.redemptionValue) {
                logger.error('Неверно определена цена реализации для целей налогообложения при погашении!')
            }

            /**
             * Проверка цены реализации для целей налогообложения по переговорным сделкам на ОРЦБ и сделкам, связанным с открытием-закрытием короткой позиции (графа 18)
             * Если «графа 2» = 2 или 5, и («графа 14» < «графа 16», и «графа 15» < «графа 17», то «графа 18» = «графа 17»)
             * 1
             * Неверно определена цена реализации для целей налогообложения по переговорным сделкам на ОРЦБ и сделкам, связанным с открытием-закрытием короткой позиции!
             */
            if ((row.tradeNumber == 2 || row.tradeNumber == 5) && row.tradeNumber < row.marketPriceInPerc1
                    && row.priceInFactRub < row.marketPriceInRub1 && row.salePriceTax != row.marketPriceInRub1) {
                logger.error('Неверно определена цена реализации для целей налогообложения по переговорным сделкам на ОРЦБ и сделкам, связанным с открытием-закрытием короткой позиции!')
            }

            /**
             * Проверка итоговой суммы расходов (графа 20)
             * «графа 20» = «графа 9» + «графа 12» + «графа 19»
             * 1
             * Неверно определены расходы!
             */
            if (row.expensesTotal != row.costOfAcquisition + row.acquisitionPriceTax + row.expensesOnSale) {
                logger.error('Неверно определены расходы!')
            }

            /**
             * Проверка суммы финансового результата (графа 21)
             * «графа 21» = «графа 18» - «графа 20»
             * 1
             * Неверно определен финансовый результат реализации (выбытия)!
             */
            if (row.profit != row.salePriceTax - row.expensesTotal) {
                logger.error('Неверно определен финансовый результат реализации (выбытия)!')
            }

            /**
             * Проверка превышения цены реализации для целей налогообложения над фактической ценой реализации (графа 22)
             1.	если «графа 2» ≠ 4, то «графа 22» = «графа 18» - «графа 15»
             2.	если «графа 2» = 4, то «графа 22» = 0
             3.	значение «графы 22» ≥ 0
             1
             Неверно определено превышение цены реализации для целей налогообложения над фактической ценой реализации!
             @todo block http://jira.aplana.com/browse/SBRFACCTAX-2883
             */

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
        DataRow realItogoKvartal = formData.dataRows.get(formData.dataRows.size() - 2)
        itogoKvartal
        for (String alias in itogoSum) {
            if (realItogoKvartal.getCell(alias).value != itogoKvartal.getCell(alias).value) {
                logger.error("Итоговые значения за текущий квартал рассчитаны неверно!")
                break
            }
        }

        /**
         * Проверка корректности расчета итоговых значений за текущий отчётный (налоговый) период
         * Графы 7-9, 11-13, 15, 17-22  строки «Итого за текущий отчётный (налоговый) период» содержат значения, рассчитанные согласно алгоритму расчета из Табл. 174
         * 1
         * Итоговые значения за текущий отчётный (налоговый) период рассчитаны неверно!
         */
        DataRow realItogo = formData.dataRows.get(formData.dataRows.size() - 1)
        itogo
        for (String alias in itogoSum) {
            if (realItogo.getCell(alias).value != itogo.getCell(alias).value) {
                logger.error("Итоговые значения за текущий квартал рассчитаны неверно!")
                break
            }
        }
    }
}

/**
 * Вставка строки в случае если форма генирует динамически строки итого (на основе данных введённых пользователем)
 */
void addNewRowInFormData() {
    DataRow<Cell> newRow = formData.createDataRow()
    int index // Здесь будет позиция вставки

    if (formData.dataRows.size() > 0) {
        DataRow<Cell> selectRow
        // Форма не пустая
        log("Форма не пустая")
        log("size = " + formData.dataRows.size())
        if (currentDataRow != null && formData.dataRows.indexOf(currentDataRow) != -1) {
            // Значит выбрал строку куда добавлять
            log("Строка вставки выбрана")
            log("indexOf = " + formData.dataRows.indexOf(currentDataRow))
            selectRow = currentDataRow
        } else {
            // Строку не выбрал поэтому добавляем в самый конец
            log("Строка вставки не выбрана, поставим в конец формы")
            selectRow = formData.dataRows.get(formData.dataRows.size() - 1) // Вставим в конец
        }

        int indexSelected = formData.dataRows.indexOf(selectRow)
        log("indexSelected = " + indexSelected.toString())

        // Определим индекс для выбранного места
        if (selectRow.getAlias() == null) {
            // Выбрана строка не итого
            log("Выбрана строка не итого")
            index = indexSelected // Поставим на то место новую строку
        } else {
            // Выбрана строка итого, для статических строг итого тут проще и надо фиксить под свою форму
            // Для динимаческих строк итого идём вверх пока не встретим конец формы или строку не итого
            log("Выбрана строка итого")

            for (index = indexSelected; index >= 0; index--) {
                log("loop index = " + index.toString())
                if (formData.dataRows.get(index).getAlias() == null) {
                    log("Нашел строку отличную от итого")
                    index++
                    break
                }
            }
            if (index < 0) {
                // Значит выше строки итого нет строк, добавим новую в начало
                log("выше строки итого нет строк")
                index = 0
            }
            log("result index = " + index.toString())
        }
    } else {
        // Форма пустая поэтому поставим строку в начало
        log("Форма пустая поэтому поставим строку в начало")
        index = 0
    }
    formData.dataRows.add(index, newRow)
    [
            'tradeNumber', 'singSecurirty', 'issue', 'acquisitionDate', 'saleDate', 'amountBonds', 'acquisitionPrice', 'costOfAcquisition',
            'marketPriceInPerc', 'marketPriceInRub', 'redemptionValue', 'priceInFactPerc', 'priceInFactRub', 'expensesOnSale'
    ].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
}

DataRow<Cell> getItogo() {
    DataRow<Cell> itogo = formData.createDataRow()
    itogo.setAlias("itogo")
    itogo.getCell('fix').colSpan = 7
    itogo.fix = "Итого"
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
    return itogo
}

/**
 * Получает строку итого за квартал в преведущей НФ(ПРИНЯТОЙ) или null если формы
 * @return
 */
DataRow<Cell> getPrevItogoKvartal() {
    FormData formPrev = getFormPrev()
    if (formPrev != null && formPrev.state == WorkflowState.ACCEPTED) {
        return formPrev.getDataRow('itogoKvartal')
    }
    return null
}

FormData getFormPrev() {
    ReportPeriod period = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
    FormData formPrev = null
    if (period != null) {
        formPrev = FormDataService.find(formData.formType.id, formData.kind, formData.departmentId, period.id)
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
    for (DataRow row in formData.dataRows) {
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
    return itogo
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
    return round(result, 3)
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
    return round(result, 3)
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
    if ((row.tradeNumber == 1 || row.tradeNumber == 2 || row.tradeNumber == 5) && (row.priceInFactPerc > row.marketPriceInPerc1 && row.priceInFactRub > row.marketPriceInRub1)) {
        result = row.priceInFactRub
    }
    if (row.tradeNumber == 4) {
        result = row.redemptionValue
    }
    if ((row.tradeNumber == 2 || row.tradeNumber == 5) && (row.priceInFactPerc < row.marketPriceInPerc1 && row.priceInFactRub < row.marketPriceInRub1)) {
        result = row.marketPriceInRub1
    }
    return round(result, 3)
}

/**
 «графа 20» = «графа 9» + «графа 12» + «графа 19»
 * @param row
 * @return
 */
BigDecimal calc20(DataRow row) {
    BigDecimal result = row.costOfAcquisition + row.acquisitionPriceTax + row.expensesOnSale
    return round(result, 3)
}

/**
 «графа 21» = «графа 18» -«графа 20»
 * @param row
 * @return
 */
BigDecimal calc21(DataRow row) {
    BigDecimal result = row.salePriceTax - row.expensesTotal
    return round(result, 3)
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
    BigDecimal result
    if (row.tradeNumber == 4) {
        result = 0
    } else {
        result = row.salePriceTax - row.priceInFactRub
    }
    return round(result, 3)
}

void calc() {
    for (DataRow row in formData.dataRows) {
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
    BigDecimal result
    if (row.acquisitionPrice > row.marketPriceInRub) {
        result = row.marketPriceInRub
    } else {
        result = row.acquisitionPrice
    }
    return round(result, 2)
}

/**
 * Удаляет все статические строки(ИТОГО) во всей форме
 */
void deleteAllStatic() {
    Iterator<DataRow> iterator = formData.dataRows.iterator() as Iterator<DataRow>
    while (iterator.hasNext()) {
        row = (DataRow) iterator.next()
        if (row.getAlias() != null) {
            iterator.remove()
        }
    }
}

/**
 * Сортирует форму в соответвие с требованиями 6.11.2.1	Перечень полей формы
 */
void sort() {
    formData.dataRows.sort({ DataRow a, DataRow b ->
        if (a.tradeNumber == b.tradeNumber && a.singSecurirty == b.singSecurirty) {
            return a.issue <=> b.issue
        }
        if (a.tradeNumber == b.tradeNumber) {
            return a.singSecurirty <=> b.singSecurirty
        }
        return a.tradeNumber <=> b.tradeNumber
    })
}

void addAllStatic() {
    formData.dataRows.add(itogoKvartal)
    formData.dataRows.add(itogo)
}

void allCheck() {
    logicalCheck()
}

/**
 * Хелпер для округления чисел
 * @param value
 * @param newScale
 * @return
 */
BigDecimal round(BigDecimal value, int newScale) {
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
    return formData.dataRows.indexOf(row) + 1
}

/**
 * Удаляет строку из формы
 */
void deleteRow() {
    if (currentDataRow != null && currentDataRow.getAlias() == null) {
        formData.dataRows.remove(currentDataRow)
    }
}