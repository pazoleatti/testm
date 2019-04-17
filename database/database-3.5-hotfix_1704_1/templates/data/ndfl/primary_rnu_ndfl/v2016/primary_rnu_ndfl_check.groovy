package form_template.ndfl.primary_rnu_ndfl.v2016

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.DeclarationCheckCode
import com.aplana.sbrf.taxaccounting.model.DeclarationData
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.PagingResult
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment
import com.aplana.sbrf.taxaccounting.model.refbook.IdDoc
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import com.aplana.sbrf.taxaccounting.model.refbook.RegistryPerson
import com.aplana.sbrf.taxaccounting.model.util.BaseWeightCalculator
import com.aplana.sbrf.taxaccounting.model.util.Pair
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory
import com.aplana.sbrf.taxaccounting.script.SharedConstants
import com.aplana.sbrf.taxaccounting.script.service.CalendarService
import com.aplana.sbrf.taxaccounting.script.service.DeclarationService
import com.aplana.sbrf.taxaccounting.script.service.DepartmentReportPeriodService
import com.aplana.sbrf.taxaccounting.script.service.DepartmentService
import com.aplana.sbrf.taxaccounting.script.service.FiasRefBookService
import com.aplana.sbrf.taxaccounting.script.service.NdflPersonService
import com.aplana.sbrf.taxaccounting.script.service.PersonService
import com.aplana.sbrf.taxaccounting.script.service.RefBookService
import com.aplana.sbrf.taxaccounting.script.service.ReportPeriodService
import com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils
import com.aplana.sbrf.taxaccounting.utils.SimpleDateUtils
import groovy.transform.EqualsAndHashCode
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import org.apache.commons.lang3.time.DateUtils

new Check(this).run()

@TypeChecked
class Check extends AbstractScriptClass {

    NdflPersonService ndflPersonService
    DeclarationData declarationData
    DepartmentReportPeriod departmentReportPeriod
    ReportPeriod reportPeriod
    DepartmentService departmentService
    CalendarService calendarService
    FiasRefBookService fiasRefBookService
    ReportPeriodService reportPeriodService
    DepartmentReportPeriodService departmentReportPeriodService
    RefBookFactory refBookFactory
    PersonService personService
    RefBookService refBookService

    // Сервис для получения рабочих дней
    DateConditionWorkDay dateConditionWorkDay

    final String SUCCESS_GET_TABLE = "Получены записи таблицы \"%s\" (%d записей)."
    final String SUCCESS_GET_REF_BOOK = "Получен справочник \"%s\" (%d записей)."

    final String T_PERSON_NAME = "Реквизиты"
    final String T_PERSON_INCOME_NAME = "Сведения о доходах и НДФЛ"
    final String T_PERSON_DEDUCTION_NAME = "Сведения о вычетах"
    final String T_PERSON_PREPAYMENT_NAME = "Сведения о доходах в виде авансовых платежей"
    final String T_PERSON = "1" //"Реквизиты"
    final String T_PERSON_INCOME = "2" // "Сведения о доходах и НДФЛ"
    final String T_PERSON_DEDUCTION = "3" // "Сведения о вычетах"
    final String T_PERSON_PREPAYMENT = "4" //"Сведения о доходах в виде авансовых платежей"

    final String R_PERSON = "Физические лица"
    final String R_CITIZENSHIP = "ОК 025-2001 (Общероссийский классификатор стран мира)"
    final String R_ID_DOC_TYPE = "Коды документов"
    final String R_STATUS = "Статусы налогоплательщика"
    final String R_INCOME_CODE = "Коды видов доходов"
    final String R_INCOME_TYPE = "Виды дохода"
    final String R_TYPE_CODE = "Коды видов вычетов"
    final String R_NOTIF_SOURCE = "Налоговые инспекции"

    final String TEMPLATE_PERSON_FL = "%s, ИНП: %s"
    final String TEMPLATE_PERSON_FL_OPER = "%s, ИНП: %s, ID операции: %s"
    final String SECTION_LINE_MSG = "Раздел %s. Строка %s"
    final String SECTION_LINES_MSG = "Раздел %s. Строки %s"

    final String C_CITIZENSHIP = "Гражданство (код страны)"
    final String C_STATUS = "Статус (код)"
    final String C_INCOME_TYPE = "Признак дохода" //"Доход.Вид.Признак"
    final String C_INCOME_CODE = "Код дохода" //"Доход.Вид.Код"
    final String C_TYPE_CODE = "Код вычета" //" Код вычета"
    final String C_NOTIF_SOURCE = "Подтверждающий документ. Код источника"
    final String C_PAYMENT_DATE = "Дата платёжного поручения" //"НДФЛ.Перечисление в бюджет.Платежное поручение.Дата"
    final String C_PAYMENT_NUMBER = "Номер платёжного поручения"//"НДФЛ.Перечисление в бюджет.Платежное поручение.Номер"
    final String C_TAX_SUMM = "Сумма платёжного поручения" //"НДФЛ.Перечисление в бюджет.Платежное поручение.Сумма"
    final String C_INCOME_ACCRUED_DATE = "Дата начисления дохода" //"Доход.Дата.Начисление"
    final String C_INCOME_ACCRUED_SUMM = "Сумма начисленного дохода" //"Доход.Сумма.Начисление"
    final String C_INCOME_PAYOUT_DATE = "Дата выплаты дохода" //"Доход.Дата.Выплата"
    final String C_INCOME_PAYOUT_SUMM = "Сумма выплаченного дохода" //"Доход.Сумма.Выплата"
    final String C_OKTMO = "ОКТМО" //"Доход.Источник выплаты.ОКТМО"
    final String C_KPP = "КПП" //"Доход.Источник выплаты.КПП"
    final String C_TOTAL_DEDUCTIONS_SUMM = "Сумма вычета" //"Сумма вычета"
    final String C_TAX_BASE = "Налоговая база" //"Налоговая база"
    final String C_TAX_RATE = "Процентная ставка (%)" //"НДФЛ.Процентная ставка"
    final String C_TAX_DATE = "Дата НДФЛ" //"НДФЛ.Расчет.Дата"
    final String C_CALCULATED_TAX = "НДФЛ исчисленный" //" НДФЛ.Расчет.Сумма.Исчисленный"
    final String C_NOT_HOLDING_TAX = "НДФЛ не удержанный" //"НДФЛ.Расчет.Сумма.Не удержанный"
    final String C_OVERHOLDING_TAX = "НДФЛ излишне удержанный" //"НДФЛ.Расчет.Сумма.Излишне удержанный"
    final String C_WITHHOLDING_TAX = "НДФЛ удержанный" //"НДФЛ.Расчет.Сумма.Удержанный"
    final String C_TAX_TRANSFER_DATE = "Срок перечисления в бюджет" //"НДФЛ.Перечисление в бюджет.Срок"
    final String C_PERIOD_CURR_SUMM = "Вычет. Текущий период. Сумма" //" Применение вычета.Текущий период.Сумма"
    final String C_INCOME_ACCRUED = "Доход. Дата" //" Начисленный доход.Дата"
    final String C_INCOME_ACCRUED_CODE = "Доход. Код дохода" //" Начисленный доход.Код дохода"
    final String C_PERIOD_CURR_DATE = "Вычет. Текущий период. Дата" //" Применение вычета.Текущий период.Дата"
    final String C_NOTIF_SUMM = "Подтверждающий документ. Сумма" //" Документ о праве на налоговый вычет.Сумма"

    final String LOG_TYPE_REFERENCES = "Значение не соответствует справочнику \"%s\""
    final String LOG_TYPE_PERSON_MSG = "Значение гр. \"%s\" (\"%s\") не соответствует Реестру физических лиц"
    final String LOG_TYPE_PERSON_MSG_2 = "Значение гр. \"%s\" (\"%s\") отсутствует в справочнике \"%s\""
    final String LOG_TYPE_2_6 = "\"Дата начисления дохода\" указана некорректно"
    final String LOG_TYPE_2_12 = "\"Сумма вычета\" указана некорректно"
    final String LOG_TYPE_2_14 = "\"Налоговая ставка\" указана некорректно"
    final String LOG_TYPE_2_14_MSG = "Значение гр. \"%s\" (\"%s\") указано некорректно. Для \"Кода дохода\" (\"%s\") и \"Статуса НП\" (\"%s\") предусмотрены ставки: %s"
    final String LOG_TYPE_2_16 = "\"НДФЛ исчисленный\" рассчитан некорректно"
    final String LOG_TYPE_2_18_19 = "\"НДФЛ не удержанный\"/\"НДФЛ излишне удержанный\" рассчитан некорректно"
    final String LOG_TYPE_2_21 = "\"Срок перечисления в бюджет\" указан некорректно"
    final String LOG_TYPE_3_7 = "\"Код источника подтверждающего документа\" указан некорректно"
    final String LOG_TYPE_3_10 = "\"Дата начисленного дохода\" указана некорректно"
    final String LOG_TYPE_3_15 = "\"Дата применения вычета в текущем периоде\" не входит в текущий отчетный период"
    final String LOG_TYPE_3_11 = "\"Код начисленного дохода\" указан некорректно"
    final String LOG_TYPE_3_16 = "\"Сумма применения вычета\" указана некорректно"
    final String LOG_TYPE_SECTION4 = "Раздел 4 заполнен некорректно"

    // Сведения о доходах в виде авансовых платежей
    final String P_NOTIF_SOURCE = "Код налогового органа, выдавшего уведомление" // графа 7 раздела 4

    // Мапа <ID_Данные о физическом лице - получателе дохода, NdflPersonFL>
    Map<Long, NdflPersonFL> ndflPersonFLMap = [:]
    // Кэш строк раздела 1 по ид
    Map<Long, NdflPerson> personsById

    //Коды стран из справочника
    Map<Long, String> countryCodeCache = [:]

    //Виды документов, удостоверяющих личность
    Map<Long, Map<String, RefBookValue>> documentTypeCache = [:]
    Map<Long, String> documentTypeCodeCache = [:]

    //Коды статуса налогоплательщика
    Map<Long, String> taxpayerStatusCodeCache = [:]

    List<String> deductionTypeCache = []

    // Коды налоговых органов
    List<String> taxInspectionCache = []

    // Кэш провайдеров cправочников
    Map<Long, RefBookDataProvider> providerCache = [:]

    final FormDataKind FORM_DATA_KIND = FormDataKind.PRIMARY

    // Дата окончания отчетного периода
    Date periodEndDate = null

    // Дата начала отчетного периода
    Date reportPeriodStartDate = null

    // Сведения о доходах и НДФЛ
    List<NdflPersonIncome> ndflPersonIncomeList

    //Кэш Асну
    Map<Long, RefBookAsnu> asnuCache = [:]

    boolean section_2_15_fatal
    boolean citizenship_fatal
    boolean valueCondition_fatal
    boolean section_2_16Fatal
    boolean section_2_21_fatal
    boolean section_3_10_fatal
    boolean section_3_16_fatal

    @TypeChecked(TypeCheckingMode.SKIP)
    Check(scriptClass) {
        //noinspection GroovyAssignabilityCheck
        super(scriptClass)
        if (scriptClass.getBinding().hasVariable("departmentService")) {
            this.departmentService = (DepartmentService) scriptClass.getProperty("departmentService")
        }
        if (scriptClass.getBinding().hasVariable("reportPeriodService")) {
            this.reportPeriodService = (ReportPeriodService) scriptClass.getProperty("reportPeriodService")
        }
        if (scriptClass.getBinding().hasVariable("departmentReportPeriodService")) {
            this.departmentReportPeriodService = (DepartmentReportPeriodService) scriptClass.getProperty("departmentReportPeriodService")
        }
        if (scriptClass.getBinding().hasVariable("declarationData")) {
            declarationData = (DeclarationData) scriptClass.getProperty("declarationData")
            departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
            reportPeriod = departmentReportPeriod.reportPeriod
        }
        if (scriptClass.getBinding().hasVariable("refBookFactory")) {
            this.refBookFactory = (RefBookFactory) scriptClass.getProperty("refBookFactory")
        }
        if (scriptClass.getBinding().hasVariable("fiasRefBookService")) {
            this.fiasRefBookService = (FiasRefBookService) scriptClass.getProperty("fiasRefBookService")
        }
        if (scriptClass.getBinding().hasVariable("calendarService")) {
            this.calendarService = (CalendarService) scriptClass.getProperty("calendarService")
        }
        if (scriptClass.getBinding().hasVariable("ndflPersonService")) {
            this.ndflPersonService = (NdflPersonService) scriptClass.getProperty("ndflPersonService")
        }
        if (scriptClass.getBinding().hasVariable("declarationService")) {
            this.declarationService = (DeclarationService) scriptClass.getProperty("declarationService")
        }
        if (scriptClass.getBinding().hasVariable("personService")) {
            this.personService = (PersonService) scriptClass.getProperty("personService")
        }
        if (scriptClass.getBinding().hasVariable("refBookService")) {
            this.refBookService = (RefBookService) scriptClass.getProperty("refBookService")
        }
    }

    @Override
    void run() {
        initConfiguration()
        switch (formDataEvent) {
            case FormDataEvent.CHECK:
                ScriptUtils.checkInterrupted()

                long time = System.currentTimeMillis()
                readChecksFatals()

                Map<Long, RegistryPerson> personMap = getActualRefPersonsByDeclarationDataId(declarationData.id)
                logForDebug(SUCCESS_GET_TABLE, R_PERSON, personMap.size())

                // Реквизиты
                List<NdflPerson> ndflPersonList = ndflPersonService.findNdflPerson(declarationData.id)
                personsById = ndflPersonList.collectEntries { [it.id, it] }
                fillNdflPersonFLMap(ndflPersonList, personMap)
                logForDebug(SUCCESS_GET_TABLE, T_PERSON_NAME, ndflPersonList.size())

                // Сведения о доходах и НДФЛ
                ndflPersonIncomeList = ndflPersonService.findNdflPersonIncome(declarationData.id)
                logForDebug(SUCCESS_GET_TABLE, T_PERSON_INCOME_NAME, ndflPersonIncomeList.size())

                // Сведения о вычетах
                List<NdflPersonDeduction> ndflPersonDeductionList = ndflPersonService.findNdflPersonDeduction(declarationData.id)
                logForDebug(SUCCESS_GET_TABLE, T_PERSON_DEDUCTION_NAME, ndflPersonDeductionList.size())

                // Сведения о доходах в виде авансовых платежей
                List<NdflPersonPrepayment> ndflPersonPrepaymentList = ndflPersonService.findNdflPersonPrepayment(declarationData.id)
                logForDebug(SUCCESS_GET_TABLE, T_PERSON_PREPAYMENT_NAME, ndflPersonPrepaymentList.size())

                logForDebug("Получение записей из таблиц НФДЛ (" + (System.currentTimeMillis() - time) + " мс)")

                time = System.currentTimeMillis()

                ScriptUtils.checkInterrupted()

                logForDebug("Проверки на соответствие справочникам / Выгрузка Реестра физических лиц (" + (System.currentTimeMillis() - time) + " мс)")

                ScriptUtils.checkInterrupted()

                // Проверки на соответствие справочникам
                checkDataReference(ndflPersonList, ndflPersonIncomeList, ndflPersonDeductionList, ndflPersonPrepaymentList, personMap)

                ScriptUtils.checkInterrupted()

                // Общие проверки
                checkDataCommon(ndflPersonList, ndflPersonIncomeList, personMap)

                ScriptUtils.checkInterrupted()

                // Проверки сведений о доходах
                checkDataIncome(ndflPersonList, ndflPersonIncomeList, ndflPersonDeductionList, ndflPersonPrepaymentList, personMap)

                ScriptUtils.checkInterrupted()

                // Проверки Сведения о вычетах
                checkDataDeduction(ndflPersonList, ndflPersonIncomeList, ndflPersonDeductionList, personMap)

                ScriptUtils.checkInterrupted()

                // Проверки Сведения о доходах в виде авансовых платежей
                checkDataPrepayment(ndflPersonList, ndflPersonIncomeList, ndflPersonDeductionList, ndflPersonPrepaymentList, personMap)

                logForDebug("Все проверки (" + (System.currentTimeMillis() - time) + " мс)")
        }
    }

    void readChecksFatals() {
        section_2_15_fatal = declarationService.isCheckFatal(DeclarationCheckCode.RNU_SECTION_2_15, declarationData.declarationTemplateId)
        citizenship_fatal = declarationService.isCheckFatal(DeclarationCheckCode.RNU_CITIZENSHIP, declarationData.declarationTemplateId)
        valueCondition_fatal = declarationService.isCheckFatal(DeclarationCheckCode.RNU_VALUE_CONDITION, declarationData.declarationTemplateId)
        section_2_16Fatal = declarationService.isCheckFatal(DeclarationCheckCode.RNU_SECTION_2_16, declarationData.declarationTemplateId)
        section_2_21_fatal = declarationService.isCheckFatal(DeclarationCheckCode.RNU_SECTION_2_21, declarationData.declarationTemplateId)
        section_3_10_fatal = declarationService.isCheckFatal(DeclarationCheckCode.RNU_SECTION_3_10, declarationData.declarationTemplateId)
        section_3_16_fatal = declarationService.isCheckFatal(DeclarationCheckCode.RNU_SECTION_3_16, declarationData.declarationTemplateId)
    }

    /**
     * Получить актуальные на отчетную дату записи Реестра физических лиц
     * @return
     */
    Map<Long, RegistryPerson> getActualRefPersonsByDeclarationDataId(long declarationDataId) {
        List<RegistryPerson> persons = personService.findActualRefPersonsByDeclarationDataId(declarationDataId)
        Map<Long, RegistryPerson> result = new HashMap<>()
        for (RegistryPerson person : persons) {
            result.put(person.getRecordId(), person)
        }
        return result
    }

    void fillNdflPersonFLMap(List<NdflPerson> ndflPersonList, Map<Long, RegistryPerson> personMap) {
        for (def ndflPerson : ndflPersonList) {
            NdflPersonFL ndflPersonFL
            if (FORM_DATA_KIND == FormDataKind.PRIMARY) {
                // РНУ-НДФЛ первичная
                String fio = (ndflPerson.lastName ?: "") + " " + (ndflPerson.firstName ?: "") + " " + (ndflPerson.middleName ?: "")
                ndflPersonFL = new NdflPersonFL(fio, ndflPerson.inp ?: "")
            } else {
                // РНУ-НДФЛ консолидированная
                RegistryPerson personRecord = personMap.get(ndflPerson.recordId)
                String fio = (personRecord.lastName ?: "") + " " + (personRecord.firstName ?: "") + " " + (personRecord.middleName ?: "")
                ndflPersonFL = new NdflPersonFL(fio, ndflPerson.recordId.toString())
            }
            ndflPersonFLMap.put(ndflPerson.id, ndflPersonFL)
        }
    }

