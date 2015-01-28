package form_template.income.declaration_op_1.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field
import groovy.xml.MarkupBuilder

/**
 * Декларация по налогу на прибыль (ОП)
 * Формирование XML для декларации налога на прибыль уровня обособленного подразделения.
 *
 * declarationTemplateId=21068
 *
 * @author rtimerbaev
 */

// Признак новой декларации (http://jira.aplana.com/browse/SBRFACCTAX-8910)
@Field
def boolean newDeclaration = true;

switch (formDataEvent) {
    case FormDataEvent.CREATE : // создать / обновить
        checkDepartmentParams(LogLevel.WARNING)
        checkDeclarationBank()
        break
    case FormDataEvent.CHECK : // проверить
        checkDepartmentParams(LogLevel.ERROR)
        break
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED : // принять из создана
        checkDepartmentParams(LogLevel.ERROR)
        break
    case FormDataEvent.CALCULATE:
        checkDepartmentParams(LogLevel.WARNING)
        def xmlBankData = checkDeclarationBank()
        generateXML(xmlBankData)
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
@Field
def recordCache = [:]

// значение подразделения из справочника 33
@Field
def departmentParam = null

// значение подразделения из справочника 330 (таблица)
@Field
def departmentParamTable = null

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

def getEndDate() {
    if (reportPeriodEndDate == null) {
        reportPeriodEndDate = reportPeriodService.getEndDate(declarationData.reportPeriodId)?.time
    }
    return reportPeriodEndDate
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

void checkDepartmentParams(LogLevel logLevel) {

    // Параметры подразделения
    def departmentParam = getDepartmentParam()
    def departmentParamIncomeRow = getDepartmentParamTable(departmentParam.record_id.value)

    // Проверки подразделения
    def List<String> errorList = getErrorTable(departmentParamIncomeRow)
    for (String error : errorList) {
        logger.log(logLevel, String.format("Для данного подразделения на форме настроек подразделений отсутствует значение атрибута %s!", error))
    }
    errorList = getErrorDepartment(departmentParam)
    for (String error : errorList) {
        logger.log(logLevel, String.format("Для данного подразделения на форме настроек подразделений отсутствует значение атрибута %s!", error))
    }

    errorList = getErrorVersion(departmentParam)
    for (String error : errorList) {
        logger.log(logLevel, String.format("Неверно указано значение атрибута %s на форме настроек подразделений!", error))
    }
}

// Провека декларации банка.
def checkDeclarationBank() {
    /** Отчётный период. */
    def reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)

    /** вид декларации 2 - декларация по налогу на прибыль уровня банка, 9 - новая декларация банка */
    def declarationTypeId = ((newDeclaration) ? 9 : 2)

    /** Идентификатор подразделения Банка. */
    def departmentBankId = 1
    def bankDeclarationData = declarationService.getLast(declarationTypeId, departmentBankId, reportPeriod.id)
    if (bankDeclarationData == null || !bankDeclarationData.accepted) {
        logger.error('Декларация Банка по прибыли за указанный период не сформирована или не находится в статусе "Принята".')
        return
    }

    /** XML декларации за предыдущий отчетный период. */
    def xmlBankData = null

    if (bankDeclarationData.id != null) {
        def xmlString = declarationService.getXmlData(bankDeclarationData.id)
        xmlString = xmlString?.replace('<?xml version="1.0" encoding="windows-1251"?>', '')
        if (!xmlString) {
            logger.error('Данные декларации Банка не заполнены.')
            return
        }
        xmlBankData = new XmlSlurper().parseText(xmlString)
    }
    if (xmlBankData == null) {
        logger.error('Не удалось получить данные декларации Банка.')
    }
    return xmlBankData
}

/** Запуск генерации XML. */
void generateXML(def xmlBankData) {
    /*
     * Константы.
     */

    def empty = 0
    def knd = '1151006'
    def kbk = '18210101011011000110'
    def kbk2 = '18210101012021000110'
    def typeNP = '1'

    // справочник "Параметры подразделения по налогу на прибыль" - начало
    def incomeParams = getProvider(33).getRecords(getEndDate() - 1, null, "DEPARTMENT_ID = ${declarationData.departmentId}", null)?.get(0)
    if (incomeParams == null) {
        throw new Exception('Ошибка при получении настроек обособленного подразделения')
    }

    def filter = "LINK = $departmentParamId and KPP ='${declarationData.kpp}'"
    def incomeParamsTable = getProvider(330).getRecords(getEndDate() - 1, null, filter, null)?.get(0)
    if (incomeParamsTable == null) {
        throw new Exception('Ошибка при получении настроек обособленного подразделения!')
    }

    if (!xmlBankData)
        return
    def reorgFormCode = getRefBookValue(5, incomeParamsTable?.REORG_FORM_CODE?.value)?.CODE?.value
    def taxOrganCode = incomeParamsTable?.TAX_ORGAN_CODE?.value
    def okvedCode = getRefBookValue(34, incomeParamsTable?.OKVED_CODE?.value)?.CODE?.value
    def phone = incomeParamsTable?.PHONE?.value
    def name = incomeParamsTable?.NAME?.value
    def inn = incomeParams?.INN?.value
    def kpp = declarationData.kpp
    def reorgInn = incomeParamsTable?.REORG_INN?.value
    def reorgKpp = incomeParamsTable?.REORG_KPP?.value
    def oktmo = getOkato(incomeParamsTable?.OKTMO?.value)
    def signatoryId = getRefBookValue(35, incomeParamsTable?.SIGNATORY_ID?.value)?.CODE?.value
    def formatVersion = incomeParamsTable?.FORMAT_VERSION?.value
    def taxPlaceTypeCode = getRefBookValue(2, incomeParamsTable?.TAX_PLACE_TYPE_CODE?.value)?.CODE?.value
    def signatorySurname = incomeParamsTable?.SIGNATORY_SURNAME?.value
    def signatoryFirstName = incomeParamsTable?.SIGNATORY_FIRSTNAME?.value
    def signatoryLastName = incomeParamsTable?.SIGNATORY_LASTNAME?.value
    def approveDocName = incomeParamsTable?.APPROVE_DOC_NAME?.value
    def approveOrgName = incomeParamsTable?.APPROVE_ORG_NAME?.value
    // справочник "Параметры подразделения по налогу на прибыль" - конец

    /** Отчётный период. */
    def reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)

    /** Налоговый период. */
    def taxPeriod = (reportPeriod != null ? reportPeriod.getTaxPeriod() : null)

    /** Признак налоговый ли это период. */
    def isTaxPeriod = (reportPeriod != null && reportPeriod.order == 4)

    // провека наличия в декларации банка данных для данного подразделения
    def findCurrentDepo = false
    /** Данные Приложения № 5 к Листу 02 из декларации Банка для данного подразделения. */
    def appl5 = null
    for (def item : xmlBankData.Документ.Прибыль.РасчНал.РаспрНалСубРФ) {
        if (item.@КППОП == kpp) {
            findCurrentDepo = true
            appl5 = item
            break
        }
    }
    if (!findCurrentDepo) {
        logger.error("В декларации Банка отсутствуют данные для подразделения: $name (в приложении № 5 к Листу 02).")
        return
    }

    // Приложение № 5 к Листу 02
    /** ОбРасч. Столбец «Признак расчёта». */
    def obRasch = appl5.@ОбРасч.text()
    /** НаимОП. Столбец «Подразделение территориального банка». */
    def naimOP = appl5.@НаимОП.text()
    /** КППОП. Столбец «КПП». */
    def kppop = appl5.@КППОП.text()
    /** ОбязУплНалОП. Столбец «Обязанность по уплате налога». */
    def obazUplNalOP = appl5.@ОбязУплНалОП.text()
    /** ДоляНалБаз. Столбец «Доля налоговой базы (%)». */
    def dolaNalBaz = appl5.@ДоляНалБаз.text()
    /** НалБазаДоля. Столбец «Налоговая база исходя из доли (руб.)». */
    def nalBazaDola = appl5.@НалБазаДоля.text()
    /** СтавНалСубРФ. Столбец «Ставка налога % в бюджет субъекта (%)». */
    def stavNalSubRF = appl5.@СтавНалСубРФ.text()
    /** СумНал. Столбец «Сумма налога». */
    def sumNal = appl5.@СумНал.text()
    /** НалНачислСубРФ. Столбец «Начислено налога в бюджет субъекта (руб.)». */
    def nalNachislSubRF = appl5.@НалНачислСубРФ.text()
    /** СумНалП. Столбец «Сумма налога к доплате». */
    def sumNalP = appl5.@СумНалП.text()
    /** НалВыплВнеРФ. Столбец «Сумма налога, выплаченная за пределами России и засчитываемая в уплату налога». */
    def nalViplVneRF = appl5.@НалВыплВнеРФ.text()
    /** МесАвПлат. Столбец «Ежемесячные авансовые платежи в квартале, следующем за отчётным периодом (текущий отчёт)». */
    def mesAvPlat = appl5.@МесАвПлат.text()
    /** МесАвПлат1КвСлед. Столбец «Ежемесячные авансовые платежи на I квартал следующего налогового периода». */
    def mesAvPlat1CvSled = appl5.@МесАвПлат1КвСлед.text()
    /** НалБазаОрг. */
    def nalBazaOrg = appl5.@НалБазаОрг.text()

    /** ПрПодп. */
    def prPodp = signatoryId

    /*
     * Расчет значений для текущей декларации.
     */

    def period = 0
    if (reorgFormCode != null) {
        period = 50
    } else if (reportPeriod.order != null) {
        def values = [21, 31, 33, 34]
        period = values[reportPeriod.order - 1]
    }

    /*
     * Формирование XML'ки.
     */

    def xmlbuilder = new MarkupBuilder(xml)
    xmlbuilder.Файл(
            ИдФайл : declarationService.generateXmlFileId(newDeclaration ? 10 : 5, declarationData.departmentReportPeriodId, taxOrganCode, declarationData.kpp),
            ВерсПрог : applicationVersion,
            ВерсФорм : formatVersion) {

        // Титульный лист
        Документ(
                КНД :  knd,
                ДатаДок : (docDate != null ? docDate : new Date()).format("dd.MM.yyyy"),
                Период : period,
                ОтчетГод : (taxPeriod != null ? taxPeriod.year : empty),
                КодНО : taxOrganCode,
                НомКорр : reportPeriodService.getCorrectionNumber(declarationData.departmentReportPeriodId),
                ПоМесту : taxPlaceTypeCode) {

            СвНП(
                    ОКВЭД : okvedCode,
                    Тлф : phone) {

                НПЮЛ(
                        НаимОрг : name,
                        ИННЮЛ : inn,
                        КПП : kpp) {

                    if (reorgFormCode != null && !reorgFormCode.equals("")) {
                        СвРеоргЮЛ([ФормРеорг: reorgFormCode] +
                                (Integer.parseInt(reorgFormCode) in [1, 2, 3, 5, 6] ?
                                        [ИННЮЛ: reorgInn, КПП: reorgKpp] : [])
                        )
                    }
                }
            }

            Подписант(ПрПодп : prPodp) {
                ФИО(
                        Фамилия : signatorySurname,
                        Имя : signatoryFirstName,
                        Отчество : signatoryLastName)
                if (prPodp != 1) {
                    СвПред(
                            [НаимДок : approveDocName] +
                                    (approveOrgName != null ? [НаимОрг : approveOrgName] : [:] )
                    )
                }
            }
            // Титульный лист - конец

            Прибыль() {
                НалПУ() {
                    // Раздел 1. Подраздел 1.1
                    // 0..n // всегда один
                    НалПУАв(
                            ТипНП : typeNP,
                            ОКТМО : oktmo) {

                        // 0..1
                        ФедБдж(
                                КБК : kbk,
                                НалПУ : empty)

                        // 0..1
                        СубБдж(
                                КБК : kbk2,
                                НалПУ : sumNalP)
                    }
                    // Раздел 1. Подраздел 1.1 - конец

                    // Раздел 1. Подраздел 1.2
                    def cvartalIchs
                    switch (reportPeriod != null ? reportPeriod.order : empty) {
                        case 3 :
                            cvartalIchs = [21, 24]
                            break
                        default:
                            cvartalIchs = [0]
                    }
                    cvartalIchs.each { cvartalIch ->
                        // 0..n
                        НалПУМес(
                                [ТипНП : typeNP] +
                                        (cvartalIch != 0 ? [КварталИсч : cvartalIch] : [:]) +
                                        [ОКТМО : oktmo]) {

                            def avPlat1 = empty
                            def avPlat2 = empty
                            def avPlat3 = empty

                            // 0..1
                            ФедБдж(
                                    КБК : kbk,
                                    АвПлат1 : avPlat1,
                                    АвПлат2 : avPlat2,
                                    АвПлат3 : avPlat3)
                            if (!isTaxPeriod) {
                                def appl5List02Row120 = new BigDecimal(mesAvPlat)
                                avPlat1 = (long) appl5List02Row120 / 3
                                avPlat2 = avPlat1
                                avPlat3 = getLong(appl5List02Row120 - avPlat1 - avPlat2)
                            }
                            // 0..1
                            СубБдж(
                                    КБК : kbk2,
                                    АвПлат1 : avPlat1,
                                    АвПлат2 : avPlat2,
                                    АвПлат3 : avPlat3)
                        }
                    }
                    // Раздел 1. Подраздел 1.2 - конец
                }

                // Приложение № 5 к Листу 02
                // 0..n - всегда один
                РаспрНалСубРФ(
                        ТипНП : typeNP,
                        ОбРасч : obRasch,
                        НаимОП : naimOP,
                        КППОП : kppop,
                        ОбязУплНалОП : obazUplNalOP,
                        НалБазаОрг : nalBazaOrg,
                        НалБазаБезЛиквОП : empty,
                        ДоляНалБаз : dolaNalBaz,
                        НалБазаДоля : nalBazaDola,
                        СтавНалСубРФ : stavNalSubRF,
                        СумНал : sumNal,
                        НалНачислСубРФ : nalNachislSubRF,
                        НалВыплВнеРФ : nalViplVneRF,
                        СумНалП : sumNalP,
                        МесАвПлат : mesAvPlat,
                        МесАвПлат1КвСлед : mesAvPlat1CvSled)
                // Приложение № 5 к Листу 02 - конец
            }
        }
    }
}

