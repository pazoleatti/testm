package form_template.deal.summary2.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * Сводный отчет для уведомления по КС.
 *
 * formTemplateId = 849
 *
 * TODO:
 *      - добавить тесты
 */

// 1.	groupName  					Наименование
//									РАЗДЕЛ 1А. Сведения о контролируемой сделке (группе однородных сделок)
// 2.	dealNum1					п. 010 "Порядковый номер сделки по уведомлению"
// 3.	interdependenceSing			п. 100
// 4.	f121						п. 121
// 5.	f122						п. 122
// 6.	f123						п. 123
// 7.	f124						п. 124
// 8.	f131						п. 131
// 9.	f132						п. 132
// 10.	f133						п. 133
// 11.	f134						п. 134
// 12.	f135						п. 135 (до 2014 г. / после 2014 г.)
// 13.	similarDealGroup			п. 200 "Группа однородных сделок"
// 14.	dealNameCode				п. 210 "Код наименования сделки"
// 15.	taxpayerSideCode			п. 211 "Код стороны сделки, которой является налогоплательщик"
// 16.	dealPriceSign				п. 220 "Признак определения цены сделки с учетом особенностей, предусмотренных статьей 105.4 НК РФ (регулируемые цены)"
// 17.	dealPriceCode				п. 230 "Код определения цены сделки"
// 18.	dealMemberCount				п. 260 "Количество участников сделки"
// 19.	income						п. 300 "Сумма доходов налогоплательщика по контролируемой сделке (группе однородных сделок) в рублях"
// 20.	incomeIncludingRegulation	п. 301 "в том числе сумма доходов по сделкам, цены которых подлежат регулированию"
// 21.	outcome						п. 310 "Сумма расходов налогоплательщика по контролируемой сделке (группе однородных сделок) в рублях"
// 22.	outcomeIncludingRegulation	п. 311 "в том числе сумма расходов по сделкам, цены которых подлежат регулированию"
// 									РАЗДЕЛ 1Б. Сведения о предмете сделки (группы однородных сделок)
// 23.	dealNum2					п. 010 "Порядковый номер сделки по уведомлению (из раздела 1А)"
// 24.	dealType					п. 020 "Тип предмета сделки"
// 25.	dealSubjectName				п. 030 "Наименование предмета сделки"
// 26.	dealSubjectCode1			п. 040 "Код предмета сделки (код по ТН ВЭД)"
// 27.	dealSubjectCode2			п. 043 "Код предмета сделки (код по ОКП)"
// 28.	dealSubjectCode3			п. 045 "Код предмета сделки (код по ОКВЭД)"
// 29.	otherNum					п. 050 "Номер другого участника сделки"
// 30.	contractNum					п. 060 "Номер договора"
// 31.	contractDate				п. 065 "Дата договора"
// 32.	countryCode					п. 070 "Код страны происхождения предмета сделки по классификатору ОКСМ (цифровой)"
// 33.	countryCode1				п. 080 "Место отправки (погрузки) товара в соответствии с товаросопроводительными документами (заполняется только для товаров)". Код страны по классификатору ОКСМ (цифровой)
// 34.	region1						п. 080 "Место отправки (погрузки) товара в соответствии с товаросопроводительными документами (заполняется только для товаров)". Регион (код)
// 35.	city1						п. 080 "Место отправки (погрузки) товара в соответствии с товаросопроводительными документами (заполняется только для товаров)". Город
// 36.	locality1					п. 080 "Место отправки (погрузки) товара в соответствии с товаросопроводительными документами (заполняется только для товаров)". Населенный пункт (село, поселок и т.д.)
// 37.	countryCode2				п. 090 "Место совершения сделки (адрес места доставки (разгрузки товара), оказания услуги, работы, совершения сделки с иными объектами гражданских прав)". Код страны по классификатору ОКСМ (цифровой)
// 38.	region2						п. 090 "Место совершения сделки (адрес места доставки (разгрузки товара), оказания услуги, работы, совершения сделки с иными объектами гражданских прав)". Регион (код)
// 39.	city2						п. 090 "Место совершения сделки (адрес места доставки (разгрузки товара), оказания услуги, работы, совершения сделки с иными объектами гражданских прав)". Город
// 40.	locality2					п. 090 "Место совершения сделки (адрес места доставки (разгрузки товара), оказания услуги, работы, совершения сделки с иными объектами гражданских прав)". Населенный пункт (село, поселок и т.д.)
// 41.	deliveryCode				п. 100 "Код условия поставки (заполняется только для товаров)"
// 42.	okeiCode					п. 110 "Код единицы измерения по ОКЕИ"
// 43.	count						п. 120 "Количество"
// 44.	price						п. 130 "Цена (тариф) за единицу измерения без учета НДС, акцизов и пошлины, руб."
// 45.	total						п. 140 "Итого стоимость без учета НДС, акцизов и пошлины, руб."
// 46.	dealDoneDate				п. 150 "Дата совершения сделки (цифрами день, месяц, год)"
//									РАЗДЕЛ 2. Сведения об организации - участнике контролируемой сделки (группы однородных сделок)
// 47.	dealNum3					п. 010 "Порядковый номер сделки (из раздела 1А)"
// 48.	dealMemberNum				п. 015 "Порядковый номер участника сделки (из раздела 1Б)"
// 49.	organInfo					п. 020 "Сведения об организации"
// 50.	countryCode3				п. 030 "Код страны по классификатору ОКСМ"
// 51.	organName					п. 040 "Наименование организации"
// 52.	organINN					п. 050 "ИНН организации"
// 53.	organKPP					п. 060 "КПП организации"
// 54.	organRegNum					п. 070 "Регистрационный номер организации в стране ее регистрации (инкорпорации)"
// 55.	taxpayerCode				п. 080 "Код налогоплательщика в стране регистрации (инкорпорации) или его аналог (если имеется)"
// 56.	address						п. 090 "Адрес"

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkCreation()
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent, scriptStatusHolder)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW:
        formDataService.getDataRowHelper(formData)?.delete(currentDataRow)
        break
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED:
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent, scriptStatusHolder)
        break
    case FormDataEvent.IMPORT:
        importData()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent, scriptStatusHolder)
        break
    case FormDataEvent.SORT_ROWS:
        sortFormDataRows()
        break
}

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

// обязательные графы (4..19, 21, 24..25, 29..31, 42..46, 48..51)
@Field
def nonEmptyColumns = [/* 'groupName', 'dealNum1', */ 'f121', 'f122', 'f123', 'f124', 'f131', 'f132', 'f133', 'f134', 'f135',
        'similarDealGroup', 'dealNameCode', 'taxpayerSideCode', 'dealPriceSign', 'dealPriceCode', 'dealMemberCount',
        'income', 'outcome', /* 'dealNum2', */ 'dealType', 'dealSubjectName', 'otherNum', 'contractNum', 'contractDate',
        'okeiCode', 'count', 'price', 'total', 'dealDoneDate', /* 'dealNum3', */ 'dealMemberNum', 'countryCode3', 'organName']

@Field
def startDate = null

// Дата окончания отчетного периода
@Field
def endDate = null

@Field
def app4_2FormTypeId = 803

void addNewRow() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def index
    if (currentDataRow == null || currentDataRow.getIndex() == -1) {
        index = dataRows.size()
    } else {
        index = currentDataRow.getIndex()
    }
    def newRow = formData.createDataRow()
    dataRows.add(index, newRow)

    formDataService.saveCachedDataRows(formData, logger)
}

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    }
    return startDate
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

// Поиск записи в справочнике по значению (для расчетов)
def Long getRecordId(def Long refBookId, def String alias, def String value) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), -1, null, logger, true)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Проверка при создании формы
void checkCreation() {
    def findForm = formDataService.find(formData.formType.id, formData.kind, formData.departmentReportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
    if (findForm != null) {
        logger.error('Отчет с указанными параметрами уже сформирован!')
    }
}

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (dataRows.isEmpty()) {
        return
    }

    // Общие значения
    // "Да"
    def Long recYesId = getRecYesId()
    // "Нет"
    def Long recNoId = getRecNoId()

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }

        // 2. Проверка заполнения обязательных полей
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)

        // 3. Проверка кода основания отнесения сделки к контролируемой
        def onlyNo = (row.f131 == recNoId && row.f132 == recNoId && row.f133 == recNoId && row.f134 == recNoId && row.f135 == recNoId)
        if ((row.f122 == recYesId || row.f123 == recYesId) && !onlyNo) {
            def msg = "Строка %d: Не допускается одновременное заполнение значением «1» любой из граф «%s», «%s» с любой из граф «%s»!"
            def names = []
            ['f131', 'f132', 'f133', 'f134', 'f135'].each { alias ->
                names.add(getColumnName(row, alias))
            }
            subMsd = names.join('», «')
            logger.error(msg, row.getIndex(), getColumnName(row, 'f122'), getColumnName(row, 'f123'), subMsd)
        }

        // 4. Проверка одновременного заполнения доходов и расходов
        if (row.income != null && row.income > 0 && row.outcome != null && row.outcome > 0) {
            def msg = "Строка %d: Значение граф «%s» и «%s» не должны быть одновременно больше «0»!"
            logger.error(msg, row.getIndex(), getColumnName(row, 'income'), getColumnName(row, 'outcome'))
        }

        // 5. Проверка неотрицательности доходов
        // 6. Проверка неотрицательности расходов
        ['income', 'outcome'].each { alias ->
            if (row[alias] != null && row[alias] < 0) {
                def msg = "Строка %d: Значение графы «%s» должно быть больше или равно «0»!"
                logger.error(msg, row.getIndex(), getColumnName(row, alias))
            }
        }

        // 7. Проверка одновременного заполнения полей «Код предмета сделки»
        if (row.dealSubjectCode1 != null && row.dealSubjectCode2 != null) {
            def msg = "Строка %d: Значение граф «%s» и «%s» не должны быть одновременно заполнены!"
            logger.error(msg, row.getIndex(), getColumnName(row, 'dealSubjectCode1'), getColumnName(row, 'dealSubjectCode2'))
        }
    }
}

