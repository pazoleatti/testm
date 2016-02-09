package form_template.income.declaration_app2.v2015

import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import groovy.transform.Field
import groovy.xml.MarkupBuilder

/**
 * Приложение №2 к декларации по налогу на прибыль (Банк)
 * Формирование XML для декларации налога на прибыль.
 * версия 2015 года
 * declarationTypeId=22
 * declarationTemplateId=
 *
 * @author bkinzyabulatov
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE : // создать / обновить
        checkDepartmentParams(LogLevel.WARNING)
        break
    case FormDataEvent.CHECK : // проверить
        checkDepartmentParams(LogLevel.ERROR)
        break
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED : // принять из создана
        checkDepartmentParams(LogLevel.ERROR)
        break
    case FormDataEvent.PRE_CALCULATION_CHECK:
        checkDepartmentParams(LogLevel.WARNING)
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

// значение подразделения из справочника 33
@Field
def departmentParam = null

// значение подразделения из справочника 330 (таблица)
@Field
def departmentParamTable = null

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

// Запуск генерации XML.
void generateXML() {

    def empty = 0

    def reportPeriodId = declarationData.reportPeriodId

    // Параметры подразделения
    def incomeParams = getDepartmentParam()
    def incomeParamsTable = getDepartmentParamTable(incomeParams.record_id.value)

    def taxOrganCode = incomeParamsTable?.TAX_ORGAN_CODE?.value
    def inn = incomeParams?.INN?.value
    def kpp = declarationData.kpp
    def formatVersion = incomeParams?.FORMAT_VERSION?.value

    // Отчётный период.
    def reportPeriod = reportPeriodService.get(reportPeriodId)

    // Приложение №2 должно формироваться только в случае если текущий экземпляр декларации относится к периоду «год»
    if (reportPeriod.order != 4) {
        return
    }

    // Данные налоговых форм.

    def formDataCollection = declarationService.getAcceptedFormDataSources(declarationData, userInfo, logger)

    // Приложение №2 "Сведения о доходах физического лица, выплаченных ему налоговым агентом, от операций с ценными бумагами, операций с финансовыми инструментами срочных сделок, а также при осуществлении выплат по ценным бумагам российских эмитентов"
    def dataRowsApp2 = getDataRows(formDataCollection, 415)
    def isCFOApp2 = false

    // Приложение №2 "Сведения о доходах физического лица, выплаченных ему налоговым агентом, от операций с ценными бумагами, операций с финансовыми инструментами срочных сделок, а также при осуществлении выплат по ценным бумагам российских эмитентов (ЦФО НДФЛ)."
    def dataRowsApp2CFO = getDataRows(formDataCollection, 418)
    if (dataRowsApp2 == null) {
        isCFOApp2 = true
        dataRowsApp2 = dataRowsApp2CFO
    } else if (dataRowsApp2CFO != null) {
        logger.warn("Неверно настроены источники декларации Банка! Одновременно созданы в качестве источников налоговые формы: «%s», «%s». Консолидация произведена из «%s».",
                formTypeService.get(415).name, formTypeService.get(418)?.name, formTypeService.get(415)?.name)
    }

    // Расчет значений для текущей декларации.

    if (xml == null) {
        return
    }

    // Формирование XML'ки.

    def builder = new MarkupBuilder(xml)
    builder.Файл(
            ИдФайл : generateXmlFileId(taxOrganCode),
            ВерсПрог : applicationVersion,
            ВерсФорм : formatVersion){

        // Титульный лист
        Документ() {
            СвНП() {
                НПЮЛ(
                        ИННЮЛ : inn,
                        КПП : kpp)
            }
            // Титульный лист - конец

            Прибыль() {
                // берем первые 500 строк (по 2 листа декларации на строку источника)
                if (dataRowsApp2.size() >= 500) {
                    dataRowsApp2 = dataRowsApp2[0..499]
                }
                // Приложение №2
                // сортируем по ФИО, потом по остальным полям
                if (isCFOApp2 && dataRowsApp2 != null) {
                    def sortColumns = ['surname', 'name', 'patronymic', 'innRF', 'inn', 'taxRate']
                    sortRows(dataRowsApp2, sortColumns)
                }

                //ДатаСправ   Дата составления
                def dataSprav = (docDate != null ? docDate : new Date()).format("dd.MM.yyyy")
                //Тип         Тип
                def type = String.format("%02d", reportPeriodService.getCorrectionNumber(declarationData.departmentReportPeriodId))

                if (dataRowsApp2) {
                    fillRecordsMap([4L, 10L, 350L, 360L, 370L])
                }

                def index = 0
                for (def row : dataRowsApp2) {
                    //НомерСправ  Справка №
                    def nomerSprav = isCFOApp2 ? (++index) : row.refNum
                    //ИННФЛ       ИНН
                    def innFL = row.innRF
                    //ИННИно       ИНН
                    def innIno = row.inn
                    //Фамилия     Фамилия
                    def surname = row.surname
                    //Имя         Имя
                    def givenName = row.name
                    //Отчество    Отчество
                    def parentName = row.patronymic
                    //СтатусНП    Статус налогоплательщика
                    def statusNP = row.status
                    //ДатаРожд    Дата рождения
                    def dataRozhd = row.birthday?.format('dd.MM.yyyy')
                    //Гражд       Гражданство (код страны)
                    def grazhd = getRefBookValue(10, row.citizenship)?.CODE?.value
                    //КодВидДок   Код вида документа, удостоверяющего личность
                    def kodVidDok = getRefBookValue(360, row.code)?.CODE?.value
                    //СерНомДок   Серия и номер документа
                    def serNomDok = row.series
                    //Индекс      Почтовый индекс
                    def zipCode = row.postcode
                    //КодРегион   Регион (код)
                    def subdivisionRF = getRefBookValue(4, row.region)?.CODE?.value
                    //Район       Район
                    def area = row.district
                    //Город       Город
                    def city = row.city
                    //НаселПункт  Населенный пункт (село, поселок)
                    def region = row.locality
                    //Улица       Улица (проспект, переулок)
                    def street = row.street
                    //Дом         Номер дома (владения)
                    def homeNumber = row.house
                    //Корпус      Номер корпуса (строения)
                    def corpNumber = row.housing
                    //Кварт       Номер квартиры
                    def apartment = row.apartment
                    //ОКСМ        Код страны
                    def oksm = getRefBookValue(10, row.country)?.CODE?.value
                    //АдрТекст    Адрес места жительства за пределами Российской Федерации
                    def adrText = row.address
                    //Ставка      Налоговая ставка
                    def stavka = row.taxRate
                    //СумДохОбщ   Общая сумма дохода
                    def sumDohObsh = row.income
                    //СумВычОбщ   Общая сумма вычетов
                    def sumVichObsh = row.deduction
                    //НалБаза     Налоговая база
                    def nalBazaApp2 = row.taxBase
                    //НалИсчисл   Сумма налога исчисленная
                    def nalIschislApp2 = row.calculated
                    //НалУдерж    Сумма налога удержанная
                    def nalUderzh = row.withheld
                    //НалУплач Сумма налога перечисленная
                    def nalUplach = row.listed
                    //НалУдержЛиш Сумма налога, излишне удержанная налоговым агентом
                    def nalUderzhLish = row.withheldAgent
                    //НалНеУдерж  Сумма налога, не удержанная налоговым агентом
                    def nalNeUderzh = row.nonWithheldAgent

                    // 0..n
                    СведДохФЛ(
                            НомерСправ : nomerSprav,
                            ДатаСправ : dataSprav,
                            Тип : type) {
                        //1..1
                        ФЛПолучДох(
                                ИННФЛ : innFL,
                                ИННИно : innIno,
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
                                [Ставка : stavka, СумДохОбщ : sumDohObsh] +
                                        (sumVichObsh != null ? [СумВычОбщ : sumVichObsh] : []) +
                                        [НалБаза : nalBazaApp2, НалИсчисл : nalIschislApp2] +
                                        (nalUderzh != null ? [НалУдерж : nalUderzh] : []) +
                                        (nalUplach != null ? [НалУплач : nalUplach] : []) +
                                        (nalUderzhLish != null ? [НалУдержЛиш : nalUderzhLish] : []) +
                                        (nalNeUderzh != null ? [НалНеУдерж : nalNeUderzh] : [])
                        )
                        //0..1
                        СпрДохФЛ() {
                            3.times{ index_1 ->
                                //КодДоход    040 (Код дохода)
                                def kodDohod040 = getRefBookValue(370, row["col_040_${index_1 + 1}"])?.CODE?.value
                                //СумДоход    041 (Сумма дохода)
                                def sumDohod041 = row["col_041_${index_1 + 1}"]

                                СумДох(КодДоход : kodDohod040, СумДоход : sumDohod041) {
                                    5.times{ index_2 ->
                                        //КодВычет    042 (Код вычета)
                                        def kodVichet042 = getRefBookValue(350, row["col_042_${index_1 + 1}_${index_2 + 1}"])?.CODE?.value
                                        //СумВычет    043 (Сумма вычета)
                                        def sumVichet043 = row["col_043_${index_1 + 1}_${index_2 + 1}"]

                                        //1..n
                                        СумВыч(КодВычет : kodVichet042, СумВычет : sumVichet043)
                                    }
                                }
                            }
                        }

                        //0..1
                        НалВычСтанд() {
                            2.times{ index_1 ->
                                //КодВычет    051 (Код вычета)
                                def kodVichet051 = getRefBookValue(350, row["col_051_3_${index_1 + 1}"])?.CODE?.value
                                //СумВычет    052 (Сумма вычета)
                                def sumVichet052 = row["col_052_3_${index_1 + 1}"]
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

List<String> getErrorTable(record) {
    List<String> errorList = new ArrayList<String>()
    if (record.TAX_ORGAN_CODE?.value == null || record.TAX_ORGAN_CODE.value.isEmpty()) {
        errorList.add("«Код налогового органа»")
    }
    return errorList
}

List<String> getErrorDepartment(record) {
    List<String> errorList = new ArrayList<String>()

    if (record.INN?.stringValue == null || record.INN.stringValue.isEmpty()) {
        errorList.add("«ИНН»")
    }
    return errorList
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

/** Получить строки формы. */
def getDataRows(def formDataCollection, def formTypeId) {
    List<FormData> formList = formDataCollection.records.findAll { it.getFormType().getId() == formTypeId };
    def dataRows = []
    for (def form : formList) {
        dataRows += (formDataService.getDataRowHelper(form)?.getAll() ?: [])
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

// Загрузка всех записей справочников в кеш.
def fillRecordsMap(def refBookIds) {
    refBookIds.each { refBookId ->
        def provider = refBookFactory.getDataProvider(refBookId)
        def records = provider.getRecords(getEndDate(), null, null, null)
        if (records) {
            records.each { record ->
                def recordId = record.get(RefBook.RECORD_ID_ALIAS).numberValue
                def key = getRefBookCacheKey(refBookId, recordId)
                if (!refBookCache.containsKey(key)) {
                    refBookCache.put(key, refBookService.getRecordData(refBookId, recordId));
                }
            }
        }
    }
}

def generateXmlFileId(String taxOrganCode) {
    def departmentParam = getDepartmentParam()
    if (departmentParam) {
        def date = Calendar.getInstance().getTime()?.format("yyyyMMdd")
        def fileId = TaxType.INCOME.declarationPrefix + '_' +
                taxOrganCode + '_' +
                taxOrganCode + '_' +
                departmentParam.INN?.value +
                declarationData.kpp + "_" +
                date + "_" +
                UUID.randomUUID().toString().toUpperCase()
        return fileId
    }
    return null
}
