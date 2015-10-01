package form_template.income.declaration_op_2.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field
import groovy.xml.MarkupBuilder

import javax.xml.stream.XMLStreamReader

/**
 * Декларация по налогу на прибыль (ОП) (с периода год 2014)
 * Формирование XML для декларации налога на прибыль уровня обособленного подразделения.
 *
 * declarationTemplateId=21707
 *
 * @author Bulat.Kinzyabulatov
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE : // создать / обновить
        checkDepartmentParams(LogLevel.WARNING)
        checkDeclarationBank(LogLevel.WARNING)
        break
    case FormDataEvent.CHECK : // проверить
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED : // принять из создана
        checkDepartmentParams(LogLevel.WARNING)
        checkDeclarationBank(declarationData.accepted ? LogLevel.WARNING : LogLevel.ERROR)
        break
    case FormDataEvent.PRE_CALCULATION_CHECK:
        checkDepartmentParams(LogLevel.WARNING)
        checkDeclarationBank(LogLevel.WARNING)
        break
    case FormDataEvent.CALCULATE:
        checkDepartmentParams(LogLevel.WARNING)
        def readerBank = checkDeclarationBank(LogLevel.WARNING, false)
        generateXML(readerBank)
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
def getRefBookValue(def long refBookId, def recordId) {
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
def checkDeclarationBank(LogLevel logLevel, boolean onlyCheck = true) {
    /** Отчётный период. */
    def reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)

    /** вид декларации 11 - декларация банка */
    def declarationTypeId = 11

    /** Идентификатор подразделения Банка. */
    def departmentBankId = 1
    def bankDeclarationData = declarationService.getLast(declarationTypeId, departmentBankId, reportPeriod.id)
    if (bankDeclarationData == null || !bankDeclarationData.accepted) {
        logger.log(logLevel, 'Декларация Банка по прибыли за указанный период не сформирована или не находится в статусе "Принята".')
        return null
    }

    /** XMLStreamReader декларации за предыдущий отчетный период. */
    def readerBank = null

    if (bankDeclarationData.id != null) {
        readerBank = declarationService.getXmlStreamReader(bankDeclarationData.id)
        if (!readerBank) {
            logger.log(logLevel, 'Данные декларации Банка не заполнены.')
            return null
        }
    }
    if (readerBank == null) {
        logger.log(logLevel, 'Не удалось получить данные декларации Банка.')
    }
    if (onlyCheck) {
        readerBank.close()
        return null
    }
    return readerBank
}

