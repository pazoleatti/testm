import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * 409 — Сводный отчет
 * formTemplateId = 409
 */
// 1.	dealNum1	п. 010 "Порядковый номер сделки по уведомлению"
// 2.	interdependenceSing	п. 100
// 3.	f121	п. 121
// 4.	f122	п. 122
// 5.	f123	п. 123
// 6.	f124	п. 124
// 7.	f131	п. 131
// 8.	f132	п. 132
// 9.	f133	п. 133
// 10.	f134	п. 134
// 11.	f135	п. 135 (до 2014 г. / после 2014 г.)
// 12.	similarDealGroup	п. 200 "Группа однородных сделок"
// 13.	dealNameCode	п. 210 "Код наименования сделки"
// 14.	taxpayerSideCode	п. 211 "Код стороны сделки, которой является налогоплательщик"
// 15.	dealPriceSign	п. 220 "Признак определения цены сделки с учетом особенностей, предусмотренных статьей 105.4 НК РФ (регулируемые цены)"
// 16.	dealPriceCode	п. 230 "Код определения цены сделки"
// 17.	dealMemberCount	п. 260 "Количество участников сделки"
// 18.	income	п. 300 "Сумма доходов налогоплательщика по контролируемой сделке (группе однородных сделок) в рублях"
// 19.	incomeIncludingRegulation	п. 301 "в том числе сумма доходов по сделкам, цены которых подлежат регулированию"
// 20.	outcome	п. 310 "Сумма расходов налогоплательщика по контролируемой сделке (группе однородных сделок) в рублях"
// 21.	outcomeIncludingRegulation	п. 311 "в том числе сумма расходов по сделкам, цены которых подлежат регулированию"
// 22.	dealNum2	п. 010 "Порядковый номер сделки по уведомлению (из раздела 1А)"
// 23.	dealType	п. 020 "Тип предмета сделки"
// 24.	dealSubjectName	п. 030 "Наименование предмета сделки"
// 25.	dealSubjectCode1	п. 040 "Код предмета сделки (код по ТН ВЭД)"
// 26.	dealSubjectCode2	п. 043 "Код предмета сделки (код по ОКП)"
// 27.	dealSubjectCode3	п. 045 "Код предмета сделки (код по ОКВЭД)"
// 28.	otherNum	п. 050 "Номер другого участника сделки"
// 29.	contractNum	п. 060 "Номер договора"
// 30.	contractDate	п. 065 "Дата договора"
// 31.	countryCode	п. 070 "Код страны происхождения предмета сделки по классификатору ОКСМ (цифровой)"
// 32.	countryCode1	Код страны по классификатору ОКСМ (цифровой)
// 33.	region1	Регион (код)
// 34.	city1	Город
// 35.	locality1	Населенный пункт (село, поселок и т.д.)
// 36.	countryCode2	Код страны по классификатору ОКСМ (цифровой)
// 37.	region2	Регион (код)
// 38.	city2	Город
// 39.	locality2	Населенный пункт (село, поселок и т.д.)
// 40.	deliveryCode	п. 100 "Код условия поставки (заполняется только для товаров)"
// 41.	okeiCode	п. 110 "Код единицы измерения по ОКЕИ"
// 42.	count	п. 120 "Количество"
// 43.	price	п. 130 "Цена (тариф) за единицу измерения без учета НДС, акцизов и пошлины, руб."
// 44.	total	п. 140 "Итого стоимость без учета НДС, акцизов и пошлины, руб."
// 45.	dealDoneDate	п. 150 "Дата совершения сделки (цифрами день, месяц, год)"
// 46.	dealNum3	п. 010 "Порядковый номер сделки (из раздела 1А)"
// 47.	dealMemberNum	п. 015 "Порядковый номер участника сделки (из раздела 1Б)"
// 48.	organInfo	п. 020 "Сведения об организации"
// 49.	countryCode3	п. 030 "Код страны по классификатору ОКСМ"
// 50.	organName	п. 040 "Наименование организации"
// 51.	organINN	п. 050 "ИНН организации"
// 52.	organKPP	п. 060 "КПП организации"
// 53.	organRegNum	п. 070 "Регистрационный номер организации в стране ее регистрации (инкорпорации)"
// 54.	taxpayerCode	п. 080 "Код налогоплательщика в стране регистрации (инкорпорации) или его аналог (если имеется)"
// 55.	address	п. 090 "Адрес"

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkCreation()
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        // В ручном режиме строки добавлять нельзя
        logger.warn("Добавление строк запрещено!")
        break
    case FormDataEvent.DELETE_ROW:
        // В ручном режиме строки удалять нельзя
        logger.warn("Удаление строк запрещено!")
        break
