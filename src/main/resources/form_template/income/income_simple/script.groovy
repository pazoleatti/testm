package form_template.income.income_simple

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import com.aplana.sbrf.taxaccounting.model.TaxPeriod
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper

import java.text.SimpleDateFormat

/**
 * Сводная форма " Доходы, учитываемые в простых РНУ" уровня обособленного подразделения
 *
 * TODO:
 *      - не сделан подсчет графы 13 (контрольные графы) потому что справочники "Отчет о прибылях и убытках" и "Оборотная ведомость" еще не реализованы
 *
 * @since 6.06.2013
 * @author auldanov
 */

// Состав графов
// графа 1  - incomeTypeId              - КНУ
// графа 2  - incomeGroup               - Группа доходов
// графа 3  - incomeTypeByOperation     - Вид дохода по операции
// графа 4  - accountNo                 - Балансовый счёт по учёту дохода
// графа 5  - rnu6Field10Sum            - РНУ-6 (графа 10) cумма
// графа 6  - rnu6Field12Accepted       - сумма
// графа 7  - rnu6Field12PrevTaxPeriod  - в т.ч. учтено в предыдущих налоговых периодах по графе 10
// графа 8  - rnu4Field5Accepted        - РНУ-4 (графа 5) сумма
// графа 9  - logicalCheck              - Логическая проверка
// графа 10 - accountingRecords         - Счёт бухгалтерского учёта
// графа 11 - opuSumByEnclosure2        - в Приложении №5
// графа 12 - opuSumByTableD            - в Таблице "Д"
// графа 13 - opuSumTotal               - в бухгалтерской отчётности
// графа 14 - difference                - Расхождение

data = getData(formData)

/**
 * Роутинг приложения
 * formDataEvent (com.aplana.sbrf.taxaccounting.model.FormDataEvent)
 */
switch (formDataEvent) {
// создать
    case FormDataEvent.CREATE:
        checkCreation()
        break
// расчитать
    case FormDataEvent.CALCULATE:
        logicalCheck()
        calcForm()
        break
// обобщить
    case FormDataEvent.COMPOSE:
        DataRowHelper form = getData(formData)
        if (form != null) {
            consolidation(form)
            logicalCheck()
            calcForm()
        }
        break
// проверить
    case FormDataEvent.CHECK:
        logicalCheck()
        calcForm()
        break
// утвердить
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:
        logicalCheck()
        calcForm()
        break
// принять из утверждена
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED:
        logicalCheck()
        calcForm()
        break
// вернуть из принята в утверждена
    case FormDataEvent.MOVE_ACCEPTED_TO_APPROVED:
        break
// принять из создана
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:
        logicalCheck()
        calcForm()
        break
// вернуть из принята в создана
    case FormDataEvent.MOVE_ACCEPTED_TO_CREATED:
        checkDeclarationBankOnCancelAcceptance()
        break
// после принятия из утверждена
    case FormDataEvent.AFTER_MOVE_APPROVED_TO_ACCEPTED:
        break
// после вернуть из "Принята" в "Утверждена"
    case FormDataEvent.AFTER_MOVE_ACCEPTED_TO_APPROVED:
        checkDeclarationBankOnCancelAcceptance()
        break
}

/**
 * 6.1.2.8.2	Алгоритмы заполнения полей формы при расчете данных формы
 */
