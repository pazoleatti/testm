package form_template.ndfl.report_6ndfl.v2016

import groovy.transform.Field
import groovy.xml.MarkupBuilder

switch (formDataEvent) {
    case FormDataEvent.CALCULATE:
        buildXml()
        break
    default:
        break
}

@Field final String DATE_FORMAT_UNDERLINE = "yyyy_MM_dd"
@Field final String DATE_FORMAT_DOT = "dd.MM.yyyy"

// Кэш провайдеров
@Field def providerCache = [:]

// значение подразделения из справочника 33
@Field def departmentParam = null

// значение подразделения из справочника 330 (таблица)
@Field def departmentParamTable = null

// Дата окончания отчетного периода
@Field def reportPeriodEndDate = null

// Кэш для справочников
@Field def refBookCache = [:]

def buildXml() {

    // Параметры подразделения
    def departmentParam = getDepartmentParam()
    def departmentParamIncomeRow = getDepartmentParamTable(departmentParam.record_id.value)

    // Отчетный период
    def reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)

    // Код периода
    def periodCode = getRefBookValue(8, reportPeriod?.dictTaxPeriodId)?.CODE?.stringValue

    // Признак лица, подписавшего документ
    def signatoryId = getRefBookValue(35, departmentParamIncomeRow?.SIGNATORY_ID?.referenceValue)?.CODE?.numberValue

    // Коды представления налоговой декларации по месту нахождения (учёта)
    def taxPlaceTypeCode = getRefBookValue(2, departmentParamIncomeRow?.TAX_PLACE_TYPE_CODE?.referenceValue)?.CODE?.stringValue

    def ndflPersonIncomeCommonValue = ndflPersonService.findNdflPersonIncomeCommonValue(declarationData.id);
    def ndflPersonIncomeByRateList = ndflPersonIncomeCommonValue?.ndflPersonIncomeByRateList
    def ndflPersonIncomeByDateList = ndflPersonService.findNdflPersonIncomeByDate(declarationData.id)

    def builder = new MarkupBuilder(xml)
    builder.mkp.xmlDeclaration(version: "1.0", encoding: "utf-8")
    builder.Файл(
            ИдФайл: generateXmlFileId(departmentParamIncomeRow, departmentParam.INN, declarationData.kpp),
            ВерсПрог: applicationVersion,
            ВерсФорм: "5.01"
    ) {
        Документ(
                КНД: "1151099",
                ДатаДок: new Date().format(DATE_FORMAT_DOT),
                Период: getPeriod(departmentParamIncomeRow, periodCode),
                ОтчетГод: reportPeriod.taxPeriod.year,
                КодНО: declarationData.taxOrganCode,
                НомКорр: reportPeriodService.getCorrectionNumber(declarationData.departmentReportPeriodId),
                ПоМесту: taxPlaceTypeCode
        ) {
            СвНП(
                    ОКТМО: departmentParamIncomeRow.OKTMO,
                    Тлф: departmentParamIncomeRow.PHONE
            ){
                НПЮЛ(
                        НаимОрг: departmentParamIncomeRow.NAME,
                        ИННЮЛ: departmentParam.INN,
                        КПП: declarationData.kpp
                )
            }
            Подписант(
                    ПрПодп: signatoryId
            ){
                ФИО(
                        Фамилия: departmentParamIncomeRow.SIGNATORY_SURNAME,
                        Имя: departmentParamIncomeRow.SIGNATORY_FIRSTNAME,
                        Отчество: departmentParamIncomeRow.SIGNATORY_LASTNAME
                ){}
                if (signatoryId == 1) {
                    СвПред(
                            НаимДок: departmentParamIncomeRow.APPROVE_DOC_NAME,
                            НаимОрг: departmentParamIncomeRow.APPROVE_ORG_NAME
                    )
                } else {
                    СвПред()
                }
            }
            НДФЛ6(){
                ОбобщПоказ(
                        КолФЛДоход: ndflPersonIncomeCommonValue?.countPerson?.value,
                        УдержНалИт: ndflPersonIncomeCommonValue?.withholdingTax?.value,
                        НеУдержНалИт: ndflPersonIncomeCommonValue?.notHoldingTax?.value,
                        ВозврНалИт: ndflPersonIncomeCommonValue?.refoundTax?.value
                ) {
                    ndflPersonIncomeByRateList.each { ndflPersonIncomeByRate ->
                        СумСтавка (
                            Ставка: ndflPersonIncomeByRate.taxRate,
                            НачислДох: ndflPersonIncomeByRate.incomeAccruedSumm.toDouble().round(2),
                            НачислДохДив: ndflPersonIncomeByRate.incomeAccruedSummDiv.toDouble().round(2),
                            ВычетНал: ndflPersonIncomeByRate.totalDeductionsSumm.toDouble().round(2),
                            ИсчислНал: ndflPersonIncomeByRate.calculatedTax,
                            ИсчислНалДив: ndflPersonIncomeByRate.calculatedTaxDiv,
                            АвансПлат: ndflPersonIncomeByRate.prepaymentSum.toDouble().round(2)
                        ) {}
                    }
                }
                ДохНал() {
                    ndflPersonIncomeByDateList.each { ndflPersonIncomeByDate ->
                        СумДата (
                            ДатаФактДох: ndflPersonIncomeByDate.incomeAccruedDate.format("dd.MM.yyyy"),
                            ДатаУдержНал: ndflPersonIncomeByDate.taxDate.format("dd.MM.yyyy"),
                            СрокПрчслНал: ndflPersonIncomeByDate.taxTransferDate.format("dd.MM.yyyy"),
                            ФактДоход: ndflPersonIncomeByDate.incomePayoutSumm.toDouble().round(2),
                            УдержНал: ndflPersonIncomeByDate.withholdingTax
                        ) {}
                    }
                }
            }
        }
    }
    println(xml)
}