// Расчеты. Алгоритмы заполнения полей формы.
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // удаляем строки-группировки по организациям
    deleteAllAliased(dataRows)

    // сортируем по организациям
    dataRows.sort { it.organName }

    // добавляем строки-группировки по организациям
    addAllStatic(dataRows)

    sortFormDataRows(false)
}

/** Логические проверки перед консолидацией. */
def preConsolidationCheck() {
    // 1. Проверка на наличие формы «Приложение 4.2»
    if (!getFormDataApp4_2()) {
        def reportPeriod = reportPeriodService.get(formData.reportPeriodId)
        def periodName = reportPeriod.name + ' ' + reportPeriod.taxPeriod.year
        logger.error("Не существует формы-источника «Приложение 4.2» в статусе «Принята» за период «%s»!", periodName)
        return false
    }
    return true
}

@Field
def formDataApp4_2 = null

def getFormDataApp4_2() {
    if (formDataApp4_2 == null) {
        def source = formDataService.getLast(app4_2FormTypeId, formData.kind, formData.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
        if (source != null && source.state == WorkflowState.ACCEPTED) {
            formDataApp4_2 = source
        }
    }
    return formDataApp4_2
}

// Консолидация
void consolidation() {
    if (!preConsolidationCheck()) {
        return
    }

    def matrixRows = []
    // row → строка источника
    def rowsMap = [:]
    // счетчик по группам для табл. 86
    def int i = 1
    // мапа идентификаторов для группировки (id группы -> номер группы)
    def groupStr = [:]

    // row → тип формы-источника
    def final typeMap = [:]
    // row → класс группы
    def final classMap = [:]
    // row → номер группы
    def final groupMap = [:]

    // собрать нужные контролируемые сделки из 4.2
    def controlledTransactions = []
    formDataService.getDataRowHelper(getFormDataApp4_2()).allSaved.each { srcRow ->
        if (srcRow.sign == getRecYesId()) {
            controlledTransactions.add(srcRow.name)
        }
    }

    // консолидация из шестерок
    def departmentFormTypes = departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id,
            formData.kind, getReportPeriodStartDate(), getReportPeriodEndDate())
    departmentFormTypes.each {
        def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
        if (source != null && source.state == WorkflowState.ACCEPTED && source.formType.taxType == TaxType.DEAL) {
            formDataService.getDataRowHelper(source).allSaved.each { srcRow ->
                if (srcRow.getAlias() == null && controlledTransactions.contains(srcRow.name)) {
                    def matrixRow = getPreRow(srcRow, source.formType.id, typeMap, classMap)

                    // идентификатор для группировки
                    String group = getGroupId(matrixRow, srcRow, typeMap)
                    if (!groupStr.containsKey(group)) {
                        groupStr.put(group, i++)
                    }
                    groupMap.put(matrixRow, groupStr.get(group))

                    matrixRows.add(matrixRow)
                    rowsMap.put(matrixRow, srcRow)
                }
            }
        }
    }

    // Сортировка по группам
    matrixRows.sort { groupMap.get(it)}

    // итоговые строки (всё то, что в итоге попадет в сводный отчет)
    def summaryRows = []
    def Integer currentGroup
    def mapForSummary = [:]

    matrixRows.each { matrixRow ->
        def srcRow = rowsMap.get(matrixRow)
        def reportClass = classMap.get(matrixRow)

        if ((reportClass == 2 && !isGroupClass2(matrixRow, srcRow, typeMap))
                || (reportClass == 3 && !getRecSWId().equals(srcRow.serviceType))
                || reportClass == 4) {
            // копируем построчно - без группировки
            if (mapForSummary.size() > 0) {
                summaryRows.add(getRow(mapForSummary, typeMap))
            }
            mapForSummary.clear()
            mapForSummary.put(matrixRow, srcRow)
            summaryRows.add(getRow(mapForSummary, typeMap))
            mapForSummary.clear()
            currentGroup = null
        } else {
            // группируем перед копированием
            if (currentGroup == null) { // первая строка
                currentGroup = groupMap.get(matrixRow)
                mapForSummary.put(matrixRow, srcRow)
            } else if (currentGroup.equals(groupMap.get(matrixRow))) { // строка из той же группы что предыдущая
                mapForSummary.put(matrixRow, srcRow)
            } else {
                // строка из новой группы

                // получаем итоговую строку для предыдущей группы
                summaryRows.add(getRow(mapForSummary, typeMap))

                currentGroup = groupMap.get(matrixRow)
                mapForSummary.clear()
                mapForSummary.put(matrixRow, srcRow)
            }
        }
    }

    if (mapForSummary.size() > 0) {
        summaryRows.add(getRow(mapForSummary, typeMap))
    }

    updateIndexes(summaryRows)
    formDataService.getDataRowHelper(formData).allCached = summaryRows
}