def calcForm() {
    /** КНУ 40001 */
    def row40001 = data.getDataRow(getRows(data), 'R53')
    row40001.rnu6Field10Sum = 0
    row40001.rnu6Field12Accepted = 0
    row40001.rnu6Field12PrevTaxPeriod = 0
    row40001.rnu4Field5Accepted = 0
    (2..52).each { n ->
        def row = data.getDataRow(getRows(data), 'R' + n)
        // «графа 5» =сумма значений  «графы 5» для строк с 2 по 52 (раздел «Доходы от реализации»)

        row40001.rnu6Field10Sum = (row40001.rnu6Field10Sum ?: 0) + (row.rnu6Field10Sum ?: 0)

        // «графа 6» =сумма значений  «графы 6» для строк с 2 по 52 (раздел «Доходы от реализации»)
        row40001.rnu6Field12Accepted = (row40001.rnu6Field12Accepted ?: 0) + (row.rnu6Field12Accepted ?: 0)

        // «графа 7» =сумма значений  «графы 7» для строк с 2 по 52 (раздел «Доходы от реализации»)
        row40001.rnu6Field12PrevTaxPeriod = (row40001.rnu6Field12PrevTaxPeriod ?: 0) + (row.rnu6Field12PrevTaxPeriod ?: 0)

        // «графа 8» =сумма значений  «графы 8» для строк с 2 по 52 (раздел «Доходы от реализации»)
        row40001.rnu4Field5Accepted = (row40001.rnu4Field5Accepted ?: 0) + (row.rnu4Field5Accepted ?: 0)
    }

    /** КНУ 40002 */
    def row40002 = data.getDataRow(getRows(data), 'R156')
    row40002.rnu6Field10Sum = 0
    row40002.rnu6Field12Accepted = 0
    row40002.rnu6Field12PrevTaxPeriod = 0
    row40002.rnu4Field5Accepted = 0
    (55..155).each { n ->
        def row = data.getDataRow(getRows(data), 'R' + n)

        // «графа 5» =сумма значений  «графы 5» для строк с 55 по 155 (раздел «Внереализационные доходы»)
        row40002.rnu6Field10Sum = (row40002.rnu6Field10Sum ?: 0) + (row.rnu6Field10Sum ?: 0)

        // «графа 6» =сумма значений  «графы 6» для строк с 55 по 155 (раздел «Внереализационные доходы»)
        row40002.rnu6Field12Accepted = (row40002.rnu6Field12Accepted ?: 0) + (row.rnu6Field12Accepted ?: 0)

        // «графа 7» =сумма значений  «графы 7» для строк с 55 по 155 (раздел «Внереализационные доходы»)
        row40002.rnu6Field12PrevTaxPeriod = (row40002.rnu6Field12PrevTaxPeriod ?: 0) + (row.rnu6Field12PrevTaxPeriod ?: 0)

        // «графа 8» =сумма значений  «графы 8» для строк с 55 по 155 (раздел «Внереализационные доходы»)
        row40002.rnu4Field5Accepted = (row40002.rnu4Field5Accepted ?: 0) + (row.rnu4Field5Accepted ?: 0)
    }
    data.save(getRows(data))
}

/**
 * 6.1.2.8.3.1	Логические проверки
 * Завяжемся на цвета а не на номера строк,
 * а-ля точное отображение аналитики
 */