// После принятия из Утверждено
    case FormDataEvent.AFTER_MOVE_CREATED_TO_ACCEPTED:
        logicCheck()
        break
// После принятия из Подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED:
        logicCheck()
        break
// Консолидация
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        logicCheck()
        if (!logger.containsLevel(LogLevel.ERROR)) {
            logger.info('Формирование сводной формы прошло успешно.')
        }
}

// Кэш провайдеров
@Field
def providerCache = [:]
// Кэш id записей справочника
@Field
def recordCache = [:]
// Кэш значений справочника
@Field
def refBookCache = [:]

// Дата окончания отчетного периода
@Field
def endDate = null

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

// Поиск записи в справочнике по значению (для расчетов)
def getRecordId(def Long refBookId, def String alias, def String value) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), -1, null, logger, true)
}

// Проверка при создании формы
void checkCreation() {
    def findForm = formDataService.find(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId)
    if (findForm != null) {
        logger.error('Формирование содного отчета невозможно, т.к. отчет с указанными параметрами уже сформирован!')
    }
}

// Логические проверки
void logicCheck() {
    // TODO В ТЗ нет
}

// Расчеты. Алгоритмы заполнения полей формы.
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def int index = 1
    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        // Порядковый номер строки
        row.dealNum1 = index
        row.dealNum2 = index
        row.dealNum3 = index
        index++
    }
    dataRowHelper.save(dataRows)
}

// Консолидация
void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def matrixRows = []
    def rowsMap = [:]
    // счетчик по группам для табл. 86
    def int i = 1
    // мапа идентификаторов для группировки
    def groupStr = [:]
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind).each {
        def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (source != null && source.state == WorkflowState.ACCEPTED && source.formType.taxType == TaxType.DEAL) {
            formDataService.getDataRowHelper(source).allCached.each { srcRow ->
                if (srcRow.getAlias() == null) {
                    def matrixRow = buildRow(srcRow, source.formType)

                    // идентификатор для группировки
                    String group = getGroupId(matrixRow, srcRow)
                    if (!groupStr.containsKey(group)) {
                        groupStr.put(group, i++)
                    }
                    matrixRow.dealNum3 = groupStr.get(group)

                    matrixRows.add(matrixRow)
                    rowsMap.put(matrixRow, srcRow)
                }
            }
        }
    }

    // "1" для графы 8 отчета 3
    //def Long serviceType1 = getRecordId(11, 'CODE', '1')

    // итоговые строки (всё то, что в итоге попадет в сводный отчет)
    def summaryRows = []

    // Сортировка по row.dealNum3
    sortRows(matrixRows, ['dealNum3'])

    def Long currentGroup
    def mapForSummary = [:]

    matrixRows.each { matrixRow ->

        if (matrixRow.dealNum3.equals(4)) { // "класс 4" копируем построчно
            mapForSummary.put(matrixRow, rowsMap.get(matrixRow))
            summaryRows.add(getRow(mapForSummary))
            mapForSummary.clear()

        } else { // 1, 2, 3 класс
            if (currentGroup == null) { // первая строка
                currentGroup = matrixRow.dealNum3
                mapForSummary.put(matrixRow, rowsMap.get(matrixRow))
            } else if (currentGroup.equals(matrixRow.dealNum3.longValue())) { // строка из той же группы что предыдущая
                mapForSummary.put(matrixRow, rowsMap.get(matrixRow))
            } else { // строка из новой группы
                // получаем итоговую строку для предыдущей группы
                summaryRows.add(getRow(mapForSummary))

                currentGroup = matrixRow.dealNum3
                mapForSummary.clear()
                mapForSummary.put(matrixRow, rowsMap.get(matrixRow))
            }
        }
    }

    if (mapForSummary.size() > 0) {
        summaryRows.add(getRow(mapForSummary))
    }

    dataRowHelper.save(summaryRows)
}

// Подготовка строки сводного отчета из первичных и консолидированных отчетов модуля МУКС
def buildRow(def srcRow, def type) {
    def row = formData.createDataRow()
    // Временный алиас строки
    row.setAlias("group_$type.id")

    // тип отчета
    row.dealNum1 = type.id
    // класс отчет
    row.dealNum2 = getReportClass(type.id)

    // TODO Формирование строк (1в1 из матрицы)

    return row
}

