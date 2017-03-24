package form_template.ndfl.report_2ndfl_1.v2016

import groovy.xml.XmlUtil

import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field
import groovy.xml.MarkupBuilder
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.CellStyle
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
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecord
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson

switch (formDataEvent) {
    case FormDataEvent.CHECK: //Проверки
        println "!CHECK!"
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
/************************************* ДАННЫЕ ДЛЯ ОБЩИХ СОБЫТИЙ *******************************************************/

/************************************* СОЗДАНИЕ XML *****************************************************************/

// Количество физических лиц в одном xml-файле
@Field
final int NUMBER_OF_PERSONS = 3000

// Кэш провайдеров
@Field
def providerCache = [:]

// запись подразделения в справочнике
@Field
def departmentParam = null

// Кэш подразделений из справочника
@Field
def departmentCache = [:]

// детали подразделения из справочника
@Field
def departmentParamRow = null

// отчетный период
@Field
def reportPeriod = null

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

@Field
def formType = null

@Field
final String DATE_FORMAT_FLATTEN = "yyyyMMdd"

@Field
final String DATE_FORMAT_DOTTED = "dd.MM.yyyy"

@Field
final int REF_BOOK_NDFL_ID = 950

@Field
final int REF_BOOK_NDFL_DETAIL_ID = 951

@Field
final int REB_BOOK_FORM_TYPE_ID = 931

@Field
final int REF_BOOK_DEDUCTION_TYPE_ID = 921

@Field
final int REF_BOOK_DEDUCTION_MARK_ID = 927

@Field
final int NDFL_REFERENCES = 964

@Field
final int REF_BOOK_DOC_STATE = 929

@Field
final int REPORT_PERIOD_TYPE_ID = 8

@Field
int REF_BOOK_OKTMO_ID = 96;

@Field
int REF_BOOK_ASNU_ID = 900

@Field
int REF_BOOK_SIGNATORY_MARK_ID = 35

@Field
final String NDFL_2_S_PRIZNAKOM_1 = "2 НДФЛ (1)"

@Field
final String NDFL_2_S_PRIZNAKOM_2 = "2 НДФЛ (2)"

@Field
final String VERS_FORM = "5.04"

@Field
final String KND = "1151078"

@Field
final String PRIZNAK_KODA_VICHETA_IMUSCHESTVENNIY = "Имущественный"

@Field
final String PRIZNAK_KODA_VICHETA_SOTSIALNIY = "Социальный"

@Field
final String PART_NUMBER = "partNumber"

@Field
final String PART_TOTAL = "partTotal"

@Field
final String NDFL_PERSON_KNF_ID = "ndflPersonKnfId"

@Field
def ndflReferencess = []

@Field
final NDFL_REFERENCES_DECLARATION_DATA_ID = "DECLARATION_DATA_ID"

@Field
final NDFL_REFERENCES_PERSON_ID = "PERSON_ID"

@Field
final NDFL_REFERENCES_NUM = "NUM"

@Field
final NDFL_REFERENCES_SURNAME = "SURNAME"

@Field
final NDFL_REFERENCES_NAME = "NAME"

@Field
final NDFL_REFERENCES_LASTNAME = "LASTNAME"

@Field
final NDFL_REFERENCES_BIRTHDAY = "BIRTHDAY"

@Field
final NDFL_REFERENCES_ERRTEXT = "ERRTEXT"

@Field
final OKTMO_CACHE = [:]

def buildXml(def writer) {
    buildXml(writer, false)
}

def buildXmlForSpecificReport(def writer) {
    buildXml(writer, true)
}

/**
 * Сформировать xml
 * @param writer
 * @param isForSpecificReport
 * @return
 */
def buildXml(def writer, boolean isForSpecificReport) {
    def refPersonIds = []
    ScriptUtils.checkInterrupted();
    ConfigurationParamModel configurationParamModel = declarationService.getAllConfig(userInfo)
    // Получим ИНН из справочника "Общие параметры"
    def sberbankInnParam = configurationParamModel?.get(ConfigurationParam.SBERBANK_INN)?.get(0)?.get(0)
    // Получим код НО пром из справочника "Общие параметры"
    def kodNoProm = configurationParamModel?.get(ConfigurationParam.NO_CODE)?.get(0)?.get(0)



    //Текущая страница представляющая порядковый номер файла
    def currentPageNumber = partNumber

    // инициализация данных о подразделении
    departmentParam = getDepartmentParam(declarationData.departmentId, declarationData.reportPeriodId)
    departmentParamRow = getDepartmentParamDetails(departmentParam?.id, declarationData.reportPeriodId)
    // Имя файла
    def fileName = generateXmlFileId(sberbankInnParam, kodNoProm)
    // Отчетный период
    reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)

    // Макет НФ
    def declarationTemplate = declarationService.getTemplate(declarationData.getDeclarationTemplateId())

    // Вид НФ
    formType = getFormType(declarationTemplate.declarationFormTypeId)

    // Данные для Файл.СвРекв
    def oktmo = getOktmoById(departmentParamRow?.OKTMO?.value)?.CODE?.value
    // Данные для Файл.СвРекв.СвЮЛ
    def kpp = departmentParamRow?.KPP?.value
    def otchetGod = reportPeriod.taxPeriod.year
    def priznakF = definePriznakF()
    def startDate = reportPeriodService.getStartDate(declarationData.reportPeriodId).getTime()
    def endDate = reportPeriodService.getEndDate(declarationData.reportPeriodId).getTime()

    // Данные для Файл.Документ.Подписант
    def prPodp = getProvider(REF_BOOK_SIGNATORY_MARK_ID).getRecordData(departmentParamRow?.SIGNATORY_ID?.value).CODE.value
    def signatoryFirstname = departmentParamRow?.SIGNATORY_FIRSTNAME?.value
    def signatorySurname = departmentParamRow?.SIGNATORY_SURNAME?.value
    def signatoryLastname = departmentParamRow?.SIGNATORY_LASTNAME?.value
    def naimDoc = departmentParamRow?.APPROVE_DOC_NAME?.value
    def naimOrgApprove = departmentParamRow?.APPROVE_ORG_NAME?.value

    // Данные для Файл.Документ.СвНА-(Данные о налоговом агенте)
    def tlf = departmentParamRow?.PHONE?.value
    def naimOrg = departmentParamRow?.NAME?.value

    // 	Данные для Файл.Документ.ПолучДох-(Данные о физическом лице - получателе дохода)
    def ndflPersonsList = getNdflPersons()

    // Порядковый номер физического лица
    def nomSpr = (currentPageNumber - 1) * NUMBER_OF_PERSONS

    // Текущая дата
    def currDate = Calendar.getInstance().getTime()
    def dateDoc = currDate.format(DATE_FORMAT_DOTTED, TimeZone.getTimeZone('Europe/Moscow'))

    // Номер корректировки
    def nomKorr = reportPeriodService.getCorrectionNumber(declarationData.departmentReportPeriodId)
    def kodNo = departmentParamRow?.TAX_ORGAN_CODE?.value
    def builder = new MarkupBuilder(writer)
    builder.setDoubleQuotes(true)
    builder.setOmitNullAttributes(true)
    builder.Файл(ИдФайл: fileName,
            ВерсПрог: applicationVersion,
            ВерсФорм: VERS_FORM) {
        СвРекв(ОКТМО: oktmo,
                ОтчетГод: otchetGod,
                ПризнакФ: priznakF) {
            СвЮЛ(ИННЮЛ: sberbankInnParam,
                    КПП: kpp) {}
        }
        ndflPersonsList.each { np ->
            ScriptUtils.checkInterrupted();
            // Порядковый номер физического лица
            if (nomKorr != 0) {
                nomSpr = getProvider(NDFL_REFERENCES).getRecords(getReportPeriodEndDate(declarationData.reportPeriodId), null, "PERSON_ID = ${np.personId}", null).get(0).NUM.value
            } else {
                nomSpr++
            }
            Документ(КНД: KND,
                    ДатаДок: dateDoc,
                    НомСпр: nomSpr,
                    ОтчетГод: otchetGod,
                    Признак: priznakF,
                    НомКорр: sprintf('%02d', nomKorr),
                    КодНО: kodNo) {
                Подписант(ПрПодп: prPodp) {
                    ФИО(Фамилия: signatorySurname,
                            Имя: signatoryFirstname,
                            Отчество: signatoryLastname) {}
                    if (prPodp == 2) {
                        СвПред(НаимДок: naimDoc,
                                НаимОрг: naimOrgApprove) {}
                    }
                }
                СвНА(ОКТМО: oktmo,
                        Тлф: tlf) {
                    СвНАЮЛ(НаимОрг: naimOrg,
                            ИННЮЛ: sberbankInnParam,
                            КПП: kpp)
                }
                ПолучДох(ИННФЛ: np.innNp,
                        ИННИно: np.innForeign,
                        Статус: np.status,
                        ДатаРожд: np.birthDay?.format(DATE_FORMAT_DOTTED),
                        Гражд: np.citizenship) {
                    ФИО(Фамилия: np.lastName,
                            Имя: np.firstName,
                            Отчество: np.middleName)
                    УдЛичнФЛ(КодУдЛичн: np.idDocType,
                            СерНомДок: np.idDocNumber)
                    АдрМЖРФ(Индекс: np.postIndex,
                            КодРегион: np.regionCode,
                            Район: np.area,
                            Город: np.city,
                            НаселПункт: np.locality,
                            Улица: np.street,
                            Дом: np.house,
                            Корпус: np.building,
                            Кварт: np.flat)
                    if (np.countryCode != null) {
                        АдрИНО(КодСтр: np.countryCode,
                                АдрТекст: np.address)
                    }
                }

                // Данные для Файл.Документ.СведДох-(Сведения о доходах физического лица)
                def ndflPersonIncomesAll = findAllIncomes(np.id, startDate, endDate, priznakF)

                // Сведения о доходах сгруппированные по ставке
                def ndflPersonIncomesGroupedByTaxRate = groupByTaxRate(ndflPersonIncomesAll)

                // Сведения о вычетах с признаком "Остальные"
                def deductionsSelectedForDeductionsInfo = ndflPersonService.findDeductionsWithDeductionsMarkOstalnie(np.id, startDate, endDate)
                def deductionsSelectedForDeductionsSum = ndflPersonService.findDeductionsWithDeductionsMarkNotOstalnie(np.id, startDate, endDate)
                def deductionsSelectedGroupedByDeductionTypeCode = groupByDeductionTypeCode(deductionsSelectedForDeductionsSum)
                // Объединенные строки сведений об уведомлении, подтверждающие право на вычет
                def unionDeductions = unionDeductionsForDeductionType(deductionsSelectedGroupedByDeductionTypeCode)


                def ndflPersonPrepayments = findPrepayments(np.id, startDate, endDate, priznakF)

                ndflPersonIncomesGroupedByTaxRate.keySet().each { taxRateKey ->
                    ScriptUtils.checkInterrupted();
                    СведДох(Ставка: taxRateKey) {

                        def ndflpersonIncomesForTaxRate = ndflPersonIncomesGroupedByTaxRate.get(taxRateKey)
                        // Сведения о доходах сгруппированные по коду дохода
                        def ndflPersonIncomesGroupedByMonthAndIncomeCode = groupIncomesByMonth(ndflpersonIncomesForTaxRate)
                        ДохВыч() {
                            ndflPersonIncomesGroupedByMonthAndIncomeCode.keySet().each { monthKey ->
                                ScriptUtils.checkInterrupted();
                                def ndflPersonIncomesGroupedByIncomeCode = ndflPersonIncomesGroupedByMonthAndIncomeCode.get(monthKey)
                                ndflPersonIncomesGroupedByIncomeCode.keySet().eachWithIndex { incomeKey, int i ->
                                    ScriptUtils.checkInterrupted();
                                    if (isForSpecificReport) {
                                        СвСумДох(Месяц: sprintf('%02d', monthKey + 1),
                                                КодДоход: incomeKey,
                                                СумДоход: ScriptUtils.round(getSumDohod(ndflPersonIncomesGroupedByIncomeCode.get(incomeKey)), 2),
                                                Страница: i < ndflPersonIncomesGroupedByIncomeCode.size() / 2 ? 1 : 2
                                        ) {
                                            def deductionsFilteredForCurrIncome = filterDeductionsByIncomeCode(ndflPersonIncomesGroupedByIncomeCode.get(incomeKey).get(0), deductionsSelectedForDeductionsInfo)
                                            deductionsFilteredForCurrIncome.each {
                                                if (it.periodCurrSumm != null && it.periodCurrSumm != 0) {
                                                    СвСумВыч(КодВычет: it.typeCode,
                                                            СумВычет: ScriptUtils.round(it.periodCurrSumm, 2)) {}
                                                }
                                            }
                                        }
                                    } else {
                                        СвСумДох(Месяц: sprintf('%02d', monthKey + 1),
                                                КодДоход: incomeKey,
                                                СумДоход: ScriptUtils.round(getSumDohod(ndflPersonIncomesGroupedByIncomeCode.get(incomeKey)), 2)
                                        ) {
                                            def deductionsFilteredForCurrIncome = filterDeductionsByIncomeCode(ndflPersonIncomesGroupedByIncomeCode.get(incomeKey).get(0), deductionsSelectedForDeductionsInfo)
                                            deductionsFilteredForCurrIncome.each {
                                                if (it.periodCurrSumm != null && it.periodCurrSumm != 0) {
                                                    СвСумВыч(КодВычет: it.typeCode,
                                                            СумВычет: ScriptUtils.round(it.periodCurrSumm, 2)) {}
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (!(deductionsSelectedGroupedByDeductionTypeCode.isEmpty())) {
                            if (taxRateKey == 13) {
                                НалВычССИ() {
                                    if (isForSpecificReport) {
                                        int countInLine = 4
                                        int linesCount
                                        if (deductionsSelectedGroupedByDeductionTypeCode.size() % countInLine == 0) {
                                            linesCount = deductionsSelectedGroupedByDeductionTypeCode.size() / countInLine
                                        } else {
                                            linesCount = deductionsSelectedGroupedByDeductionTypeCode.size() / countInLine + 1
                                        }
                                        for (int line = 0; line < linesCount; line++) {
                                            Строка() {
                                                deductionsSelectedGroupedByDeductionTypeCode.keySet().eachWithIndex { deductionTypeKey, index ->
                                                    ScriptUtils.checkInterrupted();
                                                    def lowestIndex = countInLine * line
                                                    if (index >= lowestIndex && index < lowestIndex + countInLine) {
                                                        def deductionCurrPeriodSum = ScriptUtils.round(getDeductionCurrPeriodSum(deductionsSelectedGroupedByDeductionTypeCode.get(deductionTypeKey)), 2)
                                                        if (deductionCurrPeriodSum != 0) {
                                                            ПредВычССИ(КодВычет: deductionTypeKey,
                                                                    СумВычет: deductionCurrPeriodSum) {
                                                            }
                                                        }

                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        deductionsSelectedGroupedByDeductionTypeCode.keySet().each { deductionTypeKey ->
                                            ScriptUtils.checkInterrupted();
                                            def deductionCurrPeriodSum = ScriptUtils.round(getDeductionCurrPeriodSum(deductionsSelectedGroupedByDeductionTypeCode.get(deductionTypeKey)), 2)
                                            if (deductionCurrPeriodSum != 0) {
                                                ПредВычССИ(КодВычет: deductionTypeKey,
                                                        СумВычет: deductionCurrPeriodSum) {
                                                }
                                            }
                                        }
                                    }
                                    unionDeductions.keySet().findAll { deductionTypeKey ->
                                        ScriptUtils.checkInterrupted();
                                        getDeductionMark(getDeductionType(deductionTypeKey)).equalsIgnoreCase(PRIZNAK_KODA_VICHETA_SOTSIALNIY)
                                    }.each { selected ->
                                        ScriptUtils.checkInterrupted();
                                        unionDeductions.get(selected).each {
                                            УведСоцВыч(НомерУвед: it.notifNum,
                                                    ДатаУвед: it.notifDate?.format(DATE_FORMAT_DOTTED),
                                                    ИФНСУвед: it.notifSource)
                                        }
                                    }
                                    unionDeductions.keySet().findAll { deductionTypeKey ->
                                        ScriptUtils.checkInterrupted();
                                        getDeductionMark(getDeductionType(deductionTypeKey)).equalsIgnoreCase(PRIZNAK_KODA_VICHETA_IMUSCHESTVENNIY)
                                    }.each { selected ->
                                        ScriptUtils.checkInterrupted();
                                        unionDeductions.get(selected).each {
                                            УведИмущВыч(НомерУвед: it.notifNum,
                                                    ДатаУвед: it.notifDate?.format(DATE_FORMAT_DOTTED),
                                                    ИФНСУвед: it.notifSource)
                                        }
                                    }
                                }
                            }
                        }
                        // Доходы отобранные по датам для поля tax_date(Дата НДФЛ)
                        def incomesByTaxDate =  ndflPersonService.findIncomesByPeriodAndNdflPersonIdAndTaxDate(np.id, startDate, endDate)

                        СумИтНалПер(СумДохОбщ: ScriptUtils.round(getSumDohod(ndflPersonIncomesAll), 2),
                                НалБаза: ScriptUtils.round(getNalBaza(ndflPersonIncomesAll), 2),
                                НалИсчисл: getNalIschisl(ndflPersonIncomesAll),
                                АвансПлатФикс: getAvansPlatFix(ndflPersonPrepayments),
                                НалУдерж: getNalUderzh(priznakF, incomesByTaxDate),
                                НалПеречисл: getNalPerechisl(priznakF, incomesByTaxDate),
                                НалУдержЛиш: getNalUderzhLish(priznakF, incomesByTaxDate),
                                НалНеУдерж: getNalNeUderzh(priznakF, incomesByTaxDate)) {

                            if (np.status == "6") {
                                ndflPersonPrepayments.each { prepayment ->
                                    УведФиксПлат(НомерУвед: prepayment.notifNum,
                                            ДатаУвед: prepayment.notifDate?.format(DATE_FORMAT_DOTTED),
                                            ИФНСУвед: prepayment.notifSource) {
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (!refPersonIds.contains(np.personId)) {
                refPersonIds << np.personId
                ndflReferencess << createRefBookAttributesForNdflReference(np.personId, nomSpr, np.lastName, np.firstName, np.middleName, np.birthDay)
            }

        }
    }
    ScriptUtils.checkInterrupted();
    saveNdflRefences()
    ScriptUtils.checkInterrupted();
    saveFileInfo(currDate, fileName)

    //println(writer)
}
// Сохранение информации о файле в комментариях
def saveFileInfo(currDate, fileName) {
    def fileUuid = blobDataServiceDaoImpl.create(xmlFile, fileName + ".xml", new Date())
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

def saveNdflRefences() {
    logger.setTaUserInfo(userInfo)
    getProvider(NDFL_REFERENCES).createRecordVersion(logger, new Date(), null, ndflReferencess)
}

/**
 * Заполнить значение реестра справок
 * @param personId
 * @param nomSpr
 * @param lastName
 * @param firstName
 * @param middleName
 * @param birthDay
 * @return
 */
def createRefBookAttributesForNdflReference(
        def personId, def nomSpr, def lastName, def firstName, def middleName, def birthDay) {
    Map<String, RefBookValue> row = new HashMap<String, RefBookValue>();
    row.put(NDFL_REFERENCES_DECLARATION_DATA_ID, new RefBookValue(RefBookAttributeType.NUMBER, declarationData.id))
    row.put(NDFL_REFERENCES_PERSON_ID, new RefBookValue(RefBookAttributeType.REFERENCE, personId))
    row.put(NDFL_REFERENCES_NUM, new RefBookValue(RefBookAttributeType.NUMBER, nomSpr))
    row.put(NDFL_REFERENCES_SURNAME, new RefBookValue(RefBookAttributeType.STRING, lastName))
    row.put(NDFL_REFERENCES_NAME, new RefBookValue(RefBookAttributeType.STRING, firstName))
    row.put(NDFL_REFERENCES_LASTNAME, new RefBookValue(RefBookAttributeType.STRING, middleName))
    row.put(NDFL_REFERENCES_BIRTHDAY, new RefBookValue(RefBookAttributeType.DATE, birthDay))
    row.put(NDFL_REFERENCES_ERRTEXT, new RefBookValue(RefBookAttributeType.STRING, null))
    RefBookRecord record = new RefBookRecord()
    record.setValues(row)
    return record
}

/**
 * Генерация имени файла
 * @return
 */
def generateXmlFileId(inn, kodNoProm) {
    def departmentParamRow = departmentParam ? getDepartmentParamDetails(departmentParam?.record_id?.value, declarationData.reportPeriodId) : null
    def r_t = "NO_NDFL2"
    def a = kodNoProm
    def k = departmentParamRow?.TAX_ORGAN_CODE?.value
    def o = inn + declarationData.kpp
    def date = Calendar.getInstance().getTime()?.format(DATE_FORMAT_FLATTEN)
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

// Получение данных
def getNdflPersons() {
    def toReturn = []
    def queryParameterList = ndflPersonKnfId.collate(1000)
    queryParameterList.each {
        toReturn.addAll(ndflPersonService.findByIdList(it))
    }
    return toReturn
    //ndflPersonService.findNdflPersonByParameters(declarationData.id, null, pageNumber * 3000 - 2999, NUMBER_OF_PERSONS)
}

/**
 * Получить доходы для ФЛ за период
 * @param ndflPersonId
 * @param priznakF
 * @param startDate
 * @param endDate
 * @return
 */
def filterIncomes(selectedIncomeRows, def priznakF) {
    def toReturn = new ArrayList<NdflPersonIncome>(selectedIncomeRows)
    if (priznakF == "1") {
        toReturn.removeAll { it.incomeAccruedSumm == null || it.incomeAccruedSumm == 0 }
    } else if (priznakF == "2") {
        toReturn.removeAll { it.notHoldingTax == null || it.notHoldingTax < 0 }
    }
    return toReturn
}

def findAllIncomes(def ndflPersonId, def startDate, def endDate, priznakF) {
    def toReturn
    // Временно строки отбираются без учета условия КНФ.Раздел 2.Графа 10 ≠ 0
    if (priznakF == "1") {
        //toReturn = ndflPersonService.findIncomesByPeriodAndNdflPersonId(ndflPersonId, startDate, endDate, Boolean.TRUE)
        toReturn = ndflPersonService.findIncomesByPeriodAndNdflPersonIdTemp(ndflPersonId, startDate, endDate, Boolean.TRUE)
    } else if (priznakF == "2") {
        toReturn = ndflPersonService.findIncomesByPeriodAndNdflPersonId(ndflPersonId, startDate, endDate, Boolean.FALSE)
    }
    return toReturn
}

/**
 * отфильтровать вычеты код дохода которых не совпадает
 * @param ndflPersonIncomes
 * @param ndflPersonDeductions
 * @return
 */
def filterDeductionsByIncomeCode(ndflPersonIncome, def ndflPersonDeductions) {
    def toReturn = []
    Calendar taxDateCalIncome = new GregorianCalendar();
    taxDateCalIncome.setTime(ndflPersonIncome.incomeAccruedDate)

    for (d in ndflPersonDeductions) {
        Calendar taxDateCalDeduction = new GregorianCalendar();
        taxDateCalDeduction.setTime(d.incomeAccrued)
        if (d.incomeCode == ndflPersonIncome.incomeCode && d.operationId == ndflPersonIncome.operationId &&
                taxDateCalIncome.get(Calendar.MONTH) == taxDateCalDeduction.get(Calendar.MONTH)) toReturn << d

    }
    return toReturn
}

/**
 * Получить авансы для ФЛ за период для доходов с одинаковым номером операции
 * @param ndflPersonId
* @param startDate
* @param endDate
*
* @return
*/
def findPrepayments(def ndflPersonId, def startDate, def endDate, priznakF) {
    def toReturn = []
    if (priznakF == "1") {
        return ndflPersonService.findPrepaymentsByPeriodAndNdflPersonId(ndflPersonId, startDate, endDate, true)
    } else {
        return ndflPersonService.findPrepaymentsByPeriodAndNdflPersonId(ndflPersonId, startDate, endDate, false)
    }
}

// группирока по налоговой ставке
def groupByTaxRate(def incomes) {
    def toReturn = [:]
    def rates = []
    incomes.each {
        if (!rates.contains(it.taxRate)) {
            rates << it.taxRate
        }
    }
    rates.each { rate -> toReturn[rate] = incomes.findAll { it.taxRate.equals(rate) } }
    return toReturn
}
/**
 * Метод возвращает мапу, где ключ Integer сооветстувующий значениям месяцев из класа java.util.Calendar,
 * а значение мапа, где ключ код дохода, а значение список соответствующих объектов NdflPersonIncome
 * @param incomes
 * @return
 */
def groupIncomesByMonth(incomes) {
    def groupByMonth = [:]
    def monthes = []
    def toReturn = [:]
    incomes.each {
        Calendar accruedDateCal = new GregorianCalendar();
        accruedDateCal.setTime(it.incomeAccruedDate)
        def month = accruedDateCal.get(Calendar.MONTH)
        if (!monthes.contains(month)) {
            monthes.add(month)
        }
    }
    monthes = monthes.sort()
    monthes.each { month ->
        groupByMonth[month] = incomes.findAll {
            Calendar incomeCal = new GregorianCalendar()
            incomeCal.setTime(it.incomeAccruedDate)
            month == incomeCal.get(Calendar.MONTH)
        }
    }
    groupByMonth.keySet().each { key ->
        def gm = groupByMonth.get(key)
        def groupByIncomeCode = [:]
        def incomeCodes = []
        gm.each {
            if (!incomeCodes.contains(it.incomeCode)) {
                incomeCodes << it.incomeCode
            }
        }
        incomeCodes.each { incomeCode ->
            groupByIncomeCode[incomeCode] = gm.findAll {
                it.incomeCode.equals(incomeCode)
            }
        }
        toReturn[key] = groupByIncomeCode
    }
    return toReturn
}

// Группировка по коду вычета
def groupByDeductionTypeCode(def deductions) {
    def toReturn = [:]
    def typeCodes = []
    deductions.each {
        if (!typeCodes.contains(it.typeCode)) {
            typeCodes << it.typeCode
        }
    }
    typeCodes.each { typeCode -> toReturn[typeCode] = deductions.findAll { it.typeCode == typeCode } }
    return toReturn
}

def unionDeductionsForDeductionType(deductionGroups) {

    def toReturn = [:]
    deductionGroups.keySet().each { key ->
        def tempDeductions = []
        deductionGroups.get(key).each { deduction1 ->
            if (!notifPresent(deduction1, tempDeductions)) {
                tempDeductions << deduction1
            }
        }
        toReturn[key] = tempDeductions
    }
    return toReturn
}

def notifPresent(deduction, deductions) {
    def toReturn = false
    deductions.each {
        if (it.typeCode == deduction.typeCode && it.notifType == deduction.notifType
                && it.notifDate == deduction.notifDate && it.notifNum == deduction.notifNum
                && it.notifSource == deduction.notifSource && it.notifSumm == deduction.notifSumm) {

            toReturn = true
        }
    }
    return toReturn
}

// Вычислить сумму для СумДоход
def getSumDohod(def rows) {
    def toReturn = new BigDecimal(0)
    rows.each {
        if (it.incomeAccruedSumm != null) {
            toReturn = toReturn.add(it.incomeAccruedSumm)
        }
    }
    return toReturn
}

// Вычислить сумму для НалБаза
def getNalBaza(def incomes) {
    def toReturn = new BigDecimal(0)
    incomes.each {
        if (it.taxBase != null) {
            toReturn = toReturn.add(it.taxBase)
        }
    }
    return toReturn
}

//Вычислить сумму для НалИсчисл
def getNalIschisl(def incomes) {
    def toReturn = new BigDecimal(0)
    incomes.each {
        if (it.calculatedTax != null) {
            toReturn = toReturn.add(it.calculatedTax)
        }
    }
    return toReturn
}

/**
 * Вычислить сумму для АвансПлатФикс
 * @param prepayments
 * @return
 */
def getAvansPlatFix(def prepayments) {
    def toReturn = new BigDecimal(0)
    prepayments.each {
        if (it.summ != null) {
            toReturn = toReturn.add(it.summ)
        }
    }
    return toReturn
}

/**
 * /Вычислить сумму вычета в текущем периоде
 * @param deductions
 * @return
 */
def getDeductionCurrPeriodSum(def deductions) {
    def toReturn = new BigDecimal(0)
    deductions.each {
        if (it.periodCurrSumm != null) {
            toReturn = toReturn.add(it.periodCurrSumm)
        }
    }
    return toReturn
}

//Вычислить сумму для НалУдерж
def getNalUderzh(def priznakF, def incomes) {
    def toReturn = 0L
    if (priznakF == "1") {
        incomes.each {
            if (it.withholdingTax != null) {
                toReturn += it.withholdingTax
            }
        }
        return toReturn
    } else if (priznakF == "2") {
        return 0
    }
}

//Вычислить сумму для НалПеречисл
def getNalPerechisl(def priznakF, def incomes) {
    def toReturn = 0L
    if (priznakF == "1") {
        incomes.each {
            if (it.taxSumm != null) {
                toReturn += it.taxSumm
            }
        }
        return toReturn
    } else if (priznakF == "2") {
        return 0
    }
}

//Вычислить сумму для НалУдержЛиш
def getNalUderzhLish(def priznakF, def incomes) {
    def toReturn = 0L
    if (priznakF == "1") {
        incomes.each {
            if (it.overholdingTax != null) {
                toReturn += it.overholdingTax
            }
        }
        return toReturn
    } else if (priznakF == "2") {
        return 0
    }
}

//Вычислить сумму для НалНеУдерж
def getNalNeUderzh(priznakF, incomes) {
    def toReturn = 0L
    if (priznakF == "1") {
        incomes.each {
            if (it.notHoldingTax != null) {
                toReturn += it.notHoldingTax
            }
        }
    } else if (priznakF == "2") {
        incomes.each {
            if (it.notHoldingTax != null && it.calculatedTax > 0) {
                toReturn += it.notHoldingTax
            }
        }
    }

    return toReturn
}

def getOktmoById(id) {
    def oktmo = OKTMO_CACHE.get(id)
    if (oktmo == null && id != null) {
        def rpe = getReportPeriodEndDate(declarationData.reportPeriodId)
        def provider = getProvider(REF_BOOK_OKTMO_ID)
        def oktmoList = provider.getRecords(rpe, null, "ID = ${id}", null)
        if (oktmoList.size() != 0) {
            oktmo = oktmoList.get(0)
            OKTMO_CACHE[id] = oktmo
        }

    }
    return oktmo
}

def getOktmoByIdList(idList) {
    def provider = getProvider(REF_BOOK_OKTMO_ID)
    return provider.getRecordData(idList)
}

def getOktmoByCode(code) {
    def oktmo = null
    def rpe = getReportPeriodEndDate(declarationData.reportPeriodId)
    def oktmoList = getProvider(REF_BOOK_OKTMO_ID).getRecords(rpe, null, "CODE = '${code}'", null)
    if (oktmoList.size() != 0) {
        oktmo = oktmoList.get(0)
        OKTMO_CACHE[oktmo.id.value] = oktmo
    }
    return oktmo
}

// Получить параметры подразделения (из справочника 950)
def getDepartmentParam(def departmentId, def reportPeriodId) {
    if (!departmentCache.containsKey(departmentId)) {
        def rpe = getReportPeriodEndDate(reportPeriodId)
        def provider = getProvider(REF_BOOK_NDFL_ID)
        def departmentParamList = provider.getRecords(rpe, null, "DEPARTMENT_ID = $departmentId", null)
        if (departmentParamList == null || departmentParamList.size() == 0 || departmentParamList.get(0) == null) {
            throw new Exception("Ошибка при получении настроек обособленного подразделения. Настройки подразделения заполнены не полностью")
        }
        departmentCache.put(departmentId, departmentParamList?.get(0))
    }
    return departmentCache.get(departmentId)
}

/**
 * Получить детали подразделения из справочника
 * @param departmentParamId
 * @param reportPeriodId
 * @return
 */
def getDepartmentParamDetails(def departmentParamId, def reportPeriodId) {
    if (departmentParamRow == null) {
        def departmentParamTableList = getProvider(REF_BOOK_NDFL_DETAIL_ID).getRecords(getReportPeriodEndDate(reportPeriodId), null, "REF_BOOK_NDFL_ID = $departmentParamId", null)
        if (departmentParamTableList == null || departmentParamTableList.size() == 0 || departmentParamTableList.get(0) == null) {
            throw new Exception("Ошибка при получении настроек обособленного подразделения. Настройки подразделения заполнены не полностью")
        }
        departmentParamRow = departmentParamTableList.get(0)
    }
    return departmentParamRow
}

/**
 * Получение провайдера с использованием кеширования
 * @param providerId
 * @return
 */

def getProvider(def long providerId) {
    if (!providerCache.containsKey(providerId)) {
        def provider = refBookFactory.getDataProvider(providerId)
        providerCache.put(providerId, provider)
    }
    return providerCache.get(providerId)
}

/**
 * Получение даты окончания периода
 * @param reportPeriodId
 * @return
 */
def getReportPeriodEndDate(def reportPeriodId) {
    if (reportPeriodEndDate == null) {
        reportPeriodEndDate = reportPeriodService.getEndDate(reportPeriodId)?.time
    }
    return reportPeriodEndDate
}

// Получить вид формы
def getFormType(def id) {
    if (formType == null) {
        formType = getProvider(REB_BOOK_FORM_TYPE_ID).getRecordData(id)
    }
    return formType;
}

/**
 * Определить признакФ
 */

def definePriznakF() {
    def code = formType?.CODE?.value
    switch (code) {
        case (NDFL_2_S_PRIZNAKOM_1): return "1"
        case (NDFL_2_S_PRIZNAKOM_2): return "2"

        default: return "0"
    }
}

/**
 * Получить вид вычета по коду вычета
 * @param code
 * @return
 */
def getDeductionType(def code) {
    def filter = "CODE = '$code'"
    def deductionTypeList = getProvider(REF_BOOK_DEDUCTION_TYPE_ID).getRecords(getReportPeriodEndDate(declarationData.reportPeriodId), null, filter, null)
    if (deductionTypeList == null || deductionTypeList.size() == 0 || deductionTypeList.get(0) == null) {
        throw new Exception("Ошибка при получении кодов вычета. Коды вычета заполнены не полностью")
    }
    return deductionTypeList.get(0).DEDUCTION_MARK?.value
}

/**
 * Получить признак вычета
 * @param id
 * @return
 */
def getDeductionMark(def id) {
    getProvider(REF_BOOK_DEDUCTION_MARK_ID).getRecordData(id).NAME?.value
}

/************************************* СОЗДАНИЕ ФОРМЫ *****************************************************************/

@Field
final int RNU_NDFL_DECLARATION_TYPE = 101

@Field
def departmentParamTableList = null;

@Field
def departmentParamTableListCache = [:];

def createForm() {
    def departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
    def pairKppOktmoList = []
    def currDeclarationTemplate = declarationService.getTemplate(declarationData.declarationTemplateId)
    def declarationTypeId = currDeclarationTemplate.type.id
    def ndflReferencesWithError = []
    def departmentParam
    // Поиск КПП и ОКТМО для корр периода
    if (departmentReportPeriod.correctionDate != null) {
        def prevDepartmentPeriodReport = getPrevDepartmentReportPeriod(departmentReportPeriod)
        def declarations = declarationService.find(declarationTypeId, prevDepartmentPeriodReport?.id)
        def declarationsForRemove = []
        declarations.each { declaration ->
            ScriptUtils.checkInterrupted();
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
        declarations.each {
            ScriptUtils.checkInterrupted();
            ndflReferencesWithError.addAll(getNdflReferencesWithError(it.id, it.reportPeriodId))
        }
        // Поиск КПП и ОКТМО для некорр периода
    } else {
        departmentParam = getDepartmentParam(departmentReportPeriod.departmentId, departmentReportPeriod.reportPeriod.id)
        def departmentParamTableList = getDepartmentParamDetailsList(departmentParam?.id, departmentReportPeriod.reportPeriod.id)
        def referencesOktmoList = departmentParamTableList.OKTMO?.value
        referencesOktmoList.removeAll([null])
        def oktmoForDepartment = getOktmoByIdList(referencesOktmoList)

        departmentParamTableList.each { dep ->
            ScriptUtils.checkInterrupted();
            if (dep.OKTMO?.value != null) {
                def oktmo = oktmoForDepartment.get(dep.OKTMO?.value)
                if (oktmo != null) {
                    pairKppOktmoList << new PairKppOktmo(dep.KPP?.value, oktmo.CODE.value, dep?.TAX_ORGAN_CODE?.value)
                }
            }
        }
    }
    // Все подходящие КНФ
    def allDeclarationData = findAllTerBankDeclarationData(departmentReportPeriod)
    /*declarationService.findDeclarationDataIdByTypeStatusReportPeriod(declarationData.reportPeriodId, departmentParam?.id.value,
    RNU_NDFL_DECLARATION_TYPE, DepartmentType.TERR_BANK.getCode(),
    Boolean.TRUE, State.ACCEPTED.getId())*/
    // Мапа где значение физлица для каждой пары КПП и ОКТМО
    def ndflPersonsIdGroupedByKppOktmo = [:]
    pairKppOktmoList.each { pair ->
        ScriptUtils.checkInterrupted();
        // Поиск физлиц по КПП и ОКТМО операций относящихся к ФЛ
        def ndflPersons = ndflPersonService.findNdflPersonByPairKppOktmo(allDeclarationData.id, pair.kpp.toString(), pair.oktmo.toString())
        if (ndflPersons != null && ndflPersons.size() != 0) {
            if (departmentReportPeriod.correctionDate != null) {
                def ndflPersonsPicked = []
                ndflReferencesWithError.each { reference ->
                    ndflPersons.each { person ->
                        if (reference.PERSON_ID?.value == person.personId) {
                            ndflPersonsPicked << person
                        }
                    }
                }
                ndflPersons = ndflPersonsPicked
            }
            addNdflPersons(ndflPersonsIdGroupedByKppOktmo, pair, ndflPersons)
        }
    }

    //initNdflPersons(ndflPersonsIdGroupedByKppOktmo)

    // Удаление ранее созданных отчетных форм
    declarationService.find(declarationTypeId, declarationData.departmentReportPeriodId).each {
        ScriptUtils.checkInterrupted();
        declarationService.delete(it.id, userInfo)
    }
    // Создание ОНФ для каждой пары КПП и ОКТМО
    ndflPersonsIdGroupedByKppOktmo.each { npGroup ->
        ScriptUtils.checkInterrupted();
        def oktmo = npGroup.key.oktmo
        def kpp = npGroup.key.kpp
        def taxOrganCode = npGroup.key.taxOrganCode

        def npGroupValue = npGroup.value.collate(NUMBER_OF_PERSONS)
        def partTotal = npGroupValue.size()
        npGroupValue.eachWithIndex { part, index ->
            ScriptUtils.checkInterrupted();
            def npGropSourcesIdList = part.id
            Map<String, Object> params
            Long ddId
            def indexFrom1 = ++index
            def note = "Часть ${indexFrom1} из ${partTotal}"
            params = new HashMap<String, Object>()
            ddId = declarationService.create(logger, declarationData.declarationTemplateId, userInfo,
                    departmentReportPeriodService.get(declarationData.departmentReportPeriodId), taxOrganCode, kpp.toString(), oktmo.toString(), null, null, note.toString())
            //appendNdflPersonsToDeclarationData(ddId, part)
            params.put(PART_NUMBER, indexFrom1)
            params.put(PART_TOTAL, partTotal)
            params.put(NDFL_PERSON_KNF_ID, npGropSourcesIdList)
            formMap.put(ddId, params)
        }
    }
}

/**
 * Добавить ФЛ к паре КПП и ОКТМО. Если какая-то пара КПП+ОКТМО указана в справочнике больше 1 раза,
 * прозводится анализ значения "Код НО конечный", указанные для этих повтояющихся пар
 * @param ndflPersonsGroupedByKppOktmo мапа где ключ пара КПП и ОКТМО
 * @param pairKppOktmoBeingComparing пара КПП и ОКТМО, наличие которой проверяется в ndflPersonsGroupedByKppOktmo
 * @param ndflPersonList список ФЛ относящихся к pairKppOktmoBeingComparing
 * @return
 */
def addNdflPersons(ndflPersonsGroupedByKppOktmo, pairKppOktmoBeingComparing, ndflPersonList) {
    // Физлица для каждой пары КПП и октмо
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

/**
 * получить id всех ТБ для данного отчетного периода
 * @param departmentReportPeriod
 * @return
 */
def findAllTerBankDeclarationData(def departmentReportPeriod) {
    def allDepartmentReportPeriodIds = departmentReportPeriodService.getIdsByDepartmentTypeAndReportPeriod(DepartmentType.TERR_BANK.getCode(), departmentReportPeriod.reportPeriod.id)
    def allDeclarationData = []
    allDepartmentReportPeriodIds.each {
        ScriptUtils.checkInterrupted();
        allDeclarationData.addAll(declarationService.find(RNU_NDFL_DECLARATION_TYPE, it))
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

/************************************* ВЫГРУЗКА ***********************************************************************/

/**
 * Выгрузка архива с созданными xml
 * @return
 */
def createReports() {
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
            ScriptUtils.checkInterrupted();
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

def getReportPeriod() {
    if (sourceReportPeriod == null) {
        sourceReportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
    }
    return sourceReportPeriod
}

/** Получить результат для события FormDataEvent.GET_SOURCES. */
void getSources() {
    if (!(needSources)) {
        // формы-приемники, декларации-истчоники, декларации-приемники не переопределять
        return
    }
    def reportPeriod = getReportPeriod()
    def sourceTypeId = 101
    def departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
    def allDepartmentReportPeriodIds = departmentReportPeriodService.getIdsByDepartmentTypeAndReportPeriod(DepartmentType.TERR_BANK.getCode(), departmentReportPeriod.reportPeriod.id)
    def tmpDeclarationDataList = []
    allDepartmentReportPeriodIds.each {
        ScriptUtils.checkInterrupted();
        def tmpDepartmentReportPeriod = departmentReportPeriodService.get(it)
        def tmpDeclaration = declarationService.findDeclarationDataByKppOktmoOfNdflPersonIncomes(sourceTypeId, it, tmpDepartmentReportPeriod.departmentId, tmpDepartmentReportPeriod.reportPeriod.id, declarationData.kpp, declarationData.oktmo)
        if (tmpDeclaration != null) {
            tmpDeclarationDataList << tmpDeclaration
        }
    }
    def declarationsForRemove = []
    tmpDeclarationDataList.each { declaration ->
        if (declaration.state != State.ACCEPTED) {
            declarationsForRemove << declaration
        }
    }
    tmpDeclarationDataList.removeAll(declarationsForRemove)
    tmpDeclarationDataList.each { tmpDeclarationData ->
        ScriptUtils.checkInterrupted();
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

    DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(tmpDeclarationData?.departmentReportPeriodId)
    DeclarationTemplate declarationTemplate = declarationService.getTemplate(sourceTypeId)

    // boolean light - заполняются только текстовые данные для GUI и сообщений
    if (light) {
        /**************  Параметры для легкой версии ***************/
        /** Идентификатор подразделения */
        relation.departmentId = department.id
        /** полное название подразделения */
        relation.fullDepartmentName = departmentService.getParentsHierarchy(department.id)
        /** Дата корректировки */
        relation.correctionDate = departmentReportPeriod?.correctionDate
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

/************************************* ОБЩИЕ МЕТОДЫ** *****************************************************************/

/**
 * Получить список детали подразделения из справочника
 * @param departmentParamId
 * @param reportPeriodId
 * @return
 */
def getDepartmentParamDetailsList(def departmentParamId, def reportPeriodId) {
    if (!departmentParamTableListCache.containsKey(departmentParamId)) {
        def filter = "REF_BOOK_NDFL_ID = $departmentParamId"
        def rpe = getReportPeriodEndDate(reportPeriodId)
        def provider = getProvider(REF_BOOK_NDFL_DETAIL_ID)
        def departmentParamTableList = provider.getRecords(rpe, null, filter, null)
        if (departmentParamTableList == null || departmentParamTableList.size() == 0 || departmentParamTableList.get(0) == null) {
            throw new Exception("Ошибка при получении настроек обособленного подразделения. Настройки подразделения заполнены не полностью")
        }
        departmentParamTableListCache.put(departmentParamId, departmentParamTableList)
    }
    return departmentParamTableListCache.get(departmentParamId)
}

// Получить список из реестра справок с ошибкой ФНС
def getNdflReferencesWithError(declarationDataId, reportPeriodId) {
    def filter = "DECLARATION_DATA_ID = ${declarationDataId}"
    def allNdflReferences = getProvider(NDFL_REFERENCES).getRecords(getReportPeriodEndDate(reportPeriodId), null, filter, null)
    def ndflReferencesForRemove = []
    allNdflReferences.each {
        if (it.ERRTEXT?.value == null) {
            ndflReferencesForRemove << it
        }
    }
    allNdflReferences.removeAll(ndflReferencesForRemove)
    return allNdflReferences
}

/**
 * Класс инкапсулирующий информацию о КПП, ОКТМО и кон налогового органа подразделения
 */
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

    @Override
    String toString() {
        return kpp + " " + oktmo + " " + taxOrganCode
    }
}

/************************************* СПЕЦОТЧЕТ **********************************************************************/

@Field final String ALIAS_PRIMARY_RNU_W_ERRORS = "primary_rnu_w_errors"

@Field final String TRANSPORT_FILE_TEMPLATE = "ТФ"

// Мапа где ключ идентификатор NdflPerson, значение NdflPerson соответствующий идентификатору
@Field Map<Long, NdflPerson> ndflpersonFromRNUPrimary = [:]

/**
 * Создать спецотчет
 * @return
 */
def createSpecificReport() {
    def alias = scriptSpecificReportHolder.getDeclarationSubreport().getAlias()
    if (alias == ALIAS_PRIMARY_RNU_W_ERRORS) {
        createPrimaryRnuWithErrors()
        return
    }

    def params = scriptSpecificReportHolder.subreportParamValues ?: new HashMap<String, Object>()
    def xmlStr = declarationService.getXmlData(declarationData.id)
    xmlStr = xmlStr.replace("windows-1251", "utf-8") // сведения о кодировке должны соответствовать содержимому
    def jasperPrint = declarationService.createJasperReport(scriptSpecificReportHolder.getFileInputStream(), params, {
        it.write(xmlStr, 0, xmlStr.length())
        it.flush()
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
    List<NdflPersonIncome> ndflPersonIncomeFromRNUConsolidatedList = ndflPersonService.findNdflPersonIncomeConsolidatedRNU2Ndfl(declarationData.id, declarationData.kpp, declarationData.oktmo)
    // Сведения о вычетах имеющие такой же operationId как и сведения о доходах
    List<NdflPersonDeduction> ndflPersonDeductionFromRNUConsolidatedList = []
    // Сведения об авансах имеющие такой же operationId как и сведения о доходах
    List<NdflPersonPrepayment> ndflPersonPrepaymentFromRNUConsolidatedList = []

    ndflPersonIncomeFromRNUConsolidatedList.each {
        ScriptUtils.checkInterrupted();
        ndflPersonDeductionFromRNUConsolidatedList.addAll(ndflPersonService.findDeductionsByNdflPersonAndOperation(it.ndflPersonId, it.operationId))
        ndflPersonPrepaymentFromRNUConsolidatedList.addAll(ndflPersonService.findPrepaymentsByNdflPersonAndOperation(it.ndflPersonId, it.operationId))
    }

    ndflPersonIncomeFromRNUConsolidatedList.each {
        ScriptUtils.checkInterrupted();
        NdflPersonIncome ndflPersonIncomePrimary = ndflPersonService.getIncome(it.sourceId)
        NdflPerson ndflPersonPrimary = initNdflPersonPrimary(ndflPersonIncomePrimary.ndflPersonId)
        ndflPersonPrimary.incomes.add(ndflPersonIncomePrimary)
    }

    ndflPersonDeductionFromRNUConsolidatedList.each {
        ScriptUtils.checkInterrupted();
        NdflPersonDeduction ndflPersonDeductionPrimary = ndflPersonService.getDeduction(it.sourceId)
        NdflPerson ndflPersonPrimary = initNdflPersonPrimary(ndflPersonDeductionPrimary.ndflPersonId)
        ndflPersonPrimary.deductions.add(ndflPersonDeductionPrimary)
    }

    ndflPersonPrepaymentFromRNUConsolidatedList.each {
        ScriptUtils.checkInterrupted();
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
            ScriptUtils.checkInterrupted();
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