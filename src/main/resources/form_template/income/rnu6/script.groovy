package form_template.income.rnu6

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper

import java.text.SimpleDateFormat

/**
 * 6.3	(РНУ-6) Справка бухгалтера для отражения доходов, учитываемых в РНУ-4, учёт которых требует применения метода начисления
 */

// графа 1  Число/15/                       number
// helper   Строка/1000                     helper
// графа 2  А140/CODE/Строка/15/            kny
// графа 3  Дата                            date
// графа 4  A350/NUMBER/Строка/12/          code
// графа 5  Строка/15                       docNumber
// графа 6  Дата/ДД.ММ.ГГГГ                 docDate
// графа 7  A64/CODE/Строка/3/              currencyCode
// графа 8  Число/19.4/                     rateOfTheBankOfRussia
// графа 9  Число/17.2/                     taxAccountingCurrency
// графа 10 Число/17.2/                     taxAccountingRuble
// графа 11 Число/17.2/                     accountingCurrency
// графа 12 Число/17.2/                     ruble

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkCreation()
        break
    case FormDataEvent.CHECK:
        form = dataRowsHelper
        logicCheckBefore(form)
        if (!logger.containsLevel(LogLevel.ERROR)) {
            logicalCheck(form)
            NSICheck(form)
        }
        break
    case FormDataEvent.CALCULATE:
        form = dataRowsHelper
        logicCheckBefore(form)
        if (!logger.containsLevel(LogLevel.ERROR)) {
            deleteAllStatic(form)
            sort(form)
            calc(form)
            addAllStatic(form)
            logicalCheck(form)
            NSICheck(form)
            form.save(form.getAllCached())
        }
        break
    case FormDataEvent.ADD_ROW:
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW:
        deleteRow()
        break
    case FormDataEvent.COMPOSE:
        def form = dataRowsHelper
        consolidation(form)
        sort(form)
        form.save(form.getAllCached())
        addAllStatic(form)
        //if(logicalCheck(form)){//TODO падает
            form.commit()
        //}
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:
        form = dataRowsHelper
        consolidationForms = formDataConsolidation
        for (formConsolidation in consolidationForms) {
            if (formConsolidation.getState() == WorkflowState.ACCEPTED) {
                logger.error("Подготовка первичной налоговой формы невозможно, т.к. уже подготовлена консолидированная налоговая форма.")
            }
        }
        logicCheckBefore(form)
        if (!logger.containsLevel(LogLevel.ERROR)) {
            logicalCheck(form)
            NSICheck(form)
        }
        break
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED:
        form = dataRowsHelper
        consolidationForms = formDataConsolidation
        for (formConsolidation in consolidationForms) {
            if (formConsolidation.getState() == WorkflowState.ACCEPTED) {
                logger.error("Утверждение первичной налоговой формы невозможно, т.к. уже подготовлена консолидированная налоговая форма.")
            }
        }
        logicCheckBefore(form)
        if (!logger.containsLevel(LogLevel.ERROR)) {
            logicalCheck(form)
            NSICheck(form)
        }
        break
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED:
        form = dataRowsHelper
        consolidationForms = formDataConsolidation
        for (formConsolidation in consolidationForms) {
            if (formConsolidation.getState() == WorkflowState.ACCEPTED) {
                logger.error("Принятие первичной налоговой формы невозможно, т.к. уже подготовлена консолидированная налоговая форма.")
            }
        }
        logicCheckBefore(form)
        if (!logger.containsLevel(LogLevel.ERROR)) {
            logicalCheck(form)
            NSICheck(form)
            if (!logger.containsLevel(LogLevel.ERROR)) {
                // (Ramil Timerbaev) убрал compose потому что он выполяется в ядре.
                // При выполнении на стороне скрипта compose доступен только для некоторых событии AFTER_MOVE_*
                // acceptance()
            }
        }
        break
    case FormDataEvent.MOVE_ACCEPTED_TO_APPROVED:
        form = dataRowsHelper
        consolidationForms = formDataConsolidation
        for (formConsolidation in consolidationForms) {
            if (formConsolidation.getState() == WorkflowState.ACCEPTED) {
                logger.error("Отмена принятия первичной налоговой формы невозможно, т.к. уже подготовлена консолидированная налоговая форма.")
            }
        }
        logicCheckBefore(form)
        if (!logger.containsLevel(LogLevel.ERROR)) {
            logicalCheck(form)
            NSICheck(form)
        }
        break

}

