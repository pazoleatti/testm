package form_template.ndfl.report_6ndfl.v2016

import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils
import groovy.transform.Field
import groovy.xml.MarkupBuilder

switch (formDataEvent) {
    case FormDataEvent.CHECK:
        checkXml()
        break
    case FormDataEvent.CREATE_FORMS: // создание экземпляра
        println "!CREATE_FORMS!"
        createForm()
        break
    case FormDataEvent.CALCULATE:
        buildXml()
        break
    default:
        break
}

// Коды, определяющие налоговый (отчётный) период
@Field final long REF_PERIOD_CODE_ID = RefBook.Id.PERIOD_CODE.id

// Коды представления налоговой декларации по месту нахождения (учёта)
@Field final long REF_TAX_PLACE_TYPE_CODE_ID = RefBook.Id.TAX_PLACE_TYPE_CODE.id

// Признак лица, подписавшего документ
@Field final long REF_MARK_SIGNATORY_CODE_ID = RefBook.Id.MARK_SIGNATORY_CODE.id

// Настройки подразделений по НДФЛ
@Field final long REF_NDFL_ID = RefBook.Id.NDFL.id

// Настройки подразделений по НДФЛ (таблица)
@Field final long REF_NDFL_DETAIL_ID = RefBook.Id.NDFL_DETAIL.id

@Field final FORM_NAME_NDFL6 = "НДФЛ6"
@Field final FORM_NAME_NDFL2 = "НДФЛ2"
@Field final DECLARATION_TYPE_NDFL2_ID = 102

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

@Field final String DATE_FORMAT_UNDERLINE = "yyyy_MM_dd"
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

