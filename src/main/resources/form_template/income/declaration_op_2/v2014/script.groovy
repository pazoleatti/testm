package form_template.income.declaration_op_2.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field
import groovy.xml.MarkupBuilder

/**
 * Декларация по налогу на прибыль (ОП) (год 2014)
 * Формирование XML для декларации налога на прибыль уровня обособленного подразделения.
 *
 * declarationTemplateId=21707
 *
 * @author Bulat.Kinzyabulatov
 */

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
    case FormDataEvent.PRE_CALCULATION_CHECK:
        checkDepartmentParams(LogLevel.WARNING)
        checkDeclarationBank()
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
def checkDeclarationBank() {
    /** Отчётный период. */
    def reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)

    /** вид декларации 11 - декларация банка */
    def declarationTypeId = 11

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

// Логические проверки.
void logicCheck() {
    // получение данных из xml'ки
    def xmlData = getXmlData(declarationData.reportPeriodId, declarationData.departmentId, false, false)
    if(xmlData == null){
        return
    }
    def empty = 0

    // Проверки Листа 02 - Превышение суммы налога, выплаченного за пределами РФ (всего)
    def nalVipl311 = getXmlValue(xmlData.Документ.Прибыль.РасчНал.@НалВыпл311.text())
    def nalIschisl = getXmlValue(xmlData.Документ.Прибыль.РасчНал.@НалИсчисл.text())
    if (nalVipl311 != null && nalIschisl != null && nalVipl311 > nalIschisl) {
        logger.error('Сумма налога, выплаченная за пределами РФ (всего) превышает сумму исчисленного налога на прибыль (всего)!')
    }

    // Проверки Листа 02 - Превышение суммы налога, выплаченного за пределами РФ (в федеральный бюджет)
    def nalVipl311FB = getXmlValue(xmlData.Документ.Прибыль.РасчНал.@НалВыпл311ФБ.text())
    def nalIschislFB = getXmlValue(xmlData.Документ.Прибыль.РасчНал.@НалИсчислФБ.text())
    if (nalVipl311FB != null && nalIschislFB != null &&
            nalVipl311FB > nalIschislFB) {
        logger.error('Сумма налога, выплаченная за пределами РФ (в федеральный бюджет) превышает сумму исчисленного налога на прибыль (в федеральный бюджет)!')
    }

    // Проверки Листа 02 - Превышение суммы налога, выплаченного за пределами РФ (в бюджет субъекта РФ)
    def nalVipl311Sub = getXmlValue(xmlData.Документ.Прибыль.РасчНал.@НалВыпл311Суб.text())
    def nalIschislSub = getXmlValue(xmlData.Документ.Прибыль.РасчНал.@НалИсчислСуб.text())
    if (nalVipl311Sub != null && nalIschislSub != null &&
            nalVipl311Sub > nalIschislSub) {
        logger.error('Сумма налога, выплаченная за пределами РФ (в бюджет субъекта РФ) превышает сумму исчисленного налога на прибыль (в бюджет субъекта РФ)!')
    }

    // Проверки Приложения № 1 к Листу 02 - Превышение суммы составляющих над общим показателем («Внереализационные доходы (всего)»)
    // (ВнеРеалДохПр + ВнеРеалДохСт + ВнеРеалДохБезв + ВнеРеалДохИзл + ВнеРеалДохВРасх + ВнеРеалДохРынЦБДД + ВнеРеалДохКор) < ВнеРеалДохВс
    def vneRealDohSt = getXmlValue(xmlData.Документ.Прибыль.РасчНал.ДохРеалВнеРеал.ДохВнеРеал.@ВнеРеалДохСт.text())
    def vneRealDohBezv = getXmlValue(xmlData.Документ.Прибыль.РасчНал.ДохРеалВнеРеал.ДохВнеРеал.@ВнеРеалДохБезв.text())
    def vneRealDohIzl = getXmlValue(xmlData.Документ.Прибыль.РасчНал.ДохРеалВнеРеал.ДохВнеРеал.@ВнеРеалДохИзл.text())
    def vneRealDohVRash = getXmlValue(xmlData.Документ.Прибыль.РасчНал.ДохРеалВнеРеал.ДохВнеРеал.@ВнеРеалДохВРасх.text())
    def vneRealDohRinCBDD = getXmlValue(xmlData.Документ.Прибыль.РасчНал.ДохРеалВнеРеал.ДохВнеРеал.@ВнеРеалДохРынЦБДД.text())
    def vneRealDohCor = getXmlValue(xmlData.Документ.Прибыль.РасчНал.ДохРеалВнеРеал.ДохВнеРеал.@ВнеРеалДохКор.text())
    def vneRealDohVs = getXmlValue(xmlData.Документ.Прибыль.РасчНал.ДохРеалВнеРеал.ДохВнеРеал.@ВнеРеалДохВс.text())
    if (vneRealDohSt != null && vneRealDohBezv != null && vneRealDohIzl != null && vneRealDohVRash != null &&
            vneRealDohRinCBDD != null && vneRealDohCor != null && vneRealDohVs != null &&
            (empty + vneRealDohSt + vneRealDohBezv +
                    vneRealDohIzl + vneRealDohVRash + vneRealDohRinCBDD +
                    vneRealDohCor) > vneRealDohVs) {
        logger.error('Показатель «Внереализационные доходы (всего)» меньше суммы его составляющих!')
    }

    // Проверки Приложения № 2 к Листу 02 - Превышение суммы составляющих над общим показателем («Косвенные расходы (всего)»)
    // КосвРасхВс < (Налоги + РасхКапВл10 + РасхКапВл30 + РасхТрудИнв + РасхОргИнв + РасхЗемУчВс + НИОКР)
    def cosvRashVs = getXmlValue(xmlData.Документ.Прибыль.РасчНал.РасхРеалВнеРеал.РасхРеал.@КосвРасхВс.text())
    def nalogi = getXmlValue(xmlData.Документ.Прибыль.РасчНал.РасхРеалВнеРеал.РасхРеал.@Налоги.text())
    def rashCapVl10 = getXmlValue(xmlData.Документ.Прибыль.РасчНал.РасхРеалВнеРеал.РасхРеал.@РасхКапВл10.text())
    def rashCapVl30 = getXmlValue(xmlData.Документ.Прибыль.РасчНал.РасхРеалВнеРеал.РасхРеал.@РасхКапВл30.text())
    def rashZemUchVs = getXmlValue(xmlData.Документ.Прибыль.РасчНал.РасхРеалВнеРеал.РасхРеал.@РасхЗемУчВс.text())
    if (cosvRashVs != null && nalogi != null && rashCapVl10 != null && rashCapVl30 != null && rashZemUchVs != null &&
            cosvRashVs < (nalogi + rashCapVl10 + rashCapVl30 +
            empty + empty + rashZemUchVs + empty)) {
        logger.error('Показатель «Косвенные расходы (всего)» меньше суммы его составляющих!')
    }

    // Проверки Приложения № 2 к Листу 02 - Превышение суммы составляющих над общим показателем («Внереализационные расходы (всего)»)
    // (РасхВнереалПрДО + РасхВнереалРзрв + УбытРеалПравТр + РасхЛиквОС + РасхШтраф + РасхРынЦБДД) > РасхВнеРеалВс
    def rashVnerealPrDO = getXmlValue(xmlData.Документ.Прибыль.РасчНал.РасхРеалВнеРеал.РасхВнеРеал.@РасхВнереалПрДО.text())
    def ubitRealPravTr = getXmlValue(xmlData.Документ.Прибыль.РасчНал.РасхРеалВнеРеал.РасхВнеРеал.@УбытРеалПравТр.text())
    def rashLikvOS = getXmlValue(xmlData.Документ.Прибыль.РасчНал.РасхРеалВнеРеал.РасхВнеРеал.@РасхЛиквОС.text())
    def rashShtraf = getXmlValue(xmlData.Документ.Прибыль.РасчНал.РасхРеалВнеРеал.РасхВнеРеал.@РасхШтраф.text())
    def rashRinCBDD = getXmlValue(xmlData.Документ.Прибыль.РасчНал.РасхРеалВнеРеал.РасхВнеРеал.@РасхРынЦБДД.text())
    def rashVnerealVs = getXmlValue(xmlData.Документ.Прибыль.РасчНал.РасхРеалВнеРеал.РасхВнеРеал.@РасхВнеРеалВс.text())
    if (rashVnerealPrDO != null && ubitRealPravTr != null && rashLikvOS != null &&
            rashShtraf != null && rashRinCBDD != null && rashVnerealVs != null &&
            (rashVnerealPrDO + empty + ubitRealPravTr + rashLikvOS + rashShtraf + rashRinCBDD) > rashVnerealVs) {
        logger.error('Показатель «Внереализационные расходы (всего)» меньше суммы его составляющих!')
    }

    // Проверки Приложения № 3 к Листу 02 - Проверка отрицательной разницы (убыток) от реализации права требования
    // долга до наступления срока платежа, определенной налогоплательщиком в соответствии с п. 1 статьи 279 НК
    // строка 100 = ВыручРеалПТДоСр = viruchRealPTDoSr
    // строка 120 = СтоимРеалПТДоСр = stoimRealPTDoSr
    // строка 140 = Убыт1Соот269	= ubit1Soot269
    // строка 150 = Убыт1Прев269	= ubit1Prev269
    def stoimRealPTDoSr = getXmlValue(xmlData.Документ.Прибыль.РасчНал.РасчРасхОпер.СтоимРеалПТ.@СтоимРеалПТДоСр.text())
    def viruchRealPTDoSr = getXmlValue(xmlData.Документ.Прибыль.РасчНал.РасчРасхОпер.ВыручРеалПТ.@ВыручРеалПТДоСр.text())
    def ubit1Prev269 = getXmlValue(xmlData.Документ.Прибыль.РасчНал.РасчРасхОпер.УбытРеалПТ1.@Убыт1Прев269.text())
    def ubit1Soot269 = getXmlValue(xmlData.Документ.Прибыль.РасчНал.РасчРасхОпер.УбытРеалПТ1.@Убыт1Соот269.text())
    if (stoimRealPTDoSr != null && viruchRealPTDoSr != null && ubit1Prev269 != null && ubit1Soot269 != null &&
            (stoimRealPTDoSr > viruchRealPTDoSr ?
                    (ubit1Prev269 != stoimRealPTDoSr - viruchRealPTDoSr - ubit1Soot269)
                    : (ubit1Prev269 != 0))) {
        logger.warn('В Приложении 3 к Листу 02 строка 150 неверно указана сумма!')
    }

    // Проверки Приложения № 3 к Листу 02 - Проверка отрицательной разницы (убыток), полученной налогоплательщиком
    // при уступке права требования долга после наступления срока платежа в соответствии с п. 2 статьи 279 НК
    // строка 110 = ВыручРеалПТПосСр = viruchRealPTPosSr
    // строка 130 = СтоимРеалПТПосСр = stoimRealPTPosSr
    // строка 160 = Убыт2РеалПТ		 = ubit2RealPT
    def reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
    if (reportPeriod.order == 4 && reportPeriod.taxPeriod.year == 2014) {
        def stoimRealPTPosSr = getXmlValue(xmlData.Документ.Прибыль.РасчНал.РасчРасхОпер.СтоимРеалПТ.@СтоимРеалПТПосСр.text())
        def viruchRealPTPosSr = getXmlValue(xmlData.Документ.Прибыль.РасчНал.РасчРасхОпер.ВыручРеалПТ.@ВыручРеалПТПосСр.text())
        def ubit2RealPT = getXmlValue(xmlData.Документ.Прибыль.РасчНал.РасчРасхОпер.УбытРеалПТ2.@Убыт2РеалПТ.text())
        if (stoimRealPTPosSr != null && viruchRealPTPosSr != null && ubit2RealPT != null &&
                (stoimRealPTPosSr > viruchRealPTPosSr ?
                        (ubit2RealPT != stoimRealPTPosSr - viruchRealPTPosSr)
                        : (ubit2RealPT != 0))) {
            logger.warn('В Приложении 3 к Листу 02 строка 160 неверно указана сумма!')
        }
    }
}

