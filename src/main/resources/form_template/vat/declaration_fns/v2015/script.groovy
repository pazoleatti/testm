package form_template.vat.declaration_fns.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import groovy.transform.Field
import groovy.xml.MarkupBuilder

/**
 * Декларация по НДС (раздел 1-7)
 *
 * совпадает с "Декларация по НДС (короткая, раздел 1-7)", кроме заполнения секции "РАЗДЕЛ 2"
 *
 * declarationTemplateId=2004
 */

@Field
def boolean shortDeclaration = false;

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkDepartmentParams(LogLevel.WARNING)
        break
    case FormDataEvent.CHECK:
        checkDepartmentParams(LogLevel.ERROR)
        logicCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:
        checkDepartmentParams(LogLevel.ERROR)
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
        checkDepartmentParams(LogLevel.WARNING)
        generateXML()
        break
    default:
        return
}

@Field
def providerCache = [:]
@Field
def refBookCache = [:]

@Field
def empty = 0

// Параметры подразделения
@Field
def departmentParam = null

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

def getEndDate() {
    if (reportPeriodEndDate == null) {
        reportPeriodEndDate = reportPeriodService.getEndDate(declarationData.reportPeriodId)?.time
    }
    return reportPeriodEndDate
}

// Разыменование с использованием кеширования
def getRefBookValue(def long refBookId, def recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Получение провайдера с использованием кеширования
def getProvider(def long providerId) {
    if (!providerCache.containsKey(providerId)) {
        providerCache.put(providerId, refBookFactory.getDataProvider(providerId))
    }
    return providerCache.get(providerId)
}

// Мапа соответсвтия id и наименований деклараций 8-11
def declarations() {
    [
            declaration8 : [12, 'Декларация по НДС (раздел 8)'],
            declaration81: [13, 'Декларация по НДС (раздел 8.1)'],
            declaration9 : [14, 'Декларация по НДС (раздел 9)'],
            declaration91: [15, 'Декларация по НДС (раздел 9.1)'],
            declaration10: [16, 'Декларация по НДС (раздел 10)'],
            declaration11: [17, 'Декларация по НДС (раздел 11)']
    ]
}

/**
 *  Структура для хранения данных о декларациях 8-11
 *  (id, название декларации, признак существования, признак принятости, имя файла)
 */
@Field
def Map<Long, Expando> declarationParts = null

void checkDepartmentParams(LogLevel logLevel) {
    // Параметры подразделения
    def departmentParam = getDepartmentParam()

    // Проверки подразделения
    def List<String> errorList = getErrorDepartment(departmentParam)
    for (String error : errorList) {
        logger.log(logLevel, String.format("Для данного подразделения на форме настроек подразделений отсутствует значение атрибута %s!", error))
    }
    errorList = getErrorVersion(departmentParam)
    for (String error : errorList) {
        logger.log(logLevel, String.format("Неверно указано значение атрибута %s на форме настроек подразделений", error))
    }
}

// Получить параметры подразделения
def getDepartmentParam() {
    if (departmentParam == null) {
        def departmentId = declarationData.departmentId
        def departmentParamList = getProvider(RefBook.DEPARTMENT_CONFIG_VAT).getRecords(getEndDate() - 1, null, "DEPARTMENT_ID = $departmentId", null)
        if (departmentParamList == null || departmentParamList.size() == 0 || departmentParamList.get(0) == null) {
            throw new Exception("Ошибка при получении настроек обособленного подразделения")
        }
        departmentParam = departmentParamList?.get(0)
    }
    return departmentParam
}

List<String> getErrorDepartment(record) {
    List<String> errorList = new ArrayList<String>()
    if (record.NAME?.stringValue == null || record.NAME.stringValue.isEmpty()) {
        errorList.add("«Наименование подразделения»")
    }
    if (record.OKTMO?.referenceValue == null) {
        errorList.add("«ОКТМО»")
    }
    if (record.INN?.stringValue == null || record.INN.stringValue.isEmpty()) {
        errorList.add("«ИНН»")
    }
    if (record.KPP?.stringValue == null || record.KPP.stringValue.isEmpty()) {
        errorList.add("«КПП»")
    }
    if (record.TAX_ORGAN_CODE?.stringValue == null || record.TAX_ORGAN_CODE.stringValue.isEmpty()) {
        errorList.add("«Код налогового органа»")
    }
    if (record.OKVED_CODE?.referenceValue == null) {
        errorList.add("«Код вида экономической деятельности и по классификатору ОКВЭД»")
    }
    if (record.SIGNATORY_ID?.referenceValue == null) {
        errorList.add("«Признак лица подписавшего документ»")
    }
    if (record.SIGNATORY_SURNAME?.stringValue == null || record.SIGNATORY_SURNAME.stringValue.isEmpty()) {
        errorList.add("«Фамилия подписанта»")
    }
    if (record.SIGNATORY_FIRSTNAME?.stringValue == null || record.SIGNATORY_FIRSTNAME.stringValue.isEmpty()) {
        errorList.add("«Имя подписанта»")
    }
    //Если ПрПодп (не пусто или не 1) и значение атрибута на форме настроек подразделений не задано
    if ((record.SIGNATORY_ID?.referenceValue != null && getRefBookValue(35, record.SIGNATORY_ID?.value)?.CODE?.value != 1)
            && (record.APPROVE_DOC_NAME?.stringValue == null || record.APPROVE_DOC_NAME.stringValue.isEmpty())) {
        errorList.add("«Наименование документа, подтверждающего полномочия представителя»")
    }
    if (record.TAX_PLACE_TYPE_CODE?.referenceValue == null) {
        errorList.add("«Код места, по которому представляется документ»")
    }
    def reorgFormCode = getRefBookValue(5, record?.REORG_FORM_CODE?.value)?.CODE?.value
    if (reorgFormCode != null && reorgFormCode != '0') {
        if (record.REORG_INN?.value == null || record.REORG_INN.value.isEmpty()) {
            errorList.add("«ИНН реорганизованной организации»")
        }
        if (record.REORG_KPP?.value == null || record.REORG_KPP.value.isEmpty()) {
            errorList.add("«КПП реорганизованной организации»")
        }
    }
    errorList
}

List<String> getErrorVersion(record) {
    List<String> errorList = new ArrayList<String>()
    if (record.FORMAT_VERSION.stringValue == null || !record.FORMAT_VERSION.stringValue.equals('5.04')) {
        errorList.add("«Версия формата»")
    }
    errorList
}

void generateXML() {
    // атрибуты, заполняемые по настройкам подразделений
    def departmentParam = getDepartmentParam()
    def taxOrganCode = departmentParam?.TAX_ORGAN_CODE?.value
    def okvedCode = getRefBookValue(34, departmentParam?.OKVED_CODE?.value)?.CODE?.value
    def okato = getOkato(departmentParam?.OKTMO?.value)
    def taxPlaceTypeCode = getRefBookValue(2, departmentParam?.TAX_PLACE_TYPE_CODE?.value)?.CODE?.value
    def signatoryId = getRefBookValue(35, departmentParam?.SIGNATORY_ID?.value)?.CODE?.value
    def phone = departmentParam?.PHONE?.value
    def name = departmentParam?.NAME?.value
    def inn = departmentParam?.INN?.value
    def kpp = departmentParam?.KPP?.value
    def formatVersion = departmentParam?.FORMAT_VERSION?.value
    def surname = departmentParam?.SIGNATORY_SURNAME?.value
    def firstname = departmentParam?.SIGNATORY_FIRSTNAME?.value
    def lastname = departmentParam?.SIGNATORY_LASTNAME?.value
    def approveDocName = departmentParam?.APPROVE_DOC_NAME?.value
    def approveOrgName = departmentParam?.APPROVE_ORG_NAME?.value
    def reorgINN = departmentParam?.REORG_INN?.value
    def reorgKPP = departmentParam?.REORG_KPP?.value
    def reorgFormCode = departmentParam?.REORG_FORM_CODE?.referenceValue
    def prPodp = (signatoryId != null ? signatoryId : 1)

    def sign812 = hasOneOrMoreDeclaration()
    def sign8 = isDeclarationExist(declarations().declaration8[0])
    def sign81 = isDeclarationExist(declarations().declaration81[0])
    def sign9 = isDeclarationExist(declarations().declaration9[0])
    def sign91 = isDeclarationExist(declarations().declaration91[0])
    def sign10 = isDeclarationExist(declarations().declaration10[0])
    def sign11 = isDeclarationExist(declarations().declaration11[0])
    def sign12 = 0

    def nameDecl8 = getDeclarationFileName(declarations().declaration8[0])
    def nameDecl81 = getDeclarationFileName(declarations().declaration81[0])
    def nameDecl9 = getDeclarationFileName(declarations().declaration9[0])
    def nameDecl91 = getDeclarationFileName(declarations().declaration91[0])
    def nameDecl10 = getDeclarationFileName(declarations().declaration10[0])
    def nameDecl11 = getDeclarationFileName(declarations().declaration11[0])

    def period = 0
    if (reorgFormCode != null) {
        def values = [51, 54, 53, 56]
        period = values[reportPeriod.order - 1]
    } else if (reportPeriod.order != null) {
        def values = [21, 22, 23, 24]
        period = values[reportPeriod.order - 1]
    }

    // Список данных форм-источников
    def formDataList = declarationService.getAcceptedFormDataSources(declarationData).getRecords()
    // Тип формы > Строки формы
    def dataRowsMap = [:]
    for (def formData : formDataList) {
        def dataRows = formDataService.getDataRowHelper(formData)?.getAll()
        dataRowsMap.put(formData.formType.id, dataRows)
    }

    // РАЗДЕЛ 3
    // форма 724.1
    def rows724_1 = dataRowsMap[600]
    // форма 724.4
    def rows724_4 = dataRowsMap[603]

    // TODO Вопрос к заказчику, пока не заполняем
    def nalBaza105 = empty
    def sumNal105 = empty
    def nalBaza106 = empty
    def sumNal106 = empty
    def nalBaza107 = empty
    def sumNal107 = empty
    def nalBaza108 = empty
    def sumNal108 = empty

    /** НалБаза (РеалТов18). Код строки 010 Графа 3. */
    def nalBaza010 = empty
    /** СумНал (РеалТов18). Код строки 010 Графа 5. */
    def sumNal010 = empty
    /** НалБаза (РеалТов10). Код строки 020 Графа 3. */
    def nalBaza020 = empty
    /** СумНал (РеалТов10). Код строки 020 Графа 5. */
    def sumNal020 = empty
    /** НалБаза (РеалТов118). Код строки 030 Графа 3. */
    def nalBaza030 = empty
    /** СумНал (РеалТов118). Код строки 030 Графа 5. */
    def sumNal030 = empty
    /** НалБаза (РеалТов110). Код строки 040 Графа 3. */
    def nalBaza040 = empty
    /** СумНал (РеалТов110). Код строки 040 Графа 5. */
    def sumNal040 = empty
    /** НалБаза (ОплПредПост). Код строки 070 Графа 3. */
    def nalBaza070 = empty
    /** СумНал (ОплПредПост). Код строки 070 Графа 5. */
    def sumNal070 = empty
    /** НалИсчПрод. Код строки 170 Графа 3. */
    def nalIschProd = empty
    if (rows724_1) {
        // TODO перепроверить алиасы после http://jira.aplana.com/browse/SBRFACCTAX-10404
        def row = getDataRow(rows724_1, 'total_1')
        def tmp = (row?.baseSum ?: empty)
        nalBaza010 = round(tmp)
        tmp = (row?.ndsSum ?: empty)
        sumNal010 = round(tmp)

        row = getDataRow(rows724_1, 'total_2')
        nalBaza020 = round(row?.baseSum ?: empty)
        sumNal020 = round(row?.ndsSum ?: empty)

        row = getDataRow(rows724_1, 'total_3')
        nalBaza030 = round(row?.baseSum ?: empty)
        sumNal030 = round(row?.ndsSum ?: empty)

        row = getDataRow(rows724_1, 'total_4')
        nalBaza040 = round(row?.baseSum ?: empty)
        sumNal040 = round(row?.ndsSum ?: empty)

        row = getDataRow(rows724_1, 'total_5')
        nalBaza070 = round(row?.baseSum ?: empty)
        sumNal070 = round(row?.ndsSum ?: empty)

        nalIschProd = round(getDataRow(rows724_1, 'total_7')?.nds)
    }

    /** НалПредНППриоб . Код строки 120 Графа 3. */
    def nalPredNPPriob = empty
    if (rows724_4) {
        // TODO перепроверить алиасы после http://jira.aplana.com/browse/SBRFACCTAX-10404
        nalPredNPPriob = getDataRow(rows724_4, 'total1')?.sum2
    }

    /** НалВосстОбщ. Код строки 110 Графа 5. */
    def nalVosstObsh = sumNal010 + sumNal020 + sumNal030 + sumNal040 + sumNal070 + sumNal105 + sumNal106 + sumNal107 + sumNal108
    /** НалВычОбщ. Код строки 190 Графа 5. */
    def nalVichObsh = nalPredNPPriob + nalIschProd
    /** НалПУ164. Код строки 200 и код строки 210.*/
    def nalPU164 = nalVosstObsh - nalVichObsh

    def builder = new MarkupBuilder(xml)
    builder.Файл(
            ИдФайл: declarationService.generateXmlFileId(declarationType, declarationData.departmentReportPeriodId, declarationData.taxOrganCode, declarationData.kpp),
            ВерсПрог: applicationVersion,
            ВерсФорм: formatVersion,
            'ПризнНал8-12': sign812,
            ПризнНал8: sign8,
            ПризнНал81: sign81,
            ПризнНал9: sign9,
            ПризнНал91: sign91,
            ПризнНал10: sign10,
            ПризнНал11: sign11,
            ПризнНал12: sign12) {
        Документ(
                // ТИТУЛЬНЫЙ ЛИСТ
                // Номер корректировки
                НомКорр: reportPeriodService.getCorrectionNumber(declarationData.departmentReportPeriodId),
                // Налоговый период (код)
                Период: period,
                // Отчетный год
                ОтчетГод: reportPeriodService.get(reportPeriodId).taxPeriod.year,
                // Код налогового органа
                КодНО: taxOrganCode,
                // Код места, по которому представляется документ
                ПоМесту: taxPlaceTypeCode,
                // Дата формирования документа
                ДатаДок: (docDate != null ? docDate : new Date()).format("dd.MM.yyyy")
        ) {
            // ТИТУЛЬНЫЙ ЛИСТ
            СвНП(
                    ОКВЭД: okvedCode,
                    Тлф: phone
            ) {
                НПЮЛ(
                        НаимОрг: name,
                        ИННЮЛ: inn,
                        КПП: kpp
                ) {
                    reorgFormCode = reorgFormCode != null ? getRefBookValue(5, reorgFormCode).CODE.stringValue : null
                    def boolean isReorg = reorgFormCode != null && !reorgFormCode.equals('0')

                    if (reorgFormCode != null) {
                        СвРеоргЮЛ(
                                [ФормРеорг: reorgFormCode] +
                                        (isReorg ? [ИННЮЛ: reorgINN] : [:]) +
                                        (isReorg ? [КПП: reorgKPP] : [:])
                        )
                    }
                }
            }

            Подписант(ПрПодп: prPodp) {
                ФИО(
                        [Фамилия: surname] +
                                [Имя: firstname] +
                                (lastname != null && !lastname.isEmpty() ? [Отчество: lastname] : [:])
                )
                if (prPodp == 2) {
                    СвПред(
                            НаимДок: approveDocName,
                            НаимОрг: approveOrgName
                    )
                }
            }

            // РАЗДЕЛ 1
            СумУплНП(
                    ОКТМО: okato,
                    КБК: '18210301000011000110',
                    'СумПУ_173.5': empty,
                    'СумПУ_173.1': nalPU164,
                    НомДогИТ: null,
                    ДатаНачДогИТ: null,
                    ДатаКонДогИТ: null
            )

            // РАЗДЕЛ 2
            if (!shortDeclaration) {
                for (def row : getSection2Rows(dataRowsMap)) {
                    СумУплНА(
                            КППИно: null,
                            КБК: '18210301000011000110',
                            ОКТМО: okato,
                            СумИсчисл: row.sumIschisl,
                            КодОпер: row.codeOper,
                            СумИсчислОтгр: null,
                            СумИсчислОпл: null,
                            СумИсчислНА: null
                    ) {
                        СведПродЮЛ(
                                НаимПрод: row.naimProd,
                                ИННЮЛПрод: row.innULProd
                        )
                    }
                }
            }

            // РАЗДЕЛ 3
            СумУпл164(
                    НалПУ164: nalPU164
            ) {
                СумНалОб(
                        НалВосстОбщ: nalVosstObsh
                ) {
                    РеалТов18(
                            НалБаза: nalBaza010,
                            СумНал: sumNal010
                    )
                    РеалТов10(
                            НалБаза: nalBaza020,
                            СумНал: sumNal020
                    )

                    РеалТов118(
                            НалБаза: nalBaza030,
                            СумНал: sumNal030
                    )
                    РеалТов110(
                            НалБаза: nalBaza040,
                            СумНал: sumNal040
                    )
                    РеалПредИК(
                            НалБаза: empty,
                            СумНал: empty
                    )
                    ВыпСМРСоб(
                            НалБаза: empty,
                            СумНал: empty
                    )
                    ОплПредПост(
                            НалБаза: nalBaza070,
                            СумНал: sumNal070
                    )
                    СумНалВосст(
                            СумНалВс: empty,
                            СумНал170: empty,
                            СумНалСтав0: empty
                    )
                    КорРеалТов18(
                            НалБаза: nalBaza105,
                            СумНал: sumNal105
                    )
                    КорРеалТов10(
                            НалБаза: nalBaza106,
                            СумНал: sumNal106
                    )
                    КорРеалТов118(
                            НалБаза: nalBaza107,
                            СумНал: sumNal107
                    )
                    КорРеалТов110(
                            НалБаза: nalBaza108,
                            СумНал: sumNal108
                    )
                    КорРеалПредИК(
                            НалБаза: empty,
                            СумНал: empty
                    )
                }
                СумНалВыч(
                        НалПредНППриоб: nalPredNPPriob,
                        НалПредНППок: empty,
                        НалИсчСМР: empty,
                        НалУплТамож: empty,
                        НалУплНОТовТС: empty,
                        НалИсчПрод: nalIschProd,
                        НалУплПокНА: empty,
                        НалВычОбщ: nalVichObsh
                )
            }

            // РАЗДЕЛ 4
            // TODO вопрос к заказчику о заполнении атрибутов: НалБаза, НалВосст, КорНалБазаУв, КорНалБазаУм
            if (dataRowsMap[602]) {
                НалПодтв0(
                        СумИсчислИтог: empty
                ) {
                    // форма 724.2.2
                    // TODO перепроверить алиасы после http://jira.aplana.com/browse/SBRFACCTAX-10404
                    for (def row : dataRowsMap[602]) {
                        if (row.getAlias() == 'itog') {
                            continue
                        }
                        СумОпер4(
                                КодОпер: row.code,
                                НалБаза: round(row.base),
                                НалВычПод: empty,
                                НалНеПод: empty,
                                НалВосст: empty
                        )
                        СумОпер1010447(
                                КодОпер: '1010447',
                                НалБаза: empty,
                                НалВосст: empty
                        )
                        СумОпер1010448(
                                КодОпер: '1010448',
                                КорНалБазаУв: empty,
                                КорНалБазаУм: empty
                        )
                    }
                }
            }

            // РАЗДЕЛ 7
            if (dataRowsMap[601]) {
                ОперНеНал(
                        ОплПостСв6Мес: empty
                ) {
                    // форма 724.2.1
                    // TODO перепроверить алиасы после http://jira.aplana.com/browse/SBRFACCTAX-10404
                    for (def row : dataRowsMap[601]) {
                        if (row.getAlias() == 'itog') {
                            continue
                        }
                        СумОпер7(
                                КодОпер: row.code,
                                СтРеалТов: round(row.realizeCost),
                                СтПриобТов: round(row.obtainCost ?: empty),
                                НалНеВыч: round(getNalNeVich(row))
                        )
                    }
                }
            }

            КнигаПокуп(НаимКнПок: nameDecl8)
            КнигаПокупДЛ(НаимКнПокДЛ: nameDecl81)
            КнигаПрод(НаимКнПрод: nameDecl9)
            КнигаПродДЛ(НаимКнПродДЛ: nameDecl91)
            ЖУчВыстСчФ(НаимЖУчВыстСчФ: nameDecl10)
            ЖУчПолучСчФ(НаимЖУчПолучСчФ: nameDecl11)
        }
    }
}

// Логические проверки
void logicCheck() {
    def xmlString = declarationService.getXmlData(declarationData.id)
    if (xmlString == null) {
        return
    }
    xmlString = xmlString.replace('<?xml version="1.0" encoding="windows-1251"?>', '')
    def xmlData = new XmlSlurper().parseText(xmlString)

    // 1. Существующие экземпляры декларации по НДС (раздел 8/8.1/9/9.1/10/11) текущего периода и подразделения находятся в состоянии «Принята»
    def reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
    declarations().each { id, name ->
        def declarationData = declarationService.getLast(id, declarationData.departmentId, reportPeriod.id)
        if (declarationData != null && !declarationData.accepted) {
            logger.error("Экземпляр декларации вида «$name» текущего периода и подразделения не находится в состоянии «Принята»!")
        }
    }

    // 2. Атрибуты признаки наличия разделов 8-11 заполнены согласно алгоритмам
    def checkMap = [
            'Признак наличия разделов с 8 по 12'
            : [xmlData.Файл.@'ПризнНал8-12'.text() as BigDecimal, hasOneOrMoreDeclaration()],
            'Признак наличия сведений из книги покупок об операциях, отражаемых за истекший налоговый период'
            : [getXmlValue(xmlData.Файл.@ПризнНал8).text() as BigDecimal, isDeclarationExist(declarations().declaration8[0])],
            'Признак наличия сведений из дополнительного листа книги покупок'
            : [getXmlValue(xmlData.Файл.@ПризнНал81).text() as BigDecimal, isDeclarationExist(declarations().declaration81[0])],
            'Признак наличия сведений из книги продаж об операциях, отражаемых за истекший налоговый период'
            : [getXmlValue(xmlData.Файл.@ПризнНал9).text() as BigDecimal, isDeclarationExist(declarations().declaration9[0])],
            'Признак наличия сведений из дополнительного листа книги продаж'
            : [getXmlValue(xmlData.Файл.@ПризнНал91).text() as BigDecimal, isDeclarationExist(declarations().declaration91[0])],
            'Признак наличия сведений из журнала учета выставленных счетов-фактур в отношении операций, осуществляемых в интересах другого лица на основе договоров комиссии, агентских договоров или на основе договоров транспортной экспедиции, отражаемых за истекший налоговый период'
            : [getXmlValue(xmlData.Файл.@ПризнНал10).text() as BigDecimal, isDeclarationExist(declarations().declaration10[0])],
            'Признак наличия сведений из журнала учета полученных счетов-фактур в отношении операций, осуществляемых в интересах другого лица на основе договоров комиссии, агентских договоров или на основе договоров транспортной экспедиции, отражаемых за истекший налоговый период'
            : [getXmlValue(xmlData.Файл.@ПризнНал11).text() as BigDecimal, isDeclarationExist(declarations().declaration11[0])],
            'Признак наличия сведений из счетов-фактур, выставленных лицами, указанными в пункте 5 статьи 173 Налогового кодекса Российской Федерации'
            : [getXmlValue(xmlData.Файл.@ПризнНал12).text() as BigDecimal, 0]

    ]
    checkMap.each { key, value ->
        if (value[0] != value[1]) {
            logger.error("Атрибут «$key» файла формата xml заполнен неверно! Для исправления необходимо пересчитать данные декларации (кнопка «Рассчитать»).")
        }
    }
}

// Заполнение мапы с данными о декларациях 8-11
def Map<Long, Expando> getParts() {
    if (declarationParts == null) {
        def reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
        declarations().each { id, name ->
            def declarationData = declarationService.getLast(id, declarationData.departmentId, reportPeriod.id)

            def result = new Expando()
            result.id = id
            result.name = name
            result.exist = (declarationData != null)
            result.accepted = (declarationData?.accepted)

            if (result.exist) {
                def String xmlString = declarationService.getXmlData(declarationData.id)
                xmlString = xmlString?.replace('<?xml version="1.0" encoding="windows-1251"?>', '')
                if (xmlString) {
                    result.fileName = new XmlSlurper().parseText(xmlString).Файл.@ИдФайл
                }
            }
            declarationParts[id] = result
        }
    }
    return declarationParts
}

def String getDeclarationFileName(def declarationId) {
    return getParts().get(declarationId)?.fileName ?: ''
}

def BigDecimal isDeclarationExist(def declarationId) {
    return getParts().get(declarationId)?.exist ? 1 : 0
}

def BigDecimal hasOneOrMoreDeclaration() {
    declarations().each { id, name ->
        if (isDeclarationExist(id) == 1) {
            return 1
        }
    }
    return 0
}

/**
 * Получить список значений для раздела 2.
 *
 * @param dataRowsMap мапа со строками форм-источников
 * @return список мап со значениями
 */
def getSection2Rows(def dataRowsMap) {
    def rows = []
    // форма 724.6
    for (def row : dataRowsMap[604]) {
        if (row.getAlias() != null) {
            continue
        }
        def newRow = [:]
        newRow.naimProd = row.contragent
        newRow.innULProd = null
        newRow.sumIschisl = round(row.sum2)
        newRow.codeOper = '1011712'
        rows.add(newRow)
    }
    // форма 724.7
    for (def row : dataRowsMap[605]) {
        if (row.getAlias() != null) {
            continue
        }
        def newRow = [:]
        newRow.naimProd = row.name
        newRow.innULProd = row.inn
        newRow.sumIschisl = round(row.ndsSum)
        newRow.codeOper = '1011703'
        rows.add(newRow)
    }
    return rows
}

def round(def value) {
    return ((BigDecimal) value)?.setScale(0, BigDecimal.ROUND_HALF_UP)
}

@Field
def specialCode = '1010276'

@Field
def opuCodes = ['26411.01', '26411.02']

@Field
def knuCodes = ['20860', '20870']

/**
 * Получить значение для НалНеВыч.
 *
 * @param row строка формы 724.2.1
 */
def getNalNeVich(def row) {
    def order = reportPeriodService.get(declarationData.reportPeriodId)?.order
    if (row.code == specialCode) {
        // сумма кодов ОПУ из отчета 102
        def sumOpu = getSumByOpuCodes(opuCodes)
        if (order == 1) {
            return sumOpu
        } else {
            // сумма из расходов простых
            def sumOutcome = getSumOutcomeSimple(knuCodes)
            // разность сумм
            return sumOpu - sumOutcome
        }
    } else {
        return empty
    }
}


def getSumOutcomeSimple(def knuCodes) {
    def tmp = 0
    // получаем период из прибыли соотвествующий текущему периоду НДС
    def List<ReportPeriod> periodList = reportPeriodService.getReportPeriodsByDate(TaxType.INCOME, getEndDate(), getEndDate())
    if (periodList.isEmpty()) {
        return 0
    }
    // получаем предыдущий период по прибыли
    def reportPeriodPrevIncome = reportPeriodService.getPrevReportPeriod(periodList.get(0).id)
    if (reportPeriodPrevIncome?.id == null) {
        return 0
    }
    def formDataSimple = getFormDataSimple(reportPeriodPrevIncome.id)
    def dataRowsSimple = (formDataSimple ? formDataService.getDataRowHelper(formDataSimple)?.getAll() : null)
    for (def row : dataRowsSimple) {
        if (row.consumptionTypeId in knuCodes) {
            tmp += row.rnu5Field5Accepted
        }
    }
    return tmp
}

/**
 * Получить данные формы "расходы простые" (id = 304)
 */
def getFormDataSimple(def reportPeriodId) {
    return formDataService.getLast(304, FormDataKind.SUMMARY, declarationData.departmentId, reportPeriodId, null)
}