/************************************* СОЗДАНИЕ XML *****************************************************************/
def buildXml() {

    // Параметры подразделения
    def departmentParam = getDepartmentParam(declarationData.departmentId)
    def departmentParamIncomeRow = getDepartmentParamTable(departmentParam.record_id.value)

    // Отчетный период
    def reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)

    // Код периода
    def periodCode = getRefBookValue(REF_PERIOD_CODE_ID, reportPeriod?.dictTaxPeriodId)?.CODE?.stringValue

    // Коды представления налоговой декларации по месту нахождения (учёта)
    def taxPlaceTypeCode = getRefBookValue(REF_TAX_PLACE_TYPE_CODE_ID, departmentParamIncomeRow?.TAX_PLACE_TYPE_CODE?.referenceValue)?.CODE?.stringValue

    // Признак лица, подписавшего документ
    def signatoryId = getRefBookValue(REF_MARK_SIGNATORY_CODE_ID, departmentParamIncomeRow?.SIGNATORY_ID?.referenceValue)?.CODE?.numberValue

    // Учитывать будем только информацию о доходах/налогах только за отчетный период
    def ndflPersonIncomeCommonValue = ndflPersonService.findNdflPersonIncomeCommonValue(declarationData.id, reportPeriod.startDate, reportPeriod.endDate);
    def ndflPersonIncomeByRateList = ndflPersonIncomeCommonValue?.ndflPersonIncomeByRateList
    // Учитывать будем только информацию о доходах/налогах за последний квартал отчетного периода
    def ndflPersonIncomeByDateList = ndflPersonService.findNdflPersonIncomeByDate(declarationData.id, reportPeriod.calendarStartDate, reportPeriod.endDate)

    def builder = new MarkupBuilder(xml)
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
                        ИННЮЛ: departmentParam.INN,
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
                ОбобщПоказ(
                        КолФЛДоход: ndflPersonIncomeCommonValue?.countPerson?.value,
                        УдержНалИт: ndflPersonIncomeCommonValue?.withholdingTax?.value,
                        НеУдержНалИт: ndflPersonIncomeCommonValue?.notHoldingTax?.value,
                        ВозврНалИт: ndflPersonIncomeCommonValue?.refoundTax?.value
                ) {
                    ndflPersonIncomeByRateList.each { ndflPersonIncomeByRate ->
                        if (ndflPersonIncomeByRate.incomeAccruedSumm == null) {
                            ndflPersonIncomeByRate.incomeAccruedSumm = 0
                        }
                        if (ndflPersonIncomeByRate.incomeAccruedSummDiv == null) {
                            ndflPersonIncomeByRate.incomeAccruedSummDiv = 0
                        }
                        if (ndflPersonIncomeByRate.totalDeductionsSumm == null) {
                            ndflPersonIncomeByRate.totalDeductionsSumm = 0
                        }
                        if (ndflPersonIncomeByRate.calculatedTax == null) {
                            ndflPersonIncomeByRate.calculatedTax = 0
                        }
                        if (ndflPersonIncomeByRate.calculatedTaxDiv == null) {
                            ndflPersonIncomeByRate.calculatedTaxDiv = 0
                        }
                        if (ndflPersonIncomeByRate.prepaymentSum == null) {
                            ndflPersonIncomeByRate.prepaymentSum = 0
                        }
                        СумСтавка(
                                Ставка: ndflPersonIncomeByRate.taxRate,
                                НачислДох: ScriptUtils.round(ndflPersonIncomeByRate.incomeAccruedSumm, 2),
                                НачислДохДив: ScriptUtils.round(ndflPersonIncomeByRate.incomeAccruedSummDiv, 2),
                                ВычетНал: ScriptUtils.round(ndflPersonIncomeByRate.totalDeductionsSumm, 2),
                                ИсчислНал: ndflPersonIncomeByRate.calculatedTax,
                                ИсчислНалДив: ndflPersonIncomeByRate.calculatedTaxDiv,
                                АвансПлат: ndflPersonIncomeByRate.prepaymentSum
                        ) {}
                    }
                }
                // Узел ДохНал необязателен
                if (ndflPersonIncomeByDateList.size() > 0) {
                    ДохНал() {
                        ndflPersonIncomeByDateList.each { ndflPersonIncomeByDate ->
                            if (ndflPersonIncomeByDate.incomePayoutSumm == null) {
                                ndflPersonIncomeByDate.incomePayoutSumm = 0
                            }
                            if (ndflPersonIncomeByDate.withholdingTax == null) {
                                ndflPersonIncomeByDate.withholdingTax = 0
                            }
                            СумДата(
                                    ДатаФактДох: ndflPersonIncomeByDate.incomeAccruedDate.format(DATE_FORMAT_DOT),
                                    ДатаУдержНал: ndflPersonIncomeByDate.taxDate.format(DATE_FORMAT_DOT),
                                    СрокПрчслНал: ndflPersonIncomeByDate.taxTransferDate.format(DATE_FORMAT_DOT),
                                    ФактДоход: ScriptUtils.round(ndflPersonIncomeByDate.incomePayoutSumm, 2),
                                    УдержНал: ndflPersonIncomeByDate.withholdingTax
                            ) {}
                        }
                    }
                }
            }
        }
    }
//    println(xml)
}

