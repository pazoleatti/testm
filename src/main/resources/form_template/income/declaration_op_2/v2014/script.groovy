package form_template.income.declaration_op_2.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
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
    def ubit1Prev269 = getXmlValue(xmlData.Документ.Прибыль.РасчНал.РасчРасхОпер.УбытРеалПТ.@Убыт1Прев269.text())
    def ubit1Soot269 = getXmlValue(xmlData.Документ.Прибыль.РасчНал.РасчРасхОпер.УбытРеалПТ.@Убыт1Соот269.text())
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
    def stoimRealPTPosSr = getXmlValue(xmlData.Документ.Прибыль.РасчНал.РасчРасхОпер.СтоимРеалПТ.@СтоимРеалПТПосСр.text())
    def viruchRealPTPosSr = getXmlValue(xmlData.Документ.Прибыль.РасчНал.РасчРасхОпер.ВыручРеалПТ.@ВыручРеалПТПосСр.text())
    def ubit2RealPT = getXmlValue(xmlData.Документ.Прибыль.РасчНал.РасчРасхОпер.УбытРеалПТ.@Убыт2РеалПТ.text())
    if (stoimRealPTPosSr != null && viruchRealPTPosSr != null && ubit2RealPT != null &&
            (stoimRealPTPosSr > viruchRealPTPosSr ?
                    (ubit2RealPT != stoimRealPTPosSr - viruchRealPTPosSr)
                    : (ubit2RealPT != 0))) {
        logger.warn('В Приложении 3 к Листу 02 строка 160 неверно указана сумма!')
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
    def dataRowsAdvance = getDataRows(formDataCollection, 500, FormDataKind.SUMMARY)

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
            ИдФайл : declarationService.generateXmlFileId(19, declarationData.departmentReportPeriodId, declarationData.taxOrganCode, declarationData.kpp),
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
 * Выручка от реализации товаров (работ, услуг) собственного производства (ВырРеалТовСоб).
 *
 * @param dataRows строки нф доходы сложные
 * @param dataRowsSimple строки нф доходы простые
 */
def getVirRealTovSob(def dataRows, def dataRowsSimple) {
    def result = 0.0

    // Код вида дохода = 10633, 10634, 10650, 10670
    result += getComplexIncomeSumRows9(dataRows, [10633, 10634, 10650, 10670])

    // Код вида дохода = 10001, 10006, 10041, 10300, 10310, 10320, 10330, 10340, 10350, 10360, 10370,
    // 10380, 10390, 10450, 10460, 10470, 10480, 10490, 10571, 10580, 10590, 10600, 10610, 10630,
    // 10631, 10632, 10640, 10680, 10690, 10740, 10744, 10748, 10752, 10756, 10760, 10770, 10790,
    // 10800, 11140, 11150, 11160, 11170, 11320, 11325, 11330, 11335, 11340, 11350, 11360, 11370, 11375
    result += getSimpleIncomeSumRows8(dataRowsSimple, [10001, 10006, 10041, 10300, 10310, 10320,
                                                       10330, 10340, 10350, 10360, 10370, 10380, 10390, 10450, 10460, 10470, 10480, 10490,
                                                       10571, 10580, 10590, 10600, 10610, 10630, 10631, 10632, 10640, 10680, 10690, 10740,
                                                       10744, 10748, 10752, 10756, 10760, 10770, 10790, 10800, 11140, 11150, 11160, 11170,
                                                       11320, 11325, 11330, 11335, 11340, 11350, 11360, 11370, 11375])

    // Код вида доходов = 10001, 10006, 10300, 10310, 10320, 10330, 10340, 10350, 10360, 10470,
    // 10480, 10490, 10571, 10590, 10610, 10640, 10680, 10690, 11340, 11350, 11370, 11375
    def codes = [10001, 10006, 10300, 10310, 10320, 10330, 10340, 10350, 10360, 10470, 10480,
                 10490, 10571, 10590, 10610, 10640, 10680, 10690, 11340, 11350, 11370, 11375]

    // графа 5
    result += getSumRowsByCol(dataRowsSimple, 'incomeTypeId', 'rnu6Field10Sum', codes)
    // графа 6
    result -= getSumRowsByCol(dataRowsSimple, 'incomeTypeId', 'rnu6Field12Accepted', codes)

    return getLong(result)
}

/**
 * Получить внереализационные доходы (ДохВнереал, ВнеРеалДохВс).
 *
 * @param dataRows строки нф доходы сложные
 * @param dataRowsSimple строки нф доходы простые
 */
def getDohVnereal(def dataRows, def dataRowsSimple) {
    def result = 0.0

    // Код вида дохода = 11405, 11410, 11415, 13040, 13045, 13050, 13055, 13060, 13065,
    // 13070, 13090, 13100, 13110, 13120, 13250, 13650, 13655, 13660, 13665, 13670,
    // 13675, 13680, 13685, 13690, 13695, 13700, 13705, 13710, 13715, 13720, 13780,
    // 13785, 13790, 13940, 13950, 13960, 13970, 13980, 13990, 14140, 14170, 14180,
    // 14190, 14200, 14210, 14220, 14230, 14240, 14250, 14260, 14270, 14280, 14290,
    // 14300, 14310, 14320
    result += getComplexIncomeSumRows9(dataRows, [11405, 11410, 11415, 13040, 13045, 13050, 13055,
                                                  13060, 13065, 13070, 13090, 13100, 13110, 13120, 13250, 13650, 13655, 13660, 13665,
                                                  13670, 13675, 13680, 13685, 13690, 13695, 13700, 13705, 13710, 13715, 13720, 13780,
                                                  13785, 13790, 13940, 13950, 13960, 13970, 13980, 13990, 14140, 14170, 14180, 14190,
                                                  14200, 14210, 14220, 14230, 14240, 14250, 14260, 14270, 14280, 14290, 14300, 14310, 14320])

    // Код вида дохода = 11380, 11385, 11390, 11395, 11400, 11420, 11430, 11840, 11850, 11855,
    // 11860, 11870, 11880, 11930, 11970, 12000, 12010, 12030, 12050, 12070, 12090, 12110, 12130,
    // 12150, 12170, 12190, 12210, 12230, 12250, 12270, 12290, 12320, 12340, 12360, 12390, 12400,
    // 12410, 12420, 12430, 12830, 12840, 12850, 12860, 12870, 12880, 12890, 12900, 12910, 12920,
    // 12930, 12940, 12950, 12960, 12970, 12980, 12985, 12990, 13000, 13010, 13020, 13030, 13035,
    // 13080, 13130, 13140, 13150, 13160, 13170, 13180, 13190, 13230, 13240, 13290, 13300, 13310, 13320, 13330,
    // 13340, 13400, 13410, 13725, 13730, 13920, 13925, 13930, 14000, 14010, 14015, 14020, 14030, 14040,
    // 14050, 14060, 14070, 14080, 14090, 14100, 14110, 14120, 14130, 14150, 14160
    result += getSimpleIncomeSumRows8(dataRowsSimple, [11380, 11385, 11390, 11395, 11400, 11420,
                                                       11430, 11840, 11850, 11855, 11860, 11870, 11880, 11930, 11970, 12000, 12010, 12030,
                                                       12050, 12070, 12090, 12110, 12130, 12150, 12170, 12190, 12210, 12230, 12250, 12270,
                                                       12290, 12320, 12340, 12360, 12390, 12400, 12410, 12420, 12430, 12830, 12840, 12850,
                                                       12860, 12870, 12880, 12890, 12900, 12910, 12920, 12930, 12940, 12950, 12960, 12970,
                                                       12980, 12985, 12990, 13000, 13010, 13020, 13030, 13035, 13080, 13130, 13140, 13150, 13160, 13170,
                                                       13180, 13190, 13230, 13240, 13290, 13300, 13310, 13320, 13330, 13340, 13400, 13410,
                                                       13725, 13730, 13920, 13925, 13930, 14000, 14010, 14015, 14020, 14030, 14040, 14050, 14060,
                                                       14070, 14080, 14090, 14100, 14110, 14120, 14130, 14150, 14160])

    // Код вида дохода = 11860, 11870, 11880, 11930, 11970, 12000, 13930, 14020, 14030, 14040, 14050,
    // 14060, 14070, 14080, 14090, 14100, 14110, 14130, 14150, 14160
    def codes = [11860, 11870, 11880, 11930, 11970, 12000, 13930, 14020, 14030, 14040, 14050,
                 14060, 14070, 14080, 14090, 14100, 14110, 14130, 14150, 14160]
    // графа 5
    result += getSumRowsByCol(dataRowsSimple, 'incomeTypeId', 'rnu6Field10Sum', codes)
    // графа 6
    result -= getSumRowsByCol(dataRowsSimple, 'incomeTypeId', 'rnu6Field12Accepted', codes)

    return getLong(result)
}

/**
 * Получить доходы, исключаемые из прибыли (ДохИсклПриб).
 *
 * @param dataRowsComplex строки нф доходы сложные
 * @param dataRowsSimple строки нф доходы простые
 */
def getDohIsklPrib(def dataRowsComplex, def dataRowsSimple) {
    def result = 0.0

    if (dataRowsComplex != null) {
        // Код вида доходов = 13655, 13660, 13665, 13675, 13680, 13685, 13690,
        // 13695, 13705, 13710, 13780, 13785, 13790
        result += getComplexIncomeSumRows9(dataRowsComplex,
                [13655, 13660, 13665, 13675, 13680, 13685, 13690, 13695, 13705, 13710, 13780, 13785, 13790])
    }
    if (dataRowsSimple != null) {
        // Код вида дохода = 14000
        result += getSumRowsByCol(dataRowsSimple, 'incomeTypeId', 'rnu4Field5Accepted', [14000, 14010, 14015])
    }
    return getLong(result)
}

/**
 * Получить внереализационные расходы (РасхВнереалВС).
 *
 * @param dataRows строки нф расходы сложные
 * @param dataRowsSimple строки нф расходы простые
 */
def getRashVnerealVs(def dataRows, def dataRowsSimple) {
    def result = 0.0

    // Код вида расхода = 22492, 22500, 22505, 22585, 22590, 22595, 22660, 22664, 22668,
    // 22670, 22690, 22695, 22700, 23120, 23130, 23140, 23240 - графа 9
    result += getComplexConsumptionSumRows9(dataRows, [22492, 22500, 22505, 22585, 22590, 22595, 22660, 22664, 22668,
                                                       22670, 22690, 22695, 22700, 23120, 23130, 23140, 23240])

    // Код вида расхода = 22000, 22010, 22020, 22030, 22040, 22050, 22060, 22070, 22080, 22090, 22100, 22110,
    // 22120, 22130, 22140, 22150, 22160, 22170, 22180, 22190, 22200, 22210, 22220, 22230, 22240, 22250, 22260,
    // 22270, 22280, 22290, 22300, 22310, 22320, 22330, 22340, 22350, 22360, 22370, 22380, 22385, 22390, 22395,
    // 22400, 22405, 22410, 22415, 22420, 22425, 22430, 22435, 22440, 22445, 22450, 22455, 22460, 22465, 22470,
    // 22475, 22480, 22485, 22490, 22496, 22498, 22530, 22534, 22538, 22540, 22544, 22548, 22550, 22560, 22565,
    // 22570, 22575, 22580, 22600, 22610, 22640, 22680, 22710, 22715, 22720, 22750, 22760, 22800, 22810, 22840,
    // 22850, 22860, 22870, 23040, 23050, 23100, 23110, 23200, 23210, 23220, 23230, 23250, 23260, 23270, 23280
    def knu = [ 22000, 22010, 22020, 22030, 22040, 22050, 22060, 22070,
                22080, 22090, 22100, 22110, 22120, 22130, 22140, 22150, 22160, 22170, 22180,
                22190, 22200, 22210, 22220, 22230, 22240, 22250, 22260, 22270, 22280, 22290,
                22300, 22310, 22320, 22330, 22340, 22350, 22360, 22370, 22380, 22385, 22390,
                22395, 22400, 22405, 22410, 22415, 22420, 22425, 22430, 22435, 22440, 22445,
                22450, 22455, 22460, 22465, 22470, 22475, 22480, 22485, 22490, 22496, 22498,
                22530, 22534, 22538, 22540, 22544, 22548, 22550, 22560, 22565, 22570, 22575,
                22580, 22600, 22610, 22640, 22680, 22710, 22715, 22720, 22750, 22760, 22800,
                22810, 22840, 22850, 22860, 22870, 23040, 23050, 23100, 23110, 23200, 23210,
                23220, 23230, 23250, 23260, 23270, 23280 ]
    result += getCalculatedSimpleConsumption(dataRowsSimple, knu)

    // Код вида расхода = 23150, 23160, 23170 - графа 9
    result -= getComplexConsumptionSumRows9(dataRows, [23150, 23160, 23170])

    return getLong(result)
}

/**
 * Получить налоговую базу для исчисления налога (НалБазаИсч, НалБазаОрг).
 *
 * @param row100 налоговая база
 * @param row110 сумма убытка или части убытка, уменьшающего налоговую базу за отчетный (налоговый) период
 */
def getNalBazaIsch(def row100, def row110) {
    def result
    if (row100 != null && row110 != null && (row100 < 0 || row100 == row110)) {
        result = 0.0
    } else {
        result = row100 - row110
    }
    return getLong(result)
}

/**
 * Получить сумму налога на прибыль к доплате в федеральный бюджет (или в бюджет субъекта Российской федерации).
 *
 * @param value1 сумма исчисленного налога на прибыль
 * @param value2 сумма начисленных авансовых платежей за отчетный (налоговый) период
 * @param value3 сумма налога, выплаченная за пределами Российской Федерации и засчитываемая в уплату налога
 */
def getNalDopl(def value1, def value2, value3) {
    def result = 0
    if ((value1 - value2 - value3) > 0) {
        result = value1 - value2 - value3
    }
    return getLong(result)
}

/** Получить сумму исчисленного налога на прибыль, в федеральный бюджет (НалИсчислФБ). */
def getNalIschislFB(def row120, row150) {
    return getLong(row120 * row150 / 100)
}

/**
 * Получить сумму налога на прибыль к уменьшению в федеральный бюджет (или в бюджет субъекта Российской федерации).
 *
 * @param value1 сумма начисленных авансовых платежей за отчетный (налоговый) период
 * @param value2 сумма налога, выплаченная за пределами Российской Федерации и засчитываемая в уплату налога
 * @param value3 сумма исчисленного налога на прибыль
 */
def getNalUmen(def value1, def value2, value3) {
    def result = 0
    if ((value1 + value2 - value3) > 0) {
        result = value1 + value2 - value3
    }
    return getLong(result)
}

/**
 * Косвенные расходы, всего (КосвРасхВс).
 *
 * @param dataRows строки нф расходы сложные
 * @param dataRowsSimple строки нф расходы простые
 */
def getCosvRashVs(def dataRows, def dataRowsSimple) {
    def result = 0

    // Код вида расхода = 20320, 20321, 20470, 20750, 20755, 20760, 20765, 20770,
    // 20775, 20780, 20785, 21210, 21280, 21345, 21355, 21365, 21370, 21375, 21380, 21630, 21640
    result += getComplexConsumptionSumRows9(dataRows, [20320, 20321, 20470, 20750, 20755, 20760, 20765,
                                                       20770, 20775, 20780, 20785, 21210, 21280, 21345, 21355, 21365, 21370, 21375, 21380, 21630, 21640])

    // Код вида расхода = 20291, 20300, 20310, 20330, 20332, 20334, 20336, 20338,
    // 20339, 20340, 20360, 20364, 20368, 20370, 20430, 20434, 20438, 20440, 20442,
    // 20446, 20448, 20450, 20452, 20454, 20456, 20458, 20460, 20464, 20468, 20475,
    // 20480, 20485, 20490, 20500, 20510, 20520, 20530, 20540, 20550, 20690, 20694,
    // 20698, 20700, 20710, 20810, 20812, 20814, 20816, 20820, 20825, 20830, 20840,
    // 20850, 20860, 20870, 20880, 20890, 20920, 20940, 20945, 20950, 20960, 20970,
    // 21020, 21025, 21030, 21050, 21055, 21060, 21065, 21080, 21130, 21140, 21150,
    // 21154, 21158, 21170, 21270, 21290, 21295, 21300, 21305, 21310, 21315, 21320,
    // 21325, 21340, 21350, 21360, 21400, 21405, 21410, 21580, 21590, 21600, 21610,
    // 21620, 21660, 21700, 21710, 21720, 21730, 21790, 21800, 21810
    result += getSimpleConsumptionSumRows8(dataRowsSimple, [20291, 20300, 20310, 20330, 20332, 20334,
                                                            20336, 20338, 20339, 20340, 20360, 20364, 20368, 20370, 20430, 20434, 20438, 20440,
                                                            20442, 20446, 20448, 20450, 20452, 20454, 20456, 20458, 20460, 20464, 20468, 20475,
                                                            20480, 20485, 20490, 20500, 20510, 20520, 20530, 20540, 20550, 20690, 20694, 20698,
                                                            20700, 20710, 20810, 20812, 20814, 20816, 20820, 20825, 20830, 20840, 20850, 20860,
                                                            20870, 20880, 20890, 20920, 20940, 20945, 20950, 20960, 20970, 21020, 21025, 21030,
                                                            21050, 21055, 21060, 21065, 21080, 21130, 21140, 21150, 21154, 21158, 21170, 21270,
                                                            21290, 21295, 21300, 21305, 21310, 21315, 21320, 21325, 21340, 21350, 21360, 21400,
                                                            21405, 21410, 21580, 21590, 21600, 21610, 21620, 21660, 21700, 21710, 21720, 21730,
                                                            21790, 21800, 21810])

    // графа 5
    // Код вида дохода = 20300, 20360, 20370, 20430, 20434, 20438, 20440, 20442, 20446, 20448, 20450,
    // 20452, 20454, 20456, 20458, 20460, 20464, 20468, 20475, 20480, 20485, 20490, 20500, 20530,
    // 20540, 20550, 20690, 20694, 20698, 20700, 20710, 20810, 20812, 20814, 20816, 20825, 20830,
    // 20840, 20850, 20870, 20880, 20890, 20950, 20960, 20970, 21020, 21025, 21030, 21050, 21055,
    // 21060, 21065, 21080, 21130, 21140, 21150, 21154, 21158, 21170, 21400, 21405, 21410, 21580,
    // 21590, 21620, 21660, 21700, 21710, 21730, 21790, 21800, 21810
    result += getSumRowsByCol(dataRowsSimple, 'consumptionTypeId', 'rnu7Field10Sum', [20300, 20360, 20370, 20430,
                                                                                      20434, 20438, 20440, 20442, 20446, 20448, 20450, 20452, 20454, 20456, 20458, 20460,
                                                                                      20464, 20468, 20475, 20480, 20485, 20490, 20500, 20530, 20540, 20550, 20690, 20694,
                                                                                      20698, 20700, 20710, 20810, 20812, 20814, 20816, 20825, 20830, 20840, 20850, 20870,
                                                                                      20880, 20890, 20950, 20960, 20970, 21020, 21025, 21030, 21050, 21055, 21060, 21065,
                                                                                      21080, 21130, 21140, 21150, 21154, 21158, 21170, 21400, 21405, 21410, 21580, 21590,
                                                                                      21620, 21660, 21700, 21710, 21730, 21790, 21800, 21810])

    // графа 6
    // Код вида дохода = 20300, 20360, 20370, 20430, 20434, 20438, 20440, 20442, 20446, 20448, 20450,
    // 20452, 20454, 20456, 20458, 20460, 20464, 20468, 20475, 20480, 20485, 20490, 20500, 20530,
    // 20540, 20550, 20690, 20694, 20698, 20700, 20710, 20810, 20812, 20814, 20816, 20825, 20830,
    // 20840, 20850, 20870, 20880, 20890, 20950, 20960, 20970, 21020, 21025, 21030, 21050, 21055,
    // 21060, 21065, 21080, 21130, 21140, 21150, 21154, 21158, 21170, 21400, 21405, 21410, 21580,
    // 21590, 2162021660, 21700, 21710, 21730, 21790, 21800, 21810
    result -= getSumRowsByCol(dataRowsSimple, 'consumptionTypeId', 'rnu7Field12Accepted', [20300, 20360, 20370, 20430,
                                                                                           20434, 20438, 20440, 20442, 20446, 20448, 20450, 20452, 20454, 20456, 20458, 20460,
                                                                                           20464, 20468, 20475, 20480, 20485, 20490, 20500, 20530, 20540, 20550, 20690, 20694,
                                                                                           20698, 20700, 20710, 20810, 20812, 20814, 20816, 20825, 20830, 20840, 20850, 20870,
                                                                                           20880, 20890, 20950, 20960, 20970, 21020, 21025, 21030, 21050, 21055, 21060, 21065,
                                                                                           21080, 21130, 21140, 21150, 21154, 21158, 21170, 21400, 21405, 21410, 21580, 21590,
                                                                                           2162021660, 21700, 21710, 21730, 21790, 21800, 21810])

    return getLong(result)
}

/**
 * Получить значение столбца итоговой строки из налоговой формы.
 *
 * @param dataRows строки нф
 * @param columnName название столбца
 * @return значение столбца
 */
def getTotalFromForm(def dataRows, def columnName) {
    if (dataRows != null && !dataRows.isEmpty()) {
        def totalRow = getDataRow(dataRows, 'total')
        return getLong(totalRow.getCell(columnName).value)
    }
    return 0
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
def getDataRows(def formDataCollection, def formTemplateId, def kind) {
    def formList = formDataCollection?.findAllByFormTypeAndKind(formTemplateId, kind)
    def dataRows = []
    for (def form : formList) {
        dataRows += (formDataService.getDataRowHelper(form)?.getAll()?:[])
    }
    return dataRows.isEmpty() ? null : dataRows
}

def getReportPeriod9month(def reportPeriod) {
    if (reportPeriod == null) {
        return null;
    }
    def code = getRefBookValue(8, reportPeriod.dictTaxPeriodId)?.CODE?.value
    if (code == '34') { // период "год"
        return getReportPeriod9month(reportPeriodService.getPrevReportPeriod(reportPeriod.id));
    } else if (code == '33') { // период "9 месяцев"
        return reportPeriod;
    }
    return null;
}

/**
 * Получить значение ячейки фиксированной строки из налоговой формы.
 *
 * @param dataRows строки нф
 * @param columnName название столбца
 * @param alias алиас строки
 * @return значение столбца
 *
 */
def getAliasFromForm(def dataRows, def columnName, def alias) {
    if (dataRows != null && !dataRows.isEmpty()) {
        def aliasRow = getDataRow(dataRows, alias)
        return getLong(aliasRow.getCell(columnName).value)
    }
    return 0
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