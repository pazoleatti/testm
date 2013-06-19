import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.Column
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent

/**
 * 6.41	(РНУ-60) Регистр налогового учёта закрытых сделок РЕПО с обязательством покупки по 2-й части
 * ЧТЗ http://conf.aplana.com/pages/viewpage.action?pageId=8588102 ЧТЗ_сводные_НФ_Ф2_Э1_т2.doc
 * @author ekuvshinov
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

void log(String message, Object... args) {
    //logger.info(message, args)
}

/**

 № графы    ALIAS 	            Наименование поля	                                                        Тип поля	    Формат
 1.	        tradeNumber         Номер сделки первая часть / вторая часть	                                Строка /41/
 2.		    securityName        Наименование ценной бумаги	                                                Строка /255/
 3.		    currencyCode        Код валюты	                                                                Строка /3/		                Должно содержать значение поля «Код валюты. Цифровой» справочника «Общероссийский классификатор валют»			Нет	Да
 4.		    nominalPrice        Номинальная стоимость ценных бумаг (ед. вал.)	                            Число/17.2/
 5.		    part1REPODate       Дата первой части РЕПО	                                                    Дата	        ДД.ММ.ГГГ
 6.		    part2REPODate       Дата второй части РЕПО	                                                    Дата	        ДД.ММ.ГГГ
 7.		    acquisitionPrice    Стоимость реализации, в т.ч. НКД, по первой части РЕПО (руб.коп.)	        Число/17.2/
 8.		    salePrice           Стоимость приобретения, в т.ч. НКД, по второй части РЕПО (руб.коп.)	        Число/17.2/
 9.		    income              Доходы (-) по сделке РЕПО (руб.коп.)	                                    Число/17.2/
 10.	    outcome             Расходы (+) по сделке РЕПО (руб.коп.)	                                    Число/17.2/
 11.	    rateBR              Ставка Банка России (%)	                                                    Число/17.2/
 12.	    outcome269st        Расходы по сделке РЕПО, рассчитанные с учётом ст. 269 НК РФ (руб.коп.)	    Число/17.2/
 13.	    outcomeTax          Расходы по сделке РЕПО, учитываемые для целей налогообложения (руб.коп.)	Число/17.2/

 */

void allCheck() {
    logicalCheck()
}

/**
 * 6.41.3.2.1	Логические проверки
 * Табл. 209 Логические проверки формы «Регистр налогового учёта закрытых сделок РЕПО с обязательством покупки по 2-й части»
 */