// Запуск генерации XML.
void generateXML(XMLStreamReader readerBank) {

    def empty = 0
    def knd = '1151006'
    def kbk = '18210101011011000110'
    def kbk2 = '18210101012021000110'
    def typeNP = '1'

    def reportPeriodId = declarationData.reportPeriodId

    // Параметры подразделения
    def incomeParams = getDepartmentParam()
    def incomeParamsTable = getDepartmentParamTable(incomeParams.record_id.value)

    if (!readerBank)
        return
    def reorgFormCode = getRefBookValue(5, incomeParamsTable?.REORG_FORM_CODE?.value)?.CODE?.value
    def taxOrganCode = incomeParamsTable?.TAX_ORGAN_CODE?.value
    def taxOrganCodeProm = useTaxOrganCodeProm() ? incomeParamsTable?.TAX_ORGAN_CODE_PROM?.value : taxOrganCode
    def okvedCode = getRefBookValue(34, incomeParamsTable?.OKVED_CODE?.value)?.CODE?.value
    def phone = incomeParamsTable?.PHONE?.value
    def name = incomeParamsTable?.NAME?.value
    def inn = incomeParams?.INN?.value
    def kpp = declarationData.kpp
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

    /** Признак налоговый ли это период. */
    def isTaxPeriod = (reportPeriod != null && reportPeriod.order == 4)

    // Приложение № 5 к Листу 02
    /** ОбРасч. Столбец «Признак расчёта». */
    def obRasch
    /** НаимОП. Столбец «Подразделение территориального банка». */
    def naimOP
    /** КППОП. Столбец «КПП». */
    def kppop
    /** ОбязУплНалОП. Столбец «Обязанность по уплате налога». */
    def obazUplNalOP
    /** ДоляНалБаз. Столбец «Доля налоговой базы (%)». */
    def dolaNalBaz
    /** НалБазаДоля. Столбец «Налоговая база исходя из доли (руб.)». */
    def nalBazaDola
    /** СтавНалСубРФ. Столбец «Ставка налога % в бюджет субъекта (%)». */
    def stavNalSubRF
    /** СумНал. Столбец «Сумма налога». */
    def sumNal
    /** НалНачислСубРФ. Столбец «Начислено налога в бюджет субъекта (руб.)». */
    def nalNachislSubRF
    /** СумНалП. Столбец «Сумма налога к доплате». */
    def sumNalP
    /** НалВыплВнеРФ. Столбец «Сумма налога, выплаченная за пределами России и засчитываемая в уплату налога». */
    def nalViplVneRF
    /** МесАвПлат. Столбец «Ежемесячные авансовые платежи в квартале, следующем за отчётным периодом (текущий отчёт)». */
    def mesAvPlat
    /** МесАвПлат1КвСлед. Столбец «Ежемесячные авансовые платежи на I квартал следующего налогового периода». */
    def mesAvPlat1CvSled
    /** НалБазаОрг. */
    def nalBazaOrg

    // провека наличия в декларации банка данных для данного подразделения
    def findCurrentDepo = false
    /** Данные Приложения № 5 к Листу 02 из декларации Банка для данного подразделения. */
    try {
        def elements = [:]
        while(readerBank.hasNext()) {
            if (readerBank.startElement){
                elements[readerBank.name.localPart] = true
                if (isCurrentNode(['Документ', 'Прибыль', 'РасчНал', 'РаспрНалСубРФ'], elements)) {
                    /** КППОП. Столбец «КПП». */
                    kppop = getXmlValue(readerBank, "КППОП")
                    if (kpp != null && kpp.equals(kppop)) {
                        findCurrentDepo = true
                        /** ОбРасч. Столбец «Признак расчёта». */
                        obRasch = getXmlValue(readerBank, "ОбРасч")
                        /** НаимОП. Столбец «Подразделение территориального банка». */
                        naimOP = getXmlValue(readerBank, "НаимОП")
                        /** ОбязУплНалОП. Столбец «Обязанность по уплате налога». */
                        obazUplNalOP = getXmlValue(readerBank, "ОбязУплНалОП")
                        /** ДоляНалБаз. Столбец «Доля налоговой базы (%)». */
                        dolaNalBaz = getXmlValue(readerBank, "ДоляНалБаз")
                        /** НалБазаДоля. Столбец «Налоговая база исходя из доли (руб.)». */
                        nalBazaDola = getXmlValue(readerBank, "НалБазаДоля")
                        /** СтавНалСубРФ. Столбец «Ставка налога % в бюджет субъекта (%)». */
                        stavNalSubRF = getXmlValue(readerBank, "СтавНалСубРФ")
                        /** СумНал. Столбец «Сумма налога». */
                        sumNal = getXmlValue(readerBank, "СумНал")
                        /** НалНачислСубРФ. Столбец «Начислено налога в бюджет субъекта (руб.)». */
                        nalNachislSubRF = getXmlValue(readerBank, "НалНачислСубРФ")
                        /** СумНалП. Столбец «Сумма налога к доплате». */
                        sumNalP = getXmlValue(readerBank, "СумНалП")
                        /** НалВыплВнеРФ. Столбец «Сумма налога, выплаченная за пределами России и засчитываемая в уплату налога». */
                        nalViplVneRF = getXmlValue(readerBank, "НалВыплВнеРФ")
                        /** МесАвПлат. Столбец «Ежемесячные авансовые платежи в квартале, следующем за отчётным периодом (текущий отчёт)». */
                        mesAvPlat = getXmlValue(readerBank, "МесАвПлат")
                        /** МесАвПлат1КвСлед. Столбец «Ежемесячные авансовые платежи на I квартал следующего налогового периода». */
                        mesAvPlat1CvSled = getXmlValue(readerBank, "МесАвПлат1КвСлед")
                        /** НалБазаОрг. */
                        nalBazaOrg = getXmlValue(readerBank, "НалБазаОрг")
                        break
                    }
                }
            }
            if (readerBank.endElement){
                elements[readerBank.name.localPart] = false
            }
            readerBank.next()
        }
    } finally {
        readerBank.close()
    }
    if (!findCurrentDepo) {
        logger.error("В декларации Банка отсутствуют данные для подразделения: $name (в приложении № 5 к Листу 02).")
        return
    }

    // Данные налоговых форм.

    def formDataCollection = declarationService.getAcceptedFormDataSources(declarationData)

    /** Сводная налоговая формы Банка «Расчёт распределения авансовых платежей и налога на прибыль по обособленным подразделениям организации». */
    def dataRowsAdvance = getDataRows(formDataCollection, 500)

    // Расчет значений для текущей декларации.

    // Период
    def period = 0
    if (reorgFormCode != null) {
        period = 50
    } else if (reportPeriod.order != null) {
        def values = [21, 31, 33, 34]
        period = values[reportPeriod.order - 1]
    }

    /** ПрПодп. */
    def prPodp = signatoryId

    if (xml == null) {
        return
    }

    // Формирование XML'ки.

    def builder = new MarkupBuilder(xml)
    builder.Файл(
            ИдФайл : declarationService.generateXmlFileId(19, declarationData.departmentReportPeriodId, taxOrganCodeProm, taxOrganCode, declarationData.kpp),
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
                    [ОКВЭД : okvedCode] + (phone ? [Тлф : phone] : [:])) {

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
                        [Фамилия : signatorySurname, Имя : signatoryFirstName] +
                                (signatoryLastName != null ? [Отчество : signatoryLastName] : [:]))
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
                    НалПУАв(ОКТМО: oktmo) {
                        // 0..1
                        ФедБдж(
                                КБК: kbk,
                                НалПУ: empty)

                        def appl5List02Row070 = empty
                        if (dataRowsAdvance != null) {
                            def rowForAvPlat = dataRowsAdvance.find { row -> declarationData.kpp.equals(row.kpp) }
                            appl5List02Row070 = (rowForAvPlat != null && (rowForAvPlat.taxSumToPay || rowForAvPlat.taxSumToReduction)) ? (rowForAvPlat.taxSumToPay ?: -rowForAvPlat.taxSumToReduction) : 0
                        }
                        // 0..1
                        СубБдж(
                                КБК: kbk2,
                                НалПУ: appl5List02Row070)
                    }
                    // Раздел 1. Подраздел 1.1 - конец

                    // Раздел 1. Подраздел 1.2
                    if (period != 34 && period != 50) {
                        // 0..n
                        // КвИсчислАв : '00',
                        НалПУМес(ОКТМО : oktmo) {
                            // 0..1
                            ФедБдж(
                                    КБК : kbk,
                                    АвПлат1 : empty,
                                    АвПлат2 : empty,
                                    АвПлат3 : empty)

                            def avPlat1 = empty
                            def avPlat2 = empty
                            def avPlat3 = empty
                            if (!isTaxPeriod && dataRowsAdvance != null) {
                                // получение строки подразделения, затем значение столбца «Ежемесячные авансовые платежи в квартале, следующем за отчётным периодом (текущий отчёт)»
                                def rowForAvPlat = dataRowsAdvance.find { row -> declarationData.kpp.equals(row.kpp) }
                                def avPlats = getAvPlats(rowForAvPlat, reportPeriod)
                                avPlat1 = avPlats[0]
                                avPlat2 = avPlats[1]
                                avPlat3 = avPlats[2]
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
                // 0..n
                РаспрНалСубРФ(
                        ТипНП: typeNP,
                        ОбРасч: obRasch,
                        НаимОП: naimOP,
                        КППОП: kppop,
                        ОбязУплНалОП: obazUplNalOP,
                        НалБазаОрг: nalBazaOrg,
                        НалБазаБезЛиквОП: empty,
                        ДоляНалБаз: dolaNalBaz,
                        НалБазаДоля: nalBazaDola,
                        СтавНалСубРФ: stavNalSubRF,
                        СумНал: sumNal,
                        НалНачислСубРФ: nalNachislSubRF,
                        НалВыплВнеРФ: nalViplVneRF,
                        СумНалП: sumNalP,
                        МесАвПлат: mesAvPlat,
                        МесАвПлат1КвСлед: mesAvPlat1CvSled)
                // Приложение № 5 к Листу 02 - конец
            }
        }
    }
}

