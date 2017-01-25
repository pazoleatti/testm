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

// Кэш провайдеров
@Field def providerCache = [:]

// значение подразделения из справочника 33
@Field def departmentParam = null

// значение подразделения из справочника 330 (таблица)
@Field def departmentParamTable = null

// Дата окончания отчетного периода
@Field def reportPeriodEndDate = null

def buildXml() {

    def DECLARATION_TYPE_ID = 101

    def declarationDataCons = declarationService.getLast(DECLARATION_TYPE_ID, declarationData.getDepartmentId(), declarationData.getReportPeriodId())

    // Параметры подразделения
    def departmentParam = getDepartmentParam()
    def departmentParamIncomeRow = getDepartmentParamTable(departmentParam.record_id.value)
    println(departmentParamIncomeRow)

    def ndflPersonIncomeByDateList = []
    def ndflPersonIncomeByRateList = []

    def builder = new MarkupBuilder(xml)
    builder.mkp.xmlDeclaration(version: "1.0", encoding: "utf-8")
    builder.Файл(
            ИдФайл: generateXmlFileId(),
            ВерсПрог: getApplicationVersion(),
            ВерсФорм: "5.01"
    ) {
        Документ(
                КНД: "1151099",
                ДатаДок: new Date().format("dd.MM.yyyy"),
                Период: "",
                ОтчетГод: "",
                КодНО: "",
                НомКорр: "",
                ПоМесту: ""
        ) {
            СвНП(
                    ОКТМО: getOKTMO(departmentParamIncomeRow),
                    Тлф: ""
            ){
                НПЮЛ(
                        НаимОрг: "",
                        ИННЮЛ: "",
                        КПП: getKPP(departmentParamIncomeRow)
                )
            }
            Подписант(
                    ПрПодп: ""
            ){
                ФИО(
                        Фамилия: "",
                        Имя: "",
                        Отчество: ""
                ){}
                СвПред(
                        НаимДок: "",
                        НаимОрг: ""
                )
            }
            НДФЛ6(){
                ОбобщПоказ(
                        КолФЛДоход: "",
                        УдержНалИт: "",
                        НеУдержНалИт: "",
                        ВозврНалИт: ""
                ) {
                    ndflPersonIncomeByRateList.each { ndflPersonIncomeByRate ->
                        СумСтавка {
                            Ставка: ndflPersonIncomeByRate.getTaxRate()
                            НачислДох: ndflPersonIncomeByRate.getIncomeAccruedSumm()
                            НачислДохДив: ndflPersonIncomeByRate.getIncomeAccruedSummDiv()
                            ВычетНал: ndflPersonIncomeByRate.getTotalDeductionsSumm()
                            ИсчислНал: ndflPersonIncomeByRate.getCalculatedTax()
                            ИсчислНалДив: ndflPersonIncomeByRate.getCalculatedTaxDiv()
                            АвансПлат: ndflPersonIncomeByRate.getPrepaymentSum()
                        }
                    }
                }
                ДохНал() {
                    ndflPersonIncomeByDateList.each { ndflPersonIncomeByDate ->
                        СумДата {
                            ДатаФактДох: ndflPersonIncomeByDate.getIncomeAccruedDate()
                            ДатаУдержНал: ndflPersonIncomeByDate.getTaxDate()
                            СрокПрчслНал: ndflPersonIncomeByDate.getTaxTransferDate()
                            ФактДоход: ndflPersonIncomeByDate.getIncomeAccruedDate()
                            УдержНал: ndflPersonIncomeByDate.getWithholdingTax()
                        }
                    }
                }
            }
        }
    }
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
def generateXmlFileId() {
    def R_T = "NO_NDFL6"
    def A = ""
    def K = ""
    def O = ""
    def GGGG = new Date().format("yyyy")
    def MM = new Date().format("MM")
    def DD = new Date().format("dd")
    def N = ""
    def res = R_T + "_" + A + "_" + K + "_" + O + "_" + GGGG + "_" + MM + "_" + DD + "_" + N
    return res
}

/**
 * Строка версии должна представлять собой :  "АС УН, ФП "<Наименование подсистемы>" <Номер версии>"
 */
def getApplicationVersion() {
    return 'АС УН, ФП "<Наименование подсистемы>" <Номер версии>'
}

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getCalendarStartDate(formData.reportPeriodId).time
    }
    return startDate
}

def getOKTMO(def departmentParamRow) {
    departmentParamRow?.OKTMO?.value
}

def getKPP(def departmentParamRow) {
    departmentParamRow?.KPP?.value
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

def getEndDate() {
    if (reportPeriodEndDate == null) {
        reportPeriodEndDate = reportPeriodService.getEndDate(declarationData.reportPeriodId)?.time
    }
    return reportPeriodEndDate
}

// Получить параметры подразделения (из справочника 950)
def getDepartmentParam() {
    if (departmentParam == null) {
        def departmentId = declarationData.departmentId
        def departmentParamList = getProvider(950L).getRecords(getEndDate() - 1, null, "DEPARTMENT_ID = $departmentId", null)
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
        def departmentParamTableList = getProvider(951).getRecords(getEndDate() - 1, null, filter, null)
        if (departmentParamTableList == null || departmentParamTableList.size() == 0 || departmentParamTableList.get(0) == null) {
            throw new Exception("Ошибка при получении настроек обособленного подразделения")
        }
        departmentParamTable = departmentParamTableList.get(0)
    }
    return departmentParamTable
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