package form_template.ndfl.report_6ndfl.v2016

import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils
import groovy.transform.Field
import groovy.xml.MarkupBuilder
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFSheet
import com.aplana.sbrf.taxaccounting.model.Department
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod
import com.aplana.sbrf.taxaccounting.model.DeclarationData
import com.aplana.sbrf.taxaccounting.model.DeclarationDataFile
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate
import com.aplana.sbrf.taxaccounting.model.Relation
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import com.aplana.sbrf.taxaccounting.model.TaxType
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.io.IOUtils
import com.aplana.sbrf.taxaccounting.model.ndfl.*

switch (formDataEvent) {
    case FormDataEvent.CHECK: //Проверки
        println "!CHECK!"
        checkXml()
        break
    case FormDataEvent.CALCULATE: //формирование xml
        println "!CALCULATE!"
        buildXml(xml)
        // Формирование pdf-отчета формы
//        declarationService.createPdfReport(logger, declarationData, userInfo)
        break
    case FormDataEvent.COMPOSE: // Консолидирование
        println "!COMPOSE!"
        break
    case FormDataEvent.GET_SOURCES: //формирование списка источников
        println "!GET_SOURCES!"
        getSources()
        break
    case FormDataEvent.CREATE_SPECIFIC_REPORT: //создание спецефичного отчета
        println "!CREATE_SPECIFIC_REPORT!"
        createSpecificReport()
        break
    case FormDataEvent.CREATE_FORMS: // создание экземпляра
        println "!CREATE_FORMS!"
        createForm()
        break
    case FormDataEvent.CREATE_REPORTS:
        println "!CREATE_REPORTS!"
        createReports()
        break
}

// Альяс для списка идентифокаторов физлиц из КНФ
@Field
final String NDFL_PERSON_KNF_ID = "ndflPersonKnfId"

// Коды, определяющие налоговый (отчётный) период
@Field final long REF_BOOK_PERIOD_CODE_ID = RefBook.Id.PERIOD_CODE.id

// Коды представления налоговой декларации по месту нахождения (учёта)
@Field final long REF_BOOK_TAX_PLACE_TYPE_CODE_ID = RefBook.Id.PRESENT_PLACE.id

// Признак лица, подписавшего документ
@Field final long REF_BOOK_MARK_SIGNATORY_CODE_ID = RefBook.Id.MARK_SIGNATORY_CODE.id

// Настройки подразделений по НДФЛ
@Field final long REF_BOOK_NDFL_ID = RefBook.Id.NDFL.id

// Настройки подразделений по НДФЛ (таблица)
@Field final long REF_BOOK_NDFL_DETAIL_ID = RefBook.Id.NDFL_DETAIL.id

@Field int REF_BOOK_OKTMO_ID = 96;

@Field final FORM_NAME_NDFL6 = "НДФЛ6"
@Field final FORM_NAME_NDFL2 = "НДФЛ2"
@Field final int DECLARATION_TYPE_RNU_NDFL_ID = 101
@Field final int DECLARATION_TYPE_NDFL2_ID = 102

// Узлы 6 НДФЛ
@Field final NODE_NAME_SUM_STAVKA6 = "СумСтавка"
@Field final NODE_NAME_OBOBSH_POKAZ6 = "ОбобщПоказ"
@Field final NODE_NAME_SUM_DATA6 = "СумДата"

// Узлы 2 НДФЛ
@Field final NODE_NAME_DOCUMNET2 = "Документ"
@Field final NODE_NAME_SVED_DOH2 = "СведДох"
@Field final NODE_NAME_SUM_IT_NAL_PER2 = "СумИтНалПер"
@Field final NODE_NAME_SV_SUM_DOH2 = "СвСумДох"

// Общие атрибуты
@Field final ATTR_RATE = "Ставка"
@Field final int RATE_THIRTEEN = 13

// Атрибуты 6 НДФЛ
@Field final ATTR_NACHISL_DOH6 = "НачислДох"
@Field final ATTR_NACHISL_DOH_DIV6 = "НачислДохДив"
@Field final ATTR_VICHET_NAL6 = "ВычетНал"
@Field final ATTR_ISCHISL_NAL6 = "ИсчислНал"
@Field final ATTR_NE_UDERZ_NAL_IT6 = "НеУдержНалИт"
@Field final ATTR_KOL_FL_DOHOD6 = "КолФЛДоход"
@Field final ATTR_AVANS_PLAT6 = "АвансПлат"

// Атрибуты 2 НДФЛ
@Field final ATTR_SUM_DOH_OBSH2 = "СумДохОбщ"
@Field final ATTR_NAL_ISCHISL2 = "НалИсчисл"
@Field final ATTR_NAL_NE_UDERZ2 = "НалНеУдерж"
@Field final ATTR_KOD_DOHOD2 = "КодДоход"
@Field final ATTR_SUM_DOHOD2 = "СумДоход"

@Field final String DATE_FORMAT_UNDERLINE = "yyyyMMdd"
@Field final String DATE_FORMAT_DOT = "dd.MM.yyyy"

// Кэш провайдеров
@Field def providerCache = [:]

// значение подразделения из справочника
@Field def departmentParam = null

// значение подразделения из справочника
@Field def departmentParamTable = null

// Дата окончания отчетного периода
@Field def reportPeriodEndDate = null

// Кэш для справочников
@Field def refBookCache = [:]

// Коды мест предоставления документа
@Field def presentPlaceCodeCache = [:]

@Field
final OKTMO_CACHE = [:]

/************************************* СОЗДАНИЕ XML *****************************************************************/
def buildXml(def writer) {
    buildXml(writer, false)
}

def buildXmlForSpecificReport(def writer) {
    buildXml(writer, true)
}