/**
 * Ищет точное ли совпадение узлов дерева xml c текущими незакрытыми элементами
 * @param nodeNames ожидаемые элементы xml
 * @param elements незакрытые элементы
 * @return
 */
boolean isCurrentNode(List<String> nodeNames, Map<String, Boolean> elements) {
    nodeNames.add('Файл')
    def enteredNodes = elements.findAll { it.value }.keySet() // узлы в которые вошли, но не вышли еще
    return enteredNodes.containsAll(nodeNames) && enteredNodes.size() == nodeNames.size()
}

// Получить округленное, целочисленное значение.
def getLong(def value) {
    if (value == null) {
        return 0
    }
    return roundValue(value, 0)
}

/**
 * Получить сумму значении столбца по указанным строкам.
 *
 * @param dataRows строки нф
 * @param columnCode псевдоним столбца по которому отбирать данные для суммирования
 * @param columnSum псевдоним столбца значения которого надо суммировать
 * @param codes список значении, которые надо учитывать при суммировании
 */
def getSumRowsByCol(def dataRows, def columnCode, def columnSum, def codes) {
    def result = 0
    if (!dataRows) {
        return result
    }
    dataRows.each { row ->
        def cell = row.getCell(columnSum)
        if (row.getCell(columnCode).value in (String [])codes && !cell.hasValueOwner()) {
            result += (cell.value ?: 0)
        }
    }
    return result
}

