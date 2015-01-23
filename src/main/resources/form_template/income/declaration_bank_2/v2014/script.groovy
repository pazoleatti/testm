package form_template.income.declaration_bank_2.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field
import groovy.xml.MarkupBuilder

/**
 * Декларация по налогу на прибыль (Банк) (год 2014)
 * Формирование XML для декларации налога на прибыль.
 *
 * declarationTemplateId=21447
 *
 * @author Bulat.Kinzyabulatov
 * TODO сделано Приложение №2(с вопросами), остальное как приближение или скопировано из другой декларации
 * TODO декларация нерабочая, еще настройки подразделения дожны поменяться
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE : // создать / обновить
        checkDepartmentParams(LogLevel.WARNING)
        break
    case FormDataEvent.CHECK : // проверить
        checkDepartmentParams(LogLevel.ERROR)
        logicCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED : // принять из создана
        checkDepartmentParams(LogLevel.ERROR)
        logicCheck()
        break
    case FormDataEvent.MOVE_ACCEPTED_TO_CREATED: // отменить принятие
        сancelAccepted()    // TODO убрать?
        break
    case FormDataEvent.CALCULATE:
        checkDepartmentParams(LogLevel.WARNING)
        generateXML()
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
def getEndDate = null
@Field
def reportPeriodEndDate = null

def getEndDate() {
    if (reportPeriodEndDate == null) {
        reportPeriodEndDate = reportPeriodService.getEndDate(declarationData.reportPeriodId)?.time
    }
    return reportPeriodEndDate
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Получение провайдера с использованием кеширования.
def getProvider(def long providerId) {
    if (!providerCache.containsKey(providerId)) {
        providerCache.put(providerId, refBookFactory.getDataProvider(providerId))
    }
    return providerCache.get(providerId)
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

// Логические проверки.
void logicCheck() {
    // TODO
}

// Запуск генерации XML.
void generateXML() {
    def empty = 0
    // TODO

    def knd = '1151006'
    def kbk = '18210101011011000110'//TODO
    def kbk2 = '18210101012021000110'//TODO

    def departmentId = declarationData.departmentId
    def reportPeriodId = declarationData.reportPeriodId

    // справочник "Параметры подразделения по налогу на прибыль" - начало
    //TODO настройки подразделения поменяются
    def incomeParams = getProvider(33).getRecords(getEndDate() - 1, null, "DEPARTMENT_ID = $departmentId", null)?.get(0)
    if (incomeParams == null) {
        throw new Exception('Ошибка при получении настроек обособленного подразделения!')
    }

    def filter = "LINK = $departmentParamId and TAX_ORGAN_CODE ='${declarationData.taxOrganCode}' and KPP ='${declarationData.kpp}'"
    def incomeParamsTable = getProvider(330).getRecords(getEndDate() - 1, null, filter, null)?.get(0)
    if (incomeParamsTable == null) {
        throw new Exception('Ошибка при получении настроек обособленного подразделения!')
    }

    def reorgFormCode = getRefBookValue(5, incomeParamsTable?.REORG_FORM_CODE?.value)?.CODE?.value
    def taxOrganCode = incomeParamsTable?.TAX_ORGAN_CODE?.value
    def okvedCode = getRefBookValue(34, incomeParamsTable?.OKVED_CODE?.value)?.CODE?.value
    def phone = incomeParamsTable?.PHONE?.value
    def name = incomeParamsTable?.NAME?.value
    def inn = incomeParams?.INN?.value
    def kpp = incomeParamsTable?.KPP?.value
    def reorgInn = incomeParamsTable?.REORG_INN?.value
    def reorgKpp = incomeParamsTable?.REORG_KPP?.value
    def oktmo = getRefBookValue(96, incomeParamsTable?.OKTMO?.value)?.CODE?.value?.substring(0,8)
    def signatoryId = getRefBookValue(35, incomeParamsTable?.SIGNATORY_ID?.value)?.CODE?.value
    def formatVersion = incomeParams?.FORMAT_VERSION?.value
    def taxPlaceTypeCode = getRefBookValue(2, incomeParamsTable?.TAX_PLACE_TYPE_CODE?.value)?.CODE?.value
    def signatorySurname = incomeParamsTable?.SIGNATORY_SURNAME?.value
    def signatoryFirstName = incomeParamsTable?.SIGNATORY_FIRSTNAME?.value
    def signatoryLastName = incomeParamsTable?.SIGNATORY_LASTNAME?.value
    def approveDocName = incomeParamsTable?.APPROVE_DOC_NAME?.value
    def approveOrgName = incomeParamsTable?.APPROVE_ORG_NAME?.value

    // Отчётный период.
    def reportPeriod = reportPeriodService.get(reportPeriodId)

    // Налоговый период.
    def taxPeriod = (reportPeriod != null ? taxPeriodService.get(reportPeriod.getTaxPeriod().getId()) : null)

    // ПрПодп.
    def prPodp = signatoryId

    // TODO Период.
    def period = 0
    if (reorgFormCode != null) {
        period = 50
    } else if (reportPeriod.order != null) {
        def values = [21, 31, 33, 34]
        period = values[reportPeriod.order - 1]
    }

    def formDataCollection = declarationService.getAcceptedFormDataSources(declarationData)

    // Выходная Приложение №2 "Сведения о доходах физического лица, выплаченных ему налоговым агентом, от операций с ценными бумагами, операций с финансовыми инструментами срочных сделок, а также при осуществлении выплат по ценным бумагам российских эмитентов"
    def dataRowsApp2 = getDataRows(formDataCollection, 415, FormDataKind.ADDITIONAL)

    if (xml == null) {
        return
    }

    // Формирование XML'ки.

    def builder = new MarkupBuilder(xml)
    builder.Файл(
            ИдФайл : declarationService.generateXmlFileId(11, declarationData.departmentReportPeriodId, declarationData.taxOrganCode, declarationData.kpp),
            ВерсПрог : applicationVersion,
            ВерсФорм : formatVersion){

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
                        [Фамилия : signatorySurname,
                        Имя : signatoryFirstName] +
                        (signatoryLastName != null ? [Отчество : signatoryLastName] : [:]))
                if (prPodp != 1) {
                    СвПред(
                            [НаимДок : approveDocName] +
                                    (approveOrgName != null ? [НаимОрг : approveOrgName] : [:] )
                    )
                }
            }

            Прибыль() {
                НалПУ() {
                    // Раздел 1. Подраздел 1.1
                    // 0..n // всегда один
                    НалПУАв(ОКТМО: oktmo) {
                        ФедБдж(
                                КБК: kbk,
                                НалПУ: empty)//TODO

                        СубБдж(
                                КБК: kbk2,
                                НалПУ: empty)//TODO
                    }
                    // Раздел 1. Подраздел 1.1 - конец

                    // Раздел 1. Подраздел 1.2
                    // 0..n
                    НалПУМес(КвИсчислАв: empty,//TODO
                            ОКТМО: oktmo) {//TODO
                        // 0..1
                        ФедБдж(
                                КБК: kbk,//TODO
                                АвПлат1: empty,//TODO
                                АвПлат2: empty,//TODO
                                АвПлат3: empty)//TODO

                        // 0..1
                        СубБдж(
                                КБК: kbk2,//TODO
                                АвПлат1: empty,//TODO
                                АвПлат2: empty,//TODO
                                АвПлат3: empty)//TODO
                    }
                    // Раздел 1. Подраздел 1.2 - конец

                    // Раздел 1. Подраздел 1.3
                    // 0..n
                    НалПУПроц(
                            ВидПлат : empty,//TODO
                            ОКТМО : empty,//TODO
                            КБК : empty) {//TODO

                        // 0..n
                        УплСрок(
                                Срок : empty,//TODO
                                НалПУ : empty)//TODO
                    }
                    // Раздел 1. Подраздел 1.3 - конец
                }

                // Лист 02 TODO проверить весь
                // 0..4
                РасчНал(
                        ТипНП : empty,
                        ДохРеал : empty,
                        ДохВнереал : empty,
                        РасхУмРеал : empty,
                        РасхВнереал : empty,
                        Убытки : empty,
                        ПрибУб : empty,
                        ДохИсклПриб : empty,
                        ПрибБРСт0 : empty,
                        СумЛьгот : empty,
                        НалБаза : empty,
                        УбытУмНБ : empty,
                        НалБазаИсч : empty,
                        НалБазаИсчСуб : empty,
                        СтавНалВсего : empty,
                        СтавНалФБ : empty,
                        СтавНалСуб : empty,
                        СтавНалСуб284 : empty,
                        НалИсчисл : empty,
                        НалИсчислФБ : empty,
                        НалИсчислСуб : empty,
                        АвНачисл : empty,
                        АвНачислФБ : empty,
                        АвНачислСуб : empty,
                        НалВыпл311 : empty,
                        НалВыпл311ФБ : empty,
                        НалВыпл311Суб : empty,
                        НалДоплФБ : empty,
                        НалДоплСуб : empty,
                        НалУменФБ : empty,
                        НалУменСуб : empty,
                        АвПлатМес : empty,
                        АвПлатМесФБ : empty,
                        АвПлатМесСуб : empty,
                        АвПлатУпл1Кв : empty,
                        АвПлатУпл1КвФБ : empty,
                        АвПлатУпл1КвСуб : empty) {
                    // Лист 02 - конец

                    // Приложение № 1 к Листу 02
                    // 0..1
                    ДохРеалВнеРеал(ТипНП : empty) {
                        // 0..1
                        ДохРеал(
                                ВырРеалПред : empty,
                                ВырРеалОпер32 : empty,
                                ВырРеалИтог : empty) {
                            // 0..1
                            ВырРеал(
                                    ВырРеалВс : empty,
                                    ВырРеалТовСоб : empty,
                                    ВырРеалТовПок : empty,
                                    ВырРеалИмПрав : empty,
                                    ВырРеалИмПроч : empty)
                            // 0..1
                            ВырРеалЦБ(
                                    ВырРеалЦБВс : empty,
                                    ОтклВырЦБМин : empty,
                                    ОтклВырЦБРасч : empty)
                        }
                        // 0..1
                        ДохВнеРеал(
                                ВнеРеалДохВс : empty,
                                ВнеРеалДохПр : empty,
                                ВнеРеалДохСт : empty,
                                ВнеРеалДохБезв : empty,
                                ВнеРеалДохИзл : empty,
                                ВнеРеалДохВРасх : empty,
                                ВнеРеалДохРынЦБДД : empty,
                                ВнеРеалДохКор : empty)
                    }
                    // Приложение № 1 к Листу 02 - конец

                    // Приложение № 2 к Листу 02
                    // 0..1
                    РасхРеалВнеРеал(ТипНП : empty) {
                        // 0..1
                        РасхРеал(
                                ПрямРасхРеал : empty,
                                РеалИмущПрав : empty,
                                ПриобрРеалИмущ : empty,
                                АктивРеалПред : empty,
                                ПриобРеалЦБ : empty,
                                СумОтклЦен : empty,
                                РасхОпер32 : empty,
                                УбытПрошОбсл : empty,
                                УбытРеалАмИм : empty,
                                УбытРеалЗемУч : empty,
                                НадбПокПред : empty,
                                РасхПризнИтого : empty) {

                            // 0..1
                            ПрямРасхТорг(ПрямРасхТоргВс : empty)
                            // 0..1
                            КосвРасх(
                                    КосвРасхВс : empty,
                                    Налоги : empty,
                                    РасхКапВл10 : empty,
                                    РасхКапВл30 : empty,
                                    РасхТрудИнв : empty,
                                    РасхОргИнв : empty,
                                    РасхЗемУчВс : empty,
                                    РасхЗемУчСрокНП : empty,
                                    РасхЗемУч30пр : empty,
                                    РасхЗемУчСрокРас : empty,
                                    РасхЗемУчСрокАр : empty,
                                    НИОКР : empty,
                                    НИОКРнеПолРез : empty,
                                    НИОКРПер : empty,
                                    НИОКРПерНеРез : empty)
                        }
                        // 0..1
                        СумАморт(
                                СумАмортПерЛ : empty,
                                СумАмортПерНмАЛ : empty,
                                СумАмортПерН : empty,
                                СумАмортПерНмАН : empty,
                                МетодНачАморт : '1')
                        // 0..1
                        РасхВнеРеал(
                                РасхВнеРеалВс : empty,
                                РасхВнереалПрДО : empty,
                                РасхВнереалРзрв : empty,
                                УбытРеалПравТр : empty,
                                РасхЛиквОС : empty,
                                РасхШтраф : empty,
                                РасхРынЦБДД : empty)
                        // 0..1
                        УбытПриравн(
                                УбытПриравнВс : empty,
                                УбытПрошПер : empty,
                                СумБезнадДолг : empty)
                    }
                    // Приложение № 2 к Листу 02 - конец

                    // Приложение № 3 к Листу 02
                    // 0..1
                    РасчРасхОпер(
                            ТипНП : empty,
                            КолОбРеалАИ :empty,
                            КолОбРеалАИУб : empty,
                            ВыручРеалАИ : empty,
                            ОстСтРеалАИ : empty,
                            ПрибРеалАИ : empty,
                            УбытРеалАИ : empty,
                            ВыручРеалТов : empty,
                            РасхРеалТов : empty,
                            УбытОбОбсл : empty,
                            УбытОбОбслНеобл : empty,
                            ДохДоговДУИ : empty,
                            ДохДоговДУИ_ВнР : empty,
                            РасхДоговДУИ : empty,
                            РасхДоговДУИ_ВнР : empty,
                            УбытДоговДУИ : empty,
                            ЦенаРеалПравЗУ : empty,
                            СумНевозмЗатрЗУ : empty,
                            УбытРеалЗУ : empty,
                            ВыручОп302Ит : empty,
                            РасхОп302Ит : empty,
                            УбытОп302 : empty) {
                        // 0..1
                        ВыручРеалПТ(
                                ВыручРеалПТДоСр : empty,
                                ВыручРеалПТПосСр : empty)
                        // 0..1
                        СтоимРеалПТ(
                                СтоимРеалПТДоСр : empty,
                                СтоимРеалПТПосСр : empty)
                        // 0..1
                        УбытРеалПТ(
                                Убыт1Соот269 : empty,
                                Убыт1Прев269 : empty,
                                Убыт2РеалПТ : empty,
                                Убыт2ВнРасх : empty)
                    }
                    // Приложение № 3 к Листу 02 - конец

                    УбытУменНБ(
                            //TODO
                    ){
                        //TODO
                    }

                    def dataRowsAdvance = []//TODO
                    // Приложение № 5 к Листу 02
                    if (dataRowsAdvance != null && !dataRowsAdvance.isEmpty()) {
                        dataRowsAdvance.each { row ->
                            if (row.getAlias() == null) {
                                def naimOP = null
                                def record33 = getProvider(33).getRecords(getEndDate() - 1, null, "DEPARTMENT_ID = $row.regionBankDivision", null)?.get(0)
                                if (record33 != null) {
                                    naimOP = record33?.ADDITIONAL_NAME?.value
                                }
                                // 0..n
                                РаспрНалСубРФ(
                                        ТипНП: empty,
                                        ОбРасч: getRefBookValue(26, row.calcFlag)?.CODE?.value,
                                        НаимОП: naimOP,
                                        КППОП: row.kpp,
                                        ОбязУплНалОП: getRefBookValue(25, row.obligationPayTax)?.CODE?.value,
                                        НалБазаОрг: empty,
                                        НалБазаБезЛиквОП: empty,
                                        ДоляНалБаз: row.baseTaxOf,
                                        НалБазаДоля: row.baseTaxOfRub,
                                        СтавНалСубРФ: row.subjectTaxStavka,
                                        СумНал: row.taxSum,
                                        НалНачислСубРФ: row.subjectTaxCredit,
                                        НалВыплВнеРФ: row.taxSumOutside,
                                        СумНалП: (row.taxSumToPay != 0) ? row.taxSumToPay : (-row.taxSumToReduction),
                                        МесАвПлат: row.everyMontherPaymentAfterPeriod,
                                        МесАвПлат1КвСлед: row.everyMonthForKvartalNextPeriod)
                            }
                        }
                    }
                    // Приложение № 5 к Листу 02 - конец


                    РасчНалГруп(
                            //TODO
                    ){
                        //TODO
                    }

                    ДохРасхУчГрупНБ(
                            //TODO
                    ){
                        //TODO
                    }
                }
                // TODO лист 03 переделать
                def dataRowsDividend
                // 0..1
                НалУдНА() {
                    if (dataRowsDividend != null) {
                        dataRowsDividend.each { row ->
                            def divAll = row.dividendSumNalogAgent
                            // Лист 03 А
                            // 0..n
                            НалДохДив(
                                    ВидДив : row.dividendType,
                                    НалПер : row.taxPeriod,
                                    ОтчетГод : row.financialYear.format('yyyy'),
                                    ДивВсего : getLong(divAll),
                                    НалИсчисл : getLong(row.taxSum),
                                    НалДивПред : getLong(row.taxSumFromPeriod),
                                    НалДивПосл : getLong(row.taxSumFromPeriodAll)) {
                                // 0..1
                                ДивИОФЛНеРез(
                                        ДивИнОрг : getLong(row.dividendForgeinOrgAll),
                                        ДивФЛНеРез : getLong(row.dividendForgeinPersonalAll),
                                        ДивИсч0 : getLong(row.dividendStavka0),
                                        ДивИсч5 : getLong(row.dividendStavkaLess5),
                                        ДивИсч10 : getLong(row.dividendStavkaMore5),
                                        ДивИсчСв10 : getLong(row.dividendStavkaMore10))
                                // 0..1
                                ДивРА(
                                        ДивРАВс : getLong(row.dividendRussianMembersAll),
                                        ДивРО9 : getLong(row.dividendRussianOrgStavka9),
                                        ДивРО0 : getLong(row.dividendRussianOrgStavka0),
                                        ДивФЛРез : getLong(row.dividendPersonRussia),
                                        ДивНеНП : getLong(row.dividendMembersNotRussianTax))
                                // 0..1
                                ДивНА(
                                        ДивНАдоРас : getLong(row.dividendAgentAll),
                                        ДивНАдоРас0 : getLong(row.dividendAgentWithStavka0))
                                // 0..1
                                ДивНал(
                                        ДивНалВс : getLong(row.dividendSumForTaxAll),
                                        ДивНал9 : getLong(row.dividendSumForTaxStavka9),
                                        ДивНал0 : getLong(row.dividendSumForTaxStavka0))
                            }
                            // Лист 03 А - конец
                        }
                        dataRowsDividend.each { row ->
                            // Лист 03 Б
                            // 0..n
                            НалДохЦБ(
                                    ВидДоход : '1',
                                    НалБаза : empty,
                                    СтавНал : empty,
                                    НалИсчисл : empty,
                                    НалНачислПред : empty,
                                    НалНачислПосл : empty)
                            // Лист 03 Б - конец
                        }
                    }

                    def dataRowsTaxAgent
                    // Лист 03 В
                    if (dataRowsTaxAgent != null) {
                        dataRowsTaxAgent.each { row ->
                            // 0..n
                            РеестрСумДив(
                                    ДатаПерДив : (row.dividendDate != null ? row.dividendDate.format('dd.MM.yyyy') : empty),
                                    СумДив : getLong(row.sumDividend),
                                    СумНал : getLong(row.sumTax)) {

                                СвПолуч(
                                        [НаимПолуч : row.title] +
                                                (row.phone ? [Тлф : row.phone] : [:])) {
                                    МНПолуч(
                                            (row.zipCode ? [Индекс : row.zipCode] : [:]) +
                                                    [КодРегион : getRefBookValue(4, row.subdivisionRF)?.CODE?.value] +
                                                    (row.area? [Район : row.area] : [:]) +
                                                    (row.city ? [Город : row.city] : [:]) +
                                                    (row.region ? [НаселПункт : row.region] : [:]) +
                                                    (row.street ? [Улица : row.street] : [:]) +
                                                    (row.homeNumber ? [Дом : row.homeNumber] : [:]) +
                                                    (row.corpNumber ? [Корпус : row.corpNumber] : [:]) +
                                                    (row.apartment ? [Кварт : row.apartment] : [:]))
                                    // 0..1
                                    ФИОРук(
                                            [Фамилия : row.surname] +
                                                    [Имя : row.name] +
                                                    (row.patronymic ? [Отчество : row.patronymic] : [:]))
                                }
                            }
                        }
                    }
                    // Лист 03 В - конец
                }
                // Лист 04 TODO
                НалДохСтав(
                        ВидДоход: it,
                        НалБаза: getLong(empty),
                        ДохУмНалБаз: getLong(empty),
                        СтавНал: getLong(empty),
                        НалИсчисл: empty,
                        НалДивНеРФПред: getLong(empty),
                        НалДивНеРФ: getLong(empty),
                        НалНачислПред: getLong(empty),
                        НалНачислПосл: getLong(empty))
                // Лист 04 - конец

                // Лист 05 TODO
                // 0..n
                НалБазОпОсобС(
                        ВидОпер : 5,
                        ДохВыбытПогаш : empty,
                        СумОтклМинЦ : empty,
                        РасхПриобРеал : empty,
                        СумОтклМаксЦ : empty,
                        Прибыль : empty,
                        КорПриб : empty,
                        НалБазаБезУбПред : empty,
                        СумУбытПред : empty,
                        СумУбытУменНБ : empty,
                        СумНеучУбытПер : empty,
                        СумУбытЗСДо2010 : empty,
                        НалБаза : empty)
                НалБазОпОсобН(
                        ВидОпер : 5,
                        ДохВыбытПогаш : empty,
                        СумОтклМинЦ : empty,
                        РасхПриобРеал : empty,
                        СумОтклМаксЦ : empty,
                        Прибыль : empty,
                        КорПриб : empty,
                        НалБазаБезУбПред : empty,
                        СумУбытПред : empty,
                        СумУбытУменНБ : empty,
                        СумНеучУбытПер : empty,
                        СумУбытЗСДо2010 : empty,
                        НалБаза : empty)
                // Лист 05 - конец
                ДохРасНалБазаНПФ(
                        //TODO
                ){
                    //TODO
                }
                ЦелИспИмущ(
                        //TODO
                ){
                    //TODO
                }
                ДохНеУч_РасхУч(
                        //TODO
                ){
                    //TODO
                }
                for (def row : dataRowsApp2) {
                    //НомерСправ  Справка №
                    def nomerSprav = row.column1
                    //ДатаСправ   Дата составления
                    def dataSprav = row.column2
                    //Тип         Тип
                    def type = row.column3
                    //ИННФЛ       ИНН
                    def innFL = row.column4
                    //Фамилия     Фамилия
                    def surname = row.column5
                    //Имя         Имя
                    def givenName = row.column6
                    //Отчество    Отчество
                    def parentName = row.column7
                    //СтатусНП    Статус налогоплательщика
                    def statusNP = row.column8
                    //ДатаРожд    Дата рождения
                    def dataRozhd = row.column9
                    //Гражд       Гражданство (код страны)
                    def grazhd = row.column10
                    //КодВидДок   Код вида документа, удостоверяющего личность
                    def kodVidDok = row.column11
                    //СерНомДок   Серия и номер документа
                    def serNomDok = row.column12
                    //Индекс      Почтовый индекс
                    def zipCode = row.column13
                    //КодРегион   Регион (код)
                    def subdivisionRF = getRefBookValue(4, row.column14)?.CODE?.value
                    //Район       Район
                    def area = row.column15
                    //Город       Город
                    def city = row.column16
                    //НаселПункт  Населенный пункт (село, поселок)
                    def region = row.column17
                    //Улица       Улица (проспект, переулок)
                    def street = row.column18
                    //Дом         Номер дома (владения)
                    def homeNumber = row.column19
                    //Корпус      Номер корпуса (строения)
                    def corpNumber = row.column20
                    //Кварт       Номер квартиры
                    def apartment = row.column21
                    //ОКСМ        Код страны
                    def oksm = row.column22
                    //АдрТекст    Адрес места жительства за пределами Российской Федерации
                    def adrText = row.column23
                    //Ставка      Налоговая ставка
                    def stavka = row.column24
                    //СумДохОбщ   Общая сумма дохода
                    def sumDohObsh = row.column25
                    //СумВычОбщ   Общая сумма вычетов
                    def sumVichObsh = row.column26
                    //НалБаза     Налоговая база
                    def nalBaza = row.column27
                    //НалИсчисл   Сумма налога исчисленная
                    def nalIschisl = row.column28
                    //НалУдерж    Сумма налога удержанная
                    def nalUderzh = row.column29
                    //НалПеречисл Сумма налога перечисленная
                    def nalPerechisl = row.column30//TODO верное название?
                    //НалУдержЛиш Сумма налога, излишне удержанная налоговым агентом
                    def nalUderzhLish = row.column31
                    //НалНеУдерж  Сумма налога, не удержанная налоговым агентом
                    def nalNeUderzh = row.column32

                    // Приложение №2
                    // 0..n
                    СведДохФЛ(
                            НомерСправ : nomerSprav,
                            ДатаСправ : dataSprav.format('dd.MM.yyyy'),
                            Тип : type) {
                        //1..1
                        ФЛПолучДох(
                                ИННФЛ : innFL,
                                ИННИно : innFL,//TODO
                                СтатусНП : statusNP,
                                ДатаРожд : dataRozhd,
                                Гражд : grazhd,
                                КодВидДок : kodVidDok,
                                СерНомДок : serNomDok
                        ) {
                            // 1..1
                            ФИО([Фамилия : surname, Имя : givenName] + (parentName != null ? [Отчество : parentName] : []))
                            //0..1
                            АдрМЖРФ(
                                    (zipCode ? [Индекс : zipCode] : [:]) +
                                    [КодРегион : subdivisionRF] +
                                    (area? [Район : area] : [:]) +
                                    (city ? [Город : city] : [:]) +
                                    (region ? [НаселПункт : region] : [:]) +
                                    (street ? [Улица : street] : [:]) +
                                    (homeNumber ? [Дом : homeNumber] : [:]) +
                                    (corpNumber ? [Корпус : corpNumber] : [:]) +
                                    (apartment ? [Кварт : apartment] : [:]))
                            //0..1
                            АдрМЖИно(ОКСМ : oksm, АдрТекст : adrText)
                        }
                        //1..1
                        ДохНалПер(
                            [Ставка : stavka, СумДоходОбщ : sumDohObsh] +
                            (sumVichObsh != null ? [СумВычОбщ : sumVichObsh] : []) +
                            [НалБаза : nalBaza, НалИсчисл : nalIschisl] +
                            (nalUderzh != null ? [СумВычОбщ : nalUderzh] : []) +
                            (nalPerechisl != null ? [НалПеречисл : nalPerechisl] : []) +
                            (nalUderzhLish != null ? [НалУдержЛиш : nalUderzhLish] : []) +
                            (nalNeUderzh != null ? [НалНеУдерж : nalNeUderzh] : [])
                        )
                        int num = 33
                        //0..1
                        СпрДохФЛ() {
                            3.times{
                                //КодДоход    040 (Код дохода)
                                def kodDohod040 = row["column${++num}"]
                                //СумДоход    041 (Сумма дохода)
                                def sumDohod041 = row["column${++num}"]

                                СумДох(КодДоход : kodDohod040, СумДоход : sumDohod041) {
                                    5.times{
                                        //КодВычет    042 (Код вычета)
                                        def kodVichet042 = row["column${++num}"]
                                        //СумВычет    043 (Сумма вычета)
                                        def sumVichet043 = row["column${++num}"]

                                        //1..n
                                        СумВыч(КодВычет : kodVichet042, СумВычет : sumVichet043)
                                    }
                                }
                            }
                        }

                        //0..1
                        НалВычСтанд() {
                            2.times{
                                //КодВычет    051 (Код вычета)
                                def kodVichet051 = row["column${++num}"]
                                //СумВычет    052 (Сумма вычета)
                                def sumVichet052 = row["column${++num}"]
                                //1..n
                                СумВыч(КодВычет : kodVichet051, СумВычет : sumVichet052)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Получить округленное, целочисленное значение.
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

// Получить строки формы.
def getDataRows(def formDataCollection, def formTypeId, def kind) {
    def formList = formDataCollection?.findAllByFormTypeAndKind(formTypeId, kind)
    def dataRows = []
    for (def form : formList) {
        dataRows += (formDataService.getDataRowHelper(form)?.getAll()?:[])
    }
    return dataRows.isEmpty() ? null : dataRows
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
        def filter = "LINK = $departmentParamId and TAX_ORGAN_CODE ='${declarationData.taxOrganCode}' and KPP ='${declarationData.kpp}'"
        def departmentParamTableList = getProvider(330).getRecords(getEndDate() - 1, null, filter, null)
        if (departmentParamTableList == null || departmentParamTableList.size() == 0 || departmentParamTableList.get(0) == null) {
            throw new Exception("Ошибка при получении настроек обособленного подразделения")
        }
        departmentParamTable = departmentParamTableList.get(0)
    }
    return departmentParamTable
}