/** Получить округленное, целочисленное значение. */
def getLong(def value) {
    if (value == null) {
        return 0
    }
    return roundValue(value, 0)
}

/**
 * Округляет число до требуемой точности.
 *
 * @param value округляемое число
 * @param precision точность округления, знаки после запятой
 * @return округленное число
 */
def roundValue(def value, def precision) {
    ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
}

List<String> getErrorTable(record) {
    List<String> errorList = new ArrayList<String>()
    if (record.NAME?.stringValue == null || record.NAME.stringValue.isEmpty()) {
        errorList.add("«Наименование подразделения»")
    }
    if (record.OKTMO?.referenceValue == null) {
        errorList.add("«ОКТМО»")
    }
    if (record.TAX_ORGAN_CODE?.stringValue == null || record.TAX_ORGAN_CODE.stringValue.isEmpty()) {
        errorList.add("«Код налогового органа»")
    }
    if (record.OKVED_CODE?.referenceValue == null) {
        errorList.add("«Код вида экономической деятельности и по классификатору ОКВЭД»")
    }
    def reorgFormCode = getRefBookValue(5, record?.REORG_FORM_CODE?.value)?.CODE?.value
    if (reorgFormCode != null && reorgFormCode != '0') {
        if (record.REORG_INN?.value == null || record.REORG_INN.value.isEmpty()) {
            errorList.add("«ИНН реорганизованного обособленного подразделения»")
        }
        if (record.REORG_KPP?.value == null || record.REORG_KPP.value.isEmpty()) {
            errorList.add("«КПП реорганизованного обособленного подразделения»")
        }
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
    def signatoryId = getRefBookValue(35, record?.SIGNATORY_ID?.value)?.CODE?.value
    if ((signatoryId != null && signatoryId != 1) && (record.APPROVE_DOC_NAME?.value == null || record.APPROVE_DOC_NAME.value.isEmpty())) {
        errorList.add("«Наименование документа, подтверждающего полномочия представителя»")
    }
    if (record.TAX_PLACE_TYPE_CODE?.referenceValue == null) {
        errorList.add("«Код места, по которому представляется документ»")
    }
    errorList
}

List<String> getErrorVersion(record) {
    List<String> errorList = new ArrayList<String>()
    if (record.FORMAT_VERSION == null || record.FORMAT_VERSION.stringValue == null || !record.FORMAT_VERSION.stringValue.equals('5.05')) {
        errorList.add("«Версия формата»")
    }
    errorList
}

List<String> getErrorDepartment(record) {
    List<String> errorList = new ArrayList<String>()

    if (record.INN?.stringValue == null || record.INN.stringValue.isEmpty()) {
        errorList.add("«ИНН»")
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

def getOkato(def id) {
    def String okato = null
    if(id != null){
        okato = getRefBookValue(96, id)?.CODE?.stringValue
    }
    return okato
}

// Получить параметры подразделения (из справочника 33)
def getDepartmentParam() {
    if (departmentParam == null) {
        def departmentId = declarationData.departmentId
        def departmentParamList = getProvider(33).getRecords(getEndDate() - 1, null, "DEPARTMENT_ID = $departmentId", null)
        if (departmentParamList == null || departmentParamList.size() == 0 || departmentParamList.get(0) == null) {
            throw new Exception("Ошибка при получении настроек обособленного подразделения")
        }
        departmentParam = departmentParamList?.get(0)
    }
    return departmentParam
}

// Получить параметры подразделения (из справочника 330)
def getDepartmentParamTable(def departmentParamId) {
    if (departmentParamTable == null) {
        def filter = "LINK = $departmentParamId and KPP ='${declarationData.kpp}'"
        def departmentParamTableList = getProvider(330).getRecords(getEndDate() - 1, null, filter, null)
        if (departmentParamTableList == null || departmentParamTableList.size() == 0 || departmentParamTableList.get(0) == null) {
            throw new Exception("Ошибка при получении настроек обособленного подразделения")
        }
        departmentParamTable = departmentParamTableList.get(0)
    }
    return departmentParamTable
}