/************************************* ПРОВЕРКА XML *****************************************************************/
def checkXml() {
    //---------------------------------------------------------------
    // Внутридокументные проверки

    def msgError = "В форме \"%s\" КПП: \"%s\" ОКТМО: \"%s\" "
    msgError = sprintf(msgError, FORM_NAME_NDFL6, declarationData.kpp, declarationData.oktmo)

    def ndfl6Stream = declarationService.getXmlStream(declarationData.id)
    def fileNode = new XmlSlurper().parse(ndfl6Stream);

    // ВнДок2 Расчет погрешности
    def sumDataNodes = fileNode.depthFirst().grep { it.name() == NODE_NAME_SUM_DATA6 }
    def mathError = sumDataNodes.size()

    def sumStavkaNodes = fileNode.depthFirst().grep { it.name() == NODE_NAME_SUM_STAVKA6 }
    sumStavkaNodes.each { sumStavkaNode ->
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
            logger.info(((nachislDoh - vichetNal) / 100 * stavka).toString())
            logger.info((ischislNal - mathError).toString())
            logger.info((ischislNal + mathError).toString())
            logger.error(msgError + " неверно рассчитана сумма исчисленного налога.")
        }

        // ВнДок3 Авансовый платеж
        if (avansPlat > ischislNal) {
            logger.info("3")
            logger.error(msgError + " завышена сумма фиксированного авансового платежа.")
        }
    }

    //---------------------------------------------------------------
    // Междокументные проверки

    // Код отчетного периода
    def reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
    def periodCode = getRefBookValue(REF_PERIOD_CODE_ID, reportPeriod?.dictTaxPeriodId)?.CODE?.stringValue

    if (["34", "90"].contains(periodCode)) {
        def ndfl2DeclarationDataIds = getNdfl2DeclarationDataId(reportPeriod.taxPeriod.year)
        if (ndfl2DeclarationDataIds.size() > 0) {
            checkBetweenDocumentXml(ndfl6Stream, ndfl2DeclarationDataIds)
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
    def declarationDataList = declarationService.find(DECLARATION_TYPE_NDFL2_ID, declarationData.reportPeriodId)
    for (DeclarationData dd : declarationDataList) {
        def reportPeriod = reportPeriodService.get(dd.reportPeriodId)
        def periodCode = getRefBookValue(REF_PERIOD_CODE_ID, reportPeriod?.dictTaxPeriodId)?.CODE?.stringValue
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
def checkBetweenDocumentXml(def ndfl6Stream, def ndfl2DeclarationDataIds) {

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

    def fileNode6Ndfl = new XmlSlurper().parse(ndfl6Stream);
    def sumStavkaNodes6 = fileNode6Ndfl.depthFirst().grep { it.name() == NODE_NAME_SUM_STAVKA6 }
    sumStavkaNodes6.each { sumStavkaNode6 ->
        def stavka6 = sumStavkaNode6.attributes()[ATTR_RATE].toInteger()

        // МежДок4
        def nachislDoh6 = sumStavkaNode6.attributes()[ATTR_NACHISL_DOH6].toDouble()
        mapNachislDoh6.put(stavka6, nachislDoh6)

        // МежДок5
        if (stavka6 == RATE_THIRTEEN) {
            nachislDohDiv6 = sumStavkaNode6.attributes()[ATTR_NACHISL_DOH_DIV6].toDouble()
        }

        // МежДок6
        def ischislNal6 = sumStavkaNode6.attributes()[ATTR_ISCHISL_NAL6].toDouble()
        mapIschislNal6.put(stavka6, ischislNal6)
    }

    def obobshPokazNodes6 = fileNode6Ndfl.depthFirst().grep { it.name() == NODE_NAME_OBOBSH_POKAZ6 }
    obobshPokazNodes6.each { obobshPokazNode6 ->
        // МежДок7
        neUderzNalIt6 = obobshPokazNode6.attributes()[ATTR_NE_UDERZ_NAL_IT6].toDouble()

        // МежДок8
        kolFl6 = obobshPokazNode6.attributes()[ATTR_KOL_FL_DOHOD6].toDouble()
    }

    // Суммы значений всех 2-НДФЛ сравниваются с одним 6-НДФЛ
    ndfl2DeclarationDataIds.each { ndfl2DeclarationDataId ->
        def ndfl2Stream = declarationService.getXmlStream(ndfl2DeclarationDataId)
        def fileNode2Ndfl = new XmlSlurper().parse(ndfl2Stream);

        // МежДок8
        def documentNodes = fileNode2Ndfl.depthFirst().findAll { it.name() == NODE_NAME_DOCUMNET2 }
        kolFl2 += documentNodes.size()

        def svedDohNodes = fileNode2Ndfl.depthFirst().grep { it.name() == NODE_NAME_SVED_DOH2 }
        svedDohNodes.each { svedDohNode ->
            def stavka2 = svedDohNode.attributes()[ATTR_RATE].toInteger()

            // МежДок4
            def sumDohObch2 = mapSumDohObch2.get(stavka2)
            sumDohObch2 = sumDohObch2 == null ? 0 : sumDohObch2

            // МежДок6
            def nalIschisl2 = mapNalIschisl2.get(stavka2)
            nalIschisl2 = nalIschisl2 == null ? 0 : nalIschisl2

            def sumItNalPerNodes = svedDohNode.depthFirst().grep { it.name() == NODE_NAME_SUM_IT_NAL_PER2 }
            sumItNalPerNodes.each { sumItNalPerNode ->
                sumDohObch2 += sumItNalPerNode.attributes()[ATTR_SUM_DOH_OBSH2].toDouble()
                nalIschisl2 += sumItNalPerNode.attributes()[ATTR_NAL_ISCHISL2].toDouble()

                // МежДок7
                nalNeUderz2 += sumItNalPerNode.attributes()[ATTR_NAL_NE_UDERZ2].toDouble()
            }
            mapSumDohObch2.put(stavka2, sumDohObch2)
            mapNalIschisl2.put(stavka2, nalIschisl2)

            // МежДок5
            if (stavka2 == RATE_THIRTEEN) {
                def svSumDohNodes = svedDohNode.depthFirst().grep { it.name() == NODE_NAME_SV_SUM_DOH2 }
                svSumDohNodes.each { svSumDohNode ->
                    if (svSumDohNode.attributes()[ATTR_KOD_DOHOD2].toString() == "1010") {
                        sumDohDivObch2 += svSumDohNode.attributes()[ATTR_SUM_DOHOD2].toDouble()
                    }
                }
            }
        }
    }

    // МежДок4
    mapNachislDoh6.each { stavka6, nachislDoh6 ->
        def sumDohObch2 = mapSumDohObch2.get(stavka6)
        if (nachislDoh6 != sumDohObch2) {
            msgError = sprintf(msgError, "сумме начисленного дохода") + " по ставке " + stavka6
            logger.error(msgError)
        }
    }

    // МежДок5
    if (nachislDohDiv6 != sumDohDivObch2) {
        msgError = sprintf(msgError, "сумме начисленного дохода в виде дивидендов")
        logger.error(msgError)
    }

    // МежДок6
    mapIschislNal6.each { stavka6, ischislNal6 ->
        def nalIschisl2 = mapNalIschisl2.get(stavka6)
        if (ischislNal6 != nalIschisl2) {
            msgError = sprintf(msgError, "сумме налога исчисленного") + " по ставке " + stavka6
            logger.error(msgError)
        }
    }

    // МежДок7
    if (neUderzNalIt6 != nalNeUderz2) {
        msgError = sprintf(msgError, "сумме налога, не удержанная налоговым агентом")
        logger.error(msgError)
    }

    // МежДок8
    if (kolFl6 != kolFl2) {
        msgError = sprintf(msgError, "количеству физических лиц, получивших доход")
        logger.error(msgError)
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
 * Получить параметры для конкретного тербанка
 * @return
 */
def getDepartmentParam(def departmentId) {
    if (departmentParam == null) {
        def departmentParamList = getProvider(REF_NDFL_ID).getRecords(getReportPeriodEndDate() - 1, null, "DEPARTMENT_ID = $departmentId", null)
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
        def filter = "REF_BOOK_NDFL_ID = $departmentParamId and KPP ='${declarationData.kpp}'"
        def departmentParamTableList = getProvider(REF_NDFL_DETAIL_ID).getRecords(getReportPeriodEndDate() - 1, null, filter, null)
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

/************************************* СОЗДАНИЕ ФОРМЫ *****************************************************************/

@Field
final int RNU_NDFL_DECLARATION_TYPE = 101

@Field
final int REF_BOOK_NDFL_DETAIL_ID = 951

@Field
def departmentParamTableList = null;

@Field
final int REF_BOOK_NDFL_ID = 950

def createForm() {
    def departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
    def korrPeriod = isCorrectionPeriod()
    def pairKppOktmoList = []

    def currDeclarationTemplate = declarationService.getTemplate(declarationData.declarationTemplateId)
    def declarationTypeId = currDeclarationTemplate.type.id
    // step 2
    if (korrPeriod) {
        def prevDepartmentPeriodReport = departmentReportPeriodService.getPrevLast(declarationData.departmentId, departmentReportPeriod.reportPeriod.id)
        def declarations = declarationService.find(declarationTypeId, prevDepartmentPeriodReport.id)
        def declarationsForRemove = []
        declarations.each { declaration ->
            def declarationTemplate = declarationService.getTemplate(declaration.declarationTemplateId)
            if (declarationTemplate.declarationFormKind != DeclarationFormKind.REPORTS || (declaration.state == State.ACCEPTED)) {
                declarationsForRemove << declaration
            }
        }
        declarations.removeAll(declarationsForRemove)
        declarations.each { declaration ->
            pairKppOktmoList << new PairKppOktmo(Integer.valueOf(declaration.kpp), declaration.oktmo)
        }
        formType = getFormType(currDeclarationTemplate)
        if (definePriznakF() != "0") {
            //TODO реализовать работу с реестром справок
        }
    } else {
        // step 5
        departmentParam = getDepartmentParam(departmentReportPeriod.departmentId, departmentReportPeriod.reportPeriod.id)
        departmentParamTableList = getDepartmentParamTableList(departmentParam?.id, departmentReportPeriod.reportPeriod.id)
        departmentParamTableList.each { dep ->
            pairKppOktmoList << new PairKppOktmo(dep.KPP?.value, dep.OKTMO?.value, dep?.TAX_ORGAN_CODE?.value)
        }
    }
    // step 3 и step 4
    // получить id всех ТБ для данного отчетного периода
    def allDepartmentReportPeriodIds = departmentReportPeriodService.getIdsByDepartmentTypeAndReportPeriod(DepartmentType.TERR_BANK.getCode(), departmentReportPeriod.reportPeriod.id)
    // список форм рну-ндфл для отчетного периода всех ТБ
    def allDeclarationData = []
    allDepartmentReportPeriodIds.each {
        allDeclarationData.addAll(declarationService.find(RNU_NDFL_DECLARATION_TYPE, it))
    }
    // удаление форм не со статусом принята
    def declarationsForRemove = []
    allDeclarationData.each { declaration ->
        if (declaration.state != State.ACCEPTED) {
            declarationsForRemove << declaration
        }
    }
    allDeclarationData.removeAll(declarationsForRemove)
    // TODO реализовать работу с реестром справок для шага 6

    // step 7
    // Список физлиц для каждой пары КПП и ОКТМО
    def ndflPersonsGroupedByKppOktmo = [:]
    allDeclarationData.each { declaration ->
        pairKppOktmoList.each { np ->
            def ndflPersons = ndflPersonService.findNdflPersonByPairKppOktmo(declaration.id, np.kpp.toString(), np.oktmo.toString())
            if (ndflPersons != null && ndflPersons.size != 0) {
                ndflPersonsGroupedByKppOktmo[np] = ndflPersons
            }
        }
    }

    ndflPersonsGroupedByKppOktmo.each { npGroup ->
        def oktmo = npGroup.key.oktmo
        def kpp = npGroup.key.kpp
        def taxOrganCode = npGroup.key.taxOrganCode
        Map<String, Object> params
        Long ddId
        params = new HashMap<String, Object>()
        ddId = declarationService.create(logger, declarationData.declarationTemplateId, userInfo,
                departmentReportPeriodService.get(declarationData.departmentReportPeriodId), taxOrganCode, kpp.toString(), oktmo, null, null)
        formMap.put(ddId, params)
    }
    declarationService.find(declarationTypeId, declarationData.departmentReportPeriodId).each {
        declarationService.delete(it.id, userInfo)
    }
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

def isCorrectionPeriod() {
    def nomKorr = reportPeriodService.getCorrectionNumber(declarationData.departmentReportPeriodId)
    if (nomKorr != 0) {
        return true
    }
}

def getReportPeriodEndDate(def reportPeriodId) {
    if (reportPeriodEndDate == null) {
        reportPeriodEndDate = reportPeriodService.getEndDate(reportPeriodId)?.time
    }
    return reportPeriodEndDate
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
}