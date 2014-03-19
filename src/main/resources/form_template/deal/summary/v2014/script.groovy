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
    def sourceRows = []
    // счетчик по группам для табл. 86
    def int i = 1
    // мапа идентификаторов для группировки
    def groupStr = [:]
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind).each {
        def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (source != null && source.state == WorkflowState.ACCEPTED && source.formType.taxType == TaxType.DEAL) {
            formDataService.getDataRowHelper(source).allCached.each { srcRow ->
                if (srcRow.getAlias() == null) {
                    def row = buildRow(srcRow, source.formType)

                    // идентификатор для группировки
                    String group = getGroupId(row, srcRow)
                    if (!groupStr.containsKey(group)) {
                        groupStr.put(group, i++)
                    }
                    row.dealNum3 = groupStr.get(group)

                    sourceRows.add(row)
                }
            }
        }
    }

    // "1" для графы 8 отчета 3
    //def Long serviceType1 = getRecordId(11, 'CODE', '1')

    // итоговые строки (всё то, что в итоге попадет в сводный отчет)
    def rows = []

    // Сортировка по row.dealNum3
    sortRows(sourceRows, ['dealNum3'])

    def Long currentGroup
    def list = []

    sourceRows.each { row ->
        logger.info("" + row)
        if (currentGroup == null) { // первая строка
            currentGroup = row.dealNum3
            list.add(row)
        } else if (row.dealNum3.equals(currentGroup)) { // строка из той же группы что предыдущая
            list.add(row)
        } else { // строка из новой группы

            // получаем итоговую строку для предыдущей группы
            rows.add(getRow(list))

            currentGroup = row.dealNum3
            list.clear()
            list.add(row)
        }
    }

    dataRowHelper.save(rows)
}

// Подготовка строки сводного отчета из первичных и консолидированных отчетов модуля МУКС
def buildRow(def srcRow, def fomType) {
    def row = formData.createDataRow()
    // Временный алиас строки
    row.setAlias("group_$fomType.id")

    // тип отчета
    row.dealNum1 = fomType.id
    // класс отчет
    row.dealNum2 = getReportClass(fomType.id)

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
        case 397:
        case 399:
            return 1
        case 375:
            return 3
        case 379:
        case 385:
        case 387:
        case 398:
            return 4
        default:
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

def getRow(def list) {
    def row = formData.createDataRow()
    // Временный алиас строки
    row.setAlias(list.get(0).getAlias())
    // TODO что копировать?
    ['dealNum1','dealNum2','dealNum3'].each { alias ->
        row.getCell(alias).setValue(list.get(0).getCell(alias).value, null)
    }
    list.each { srcRow ->
        // TODO заполнить row по табл. 87
        switch (srcRow.dealNum1) {
            case 376:
                // 1
                if (srcRow.income != null) {
                    row.income = (row.income ?: 0) + srcRow.income
                    row.taxpayerSideCode = getRecordId(65, 'CODE', "022")
                    row.dealSubjectName = 'Предоставление помещений в аренду (субаренду)'
                } else if (srcRow.outcome != null) {
                    row.outcome = (row.outcome ?: 0) + srcRow.income
                    row.taxpayerSideCode = getRecordId(65, 'CODE', "020")
                    row.dealSubjectName = 'Получение помещений в аренду (субаренду)'
                }
                row.dealNameCode = getRecordId(67, 'CODE', "012")
                if (srcRow.dealDoneDate != null && (row.dealDoneDate == null || srcRow.dealDoneDate > row.dealDoneDate)) {
                    row.dealDoneDate = srcRow.dealDoneDate
                }
                row.similarDealGroup = getRecYesId()
                break
            case 377:
                // 2
                row.outcome = (row.outcome ?: 0) + (srcRow.outcome ?: 0)
                row.dealNameCode = getRecordId(67, 'CODE', "019")
                row.taxpayerSideCode = getRecordId(65, 'CODE', "012")
                row.dealSubjectName = 'Услуги, связанные с обслуживанием недвижимости'
                if (srcRow.dealDoneDate != null && (row.dealDoneDate == null || srcRow.dealDoneDate > row.dealDoneDate)) {
                    row.dealDoneDate = srcRow.dealDoneDate
                }
                row.similarDealGroup = getRecYesId()
                break
            case 375:
                // 3
                row.outcome = (row.outcome ?: 0) + (srcRow.outcome ?: 0)
                row.dealNameCode = getRecordId(67, 'CODE', "019")
                row.taxpayerSideCode = getRecordId(65, 'CODE', "012")
                row.dealSubjectName = 'Услуги по разработке, внедрению и модификации программного обеспечения'
                if (srcRow.dealDoneDate != null && (row.dealDoneDate == null || srcRow.dealDoneDate > row.dealDoneDate)) {
                    row.dealDoneDate = srcRow.dealDoneDate
                }
                row.similarDealGroup = getRecYesId()
                break
            case 379:
                // 4
                row.income = (row.income ?: 0) + (srcRow.income ?: 0)
                row.dealNameCode = getRecordId(67, 'CODE', "016")
                row.taxpayerSideCode = getRecordId(65, 'CODE', "028")
                row.dealSubjectName = 'Услуги по предоставлению права пользования товарным знаком'
                if (srcRow.dealDoneDate != null && (row.dealDoneDate == null || srcRow.dealDoneDate > row.dealDoneDate)) {
                    row.dealDoneDate = srcRow.dealDoneDate
                }
                row.similarDealGroup = getRecYesId()
                break
            case 380:
                // 5
                row.outcome = (row.outcome ?: 0) + (srcRow.outcome ?: 0)
                row.dealNameCode = getRecordId(67, 'CODE', "019")
                row.taxpayerSideCode = getRecordId(65, 'CODE', "012")
                row.dealSubjectName = 'Приобретение услуг, связанных с организацией и проведением торгов по реализации имущества'
                if (srcRow.dealDoneDate != null && (row.dealDoneDate == null || srcRow.dealDoneDate > row.dealDoneDate)) {
                    row.dealDoneDate = srcRow.dealDoneDate
                }
                row.similarDealGroup = getRecYesId()
                break
            case 381:
                // 6
               /* if(getRecRPCId().equals()){

                }   else if(){

                }*/
                break
            case 382:
                // 7
            case 383:
                // 8
                break
            case 384:
                // 9
                break
            case 385:
                // 10
                break
            case 386:
                // 11
                break
            case 387:
                // 12
                break
            case 388:
                // 13
                break
            case 389:
                // 14
                break
            case 390:
                // 15
                break
            case 391:
                // 16
                break
            case 392:
                // 17
                break
            case 393:
                // 18
                break
            case 394:
                // 19
                break
            case 397:
                // 20
                break
            case 398:
                // 21
                break
            case 399:
                // 22
                break
            case 402:
                // 23
                break
            case 401:
                // 24
                break
            case 403:
                // 25
                break
        }
    }
    return row
}

// Общие значения

// "Да"
@Field
def Long recYesId

def Long getRecYesId(){
    if (recYesId == null)
        recYesId = getRecordId(38, 'CODE', '1')
    return recYesId
}

// "Нет"
@Field
def Long recNoId

def Long getRecNoId(){
    if (recNoId == null)
        recNoId = getRecordId(38, 'CODE', '0')
    return recNoId
}

// "Признак сделки, совершенной в РПС" = 1
@Field
def Long recRPCId

def Long getRecRPCId(){
    if (recRPCId == null)
        recRPCId = getRecordId(36, 'SIGN', 'Да')
    return recRPCId
}