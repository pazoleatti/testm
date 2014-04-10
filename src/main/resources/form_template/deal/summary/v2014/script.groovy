import com.aplana.sbrf.taxaccounting.model.DataRow
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
    // ТЗ нет
}

// Расчеты. Алгоритмы заполнения полей формы.
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // удаляем строки-группировки по организациям
    for (Iterator<DataRow> iter = dataRows.iterator() as Iterator<DataRow>; iter.hasNext();) {
        row = (DataRow) iter.next()
        if (row.getAlias() != null) {
            iter.remove()
            dataRowHelper.delete(row)
        }
    }
    // сортируем по организациям
    dataRows.sort { it.organName }

    def int index = 1
    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        // Порядковый номер строки
        row.dealNum1 = index
        row.dealNum2 = index
        row.dealNum3 = index
        row.dealMemberNum = index
        index++
    }
    dataRowHelper.save(dataRows)

    // добавляем строки-группировки по организациям
    addAllStatic()
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
                    def matrixRow = getPreRow(srcRow, source.formType.id)

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

    // итоговые строки (всё то, что в итоге попадет в сводный отчет)
    def summaryRows = []

    // Сортировка по dealNum3
    sortRows(matrixRows, ['dealNum3'])

    def Long currentGroup
    def mapForSummary = [:]

    matrixRows.each { matrixRow ->
        def srcRow = rowsMap.get(matrixRow)
        def Long reportClass = matrixRow.dealNum2.longValue()

        if ((reportClass.equals(2L) && !isGroupClass2(matrixRow, srcRow))
                || (reportClass.equals(3L) && !getRecSWId().equals(srcRow.serviceType))
                || reportClass.equals(4L)) { // копируем построчно
            if (mapForSummary.size() > 0) {
                summaryRows.add(getRow(mapForSummary))
            }
            mapForSummary.clear()
            mapForSummary.put(matrixRow, srcRow)
            summaryRows.add(getRow(mapForSummary))
            mapForSummary.clear()
            currentGroup = null

        } else { // группируем перед копированием
            if (currentGroup == null) { // первая строка
                currentGroup = matrixRow.dealNum3
                mapForSummary.put(matrixRow, srcRow)
            } else if (currentGroup.equals(matrixRow.dealNum3.longValue())) { // строка из той же группы что предыдущая
                mapForSummary.put(matrixRow, srcRow)
            } else { // строка из новой группы

                // получаем итоговую строку для предыдущей группы
                summaryRows.add(getRow(mapForSummary))

                currentGroup = matrixRow.dealNum3
                mapForSummary.clear()
                mapForSummary.put(matrixRow, srcRow)
            }
        }
    }

    if (mapForSummary.size() > 0) {
        summaryRows.add(getRow(mapForSummary))
    }

    dataRowHelper.save(summaryRows)
}