def buildXml(def writer, boolean isForSpecificReport) {
    ScriptUtils.checkInterrupted()
    ConfigurationParamModel configurationParamModel = declarationService.getAllConfig(userInfo)
    // Получим ИНН из справочника "Общие параметры"
    def sberbankInnParam = configurationParamModel?.get(ConfigurationParam.SBERBANK_INN)?.get(0)?.get(0)
    // Получим код НО пром из справочника "Общие параметры"
    def kodNoProm = configurationParamModel?.get(ConfigurationParam.NO_CODE)?.get(0)?.get(0)

    // Параметры подразделения
    def departmentParam = getDepartmentParam(declarationData.departmentId)
    def departmentParamIncomeRow = getDepartmentParamTable(departmentParam?.id.value)

    // Отчетный период
    def reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)

    // Код периода
    def periodCode = getRefBookValue(REF_BOOK_PERIOD_CODE_ID, reportPeriod?.dictTaxPeriodId)?.CODE?.stringValue

    // Коды представления налоговой декларации по месту нахождения (учёта)
    def poMestuParam = getRefPresentPlace().get(departmentParamIncomeRow?.PRESENT_PLACE?.referenceValue)
    if (poMestuParam == null) {
        throw new RuntimeException("Код места в настройках подразделений не соответствует справочнику")
    }
    def taxPlaceTypeCode = poMestuParam?.CODE?.value

    // Признак лица, подписавшего документ
    def signatoryId = getRefBookValue(REF_BOOK_MARK_SIGNATORY_CODE_ID, departmentParamIncomeRow?.SIGNATORY_ID?.referenceValue)?.CODE?.numberValue

    // Текущая дата
    def currDate = new Date()

    def fileName = generateXmlFileId(departmentParamIncomeRow, sberbankInnParam, declarationData.kpp, kodNoProm)
    def builder = new MarkupBuilder(writer)
    builder.setDoubleQuotes(true)
    builder.setOmitNullAttributes(true)
    builder.Файл(
            ИдФайл: fileName,
            ВерсПрог: applicationVersion,
            ВерсФорм: "5.01"
    ) {
        Документ(
                КНД: "1151099",
                ДатаДок: currDate.format(DATE_FORMAT_DOT),
                Период: getPeriod(departmentParamIncomeRow, periodCode),
                ОтчетГод: reportPeriod.taxPeriod.year,
                КодНО: departmentParamIncomeRow?.TAX_ORGAN_CODE?.value,
                НомКорр: reportPeriodService.getCorrectionNumber(declarationData.departmentReportPeriodId),
                ПоМесту: taxPlaceTypeCode
        ) {
            def svNP = ["ОКТМО": declarationData.oktmo]
            // Атрибут Тлф необязателен
            if (departmentParamIncomeRow.PHONE && !departmentParamIncomeRow.PHONE.empty) {
                svNP.put("Тлф", departmentParamIncomeRow.PHONE)
            }
            СвНП(svNP) {
                НПЮЛ(
                        НаимОрг: departmentParamIncomeRow.NAME,
                        ИННЮЛ: sberbankInnParam,
                        КПП: declarationData.kpp
                )
            }
            Подписант(
                    ПрПодп: signatoryId
            ) {
                // Узел ФИО необязателен
                if (departmentParamIncomeRow.SIGNATORY_SURNAME && !departmentParamIncomeRow.SIGNATORY_SURNAME.empty) {
                    def fio = ["Фамилия": departmentParamIncomeRow.SIGNATORY_SURNAME, "Имя": departmentParamIncomeRow.SIGNATORY_FIRSTNAME]
                    // Атрибут Отчество необязателен
                    if (departmentParamIncomeRow.SIGNATORY_LASTNAME && !departmentParamIncomeRow.SIGNATORY_LASTNAME.empty) {
                        fio.put("Отчество", departmentParamIncomeRow.SIGNATORY_LASTNAME)
                    }
                    ФИО(fio) {}
                }
                if (signatoryId == 2) {
                    def svPred = ["НаимДок": departmentParamIncomeRow.APPROVE_DOC_NAME]
                    if (departmentParamIncomeRow.APPROVE_ORG_NAME && !departmentParamIncomeRow.APPROVE_ORG_NAME.empty) {
                        svPred.put("НаимОрг", departmentParamIncomeRow.APPROVE_ORG_NAME)
                    }
                    СвПред(svPred) {}
                }
            }
            НДФЛ6() {
                //Все доходы
                def ndflPersonIncomeList = []
                // Группировка id ndflPerson в список по 1000 штук поскольку Oracle не работает со списком размером более 1000
                def ndflPersonidForSearch = ndflPersonKnfId.collate(1000)
                // Поиск и добавление доходов
                ndflPersonidForSearch.each {
                    ndflPersonIncomeList.addAll(ndflPersonService.findIncomesForPersonByKppOktmoAndPeriod(it, declarationData.kpp, declarationData.oktmo, reportPeriod.startDate, reportPeriod.endDate))
                }
                // Отфилтьтруем строки у которых раздел 2.графа 6 не в этом отчетном периоде
                def incomesWhithoutAccruedDateNotInCurrentPeriod = ndflPersonIncomeList.findAll {
                    it.incomeAccruedDate == null || it.incomeAccruedDate.compareTo(reportPeriod.startDate) >= 0
                }

                // Сумма удержанная
                def incomeAccruedTotal = getNalUderzh(incomesWhithoutAccruedDateNotInCurrentPeriod)

                // Сумма не удержанная
                def incomeNotHoldingTotal = getNalNeUderzh(incomesWhithoutAccruedDateNotInCurrentPeriod)

                // Сумма возвращенная
                def refoundTotal = getTotalRefound(incomesWhithoutAccruedDateNotInCurrentPeriod)
                ОбобщПоказ(
                        КолФЛДоход: ndflPersonKnfId.size(),
                        УдержНалИт: incomeAccruedTotal,
                        НеУдержНалИт: incomeNotHoldingTotal,
                        ВозврНалИт: refoundTotal
                ) {

                    // Доходы сгруппированыые по ставке, ключ ставка - значение список операций
                    def incomesGroupedByRate = groupByTaxRate(ndflPersonIncomeList)

                    // Доходы без ставки
                    def incomesWithNullTaxRate = ndflPersonIncomeList.findAll { it.taxRate == null }

                    // Объединенные доходы без ставки и доходы имеющие одинаковый номер операции, ключ ставка - значение список операций
                    incomesGroupedByRate.each { key, value ->
                        def pickedIncomes = []
                        value.each { incomeGrouped ->
                            incomesWithNullTaxRate.each { incomeNullRate ->
                                if (incomeNullRate.operationId == incomeGrouped.operationId) {
                                    pickedIncomes << incomeNullRate
                                }
                            }
                        }
                        value.addAll(pickedIncomes)
                    }

                    incomesGroupedByRate.keySet().eachWithIndex { rate, index ->
                        ScriptUtils.checkInterrupted()
                        def idList = incomesGroupedByRate.get(rate).id

                        def idsForSearch = idList.collate(1000)

                        def prepaymentsForRate = []
                        idsForSearch.each {
                            ScriptUtils.checkInterrupted()
                            prepaymentsForRate.addAll(ndflPersonService.findPrepaymentsByOperationList(it))
                        }
                        // Сумма авансов
                        def prepaymentSumm = getSumOfPrepayments(prepaymentsForRate)

                        if (isForSpecificReport) {
                            СумСтавка(
                                    Ставка: rate,
                                    НачислДох: ScriptUtils.round(getSumOfAccrued(incomesGroupedByRate.get(rate)), 2),
                                    НачислДохДив: ScriptUtils.round(getSumOfAccruedWhenIncomeCodeDiv(incomesGroupedByRate.get(rate)), 2),
                                    ВычетНал: ScriptUtils.round(getSumOfDeductions(incomesGroupedByRate.get(rate)), 2),
                                    ИсчислНал: getSumOfCalculatedTax(incomesGroupedByRate.get(rate)),
                                    ИсчислНалДив: getSumOfCalculatedTaxWhenIncomeCodeDiv(incomesGroupedByRate.get(rate)),
                                    АвансПлат: prepaymentSumm,
                                    НомСтр: index + 1
                            ) {}
                        } else {
                            СумСтавка(
                                    Ставка: rate,
                                    НачислДох: ScriptUtils.round(getSumOfAccrued(incomesGroupedByRate.get(rate)), 2),
                                    НачислДохДив: ScriptUtils.round(getSumOfAccruedWhenIncomeCodeDiv(incomesGroupedByRate.get(rate)), 2),
                                    ВычетНал: ScriptUtils.round(getSumOfDeductions(incomesGroupedByRate.get(rate)), 2),
                                    ИсчислНал: getSumOfCalculatedTax(incomesGroupedByRate.get(rate)),
                                    ИсчислНалДив: getSumOfCalculatedTaxWhenIncomeCodeDiv(incomesGroupedByRate.get(rate)),
                                    АвансПлат: prepaymentSumm
                            ) {}
                        }
                    }
                }
                /* Мапа используется для заполнения раздела ДохНал. Ключом выступает объект Пара физлицо-ИдОперации,
                а значением спиок доходов которые соответствуют этой паре*/
                def pairPersonOperationIdMap = [:]
                // Находим все возможные значения
                ndflPersonIncomeList.each {
                    PairPersonOperationId pair = new PairPersonOperationId(it.ndflPersonId, it.operationId)
                    def incomesGroup = pairPersonOperationIdMap.get(pair)
                    if (incomesGroup == null) {
                        pairPersonOperationIdMap.put(pair, [it])
                    } else {
                        incomesGroup << it
                    }
                }
                // В этом временном списке находятся пары NdflPersonId-OperationId, которые не следует включать в отчет
                def pairOperationsForRemove = []

                // заполняем pairOperationsForRemove
                pairPersonOperationIdMap.each { k, v ->
                    ScriptUtils.checkInterrupted()
                    def absentAccruedDate = true
                    def absentTaxDate = true
                    def absentTransferDate = true
                    for (def income : v) {
                        if (income.incomeAccruedDate != null && income.incomeAccruedSumm != null && !income.incomeAccruedSumm.equals(new BigDecimal(0))) {
                            absentAccruedDate = false
                            break
                        }
                    }
                    for (def income : v) {
                        if (income.taxDate != null) {
                            if (income.taxDate >= reportPeriod.calendarStartDate && income.taxDate <= reportPeriod.endDate) {
                                absentTaxDate = false
                                break
                            }
                        }
                    }
                    for (def income : v) {
                        if (income.taxTransferDate != null) {
                            if (income.taxTransferDate >= reportPeriod.calendarStartDate && income.taxTransferDate <= reportPeriod.endDate) {
                                absentTransferDate = false
                                break
                            }
                        }
                    }
                    if (absentAccruedDate || absentTaxDate || absentTransferDate) {
                        pairOperationsForRemove << k
                    }
                }
                pairOperationsForRemove.each {
                    pairPersonOperationIdMap.remove(it)
                }
                if (pairPersonOperationIdMap.size() != 0) {

                    ДохНал() {
                        pairPersonOperationIdMap.values().each { listIncomes ->
                            ScriptUtils.checkInterrupted()
                            def incomeAccruedDate
                            def taxDate
                            def transferDate
                            def incomePayoutSumm = new BigDecimal(0)
                            def withholdingTax = 0
                            listIncomes.each {
                                if (it.incomeAccruedDate != null && it.incomeAccruedSumm != null && !it.incomeAccruedSumm.equals(new BigDecimal(0))) {
                                    incomeAccruedDate = it.incomeAccruedDate
                                }
                                if (it.taxDate != null) {
                                    boolean notEmpty = false
                                    if (it.withholdingTax != null && it.withholdingTax != 0) {
                                        notEmpty = true
                                    } else if (it.incomeAccruedSumm.equals(it.incomeAccruedSumm) && it.taxBase.equals(new BigDecimal(0)) && it.calculatedTax == 0) {
                                        notEmpty = true
                                    }
                                    if (notEmpty) {
                                        taxDate = it.taxDate
                                    }
                                }
                                if (it.taxTransferDate != null && it.taxSumm != null && it.taxSumm != 0) {
                                    transferDate = it.taxTransferDate
                                }
                                if (it.incomePayoutSumm != null) {
                                    incomePayoutSumm = incomePayoutSumm.add(it.incomePayoutSumm)
                                }
                                if (it.withholdingTax != null) {
                                    withholdingTax += it.withholdingTax
                                }
                            }
                            СумДата(
                                    ДатаФактДох: incomeAccruedDate?.format(DATE_FORMAT_DOT),
                                    ДатаУдержНал: taxDate?.format(DATE_FORMAT_DOT),
                                    СрокПрчслНал: transferDate?.format(DATE_FORMAT_DOT),
                                    ФактДоход: ScriptUtils.round(incomePayoutSumm, 2),
                                    УдержНал: withholdingTax
                            ) {}
                        }
                    }
                }
            }
        }
    }
    ScriptUtils.checkInterrupted()
    saveFileInfo(currDate, fileName)

