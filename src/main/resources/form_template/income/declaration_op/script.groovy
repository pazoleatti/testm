package form_template.income.declaration_op

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType

/**
 * Формирование XML для декларации налога на прибыль уровня обособленного подразделения.
 *
 * @author rtimerbaev
 */

switch (formDataEvent) {
    // создать / обновить
    case FormDataEvent.CREATE :
        break
    // проверить
    case FormDataEvent.CHECK :
        return
        break
    // принять из создана
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :
        return
        break
    // после принять из создана
    case FormDataEvent.AFTER_MOVE_CREATED_TO_ACCEPTED :
        break
    // из принять в создана
    case FormDataEvent.MOVE_ACCEPTED_TO_CREATED :
        return
        break
}

/*
 * Константы.
 */
def empty = 0
def knd = '1151006'
def kbk = '18210101011011000110'
def kbk2 = '18210101012021000110'
def typeNP = '1'

/** Предпослденяя дата отчетного периода на которую нужно получить настройки подразделения из справочника. */
def reportDate = reportPeriodService.getEndDate(declarationData.reportPeriodId)
if (reportDate != null) {
    reportDate = reportDate.getTime() - 1
}

// справочник "Параметры подразделения по налогу на прибыль" - начало
def departmentParamIncomeRefDataProvider = refBookFactory.getDataProvider(33)
def departmentParamIncomeRecords = departmentParamIncomeRefDataProvider.getRecords(reportDate, null,
        "DEPARTMENT_ID = '" + declarationData.departmentId + "'", null);
if (departmentParamIncomeRecords == null || departmentParamIncomeRecords.getRecords().isEmpty()) {
    throw new Exception("Не удалось получить настройки обособленного подразделения.")
}
def incomeParams = departmentParamIncomeRecords.getRecords().getAt(0)
if (incomeParams == null) {
    throw new Exception("Ошибка при получении настроек обособленного подразделения.")
}
def reorgFormCode = refBookService.getStringValue(5, getValue(incomeParams, 'REORG_FORM_CODE'), 'CODE')
def taxOrganCode = getValue(incomeParams, 'TAX_ORGAN_CODE')
def okvedCode = refBookService.getStringValue(34, getValue(incomeParams, 'OKVED_CODE'), 'CODE')
def phone = getValue(incomeParams, 'PHONE')
def name = getValue(incomeParams, 'NAME')
def inn = getValue(incomeParams, 'INN')
def kpp = getValue(incomeParams, 'KPP')
def reorgInn = getValue(incomeParams, 'REORG_INN')
def reorgKpp = getValue(incomeParams, 'REORG_KPP')
def okato = refBookService.getStringValue(3, getValue(incomeParams, 'OKATO'), 'OKATO')
def signatoryId = refBookService.getNumberValue(35, getValue(incomeParams, 'SIGNATORY_ID'), 'CODE')
def appVersion = getValue(incomeParams, 'APP_VERSION')
def formatVersion = getValue(incomeParams, 'FORMAT_VERSION')
def taxPlaceTypeCode = refBookService.getStringValue(2, getValue(incomeParams, 'TAX_PLACE_TYPE_CODE'), 'CODE')
def signatorySurname = getValue(incomeParams, 'SIGNATORY_SURNAME')
def signatoryFirstName = getValue(incomeParams, 'SIGNATORY_FIRSTNAME')
def signatoryLastName = getValue(incomeParams, 'SIGNATORY_LASTNAME')
def approveDocName = getValue(incomeParams, 'APPROVE_DOC_NAME')
def approveOrgName = getValue(incomeParams, 'APPROVE_ORG_NAME')
// справочник "Параметры подразделения по налогу на прибыль" - конец

/** Отчётный период. */
def reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)

/** Налоговый период. */
def taxPeriod = (reportPeriod != null ? reportPeriod.getTaxPeriod() : null)

