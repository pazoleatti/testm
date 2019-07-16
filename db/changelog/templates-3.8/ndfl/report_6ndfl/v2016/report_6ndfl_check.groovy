package form_template.ndfl.report_6ndfl.v2016

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.DeclarationData
import com.aplana.sbrf.taxaccounting.model.Department
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.OperationType
import com.aplana.sbrf.taxaccounting.model.Relation
import com.aplana.sbrf.taxaccounting.model.State
import com.aplana.sbrf.taxaccounting.script.service.*
import com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode

new Check(this).run()

@TypeChecked
class Check extends AbstractScriptClass {

    DeclarationData declarationData
    DepartmentReportPeriodService departmentReportPeriodService
    DepartmentService departmentService
    ReportPeriodService reportPeriodService
    SourceService sourceService
    OperationType operationType

    final FORM_NAME_NDFL6 = "6-НДФЛ"
    final FORM_NAME_NDFL2 = "2-НДФЛ (1)"
    final int DECLARATION_TYPE_NDFL2_1_ID = 102
    final String DATE_FORMAT_DOTTED = "dd.MM.yyyy"

    // Узлы 6 НДФЛ
    final NODE_NAME_SUM_STAVKA6 = "СумСтавка"
    final NODE_NAME_OBOBSH_POKAZ6 = "ОбобщПоказ"
    final NODE_NAME_SUM_DATA6 = "СумДата"

// Узлы 2 НДФЛ
    final NODE_NAME_DOCUMNET2 = "Документ"
    final NODE_NAME_SVED_DOH2 = "СведДох"
    final NODE_NAME_SUM_IT_NAL_PER2 = "СумИтНалПер"
    final NODE_NAME_SV_SUM_DOH2 = "СвСумДох"

// Общие атрибуты
    final ATTR_RATE = "Ставка"
    final int RATE_THIRTEEN = 13

// Атрибуты 6 НДФЛ
    final ATTR_NACHISL_DOH6 = "НачислДох"
    final ATTR_NACHISL_DOH_DIV6 = "НачислДохДив"
    final ATTR_VICHET_NAL6 = "ВычетНал"
    final ATTR_ISCHISL_NAL6 = "ИсчислНал"
    final ATTR_NE_UDERZ_NAL_IT6 = "НеУдержНалИт"
    final ATTR_KOL_FL_DOHOD6 = "КолФЛДоход"
    final ATTR_AVANS_PLAT6 = "АвансПлат"

// Атрибуты 2 НДФЛ
    final ATTR_SUM_DOH_OBSH2 = "СумДохОбщ"
    final ATTR_NAL_ISCHISL2 = "НалИсчисл"
    final ATTR_NAL_NE_UDERZ2 = "НалНеУдерж"
    final ATTR_KOD_DOHOD2 = "КодДоход"
    final ATTR_SUM_DOHOD2 = "СумДоход"

    private Check() {
    }

    @TypeChecked (TypeCheckingMode.SKIP)
    Check(scriptClass) {
        super(scriptClass)
        if (scriptClass.getBinding().hasVariable("declarationData")) {
            this.declarationData = (DeclarationData) scriptClass.getProperty("declarationData");
        }
        if (scriptClass.getBinding().hasVariable("departmentReportPeriodService")) {
            this.departmentReportPeriodService = (DepartmentReportPeriodService) scriptClass.getProperty("departmentReportPeriodService");
        }
        if (scriptClass.getBinding().hasVariable("departmentService")) {
            this.departmentService = (DepartmentService) scriptClass.getProperty("departmentService");
        }
        if (scriptClass.getBinding().hasVariable("reportPeriodService")) {
            this.reportPeriodService = (ReportPeriodService) scriptClass.getProperty("reportPeriodService");
        }
        if (scriptClass.getBinding().hasVariable("sourceService")) {
            this.sourceService = (SourceService) scriptClass.getProperty("sourceService")
        }
        if (scriptClass.getBinding().hasVariable("operationType")) {
            this.operationType = (OperationType) scriptClass.getProperty("operationType")
        }
    }

