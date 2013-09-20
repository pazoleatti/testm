package form_template.deal.notification

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.log.LogLevel

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

void checkDeparmentParams(LogLevel logLevel) {
    def departmentId = declarationData.departmentId

    // Параметры подразделения
    def refDataProvider = refBookFactory.getDataProvider(37)
    def departmentParam = refDataProvider.getRecords(new Date(), null, "DEPARTMENT_ID = '$departmentId'", null).get(0)

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

    def departmentId = declarationData.departmentId

    // Параметры подразделения
    def refDataProvider = refBookFactory.getDataProvider(37)
    def departmentParam = refDataProvider.getRecords(new Date(), null, "DEPARTMENT_ID = '$departmentId'", null).get(0)

    def formDataCollection = declarationService.getAcceptedFormDataSources(declarationData)

    def builder = new MarkupBuilder(xml)

    // Тип декларации - Уведомление
    def notificationType = 6
    // Код формы отчетности по КНД
    def String KND = '1110025'
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
                ОтчетГод: '' + reportPeriodService.get(declarationData.reportPeriodId).year,
                // Код налогового органа
                КодНО: departmentParam.TAX_ORGAN_CODE.stringValue,
                // Номер корректировки
                // TODO сделать на следующей версии
                НомКорр: '0',
                // Код места, по которому представляется документ
                ПоМесту: departmentParam.TAX_PLACE_TYPE_CODE.stringValue
        ) {
            СвНП(
                    ОКАТО: departmentParam.OKATO.stringValue,
                    ОКВЭД: departmentParam.OKVED_CODE.stringValue,
                    Тлф: departmentParam.PHONE.stringValue
            ) {
                НПЮЛ(
                        НаимОрг: departmentParam.NAME.stringValue,
                        ИННЮЛ: departmentParam.INN.stringValue,
                        КПП: departmentParam.KPP.stringValue
                ) {
                    reorgFormCode = departmentParam.REORG_FORM_CODE.stringValue
                    if (reorgFormCode != null && !reorgFormCode.equals('0')) {
                        СвРеоргЮЛ(
                                ФормРеорг: reorgFormCode,
                                ИННЮЛ: departmentParam.REORG_INN.stringValue,
                                КПП: departmentParam.REORG_KPP.stringValue)
                    }
                }
            }
            def prPodp = 1
            if (departmentParam.SIGNATORY_ID.referenceValue != null)
                prPodp = refBookService.getRecordData(35, departmentParam.SIGNATORY_ID.referenceValue).CODE.numberValue
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
                    def dataRowHelper = formDataService.getDataRowHelper(formDataCollection.getRecords().get(0))
                    // "Да"
                    def Long recYesId = null
                    // "Нет"
                    def Long recNoId = null
                    def valYes = refBookFactory.getDataProvider(38L).getRecords(new Date(), null, "CODE = 1", null)
                    def valNo = refBookFactory.getDataProvider(38L).getRecords(new Date(), null, "CODE = 0", null)
                    if (valYes != null && valYes.size() == 1)
                        recYesId = valYes.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
                    if (valNo != null && valNo.size() == 1)
                        recNoId = valNo.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
                    Map<Long, String> mapYesNo = new HashMap<Long, String>()
                    mapYesNo.put(recYesId, '1')
                    mapYesNo.put(recNoId, '0')
                    mapYesNo.put(null, '0')

                    for (row in dataRowHelper.getAllCached()) {
                        СвКонтрСд(
                                НомПорСд: row.dealNum1
                        ) {
                            def String interdependenceSing = row.interdependenceSing != null ? '' + refBookService.getRecordData(69, row.interdependenceSing).CODE.numberValue : null
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
                            def String dealNameCode = row.dealNameCode != null ? refBookService.getRecordData(67, row.dealNameCode).CODE.stringValue : null
                            def String taxpayerSideCode = row.taxpayerSideCode != null ? refBookService.getRecordData(65, row.taxpayerSideCode).CODE.stringValue : ''
                            while (taxpayerSideCode.length() < 3)
                                taxpayerSideCode = '0' + taxpayerSideCode
                            def String dealPriceCode = row.taxpayerSideCode != null ? '' + refBookService.getRecordData(66, row.dealPriceCode).CODE.numberValue : null
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
                                            [СумРасхСд: row.outcome != null ? row.outcome : 0] +
                                            (row.outcomeIncludingRegulation != null ? [СумРасхСдРег: row.outcomeIncludingRegulation] : [:])
                            )
                            def String dealType = row.dealType != null ? '' + refBookService.getRecordData(64, row.dealType).CODE.numberValue : null
                            СвПредмСд(
                                    ТипПредСд: dealType
                            ) {
                                def String dealSubjectCode2 = row.dealSubjectCode2 != null ? '' + refBookService.getRecordData(68, row.dealSubjectCode2).CODE.numberValue : null
                                def String dealSubjectCode3 = row.dealSubjectCode3 != null ? '' + refBookService.getRecordData(34, row.dealSubjectCode3).CODE.stringValue : null
                                def String countryCode = row.countryCode != null ? '' + refBookService.getRecordData(10, row.countryCode).CODE.numberValue : null
                                def String deliveryCode = row.deliveryCode != null ? refBookService.getRecordData(63, row.deliveryCode).STRCODE.stringValue : null
                                def String okeiCode = row.okeiCode != null ? '' + refBookService.getRecordData(12, row.okeiCode).CODE.stringValue : null
                                ПерПредСд(
                                        [НаимПредСд: row.dealSubjectName] +
                                                (dealSubjectCode2 != null ? [ОКП: dealSubjectCode2] : [:]) +
                                                [ОКВЭД: dealSubjectCode3] +
                                                [НомУчСд: row.otherNum] +
                                                [НомДог: row.contractNum] +
                                                [ДатаДог: row.contractDate.format("dd.MM.yyyy")] +
                                                (countryCode != null ? [ОКСМ: countryCode] : [:]) +
                                                (deliveryCode != null ? [КодУсловПост: deliveryCode] : [:]) +
                                                [ОКЕИ: okeiCode] +
                                                [Количество: row.count] +
                                                [ЦенаЕдин: row.price] +
                                                [СтоимИтог: row.total] +
                                                [ДатаСовСд: row.dealDoneDate.format("dd.MM.yyyy")]
                                ) {
                                    def String countryCode1 = row.countryCode1 != null ? '' + refBookService.getRecordData(10, row.countryCode1).CODE.stringValue : '000'
                                    def String region1 = row.region1 != null ? '' + refBookService.getRecordData(4, row.region1).CODE.stringValue : null
                                    МестОтпрТов(
                                            [ОКСМ: countryCode1] +
                                                    (region1 != null ? [КодРегион: region1] : [:]) +
                                                    (row.city1 != null ? [Город: row.city1] : [:]) +
                                                    (row.locality1 != null ? [НаселПункт: row.locality1] : [:])
                                    )
                                    def String countryCode2 = row.countryCode2 != null ? '' + refBookService.getRecordData(10, row.countryCode2).CODE.stringValue : '000'
                                    def String region2 = row.region2 != null ? '' + refBookService.getRecordData(4, row.region2).CODE.stringValue : null
                                    МестСовСд(
                                            [ОКСМ: countryCode2] +
                                                    [КодРегион: region2] +
                                                    (row.city2 != null ? [Город: row.city2] : [:]) +
                                                    (row.locality2 != null ? [НаселПункт: row.locality2] : [:])
                                    )
                                }
                            }
                            def String organInfo = row.organInfo != null ? '' + refBookService.getRecordData(4, row.organInfo).VALUE.numberValue : null
                            def String countryCode3 = row.countryCode3 != null ? '' + refBookService.getRecordData(10, row.countryCode3).CODE.stringValue : null
                            def String organName, organINN, organKPP, organRegNum, taxpayerCode, address
                            if (row.organName != null) {
                                def map = refBookService.getRecordData(9, row.organName)
                                organName = map.NAME.stringValue
                                organINN = '' + map.INN_KIO.stringValue
                                organKPP = '' + map.KPP.numberValue
                                organRegNum = map.REG_NUM.stringValue
                                taxpayerCode = map.TAXPAYER_CODE.stringValue
                                address = map.ADDRESS.stringValue
                            }
                            СвОргУчаст(
                                    НомПорСд: row.dealNum2,
                                    ПрОрг: organInfo,
                                    ОКСМ: countryCode3,
                                    НаимОрг: organName,
                                    ИННЮЛ: organINN,
                                    КПП: organKPP,
                                    РегНомИн: organRegNum,
                                    КодНПРег: taxpayerCode,
                                    АдрИнТекст: address
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
    if (record.REORG_KPP.stringValue == null || record.REORG_KPP.stringValue.isEmpty()) {
        errorList.add("«КПП реорганизованного обособленного подразделения»")
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
    if (record.APP_VERSION.stringValue == null || !record.APP_VERSION.stringValue.equals('XLR_FNP_TAXCOM_5_01')) {
        errorList.add("«Версия программы, с помощью которой сформирован файл»")
    }

    errorList
}