/** Признак налоговый ли это период. */
def isTaxPeriod = (reportPeriod != null && reportPeriod.order == 4)

/*
 * Провека декларации банка.
 */

/** вид декларации 2 - декларация по налогу на прибыль уровня банка. */
def declarationTypeId = 2

/** Идентификатор подразделения Банка. */
def departmentBankId = 1
def bankDeclarationData = declarationService.find(declarationTypeId, departmentBankId, reportPeriod.id)
if (bankDeclarationData == null || !bankDeclarationData.accepted) {
    logger.error('Декларация Банка по прибыли за указанный период не сформирована или не находится в статусе "Принята".')
    return
}

/** XML декларации за предыдущий отчетный период. */
def xmlBankData = null

if (bankDeclarationData.id != null) {
    def xmlString = declarationService.getXmlData(bankDeclarationData.id)
    xmlString = xmlString.replace('<?xml version="1.0" encoding="windows-1251"?>', '')
    if (xmlString == null) {
        logger.error('Данные декларации Банка не заполнены.')
        return
    }
    xmlBankData = new XmlSlurper().parseText(xmlString)
}
if (xmlBankData == null) {
    logger.error('Не удалось получить данные декларации Банка.')
}

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
if (reorgFormCode == 50) {
    period = 50
} else if (reportPeriod.order != null) {
    def values = [21, 31, 33, 34]
    period = values[reportPeriod.order - 1]
}

if (xml == null) {
    return
}

/*
 * Формирование XML'ки.
 */

import groovy.xml.MarkupBuilder;
def xmlbuilder = new MarkupBuilder(xml)

xmlbuilder.Файл(
        ИдФайл : declarationService.generateXmlFileId(5, declarationData.departmentId, declarationData.reportPeriodId),
        ВерсПрог : appVersion,
        ВерсФорм : formatVersion) {

    // Титульный лист
    Документ(
            КНД :  knd,
            ДатаДок : (docDate != null ? docDate : new Date()).format("dd.MM.yyyy"),
            Период : period,
            ОтчетГод : (taxPeriod != null && taxPeriod.startDate != null ? taxPeriod.startDate.format('yyyy') : empty),
            КодНО : taxOrganCode,
            НомКорр : '0', // TODO (от Айдара) учесть что потом будут корректирующие периоды
            ПоМесту : taxPlaceTypeCode) {

        СвНП(
                ОКВЭД : okvedCode,
                Тлф : phone) {

            НПЮЛ(
                    НаимОрг : name,
                    ИННЮЛ : inn,
                    КПП : kpp) {

                if (reorgFormCode != null) {
                    СвРеоргЮЛ(
                            ФормРеорг : reorgFormCode,
                            ИННЮЛ : reorgInn,
                            КПП : reorgKpp)
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
                        ОКАТО : okato) {

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
                                    [ОКАТО : okato]) {

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

/*
 * Вычисления.
 */

/**
 * Получить округленное, целочисленное значение.
 */
def getLong(def value) {
    if (value == null) {
        return 0
    }
    return roundValue(value, 0)
}

/**
 * Получить значение или ноль, если значения нет.
 */
def getValue(def value) {
    return (value != null ? value : 0)
}

/**
 * Получить данные нф.
 *
 * @param form нф
 */
def getDataRowHelper(def form) {
    return (form != null ? formDataService.getDataRowHelper(form) : null)
}

/**
 * Получить значение атрибута строки справочника.

 * @param record строка справочника
 * @param alias алиас
 */
def getValue(def record, def alias) {
    def value = record.get(alias)
    switch (value.getAttributeType()) {
        case RefBookAttributeType.DATE :
            return value.getDateValue()
        case RefBookAttributeType.NUMBER :
            return value.getNumberValue()
        case RefBookAttributeType.STRING :
            return value.getStringValue()
        case RefBookAttributeType.REFERENCE :
            return value.getReferenceValue()
    }
    return null
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