/**
 * Получить сумму графы 9 формы доходы сложные.
 *
 * @param dataRows строки нф доходы сложные
 * @param codes коды которые надо учитывать при суммировании
 */
def getComplexIncomeSumRows9(def dataRows, def codes) {
    return getSumRowsByCol(dataRows, 'incomeTypeId', 'incomeTaxSumS', codes)
}

/**
 * Получить сумму графы 9 формы расходы сложные.
 *
 * @param dataRows строки нф расходы сложные
 * @param codes коды которые надо учитывать при суммировании
 */
def getComplexConsumptionSumRows9(def dataRows, def codes) {
    return getSumRowsByCol(dataRows, 'consumptionTypeId', 'consumptionTaxSumS', codes)
}

/**
 * Получить сумму графы 8 формы доходы простые.
 *
 * @param dataRows строки нф доходы простые
 * @param codes коды которые надо учитывать при суммировании
 */
def getSimpleIncomeSumRows8(def dataRows, def codes) {
    return getSumRowsByCol(dataRows, 'incomeTypeId', 'rnu4Field5Accepted', codes)
}

/**
 * Получить сумму графы 8 формы расходы простые.
 *
 * @param dataRows строки нф расходы простые
 * @param codes коды которые надо учитывать при суммировании
 */
def getSimpleConsumptionSumRows8(def dataRows, def codes) {
    return getSumRowsByCol(dataRows, 'consumptionTypeId', 'rnu5Field5Accepted', codes)
}

/** Подсчет простых расходов: сумма(графа 8 + графа 5 - графа 6). */
def getCalculatedSimpleConsumption(def dataRowsSimple, def codes) {
    def result = 0
    if (dataRowsSimple == null) {
        return result
    }
    dataRowsSimple.each { row ->
        if (row.getCell('consumptionTypeId').value in (String [])codes) {
            result +=
                    (row.rnu5Field5Accepted ?: 0) +
                            (row.rnu7Field10Sum ?: 0) -
                            (row.rnu7Field12Accepted ?: 0)
        }
    }
    return result
}

// Получить значения для АвПлат1 (220), АвПлат2 (230), АвПлат3 (240)
def getAvPlats(def row, def reportPeriod) {
    def appl5List02Row120 = (row != null && row.everyMontherPaymentAfterPeriod != null ? row.everyMontherPaymentAfterPeriod : 0)
    def avPlat1
    def avPlat2
    def avPlat3
    if (reportPeriod.taxPeriod.year >= 2015 || reportPeriod.taxPeriod.year == 2015 && reportPeriod.order > 2) {
        // с 9 месяцев 2015
        def a = roundValue(appl5List02Row120 / 3, 0)
        avPlat1 = a
        avPlat2 = a
        avPlat3 = a
        def b = a * 3
        def c = appl5List02Row120 - b
        if (c == -1) {
            avPlat1--
        } else if (c == 1) {
            avPlat3++
        }
    } else {
        // до 9 месяцев 2015
        avPlat1 = (long) appl5List02Row120 / 3
        avPlat2 = avPlat1
        avPlat3 = getLong(appl5List02Row120 - avPlat1 - avPlat2)
    }
    return [avPlat1, avPlat2, avPlat3]
}

/**
 * Получить xml декларации.
 *
 * @param reportPeriodId
 * @param departmentId
 */