    /**
     * Проверка. Для ФЛ в разделе 2 есть только одна фиктивная строка
     */
    def checkDummyIncomes(List<NdflPersonIncome> incomes) {
        Map<Long, List<NdflPersonIncome>> incomesByPersonId = incomes.groupBy { NdflPersonIncome it -> it.ndflPersonId }
        for (def personId : incomesByPersonId.keySet()) {
            ScriptUtils.checkInterrupted()
            def incomesOfPerson = incomesByPersonId.get(personId)
            for (def income : incomesOfPerson) {
                NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(income.ndflPersonId)
                String fioAndInpAndOperId = sprintf(TEMPLATE_PERSON_FL_OPER, [ndflPersonFL.fio, ndflPersonFL.inp, income.operationId])
                String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [ndflPersonFL.fio, ndflPersonFL.inp])
                if (income.isDummy()) {
                    if (incomesOfPerson.size() > 1) {
                        String errMsg = String.format("У ФЛ: %s в Разделе 2 имеется более одной строки, несмотря на то, " +
                                "что текущая строка (для которой ставка налога = 0, ID операции = 0) показывает отсутствие операций по данному ФЛ",
                                fioAndInp
                        )
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, income.rowNum ?: "")
                        logger.errorExp("%s. %s.", "Для ФЛ в разделе 2 есть только одна фиктивная строка", fioAndInpAndOperId, pathError, errMsg)
                    }
                }
            }
        }
    }

    /**
     * Проверки на соответствие справочникам
     * @return
     */
    def checkDataReference(
            List<NdflPerson> ndflPersonList, List<NdflPersonIncome> ndflPersonIncomeList, List<NdflPersonDeduction> ndflPersonDeductionList,
            List<NdflPersonPrepayment> ndflPersonPrepaymentList, Map<Long, RegistryPerson> personMap) {

        long time = System.currentTimeMillis()
        // Страны
        Map<Long, String> citizenshipCodeMap = getRefCountryCode()
        logForDebug(SUCCESS_GET_REF_BOOK, R_CITIZENSHIP, citizenshipCodeMap.size())

        // Виды документов, удостоверяющих личность
        Map<Long, String> documentTypeMap = getRefDocumentTypeCode()
        logForDebug(SUCCESS_GET_REF_BOOK, R_ID_DOC_TYPE, documentTypeMap.size())

        // Статус налогоплательщика
        Map<Long, String> taxpayerStatusMap = getRefTaxpayerStatusCode()
        logForDebug(SUCCESS_GET_REF_BOOK, R_STATUS, taxpayerStatusMap.size())

        // Коды видов доходов Map<REF_BOOK_INCOME_TYPE.ID, REF_BOOK_INCOME_TYPE>
        Map<Long, Map<String, RefBookValue>> incomeCodeMap = getRefIncomeCode()
        logForDebug(SUCCESS_GET_REF_BOOK, R_INCOME_CODE, incomeCodeMap.size())

        // Виды доходов Map<REF_BOOK_INCOME_KIND.MARK, List<REF_BOOK_INCOME_KIND>>
        Map<String, List<Map<String, RefBookValue>>> incomeTypeMap = getRefIncomeType()
        logForDebug(SUCCESS_GET_REF_BOOK, R_INCOME_TYPE, incomeTypeMap.size())

        // Коды видов вычетов
        List<String> deductionTypeList = getRefDeductionType()
        logForDebug(SUCCESS_GET_REF_BOOK, R_TYPE_CODE, deductionTypeList.size())

        // Коды налоговых органов
        List<String> taxInspectionList = getRefNotifSource()
        logForDebug(SUCCESS_GET_REF_BOOK, R_NOTIF_SOURCE, taxInspectionList.size())

        logForDebug("Проверки на соответствие справочникам / Выгрузка справочников (" + (System.currentTimeMillis() - time) + " мс)")

        //long timeIsExistsAddress = 0
        time = System.currentTimeMillis()
        //в таком цикле не отображается номер строки при ошибках ndflPersonList.each { ndflPerson ->}
        for (NdflPerson ndflPerson : ndflPersonList) {

            ScriptUtils.checkInterrupted()

            NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPerson.id)
            String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [ndflPersonFL.fio, ndflPersonFL.inp])

            // Проверка отключена в рамках https://jira.aplana.com/browse/SBRFNDFL-7256
            /*long tIsExistsAddress = System.currentTimeMillis()


            if (!isPersonAddressEmpty(ndflPerson)) {
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                if (ndflPerson.postIndex != null && !ndflPerson.postIndex.matches("[0-9]{6}")) {
                    logFiasIndexError(fioAndInp, pathError, "Индекс", ndflPerson.postIndex)
                }
            }

            timeIsExistsAddress += System.currentTimeMillis() - tIsExistsAddress*/

            // Спр2 Гражданство (Обязательное поле)
            if (ndflPerson.citizenship != null && !citizenshipCodeMap.find { key, value -> value == ndflPerson.citizenship }) {
                //TODO turn_to_error
                String errMsg = String.format(LOG_TYPE_PERSON_MSG_2,
                        C_CITIZENSHIP, ndflPerson.citizenship ?: "",
                        R_CITIZENSHIP
                )
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                logger.logCheck("%s. %s.",
                        citizenship_fatal,
                        String.format(LOG_TYPE_REFERENCES, R_CITIZENSHIP), fioAndInp, pathError, errMsg)
            }

            // Спр3 Документ удостоверяющий личность.Код (Обязательное поле)
            if (ndflPerson.idDocType != null && !documentTypeMap.find { key, value -> value == ndflPerson.idDocType }) {
                //TODO turn_to_error
                String errMsg = String.format(LOG_TYPE_PERSON_MSG_2, "ДУЛ Код", ndflPerson.idDocType ?: "", R_ID_DOC_TYPE)
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_ID_DOC_TYPE), fioAndInp, pathError, errMsg)
            }

            // Спр4 Статус (Обязательное поле)
            if (ndflPerson.status != "0" && !taxpayerStatusMap.find { key, value -> value == ndflPerson.status }) {
                //TODO turn_to_error
                String errMsg = String.format(LOG_TYPE_PERSON_MSG_2,
                        C_STATUS, ndflPerson.status ?: "",
                        R_STATUS
                )
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_STATUS), fioAndInp, pathError, errMsg)
            }

            // Спр10 Наличие связи с "Физическое лицо"
            if (ndflPerson.personId == null || ndflPerson.personId == 0L) {
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                logger.errorExp("%s. %s.", "Не установлена ссылка на запись Реестра физических лиц", fioAndInp, pathError,
                        "Не установлена ссылка на запись Реестра физических лиц. Выполните операцию идентификации")
            } else {
                RegistryPerson personRecord = personMap.get(ndflPerson.recordId)

                if (!personRecord) {
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                    logger.errorExp("%s. %s.", " Отсутствует актуальная версия связанного ФЛ", fioAndInp, pathError,
                            "Для физического лица из (Реестра физических лиц) определенного по установленной ссылке " +
                                    "отсутствует актуальная на настоящий момент времени версия")
                } else {
                    // Спр11 Фамилия (Обязательное поле)
                    if (personRecord.lastName != null && !ndflPerson.lastName.toLowerCase().equals(personRecord.lastName.toLowerCase())) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "ФИО не соответствует Реестру физических лиц", fioAndInp, pathError,
                                String.format(LOG_TYPE_PERSON_MSG, "Фамилия", ndflPerson.lastName ?: ""))
                    }

                    // Спр11 Имя (Обязательное поле)
                    if (personRecord.firstName != null && !ndflPerson.firstName.toLowerCase().equals(personRecord.firstName.toLowerCase())) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "ФИО не соответствует Реестру физических лиц", fioAndInp, pathError,
                                String.format(LOG_TYPE_PERSON_MSG, "Имя", ndflPerson.firstName ?: ""))
                    }

                    // Спр11 Отчество (Необязательное поле)
                    if (personRecord.middleName != null && ndflPerson.middleName != null && !ndflPerson.middleName.toLowerCase().equals(personRecord.middleName.toLowerCase())) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "ФИО не соответствует Реестру физических лиц", fioAndInp, pathError,
                                String.format(LOG_TYPE_PERSON_MSG, "Отчество", ndflPerson.middleName ?: ""))
                    }

                    if (FORM_DATA_KIND.equals(FormDataKind.PRIMARY)) {
                        // Спр12 ИНП первичная (Обязательное поле)
                        if (!(ndflPerson.inp == personRecord.snils || personRecord.getPersonIdentityList().inp.contains(ndflPerson.inp))) {
                            String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                            logger.warnExp("%s. %s.", "ИНП не соответствует Реестру физических лиц", fioAndInp, pathError,
                                    String.format(LOG_TYPE_PERSON_MSG, "ИНП", ndflPerson.inp ?: ""))
                        }
                    } else {
                        //Спр12.1 ИНП консолидированная - проверка соответствия RECORD_ID
                        //if (formType == CONSOLIDATE){}
                        String recordId = String.valueOf(personRecord.recordId)
                        if (!ndflPerson.inp.equals(recordId)) {
                            //TODO turn_to_error
                            String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                            logger.warnExp("%s. %s.", "ИНП не соответствует Реестру физических лиц", fioAndInp, pathError,
                                    String.format(LOG_TYPE_PERSON_MSG, "ИНП", ndflPerson.inp ?: ""))
                        }
                    }
                    // Спр13 Дата рождения (Обязательное поле)
                    if (personRecord.birthDate != null && !personRecord.birthDate.equals(ndflPerson?.birthDay)) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "Дата рождения не соответствует Реестру физических лиц", fioAndInp, pathError,
                                String.format(LOG_TYPE_PERSON_MSG, "Дата рождения", ndflPerson.birthDay ? ScriptUtils.formatDate(ndflPerson.birthDay) : ""))
                    }

                    // Спр14 Гражданство (Обязательное поле)
                    if (ndflPerson.citizenship != null && !ndflPerson.citizenship.equals(personRecord.citizenship?.code)) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "Код гражданства не соответствует Реестру физических лиц", fioAndInp, pathError,
                                String.format(LOG_TYPE_PERSON_MSG, C_CITIZENSHIP, ndflPerson.citizenship ?: ""))
                    }

                    // Спр15 ИНН.В Российской федерации (Необязательное поле)
                    if (ndflPerson.innNp != null && !ndflPerson.innNp.equals(personRecord.inn)) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "ИНН в РФ не соответствует Реестру физических лиц", fioAndInp, pathError,
                                String.format(LOG_TYPE_PERSON_MSG, "ИНН в РФ", ndflPerson.innNp ?: ""))
                    }

                    // Спр16 ИНН.В стране гражданства (Необязательное поле)
                    if (ndflPerson.innForeign != null && !ndflPerson.innForeign.equals(personRecord.innForeign)) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "ИНН в ИНО не соответствует Реестру физических лиц", fioAndInp, pathError,
                                String.format(LOG_TYPE_PERSON_MSG, "ИНН в ИНО", ndflPerson.innForeign ?: ""))
                    }

                    if (FORM_DATA_KIND.equals(FormDataKind.PRIMARY)) {
                        // Спр17 Документ удостоверяющий личность (Первичная) (Обязательное поле)
                        if (ndflPerson.idDocType != null && !ndflPerson.idDocType.equals(personRecord.reportDoc?.docType?.code)) {
                            String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                            logger.warnExp("%s. %s.", "Код и номер ДУЛ не соответствуют Реестру физических лиц", fioAndInp, pathError,
                                    String.format(LOG_TYPE_PERSON_MSG, "ДУЛ Код", ndflPerson.idDocType ?: ""))
                        }
                        if (ndflPerson.idDocNumber != null && BaseWeightCalculator.prepareStringDul(personRecord.reportDoc?.documentNumber)?.toUpperCase() != BaseWeightCalculator.prepareStringDul(ndflPerson.idDocNumber).toUpperCase()) {
                            String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                            logger.warnExp("%s. %s.", "Код и номер ДУЛ не соответствуют Реестру физических лиц", fioAndInp, pathError,
                                    String.format(LOG_TYPE_PERSON_MSG, "ДУЛ Номер", ndflPerson.idDocNumber ?: ""))
                        }
                    } else {
                        if (ndflPerson.idDocType != null && !personRecord.documents.docType.code.contains(ndflPerson.idDocType)) {
                            String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                            logger.warnExp("%s. %s.", "Код и номер ДУЛ не соответствуют Реестру физических лиц", fioAndInp, pathError,
                                    String.format(LOG_TYPE_PERSON_MSG, "ДУЛ Код\" (\"${ndflPerson.idDocType ?: ""}\"), \"ДУЛ Номер", ndflPerson.idDocNumber ?: ""))
                        }
                        for (IdDoc idDoc : personRecord.documents) {
                            if (ndflPerson.idDocNumber != null && BaseWeightCalculator.prepareStringDul(idDoc.documentNumber) != BaseWeightCalculator.prepareStringDul(ndflPerson.idDocNumber).toUpperCase()) {
                                if (personRecord.reportDoc == null || personRecord.reportDoc.id == null) {
                                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                                    logger.warnExp("%s. %s.", "Код и номер ДУЛ не соответствуют Реестру физических лиц", fioAndInp, pathError,
                                            "\"ДУЛ Номер\" не включается в отчетность")
                                }
                            }
                        }
                    }

                    // Спр18 Статус налогоплательщика
                    if (ndflPerson.status != personRecord.taxPayerState?.code) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "Статус налогоплательщика не соответствует Реестру физических лиц", fioAndInp, pathError,
                                String.format(LOG_TYPE_PERSON_MSG, C_STATUS, ndflPerson.status ?: ""))
                    }
                }
            }
        }
        logForDebug("Проверки на соответствие справочникам / " + T_PERSON_NAME + " (" + (System.currentTimeMillis() - time) + " мс)")

        /*logForDebug("Проверки на соответствие справочникам / Проверка существования адреса (" + timeIsExistsAddress + " мс)")*/

        time = System.currentTimeMillis()
        for (NdflPersonIncome ndflPersonIncome : ndflPersonIncomeList) {
            if (!ndflPersonIncome.isDummy()) {
                ScriptUtils.checkInterrupted()

                NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPersonIncome.ndflPersonId)
                String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [ndflPersonFL.fio, ndflPersonFL.inp])

                // Спр5 Код вида дохода (Необязательное поле)
                if (ndflPersonIncome.incomeCode && ndflPersonIncome.incomeAccruedDate != null &&
                        !incomeCodeMap.find { key, value ->
                            value.CODE?.stringValue == ndflPersonIncome.incomeCode &&
                                    ndflPersonIncome.incomeAccruedDate >= value.record_version_from?.dateValue &&
                                    ndflPersonIncome.incomeAccruedDate <= value.record_version_to?.dateValue
                        }
                ) {
                    String errMsg = String.format(LOG_TYPE_PERSON_MSG_2,
                            C_INCOME_CODE, ndflPersonIncome.incomeCode ?: "",
                            R_INCOME_CODE
                    )
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                    logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_INCOME_CODE), fioAndInp, pathError, errMsg)
                }

                /*
                Спр6
                При проверке Вида дохода должно проверятся не только наличие признака дохода в справочнике, но и принадлежность признака к конкретному Коду вида дохода

                Доход.Вид.Признак (Графа 5) - (Необязательное поле)
                incomeTypeMap <REF_BOOK_INCOME_KIND.MARK, List<REF_BOOK_INCOME_KIND>>

                Доход.Вид.Код (Графа 4) - (Необязательное поле)
                incomeCodeMap <REF_BOOK_INCOME_TYPE.ID, REF_BOOK_INCOME_TYPE>
                 */
                if (ndflPersonIncome.incomeType && ndflPersonIncome.incomeCode) {
                    List<Map<String, RefBookValue>> incomeTypeRowList = incomeTypeMap.get(ndflPersonIncome.incomeType)
                    if (incomeTypeRowList == null || incomeTypeRowList.isEmpty()) {
                        String errMsg = String.format(LOG_TYPE_PERSON_MSG_2,
                                C_INCOME_TYPE, ndflPersonIncome.incomeType ?: "",
                                R_INCOME_TYPE
                        )
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                        logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_INCOME_TYPE), fioAndInp, pathError, errMsg)
                    } else {
                        if (ndflPersonIncome.incomeAccruedDate != null) {
                            List<Map<String, RefBookValue>> incomeCodeRefList = []
                            incomeTypeRowList.each { incomeTypeRow ->
                                if (ndflPersonIncome.incomeAccruedDate >= incomeTypeRow.record_version_from?.dateValue &&
                                        ndflPersonIncome.incomeAccruedDate <= incomeTypeRow.record_version_to?.dateValue) {
                                    RefBookValue refBookValue = incomeTypeRow?.INCOME_TYPE_ID
                                    def incomeCodeRef = incomeCodeMap.get((Long) refBookValue?.getValue())
                                    incomeCodeRefList.add(incomeCodeRef)
                                }
                            }
                            Map<String, RefBookValue> incomeCodeRef = incomeCodeRefList.find { Map<String, RefBookValue> value ->
                                value?.CODE?.stringValue == ndflPersonIncome.incomeCode
                            }
                            if (!incomeCodeRef) {
                                String errMsg = String.format("Значение гр. \"%s\" (\"%s\"), \"%s\" (\"%s\") отсутствует в справочнике \"%s\"",
                                        C_INCOME_CODE, ndflPersonIncome.incomeCode ?: "",
                                        C_INCOME_TYPE, ndflPersonIncome.incomeType ?: "",
                                        R_INCOME_TYPE
                                )
                                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                                logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_INCOME_TYPE), fioAndInp, pathError,
                                        errMsg)
                            }
                        }
                    }
                }
            }
        }
        logForDebug("Проверки на соответствие справочникам / '${T_PERSON_INCOME_NAME}' (" + (System.currentTimeMillis() - time) + " мс)")

        time = System.currentTimeMillis()
        for (NdflPersonDeduction ndflPersonDeduction : ndflPersonDeductionList) {

            ScriptUtils.checkInterrupted()

            NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPersonDeduction.ndflPersonId)
            String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [ndflPersonFL.fio, ndflPersonFL.inp])

            // Спр8 Код вычета (Обязательное поле)
            if (ndflPersonDeduction.typeCode != "000" && ndflPersonDeduction.typeCode != null && !deductionTypeList.contains(ndflPersonDeduction.typeCode)) {
                String errMsg = String.format(LOG_TYPE_PERSON_MSG_2,
                        C_TYPE_CODE, ndflPersonDeduction.typeCode ?: "",
                        R_TYPE_CODE
                )
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum ?: "")
                logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_TYPE_CODE), fioAndInp, pathError, errMsg)
            }

            // Спр9 Документ о праве на налоговый вычет.Код источника (Обязательное поле)
            if (ndflPersonDeduction.notifSource != null && !taxInspectionList.contains(ndflPersonDeduction.notifSource)) {
                //TODO turn_to_error
                String errMsg = String.format(LOG_TYPE_PERSON_MSG_2,
                        C_NOTIF_SOURCE, ndflPersonDeduction.notifSource ?: "",
                        R_NOTIF_SOURCE
                )
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum ?: "")
                logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_NOTIF_SOURCE), fioAndInp, pathError, errMsg)
            }
        }
        logForDebug("Проверки на соответствие справочникам / '${T_PERSON_DEDUCTION_NAME}' (" + (System.currentTimeMillis() - time) + " мс)")

        time = System.currentTimeMillis()
        for (NdflPersonPrepayment ndflPersonPrepayment : ndflPersonPrepaymentList) {

            ScriptUtils.checkInterrupted()

            NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPersonPrepayment.ndflPersonId)
            String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [ndflPersonFL.fio, ndflPersonFL.inp])

            // Спр9 Уведомление, подтверждающее право на уменьшение налога на фиксированные авансовые платежи.Код налогового органа, выдавшего уведомление (Обязательное поле)
            if (ndflPersonPrepayment.notifSource != null && !taxInspectionList.contains(ndflPersonPrepayment.notifSource)) {
                //TODO turn_to_error
                String errMsg = String.format(LOG_TYPE_PERSON_MSG_2,
                        P_NOTIF_SOURCE, ndflPersonPrepayment.notifSource ?: "",
                        R_NOTIF_SOURCE
                )
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_PREPAYMENT, ndflPersonPrepayment.rowNum ?: "")
                logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_NOTIF_SOURCE), fioAndInp, pathError, errMsg)
            }
        }
        logForDebug("Проверки на соответствие справочникам / '${T_PERSON_PREPAYMENT_NAME}' (" + (System.currentTimeMillis() - time) + " мс)")
    }

    /**
     * Общие проверки
     */
    def checkDataCommon(List<NdflPerson> ndflPersonList, List<NdflPersonIncome> ndflPersonIncomeList, Map<Long, RegistryPerson> personMap) {
        long time = System.currentTimeMillis()
        long timeTotal = time

        logForDebug("Общие проверки: инициализация (" + (System.currentTimeMillis() - time) + " мс)")

        time = System.currentTimeMillis()

        for (NdflPerson ndflPerson : ndflPersonList) {

            ScriptUtils.checkInterrupted()

            NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPerson.id)
            String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [ndflPersonFL.fio, ndflPersonFL.inp])

            // Общ1 Корректность ИНН
            if (ndflPerson.citizenship == "643") {
                if (ndflPerson.innNp == null) {
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                    logger.warnExp("%s. %s.", "\"ИНН\" не указан", fioAndInp, pathError,
                            "Значение гр. \"ИНН в РФ\" не указано.")
                }
            }

            //Общ2 Наличие обязательных реквизитов для формирования отчетности
            boolean checkLastName = checkRequiredAttribute(ndflPerson, fioAndInp, "lastName", "Фамилия")
            boolean checkFirstName = checkRequiredAttribute(ndflPerson, fioAndInp, "firstName", "Имя")
            checkRequiredAttribute(ndflPerson, fioAndInp, "birthDay", "Дата рождения")
            checkRequiredAttribute(ndflPerson, fioAndInp, "citizenship", C_CITIZENSHIP)
            boolean checkIdDocType = checkRequiredAttribute(ndflPerson, fioAndInp, "idDocType", "ДУЛ Код")
            boolean checkIdDocNumber = checkRequiredAttribute(ndflPerson, fioAndInp, "idDocNumber", "ДУЛ Номер")
            checkRequiredAttribute(ndflPerson, fioAndInp, "status", C_STATUS)

            if (checkLastName) {
                List<String> errorMessages = ScriptUtils.checkLastName(ndflPerson.lastName, ndflPerson.citizenship ?: "")
                if (!errorMessages.isEmpty()) {
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                    for (String message : errorMessages) {
                        logger.warnExp("%s. %s.", "\"Фамилия\", \"Имя\" не соответствует формату", fioAndInp, pathError, message)
                    }
                }
            }
            if (checkFirstName) {
                List<String> errorMessages = ScriptUtils.checkFirstName(ndflPerson.firstName, ndflPerson.citizenship ?: "")
                if (!errorMessages.isEmpty()) {
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                    for (String message : errorMessages) {
                        logger.warnExp("%s. %s.", "\"Фамилия\", \"Имя\" не соответствует формату", fioAndInp, pathError, message)
                    }
                }
            }

            if (checkIdDocType && checkIdDocNumber) {
                String checkDul = ScriptUtils.checkDul(ndflPerson.idDocType, ndflPerson.idDocNumber, "ДУЛ Номер")
                if (checkDul != null) {
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                    logger.warnExp("%s. %s.", "\"ДУЛ\" не соответствует формату", fioAndInp, pathError,
                            checkDul)
                }
            }

            // Общ11 СНИЛС (Необязательное поле)
            if (ndflPerson.snils != null && !ScriptUtils.checkSnils(ndflPerson.snils)) {
                String errMsg = String.format("Значение гр. \"%s\" (\"%s\") не соответствует формату",
                        "СНИЛС", ndflPerson.snils ?: ""
                )
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                logger.warnExp("%s. %s.", "\"СНИЛС\" не соответствует формату", fioAndInp, pathError,
                        errMsg)
            }
        }
        logForDebug("Общие проверки / '${T_PERSON_NAME}' (" + (System.currentTimeMillis() - time) + " мс)")

        time = System.currentTimeMillis()
        // Общ7 Наличие или отсутствие значения в графе в зависимости от условий
        List<ColumnFillConditionData> columnFillConditionDataList = []
        //1 Раздел 2. Графа 4 должна быть заполнена, если заполнена хотя бы одна из граф: "Раздел 2. Графа 10" ИЛИ "Раздел 2. Графа 11"
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column10Fill(),
                new Column4Fill(),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_INCOME_CODE,
                        C_INCOME_ACCRUED_SUMM
                )
        )
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column11Fill(),
                new Column4Fill(),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_INCOME_CODE,
                        C_INCOME_PAYOUT_SUMM
                )
        )
        //2 Раздел 2. Графа 5 должна быть заполнена, если заполнена хотя бы одна из граф: "Раздел 2. Графа 10" ИЛИ "Раздел 2. Графа 11"
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column10Fill(),
                new Column5Fill(),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_INCOME_TYPE,
                        C_INCOME_ACCRUED_SUMM
                )
        )
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column11Fill(),
                new Column5Fill(),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_INCOME_TYPE,
                        C_INCOME_PAYOUT_SUMM
                )
        )
        //3 Раздел 2. Графа 6 должна быть заполнена, если заполнена Раздел 2. Графа 10
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column10Fill(),
                new Column6Fill(),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_INCOME_ACCRUED_DATE,
                        C_INCOME_ACCRUED_SUMM
                )
        )
        //4 Раздел 2. Графа 7 должна быть заполнена, если заполнена Раздел 2. Графа 11
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column11Fill(),
                new Column7Fill(),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_INCOME_PAYOUT_DATE,
                        C_INCOME_PAYOUT_SUMM
                )
        )
        //5 Раздел 2. Графа 8 Должна быть всегда заполнена
        columnFillConditionDataList << new ColumnFillConditionData(
                new ColumnTrueFillOrNotFill(),
                new Column8Fill(),
                String.format("Не заполнена гр. \"%s\"",
                        C_OKTMO
                )
        )
        //6 Раздел 2. Графа 9 Должна быть всегда заполнена
        columnFillConditionDataList << new ColumnFillConditionData(
                new ColumnTrueFillOrNotFill(),
                new Column9Fill(),
                String.format("Не заполнена гр. \"%s\"",
                        C_KPP
                )
        )
        //7 Раздел 2. Графа 10 должна быть заполнена, если заполнена Раздел 2. Графа 6
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column6Fill(),
                new Column10Fill(),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_INCOME_ACCRUED_SUMM,
                        C_INCOME_ACCRUED_DATE
                )
        )
        //8 Раздел 2. Графа 11 должна быть заполнена, если заполнена Раздел 2. Графа 7
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column7Fill(),
                new Column11Fill(),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_INCOME_PAYOUT_SUMM,
                        C_INCOME_PAYOUT_DATE
                )
        )
        //9 Раздел 2. Графа 13 Должна быть заполнена, если заполнена Раздел 2. Графа 10.
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column10Fill(),
                new Column13Fill(),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_TAX_BASE,
                        C_INCOME_ACCRUED_SUMM
                )
        )
        //10 Раздел 2. Графы 14 Должна быть заполнена, если заполнена Раздел 2. Графа 10 ИЛИ Раздел 2. Графа 11.
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column10Fill(),
                new Column14Fill(),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_TAX_RATE, C_INCOME_ACCRUED_SUMM
                )
        )
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column11Fill(),
                new Column14Fill(),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_TAX_RATE, C_INCOME_PAYOUT_SUMM
                )
        )
        //11 Раздел 2. Графы 15 Должна быть заполнена, если заполнена хотя бы одна из граф: "Раздел 2. Графа 10" ИЛИ "Раздел 2. Графа 11"
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column10Fill(),
                new Column15Fill(),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_TAX_DATE, C_INCOME_ACCRUED_SUMM
                )
        )
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column11Fill(),
                new Column15Fill(),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_TAX_DATE, C_INCOME_PAYOUT_SUMM
                )
        )
        //12 Раздел 2. Графа 16 Должна быть заполнена, если заполнена "Раздел 2. Графа 10"
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column10Fill(),
                new Column16Fill(),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_CALCULATED_TAX, C_INCOME_ACCRUED_SUMM
                )
        )
        //13 Раздел 2. Графа 17 Должна быть заполнена, если заполнена "Раздел 2. Графа 11"
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column11Fill(),
                new Column17Fill(),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_WITHHOLDING_TAX, C_INCOME_PAYOUT_SUMM
                )
        )
        //14 Раздел 2. Графа 21 Должна быть заполнена, если выполняется одно из условий:
        // 1. заполнена "Раздел 2. Графа 7"
        // 2. одновременно заполнены "Раздел 2. Графа 22" И "Раздел 2. Графа 23" И "Раздел 2. Графа 24"
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column7Fill(),
                new Column21Fill(),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_TAX_TRANSFER_DATE, C_INCOME_PAYOUT_DATE
                )

        )
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column22And23And24Fill(),
                new Column21Fill(),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнены гр. \"%s\", \"%s\", \"%s\"",
                        C_TAX_TRANSFER_DATE,
                        C_PAYMENT_DATE,
                        C_PAYMENT_NUMBER,
                        C_TAX_SUMM
                )

        )
        //15 Должны быть либо заполнены все 3 Графы 22, 23, 24, либо ни одна из них
        columnFillConditionDataList << new ColumnFillConditionData(
                new ColumnTrueFillOrNotFill(),
                new Column22And23And24FillOrColumn22And23And24NotFill(),
                String.format("Гр. \"%s\", гр. \"%s\", гр. \"%s\" должны быть заполнены одновременно или не заполнена ни одна из них",
                        C_PAYMENT_DATE,
                        C_PAYMENT_NUMBER,
                        C_TAX_SUMM
                )

        )

        for (NdflPersonIncome ndflPersonIncome : ndflPersonIncomeList) {
            if (!ndflPersonIncome.isDummy()) {
                ScriptUtils.checkInterrupted()

                def operationId = ndflPersonIncome.operationId ?: ""
                NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPersonIncome.ndflPersonId)
                String fioAndInpAndOperId = sprintf(TEMPLATE_PERSON_FL_OPER, [ndflPersonFL.fio, ndflPersonFL.inp, operationId])

                // Общ5 Принадлежность дат операций к отчетному периоду. Проверка перенесана в событие загрузки ТФ

                // Общ7 Наличие или отсутствие значения в графе в зависимости от условий
                columnFillConditionDataList.each { columnFillConditionData ->
                    if (columnFillConditionData.columnConditionCheckerAsIs.check(ndflPersonIncome) &&
                            !columnFillConditionData.columnConditionCheckerToBe.check(ndflPersonIncome)) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                        logger.logCheck("%s. %s.",
                                valueCondition_fatal,
                                "Наличие (отсутствие) значения в графе не соответствует алгоритму заполнения РНУ НДФЛ",
                                fioAndInpAndOperId, pathError, columnFillConditionData.conditionMessage)
                    }
                }
            }
        }
        ScriptUtils.checkInterrupted()
        logForDebug("Общие проверки / " + T_PERSON_INCOME_NAME + " (" + (System.currentTimeMillis() - time) + " мс)")

        time = System.currentTimeMillis()
        // Общ16 Для ФЛ в разделе 2 есть только одна фиктивная строка
        checkDummyIncomes(ndflPersonIncomeList)
        logForDebug("Общие проверки / Фиктивные записи (" + (System.currentTimeMillis() - time) + " мс)")

        logForDebug("Общие проверки всего (" + (System.currentTimeMillis() - timeTotal) + " мс)")
    }

    /**
     * Проверки сведений о доходах
     * @param ndflPersonList
     * @param ndflPersonIncomeList
     * @param ndflPersonDeductionList
     * @param ndflPersonPrepaymentList
     */
    def checkDataIncome(List<NdflPerson> ndflPersonList, List<NdflPersonIncome> ndflPersonIncomeList, List<NdflPersonDeduction> ndflPersonDeductionList,
                        List<NdflPersonPrepayment> ndflPersonPrepaymentList, Map<Long, RegistryPerson> personMap) {

        long time = System.currentTimeMillis()

        Map<Long, NdflPerson> personsCache = [:]
        ndflPersonList.each { ndflPerson ->
            personsCache.put(ndflPerson.id, ndflPerson)
        }

        Map<Long, List<NdflPersonPrepayment>> ndflPersonPrepaymentCache = [:]
        ndflPersonPrepaymentList.each { NdflPersonPrepayment ndflPersonPrepayment ->
            List<NdflPersonPrepayment> ndflPersonPrepaymentListByPersonIdList = ndflPersonPrepaymentCache.get(ndflPersonPrepayment.ndflPersonId) ?: new ArrayList<NdflPersonPrepayment>()
            ndflPersonPrepaymentListByPersonIdList.add(ndflPersonPrepayment)
            ndflPersonPrepaymentCache.put(ndflPersonPrepayment.ndflPersonId, ndflPersonPrepaymentListByPersonIdList)
        }

        List<DateConditionData<IncomeAccruedDateConditionChecker>> dateConditionDataList = []
        List<DateConditionData<TaxTransferDateConditionChecker>> dateConditionDataListForBudget = []

        dateConditionWorkDay = new DateConditionWorkDay(calendarService)

        // 1. "Графа 6" = "Графе 7"
        dateConditionDataList << new DateConditionData(["1010", "1011", "1110", "1400", "1552", "2001", "2010", "2012", "2300", "2301",
                                                        "2640", "2641", "2710", "2760", "2762", "2770", "2800", "2900", "3020", "3023", "4800"],
                ["00"], new Column6EqualsColumn7(),
                "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"%s\" (\"%s\") для кода дохода и признака дохода, " +
                        "указанных в гр. \"%s\" (\"%s\") и гр. \"%s\" (\"%s\")")

        // 2. "Графа 6" = "Графе 7"
        dateConditionDataList << new DateConditionData(["1530", "1531", "1532", "1533", "1535", "1536", "1537", "1539",
                                                        "1541", "1542", "1551", "1552", "1553", "1554"],
                ["01", "02"], new Column6EqualsColumn7(),
                "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"%s\" (\"%s\") для кода дохода и признака дохода, " +
                        "указанных в гр. \"%s\" (\"%s\") и гр. \"%s\" (\"%s\")")

        // 3. "Графа 6" = "Графе 7"
        dateConditionDataList << new DateConditionData(["2002", "2003"], ["07"], new Column6EqualsColumn7(),
                "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"%s\" (\"%s\") для кода дохода и признака дохода, " +
                        "указанных в гр. \"%s\" (\"%s\") и гр. \"%s\" (\"%s\")")

        // 4. "Графа 6" = "Графе 7"
        dateConditionDataList << new DateConditionData(["2013", "2014", "4800", "2510", "2202", "2740", "2750", "2790", "2520"], ["13"], new Column6EqualsColumn7(),
                "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"%s\" (\"%s\") для кода дохода и признака дохода, " +
                        "указанных в гр. \"%s\" (\"%s\") и гр. \"%s\" (\"%s\")")

        // 5. "Графа 6" = "Графе 7"
        dateConditionDataList << new DateConditionData(["2520", "2720", "2740", "2750", "2790", "4800"], ["14"], new Column6EqualsColumn7(),
                "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"%s\" (\"%s\") для кода дохода и признака дохода, " +
                        "указанных в гр. \"%s\" (\"%s\") и гр. \"%s\" (\"%s\")")

        // 6. Соответствует последнему рабочему календарному дню года
        dateConditionDataList << new DateConditionData(["1530", "1531", "1532", "1533", "1535", "1536", "1537", "1539",
                                                        "1541", "1542", "1544", "1546", "1548", "1551", "1552", "1553", "1554"],
                ["04"], new LastYearWorkDay(),
                "Значение гр. \"%s\" (\"%s\") должно быть равно последнему рабочему дню года, за который был начислен доход (%4\$s), " +
                        "для Кода дохода = \"%6\$s\" и Признака дохода = \"%8\$s\"")

        // 7. Последний календарный день месяца
        dateConditionDataList << new DateConditionData(["2000", "2003"], ["05"], new LastMonthCalendarDay(),
                "Значение гр. \"%s\" (\"%s\") должно быть равно последнему календарному дню месяца, за который был начислен доход, " +
                        "для кода дохода и признака дохода, указанных в гр. \"%5\$s\" (\"%6\$s\") и гр. \"%7\$s\" (\"%8\$s\")")

        // 8. Последний календарный день месяца
        dateConditionDataList << new DateConditionData(["2000"], ["11"], new LastMonthCalendarDay(),
                "Значение гр. \"%s\" (\"%s\") должно быть равно последнему календарному дню месяца, за который был начислен доход, " +
                        "для кода дохода и признака дохода, указанных в гр. \"%5\$s\" (\"%6\$s\") и гр. \"%7\$s\" (\"%8\$s\")")

        // 9. Последний календарный день месяца
        dateConditionDataList << new DateConditionData(["2610"], ["00"], new LastMonthCalendarDay(),
                "Значение гр. \"%s\" (\"%s\") должно быть равно последнему календарному дню месяца, за который был начислен доход, " +
                        "для кода дохода и признака дохода, указанных в гр. \"%5\$s\" (\"%6\$s\") и гр. \"%7\$s\" (\"%8\$s\")")

        // 1,2 "Графа 21" = "Графа 7" + "1 рабочий день"
        dateConditionDataListForBudget << new DateConditionData(["1010", "1011", "3020", "3023",
                                                                 "1110", "1400", "2001", "2010", "2301", "2710", "2760",
                                                                 "2762", "2770", "2900", "4800"], ["00"],
                new Column21EqualsColumn7Plus1WorkingDay(), null)

        // 3 "Графа 21" = "Графа 7" + 1 рабочий день
        dateConditionDataListForBudget << new DateConditionData(["2000"], ["05", "06", "11", "12"],
                new Column21EqualsColumn7Plus1WorkingDay(), null)

        // 4 "Графа 21" = "Графа 7" + 1 рабочий день
        dateConditionDataListForBudget << new DateConditionData(["2002"], ["07", "08", "09", "10"],
                new Column21EqualsColumn7Plus1WorkingDay(), null)

        // 5 "Графа 21" = "Графа 7" + 1 рабочий день
        dateConditionDataListForBudget << new DateConditionData(["2003"], ["05", "06", "07", "08", "09", "10"],
                new Column21EqualsColumn7Plus1WorkingDay(), null)

        // 6 "Графа 21" = "Графа 7" + "1 рабочий день"
        dateConditionDataListForBudget << new DateConditionData(["2520", "2740", "2750", "2790", "4800", "2013", "2014",
                                                                 "2510", "2202", "2740", "2750", "2790", "2520"], ["13"],
                new Column21EqualsColumn7Plus1WorkingDay(), null)

        // 7,8,9 "Графа 21" = "Графа 7" + "1 рабочий день"
        dateConditionDataListForBudget << new DateConditionData(["2610", "2611", "2640", "2641", "2800"], ["00"],
                new Column21EqualsColumn7Plus1WorkingDay(), null)

        // 10 "Графа 21" <= "Графа 7" + "30 календарных дней"
        dateConditionDataListForBudget << new DateConditionData(["1530", "1531", "1532", "1533", "1535", "1536", "1537", "1539",
                                                                 "1541", "1542", "1551", "1552", "1553", "1554"], ["01", "03"],
                new Column21EqualsColumn7Plus30WorkingDays(), null)

        // 11 "Графа 21" ≤ "Графа 7" + "30 календарных дней"
        dateConditionDataListForBudget << new DateConditionData(["1530", "1531", "1532", "1533", "1535", "1536", "1537", "1539",
                                                                 "1541", "1542", "1551", "1552", "1553", "1554"], ["02"],
                new Column21EqualsColumn7Plus30WorkingDays(), null)

        // 12 "Графа 21" = Последний календарный день месяца для месяца "Графы 7", если Последний календарный день месяца - выходной день, то "Графа 21" = следующий рабочий день
        dateConditionDataListForBudget << new DateConditionData(["2012", "2300"], ["00"],
                new Column21EqualsColumn7LastDayOfMonth(), null)

        // 13
        dateConditionDataListForBudget << new DateConditionData(["2720", "2740", "2750", "2790", "4800"], ["14"],
                new Column21ForNaturalIncome(), null)

        //14 "Графа 21" = последний календарный день первого месяца года, следующего за годом, указанным в "Графа 7"
        dateConditionDataListForBudget << new DateConditionData([], ["04"],
                new Column21EqualsLastDayOfFirstMonthOfNextYear(), null)

        // Сгруппируем Сведения о доходах на основании принадлежности к плательщику
        Map<Long, List<NdflPersonIncome>> incomesByPersonId = ndflPersonIncomeList.groupBy { it.ndflPersonId }
        Map<Long, Map<String, List<NdflPersonDeduction>>> deductionsByPersonIdAndOperationId =
                ndflPersonDeductionList.groupBy({ NdflPersonDeduction it -> it.ndflPersonId }, { NdflPersonDeduction it -> it.operationId })
        Map<Long, Map<String, List<NdflPersonPrepayment>>> prepaymentsByPersonIdAndOperationId =
                ndflPersonPrepaymentList.groupBy({ NdflPersonPrepayment it -> it.ndflPersonId }, { NdflPersonPrepayment it -> it.operationId })

        Map<String, List<NdflPersonIncome>> incomesByPersonIdForCol16Sec2Check = null

        // Операции по которым уже сформировано сообщение ошибки при проверке Раздела 2 Графы 16 случай "Графа 14" ≠ "13"
        Set<String> operationsCol16Sec2TaxRateNot13 = []

        incomesByPersonId.each { ndflPersonId, allIncomesOfPerson ->
            ScriptUtils.checkInterrupted()

            NdflPerson ndflPerson = personsCache.get(ndflPersonId)
            NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPersonId)
            Collection<List<NdflPersonIncome>> personOperations = allIncomesOfPerson.groupBy {
                new Pair<String, Long>(it.operationId, it.asnuId)
            }.values()
            for (List<NdflPersonIncome> allIncomesOfOperation : personOperations) {
                def operationId = allIncomesOfOperation.first().operationId
                List<NdflPersonDeduction> allDeductionsOfOperation = deductionsByPersonIdAndOperationId.get(ndflPersonId)?.get(operationId) ?: new ArrayList<NdflPersonDeduction>()
                List<NdflPersonPrepayment> allPrepaymentsOfOperation = prepaymentsByPersonIdAndOperationId.get(ndflPersonId)?.get(operationId) ?: new ArrayList<NdflPersonPrepayment>()
                String fioAndInpAndOperId = sprintf(TEMPLATE_PERSON_FL_OPER, [ndflPersonFL.fio, ndflPersonFL.inp, operationId])
                String rowNums = allIncomesOfOperation?.rowNum?.sort()?.join(", ") ?: ""
                // содержат суммы всех строк операции, если ни в одной строке значение не заполнено, то сумма равна null
                NdflPersonIncome totalOperationIncome = sumIncomes(allIncomesOfOperation)
                NdflPersonDeduction totalOperationDeduction = sumDeductions(allDeductionsOfOperation)
                /**
                 * Проверки по операциям
                 */
                if (!isDummy(allIncomesOfOperation)) {
                    // СведДох2 Сумма вычета (Графа 12)
                    BigDecimal incomesAccruedSum = totalOperationIncome.incomeAccruedSumm ?: 0
                    BigDecimal incomesDeductionsSum = totalOperationIncome.totalDeductionsSumm ?: 0
                    BigDecimal deductionsSum = totalOperationDeduction.periodCurrSumm ?: 0
                    if (incomesAccruedSum && incomesDeductionsSum && signOf(incomesAccruedSum) != signOf(incomesDeductionsSum)) {
                        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                        String errMsg = String.format("Для строк операции с \"ID операции\"=\"%s\" сумма значений гр. \"Сумма вычета\" (\"%s\") и сумма значений гр. " +
                                "\"Сумма начисленного дохода\" (\"%s\") должны иметь одинаковый знак",
                                operationId, incomesDeductionsSum, incomesAccruedSum)
                        String pathError = String.format(SECTION_LINES_MSG, T_PERSON_INCOME, rowNums)
                        logger.warnExp("%s. %s.", LOG_TYPE_2_12, fioAndInpAndOperId, pathError, errMsg)
                    }
                    if (incomesAccruedSum.abs() < incomesDeductionsSum.abs()) {
                        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                        String errMsg = String.format("Для строк операции с \"ID операции\"=\"%s\" Модуль суммы значений гр. \"Сумма вычета\" (\"%s\") должен быть меньше " +
                                "или равен модулю суммы значений гр. \"Сумма начисленного дохода\" (\"%s\")",
                                operationId, incomesDeductionsSum, incomesAccruedSum)
                        String pathError = String.format(SECTION_LINES_MSG, T_PERSON_INCOME, rowNums)
                        logger.warnExp("%s. %s.", LOG_TYPE_2_12, fioAndInpAndOperId, pathError, errMsg)
                    }
                    if (incomesDeductionsSum != deductionsSum) {
                        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                        String errMsg = String.format("Для строк операции с \"ID операции\"=\"%s\" сумма значений гр. \"Сумма вычета\" " +
                                "Раздела 2 (\"%s\") должна быть равна сумме значений гр. \"Вычет. Текущий период. Сумма\" Раздела 3 (\"%s\")",
                                operationId, incomesDeductionsSum, deductionsSum)
                        String pathError = String.format(SECTION_LINES_MSG, T_PERSON_INCOME, rowNums)
                        logger.warnExp("%s. %s.", LOG_TYPE_2_12, fioAndInpAndOperId, pathError, errMsg)
                    }

                    // СведДох7 Заполнение Раздела 2 Графы 18 и 19
                    BigDecimal notHoldingTax = totalOperationIncome.notHoldingTax ?: 0
                    BigDecimal overholdingTax = totalOperationIncome.overholdingTax ?: 0
                    BigDecimal calculatedTax = totalOperationIncome.calculatedTax ?: 0
                    BigDecimal withholdingTax = totalOperationIncome.withholdingTax ?: 0
                    if (notHoldingTax - overholdingTax != calculatedTax - withholdingTax) {
                        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                        String errMsg = "Для строк операции с \"ID операции\"=\"$operationId\" разность сумм значений гр. \"НДФЛ не удержанный\" " +
                                "(\"$notHoldingTax\") и гр. \"НДФЛ излишне удержанный\" (\"$overholdingTax\") " +
                                "должна быть равна разности сумм значений гр.\"НДФЛ исчисленный\" (\"$calculatedTax\") и " +
                                "гр.\"НДФЛ удержанный\" (\"$withholdingTax\") по всем строкам одной операции"
                        String pathError = String.format(SECTION_LINES_MSG, T_PERSON_INCOME, rowNums)
                        logger.warnExp("%s. %s.", LOG_TYPE_2_18_19, fioAndInpAndOperId, pathError, errMsg)
                    }
                }

                /**
                 * Проверки по строкам
                 */
                for (NdflPersonIncome ndflPersonIncome : allIncomesOfOperation) {
                    // СведДох1 Доход.Дата.Начисление (Графа 6)
                    if (!ndflPersonIncome.isDummy() && ndflPersonIncome.incomeAccruedSumm != null) {
                        dateConditionDataList.each { dateConditionData ->
                            if (dateConditionData.incomeCodes.contains(ndflPersonIncome.incomeCode) && dateConditionData.incomeTypes.contains(ndflPersonIncome.incomeType)) {
                                boolean check = dateConditionData.checker.check(ndflPersonIncome, allIncomesOfOperation)
                                if (!check) {
                                    // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                                    String errMsg = String.format(dateConditionData.conditionMessage,
                                            C_INCOME_ACCRUED_DATE, formatDate(ndflPersonIncome.incomeAccruedDate),
                                            C_INCOME_PAYOUT_DATE, formatDate(dateConditionData.checker.getDateCompared(ndflPersonIncome)),
                                            C_INCOME_CODE, ndflPersonIncome.incomeCode,
                                            C_INCOME_TYPE, ndflPersonIncome.incomeType
                                    )
                                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                                    logger.warnExp("%s. %s.", LOG_TYPE_2_6, fioAndInpAndOperId, pathError, errMsg)
                                }
                            }
                        }
                    }

                    // Заполнение Раздела 2 Графы 13
                    if (ndflPersonIncome.incomeAccruedDate && ndflPersonIncome.taxBase != (ndflPersonIncome.incomeAccruedSumm ?: 0) - (ndflPersonIncome.totalDeductionsSumm ?: 0)) {
                        String errMsg = "Значение гр. \"Налоговая База\" \"$ndflPersonIncome.taxBase\" не совпадает с расчетным " +
                                "\"${(ndflPersonIncome.incomeAccruedSumm ?: 0) - (ndflPersonIncome.totalDeductionsSumm ?: 0)}\""
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                        logger.warnExp("%s. %s.", "\"Налоговая база\" указана некорректно", fioAndInpAndOperId, pathError, errMsg)
                    }

                    // СведДох3 НДФЛ.Процентная ставка (Графа 14)
                    if (!ndflPersonIncome.isDummy() && ndflPersonIncome.taxRate != null) {
                        boolean checkNdflPersonIncomingTaxRateTotal = false

                        boolean presentCitizenship = ndflPerson.citizenship != null
                        boolean presentIncomeCode = ndflPersonIncome.incomeCode != null
                        boolean presentStatus = ndflPerson.status != null
                        boolean presentTaxRate = ndflPersonIncome.taxRate != null
                        def ndflPersonIncomingTaxRates = []
                        CHECK_NDFL_PERSON_INCOMING_TAX_RATE_13:
                        {
                            if (presentCitizenship && presentIncomeCode && presentStatus && presentTaxRate) {
                                Boolean conditionA = ndflPerson.citizenship == "643" && ndflPersonIncome.incomeCode != "1010" && ndflPerson.status != "2"
                                Boolean conditionB = ndflPerson.citizenship == "643" && ["1010", "1011"].contains(ndflPersonIncome.incomeCode) && ndflPerson.status == "1"
                                Boolean conditionC = ndflPerson.citizenship != "643" && ["2000", "2001", "2010"].contains(ndflPersonIncome.incomeCode) && ndflPerson.status ? Integer.parseInt(ndflPerson.status) : 0 >= 3
                                if (conditionA || conditionB || conditionC) {
                                    if (ndflPersonIncome.taxRate == 13) {
                                        checkNdflPersonIncomingTaxRateTotal = true
                                    } else {
                                        ndflPersonIncomingTaxRates << "\"13\""
                                    }
                                }
                            }
                        }
                        CHECK_NDFL_PERSON_INCOMING_TAX_RATE_15:
                        {
                            if ((presentIncomeCode && presentStatus && presentTaxRate) && (ndflPersonIncome.incomeCode == "1010" && ndflPerson.status != "1")) {
                                if (ndflPersonIncome.taxRate == 15) {
                                    checkNdflPersonIncomingTaxRateTotal = true
                                } else {
                                    ndflPersonIncomingTaxRates << "\"15\""
                                }
                            }
                        }
                        CHECK_NDFL_PERSON_INCOMING_TAX_RATE_35:
                        {
                            if ((presentIncomeCode && presentStatus && presentTaxRate) && (["2740", "3020", "2610", "3023"].contains(ndflPersonIncome.incomeCode) && ndflPerson.status != "2")) {
                                if (ndflPersonIncome.taxRate == 35) {
                                    checkNdflPersonIncomingTaxRateTotal = true
                                } else {
                                    ndflPersonIncomingTaxRates << "\"35\""
                                }
                            }
                        }
                        CHECK_NDFL_PERSON_INCOMING_TAX_RATE_30:
                        {
                            if (presentIncomeCode && presentStatus && presentTaxRate) {
                                def conditionA = Integer.parseInt(ndflPerson.status) >= 2 && ndflPersonIncome.incomeCode != "1010"
                                def conditionB = Integer.parseInt(ndflPerson.status) > 2 && !["2000", "2001", "2010"].contains(ndflPersonIncome.incomeCode)
                                if (conditionA || conditionB) {
                                    if (ndflPersonIncome.taxRate == 30) {
                                        checkNdflPersonIncomingTaxRateTotal = true
                                    } else {
                                        ndflPersonIncomingTaxRates << "\"30\""
                                    }
                                }
                            }
                        }
                        CHECK_NDFL_PERSON_INCOMING_TAX_RATE_9:
                        {
                            if ((presentCitizenship && presentIncomeCode && presentStatus && presentTaxRate) && (ndflPerson.citizenship == "643" && ndflPersonIncome.incomeCode == "1110" && ndflPerson.status == "1")) {
                                if (ndflPersonIncome.taxRate == 9) {
                                    checkNdflPersonIncomingTaxRateTotal = true
                                } else {
                                    ndflPersonIncomingTaxRates << "\"9\""
                                }
                            }
                        }
                        CHECK_NDFL_PERSON_INCOMING_TAX_RATE_OTHER:
                        {
                            if ((presentCitizenship && presentIncomeCode && presentStatus && presentTaxRate) && (ndflPerson.citizenship != "643" && ndflPersonIncome.incomeCode == "1010" && ndflPerson.status != "1")) {
                                if (![13, 15, 35, 30, 9].contains(ndflPersonIncome.taxRate)) {
                                    checkNdflPersonIncomingTaxRateTotal = true
                                } else {
                                    ndflPersonIncomingTaxRates << "\"Специальная ставка\""
                                }
                            }
                        }
                        if (!checkNdflPersonIncomingTaxRateTotal && !ndflPersonIncomingTaxRates.isEmpty()) {
                            // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                            String errMsg = String.format(LOG_TYPE_2_14_MSG, "Процентная ставка (%)", ndflPersonIncome.taxRate,
                                    ndflPersonIncome.incomeCode, ndflPerson.status,
                                    ndflPersonIncomingTaxRates.join(", ")
                            )
                            String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                            logger.warnExp("%s. %s.", LOG_TYPE_2_14, fioAndInpAndOperId, pathError, errMsg)
                        }
                    }

                    // СведДох4 НДФЛ.Расчет.Дата (Графа 15)
                    if (!ndflPersonIncome.isDummy() && ndflPersonIncome.taxDate != null) {
                        List<CheckData> logTypeMessagePairList = []
                        // П.1
                        if (ndflPersonIncome.calculatedTax >= 0 && ndflPersonIncome.incomeAccruedDate &&
                                ndflPersonIncome.incomeAccruedDate >= reportPeriod.calendarStartDate &&
                                ndflPersonIncome.incomeAccruedDate <= reportPeriod.endDate) {
                            // «Графа 15 Раздел 2» = «Графа 6 Раздел 2»
                            if (ndflPersonIncome.taxDate != ndflPersonIncome.incomeAccruedDate) {
                                logTypeMessagePairList.add(new CheckData("\"Дата исчисленного налога\" указана некорректно",
                                        ("Значение гр. \"${C_TAX_DATE}\" (\"${formatDate(ndflPersonIncome.taxDate)}\") не равно значению гр. " +
                                                "\"${C_INCOME_ACCRUED_DATE}\" (\"${formatDate(ndflPersonIncome.incomeAccruedDate)}\"). " +
                                                "Даты могут не совпадать, если это строка корректировки начисления!").toString(),
                                        section_2_15_fatal))
                            }
                        }
                        // П.2
                        // У проверяемой строки "Графы 16" < "0"
                        if (ndflPersonIncome.calculatedTax != null && ndflPersonIncome.calculatedTax < 0 && ndflPersonIncome.totalDeductionsSumm != null) {
                            // Существует хотя бы одна строка, по которой выполняются условия:
                            // 1. "Раздел 3.Графа 2" = "Раздел 2. Графа 2" проверяемой строки
                            // 2. "Раздел 3. Графа 9" = "Раздел 2. Графа 3" проверяемой строки
                            // 3. "Раздел 3. Графа 15" = "Раздел 2. Графа 15" проверяемой строки
                            def isDeductionExists = allDeductionsOfOperation.find {
                                it.periodCurrDate == ndflPersonIncome.taxDate
                            } != null
                            if (!isDeductionExists) {
                                logTypeMessagePairList.add(new CheckData("\"Дата исчисленного налога\" указана некорректно",
                                        ("Значение гр. \"${C_TAX_DATE}\" (\"${formatDate(ndflPersonIncome.taxDate)}\") отсутствует в гр. " +
                                                "\"${C_PERIOD_CURR_DATE}\" хотя бы одной строки операции Раздела 3.").toString(),
                                        section_2_15_fatal))
                            }
                        }
                        // П.3
                        // В проверяемой строке: "Графа 19" НЕ заполнена и "Графа 7" заполнена
                        if (ndflPersonIncome.overholdingTax == null && ndflPersonIncome.incomePayoutDate != null) {
                            def countPositiveCol17 = allIncomesOfOperation.count {
                                it.withholdingTax != null && it.withholdingTax > 0
                            }
                            // По операции («Графа 3»), указанной в проверяемой строке, существует только одна строка, у которой "Графа 17" > "0"
                            if (countPositiveCol17 == 1) {
                                // «Графа 15 Раздел 2» = «Графа 7 Раздел 2»
                                if (ndflPersonIncome.taxDate != ndflPersonIncome.incomePayoutDate) {
                                    logTypeMessagePairList.add(new CheckData("\"Дата удержанного налога\" указана некорректно",
                                            ("Значение гр. \"${C_TAX_DATE}\" (\"${formatDate(ndflPersonIncome.taxDate)}\") должно быть равно " +
                                                    "значению гр. \"${C_INCOME_PAYOUT_DATE}\" (\"${formatDate(ndflPersonIncome.incomePayoutDate)}\").").toString(),
                                            section_2_15_fatal))
                                }
                            }
                        }
                        // П.4
                        // Одновременно выполняются условия :
                        // 1. "Графа 18" > 0
                        // 2. "Графа 7" заполнена
                        // 3. "Графа 4" не равна {1530, 1531, 1532, 1533, 1535, 1536, 1537, 1539, 1541, 1542, 1551, 1552, 1553, 1554}
                        if (ndflPersonIncome.notHoldingTax != null && ndflPersonIncome.notHoldingTax > 0 &&
                                ndflPersonIncome.incomePayoutDate != null &&
                                ndflPersonIncome.incomeCode != null &&
                                !(ndflPersonIncome.incomeCode in ["1530", "1531", "1532", "1533", "1535", "1536", "1537", "1539", "1541", "1542", "1551", "1552", "1553", "1554"])
                        ) {
                            // "Графа 15" = "Графа 7"
                            if (ndflPersonIncome.taxDate != ndflPersonIncome.incomePayoutDate) {
                                logTypeMessagePairList.add(new CheckData("\"Дата не удержанного налога\" указана некорректно",
                                        ("Значение гр. \"${C_TAX_DATE}\" (\"${formatDate(ndflPersonIncome.taxDate)}\") должно быть равно " +
                                                "значению гр. \"${C_INCOME_PAYOUT_DATE}\" (\"${formatDate(ndflPersonIncome.incomePayoutDate)}\").").toString(),
                                        section_2_15_fatal))
                            }
                        }
                        // П.5
                        // Для проверяемой строки выполняются все условия:
                        //  - "Графа 18" > 0
                        //  - "Графа 6" заполнена
                        //  - "Графа 4" = {1530, 1531, 1532, 1533, 1535, 1536, 1537, 1539, 1541, 1542, 1551, 1552, 1553, 1554}
                        if (ndflPersonIncome.notHoldingTax != null && ndflPersonIncome.notHoldingTax > 0 &&
                                ndflPersonIncome.incomeAccruedDate != null &&
                                ndflPersonIncome.incomeCode in ["1530", "1531", "1532", "1533", "1535", "1536", "1537", "1539", "1541", "1542", "1551", "1552", "1553", "1554"]
                        ) {
                            // Существует строка, по которой выполняются условия:
                            //    - Заполнена "Графа 7"
                            //    - "Графа 2" = "Графа 2" проверяемой строки
                            //    - "Графа 3" = "Графа 3" проверяемой строки
                            //    - "Графа 4" = "Графа 4" проверяемой строки
                            //    - Если найдено несколько строк, то брать одну строку, у которой значение "Граф 7" является максимальной.
                            //      При этом если найдено несколько строк с одинаковыми значениями максимальной даты, то брать строку, созданную первой
                            def foundIncomeMaxCol7 = allIncomesOfOperation.findAll {
                                it.incomePayoutDate != null && it.incomeCode == ndflPersonIncome.incomeCode
                            }.max { it.incomePayoutDate }
                            // Для найденной в предыдущем пункте строки «Графа 7» принадлежит отчетному периоду
                            if (foundIncomeMaxCol7 != null && dateRelateToCurrentPeriod(foundIncomeMaxCol7.incomePayoutDate)) {
                                // "Графа 15" = "Графа 6"
                                if (ndflPersonIncome.taxDate != ndflPersonIncome.incomeAccruedDate) {
                                    logTypeMessagePairList.add(new CheckData("\"Дата не удержанного налога\" указана некорректно",
                                            ("Значение гр. \"${C_TAX_DATE}\" (\"${formatDate(ndflPersonIncome.taxDate)}\") должно быть равно " +
                                                    "значению гр. \"${C_INCOME_ACCRUED_DATE}\" (\"${formatDate(ndflPersonIncome.incomeAccruedDate)}\").").toString(),
                                            section_2_15_fatal))
                                }
                            }
                        }
                        // П.6
                        // Одновременно выполняются условия :
                        // 1. "Графа 18" > 0
                        // 2. "Графа 4" равна {1530, 1531, 1532, 1533, 1535, 1536, 1537, 1539, 1541, 1542, 1551, 1552, 1553, 1554}
                        // 3. "Графа 6" НЕ принадлежит отчетному периоду
                        if (ndflPersonIncome.notHoldingTax != null && ndflPersonIncome.notHoldingTax > 0 &&
                                ndflPersonIncome.incomeCode in ["1530", "1531", "1532", "1533", "1535", "1536", "1537", "1539", "1541", "1542", "1551", "1552", "1553", "1554"] &&
                                ndflPersonIncome.incomeAccruedDate != null && !dateRelateToCurrentPeriod(ndflPersonIncome.incomeAccruedDate)
                        ) {
                            // "Графа 15" соответствует маске 31.12.20**
                            if (ndflPersonIncome.taxDate != null) {
                                Calendar calendarPayout = Calendar.getInstance()
                                calendarPayout.setTime(ndflPersonIncome.taxDate)
                                int dayOfMonth = calendarPayout.get(Calendar.DAY_OF_MONTH)
                                int month = calendarPayout.get(Calendar.MONTH)
                                if (!(dayOfMonth == 31 && month == 12)) {
                                    logTypeMessagePairList.add(new CheckData("\"Дата не удержаннного налога\" указана некорректно",
                                            ("Значение гр. \"${C_TAX_DATE}\" (\"${formatDate(ndflPersonIncome.taxDate)}\") должно быть равно последнему календарному дню года налогового периода.").toString()))
                                }
                            }
                        }
                        // П.7
                        // Одновременно выполняются условия:
                        // 1. "Графа 19" > 0
                        // 2. "Графа 7" заполнена
                        if (ndflPersonIncome.overholdingTax != null && ndflPersonIncome.overholdingTax > 0 &&
                                ndflPersonIncome.incomePayoutDate != null) {
                            // "Графа 15" = "Графа 7"
                            if (ndflPersonIncome.taxDate != ndflPersonIncome.incomePayoutDate) {
                                logTypeMessagePairList.add(new CheckData("\"Дата излишне удержанного налога\" указана некорректно",
                                        ("Значение гр. \"${C_TAX_DATE}\" (\"${ndflPersonIncome.taxDate ? ScriptUtils.formatDate(ndflPersonIncome.taxDate) : ""}\") " +
                                                "должно быть равно значению гр. \"${C_INCOME_PAYOUT_DATE}\" " +
                                                "(\"${ndflPersonIncome.incomePayoutDate ? ScriptUtils.formatDate(ndflPersonIncome.incomePayoutDate) : ""}\").").toString(), section_2_15_fatal))
                            }
                        }
                        // П.8
                        // Проверка не должна выполняться.
                        /*if (ndflPersonIncome.refoundTax != null && ndflPersonIncome.overholdingTax != null && withholdingTaxPresented && calculatedTaxPresented && (ndflPersonIncome.refoundTax ?: 0 > 0) &&
                                (ndflPersonIncome.withholdingTax ?: 0) > (ndflPersonIncome.calculatedTax ?: 0) &&
                                (ndflPersonIncome.overholdingTax ?: 0) &&
                                ndflPersonIncome.incomeCode != "0" && ndflPersonIncome.incomeCode != null) {
                            // «Графа 15 Раздел 2» = «Графа 7 Раздел 2»
                            if (ndflPersonIncome.taxDate != null && ndflPersonIncome.incomePayoutDate != null && ndflPersonIncome.taxDate != ndflPersonIncome.incomePayoutDate) {
                                checkTaxDate = false
                                logTypeMessagePairList.add(new CheckData("\"Дата расчета возвращенного налогоплательщику налога\" указана некорректно", ("Значение гр. \"${C_TAX_DATE}\" (\"${ndflPersonIncome.taxDate ? ScriptUtils.formatDate(ndflPersonIncome.taxDate) : ""}\") должно быть равно значению гр. \"${C_INCOME_PAYOUT_DATE}\" (\"${ndflPersonIncome.incomePayoutDate ? ScriptUtils.formatDate(ndflPersonIncome.incomePayoutDate) : ""}\")").toString(), section_2_15_fatal))
                            }
                        }*/
                        if (!logTypeMessagePairList.isEmpty()) {
                            String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                            for (CheckData checkData : logTypeMessagePairList) {
                                logger.logCheck("%s. %s", checkData.fatal, checkData.msgFirst, fioAndInpAndOperId, pathError, checkData.msgLast)
                            }
                        }
                    }

                    // СведДох5 НДФЛ.Расчет.Сумма.Исчисленный (Заполнение Раздела 2 Графы 16)
                    if (!ndflPersonIncome.isDummy() && ndflPersonIncome.calculatedTax != null) {
                        if (ndflPersonIncome.taxRate != 13) {
                            if (!operationsCol16Sec2TaxRateNot13.contains(ndflPersonIncome.operationId)) {
                                // условие | ∑ Р.2.Гр.16 - ∑ ОКРУГЛ(Р.2.Гр.13 x Р.2.Гр.14/100) – ∑ Р.4.Гр.4 | < 1
                                // ∑ Р.2.Гр.16
                                BigDecimal var1 = (BigDecimal) allIncomesOfOperation.sum { NdflPersonIncome income -> income.calculatedTax ?: 0 }
                                // ∑ ОКРУГЛ(Р.2.Гр.13 x Р.2.Гр.14/100)
                                BigDecimal var2 = (BigDecimal) allIncomesOfOperation.sum { NdflPersonIncome income ->
                                    income.calculatedTax != null ? ScriptUtils.round((income.taxBase ?: 0) * (income.taxRate ?: 0) / 100) : 0
                                }
                                // ∑ Р.4.Гр.4
                                BigDecimal var3 = (BigDecimal) allPrepaymentsOfOperation?.sum { NdflPersonPrepayment prepayment -> prepayment.summ ?: 0 } ?: 0
                                BigDecimal ВычисленноеЗначениеНалога = var2 - var3
                                if (!((var1 - ВычисленноеЗначениеНалога).abs() < 1)) {
                                    operationsCol16Sec2TaxRateNot13 << ndflPersonIncome.operationId
                                    String errMsg = String.format("Для строк операции с \"ID операции\"=\"%s\" значение налога исчисленного в гр. 16 (%s р) не совпадает с расчетным (%s р)",
                                            operationId, var1, ВычисленноеЗначениеНалога
                                    )
                                    String pathError = String.format(SECTION_LINES_MSG, T_PERSON_INCOME, rowNums)
                                    logger.logCheck("%s. %s.",
                                            section_2_16Fatal,
                                            LOG_TYPE_2_16, fioAndInpAndOperId, pathError, errMsg)
                                }
                            }
                        } else {
                            if (ndflPersonIncome.incomeCode == "1010") {
                                // условие | Р.2.Гр.16 - ОКРУГЛ (Р.2.Гр.13 x Р.2.Гр.14/100) | < 1
                                // ОКРУГЛ (Р.2.Гр.13 x Р.2.Гр.14/100)
                                BigDecimal ВычисленноеЗначениеНалога = ScriptUtils.round(((ndflPersonIncome.taxBase ?: 0) * (ndflPersonIncome.taxRate ?: 0)) / 100, 0)
                                if (!((ndflPersonIncome.calculatedTax - ВычисленноеЗначениеНалога).abs() < 1)) {
                                    String errMsg = String.format("Значение налога исчисленного в гр. 16 (%s р) не совпадает с расчетным (%s р)",
                                            ndflPersonIncome.calculatedTax, ВычисленноеЗначениеНалога
                                    )
                                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                                    logger.logCheck("%s. %s.",
                                            section_2_16Fatal,
                                            LOG_TYPE_2_16, fioAndInpAndOperId, pathError, errMsg)
                                }
                            } else {
                                def groupKey = { NdflPersonIncome income -> personsCache.get(income.ndflPersonId).inp + "_" + income.kpp + "_" + income.oktmo }
                                // Группы сортировки, вычисляем 1 раз для всех строк
                                if (incomesByPersonIdForCol16Sec2Check == null) {
                                    // отбираем все строки формы с заполненной графой 16 и у которых одновременно: 1) Р.2.Гр.14 = «13». 2) Р.2.Гр.4 ≠ «1010».
                                    // затем группирует по инп, кпп и октмо
                                    incomesByPersonIdForCol16Sec2Check = ndflPersonIncomeList.findAll {
                                        it.calculatedTax != null && it.taxRate == 13 && it.incomeCode != "1010"
                                    }.groupBy {
                                        groupKey(it)
                                    }
                                    // сортируем внутри каждой группы
                                    incomesByPersonIdForCol16Sec2Check.each { k, v ->
                                        v.sort(true, { NdflPersonIncome a, NdflPersonIncome b ->
                                            a.incomeAccruedDate <=> b.incomeAccruedDate ?: a.taxDate <=> b.taxDate ?: a.incomeCode <=> b.incomeCode ?:
                                                    a.incomeType <=> b.incomeType ?: a.operationId <=> b.operationId
                                        })
                                    }
                                }
                                def groupIncomes = incomesByPersonIdForCol16Sec2Check.get(groupKey(ndflPersonIncome))
                                def groupPrepayments = ndflPersonPrepaymentList.findAll {
                                    it.operationId in groupIncomes.operationId
                                }
                                BigDecimal АвансовыеПлатежиПоГруппе = (BigDecimal) groupPrepayments.sum { NdflPersonPrepayment prepayment -> prepayment.summ ?: 0 } ?: 0
                                BigDecimal taxBaseSum = (BigDecimal) groupIncomes.sum { NdflPersonIncome income ->
                                    income.calculatedTax != null && income.taxBase ? income.taxBase : 0
                                } ?: 0
                                BigDecimal calculatedTaxSum = (BigDecimal) groupIncomes.sum { NdflPersonIncome income -> income.calculatedTax ?: 0 } ?: 0
                                BigDecimal ОбщаяДельта = (ScriptUtils.round(taxBaseSum * 13 / 100) - АвансовыеПлатежиПоГруппе - calculatedTaxSum)
                                        .abs()

                                if (ОбщаяДельта >= 1) {
                                    // "S1" = ∑ Р.2.Гр.13 с первой строки группы сортировки по проверяемую строку включительно.
                                    // Суммируются только строки, для которых значение Р.2.Гр.10 не пустое.
                                    BigDecimal s1 = 0
                                    for (def income : groupIncomes) {
                                        s1 += income.taxBase ?: 0
                                        if (income == ndflPersonIncome) {
                                            break
                                        }
                                    }
                                    // ∑ Р.2.Гр.16 с первой строки группы сортировки до проверяемой строки (проверяемая строка не включается).
                                    // Суммируются только строки, для которых значение Р.2.Гр.16 не пустое.
                                    BigDecimal s2 = 0
                                    for (def income : groupIncomes) {
                                        if (income == ndflPersonIncome) {
                                            break
                                        }
                                        s2 += income.calculatedTax ?: 0
                                    }
                                    // "S3" = ∑ Р.4.Гр.4 по операции, указанной в проверяемой строке
                                    BigDecimal s3 = new BigDecimal("0")
                                    List<String> operationIdList = []
                                    for (NdflPersonIncome income : groupIncomes) {
                                        operationIdList << income.operationId
                                        if (income == ndflPersonIncome) {
                                            break
                                        }
                                    }
                                    for (NdflPersonPrepayment ndflPersonPrepayment : ndflPersonPrepaymentList) {
                                        NdflPerson incomePerson = personsCache.get(ndflPersonIncome.getNdflPersonId())
                                        NdflPerson prepaymentPerson = personsCache.get(ndflPersonPrepayment.getNdflPersonId())
                                        if (incomePerson.inp == prepaymentPerson.inp && operationIdList.contains(ndflPersonPrepayment.operationId)) {
                                            s3 = s3.add(ndflPersonPrepayment.summ)
                                        }
                                    }

                                    // ОКРУГЛ (S1 x Р.2.Гр.14 / 100)
                                    BigDecimal var1 = ScriptUtils.round(s1 * (ndflPersonIncome.taxRate ?: 0) / 100)
                                    // где ВычисленноеЗначениеНалога = ОКРУГЛ (S1 x Р.2.Гр.14 / 100) - S2 - S3
                                    BigDecimal ВычисленноеЗначениеНалога = var1 - s2 - s3

                                    // Для ПНФ: | Р.2.Гр.16 – ВычисленноеЗначениеНалога | <= 1
                                    if (!((ndflPersonIncome.calculatedTax - ВычисленноеЗначениеНалога).abs() <= 1)) {
                                        String errMsg = String.format("Значение налога исчисленного в гр. 16 (%s р) не совпадает с расчетным (%s р)",
                                                ndflPersonIncome.calculatedTax, ВычисленноеЗначениеНалога)
                                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                                        logger.logCheck("%s. %s.",
                                                section_2_16Fatal,
                                                LOG_TYPE_2_16, fioAndInpAndOperId, pathError, errMsg)
                                    }
                                }
                            }
                        }
                    }

                    /* todo Убрал проверку Заполнения Раздела 2 Графы 17, в SBRFNDFL-3997 её доработают
                    // СведДох6 НДФЛ.Расчет.Сумма.Удержанный (Графа 17)
                    if (!ndflPersonIncome.isDummy() && ndflPersonIncome.withholdingTax != null && ndflPersonIncome.withholdingTax != 0) {
                        // СведДох7.1
                        if ((["2520", "2720", "2740", "2750", "2790", "4800"].contains(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeType == "13")
                                || (["1530", "1531", "1532", "1533", "1535", "1536", "1537", "1539", "1541", "1542", "1543", "1544",
                                     "1545", "1546", "1547", "1548", "1549", "1551", "1552", "1554"].contains(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeType == "02")
                                && (ndflPersonIncome.overholdingTax == null || ndflPersonIncome.overholdingTax == 0)
                        ) {
                            // «Графа 17 Раздел 2» = «Графа 16 Раздел 2» = «Графа 24 Раздел 2»
                            if (!(ndflPersonIncome.withholdingTax == ndflPersonIncome.calculatedTax
                                    && ndflPersonIncome.withholdingTax == ndflPersonIncome.taxSumm ?: 0)) {
                                String errMsg = String.format("Значение гр. \"%s\" (\"%s\") должно быть равно значениям гр. \"%s\" (\"%s\") и гр. \"%s\" (\"%s\")",
                                        C_WITHHOLDING_TAX, ndflPersonIncome.withholdingTax ?: 0,
                                        C_CALCULATED_TAX, ndflPersonIncome.calculatedTax ?: 0,
                                        C_TAX_SUMM, ndflPersonIncome.taxSumm ?: 0
                                )
                                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                                logger.logCheck("%s. %s.",
                                        declarationService.isCheckFatal(DeclarationCheckCode.RNU_SECTION_2_17, declarationData.declarationTemplateId),
                                        LOG_TYPE_2_17, fioAndInpAndOperId, pathError, errMsg)
                            }
                        } else if (((["2520", "2720", "2740", "2750", "2790", "4800"].contains(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeType == "13")
                                || (["1530", "1531", "1532", "1533", "1535", "1536", "1537", "1539", "1541", "1542", "1543", "1544",
                                     "1545", "1546", "1547", "1548", "1549", "1551", "1552", "1554"].contains(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeType != "02"))
                                && ndflPersonIncome.overholdingTax > 0
                        ) {
                            // «Графа 17 Раздел 2» = («Графа 16 Раздел 2» + «Графа 16 Раздел 2» предыдущей записи) = «Графа 24 Раздел 2» и «Графа 17 Раздел 2» <= ((«Графа 13 Раздел 2» - «Графа 16 Раздел 2») × 50%)
                            List<NdflPersonIncome> ndflPersonIncomeCurrentList = ndflPersonIncomeCache.get(ndflPersonIncome.ndflPersonId) ?: new ArrayList<NdflPersonIncome>()
                            NdflPersonIncome ndflPersonIncomePreview = null
                            if (!ndflPersonIncomeCurrentList.isEmpty()) {
                                for (NdflPersonIncome ndflPersonIncomeCurrent in ndflPersonIncomeCurrentList) {
                                    if (ndflPersonIncomeCurrent.incomeAccruedDate <= ndflPersonIncome.incomeAccruedDate &&
                                            (ndflPersonIncomePreview == null || ndflPersonIncomePreview.incomeAccruedDate < ndflPersonIncomeCurrent.incomeAccruedDate)) {
                                        ndflPersonIncomePreview = ndflPersonIncomeCurrent
                                    }
                                }
                            }
                            if (!(ndflPersonIncome.withholdingTax == (ndflPersonIncome.calculatedTax ?: 0) + (ndflPersonIncomePreview.calculatedTax ?: 0))) {
                                String errMsg = String.format("Значение гр. \"%s\" (\"%s\") должно быть равно сумме значений гр. \"%s\" (\"%s\") и гр. \"%s\" (\"%s\") предыдущей записи",
                                        C_WITHHOLDING_TAX, ndflPersonIncome.withholdingTax ?: 0,
                                        C_CALCULATED_TAX, ndflPersonIncome.calculatedTax ?: 0,
                                        C_CALCULATED_TAX, ndflPersonIncomePreview.calculatedTax ?: 0
                                )
                                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                                logger.logCheck("%s. %s.",
                                        declarationService.isCheckFatal(DeclarationCheckCode.RNU_SECTION_2_17, declarationData.declarationTemplateId),
                                        LOG_TYPE_2_17, fioAndInpAndOperId, pathError, errMsg)
                            }
                            if (!(ndflPersonIncome.withholdingTax == ndflPersonIncome.taxSumm ?: 0)) {
                                String errMsg = String.format("Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"%s\" (\"%s\")",
                                        C_WITHHOLDING_TAX, ndflPersonIncome.withholdingTax ?: 0,
                                        C_TAX_SUMM, ndflPersonIncome.taxSumm ?: 0
                                )
                                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                                logger.logCheck("%s. %s.",
                                        declarationService.isCheckFatal(DeclarationCheckCode.RNU_SECTION_2_17, declarationData.declarationTemplateId),
                                        LOG_TYPE_2_17, fioAndInpAndOperId, pathError, errMsg)
                            }
                            if (!(ndflPersonIncome.withholdingTax <= (ScriptUtils.round(ndflPersonIncome.taxBase ?: 0, 0) - ndflPersonIncome.calculatedTax ?: 0) * 0.50)) {
                                String errMsg = String.format("Значение гр. \"%s\" (\"%s\") не должно превышать 50%% от разности значение гр. \"%s\" (\"%s\") и гр. \"%s\" (\"%s\")",
                                        C_WITHHOLDING_TAX, ndflPersonIncome.withholdingTax ?: 0,
                                        C_TAX_BASE, ndflPersonIncome.taxBase ?: 0,
                                        C_CALCULATED_TAX, ndflPersonIncome.calculatedTax ?: 0
                                )
                                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                                logger.logCheck("%s. %s.",
                                        declarationService.isCheckFatal(DeclarationCheckCode.RNU_SECTION_2_17, declarationData.declarationTemplateId),
                                        LOG_TYPE_2_17, fioAndInpAndOperId, pathError, errMsg)
                            }
                        } else if ((["2520", "2720", "2740", "2750", "2790", "4800"].contains(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeType == "14")
                                || (["1530", "1531", "1532", "1533", "1535", "1536", "1537", "1539", "1541", "1542", "1544", "1545",
                                     "1546", "1547", "1548", "1549", "1551", "1552", "1554"].contains(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeType == "02")
                        ) {
                            if (!(ndflPersonIncome.withholdingTax == 0 || ndflPersonIncome.withholdingTax == null)) {
                                String errMsg = String.format("Значение гр. \"%s\" (\"%s\") должно быть равно \"0\"",
                                        C_WITHHOLDING_TAX, ndflPersonIncome.withholdingTax ?: 0
                                )
                                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                                logger.logCheck("%s. %s.",
                                        declarationService.isCheckFatal(DeclarationCheckCode.RNU_SECTION_2_17, declarationData.declarationTemplateId),
                                        LOG_TYPE_2_17, fioAndInpAndOperId, pathError, errMsg)
                            }
                        } else if (!(ndflPersonIncome.incomeCode != null)) {
                            if (!(ndflPersonIncome.withholdingTax != ndflPersonIncome.taxSumm ?: 0)) {
                                String errMsg = String.format("Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"%s\" (\"%s\")",
                                        C_WITHHOLDING_TAX, ndflPersonIncome.withholdingTax ?: 0,
                                        C_TAX_SUMM, ndflPersonIncome.taxSumm ?: 0
                                )
                                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                                logger.logCheck("%s. %s.",
                                        declarationService.isCheckFatal(DeclarationCheckCode.RNU_SECTION_2_17, declarationData.declarationTemplateId),
                                        LOG_TYPE_2_17, fioAndInpAndOperId, pathError, errMsg)
                            }
                        }
                    }*/

                    // СведДох9 НДФЛ.Расчет.Сумма.Возвращенный налогоплательщику (Графа 20)
                    // Проверка не должна выполняться.
                    /*if (!ndflPersonIncome.isDummy() && ndflPersonIncome.refoundTax != null && ndflPersonIncome.refoundTax > 0) {
                        if (!(refoundTaxSum <= overholdingTaxSum)) {
                            String errMsg = String.format("Сумма значений гр. \"%s\" (\"%s\") не должна превышать сумму значений гр.\"%s\" (\"%s\") для всех строк одной операции",
                                    C_REFOUND_TAX, refoundTaxSum ?: "0",
                                    C_OVERHOLDING_TAX, overholdingTaxSum ?: "0"
                            )
                            String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                            logger.errorExp("%s. %s.", LOG_TYPE_2_20, fioAndInpAndOperId, pathError, errMsg)
                        }
                    }*/

                    // СведДох10 НДФЛ.Перечисление в бюджет.Срок (Графа 21)
                    if (!ndflPersonIncome.isDummy() && ndflPersonIncome.incomePayoutDate != null && ndflPersonIncome.taxTransferDate != null) {
                        dateConditionDataListForBudget.each { dateConditionData ->
                            if ((dateConditionData.incomeCodes.contains(ndflPersonIncome.incomeCode) || dateConditionData.incomeCodes.isEmpty()) &&
                                    dateConditionData.incomeTypes.contains(ndflPersonIncome.incomeType)) {
                                def checkedIncome = ndflPersonIncome
                                String errMsg = dateConditionData.checker.check(checkedIncome)
                                if (checkedIncome != null && errMsg != null) {
                                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, checkedIncome.rowNum ?: "")
                                    logger.logCheck("%s. %s.",
                                            section_2_21_fatal,
                                            LOG_TYPE_2_21, fioAndInpAndOperId, pathError, errMsg)
                                }
                            }
                        }
                    }
                }
            }
        }

        logForDebug("Проверки сведений о доходах (" + (System.currentTimeMillis() - time) + " мс)")
    }

    /**
     * Формирует суммарную строку дохода по нескольким строкам. Если ни в одной строке значение не заполнено, то сумма равна null
     */
    NdflPersonIncome sumIncomes(List<NdflPersonIncome> incomes) {
        def totalIncome = new NdflPersonIncome()
        for (def income : incomes) {
            if (income.totalDeductionsSumm != null) {
                totalIncome.totalDeductionsSumm = (totalIncome.totalDeductionsSumm ?: new BigDecimal(0)) + income.totalDeductionsSumm
            }
            if (income.incomePayoutSumm != null) {
                totalIncome.incomePayoutSumm = (totalIncome.incomePayoutSumm ?: new BigDecimal(0)) + income.incomePayoutSumm
            }
            if (income.incomeAccruedSumm != null) {
                totalIncome.incomeAccruedSumm = (totalIncome.incomeAccruedSumm ?: new BigDecimal(0)) + income.incomeAccruedSumm
            }
            if (income.refoundTax != null) {
                totalIncome.refoundTax = (totalIncome.refoundTax ?: 0L) + income.refoundTax
            }
            if (income.calculatedTax != null) {
                totalIncome.calculatedTax = (totalIncome.calculatedTax ?: new BigDecimal(0)) + income.calculatedTax
            }
            if (income.withholdingTax != null) {
                totalIncome.withholdingTax = (totalIncome.withholdingTax ?: new BigDecimal(0)) + income.withholdingTax
            }
            if (income.overholdingTax != null) {
                totalIncome.overholdingTax = (totalIncome.overholdingTax ?: new BigDecimal(0)) + income.overholdingTax
            }
            if (income.notHoldingTax != null) {
                totalIncome.notHoldingTax = (totalIncome.notHoldingTax ?: new BigDecimal(0)) + income.notHoldingTax
            }
            if (income.taxSumm != null) {
                totalIncome.taxSumm = (totalIncome.taxSumm ?: new BigDecimal(0)) + income.taxSumm
            }
            if (income.taxBase != null) {
                totalIncome.taxBase = (totalIncome.taxBase ?: new BigDecimal(0)) + income.taxBase
            }
        }
        return totalIncome
    }

    /**
     * Формирует суммарную строку дохода по нескольким строкам. Если ни в одной строке значение не заполнено, то сумма равна null
     */
    NdflPersonDeduction sumDeductions(List<NdflPersonDeduction> deductions) {
        def totalDeduction = new NdflPersonDeduction()
        for (def deduction : deductions) {
            if (deduction.incomeSumm != null) {
                totalDeduction.incomeSumm = (totalDeduction.incomeSumm ?: new BigDecimal(0)) + deduction.incomeSumm
            }
            if (deduction.notifSumm != null) {
                totalDeduction.notifSumm = (totalDeduction.notifSumm ?: new BigDecimal(0)) + deduction.notifSumm
            }
            if (deduction.periodCurrSumm != null) {
                totalDeduction.periodCurrSumm = (totalDeduction.periodCurrSumm ?: new BigDecimal(0)) + deduction.periodCurrSumm
            }
            if (deduction.periodPrevSumm != null) {
                totalDeduction.periodPrevSumm = (totalDeduction.periodPrevSumm ?: new BigDecimal(0)) + deduction.periodPrevSumm
            }
        }
        return totalDeduction
    }

    /**
     * Проверки Сведения о вычетах
     */
    def checkDataDeduction(List<NdflPerson> ndflPersonList, List<NdflPersonIncome> ndflPersonIncomeList,
                           List<NdflPersonDeduction> ndflPersonDeductionList, Map<Long, RegistryPerson> personMap) {

        long time = System.currentTimeMillis()

        Map<String, Map<String, NdflPersonIncome>> mapNdflPersonIncome = [:]
        for (NdflPersonIncome ndflPersonIncome : ndflPersonIncomeList) {
            String operationIdNdflPersonId = "${ndflPersonIncome.operationId}_${ndflPersonIncome.ndflPersonId}"
            if (!mapNdflPersonIncome.containsKey(operationIdNdflPersonId)) {
                mapNdflPersonIncome.put(operationIdNdflPersonId, new LinkedHashMap<String, NdflPersonIncome>())
            }
            mapNdflPersonIncome.get(operationIdNdflPersonId).put(ndflPersonIncome.incomeAccruedDate ? ScriptUtils.formatDate(ndflPersonIncome.incomeAccruedDate) : "", ndflPersonIncome)
        }

        Map<Long, Map<String, List<NdflPersonIncome>>> incomesByPersonIdAndOperationId =
                ndflPersonIncomeList.groupBy({ NdflPersonIncome it -> it.ndflPersonId }, { NdflPersonIncome it -> it.operationId })

        def col16CheckDeductionGroups_1 = ndflPersonDeductionList.findAll {
            it.notifType == "2"
        }.groupBy {
            new Col16CheckDeductionGroup_1Key(it.ndflPersonId, it.operationId, it.notifDate, it.notifNum, it.notifSource, it.notifSumm)
        }

        def col16CheckDeductionGroups_2 = ndflPersonDeductionList.findAll {
            it.notifType == "1"
        }.groupBy { new Col16CheckDeductionGroup_2Key(it.ndflPersonId, it.operationId) }

        for (NdflPersonDeduction ndflPersonDeduction : ndflPersonDeductionList) {
            ScriptUtils.checkInterrupted()

            def operationId = ndflPersonDeduction.operationId
            def allIncomesOfOperation = incomesByPersonIdAndOperationId.get(ndflPersonDeduction.ndflPersonId)?.get(operationId) ?: new ArrayList<NdflPersonIncome>()

            NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPersonDeduction.ndflPersonId)
            String fioAndInpAndOperId = sprintf(TEMPLATE_PERSON_FL_OPER, [ndflPersonFL.fio, ndflPersonFL.inp, operationId])

            // Выч0 Строка Раздела 3 не относится к операции с фиктивной строкой
            for (def income : allIncomesOfOperation) {
                if (income.isDummy()) {
                    String errMsg = "относится к операции, для которой в Разделе 2 имеется строка $income.rowNum (ФЛ: $ndflPersonFL.fio, " +
                            "ИНП: $ndflPersonFL.inp, ставка налога = 0, ID операции = 0), показывающая отсутствие операций по данному ФЛ"
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum ?: "")
                    logger.errorExp("%s %s", "", fioAndInpAndOperId, pathError, errMsg)
                    break
                }
            }
            // Выч1 Документ о праве на налоговый вычет.Код источника (Графа 7)
            if (ndflPersonDeduction.notifType == "1" && ndflPersonDeduction.notifSource != "0000") {
                // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                String errMsg = String.format("Значение гр. \"%s\" (\"%s\") не соответствует значению гр. \"%s\" (\"%s\")",
                        C_NOTIF_SOURCE, ndflPersonDeduction.notifSource ?: "",
                        C_TYPE_CODE, ndflPersonDeduction.typeCode ?: ""
                )
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum ?: "")
                logger.warnExp("%s. %s.", LOG_TYPE_3_7, fioAndInpAndOperId, pathError, errMsg)
            }

            // Выч2 Начисленный доход.Дата (Графы 10)
            // Если одновременно заполнены графы проверяемой строки Раздела 3 "Графа 10","Графа 11","Графа 12", "Графа 15"
            if (ndflPersonDeduction.incomeAccrued != null && ndflPersonDeduction.incomeCode != null &&
                    ndflPersonDeduction.incomeSumm != null && ndflPersonDeduction.periodCurrDate != null) {
                // Существует строка Раздела 2, для которой одновременно выполняются условия:
                // - "Раздел 2. Графа 2" = "Раздел 3. Графа 2" проверяемой строки (ИНП)
                // - "Раздел 2. Графа 3" = "Раздел 3. Графа 9" проверяемой строки (ID операции)
                // - "Раздел 2. Графа 6" = "Раздел 3. Графа 10" проверяемой строки (Дата начисления дохода)
                // - "Раздел 2. Графа 4" = "Раздел 3. Графа 11" проверяемой строки (Код дохода)
                // - "Раздел 2. Графа 10" = "Раздел 3. Графа 12" проверяемой строки (Сумма дохода)
                // - "Раздел 2. Графа 15" = "Раздел 3. Графа 15" проверяемой строки (Дата применения вычета)
                // - "Раздел 2. Графа 12" заполнена
                // Если выполняется хотя бы одно из условий:
                // КодАСНУ не равна ни одному из значений: 1000; 1001; 1002
                // Дата, указанная в "Раздел 3.Графа 15" не является последним календарным днём месяца
                List<NdflPersonIncome> incomeExists = allIncomesOfOperation.findAll {NdflPersonIncome income ->
                    String asnuCode = getRefAsnu().get(income.asnuId).code

                    income.incomeAccruedDate == ndflPersonDeduction.incomeAccrued && income.incomeCode == ndflPersonDeduction.incomeCode &&
                            income.incomeAccruedSumm == ndflPersonDeduction.incomeSumm && income.totalDeductionsSumm != null &&
                            (!["1000", "1001", "1002"].contains(asnuCode) || !isLastMonthDay(ndflPersonDeduction.getPeriodCurrDate()))
                }

                if (!incomeExists.isEmpty() && incomeExists.find {NdflPersonIncome income ->
                    income.taxDate == ndflPersonDeduction.periodCurrDate
                } == null) {
                    String errMsg = "В разделе 2 отсутствует соответствующая строка начисления, содержащая информацию о вычете, с параметрами " +
                            "\"ID операции\": $ndflPersonDeduction.operationId, \"Дата начисления\": ${formatDate(ndflPersonDeduction.incomeAccrued)}, " +
                            "\"Код дохода\": $ndflPersonDeduction.incomeCode, \"Сумма начисленного дохода\": $ndflPersonDeduction.incomeSumm, " +
                            "\"Дата НДФЛ\": ${formatDate(ndflPersonDeduction.periodCurrDate)}"
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum ?: "")
                    logger.logCheck("%s. %s.",
                            section_3_10_fatal,
                            LOG_TYPE_3_10, fioAndInpAndOperId, pathError, errMsg)
                }
            }

            // Выч3 Применение вычета.Текущий период.Дата (Графы 15)
            if (ndflPersonDeduction.periodCurrDate != null) {
                // "Графа 15" принадлежит к отчетному периоду
                if (!dateRelateToCurrentPeriod(ndflPersonDeduction.periodCurrDate)) {
                    def departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
                    String strCorrPeriod = ""
                    if (departmentReportPeriod.getCorrectionDate() != null) {
                        strCorrPeriod = ", с датой сдачи корректировки " + departmentReportPeriod.getCorrectionDate().format("dd.MM.yyyy")
                    }
                    String errMsg = String.format("Значение гр. \"%s\" (\"%s\")\" не входит в отчетный период налоговой формы \"%s\"",
                            C_PERIOD_CURR_DATE, formatDate(ndflPersonDeduction.periodCurrDate),
                            departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear() + ", " + departmentReportPeriod.getReportPeriod().getName() + strCorrPeriod)
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum ?: "")
                    logger.logCheck("%s. %s.",
                            section_2_15_fatal,
                            LOG_TYPE_3_15, fioAndInpAndOperId, pathError, errMsg)
                }
            }

            // Выч6 Применение вычета.Текущий период.Сумма (Графы 16)
            if (ndflPersonDeduction.notifType == "2") {
                def key = new Col16CheckDeductionGroup_1Key(ndflPersonDeduction.ndflPersonId, ndflPersonDeduction.operationId, ndflPersonDeduction.notifDate,
                        ndflPersonDeduction.notifNum, ndflPersonDeduction.notifSource, ndflPersonDeduction.notifSumm)
                List<NdflPersonDeduction> deductionsGroup = col16CheckDeductionGroups_1?.get(key) ?: new ArrayList<NdflPersonDeduction>()
                if (deductionsGroup) {
                    BigDecimal sum16 = (BigDecimal) deductionsGroup.sum { NdflPersonDeduction deduction -> deduction.periodCurrSumm ?: 0 } ?: 0
                    if (sum16 > ndflPersonDeduction.notifSumm) {
                        String errMsg = String.format("Раздел 3. ID операции: \"%s\". Для строк документа (тип: \"%s\", номер: \"%s\", дата: \"%s\", " +
                                "код источника:  \"%s\", сумма: \"%s\") сумма значений гр. \"Вычет.Текущий период.Сумма\" (%s) должна быть меньше или равна " +
                                "значения гр. \"Подтверждающий документ.Сумма\" (%s)",
                                ndflPersonDeduction.operationId, ndflPersonDeduction.notifType, ndflPersonDeduction.notifNum,
                                formatDate(ndflPersonDeduction.notifDate), ndflPersonDeduction.notifSource, ndflPersonDeduction.notifSumm,
                                sum16, ndflPersonDeduction.notifSumm
                        )
                        logger.logCheck(errMsg,
                                section_3_16_fatal,
                                LOG_TYPE_3_16, fioAndInpAndOperId)
                    }
                    deductionsGroup.clear()
                }
            }
            // Выч6.1
            if (ndflPersonDeduction.notifType == "1") {
                def key = new Col16CheckDeductionGroup_2Key(ndflPersonDeduction.ndflPersonId, ndflPersonDeduction.operationId)
                List<NdflPersonDeduction> deductionsGroup = col16CheckDeductionGroups_2?.get(key) ?: new ArrayList<NdflPersonDeduction>()
                if (deductionsGroup) {
                    BigDecimal sum16 = (BigDecimal) deductionsGroup.sum { NdflPersonDeduction deduction -> deduction.periodCurrSumm ?: 0 } ?: 0
                    BigDecimal sum8 = (BigDecimal) deductionsGroup.sum { NdflPersonDeduction deduction -> deduction.notifSumm ?: 0 } ?: 0
                    if (sum16 > sum8) {
                        String errMsg = String.format("Раздел 3. ID операции: \"%s\". Для строк, у которых указан тип: \"%s\",  сумма значений гр. \"%s\" (%s) должна быть меньше или равна сумме значений гр. \"%s\" (%s)",
                                ndflPersonDeduction.operationId, ndflPersonDeduction.notifType,
                                C_PERIOD_CURR_SUMM, sum16, C_NOTIF_SUMM, sum8
                        )
                        logger.logCheck(errMsg,
                                section_3_16_fatal,
                                LOG_TYPE_3_16, fioAndInpAndOperId)
                    }
                    deductionsGroup.clear()
                }
            }
        }
        logForDebug("Проверки сведений о вычетах (" + (System.currentTimeMillis() - time) + " мс)")
    }

    /**
     * Проверки для Раздел 4. Сведения о доходах в виде авансовых платежей
     */
    def checkDataPrepayment(List<NdflPerson> ndflPersonList, List<NdflPersonIncome> ndflPersonIncomeList, List<NdflPersonDeduction> ndflPersonDeductionList,
                            List<NdflPersonPrepayment> ndflPersonPrepaymentList, Map<Long, RegistryPerson> personMap) {
        long time = System.currentTimeMillis()

        Map<Long, NdflPerson> personByIdMap = ndflPersonList.collectEntries { [it.id, it] }
        Map<Long, Map<String, List<NdflPersonIncome>>> incomesByPersonIdAndOperationId =
                ndflPersonIncomeList.groupBy({ NdflPersonIncome it -> it.ndflPersonId }, { NdflPersonIncome it -> it.operationId })
        for (def prepayment : ndflPersonPrepaymentList) {
            def operationId = prepayment.operationId
            def allIncomesOfOperation = incomesByPersonIdAndOperationId.get(prepayment.ndflPersonId)?.get(operationId) ?: new ArrayList<NdflPersonIncome>()
            NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(prepayment.ndflPersonId)
            String fioAndInpAndOperId = sprintf(TEMPLATE_PERSON_FL_OPER, [ndflPersonFL.fio, ndflPersonFL.inp, operationId])

            def person = personByIdMap[prepayment.ndflPersonId]

            // 0 Строка Раздела 4 не относится к операции с фиктивной строкой
            for (def income : allIncomesOfOperation) {
                if (income.isDummy()) {
                    String errMsg = "относится к операции, для которой в Разделе 2 имеется строка $income.rowNum (ФЛ: $ndflPersonFL.fio, " +
                            "ИНП: $ndflPersonFL.inp, ставка налога = 0, ID операции = 0), показывающая отсутствие операций по данному ФЛ"
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_PREPAYMENT, prepayment.rowNum ?: "")
                    logger.errorExp("%s %s", "", fioAndInpAndOperId, pathError, errMsg)
                    break
                }
            }
            // 1 Заполнение Раздела 4 только для НП с кодом статуса = "6"
            if (person.status != "6" && person.inp == ndflPersonFL.inp) {
                String errMsg = String.format("Наличие строки некорректно, так как для ФЛ ИНП: %s Статус (Код) не равен \"6\"", person.inp)
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_PREPAYMENT, prepayment.rowNum ?: "")
                logger.logCheck("%s. %s.",
                        true, LOG_TYPE_SECTION4, fioAndInpAndOperId, pathError, errMsg)
            }
        }

        logForDebug("Проверки сведений о доходах в виде авансовых платежей (" + (System.currentTimeMillis() - time) + " мс)")
    }

    boolean isDummy(List<NdflPersonIncome> incomes) {
        for (def income : incomes) {
            if (!income.isDummy()) {
                return false
            }
        }
        return true
    }

    class NdflPersonFL {
        String fio
        String inp

        NdflPersonFL(String fio, String inp) {
            this.fio = fio
            this.inp = inp
        }
    }

    /**
     * Класс для проверки заполненности полей
     */
    class ColumnFillConditionData {
        ColumnFillConditionChecker columnConditionCheckerAsIs
        ColumnFillConditionChecker columnConditionCheckerToBe
        String conditionMessage

        ColumnFillConditionData(ColumnFillConditionChecker columnConditionCheckerAsIs, ColumnFillConditionChecker columnConditionCheckerToBe, String conditionMessage) {
            this.columnConditionCheckerAsIs = columnConditionCheckerAsIs
            this.columnConditionCheckerToBe = columnConditionCheckerToBe
            this.conditionMessage = conditionMessage
        }
    }

    interface ColumnFillConditionChecker {
        boolean check(NdflPersonIncome ndflPersonIncome)
    }
    /**
     * Проверка: "Раздел 2. Графа 4,5 заполнены"
     */
    class Column4Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return !!ndflPersonIncome.incomeCode
        }
    }
    /**
     * Проверка: "Раздел 2. Графа 4,5 заполнены"
     */
    class Column5Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return !!ndflPersonIncome.incomeType
        }
    }
    /**
     * Проверка: "Раздел 2. Графа 6 заполнена"
     */
    class Column6Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.incomeAccruedDate != null
        }
    }
    /**
     * Проверка: "Раздел 2. Графа 7 заполнена"
     */
    class Column7Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.incomePayoutDate != null
        }
    }
    /**
     * Проверка: "Раздел 2. Графа 8 заполнена"
     */
    class Column8Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return !!ndflPersonIncome.oktmo
        }
    }
    /**
     * Проверка: "Раздел 2. Графа 9 заполнена"
     */
    class Column9Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return !!ndflPersonIncome.kpp
        }
    }
    /**
     * Проверка: "Раздел 2. Графа 10 заполнена"
     */
    class Column10Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.incomeAccruedSumm != null
        }
    }
    /**
     * Проверка: "Раздел 2. Графа 11 заполнена"
     */
    class Column11Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.incomePayoutSumm != null
        }
    }
    /**
     * Проверка: "Раздел 2. Графа 10 или 11 заполнена"
     */
    class Column10Or11Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.incomeAccruedSumm != null || ndflPersonIncome.incomePayoutSumm != null
        }
    }
    /**
     * Проверка: "Раздел 2. Графы 7, 11 заполнены"
     */
    class Column7And11Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.incomePayoutDate != null && ndflPersonIncome.incomePayoutSumm != null
        }
    }
    /**
     * Проверка: "Раздел 2. Графы 13 заполнены"
     */
    class Column13Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.taxBase != null
        }
    }
    /**
     * Проверка: "Раздел 2. Графы  14 заполнены"
     */
    class Column14Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.taxRate != null
        }
    }
    /**
     * Проверка: "Раздел 2. Графы 15 заполнены"
     */
    class Column15Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.taxDate != null
        }
    }

    /**
     * Проверка: "Раздел 2. Графы 16 заполнена"
     */
    class Column16Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.calculatedTax != null
        }
    }
    /**
     * Проверка: "Раздел 2. Графы 17 заполнена"
     */
    class Column17Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.withholdingTax != null
        }
    }
    /**
     * Проверка: "Раздел 2. Графа 18 заполнена"
     */
    class Column18Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.notHoldingTax != null
        }
    }
    /**
     * Проверка: "Раздел 2. Графа 19 заполнена"
     */
    class Column19Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.overholdingTax != null
        }
    }
    /**
     * Проверка: "Раздел 2. Графы 20 заполнена"
     */
    class Column20Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.refoundTax != null
        }
    }
    /**
     * Проверка: "Раздел 2. Графы 21 заполнена"
     */
    class Column21Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.taxTransferDate != null
        }
    }
    /**
     * Проверка: "Раздел 2. Графы 21 НЕ заполнена"
     */
    class Column21NotFill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.taxTransferDate == null
        }
    }
    /**
     * Проверка: "Раздел 2. Графы 7, 11 ИЛИ 22, 23, 24 заполнены"
     */
    class Column7And11Or22And23And24Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return (new Column7And11Fill().check(ndflPersonIncome)) || (new Column22And23And24Fill().check(ndflPersonIncome))
        }
    }
    /**
     * Проверка: "Раздел 2. Графы 7, 11 И 22, 23, 24 НЕ заполнены"
     */
    class Column7And11And22And23And24NotFill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return !(new Column7And11Fill().check(ndflPersonIncome)) && (new Column22And23And24NotFill().check(ndflPersonIncome))
        }
    }
    /**
     * Проверка: "Раздел 2. Графы 22, 23, 24 НЕ заполнены"
     */
    class Column22And23And24NotFill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.paymentDate == null && ScriptUtils.isEmpty(ndflPersonIncome.paymentNumber) && ndflPersonIncome.taxSumm == null
        }
    }
    /**
     * Проверка: "Раздел 2. Графы 22, 23, 24 заполнены"
     */
    class Column22And23And24Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.paymentDate != null && !ScriptUtils.isEmpty(ndflPersonIncome.paymentNumber) && ndflPersonIncome.taxSumm != null
        }
    }
    /**
     * 	Должны быть либо заполнены все 3 Графы 22, 23, 24, либо ни одна их них
     */
    class Column22And23And24FillOrColumn22And23And24NotFill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return (new Column22And23And24NotFill().check(ndflPersonIncome)) || (new Column22And23And24Fill().check(ndflPersonIncome))
        }
    }
    /**
     * 	Всегда возвращает true
     */
    class ColumnTrueFillOrNotFill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return true
        }
    }

    /**
     * Класс для получения рабочих дней
     */
    class DateConditionWorkDay {

        // Мапа рабочих дней со сдвигом
        private Map<Date, Date> workDayWithOffset0Cache
        private Map<Date, Date> workDayWithOffset1Cache
        private Map<Date, Date> workDayWithOffset30Cache
        private Map<Integer, Date> lastWorkDayOfTheYear
        CalendarService calendarService

        DateConditionWorkDay(CalendarService calendarService) {
            workDayWithOffset0Cache = [:]
            workDayWithOffset1Cache = [:]
            workDayWithOffset30Cache = [:]
            lastWorkDayOfTheYear = [:]
            this.calendarService = calendarService
        }

        /**
         * Возвращает дату рабочего дня, смещенного относительно даты startDate.
         *
         * @param startDate начальная дата, может быть и рабочим днем и выходным
         * @param offset на сколько рабочих дней необходимо сдвинуть начальную дату. Может быть меньше 0, тогда сдвигается в обратную сторону
         * @return смещенная на offset рабочих дней дата
         */
        Date getWorkDay(Date startDate, int offset) {
            Date resultDate
            if (offset == 0) {
                resultDate = workDayWithOffset0Cache.get(startDate)
                if (resultDate == null) {
                    resultDate = calendarService.getWorkDay(startDate, offset)
                    workDayWithOffset0Cache.put(startDate, resultDate)
                }
            } else if (offset == 1) {
                resultDate = workDayWithOffset1Cache.get(startDate)
                if (resultDate == null) {
                    resultDate = calendarService.getWorkDay(startDate, offset)
                    workDayWithOffset1Cache.put(startDate, resultDate)
                }
            } else if (offset == 30) {
                resultDate = workDayWithOffset30Cache.get(startDate)
                if (resultDate == null) {
                    resultDate = calendarService.getWorkDay(startDate, offset)
                    workDayWithOffset30Cache.put(startDate, resultDate)
                }
            }
            return resultDate
        }

        Date getLastDayOfTheYear(int year) {
            Date resultDate = lastWorkDayOfTheYear.get(year)
            if (resultDate == null) {
                resultDate = calendarService.getLastWorkDayByYear(year)
                lastWorkDayOfTheYear.put(year, resultDate)
            }
            return resultDate
        }
    }

    /**
     * Класс для соотнесения вида проверки в зависимости от значений "Код вида дохода" и "Признак вида дохода"
     */
    class DateConditionData<T> {
        List<String> incomeCodes
        List<String> incomeTypes
        T checker
        String conditionMessage

        DateConditionData(List<String> incomeCodes, List<String> incomeTypes, T checker, String conditionMessage) {
            this.incomeCodes = incomeCodes
            this.incomeTypes = incomeTypes
            this.checker = checker
            this.conditionMessage = conditionMessage
        }
    }

    /**
     * Используется для проверки Доход.Дата.Начисление (Графа 6)
     */
    abstract class IncomeAccruedDateConditionChecker implements DateConditionChecker {
        /**
         * Дата выплаты может находится не в проверяемой строке, в таком случае checker выдаёт ту дату с которой сравнивал
         */
        Date getDateCompared(NdflPersonIncome checkedIncome) {
            return checkedIncome.incomePayoutDate
        }
    }

    /**
     * Используется для проверки НДФЛ.Перечисление в бюджет.Срок (Графа 21)
     */
    abstract class TaxTransferDateConditionChecker implements DateConditionCheckerForBudget {

        /**
         * "Дата перечисления в бюджет" может иметь значение "00.00.0000", для этого форматируем её этим способом.
         */
        static String formatTaxTransferDate(Date taxTransferDate) {
            String dateString = taxTransferDate.format(SharedConstants.DATE_FORMAT)
            if (dateString == SharedConstants.DATE_ZERO_AS_DATE) {
                return SharedConstants.DATE_ZERO_AS_STRING
            } else {
                return dateString
            }
        }
    }

    interface DateConditionChecker {
        /**
         * Выполняет проверку в строке раздела 2
         *
         * @param checkedIncome проверяемая строка раздела 2
         * @param allIncomesOf группа строк, относящиеся к проверяемой каким-то условием (например, по ид операции)
         * @return пройдена ли проверка
         */
        boolean check(NdflPersonIncome checkedIncome, List<NdflPersonIncome> allIncomesOf)
    }

    interface DateConditionCheckerForBudget {
        /**
         * Выполняет проверку в строке раздела 2
         *
         * @param checkedIncome проверяемая строка раздела 2
         * @return если проверка не пройдена, то сообщение с ошибкой, если пройдена {@code null}
         */
        String check(NdflPersonIncome checkedIncome)
    }

    /**
     * Если существует только одна строка, для которой одновременно выполняются условия:
     * 1) Заполнена "Графа 7"
     * 2) "Графа 2" = "Графа 2" проверяемой строки
     * 3) "Графа 3" = "Графа 3" проверяемой строки
     * 4) "Графа 4" = "Графа 4" проверяемой строки
     * 5) "Графа 5" = "Графа 5" проверяемой строки
     * то "Графе 6" проверяемой строки = "Графа 7" найденной строки
     */
    class Column6EqualsColumn7 extends IncomeAccruedDateConditionChecker {
        Date incomePayoutDate

        @Override
        boolean check(NdflPersonIncome checkedIncome, List<NdflPersonIncome> allIncomesOfOperation) {
            incomePayoutDate = null

            List<NdflPersonIncome> foundIncomes = allIncomesOfOperation.findAll {
                it.incomePayoutDate && it.incomeCode == checkedIncome.incomeCode && it.incomeType == checkedIncome.incomeType
            }
            if (1 == foundIncomes.size()) {
                incomePayoutDate = foundIncomes.get(0).incomePayoutDate
                return checkedIncome.incomeAccruedDate == foundIncomes.get(0).incomePayoutDate
            }
            return true
        }

        @Override
        Date getDateCompared(NdflPersonIncome checkedIncome) {
            return incomePayoutDate
        }
    }

    /**
     * Проверка "Последний рабочий день года"
     */
    class LastYearWorkDay extends IncomeAccruedDateConditionChecker {
        Date comparedDate

        @Override
        boolean check(NdflPersonIncome checkedIncome, List<NdflPersonIncome> allIncomesOfOperation) {
            if (checkedIncome.incomeAccruedDate == null) {
                return true
            }
            Calendar calendar = Calendar.getInstance()
            calendar.setTime(checkedIncome.incomeAccruedDate)
            comparedDate = dateConditionWorkDay.getLastDayOfTheYear(calendar.get(Calendar.YEAR))
            return checkedIncome.incomeAccruedDate.equals(comparedDate)
        }

        Date getDateCompared(NdflPersonIncome checkedIncome) {
            return comparedDate
        }
    }

    /**
     * Проверка "Последний календарный день месяца"
     */
    class LastMonthCalendarDay extends IncomeAccruedDateConditionChecker {
        @Override
        boolean check(NdflPersonIncome checkedIncome, List<NdflPersonIncome> allIncomesOfOperation) {
            if (checkedIncome.incomeAccruedDate == null) {
                return true
            }
            Calendar calendar = Calendar.getInstance()
            calendar.setTime(checkedIncome.incomeAccruedDate)
            int currentMonth = calendar.get(Calendar.MONTH)
            calendar.add(calendar.DATE, 1)
            int comparedMonth = calendar.get(Calendar.MONTH)
            return currentMonth != comparedMonth
        }
    }

    /**
     * Проверка: "Графа 21" = "Графа 7" + "1 рабочий день"
     */
    class Column21EqualsColumn7Plus1WorkingDay extends TaxTransferDateConditionChecker {
        @Override
        String check(NdflPersonIncome checkedIncome) {
            Calendar calendar21 = Calendar.getInstance()
            calendar21.setTime(checkedIncome.taxTransferDate)

            // "Графа 7" + "1 рабочий день"
            int offset = 1
            Date workDay = dateConditionWorkDay.getWorkDay(checkedIncome.incomePayoutDate, offset)
            Calendar calendar7 = Calendar.getInstance()
            calendar7.setTime(workDay)

            if (calendar21.equals(calendar7)) {
                return null
            }

            return String.format("Значение гр. \"Срок перечисления в бюджет\" (%s) должно быть равно значению гр. \"Дата выплаты дохода\" (%s) + 1 рабочий день. Корректное значение срока перечисления в бюджет: %s",
                    formatTaxTransferDate(checkedIncome.taxTransferDate),
                    ScriptUtils.formatDate(checkedIncome.incomePayoutDate),
                    ScriptUtils.formatDate(calendar7.getTime()))
        }
    }

    /**
     * "Графа 21" == "Графа 7" + "30 дней"
     */
    class Column21EqualsColumn7Plus30WorkingDays extends TaxTransferDateConditionChecker {
        @Override
        String check(NdflPersonIncome checkedIncome) {
            // "Следующий рабочий день" после "Графа 7" + "30 календарных дней"
            Date incomePayoutPlus30CalendarDays = DateUtils.addDays(checkedIncome.incomePayoutDate, 30)
            Date incomePayoutPlus30CalendarDaysWorkingDay = dateConditionWorkDay.getWorkDay(incomePayoutPlus30CalendarDays, 0)

            if (checkedIncome.taxTransferDate.equals(incomePayoutPlus30CalendarDaysWorkingDay)) {
                return null
            }
            return String.format("Значение гр. \"Срок перечисления в бюджет\" (%s) должно быть равно значению гр. \"Дата выплаты дохода\" (%s) + 30 календарных дней. Если дата попадает на выходной день, то дата переносится на следующий рабочий день. Корректное значение срока перечисления в бюджет: %s",
                    formatTaxTransferDate(checkedIncome.taxTransferDate),
                    ScriptUtils.formatDate(checkedIncome.incomePayoutDate),
                    ScriptUtils.formatDate(incomePayoutPlus30CalendarDaysWorkingDay))
        }
    }
    /**
     * "Графа 21" = Последний календарный день месяца для месяца "Графы 7", если Последний календарный день месяца - выходной день, то "Графа 21" = следующий рабочий день
     */
    class Column21EqualsColumn7LastDayOfMonth extends TaxTransferDateConditionChecker {
        @Override
        String check(NdflPersonIncome checkedIncome) {
            if (checkedIncome.taxTransferDate == null || checkedIncome.incomePayoutDate == null) {
                return false
            }
            Calendar calendar21 = Calendar.getInstance()
            calendar21.setTime(checkedIncome.taxTransferDate)

            Calendar calendar7 = Calendar.getInstance()
            calendar7.setTime(checkedIncome.incomePayoutDate)

            // находим последний день месяца
            calendar7.set(Calendar.DAY_OF_MONTH, calendar7.getActualMaximum(Calendar.DAY_OF_MONTH))
            Date workDay = calendar7.getTime()
            // если последний день месяца приходится на выходной, то следующий первый рабочий день
            int offset = 0
            workDay = dateConditionWorkDay.getWorkDay(workDay, offset)
            calendar7.setTime(workDay)

            if (calendar21.equals(calendar7)) {
                return null
            }
            return String.format("Значение гр. \"Срок перечисления в бюджет\" (%s) должно быть равно последнему календарному дню месяца, указанного в гр.\"Дата выплаты дохода\" (%s). " +
                    "Если дата попадает на выходной день, то дата переносится на следующий рабочий день. Корректное значение срока перечисления в бюджет: %s",
                    formatTaxTransferDate(checkedIncome.taxTransferDate),
                    ScriptUtils.formatDate(checkedIncome.incomePayoutDate),
                    ScriptUtils.formatDate(calendar7.getTime()))
        }
    }

    class Column21ForNaturalIncome extends TaxTransferDateConditionChecker {
        @Override
        String check(NdflPersonIncome checkedIncome) {
            List<NdflPersonIncome> matchedIncomes = []
            NdflPersonFL checkedIncomeFl = ndflPersonFLMap.get(checkedIncome.ndflPersonId)
            for (NdflPersonIncome income : ndflPersonIncomeList) {
                if (income.getId() != checkedIncome.getId()) {
                    NdflPersonFL ndflPersonFl = ndflPersonFLMap.get(income.ndflPersonId)
                    if (checkedIncomeFl?.inp == ndflPersonFl.inp && !["02", "14"].contains(income.incomeType) && income.incomePayoutDate >= checkedIncome.incomePayoutDate && income.asnuId == checkedIncome.asnuId) {
                        matchedIncomes << income
                    }
                }
            }
            Calendar zeroDate = Calendar.getInstance()
            zeroDate.set(1901, Calendar.JANUARY, 1)
            if (matchedIncomes.isEmpty() && checkedIncome.taxTransferDate != SimpleDateUtils.toStartOfDay(zeroDate.getTime())) {
                return String.format("Значение гр. \"Срок перечисления в бюджет\" (%s) должно быть равно значению (00.00.0000), так как не найдена строка выплаты, у которой \"Признак дохода\" не равен 02 или 14",
                        formatTaxTransferDate(checkedIncome.taxTransferDate))
            } else if (matchedIncomes.isEmpty()) {
                return null
            } else {
                Collections.sort(matchedIncomes, new Comparator<NdflPersonIncome>() {
                    @Override
                    int compare(NdflPersonIncome o1, NdflPersonIncome o2) {
                        int payoutDate = o1.incomePayoutDate.compareTo(o2.incomePayoutDate)
                        if (payoutDate != 0) return payoutDate
                        return o1.taxTransferDate.compareTo(o2.taxTransferDate)
                    }
                })
                if (checkedIncome.getTaxTransferDate() == matchedIncomes.get(0).getTaxTransferDate()) {
                    return null
                } else {
                    return String.format("Значение гр. \"Срок перечисления в бюджет\" (%s) должно быть равно значению гр. \"Срок перечисления в бюджет\" (%s) строки выплаты, у которой \"Признак дохода\" не равен 02 или 14",
                            formatTaxTransferDate(checkedIncome.taxTransferDate),
                            formatTaxTransferDate(matchedIncomes.get(0).taxTransferDate))
                }
            }

        }
    }

    class Column21EqualsLastDayOfFirstMonthOfNextYear extends TaxTransferDateConditionChecker {
        @Override
        String check(NdflPersonIncome checkedIncome) {
            Calendar calendar7 = Calendar.getInstance()
            calendar7.setTime(checkedIncome.incomePayoutDate)
            calendar7.set(calendar7.get(Calendar.YEAR) + 1, Calendar.JANUARY, 31)

            Date referenceValue = dateConditionWorkDay.getWorkDay(calendar7.getTime(), 0)

            if (referenceValue == SimpleDateUtils.toStartOfDay(checkedIncome.taxTransferDate)) {
                return null
            }
            return String.format("Значение гр. \"Срок перечисления в бюджет\" (%s) должно быть равно последнему календарному дню месяца, следующего за годом указанным в гр. \"Дата выплаты дохода\" (%s). Если дата попадает на выходной день, то она переносится на следующий рабочий день. Корректное значение срока перечисления в бюджет: %s",
                    formatTaxTransferDate(checkedIncome.taxTransferDate),
                    ScriptUtils.formatDate(checkedIncome.incomePayoutDate),
                    ScriptUtils.formatDate(referenceValue))
        }
    }

    String formatDate(Date date) {
        return date ? ScriptUtils.formatDate(date) : ""
    }

    /**
     * Получить "Страны"
     * @return
     */
    Map<Long, String> getRefCountryCode() {
        if (countryCodeCache.size() == 0) {
            PagingResult<Map<String, RefBookValue>> refBookMap = getRefBook(RefBook.Id.COUNTRY.getId())
            refBookMap.each { Map<String, RefBookValue> refBook ->
                countryCodeCache.put((Long) refBook?.id?.numberValue, refBook?.CODE?.stringValue)
            }
        }
        return countryCodeCache
    }

    /**
     * Выгрузка из справочников по условию и версии
     * @param refBookId
     * @param whereClause
     * @return
     * Поскольку поиск осуществляется с использованием оператора EXISTS необходимодимо всегда связывать поле подзапроса через ALIAS frb
     */
    Map<Long, Map<String, RefBookValue>> getRefBookByRecordVersionWhere(Long refBookId, String whereClause, Date version) {
        Map<Long, Map<String, RefBookValue>> refBookMap = getProvider(refBookId).getRecordDataVersionWhere(whereClause, version)
        if (refBookMap == null || refBookMap.size() == 0) {
            //throw new ScriptException("Не найдены записи справочника " + refBookId)
            return Collections.emptyMap()
        }
        return refBookMap
    }

    Map<Long, String> getRefDocumentTypeCode() {
        if (documentTypeCodeCache.size() == 0) {
            Map<Long, Map<String, RefBookValue>> refBookList = getRefDocumentType()
            refBookList.each { Long id, Map<String, RefBookValue> refBookValueMap ->
                documentTypeCodeCache.put(id, refBookValueMap?.get("CODE")?.getStringValue())
            }
        }
        return documentTypeCodeCache
    }

    /**
     * Получить "Статусы налогоплательщика"
     * @return
     */
    Map<Long, String> getRefTaxpayerStatusCode() {
        if (taxpayerStatusCodeCache.size() == 0) {
            PagingResult<Map<String, RefBookValue>> refBookMap = getRefBook(RefBook.Id.TAXPAYER_STATUS.getId())
            refBookMap.each { Map<String, RefBookValue> refBook ->
                taxpayerStatusCodeCache.put((Long) refBook?.id?.numberValue, refBook?.CODE?.stringValue)
            }
        }
        return taxpayerStatusCodeCache
    }

    /**
     * Получить "Коды видов доходов"
     * @return
     */
    Map<Long, Map<String, RefBookValue>> getRefIncomeCode() {
        // Map<REF_BOOK_INCOME_TYPE.ID, REF_BOOK_INCOME_TYPE>
        Map<Long, Map<String, RefBookValue>> mapResult = [:]
        PagingResult<Map<String, RefBookValue>> refBookMap = getRefBook(RefBook.Id.INCOME_CODE.id)
        refBookMap.each { Map<String, RefBookValue> refBook ->
            mapResult.put((Long) refBook?.id?.numberValue, refBook)
        }
        return mapResult
    }

    /**
     * Получить "Виды доходов"
     * @return мапа , где ключ значение признака дохода, значение - список записей из справочника "Виды доходов" соответствующие данному признаку
     */
    Map<String, List<Map<String, RefBookValue>>> getRefIncomeType() {
        // Map<REF_BOOK_INCOME_KIND.MARK, List<REF_BOOK_INCOME_KIND.INCOME_TYPE_ID>>
        Map<String, List<Map<String, RefBookValue>>> mapResult = [:]
        PagingResult<Map<String, RefBookValue>> refBookList = getRefBook(RefBook.Id.INCOME_KIND.id)
        refBookList.each { Map<String, RefBookValue> refBookRow ->
            String mark = refBookRow?.MARK?.stringValue
            List<Map<String, RefBookValue>> refBookRowList = mapResult.get(mark)
            if (refBookRowList == null) {
                refBookRowList = []
            }
            refBookRowList.add(refBookRow)
            mapResult.put(mark, refBookRowList)
        }
        return mapResult
    }

    /**
     * Получить "Коды видов вычетов"
     * @return
     */
    List<String> getRefDeductionType() {
        if (deductionTypeCache.size() == 0) {
            PagingResult<Map<String, RefBookValue>> refBookList = getRefBook(RefBook.Id.DEDUCTION_TYPE.id)
            refBookList.each { Map<String, RefBookValue> refBook ->
                deductionTypeCache.add(refBook?.CODE?.stringValue)
            }
        }
        return deductionTypeCache
    }

    /**
     * Получить "Коды налоговых органов"
     * @return
     */
    List<String> getRefNotifSource() {
        if (taxInspectionCache.size() == 0) {
            PagingResult<Map<String, RefBookValue>> refBookList = getRefBook(RefBook.Id.TAX_INSPECTION.id)
            refBookList.each { Map<String, RefBookValue> refBook ->
                taxInspectionCache.add(refBook?.CODE?.stringValue)
            }
        }
        return taxInspectionCache
    }

    /**
     * Проверка адреса на пустоту
     * @param Данные о ФЛ из формы
     * @return
     */
    boolean isPersonAddressEmpty(NdflPerson ndflPerson) {
        boolean emptyAddress = ScriptUtils.isEmpty(ndflPerson.regionCode) && ScriptUtils.isEmpty(ndflPerson.area) &&
                ScriptUtils.isEmpty(ndflPerson.city) && ScriptUtils.isEmpty(ndflPerson.locality) &&
                ScriptUtils.isEmpty(ndflPerson.street) && ScriptUtils.isEmpty(ndflPerson.house) &&
                ScriptUtils.isEmpty(ndflPerson.building) && ScriptUtils.isEmpty(ndflPerson.flat)
        return emptyAddress
    }

    void logFiasError(String fioAndInp, String pathError, String name, String value) {
        logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, "ФИАС"), fioAndInp, pathError,
                "Значение гр. \"" + name + "\" (\"" + (value ?: "") + "\") отсутствует в справочнике \"ФИАС\"")
    }

    void logFiasIndexError(String fioAndInp, String pathError, String name, String value) {
        logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, "ФИАС"), fioAndInp, pathError,
                "Значение гр. \"" + name + "\" (\"" + (value ?: "") + "\") не соответствует требуемому формату")
    }

    boolean checkRequiredAttribute(NdflPerson ndflPerson, String fioAndInp, String alias, String attributeName) {
        if (ndflPerson[alias] == null || (ndflPerson[alias]) instanceof String && (org.apache.commons.lang3.StringUtils.isBlank((String) ndflPerson[alias]) || ndflPerson[alias] == "0")) {
            String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
            String msg
            if (ndflPerson[alias] == "0") {
                msg = "Значение гр. \"$attributeName\" не может быть равно \"0\""
            } else {
                msg = "Значение гр. \"$attributeName\" не указано"
            }
            logger.warnExp("%s. %s.", "Не указан обязательный реквизит ФЛ", fioAndInp, pathError, msg)
            return false
        }
        return true
    }

    int signOf(BigDecimal number) {
        return number > 0 ? 1 : number < 0 ? -1 : 0
    }

    /**
     * Проверяется заполненность согласно временному решению
     * Если сумма начисленного дохода равна сумме вычетов, будет ноль в графах:
     Раздел 2. Графа 13. Налоговая база
     Раздел 2. Графа 16. Сумма исчисленного налога
     Раздел 2. Графа 17. Сумма удержанного налога
     * @param checkingValue
     * @param incomeAccruedSum
     * @param totalDeductionSum
     * @return true - заполнен, false - не заполнен
     */
    boolean isPresentedByTempSolution(BigDecimal checkingValue, BigDecimal incomeAccruedSum, BigDecimal totalDeductionSum) {
        if (checkingValue == null) {
            return false
        }
        if (incomeAccruedSum != totalDeductionSum && checkingValue == new BigDecimal(0)) {
            return false
        }
        return true
    }

    // Проверка принадлежности даты к периоду формы
    boolean dateRelateToCurrentPeriod(Date date) {
        if (date == null || (date >= getReportPeriodCalendarStartDate() && date <= getReportPeriodEndDate())) {
            return true
        }
        return false
    }

    /**
     * Получить дату начала отчетного периода
     * @return
     */
    Date getReportPeriodStartDate() {
        return reportPeriod.startDate
    }

    /**
     * Получить дату начала отчетного периода
     * @return
     */
    Date getReportPeriodCalendarStartDate() {
        return reportPeriod.calendarStartDate
    }

    /**
     * Получить дату окончания отчетного периода
     * @return
     */
    Date getReportPeriodEndDate() {
        return reportPeriod.endDate
    }

    /**
     * Получить записи справочника по его идентификатору в отчётном периоде
     * @param refBookId - идентификатор справочника
     * @return - список записей справочника
     */
    PagingResult<Map<String, RefBookValue>> getRefBook(long refBookId) {
        // Передаем как аргумент только срок действия версии справочника
        PagingResult<Map<String, RefBookValue>> refBookList = getProvider(refBookId).getRecordsVersion(getReportPeriodStartDate(), getReportPeriodEndDate(), null, null)
        if (refBookList == null || refBookList.size() == 0) {
            throw new Exception("Ошибка при получении записей справочника " + refBookId)
        }
        return refBookList
    }

    /**
     * Получение провайдера с использованием кеширования.
     * @param providerId
     * @return
     */
    RefBookDataProvider getProvider(def long providerId) {
        if (!providerCache.containsKey(providerId)) {
            providerCache.put(providerId, refBookFactory.getDataProvider(providerId))
        }
        return providerCache.get(providerId)
    }

    /**
     * Получить "Виды документов"
     */
    Map<Long, Map<String, RefBookValue>> getRefDocumentType() {
        if (documentTypeCache.size() == 0) {
            PagingResult<Map<String, RefBookValue>> refBookList = getRefBook(RefBook.Id.DOCUMENT_CODES.getId())
            refBookList.each { Map<String, RefBookValue> refBook ->
                documentTypeCache.put((Long) refBook?.id?.numberValue, refBook)
            }
        }
        return documentTypeCache
    }

    /**
     * Выгрузка из справочников по условию
     * @param refBookId
     * @param whereClause
     * @return
     * Поскольку поиск осуществляется с использованием оператора EXISTS необходимодимо всегда связывать поле подзапроса через ALIAS frb
     */
    Map<Long, Map<String, RefBookValue>> getRefBookByRecordWhere(Long refBookId, String whereClause) {
        Map<Long, Map<String, RefBookValue>> refBookMap = getProvider(refBookId).getRecordDataWhere(whereClause)
        if (refBookMap == null || refBookMap.size() == 0) {
            //throw new ScriptException("Не найдены записи справочника " + refBookId)
            return Collections.emptyMap()
        }
        return refBookMap
    }

    /**
     * Получить "АСНУ"
     * @return
     */
    Map<Long, RefBookAsnu> getRefAsnu() {
        if (asnuCache.isEmpty()) {
            List<RefBookAsnu> asnuList = refBookService.findAllAsnu()
            asnuList.each { RefBookAsnu asnu ->
                Long asnuId = (Long) asnu?.id
                asnuCache.put(asnuId, asnu)
            }
        }
        return asnuCache
    }

    /**
     * Является ди дата последним календарным днем месяца
     * @param date проверяемая дата
     * @return
     */
    boolean isLastMonthDay(Date date) {
        if (!date) return false
        Calendar calendar = Calendar.getInstance()
        calendar.setTime(date)
        int currentMonth = calendar.get(Calendar.MONTH)
        calendar.add(calendar.DATE, 1)
        int comparedMonth = calendar.get(Calendar.MONTH)
        return currentMonth != comparedMonth
    }

    /**
     * Получить все записи справочника по его идентификатору и коллекции идентификаторов записей справочника
     * @param refBookId - идентификатор справочника
     * @param recordIds - коллекция идентификаторов записей справочника
     * @return - возвращает мапу
     */
    Map<Long, Map<String, RefBookValue>> getRefBookByRecordIds(long refBookId, List<Long> recordIds) {
        Map<Long, Map<String, RefBookValue>> refBookMap = getProvider(refBookId).getRecordData(recordIds)
        if (refBookMap == null || refBookMap.size() == 0) {
            throw new ScriptException("Ошибка при получении записей справочника " + refBookId)
        }
        return refBookMap
    }

    class CheckData {
        String msgFirst
        String msgLast
        boolean fatal

        CheckData(String msgFirst, String msgLast) {
            this.msgFirst = msgFirst
            this.msgLast = msgLast
            this.fatal = false
        }

        CheckData(String msgFirst, String msgLast, boolean fatal) {
            this.msgFirst = msgFirst
            this.msgLast = msgLast
            this.fatal = fatal
        }
    }

    @EqualsAndHashCode
    class Col16CheckDeductionGroup_1Key {
        Long ndflPersonId
        String operationId
        Date notifDate
        String notifNum
        String notifSource
        BigDecimal notifSumm

        public Col16CheckDeductionGroup_1Key(Long ndflPersonId, String operationId, Date notifDate, String notifNum, String notifSource, BigDecimal notifSumm) {
            this.ndflPersonId = ndflPersonId
            this.operationId = operationId
            this.notifDate = notifDate
            this.notifNum = notifNum
            this.notifSource = notifSource
            this.notifSumm = notifSumm
        }
    }

    @EqualsAndHashCode
    class Col16CheckDeductionGroup_2Key {
        long ndflPersonId
        String operationId

        public Col16CheckDeductionGroup_2Key(long ndflPersonId, String operationId) {
            this.ndflPersonId = ndflPersonId
            this.operationId = operationId
        }
    }
}