/**
 * Принять.
 */
void acceptance() {
    for (target in departmentFormTypeService.getFormDestinations(formData.departmentId, formData.getFormType().getId(), formData.getKind())) {
        formDataCompositionService.compose(formData, target.departmentId, target.formTypeId, target.kind)
    }
}

List <FormData> getFormDataConsolidation() {
    List <FormData> result = new ArrayList<>()
    for (destionationFormType in departmentFormTypeService.getFormDestinations(formData.departmentId, formData.getFormType().getId(), formData.getKind())){
        if (destionationFormType.formTypeId == formData.getFormType().getId()) {
            FormData form = formDataService.find(destionationFormType.formTypeId, destionationFormType.kind, destionationFormType.departmentId, formData.reportPeriodId)
            if (form != null) {
                result.add(form)
            }
        }
    }
    return result
}

DataRowHelper getDataRowsHelper() {
    DataRowHelper dataRowsHelper = null
    if (formData.id != null) {
        dataRowsHelper = formDataService.getDataRowHelper(formData)
    }
    return dataRowsHelper
}

void logicCheckBefore(DataRowHelper form) {
    columns = ['kny', 'date', 'code', 'docNumber', 'docDate', 'currencyCode', 'taxAccountingCurrency', 'rateOfTheBankOfRussia', 'taxAccountingRuble', 'accountingCurrency', 'ruble']
    if (formDataEvent == FormDataEvent.CALCULATE) {
        columns -= ['rateOfTheBankOfRussia', 'taxAccountingRuble', 'ruble']
    }
    for (row in form.allCached) {
        if (row.getAlias() == null) {
            if(!checkRequiredColumns(row,columns)){
                return
            }
        }
    }
    if (checkUniq456(form)) {
        return
    }
}

void NSICheck(DataRowHelper form) {
    reportDate = reportPeriodService.getEndDate(formData.reportPeriodId)
    reportDate.set(Calendar.DATE, reportDate.get(Calendar.DATE) + 1)
    for (row in form.getAllCached()) {
        if (row.getAlias() == null) {
            // ПС 1 и 2
            incomeSBRFRecords = providerIncomeSBRF.getRecords(reportDate.time, null, null, null).records
            isFind = false
            isFind2 = false
            for (record in incomeSBRFRecords) {
                if (row.kny == record.get(RefBook.RECORD_ID_ALIAS).getNumberValue()) {
                    isFind = true
                }
                if (row.code == record.get(RefBook.RECORD_ID_ALIAS).getNumberValue()) {
                    isFind2 = true
                }
                if (isFind && isFind2) {
                    break
                }
            }

            def index = row.number
            def errorMsg
            if (index != null) {
                errorMsg = "В строке \"№ пп\" равной $index "
            } else {
                index = getIndex(row) + 1
                errorMsg = "В строке $index "
            }

            if (!isFind) {
                logger.warn(errorMsg + 'балансовый счёт в справочнике отсутствует!')
            }
            if (!isFind2) {
                logger.warn(errorMsg + 'балансовый счёт в справочнике отсутствует!')
            }
            // ПС 3
            records = providerClassValut.getRecords(reportDate.time, null, null, null).records
            isFind = false
            for (record in records) {
                if (row.currencyCode == record.get(RefBook.RECORD_ID_ALIAS).getNumberValue()) {
                    isFind = true
                    break
                }
            }
            if (!isFind) {
                logger.warn(errorMsg + 'неверно заполнена графа %s!', row.getCell('currencyCode').column.name)
            }
            // ПС 4
            records = getProviderCurrency().getRecords(row.date as Date, null,  'CODE_NUMBER = ' + row.currencyCode, null).records
            isFind = false
            for (record in records) {
                if (row.rateOfTheBankOfRussia == (record.get('RATE').getNumberValue() as BigDecimal).setScale(4, BigDecimal.ROUND_HALF_UP)) {
                    isFind = true
                    break
                }
            }
            if (!isFind) {
                logger.warn(errorMsg + 'неверно заполнена графа %s!', row.getCell('rateOfTheBankOfRussia').column.name)
            }
        }
    }
}