/**
 * Генерация значения атрибута ИдФайл R_T_A_K_O_GGGGMMDD_N
 * R_T - NO_NDFL6
 * A - идентификатор получателя, которому направляется файл обмена;
 * K - идентификатор конечного получателя, для которого предназначена информация из данного файла обмена;
 * O - 	Девятнадцатиразрядный код (идентификационный номер налогоплательщика (далее - ИНН) и код причины постановки на учет (далее - КПП) организации (обособленного подразделения);
 * GGGG - Год формирования передаваемого файла
 * MM - Месяц формирования передаваемого файла
 * DD - День формирования передаваемого файла
 * N - Идентификационный номер файла должен обеспечивать уникальность файла, длина - от 1 до 36 знаков
 */
def generateXmlFileId(def departmentParamIncomeRow, def INN, def KPP) {
    def R_T = "NO_NDFL6"
    def A = departmentParamIncomeRow?.TAX_ORGAN_CODE_MID?.value
    def K = departmentParamIncomeRow?.TAX_ORGAN_CODE?.value
    def O = INN?.value + KPP
    def currDate = new Date().format(DATE_FORMAT_UNDERLINE)
    def N = UUID.randomUUID().toString().toUpperCase()
    def res = R_T + "_" + A + "_" + K + "_" + O + "_" + currDate + "_" + N
    return res
}

// Код периода
def getPeriod(def departmentParamIncomeRow, def periodCode) {
    if (departmentParamIncomeRow?.REORG_FORM_CODE?.value) {
        def result;
        switch (periodCode) {
            case 21:
                result = "51"
                break
            case 31:
                result = "52"
                break
            case 33:
                result = "53"
                break
            case 34:
                result = "90"
                break
        }
        return result;
    } else {
        return periodCode;
    }
}


def getReportPeriodEndDate() {
    if (reportPeriodEndDate == null) {
        reportPeriodEndDate = reportPeriodService.getEndDate(declarationData.reportPeriodId)?.time
    }
    return reportPeriodEndDate
}

// Получить параметры подразделения (из справочника 950)
def getDepartmentParam() {
    if (departmentParam == null) {
        def departmentId = declarationData.departmentId
        def departmentParamList = getProvider(950L).getRecords(getReportPeriodEndDate() - 1, null, "DEPARTMENT_ID = $departmentId", null)
        if (departmentParamList == null || departmentParamList.size() == 0 || departmentParamList.get(0) == null) {
            throw new Exception("Ошибка при получении настроек обособленного подразделения")
        }
        departmentParam = departmentParamList?.get(0)
    }
    return departmentParam
}

/**
 * Получить параметры подразделения (из справочника 951)
 * @param departmentParamId
 * @return
 */
def getDepartmentParamTable(def departmentParamId) {
    if (departmentParamTable == null) {
        def filter = "LINK = $departmentParamId and KPP ='${declarationData.kpp}'"
        def departmentParamTableList = getProvider(951).getRecords(getReportPeriodEndDate() - 1, null, filter, null)
        if (departmentParamTableList == null || departmentParamTableList.size() == 0 || departmentParamTableList.get(0) == null) {
            throw new Exception("Ошибка при получении настроек обособленного подразделения")
        }
        departmentParamTable = departmentParamTableList.get(0)
    }
    return departmentParamTable
}

/**
 * Получение провайдера с использованием кеширования.
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
 * Разыменование записи справочника
 */
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}