//    println(writer)
}

class PairPersonOperationId {
    Long ndflPersonId
    Long operationId

    PairPersonOperationId(Long ndflPersonId, Long operationId) {
        this.ndflPersonId = ndflPersonId
        this.operationId = operationId
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        PairPersonOperationId that = (PairPersonOperationId) o

        if (ndflPersonId != that.ndflPersonId) return false
        if (operationId != that.operationId) return false

        return true
    }

    int hashCode() {
        int result
        result = ndflPersonId.hashCode()
        result = 31 * result + operationId.hashCode()
        return result
    }
}

def saveFileInfo(currDate, fileName) {
    def fileUuid = blobDataServiceDaoImpl.create(xmlFile, fileName + ".XML", new Date())
    def createUser = declarationService.getSystemUserInfo().getUser()

    def fileTypeProvider = refBookFactory.getDataProvider(RefBook.Id.ATTACH_FILE_TYPE.getId())
    def fileTypeId = fileTypeProvider.getUniqueRecordIds(new Date(), "CODE = ${AttachFileType.TYPE_2.id}").get(0)

    def declarationDataFile = new DeclarationDataFile()
    declarationDataFile.setDeclarationDataId(declarationData.id)
    declarationDataFile.setUuid(fileUuid)
    declarationDataFile.setUserName(createUser.getName())
    declarationDataFile.setUserDepartmentName(departmentService.getParentsHierarchyShortNames(createUser.getDepartmentId()))
    declarationDataFile.setFileTypeId(fileTypeId)
    declarationDataFile.setDate(currDate)
    declarationService.saveFile(declarationDataFile)
}

//Вычислить сумму для УдержНалИт
def getNalUderzh(def incomes) {
    def toReturn = 0L
    incomes.each {
        if (it.withholdingTax != null) {
            toReturn += it.withholdingTax
        }
    }
    return toReturn
}

//Вычислить сумму для 	НеУдержНалИт
def getNalNeUderzh(def incomes) {
    def toReturn = 0L
    incomes.each {
        if (it.notHoldingTax != null) {
            toReturn += it.notHoldingTax
        }
    }
    return toReturn
}

// Вычислить сумму возвращенную
def getTotalRefound(def incomes) {
    def toReturn = 0L
    incomes.each {
        if (it.refoundTax != null) {
            toReturn += it.refoundTax
        }
    }
    return toReturn
}

// группирока по налоговой ставке
def groupByTaxRate(def incomes) {
    def toReturn = [:]
    def rates = []
    incomes.each {
        if (it.taxRate != null && !rates.contains(it.taxRate)) {
            rates << it.taxRate
        }
    }

    rates.each { rate -> toReturn[rate] = incomes.findAll { it.taxRate.equals(rate) } }
    return toReturn
}

// Вычислить сумму дохода
def getSumOfAccrued(incomes) {
    def toReturn = new BigDecimal(0)
    incomes.each {
        if (it.incomeAccruedSumm != null) {
            toReturn = toReturn.add(it.incomeAccruedSumm)
        }
    }
    return toReturn
}

// Вычислить сумму дохода  и имеющих в графе 4 раздела 2 = 1010
def getSumOfAccruedWhenIncomeCodeDiv(incomes) {
    def toReturn = new BigDecimal(0)
    incomes.each {
        if (it.incomeAccruedSumm != null && it.incomeCode == "1010") {
            toReturn = toReturn.add(it.incomeAccruedSumm)
        }
    }
    return toReturn
}

// Вычислить общую сумму вычетов
def getSumOfDeductions(incomes) {
    def toReturn = new BigDecimal(0)
    incomes.each {
        if (it.totalDeductionsSumm != null && it.incomeAccruedDate != null && it.incomeAccruedSumm != null && it.incomeAccruedSumm != 0) {
            toReturn = toReturn.add(it.totalDeductionsSumm)
        }
    }
    return toReturn
}

// Вычислить общую исчисленного НДФЛ
def getSumOfCalculatedTax(incomes) {
    def toReturn = 0L
    incomes.each {
        if (it.calculatedTax != null) {
            toReturn += it.calculatedTax
        }
    }
    return toReturn
}

def getSumOfCalculatedTaxWhenIncomeCodeDiv(incomes) {
    def toReturn = 0L
    incomes.each {
        if (it.calculatedTax != null && it.incomeCode == "1010") {
            toReturn += it.calculatedTax
        }
    }
    return toReturn
}


def getSumOfPrepayments(prepayments) {
    def toReturn = 0L
    prepayments.each {
        if (it.summ != null) {
            toReturn += it.summ
        }
    }
    return toReturn
}

/************************************* ПРОВЕРКА XML *****************************************************************/
def checkXml() {
    //---------------------------------------------------------------
    // Внутридокументные проверки
    ScriptUtils.checkInterrupted()
    def msgError = "В форме \"%s\" КПП: \"%s\" ОКТМО: \"%s\" "
    msgError = sprintf(msgError, FORM_NAME_NDFL6, declarationData.kpp, declarationData.oktmo)

    def ndfl6Stream = declarationService.getXmlStream(declarationData.id)
    def fileNode = new XmlSlurper().parse(ndfl6Stream);

    // ВнДок2 Расчет погрешности
    def sumDataNodes = fileNode.depthFirst().grep { it.name() == NODE_NAME_SUM_DATA6 }
    def mathError = sumDataNodes.size()

    def sumStavkaNodes = fileNode.depthFirst().grep { it.name() == NODE_NAME_SUM_STAVKA6 }
    sumStavkaNodes.each { sumStavkaNode ->
        ScriptUtils.checkInterrupted()
        def stavka = sumStavkaNode.attributes()[ATTR_RATE].toDouble()
        def nachislDoh = sumStavkaNode.attributes()[ATTR_NACHISL_DOH6].toDouble()
        def vichetNal = sumStavkaNode.attributes()[ATTR_VICHET_NAL6].toDouble()
        def ischislNal = sumStavkaNode.attributes()[ATTR_ISCHISL_NAL6].toDouble()
        def avansPlat = sumStavkaNode.attributes()[ATTR_AVANS_PLAT6].toDouble()

        // ВнДок1 Сравнение сумм вычетов и дохода
        if (vichetNal > nachislDoh) {
            logger.error(msgError + " сумма налоговых вычетов превышает сумму начисленного дохода.")
        }

        // ВнДок2 Исчисленный налог
        if (((nachislDoh - vichetNal) / 100 * stavka > ischislNal + mathError) ||
                ((nachislDoh - vichetNal) / 100 * stavka < ischislNal - mathError)) {
            logger.error(msgError + " неверно рассчитана сумма исчисленного налога.")
        }

        // ВнДок3 Авансовый платеж
        if (avansPlat > ischislNal) {
            logger.error(msgError + " завышена сумма фиксированного авансового платежа.")
        }
    }

    //---------------------------------------------------------------
    // Междокументные проверки

    // Код отчетного периода
    def reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
    def periodCode = getRefBookValue(REF_BOOK_PERIOD_CODE_ID, reportPeriod?.dictTaxPeriodId)?.CODE?.stringValue

    if (["34", "90"].contains(periodCode)) {
        def ndfl2DeclarationDataIds = getNdfl2DeclarationDataId(reportPeriod.taxPeriod.year)
        if (ndfl2DeclarationDataIds.size() > 0) {
            checkBetweenDocumentXml(ndfl2DeclarationDataIds)
        }
    }
}

/**
 * Получение идентификаторо DeclarationData 2-ндфл
 * @param taxPeriodYear - отчетный год
 *
 */