// Строка сводного отчета из первичных и консолидированных отчетов модуля МУКС (табл. 85)
def buildRow(def srcRow, def matrixRow, def typeMap) {
    def row = formData.createDataRow()

    def formTypeId = typeMap.get(matrixRow)

    // Общие значения
    // "Да"
    def Long recYesId = getRecYesId()
    // "Нет"
    def Long recNoId = getRecNoId()

    // Графа 3
    row.interdependenceSing = getRecordId(69, 'CODE', '1')

    // Графа 4
    // row.f121 - заполняется после графы 50

    // Графа 5
    row.f122 = recNoId
    // для 6.15 (18)
    if (formTypeId == 837 && srcRow.signPhis != null && srcRow.dependence == recNoId && srcRow.signTransaction == recYesId) {
        def signPhis = getRefBookValue(18, srcRow.signPhis)
        if (signPhis?.SIGN?.value == 'Физическая поставка') {
            row.f122 = recYesId
        }
    }

    // Графа 6
    // row.f123, заполняется после графы 50

    // Графа 7
    row.f124 = recNoId

    // Графа 8
    // row.f131, заполняется после графы 50

    // Графа 9
    row.f132 = recNoId

    // Графа 10
    row.f133 = recNoId

    // Графа 11
    // row.f134 - заполняется после графы 50

    // Графа 12
    row.f135 = recNoId

    // Графа 13
    switch (formTypeId) {
        case 812: // 6.3 (1)
        case 814: // 6.5 (2)
        case 826: // 6.13 (5)
        case 813: // 6.4 (7)
        case 828: // 6.19 (20)
        case 833: // 6.24 (22)
            row.similarDealGroup = recYesId
            break
        case 805: // 6.7 (4)
        case 819: // 6.12 (6)
        case 817: // 6.9 (10)
        case 816: // 6.1 (12)
        case 832: // 6.23 (21)
        case 836: // 6.25 (26)
            row.similarDealGroup = recNoId
            break
        case 815: // 6.8 (3)
        case 806: // 6.6 (8)
        case 827: // 6.11 (9)
        case 825: // 6.10.2 (11)
        case 823: // 6.10.1 (13)
        case 804: // 6.2 (14)
        case 811: // 6.17 (15)
        case 839: // 6.16 (16)
        case 835: // 6.14 (17)
        case 837: // 6.15 (18)
        case 838: // 6.18 (19)
        case 831: // 6.20 (23)
        case 834: // 6.22 (24)
        case 830: // 6.21 (25)
            // Заполняется уже для сгруппировонной строки
            break
    }

    // Графа 14
    def tmp = null
    switch (formTypeId) {
        case 816: // 6.1 (12)
        case 804: // 6.2 (14)
        case 831: // 6.20 (23)
            tmp = '012'
            break
        case 812: // 6.3 (1)
            tmp = '002'
            break
        case 813: // 6.4 (7)
        case 814: // 6.5 (2)
        case 815: // 6.8 (3)
        case 826: // 6.13 (5)
        case 832: // 6.23 (21)
        case 833: // 6.24 (22)
            tmp = '019'
            break
        case 806: // 6.6 (8)
        case 835: // 6.14 (17)
        case 837: // 6.15 (18)
        case 839: // 6.16 (16)
            tmp = '032'
            break
        case 805: // 6.7 (4)
            tmp = '016'
            break
        case 817: // 6.9 (10)
        case 836: // 6.25 (26)
            tmp = '029'
            break
        case 825: // 6.10.2 (11)
        case 823: // 6.10.1 (13)
        case 830: // 6.21 (25)
        case 834: // 6.22 (24)
            tmp = '003'
            break
        case 827: // 6.11 (9)
        case 819: // 6.12 (6)
        case 838: // 6.18 (19)
            tmp = '015'
            break
        case 811: // 6.17 (15)
            tmp = '017'
            break
        case 828: // 6.19 (20)
            tmp = '004'
            break
    }
    if (tmp != null) {
        row.dealNameCode = getRecordId(67, 'CODE', tmp)
    }

    // Графа 15
    tmp = null
    switch (formTypeId) {
        case 816: // 6.1 (12)
        case 804: // 6.2 (14)
        case 817: // 6.9 (10)
            tmp = '022'
            break
        case 812: // 6.3 (1)
            tmp = '004'
            break
        case 813: // 6.4 (7)
        case 832: // 6.23 (21)
            tmp = '011'
            break
        case 814: // 6.5 (2)
        case 815: // 6.8 (3)
        case 826: // 6.13 (5)
        case 837: // 6.15 (18)
        case 833: // 6.24 (22)
            tmp = '012'
            break
        case 806: // 6.6 (8)
        case 835: // 6.14 (17)
        case 839: // 6.16 (16)
            tmp = '052'
            break
        case 805: // 6.7 (4)
            tmp = '028'
            break
        case 823: // 6.10.1 (13)
        case 825: // 6.10.2 (11)
            tmp = '005'
            break
        case 827: // 6.11 (9)
            if (getRecBuyId() == srcRow.transactionType) {
                tmp = '026'
            } else if (getRecSellId() == srcRow.transactionType) {
                tmp = '027'
            }
            break
        case 819: // 6.12 (6)
            if ((srcRow.incomeSum ?: 0) == 0 && (srcRow.outcomeSum ?: 0) > 0) {
                tmp = '026'
            } else if ((srcRow.incomeSum ?: 0) > 0 && (srcRow.outcomeSum ?: 0) == 0) {
                tmp = '027'
            }
            break
        case 811: // 6.17 (15)
            tmp = '030'
            break
        case 838: // 6.18 (19)
            tmp = (getRecDealBuyId() == srcRow.dealFocus ? '026' : '027')
            break
        case 828: // 6.19 (20)
            tmp = '007'
            break
        case 831: // 6.20 (23)
            tmp = '020'
            break
        case 830: // 6.21 (25)
        case 834: // 6.22 (24)
            tmp = '002'
            break
        case 836: // 6.25 (26)
            tmp = '048'
            break
    }
    if (tmp != null) {
        row.taxpayerSideCode = getRecordId(65, 'CODE', tmp)
    }

    // Графа 16
    // заполняется после графы 30 и 31

    // Графа 17
    tmp = 0
    switch (formTypeId) {
        case 817: // 6.9 (10)
            tmp = 3
            break
        case 827: // 6.11 (9)
            tmp = 2
            if (srcRow.dealMode != null) {
                def record14 = getRefBookValue(14, srcRow.dealMode)
                if (record14.ID.value == 2) {
                    tmp = 0
                }
            }
            break
    }
    row.dealPriceCode = getRecordId(66, 'CODE', "$tmp")

    // Графа 18
    row.dealMemberCount = 2

    // Графа 19
    // заполняется предварительно для каждой строки getPreRow
    row.income = 0

    // Графа 21
    // заполняется предварительно для каждой строки getPreRow
    row.outcome = 0

    // Графа 24
    def int value24 = 2
    switch (formTypeId) {
        case 805: // 6.7 (4)
        case 817: // 6.9 (10)
        case 828: // 6.19 (20)
        case 831: // 6.20 (23)
        case 836: // 6.25 (26)
            value24 = 3
            break
        case 837: // 6.15 (18)
        case 838: // 6.18 (19)
            def record18 = getRefBookValue(18, srcRow.signPhis)
            value24 = (record18?.SIGN?.value == "ОМС") ? 2 : 1
            break
    }
    row.dealType = getRecordId(64, 'CODE', "$value24")

    // Графа 25
    switch (formTypeId) {
        case 816: // 6.1 (12)
            row.dealSubjectName = 'Размещение денежных средств корпоративным клиентам - не регулируемые сделки'
            break
        case 804: // 6.2 (14)
            row.dealSubjectName = 'Размещение денежных средств в межбанковские кредиты'
            break
        case 812: // 6.3 (1)
            row.dealSubjectName = 'Предоставление помещений в аренду (субаренду)'
            break
        case 813: // 6.4 (7)
            row.dealSubjectName = 'Оказание банковских услуг'
            break
        case 814: // 6.5 (2)
            row.dealSubjectName = 'Услуги, связанные с обслуживанием недвижимости'
            break
        case 806: // 6.6 (8)
            def String out = ('Да'.equals(srcRow.dealsMode) ? "" : "вне")
            if (srcRow.incomeSum != null) {
                row.dealSubjectName = "Операции РЕПО - " + out + "биржевые (процентный доход)"
            } else if (srcRow.outcomeSum != null) {
                row.dealSubjectName = "Операции РЕПО - " + out + "биржевые (процентный расход)"
            }
            break
        case 805: // 6.7 (4)
            row.dealSubjectName = 'Услуги по предоставлению права пользования товарным знаком'
            break
        case 815: // 6.8 (3)
            row.dealSubjectName = 'Услуги по разработке, внедрению и модификации программного обеспечения'
            break
        case 817: // 6.9 (10)
            row.dealSubjectName = 'Уступка прав требования - с обязательной оценкой'
            break
        case 823: // 6.10.1 (13)
            row.dealSubjectName = 'Выдача гарантий'
            break
        case 825: // 6.10.2 (11)
            row.dealSubjectName = 'Открытие аккредитивов и инструментов торгового финансирования'
            break
        case 827: // 6.11 (9)
            def String out = (getRecDealsModeId().equals(srcRow.dealMode) ? "" : "вне")
            if (getRecBuyId().equals(srcRow.transactionType)) {
                row.dealSubjectName = "Покупка ЦБ - " + out + "биржевые сделки"
            } else if (getRecSellId().equals(srcRow.transactionType)) {
                row.dealSubjectName = "Продажа ЦБ - " + out + "биржевые сделки"
            }
            break
        case 819: // 6.12 (6)
            def String out = (getRecRPCId().equals(srcRow.dealSign) ? "" : "вне")
            if ((srcRow.incomeSum ?: 0) == 0 && (srcRow.outcomeSum ?: 0) > 0) {
                row.dealSubjectName = "Покупка акций и долей - " + out + "биржевые сделки"
            } else if ((srcRow.incomeSum ?: 0) > 0 && (srcRow.outcomeSum ?: 0) == 0) {
                row.dealSubjectName = "Продажа акций и долей - " + out + "биржевые сделки"
            }
            break
        case 826: // 6.13 (5)
            row.dealSubjectName = 'Приобретение услуг, связанных с организацией и проведением торгов по реализации имущества'
            break
        case 835: // 6.14 (17)
            if ((srcRow.income ?: 0) - (srcRow.outcome ?: 0) >= 0) {
                row.dealSubjectName = 'Беспоставочные (расчетные) срочные сделки - доходные'
            } else {
                row.dealSubjectName = 'Беспоставочные (расчетные) срочные сделки - расходные'
            }
            break
        case 837: // 6.15 (18)
            def buyOrSale = (getRecRUSId() == srcRow.dealCountryCode ? 'покупка' : 'продажа')
            def incomeOrOutcome = ((srcRow.income ?: 0) - (srcRow.outcome ?: 0) >= 0 ? 'доход' : 'расход')
            row.dealSubjectName = String.format('Срочные поставочные сделки купли-продажи драгоценных металлов (сделки с отсрочкой исполнения), %s, %s', buyOrSale, incomeOrOutcome)
            break
        case 839: // 6.16 (16)
            if ((srcRow.incomeSum ?: 0) - (srcRow.outcomeSum ?: 0) >= 0) {
                row.dealSubjectName = 'Срочные поставочные конверсионные сделки (сделки с отсрочкой исполнения) - доход'
            } else {
                row.dealSubjectName = 'Срочные поставочные конверсионные сделки (сделки с отсрочкой исполнения) - расход'
            }
            break
        case 811: // 6.17 (15)
            if ((srcRow.income ?: 0) - (srcRow.outcome ?: 0) >= 0) {
                row.dealSubjectName = 'Кассовые конверсионные сделки - доходные'
            } else if (srcRow.outcome != null) {
                row.dealSubjectName = 'Кассовые конверсионные сделки - расходные'
            }
            break
        case 838: // 6.18 (19)
            def buyOrSale = (getRecDealBuyId() == srcRow.dealFocus ? 'покупки' : 'продажи')
            def incomeOrOutcome = ((srcRow.incomeSum ?: 0) - (srcRow.outcomeSum ?: 0) >= 0 ? 'доходные' : 'расходные')
            row.dealSubjectName = String.format('Кассовые сделки %s драгоценных металлов - %s', buyOrSale, incomeOrOutcome)
            break
        case 828: // 6.19 (20)
            row.dealSubjectName = 'Привлечение денежных средств'
            break
        case 831: // 6.20 (23)
            row.dealSubjectName = 'Привлечение средств на межбанковском рынке'
            break
        case 830: // 6.21 (25)
            row.dealSubjectName = 'Привлечение гарантий (открытие аккредитивов и других инструментов торгового финансирования)'
            break
        case 834: // 6.22 (24)
            row.dealSubjectName = 'Привлечение гарантий'
            break
        case 832: // 6.23 (21)
        case 833: // 6.24 (22)
            row.dealSubjectName = srcRow.serviceType
            break
        case 836: // 6.25 (26)
            row.dealSubjectName = 'Приобретение прав требования'
            break
    }

    // Графа 26
    // Графа 27
    if (value24 == 1) {
        switch (formTypeId) {
            case 837: // 6.15 (18)
                row.dealSubjectCode1 = (srcRow.signTransaction == recYesId ? srcRow.innerCode : null)
                row.dealSubjectCode2 = (srcRow.signTransaction == recNoId ? srcRow.innerCode : null)
                break
            case 838: // 6.18 (19)
                row.dealSubjectCode1 = (srcRow.foreignDeal == recYesId ? srcRow.metalName : null)
                row.dealSubjectCode2 = (srcRow.foreignDeal == recNoId ? srcRow.metalName : null)
                break
        }
    }

    // Графа 28
    if (value24 in [2, 3]) {
        tmp = null
        switch (formTypeId) {
            case 816: // 6.1 (12)
            case 804: // 6.2 (14)
            case 806: // 6.6 (8)
            case 817: // 6.9 (10)
            case 836: // 6.25 (26)
                tmp = '65.22'
                break
            case 812: // 6.3 (1)
                tmp = '70.20.2'
                break
            case 813: // 6.4 (7)
                tmp = '65.12'
                break
            case 814: // 6.5 (2)
                tmp = '70.32.2'
                break
            case 805: // 6.7 (4)
            case 826: // 6.13 (5)
                tmp = '74.8'
                break
            case 815: // 6.8 (3)
                tmp = '72.20'
                break
            case 825: // 6.10.2 (11)
            case 823: // 6.10.1 (13)
            case 827: // 6.11 (9)
            case 819: // 6.12 (6)
            case 835: // 6.14 (17)
            case 839: // 6.16 (16)
            case 830: // 6.21 (25)
            case 834: // 6.22 (24)
                tmp = '65.23'
                break
            case 837: // 6.15 (18)
            case 811: // 6.17 (15)
            case 838: // 6.18 (19)
            case 828: // 6.19 (20)
            case 831: // 6.20 (23)
                tmp = '65.12'
                break
            case 832: // 6.23 (21)
            case 833: // 6.24 (22)
                tmp = '74'
                break
        }
        if (tmp != null) {
            row.dealSubjectCode3 = getRecordId(34, 'CODE', tmp)
        }
    }

    // Графа 29
    row.otherNum = 1

    // Графа 48
    row.dealMemberNum = row.otherNum

    // Графа 30
    // заполняется предварительно для каждой строки getPreRow
    row.contractNum = matrixRow.contractNum

    // Графа 31
    // заполняется предварительно для каждой строки getPreRow
    row.contractDate = matrixRow.contractDate

    // Графа 16
    Calendar compareCalendar16 = Calendar.getInstance()
    compareCalendar16.set(2011, 12, 28)
    // TODO (Ramil Timerbaev) Значение "123" должно быть уточнено заказчиком в ходе внедрения
    if (compareCalendar16.getTime().equals(row.contractDate) && "123".equals(row.contractNum)) {
        row.dealPriceSign = recYesId
    } else {
        row.dealPriceSign = recNoId
    }

    // Графа 32
    switch (formTypeId) {
        case 837: // 6.15 (18)
            row.countryCode = srcRow.dealCountryCode
            break
        case 838: // 6.18 (19)
            row.countryCode = srcRow.countryCodeNumeric
            break
    }

    // Графа 33, 34, 35, 36
    if (formTypeId == 837 || formTypeId == 838) {
        def record18 = getRefBookValue(18, srcRow.signPhis)
        if (record18?.SIGN?.value == "Физическая поставка") {
            if (formTypeId == 837) {
                row.countryCode1 = srcRow.countryCode2
                row.region1 = srcRow.region1
                row.city1 = srcRow.city1
                row.locality1 = srcRow.settlement1
            } else if (formTypeId == 838) {
                row.countryCode1 = srcRow.countryCodeNumeric
                row.region1 = srcRow.regionCode
                row.city1 = srcRow.city
                row.locality1 = srcRow.locality
            }
        }
    }

    // Графа 37, 38, 39, 40
    row.countryCode2 = getRecRUSId()
    row.region2 = getRecordId(4, 'CODE', '77')
    row.city2 = 'Москва'
    row.locality2 = row.city2
    switch (formTypeId) {
        case 812: // 6.3 (1)
        case 814: // 6.5 (2)
            row.countryCode2 = srcRow.country
            row.region2 = srcRow.region
            row.city2 = srcRow.city
            row.locality2 = srcRow.settlement
            break
        case 837: // 6.15 (18)
            def record18 = getRefBookValue(18, srcRow.signPhis)
            if (record18?.SIGN?.value == "Физическая поставка") {
                row.countryCode2 = srcRow.countryCode3
                row.region2 = srcRow.region2
                row.city2 = srcRow.city2
                row.locality2 = srcRow.settlement2
            }
            break
        case 838: // 6.18 (19)
            def record18 = getRefBookValue(18, srcRow.signPhis)
            if (record18?.SIGN?.value == "Физическая поставка") {
                row.countryCode2 = srcRow.countryCodeNumeric2
                row.region2 = srcRow.region2
                row.city2 = srcRow.city2
                row.locality2 = srcRow.locality2
            }
            break
    }

    // Графа 41
    if (value24 == 1) {
        if (formTypeId == 837) {
            row.deliveryCode = srcRow.conditionCode
        } else if (formTypeId == 838) {
            row.deliveryCode = srcRow.deliveryCode
        }
    }

    // Графа 42
    tmp = null
    switch (formTypeId) {
        case 812: // 6.3 (1)
        case 814: // 6.5 (2)
            tmp = '055'
            break
        case 816: // 6.1 (12)
        case 804: // 6.2 (14)
        case 813: // 6.4 (7)
        case 806: // 6.6 (8)
        case 805: // 6.7 (4)
        case 815: // 6.8 (3)
        case 823: // 6.10.1 (13)
        case 825: // 6.10.2 (11)
        case 827: // 6.11 (9)
        case 826: // 6.13 (5)
        case 835: // 6.14 (17)
        case 837: // 6.15 (18)
        case 839: // 6.16 (16)
        case 811: // 6.17 (15)
        case 838: // 6.18 (19)
        case 828: // 6.19 (20)
        case 831: // 6.20 (23)
        case 830: // 6.21 (25)
        case 834: // 6.22 (24)
        case 832: // 6.23 (21)
        case 833: // 6.24 (22)
            tmp = '796'
            break
        case 817: // 6.9 (10)
        case 819: // 6.12 (6)
        case 836: // 6.25 (26)
            row.okeiCode = srcRow.okeiCode
            break
    }
    if (tmp != null) {
        row.okeiCode = getRecordId(12, 'CODE', tmp)
    }

    // Графа 43
    // заполняется после, для группы
    row.count = 0

    // Графа 44
    // заполняется позже, для группы

    // Графа 45
    // заполняется позже, для группы
    row.total = 0

    // Графа 46
    // заполняется предварительно для каждой строки getPreRow
    row.dealDoneDate = matrixRow.dealDoneDate

    // Графа 50
    // countryCode3 заполняется после графы 51

    // Графа 51
    // заполняется предварительно для каждой строки getPreRow
    row.organName = matrixRow.organName

    if (row.organName != null) {
        def record520 = getRefBookValue(520, row.organName)
        def record525 = getRefBookValue(525, record520?.TYPE?.value)
        def record511 = getRefBookValue(511, record520?.TAX_STATUS?.value)

        // Графа 4
        row.f121 = (record525?.CODE?.value == 'РОЗ') ? recNoId : recYesId

        // Графа 6 (логика, обратная графе 4)
        row.f123 = (row.f121 == recNoId) ? recYesId : recNoId

        // Графа 8
        row.f131 = (record525?.CODE?.value == 'ВЗЛ' && record511?.CODE?.value == 2) ? recYesId : recNoId

        // Графа 11
        row.f134 = (record511?.CODE?.value == 1) ? recYesId : recNoId

        // Графа 50
        row.countryCode3 = record520?.COUNTRY_CODE?.value

        // Графа 56
        def record513 = getRefBookValue(513, record520.ORG_CODE?.value)
        row.address = (record513?.CODE?.value == 2 ? record520?.ADDRESS?.value : '')
    }

    // Графа 49, 52..56
    // зависимые графы

    return row
}