def logicalCheck() {
    refBookIncome102 = refBookFactory.getDataProvider(52L)
    refBookIncome101 = refBookFactory.getDataProvider(50L)
    getRows(data).each {DataRow<Cell> row ->

        /*
         * Проверка объязательных полей
         */
        def requiredColumns = ['rnu6Field10Sum', 'rnu6Field12Accepted', 'rnu6Field12PrevTaxPeriod', 'rnu4Field5Accepted']
        if (!checkRequiredColumns(row, requiredColumns)) {
            return
        }

        /**
         * Графа 9
         * Номера строк: Все строки. Для ячеек, обозначенных как вычисляемые (см. Табл. 12)
         *
         * Алгоритм заполннеия:
         * Если Сумма <0, то «графа 9»= «ТРЕБУЕТСЯ ОБЪЯСНЕНИЕ » Иначе «графа 9»= Сумма ,где
         * Сумма= ОКРУГЛ( «графа5»-(«графа 6»-«графа 7»);2)
         */
        List calc9graph = ['R2', 'R3', 'R5', 'R6', 'R7', 'R8', 'R9', 'R10', 'R11', 'R17', 'R18', 'R19', 'R20', 'R22',
                'R24', 'R28', 'R29', 'R30', 'R48', 'R49', 'R51', 'R52', 'R65', 'R66', 'R67','R68', 'R69', 'R70', 'R139',
                'R142', 'R143', 'R144', 'R145', 'R146', 'R147', 'R148', 'R149', 'R150', 'R151', 'R153', 'R154', 'R155'
        ]

        if (calc9graph.contains(row.getAlias())) {
            summ = ((BigDecimal) ((row.rnu6Field10Sum ?: 0) - (row.rnu6Field12Accepted ?: 0) + (row.rnu6Field12PrevTaxPeriod ?: 0))).setScale(2, BigDecimal.ROUND_HALF_UP)
            row.logicalCheck = summ < 0 ? "Требуется объяснение" : summ.toString()
        }

        List<String> rowsNotCalc = ['R1', 'R53', 'R54', 'R156']

        /**
         * Графа 11
         * Номера строк: Все строки. Все строки. Для ячеек, обозначенных как вычисляемые (см. Табл. 12)
         *
         * Алгоритм заполннеия:
         * «графа 11» = сумма значений «графы 6» формы «Сводная форма начисленных доходов уровня обособленного подразделения»(см. раздел 6.1.1)
         * для тех строк, для которых значение «графы 4»  равно значению «графы 4» текущей строки
         */
        if (!rowsNotCalc.contains(row.getAlias())) {
            // получим форму «Сводная форма начисленных доходов уровня обособленного подразделения»(см. раздел 6.1.1)
            def sum6ColumnOfForm302 = 0
            FormData formData302 = formDataService.find(302, FormDataKind.SUMMARY, formData.departmentId, formData.reportPeriodId)
            if (formData302 != null) {
                data302 = formDataService.getDataRowHelper(formData302)
                for (DataRow<Cell> rowOfForm302 in data302.allCached) {
                    if (rowOfForm302.incomeBuhSumAccountNumber == row.accountNo) {
                        sum6ColumnOfForm302 += rowOfForm302.incomeBuhSumAccepted ?: 0
                    }
                }
            }
            row.opuSumByEnclosure2 = sum6ColumnOfForm302
        }

        /**
         * Графа 12
         * Номера строк: Все строки. Для ячеек, обозначенных как вычисляемые (см. Табл. 12)
         *
         * Алгоритм заполннеия:
         * «графа 12» = сумма значений «графы 8» для тех строк,
         * для которых значение «графы 4»  равно значению «графы 4» текущей строки.
         *
         */
        if (!rowsNotCalc.contains(row.getAlias())) {
            def sum8Column = 0
            getRows(data).each { irow ->
                if (irow.accountNo == row.accountNo) {
                    sum8Column += irow.rnu4Field5Accepted ?: 0
                }
            }
            row.opuSumByTableD = sum8Column
        }

        /**
         * Графа 13
         * Номера строк:
         * Все строки. Для ячеек, обозначенных как вычисляемые (см. Табл. 12)
         * за исключением строк  118-119, 141-142
         *
         * Алгоритм заполннеия:
         * «графа13» =  сумма значений поля «Сумма, руб» для всех записей «Отчета о прибылях и убытках»  для которых выполняются следующие условия:
         * Выбираются данные отчета за период, для которого сформирована текущая форма
         * Значение поля «Кода ОПУ» Отчета о прибылях и убытках совпадает со значением «графы 10» текущей строки текущей формы.
         */

        if (!rowsNotCalc.contains(row.getAlias()) && !(row.getAlias() in ['R118', 'R119', 'R141', 'R142'])) {
            row.opuSumTotal = 0
            for (income102 in refBookIncome102.getRecords(reportPeriodService.getEndDate(formData.reportPeriodId).time, null, 'OPU_CODE = \'' + row.accountingRecords.toString() + '\'', null).getRecords()) {
                row.opuSumTotal += income102.get("TOTAL_SUM").getNumberValue()
            }
        }

        /**
         * Графа 13
         * Номера строк: 118-119, 141-142
         *
         * Алгоритм заполннеия:
         *  «графа13» =сумма значений поля «Обороты по дебету, руб» для всех записей «Оборотной Ведомости», для которых выполняются следующие условия:
         * 	Выбираются данные отчета за период, для которого сформирована текущая форма
         * 	Значение поля «Номер счета» совпадает совпадает со значением «графы 10» текущей строки текущей формы.
         */
        if (row.getAlias() in ['R118', 'R119', 'R141', 'R142']) {
            row.opuSumTotal = 0
            for (income101 in refBookIncome101.getRecords(reportPeriodService.getEndDate(formData.reportPeriodId).time, null, 'ACCOUNT = \'' + row.accountingRecords.toString() + '\'', null).getRecords()) {
                row.opuSumTotal += income101.get("DEBET_RATE").getNumberValue()
            }
        }

        /**
         * Графа 14
         * Номера строк:
         * Все строки. Для ячеек, обозначенных как вычисляемые (см. Табл. 12)
         * за исключением строк  118-119, 141-142
         *
         * Алгоритм заполннеия:
         *  «графа 14» = («графа 11» + «графа 12») – «графа 13»
         */
        if (!rowsNotCalc.contains(row.getAlias()) && !(row.getAlias() in ['R118', 'R119', 'R141', 'R142'])) {
            row.difference = (row.opuSumByEnclosure2 ?: 0) + (row.opuSumByTableD ?: 0) - (row.opuSumTotal ?: 0)
        }

        /**
         * Графа 14
         * Номера строк:
         * 118-119
         *
         * Алгоритм заполннеия:
         *  «графа 14» = «графа 13» - «графа 8»
         */
        if (row.getAlias() in ['R118', 'R119']) {
            row.difference = (row.opuSumTotal ?: 0) - (row.rnu4Field5Accepted ?: 0)
        }

        /**
         * Графа 14
         * Номера строк:
         * 141-142
         *
         * Алгоритм заполннеия:
         *  «графа 14» = «графа 13» - ( А+ Б)
         *   А – значение «графы 8» для строки 141
         *   Б – значение «графы 8» для строки 142
         */
        if (row.getAlias() in ['R141', 'R142']) {
            data.getDataRow(getRows(data), 'R141')
            data.getDataRow(getRows(data), 'R142')
            row.difference = (row.opuSumTotal ?: 0) -
                    ((data.getDataRow(getRows(data), 'R141').rnu4Field5Accepted ?: 0) +
                            (data.getDataRow(getRows(data), 'R142').rnu4Field5Accepted ?: 0))
        }
    }
    data.save(getRows(data))
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
 * Функция возвращает тип ячейки вычисляемая она или нет
 * @param cell
 * @return
 */
def isCalcField(Cell cell) {
    return cell.getStyleAlias() == "Контрольные суммы"
}

/**
 * Консолидация формы
 */
def consolidation(DataRowHelper form) {
    isBank() ? consolidationBank(form) : consolidationSummary()
    form.save(form.allCached)
    form.commit()
}

def consolidationBank(DataRowHelper form) {
// очистить форму
    form.getAllCached().each { row ->
        ['rnu6Field10Sum', 'rnu6Field12Accepted', 'rnu6Field12PrevTaxPeriod', 'rnu4Field5Accepted', 'logicalCheck', 'accountingRecords'].each { alias ->
            row.getCell(alias).setValue(null)
        }
    }
    // получить данные из источников
    for (departmentFormType in departmentFormTypeService.getFormSources(formData.departmentId, formData.getFormType().getId(), formData.getKind())) {
        def child = formDataService.find(departmentFormType.formTypeId, departmentFormType.kind, departmentFormType.departmentId, formData.reportPeriodId)
        if (child != null && child.state == WorkflowState.ACCEPTED && child.formType.id == departmentFormType.formTypeId) {
            getRows(getData(child)).eachWithIndex() { DataRow<Cell> row, i ->
                DataRow<Cell> rowResult = form.getDataRow(form.allCached, row.getAlias())
                for (alias in ['rnu6Field10Sum', 'rnu6Field12Accepted', 'rnu6Field12PrevTaxPeriod', 'rnu4Field5Accepted']) {
                    if (row.getCell(alias).getValue() != null) {
                        rowResult.getCell(alias).setValue(summ(rowResult.getCell(alias), row.getCell(alias)))
                    }
                }
            }
        }
    }
}

def consolidationSummary() {
    // очистить форму
    getRows(data).each { row ->
        ['rnu6Field10Sum', 'rnu6Field12Accepted', 'rnu6Field12PrevTaxPeriod', 'rnu4Field5Accepted'].each { alias ->
            row.getCell(alias).setValue(null)
        }
    }
    reportPeriodPrev = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
    formPrev = null
    dataPrev = null
    if (reportPeriodPrev != null) {
        formPrev = formDataService.find(formData.formType.id, formData.kind, formData.departmentId, reportPeriodPrev.id)
        if (formPrev != null) {
            dataPrev = getData(formPrev)
        }
    }
    getRows567().each { rowNum ->
        def DataRow row = data.getDataRow(getRows(data), 'R' + rowNum)
        row.rnu6Field10Sum = 0
        row.rnu6Field12Accepted = 0
        row.rnu6Field12PrevTaxPeriod = 0
    }
    getRows8().each { rowNum ->
        def DataRow row = data.getDataRow(getRows(data), 'R' + rowNum)
        row.rnu4Field5Accepted = 0
    }
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (source != null && source.state == WorkflowState.ACCEPTED) {
            getRows567().each { rowNum ->
                def DataRow row = data.getDataRow(getRows(data), 'R' + rowNum)
                def graph5 = 0
                def graph6 = 0
                def graph7 = 0
                if (source.getFormType().getId() == 318) {
                    def dataRNU6 = getData(source)
                    getRows(dataRNU6).each { DataRow rowRNU6 ->
                        if (rowRNU6.getAlias() == null) {
                            def knu = getKNUValue(rowRNU6.kny)
                            // если «графа 2» (столбец «Код налогового учета») формы источника = «графе 1» (столбец «КНУ») текущей строки и
                            //«графа 4» (столбец «Балансовый счёт (номер)») формы источника = «графе 4» (столбец «Балансовый счёт по учёту дохода»)
                            if (row.incomeTypeId != null && row.accountNo != null && row.incomeTypeId == knu && isEqualNum(row.accountNo, rowRNU6.code)) {
                                //«графа 5» =  сумма значений по «графе 10» (столбец «Сумма дохода в налоговом учёте. Рубли») всех форм источников вида «(РНУ-6)
                                graph5 += rowRNU6.taxAccountingRuble ?: 0
                                //«графа 6» =  сумма значений по «графе 12» (столбец «Сумма дохода в бухгалтерском учёте. Рубли») всех форм источников вида «(РНУ-6)
                                graph6 += rowRNU6.ruble ?: 0
                                //графа 7
                                if (rowRNU6.ruble != null && rowRNU6.ruble != 0) {
                                    SimpleDateFormat formatY = new SimpleDateFormat('yyyy')
                                    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
                                    Date dateFrom = format.parse('01.01.' + (Integer.valueOf(formatY.format(rowRNU6.date)) - 3))
                                    List<TaxPeriod> taxPeriodList = taxPeriodService.listByTaxTypeAndDate(TaxType.INCOME, dateFrom, rowRNU6.date)
                                    taxPeriodList.each { TaxPeriod taxPeriod ->
                                        List<ReportPeriod> reportPeriodList = reportPeriodService.listByTaxPeriod(taxPeriod.getId())
                                        reportPeriodList.each { ReportPeriod reportPeriod ->
                                            def primaryRNU6 = formDataService.find(source.formType.id, FormDataKind.PRIMARY, source.departmentId, reportPeriod.getId())//TODO подразделение
                                            if (primaryRNU6!=null) {
                                                def dataPrimary = getData(primaryRNU6)
                                                getRows(dataPrimary).each { DataRow rowPrimary ->
                                                    if (rowPrimary.code != null && rowPrimary.code == rowRNU6.code &&
                                                            rowPrimary.docNumber != null && rowPrimary.docNumber == rowRNU6.docNumber &&
                                                            rowPrimary.docDate != null && rowPrimary.docDate == rowRNU6.docDate) {
                                                        graph7 += rowPrimary.taxAccountingRuble
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                row.rnu6Field10Sum += graph5
                row.rnu6Field12Accepted += graph6
                row.rnu6Field12PrevTaxPeriod += graph7
            }
            getRows8().each { rowNum ->
                def DataRow row = data.getDataRow(getRows(data), 'R' + rowNum)
                def graph8 = 0
                if (source.getFormType().getId() == 316) {
                    def dataRNU4 = getData(source)
                    getRows(dataRNU4).each { rowRNU4 ->
                        if (rowRNU4.getAlias() == null) {
                            def knu = getKNUValue(rowRNU4.code)
                            if (row.incomeTypeId != null && row.accountNo != null && row.incomeTypeId == knu && isEqualNum(row.accountNo, rowRNU4.balance)) {
                                //«графа 8» =  сумма значений по «графе 5» (столбец «Сумма дохода за отчётный квартал») всех форм источников вида «(РНУ-4)
                                grap    h8 += rowRNU4.sum
                            }
                        }
                    }
                }
                row.rnu4Field5Accepted += graph8
            }
        }
    }
    if (dataPrev != null && reportPeriodService.get(formData.reportPeriodId).order != 1) {
        getRows567().each { rowNum ->
            //«графа 5» +=«графа 5» формы предыдущего отчётного периода (не учитывается при расчете в первом отчётном периоде)
            def rowPrev = dataPrev.getDataRow(getRows(dataPrev), 'R' + rowNum)
            row.rnu6Field10Sum += rowPrev.rnu6Field10Sum
            //«графа 6» +=«графа 6» формы предыдущего отчётного периода (не учитывается при расчете в первом отчётном периоде)
            row.rnu6Field12Accepted += rowPrev.rnu6Field12Accepted
            //«графа 7» +=«графа 7» формы предыдущего отчётного периода (не учитывается при расчете в первом отчётном периоде)
            row.rnu6Field12PrevTaxPeriod += rowPrev.rnu6Field12PrevTaxPeriod
        }
        getRows8().each { rowNum ->
            //«графа 8» +=«графа 8» формы предыдущего отчётного периода (не учитывается при расчете в первом отчётном периоде)
            def rowPrev = dataPrev.getDataRow(getRows(dataPrev), 'R' + rowNum)
            row.rnu4Field5Accepted += rowPrev.rnu4Field5Accepted
        }
    }
}
/**
 * Скрипт для проверки создания.
 *
 * @author auldanov
 */
void checkCreation() {
    def findForm = formDataService.find(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }

    if (formData.kind != FormDataKind.SUMMARY) {
        logger.error("Нельзя создавать форму с типом ${formData.kind?.name}")
    }
}

//// обертка предназначенная для прямых вызовов функции без formData
//BigDecimal summ(ColumnRange cr) {
//    return summ(formData, cr, cr, {return true;})
//}

/**
 * Проверка на банк.
 */
def isBank() {
    boolean isBank = true
    departmentFormTypeService.getFormDestinations(formData.departmentId, formData.formTemplateId, FormDataKind.SUMMARY).each {
        if (it.departmentId != formData.departmentId) {
            isBank = false
        }
    }
    return isBank
}

/**
 * Проверки наличия декларации Банка при отмене принятия нф.
 *
 * @author rtimerbaev
 * @since 21.03.2013 11:00
 */
void checkDeclarationBankOnCancelAcceptance() {
    if (!isBank()) {
        return
    }
    departmentFormTypeService.getDeclarationDestinations(formData.getDepartmentId(), formData.getFormType().getId(), FormDataKind.SUMMARY).each { department ->
        def bank = declarationService.find(2, department.departmentId, formData.reportPeriodId)
        if (bank != null && bank.accepted) {
            logger.error('Отмена принятия налоговой формы невозможно, т.к. уже принята декларация Банка.')
        }
    }
}

// Строки для которых считать графы 5,6,7
def getRows567() {
    return ([2, 3] + (5..11) + (17..20) + [22, 24] + (28..30) + [48, 49, 51, 52] + (65..70) + [139] + (142..151) + (153..155))
}

def getRows8() {
    return ((2..52) + (55..155))
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

def getKNUValue(def value) {
    return refBookService.getStringValue(28, value, 'CODE')
}

def getBalanceValue(def value) {
    return refBookService.getStringValue(28, value, 'NUMBER')
}

boolean isEqualNum(String accNum, def balance) {
    return accNum.replace('.', '') == getBalanceValue(balance).replace('.', '')
}

/**
 * Проверка пустое ли значение.
 */
def isEmpty(def value) {
    return value == null || value == ''
}

/**
 * Получить значение или ноль.
 *
 * @param value значение которое надо проверить
 */
def getValue(def value) {
    return value ?: 0
}

/**
 * Получить сумму значений для указанной формы.
 *
 * @param rows строки формы
 * @param columnAliasCheck алиас графы, по которой отбираются строки для суммирования
 * @param columnAliasSum алиас графы, значения которой суммируются
 * @param value значение, по которому отбираются строки для суммирования
 */
def getSumFromData(rows, columnAliasCheck, columnAliasSum, value) {
    def sum = 0
    if (rows != null && (columnAliasCheck != null || columnAliasCheck != '') &&
            (columnAliasSum != null || columnAliasSum != '') && value != null) {
        for (def row : rows) {
            if (row.getCell(columnAliasCheck).getValue() == value) {
                sum += (row.getCell(columnAliasSum).getValue() ?: 0)
            }
        }
    }
    return sum
}

/**
 * Получить строки формы "дохоы сложные" (id = 302)
 */
def getComplexRows() {
    def formDataComplex = formDataService.find(302, formData.kind, formDataDepartment.id, formData.reportPeriodId)
    if (formDataComplex != null) {
        def data = getData(formDataComplex)
        if (data != null) {
            return getRows(data)
        }
    }
    return null
}

/**
 * Получить строку по алиасу.
 *
 * @param data данные нф (helper)
 * @param alias алиас
 */
def getRowByAlias(def data, def alias) {
    return data.getDataRow(getRows(data), alias)
}