/**
 * Курс валют
 * @return
 */
RefBookDataProvider getProviderCurrency() {
    return refBookFactory.getDataProvider(22L);
}

/**
 * Получает провайдер Классификатор доходов ОАО «Сбербанк России» для целей налогового учёта
 * @return
 */
RefBookDataProvider getProviderIncomeSBRF() {
    return refBookFactory.getDataProvider(28L)
}

RefBookDataProvider getProviderClassValut() {
    return refBookFactory.getDataProvider(15L)
}

/**
 * Консолидация.
 */
void consolidation(DataRowHelper form) {

    // удалить все строки и собрать из источников их строки
    form.clear()

    for (formDataSource in departmentFormTypeService.getFormSources(formData.departmentId, formData.getFormType().getId(), formData.getKind())) {
        if (formDataSource.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(formDataSource.formTypeId, formDataSource.kind, formDataSource.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                sourceForm = formDataService.getDataRowHelper(source)
                for (row in sourceForm.allCached) {
                    if (row.getAlias() == null) {
                        form.insert(row, form.allCached.size() + 1)
                    }
                }
            }
        }
    }
    form.commit()
}

def logicalCheck(DataRowHelper form) {

    List<BigDecimal> uniq = new ArrayList<>(form.allCached.size())
    List<Map<Integer, Object>> docs = new ArrayList<>()
    List<Map<Integer, Object>> uniq456 = new ArrayList<>(form.allCached.size())
    for (row in form.allCached) {
        if (row.getAlias() == null) {
            // LC Проверка на уникальность записи по налоговому учету
            Map<Integer, Object> m = new HashMap<>();
            m.put(4, row.code );
            m.put(5, row.docNumber);
            m.put(6, row.docDate);
            if (uniq456.contains(m)) {
                def index = ((Integer)(form.allCached.indexOf(row) + 1))
                SimpleDateFormat dateFormat = new SimpleDateFormat('dd.MM.yyyy')
                logger.error("Для строки $index имеется  другая запись в налоговом учете с аналогичными значениями балансового счета=%s, документа № %s от %s.", getNumberAttribute(row.code).toString(), row.docNumber.toString(), dateFormat.format(row.docDate))
                return false
            }
            uniq456.add(m)

            def index = row.number
            def errorMsg
            if (index != null) {
                errorMsg = "В строке \"№ пп\" равной $index "
            } else {
                index = getIndex(row) + 1
                errorMsg = "В строке $index "
            }

            //logger.info('Проверка на заполнение поля «<Наименование поля>»')
            // LC Проверка на заполнение поля «<Наименование поля>»
            columns = ['number', 'kny', 'date', 'code', 'docNumber', 'docDate', 'currencyCode', 'rateOfTheBankOfRussia', 'taxAccountingCurrency', 'taxAccountingRuble', 'accountingCurrency', 'ruble']
            if (!checkRequiredColumns(row,columns)){
                return false
            }

            //logger.info('Проверка на нулевые значения')
            // Проверка на нулевые значения
            if (row.taxAccountingCurrency == row.taxAccountingRuble && row.taxAccountingRuble == row.accountingCurrency
                    && row.accountingCurrency == row.ruble && row.ruble == 0) {
                logger.error(errorMsg + 'все суммы по операции нулевые!')
                return false
            }

            //logger.info('Проверка, что не  отображаются данные одновременно по бухгалтерскому и по налоговому учету')
            if (row.taxAccountingCurrency == null && row.accountingCurrency == null
                    || row.taxAccountingCurrency == 0 && row.accountingCurrency == 0) {
                logger.error(errorMsg + 'не заполнены оба поля «%s» и «%s>»', row.getCell('taxAccountingCurrency').column.name, row.getCell('taxAccountingCurrency').column.name)
                return false
            }

            //logger.info('Проверка даты совершения операции и границ отчётного периода')
            // Проверка даты совершения операции и границ отчётного периода
            if (reportPeriodService.getStartDate(formData.reportPeriodId).time.time > row.date.time
                    || row.date.time > reportPeriodService.getEndDate(formData.reportPeriodId).time.time) {
                logger.error(errorMsg + 'дата совершения операции вне границ отчётного периода!')
                return false
            }

            // @todo LC Проверка на превышение суммы дохода по данным бухгалтерского учёта над суммой начисленного дохода
            Map<Integer, Object> map = new HashMap<>()
            map.put(5, row.docNumber)
            map.put(6, row.docDate)
            if (!docs.indexOf(map)) {
                docs.add(map)
                c12 = 0
                c10 = 0
                for(rowSum in form.allCached) {
                    if (rowSum.docNumber == row.docNumber && rowSum.docDate == row.docDate) {
                        c12 += rowSum.ruble
                        c10 += rowSum.taxAccountingRuble
                    }
                }
                if (!(c10 > c12)) {
                    logger.error(errorMsg + 'cумма данных бухгалтерского учёта превышает сумму начисленных платежей для документа %s от %s!', row.docNumber as String, rowSum.docDate as String)
                    return false
                }
            }

            //logger.info('Проверка на уникальность поля «№ пп»')
            // Проверка на уникальность поля «№ пп» SBRFACCTAX-3507
            if (uniq.contains(row.number)) {
                logger.error('Нарушена уникальность номера по порядку!')
                return false
            } else {
                uniq.add(row.number as BigDecimal)
            }

            // Проверка соответствия балансового счета коду налогового учета
            if (!(row.kny == row.code)) {
                logger.error('Балансовый счет не соответствует коду налогового учета!')
                return false
            }
            // Арифметические проверки расчета неитоговых граф
            if (row.docNumber != '0000' && !(row.taxAccountingRuble == calc10(row))) {
                logger.error(errorMsg + 'неверно рассчитана графа %s!', row.getCell('taxAccountingRuble').column.name)
                return false
            }
            if (row.docNumber != '0000' && !(row.ruble == calc12(row))) {
                logger.error(errorMsg + 'неверно рассчитана графа %s!', row.getCell('ruble').column.name)
                return false
            }

            // Проверка наличия суммы дохода в налоговом учете, для первичного документа, указанного для суммы дохода в бухгалтерском учёте
            // Проверка значения суммы дохода в налоговом учете, для первичного документа, указанного для суммы дохода в бухгалтерском учёте
            if (row.docDate != null) {
                date = row.docDate as Date
                from = new GregorianCalendar()
                from.setTime(date)
                from.set(Calendar.YEAR, from.get(Calendar.YEAR) - 3)
                taxPeriods = taxPeriodService.listByTaxTypeAndDate(TaxType.INCOME, from.getTime(), date)
                isFind = false
                for (taxPeriod in taxPeriods) {
                    reportPeriods = reportPeriodService.listByTaxPeriod(taxPeriod.id)
                    for (reportPeriod in reportPeriods) {
                        findFormData = formDataService.find(formData.formType.id, formData.kind, formData.departmentId, reportPeriod.id)
                        if (findFormData != null) {
                            findForm = formDataService.getDataRowHelper(findFormData)
                            for (findRow in findForm.getAllCached()) {
                                // SBRFACCTAX-3531 исключать строку из той же самой формы не надо
                                if (findRow.code == row.code && findRow.docNumber == row.docNumber && findRow.docDate == row.docDate) {
                                    isFind = true
                                    if (!(findRow.ruble > row.ruble)) {
                                        logger.warn('Операция, указанная в строке %s, в налоговом учете имеет сумму, меньше чем указано в бухгалтерском учете! См. РНУ-6 в %s отчетном периоде.', row.number.toString(), reportPeriod.name)
                                    }
                                }
                            }
                        }
                    }
                }
                if (!isFind) {
                    logger.warn('Операция, указанная в строке %s, в налоговом учете за последние 3 года не проходила!', row.number.toString())
                }
            }

            // FIXME SBRFACCTAX-3535
        } else {
            // Итоговые строки

            if (row.getAlias() != 'itogo') {
                // Итого по КНУ

                // Арифметические проверки расчета итоговых строк «Итого по КНУ»
                itogoKNY = itogoKNY(form, form.getAllCached().indexOf(row) - 1)
                if (row.taxAccountingRuble != itogoKNY.taxAccountingRuble) {
                    logger.error('Неверное итоговое значение %s для графы %s', row.helper as String, row.getCell('taxAccountingRuble').column.name)
                    return false
                }
                if (row.ruble != itogoKNY.ruble) {
                    logger.error('Неверное итоговое значение %s для графы %s', row.helper as String, row.getCell('ruble').column.name)
                    return false
                }
            } else {
                // Общее итого

                // Арифметические проверки расчета строки общих итогов
                itogo = getItogo(form)
                if (row.taxAccountingRuble != itogo.taxAccountingRuble || row.ruble != itogo.ruble) {
                    logger.error('Неверное итоговое значение')
                    return false
                }
            }
        }
    }
    return true
}