void logicalCheck() {
    for (row in formData.dataRows) {
        if (row.getAlias() == null) {
            // 1. Проверка на заполнение поля «<Наименование поля>»
            for (alias in ['outcome269st', 'outcomeTax']) {
                if (row.getCell(alias).value == null) {
                    setError(row.getCell(alias).column)
                }
            }
            // 2. Проверка даты первой части РЕПО
            if (row.part1REPODate != null && reportDate.time.before((Date) row.part1REPODate)) {
                log(reportDate.time.toString())
                logger.error("Неверно указана дата первой части сделки!")
            }
            // 3. Проверка даты второй части РЕПО @todo проверить http://jira.aplana.com/browse/SBRFACCTAX-2851
            if (row.part2REPODate != null
                    && (reportPeriodService.getStartDate(formData.reportPeriodId).time.after((Date) row.part2REPODate) || reportPeriodService.getEndDate(formData.reportPeriodId).time.before((Date) row.part2REPODate)
            )) {
                log(reportPeriodService.getStartDate(formData.reportPeriodId).time.toString())
                log(reportPeriodService.getEndDate(formData.reportPeriodId).time.toString())
                logger.error("Неверно указана дата второй части сделки!")
            }

            // @TODO LC 4, 5 block http://jira.aplana.com/browse/SBRFACCTAX-2870 ниже вариант как в ЧТЗ возможно его стоит изменить
//            // 4. Проверка финансового результата
//            if (!(row.income > 0 && row.outcome == 0)) {
//                log('income = ' + row.income.toString())
//                log('outcome = ' + row.outcome.toString())
//                logger.error("Задвоение финансового результата!")
//            }
//
//            // 5. Проверка финансового результата
//            if (!(row.outcome > 0 && row.income == 0)) {
//                logger.error("Задвоение финансового результата!")
//            }
            // Проверка финансового результата в соответсвие с разговором в skype Гриша поправит аналитику
            if (row.outcome > 0 && row.income > 0) {
                logger.error("Задвоение финансового результата!")
            }

            // 6. Проверка финансового результата
            if (row.outcome == 0 && !(row.outcome269st == 0 && row.outcomeTax == 0)) {
                logger.error("Задвоение финансового результата!")
            }

            // 7. Проверка финансового результата
            BigDecimal temp = (row.salePrice ?: 0) - (row.acquisitionPrice ?: 0)
            if (temp < 0 && !(temp.abs() == row.income)) {
                logger.warn("Неверно определены доходы")
            }

            // 8. Проверка финансового результата
            if (temp > 0 && !(temp == row.outcome)) {
                log("temp = " + temp.toString())
                log("outcome = " + row.outcome.toString())
                logger.warn("Неверно определены расходы")
            }

            // 9. Арифметические проверки граф 9, 10, 11, 12, 13
            List checks = ['income', 'outcome', 'rateBR', 'outcome269st', 'outcomeTax']
            Map<String, BigDecimal> value = [:]
            value.put('income', calc9(row))
            value.put('outcome', calc10(row))
            value.put('rateBR', calc11(row))
            value.put('outcome269st', calc12(row))
            value.put('outcomeTax', calc13(row))
            for (String check in checks) {
                if (row.getCell(check).value != value.get(check)) {
                    log("calc = " + value.get(check).toString())
                    log("row = " + row.getCell(check).value.toString())
                    logger.error("Неверно рассчитана графа " + row.getCell(check).column.name.replace('%', '') + "!")
                }
            }
        }
    }
    // 10. Проверка итоговых значений по всей форме
    List itogoSum = ['nominalPrice', 'acquisitionPrice', 'salePrice', 'income', 'outcome', 'outcome269st', 'outcomeTax']
    DataRow realItogo = formData.dataRows.get(formData.dataRows.size() - 1)
    itogo
    for (String alias in itogoSum) {
        if (realItogo.getCell(alias).value != itogo.getCell(alias).value) {
            log("columnn = " + itogo.getCell(alias).column.name.toString())
            log("real = " + realItogo.getCell(alias).value.toString())
            log("calc = " + itogo.getCell(alias).value.toString())
            logger.error("Итоговые значения рассчитаны неверно!")
            break
        }
    }
}

void setError(Column c) {

    if (!c.name.empty) {
        logger.error('Поле ' + c.name.replace('%', '') + ' не заполнено')
    }
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
    formData.dataRows.add(itogo)
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
    for (DataRow row in formData.dataRows) {
        if (row.getAlias() == null) {
            for (String name in itogoSum) {
                if (itogo.getCell(name).value == null) {
                    itogo.getCell(name).value = row.getCell(name).value ?: 0
                } else {
                    itogo.getCell(name).value += row.getCell(name).value ?: 0
                }
            }
        }
    }
    return itogo
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
    return round(result, 2)
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
    return round(result, 2)
}

BigDecimal calc11(DataRow row) {
    Date date01_09_2008 = new Date(1220227200000)       // 1220227200000 - 01.09.2008 00:00 GMT
    Date date31_12_2009 = new Date(1262217600000)       // 1262217600000 - 31.12.2009 00:00 GMT
    Date date01_01_2011 = new Date(1293840000000)       // 1293840000000 - 01.01.2011 00:00 GMT
    Date date31_12_2012 = new Date(1356912000000)       // 1356912000000 - 31.12.2012 00:00 GMT
    BigDecimal result
    BigDecimal stavka = new BigDecimal(5)   // @todo Ставка рефенансирования  block http://jira.aplana.com/browse/SBRFACCTAX-2711
    /**
     * В следущих проверках нет надобности но они зачеме то указаны в чтз если что разкомитить.
     if (row.outcome == 0) {result = null}if (row.currencyCode == null) {result = null}*/
    if (row.currencyCode == 810) {
        result = stavka
    } else {
        if (row.part2REPODate != null && row.part2REPODate.compareTo(date01_09_2008) >= 0 && row.part2REPODate.compareTo(date31_12_2009) <= 0) {
            result = 22
        } else if (row.part2REPODate != null && row.part2REPODate.compareTo(date01_01_2011) >= 0 && row.part2REPODate.compareTo(date31_12_2012) <= 0) {
            result = stavka
        } else {
            result = 15
        }
    }
    return round(result, 2)
}