def Long getReportClass(def formTypeId) {
    switch (formTypeId) {
        case 376:
        case 377:
        case 380:
        case 381:
        case 382:
        case 385:
        case 397:
        case 399:
            return 1
        case 375:
            // TODO ?
            return 3
        case 379:
        case 387:
        case 398:
            return 4
        default:
            // TODO чем отличается от 1?
            return 2
    }
}

def String getGroupId(def row, def srcRow) {
    def StringBuilder group = new StringBuilder()
    group.append(row.dealNum1).append("#")
            .append(row.organName).append("#")
            .append(row.contractDate).append("#")
            .append(row.contractNum).append("#")
    switch (row.dealNum1) {
        case 390:
            group.append(srcRow.currencyCode)
            break
        case 391:
            group.append(srcRow.currencyCode).append("#")
            group.append(srcRow.dealType)
            break
        case 392:
            group.append(srcRow.transactionType).append("#")
            break
        case 393:
            group.append(srcRow.innerCode).append("#")
            group.append(srcRow.dealType)
            break
        case 394:
            group.append(srcRow.metalName).append("#")
            group.append(srcRow.dealFocus)
            break
    }
    return group.toString()
}

def getRow(def map) {

    // для отчетов 16..19 надо считать суммы по двум столбцам
    def totalSum = 0
    map.each { matrixRow, srcRow ->
        switch (matrixRow.dealNum1) {
            case 391:
            case 394:
                // 16 и 19
                totalSum = (srcRow.incomeSum ?: 0) - (srcRow.outcomeSum ?: 0)
                break
            case 392:
            case 393:
                // 17 и 18
                totalSum = (srcRow.incomeSum ?: 0) - (srcRow.consumptionSum ?: 0)
                break
        }
    }

    def row = formData.createDataRow()
    def boolean first = true
    map.each { matrixRow, srcRow ->

        if (first) {
            first = false
            // Копируем значения из матрицы по всем графам
            row.setAlias(matrixRow.getAlias())
            row.each { alias, value ->
                row.getCell(alias).setValue(matrixRow.getCell(alias).value, null)
            }
            // обнуление сумм, которые расчитываем далее
            row.income = null
            row.outcome = null

            // TODO перенести общие для группы графы сюда под свой свитч?
            // (row.dealNameCode, row.taxpayerSideCode, row.dealSubjectName, row.similarDealGroup)
        }

        // Атрибут «п. 150 "Дата совершения сделки (цифрами день, месяц, год)"» всегда расчитывавется одинакого
        if (matrixRow.dealDoneDate != null && (row.dealDoneDate == null || matrixRow.dealDoneDate > row.dealDoneDate)) {
            row.dealDoneDate = matrixRow.dealDoneDate
        }

        // TODO подумать о суммах (income, outcome) и дате (dealDoneDate) для "класс 4" - зачем расчеты, если по одной строке всегда?
        switch (matrixRow.dealNum1) {
            case 376:
                // 1
                if (srcRow.incomeBankSum != null) {
                    row.income = (row.income ?: 0) + srcRow.incomeBankSum
                    row.taxpayerSideCode = getRecordId(65, 'CODE', "004")
                    row.dealSubjectName = 'Предоставление помещений в аренду (субаренду)'
                } else if (srcRow.outcomeBankSum != null) {
                    row.outcome = (row.outcome ?: 0) + srcRow.outcomeBankSum
                    row.taxpayerSideCode = getRecordId(65, 'CODE', "003")
                    row.dealSubjectName = 'Получение помещений в аренду (субаренду)'
                }
                row.dealNameCode = getRecordId(67, 'CODE', "002")

                row.similarDealGroup = getRecNoId()
                break
            case 377:
                // 2
                row.outcome = (row.outcome ?: 0) + (srcRow.bankSum ?: 0)
                row.dealNameCode = getRecordId(67, 'CODE', "019")
                row.taxpayerSideCode = getRecordId(65, 'CODE', "012")
                row.dealSubjectName = 'Услуги, связанные с обслуживанием недвижимости'
                row.similarDealGroup = getRecNoId()
                break
            case 375:
                // 3
                row.outcome = (row.outcome ?: 0) + (srcRow.expensesSum ?: 0)
                row.dealNameCode = getRecordId(67, 'CODE', "019")
                row.taxpayerSideCode = getRecordId(65, 'CODE', "012")
                row.dealSubjectName = 'Услуги по разработке, внедрению и модификации программного обеспечения'
                row.similarDealGroup = getRecNoId()
                break
            case 379:
                // 4
                row.income = (row.income ?: 0) + (srcRow.sum ?: 0)
                row.dealNameCode = getRecordId(67, 'CODE', "016")
                row.taxpayerSideCode = getRecordId(65, 'CODE', "028")
                row.dealSubjectName = 'Услуги по предоставлению права пользования товарным знаком'
                row.similarDealGroup = getRecNoId()
                break
            case 380:
                // 5
                row.outcome = (row.outcome ?: 0) + (srcRow.sum ?: 0)
                row.dealNameCode = getRecordId(67, 'CODE', "019")
                row.taxpayerSideCode = getRecordId(65, 'CODE', "012")
                row.dealSubjectName = 'Приобретение услуг, связанных с организацией и проведением торгов по реализации имущества'
                row.similarDealGroup = getRecNoId()
                break
            case 381:
                // 6
                def String out = (getRecRPCId().equals(srcRow.dealSign) ? "" : "вне")
                if ((srcRow.incomeSum ?: 0) == 0 && srcRow.outcomeSum > 0) {
                    row.outcome = (row.outcome ?: 0) + (srcRow.outcomeSum ?: 0)
                    row.taxpayerSideCode = getRecordId(65, 'CODE', "026")
                    row.dealSubjectName = "Покупка акций и долей - " + out + "биржевые сделки"
                } else if (srcRow.incomeSum > 0 && (srcRow.outcomeSum ?: 0) == 0) {
                    row.income = (row.income ?: 0) + (srcRow.cost ?: 0)
                    row.taxpayerSideCode = getRecordId(65, 'CODE', "027")
                    row.dealSubjectName = "Продажа акций и долей - " + out + "биржевые сделки"
                }
                row.dealNameCode = getRecordId(67, 'CODE', "015")
                row.similarDealGroup = getRecNoId()
                break
            case 382:
                // 7
                row.income = (row.income ?: 0) + (srcRow.bankIncomeSum ?: 0)
                row.dealNameCode = getRecordId(67, 'CODE', "019")
                row.taxpayerSideCode = getRecordId(65, 'CODE', "011")
                row.dealSubjectName = 'Оказание банковских услуг'
                row.similarDealGroup = getRecNoId()
                break
            case 383:
                // 8
                def String out = (getRecDealsModeId().equals(srcRow.dealsMode) ? "" : "вне")
                if (srcRow.percentIncomeSum != null) {
                    row.income = (row.income ?: 0) + (srcRow.percentIncomeSum ?: 0)
                    row.dealSubjectName = "Операции РЕПО - " + out + "биржевые (процентный доход)"
                } else if (srcRow.percentConsumptionSum != null) {
                    row.outcome = (row.outcome ?: 0) + (srcRow.percentConsumptionSum ?: 0)
                    row.dealSubjectName = "Операции РЕПО - " + out + "биржевые (процентный расход)"
                }
                row.dealNameCode = getRecordId(67, 'CODE', "032")
                row.taxpayerSideCode = getRecordId(65, 'CODE', "052")
                row.similarDealGroup = getRecYesId()
                break
            case 384:
                // 9
                def String out = (getRecDealsModeId().equals(srcRow.transactionMode) ? "" : "вне")
                if (getRecBuyId().equals(srcRow.transactionType)) {
                    row.outcome = (row.outcome ?: 0) + (srcRow.transactionSumRub ?: 0)
                    row.taxpayerSideCode = getRecordId(65, 'CODE', "026")
                    row.dealSubjectName = "Покупка ЦБ - " + out + "биржевые сделки"
                } else if (getRecSellId().equals(srcRow.transactionType)) {
                    row.income = (row.income ?: 0) + (srcRow.transactionSumRub ?: 0)
                    row.taxpayerSideCode = getRecordId(65, 'CODE', "027")
                    row.dealSubjectName = "Продажа ЦБ - " + out + "биржевые сделки"
                }
                row.dealNameCode = getRecordId(67, 'CODE', "015")
                row.similarDealGroup = getRecYesId()
                break
            case 385:
                // 10
                row.income = (row.income ?: 0) + (srcRow.totalCost ?: 0)
                row.dealNameCode = getRecordId(67, 'CODE', "029")
                row.taxpayerSideCode = getRecordId(65, 'CODE', "022")
                row.dealSubjectName = 'Уступка прав требования - с обязательной оценкой'
                row.similarDealGroup = getRecNoId()
                break
            case 386:
                // 11
                row.income = (row.income ?: 0) + (srcRow.sum ?: 0)
                row.dealNameCode = getRecordId(67, 'CODE', "003")
                row.taxpayerSideCode = getRecordId(65, 'CODE', "005")
                row.dealSubjectName = 'открытие аккредитивов и инструментов торгового финансирования'
                row.similarDealGroup = getRecYesId()
                break
            case 387:
                // 12
                row.income = (row.income ?: 0) + (srcRow.cost ?: 0)
                row.dealNameCode = getRecordId(67, 'CODE', "012")
                row.taxpayerSideCode = getRecordId(65, 'CODE', "022")
                row.dealSubjectName = 'Размещение денежных средств корпоративным клиентам - регулируемые сделки'
                row.similarDealGroup = getRecYesId()
                break
            case 388:
                // 13
                row.income = (row.income ?: 0) + (srcRow.sum ?: 0)
                row.dealNameCode = getRecordId(67, 'CODE', "003")
                row.taxpayerSideCode = getRecordId(65, 'CODE', "005")
                row.dealSubjectName = 'Выдача гарантий (открытие аккредитивов и инструментов торгового финансирования)'
                row.similarDealGroup = getRecYesId()
                break
            case 389:
                // 14
                row.income = (row.income ?: 0) + (srcRow.sum ?: 0)
                row.dealNameCode = getRecordId(67, 'CODE', "012")
                row.taxpayerSideCode = getRecordId(65, 'CODE', "022")
                row.dealSubjectName = 'Размещение денежных средств в межбанковские кредиты'
                row.similarDealGroup = getRecYesId()
                break
            case 390:
                // 15
                if (srcRow.incomeSum != null) {
                    row.income = (row.income ?: 0) + (srcRow.incomeSum ?: 0)
                    row.dealSubjectName = 'Кассовые конверсионные сделки - доходные'
                } else if (srcRow.outcomeSum != null) {
                    row.outcome = (row.outcome ?: 0) + (srcRow.outcomeSum ?: 0)
                    row.dealSubjectName = 'Кассовые конверсионные сделки - расходные'
                }
                row.dealNameCode = getRecordId(67, 'CODE', "017")
                row.taxpayerSideCode = getRecordId(65, 'CODE', "030")
                row.similarDealGroup = getRecYesId()
                break
            case 391:
                // 16
                //TODO income и outcome указаны как суммы по srcRow.price. Опечатка? делаю сразу по другим графам
                def String dealName = 'Срочные поставочные конверсионные сделки (сделки с отсрочкой исполнения) - '
                if (totalSum >= 0) {
                    row.outcome = (row.outcome ?: 0) + (srcRow.outcomeSum ?: 0)
                    row.dealSubjectName = dealName + 'доход'
                } else {
                    row.income = (row.income ?: 0) + (srcRow.incomeSum ?: 0)
                    row.dealSubjectName = dealName + 'расход'

                }
                row.dealNameCode = getRecordId(67, 'CODE', "032")
                row.taxpayerSideCode = getRecordId(65, 'CODE', "052")
                row.similarDealGroup = getRecYesId()
                break
        // TODO в 17..18 делаю как по ТЗ но есть вопрос ( income и outcome указаны как суммы по srcRow.price. Опечатка? делаю сразу по другим графам)
            case 392:
                // 17
                def String dealName = 'Беспоставочные (расчетные) срочные сделки - '
                if (totalSum >= 0) {
                    row.outcome = (row.outcome ?: 0) + (srcRow.price ?: 0)
                    row.dealSubjectName = dealName + 'доходные'
                } else {
                    row.income = (row.income ?: 0) + (srcRow.price ?: 0)
                    row.dealSubjectName = dealName + 'расходные'

                }
                row.dealNameCode = getRecordId(67, 'CODE', "032")
                row.taxpayerSideCode = getRecordId(65, 'CODE', "052")
                row.similarDealGroup = getRecYesId()
                break
            case 393:
                // 18
                def String dealName = 'Срочные поставочные сделки купли-продажи драгоценных металлов (сделки с ' +
                        'отсрочкой исполнения), ' + (getRecRUSId().equals(srcRow.unitCountryCode) ? "покупка, " : "продажа, ")
                if (totalSum >= 0) {
                    row.outcome = (row.outcome ?: 0) + (srcRow.priceOne ?: 0)
                    row.dealSubjectName = dealName + 'доход'
                } else {
                    row.income = (row.income ?: 0) + (srcRow.priceOne ?: 0)
                    row.dealSubjectName = dealName + 'расход'
                }
                row.dealNameCode = getRecordId(67, 'CODE', "015")
                row.taxpayerSideCode = getRecordId(65, 'CODE', "052")
                row.similarDealGroup = getRecYesId()
                break
            case 394:
                // 19
                // TODO для 300 и 310 явно что-то не то
                def boolean dealBuy = getRecDealBuyId().equals(srcRow.dealFocus)
                def String dealName = 'Кассовые сделки ' + (dealBuy ? "покупки " : "продажи ") + ' драгоценных металлов - '
                if (totalSum >= 0) {
                    //row.outcome = (row.outcome ?: 0) + (srcRow. ?: 0)
                    row.dealSubjectName = dealName + 'доходные'
                } else {
                    //row.income = (row.income ?: 0) + (srcRow. ?: 0)
                    row.dealSubjectName = dealName + 'расходные'
                }
                row.dealNameCode = getRecordId(67, 'CODE', "015")
                row.taxpayerSideCode = getRecordId(65, 'CODE', (dealBuy ? "026" : "027"))
                row.similarDealGroup = getRecYesId()
                break
            case 397:
                // 20
                row.outcome = (row.outcome ?: 0) + (srcRow.sum ?: 0)
                row.dealNameCode = getRecordId(67, 'CODE', "004")
                row.taxpayerSideCode = getRecordId(65, 'CODE', "007")
                row.dealSubjectName = 'Привлечение денежных средств'
                row.similarDealGroup = getRecYesId()
                break
            case 398:
                // 21
                row.income = (row.income ?: 0) + (srcRow.bankIncomeSum ?: 0)
                row.dealNameCode = getRecordId(67, 'CODE', "016")
                row.taxpayerSideCode = getRecordId(65, 'CODE', "028")
                // TODO причем тут ПО?
                row.dealSubjectName = 'Предоставление лицензий на программное обеспечение'
                row.similarDealGroup = getRecNoId()
                break
            case 399:
                // 22
                // TODO странное условие в таблице 87, наверно оно дб в таблице 86
                row.outcome = (row.outcome ?: 0) + (srcRow.bankIncomeSum ?: 0)
                row.dealNameCode = getRecordId(67, 'CODE', "019")
                row.taxpayerSideCode = getRecordId(65, 'CODE', "012")
                row.dealSubjectName = getRefBookValue(13, srcRow.serviceName)?.NAME?.stringValue
                row.similarDealGroup = getRecNoId()
                break
            case 402:
                // 23
                row.outcome = (row.outcome ?: 0) + (srcRow.sum ?: 0)
                row.dealNameCode = getRecordId(67, 'CODE', "012")
                row.taxpayerSideCode = getRecordId(65, 'CODE', "020")
                row.dealSubjectName = 'Привлечение денежных средств в межбанковские кредиты'
                row.similarDealGroup = getRecYesId()
                break
            case 401:
                // 24
                // TODO опечатка в тз?
                row.outcome = (row.outcome ?: 0) + (srcRow.sum ?: 0)
                row.dealNameCode = getRecordId(67, 'CODE', "003")
                row.taxpayerSideCode = getRecordId(65, 'CODE', "002")
                row.dealSubjectName = 'Привлечение гарантий (открытие аккредитивов и других инструментов торгового финансирования)'
                row.similarDealGroup = getRecYesId()
                break
            case 403:
                // 25
                row.outcome = (row.outcome ?: 0) + (srcRow.outcomeSum ?: 0)
                // TODO далее всё как в 24, опечатка?
                row.dealNameCode = getRecordId(67, 'CODE', "003")
                row.taxpayerSideCode = getRecordId(65, 'CODE', "002")
                row.dealSubjectName = 'Привлечение гарантий (открытие аккредитивов и других инструментов торгового финансирования)'
                row.similarDealGroup = getRecYesId()
                break
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

// "Направленности сделок" = «продажа»
@Field
def Long recDealSellId

def Long getRecDealSellId() {
    if (recDealSellId == null)
        recDealSellId = getRecordId(20, 'DIRECTION', 'продажа')
    return recDealSellId
}