// Запуск генерации XML.
void generateXML(def xmlBankData) {

    def empty = 0
    def knd = '1151006'
    def kbk = '18210101011011000110'
    def kbk2 = '18210101012021000110'
    def typeNP = '1'

    def reportPeriodId = declarationData.reportPeriodId

    // Параметры подразделения
    def incomeParams = getDepartmentParam()
    def incomeParamsTable = getDepartmentParamTable(incomeParams.record_id.value)

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
            ИдФайл : declarationService.generateXmlFileId(19, declarationData.departmentReportPeriodId, taxOrganCode, declarationData.kpp),
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

                        // 0..1
                        СубБдж(
                                КБК: kbk2,
                                НалПУ: empty)
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
                                // получение строки подразделения "ЦА", затем значение столбца «Ежемесячные авансовые платежи в квартале, следующем за отчётным периодом (текущий отчёт)»
                                def rowForAvPlat = getDataRow(dataRowsAdvance, 'ca')
                                def appl5List02Row120 = (rowForAvPlat != null && rowForAvPlat.everyMontherPaymentAfterPeriod != null ? rowForAvPlat.everyMontherPaymentAfterPeriod : 0)
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

/**
 * Получить xml декларации.
 *
 * @param reportPeriodId
 * @param departmentId
 */
def getXmlData(def reportPeriodId, def departmentId, def acceptedOnly, def anyPrevDeclaration) {
    if (reportPeriodId != null) {
        // вид декларации 11 - декларация банка
        def declarationTypeId = 11
        def xml = getExistedXmlData(declarationTypeId, departmentId, reportPeriodId, acceptedOnly)
        if (xml != null) {
            return xml
        }
        // для новой декларации можно поискать в прошлом периоде другую декларацию (обычную Банка)
        if (anyPrevDeclaration) {
            declarationTypeId = 2
            return getExistedXmlData(declarationTypeId, departmentId, reportPeriodId, acceptedOnly)
        }
    }
    return null
}

def getExistedXmlData(def declarationTypeId, def departmentId, def reportPeriodId, def acceptedOnly) {
    def declarationData = declarationService.getLast(declarationTypeId, departmentId, reportPeriodId)
    if (declarationData != null && declarationData.id != null && (!acceptedOnly || declarationData.accepted)) {
        def xmlString = declarationService.getXmlData(declarationData.id)
        if (xmlString == null) return null
        xmlString = xmlString.replace('<?xml version="1.0" encoding="windows-1251"?>', '')
        return new XmlSlurper().parseText(xmlString)
    }
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

/** Получить числовое значение из xml'ки. */
def getXmlValue(def value) {
    if (!value) {
        return null
    }
    return new BigDecimal(value)
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