BigDecimal calc12(DataRow row) {
    Date date01_09_2008 = new Date(1220227200000)       // 1220227200000 - 01.09.2008 00:00 GMT
    Date date31_12_2009 = new Date(1262217600000)       // 1262217600000 - 31.12.2009 00:00 GMT
    Date date01_01_2011 = new Date(1293840000000)       // 1293840000000 - 01.01.2011 00:00 GMT
    Date date31_12_2012 = new Date(1356912000000)       // 1356912000000 - 31.12.2012 00:00 GMT
    Date date01_01_2010 = new Date(1262282400000)       // 1262282400000 - 01.01.2010 00:00 GMT
    Date date30_06_2010 = new Date(1277834400000)       // 1277834400000 - 30.06.2010 00:00 GMT
    Date date01_11_2009 = new Date(1257012000000)       // 1257012000000 - 01.11.2009 00:00 GMT
    BigDecimal result = null
    if (row.outcome != null && row.outcome > 0) {
        long difference = row.part2REPODate.getTime() - row.part1REPODate.getTime() / (1000 * 60 * 60 * 24) // необходимо получить кол-во в днях
        difference = difference == 0 ? 1 : difference   // Эти вычисления для того чтобы получить разницу в днях, если она нулевая считаем равной 1 так написано в чтз
        // @todo Возможно надо исправить 365 на 366 смотри и какой год проверять на высокостность http://jira.aplana.com/browse/SBRFACCTAX-2844
        if (row.currencyCode == 810) {
            if (row.part2REPODate.compareTo(date01_09_2008) >= 0 && row.part2REPODate.compareTo(date31_12_2009) <= 0) {
                /*
                a.	Если «графа 6» принадлежит периоду с 01.09.2008 по 31.12.2009, то:
                    «графа 12» = («графа 7» ? «графа 11» ? 1,5) ? ((«графа6» - «графа5») / 365 (366)) / 100;

                 */
                result = (row.acquisitionPrice * (row.rateBR ?: 0) * 1.5) * (difference / countDaysInYear) / 100
            } else if (row.part2REPODate.compareTo(date01_01_2010) >= 0 && row.part2REPODate.compareTo(date30_06_2010) <= 0 && row.part1REPODate.compareTo(date01_11_2009) < 0) {
                /*
                b.	Если «графа 6» принадлежит периоду с 01.01.2010 по 30.06.2010 и одновременно сделка открыта до 01.11.2009 («графа 5» < 01.11.2009 г.), то
                    «графа 12» = («графа 7» ? «графа 11» ? 2) ? ((«графа 6» - «графа 5») / 365 (366)) / 100;
                 */
                result = (row.acquisitionPrice * (row.rateBR ?: 0) * 2) * (difference / countDaysInYear) / 100
            } else if (row.part2REPODate.compareTo(date01_01_2010) >= 0 && row.part2REPODate.compareTo(date31_12_2012) <= 0) {
                /*
                c.	Если «графа 6» принадлежит периоду с 01.01.2010 по 31.12.2012, то:
                    «графа 12» = («графа 7» ? «графа 11» ? 1,8) ? ((«графа6» - «графа5») / 365(366)) / 100.
                 */
                result = (row.acquisitionPrice * (row.rateBR ?: 0) * 1.8) * (difference / countDaysInYear) / 100
            } else {
                /*
                d.	Иначе
                    «графа 12» = («графа 7» ? «графа 11» ? 1,1) ? ((«графа 6» -« графа 5») / 365 (366)) / 100;.
                 */
                result = (row.acquisitionPrice * (row.rateBR ?: 0) * 1.1) * (difference / countDaysInYear) / 100
            }
        } else {
            result = (row.acquisitionPrice * (row.rateBR ?: 0)) * (difference / countDaysInYear) / 100
            if (row.part2REPODate.compareTo(date01_01_2011) >= 0 && row.part2REPODate.compareTo(date31_12_2012) <= 0) {
                result = (row.acquisitionPrice * (row.rateBR ?: 0) * 0.8) * (difference / countDaysInYear) / 100
            }
        }
    }
    if (row.outcome != null && row.outcome == 0) {
        result = 0
    }
    return round(result, 2)
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
    for (DataRow row in formData.dataRows) {
        if (row.getAlias() == null) {
            row.income = calc9(row)
            row.outcome = calc10(row)
            row.rateBR = calc11(row)
            row.outcome269st = calc12(row)
            row.outcomeTax = calc13(row)
        }
    }
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
 * Сортирует форму в соответвие с требованиями 6.11.2.1	Перечень полей формы
 */
void sort() {
    formData.dataRows.sort({ DataRow a, DataRow b ->
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
        formData.dataRows.remove(currentDataRow)
    }
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
            'tradeNumber', 'securityName', 'currencyCode', 'nominalPrice', 'part1REPODate', 'part2REPODate', 'acquisitionPrice', 'salePrice'
    ].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
}