def getXmlStreamReader(def reportPeriodId, def departmentId, def acceptedOnly, def anyPrevDeclaration) {
    if (reportPeriodId != null) {
        // вид декларации 11 - декларация банка
        def declarationTypeId = 11
        def xml = getStreamReader(declarationTypeId, departmentId, reportPeriodId, acceptedOnly)
        if (xml != null) {
            return xml
        }
        // для новой декларации можно поискать в прошлом периоде другую декларацию (обычную Банка)
        if (anyPrevDeclaration) {
            declarationTypeId = 2
            return getStreamReader(declarationTypeId, departmentId, reportPeriodId, acceptedOnly)
        }
    }
    return null
}

def getStreamReader(def declarationTypeId, def departmentId, def reportPeriodId, def acceptedOnly) {
    def declarationData = declarationService.getLast(declarationTypeId, departmentId, reportPeriodId)
    if (declarationData != null && declarationData.id != null && (!acceptedOnly || declarationData.accepted)) {
        return declarationService.getXmlStreamReader(declarationData.id)
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

List<String> getErrorTable(record) {
    List<String> errorList = new ArrayList<String>()
    if (record.NAME?.value == null || record.NAME.value.isEmpty()) {
        errorList.add("«Наименование подразделения»")
    }
    if (record.OKTMO == null || record.OKTMO.value == null) {
        errorList.add("«Код по ОКТМО»")
    }
    if (record.TAX_ORGAN_CODE?.value == null || record.TAX_ORGAN_CODE.value.isEmpty()) {
        errorList.add("«Код налогового органа»")
    }
    if (record.OKVED_CODE?.value == null) {
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
    if (record.SIGNATORY_ID?.value == null) {
        errorList.add("«Признак лица подписавшего документ»")
    }
    if (record.SIGNATORY_SURNAME?.value == null || record.SIGNATORY_SURNAME.value.isEmpty()) {
        errorList.add("«Фамилия подписанта»")
    }
    if (record.SIGNATORY_FIRSTNAME?.value == null || record.SIGNATORY_FIRSTNAME.value.isEmpty()) {
        errorList.add("«Имя подписанта»")
    }
    def signatoryId = getRefBookValue(35, record?.SIGNATORY_ID?.value)?.CODE?.value
    if ((signatoryId != null && signatoryId != 1) && (record.APPROVE_DOC_NAME?.value == null || record.APPROVE_DOC_NAME.value.isEmpty())) {
        errorList.add("«Наименование документа, подтверждающего полномочия представителя»")
    }
    if (record.TAX_PLACE_TYPE_CODE?.value == null) {
        errorList.add("«Код места, по которому представляется документ»")
    }
    errorList
}

List<String> getErrorDepartment(record) {
    List<String> errorList = new ArrayList<String>()

    if (record.INN?.stringValue == null || record.INN.stringValue.isEmpty()) {
        errorList.add("«ИНН»")
    }
    if (record.TAX_RATE?.value == null) {
        errorList.add("«Ставка налога»")
    }
    errorList
}

List<String> getErrorVersion(record) {
    List<String> errorList = new ArrayList<String>()
    if (record.FORMAT_VERSION == null || record.FORMAT_VERSION.value == null || !record.FORMAT_VERSION.value.equals('5.06')) {
        errorList.add("«Версия формата»")
    }
    errorList
}

/**
 * Получение провайдера с использованием кеширования.
 *
 * @param providerId
 * @return
 */
def getProvider(def long providerId) {
    if (!providerCache.containsKey(providerId)) {
        providerCache.put(providerId, refBookFactory.getDataProvider(providerId))
    }
    return providerCache.get(providerId)
}

BigDecimal getXmlDecimal(def reader, String attrName) {
    def value = reader?.getAttributeValue(null, attrName)
    if (!value) {
        return null
    }
    return new BigDecimal(value)
}

String getXmlValue(XMLStreamReader reader, String attrName) {
    return reader?.getAttributeValue(null, attrName)
}

/** Получить строки формы. */
def getDataRows(def formDataCollection, def formTypeId) {
    def formList = formDataCollection?.records?.findAll { it.formType.id == formTypeId }
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
            throw new Exception("Ошибка при получении настроек обособленного подразделения. Настройки подразделения заполнены не полностью")
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
            throw new Exception("Ошибка при получении настроек обособленного подразделения. Настройки подразделения заполнены не полностью")
        }
        departmentParamTable = departmentParamTableList.get(0)
    }
    return departmentParamTable
}

@Field
def declarationReportPeriod

boolean useTaxOrganCodeProm() {
    if (declarationReportPeriod == null) {
        declarationReportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
    }
    return (declarationReportPeriod?.taxPeriod?.year > 2015 || declarationReportPeriod?.order > 2)
}