// Строка сводного отчета из первичных и консолидированных отчетов модуля МУКС (табл. 85)
def buildRow(def srcRow, def matrixRow) {
    def row = formData.createDataRow()
    def BigDecimal formTypeId = matrixRow.dealNum1

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
        case 376: // 1
        case 377: // 2
        case 380: // 5
        case 382: // 7
        case 399: // 22
            row.similarDealGroup = getRecYesId()
            break
        case 375: // 3
            // Заполняется уже для сгруппировонной строки
            break
        case 379: // 4
        case 381: // 6
        case 383: // 8
        case 384: // 9
        case 385: // 10
        case 386: // 11
        case 387: // 12
        case 388: // 13
        case 389: // 14
        case 390: // 15
        case 391: // 16
        case 392: // 17
        case 393: // 18
        case 394: // 19
        case 397: // 20
        case 398: // 21
        case 402: // 23
        case 401: // 24
        case 403: // 25
        case 404: // 26
            row.similarDealGroup = getRecNoId()
            break
    }

    // Графа 13
    def String val13 = null
    switch (formTypeId) {
        case 376:
            val13 = '002'
            break
        case 377:
        case 375:
        case 380:
        case 382:
        case 399:
            val13 = '019'
            break
        case 379:
        case 398:
            val13 = '016'
            break
        case 383:
        case 391:
        case 392:
            val13 = '032'
            break
        case 384:
        case 381:
        case 393:
        case 394:
            val13 = '015'
            break
        case 385:
        case 404: // 26
            val13 = '029'
            break
        case 386:
        case 388:
        case 401:
        case 402:
        case 403:
            val13 = '003'
            break
        case 387:
        case 389:
            val13 = '012'
            break
        case 390:
            val13 = '017'
            break
        case 397:
            val13 = '004'
            break
    }
    if (val13 != null) {
        row.dealNameCode = getRecordId(67, 'CODE', "$val13")
    }

    // Графа 14
    def String val14 = null
    switch (formTypeId) {
        case 376: // 1
            if (srcRow.incomeBankSum != null) {
                val14 = '004'
            } else if (srcRow.outcomeBankSum != null) {
                val14 = '003'
            }
            break
        case 377: // 2
        case 375: // 3
        case 380: // 5
        case 399: // 22
            val14 = '012'
            break
        case 379: // 4
        case 398: // 21
            val14 = '028'
            break
            break
        case 381: // 6
            if ((srcRow.incomeSum ?: 0) == 0 && (srcRow.outcomeSum ?: 0) > 0) {
                val14 = '026'
            } else if ((srcRow.incomeSum ?: 0) > 0 && (srcRow.outcomeSum ?: 0) == 0) {
                val14 = '027'
            }
            break
        case 382: // 7
            val14 = '011'
            break
        case 383: // 8
        case 391: // 16
        case 392: // 17
        case 393: // 18
            val14 = '052'
            break
        case 384: // 9
            if (getRecBuyId().equals(srcRow.transactionType)) {
                val14 = '026'
            } else if (getRecSellId().equals(srcRow.transactionType)) {
                val14 = '027'
            }
            break
        case 385: // 10
        case 387: // 12
        case 389: // 14
            val14 = '022'
            break
        case 386: // 11
        case 388: // 13
            val14 = '005'
            break
        case 390: // 15
            val14 = '030'
            break
        case 394: // 19
            def boolean dealBuy = getRecDealBuyId().equals(srcRow.dealFocus)
            val14 = (dealBuy ? '026' : '027')
            break
        case 397: // 20
            val14 = '007'
            break
        case 402: // 23
            val14 = '020'
            break
        case 401: // 24
        case 403: // 25
            val14 = '002'
            break
        case 404: // 26
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
        case 385:
            val16 = 3
            break
        case 384:
            if (srcRow.transactionMode != null) {
                def val16Rec = getRefBookValue(14, srcRow.transactionMode)
                if (val16Rec.ID != null && val16Rec.ID == 1) {
                    val16 = 2
                }
            }
            break
    }

    row.dealPriceCode = getRecordId(66, 'CODE', "$val16")

    // Графа 17
    row.dealMemberCount = 2

    // Графа 18
    // заполняется предварительно для каждой строки getPreRow(def srcRow, def BigDecimal formTypeId)
    row.income = 0

    // Графа 20
    // заполняется предварительно для каждой строки getPreRow(def srcRow, def BigDecimal formTypeId)
    row.outcome = 0

    // Графа 23
    def int val23 = 2
    switch (formTypeId) {
        case 379:
        case 385:
        case 397:
        case 402:
            val23 = 3
            break
        case 393:
        case 394:
            val23 = 1
            break
    }

    row.dealType = getRecordId(64, 'CODE', "$val23")

    // Графа 24
    switch (formTypeId) {
        case 376: // 1
            if (srcRow.incomeBankSum != null) {
                row.dealSubjectName = 'Предоставление помещений в аренду (субаренду)'
            } else if (srcRow.outcomeBankSum != null) {
                row.dealSubjectName = 'Получение помещений в аренду (субаренду)'
            }
            break
        case 377: // 2
            row.dealSubjectName = 'Услуги, связанные с обслуживанием недвижимости'
            break
        case 375: // 3
            row.dealSubjectName = 'Услуги по разработке, внедрению и модификации программного обеспечения'
            break
        case 379: // 4
            row.dealSubjectName = 'Услуги по предоставлению права пользования товарным знаком'
            break
        case 380: // 5
            row.dealSubjectName = 'Приобретение услуг, связанных с организацией и проведением торгов по реализации имущества'
            break
        case 381: // 6
            def String out = (getRecRPCId().equals(srcRow.dealSign) ? "" : "вне")
            if ((srcRow.incomeSum ?: 0) == 0 && (srcRow.outcomeSum ?: 0) > 0) {
                row.dealSubjectName = "Покупка акций и долей - " + out + "биржевые сделки"
            } else if ((srcRow.incomeSum ?: 0) > 0 && (srcRow.outcomeSum ?: 0) == 0) {
                row.dealSubjectName = "Продажа акций и долей - " + out + "биржевые сделки"
            }
            break
        case 382: // 7
            row.dealSubjectName = 'Оказание банковских услуг'
            break
        case 383: // 8
            def String out = (getRecDealsModeId().equals(srcRow.dealsMode) ? "" : "вне")
            if (srcRow.percentIncomeSum != null) {
                row.dealSubjectName = "Операции РЕПО - " + out + "биржевые (процентный доход)"
            } else if (srcRow.percentConsumptionSum != null) {
                row.dealSubjectName = "Операции РЕПО - " + out + "биржевые (процентный расход)"
            }
            break
        case 384: // 9
            def String out = (getRecDealsModeId().equals(srcRow.transactionMode) ? "" : "вне")
            if (getRecBuyId().equals(srcRow.transactionType)) {
                row.dealSubjectName = "Покупка ЦБ - " + out + "биржевые сделки"
            } else if (getRecSellId().equals(srcRow.transactionType)) {
                row.dealSubjectName = "Продажа ЦБ - " + out + "биржевые сделки"
            }
            break
        case 385: // 10
            row.dealSubjectName = 'Уступка прав требования - с обязательной оценкой'
            break
        case 386: // 11
            row.dealSubjectName = 'открытие аккредитивов и инструментов торгового финансирования'
            break
        case 387: // 12
            row.dealSubjectName = 'Размещение денежных средств корпоративным клиентам - не регулируемые сделки'
            break
        case 388: // 13
            row.dealSubjectName = 'Выдача гарантий (открытие аккредитивов и инструментов торгового финансирования)'
            break
        case 389: // 14
            row.dealSubjectName = 'Размещение денежных средств в межбанковские кредиты'
            break
        case 390: // 15
            if (srcRow.incomeSum != null) {
                row.dealSubjectName = 'Кассовые конверсионные сделки - доходные'
            } else if (srcRow.outcomeSum != null) {
                row.dealSubjectName = 'Кассовые конверсионные сделки - расходные'
            }
            break
        case 391: // 16
        case 392: // 17
        case 393: // 18
        case 394: // 19
            // расчитывается дальше для группы
            break
        case 397: // 20
            row.dealSubjectName = 'Привлечение денежных средств'
            break
        case 398: // 21
            row.dealSubjectName = 'Предоставление лицензий на программное обеспечение'
            break
        case 399: // 22
            row.dealSubjectName = getRefBookValue(13, srcRow.serviceName)?.NAME?.stringValue
            break
        case 402: // 23
            row.dealSubjectName = 'Привлечение денежных средств в межбанковские кредиты'
            break
        case 401: // 24
            row.dealSubjectName = 'Привлечение гарантий (открытие аккредитивов и других инструментов торгового финансирования)'
            break
        case 403: // 25
            row.dealSubjectName = 'Привлечение гарантий (открытие аккредитивов и других инструментов торгового финансирования)'
            break
        case 404: // 26
            row.dealSubjectName = 'Приобретение прав требования'
            break
    }

    // Графа 26
    def val26 = null
    switch (formTypeId) {
        case 393:
            val26 = srcRow.innerCode
            break
        case 394:
            val26 = srcRow.metalName
            break
    }
    if (val26 != null) {
        def String innerCode = getRefBookValue(17, val26).INNER_CODE.stringValue
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
        if (code != null){
            row.dealSubjectCode2 = getRecordId(68, 'CODE', code)
        }
    }

    // Графа 27
    def String val27 = null
    switch (formTypeId) {
        case 376:
            val27 = '70.20.2'
            break
        case 377:
            val27 = '70.32.2'
            break
        case 375:
            val27 = '72.20'
            break
        case 379:
        case 380:
            val27 = '74.8'
            break
        case 381:
        case 384:
        case 386:
        case 388:
        case 391:
        case 392:
        case 401:
        case 403:
            val27 = '65.23'
            break
        case 382:
            val27 = '67.12'
            break
        case 383:
        case 385:
        case 387:
        case 389:
            val27 = '65.22'
            break
        case 390:
        case 397:
        case 402:
            val27 = '65.12'
            break
        case 398:
        case 399:
            val27 = '74'
            break
    }

    if (val27 != null) {
        row.dealSubjectCode3 = getRecordId(34, 'CODE', "$val27")
    }

    // Графа 28
    row.otherNum = 1

    // Графа 29
    // заполняется предварительно для каждой строки getPreRow(def srcRow, def BigDecimal formTypeId)
    row.contractNum = matrixRow.contractNum

    // Графа 30
    // заполняется предварительно для каждой строки getPreRow(def srcRow, def BigDecimal formTypeId)
    row.contractDate = matrixRow.contractDate

    // Заполнение графы 15
    Calendar compareCalendar15 = Calendar.getInstance()
    compareCalendar15.set(2011, 12, 28)
    if (compareCalendar15.getTime().equals(row.contractDate) && "123".equals(row.contractNum)) {
        row.dealPriceSign = recYesId
    }

    // Графа 31
    switch (formTypeId) {
        case 393:
            row.countryCode = srcRow.unitCountryCode
            break
        case 394:
            row.countryCode = srcRow.countryCodeNumeric
            break
    }

    // Графа 32, Графа 33, Графа 34, Графа 35
    if (formTypeId == 393 || formTypeId == 394) {
        sign32 = formTypeId == 393 ? srcRow.signPhis : srcRow.deliverySign
        if (sign32 != null) {
            def values32 = getRefBookValue(18, sign32)
            if (values32 != null && values32.SIGN.stringValue.equals("Физическая поставка")) {
                if (formTypeId == 393) {
                    row.countryCode1 = srcRow.countryCode2
                    row.region1 = srcRow.region1
                    row.city1 = srcRow.city1
                    row.locality1 = srcRow.settlement1
                }

                if (formTypeId == 394) {
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
        case 376:
        case 377:
            row.countryCode2 = srcRow.country
            row.region2 = srcRow.region
            row.city2 = srcRow.city
            row.locality2 = srcRow.settlement
            break
        case 393:
            row.countryCode2 = srcRow.countryCode3
            row.region2 = srcRow.region2
            row.city2 = srcRow.city2
            row.locality2 = srcRow.settlement2
            break
        case 394:
            row.countryCode2 = srcRow.countryCodeNumeric2
            row.region2 = srcRow.region2
            row.city2 = srcRow.city2
            row.locality2 = srcRow.locality2
            break
        default:
            row.countryCode2 = getRecordId(10, 'CODE', '643')
            row.region2 = getRecordId(4, 'CODE', '77')
            row.city2 = 'Москва'
            row.locality2 = row.city2
            break
    }

    // Графа 40
    if (formTypeId == 393) {
        row.deliveryCode = srcRow.conditionCode
    }
    if (formTypeId == 394) {
        row.deliveryCode = srcRow.deliveryCode
    }

    // Графа 41
    def String val41 = null
    switch (formTypeId) {
        case 376:
        case 377:
            val41 = '055'
            break
        case 375:
        case 379:
        case 380:
        case 382:
        case 383:
        case 384:
        case 390:
        case 391:
        case 392:
        case 386:
        case 387:
        case 388:
        case 389:
        case 393:
        case 394:
        case 397:
        case 398:
        case 399:
        case 402:
        case 401:
        case 403:
            val41 = '796'
            break
        case 381:
        case 385:
        case 404:
            row.okeiCode = srcRow.okeiCode
            break
    }
    if (val41 != null) {
        row.okeiCode = getRecordId(12, 'CODE', "$val41")
    }

    // Графа 42
    switch (formTypeId) {
        case 376:
        case 377:
        case 381:
        case 385:
        case 387:
        case 389:
        case 393:
        case 394:
        case 404:
            row.count = srcRow.count
            break
        case 384:
            row.count = srcRow.bondCount
            break
        default:
            row.count = 1
            break
    }

    // Графа 43
    switch (formTypeId) {
        case 376:
        case 377:
        case 375:
        case 379:
        case 380:
        case 381:
        case 382:
        case 387:
        case 390:
        case 391:
        case 392:
        case 385:
        case 386:
        case 388:
        case 389:
        case 394:
        case 397:
        case 398:
        case 399:
        case 402:
        case 401:
        case 403:
        case 404:
            row.price = srcRow.price
            break
        case 383:
            row.price = srcRow.priceFirstCurrency
            break
        case 384:
        case 393:
            row.price = srcRow.priceOne
            break
    }

    // Графа 44
    switch (formTypeId) {
        case 375:
        case 376:
        case 377:
        case 379:
        case 380:
        case 381:
        case 382:
        case 387:
        case 392:
        case 397:
        case 398:
        case 399:
            row.total = srcRow.cost
            break
        case 383:
            if (srcRow.percentIncomeSum != null)
                row.total = srcRow.percentIncomeSum
            if (srcRow.percentConsumptionSum != null)
                row.total = srcRow.percentConsumptionSum
            break
        case 384:
            row.total = srcRow.transactionSumRub
            break
        case 385:
        case 404:
            row.total = srcRow.totalCost
            break
        case 386:
        case 388:
        case 389:
        case 390:
        case 391:
        case 394:
        case 402:
        case 401:
        case 403:
            row.total = srcRow.total
            break
        case 393:
            row.total = srcRow.totalNds
            break
    }

    // Графа 45
    // заполняется предварительно для каждой строки getPreRow(def srcRow, def BigDecimal formTypeId)
    row.dealDoneDate = matrixRow.dealDoneDate

    // Графа 49
    // countryCode3 заполняется после графы 50

    // Графа 50
    // заполняется предварительно для каждой строки getPreRow(def srcRow, def BigDecimal formTypeId)
    row.organName = matrixRow.organName

    if (row.organName != null) {
        // Графа 3

        // Если атрибут 50 «Матрицы» содержит значение, в котором в справочнике
        // «Организации – участники контролируемых сделок» атрибут «Резидент оффшорной зоны» = 1,
        // то заполняется значением «0». В ином случае заполняется значением «1».
        def val = getRefBookValue(9, row.organName)
        row.f121 = val.OFFSHORE.numberValue == 1 ? recNoId : recYesId

        // Графа 5 (логика, обратная графе 3)
        row.f123 = row.f121 == recYesId ? recNoId : recYesId

        // Графа 7 (та же логика, что у графы 3)
        row.f131 = row.f121

        // Графа 10
        // Если атрибут 50 «Матрицы» содержит значение, в котором в справочнике
        // «Организации – участники контролируемых сделок» атрибут «Освобождена от налога на прибыль либо является
        // резидентом Сколково» = 1, то заполняется значением «1». В ином случае заполняется значением «0».
        row.f134 = val.SKOLKOVO.numberValue == 1 ? recYesId : recNoId

        // Графа 49
        // Код страны
        row.countryCode3 = val.COUNTRY?.referenceValue
    }

    // Графа 48, 51, 52, 53, 54, 55
    // зависимые в конфигураторе


    return row
}

// определение класса строки по типу формы
def Long getReportClass(def BigDecimal formTypeId) {
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
            return 3
        case 379:
        case 387:
        case 398:
        case 404:
            return 4
        default:
            return 2
    }
}

// значение для группировки строки (табл. 86)
def String getGroupId(def matrixRow, def srcRow) {
    def StringBuilder group = new StringBuilder()
    group.append(matrixRow.dealNum1).append("#")
            .append(matrixRow.organName).append("#")
            .append(matrixRow.contractDate).append("#")
            .append(matrixRow.contractNum).append("#")
    switch (matrixRow.dealNum1) {
        case 376: // 1
            group.append(srcRow.incomeBankSum != null).append("#")
            group.append(srcRow.outcomeBankSum != null)
            break
        case 375: // 3
            group.append(srcRow.serviceType)
            break
        case 381: // 6
            group.append(getRecRPCId().equals(srcRow.dealSign)).append("#")
            group.append((srcRow.incomeSum ?: 0) == 0 && (srcRow.outcomeSum ?: 0) > 0).append("#")
            group.append((srcRow.incomeSum ?: 0) > 0 && (srcRow.outcomeSum ?: 0) == 0)
            break
        case 383: // 8
            group.append(getRecDealsModeId().equals(srcRow.dealsMode)).append("#")
            group.append(srcRow.percentIncomeSum != null).append("#")
            group.append(srcRow.percentConsumptionSum != null)
            break
        case 384: // 9
            group.append(getRecDealsModeId().equals(srcRow.transactionMode)).append("#")
            group.append(getRecBuyId().equals(srcRow.transactionType)).append("#")
            group.append(getRecSellId().equals(srcRow.transactionType))
            break
        case 390: // 15
            group.append(srcRow.currencyCode).append("#")
            group.append(srcRow.incomeSum).append("#")
            group.append(srcRow.outcomeSum)
            break
        case 391: // 16
            group.append(srcRow.currencyCode).append("#")
            group.append(srcRow.dealType).append("#")
            group.append((srcRow.incomeSum ?: 0) - (srcRow.outcomeSum ?: 0) > 0)
            break
        case 392: // 17
            group.append(srcRow.transactionType).append("#")
            group.append((srcRow.incomeSum ?: 0) - (srcRow.consumptionSum ?: 0) > 0)
            break
        case 393: // 18
            group.append(srcRow.innerCode).append("#")
            group.append(srcRow.dealType).append("#")
            group.append((srcRow.incomeSum ?: 0) - (srcRow.consumptionSum ?: 0) > 0)
            break
        case 394: // 19
            group.append(srcRow.metalName).append("#")
            group.append(srcRow.dealFocus).append("#")
            group.append((srcRow.incomeSum ?: 0) - (srcRow.outcomeSum ?: 0) > 0)
            break
        case 399: // 22
            group.append(srcRow.serviceName)
            break
    }
    return group.toString()
}

// получение строки итогового отчета на основании группы строк (или одной строки) из "матрицы" и источника (табл. 87)
def getRow(def map) {
    // для отчетов 16..19 надо считать суммы по двум столбцам
    def totalSum = 0
    map.each { matrixRow, srcRow ->
        if (matrixRow.dealNum1.longValue() in [391L, 394L, 392L, 393L]) {
            totalSum = matrixRow.income - matrixRow.outcome
        }
    }

    def row = formData.createDataRow()
    def boolean first = true
    map.each { matrixRow, srcRow ->
        if (first) {
            first = false

            row = buildRow(srcRow, matrixRow)

            if (matrixRow.dealNum1.longValue().equals(375L)) {
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

        switch (matrixRow.dealNum1) {
            case 376: // 1
            case 383: // 8
            case 390: // 15
                row.income = row.income + matrixRow.income
                row.outcome = row.outcome + matrixRow.outcome
                break
            case 377: // 2
            case 375: // 3
            case 380: // 5
            case 397: // 20
            case 398: // 21
            case 399: // 22
            case 402: // 23
            case 401: // 24
            case 403: // 25
            case 404: // 26
                row.outcome = row.outcome + matrixRow.outcome
                break
            case 379: // 4
            case 382: // 7
            case 385: // 10
            case 386: // 11
            case 387: // 12
            case 388: // 13
            case 389: // 14
                row.income = row.income + matrixRow.income
                break
            case 381: // 6
                if ((srcRow.incomeSum ?: 0) == 0 && (srcRow.outcomeSum ?: 0) > 0) {
                    row.outcome = row.outcome + srcRow.outcomeSum
                } else if ((srcRow.incomeSum ?: 0) > 0 && (srcRow.outcomeSum ?: 0) == 0) {
                    row.income = row.income + srcRow.cost
                }
                break
            case 384: // 9
                if (getRecBuyId().equals(srcRow.transactionType)) {
                    row.outcome = row.outcome + srcRow.transactionSumRub
                } else if (getRecSellId().equals(srcRow.transactionType)) {
                    row.income = row.income + srcRow.transactionSumRub
                }
                break
            case 391: // 16
                def String dealName = 'Срочные поставочные конверсионные сделки (сделки с отсрочкой исполнения) - '
                if (totalSum >= 0) {
                    row.outcome = (row.outcome ?: 0) + (srcRow.price ?: 0)
                    row.dealSubjectName = dealName + 'доход'
                } else {
                    row.income = (row.income ?: 0) + (srcRow.price ?: 0)
                    row.dealSubjectName = dealName + 'расход'
                }
                break
            case 392: // 17
                def String dealName = 'Беспоставочные (расчетные) срочные сделки - '
                if (totalSum >= 0) {
                    row.outcome = (row.outcome ?: 0) + (srcRow.price ?: 0)
                    row.dealSubjectName = dealName + 'доходные'
                } else {
                    row.income = (row.income ?: 0) + (srcRow.price ?: 0)
                    row.dealSubjectName = dealName + 'расходные'
                }
                break
            case 393: // 18
                def String dealName = 'Срочные поставочные сделки купли-продажи драгоценных металлов (сделки с ' +
                        'отсрочкой исполнения), ' + (getRecRUSId().equals(srcRow.unitCountryCode) ? "покупка, " : "продажа, ")
                if (totalSum >= 0) {
                    row.outcome = (row.outcome ?: 0) + (srcRow.priceOne ?: 0)
                    row.dealSubjectName = dealName + 'доход'
                } else {
                    row.income = (row.income ?: 0) + (srcRow.priceOne ?: 0)
                    row.dealSubjectName = dealName + 'расход'
                }
                break
            case 394: // 19
                def boolean dealBuy = getRecDealBuyId().equals(srcRow.dealFocus)
                def String dealName = 'Кассовые сделки ' + (dealBuy ? "покупки " : "продажи ") + ' драгоценных металлов - '
                if (totalSum >= 0) {
                    row.outcome = (row.outcome ?: 0) + (srcRow.incomeSum ?: 0)
                    row.dealSubjectName = dealName + 'доходные'
                } else {
                    row.income = (row.income ?: 0) + (srcRow.outcomeSum ?: 0)
                    row.dealSubjectName = dealName + 'расходные'
                }
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

// "Услуги в части программного обеспечения" = 1 (Услуги по разработке, внедрению, поддержке и модификации программного обеспечения)
@Field
def Long recSWId

def Long getRecSWId() {
    if (recSWId == null)
        recSWId = getRecordId(11, 'CODE', '1')
    return recSWId
}

// дополнительное условие для отчетов "класс 2" на попадание в группу
boolean isGroupClass2(def matrixRow, def srcRow) {
    def boolean class2ext = false
    switch (matrixRow.dealNum1) {
        case 383: // 8
        case 392: // 17
        case 393: // 18
            class2ext = srcRow.transactionNum == null && srcRow.transactionDeliveryDate == null
            break
        case 384: // 9
            class2ext = srcRow.transactionDeliveryDate == null
            break
        case 386: // 11
        case 389: // 14
        case 390: // 15
        case 391: // 16
        case 394: // 19
        case 402: // 23
        case 401: // 24
        case 403: // 25
            class2ext = srcRow.dealNumber == null && srcRow.dealDate == null
            break
        case 388: // 13
            class2ext = srcRow.dealNumber == null && srcRow.transactionDeliveryDate == null
            break
    }
    return class2ext || (matrixRow.contractDate != null && matrixRow.contractNum != null)
}

// Заполняем каждую строку полученную из источника необходимыми предварительными значениями
def getPreRow(def srcRow, def BigDecimal formTypeId) {
    def row = formData.createDataRow()
    // тип отчета
    row.dealNum1 = formTypeId
    // класс отчет
    row.dealNum2 = getReportClass(formTypeId)

    // Графа 18
    switch (formTypeId) {
        case 376: // 1
            row.income = srcRow.incomeBankSum
            break
        case 379: // 4
        case 386: // 11
        case 388: // 13
        case 389: // 14
            row.income = srcRow.sum
            break
        case 381: // 6
        case 387: // 12
            row.income = srcRow.cost
            break
        case 382: // 7
        case 398: // 21
            row.income = srcRow.bankIncomeSum
            break
        case 383: // 8
            row.income = srcRow.percentIncomeSum
            break
        case 384: // 9
            row.income = srcRow.transactionSumRub
            break
        case 385: // 10
            row.income = srcRow.totalCost
            break
        case 390: // 15
            row.income = srcRow.incomeSum
            break
        case 391: // 16
            row.income = srcRow.price
            break
        case 392: // 17
            row.income = srcRow.price
            break
        case 393: // 18
            row.income = srcRow.priceOne
            break
        case 394: // 19
            row.income = srcRow.outcomeSum
            break
    }
    if (row.income == null) {
        row.income = 0
    }

    // Графа 20
    switch (formTypeId) {
        case 376: // 1
            row.outcome = srcRow.outcomeBankSum
            break
        case 377: // 2
            row.outcome = srcRow.bankSum
            break
        case 375: // 3
            row.outcome = srcRow.expensesSum
            break
        case 380: // 5
        case 397: // 20
        case 402: // 23
        case 401: // 24
            row.outcome = srcRow.sum
            break
        case 381: // 6
        case 390: // 15
        case 403: // 25
            row.outcome = srcRow.outcomeSum
            break
        case 383: // 8
            row.outcome = srcRow.percentConsumptionSum
            break
        case 384: // 9
            row.outcome = srcRow.transactionSumRub
            break
        case 391: // 16
        case 392: // 17
        case 404: // 26
            row.outcome = srcRow.price
            break
        case 393: // 18
            row.outcome = srcRow.priceOne
            break
        case 394: // 19
            row.outcome = srcRow.incomeSum
            break
        case 399: // 22
            row.outcome = srcRow.bankIncomeSum
            break
    }
    if (row.outcome == null) {
        row.outcome = 0
    }

    // Графа 30
    switch (formTypeId) {
        case 376:
        case 377:
        case 382:
        case 383:
        case 384:
        case 385:
        case 392:
        case 393:
        case 398:
        case 399:
        case 404:
            row.contractDate = srcRow.contractDate
            break
        case 375:
        case 379:
        case 380:
        case 381:
        case 386:
        case 387:
        case 388:
        case 389:
        case 390:
        case 391:
        case 394:
        case 397:
        case 402:
        case 401:
        case 403:
            row.contractDate = srcRow.docDate
            break
    }

    switch (formTypeId) {
        case 376:
        case 377:
        case 382:
        case 383:
        case 384:
        case 385:
        case 392:
        case 393:
        case 398:
        case 399:
        case 404:
            row.contractNum = srcRow.contractNum
            break
        case 375:
        case 379:
        case 380:
        case 381:
        case 386:
        case 387:
        case 388:
        case 389:
        case 391:
        case 394:
        case 397:
        case 401:
        case 403:
            row.contractNum = srcRow.docNumber
            break
        case 390:
        case 402:
            row.contractNum = srcRow.docNum
            break
    }

    // Графа 45
    // заполняется предварительно для каждой строки
    switch (formTypeId) {
        case 376:
        case 377:
        case 382:
        case 383:
        case 385:
        case 392:
        case 393:
        case 398:
        case 399:
        case 404:
            row.dealDoneDate = srcRow.transactionDate
            break
        case 375:
        case 379:
        case 381:
        case 387:
        case 397:
            row.dealDoneDate = srcRow.dealDate
            break
        case 380:
            row.dealDoneDate = srcRow.date
            break
        case 386:
        case 388:
        case 389:
        case 390:
        case 391:
        case 394:
        case 402:
        case 401:
        case 403:
            row.dealDoneDate = srcRow.dealDoneDate
            break
        case 384:
            row.dealDoneDate = srcRow.transactionDeliveryDate
            break
    }

    // Графа 50
    switch (formTypeId) {
        case 376:
        case 377:
        case 382:
        case 383:
        case 398:
        case 399:
            row.organName = srcRow.jurName
            break
        case 375:
        case 379:
        case 380:
        case 381:
        case 387:
        case 397:
        case 402:
            row.organName = srcRow.fullNamePerson
            break
        case 384:
            row.organName = srcRow.contraName
            break
        case 385:
        case 392:
        case 393:
        case 404:
            row.organName = srcRow.name
            break
        case 386:
        case 388:
        case 389:
        case 390:
        case 391:
        case 394:
        case 401:
        case 403:
            row.organName = srcRow.fullName
            break
    }

    return row
}

// Проставляет статические строки
void addAllStatic() {
    if (!logger.containsLevel(LogLevel.ERROR)) {
        def dataRowHelper = formDataService.getDataRowHelper(formData)
        def dataRows = dataRowHelper.getAllCached()
        def int index = 1
        for (int i = 0; i < dataRows.size(); i++) {
            def row = dataRows.get(i)
            def nextRow = null

            if (i < dataRows.size() - 1) {
                nextRow = dataRows.get(i + 1)
            }
            if (row.getAlias() == null) {
            }
            if (nextRow == null || row.organName != nextRow.organName) {

                def newRow = formData.createDataRow()
                newRow.getCell('groupName').colSpan = 56
                if (row.organName != null)
                    newRow.groupName = getRefBookValue(9, row.organName).NAME.stringValue
                newRow.setAlias('grp#'.concat(i.toString()))
                dataRowHelper.insert(newRow, ++i + 1 - index)
                index = 1
            } else {
                index++
            }
        }
    }
}