// определение класса строки по типу формы
def getReportClass(def formTypeId) {
    switch (formTypeId) {
        case 812 : // 6.3 (1)
        case 813 : // 6.4 (7)
        case 814 : // 6.5 (2)
        case 826 : // 6.13 (5)
        case 828 : // 6.19 (20)
        case 833 : // 6.24 (22)
            return 1
        case 804 : // 6.2 (14)
        case 806 : // 6.6 (8)
        case 823 : // 6.10.1 (13)
        case 825 : // 6.10.2 (11)
        case 827 : // 6.11 (9)
        case 835 : // 6.14 (17)
        case 837 : // 6.15 (18)
        case 839 : // 6.16 (16)
        case 811 : // 6.17 (15)
        case 838 : // 6.18 (19)
        case 831 : // 6.20 (23)
        case 830 : // 6.21 (25)
        case 834 : // 6.22 (24)
            return 2
        case 815 : // 6.8 (3)
            return 3
        case 816 : // 6.1 (12)
        case 805 : // 6.7 (4)
        case 817 : // 6.9 (10)
        case 819 : // 6.12 (6)
        case 832 : // 6.23 (21)
        case 836 : // 6.25 (26)
        default:
            return 4
    }
}

/**
 * Значение для группировки строки (табл. 112).
 *
 * @param matrixRow строка для сводной
 * @param srcRow строка источника
 * @param typeMap мапа (строка сводной -> тип формы источника)
 */
