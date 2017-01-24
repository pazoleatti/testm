package form_template.ndfl.report_2ndfl.v2016

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field
import groovy.xml.MarkupBuilder

switch (formDataEvent) {
    case FormDataEvent.CHECK: //Проверки
        println "!CHECK!"
        break
    case FormDataEvent.CALCULATE: //формирование xml
        println "!CALCULATE!"
        buildXml()
        break
    case FormDataEvent.COMPOSE: // Консолидирование
        println "!COMPOSE!"
        break
    case FormDataEvent.GET_SOURCES: //формирование списка источников
        println "!GET_SOURCES!"
        break
    case FormDataEvent.CREATE_SPECIFIC_REPORT: //создание спецефичного отчета
        println "!CREATE_SPECIFIC_REPORT!"
        break
}

// Кэш провайдеров
@Field
def providerCache = [:]

@Field
def departmentParam = [:]

// значение подразделения из справочника 310 (таблица)
@Field
def departmentParamTable = [:]

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

def buildXml() {
    departmentParam = getDepartmentParam()
    def departmentParamRow = departmentParam ? departmentParamTable(departmentParam?.record_id?.value) : null

    // Данные для Файл.СвРекв
    def oktmo = getOKTMO(departmentParamRow)
    def kpp = getKPP()

    def listKnf = []
    def nomSpr = 1
    def svSumDoh = []
    def builder = new MarkupBuilder(xml)
    builder.Файл(ИдФайл: generateXmlFileId(),
            ВерсПрог: applicationVersion,
            ВерсФорм: "5.04") {
        СвРекв(ОКТМО: oktmo,
                ОтчетГод: "", //TODO ???создать объект taxPeriodService для TaxPeriodDao и получить год из TaxPeriod???
                ПризнакФ: "") {
            СвЮЛ(ИННЮЛ: "7707083893",
                    КПП: kpp)
            СвФЛ()
        }
        listKnf.collate(3000).each{
            Документ(КНД: "1151078",
                    ДатаДок: new Date().format("dd.MM.yyyy"),
                    НомСпр: nomSpr++,
                    ОтчетГод: "",
                    Признак: "",
                    НомКорр: "",
                    КодНО: "") {
                Подписант(ПрПодп: "") {
                    ФИО(Фамилия: "",
                            Имя: "",
                            Отчество: "")
                    СвПред(НаимДок: "",
                            НаимОрг: "")
                }
                СвНА(ОКТМО: "",
                        Тлф: "") {
                    СвНАЮЛ(НаимОрг: "",
                            ИННЮЛ: "7707083893",
                            КПП: "")
                    СвНАФЛ()
                }
                ПолучДох(ИННФЛ: "",
                        ИННИно: "",
                        Статус: "",
                        ДатаРожд: "",
                        Гражд: "") {
                    ФИО(Фамилия: "",
                            Имя: "",
                            Отчество: "")
                    УдЛичнФЛ(КодУдЛичн: "",
                            СерНомДок: "")
                    АдрМЖРФ(Индекс: "",
                            КодРегион: "",
                            Район: "",
                            Город: "",
                            НаселПункт: "",
                            Улица: "",
                            Дом: "",
                            Корпус: "",
                            Кварт: "")
                    АдрИНО(КодСтр: "",
                            АдрТекст: "")
                }
                СведДох(Ставка: "") {
                    svSumDoh.each{ДохВыч(){
                            СвСумДох(Месяц: "",
                                    КодДоход: "",
                                    СумДоход: "") {
                                СвСумВыч(КодВычет: "",
                                        СумВычет: "") {

                                }
                            }
                        }
                    }
                    НалВычССИ(){
                        ПредВычССИ(КодВычет: "",
                                СумВычет: "") {

                        }
                        УведСоцВыч(НомерУвед: "",
                                ДатаУвед: "",
                                ИФНСУвед: "")
                        УведИмущВыч(УведИмущВыч: "",
                                ДатаУвед: "",
                                ИФНСУвед: "")
                    }
                    СумИтНалПер(СумДохОбщ: "",
                            НалБаза: "",
                            НалИсчисл: "",
                            АвансПлатФикс: "",
                            НалУдерж: "",
                            НалПеречисл: "",
                            НалУдержЛиш: "",
                            НалНеУдерж: "") {
                        УведФиксПлат(НомерУвед: "",
                                ДатаУвед: "",
                                ИФНСУвед: "") {

                        }
                    }
                }
            }

        }
    }
}

def generateXmlFileId() {

    def departmentParam = getDepartmentParam()

    def departmentParamRow = departmentParam ? getDepartmentParamTable(departmentParam?.record_id?.value) : null
        def r_t = "NO_NDFL2"
        def a = departmentParamRow?.TAX_ORGAN_CODE_MID?.value
        def k = departmentParamRow?.TAX_ORGAN_CODE?.value
        def o = "7707083893"
        def date = Calendar.getInstance().getTime()?.format("yyyyMMdd")
        def n = UUID.randomUUID().toString().toUpperCase()
        // R_T_A_K_O_GGGGMMDD_N
        def fileId = r_t + '_' +
                a + '_' +
                k + '_' +
                o + '_' +
                date + '_' +
                n
        return fileId
}

def getOKTMO(def departmentParamRow) {
    departmentParamRow?.OKTMO?.value
}

def getKPP(def departmentParamRow) {
    departmentParamRow?.KPP?.value
}


// Получить параметры подразделения (из справочника 950)

def getDepartmentParam() {
    if (departmentParam == null) {
        def departmentId = declarationData.departmentId
        def departmentParamList = getProvider(950).getRecords(getReportPeriodEndDate() - 1, null, "DEPARTMENT_ID = $departmentId", null)
        if (departmentParamList == null || departmentParamList.size() == 0 || departmentParamList.get(0) == null) {
            throw new Exception("Ошибка при получении настроек обособленного подразделения. Настройки подразделения заполнены не полностью")
        }
        departmentParam = departmentParamList?.get(0)
    }
    return departmentParam
}


def getDepartmentParamTable(def departmentParamId) {
    if (departmentParamTable == null) {
        def filter = "LINK = $departmentParamId and KPP ='${declarationData.kpp}'"
        def departmentParamTableList = getProvider(951).getRecords(getReportPeriodEndDate() - 1, null, filter, null)
        if (departmentParamTableList == null || departmentParamTableList.size() == 0 || departmentParamTableList.get(0) == null) {
            throw new Exception("Ошибка при получении настроек обособленного подразделения. Настройки подразделения заполнены не полностью")
        }
        departmentParamTable = departmentParamTableList.get(0)
    }
    return departmentParamTable
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

def getReportPeriodEndDate() {
    if (reportPeriodEndDate == null) {
        reportPeriodEndDate = reportPeriodService.getEndDate(declarationData.reportPeriodId)?.time
    }
    return reportPeriodEndDate
}