def getNdfl2DeclarationDataId(def taxPeriodYear) {
    def result = []
    def declarationDataList = declarationService.find(DECLARATION_TYPE_NDFL2_ID, declarationData.departmentReportPeriodId)
    for (DeclarationData dd : declarationDataList) {
        ScriptUtils.checkInterrupted()
        def reportPeriod = reportPeriodService.get(dd.reportPeriodId)
        def periodCode = getRefBookValue(REF_BOOK_PERIOD_CODE_ID, reportPeriod?.dictTaxPeriodId)?.CODE?.stringValue
        if (reportPeriod.taxPeriod.year == taxPeriodYear
                && periodCode == "34"
                && dd.kpp == declarationData.kpp
                && dd.oktmo == declarationData.oktmo
                && dd.taxOrganCode == declarationData.taxOrganCode) {
            result.add(dd.id)
        }
    }
    return result
}

/**
 * Междокументные проверки
 * @return
 */
def checkBetweenDocumentXml(def ndfl2DeclarationDataIds) {

    def msgError = "%s КПП: \"%s\" ОКТМО: \"%s\" не соответствуют форме %s КПП: \"%s\" ОКТМО: \"%s\""
    msgError = "Контрольные соотношения по %s формы " + sprintf(msgError, FORM_NAME_NDFL6, declarationData.kpp, declarationData.oktmo, FORM_NAME_NDFL2, declarationData.kpp, declarationData.oktmo)

    // МежДок4
    // Мапа <Ставка, НачислДох>
    def mapNachislDoh6 = [:]
    // Мапа <Ставка, Сумма(СумИтНалПер.СумДохОбщ)>
    def mapSumDohObch2 = [:]

    // МежДок5
    // НачислДохДив
    def nachislDohDiv6 = 0.0
    // Сумма(СвСумДох.СумДоход)
    def sumDohDivObch2 = 0.0

    // МежДок6
    // Мапа <Ставка, ИсчислНал>
    def mapIschislNal6 = [:]
    // Мапа <Ставка, Сумма(СумИтНалПер.НалИсчисл)>
    def mapNalIschisl2 = [:]

    // МежДок7
    // НеУдержНалИт
    def neUderzNalIt6 = 0
    // Сумма(СумИтНалПер.НалНеУдерж)
    def nalNeUderz2 = 0

    // МежДок8
    def kolFl6 = 0
    def kolFl2 = 0

    def ndfl6Stream = declarationService.getXmlStream(declarationData.id)
    def fileNode6Ndfl = new XmlSlurper().parse(ndfl6Stream);
    def sumStavkaNodes6 = fileNode6Ndfl.depthFirst().grep { it.name() == NODE_NAME_SUM_STAVKA6 }
    sumStavkaNodes6.each { sumStavkaNode6 ->
        ScriptUtils.checkInterrupted()
        def stavka6 = Integer.valueOf(sumStavkaNode6.attributes()[ATTR_RATE]) ?: 0

        // МежДок4
        def nachislDoh6 = ScriptUtils.round(Double.valueOf(sumStavkaNode6.attributes()[ATTR_NACHISL_DOH6]), 2) ?: 0
        mapNachislDoh6.put(stavka6, nachislDoh6)

        // МежДок5
        if (stavka6 == RATE_THIRTEEN) {
            nachislDohDiv6 = ScriptUtils.round(Double.valueOf(sumStavkaNode6.attributes()[ATTR_NACHISL_DOH_DIV6]), 2) ?: 0
        }

        // МежДок6
        def ischislNal6 = Long.valueOf(sumStavkaNode6.attributes()[ATTR_ISCHISL_NAL6]) ?: 0
        mapIschislNal6.put(stavka6, ischislNal6)
    }

    def obobshPokazNodes6 = fileNode6Ndfl.depthFirst().grep { it.name() == NODE_NAME_OBOBSH_POKAZ6 }
    obobshPokazNodes6.each { obobshPokazNode6 ->
        ScriptUtils.checkInterrupted()
        // МежДок7
        neUderzNalIt6 = Long.valueOf(obobshPokazNode6.attributes()[ATTR_NE_UDERZ_NAL_IT6]) ?: 0

        // МежДок8
        kolFl6 = Integer.valueOf(obobshPokazNode6.attributes()[ATTR_KOL_FL_DOHOD6]) ?: 0
    }

    // Суммы значений всех 2-НДФЛ сравниваются с одним 6-НДФЛ
    ndfl2DeclarationDataIds.each { ndfl2DeclarationDataId ->
        ScriptUtils.checkInterrupted()
        def ndfl2Stream = declarationService.getXmlStream(ndfl2DeclarationDataId)
        def fileNode2Ndfl = new XmlSlurper().parse(ndfl2Stream);

        // МежДок8
        def documentNodes = fileNode2Ndfl.depthFirst().findAll { it.name() == NODE_NAME_DOCUMNET2 }
        kolFl2 += documentNodes.size()

        def svedDohNodes = fileNode2Ndfl.depthFirst().grep { it.name() == NODE_NAME_SVED_DOH2 }
        svedDohNodes.each { svedDohNode ->
            ScriptUtils.checkInterrupted()
            def stavka2 = Integer.valueOf(svedDohNode.attributes()[ATTR_RATE]) ?: 0

            // МежДок4
            def sumDohObch2 = mapSumDohObch2.get(stavka2)
            sumDohObch2 = sumDohObch2 == null ? 0 : sumDohObch2

            // МежДок6
            def nalIschisl2 = mapNalIschisl2.get(stavka2)
            nalIschisl2 = nalIschisl2 == null ? 0 : nalIschisl2

            def sumItNalPerNodes = svedDohNode.depthFirst().grep { it.name() == NODE_NAME_SUM_IT_NAL_PER2 }
            sumItNalPerNodes.each { sumItNalPerNode ->
                sumDohObch2 += ScriptUtils.round(Double.valueOf(sumItNalPerNode.attributes()[ATTR_SUM_DOH_OBSH2]), 2) ?: 0
                nalIschisl2 += Long.valueOf(sumItNalPerNode.attributes()[ATTR_NAL_ISCHISL2]) ?: 0

                // МежДок7
                nalNeUderz2 += Long.valueOf(sumItNalPerNode.attributes()[ATTR_NAL_NE_UDERZ2]) ?: 0
            }
            mapSumDohObch2.put(stavka2, sumDohObch2)
            mapNalIschisl2.put(stavka2, nalIschisl2)

            // МежДок5
            if (stavka2 == RATE_THIRTEEN) {
                def svSumDohNodes = svedDohNode.depthFirst().grep { it.name() == NODE_NAME_SV_SUM_DOH2 }
                svSumDohNodes.each { svSumDohNode ->
                    if (svSumDohNode.attributes()[ATTR_KOD_DOHOD2].toString() == "1010") {
                        sumDohDivObch2 += svSumDohNode.attributes()[ATTR_SUM_DOHOD2] ?: 0
                    }
                }
            }
        }
    }

    // МежДок4
    mapNachislDoh6.each { stavka6, nachislDoh6 ->
        ScriptUtils.checkInterrupted()
        def sumDohObch2 = mapSumDohObch2.get(stavka6)

        if (ScriptUtils.round(nachislDoh6, 2) != ScriptUtils.round(sumDohObch2, 2)) {
            def msgErrorRes = sprintf(msgError, "сумме начисленного дохода") + " по ставке " + stavka6
            logger.error(msgErrorRes)
        }
    }

    // МежДок5
    if (ScriptUtils.round(nachislDohDiv6, 2) != ScriptUtils.round(sumDohDivObch2, 2)) {
        def msgErrorRes = sprintf(msgError, "сумме начисленного дохода в виде дивидендов")
        logger.error(msgErrorRes)
    }

    // МежДок6

    mapIschislNal6.each { stavka6, ischislNal6 ->
        def nalIschisl2 = mapNalIschisl2.get(stavka6)
        if (ischislNal6 != nalIschisl2) {
            def msgErrorRes = sprintf(msgError, "сумме налога исчисленного") + " по ставке " + stavka6
            logger.error(msgErrorRes)
        }
    }

    // МежДок7
    if (neUderzNalIt6 != nalNeUderz2) {
        def msgErrorRes = sprintf(msgError, "сумме налога, не удержанная налоговым агентом")
        logger.error(msgErrorRes)
    }

    // МежДок8
    if (kolFl6 != kolFl2) {
        def msgErrorRes = sprintf(msgError, "количеству физических лиц, получивших доход")
        logger.error(msgErrorRes)
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
def generateXmlFileId(def departmentParamIncomeRow, def INN, def KPP, def kodNoProm) {
    def R_T = "NO_NDFL6"
    def A = kodNoProm
    def K = departmentParamIncomeRow?.TAX_ORGAN_CODE?.value
    def O = INN + KPP
    def currDate = new Date().format(DATE_FORMAT_UNDERLINE)
    def N = UUID.randomUUID().toString().toUpperCase()
    def res = R_T + "_" + A + "_" + K + "_" + O + "_" + currDate + "_" + N
    return res
}

/**
 * Период
 */
def getPeriod(def departmentParamIncomeRow, def periodCode) {
    if (departmentParamIncomeRow.REORG_FORM_CODE && !departmentParamIncomeRow.REORG_FORM_CODE.empty) {
        def result;
        switch (periodCode) {
            case "21":
                result = "51"
                break
            case "31":
                result = "52"
                break
            case "33":
                result = "53"
                break
            case "34":
                result = "90"
                break
        }
        return result;
    } else {
        return periodCode;
    }
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

/************************************* СОЗДАНИЕ ФОРМЫ *****************************************************************/

@Field
def departmentParamTableList = null;

@Field
final int REF_BOOK_DOC_STATE = 929

def createForm() {
    def departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
    def pairKppOktmoList = []

    def currDeclarationTemplate = declarationService.getTemplate(declarationData.declarationTemplateId)
    def declarationTypeId = currDeclarationTemplate.type.id

    if (departmentReportPeriod.correctionDate != null) {
        def prevDepartmentPeriodReport = getPrevDepartmentReportPeriod(departmentReportPeriod)
        def declarations = declarationService.find(declarationTypeId, prevDepartmentPeriodReport?.id)
        def declarationsForRemove = []
        declarations.each { declaration ->
            ScriptUtils.checkInterrupted()
            def stateDocReject = getProvider(REF_BOOK_DOC_STATE).getRecords(null, null, "NAME = 'Отклонен'", null).get(0).id.value
            def stateDocNeedClarify = getProvider(REF_BOOK_DOC_STATE).getRecords(null, null, "NAME = 'Требует уточнения'", null).get(0).id.value
            def stateDocError = getProvider(REF_BOOK_DOC_STATE).getRecords(null, null, "NAME = 'Ошибка'", null).get(0).id.value
            def declarationTemplate = declarationService.getTemplate(declaration.declarationTemplateId)
            if (!(declarationTemplate.declarationFormKind == DeclarationFormKind.REPORTS && (declaration.docState == stateDocReject
                    || declaration.docState == stateDocNeedClarify || declaration.docState == stateDocError))) {
                declarationsForRemove << declaration
            }
        }
        declarations.removeAll(declarationsForRemove)
        declarations.each { declaration ->
            pairKppOktmoList << new PairKppOktmo(declaration.kpp, declaration.oktmo, null)
        }
        // Поиск КПП и ОКТМО для некорр периода
    } else {
        def departmentParam = getDepartmentParam(departmentReportPeriod.departmentId, departmentReportPeriod.reportPeriod.id)
        def departmentParamTableList = getDepartmentParamTableList(departmentParam?.id, departmentReportPeriod.reportPeriod.id)
        def referencesOktmoList = departmentParamTableList.OKTMO?.value
        referencesOktmoList.removeAll([null])
        def oktmoForDepartment = getOktmoByIdList(referencesOktmoList)

        departmentParamTableList.each { dep ->
            ScriptUtils.checkInterrupted()
            if (dep.OKTMO?.value != null) {
                def oktmo = oktmoForDepartment.get(dep.OKTMO?.value)
                if (oktmo != null) {
                    pairKppOktmoList << new PairKppOktmo(dep.KPP?.value, oktmo.CODE.value, dep?.TAX_ORGAN_CODE?.value)
                }
            }
        }
    }

    // список форм рну-ндфл для отчетного периода всех ТБ
    def allDeclarationData = findAllTerBankDeclarationData(departmentReportPeriod)

    // Список физлиц для каждой пары КПП и ОКТМО
    def ndflPersonsGroupedByKppOktmo = [:]

    pairKppOktmoList.each { pair ->
        ScriptUtils.checkInterrupted()
        def ndflPersons = ndflPersonService.findNdflPersonByPairKppOktmo(allDeclarationData.id, pair.kpp.toString(), pair.oktmo.toString())
        if (ndflPersons != null && ndflPersons.size() != 0) {
            addNdflPersons(ndflPersonsGroupedByKppOktmo, pair, ndflPersons)
        }
    }
    //logger.info(ndflPersonsGroupedByKppOktmo.toString())

    declarationService.find(declarationTypeId, declarationData.departmentReportPeriodId).each {
        declarationService.delete(it.id, userInfo)
    }

    ndflPersonsGroupedByKppOktmo.each { npGroup ->
        ScriptUtils.checkInterrupted()
        Map<String, Object> params
        def oktmo = npGroup.key.oktmo
        def kpp = npGroup.key.kpp
        def taxOrganCode = npGroup.key.taxOrganCode
        def npGropSourcesIdList = npGroup.value.id
        Long ddId
        params = new HashMap<String, Object>()
        ddId = declarationService.create(logger, declarationData.declarationTemplateId, userInfo,
                departmentReportPeriodService.get(declarationData.departmentReportPeriodId), taxOrganCode, kpp.toString(), oktmo.toString(), null, null, null)

        params.put(NDFL_PERSON_KNF_ID, npGropSourcesIdList)
        formMap.put(ddId, params)
    }

}

def getOktmoByIdList(idList) {
    def provider = getProvider(REF_BOOK_OKTMO_ID)
    return provider.getRecordData(idList)
}

/**
 * получить id всех ТБ для данного отчетного периода
 * @param departmentReportPeriod
 * @return
 */
def findAllTerBankDeclarationData(def departmentReportPeriod) {
    def allDepartmentReportPeriodIds = departmentReportPeriodService.getIdsByDepartmentTypeAndReportPeriod(DepartmentType.TERR_BANK.getCode(), departmentReportPeriod.reportPeriod.id)
    def allDeclarationData = []
    allDepartmentReportPeriodIds.each {
        ScriptUtils.checkInterrupted()
        allDeclarationData.addAll(declarationService.find(DECLARATION_TYPE_RNU_NDFL_ID, it))
    }
    // удаление форм не со статусом Принята
    def declarationsForRemove = []
    allDeclarationData.each { declaration ->
        if (declaration.state != State.ACCEPTED) {
            declarationsForRemove << declaration
        }
    }
    allDeclarationData.removeAll(declarationsForRemove)
    return allDeclarationData
}

def addNdflPersons(ndflPersonsGroupedByKppOktmo, pairKppOktmoBeingComparing, ndflPersonList) {

    def kppOktmoNdflPersons = ndflPersonsGroupedByKppOktmo.get(pairKppOktmoBeingComparing)
    if (kppOktmoNdflPersons == null) {
        ndflPersonsGroupedByKppOktmo.put(pairKppOktmoBeingComparing, ndflPersonList)
    } else {
        def kppOktmoNdflPersonsEntrySet = ndflPersonsGroupedByKppOktmo.entrySet()
        kppOktmoNdflPersonsEntrySet.each {
            if (it.getKey().equals().pairKppOktmoBeingComparing) {
                if (it.getKey().taxOrganCode != pairKppOktmoBeingComparing.taxOrganCode) {
                    logger.warn("Для КПП = ${pairKppOktmoBeingComparing.kpp} ОКТМО = ${pairKppOktmoBeingComparing.oktmo} в справочнике \"Настройки подразделений\" задано несколько значений Кода НО (кон).")
                }
                //Если Коды НО совпадают, для всех дублей пар КПП+ОКТМО создается одна ОНФ, в которой указывается совпадающий Код НО.
                it.getValue().addAll(ndflPersonList)
            }
        }
    }
}

def getPrevDepartmentReportPeriod(departmentReportPeriod) {
    def prevDepartmentReportPeriod = departmentReportPeriodService.getPrevLast(declarationData.departmentId, departmentReportPeriod.reportPeriod.id)
    if (prevDepartmentReportPeriod == null) {
        prevDepartmentReportPeriod = departmentReportPeriodService.getFirst(departmentReportPeriod.departmentId, departmentReportPeriod.reportPeriod.id)
    }
    return prevDepartmentReportPeriod
}

def createReports() {
    ScriptUtils.checkInterrupted()
    ZipArchiveOutputStream zos = new ZipArchiveOutputStream(outputStream);
    scriptParams.put("fileName", "reports.zip")
    try {
        Department department = departmentService.get(declarationData.departmentId);
        DeclarationTemplate declarationTemplate = declarationService.getTemplate(declarationData.declarationTemplateId);
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId);
        String strCorrPeriod = "";
        if (departmentReportPeriod.getCorrectionDate() != null) {
            strCorrPeriod = ", с датой сдачи корректировки " + SDF_DD_MM_YYYY.get().format(departmentReportPeriod.getCorrectionDate());
        }
        String path = String.format("Отчетность %s, %s, %s, %s%s",
                declarationTemplate.getName(),
                department.getName(),
                departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear(), departmentReportPeriod.getReportPeriod().getName(), strCorrPeriod);
        def declarationTypeId = declarationService.getTemplate(declarationData.declarationTemplateId).type.id
        declarationService.find(declarationTypeId, declarationData.departmentReportPeriodId).each {
            ScriptUtils.checkInterrupted()
            if (it.fileName == null) {
                return
            }
            ZipArchiveEntry ze = new ZipArchiveEntry(path + "/" + it.taxOrganCode + "/" + it.fileName + ".xml");
            zos.putArchiveEntry(ze);
            IOUtils.copy(declarationService.getXmlStream(it.id), zos)
            zos.closeArchiveEntry();
        }
    } finally {
        IOUtils.closeQuietly(zos);
    }
}

/*********************************ПОЛУЧИТЬ ИСТОЧНИКИ*******************************************************************/
@Field
def sourceReportPeriod = null

@Field
def departmentReportPeriodMap = [:]

// Мапа для хранения полного названия подразделения (id подразделения  -> полное название)
@Field
def departmentFullNameMap = [:]


def getReportPeriod() {
    if (sourceReportPeriod == null) {
        sourceReportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
    }
    return sourceReportPeriod
}

/** Получить результат для события FormDataEvent.GET_SOURCES. */
void getSources() {
    ScriptUtils.checkInterrupted()
    if (!(needSources)) {
        // формы-приемники, декларации-истчоники, декларации-приемники не переопределять
        return
    }
    def reportPeriod = getReportPeriod()
    def sourceTypeId = 101
    def departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
    def allDepartmentReportPeriodIds = departmentReportPeriodService.getIdsByDepartmentTypeAndReportPeriod(DepartmentType.TERR_BANK.getCode(), departmentReportPeriod.reportPeriod.id)
    // Найти подразделения в РНУ которых имеются операции из декларации
    def tmpDeclarationDataList = getDeclarationDataList(sourceTypeId, allDepartmentReportPeriodIds)
    def declarationsForRemove = []
    tmpDeclarationDataList.each { declaration ->
        if (declaration.state != State.ACCEPTED) {
            declarationsForRemove << declaration
        }
    }
    tmpDeclarationDataList.removeAll(declarationsForRemove)
    tmpDeclarationDataList.each { tmpDeclarationData ->
        ScriptUtils.checkInterrupted()
        def department = departmentService.get(tmpDeclarationData.departmentId)
        def relation = getRelation(tmpDeclarationData, department, reportPeriod, sourceTypeId)
        if (relation) {
            sources.sourceList.add(relation)
        }
    }
    sources.sourcesProcessedByScript = true
}

/**
 * Получить запись для источника-приемника.
 *
 * @param tmpDeclarationData нф
 * @param department подразделение
 * @param period период нф
 * @param monthOrder номер месяца (для ежемесячной формы)
 */
def getRelation(DeclarationData tmpDeclarationData, Department department, ReportPeriod period, def sourceTypeId) {
    // boolean excludeIfNotExist - исключить несозданные источники

    if (excludeIfNotExist && tmpDeclarationData == null) {
        return null
    }
    // WorkflowState stateRestriction - ограничение по состоянию для созданных экземпляров
    if (stateRestriction && tmpDeclarationData != null && stateRestriction != tmpDeclarationData.state) {
        return null
    }
    Relation relation = new Relation()
    def isSource = sourceTypeId != 101

    DepartmentReportPeriod departmentReportPeriod = getDepartmentReportPeriodById(tmpDeclarationData?.departmentReportPeriodId) as DepartmentReportPeriod
    DeclarationTemplate declarationTemplate = declarationService.getTemplate(sourceTypeId)

    // boolean light - заполняются только текстовые данные для GUI и сообщений
    if (light) {
        /**************  Параметры для легкой версии ***************/
        /** Идентификатор подразделения */
        relation.departmentId = department.id
        /** полное название подразделения */
        relation.fullDepartmentName = getDepartmentFullName(department.id)
        /** Дата корректировки */
        relation.correctionDate = new Date()
        /** Вид нф */
        relation.declarationTypeName = declarationTemplate?.name
        /** Год налогового периода */
        relation.year = period.taxPeriod.year
        /** Название периода */
        relation.periodName = period.name
    }
    /**************  Общие параметры ***************/
    /** подразделение */
    relation.department = department
    /** Период */
    relation.departmentReportPeriod = departmentReportPeriod
    /** Статус ЖЦ */
    relation.declarationState = tmpDeclarationData?.state
    /** форма/декларация создана/не создана */
    relation.created = (tmpDeclarationData != null)
    /** является ли форма источников, в противном случае приемник*/
    relation.source = isSource
    /** Введена/выведена в/из действие(-ия) */
    relation.status = declarationTemplate.status == VersionedObjectStatus.NORMAL
    /** Налог */
    relation.taxType = TaxType.NDFL
    /**************  Параметры НФ ***************/
    /** Идентификатор созданной формы */
    relation.declarationDataId = tmpDeclarationData?.id
    /** Вид НФ */
    relation.declarationTemplate = declarationTemplate
    /** Тип НФ */
    //relation.formDataKind = tmpDeclarationData.kind

    return relation
}
// Найти подразделения в РНУ которых имеются операции из декларации
def getDeclarationDataList(def sourceTypeId, def allDepartmentReportPeriodIds) {
    // Найти все доходы по декларации
    def toReturn = []
    def incomes = ndflPersonService.findNdflPersonIncome(declarationData.id)
    def kppOktmoSet = [].toSet()
    incomes.each { income ->
        kppOktmoSet << new PairKppOktmo(income.kpp, income.oktmo, null)
    }
    for (departmentReportPeriodId in allDepartmentReportPeriodIds) {
        ScriptUtils.checkInterrupted()
        def tmpDepartmentReportPeriod = departmentReportPeriodService.get(departmentReportPeriodId)
        for (kppOktmo in kppOktmoSet) {
            ScriptUtils.checkInterrupted()
            def declarationData = declarationService.findDeclarationDataByKppOktmoOfNdflPersonIncomes(sourceTypeId, departmentReportPeriodId, tmpDepartmentReportPeriod.departmentId, tmpDepartmentReportPeriod.reportPeriod.id, kppOktmo.kpp, kppOktmo.oktmo)
            if (declarationData != null) {
                toReturn << declarationData
                break
            }
        }
    }
    return toReturn
}

def getDepartmentReportPeriodById(def id) {
    if (id != null && departmentReportPeriodMap[id] == null) {
        departmentReportPeriodMap[id] = departmentReportPeriodService.get(id)
    }
    return departmentReportPeriodMap[id]
}
/** Получить полное название подразделения по id подразделения. */
def getDepartmentFullName(def id) {
    if (departmentFullNameMap[id] == null) {
        departmentFullNameMap[id] = departmentService.getParentsHierarchy(id)
    }
    return departmentFullNameMap[id]
}
/************************************* ОБЩИЕ МЕТОДЫ** *****************************************************************/

// Получить список детали подразделения из справочника для некорректировочного периода
def getDepartmentParamTableList(def departmentParamId, def reportPeriodId) {
    if (departmentParamTableList == null) {
        def filter = "REF_BOOK_NDFL_ID = $departmentParamId"
        departmentParamTableList = getProvider(REF_BOOK_NDFL_DETAIL_ID).getRecords(getReportPeriodEndDate(reportPeriodId) - 1, null, filter, null)
        if (departmentParamTableList == null || departmentParamTableList.size() == 0 || departmentParamTableList.get(0) == null) {
            throw new Exception("Ошибка при получении настроек обособленного подразделения. Настройки подразделения заполнены не полностью")
        }
    }
    return departmentParamTableList
}

def getDepartmentParam(def departmentId, def reportPeriodId) {
    if (departmentParam == null) {
        def departmentParamList = getProvider(REF_BOOK_NDFL_ID).getRecords(getReportPeriodEndDate(reportPeriodId) - 1, null, "DEPARTMENT_ID = $departmentId", null)

        if (departmentParamList == null || departmentParamList.size() == 0 || departmentParamList.get(0) == null) {
            throw new Exception("Ошибка при получении настроек обособленного подразделения. Настройки подразделения заполнены не полностью")
        }
        departmentParam = departmentParamList?.get(0)
    }
    return departmentParam
}

/**
 * Получить настройки подразделения
 * @return
 */
def getDepartmentParam(def departmentId) {
    if (departmentParam == null) {
        def departmentParamList = getProvider(REF_BOOK_NDFL_ID).getRecords(getReportPeriodEndDate() - 1, null, "DEPARTMENT_ID = $departmentId", null)
        if (departmentParamList == null || departmentParamList.size() == 0 || departmentParamList.get(0) == null) {
            throw new Exception("Ошибка при получении настроек обособленного подразделения")
        }
        departmentParam = departmentParamList?.get(0)
    }
    return departmentParam
}

/**
 * Получить параметры подразделения
 * @param departmentParamId
 * @return
 */
def getDepartmentParamTable(def departmentParamId) {
    if (departmentParamTable == null) {
        def filter = "REF_BOOK_NDFL_ID = $departmentParamId and KPP ='${declarationData.kpp}'"
        def departmentParamTableList = getProvider(REF_BOOK_NDFL_DETAIL_ID).getRecords(getReportPeriodEndDate() - 1, null, filter, null)
        if (departmentParamTableList == null || departmentParamTableList.size() == 0 || departmentParamTableList.get(0) == null) {
            throw new Exception("Ошибка при получении настроек обособленного подразделения")
        }
        departmentParamTable = departmentParamTableList.get(0)
    }
    return departmentParamTable
}

def getOktmoById(id) {
    def oktmo = OKTMO_CACHE.get(id)
    if (oktmo == null) {
        def rpe = getReportPeriodEndDate(declarationData.reportPeriodId)
        def oktmoList = getProvider(REF_BOOK_OKTMO_ID).getRecords(rpe, null, "ID = ${id}", null)
        if (oktmoList.size() != 0) {
            oktmo = oktmoList.get(0)
            OKTMO_CACHE[id] = oktmo
        }

    }
    return oktmo
}

def getReportPeriodEndDate(def reportPeriodId) {
    if (reportPeriodEndDate == null) {
        reportPeriodEndDate = reportPeriodService.getEndDate(reportPeriodId)?.time
    }
    return reportPeriodEndDate
}

/**
 * Получить дату окончания отчетного периода
 * @return
 */
def getReportPeriodEndDate() {
    if (reportPeriodEndDate == null) {
        reportPeriodEndDate = reportPeriodService.getEndDate(declarationData.reportPeriodId)?.time
    }
    return reportPeriodEndDate
}

/**
 * Получить "Коды мест предоставления документа"
 * @return
 */
def getRefPresentPlace() {
    if (presentPlaceCodeCache.size() == 0) {
        def refBookMap = getRefBook(REF_BOOK_TAX_PLACE_TYPE_CODE_ID)
        refBookMap.each { refBook ->
            presentPlaceCodeCache.put(refBook?.id?.numberValue, refBook)
        }
    }
    return presentPlaceCodeCache
}

/**
 * Получить все записи справочника по его идентификатору
 * @param refBookId - идентификатор справочника
 * @return - возвращает лист
 */
def getRefBook(def long refBookId) {
    // Передаем как аргумент только срок действия версии справочника
    def refBookList = getProvider(refBookId).getRecords(getReportPeriodEndDate() - 1, null, null, null)
    if (refBookList == null || refBookList.size() == 0) {
        throw new Exception("Ошибка при получении записей справочника " + refBookId)
    }
    return refBookList
}

class PairKppOktmo {
    def kpp
    def oktmo
    def taxOrganCode

    PairKppOktmo(def kpp, def oktmo, def taxOrganCode) {
        this.kpp = kpp
        this.oktmo = oktmo
        this.taxOrganCode = taxOrganCode
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        PairKppOktmo that = (PairKppOktmo) o

        if (kpp != that.kpp) return false
        if (oktmo != that.oktmo) return false

        return true
    }

    int hashCode() {
        int result
        result = (kpp != null ? kpp.hashCode() : 0)
        result = 31 * result + (oktmo != null ? oktmo.hashCode() : 0)
        return result
    }
}
/************************************* СПЕЦОТЧЕТ **********************************************************************/

@Field final String ALIAS_PRIMARY_RNU_W_ERRORS = "primary_rnu_w_errors"

@Field final String TRANSPORT_FILE_TEMPLATE = "ТФ"

def createSpecificReport() {
    def alias = scriptSpecificReportHolder.getDeclarationSubreport().getAlias()
    if (alias == ALIAS_PRIMARY_RNU_W_ERRORS) {
        createPrimaryRnuWithErrors()
        return
    }

    def params = scriptSpecificReportHolder.subreportParamValues ?: new HashMap<String, Object>()

    def jasperPrint = declarationService.createJasperReport(scriptSpecificReportHolder.getFileInputStream(), params, {
        buildXmlForSpecificReport(it)
    });

    declarationService.exportXLSX(jasperPrint, scriptSpecificReportHolder.getFileOutputStream());
    scriptSpecificReportHolder.setFileName(scriptSpecificReportHolder.getDeclarationSubreport().getAlias() + ".xlsx")
}

/**
 * Создать Спецотчет Первичные РНУ с ошибками
 * @return
 */
def createPrimaryRnuWithErrors() {
    // Сведения о доходах из КНФ, которая является источником для входящей ОНФ и записи в реестре справок соответствующим доходам физлицам имеют ошибки
    List<NdflPersonIncome> ndflPersonIncomeFromRNUConsolidatedList = ndflPersonService.findNdflPersonIncomeConsolidatedRNU6Ndfl(declarationData.id, declarationData.kpp, declarationData.oktmo)
    // Сведения о вычетах имеющие такой же operationId как и сведения о доходах
    List<NdflPersonDeduction> ndflPersonDeductionFromRNUConsolidatedList = []
    // Сведения об авансах имеющие такой же operationId как и сведения о доходах
    List<NdflPersonPrepayment> ndflPersonPrepaymentFromRNUConsolidatedList = []

    ndflPersonIncomeFromRNUConsolidatedList.each {
        ScriptUtils.checkInterrupted()
        ndflPersonDeductionFromRNUConsolidatedList.addAll(ndflPersonService.findDeductionsByNdflPersonAndOperation(it.ndflPersonId, it.operationId))
        ndflPersonPrepaymentFromRNUConsolidatedList.addAll(ndflPersonService.findPrepaymentsByNdflPersonAndOperation(it.ndflPersonId, it.operationId))
    }

    ndflPersonIncomeFromRNUConsolidatedList.each {
        ScriptUtils.checkInterrupted()
        NdflPersonIncome ndflPersonIncomePrimary = ndflPersonService.getIncome(it.sourceId)
        NdflPerson ndflPersonPrimary = initNdflPersonPrimary(ndflPersonIncomePrimary.ndflPersonId)
        ndflPersonPrimary.incomes.add(ndflPersonIncomePrimary)
    }

    ndflPersonDeductionFromRNUConsolidatedList.each
    ScriptUtils.checkInterrupted() {
        NdflPersonDeduction ndflPersonDeductionPrimary = ndflPersonService.getDeduction(it.sourceId)
        NdflPerson ndflPersonPrimary = initNdflPersonPrimary(ndflPersonDeductionPrimary.ndflPersonId)
        ndflPersonPrimary.deductions.add(ndflPersonDeductionPrimary)
    }

    ndflPersonDeductionFromRNUConsolidatedList.each {
        ScriptUtils.checkInterrupted()
        NdflPersonPrepayment ndflPersonPrepaymentPrimary = ndflPersonService.getPrepayment(it.sourceId)
        NdflPerson ndflPersonPrimary = initNdflPersonPrimary(ndflPersonPrepaymentPrimary.ndflPersonId)
        ndflPersonPrimary.prepayments.add(ndflPersonPrepaymentPrimary)
    }
    fillPrimaryRnuWithErrors()
}

/**
 * Заполнение печатного представления спецотчета "Первичные РНУ с ошибками"
 * @return
 */
def fillPrimaryRnuWithErrors() {
    def writer = scriptSpecificReportHolder.getFileOutputStream()
    def workbook = getSpecialReportTemplate()
    fillGeneralData(workbook)
    fillPrimaryRnuNDFLWithErrorsTable(workbook)
    workbook.write(writer)
    writer.close()
    scriptSpecificReportHolder
            .setFileName(scriptSpecificReportHolder.getDeclarationSubreport().getAlias() + ".xlsx")
}

/**
 * Заполнение шапки Спецотчета Первичные РНУ с ошибками
 */
def fillGeneralData(workbook) {
    XSSFSheet sheet = workbook.getSheetAt(0)
    XSSFCellStyle style = makeStyleLeftAligned(workbook)
    DeclarationTemplate declarationTemplate = declarationService.getTemplate(declarationData.declarationTemplateId)
    DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
    Department department = departmentService.get(departmentReportPeriod.departmentId)
    // Вид отчетности
    String declarationTypeName = declarationTemplate.type.name
    String note = declarationData.note
    // Период
    int year = departmentReportPeriod.reportPeriod.taxPeriod.year
    String periodName = getProvider(REPORT_PERIOD_TYPE_ID)
            .getRecords(getReportPeriodEndDate(declarationData.reportPeriodId), null, "ID = ${departmentReportPeriod.reportPeriod.dictTaxPeriodId}", null).get(0).NAME.value
    // Территориальный банк
    String departmentName = department.name
    // КПП
    String kpp = declarationData.kpp
    //	Дата сдачи корректировки
    String dateDelivery = getProvider(REPORT_PERIOD_TYPE_ID)
            .getRecords(getReportPeriodEndDate(declarationData.reportPeriodId), null, "ID = ${departmentReportPeriod.reportPeriod.dictTaxPeriodId}", null).get(0).END_DATE.value?.format(DATE_FORMAT_DOTTED)
    // ОКТМО
    String oktmo = declarationData.oktmo
    // Код НО (конечный)
    String taxOrganCode = declarationData.taxOrganCode
    // Дата формирования
    String currentDate = new Date().format(DATE_FORMAT_DOTTED, TimeZone.getTimeZone('Europe/Moscow'))

    XSSFCell cell1 = sheet.getRow(2).createCell(1)
    cell1.setCellValue(declarationTypeName + " " + note)
    cell1.setCellStyle(style)
    XSSFCell cell2 = sheet.getRow(3).createCell(1)
    cell2.setCellValue(year + ":" + periodName)
    cell2.setCellStyle(style)
    XSSFCell cell3 = sheet.getRow(4).createCell(1)
    cell3.setCellValue(dateDelivery)
    cell3.setCellStyle(style)
    XSSFCell cell4 = sheet.getRow(5).createCell(1)
    cell4.setCellValue(departmentName)
    cell4.setCellStyle(style)
    XSSFCell cell5 = sheet.getRow(6).createCell(1)
    cell5.setCellValue(kpp)
    cell5.setCellStyle(style)
    XSSFCell cell6 = sheet.getRow(7).createCell(1)
    cell6.setCellValue(oktmo)
    cell6.setCellStyle(style)
    XSSFCell cell7 = sheet.getRow(8).createCell(1)
    cell7.setCellValue(taxOrganCode)
    cell7.setCellStyle(style)
    XSSFCell cell8 = sheet.getRow(2).createCell(11)
    cell8.setCellValue(currentDate)
    cell8.setCellStyle(style)
}

def initNdflPersonPrimary(ndflPersonId) {
    NdflPerson ndflPersonPrimary = ndflpersonFromRNUPrimary[ndflPersonId]
    if (ndflPersonPrimary == null) {
        ndflPersonPrimary = ndflPersonService.get(ndflPersonId)
        ndflPersonPrimary.incomes.clear()
        ndflPersonPrimary.deductions.clear()
        ndflPersonPrimary.prepayments.clear()
        ndflpersonFromRNUPrimary[ndflPersonId] = ndflPersonPrimary
    }
    return ndflPersonPrimary
}

/**
 * Заполнить таблицу Спецотчета Первичные РНУ с ошибками
 * @param workbook
 * @return
 */
def fillPrimaryRnuNDFLWithErrorsTable(final XSSFWorkbook workbook) {
    XSSFSheet sheet = workbook.getSheetAt(0)
    def startIndex = 12
    ndflpersonFromRNUPrimary.values().each { ndflPerson ->
        ndflPerson.incomes.each { income ->
            ScriptUtils.checkInterrupted()
            fillPrimaryRnuNDFLWithErrorsRow(workbook, ndflPerson, income, "Свед о дох", startIndex)
            startIndex++
        }
    }
}

/**
 * Заполнение строки для таблицы Спецотчета Первичные РНУ с ошибками
 * @param workbook
 * @param ndflPerson
 * @param operation операция отражающая доход, вычет или аванс
 * @param sectionName Название раздела, в котором содержится операция
 * @param index текущий индекс строки таблицы
 * @return
 */
def fillPrimaryRnuNDFLWithErrorsRow(final XSSFWorkbook workbook, ndflPerson, operation, sectionName, index) {
    XSSFSheet sheet = workbook.getSheetAt(0)
    XSSFRow row = sheet.createRow(index)
    def styleLeftAligned = makeStyleLeftAligned(workbook)
    styleLeftAligned = thinBorderStyle(styleLeftAligned)
    def styleCenterAligned = makeStyleCenterAligned(workbook)
    styleCenterAligned = thinBorderStyle(styleCenterAligned)
    styleCenterAligned.setDataFormat(ScriptUtils.createXlsDateFormat(workbook))
    // Первичная НФ
    DeclarationData primaryRnuDeclarationData = declarationService.getDeclarationData(ndflPerson.declarationDataId)
    DeclarationDataFile primaryRnuDeclarationDataFile = declarationService.findFilesWithSpecificType(ndflPerson.declarationDataId, TRANSPORT_FILE_TEMPLATE).get(0)
    DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(primaryRnuDeclarationData.departmentReportPeriodId)
    Department department = departmentService.get(departmentReportPeriod.departmentId)
    // Период
    int year = departmentReportPeriod.reportPeriod.taxPeriod.year
    String periodName = getProvider(REPORT_PERIOD_TYPE_ID)
            .getRecords(getReportPeriodEndDate(primaryRnuDeclarationData.reportPeriodId), null, "ID = ${departmentReportPeriod.reportPeriod.dictTaxPeriodId}", null).get(0).NAME.value
    // Подразделение
    String departmentName = department.shortName
    // АСНУ
    String asnu = getProvider(REF_BOOK_ASNU_ID).getRecords(getReportPeriodEndDate(declarationData.reportPeriodId), null, "ID = ${primaryRnuDeclarationData.asnuId}", null).get(0).NAME.value
    // Имя ТФ
    String transportFileName = primaryRnuDeclarationDataFile.fileName
    // Загрузил ТФ
    String userName = primaryRnuDeclarationDataFile.userName
    // Дата загрузки ТФ
    Date uploadDate = primaryRnuDeclarationDataFile.date
    // Строка с ошибкой.Строка
    String rowNum = operation.rowNum.toString()
    // Строка с ошибкой.ID операции
    String operationId = operation.operationId.toString()
    // 	Физическое лицо, к которому относится ошибочная строка. Документ
    String idDocNumber = ndflPerson.idDocNumber
    // Физическое лицо, к которому относится ошибочная строка. ФИО
    String lastname = ndflPerson.lastName != null ? ndflPerson.lastName + " " : ""
    String firstname = ndflPerson.firstName != null ? ndflPerson.firstName + " " : ""
    String middlename = ndflPerson.middleName != null ? ndflPerson.middleName : ""
    String fio = lastname + firstname + middlename
    // Физическое лицо, к которому относится ошибочная строка. Дата рождения
    Date birthDay = ndflPerson.birthDay

    XSSFCell cell1 = row.createCell(0)
    cell1.setCellValue(periodName + ":" + year)
    cell1.setCellStyle(styleCenterAligned)
    XSSFCell cell2 = row.createCell(1)
    cell2.setCellValue(departmentName)
    cell2.setCellStyle(styleLeftAligned)
    XSSFCell cell3 = row.createCell(2)
    cell3.setCellValue(asnu)
    cell3.setCellStyle(styleCenterAligned)
    XSSFCell cell4 = row.createCell(3)
    cell4.setCellValue(transportFileName)
    cell4.setCellStyle(styleLeftAligned)
    XSSFCell cell5 = row.createCell(4)
    cell5.setCellValue(userName)
    cell5.setCellStyle(styleCenterAligned)
    XSSFCell cell6 = row.createCell(5)
    cell6.setCellValue(uploadDate)
    cell6.setCellStyle(styleCenterAligned)
    XSSFCell cell7 = row.createCell(6)
    cell7.setCellValue(sectionName)
    cell7.setCellStyle(styleCenterAligned)
    XSSFCell cell8 = row.createCell(7)
    cell8.setCellValue(rowNum)
    cell8.setCellStyle(styleCenterAligned)
    XSSFCell cell9 = row.createCell(8)
    cell9.setCellValue(operationId)
    cell9.setCellStyle(styleCenterAligned)
    XSSFCell cell10 = row.createCell(9)
    cell10.setCellValue(idDocNumber)
    cell10.setCellStyle(styleCenterAligned)
    XSSFCell cell11 = row.createCell(10)
    cell11.setCellValue(fio)
    cell11.setCellStyle(styleCenterAligned)
    XSSFCell cell12 = row.createCell(11)
    cell12.setCellValue(birthDay)
    cell12.setCellStyle(styleCenterAligned)
}

// Находит в базе данных шаблон спецотчета по физическому лицу и возвращает его
def getSpecialReportTemplate() {
    def blobData = blobDataServiceDaoImpl.get(scriptSpecificReportHolder.getDeclarationSubreport().getBlobDataId())
    new XSSFWorkbook(blobData.getInputStream())
}

/**
 * Создать стиль ячейки с выравниваем слева
 * @param workbook
 * @return
 */

def makeStyleLeftAligned(XSSFWorkbook workbook) {
    def XSSFCellStyle style = workbook.createCellStyle()
    style.setAlignment(CellStyle.ALIGN_LEFT)
    return style
}

/**
 * Создать стиль ячейки с выравниваем по центру
 * @param workbook
 * @return
 */

def makeStyleCenterAligned(XSSFWorkbook workbook) {
    def XSSFCellStyle style = workbook.createCellStyle()
    style.setAlignment(CellStyle.ALIGN_CENTER)
    return style
}

/**
 * Добавляет к стилю ячейки тонкие границы
 * @param style
 * @return
 */
def thinBorderStyle(final style) {
    style.setBorderTop(CellStyle.BORDER_THIN)
    style.setBorderBottom(CellStyle.BORDER_THIN)
    style.setBorderLeft(CellStyle.BORDER_THIN)
    style.setBorderRight(CellStyle.BORDER_THIN)
    return style
}