def String getGroupId(def matrixRow, def srcRow, def typeMap) {
    def StringBuilder group = new StringBuilder()

    group.append(typeMap.get(matrixRow)).append("#")
            .append(matrixRow.organName).append("#")
            .append(matrixRow.contractDate).append("#")
            .append(matrixRow.contractNum).append("#")

    switch (typeMap.get(matrixRow)) {
        case 806: // 6.6 (8)
            group.append('Да'.equals(srcRow.dealsMode)).append("#")
            group.append(srcRow.incomeSum != null).append("#")
            group.append(srcRow.outcomeSum != null)
            break
        case 827: // 6.11 (9)
            group.append(getRecDealsModeId().equals(srcRow.dealMode)).append("#")
            group.append(getRecBuyId().equals(srcRow.transactionType)).append("#")
            group.append(getRecSellId().equals(srcRow.transactionType))
            break
        case 811: // 6.17 (15)
            group.append(srcRow.currencyCode).append("#")
            group.append(srcRow.income != 0).append("#")
            group.append(srcRow.outcome != 0)
            break
        case 839: // 6.16 (16)
            group.append(srcRow.currencyCode).append("#")
            group.append(srcRow.dealType).append("#")
            group.append((srcRow.incomeSum ?: 0) - (srcRow.outcomeSum ?: 0) > 0)
            break
        case 835: // 6.14 (17)
            group.append(srcRow.dealType).append("#")
            group.append((srcRow.income ?: 0) - (srcRow.outcome ?: 0) > 0)
            break
        case 837: // 6.15 (18)
            group.append(srcRow.innerCode).append("#")
            group.append(srcRow.dealType).append("#")
            group.append(srcRow.dealCountryCode == getRecRUSId()).append("#")
            group.append((srcRow.income ?: 0) - (srcRow.outcome ?: 0) > 0)
            break
        case 838: // 6.18 (19)
            group.append(srcRow.metalName).append("#")
            group.append(srcRow.dealFocus).append("#")
            group.append((srcRow.incomeSum ?: 0) - (srcRow.outcomeSum ?: 0) > 0)
            break
        case 833: // 6.24 (22)
            group.append(srcRow.serviceType)
            break
    }
    return group.toString()
}

/**
 * Получение строки сводного отчета на основании группы строк (или одной строки) из "матрицы" и источника (табл. 87).
 *
 * @param map мапа (строка матрицы -> строка источника)
 * @param typeMap
 * @return
 */
def getRow(def map, def typeMap) {
    def row = formData.createDataRow()
    def boolean first = true
    map.each { matrixRow, srcRow ->
        if (first) {
            first = false

            row = buildRow(srcRow, matrixRow, typeMap)

            // графа 13
            // 6.2, 6.6, 6.8, 6.10.1, 6.10.2, 6.11, 6.14, 6.15, 6.16, 6.17, 6.18, 6.20, 6.21, 6.22
            if (typeMap.get(matrixRow) in [815, 806, 827, 825, 823, 804, 811, 839, 835, 837, 838, 831, 834, 830]) {
                if (map.size() > 1) {
                    row.similarDealGroup = getRecYesId()
                } else {
                    row.similarDealGroup = getRecNoId()
                }
            }
        }

        // графа 46
        if (matrixRow.dealDoneDate != null && (row.dealDoneDate == null || matrixRow.dealDoneDate > row.dealDoneDate)) {
            row.dealDoneDate = matrixRow.dealDoneDate
        }

        // Графа 43
        switch (typeMap.get(matrixRow)) {
            case 816: // 6.1 (12)
            case 812: // 6.3 (1)
            case 814: // 6.5 (2)
            case 817: // 6.9 (10)
            case 827: // 6.11 (9)
            case 819: // 6.12 (6)
            case 837: // 6.15 (18)
            case 838: // 6.18 (19)
            case 836: // 6.25 (26)
                row.count = row.count + (srcRow.count ?: 0)
                break
            default:
                row.count = 1
                break
        }

        // Графа 45
        switch (typeMap.get(matrixRow)) {
            case 816: // 6.1 (12)
            case 804: // 6.2 (14)
            case 812: // 6.3 (1)
            case 813: // 6.4 (7)
            case 814: // 6.5 (2)
            case 805: // 6.7 (4)
            case 815: // 6.8 (3)
            case 817: // 6.9 (10)
            case 823: // 6.10.1 (13)
            case 825: // 6.10.2 (11)
            case 819: // 6.12 (6)
            case 826: // 6.13 (5)
            case 835: // 6.14 (17)
            case 837: // 6.15 (18)
            case 839: // 6.16 (16)
            case 811: // 6.17 (15)
            case 828: // 6.19 (20)
            case 831: // 6.20 (23)
            case 830: // 6.21 (25)
            case 834: // 6.22 (24)
            case 832: // 6.23 (21)
            case 833: // 6.24 (22)
            case 836: // 6.25 (26)
                row.total = row.total + (srcRow.cost ?: 0)
                break
            case 806: // 6.6 (8)
                if (srcRow.incomeSum != null)
                    row.total = row.total + srcRow.incomeSum
                else if (srcRow.outcomeSum != null)
                    row.total = row.total + srcRow.outcomeSum
                break
            case 827: // 6.11 (9)
                row.total = row.total + (srcRow.sum ?: 0)
                break
            case 838: // 6.18 (19)
                row.total = row.total + (srcRow.total ?: 0)
                break
        }

        // Графа 19 и 21
        switch (typeMap.get(matrixRow)) {
            case 816: // 6.1 (12)
            case 804: // 6.2 (14)
            case 813: // 6.4 (7)
            case 805: // 6.7 (4)
            case 817: // 6.9 (10)
            case 823: // 6.10.1 (13)
            case 825: // 6.10.2 (11)
            case 832: // 6.23 (21)
                row.income = row.income + matrixRow.income
                break
            case 812: // 6.3 (1)
            case 814: // 6.5 (2)
            case 815: // 6.8 (3)
            case 826: // 6.13 (5)
            case 828: // 6.19 (20)
            case 831: // 6.20 (23)
            case 830: // 6.21 (25)
            case 834: // 6.22 (24)
            case 833: // 6.24 (22)
            case 836: // 6.25 (26)
                row.outcome = row.outcome + matrixRow.outcome
                break
            case 806: // 6.6 (8)
            case 827: // 6.11 (9)
            case 819: // 6.12 (6)
            case 835: // 6.14 (17)
            case 837: // 6.15 (18)
            case 839: // 6.16 (16)
            case 811: // 6.17 (15)
            case 838: // 6.18 (19)
                row.income = row.income + matrixRow.income
                row.outcome = row.outcome + matrixRow.outcome
                break
        }
    }

    // графа 44
    if ((row.count ?: 0) != 0) {
        if (row.income > 0) {
            row.price = row.income / row.count
        } else if (row.outcome > 0) {
            row.price = row.outcome / row.count
        } else {
            row.price = 0
        }
    }

    return row
}

// Общие значения

// "Да"
@Field
def Long recYesId

def Long getRecYesId() {
    if (recYesId == null)
        recYesId = getRecordId(38, 'CODE', '1')
    return recYesId
}

// "Нет"
@Field
def Long recNoId

def Long getRecNoId() {
    if (recNoId == null)
        recNoId = getRecordId(38, 'CODE', '0')
    return recNoId
}

// "Признак сделки, совершенной в РПС" = 1
@Field
def Long recRPCId

def Long getRecRPCId() {
    if (recRPCId == null)
        recRPCId = getRecordId(36, 'SIGN', 'Да')
    return recRPCId
}

// "Режим переговорных сделок" = 1
@Field
def Long recDealsModeId

def Long getRecDealsModeId() {
    if (recDealsModeId == null)
        recDealsModeId = getRecordId(14, 'MODE', 'Да')
    return recDealsModeId
}

// "Тип сделки" = «покупка»(B)
@Field
def Long recBuyId

def Long getRecBuyId() {
    if (recBuyId == null)
        recBuyId = getRecordId(16, 'CODE', 'B')
    return recBuyId
}

// "Тип сделки" = «продажа», (S)
@Field
def Long recSellId

def Long getRecSellId() {
    if (recSellId == null)
        recSellId = getRecordId(16, 'CODE', 'S')
    return recSellId
}

// "ОК 025-2001 (Общероссийский классификатор стран мира)" = Россия
@Field
def Long recRUSId

