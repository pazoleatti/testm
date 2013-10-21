package form_template.deal.notification

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import groovy.transform.Field
import groovy.xml.MarkupBuilder
/**
 * Уведомление. Генератор XML.
 * http://conf.aplana.com/pages/viewpage.action?pageId=9594552
 * http://conf.aplana.com/pages/viewpage.action?pageId=10388852
 *
 * @author Dmitriy Levykin
 */

// TODO в DeclarationDataServiceImpl в check и setAccepted метод validateDeclaration вызывается до скрипта, а по аналитике д.б. после проверки подразделений (checkDeparmentParams)

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkDeparmentParams(LogLevel.WARNING)
        generateXML()
        break
    case FormDataEvent.CHECK:
        checkDeparmentParams(LogLevel.ERROR)
        break
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:
        checkDeparmentParams(LogLevel.ERROR)
        break
    default:
        return
}

// Кэш провайдеров
@Field
def providerCache = [:]
// Кэш значений справочника
@Field
def refBookCache = [:]

void checkDeparmentParams(LogLevel logLevel) {
    def date = reportPeriodService.getStartDate(declarationData.reportPeriodId).getTime()

    def departmentId = declarationData.departmentId

    // Параметры подразделения
    def departmentParam = getProvider(37).getRecords(date, null, "DEPARTMENT_ID = '$departmentId'", null).get(0)

    if (departmentParam == null) {
        throw new Exception("Ошибка при получении настроек обособленного подразделения")
    }

    // Проверки подразделения
    def List<String> errorList = getErrorDepartment(departmentParam)
    for (String error : errorList) {
        logger.log(logLevel, String.format("Для данного подразделения на форме настроек подразделений отсутствует значение атрибута %s", error))
    }
    errorList = getErrorVersion(departmentParam)
    for (String error : errorList) {
        logger.log(logLevel, String.format("Неверно указано значение атрибута %s на форме настроек подразделений для %s", error, departmentParam.NAME.stringValue))
    }
}

/**
 * Запуск генерации XML
 */