/**
 * Проверить заполненость обязательных полей.
 *
 * @param row строка
 * @param columns список обязательных графов
 */
def checkRequiredColumns(def row, def columns) {
    def colNames = []

    def cell
    columns.each {
        cell = row.getCell(it)
        if (cell.getValue() == null || cell.getValue() == '') {
            def name = getColumnName(row, it)
            colNames.add('"' + name + '"')
        }
    }
    if (!colNames.isEmpty()) {
        def index = row.number
        def errorMsg = colNames.join(', ')
        if (index != null) {
            logger.error("В строке \"№ пп\" равной $index не заполнены колонки : $errorMsg.")
        } else {
            index = dataRowsHelper.allCached.indexOf(row) + 1
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

void addAllStatic(DataRowHelper form) {
    for (int i = 0; i < form.getAllCached().size(); i++) {
        row = form.getAllCached().get(i)
        newxRow = null

        if (i + 1 < form.getAllCached().size()) {
            newxRow = form.getAllCached().get(i + 1)
        }

        if (row.getAlias() == null && newxRow == null || newxRow != null && row.kny != newxRow.kny) {
            itogoKNY = itogoKNY(form, i)
            form.insert(itogoKNY, i + 2)
            i++
        }
    }

    rowItogo = getItogo(form)
    form.insert(rowItogo, form.getAllCached().size() + 1)
}

DataRow getItogo(DataRowHelper form) {
    newRow = formData.createDataRow()
    newRow.setAlias('itogo')
    newRow.getCell('helper').colSpan = 2
    newRow.helper = 'Итого'

    columns = ['taxAccountingRuble', 'ruble']

    for (column in columns) {
        newRow.getCell(column).value = new BigDecimal(0)
    }

    for (row in form.getAllCached()) {
        if (row.getAlias() == null) {
            for (column in columns) {
                if (row.getCell(column).value != null) {
                    newRow.getCell(column).value += row.getCell(column).value
                }
            }
        }
    }
    setTotalStyle(newRow)
    return newRow
}

/**
 * Считает итого по kny
 * @param form
 * @param i индекс последней строки в форме с целевым кну
 * @return
 */
DataRow itogoKNY(DataRowHelper form, int i) {
    newRow = formData.createDataRow()
    newRow.getCell('helper').colSpan = 2
    newRow.setAlias('itogoKNY#'.concat(i.toString()))
    StringBuffer sb = new StringBuffer("Итого по КНУ ");
    String kny = null
    // Получим КНУ вверху строки может быть другая строка итого(гипотетически) и надо искать строку внесённую пользователем
    for (int j = i; j >= 0; j--) {
        if (form.getAllCached().get(j).getAlias() == null) {
            kny = getKNY(form.getAllCached().get(j).kny as Long)
            sb.append(kny)
            break
        }
    }
    newRow.helper = sb.toString()

    columns = ['taxAccountingRuble', 'ruble']

    for (column in columns) {
        newRow.getCell(column).value = new BigDecimal(0)
    }

    for (j = i; j >= 0; j--) {
        row = form.getAllCached().get(j)
        if (row.getAlias() == null && kny.equals(getKNY(row.kny as Long))) {
            for (column in columns) {
                if (row.getCell(column).value != null) {
                    newRow.getCell(column).value += row.getCell(column).value
                }
            }
        }
    }
    setTotalStyle(newRow)

    return newRow
}

/**
 * Сортирует форму в соответвие с требованиями 6.3.3	Перечень полей формы
 */
void sort(DataRowHelper form) {
    form.getAllCached().sort({ DataRow a, DataRow b ->
        if (a.kny == b.kny && a.code == b.code) {
            return a.date.time <=> b.date.time
        }
        if (a.kny == b.kny) {
            return getBS(a.code as Long) <=> getBS(b.code as Long)
        }
        return getKNY(a.kny as Long) <=> getKNY(b.kny as Long)
    })
}

void calc(DataRowHelper form) {
    // Расчеты не надо делать на
    if (formData.kind == FormDataKind.PRIMARY) {
        for (row in form.allCached) {
            if (row.getAlias() == null) {
                row.number = calc1(row)
                row.rateOfTheBankOfRussia = calc8(row)
                row.taxAccountingRuble = calc10(row)
                row.ruble = calc12(row)
            }
        }
    }
}

BigDecimal calc1(DataRow row) {
    return new BigDecimal(dataRowsHelper.getAllCached().indexOf(row) + 1)
}

BigDecimal calc8(DataRow row) {
    result = 0
    if (get(providerClassValut, row.currencyCode as Long, 'CODE').getStringValue() == '810') {
        result = 1
    } else {
        result = getCurrency(row.date as Date, row.currencyCode as Long)
        if (result == null) {
            throw new javax.script.ScriptException('Неизвестный курс валют')
        }
    }
    return result
}

/**
 * «Курс валюты» справочника «Курсы валют» на дату
 * @param row
 * @return
 */
BigDecimal getCurrency(Date date, Long code) {
    records = providerCurrency.getRecords(date, null, 'CODE_NUMBER = ' + code, null).getRecords() // отфильтровать по валюте SBRFACCTAX-3482
    if (records.size() > 0 && records.get(0).containsKey('RATE')) {
        return records.get(0).get('RATE').getNumberValue();
    } else {
        throw new javax.script.ScriptException('Не могу получить recordId курс')
    }
}

BigDecimal calc10(DataRow row) {
    result = null
    if (row.docNumber == '0000') {
        // @todo SBRFACCTAX-3469
    } else {
        result = row.taxAccountingCurrency * row.rateOfTheBankOfRussia
    }
    return result.setScale(2, BigDecimal.ROUND_HALF_UP)
}

BigDecimal calc12(DataRow row) {
    // FIXME SBRFACCTAX-3534
    result = new BigDecimal(0)
    if (row.docNumber == '0000') {
        // @todo SBRFACCTAX-3469
    } else {
        result = row.accountingCurrency * row.rateOfTheBankOfRussia
    }
    return result.setScale(2, BigDecimal.ROUND_HALF_UP)
}

RefBookValue get(RefBookDataProvider provider, Long recordId, String alias) {
    result = provider.getRecordData(recordId)
    if (result.containsKey(alias)) {
        return result.get(alias)
    } else {
        return null
    }
}

String getKNY(Long recordId) {
    result = get(providerIncomeSBRF, recordId, 'CODE')
    if (result == null) {
        return new String()
    }
    return result
}

String getBS(Long recordId) {
    result = get(providerIncomeSBRF, recordId, 'NUMBER')
    if (result == null) {
        return new String()
    }
    return result
}

/**
 * Удаляет строку из формы
 */
void deleteRow() {
    if (currentDataRow != null) {
        dataRowsHelper.delete(currentDataRow)
    }
}

/**
 * Удаляет все статические строки(ИТОГО) во всей форме
 */
void deleteAllStatic(DataRowHelper form) {
    List<DataRow<Cell>> forDelete = new ArrayList<>();
    for (row in form.allCached) {
        if (row.getAlias() != null) {
            forDelete.add(row)
        }
    }
    for (row in forDelete) {
        form.delete(row)
    }
}

/**
 * Вставка строки в случае если форма генирует динамически строки итого (на основе данных введённых пользователем)
 */
void addNewRow() {
    DataRow<Cell> newRow = formData.createDataRow()

    def form = dataRowsHelper
    [
            'kny', 'date', 'code', 'docNumber', 'docDate', 'currencyCode', 'taxAccountingCurrency', 'accountingCurrency'
    ].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    def index = 0
    if (currentDataRow!=null){
        index = currentDataRow.getIndex()
        def row = currentDataRow
        while(isTotal(row) && index>0){
            row = getRows(form).get(--index)
        }
        if(index!=currentDataRow.getIndex() && !isTotal(getRows(form).get(index))){
            index++
        }
    }else if (getRows(form).size()>0) {
        for(int i = getRows(form).size()-1;i>=0;i--){
            def row = getRows(form).get(i)
            if(!isTotal(row)){
                index = getRows(form).indexOf(row)+1
                break
            }
        }
    }
    form.insert(newRow,index+1)
}

/**
 * Проверка при создании формы.
 */
void checkCreation() {
    // отчётный период
    reportPeriod = reportPeriodService.get(formData.reportPeriodId)

    //проверка периода ввода остатков
    if (reportPeriod != null && reportPeriodService.isBalancePeriod(reportPeriod.id, formData.departmentId)) {
        logger.error('Налоговая форма не может создаваться в периоде ввода остатков.')
        return
    }

    findForm = formDataService.find(formData.formType.id,
            formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }
}

/**
 * Получить атрибут 130 - "Код налогового учёта" справочник 28 - "Классификатор доходов Сбербанка России для целей налогового учёта".
 *
 * @param id идентификатор записи справочника
 */
def getNumberAttribute(def id) {
    return refBookService.getStringValue(28, id, 'NUMBER')
}

/**
 * Устаносить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    ['number', 'helper', 'kny', 'date', 'code', 'docNumber', 'docDate',
            'currencyCode', 'rateOfTheBankOfRussia', 'taxAccountingCurrency',
            'taxAccountingRuble', 'accountingCurrency', 'ruble'].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
}

def checkUniq456(def form) {
    // 9. Проверка на уникальность записи по налоговому учету
    // список значенией граф 4,5,6
    boolean result = true
    SimpleDateFormat dateFormat = new SimpleDateFormat('dd.MM.yyyy')

    List<Map<Integer, Object>> uniq456 = new ArrayList<>()
    Map<Object, List<Integer>> notUniq = new HashMap<>()
    for (row in form.allCached) {
        if (!isTotal(row)) {
            Map<Integer, Object> m = new HashMap<>();

            m.put(4, row.code );
            m.put(5, row.docNumber);
            m.put(6, row.docDate);
            if (notUniq.get(m) != null) {
                List<Integer> tmpList = notUniq.get(m)
                tmpList.add(row.number)
                notUniq.put(m, tmpList)

            } else {
                notUniq.put(m, new ArrayList<Integer>([row.number]))
            }
            uniq456.add(m)
        }
    }
    if (!notUniq.isEmpty()) {
        notUniq = notUniq.sort {it.value}
        for (def item : notUniq) {
            if (item.value.size() > 1)
            {
                StringBuilder numberList = new StringBuilder()
                item.getValue().each { rNum ->
                    if (numberList.length() != 0) {
                        numberList.append(', ')
                    }
                    numberList.append(rNum)
                }
                logger.error("Несколько строк $numberList содержат записи в налоговом учете для балансового счета=%s, документа № %s от %s", getNumberAttribute(item.getKey().get(4)).toString(), item.getKey().get(5).toString(), dateFormat.format(item.getKey().get(6)))
                result = false
            }
        }
    }
    return result
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
 * Проверка является ли строка итоговой.
 */
def isTotal(def row) {
    return row != null && row.getAlias() != null
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