def Long getRecRUSId() {
    if (recRUSId == null)
        recRUSId = getRecordId(10, 'CODE', '643')
    return recRUSId
}

// "Направленности сделок" = «покупка»
@Field
def Long recDealBuyId

def Long getRecDealBuyId() {
    if (recDealBuyId == null)
        recDealBuyId = getRecordId(20, 'DIRECTION', 'покупка')
    return recDealBuyId
}

// "Услуги в части программного обеспечения" = 1 (Услуги по разработке, внедрению, поддержке и модификации программного обеспечения)
@Field
def Long recSWId

def Long getRecSWId() {
    if (recSWId == null)
        recSWId = getRecordId(11, 'CODE', '1')
    return recSWId
}

// дополнительное условие для отчетов "класс 2" на попадание в группу
boolean isGroupClass2(def matrixRow, def srcRow, def typeMap) {
    def boolean class2ext = false
    switch (typeMap.get(matrixRow)) {
        case 827 : // 6.11 (9)
            class2ext = srcRow.dealDate == null
            break
        case 804 : // 6.2 (14)
        case 806 : // 6.6 (8)
        case 823 : // 6.10.1 (13)
        case 825 : // 6.10.2 (11)
        case 835 : // 6.14 (17)
        case 837 : // 6.15 (18)
        case 839 : // 6.16 (16)
        case 811 : // 6.17 (15)
        case 838 : // 6.18 (19)
        case 831 : // 6.20 (23)
        case 830 : // 6.21 (25)
        case 834 : // 6.22 (24)
            class2ext = srcRow.dealNumber == null && srcRow.dealDate == null
            break
    }
    return class2ext || (matrixRow.contractDate != null && matrixRow.contractNum != null)
}

// Заполняем каждую строку полученную из источника необходимыми предварительными значениями
def getPreRow(def srcRow, def formTypeId, def typeMap, def classMap) {
    def row = formData.createDataRow()
    // тип отчета
    typeMap.put(row, formTypeId)
    // класс отчета
    classMap.put(row, getReportClass(formTypeId))

    // Графа 19
    switch (formTypeId) {
        case 816: // 6.1 (12)
        case 817: // 6.9 (10)
            row.income = srcRow.cost
            break
        case 804: // 6.2 (14)
        case 812: // 6.3 (1)
        case 813: // 6.4 (7)
        case 805: // 6.7 (4)
        case 823: // 6.10.1 (13)
        case 825: // 6.10.2 (11)
        case 832: // 6.23 (21)
            row.income = srcRow.sum
            break
        case 806: // 6.6 (8)
            row.income = (srcRow.incomeSum ?: 0)
            break
        case 819: // 6.12 (6)
            row.income = ((srcRow.incomeSum ?: 0) > 0 && (srcRow.outcomeSum ?: 0) == 0 ? srcRow.cost : 0)
            break
        case 827: // 6.11 (9)
            row.income = (getRecSellId() == srcRow.transactionType ? srcRow.sum : 0)
            break
        case 835: // 6.14 (17)
            row.income = ((srcRow.income ?: 0) - (srcRow.outcome ?: 0) >= 0 ? srcRow.price : 0)
            break
        case 837: // 6.15 (18)
        case 811: // 6.17 (15)
            row.income = ((srcRow.income ?: 0) - (srcRow.outcome ?: 0) >= 0 ? srcRow.cost : 0)
            break
        case 839: // 6.16 (16)
            row.income = ((srcRow.incomeSum ?: 0) - (srcRow.outcomeSum ?: 0) >= 0 ? srcRow.price : 0)
            break
        case 838: // 6.18 (19)
            row.income = ((srcRow.incomeSum ?: 0) - (srcRow.outcomeSum ?: 0) >= 0 ? srcRow.total : 0)
            break
    }
    if (row.income == null) {
        row.income = 0
    }

    // Графа 21
    switch (formTypeId) {
        case 814: // 6.5 (2)
        case 815: // 6.8 (3)
        case 828: // 6.19 (20)
        case 830: // 6.21 (25)
        case 834: // 6.22 (24)
            row.outcome = srcRow.sum
            break
        case 806: // 6.6 (8)
            row.outcome = (srcRow.outcomeSum ?: 0)
            break
        case 819: // 6.12 (6)
            row.outcome = ((srcRow.incomeSum ?: 0) == 0 && (srcRow.outcomeSum ?: 0) > 0 ? srcRow.outcomeSum : 0)
            break
        case 826: // 6.13 (5)
            row.outcome = srcRow.outcomeSum
            break
        case 827: // 6.11 (9)
            row.outcome = (getRecBuyId() == srcRow.transactionType ? srcRow.sum : 0)
            break
        case 835: // 6.14 (17)
            row.outcome = ((srcRow.income ?: 0) - (srcRow.outcome ?: 0) < 0 ? srcRow.price : 0)
            break
        case 837: // 6.15 (18)
        case 811: // 6.17 (15)
            row.outcome = ((srcRow.income ?: 0) - (srcRow.outcome ?: 0) < 0 ? srcRow.cost : 0)
            break
        case 839: // 6.16 (16)
            row.outcome = ((srcRow.incomeSum ?: 0) - (srcRow.outcomeSum ?: 0) < 0 ? srcRow.price : 0)
            break
        case 838: // 6.18 (19)
            row.outcome = ((srcRow.incomeSum ?: 0) - (srcRow.outcomeSum ?: 0) < 0 ? srcRow.total : 0)
            break
        case 831: // 6.20 (23)
            row.outcome = srcRow.outcome
            break
        case 833: // 6.24 (22)
            row.outcome = srcRow.cost
            break
        case 836: // 6.25 (26)
            row.outcome = srcRow.price
            break
    }
    if (row.outcome == null) {
        row.outcome = 0
    }

    // Графа 30
    row.contractNum = srcRow.docNumber

    // Графа 31
    row.contractDate = srcRow.docDate

    // Графа 46
    switch (formTypeId) {
        case 827: // 6.11 (9)
            row.dealDoneDate = srcRow.dealDate
            break
        default:
            row.dealDoneDate = srcRow.dealDoneDate
            break
    }

    // Графа 51
    row.organName = srcRow.name

    return row
}

// Проставляет статические строки
void addAllStatic(def dataRows) {
    if (logger.containsLevel(LogLevel.ERROR)) {
        return
    }
    def temp = []
    def prevOrganName = null
    def boolean firstRow = true
    for (int i = 0; i < dataRows.size(); i++) {
        def row = dataRows.get(i)
        if (firstRow || row.organName != prevOrganName) {
            firstRow = false
            prevOrganName = row.organName
            def newRow = formData.createDataRow()
            newRow.getCell('groupName').colSpan = 56
            ['groupName', 'dealNum1', 'interdependenceSing', 'f121', 'f122', 'f123', 'f124',
                    'f131', 'f132', 'f133', 'f134', 'f135', 'similarDealGroup', 'dealNameCode',
                    'taxpayerSideCode', 'dealPriceSign', 'dealPriceCode', 'dealMemberCount',
                    'income', 'incomeIncludingRegulation', 'outcome', 'outcomeIncludingRegulation',
                    'dealNum2', 'dealType', 'dealSubjectName', 'dealSubjectCode1', 'dealSubjectCode2',
                    'dealSubjectCode3', 'otherNum', 'contractNum', 'contractDate', 'countryCode',
                    'countryCode1', 'region1', 'city1', 'locality1', 'countryCode2', 'region2',
                    'city2', 'locality2', 'deliveryCode', 'okeiCode', 'count', 'price', 'total',
                    'dealDoneDate', 'dealNum3', 'dealMemberNum', 'organInfo', 'countryCode3',
                    'organName', 'organINN', 'organKPP', 'organRegNum', 'taxpayerCode', 'address'].each {
                newRow.getCell(it).setStyleAlias('Раздел 2 уровня')
            }
            newRow.groupName = getRefBookValue(520, row.organName)?.NAME?.value
            newRow.setAlias('grp#'.concat(i.toString()))
            temp.add(newRow)
        }
        temp.add(row)
    }
    dataRows.clear()
    dataRows.addAll(temp)
}