void generateXML() {
    def date = reportPeriodService.getStartDate(declarationData.reportPeriodId).getTime()

    def departmentId = declarationData.departmentId

    // Параметры подразделения
    def departmentParam = getProvider(37).getRecords(date, null, "DEPARTMENT_ID = '$departmentId'", null).get(0)

    def formDataCollection = declarationService.getAcceptedFormDataSources(declarationData)

    def builder = new MarkupBuilder(xml)

    // Тип декларации - Уведомление
    def notificationType = 6
    // Код формы отчетности по КНД
    def String KND = '1110025'

    def okato = departmentParam.OKATO.referenceValue != null ? getRefBookValue(3, departmentParam.OKATO.referenceValue).OKATO.stringValue : null
    def okvedCode = departmentParam.OKVED_CODE.referenceValue != null ? getRefBookValue(34, departmentParam.OKVED_CODE.referenceValue).CODE.stringValue : null
    def taxPlaceTypeCode = departmentParam.TAX_PLACE_TYPE_CODE.referenceValue != null ? getRefBookValue(2, departmentParam.TAX_PLACE_TYPE_CODE.referenceValue).CODE.stringValue : null

    def matrixFormData = formDataCollection.getRecords().get(0);

    // Заполнение кэша
    formDataService.fillRefBookCache(matrixFormData.getId(), refBookCache)

    builder.Файл(
            ИдФайл: declarationService.generateXmlFileId(notificationType, departmentId, declarationData.reportPeriodId),
            ВерсПрог: departmentParam.APP_VERSION.stringValue,
            ВерсФорм: departmentParam.FORMAT_VERSION.stringValue) {
        Документ(
                // Код формы отчетности по КНД
                КНД: KND,
                // Дата формирования документа
                ДатаДок: (docDate != null ? docDate : new Date()).format("dd.MM.yyyy"),
                // Отчетный год
                ОтчетГод: reportPeriodService.get(declarationData.reportPeriodId).year,
                // Код налогового органа
                КодНО: departmentParam.TAX_ORGAN_CODE.stringValue,
                // Номер корректировки
                // TODO сделать на следующей версии
                НомКорр: '0',
                // Код места, по которому представляется документ
                ПоМесту: taxPlaceTypeCode
        ) {
            СвНП(
                    ОКАТО: okato,
                    ОКВЭД: okvedCode,
                    Тлф: departmentParam.PHONE.stringValue
            ) {
                НПЮЛ(
                        НаимОрг: departmentParam.NAME.stringValue,
                        ИННЮЛ: departmentParam.INN.stringValue,
                        КПП: departmentParam.KPP.stringValue
                ) {
                    def reorgFormCode = departmentParam.REORG_FORM_CODE.referenceValue
                    reorgFormCode = reorgFormCode != null ? getRefBookValue(5, reorgFormCode).CODE.stringValue : null
                    def boolean isReorg = reorgFormCode != null && !reorgFormCode.equals('0')

                    if (reorgFormCode != null) {
                        СвРеоргЮЛ(
                                [ФормРеорг: reorgFormCode] +
                                        (isReorg ? [ИННЮЛ: departmentParam.REORG_INN.stringValue] : [:]) +
                                        (isReorg ? [КПП: departmentParam.REORG_KPP.stringValue] : [:])
                        )
                    }
                }
            }
            def prPodp = 1
            if (departmentParam.SIGNATORY_ID.referenceValue != null)
                prPodp = getRefBookValue(35, departmentParam.SIGNATORY_ID.referenceValue).CODE.numberValue
            Подписант(
                    ПрПодп: prPodp
            ) {
                def String surname = departmentParam.SIGNATORY_SURNAME.stringValue
                def String firstname = departmentParam.SIGNATORY_FIRSTNAME.stringValue
                def String lastname = departmentParam.SIGNATORY_LASTNAME.stringValue
                ФИО(
                        [Фамилия: surname] +
                                [Имя: firstname] +
                                (lastname != null && !lastname.isEmpty() ? [Отчество: lastname] : [:])
                )
                if (prPodp == 2)
                    СвПред(
                            НаимДок: departmentParam.APPROVE_DOC_NAME.stringValue,
                            НаимОрг: departmentParam.APPROVE_ORG_NAME.stringValue
                    )
            }

            // По строкам матрицы
            УвКонтрСд() {
                if (formDataCollection.getRecords().size() != 0) {
                    def dataRowHelper = formDataService.getDataRowHelper(matrixFormData)
                    // "Да"
                    def Long recYesId = null
                    // "Нет"
                    def Long recNoId = null
                    def valYes = getProvider(38L).getRecords(new Date(), null, "CODE = 1", null)
                    def valNo = getProvider(38L).getRecords(new Date(), null, "CODE = 0", null)
                    if (valYes != null && valYes.size() == 1)
                        recYesId = valYes.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
                    if (valNo != null && valNo.size() == 1)
                        recNoId = valNo.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
                    Map<Long, String> mapYesNo = new HashMap<Long, String>()
                    mapYesNo.put(recYesId, '1')
                    mapYesNo.put(recNoId, '0')

                    for (row in dataRowHelper.getAllCached()) {
                        if (row.getAlias() != null) {
                            continue
                        }
                        СвКонтрСд(
                                НомПорСд: row.dealNum1
                        ) {
                            def String interdependenceSing = row.interdependenceSing != null ? getRefBookValue(69, row.interdependenceSing).CODE.numberValue : null
                            ОснКонтрСд(
                                    ВзЗавис: interdependenceSing
                            ) {
                                'Осн105.14'(
                                        Осн121: mapYesNo.get(row.f121),
                                        Осн122: mapYesNo.get(row.f122),
                                        Осн123: mapYesNo.get(row.f123),
                                        Осн124: mapYesNo.get(row.f124)
                                )
                                'ОснРФ105.14'(
                                        Осн131: mapYesNo.get(row.f131),
                                        Осн132: mapYesNo.get(row.f132),
                                        Осн133: mapYesNo.get(row.f133),
                                        Осн134: mapYesNo.get(row.f134),
                                        Осн135: mapYesNo.get(row.f135)
                                )
                            }
                            def String dealNameCode = row.dealNameCode != null ? getRefBookValue(67, row.dealNameCode).CODE.stringValue : null
                            def String taxpayerSideCode = row.taxpayerSideCode != null ? getRefBookValue(65, row.taxpayerSideCode).CODE.stringValue : ''
                            while (taxpayerSideCode.length() < 3)
                                taxpayerSideCode = '0' + taxpayerSideCode
                            def String dealPriceCode = row.dealPriceCode != null ? getRefBookValue(66, row.dealPriceCode).CODE.numberValue : null
                            КонтрСд(
                                    [ГрупОС: mapYesNo.get(row.similarDealGroup)] +
                                            [КодНаимСд: dealNameCode] +
                                            [КодСторСд: taxpayerSideCode] +
                                            [ПрОпрЦен: mapYesNo.get(row.dealPriceSign)] +
                                            [КодОпрЦен: dealPriceCode] +
                                            [КолУчСд: row.dealMemberCount]
                            )
                            ДохРасхСд(
                                    [СумДохСд: row.income != null ? row.income : 0] +
                                            (row.incomeIncludingRegulation != null ? [СумДохСдРег: row.incomeIncludingRegulation] : [:]) +
                                            [СумРасхСд: row.outcome] +
                                            (row.outcomeIncludingRegulation != null ? [СумРасхСдРег: row.outcomeIncludingRegulation] : [:])
                            )
                            def String dealType = row.dealType != null ? getRefBookValue(64, row.dealType).CODE.numberValue : null
                            СвПредмСд(
                                    ТипПредСд: dealType
                            ) {
                                def String dealSubjectCode2 = row.dealSubjectCode2 != null ? getRefBookValue(68, row.dealSubjectCode2).CODE.numberValue : null
                                def String dealSubjectCode3 = row.dealSubjectCode3 != null ? getRefBookValue(34, row.dealSubjectCode3).CODE.stringValue : null
                                def String countryCode = row.countryCode != null ? getRefBookValue(10, row.countryCode).CODE.stringValue : null
                                def String deliveryCode = row.deliveryCode != null ? getRefBookValue(63, row.deliveryCode).STRCODE.stringValue : null
                                def String okeiCode = row.okeiCode != null ? getRefBookValue(12, row.okeiCode).CODE.stringValue : null
                                ПерПредСд(
                                        [НаимПредСд: row.dealSubjectName] +
                                                (dealSubjectCode2 != null ? [ОКП: dealSubjectCode2] : [:]) +
                                                [ОКВЭД: dealSubjectCode3] +
                                                [НомУчСд: row.otherNum] +
                                                [НомДог: row.contractNum] +
                                                (row.contractDate != null ? [ДатаДог: row.contractDate.format("dd.MM.yyyy")] : [:]) +
                                                (countryCode != null ? [ОКСМ: countryCode] : [:]) +
                                                (deliveryCode != null ? [КодУсловПост: deliveryCode] : [:]) +
                                                [ОКЕИ: okeiCode] +
                                                [Количество: row.count] +
                                                [ЦенаЕдин: row.price] +
                                                [СтоимИтог: row.total] +
                                                (row.dealDoneDate != null ? [ДатаСовСд: row.dealDoneDate.format("dd.MM.yyyy")] : [:])
                                ) {
                                    def String countryCode1 = row.countryCode1 != null ? getRefBookValue(10, row.countryCode1).CODE.stringValue : '000'
                                    def String region1 = row.region1 != null ? getRefBookValue(4, row.region1).CODE.stringValue : null
                                    МестОтпрТов(
                                            [ОКСМ: countryCode1] +
                                                    (region1 != null ? [КодРегион: region1] : [:]) +
                                                    (row.city1 != null ? [Город: row.city1] : [:]) +
                                                    (row.locality1 != null ? [НаселПункт: row.locality1] : [:])
                                    )
                                    def String countryCode2 = row.countryCode2 != null ? getRefBookValue(10, row.countryCode2).CODE.stringValue : '000'
                                    def String region2 = row.region2 != null ? getRefBookValue(4, row.region2).CODE.stringValue : null
                                    МестСовСд(
                                            [ОКСМ: countryCode2] +
                                                    (region2 != null ? [КодРегион: region2] : [:]) +
                                                    (row.city2 != null ? [Город: row.city2] : [:]) +
                                                    (row.locality2 != null ? [НаселПункт: row.locality2] : [:])
                                    )
                                }
                            }
                            def String organInfo = row.organInfo != null ? getRefBookValue(70, row.organInfo).CODE.numberValue : null
                            def String countryCode3 = row.countryCode3 != null ? getRefBookValue(10, row.countryCode3).CODE.stringValue : null
                            def String organName, organINN, organKPP, organRegNum, taxpayerCode, address
                            if (row.organName != null) {
                                def map = getRefBookValue(9, row.organName)
                                organName = map.NAME.stringValue
                                organINN = map.INN_KIO.stringValue
                                organKPP = map.KPP.numberValue
                                organRegNum = map.REG_NUM.stringValue
                                taxpayerCode = map.TAXPAYER_CODE.stringValue
                                address = map.ADDRESS.stringValue
                            }
                            СвОргУчаст(
                                    [НомПорСд: row.dealNum2] +
                                            [ПрОрг: organInfo] +
                                            [ОКСМ: countryCode3] +
                                            [НаимОрг: organName] +
                                            (organINN != null ? [ИННЮЛ: organINN] : [:]) +
                                            (organKPP != null ? [КПП: organKPP] : [:]) +
                                            (organRegNum != null ? [РегНомИн: organRegNum] : [:]) +
                                            (taxpayerCode != null ? [КодНПРег: taxpayerCode] : [:]) +
                                            (address != null ? [АдрИнТекст: address] : [:])
                            )
                        }
                    }
                }
            }
        }
    }
}