    @Override
    public void run() {
        switch (formDataEvent) {
            case FormDataEvent.CHECK:
                check()
        }
    }

    def check() {
        checkSources()
        checkXml()
    }

    // Проверяет что все формы-источники в текущем тербанке находятся в состоянии "Принята" и были принятыы после консолидации текущей формы
    void checkSources() {
        Integer declarationTerbankId = departmentService.getParentTBId(declarationData.departmentId)
        List<Relation> sources = sourceService.getSourcesInfo(declarationData)
        List<Relation> sourcesInTheSameTerbank = sources.findAll {
            Integer sourceTerbankId = departmentService.getParentTBId(it.departmentId)
            return declarationTerbankId == sourceTerbankId
        }
        boolean isAnySourceNotAccepted = sourcesInTheSameTerbank.any { it.declarationState != State.ACCEPTED }

        Date lastConsolidationDate = declarationService.getMaxLogDateByDeclarationIdAndEvent(declarationData.id, FormDataEvent.CREATE)
        boolean isAnySourceAcceptedAfterTheLastConsolidation = sourcesInTheSameTerbank.any {
            Date lastSourceAcceptanceDate = declarationService.getMaxLogDateByDeclarationIdAndEvent(it.declarationDataId, FormDataEvent.ACCEPT)
            return lastSourceAcceptanceDate > lastConsolidationDate
        }

        if (isAnySourceNotAccepted && isAnySourceAcceptedAfterTheLastConsolidation) {
            logger.logCheck("В списке \"Источники-приемники\" есть формы-источники, принятые позже создания отчетной формы, " +
                    "а так же формы-источники в состоянии, отличном от \"Принята\"",
                    operationType == OperationType.ACCEPT_DEC, "Состояние форм-источников отличается от \"Принята\"", "")
        } else if (isAnySourceNotAccepted) {
            logger.logCheck("В списке \"Источники-приемники\" есть формы-источники в состоянии, отличном от \"Принята\"",
                    operationType == OperationType.ACCEPT_DEC, "Состояние форм-источников отличается от \"Принята\"", "")
        } else if (isAnySourceAcceptedAfterTheLastConsolidation) {
            logger.logCheck("В списке \"Источники-приемники\" есть формы источники, принятые позже создания отчетной формы",
                    operationType == OperationType.ACCEPT_DEC, "Состояние форм-источников отличается от \"Принята\"", "")
        }
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    def checkXml() {
        //---------------------------------------------------------------
        // Внутридокументные проверки
        ScriptUtils.checkInterrupted()
        def msgError = "В форме \"%s\", КПП: \"%s\", ОКТМО: \"%s\" "
        msgError = sprintf(msgError, FORM_NAME_NDFL6, declarationData.kpp, declarationData.oktmo)

        def ndfl6Stream = declarationService.getXmlStream(declarationData.id, userInfo)
        def fileNode = new XmlSlurper().parse(ndfl6Stream);

        // ВнДок2 Расчет погрешности
        def kolFl6
        def obobshPokazNodes6 = fileNode.depthFirst().grep { it.name() == NODE_NAME_OBOBSH_POKAZ6 }
        obobshPokazNodes6.each { obobshPokazNode6 ->
            ScriptUtils.checkInterrupted()
            kolFl6 = Integer.valueOf(obobshPokazNode6.attributes()[ATTR_KOL_FL_DOHOD6]) ?: 0
        }
        def sumDataNodes = fileNode.depthFirst().grep { it.name() == NODE_NAME_SUM_DATA6 }
        def ДопустимаяПогрешность = sumDataNodes.size() * kolFl6

        def sumStavkaNodes = fileNode.depthFirst().grep { it.name() == NODE_NAME_SUM_STAVKA6 }
        sumStavkaNodes.each { sumStavkaNode ->
            ScriptUtils.checkInterrupted()
            def Ставка = sumStavkaNode.attributes()[ATTR_RATE] ? sumStavkaNode.attributes()[ATTR_RATE].toInteger() : 0
            def НачислДох = sumStavkaNode.attributes()[ATTR_NACHISL_DOH6].toDouble()
            def ВычетНал = sumStavkaNode.attributes()[ATTR_VICHET_NAL6].toDouble()
            def ИсчислНал = sumStavkaNode.attributes()[ATTR_ISCHISL_NAL6].toDouble()
            def АвансПлат = sumStavkaNode.attributes()[ATTR_AVANS_PLAT6].toDouble()

            // ВнДок1 Сравнение сумм вычетов и дохода
            if (ВычетНал > НачислДох) {
                logger.errorExp(msgError + " «Сумма налоговых вычетов» превышает «Cумму начисленного дохода»", "«Сумма налоговых вычетов» превышает «Cумму начисленного дохода»", "")
            }
            def ИсчислНалРасч = Math.round((НачислДох - ВычетНал) / 100 * Ставка)
            def Дельта = ИсчислНалРасч - ИсчислНал
            // ВнДок2 Исчисленный налог
            if (!(Math.abs(Дельта) <= ДопустимаяПогрешность)) {
                logger.warnExp(msgError + " неверно рассчитана «Cумма исчисленного налога». Модуль разности (${Math.abs(Дельта)}) рассчитанного " +
                        "значения суммы исчисленного налога ($ИсчислНалРасч) и указанного в строке 040 ($ИсчислНал) больше допустимого " +
                        "значения погрешности ($ДопустимаяПогрешность).", "«Cумма исчисленного налога» рассчитана некорректно", "")
            }

            // ВнДок3 Авансовый платеж
            if (АвансПлат > 0 && АвансПлат > ИсчислНал) {
                logger.errorExp(msgError + " завышена «Cумма фиксированного авансового платежа», сумма Фиксированного авансового платежа не должна превышать сумму Исчисленного налога.",
                        "«Cумма фиксированного авансового платежа» заполнена некорректно", "")
            }

            // ВнДок3 Авансовый платеж
            if (ИсчислНал && ИсчислНал < 0) {
                logger.warnExp(msgError + " значение \"Сумма исчисленного налога\" не может иметь отрицательное значение.",
                        "«Сумма исчисленного налога» не должна быть отрицательной", "")
            }
        }

        //---------------------------------------------------------------
        // Междокументные проверки

        // Код отчетного периода
        def reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
        def ndfl2DeclarationDataIds = getNdfl2DeclarationDataId()
        if (ndfl2DeclarationDataIds.size() > 0) {
            checkBetweenDocumentXml(ndfl2DeclarationDataIds)
        }
    }

/**
 * Получение идентификаторо DeclarationData 2-ндфл
 * @param taxPeriodYear - отчетный год
 *
 */
    def getNdfl2DeclarationDataId() {
        def result = []
        List<DeclarationData> declarationDataList = declarationService.findAllByTypeIdAndPeriodId(DECLARATION_TYPE_NDFL2_1_ID, declarationData.departmentReportPeriodId)
        for (DeclarationData dd : declarationDataList) {
            ScriptUtils.checkInterrupted()
            if (dd.departmentReportPeriodId == declarationData.departmentReportPeriodId
                    && dd.kpp == declarationData.kpp
                    && dd.oktmo == declarationData.oktmo
                    && dd.taxOrganCode == declarationData.taxOrganCode) {
                result.add(dd.id)
            }
        }
        if (result.isEmpty()) {
            DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
            Department department = departmentService.get(departmentReportPeriod.departmentId)
            logger.warn("Выполнить проверку междокументных контрольных соотношений невозможно. Отсутствует форма 2-НДФЛ (1) для подразделения: \"${department.name}\", КПП: ${declarationData.kpp}, ОКТМО: ${declarationData.oktmo}, Код НО: ${declarationData.taxOrganCode}, Период: ${departmentReportPeriod.reportPeriod.name} ${departmentReportPeriod.reportPeriod.taxPeriod.year} ${departmentReportPeriod.correctionDate ? " с датой сдачи корректировки " + departmentReportPeriod.getCorrectionDate().format(DATE_FORMAT_DOTTED) : ""}")
        }
        return result
    }

/**
 * Междокументные проверки
 * @return
 */
    @TypeChecked (TypeCheckingMode.SKIP)
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

        def ndfl6Stream = declarationService.getXmlStream(declarationData.id, userInfo)
        def fileNode6Ndfl = new XmlSlurper().parse(ndfl6Stream);
        def sumStavkaNodes6 = fileNode6Ndfl.depthFirst().grep { it.name() == NODE_NAME_SUM_STAVKA6 }
        sumStavkaNodes6.each { sumStavkaNode6 ->
            ScriptUtils.checkInterrupted()
            def stavka6 = sumStavkaNode6.attributes()[ATTR_RATE] ? Integer.valueOf(sumStavkaNode6.attributes()[ATTR_RATE]) : 0

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
        for (def ndfl2DeclarationDataId : ndfl2DeclarationDataIds) {
            ScriptUtils.checkInterrupted()
            def ndfl2Stream = declarationService.getXmlStream(ndfl2DeclarationDataId, userInfo)
            if (ndfl2Stream == null) {
                DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
                Department department = departmentService.get(departmentReportPeriod.departmentId)
                logger.warn("Выполнить проверку междокументных контрольных соотношений невозможно. Заблокирована форма 2-НДФЛ (1) для подразделения: \"${department.name}\", КПП: ${declarationData.kpp}, ОКТМО: ${declarationData.oktmo}, Код НО: ${declarationData.taxOrganCode}, период: ${departmentReportPeriod.reportPeriod.name} ${departmentReportPeriod.reportPeriod.taxPeriod.year} ${departmentReportPeriod.correctionDate ? " с датой сдачи корректировки " + departmentReportPeriod.getCorrectionDate().format(DATE_FORMAT_DOTTED) : ""}. Форма находится в процессе создания.")
                continue
            }
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
                            sumDohDivObch2 += Double.valueOf(svSumDohNode.attributes()[ATTR_SUM_DOHOD2] ?: 0)
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
                def msgErrorRes = sprintf(msgError, "«Сумме начисленного дохода»") + " по «Ставке» " + stavka6
                logger.warnExp(msgErrorRes, "«Сумма начисленного дохода» рассчитана некорректно", "")
            }
        }

        // МежДок5
        if (ScriptUtils.round(nachislDohDiv6, 2) != ScriptUtils.round(sumDohDivObch2, 2)) {
            def msgErrorRes = sprintf(msgError, "«Сумме начисленного дохода» в виде дивидендов")
            logger.warnExp(msgErrorRes, "«Сумма начисленного дохода» рассчитана некорректно", "")
        }

        // МежДок6

        mapIschislNal6.each { stavka6, ischislNal6 ->
            def nalIschisl2 = mapNalIschisl2.get(stavka6)
            if (ischislNal6 != nalIschisl2) {
                def msgErrorRes = sprintf(msgError, "«Сумме налога исчисленного»") + " по «Ставке» " + stavka6
                logger.warnExp(msgErrorRes, "«Сумма налога исчисленного» рассчитана некорректно", "")
            }
        }

        // МежДок7
        if (neUderzNalIt6 != nalNeUderz2) {
            def msgErrorRes = sprintf(msgError, "«Сумме налога, не удержанной налоговым агентом»")
            logger.warnExp(msgErrorRes, "«Сумма налога, не удержанная налоговым агентом» рассчитана некорректно", "")
        }

        // МежДок8
        if (kolFl6 != kolFl2) {
            def msgErrorRes = sprintf(msgError, "количеству физических лиц, получивших доход")
            logger.warnExp(msgErrorRes, "«Количество физических лиц, получивших доход» рассчитано некорректно", "")
        }
    }
}