def getXML(def String startStr, def String endStr) {
    def fileName = (UploadFileName ? UploadFileName.toLowerCase() : null)
    if (fileName == null || fileName == '') {
        throw new ServiceException('Имя файла не должно быть пустым')
    }
    def is = ImportInputStream
    if (is == null) {
        throw new ServiceException('Поток данных пуст')
    }
    if (!fileName.endsWith('.xls') && !fileName.endsWith('.xlsx') && !fileName.endsWith('.xlsm')) {
        throw new ServiceException('Выбранный файл не соответствует формату xls/xlsx/xlsm!')
    }
    def xmlString = importService.getData(is, fileName, 'windows-1251', startStr, endStr)
    if (xmlString == null) {
        throw new ServiceException('Отсутствие значения после обработки потока данных')
    }
    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null) {
        throw new ServiceException('Отсутствие значения после обработки потока данных')
    }
    return xml
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 55
    int HEADER_ROW_COUNT = 4
    // ищет со второй графы
    String TABLE_START_VALUE = 'РАЗДЕЛ 1А. Сведения о контролируемой сделке (группе однородных сделок)'
    String TABLE_END_VALUE = null

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset' : 0, 'colOffset' : 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT, tmpRow)
    if (logger.containsLevel(LogLevel.ERROR)) {
        return
    }
    // освобождение ресурсов для экономии памяти
    headerValues.clear()
    headerValues = null

    def fileRowIndex = paramsMap.rowOffset
    def colOffset = paramsMap.colOffset
    paramsMap.clear()
    paramsMap = null

    def rowIndex = 0
    def rows = []
    def allValuesCount = allValues.size()
    def boolean emptyRow = false

    // формирвание строк нф
    for (def i = 0; i < allValuesCount; i++) {
        rowValues = allValues[0]
        fileRowIndex++
        // все строки пустые - выход
        if ((rowValues.find { it != "" }.toString()) == "") {
            if (emptyRow) {
                allValues.remove(rowValues)
                rowValues.clear()
                break
            }
            emptyRow = true
        }
        // строка пустая
        if (!rowValues) {
            allValues.remove(rowValues)
            rowValues.clear()
            break
        }
        // все значения строки пустые
        if (rowValues.isEmpty() || !rowValues.find { it }) {
            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }
        emptyRow = false

        // простая строка
        rowIndex++
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
        rows.add(newRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    showMessages(rows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        updateIndexes(rows)
        formDataService.getDataRowHelper(formData).allCached = rows
    }
}

/**
 * Проверить шапку таблицы
 *
 * @param headerRows строки шапки
 * @param colCount количество колонок в таблице
 * @param rowCount количество строк в таблице
 * @param tmpRow вспомогательная строка для получения названии графов
 */
void checkHeaderXls(def headerRows, def colCount, rowCount, def tmpRow) {
    if (headerRows.isEmpty()) {
        throw new ServiceException(WRONG_HEADER_ROW_SIZE)
    }
    checkHeaderSize(headerRows[headerRows.size() - 1].size(), headerRows.size(), colCount, rowCount)

    def headerMapping = [
            ([(headerRows[0][0]) : 'РАЗДЕЛ 1А. Сведения о контролируемой сделке (группе однородных сделок)']),
            ([(headerRows[0][21]): 'РАЗДЕЛ 1Б. Сведения о предмете сделки (группы однородных сделок)']),
            ([(headerRows[0][45]): 'РАЗДЕЛ 2. Сведения об организации - участнике контролируемой сделки (группы однородных сделок)']),
            ([(headerRows[1][0]) : 'п. 010 Порядковый номер сделки']),
            ([(headerRows[1][1]) : 'I. Основания для контроля сделки (группы однородных сделок) в соответствии со статьей 105.14 НК РФ ']),
            ([(headerRows[1][11]): 'II. Сведения о контролируемой сделке (группе однородных сделок)']),
            ([(headerRows[1][17]): 'III. Сумма полученных доходов и произведенных расходов налогоплательщика по контролируемой сделке (группе однородных сделок)']),
            ([(headerRows[1][21]): 'I. Общие сведения о предмете сделки (группы однородных сделок)']),
            ([(headerRows[1][23]): 'II. Перечень предметов сделки (группы однородных сделок)']),
            ([(headerRows[1][45]): 'I. Сведения об организации']),
            ([(headerRows[2][1]) : 'Взаимозависимость']),
            ([(headerRows[2][2]) : 'Основания для признания сделки контролируемой согласно статье 105.14 НК РФ']),
            ([(headerRows[2][6]) : 'Особенности отнесения сделки к контролируемой при ее совершении с российским взаимозависимым лицом']),
            ([(headerRows[2][11]): getColumnName(tmpRow, 'similarDealGroup')]),
            ([(headerRows[2][12]): getColumnName(tmpRow, 'dealNameCode')]),
            ([(headerRows[2][13]): getColumnName(tmpRow, 'taxpayerSideCode')]),
            ([(headerRows[2][14]): getColumnName(tmpRow, 'dealPriceSign')]),
            ([(headerRows[2][15]): getColumnName(tmpRow, 'dealPriceCode')]),
            ([(headerRows[2][16]): getColumnName(tmpRow, 'dealMemberCount')]),
            ([(headerRows[2][17]): getColumnName(tmpRow, 'income')]),
            ([(headerRows[2][18]): getColumnName(tmpRow, 'incomeIncludingRegulation')]),
            ([(headerRows[2][19]): getColumnName(tmpRow, 'outcome')]),
            ([(headerRows[2][20]): getColumnName(tmpRow, 'outcomeIncludingRegulation')]),
            ([(headerRows[2][21]): getColumnName(tmpRow, 'dealNum2')]),
            ([(headerRows[2][22]): getColumnName(tmpRow, 'dealType')]),
            ([(headerRows[2][23]): getColumnName(tmpRow, 'dealSubjectName')]),
            ([(headerRows[2][24]): getColumnName(tmpRow, 'dealSubjectCode1')]),
            ([(headerRows[2][25]): getColumnName(tmpRow, 'dealSubjectCode2')]),
            ([(headerRows[2][26]): getColumnName(tmpRow, 'dealSubjectCode3')]),
            ([(headerRows[2][27]): getColumnName(tmpRow, 'otherNum')]),
            ([(headerRows[2][28]): getColumnName(tmpRow, 'contractNum')]),
            ([(headerRows[2][29]): getColumnName(tmpRow, 'contractDate')]),
            ([(headerRows[2][30]): getColumnName(tmpRow, 'countryCode')]),
            ([(headerRows[2][31]): 'п. 080 "Место отправки (погрузки) товара в соответствии с товаросопроводительными документами (заполняется только для товаров)"']),
            ([(headerRows[2][35]): 'п. 090 "Место совершения сделки (адрес места доставки (разгрузки товара), оказания услуги, работы, совершения сделки с иными объектами гражданских прав)"']),
            ([(headerRows[2][39]): getColumnName(tmpRow, 'deliveryCode')]),
            ([(headerRows[2][40]): getColumnName(tmpRow, 'okeiCode')]),
            ([(headerRows[2][41]): getColumnName(tmpRow, 'count')]),
            ([(headerRows[2][42]): getColumnName(tmpRow, 'price')]),
            ([(headerRows[2][43]): getColumnName(tmpRow, 'total')]),
            ([(headerRows[2][44]): getColumnName(tmpRow, 'dealDoneDate')]),
            ([(headerRows[2][45]): getColumnName(tmpRow, 'dealNum3')]),
            ([(headerRows[2][46]): getColumnName(tmpRow, 'dealMemberNum')]),
            ([(headerRows[2][47]): getColumnName(tmpRow, 'organInfo')]),
            ([(headerRows[2][48]): getColumnName(tmpRow, 'countryCode3')]),
            ([(headerRows[2][49]): getColumnName(tmpRow, 'organName')]),
            ([(headerRows[2][50]): getColumnName(tmpRow, 'organINN')]),
            ([(headerRows[2][51]): getColumnName(tmpRow, 'organKPP')]),
            ([(headerRows[2][52]): getColumnName(tmpRow, 'organRegNum')]),
            ([(headerRows[2][53]): getColumnName(tmpRow, 'taxpayerCode')]),
            ([(headerRows[2][54]): getColumnName(tmpRow, 'address')]),
            ([(headerRows[3][1]) : getColumnName(tmpRow, 'interdependenceSing')]),
            ([(headerRows[3][2]) : getColumnName(tmpRow, 'f121')]),
            ([(headerRows[3][3]) : getColumnName(tmpRow, 'f122')]),
            ([(headerRows[3][4]) : getColumnName(tmpRow, 'f123')]),
            ([(headerRows[3][5]) : getColumnName(tmpRow, 'f124')]),
            ([(headerRows[3][6]) : getColumnName(tmpRow, 'f131')]),
            ([(headerRows[3][7]) : getColumnName(tmpRow, 'f132')]),
            ([(headerRows[3][8]) : getColumnName(tmpRow, 'f133')]),
            ([(headerRows[3][9]) : getColumnName(tmpRow, 'f134')]),
            ([(headerRows[3][10]): getColumnName(tmpRow, 'f135')]),
            ([(headerRows[3][31]): 'Код страны по классификатору ОКСМ (цифровой)']),
            ([(headerRows[3][32]): 'Регион (код)']),
            ([(headerRows[3][33]): 'Город']),
            ([(headerRows[3][34]): 'Населенный пункт (село, поселок и т.д.)']),
            ([(headerRows[3][35]): 'Код страны по классификатору ОКСМ (цифровой)']),
            ([(headerRows[3][36]): 'Регион (код)']),
            ([(headerRows[3][37]): 'Город']),
            ([(headerRows[3][38]): 'Населенный пункт (село, поселок и т.д.)'])
    ]
    checkHeaderEquals(headerMapping, logger)
}

/**
 * Получить новую строку нф по значениям из экселя.
 *
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 */
def getNewRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex) {
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // 3. п. 100
    def colIndex = 1
    newRow.interdependenceSing = getRecordIdImport(69, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    // 4. п. 121
    colIndex++
    newRow.f121 = getYesNoByNumber(parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true))
    // 5. п. 122
    colIndex++
    newRow.f122 = getYesNoByNumber(parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true))
    // 6. п. 123
    colIndex++
    newRow.f123 = getYesNoByNumber(parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true))
    // 7. п. 124
    colIndex++
    newRow.f124 = getYesNoByNumber(parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true))
    // 8. п. 131
    colIndex++
    newRow.f131 = getYesNoByNumber(parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true))
    // 9. п. 132
    colIndex++
    newRow.f132 = getYesNoByNumber(parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true))
    // 10. п. 133
    colIndex++
    newRow.f133 = getYesNoByNumber(parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true))
    // 11. п. 134
    colIndex++
    newRow.f134 = getYesNoByNumber(parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true))
    // 12. п. 135 (до 2014 г. / после 2014 г.)
    colIndex++
    newRow.f135 = getYesNoByNumber(parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true))
    // 13. п. 200 "Группа однородных сделок"
    colIndex++
    newRow.similarDealGroup = getYesNoByNumber(parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true))
    // 14. п. 210 "Код наименования сделки"
    colIndex++
    newRow.dealNameCode = getRecordIdImport(67, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    // 15. п. 211 "Код стороны сделки, которой является налогоплательщик"
    colIndex++
    newRow.taxpayerSideCode = getRecordIdImport(65, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    // 16. п. 220 "Признак определения цены сделки с учетом особенностей, предусмотренных статьей 105.4 НК РФ (регулируемые цены)"
    colIndex++
    newRow.dealPriceSign = getYesNoByNumber(parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true))
    // 17. п. 230 "Код определения цены сделки"
    colIndex++
    newRow.dealPriceCode = getRecordIdImport(66, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    // 18. п. 260 "Количество участников сделки"
    colIndex++
    newRow.dealMemberCount = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // 19. п. 300 "Сумма доходов налогоплательщика по контролируемой сделке (группе однородных сделок) в рублях"
    colIndex++
    newRow.income = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // 20. п. 301 "в том числе сумма доходов по сделкам, цены которых подлежат регулированию"
    colIndex++
    newRow.incomeIncludingRegulation = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // 21. п. 310 "Сумма расходов налогоплательщика по контролируемой сделке (группе однородных сделок) в рублях"
    colIndex++
    newRow.outcome = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // 22.	п. 311 "в том числе сумма расходов по сделкам, цены которых подлежат регулированию"
    colIndex++
    newRow.outcomeIncludingRegulation = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // 23.
    colIndex++
    // 24. п. 020 "Тип предмета сделки"
    colIndex++
    newRow.dealType = getRecordIdImport(64, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    // 25. п. 030 "Наименование предмета сделки"
    colIndex++
    newRow.dealSubjectName = values[colIndex]
    // 26. п. 040 "Код предмета сделки (код по ТН ВЭД)"
    colIndex++
    def tmp = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    def record73Id = (tmp ? getRecordIdImport(73, 'CODE', tmp.toString(), fileRowIndex, colIndex + colOffset) : null)
    if (record73Id) {
        newRow.dealSubjectCode1 = getRecordIdImport(17, 'TN_VED_CODE', record73Id.toString(), fileRowIndex, colIndex + colOffset)
    }
    // 27. п. 043 "Код предмета сделки (код по ОКП)"
    colIndex++
    def record68Id = getRecordIdImport(68, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    if (record68Id) {
        newRow.dealSubjectCode2 = getRecordIdImport(17, 'OKP_CODE', record68Id.toString(), fileRowIndex, colIndex + colOffset)
    }
    // 28.	п. 045 "Код предмета сделки (код по ОКВЭД)"
    colIndex++
    newRow.dealSubjectCode3 = getRecordIdImport(34, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    // 29. п. 050 "Номер другого участника сделки"
    colIndex++
    newRow.otherNum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // 30. п. 060 "Номер договора"
    colIndex++
    newRow.contractNum = values[colIndex]
    // 31. п. 065 "Дата договора"
    colIndex++
    newRow.contractDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    // 32. п. 070 "Код страны происхождения предмета сделки по классификатору ОКСМ (цифровой)"
    colIndex++
    newRow.countryCode = getRecordIdImport(10, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    // 33. Код страны по классификатору ОКСМ (цифровой)
    colIndex++
    newRow.countryCode1 = getRecordIdImport(10, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    // 34. Регион (код)
    colIndex++
    newRow.region1 = getRecordIdImport(4, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    // 35. Город
    colIndex++
    newRow.city1 = values[colIndex]
    // 36. Населенный пункт (село, поселок и т.д.)
    colIndex++
    newRow.locality1 = values[colIndex]
    // 37. Код страны по классификатору ОКСМ (цифровой)
    colIndex++
    newRow.countryCode2 = getRecordIdImport(10, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    // 38. Регион (код)
    colIndex++
    newRow.region2 = getRecordIdImport(4, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    // 39. Город
    colIndex++
    newRow.city2 = values[colIndex]
    // 40. Населенный пункт (село, поселок и т.д.)
    colIndex++
    newRow.locality2 = values[colIndex]
    // 41. п. 100 "Код условия поставки (заполняется только для товаров)"
    colIndex++
    newRow.deliveryCode = getRecordIdImport(63, 'STRCODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    // 42. п. 110 "Код единицы измерения по ОКЕИ"
    colIndex++
    newRow.okeiCode = getRecordIdImport(12, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    // 43. п. 120 "Количество"
    colIndex++
    newRow.count = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // 44. п. 130 "Цена (тариф) за единицу измерения без учета НДС, акцизов и пошлины, руб."
    colIndex++
    newRow.price = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // 45. п. 140 "Итого стоимость без учета НДС, акцизов и пошлины, руб."
    colIndex++
    newRow.total = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // 46. п. 150 "Дата совершения сделки (цифрами день, месяц, год)"
    colIndex++
    newRow.dealDoneDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    // 47.
    colIndex++
    // 48. п. 015 "Порядковый номер участника сделки (из раздела 1Б)"
    colIndex++
    newRow.dealMemberNum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // 49
    colIndex++
    // 50. п. 030 "Код страны по классификатору ОКСМ"
    colIndex++
    newRow.countryCode3 = getRecordIdImport(10, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    // 51. п. 040 "Наименование организации"
    colIndex++
    newRow.organName = getRecordIdImport(520, 'NAME', values[colIndex], fileRowIndex, colIndex + colOffset)
    def record520 = getRefBookValue(520, newRow.organName)
    if (record520 != null) {
        // 49. п. 020 "Сведения об организации"
        colIndex = 47
        def record513 = getRefBookValue(513, record520.ORG_CODE?.value)
        def parentColumnName = getColumnName(newRow, 'organName')
        def parentColumnValue = record520?.NAME?.value
        def expectedValue = record513?.CODE?.value?.toString()
        formDataService.checkReferenceValue(values[colIndex], [expectedValue], parentColumnName, parentColumnValue, fileRowIndex, colIndex + colOffset, logger, false)

        // 52. п. 050 "ИНН организации"
        colIndex = 50
        expectedValue = (record520.INNKIO?.stringValue != null ? record520.INNKIO?.stringValue : "")
        formDataService.checkReferenceValue(values[colIndex], [expectedValue], parentColumnName, parentColumnValue, fileRowIndex, colIndex + colOffset, logger, false)

        // 53. п. 060 "КПП организации"
        colIndex++
        expectedValue = (record520.KPP?.stringValue != null ? record520.KPP?.stringValue : "")
        formDataService.checkReferenceValue(values[colIndex], [expectedValue], parentColumnName, parentColumnValue, fileRowIndex, colIndex + colOffset, logger, false)

        // 54. п. 070 "Регистрационный номер организации в стране ее регистрации (инкорпорации)"
        colIndex++
        expectedValue = (record520.RS?.stringValue != null ? record520.RS?.stringValue : "")
        formDataService.checkReferenceValue(values[colIndex], [expectedValue], parentColumnName, parentColumnValue, fileRowIndex, colIndex + colOffset, logger, false)

        // 55. п. 080 "Код налогоплательщика в стране регистрации (инкорпорации) или его аналог (если имеется)"
        colIndex++
        expectedValue = (record520.TAX_CODE_INCORPORATION?.stringValue != null ? record520.TAX_CODE_INCORPORATION?.stringValue : "")
        formDataService.checkReferenceValue(values[colIndex], [expectedValue], parentColumnName, parentColumnValue, fileRowIndex, colIndex + colOffset, logger, false)

        // 56. п. 090 "Адрес"
        colIndex++
        if(record513?.CODE?.value == 2) {
            expectedValue = (record520.ADDRESS?.stringValue != null ? record520.ADDRESS?.stringValue : "")
            formDataService.checkReferenceValue(values[colIndex], [expectedValue], parentColumnName, parentColumnValue, fileRowIndex, colIndex + colOffset, logger, false)
        }
        newRow.address = (record513?.CODE?.value == 2 ? record520?.ADDRESS?.value : '')
    }

    return newRow
}

def getYesNoByNumber(def number) {
    if (number == 1) {
        return getRecYesId()
    } else if (number == 0) {
        return getRecNoId()
    }
    return null
}

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, getSubTotalRows(dataRows), null, false)
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows)
    }
}

// Получение подитоговых строк
def getSubTotalRows(def dataRows) {
    return dataRows.findAll { it.getAlias() != null}
}