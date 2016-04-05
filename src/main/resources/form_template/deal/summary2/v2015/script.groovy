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
 *      - сверить алиасы и ТИПЫ столбцов старых источников с алиасами и ТИПАМИ новых источников (шестерок)
 *      - отладить скрипт консолидацией со всеми источниками
 *      - проверить логические проверки
 *      - проверить проверку перед консолидацией
 *      - добавить обработку формы 4.2
 *      - сделать/поправить загрузку из эксель
 *      - добавить тесты
 * Поправленные методы:
 *      - getReportClass
 *      - getGroupId
 *      - isGroupClass2
 * Остановился на строке "// копируем построчно - без группировки"
 */

// TODO:
//      - сверить алиасы старых источников с алиасами шестерок
//
//      - проверить логические проверки
//      - проверить проверку перед консолидацией

// 1.	name    					Наименование
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
// TODO (Ramil Timerbaev) сменился справочник
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
// TODO (Ramil Timerbaev) сменился тип
// 50.	countryCode3				п. 030 "Код страны по классификатору ОКСМ"
// 51.	organName					п. 040 "Наименование организации"
// TODO (Ramil Timerbaev) сменился тип
// 52.	organINN					п. 050 "ИНН организации"
// TODO (Ramil Timerbaev) сменился тип
// 53.	organKPP					п. 060 "КПП организации"
// TODO (Ramil Timerbaev) сменился тип
// 54.	organRegNum					п. 070 "Регистрационный номер организации в стране ее регистрации (инкорпорации)"
// TODO (Ramil Timerbaev) сменился тип
// 55.	taxpayerCode				п. 080 "Код налогоплательщика в стране регистрации (инкорпорации) или его аналог (если имеется)"
// TODO (Ramil Timerbaev) сменился тип
// 56.	address						п. 090 "Адрес"

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkCreation()
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
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
    case FormDataEvent.AFTER_MOVE_CREATED_TO_ACCEPTED:
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED:
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
        formDataService.saveCachedDataRows(formData, logger)
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

// обязательные графы (1, 2, 4..19, 21, 23..25, 29..31, 42..51)
@Field
def nonEmptyColumns = ['name', 'dealNum1', 'f121', 'f122', 'f123', 'f124', 'f131', 'f132', 'f133', 'f134', 'f135',
        'similarDealGroup', 'dealNameCode', 'taxpayerSideCode', 'dealPriceSign', 'dealPriceCode', 'dealMemberCount',
        'income', 'outcome', 'dealNum2', 'dealType', 'dealSubjectName', 'otherNum', 'contractNum', 'contractDate',
        'okeiCode', 'count', 'price', 'total', 'dealDoneDate', 'dealNum3', 'dealMemberNum', 'organInfo', 'countryCode3', 'organName']

@Field
def startDate = null

// Дата окончания отчетного периода
@Field
def endDate = null

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
            reportPeriodEndDate, rowIndex, colIndex, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Проверка при создании формы