List<String> getErrorDepartment(record) {
    List<String> errorList = new ArrayList<String>()
    if (record.NAME.stringValue == null || record.NAME.stringValue.isEmpty()) {
        errorList.add("«Наименование подразделения»")
    }
    if (record.OKATO.referenceValue == null) {
        errorList.add("«Код по ОКАТО»")
    }
    if (record.INN.stringValue == null || record.INN.stringValue.isEmpty()) {
        errorList.add("«ИНН»")
    }
    if (record.KPP.stringValue == null || record.KPP.stringValue.isEmpty()) {
        errorList.add("«КПП»")
    }
    if (record.TAX_ORGAN_CODE.stringValue == null || record.TAX_ORGAN_CODE.stringValue.isEmpty()) {
        errorList.add("«Код налогового органа»")
    }
    if (record.OKVED_CODE.referenceValue == null) {
        errorList.add("«Код вида экономической деятельности и по классификатору ОКВЭД»")
    }
    if (record.NAME.stringValue == null || record.NAME.stringValue.isEmpty()) {
        errorList.add("«ИНН реорганизованного обособленного подразделения»")
    }
    if (record.SIGNATORY_ID.referenceValue == null) {
        errorList.add("«Признак лица подписавшего документ»")
    }
    if (record.SIGNATORY_SURNAME.stringValue == null || record.SIGNATORY_SURNAME.stringValue.isEmpty()) {
        errorList.add("«Фамилия подписанта»")
    }
    if (record.SIGNATORY_FIRSTNAME.stringValue == null || record.SIGNATORY_FIRSTNAME.stringValue.isEmpty()) {
        errorList.add("«Имя подписанта»")
    }
    if (record.APPROVE_DOC_NAME.stringValue == null || record.APPROVE_DOC_NAME.stringValue.isEmpty()) {
        errorList.add("«Наименование документа, подтверждающего полномочия представителя»")
    }
    if (record.TAX_PLACE_TYPE_CODE.referenceValue == null) {
        errorList.add("«Код места, по которому представляется документ»")
    }

    errorList
}

List<String> getErrorVersion(record) {
    List<String> errorList = new ArrayList<String>()
    if (record.FORMAT_VERSION.stringValue == null || !record.FORMAT_VERSION.stringValue.equals('5.01')) {
        errorList.add("«Версия формата»")
    }

    errorList
}

/**
 * Получение провайдера с использованием кеширования
 * @param providerId
 * @return
 */
def getProvider(def long providerId) {
    if (!providerCache.containsKey(providerId)) {
        providerCache.put(providerId, refBookFactory.getDataProvider(providerId))
    }
    return providerCache.get(providerId)
}

/**
 * Разыменование с использованием кеширования
 * @param refBookId
 * @param recordId
 * @return
 */
def getRefBookValue(def long refBookId, def long recordId) {
    if (!refBookCache.containsKey(recordId)) {
        refBookCache.put(recordId, refBookService.getRecordData(refBookId, recordId))
    }
    return refBookCache.get(recordId)
}