void checkCreation() {
    def findForm = formDataService.find(formData.formType.id, formData.kind, formData.departmentReportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing);
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
        // TODO (Ramil Timerbaev) проверить
        def onlyNo = (row.f131 == recNoId && row.f132 == recNoId && row.f133 == recNoId && row.f134 == recNoId && row.f135 == recNoId)
        def haveNo = (row.f131 == recNoId || row.f132 == recNoId || row.f133 == recNoId || row.f134 == recNoId || row.f135 == recNoId)
        if (((row.f122 == recYesId || row.f123 == recYesId) && !onlyNo) ||
                (haveNo && (row.f122 != recYesId || row.f123 != recYesId))
        ) {
            def msg = "Строка %d: Не допускается одновременное заполнение значением «1» любой из граф «Наименование поля 5», «Наименование поля 6» с любой из граф «%s»!"
            def names = []
            ['f131', 'f132', 'f133', 'f134', 'f135'].each { alias ->
                names.add(getColumnName(row, alias))
            }
            subMsd = names.join('», «')
            logger.error(msg, row.getIndex(), getColumnName(row, 'f122'), getColumnName(row, 'f123'), subMsd)
        }

        // 4. Проверка одновременного заполнения доходов и расходов
        // TODO (Ramil Timerbaev) проверить
        if (row.income != null && row.income > 0 && row.outcome != null && row.outcome > 0) {
            def msg = "Строка %d: Значение граф «%s» и  «%s» не должны быть одновременно больше «0»!"
            logger.error(msg, row.getIndex(), getColumnName(row, 'income'), getColumnName(row, 'outcome'))
        }

        // 5. Проверка неотрицательности доходов
        // 6. Проверка неотрицательности расходов
        // TODO (Ramil Timerbaev) проверить
        ['income', 'outcome'].each { alias ->
            if (row[alias] != null && row[alias] < 0) {
                def msg = "Строка %d: Значение графы «%s» должно быть больше или равно «0»!"
                logger.error(msg, row.getIndex(), getColumnName(row, alias))
            }
        }

        // 7. Проверка одновременного заполнения полей «Код предмета сделки»
        // TODO (Ramil Timerbaev) проверить
        if (row.dealSubjectCode1 != null && row.dealSubjectCode2 != null) {
            def msg = "Строка %d: Значение граф «%s» и «%s» не должны быть одновременно заполнены!"
            logger.error(msg, row.getIndex(), getColumnName(row, 'dealSubjectCode1'), getColumnName(row, 'dealSubjectCode2'))
        }

        // 8. Проверка заполнения граф «ИНН, КПП организации»
        def organInfo = null
        def record520 = getRefBookValue(520, row.organName)
        if (record520?.ORG_CODE?.value) {
            def record513 = getRefBookValue(513, record520?.ORG_CODE?.value)
            organInfo = record513?.CODE?.value
        }
        if (organInfo != null && organInfo == 1) {
            // a
            // TODO (Ramil Timerbaev) проверить
            ['organINN', 'organKPP'].each { alias ->
                if (row[alias] == null) {
                    def msg = "Строка %d: Значение графы «%s» должно быть заполнено, т.к. значение графы «%s» равно «1»!"
                    logger.error(msg, row.getIndex(), getColumnName(row, alias), getColumnName(row, 'organInfo'))
                }
            }
        }
        if (organInfo != null && organInfo == 2 && row.organRegNum == null && row.taxpayerCode == null) {
            // b
            // TODO (Ramil Timerbaev) проверить
            def msg = "Строка %d: Значение графы «%s» или графы «%s» должно быть заполнено, т.к. значение графы «%s» равно «2»!"
            logger.error(msg, row.getIndex(), getColumnName(row, 'organRegNum'), getColumnName(row, 'taxpayerCode'), getColumnName(row, 'organInfo'))
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

/**
 * Логические проверки перед консолидацией.
 *
 * @param departmentFormTypes список источников
 */
def preConsolidationCheck(def departmentFormTypes) {
    // 1. Проверка на наличие формы «Приложение 4.2»
    def app4_2Id = 803
    def app4_2Find = false
    for (def departmentFormType : departmentFormTypes) {
        if (departmentFormType.formTypeId == app4_2Id) {
            def source = formDataService.getLast(departmentFormType.formTypeId, departmentFormType.kind, departmentFormType.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                app4_2Find = true
                break
            }
        }
    }
    if (!app4_2Find) {
        def reportPeriod = reportPeriodService.get(formData.reportPeriodId)
        def periodName = reportPeriod.name + ' ' + reportPeriod.taxPeriod.year
        logger.error("Не существует формы-источника «Приложение 4.2» в статусе «Принята» за период «%s»!", periodName)
        return false
    }
    return true
}

// Консолидация
void consolidation() {
    def departmentFormTypes = departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind,
            getReportPeriodStartDate(), getReportPeriodEndDate())
    // TODO (Ramil Timerbaev) проверить
    if (!preConsolidationCheck(departmentFormTypes)) {
        return
    }

    def matrixRows = []
    def rowsMap = [:]
    // счетчик по группам для табл. 86
    def int i = 1
    // мапа идентификаторов для группировки
    def groupStr = [:]

    // row → тип формы-источника
    def final typeMap = [:]
    // row → класс группы
    def final classMap = [:]
    // row → id группы
    def final groupMap = [:]

    departmentFormTypes.each {
        def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
        if (source != null && source.state == WorkflowState.ACCEPTED && source.formType.taxType == TaxType.DEAL) {
            formDataService.getDataRowHelper(source).allSaved.each { srcRow ->
                if (srcRow.getAlias() == null) {
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

    // Графа 2
    row.interdependenceSing = getRecordId(69, 'CODE', '1')

    // Графа 3
    // row.f121, заполняется после графы 50

    // Графа 4
    row.f122 = recNoId
    if (formTypeId == 837 && srcRow.signPhis != null && srcRow.dependence == recNoId && srcRow.signTransaction == recYesId) {
        def signPhis = getRefBookValue(18, srcRow.signPhis)
        if (signPhis != null && signPhis.SIGN.stringValue.equals("Физическая поставка")) {
            row.f122 = recYesId
        }
    }

    // Графа 5
    // row.f123, заполняется после графы 50

    // Графа 6
    row.f124 = recNoId

    // Графа 7
    // row.f131, заполняется после графы 50

    // Графа 8
    row.f132 = recNoId

    // Графа 9
    row.f133 = recNoId

    // Графа 10
    // row.f134, заполняется после графы 50

    // Графа 11
    row.f135 = recNoId

    // Графа 12
    switch (formTypeId) {
        case 812: // 6.3 (1)
        case 814: // 6.5 (2)
        case 826: // 6.13 (5)
        case 813: // 6.4 (7)
        case 828: // 6.19 (20)
        case 833: // 6.24 (22)
            row.similarDealGroup = getRecYesId()
            break
        case 805: // 6.7 (4)
        case 819: // 6.12 (6)
        case 817: // 6.9 (10)
        case 816: // 6.1 (12)
        case 832: // 6.23 (21)
        case 836: // 6.25 (26)
            row.similarDealGroup = getRecNoId()
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

    // Графа 13
    def String val13 = null
    switch (formTypeId) {
        case 812: // 6.3 (1)
            val13 = '002'
            break
        case 814: // 6.5 (2)
        case 815: // 6.8 (3)
        case 826: // 6.13 (5)
        case 813: // 6.4 (7)
        case 833: // 6.24 (22)
            val13 = '019'
            break
        case 805: // 6.7 (4)
        case 832: // 6.23 (21)
            val13 = '016'
            break
        case 806: // 6.6 (8)
        case 839: // 6.16 (16)
        case 835: // 6.14 (17)
            val13 = '032'
            break
        case 827: // 6.11 (9)
        case 819: // 6.12 (6)
        case 838: // 6.18 (19)
            val13 = '015'
            break
        case 837: // 6.15 (18)
            val13 = '032'
            break
        case 817: // 6.9 (10)
        case 836: // 6.25 (26)
            val13 = '029'
            break
        case 825: // 6.10.2 (11)
        case 823: // 6.10.1 (13)
        case 834: // 6.22 (24)
        case 830: // 6.21 (25)
            val13 = '003'
            break
        case 831: // 6.20 (23)
            val13 = '012'
            break
        case 816: // 6.1 (12)
        case 804: // 6.2 (14)
            val13 = '012'
            break
        case 811: // 6.17 (15)
            val13 = '017'
            break
        case 828: // 6.19 (20)
            val13 = '004'
            break
    }
    if (val13 != null) {
        row.dealNameCode = getRecordId(67, 'CODE', "$val13")
    }

    // Графа 14
    def String val14 = null
    switch (formTypeId) {
        case 812: // 6.3 (1)
            if (srcRow.incomeBankSum != null) {
                val14 = '004'
            } else if (srcRow.outcomeBankSum != null) {
                val14 = '003'
            }
            break
        case 814: // 6.5 (2)
        case 815: // 6.8 (3)
        case 826: // 6.13 (5)
        case 833: // 6.24 (22)
            val14 = '012'
            break
        case 805: // 6.7 (4)
        case 832: // 6.23 (21)
            val14 = '028'
            break
            break
        case 819: // 6.12 (6)
            if ((srcRow.incomeSum ?: 0) == 0 && (srcRow.outcomeSum ?: 0) > 0) {
                val14 = '026'
            } else if ((srcRow.incomeSum ?: 0) > 0 && (srcRow.outcomeSum ?: 0) == 0) {
                val14 = '027'
            }
            break
        case 813: // 6.4 (7)
            val14 = '011'
            break
        case 806: // 6.6 (8)
        case 839: // 6.16 (16)
        case 835: // 6.14 (17)
        case 837: // 6.15 (18)
            val14 = '052'
            break
        case 827: // 6.11 (9)
            if (getRecBuyId().equals(srcRow.transactionType)) {
                val14 = '026'
            } else if (getRecSellId().equals(srcRow.transactionType)) {
                val14 = '027'
            }
            break
        case 817: // 6.9 (10)
        case 816: // 6.1 (12)
        case 804: // 6.2 (14)
            val14 = '022'
            break
        case 825: // 6.10.2 (11)
        case 823: // 6.10.1 (13)
            val14 = '005'
            break
        case 811: // 6.17 (15)
            val14 = '030'
            break
        case 838: // 6.18 (19)
            def boolean dealBuy = getRecDealBuyId().equals(srcRow.dealFocus)
            val14 = (dealBuy ? '026' : '027')
            break
        case 828: // 6.19 (20)
            val14 = '007'
            break
        case 831: // 6.20 (23)
            val14 = '020'
            break
        case 834: // 6.22 (24)
        case 830: // 6.21 (25)
            val14 = '002'
            break
        case 836: // 6.25 (26)
            val14 = '048'
            break
    }
    if (val14 != null) {
        row.taxpayerSideCode = getRecordId(65, 'CODE', "$val14")
    }

    // Графа 15
    // справочное, заполняется после графы 50, по-умолчанию 0
    row.dealPriceSign = recNoId

    // Графа 16
    def int val16 = 0
    switch (formTypeId) {
        case 817: // 6.9 (10)
            val16 = 3
            break
        case 827: // 6.11 (9)
            if (srcRow.dealMode != null) {
                def val16Rec = getRefBookValue(14, srcRow.dealMode)
                if (val16Rec.ID.value != 2) {
                    val16 = 2
                }
            }
            break
    }
    row.dealPriceCode = getRecordId(66, 'CODE', "$val16")

    // Графа 17
    row.dealMemberCount = 2

    // Графа 18
    // заполняется предварительно для каждой строки getPreRow
    row.income = 0

    // Графа 20
    // заполняется предварительно для каждой строки getPreRow
    row.outcome = 0

    // Графа 23
    def int val23 = 2
    switch (formTypeId) {
        case 805: // 6.7 (4)
        case 817: // 6.9 (10)
        case 828: // 6.19 (20)
        case 831: // 6.20 (23)
        case 836: // 6.25 (26)
            val23 = 3
            break
        case 837: // 6.15 (18)
        case 838: // 6.18 (19)
            // TODO (Ramil Timerbaev) проверить необходимость 837 тут
            sign23 = formTypeId == 837 ? srcRow.signPhis : srcRow.deliverySign
            def values23 = getRefBookValue(18, sign23)
            if (values23 != null && values23.SIGN.stringValue.equals("ОМС")) {
                val23 = 2
            } else {
                val23 = 1
            }
            break
    }
    row.dealType = getRecordId(64, 'CODE', "$val23")

    // Графа 24
    switch (formTypeId) {
        case 812: // 6.3 (1)
            if (srcRow.incomeBankSum != null) {
                row.dealSubjectName = 'Предоставление помещений в аренду (субаренду)'
            } else if (srcRow.outcomeBankSum != null) {
                row.dealSubjectName = 'Получение помещений в аренду (субаренду)'
            }
            break
        case 814: // 6.5 (2)
            row.dealSubjectName = 'Услуги, связанные с обслуживанием недвижимости'
            break
        case 815: // 6.8 (3)
            row.dealSubjectName = 'Услуги по разработке, внедрению и модификации программного обеспечения'
            break
        case 805: // 6.7 (4)
            row.dealSubjectName = 'Услуги по предоставлению права пользования товарным знаком'
            break
        case 826: // 6.13 (5)
            row.dealSubjectName = 'Приобретение услуг, связанных с организацией и проведением торгов по реализации имущества'
            break
        case 819: // 6.12 (6)
            def String out = (getRecRPCId().equals(srcRow.dealSign) ? "" : "вне")
            if ((srcRow.incomeSum ?: 0) == 0 && (srcRow.outcomeSum ?: 0) > 0) {
                row.dealSubjectName = "Покупка акций и долей - " + out + "биржевые сделки"
            } else if ((srcRow.incomeSum ?: 0) > 0 && (srcRow.outcomeSum ?: 0) == 0) {
                row.dealSubjectName = "Продажа акций и долей - " + out + "биржевые сделки"
            }
            break
        case 813: // 6.4 (7)
            row.dealSubjectName = 'Оказание банковских услуг'
            break
        case 806: // 6.6 (8)
            def String out = ('Да'.equals(srcRow.dealsMode) ? "" : "вне")
            if (srcRow.incomeSum != null) {
                row.dealSubjectName = "Операции РЕПО - " + out + "биржевые (процентный доход)"
            } else if (srcRow.outcomeSum != null) {
                row.dealSubjectName = "Операции РЕПО - " + out + "биржевые (процентный расход)"
            }
            break
        case 827: // 6.11 (9)
            def String out = (getRecDealsModeId().equals(srcRow.dealMode) ? "" : "вне")
            if (getRecBuyId().equals(srcRow.transactionType)) {
                row.dealSubjectName = "Покупка ЦБ - " + out + "биржевые сделки"
            } else if (getRecSellId().equals(srcRow.transactionType)) {
                row.dealSubjectName = "Продажа ЦБ - " + out + "биржевые сделки"
            }
            break
        case 817: // 6.9 (10)
            row.dealSubjectName = 'Уступка прав требования - с обязательной оценкой'
            break
        case 825: // 6.10.2 (11)
            row.dealSubjectName = 'открытие аккредитивов и инструментов торгового финансирования'
            break
        case 816: // 6.1 (12)
            row.dealSubjectName = 'Размещение денежных средств корпоративным клиентам - не регулируемые сделки'
            break
        case 823: // 6.10.1 (13)
            row.dealSubjectName = 'Выдача гарантий'
            break
        case 804: // 6.2 (14)
            row.dealSubjectName = 'Размещение денежных средств в межбанковские кредиты'
            break
        case 811: // 6.17 (15)
            if (srcRow.income != null) {
                row.dealSubjectName = 'Кассовые конверсионные сделки - доходные'
            } else if (srcRow.outcome != null) {
                row.dealSubjectName = 'Кассовые конверсионные сделки - расходные'
            }
            break
        case 839: // 6.16 (16)
        case 835: // 6.14 (17)
        case 837: // 6.15 (18)
        case 838: // 6.18 (19)
            // расчитывается дальше для группы
            break
        case 828: // 6.19 (20)
            row.dealSubjectName = 'Привлечение денежных средств'
            break
        case 832: // 6.23 (21)
        case 833: // 6.24 (22)
            row.dealSubjectName = srcRow.serviceType
            break
        case 831: // 6.20 (23)
            row.dealSubjectName = 'Привлечение средств на межбанковском рынке'
            break
        case 834: // 6.22 (24)
            row.dealSubjectName = 'Привлечение гарантий'
            break
        case 830: // 6.21 (25)
            row.dealSubjectName = 'Привлечение гарантий (открытие аккредитивов и других инструментов торгового финансирования)'
            break
        case 836: // 6.25 (26)
            row.dealSubjectName = 'Приобретение прав требования'
            break
    }

    def val25and26 = null
    switch (formTypeId) {
        case 837: // 6.15 (18)
            val25and26 = srcRow.innerCode
            break
        case 838: // 6.18 (19)
            val25and26 = srcRow.metalName
            break
    }
    if (val25and26 != null && val23 == 1) {
        def metal = getRefBookValue(17, val25and26)

        // Графа 25
        row.dealSubjectCode1 = metal.TN_VED_CODE.referenceValue

        // Графа 26
        def String innerCode = metal.INNER_CODE.stringValue
        def String code = null;
        if ("A33".equals(innerCode)) {
            code = '17 5140'
        } else if ("A76".equals(innerCode)) {
            code = '17 5120'
        } else if ("A98".equals(innerCode)) {
            code = '17 5340'
        } else if ("A99".equals(innerCode)) {
            code = '17 5220'
        }
        if (code != null) {
            row.dealSubjectCode2 = getRecordId(68, 'CODE', code)
        }
    }

    // Графа 27
    if (val23 in [2, 3]) {
        def String val27 = null
        switch (formTypeId) {
            case 812: // 6.3 (1)
                val27 = '70.20.2'
                break
            case 814: // 6.5 (2)
                val27 = '70.32.2'
                break
            case 815: // 6.8 (3)
                val27 = '72.20'
                break
            case 805: // 6.7 (4)
            case 826: // 6.13 (5)
                val27 = '74.8'
                break
            case 837: // 6.15 (18)
            case 838: // 6.18 (19)
                val27 = '65.12'
                break
            case 819: // 6.12 (6)
            case 827: // 6.11 (9)
            case 825: // 6.10.2 (11)
            case 823: // 6.10.1 (13)
            case 839: // 6.16 (16)
            case 835: // 6.14 (17)
            case 834: // 6.22 (24)
            case 830: // 6.21 (25)
                val27 = '65.23'
                break
            case 813: // 6.4 (7)
                val27 = '65.12'
                break
            case 806: // 6.6 (8)
            case 817: // 6.9 (10)
            case 816: // 6.1 (12)
            case 804: // 6.2 (14)
            case 836: // 6.25 (26)
                val27 = '65.22'
                break
            case 811: // 6.17 (15)
            case 828: // 6.19 (20)
            case 831: // 6.20 (23)
                val27 = '65.12'
                break
            case 832: // 6.23 (21)
            case 833: // 6.24 (22)
                val27 = '74'
                break
        }
        if (val27 != null) {
            row.dealSubjectCode3 = getRecordId(34, 'CODE', "$val27")
        }
    }

    // Графа 28
    row.otherNum = 1

    // Графа 48
    row.dealMemberNum = row.otherNum

    // Графа 29
    // заполняется предварительно для каждой строки getPreRow
    row.contractNum = matrixRow.contractNum

    // Графа 30
    // заполняется предварительно для каждой строки getPreRow
    row.contractDate = matrixRow.contractDate

    // Графа 15
    Calendar compareCalendar15 = Calendar.getInstance()
    compareCalendar15.set(2011, 12, 28)
    if (compareCalendar15.getTime().equals(row.contractDate) && "123".equals(row.contractNum)) {
        row.dealPriceSign = recYesId
    }

    // Графа 31
    switch (formTypeId) {
        case 837: // 6.15 (18)
            row.countryCode = srcRow.unitCountryCode
            break
        case 838: // 6.18 (19)
            row.countryCode = srcRow.countryCodeNumeric
            break
    }

    // Графа 32, Графа 33, Графа 34, Графа 35
    if (formTypeId == 837 || formTypeId == 838) {
        sign32 = formTypeId == 837 ? srcRow.signPhis : srcRow.deliverySign
        if (sign32 != null) {
            def values32 = getRefBookValue(18, sign32)
            if (values32 != null && values32.SIGN.stringValue.equals("Физическая поставка")) {
                if (formTypeId == 837) {
                    row.countryCode1 = srcRow.countryCode2
                    row.region1 = srcRow.region1
                    row.city1 = srcRow.city1
                    row.locality1 = srcRow.settlement1
                }

                if (formTypeId == 838) {
                    row.countryCode1 = srcRow.countryCodeNumeric
                    row.region1 = srcRow.regionCode
                    row.city1 = srcRow.city
                    row.locality1 = srcRow.locality
                }
            }
        }
    }

    // Графа 36, Графа 37, Графа 38, Графа 39
    switch (formTypeId) {
        case 812: // 6.3 (1)
        case 814: // 6.5 (2)
            row.countryCode2 = srcRow.country
            row.region2 = srcRow.region
            row.city2 = srcRow.city
            row.locality2 = srcRow.settlement
            break
        case 837: // 6.15 (18)
            row.countryCode2 = srcRow.countryCode3
            row.region2 = srcRow.region2
            row.city2 = srcRow.city2
            row.locality2 = srcRow.settlement2
            break
        case 838: // 6.18 (19)
            row.countryCode2 = srcRow.countryCodeNumeric2
            row.region2 = srcRow.region2
            row.city2 = srcRow.city2
            row.locality2 = srcRow.locality2
            break
        default:
            row.countryCode2 = getRecRUSId()
            row.region2 = getRecordId(4, 'CODE', '77')
            row.city2 = 'Москва'
            row.locality2 = row.city2
            break
    }

    // Графа 40
    if (val23 == 1) {
        if (formTypeId == 837) {
            row.deliveryCode = srcRow.conditionCode
        } else if (formTypeId == 838) {
            row.deliveryCode = srcRow.deliveryCode
        }
    }

    // Графа 41
    def String val41 = null
    switch (formTypeId) {
        case 812: // 6.3 (1)
        case 814: // 6.5 (2)
            val41 = '055'
            break
        case 815: // 6.8 (3)
        case 805: // 6.7 (4)
        case 826: // 6.13 (5)
        case 813: // 6.4 (7)
        case 806: // 6.6 (8)
        case 827: // 6.11 (9)
        case 811: // 6.17 (15)
        case 839: // 6.16 (16)
        case 835: // 6.14 (17)
        case 825: // 6.10.2 (11)
        case 816: // 6.1 (12)
        case 823: // 6.10.1 (13)
        case 804: // 6.2 (14)
        case 837: // 6.15 (18)
        case 838: // 6.18 (19)
        case 828: // 6.19 (20)
        case 832: // 6.23 (21)
        case 833: // 6.24 (22)
        case 831: // 6.20 (23)
        case 834: // 6.22 (24)
        case 830: // 6.21 (25)
            val41 = '796'
            break
        case 819: // 6.12 (6)
        case 817: // 6.9 (10)
        case 836: // 6.25 (26)
            row.okeiCode = srcRow.okeiCode
            break
    }
    if (val41 != null) {
        row.okeiCode = getRecordId(12, 'CODE', "$val41")
    }

    // Графа 42
    // заполняется после, для группы
    row.count = 0

    // Графа 43
    // заполняется позже, для группы

    // Графа 44
    // заполняется позже, для группы
    row.total = 0

    // Графа 45
    // заполняется предварительно для каждой строки getPreRow
    row.dealDoneDate = matrixRow.dealDoneDate

    // Графа 49
    // countryCode3 заполняется после графы 50

    // Графа 50
    // заполняется предварительно для каждой строки getPreRow
    row.organName = matrixRow.organName

    if (row.organName != null) {
        // Графа 3

        // Если атрибут 50 «Матрицы» содержит значение, в котором в справочнике
        // «Организации – участники контролируемых сделок» атрибут «Резидент оффшорной зоны» = 1,
        // то заполняется значением «0». В ином случае заполняется значением «1».
        def val = getRefBookValue(9, row.organName)
        row.f121 = (val.OFFSHORE.referenceValue == recYesId) ? recNoId : recYesId

        // Графа 5 (логика, обратная графе 3)
        row.f123 = (row.f121 == recYesId) ? recNoId : recYesId

        // Графа 7 (та же логика, что у графы 3)
        row.f131 = row.f121

        // Графа 10
        // Если атрибут 50 «Матрицы» содержит значение, в котором в справочнике
        // «Организации – участники контролируемых сделок» атрибут «Освобождена от налога на прибыль либо является
        // резидентом Сколково» = 1, то заполняется значением «1». В ином случае заполняется значением «0».
        row.f134 = (val.SKOLKOVO.referenceValue == recYesId) ? recYesId : recNoId

        // Графа 49
        // Код страны
        row.countryCode3 = val.COUNTRY?.referenceValue

        // Графа 53, 54, 55 - сменили тип для наглядности: что было видно какие данные попадут в уведомление
        def organizationCode = getRefBookValue(70, val.ORGANIZATION?.referenceValue)?.CODE?.value
        // заполняются только для иностранных организации (код равен 2)
        if (organizationCode == 2) {
            // Графа 53
            row.organRegNum = val?.REG_NUM?.value

            // Графа 54
            row.taxpayerCode = val?.TAXPAYER_CODE?.value

            // Графа 55
            row.address = val?.ADDRESS?.value

            if (!row.organRegNum && !row.taxpayerCode) {
                row.organRegNum = '0'
            }
        }
    }

    // Графа 48, 51, 52
    // зависимые в конфигураторе

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

// значение для группировки строки (табл. 86)
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

// получение строки итогового отчета на основании группы строк (или одной строки) из "матрицы" и источника (табл. 87)
def getRow(def map, def typeMap) {
    // для отчетов 16..19 надо считать суммы по двум столбцам
    def totalSum = 0
    map.each { matrixRow, srcRow ->
        if (typeMap.get(matrixRow) in [839, 838]) {
            totalSum = (srcRow.incomeSum ?: 0) - (srcRow.outcomeSum ?: 0)
        } else if (typeMap.get(matrixRow) in [835, 837]) {
            totalSum = (srcRow.incomeSum ?: 0) - (srcRow.consumptionSum ?: 0)
        }
    }

    def row = formData.createDataRow()
    def boolean first = true
    map.each { matrixRow, srcRow ->
        if (first) {
            first = false

            row = buildRow(srcRow, matrixRow, typeMap)
            if (typeMap.get(matrixRow) in [815, 806, 827, 825, 823, 804, 811, 839, 835, 837, 838, 831, 834, 830]) {
                if (map.size() > 1) {
                    row.similarDealGroup = getRecYesId()
                } else {
                    row.similarDealGroup = getRecNoId()
                }
            }
        }

        // Атрибут «п. 150 "Дата совершения сделки (цифрами день, месяц, год)"» всегда расчитывавется одинакого
        if (matrixRow.dealDoneDate != null && (row.dealDoneDate == null || matrixRow.dealDoneDate > row.dealDoneDate)) {
            row.dealDoneDate = matrixRow.dealDoneDate
        }

        // Графа 42
        switch (typeMap.get(matrixRow)) {
            case 812: // 6.3 (1)
            case 814: // 6.5 (2)
            case 819: // 6.12 (6)
            case 817: // 6.9 (10)
            case 816: // 6.1 (12)
            case 804: // 6.2 (14)
            case 837: // 6.15 (18)
            case 838: // 6.18 (19)
            case 836: // 6.25 (26)
                row.count = row.count + (srcRow.count ?: 0)
                break
            case 827: // 6.11 (9)
                row.count = row.count + (srcRow.bondCount ?: 0)
                break
            default:
                row.count = 1
                break
        }

        // Графа 44 п. 140 "Итого стоимость без учета НДС, акцизов и пошлины, руб."
        switch (typeMap.get(matrixRow)) {
            case 812: // 6.3 (1)
            case 814: // 6.5 (2)
            case 815: // 6.8 (3)
            case 805: // 6.7 (4)
            case 826: // 6.13 (5)
            case 819: // 6.12 (6)
            case 813: // 6.4 (7)
            case 816: // 6.1 (12)
            case 835: // 6.14 (17)
            case 828: // 6.19 (20)
            case 832: // 6.23 (21)
            case 833: // 6.24 (22)
                row.total = row.total + (srcRow.cost ?: 0)
                break
            case 806: // 6.6 (8)
                if (srcRow.incomeSum != null)
                    row.total = row.total + srcRow.incomeSum
                else if (srcRow.outcomeSum != null)
                    row.total = row.total + srcRow.outcomeSum
                break
            case 827: // 6.11 (9)
                row.total = row.total + (srcRow.transactionSumRub ?: 0)
                break
            case 817: // 6.9 (10)
            case 836: // 6.25 (26)
                row.total = row.total + (srcRow.totalCost ?: 0)
                break
            case 825: // 6.10.2 (11)
            case 823: // 6.10.1 (13)
            case 804: // 6.2 (14)
            case 811: // 6.17 (15)
            case 839: // 6.16 (16)
            case 838: // 6.18 (19)
            case 831: // 6.20 (23)
            case 834: // 6.22 (24)
            case 830: // 6.21 (25)
                row.total = row.total + (srcRow.total ?: 0) // cost // TODO (Ramil Timerbaev)
                break
            case 837: // 6.15 (18)
                row.total = row.total + (srcRow.totalNds ?: 0)
                break
        }

        switch (typeMap.get(matrixRow)) {
            case 812: // 6.3 (1)
            case 806: // 6.6 (8)
            case 811: // 6.17 (15)
                row.income = row.income + matrixRow.income
                row.outcome = row.outcome + matrixRow.outcome
                break
            case 814: // 6.5 (2)
            case 815: // 6.8 (3)
            case 826: // 6.13 (5)
            case 828: // 6.19 (20)
            case 833: // 6.24 (22)
            case 831: // 6.20 (23)
            case 834: // 6.22 (24)
            case 830: // 6.21 (25)
            case 836: // 6.25 (26)
                row.outcome = row.outcome + matrixRow.outcome
                break
            case 805: // 6.7 (4)
            case 813: // 6.4 (7)
            case 817: // 6.9 (10)
            case 825: // 6.10.2 (11)
            case 816: // 6.1 (12)
            case 823: // 6.10.1 (13)
            case 804: // 6.2 (14)
            case 832: // 6.23 (21)
                row.income = row.income + matrixRow.income
                break
            case 819: // 6.12 (6)
                if ((srcRow.incomeSum ?: 0) == 0 && (srcRow.outcomeSum ?: 0) > 0) {
                    row.outcome = row.outcome + srcRow.outcomeSum
                } else if ((srcRow.incomeSum ?: 0) > 0 && (srcRow.outcomeSum ?: 0) == 0) {
                    row.income = row.income + srcRow.cost
                }
                break
            case 827: // 6.11 (9)
                if (getRecBuyId().equals(srcRow.transactionType)) {
                    row.outcome = row.outcome + srcRow.transactionSumRub
                } else if (getRecSellId().equals(srcRow.transactionType)) {
                    row.income = row.income + srcRow.transactionSumRub
                }
                break
            case 839: // 6.16 (16)
                def String dealName = 'Срочные поставочные конверсионные сделки (сделки с отсрочкой исполнения) - '
                if (totalSum >= 0) {
                    row.income = (row.income ?: 0) + (srcRow.price ?: 0)
                    row.dealSubjectName = dealName + 'доход'
                } else {
                    row.outcome = (row.outcome ?: 0) + (srcRow.price ?: 0)
                    row.dealSubjectName = dealName + 'расход'
                }
                break
            case 835: // 6.14 (17)
                def String dealName = 'Беспоставочные (расчетные) срочные сделки - '
                if (totalSum >= 0) {
                    row.income = (row.income ?: 0) + (srcRow.price ?: 0)
                    row.dealSubjectName = dealName + 'доходные'
                } else {
                    row.outcome = (row.outcome ?: 0) + (srcRow.price ?: 0)
                    row.dealSubjectName = dealName + 'расходные'
                }
                break
            case 837: // 6.15 (18)
                def String dealName = 'Срочные поставочные сделки купли-продажи драгоценных металлов (сделки с ' +
                        'отсрочкой исполнения), ' + (getRecRUSId().equals(srcRow.unitCountryCode) ? "покупка, " : "продажа, ")
                if (totalSum >= 0) {
                    row.income = (row.income ?: 0) + (srcRow.priceOne ?: 0)
                    row.dealSubjectName = dealName + 'доход'
                } else {
                    row.outcome = (row.outcome ?: 0) + (srcRow.priceOne ?: 0)
                    row.dealSubjectName = dealName + 'расход'
                }
                break
            case 838: // 6.18 (19)
                def boolean dealBuy = getRecDealBuyId().equals(srcRow.dealFocus)
                def String dealName = 'Кассовые сделки ' + (dealBuy ? "покупки " : "продажи ") + ' драгоценных металлов - '
                if (totalSum >= 0) {
                    row.income = (row.income ?: 0) + (srcRow.incomeSum ?: 0)
                    row.dealSubjectName = dealName + 'доходные'
                } else {
                    row.outcome = (row.outcome ?: 0) + (srcRow.outcomeSum ?: 0)
                    row.dealSubjectName = dealName + 'расходные'
                }
                break
        }
    }

    // п. 130 "Цена (тариф) за единицу измерения без учета НДС, акцизов и пошлины, руб."
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

    // Графа 18
    switch (formTypeId) {
        case 812: // 6.3 (1)
            row.income = srcRow.incomeBankSum
            break
        case 805: // 6.7 (4)
        case 825: // 6.10.2 (11)
        case 823: // 6.10.1 (13)
        case 804: // 6.2 (14)
            row.income = srcRow.sum
            break
        case 819: // 6.12 (6)
        case 816: // 6.1 (12)
            row.income = srcRow.cost
            break
        case 813: // 6.4 (7)
        case 832: // 6.23 (21)
            row.income = srcRow.bankIncomeSum
            break
        case 806: // 6.6 (8)
            row.income = srcRow.incomeSum
            break
        case 827: // 6.11 (9)
            row.income = srcRow.transactionSumRub
            break
        case 817: // 6.9 (10)
            row.income = srcRow.totalCost
            break
        case 811: // 6.17 (15)
            row.income = srcRow.income
            break
        case 839: // 6.16 (16)
            row.income = srcRow.price
            break
        case 835: // 6.14 (17)
            row.income = srcRow.price
            break
        case 837: // 6.15 (18)
            row.income = srcRow.priceOne
            break
        case 838: // 6.18 (19)
            row.income = srcRow.outcomeSum
            break
    }
    if (row.income == null) {
        row.income = 0
    }

    // Графа 20
    switch (formTypeId) {
        case 812: // 6.3 (1)
            row.outcome = srcRow.outcomeBankSum
            break
        case 814: // 6.5 (2)
            row.outcome = srcRow.bankSum
            break
        case 815: // 6.8 (3)
            row.outcome = srcRow.expensesSum
            break
        case 826: // 6.13 (5)
        case 828: // 6.19 (20)
        case 831: // 6.20 (23)
        case 834: // 6.22 (24)
            row.outcome = srcRow.sum
            break
        case 819: // 6.12 (6)
        case 811: // 6.17 (15)
            row.outcome = srcRow.outcome
            break
        case 830: // 6.21 (25)
            row.outcome = srcRow.sum // outcomeSum // TODO (Ramil Timerbaev)
            break
        case 806: // 6.6 (8)
            row.outcome = srcRow.outcomeSum
            break
        case 827: // 6.11 (9)
            row.outcome = srcRow.transactionSumRub
            break
        case 839: // 6.16 (16)
        case 835: // 6.14 (17)
        case 836: // 6.25 (26)
            row.outcome = srcRow.price
            break
        case 837: // 6.15 (18)
            row.outcome = srcRow.priceOne
            break
        case 838: // 6.18 (19)
            row.outcome = srcRow.incomeSum
            break
        case 833: // 6.24 (22)
            row.outcome = srcRow.bankIncomeSum
            break
    }
    if (row.outcome == null) {
        row.outcome = 0
    }

    // Графа 30
    switch (formTypeId) {
        case 812: // 6.3 (1)
        case 814: // 6.5 (2)
        case 813: // 6.4 (7)
        case 806: // 6.6 (8)
        case 827: // 6.11 (9)
        case 817: // 6.9 (10)
        case 835: // 6.14 (17)
        case 837: // 6.15 (18)
        case 832: // 6.23 (21)
        case 833: // 6.24 (22)
        case 836: // 6.25 (26)
            row.contractDate = srcRow.contractDate
            break
        case 815: // 6.8 (3)
        case 805: // 6.7 (4)
        case 826: // 6.13 (5)
        case 819: // 6.12 (6)
        case 825: // 6.10.2 (11)
        case 816: // 6.1 (12)
        case 823: // 6.10.1 (13)
        case 804: // 6.2 (14)
        case 811: // 6.17 (15)
        case 839: // 6.16 (16)
        case 838: // 6.18 (19)
        case 828: // 6.19 (20)
        case 831: // 6.20 (23)
        case 834: // 6.22 (24)
        case 830: // 6.21 (25)
            row.contractDate = srcRow.docDate
            break
    }

    switch (formTypeId) {
        case 812: // 6.3 (1)
        case 814: // 6.5 (2)
        case 813: // 6.4 (7)
        case 806: // 6.6 (8)
        case 827: // 6.11 (9)
        case 817: // 6.9 (10)
        case 835: // 6.14 (17)
        case 837: // 6.15 (18)
        case 832: // 6.23 (21)
        case 833: // 6.24 (22)
        case 836: // 6.25 (26)
            row.contractNum = srcRow.contractNum
            break
        case 815: // 6.8 (3)
        case 805: // 6.7 (4)
        case 826: // 6.13 (5)
        case 819: // 6.12 (6)
        case 825: // 6.10.2 (11)
        case 816: // 6.1 (12)
        case 823: // 6.10.1 (13)
        case 804: // 6.2 (14)
        case 839: // 6.16 (16)
        case 838: // 6.18 (19)
        case 828: // 6.19 (20)
        case 834: // 6.22 (24)
        case 830: // 6.21 (25)
            row.contractNum = srcRow.docNumber
            break
        case 811: // 6.17 (15)
        case 831: // 6.20 (23)
            row.contractNum = srcRow.docNum
            break
    }

    // Графа 45
    switch (formTypeId) {
        case 812: // 6.3 (1)
        case 814: // 6.5 (2)
        case 813: // 6.4 (7)
        case 806: // 6.6 (8)
        case 817: // 6.9 (10)
        case 835: // 6.14 (17)
        case 837: // 6.15 (18)
        case 832: // 6.23 (21)
        case 833: // 6.24 (22)
        case 836: // 6.25 (26)
            row.dealDoneDate = srcRow.transactionDate
            break
        case 815: // 6.8 (3)
        case 805: // 6.7 (4)
        case 819: // 6.12 (6)
        case 816: // 6.1 (12)
        case 828: // 6.19 (20)
            row.dealDoneDate = srcRow.dealDate
            break
        case 826: // 6.13 (5)
            row.dealDoneDate = srcRow.date
            break
        case 825: // 6.10.2 (11)
        case 823: // 6.10.1 (13)
        case 804: // 6.2 (14)
        case 811: // 6.17 (15)
        case 839: // 6.16 (16)
        case 838: // 6.18 (19)
        case 831: // 6.20 (23)
        case 834: // 6.22 (24)
        case 830: // 6.21 (25)
            row.dealDoneDate = srcRow.dealDoneDate
            break
        case 827: // 6.11 (9)
            row.dealDoneDate = srcRow.transactionDeliveryDate
            break
    }

    // Графа 50
    switch (formTypeId) {
        case 812: // 6.3 (1)
        case 814: // 6.5 (2)
        case 813: // 6.4 (7)
        case 806: // 6.6 (8)
        case 832: // 6.23 (21)
        case 833: // 6.24 (22)
            row.organName = srcRow.jurName
            break
        case 815: // 6.8 (3)
        case 805: // 6.7 (4)
        case 826: // 6.13 (5)
        case 819: // 6.12 (6)
        case 816: // 6.1 (12)
        case 828: // 6.19 (20)
        case 831: // 6.20 (23)
            row.organName = srcRow.fullNamePerson
            break
        case 827: // 6.11 (9)
            row.organName = srcRow.contraName
            break
        case 817: // 6.9 (10)
        case 835: // 6.14 (17)
        case 837: // 6.15 (18)
        case 836: // 6.25 (26)
            row.organName = srcRow.name
            break
        case 825: // 6.10.2 (11)
        case 823: // 6.10.1 (13)
        case 804: // 6.2 (14)
        case 811: // 6.17 (15)
        case 839: // 6.16 (16)
        case 838: // 6.18 (19)
        case 834: // 6.22 (24)
        case 830: // 6.21 (25)
            row.organName = srcRow.fullName
            break
    }
    return row
}

// Проставляет статические строки
void addAllStatic(def dataRows) {
    def temp = []
    if (logger.containsLevel(LogLevel.ERROR)) {
        return
    }
    def prevOrganName = null
    def boolean firstRow = true
    for (int i = 0; i < dataRows.size(); i++) {
        def row = dataRows.get(i)
        if (firstRow || row.organName != prevOrganName) {
            firstRow = false
            prevOrganName = row.organName
            def newRow = formData.createDataRow()
            newRow.getCell('name').colSpan = 56
            ['name', 'dealNum1', 'interdependenceSing', 'f121', 'f122', 'f123', 'f124',
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
            if (row.organName != null) {
                newRow.name = getRefBookValue(9, row.organName).NAME.stringValue
            }
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
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
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
            ([(headerRows[2][31]): 'п. 080 Место отправки (погрузки) товара в соответствии с товаросопроводительными документами (заполняется только для товаров)']),
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
            ([(headerRows[3][31]): getColumnName(tmpRow, 'countryCode1')]),
            ([(headerRows[3][32]): getColumnName(tmpRow, 'region1')]),
            ([(headerRows[3][33]): getColumnName(tmpRow, 'city1')]),
            ([(headerRows[3][34]): getColumnName(tmpRow, 'locality1')]),
            ([(headerRows[3][35]): getColumnName(tmpRow, 'countryCode2')]),
            ([(headerRows[3][36]): getColumnName(tmpRow, 'region2')]),
            ([(headerRows[3][37]): getColumnName(tmpRow, 'city2')]),
            ([(headerRows[3][38]): getColumnName(tmpRow, 'locality2')])
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

    // 2. п. 100
    def colIndex = 1
    newRow.interdependenceSing = getRecordIdImport(69, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    // 3. п. 121
    colIndex++
    newRow.f121 = getYesNoByNumber(parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true))
    // 4. п. 122
    colIndex++
    newRow.f122 = getYesNoByNumber(parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true))
    // 5. п. 123
    colIndex++
    newRow.f123 = getYesNoByNumber(parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true))
    // 6. п. 124
    colIndex++
    newRow.f124 = getYesNoByNumber(parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true))
    // 7. п. 131
    colIndex++
    newRow.f131 = getYesNoByNumber(parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true))
    // 8. п. 132
    colIndex++
    newRow.f132 = getYesNoByNumber(parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true))
    // 9. п. 133
    colIndex++
    newRow.f133 = getYesNoByNumber(parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true))
    // 10. п. 134
    colIndex++
    newRow.f134 = getYesNoByNumber(parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true))
    // 11. п. 135 (до 2014 г. / после 2014 г.)
    colIndex++
    newRow.f135 = getYesNoByNumber(parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true))
    // 12. п. 200 "Группа однородных сделок"
    colIndex++
    newRow.similarDealGroup = getYesNoByNumber(parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true))
    // 13. п. 210 "Код наименования сделки"
    colIndex++
    newRow.dealNameCode = getRecordIdImport(67, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    // 14. п. 211 "Код стороны сделки, которой является налогоплательщик"
    colIndex++
    newRow.taxpayerSideCode = getRecordIdImport(65, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    // 15. п. 220 "Признак определения цены сделки с учетом особенностей, предусмотренных статьей 105.4 НК РФ (регулируемые цены)"
    colIndex++
    newRow.dealPriceSign = getYesNoByNumber(parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true))
    // 16. п. 230 "Код определения цены сделки"
    colIndex++
    newRow.dealPriceCode = getRecordIdImport(66, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    // 17. п. 260 "Количество участников сделки"
    colIndex++
    newRow.dealMemberCount = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // 18. п. 300 "Сумма доходов налогоплательщика по контролируемой сделке (группе однородных сделок) в рублях"
    colIndex++
    newRow.income = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // 19. п. 301 "в том числе сумма доходов по сделкам, цены которых подлежат регулированию"
    colIndex++
    newRow.incomeIncludingRegulation = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // 20. п. 310 "Сумма расходов налогоплательщика по контролируемой сделке (группе однородных сделок) в рублях"
    colIndex++
    newRow.outcome = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // 21.	п. 311 "в том числе сумма расходов по сделкам, цены которых подлежат регулированию"
    colIndex++
    newRow.outcomeIncludingRegulation = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // 22.
    colIndex++
    // 23. п. 020 "Тип предмета сделки"
    colIndex++
    newRow.dealType = getRecordIdImport(64, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    // 24. п. 030 "Наименование предмета сделки"
    colIndex++
    newRow.dealSubjectName = values[colIndex]
    // 25. п. 040 "Код предмета сделки (код по ТН ВЭД)"
    colIndex++
    newRow.dealSubjectCode1 = getRecordIdImport(73, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    // 26. п. 043 "Код предмета сделки (код по ОКП)"
    colIndex++
    newRow.dealSubjectCode2 = getRecordIdImport(68, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    // 27.	п. 045 "Код предмета сделки (код по ОКВЭД)"
    colIndex++
    newRow.dealSubjectCode3 = getRecordIdImport(34, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    // 28. п. 050 "Номер другого участника сделки"
    colIndex++
    newRow.otherNum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // 29. п. 060 "Номер договора"
    colIndex++
    newRow.contractNum = values[colIndex]
    // 30. п. 065 "Дата договора"
    colIndex++
    newRow.contractDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    // 31. п. 070 "Код страны происхождения предмета сделки по классификатору ОКСМ (цифровой)"
    colIndex++
    newRow.countryCode = getRecordIdImport(10, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    // 32. Код страны по классификатору ОКСМ (цифровой)
    colIndex++
    newRow.countryCode1 = getRecordIdImport(10, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    // 33. Регион (код)
    colIndex++
    newRow.region1 = getRecordIdImport(4, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    // 34. Город
    colIndex++
    newRow.city1 = values[colIndex]
    // 35. Населенный пункт (село, поселок и т.д.)
    colIndex++
    newRow.locality1 = values[colIndex]
    // 36. Код страны по классификатору ОКСМ (цифровой)
    colIndex++
    newRow.countryCode2 = getRecordIdImport(10, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    // 37. Регион (код)
    colIndex++
    newRow.region2 = getRecordIdImport(4, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    // 38. Город
    colIndex++
    newRow.city2 = values[colIndex]
    // 39. Населенный пункт (село, поселок и т.д.)
    colIndex++
    newRow.locality2 = values[colIndex]
    // 40. п. 100 "Код условия поставки (заполняется только для товаров)"
    colIndex++
    newRow.deliveryCode = getRecordIdImport(63, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    // 41. п. 110 "Код единицы измерения по ОКЕИ"
    colIndex++
    newRow.okeiCode = getRecordIdImport(12, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    // 42. п. 120 "Количество"
    colIndex++
    newRow.count = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // 43. п. 130 "Цена (тариф) за единицу измерения без учета НДС, акцизов и пошлины, руб."
    colIndex++
    newRow.price = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // 44. п. 140 "Итого стоимость без учета НДС, акцизов и пошлины, руб."
    colIndex++
    newRow.total = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // 45. п. 150 "Дата совершения сделки (цифрами день, месяц, год)"
    colIndex++
    newRow.dealDoneDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    // 46.
    colIndex++
    // 47. п. 015 "Порядковый номер участника сделки (из раздела 1Б)"
    colIndex++
    newRow.dealMemberNum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // 48
    colIndex++
    // 49. п. 030 "Код страны по классификатору ОКСМ"
    colIndex++
    newRow.countryCode3 = getRecordIdImport(10, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    // 50. п. 040 "Наименование организации"
    colIndex++
    newRow.organName = getRecordIdImport(9, 'NAME', values[colIndex], fileRowIndex, colIndex + colOffset)
    def map = getRefBookValue(9, newRow.organName)
    if (map != null) {
        // 48. п. 020 "Сведения об организации"
        colIndex = 47
        def map2 = getRefBookValue(70, map.ORGANIZATION?.referenceValue)
        formDataService.checkReferenceValue(9, values[colIndex], map2.VALUE?.stringValue, fileRowIndex, colIndex + colOffset, logger, false)

        // 51. п. 050 "ИНН организации"
        colIndex = 50
        def expectedValue = (map.INN_KIO?.stringValue != null ? map.INN_KIO?.stringValue : "")
        formDataService.checkReferenceValue(9, values[colIndex], expectedValue, fileRowIndex, colIndex + colOffset, logger, false)

        // 52. п. 060 "КПП организации"
        colIndex++
        expectedValue = (map.KPP?.stringValue != null ? map.KPP?.stringValue : "")
        formDataService.checkReferenceValue(9, values[colIndex], expectedValue, fileRowIndex, colIndex + colOffset, logger, false)

        // 53. п. 070 "Регистрационный номер организации в стране ее регистрации (инкорпорации)"
        colIndex++
        expectedValue = (map.REG_NUM?.stringValue != null ? map.REG_NUM?.stringValue : "")
        formDataService.checkReferenceValue(9, values[colIndex], expectedValue, fileRowIndex, colIndex + colOffset, logger, false)

        // 54. п. 080 "Код налогоплательщика в стране регистрации (инкорпорации) или его аналог (если имеется)"
        colIndex++
        expectedValue = (map.TAXPAYER_CODE?.stringValue != null ? map.TAXPAYER_CODE?.stringValue : "")
        formDataService.checkReferenceValue(9, values[colIndex], expectedValue, fileRowIndex, colIndex + colOffset, logger, false)

        // 55. п. 090 "Адрес"
        colIndex++
        formDataService.checkReferenceValue(9, values[colIndex], map.ADDRESS?.stringValue, fileRowIndex, colIndex + colOffset, logger, false)

        // Графа 53, 54, 55 - сменили тип для наглядности: что было видно какие данные попадут в уведомление
        // 53. п. 070 "Регистрационный номер организации в стране ее регистрации (инкорпорации)"
        newRow.organRegNum = map.REG_NUM?.stringValue

        // 54. п. 080 "Код налогоплательщика в стране регистрации (инкорпорации) или его аналог (если имеется)"
        newRow.taxpayerCode = map.TAXPAYER_CODE?.stringValue

        // 55. п. 090 "Адрес"
        newRow.address = map.ADDRESS?.stringValue
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