package form_template.ndfl.primary_rnu_ndfl.v2016

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment
import com.aplana.sbrf.taxaccounting.model.util.BaseWeigthCalculator
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory
import com.aplana.sbrf.taxaccounting.script.service.CalendarService
import com.aplana.sbrf.taxaccounting.script.service.DeclarationService
import com.aplana.sbrf.taxaccounting.script.service.DepartmentReportPeriodService
import com.aplana.sbrf.taxaccounting.script.service.DepartmentService
import com.aplana.sbrf.taxaccounting.script.service.FiasRefBookService
import com.aplana.sbrf.taxaccounting.script.service.NdflPersonService
import com.aplana.sbrf.taxaccounting.script.service.ReportPeriodService
import com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils

import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import org.joda.time.LocalDateTime

import java.util.regex.Matcher
import java.util.regex.Pattern

new Check(this).run()

@TypeChecked
class Check extends AbstractScriptClass {

    NdflPersonService ndflPersonService
    DeclarationData declarationData
    DepartmentService departmentService
    CalendarService calendarService
    FiasRefBookService fiasRefBookService
    ReportPeriodService reportPeriodService
    DepartmentReportPeriodService departmentReportPeriodService
    RefBookFactory refBookFactory

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
    final String R_INP = "Идентификаторы налогоплательщиков"
    final String R_DUL = "Документы, удостоверяющие личность"
    final String R_ADDRESS = "Адреса"
    final String R_FIAS = "ФИАС"

    final String RF_RECORD_ID = "RECORD_ID"
    final String RF_ADDRESS = "ADDRESS"
    final String RF_LAST_NAME = "LAST_NAME"
    final String RF_FIRST_NAME = "FIRST_NAME"
    final String RF_MIDDLE_NAME = "MIDDLE_NAME"
    final String RF_SNILS = "SNILS"
    final String RF_BIRTH_DATE = "BIRTH_DATE"
    final String RF_INN = "INN"
    final String RF_INN_FOREIGN = "INN_FOREIGN"
    final String RF_TAXPAYER_STATE = "TAXPAYER_STATE"
    final String RF_CITIZENSHIP = "CITIZENSHIP"

    final String RF_DOC_ID = "DOC_ID"
    final String RF_DOC_NUMBER = "DOC_NUMBER"
    final String RF_INC_REP = "INC_REP"

    final String RF_REGION_CODE = "REGION_CODE"
    final String RF_LOCALITY = "LOCALITY"
    final String RF_BUILD = "BUILD"
    final String RF_DISTRICT = "DISTRICT"
    final String RF_CITY = "CITY"
    final String RF_STREET = "STREET"
    final String RF_HOUSE = "HOUSE"
    final String RF_APPARTMENT = "APPARTMENT"

    final String TEMPLATE_PERSON_FL = "%s, ИНП: %s"
    final String TEMPLATE_PERSON_FL_OPER = "%s, ИНП: %s, ID операции: %s"
    final String SECTION_LINE_MSG = "Раздел %s. Строка %s"

    final String C_CITIZENSHIP = "Гражданство (код страны)"
    final String C_STATUS = "Статус (код)"
    final String C_INCOME_TYPE = "Признак дохода" //"Доход.Вид.Признак"
    final String C_INCOME_CODE = "Код дохода" //"Доход.Вид.Код"
    final String C_TYPE_CODE = "Код вычета" //" Код вычета"
    final String C_NOTIF_SOURCE = "Подтверждающий документ. Код источника"
    final String C_PAYMENT_DATE = "Дата платежного поручения" //"НДФЛ.Перечисление в бюджет.Платежное поручение.Дата"
    final String C_PAYMENT_NUMBER = "Номер платежного поручения"//"НДФЛ.Перечисление в бюджет.Платежное поручение.Номер"
    final String C_TAX_SUMM = "Сумма платежного поручения" //"НДФЛ.Перечисление в бюджет.Платежное поручение.Сумма"
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
    final String C_REFOUND_TAX = "НДФЛ возвращенный НП" //C_REFOUND_TAX
    final String C_TAX_TRANSFER_DATE = "Срок перечисления в бюджет" //"НДФЛ.Перечисление в бюджет.Срок"
    final String C_PERIOD_CURR_SUMM = "Вычет. Текущий период. Сумма" //" Применение вычета.Текущий период.Сумма"
    final String C_INCOME_ACCRUED = "Доход. Дата" //" Начисленный доход.Дата"
    final String C_INCOME_ACCRUED_CODE = "Доход. Код дохода" //" Начисленный доход.Код дохода"
    final String C_INCOME_ACCRUED_P_SUMM = "Доход. Сумма" //" Начисленный доход.Сумма"
    final String C_PERIOD_CURR_DATE = "Вычет. Текущий период. Дата" //" Применение вычета.Текущий период.Дата"
    final String C_NOTIF_SUMM = "Подтверждающий документ. Сумма" //" Документ о праве на налоговый вычет.Сумма"

    final String LOG_TYPE_REFERENCES = "Значение не соответствует справочнику \"%s\""
    final String LOG_TYPE_PERSON_MSG = "Значение гр. \"%s\" (\"%s\") не соответствует справочнику \"%s\""
    final String LOG_TYPE_PERSON_MSG_2 = "Значение гр. \"%s\" (\"%s\") отсутствует в справочнике \"%s\""
    final String LOG_TYPE_2_6 = "\"Дата начисления дохода\" указана некорректно"
    final String LOG_TYPE_2_12 = "\"Сумма вычета\" указана некорректно"
    final String LOG_TYPE_2_14 = "\"Налоговая ставка\" указана некорректно"
    final String LOG_TYPE_2_14_MSG = "Значение гр. \"%s\" (\"%s\") указано некорректно. Для \"Кода дохода\" (\"%s\") и \"Статуса НП\" (\"%s\") предусмотрены ставки: %s"
    final String LOG_TYPE_2_16 = "\"НДФЛ исчисленный\" рассчитан некорректно"
    final String LOG_TYPE_2_17 = "\"НДФЛ удержанный\" рассчитан некорректно"
    final String LOG_TYPE_2_18 = "\"НДФЛ не удержанный\" рассчитан некорректно"
    final String LOG_TYPE_2_19 = "\"НДФЛ излишне удержанный\" рассчитан некорректно"
    final String LOG_TYPE_2_20 = "\"НДФЛ возвращеный НП\" рассчитан некорректно"
    final String LOG_TYPE_2_21 = "\"Срок перечисления в бюджет\" рассчитан некорректно"
    final String LOG_TYPE_NOT_ZERO = "Значение не может быть \"0\""
    final String LOG_TYPE_3_7 = "\"Код источника подтверждающего документа\" указан некорректно"
    final String LOG_TYPE_3_10 = "\"Дата начисленного дохода\" указана некорректно"
    final String LOG_TYPE_3_10_2 = "\"Дата применения вычета в текущем периоде\" не соответствует \"Дате начисления дохода\""
    final String LOG_TYPE_3_11 = "\"Код начисленного дохода\" указан некорректно"
    final String LOG_TYPE_3_12 = "\"Сумма начисленного дохода\" указана некорректно"
    final String LOG_TYPE_3_16 = "\"Сумма применения вычета\" указана некорректно"

    // Сведения о доходах в виде авансовых платежей
    final String P_NOTIF_SOURCE = "Код налогового органа, выдавшего уведомление"

    // Мапа <ID_Данные о физическом лице - получателе дохода, NdflPersonFL>
    Map<Long, NdflPersonFL> ndflPersonFLMap = [:]
    /**
     * Карта соответствия адреса формы адресу в справочнике ФИАС
     */
    Map<Long, FiasCheckInfo> fiasAddressIdsCache = [:];

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

    Map<Long, List<Map<String, RefBookValue>>> inpActualCache = [:]

    Map<Long, Map<Long, Map<String, RefBookValue>>> dulActualCache = [:]

    //Адреса физлиц
    Map<Long, Map<String, RefBookValue>> addressCache = [:]

    // Кэш провайдеров cправочников
    Map<Long, RefBookDataProvider> providerCache = [:]

    final FormDataKind FORM_DATA_KIND = FormDataKind.PRIMARY;

    // Дата окончания отчетного периода
    Date periodEndDate = null

    // Дата начала отчетного периода
    Date reportPeriodStartDate = null

    private Check() {
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    Check(scriptClass) {
        super(scriptClass)
        if (scriptClass.getBinding().hasVariable("departmentService")) {
            this.departmentService = (DepartmentService) scriptClass.getProperty("departmentService");
        }
        if (scriptClass.getBinding().hasVariable("reportPeriodService")) {
            this.reportPeriodService = (ReportPeriodService) scriptClass.getProperty("reportPeriodService");
        }
        if (scriptClass.getBinding().hasVariable("declarationData")) {
            this.declarationData = (DeclarationData) scriptClass.getProperty("declarationData");
        }
        if (scriptClass.getBinding().hasVariable("refBookFactory")) {
            this.refBookFactory = (RefBookFactory) scriptClass.getProperty("refBookFactory");
        }
        if (scriptClass.getBinding().hasVariable("fiasRefBookService")) {
            this.fiasRefBookService = (FiasRefBookService) scriptClass.getProperty("fiasRefBookService");
        }
        if (scriptClass.getBinding().hasVariable("calendarService")) {
            this.calendarService = (CalendarService) scriptClass.getProperty("calendarService");
        }
        if (scriptClass.getBinding().hasVariable("ndflPersonService")) {
            this.ndflPersonService = (NdflPersonService) scriptClass.getProperty("ndflPersonService");
        }
        if (scriptClass.getBinding().hasVariable("departmentReportPeriodService")) {
            this.departmentReportPeriodService = (DepartmentReportPeriodService) scriptClass.getProperty("departmentReportPeriodService");
        }
        if (scriptClass.getBinding().hasVariable("declarationService")) {
            this.declarationService = (DeclarationService) scriptClass.getProperty("declarationService");
        }
    }

    @Override
    public void run() {
        initConfiguration()
        switch (formDataEvent) {
            case FormDataEvent.CHECK:
                ScriptUtils.checkInterrupted();

                long time = System.currentTimeMillis();
                // Реквизиты
                List<NdflPerson> ndflPersonList = ndflPersonService.findNdflPerson(declarationData.id)
                logForDebug(SUCCESS_GET_TABLE, T_PERSON_NAME, ndflPersonList.size())

                // Сведения о доходах и НДФЛ
                List<NdflPersonIncome> ndflPersonIncomeList = ndflPersonService.findNdflPersonIncome(declarationData.id)
                logForDebug(SUCCESS_GET_TABLE, T_PERSON_INCOME_NAME, ndflPersonIncomeList.size())

                // Сведения о вычетах
                List<NdflPersonDeduction> ndflPersonDeductionList = ndflPersonService.findNdflPersonDeduction(declarationData.id)
                logForDebug(SUCCESS_GET_TABLE, T_PERSON_DEDUCTION_NAME, ndflPersonDeductionList.size())

                // Сведения о доходах в виде авансовых платежей
                List<NdflPersonPrepayment> ndflPersonPrepaymentList = ndflPersonService.findNdflPersonPrepayment(declarationData.id)
                logForDebug(SUCCESS_GET_TABLE, T_PERSON_PREPAYMENT_NAME, ndflPersonPrepaymentList.size())

                logForDebug("Получение записей из таблиц НФДЛ (" + (System.currentTimeMillis() - time) + " мс)");

                time = System.currentTimeMillis();
                // ФЛ Map<person_id, RefBook>
                Map<Long, Map<String, RefBookValue>> personMap = getActualRefPersonsByDeclarationDataId(declarationData.id)
                logForDebug(SUCCESS_GET_TABLE, R_PERSON, personMap.size())

                logForDebug("Проверки на соответствие справочникам / Выгрузка справочника Физические лица (" + (System.currentTimeMillis() - time) + " мс)");

                ScriptUtils.checkInterrupted();

                // Проверки на соответствие справочникам
                checkDataReference(ndflPersonList, ndflPersonIncomeList, ndflPersonDeductionList, ndflPersonPrepaymentList, personMap)

                ScriptUtils.checkInterrupted();

                // Общие проверки
                checkDataCommon(ndflPersonList, ndflPersonIncomeList, personMap)

                ScriptUtils.checkInterrupted();

                // Проверки сведений о доходах
                checkDataIncome(ndflPersonList, ndflPersonIncomeList, ndflPersonDeductionList, ndflPersonPrepaymentList, personMap)

                ScriptUtils.checkInterrupted();

                // Проверки Сведения о вычетах
                checkDataDeduction(ndflPersonList, ndflPersonIncomeList, ndflPersonDeductionList, personMap)

                logForDebug("Все проверки (" + (System.currentTimeMillis() - time) + " мс)");
        }
    }

    /**
     * Получить актуальные на отчетную дату записи справочника "Физические лица"
     * @return Map < person_id , Map < имя_поля , значение_поля > >
     */
    Map<Long, Map<String, RefBookValue>> getActualRefPersonsByDeclarationDataId(declarationDataId) {
        String whereClause = """
                    JOIN ref_book_person p ON (frb.record_id = p.record_id)
                    JOIN ndfl_person np ON (np.declaration_data_id = ${declarationDataId} AND p.id = np.person_id)
                """
        Map<Long, Map<String, RefBookValue>> refBookMap = getRefBookByRecordVersionWhere(RefBook.Id.PERSON.id, whereClause, getReportPeriodEndDate() - 1)
        Map<Long, Map<String, RefBookValue>> refBookMapResult = new HashMap<Long, Map<String, RefBookValue>>();
        refBookMap.each { Long personId, Map<String, RefBookValue> refBookValue ->
            Long refBookRecordId = (Long) refBookValue.get(RF_RECORD_ID).value
            refBookMapResult.put(refBookRecordId, refBookValue)
        }
        return refBookMapResult
    }

    /**
     * Проверки на соответствие справочникам
     * @return
     */
    def checkDataReference(
            List<NdflPerson> ndflPersonList, List<NdflPersonIncome> ndflPersonIncomeList, List<NdflPersonDeduction> ndflPersonDeductionList,
            List<NdflPersonPrepayment> ndflPersonPrepaymentList, Map<Long, Map<String, RefBookValue>> personMap) {

        long time = System.currentTimeMillis();
        // Страны
        def citizenshipCodeMap = getRefCountryCode()
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

        // Виды доходов Map<REF_BOOK_INCOME_KIND.MARK, List<REF_BOOK_INCOME_KIND.INCOME_TYPE_ID>>
        Map<String, List<Long>> incomeTypeMap = getRefIncomeType()
        logForDebug(SUCCESS_GET_REF_BOOK, R_INCOME_TYPE, incomeTypeMap.size())

        // Коды видов вычетов
        List<String> deductionTypeList = getRefDeductionType()
        logForDebug(SUCCESS_GET_REF_BOOK, R_TYPE_CODE, deductionTypeList.size())

        // Коды налоговых органов
        List<String> taxInspectionList = getRefNotifSource()
        logForDebug(SUCCESS_GET_REF_BOOK, R_NOTIF_SOURCE, taxInspectionList.size())

        logForDebug("Проверки на соответствие справочникам / Выгрузка справочников (" + (System.currentTimeMillis() - time) + " мс)");

        time = System.currentTimeMillis();
        // ИНП Map<person_id, List<RefBook>>
        Map<Long, List<Map<String, RefBookValue>>> inpMap = getActualRefInpMapByDeclarationDataId()
        logForDebug(SUCCESS_GET_TABLE, R_INP, inpMap.size())
        logForDebug("Проверки на соответствие справочникам / Выгрузка справочника ИНП (" + (System.currentTimeMillis() - time) + " мс)");

        time = System.currentTimeMillis();
        // ДУЛ Map<person_id, List<RefBook>>
        Map<Long, Map<Long, Map<String, RefBookValue>>> dulMap = getActualRefDulByDeclarationDataId()
        logForDebug(SUCCESS_GET_TABLE, R_DUL, dulMap.size())
        logForDebug("Проверки на соответствие справочникам / Выгрузка справочника ДУЛ (" + (System.currentTimeMillis() - time) + " мс)");

        // Получим Мапу адресов
        // Адреса
        def addressIds = []
        Map<Long, Map<String, RefBookValue>> addressMap = [:]
        time = System.currentTimeMillis();
        personMap.each { recordId, person ->
            // Сохраним идентификаторы адресов в коллекцию
            if (person.get(RF_ADDRESS).value != null) {
                addressIds.add(person.get(RF_ADDRESS).value)
            }
        }
        if (addressIds.size() > 0) {
            addressMap = getRefAddress(addressIds)
            logForDebug(SUCCESS_GET_TABLE, R_ADDRESS, addressMap.size())
        }
        logForDebug("Проверки на соответствие справочникам / Выгрузка справочника Адреса (" + (System.currentTimeMillis() - time) + " мс)");

        //поиск всех адресов формы в справочнике ФИАС
        time = System.currentTimeMillis();

        //первый запрос, проверяет что адрес присутствует в фиас
        Map<Long, FiasCheckInfo> checkFiasExistAddressMap = getFiasAddressIdsMap();

        logForDebug("Проверки на соответствие справочникам / Выгрузка справочника $R_FIAS (" + (System.currentTimeMillis() - time) + " мс)");

        long timeIsExistsAddress = 0
        time = System.currentTimeMillis();
        //в таком цикле не отображается номер строки при ошибках ndflPersonList.each { ndflPerson ->}
        for (NdflPerson ndflPerson : ndflPersonList) {

            ScriptUtils.checkInterrupted();

            NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPerson.id)
            if (ndflPersonFL == null) {
                if (FORM_DATA_KIND.equals(FormDataKind.PRIMARY)) {
                    // РНУ-НДФЛ первичная
                    String fio = (ndflPerson.lastName ?: "") + " " + (ndflPerson.firstName ?: "") + " " + (ndflPerson.middleName ?: "")
                    ndflPersonFL = new NdflPersonFL(fio, ndflPerson.inp ?: "")
                } else {
                    // РНУ-НДФЛ консолидированная
                    Map<String, RefBookValue> personRecord = personMap.get(ndflPerson.recordId)
                    String fio = (personRecord.get(RF_LAST_NAME).value?.toString() ?: "") + " " + (personRecord.get(RF_FIRST_NAME).value?.toString() ?: "") + " " + (personRecord.get(RF_MIDDLE_NAME).value?.toString() ?: "")
                    ndflPersonFL = new NdflPersonFL(fio, ndflPerson.recordId.toString())
                }
                ndflPersonFLMap.put(ndflPerson.id, ndflPersonFL)
            }
            String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [ndflPersonFL.fio, ndflPersonFL.inp])

            // Спр1 ФИАС
            // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-448

            long tIsExistsAddress = System.currentTimeMillis();
            if (!isPersonAddressEmpty(ndflPerson)) {

                List<String> address = []
                FiasCheckInfo fiasCheckInfo = checkFiasExistAddressMap.get(ndflPerson.id)
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                if (!ScriptUtils.isEmpty(ndflPerson.regionCode) && !fiasCheckInfo.validRegion) {
                    logFiasError(fioAndInp, pathError, "Код субъекта", ndflPerson.regionCode)
                } else if (!ScriptUtils.isEmpty(ndflPerson.area) && !fiasCheckInfo.validArea) {
                    logFiasError(fioAndInp, pathError, "Район", ndflPerson.area)
                } else if (!ScriptUtils.isEmpty(ndflPerson.city) && !fiasCheckInfo.validCity) {
                    logFiasError(fioAndInp, pathError, "Город", ndflPerson.city)
                } else if (!ScriptUtils.isEmpty(ndflPerson.locality) && !fiasCheckInfo.validLoc) {
                    logFiasError(fioAndInp, pathError, "Населенный пункт", ndflPerson.locality)
                } else if (!ScriptUtils.isEmpty(ndflPerson.street) && !fiasCheckInfo.validStreet) {
                    logFiasError(fioAndInp, pathError, "Улица", ndflPerson.street)
                }
                if (ndflPerson.postIndex != null && !ndflPerson.postIndex.matches("[0-9]{6}")) {
                    logFiasIndexError(fioAndInp, pathError, "Индекс", ndflPerson.postIndex)
                }
            }
            timeIsExistsAddress += System.currentTimeMillis() - tIsExistsAddress

            // Спр2 Гражданство (Обязательное поле)
            if (ndflPerson.citizenship != null && !citizenshipCodeMap.find { key, value -> value == ndflPerson.citizenship }) {
                //TODO turn_to_error
                String errMsg = String.format(LOG_TYPE_PERSON_MSG_2,
                        C_CITIZENSHIP, ndflPerson.citizenship ?: "",
                        R_CITIZENSHIP
                )
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                logger.logCheck("%s. %s.",
                        declarationService.isCheckFatal(DeclarationCheckCode.RNU_CITIZENSHIP, declarationData.declarationTemplateId),
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
            if (ndflPerson.personId == null || ndflPerson.personId == 0) {
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                logger.errorExp("%s. %s.", "Отсутствует связь со справочником \"Физические лица\"", fioAndInp, pathError,
                        "Не удалось установить связь со справочником \"$R_PERSON\"")
            } else {
                def personRecord = personMap.get(ndflPerson.recordId)

                if (!personRecord) {
                    //TODO turn_to_error
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                    logger.errorExp("%s. %s.", "Отсутствует связь со справочником \"Физические лица\"", fioAndInp, pathError,
                            "Не удалось установить связь со справочником \"$R_PERSON\"")
                } else {
                    // Спр11 Фамилия (Обязательное поле)
                    if (personRecord.get(RF_LAST_NAME).value != null && !ndflPerson.lastName.equals(personRecord.get(RF_LAST_NAME).value)) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "ФИО не соответствует справочнику \"Физические лица\"", fioAndInp, pathError,
                                String.format(LOG_TYPE_PERSON_MSG, "Фамилия", ndflPerson.lastName ?: "", R_PERSON))
                    }

                    // Спр11 Имя (Обязательное поле)
                    if (personRecord.get(RF_FIRST_NAME).value != null && !ndflPerson.firstName.equals(personRecord.get(RF_FIRST_NAME).value)) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "ФИО не соответствует справочнику \"Физические лица\"", fioAndInp, pathError,
                                String.format(LOG_TYPE_PERSON_MSG, "Имя", ndflPerson.firstName ?: "", R_PERSON))
                    }

                    // Спр11 Отчество (Необязательное поле)
                    if (personRecord.get(RF_MIDDLE_NAME).value != null && ndflPerson.middleName != null && !ndflPerson.middleName.equals(personRecord.get(RF_MIDDLE_NAME).value)) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "ФИО не соответствует справочнику \"Физические лица\"", fioAndInp, pathError,
                                String.format(LOG_TYPE_PERSON_MSG, "Отчество", ndflPerson.middleName ?: "", R_PERSON))
                    }

                    if (FORM_DATA_KIND.equals(FormDataKind.PRIMARY)) {
                        // Спр12 ИНП первичная (Обязательное поле)
                        List<Map<String, RefBookValue>> record = inpMap.get(personRecord.get("id")?.value)
                        List<String> inpList = new LinkedList<String>()
                        for (Map<String, RefBookValue> value : record) {
                            if (value.get("INP") != null) {
                                inpList.add(value.get("INP").getStringValue())
                            }
                        }
                        if (!(ndflPerson.inp == personRecord.get(RF_SNILS)?.value || inpList?.contains(ndflPerson.inp))) {
                            String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                            logger.warnExp("%s. %s.", "ИНП не соответствует справочнику \"Физические лица\"", fioAndInp, pathError,
                                    String.format(LOG_TYPE_PERSON_MSG, "ИНП", ndflPerson.inp ?: "", R_PERSON))
                        }
                    } else {
                        //Спр12.1 ИНП консолидированная - проверка соответствия RECORD_ID
                        //if (formType == CONSOLIDATE){}
                        String recordId = String.valueOf(personRecord.get(RF_RECORD_ID).getNumberValue().longValue());
                        if (!ndflPerson.inp.equals(recordId)) {
                            //TODO turn_to_error
                            String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                            logger.warnExp("%s. %s.", "ИНП не соответствует справочнику \"Физические лица\"", fioAndInp, pathError,
                                    String.format(LOG_TYPE_PERSON_MSG, "ИНП", ndflPerson.inp ?: "", R_PERSON))
                        }
                    }
                    // Спр13 Дата рождения (Обязательное поле)
                    if (personRecord.get(RF_BIRTH_DATE).value != null && !ndflPerson.birthDay.equals(new LocalDateTime(personRecord.get(RF_BIRTH_DATE).getDateValue()))) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "Дата рождения не соответствует справочнику \"Физические лица\"", fioAndInp, pathError,
                                String.format(LOG_TYPE_PERSON_MSG, "Дата рождения", ndflPerson.birthDay ? ScriptUtils.formatDate(ndflPerson.birthDay) : "", R_PERSON))
                    }

                    // Спр14 Гражданство (Обязательное поле)
                    def citizenship = citizenshipCodeMap.get(personRecord.get(RF_CITIZENSHIP).value)
                    if (ndflPerson.citizenship != null && !ndflPerson.citizenship.equals(citizenship)) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "Код гражданства не соответствует справочнику \"Физические лица\"", fioAndInp, pathError,
                                String.format(LOG_TYPE_PERSON_MSG, C_CITIZENSHIP, ndflPerson.citizenship ?: "", R_PERSON))
                    }

                    // Спр15 ИНН.В Российской федерации (Необязательное поле)
                    if (ndflPerson.innNp != null && !ndflPerson.innNp.equals(personRecord.get(RF_INN).value)) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "ИНН в РФ не соответствует справочнику \"Физические лица\"", fioAndInp, pathError,
                                String.format(LOG_TYPE_PERSON_MSG, "ИНН в РФ", ndflPerson.innNp ?: "", R_PERSON))
                    }

                    // Спр16 ИНН.В стране гражданства (Необязательное поле)
                    if (ndflPerson.innForeign != null && !ndflPerson.innForeign.equals(personRecord.get(RF_INN_FOREIGN).value)) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "ИНН в ИНО не соответствует справочнику \"Физические лица\"", fioAndInp, pathError,
                                String.format(LOG_TYPE_PERSON_MSG, "ИНН в ИНО", ndflPerson.innForeign ?: "", R_PERSON))
                    }

                    if (FORM_DATA_KIND.equals(FormDataKind.PRIMARY)) {
                        // Спр17 Документ удостоверяющий личность (Первичная) (Обязательное поле)
                        Map<Long, Map<String, RefBookValue>> allDocList = dulMap.get(personRecord.get("id")?.value)
                        // Вид документа
                        def personDocTypeList = []
                        // Серия и номер документа
                        def personDocNumberList = []
                        allDocList.each { Long key, Map<String, RefBookValue> dul ->
                            personDocTypeList.add(documentTypeMap.get(dul.get(RF_DOC_ID).value))
                            personDocNumberList.add(BaseWeigthCalculator.prepareStringDul(dul.get(RF_DOC_NUMBER).getStringValue()).toUpperCase())
                        }
                        if (ndflPerson.idDocType != null && !personDocTypeList.contains(ndflPerson.idDocType)) {
                            String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                            logger.warnExp("%s. %s.", "Код и номер ДУЛ не соответствуют справочнику \"Физические лица\"", fioAndInp, pathError,
                                    String.format(LOG_TYPE_PERSON_MSG, "ДУЛ Код", ndflPerson.idDocType ?: "", R_PERSON))
                        }
                        if (ndflPerson.idDocNumber != null && !personDocNumberList.contains(BaseWeigthCalculator.prepareStringDul(ndflPerson.idDocNumber).toUpperCase())) {
                            String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                            logger.warnExp("%s. %s.", "Код и номер ДУЛ не соответствуют справочнику \"Физические лица\"", fioAndInp, pathError,
                                    String.format(LOG_TYPE_PERSON_MSG, "ДУЛ Номер", ndflPerson.idDocNumber ?: "", R_PERSON))
                        }
                    } else {
                        Map<Long, Map<String, RefBookValue>> allDocList = dulMap.get(ndflPerson.personId)
                        //Ищем в справочнике запись по параметрам код документа и номер
                        Map<String, RefBookValue> dulRecordValues = [:]
                        allDocList.each { Long key, Map<String, RefBookValue> recordValues ->
                            String docTypeCode = documentTypeMap.get(recordValues.get(RF_DOC_ID).getReferenceValue())
                            String docNumber = BaseWeigthCalculator.prepareStringDul(recordValues.get(RF_DOC_NUMBER).getStringValue()).toUpperCase()
                            if (ndflPerson.idDocType.equals(docTypeCode) && BaseWeigthCalculator.prepareStringDul(ndflPerson.idDocNumber).toUpperCase().equals(docNumber)) {
                                dulRecordValues.putAll(recordValues)
                            }
                        }

                        if (dulRecordValues == null) {
                            String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                            logger.warnExp("%s. %s.", "Код и номер ДУЛ не соответствуют справочнику \"Физические лица\"", fioAndInp, pathError,
                                    String.format(LOG_TYPE_PERSON_MSG, "ДУЛ Код\" (\"${ndflPerson.idDocType ?: ""}\"), \"ДУЛ Номер", ndflPerson.idDocNumber ?: "", R_PERSON))
                        } else {
                            int incRep = dulRecordValues.get(RF_INC_REP).getNumberValue().intValue()
                            if (incRep != 1) {
                                String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                                logger.warnExp("%s. %s.", "Код и номер ДУЛ не соответствуют справочнику \"Физические лица\"", fioAndInp, pathError,
                                        "\"ДУЛ Номер\" не включается в отчетность")
                            }
                        }
                    }

                    // Спр18 Статус налогоплательщика (Обязательное поле)
                    def taxpayerStatus = taxpayerStatusMap.get(personRecord.get(RF_TAXPAYER_STATE).value)
                    if (ndflPerson.status != null && !ndflPerson.status.equals(taxpayerStatus)) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "Статус налогоплательщица не соответствует справочнику \"Физические лица\"", fioAndInp, pathError,
                                String.format(LOG_TYPE_PERSON_MSG, C_STATUS, ndflPerson.status ?: "", R_PERSON))
                    }

                    // Спр19 Адрес (Необязательное поле)
                    // Сравнение должно быть проведено даже с учетом пропусков
                    Map<String, RefBookValue> address = addressMap.get(personRecord.get(RF_ADDRESS).value)
                    def regionCode
                    def area
                    def city
                    def locality
                    def street
                    def house
                    def building
                    def flat
                    if (address != null) {
                        regionCode = address.get(RF_REGION_CODE).value
                        area = address.get(RF_DISTRICT).value
                        city = address.get(RF_CITY).value
                        locality = address.get(RF_LOCALITY).value
                        street = address.get(RF_STREET).value
                        house = address.get(RF_HOUSE).value
                        building = address.get(RF_BUILD).value
                        flat = address.get(RF_APPARTMENT).value
                    }

                    List<String> ndflPersonAddress = []

                    // Адрес регистрации в Российской Федерации.Код субъекта
                    if (ndflPerson.regionCode != null && !ndflPerson.regionCode.equals(regionCode)) {
                        ndflPersonAddress.add("Код субъекта=" + ndflPerson.regionCode ?: "")
                    }

                    // Адрес регистрации в Российской Федерации.Район
                    if (ndflPerson.area != null && !ndflPerson.area.equals(area)) {
                        ndflPersonAddress.add("Район=" + ndflPerson.area ?: "")
                    }

                    // Адрес регистрации в Российской Федерации.Город
                    if (ndflPerson.city != null && !ndflPerson.city.equals(city)) {
                        ndflPersonAddress.add("Город=" + ndflPerson.city ?: "")
                    }

                    // Адрес регистрации в Российской Федерации.Населенный пункт
                    if (ndflPerson.locality != null && !ndflPerson.locality.equals(locality)) {
                        ndflPersonAddress.add("Населенный пункт=" + ndflPerson.locality ?: "")
                    }

                    // Адрес регистрации в Российской Федерации.Улица
                    if (ndflPerson.street != null && !ndflPerson.street.equals(street)) {
                        ndflPersonAddress.add("Улица=" + ndflPerson.street ?: "")
                    }

                    // Адрес регистрации в Российской Федерации.Дом
                    if (ndflPerson.house != null && !ndflPerson.house.equals(house)) {
                        ndflPersonAddress.add("Дом=" + ndflPerson.house ?: "")
                    }

                    // Адрес регистрации в Российской Федерации.Корпус
                    if (ndflPerson.building != null && !ndflPerson.building.equals(building)) {
                        ndflPersonAddress.add("Корпус=" + ndflPerson.building ?: "")
                    }

                    // Адрес регистрации в Российской Федерации.Квартира
                    if (ndflPerson.flat != null && !ndflPerson.flat.equals(flat)) {
                        ndflPersonAddress.add("Квартира=" + ndflPerson.flat ?: "")
                    }
                    if (!ndflPersonAddress.isEmpty()) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "Адрес не соответствует справочнику \"Физические лица\"", fioAndInp, pathError,
                                String.format(LOG_TYPE_PERSON_MSG, "Форма.Реквизиты.Адрес регистрации в Российской Федерации", ndflPersonAddress.join(", "), R_PERSON))

                    }
                }
            }
        }
        logForDebug("Проверки на соответствие справочникам / " + T_PERSON_NAME + " (" + (System.currentTimeMillis() - time) + " мс)");

        logForDebug("Проверки на соответствие справочникам / Проверка существования адреса (" + timeIsExistsAddress + " мс)");

        time = System.currentTimeMillis();
        for (NdflPersonIncome ndflPersonIncome : ndflPersonIncomeList) {

            ScriptUtils.checkInterrupted();

            NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPersonIncome.ndflPersonId)
            String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [ndflPersonFL.fio, ndflPersonFL.inp])

            // Спр5 Код вида дохода (Необязательное поле)
            if (ndflPersonIncome.incomeCode != null && ndflPersonIncome.incomeAccruedDate != null && !incomeCodeMap.find { key, value ->
                value.CODE?.stringValue == ndflPersonIncome.incomeCode &&
                        ndflPersonIncome.incomeAccruedDate.toDate() >= value.record_version_from?.dateValue &&
                        ndflPersonIncome.incomeAccruedDate.toDate() <= value.record_version_to?.dateValue
            }) {
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
                incomeTypeMap <REF_BOOK_INCOME_KIND.MARK, List<REF_BOOK_INCOME_KIND.INCOME_TYPE_ID>>

                Доход.Вид.Код (Графа 4) - (Необязательное поле)
                incomeCodeMap <REF_BOOK_INCOME_TYPE.ID, REF_BOOK_INCOME_TYPE>
                 */
            if (ndflPersonIncome.incomeType != null && !ScriptUtils.isEmpty(ndflPersonIncome.incomeType)) {
                List<Long> incomeTypeIdList = incomeTypeMap.get(ndflPersonIncome.incomeType)
                if (incomeTypeIdList == null || incomeTypeIdList.isEmpty()) {
                    String errMsg = String.format(LOG_TYPE_PERSON_MSG_2,
                            C_INCOME_TYPE, ndflPersonIncome.incomeType ?: "",
                            R_INCOME_TYPE
                    )
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                    logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_INCOME_TYPE), fioAndInp, pathError, errMsg)
                } else {
                    if (!ScriptUtils.isEmpty(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeAccruedDate != null) {
                        List<Map<String, RefBookValue>> incomeCodeRefList = []
                        incomeTypeIdList.each { incomeTypeId ->
                            def incomeCodeRef = incomeCodeMap.get(incomeTypeId)
                            incomeCodeRefList.add(incomeCodeRef)
                        }
                        Map<String, RefBookValue> incomeCodeRef = incomeCodeRefList.find { Map<String, RefBookValue> value ->
                            value?.CODE?.stringValue == ndflPersonIncome.incomeCode &&
                                    ndflPersonIncome.incomeAccruedDate >= new LocalDateTime(value.record_version_from?.dateValue) &&
                                    ndflPersonIncome.incomeAccruedDate <= new LocalDateTime(value.record_version_to?.dateValue)
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
        logForDebug("Проверки на соответствие справочникам / '${T_PERSON_INCOME_NAME}' (" + (System.currentTimeMillis() - time) + " мс)");

        time = System.currentTimeMillis();
        for (NdflPersonDeduction ndflPersonDeduction : ndflPersonDeductionList) {

            ScriptUtils.checkInterrupted();

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
        logForDebug("Проверки на соответствие справочникам / '${T_PERSON_DEDUCTION_NAME}' (" + (System.currentTimeMillis() - time) + " мс)");

        time = System.currentTimeMillis();
        for (NdflPersonPrepayment ndflPersonPrepayment : ndflPersonPrepaymentList) {

            ScriptUtils.checkInterrupted();

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
        logForDebug("Проверки на соответствие справочникам / '${T_PERSON_PREPAYMENT_NAME}' (" + (System.currentTimeMillis() - time) + " мс)");
    }

    /**
     * Общие проверки
     */
    def checkDataCommon(List<NdflPerson> ndflPersonList, List<NdflPersonIncome> ndflPersonIncomeList, Map<Long, Map<String, RefBookValue>> personMap) {
        long time = System.currentTimeMillis();
        long timeTotal = time

        logForDebug("Общие проверки: инициализация (" + (System.currentTimeMillis() - time) + " мс)");

        time = System.currentTimeMillis();

        for (NdflPerson ndflPerson : ndflPersonList) {

            ScriptUtils.checkInterrupted();

            NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPerson.id)
            if (ndflPersonFL == null) {
                if (FORM_DATA_KIND.equals(FormDataKind.PRIMARY)) {
                    // РНУ-НДФЛ первичная
                    String fio = (ndflPerson.lastName ?: "") + " " + (ndflPerson.firstName ?: "") + " " + (ndflPerson.middleName ?: "")
                    ndflPersonFL = new NdflPersonFL(fio, ndflPerson.inp ?: "")
                } else {
                    // РНУ-НДФЛ консолидированная
                    def personRecord = personMap.get(ndflPerson.recordId)
                    String fio = (personRecord.get(RF_LAST_NAME).value.toString() ?: "") + " " + (personRecord.get(RF_FIRST_NAME).value.toString() ?: "") + " " + (personRecord.get(RF_MIDDLE_NAME).value.toString() ?: "")
                    ndflPersonFL = new NdflPersonFL(fio, ndflPerson.recordId.toString())
                }
                ndflPersonFLMap.put(ndflPerson.id, ndflPersonFL)
            }
            String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [ndflPersonFL.fio, ndflPersonFL.inp])

            // Общ1 Корректность ИНН
            if (ndflPerson.citizenship == "643") {
                if (ndflPerson.innNp == null) {
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                    logger.warnExp("%s. %s.", "\"ИНН\" не указан", fioAndInp, pathError,
                            "Значение гр. \"ИНН в РФ\" не указано. Прием налоговым органом обеспечивается, может быть предупреждение")
                } else {
                    String checkInn = ScriptUtils.checkInn(ndflPerson.innNp)
                    if (checkInn != null) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.errorExp("%s. %s.", "\"ИНН\" не соответствует формату", fioAndInp, pathError,
                                checkInn)
                    }
                }
            }

            //Общ2 Наличие обязательных реквизитов для формирования отчетности
            boolean checkLastName = checkRequiredAttribute(ndflPerson, fioAndInp, "lastName", "Фамилия")
            boolean checkFirstName = checkRequiredAttribute(ndflPerson, fioAndInp, "firstName", "Имя")
            checkRequiredAttribute(ndflPerson, fioAndInp, "birthDay", "Дата рождения")
            boolean checkCitizenship = checkRequiredAttribute(ndflPerson, fioAndInp, "citizenship", C_CITIZENSHIP)
            boolean checkIdDocType = checkRequiredAttribute(ndflPerson, fioAndInp, "idDocType", "ДУЛ Код")
            boolean checkIdDocNumber = checkRequiredAttribute(ndflPerson, fioAndInp, "idDocNumber", "ДУЛ Номер")
            checkRequiredAttribute(ndflPerson, fioAndInp, "status", C_STATUS)
            if (checkCitizenship) {
                if (ndflPerson.citizenship == "643") {
                    checkRequiredAttribute(ndflPerson, fioAndInp, "regionCode", "Код субъекта")
                } else {
                    checkRequiredAttribute(ndflPerson, fioAndInp, "countryCode", "Код страны проживания вне РФ")
                    checkRequiredAttribute(ndflPerson, fioAndInp, "address", "Адрес проживания вне РФ ")
                }
            }

            if (ndflPerson.citizenship == "643") {
                if (checkLastName) {
                    String checkName = ScriptUtils.checkName(ndflPerson.lastName, "Фамилия")
                    if (checkName != null) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "\"Фамилия\", \"Имя\" не соответствует формату", fioAndInp, pathError,
                                checkName)
                    }
                }
                if (checkFirstName) {
                    String checkName = ScriptUtils.checkName(ndflPerson.firstName, "Имя")
                    if (checkName != null) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "\"Фамилия\", \"Имя\" не соответствует формату", fioAndInp, pathError,
                                checkName)
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
        logForDebug("Общие проверки / '${T_PERSON_NAME}' (" + (System.currentTimeMillis() - time) + " мс)");

        time = System.currentTimeMillis();
        Department department = departmentService.get(declarationData.departmentId)
        for (NdflPersonIncome ndflPersonIncome : ndflPersonIncomeList) {

            ScriptUtils.checkInterrupted();

            boolean applyTemporalySolution = true
//            if (ndflPersonIncome.incomeAccruedSumm == ndflPersonIncome.totalDeductionsSumm) {
//                applyTemporalySolution = true
//            }

            def operationId = ndflPersonIncome.operationId ?: ""
            NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPersonIncome.ndflPersonId)
            String fioAndInpAndOperId = sprintf(TEMPLATE_PERSON_FL_OPER, [ndflPersonFL.fio, ndflPersonFL.inp, operationId])

            // Общ5 Принадлежность дат операций к отчетному периоду. Проверка перенесана в событие загрузки ТФ

            // Общ7 Наличие или отсутствие значения в графе в зависимости от условий
            List<ColumnFillConditionData> columnFillConditionDataList = []
            //1 Раздел 2. Графа 4 должна быть заполнена, если не заполнены Раздел 2. Графы 22,23,24
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column22And23And24NotFill(),
                    new Column4Fill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" должна быть заполнена, так как не заполнены гр. \"%s\", \"%s\", \"%s\"",
                            C_INCOME_CODE,
                            C_PAYMENT_DATE,
                            C_PAYMENT_NUMBER,
                            C_TAX_SUMM
                    )
            )
            //1 Раздел 2. Графа 5 должна быть заполнена, если не заполнены Раздел 2. Графы 22,23,24
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column22And23And24NotFill(),
                    new Column5Fill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" должна быть заполнена, так как не заполнены гр. \"%s\", \"%s\", \"%s\"",
                            C_INCOME_TYPE,
                            C_PAYMENT_DATE,
                            C_PAYMENT_NUMBER,
                            C_TAX_SUMM
                    )
            )
            //2 Раздел 2. Графа 6 должна быть заполнена, если заполнена Раздел 2. Графа 10
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column10Fill(),
                    new Column6Fill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                            C_INCOME_ACCRUED_DATE,
                            C_INCOME_ACCRUED_SUMM
                    )
            )
            //3 Раздел 2. Графа 7 должна быть заполнена, если заполнена Раздел 2. Графа 11
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column11Fill(),
                    new Column7Fill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                            C_INCOME_PAYOUT_DATE,
                            C_INCOME_PAYOUT_SUMM
                    )
            )
            //3 Раздел 2. Графа 8 Должна быть всегда заполнена
            columnFillConditionDataList << new ColumnFillConditionData(
                    new ColumnTrueFillOrNotFill(),
                    new Column8Fill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Не заполнена гр. \"%s\"",
                            C_OKTMO
                    )
            )
            //3 Раздел 2. Графа 9 Должна быть всегда заполнена
            columnFillConditionDataList << new ColumnFillConditionData(
                    new ColumnTrueFillOrNotFill(),
                    new Column8Fill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Не заполнена гр. \"%s\"",
                            C_KPP
                    )
            )
            //4 Раздел 2. Графа 10 должна быть заполнена, если заполнена Раздел 2. Графа 6
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column6Fill(),
                    new Column10Fill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                            C_INCOME_ACCRUED_SUMM,
                            C_INCOME_ACCRUED_DATE
                    )
            )
            //5 Раздел 2. Графа 11 должна быть заполнена, если заполнена Раздел 2. Графа 7
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column7Fill(),
                    new Column11Fill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                            C_INCOME_PAYOUT_SUMM,
                            C_INCOME_PAYOUT_DATE
                    )
            )
            //6 Раздел 2. Графа 12 должна быть не заполнена, если заполнены Раздел 2. Графы 22, 23, 24
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column22And23And24Fill(),
                    new Column12NotFill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" (\"%s\") не должна быть заполнена, так как заполнены гр. \"%s\", \"%s\", \"%s\"",
                            C_TOTAL_DEDUCTIONS_SUMM, ndflPersonIncome.totalDeductionsSumm ?: "",
                            C_PAYMENT_DATE,
                            C_PAYMENT_NUMBER,
                            C_TAX_SUMM
                    )
            )
            //7 Раздел 2. Графы 13 должны быть заполнены, если не заполнены Раздел 2. Графы 22, 23, 24
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column22And23And24NotFill(),
                    new Column13Fill(applyTemporalySolution),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" должна быть заполнена, так как не заполнены гр. \"%s\", \"%s\", \"%s\"",
                            C_TAX_BASE,
                            C_PAYMENT_DATE,
                            C_PAYMENT_NUMBER,
                            C_TAX_SUMM
                    )
            )
            //7 Раздел 2. Графы 14 должны быть заполнены, если не заполнены Раздел 2. Графы 22, 23, 24
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column22And23And24NotFill(),
                    new Column14Fill(applyTemporalySolution),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" должна быть заполнена, так как не заполнены гр. \"%s\", \"%s\", \"%s\"",
                            C_TAX_RATE,
                            C_PAYMENT_DATE,
                            C_PAYMENT_NUMBER,
                            C_TAX_SUMM
                    )
            )
            //7 Раздел 2. Графы 15 должны быть заполнены, если не заполнены Раздел 2. Графы 22, 23, 24
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column22And23And24NotFill(),
                    new Column15Fill(applyTemporalySolution),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" должна быть заполнена, так как не заполнены гр. \"%s\", \"%s\", \"%s\"",
                            C_TAX_DATE,
                            C_PAYMENT_DATE,
                            C_PAYMENT_NUMBER,
                            C_TAX_SUMM
                    )
            )
            //8 Раздел 2. Графы 6 должны быть заполнены, если заполнена Раздел 2. Графа 16
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column16Fill(),
                    new Column6Fill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                            C_INCOME_ACCRUED_DATE,
                            C_CALCULATED_TAX
                    )
            )
            //8 Раздел 2. Графы 10 должны быть заполнены, если заполнена Раздел 2. Графа 16
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column16Fill(),
                    new Column10Fill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                            C_INCOME_ACCRUED_SUMM,
                            C_CALCULATED_TAX
                    )
            )
            //9 Раздел 2. Графа 6 должны быть заполнены, если заполнена Раздел 2. Графа 18 или 19
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column18Fill(),
                    new Column6Fill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                            C_INCOME_ACCRUED_DATE,
                            C_NOT_HOLDING_TAX
                    )
            )
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column19Fill(),
                    new Column6Fill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                            C_INCOME_ACCRUED_DATE,
                            C_OVERHOLDING_TAX
                    )
            )
            //9 Раздел 2. Графа 10 должны быть заполнены, если заполнена Раздел 2. Графа 18 или 19
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column18Fill(),
                    new Column10Fill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                            C_INCOME_ACCRUED_SUMM,
                            C_NOT_HOLDING_TAX
                    )
            )
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column19Fill(),
                    new Column10Fill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                            C_INCOME_ACCRUED_SUMM,
                            C_OVERHOLDING_TAX
                    )
            )
            //10 Раздел 2. Графы 7 должны быть заполнены, если заполнена Раздел 2. Графа 17
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column17Fill(),
                    new Column7Fill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                            C_INCOME_PAYOUT_DATE,
                            C_WITHHOLDING_TAX
                    )

            )
            //10 Раздел 2. Графы 11 должны быть заполнены, если заполнена Раздел 2. Графа 17
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column17Fill(),
                    new Column11Fill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                            C_INCOME_PAYOUT_SUMM,
                            C_WITHHOLDING_TAX
                    )
            )
            //11 Раздел 2. Графы 7 должны быть заполнены, если заполнена Раздел 2. Графа 20
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column20Fill(),
                    new Column7Fill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                            C_INCOME_PAYOUT_DATE,
                            C_REFOUND_TAX
                    )

            )
            //11 Раздел 2. Графы 11 должны быть заполнены, если заполнена Раздел 2. Графа 20
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column20Fill(),
                    new Column11Fill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                            C_INCOME_PAYOUT_SUMM,
                            C_REFOUND_TAX
                    )
            )
            //12 Раздел 2. Графа 21 должна быть заполнена, если заполнены Раздел 2. Графы 7, 11
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column7And11Fill(),
                    new Column21Fill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" должна быть заполнена, так как заполнены гр. \"%s\", \"%s\"",
                            C_TAX_TRANSFER_DATE,
                            C_INCOME_PAYOUT_DATE,
                            C_INCOME_PAYOUT_SUMM
                    )

            )
            //12 Раздел 2. Графа 21 должна быть заполнена, если заполнены Раздел 2. Графы 23, 23, 24
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column22And23And24Fill(),
                    new Column21Fill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" должна быть заполнена, так как заполнены гр. \"%s\", \"%s\", \"%s\"",
                            C_TAX_TRANSFER_DATE,
                            C_PAYMENT_DATE,
                            C_PAYMENT_NUMBER,
                            C_TAX_SUMM
                    )

            )
            //12 Раздел 2. Графа 21 должна быть НЕ заполнена, если НЕ заполнены Раздел 2. Графы 7, 11 и 22, 23, 24
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column7And11And22And23And24NotFill(),
                    new Column21NotFill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" (\"%s\") не должна быть заполнена, так как не заполнены гр. \"%s\", гр. \"%s\", и не заполнены гр. \"%s\", гр. \"%s\", гр. \"%s\"",
                            C_TAX_TRANSFER_DATE, ndflPersonIncome.taxTransferDate ? ScriptUtils.formatDate(ndflPersonIncome.taxTransferDate) : "",
                            C_INCOME_PAYOUT_DATE,
                            C_INCOME_PAYOUT_SUMM,
                            C_PAYMENT_DATE,
                            C_PAYMENT_NUMBER,
                            C_TAX_SUMM
                    )
            )
            //13 Должны быть либо заполнены все 3 Графы 22, 23, 24, либо ни одна из них
            columnFillConditionDataList << new ColumnFillConditionData(
                    new ColumnTrueFillOrNotFill(),
                    new Column22And23And24FillOrColumn22And23And24NotFill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\", гр. \"%s\", гр. \"%s\" должны быть заполнены одновременно или не заполнена ни одна из них",
                            C_PAYMENT_DATE,
                            C_PAYMENT_NUMBER,
                            C_TAX_SUMM
                    )

            )
            columnFillConditionDataList.each { columnFillConditionData ->
                if (columnFillConditionData.columnConditionCheckerAsIs.check(ndflPersonIncome) &&
                        !columnFillConditionData.columnConditionCheckerToBe.check(ndflPersonIncome)) {
                    logger.logCheck("%s. %s.",
                            declarationService.isCheckFatal(DeclarationCheckCode.RNU_VALUE_CONDITION, declarationData.declarationTemplateId),
                            "Наличие (отсутствие) значения в графе не соответствует алгоритму заполнения РНУ НДФЛ",
                            fioAndInpAndOperId, columnFillConditionData.conditionPath, columnFillConditionData.conditionMessage)
                }
            }
        }

        ScriptUtils.checkInterrupted();

        logForDebug("Общие проверки / " + T_PERSON_INCOME_NAME + " (" + (System.currentTimeMillis() - time) + " мс)");

        logForDebug("Общие проверки всего (" + (System.currentTimeMillis() - timeTotal) + " мс)");
    }

    /**
     * Проверки сведений о доходах
     * @param ndflPersonList
     * @param ndflPersonIncomeList
     * @param ndflPersonDeductionList
     * @param ndflPersonPrepaymentList
     */
    def checkDataIncome(List<NdflPerson> ndflPersonList, List<NdflPersonIncome> ndflPersonIncomeList, List<NdflPersonDeduction> ndflPersonDeductionList,
                        List<NdflPersonPrepayment> ndflPersonPrepaymentList, Map<Long, Map<String, RefBookValue>> personMap) {

        long time = System.currentTimeMillis()

        Map<Long, NdflPerson> personsCache = [:]
        ndflPersonList.each { ndflPerson ->
            personsCache.put(ndflPerson.id, ndflPerson)

            NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPerson.id)
            if (ndflPersonFL == null) {
                if (FORM_DATA_KIND.equals(FormDataKind.PRIMARY)) {
                    // РНУ-НДФЛ первичная
                    String fio = ndflPerson.lastName + " " + ndflPerson.firstName + " " + (ndflPerson.middleName ?: "")
                    ndflPersonFL = new NdflPersonFL(fio, ndflPerson.inp)
                } else {
                    // РНУ-НДФЛ консолидированная
                    def personRecord = personMap.get(ndflPerson.recordId)
                    String fio = personRecord.get(RF_LAST_NAME).value.toString() + " " + personRecord.get(RF_FIRST_NAME).value.toString() + " " + (personRecord.get(RF_MIDDLE_NAME).value.toString() ?: "")
                    ndflPersonFL = new NdflPersonFL(fio, ndflPerson.recordId.toString())
                }
                ndflPersonFLMap.put(ndflPerson.id, ndflPersonFL)
            }
        }

        Map<Long, List<NdflPersonPrepayment>> ndflPersonPrepaymentCache = [:]
        ndflPersonPrepaymentList.each { NdflPersonPrepayment ndflPersonPrepayment ->
            List<NdflPersonPrepayment> ndflPersonPrepaymentListByPersonIdList = ndflPersonPrepaymentCache.get(ndflPersonPrepayment.ndflPersonId) ?: new ArrayList<NdflPersonPrepayment>()
            ndflPersonPrepaymentListByPersonIdList.add(ndflPersonPrepayment)
            ndflPersonPrepaymentCache.put(ndflPersonPrepayment.ndflPersonId, ndflPersonPrepaymentListByPersonIdList)
        }

        List<DateConditionData> dateConditionDataList = []
        List<DateConditionData> dateConditionDataListForBudget = []

        DateConditionWorkDay dateConditionWorkDay = new DateConditionWorkDay(calendarService)

        // "Графа 6" = "Графе 7"
        dateConditionDataList << new DateConditionData(["1010", "1011", "3020", "1110", "1400", "2001", "2010", "2012",
                                                        "2300", "2710", "2760", "2762", "2770", "2900", "4800"],
                ["00"], new Column6EqualsColumn7(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"Дата выплаты дохода\"")

        // "Графа 6" = "Графе 7"
        dateConditionDataList << new DateConditionData(["1530", "1531", "1533", "1535", "1536", "1537", "1539",
                                                        "1541", "1542", "1543", "1544", "1545", "1546", "1547",
                                                        "1548", "1549", "1551", "1552", "1554"],
                ["01", "02"], new Column6EqualsColumn7(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"Дата выплаты дохода\"")

        // Соответствует маске 31.12.20**
        dateConditionDataList << new DateConditionData(["1530", "1531", "1533", "1535", "1536", "1537", "1539",
                                                        "1541", "1542", "1543"],
                ["04"], new MatchMask("31.12.20\\d{2}"), "Значение гр. \"%s\" (\"%s\") должно быть равно \"31.12.20**\"")

        // Последний календарный день месяца
        dateConditionDataList << new DateConditionData(["2000"], ["05"], new LastMonthCalendarDay(), "Значение гр. \"%s\" (\"%s\") должно быть равно последнему календарному дню месяца, за который был начислен доход")

        // "Графа 6" = "Графе 7"
        dateConditionDataList << new DateConditionData(["2000", "2002"], ["07"], new Column6EqualsColumn7(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"Дата выплаты дохода\"")

        // Если «графа 7» < 31.12.20**, то «графа 6» = «графа 7», иначе «графа 6» = 31.12.20**
        dateConditionDataList << new DateConditionData(["2000", "2002"], ["08", "09", "10"], new Column7LastDayOfYear1(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"Дата выплаты дохода\"")
        dateConditionDataList << new DateConditionData(["2000", "2002"], ["08", "09", "10"], new Column7LastDayOfYear2(), "Значение гр. \"%s\" (\"%s\") должно быть равно \"31.12.20**\"")

        // Последний календарный день месяца
        dateConditionDataList << new DateConditionData(["2000", "2002"], ["11"], new LastMonthCalendarDay(), "Значение гр. \"%s\" (\"%s\") должно быть равно последнему календарному дню месяца, в котором утверждён авансовый отчёт о командировке")

        // Если «графа 7» < 31.12.20**, то «графа 6» = «графа 7», иначе «графа 6» = 31.12.20**
        dateConditionDataList << new DateConditionData(["2000", "2002"], ["12"], new Column7LastDayOfYear1(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"Дата выплаты дохода\"")
        dateConditionDataList << new DateConditionData(["2000", "2002"], ["12"], new Column7LastDayOfYear2(), "Значение гр. \"%s\" (\"%s\") должно быть равно \"31.12.20**\"")

        // "Графа 6" = "Графе 7"
        dateConditionDataList << new DateConditionData(["2520", "2720", "2740", "2750", "2790"], ["00"], new Column6EqualsColumn7(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"Дата выплаты дохода\"")

        // Последний календарный день месяца
        dateConditionDataList << new DateConditionData(["2610"], ["00"], new LastMonthWorkDayIncomeAccruedDate(), "Значение гр. \"%s\" (\"%s\") должно быть равно последнему календарному дню каждого месяца, в течение срока, на который были предоставлены кредитные (заёмные) средства")

        // "Графа 6" = "Графе 7"
        dateConditionDataList << new DateConditionData(["2640", "2641"], ["00"], new Column6EqualsColumn7(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"Дата выплаты дохода\"")

        // "Графа 6" = "Графе 7"
        dateConditionDataList << new DateConditionData(["2800"], ["00"], new Column6EqualsColumn7(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"Дата выплаты дохода\"")

        // 1,2 "Графа 21" = "Графа 7" + "1 рабочий день"
        dateConditionDataListForBudget << new DateConditionData(["1010", "1011", "3020", "1110", "1400", "2001", "2010",
                                                                 "2710", "2760", "2762", "2770", "2900", "4800"], ["00"],
                new Column21EqualsColumn7Plus1WorkingDay(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"%s\" (\"%s\") + 1 рабочий день")

        // 3,4 "Графа 21" <= "Графа 7" + "30 календарных дней", если "Графа 7" + "30 календарных дней" - выходной день, то "Графа 21" <= "Следующий рабочий день" после "Графа 7" + "30 календарных дней"
        dateConditionDataListForBudget << new DateConditionData(["1530", "1531", "1533", "1535", "1536", "1537", "1539",
                                                                 "1541", "1542", "1543", "1544", "1545", "1546", "1547",
                                                                 "1548", "1549", "1551", "1552", "1553", "1554"], ["01", "02", "03", "04"],
                new Column21EqualsColumn7Plus30WorkingDays(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"%s\" (\"%s\") + 30 календарных дней")

        // 6 "Графа 21" = "Графа 7" + "1 рабочий день"
        dateConditionDataListForBudget << new DateConditionData(["2000"], ["05", "06", "07", "08", "09", "10", "11", "12"],
                new Column21EqualsColumn7Plus1WorkingDay(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"%s\" (\"%s\") + 1 рабочий день")

        // 7 "Графа 21" = "Графа 7" + "1 рабочий день"
        dateConditionDataListForBudget << new DateConditionData(["2002"], ["07", "08", "09", "10"],
                new Column21EqualsColumn7Plus1WorkingDay(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"%s\" (\"%s\") + 1 рабочий день")

        // 8 "Графа 21" = "Графа 7" + "1 рабочий день"
        dateConditionDataListForBudget << new DateConditionData(["2003"], ["13"],
                new Column21EqualsColumn7Plus1WorkingDay(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"%s\" (\"%s\") + 1 рабочий день")

        // 9 "Графа 21" = Последний календарный день месяца для месяца "Графы 7", если Последний календарный день месяца - выходной день, то "Графа 21" = следующий рабочий день
        dateConditionDataListForBudget << new DateConditionData(["2012", "2300"], ["00"],
                new Column21EqualsColumn7LastDayOfMonth(), "Значение гр. \"%s\" (\"%s\") должно быть равно последнему календарному дню месяца выплаты дохода")

        // 10 "Графа 21" = "Графа 7" + "1 рабочий день"
        dateConditionDataListForBudget << new DateConditionData(["2740", "2750", "2790", "4800"], ["13"],
                new Column21EqualsColumn7Plus1WorkingDay(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"%s\" (\"%s\") + 1 рабочий день")

        // 12,13,14 "Графа 21" = "Графа 7" + "1 рабочий день"
        dateConditionDataListForBudget << new DateConditionData(["2610", "2640", "2641", "2800"], ["00"],
                new Column21EqualsColumn7Plus1WorkingDay(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"%s\" (\"%s\") + 1 рабочий день")

        // Сгруппируем Сведения о доходах на основании принадлежности к плательщику
        Map<Long, List<NdflPersonIncome>> ndflPersonIncomeCache = [:]
        ndflPersonIncomeList.each { NdflPersonIncome ndflPersonIncome ->
            List<NdflPersonIncome> ndflPersonIncomeByNdflPersonIdList = ndflPersonIncomeCache.get(ndflPersonIncome.ndflPersonId) ?: new ArrayList<NdflPersonIncome>()
            ndflPersonIncomeByNdflPersonIdList.add(ndflPersonIncome)
            ndflPersonIncomeCache.put(ndflPersonIncome.ndflPersonId, ndflPersonIncomeByNdflPersonIdList)
        }

        ndflPersonIncomeCache.each { Map.Entry<Long, List<NdflPersonIncome>> item ->

            ScriptUtils.checkInterrupted();

            for (NdflPersonIncome ndflPersonIncome : item.value) {
                NdflPerson ndflPerson = personsCache.get(ndflPersonIncome.ndflPersonId)

                def operationId = ndflPersonIncome.operationId
                NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPersonIncome.ndflPersonId)
                String fioAndInpAndOperId = sprintf(TEMPLATE_PERSON_FL_OPER, [ndflPersonFL.fio, ndflPersonFL.inp, operationId])

                List<NdflPersonIncome> ndflPersonIncomeCurrentByPersonIdList = ndflPersonIncomeCache.get(ndflPersonIncome.ndflPersonId) ?: new ArrayList<NdflPersonIncome>()
                List<NdflPersonIncome> ndflPersonIncomeCurrentByPersonIdAndOperationIdList = ndflPersonIncomeCurrentByPersonIdList.each { NdflPersonIncome income ->
                    income.operationId == ndflPersonIncome.operationId
                } ?: new ArrayList<NdflPersonIncome>()

                // СведДох1 Доход.Дата.Начисление (Графа 6)
                if (dateConditionDataList != null && !(ndflPersonIncome.incomeAccruedSumm == null || ndflPersonIncome.incomeAccruedSumm == 0)) {
                    dateConditionDataList.each { dateConditionData ->
                        if (dateConditionData.incomeCodes.contains(ndflPersonIncome.incomeCode) && dateConditionData.incomeTypes.contains(ndflPersonIncome.incomeType)) {
                            if (!dateConditionData.checker.check(ndflPersonIncome, dateConditionWorkDay)) {
                                // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                                String errMsg = String.format(dateConditionData.conditionMessage,
                                        C_INCOME_ACCRUED_DATE, ndflPersonIncome.incomeAccruedDate ? ScriptUtils.formatDate(ndflPersonIncome.incomeAccruedDate) : ""
                                )
                                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                                logger.warnExp("%s. %s.", LOG_TYPE_2_6, fioAndInpAndOperId, pathError, errMsg)
                            }
                        }
                    }
                }

                // СведДох2 Сумма вычета (Графа 12)
                if (ndflPersonIncome.totalDeductionsSumm != null && ndflPersonIncome.totalDeductionsSumm != 0
                        && ndflPersonIncome.incomeAccruedSumm != null && ndflPersonIncome.incomeAccruedSumm != 0) {
                    BigDecimal sumNdflDeduction = getDeductionSumForIncome(ndflPersonIncome, ndflPersonDeductionList)
                    if (!comparNumbEquals(ndflPersonIncome.totalDeductionsSumm ?: new BigDecimal(0), sumNdflDeduction)) {
                        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                        String errMsg = String.format("Значение гр. \"%s\" (\"%s\") должно быть равно сумме гр. \"%s\" (\"%s\") раздела 3",
                                C_TOTAL_DEDUCTIONS_SUMM, ndflPersonIncome.totalDeductionsSumm ?: 0,
                                C_PERIOD_CURR_SUMM, sumNdflDeduction ?: 0)
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                        logger.warnExp("%s. %s.", LOG_TYPE_2_12, fioAndInpAndOperId, pathError, errMsg)
                    }
                    if (comparNumbGreater(sumNdflDeduction, ndflPersonIncome.incomeAccruedSumm ?: new BigDecimal(0))) {
                        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                        String errMsg = String.format("Значение гр. \"%s\" (\"%s\") должно быть не меньше значение гр. \"%s\" (\"%s\")",
                                C_INCOME_ACCRUED_SUMM, ndflPersonIncome.incomeAccruedSumm ?: 0,
                                C_PERIOD_CURR_SUMM, sumNdflDeduction
                        )
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                        logger.warnExp("%s. %s.", LOG_TYPE_2_12, fioAndInpAndOperId, pathError, errMsg)
                    }
                }

                // СведДох4 НДФЛ.Процентная ставка (Графа 14)
                if ((ndflPersonIncome.taxRate ?: 0) > 0) {
                    boolean checkNdflPersonIncomingTaxRateTotal = false;

                    boolean presentCitizenship = ndflPerson.citizenship != null && ndflPerson.citizenship != "0"
                    boolean presentIncomeCode = ndflPersonIncome.incomeCode != null && ndflPersonIncome.incomeCode != "0"
                    boolean presentStatus = ndflPerson.status != null && ndflPerson.status != "0"
                    boolean presentTaxRate = ndflPersonIncome.taxRate != null && ndflPersonIncome.taxRate != 0
                    def ndflPersonIncomingTaxRates = []
                    CHECK_NDFL_PERSON_INCOMING_TAX_RATE_13:
                    {
                        if (presentCitizenship && presentIncomeCode && presentStatus && presentTaxRate) {
                            Boolean conditionA = ndflPerson.citizenship == "643" && ndflPersonIncome.incomeCode != "1010" && ndflPerson.status != "2"
                            Boolean conditionB = ndflPerson.citizenship == "643" && ["1010", "1011"].contains(ndflPersonIncome.incomeCode) && ndflPerson.status == "1"
                            Boolean conditionC = ndflPerson.citizenship != "643" && ["2000", "2001", "2010", "2002", "2003"].contains(ndflPersonIncome.incomeCode) && ndflPerson.status ? Integer.parseInt(ndflPerson.status) : 0 >= 3
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
                        if ((presentIncomeCode && presentStatus && presentTaxRate) && (["2740", "3020", "2610"].contains(ndflPersonIncome.incomeCode) && ndflPerson.status != "2")) {
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
                            def conditionA = ndflPerson.status ? Integer.parseInt(ndflPerson.status.toString()) : 0 >= 2 && ndflPersonIncome.incomeCode != "1010"
                            def conditionB = ndflPerson.status ? Integer.parseInt(ndflPerson.status.toString()) : 0 > 2 && !["2000", "2001", "2010"].contains(ndflPersonIncome.incomeCode)
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
                        String errMsg = String.format(LOG_TYPE_2_14_MSG, "Процентная ставка (%)", ndflPersonIncome.taxRate ?: "",
                                ndflPersonIncome.incomeCode, ndflPerson.status,
                                ndflPersonIncomingTaxRates.join(", ")
                        )
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                        logger.warnExp("%s. %s.", LOG_TYPE_2_14, fioAndInpAndOperId, pathError, errMsg)
                    }
                }

                // СведДох5 НДФЛ.Расчет.Дата (Графа 15)
                if (ndflPersonIncome.taxDate != null) {

                    boolean checkTaxDate = true
                    List<CheckData> logTypeMessagePairList = []
                    boolean calculatedTaxPresented = isPresentedByTempSolution(ndflPersonIncome.calculatedTax, ndflPersonIncome.incomeAccruedSumm, ndflPersonIncome.totalDeductionsSumm)
                    boolean withholdingTaxPresented = isPresentedByTempSolution(ndflPersonIncome.withholdingTax, ndflPersonIncome.incomeAccruedSumm, ndflPersonIncome.totalDeductionsSumm)
                    boolean section_2_15_fatal = declarationService.isCheckFatal(DeclarationCheckCode.RNU_SECTION_2_15, declarationData.declarationTemplateId)
                    // СведДох5.1
                    if (calculatedTaxPresented && (ndflPersonIncome.calculatedTax ?: 0 > 0) && ndflPersonIncome.incomeCode != "0" && ndflPersonIncome.incomeCode != null) {
                        // «Графа 15 Раздел 2» = «Графа 6 Раздел 2»
                        if (ndflPersonIncome.taxDate != null && ndflPersonIncome.incomeAccruedDate != null && ndflPersonIncome.taxDate != ndflPersonIncome.incomeAccruedDate) {
                            checkTaxDate = false
                            logTypeMessagePairList.add(new CheckData(("\"${"Дата исчисленного налога"}\" рассчитана некорректно").toString(), ("Значение гр. \"${C_TAX_DATE}\" (\"${ndflPersonIncome.taxDate ? ScriptUtils.formatDate(ndflPersonIncome.taxDate) : ""}\") должно быть равно значению гр. \"${C_INCOME_ACCRUED_DATE}\" (\"${ndflPersonIncome.incomeAccruedDate ? ScriptUtils.formatDate(ndflPersonIncome.incomeAccruedDate) : ""}\")").toString(), section_2_15_fatal))
                        }
                    }
                    // СведДох5.2
                    if (withholdingTaxPresented && (ndflPersonIncome.withholdingTax ?: 0 > 0) && ndflPersonIncome.incomeCode != "0" && ndflPersonIncome.incomeCode != null) {
                        // «Графа 15 Раздел 2» = «Графа 7 Раздел 2»
                        if (ndflPersonIncome.taxDate != null && ndflPersonIncome.incomePayoutDate != null && ndflPersonIncome.taxDate != ndflPersonIncome.incomePayoutDate) {
                            checkTaxDate = false
                            logTypeMessagePairList.add(new CheckData(("\"${"Дата удержанного налога"}\" рассчитана некорректно").toString(), ("Значение гр. \"${C_TAX_DATE}\" (\"${ndflPersonIncome.taxDate ? ScriptUtils.formatDate(ndflPersonIncome.taxDate) : ""}\") должно быть равно значению гр. \"${C_INCOME_PAYOUT_DATE}\" (\"${ndflPersonIncome.incomePayoutDate ? ScriptUtils.formatDate(ndflPersonIncome.incomePayoutDate) : ""}\")").toString(), section_2_15_fatal))
                        }
                    }
                    // СведДох5.3
                    if (ndflPersonIncome.notHoldingTax != null && withholdingTaxPresented && (ndflPersonIncome.notHoldingTax ?: 0 > 0) &&
                            (ndflPersonIncome.withholdingTax ?: 0) < (ndflPersonIncome.calculatedTax ?: 0) &&
                            ndflPersonIncome.incomeCode != "0" && ndflPersonIncome.incomeCode != null &&
                            !["1530", "1531", "1533", "1535", "1536", "1537", "1539", "1541", "1542", "1543"].contains(ndflPersonIncome.incomeCode)) {
                        // «Графа 15 Раздел 2» = «Графа 7 Раздел 2»
                        if (ndflPersonIncome.taxDate != null && ndflPersonIncome.incomePayoutDate != null && ndflPersonIncome.taxDate != ndflPersonIncome.incomePayoutDate) {
                            checkTaxDate = false
                            logTypeMessagePairList.add(new CheckData("\"Дата не удержаннного налога\" рассчитана некорректно", ("Значение гр. \"${C_TAX_DATE}\" (\"${ndflPersonIncome.taxDate ? ScriptUtils.formatDate(ndflPersonIncome.taxDate) : ""}\") должно быть равно значению гр. \"${C_INCOME_PAYOUT_DATE}\" (\"${ndflPersonIncome.incomePayoutDate ? ScriptUtils.formatDate(ndflPersonIncome.incomePayoutDate) : ""}\")").toString()))
                        }
                    }
                    // СведДох5.4
                    if (ndflPersonIncome.notHoldingTax != null && withholdingTaxPresented && calculatedTaxPresented && (ndflPersonIncome.notHoldingTax ?: 0 > 0) &&
                            (ndflPersonIncome.withholdingTax ?: 0) < (ndflPersonIncome.calculatedTax ?: 0) &&
                            ["1530", "1531", "1533", "1535", "1536", "1537", "1539", "1541", "1542", "1543"].contains(ndflPersonIncome.incomeCode) &&
                            ndflPersonIncome.incomePayoutDate >= new LocalDateTime(getReportPeriodStartDate()) && ndflPersonIncome.incomePayoutDate <= new LocalDateTime(getReportPeriodEndDate())) {
                        // «Графа 15 Раздел 2» = «Графа 6 Раздел 2»
                        if (ndflPersonIncome.taxDate != null && ndflPersonIncome.incomeAccruedDate != null && ndflPersonIncome.taxDate != ndflPersonIncome.incomeAccruedDate) {
                            checkTaxDate = false
                            logTypeMessagePairList.add(new CheckData("\"Дата не удержаннного налога\" рассчитана некорректно", ("Значение гр. \"${C_TAX_DATE}\" (\"${ndflPersonIncome.taxDate ? ScriptUtils.formatDate(ndflPersonIncome.taxDate) : ""}\") должно быть равно значению гр. \"${C_INCOME_ACCRUED_DATE}\" (\"${ndflPersonIncome.incomeAccruedDate ? ScriptUtils.formatDate(ndflPersonIncome.incomeAccruedDate) : ""}\")").toString()))
                        }
                    }
                    // СведДох5.5
                    if (ndflPersonIncome.notHoldingTax != null && withholdingTaxPresented && calculatedTaxPresented && (ndflPersonIncome.notHoldingTax ?: 0 > 0) &&
                            (ndflPersonIncome.withholdingTax ?: 0) < (ndflPersonIncome.calculatedTax ?: 0) &&
                            ["1530", "1531", "1533", "1535", "1536", "1537", "1539", "1541", "1542"].contains(ndflPersonIncome.incomeCode) &&
                            (ndflPersonIncome.incomeAccruedDate < new LocalDateTime(getReportPeriodStartDate()) || ndflPersonIncome.incomeAccruedDate > new LocalDateTime(getReportPeriodEndDate()))) {
                        // «Графа 15 Раздел 2"» = "31.12.20**"
                        if (ndflPersonIncome.taxDate != null) {
                            int dayOfMonth = ndflPersonIncome.taxDate.dayOfMonth().get()
                            int month = ndflPersonIncome.taxDate.monthOfYear().get()
                            if (!(dayOfMonth == 31 && month == 12)) {
                                checkTaxDate = false
                                logTypeMessagePairList.add(new CheckData("\"Дата не удержаннного налога\" рассчитана некорректно", ("Значение гр. \"${C_TAX_DATE}\" (\"${ndflPersonIncome.taxDate ? ScriptUtils.formatDate(ndflPersonIncome.taxDate) : ""}\") должно быть равно \"31.12.20**\"").toString()))
                            }
                        }
                    }
                    // СведДох5.6
                    if (ndflPersonIncome.overholdingTax != null && withholdingTaxPresented && calculatedTaxPresented && (ndflPersonIncome.overholdingTax ?: 0 > 0) &&
                            (ndflPersonIncome.withholdingTax ?: 0) > (ndflPersonIncome.calculatedTax ?: 0) &&
                            ndflPersonIncome.incomeCode != "0" && ndflPersonIncome.incomeCode != null) {
                        // «Графа 15 Раздел 2» = «Графа 7 Раздел 2»
                        if (ndflPersonIncome.taxDate != null && ndflPersonIncome.incomePayoutDate != null && ndflPersonIncome.taxDate != ndflPersonIncome.incomePayoutDate) {
                            checkTaxDate = false
                            logTypeMessagePairList.add(new CheckData("\"Дата излишне удержанного налога\" рассчитана некорректно", ("Значение гр. \"${C_TAX_DATE}\" (\"${ndflPersonIncome.taxDate ? ScriptUtils.formatDate(ndflPersonIncome.taxDate) : ""}\") должно быть равно значению гр. \"${C_INCOME_PAYOUT_DATE}\" (\"${ndflPersonIncome.incomePayoutDate ? ScriptUtils.formatDate(ndflPersonIncome.incomePayoutDate) : ""}\")").toString(), section_2_15_fatal))
                        }
                    }
                    // СведДох5.7
                    if (ndflPersonIncome.refoundTax != null && ndflPersonIncome.overholdingTax != null && withholdingTaxPresented && calculatedTaxPresented && (ndflPersonIncome.refoundTax ?: 0 > 0) &&
                            (ndflPersonIncome.withholdingTax ?: 0) > (ndflPersonIncome.calculatedTax ?: 0) &&
                            (ndflPersonIncome.overholdingTax ?: 0) &&
                            ndflPersonIncome.incomeCode != "0" && ndflPersonIncome.incomeCode != null) {
                        // «Графа 15 Раздел 2» = «Графа 7 Раздел 2»
                        if (ndflPersonIncome.taxDate != null && ndflPersonIncome.incomePayoutDate != null && ndflPersonIncome.taxDate != ndflPersonIncome.incomePayoutDate) {
                            checkTaxDate = false
                            logTypeMessagePairList.add(new CheckData("\"Дата расчета возвращенного налогоплательщику налога\" рассчитана некорректно", ("Значение гр. \"${C_TAX_DATE}\" (\"${ndflPersonIncome.taxDate ? ScriptUtils.formatDate(ndflPersonIncome.taxDate) : ""}\") должно быть равно значению гр. \"${C_INCOME_PAYOUT_DATE}\" (\"${ndflPersonIncome.incomePayoutDate ? ScriptUtils.formatDate(ndflPersonIncome.incomePayoutDate) : ""}\")").toString(), section_2_15_fatal))
                        }
                    }
                    if (!checkTaxDate) {
                        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                        for (CheckData checkData : logTypeMessagePairList) {
                            logger.logCheck("%s. %s.", checkData.fatal, checkData.msgFirst, fioAndInpAndOperId, pathError, checkData.msgLast)
                        }
                    }
                }

                // СведДох6 НДФЛ.Расчет.Сумма.Исчисленный (Графа 16)
                if (ndflPersonIncome.calculatedTax != null) {
                    // СведДох6.1
                    if (ndflPersonIncome.taxRate != 13) {
                        if ((ndflPersonIncome.calculatedTax ?: 0) != ScriptUtils.round(((ndflPersonIncome.taxBase ?: 0) * (ndflPersonIncome.taxRate ?: 0)) / 100, 0)) {
                            // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                            String errMsg = String.format("Значение гр. \"%s\" (\"%s\") должно быть равно произведению значений гр. \"%s\" (\"%s\") и гр. \"%s\" (\"%s\") с округлением до целого числа",
                                    C_CALCULATED_TAX, ndflPersonIncome.calculatedTax ?: 0,
                                    C_TAX_BASE, ndflPersonIncome.taxBase ?: 0,
                                    "Процентная ставка", (ndflPersonIncome.taxRate ?: 0)
                            )
                            String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                            logger.logCheck("%s. %s.",
                                    declarationService.isCheckFatal(DeclarationCheckCode.RNU_SECTION_2_16, declarationData.declarationTemplateId),
                                    LOG_TYPE_2_16, fioAndInpAndOperId, pathError, errMsg)
                        }
                    }
                    // СведДох6.2
                    if (ndflPersonIncome.taxRate == 13 && ndflPersonIncome.incomeCode != "1010" && ndflPerson.status != "6") {
                        /*
                            S1 - сумма значений по "Графе 13" (taxBase)
                            Для суммирования строк по "Графе 13" (taxBase) должны быть соблюдены ВСЕ следующие условия:
                            1. Суммирование значений должно осуществляться для каждого ФЛ по отдельности
                            2. Для суммирования значений должны учитывать только те строки, в которых "Графа 6" (incomeAccruedDate) <= "Графы 6" для текущей строки (МЕНЬШЕ ИЛИ РАВНО)
                            3. Значение "Графы 10" (incomeAccruedSumm) != 0
                            4. Значение "Графы 6" должно >= даты начала отчетного периода и <= даты окончания отчетного периода
                            5. Значение "Графы 14" (taxRate) = 13
                            6. Значение "Графы 4" (incomeCode) != "1010"
                             */
                        List<NdflPersonIncome> ndflPersonIncomeCurrentList = ndflPersonIncomeCache.get(ndflPersonIncome.ndflPersonId) ?: new ArrayList<NdflPersonIncome>()
                        List<NdflPersonIncome> S1List = new ArrayList<NdflPersonIncome>()
                        ndflPersonIncomeCurrentList.each { NdflPersonIncome ndflPersonIncomeCurrent ->
                            if (ndflPersonIncomeCurrent.incomeAccruedDate <= ndflPersonIncome.incomeAccruedDate &&
                                    ndflPersonIncomeCurrent.incomeAccruedSumm != null && ndflPersonIncomeCurrent.incomeAccruedSumm != 0 &&
                                    ndflPersonIncome.incomeAccruedDate >= new LocalDateTime(getReportPeriodStartDate()) && ndflPersonIncome.incomeAccruedDate <= new LocalDateTime(getReportPeriodEndDate()) &&
                                    ndflPersonIncomeCurrent.taxRate == 13 && ndflPersonIncomeCurrent.incomeCode != "1010") {
                                S1List.add(ndflPersonIncomeCurrent)
                            }
                        }
                        BigDecimal S1 = new BigDecimal(0)
                        S1List.each { NdflPersonIncome npiItem ->
                            if (npiItem.taxBase != null) {
                                S1 = S1.add(npiItem.taxBase)
                            }
                        }
                        /*
                            S2 - сумма значений по "Графе 16" (calculatedTax)
                            Для суммирования строк по "Графе 16" (calculatedTax) должны быть соблюдены ВСЕ следующие условия:
                            1. Суммирование значений должно осуществляться для каждого ФЛ по отдельности
                            2. Для суммирования значений должны учитывать только те строки, в которых "Графа 6" (incomeAccruedDate) < "Графы 6" для текущей строки (МЕНЬШЕ)
                            2. Значение "Графы 6" должно >= даты начала отчетного периода и <= даты окончания отчетного периода
                            3. Значение "Графы 14" (taxRate) = 13
                            4. Значение "Графы 4" (incomeCode) != "1010"
                             */
                        List<NdflPersonIncome> S2List = new ArrayList<NdflPersonIncome>()
                        ndflPersonIncomeCurrentList.each { NdflPersonIncome ndflPersonIncomeCurrent ->
                            if (ndflPersonIncomeCurrent.incomeAccruedDate < ndflPersonIncome.incomeAccruedDate &&
                                    ndflPersonIncome.incomeAccruedDate >= new LocalDateTime(getReportPeriodStartDate()) && ndflPersonIncome.incomeAccruedDate <= new LocalDateTime(getReportPeriodEndDate()) &&
                                    ndflPersonIncomeCurrent.taxRate == 13 && ndflPersonIncomeCurrent.incomeCode != "1010") {
                                S2List.add(ndflPersonIncomeCurrent)
                            }
                        }
                        BigDecimal S2 = new BigDecimal(0)
                        S2List.each { NdflPersonIncome npiItem ->
                            if (npiItem.calculatedTax != null) {
                                S2 = S2.add(npiItem.calculatedTax)
                            }
                        }
                        // Сумма по «Графа 16» текущей операции = S1 x 13% - S2
                        if (ndflPersonIncome.calculatedTax != ScriptUtils.round((S1 * 0.13 - S2), 0)) {
                            // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                            String errMsg = String.format("Значение гр. \"%s\" (\"%s\") должно быть равно выражению: Сумма значений гр. \"%s\" с начала периода на отчетную дату х 13%% - сумма значений гр. \"%s\" за предыдущие отчетные периоды",
                                    C_CALCULATED_TAX, ndflPersonIncome.calculatedTax ?: 0,
                                    C_TAX_BASE,
                                    C_CALCULATED_TAX
                            )
                            String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                            logger.logCheck("%s. %s.",
                                    declarationService.isCheckFatal(DeclarationCheckCode.RNU_SECTION_2_16, declarationData.declarationTemplateId),
                                    LOG_TYPE_2_16, fioAndInpAndOperId, pathError, errMsg)
                        }
                    }
                    // СведДох6.3
                    if (ndflPersonIncome.taxRate == 13 && ndflPerson.status == "6") {
                        List<NdflPersonPrepayment> ndflPersonPrepaymentListByBersonIdList = ndflPersonPrepaymentCache.get(ndflPersonIncome.ndflPersonId) ?: new ArrayList<NdflPersonPrepayment>()
                        if (!ndflPersonPrepaymentListByBersonIdList.isEmpty()) {
                            List<NdflPersonPrepayment> ndflPersonPrepaymentCurrentList = new ArrayList<>()
                            ndflPersonPrepaymentListByBersonIdList.each { NdflPersonPrepayment ndflPersonPrepaymentCurrent ->
                                if (ndflPersonPrepaymentCurrent.operationId == ndflPersonIncome.operationId) {
                                    ndflPersonPrepaymentCurrentList.add(ndflPersonPrepaymentCurrent)
                                }
                            }
                            BigDecimal ndflPersonPrepaymentSum = new BigDecimal(0)
                            ndflPersonPrepaymentCurrentList.each { NdflPersonPrepayment ndflPersonPrepaymentCurrent ->
                                if (ndflPersonPrepaymentCurrent.summ != null) {
                                    ndflPersonPrepaymentSum = ndflPersonPrepaymentSum.add(ndflPersonPrepaymentCurrent.summ)
                                }
                            }
                            if (!(ndflPersonIncome.calculatedTax ==
                                    ScriptUtils.round(((ndflPersonIncome.taxBase ?: 0) * 0.13 - ndflPersonPrepaymentSum ?: 0), 0))
                            ) {
                                // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                                String errMsg = String.format("Значение гр. \"%s\" (\"%s\") должно быть равно выражению: гр. \"%s\" (\"%s\") х 13%% - \"%s\" (\"%s\")",
                                        C_CALCULATED_TAX, ndflPersonIncome.calculatedTax ?: 0,
                                        C_TAX_BASE, ndflPersonIncome.taxBase ?: 0,
                                        "Сумма фиксированного авансового платежа", ndflPersonPrepaymentSum
                                )
                                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                                logger.logCheck("%s. %s.",
                                        declarationService.isCheckFatal(DeclarationCheckCode.RNU_SECTION_2_16, declarationData.declarationTemplateId),
                                        LOG_TYPE_2_16, fioAndInpAndOperId, pathError, errMsg)
                            }
                        }
                    }
                }

                // СведДох7 НДФЛ.Расчет.Сумма.Удержанный (Графа 17)
                if (ndflPersonIncome.withholdingTax != null && ndflPersonIncome.withholdingTax != 0) {
                    // СведДох7.1
                    if ((["2520", "2720", "2740", "2750", "2790", "4800"].contains(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeType == "13")
                            || (["1530", "1531", "1532", "1533", "1535", "1536", "1537", "1539", "1541", "1542", "1543", "1544",
                                 "1545", "1546", "1547", "1548", "1549", "1551", "1552", "1554"].contains(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeType == "02")
                            && (ndflPersonIncome.overholdingTax == null || ndflPersonIncome.overholdingTax == 0)
                    ) {
                        // «Графа 17 Раздел 2» = «Графа 16 Раздел 2» = «Графа 24 Раздел 2»
                        if (!(ndflPersonIncome.withholdingTax == ndflPersonIncome.calculatedTax
                                && ndflPersonIncome.withholdingTax == ndflPersonIncome.taxSumm ?: 0)) {
                            // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
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
                            // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
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
                            // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
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
                            // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
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
                            // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
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
                            // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
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
                }

                // "Сумма Граф 16"
                BigDecimal calculatedTaxSum = new BigDecimal(0)
                ndflPersonIncomeCurrentByPersonIdAndOperationIdList.each { NdflPersonIncome ndflPersonIncomeCurrent ->
                    if (ndflPersonIncomeCurrent.calculatedTax != null) {
                        calculatedTaxSum = calculatedTaxSum.add(ndflPersonIncomeCurrent.calculatedTax)
                    }
                }
                // "Сумма Граф 17"
                BigDecimal withholdingTaxSum = new BigDecimal(0)
                ndflPersonIncomeCurrentByPersonIdAndOperationIdList.each { NdflPersonIncome ndflPersonIncomeCurrent ->
                    if (ndflPersonIncomeCurrent.withholdingTax != null)
                        withholdingTaxSum = withholdingTaxSum.add(ndflPersonIncomeCurrent.withholdingTax)
                }
                // "Сумма Граф 18"
                BigDecimal notHoldingTaxSum = new BigDecimal(0)
                ndflPersonIncomeCurrentByPersonIdAndOperationIdList.each { NdflPersonIncome ndflPersonIncomeCurrent ->
                    if (ndflPersonIncomeCurrent.notHoldingTax != null) {
                        notHoldingTaxSum = notHoldingTaxSum.add(ndflPersonIncomeCurrent.notHoldingTax)
                    }
                }
                // "Сумма Граф 19"
                BigDecimal overholdingTaxSum = new BigDecimal(0)
                ndflPersonIncomeCurrentByPersonIdAndOperationIdList.each { NdflPersonIncome ndflPersonIncomeCurrent ->
                    if (ndflPersonIncomeCurrent.overholdingTax != null) {
                        overholdingTaxSum = overholdingTaxSum.add(ndflPersonIncomeCurrent.overholdingTax)
                    }
                }
                // "Сумма Граф 20"
                Long refoundTaxSum = 0L
                ndflPersonIncomeCurrentByPersonIdAndOperationIdList.each { NdflPersonIncome ndflPersonIncomeCurrent ->
                    if (ndflPersonIncomeCurrent.refoundTax != null) {
                        refoundTaxSum += ndflPersonIncomeCurrent.refoundTax
                    }
                }
                // "Сумма Граф 24"
                // Отменил изменения https://jira.aplana.com/browse/SBRFNDFL-1307, поскольку они привели к https://jira.aplana.com/browse/SBRFNDFL-1483
                //Long taxSumm = ndflPersonIncomeCurrentByPersonIdAndOperationIdList.sum {it.taxSumm?: 0} ?: 0

                // СведДох8 НДФЛ.Расчет.Сумма.Не удержанный (Графа 18)
                if (ndflPersonIncome.notHoldingTax != null && calculatedTaxSum > withholdingTaxSum) {
                    if (!(notHoldingTaxSum == calculatedTaxSum - withholdingTaxSum)) {
                        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                        String errMsg = String.format("Сумма значений гр. \"%s\" (\"%s\") должна быть равна разнице сумм значений гр.\"%s\" (\"%s\") и гр.\"%s\" (\"%s\") для всех строк одной операции",
                                C_NOT_HOLDING_TAX, notHoldingTaxSum ?: "0",
                                C_CALCULATED_TAX, calculatedTaxSum ?: "0",
                                C_WITHHOLDING_TAX, withholdingTaxSum ?: "0"
                        )
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                        logger.warnExp("%s. %s.", LOG_TYPE_2_18, fioAndInpAndOperId, pathError, errMsg)
                    }
                }

                // СведДох9 НДФЛ.Расчет.Сумма.Излишне удержанный (Графа 19)
                if (ndflPersonIncome.overholdingTax != null && calculatedTaxSum < withholdingTaxSum) {
                    if (!(overholdingTaxSum == withholdingTaxSum - calculatedTaxSum)) {
                        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                        String errMsg = String.format("Сумма значений гр. \"%s\" (\"%s\") должна быть равна разнице сумм значений гр.\"%s\" (\"%s\") и гр.\"%s\" (\"%s\") для всех строк одной операции",
                                C_OVERHOLDING_TAX, overholdingTaxSum ?: "0",
                                C_WITHHOLDING_TAX, withholdingTaxSum ?: "0",
                                C_CALCULATED_TAX, calculatedTaxSum ?: "0"
                        )
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                        logger.warnExp("%s. %s.", LOG_TYPE_2_19, fioAndInpAndOperId, pathError, errMsg)
                    }
                }

                // СведДох10 НДФЛ.Расчет.Сумма.Возвращенный налогоплательщику (Графа 20)
                if (ndflPersonIncome.refoundTax != null && ndflPersonIncome.refoundTax > 0) {
                    if (!(refoundTaxSum <= overholdingTaxSum)) {
                        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                        String errMsg = String.format("Сумма значений гр. \"%s\" (\"%s\") не должна превышать сумму значений гр.\"%s\" (\"%s\") для всех строк одной операции",
                                C_REFOUND_TAX, refoundTaxSum ?: "0",
                                C_OVERHOLDING_TAX, overholdingTaxSum ?: "0"
                        )
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                        logger.warnExp("%s. %s.", LOG_TYPE_2_20, fioAndInpAndOperId, pathError, errMsg)
                    }
                }

                // СведДох11 НДФЛ.Перечисление в бюджет.Платежное поручение.Сумма (Графа 24)
                // Заменил проверку заполненности 2.24, на проверку заполненности 2.21
                if (ndflPersonIncome.taxTransferDate != null) {
                    dateConditionDataListForBudget.each { dateConditionData ->
                        if (dateConditionData.incomeCodes.contains(ndflPersonIncome.incomeCode) && dateConditionData.incomeTypes.contains(ndflPersonIncome.incomeType)) {
                            // Все подпункты, кроме 11-го
                            if (!dateConditionData.checker.check(ndflPersonIncome, dateConditionWorkDay)) {
                                // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                                String errMsg = String.format(dateConditionData.conditionMessage,
                                        C_TAX_TRANSFER_DATE, ndflPersonIncome.taxTransferDate ? ScriptUtils.formatDate(ndflPersonIncome.taxTransferDate) : "",
                                        C_INCOME_PAYOUT_DATE, ndflPersonIncome.incomePayoutDate ? ScriptUtils.formatDate(ndflPersonIncome.incomePayoutDate) : ""
                                )
                                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                                logger.logCheck("%s. %s.",
                                        declarationService.isCheckFatal(DeclarationCheckCode.RNU_SECTION_2_21, declarationData.declarationTemplateId),
                                        LOG_TYPE_2_21, fioAndInpAndOperId, pathError, errMsg)
                            }
                        }
                    }
                    if (["2520", "2720", "2740", "2750", "2790", "4800"].contains(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeType == "14") {
                        // 11 подпункт "Графа 21" = "Графа 7" + "1 рабочий день"
                        /*
                            Найти следующую за текущей строкой, удовлетворяющую условиям:
                            "Графа 10" > "0"
                            "Графа 5" не равно "02"
                            "Графа 5"не равно "14"
                            "Графа 7" является минимальной из "Граф 7", удовлетворяющих условию: ("Графа 7" (следующей строки) >= "Графа 7" (текущей строки))
                            "Графа 7" <= "31.12.20**" + "1 календарный день", где 31.12.20** - последний день текущего года
                             */

                        // Получим 1-ый рабочий день следующего года
                        Calendar firstWorkingDay = Calendar.getInstance()
                        firstWorkingDay.setTime(getReportPeriodStartDate())
                        firstWorkingDay.set(Calendar.DAY_OF_YEAR, firstWorkingDay.getActualMaximum(Calendar.DAY_OF_YEAR))
                        firstWorkingDay.add(Calendar.DATE, 1)
                        if (firstWorkingDay.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                            firstWorkingDay.add(Calendar.DATE, 2);
                        }
                        if (firstWorkingDay.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                            firstWorkingDay.add(Calendar.DATE, 1);
                        }

                        // Найдем следующую за текущей строку в РНУ
                        List<NdflPersonIncome> ndflPersonIncomeCurrentList = ndflPersonIncomeCache.get(ndflPersonIncome.ndflPersonId) ?: new ArrayList<NdflPersonIncome>()
                        NdflPersonIncome ndflPersonIncomeFind = null;
                        ndflPersonIncomeCurrentList.each { NdflPersonIncome ndflPersonIncomeCurrent ->
                            if ((ndflPersonIncomeCurrent.incomeAccruedSumm ?: new BigDecimal(0) > new BigDecimal(0)) && !["02", "14"].contains(ndflPersonIncomeCurrent.incomeType)
                                    && (ndflPersonIncomeFind == null || ndflPersonIncomeFind.incomePayoutDate > ndflPersonIncomeCurrent.incomePayoutDate)
                                    && ndflPersonIncome.incomePayoutDate <= ndflPersonIncomeCurrent.incomePayoutDate
                                    && ndflPersonIncome.operationId < ndflPersonIncomeCurrent.operationId) {
                                if (ndflPersonIncomeCurrent.incomePayoutDate.compareTo(new LocalDateTime(firstWorkingDay.getTime())) < 0 || ((LocalDateTime) ndflPersonIncomeCurrent.incomePayoutDate).equals(new LocalDateTime(firstWorkingDay.getTime()))) {
                                    ndflPersonIncomeFind = ndflPersonIncomeCurrent
                                }
                            }
                        }
                        if (ndflPersonIncomeFind != null) {
                            // Графа21(текущей строки) должна быть равна Графа7(следующей строки) + 1 рабочий день
                            // Берём Графу21 из текущей проверяемой строки
                            // Берём Графу7 из следующей найденной строки
                            NdflPersonIncome ndflPersonIncomeCheck = new NdflPersonIncome()
                            ndflPersonIncomeCheck.taxTransferDate = new LocalDateTime(ndflPersonIncome.taxTransferDate.getLocalMillis())
                            ndflPersonIncomeCheck.incomePayoutDate = new LocalDateTime(ndflPersonIncomeFind.incomePayoutDate.getLocalMillis())

                            Column21EqualsColumn7Plus1WorkingDay column7Plus1WorkingDay = new Column21EqualsColumn7Plus1WorkingDay()
                            if (!column7Plus1WorkingDay.check(ndflPersonIncomeCheck, dateConditionWorkDay)) {
                                // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                                String errMsg = String.format("Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"%s\" (\"%s\") + 1 рабочий день",
                                        C_TAX_TRANSFER_DATE, ndflPersonIncome.taxTransferDate ? ScriptUtils.formatDate(ndflPersonIncome.taxTransferDate) : "",
                                        C_INCOME_PAYOUT_DATE, ndflPersonIncomeFind.incomePayoutDate ? ScriptUtils.formatDate(ndflPersonIncomeFind.incomePayoutDate) : ""
                                )
                                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                                logger.logCheck("%s. %s.",
                                        declarationService.isCheckFatal(DeclarationCheckCode.RNU_SECTION_2_21, declarationData.declarationTemplateId),
                                        LOG_TYPE_2_21, fioAndInpAndOperId, pathError, errMsg)
                            }
                        } else {
                            // ToDo https://jira.aplana.com/browse/SBRFNDFL-1448
                            if (false) {
                                String errMsg = String.format("Значение гр. \"%s\" (\"%s\") должно быть равно \"00.00.0000\"",
                                        C_TAX_TRANSFER_DATE, ndflPersonIncome.taxTransferDate ? ScriptUtils.formatDate(ndflPersonIncome.taxTransferDate) : ""
                                )
                                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                                logger.logCheck("%s. %s.",
                                        declarationService.isCheckFatal(DeclarationCheckCode.RNU_SECTION_2_21, declarationData.declarationTemplateId),
                                        LOG_TYPE_2_21, fioAndInpAndOperId, pathError, errMsg)
                            }
                        }
                    }
                }

                //СведДох12	 Отсутствие нулевых значений
                LOG_TYPE_NOT_ZERO_CHECK:
                {
                    if (ndflPersonIncome.incomeAccruedSumm != null && ScriptUtils.isEmpty(ndflPersonIncome.incomeAccruedSumm)) {
                        String errMsg = String.format("Значение гр. \"%s\" (\"%s\") не может быть равно \"0\"",
                                C_INCOME_ACCRUED_SUMM, ndflPersonIncome.incomeAccruedSumm
                        )
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                        logger.warnExp("%s. %s.", LOG_TYPE_NOT_ZERO, fioAndInpAndOperId, pathError, errMsg)
                    }
                    if (ndflPersonIncome.incomePayoutSumm != null && ScriptUtils.isEmpty(ndflPersonIncome.incomePayoutSumm)) {
                        String errMsg = String.format("Значение гр. \"%s\" (\"%s\") не может быть равно \"0\"",
                                C_INCOME_PAYOUT_SUMM, ndflPersonIncome.incomePayoutSumm
                        )
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                        logger.warnExp("%s. %s.", LOG_TYPE_NOT_ZERO, fioAndInpAndOperId, pathError, errMsg)
                    }
                    if (ndflPersonIncome.taxRate != null && ScriptUtils.isEmpty(ndflPersonIncome.taxRate)) {
                        String errMsg = String.format("Значение гр. \"%s\" (\"%s\") не может быть равно \"0\"",
                                C_TAX_RATE, ndflPersonIncome.taxRate
                        )
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                        logger.warnExp("%s. %s.", LOG_TYPE_NOT_ZERO, fioAndInpAndOperId, pathError, errMsg)
                    }
                    if (ndflPersonIncome.taxSumm != null && ScriptUtils.isEmpty(ndflPersonIncome.taxSumm)) {
                        String errMsg = String.format("Значение гр. \"%s\" (\"%s\") не может быть равно \"0\"",
                                C_TAX_SUMM, ndflPersonIncome.taxSumm
                        )
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                        logger.warnExp("%s. %s.", LOG_TYPE_NOT_ZERO, fioAndInpAndOperId, pathError, errMsg)
                    }
                }
            }
        }

        logForDebug("Проверки сведений о доходах (" + (System.currentTimeMillis() - time) + " мс)");
    }

    /**
     * Проверки Сведения о вычетах
     * @param ndflPersonList
     * @param ndflPersonIncomeList
     * @param ndflPersonDeductionList
     * @param personMap
     */
    def checkDataDeduction(List<NdflPerson> ndflPersonList, List<NdflPersonIncome> ndflPersonIncomeList,
                           List<NdflPersonDeduction> ndflPersonDeductionList, Map<Long, Map<String, RefBookValue>> personMap) {

        long time = System.currentTimeMillis()

        for (NdflPerson ndflPerson : ndflPersonList) {
            NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPerson.id)
            if (ndflPersonFL == null) {
                if (FORM_DATA_KIND.equals(FormDataKind.PRIMARY)) {
                    // РНУ-НДФЛ первичная
                    String fio = ndflPerson.lastName + " " + ndflPerson.firstName + " " + (ndflPerson.middleName ?: "")
                    ndflPersonFL = new NdflPersonFL(fio, ndflPerson.inp)
                } else {
                    // РНУ-НДФЛ консолидированная
                    Map<String, RefBookValue> personRecord = personMap.get(ndflPerson.recordId)
                    String fio = personRecord.get(RF_LAST_NAME).value.toString() + " " + personRecord.get(RF_FIRST_NAME).value.toString() + " " + (personRecord.get(RF_MIDDLE_NAME).value.toString() ?: "")
                    ndflPersonFL = new NdflPersonFL(fio, ndflPerson.recordId.toString())
                }
                ndflPersonFLMap.put(ndflPerson.id, ndflPersonFL)
            }
        }

        Map<String, Map<String, NdflPersonIncome>> mapNdflPersonIncome = [:]
        for (NdflPersonIncome ndflPersonIncome : ndflPersonIncomeList) {
            String operationIdNdflPersonId = "${ndflPersonIncome.operationId}_${ndflPersonIncome.ndflPersonId}"
            if (!mapNdflPersonIncome.containsKey(operationIdNdflPersonId)) {
                mapNdflPersonIncome.put(operationIdNdflPersonId, new LinkedHashMap<String, NdflPersonIncome>())
            }
            mapNdflPersonIncome.get(operationIdNdflPersonId).put(ndflPersonIncome.incomeAccruedDate ? ScriptUtils.formatDate(ndflPersonIncome.incomeAccruedDate) : "", ndflPersonIncome)
        }

        for (NdflPersonDeduction ndflPersonDeduction : ndflPersonDeductionList) {

            ScriptUtils.checkInterrupted();

            def operationId = ndflPersonDeduction.operationId
            NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPersonDeduction.ndflPersonId)
            String fioAndInpAndOperId = sprintf(TEMPLATE_PERSON_FL_OPER, [ndflPersonFL.fio, ndflPersonFL.inp, operationId])

            // Выч14 Документ о праве на налоговый вычет.Код источника (Графа 7)
            if (ndflPersonDeduction.notifType == "1" && ndflPersonDeduction.notifSource != "0000") {
                // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                String errMsg = String.format("Значение гр. \"%s\" (\"%s\") не соответствует значению гр. \"%s\" (\"%s\")",
                        C_NOTIF_SOURCE, ndflPersonDeduction.notifSource ?: "",
                        C_TYPE_CODE, ndflPersonDeduction.typeCode ?: ""
                )
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum ?: "")
                logger.warnExp("%s. %s.", LOG_TYPE_3_7, fioAndInpAndOperId, pathError, errMsg)
            }

            // Выч15 (Графы 9)
            // Выч16 (Графы 10)
            String operationIdNdflPersonIdDate = "${ndflPersonDeduction.operationId}_${ndflPersonDeduction.ndflPersonId}"
            Map<String, NdflPersonIncome> mapNdflPersonIncomeDate = mapNdflPersonIncome.get(operationIdNdflPersonIdDate)
            if (mapNdflPersonIncomeDate == null) {
                // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                String errMsg = String.format("Для гр. \"%s\" (\"%s\") отсутствуют операция или физическое лицо в разделе 2",
                        C_INCOME_ACCRUED, ndflPersonDeduction.incomeAccrued ? ScriptUtils.formatDate(ndflPersonDeduction.incomeAccrued) : ""
                )
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum ?: "")
                logger.logCheck("%s. %s.",
                        declarationService.isCheckFatal(DeclarationCheckCode.RNU_SECTION_3_10, declarationData.declarationTemplateId),
                        LOG_TYPE_3_10, fioAndInpAndOperId, pathError, errMsg)
            } else {
                NdflPersonIncome ndflPersonIncome = mapNdflPersonIncomeDate.get(ndflPersonDeduction.incomeAccrued ? ScriptUtils.formatDate(ndflPersonDeduction.incomeAccrued) : "")
                if (ndflPersonIncome == null) {
                    // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                    ndflPersonIncome = mapNdflPersonIncomeDate.values().find {
                        it.incomeAccruedDate != null
                    }
                    String errMsg
                    if (ndflPersonIncome != null) {
                        errMsg = String.format("Значение гр. \"%s\" (\"%s\") не соответствует значению гр. \"%s\" (\"%s\")",
                                C_INCOME_ACCRUED, ndflPersonDeduction.incomeAccrued ? ScriptUtils.formatDate(ndflPersonDeduction.incomeAccrued) : "",
                                C_INCOME_ACCRUED_DATE, ScriptUtils.formatDate(ndflPersonIncome.incomeAccruedDate)
                        )
                    } else {
                        errMsg = String.format("Для гр. \"%s\" (\"%s\") не найдено заполненных гр. \"%s\" Раздела 2",
                                C_INCOME_ACCRUED, ndflPersonDeduction.incomeAccrued ? ScriptUtils.formatDate(ndflPersonDeduction.incomeAccrued) : "",
                                C_INCOME_ACCRUED_DATE
                        )
                    }
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum ?: "")
                    logger.logCheck("%s. %s.",
                            declarationService.isCheckFatal(DeclarationCheckCode.RNU_SECTION_3_10, declarationData.declarationTemplateId),
                            LOG_TYPE_3_10, fioAndInpAndOperId, pathError, errMsg)
                } else {
                    // Выч17 Начисленный доход.Код дохода (Графы 11)
                    if (ndflPersonDeduction.incomeCode != ndflPersonIncome.incomeCode) {
                        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                        String errMsg = String.format("Обнаружены расхождения между значением гр. \"%s\", к которому был применен вычет (\"%s\"), указанным в Разделе 2, и значением гр. \"%s\" (\"%s\"), указанным в Разделе 3",
                                C_INCOME_CODE, ndflPersonIncome.incomeCode ?: "",
                                C_INCOME_ACCRUED_CODE, ndflPersonDeduction.incomeCode ?: ""
                        )
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum ?: "")
                        logger.warnExp("%s. %s.", LOG_TYPE_3_11, fioAndInpAndOperId, pathError, errMsg)
                    }

                    // Выч18 Начисленный доход.Сумма (Графы 12)
                    if (!comparNumbEquals(ndflPersonDeduction.incomeSumm, ndflPersonIncome.incomeAccruedSumm)) {
                        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                        String errMsg = String.format("Обнаружены расхождения между значением гр. \"%s\", к которому был применен вычет (\"%s\"), указанным в Разделе 2, и значением гр. \"%s\" (\"%s\"), указанным в Разделе 3",
                                C_INCOME_ACCRUED_SUMM, ndflPersonIncome.incomeAccruedSumm ?: "",
                                C_INCOME_ACCRUED_P_SUMM, ndflPersonDeduction.incomeSumm ?: ""
                        )
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum ?: "")
                        logger.warnExp("%s. %s.", LOG_TYPE_3_12, fioAndInpAndOperId, pathError, errMsg)
                    }
                }
            }

            // Выч20 Начисленный доход.Дата (Графы 10)
            if (ndflPersonDeduction.periodCurrDate != ndflPersonDeduction.incomeAccrued) {
                // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                String errMsg = String.format("Значение гр. \"%s\" (\"%s\")\" не соответствует значению гр. \"%s\" (\"%s\")",
                        C_PERIOD_CURR_DATE, ndflPersonDeduction.periodCurrDate ? ScriptUtils.formatDate(ndflPersonDeduction.periodCurrDate) : "",
                        C_INCOME_ACCRUED, ndflPersonDeduction.incomeAccrued ? ScriptUtils.formatDate(ndflPersonDeduction.incomeAccrued) : ""
                )
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum ?: "")
                logger.logCheck("%s. %s.",
                        declarationService.isCheckFatal(DeclarationCheckCode.RNU_SECTION_3_10_2, declarationData.declarationTemplateId),
                        LOG_TYPE_3_10_2, fioAndInpAndOperId, pathError, errMsg)
            }

            // Выч21 Документ о праве на налоговый вычет.Сумма (Графы 16) (Графы 8)
            if (comparNumbGreater(ndflPersonDeduction.periodCurrSumm ?: new BigDecimal(0), ndflPersonDeduction.notifSumm ?: new BigDecimal(0))) {
                // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                String errMsg = String.format("Значение гр. \"%s\" (%s) не должно превышать значение гр. \"%s\" (%s) согласно подтверждающему документу",
                        C_PERIOD_CURR_SUMM, ndflPersonDeduction.periodCurrSumm ?: "",
                        C_NOTIF_SUMM, ndflPersonDeduction.notifSumm ?: ""
                )
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum ?: "")
                logger.logCheck("%s. %s.",
                        declarationService.isCheckFatal(DeclarationCheckCode.RNU_SECTION_3_16, declarationData.declarationTemplateId),
                        LOG_TYPE_3_16, fioAndInpAndOperId, pathError, errMsg)
            }
        }
        logForDebug("Проверки сведений о вычетах (" + (System.currentTimeMillis() - time) + " мс)");
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
        String conditionPath
        String conditionMessage

        ColumnFillConditionData(ColumnFillConditionChecker columnConditionCheckerAsIs, ColumnFillConditionChecker columnConditionCheckerToBe, String conditionPath, String conditionMessage) {
            this.columnConditionCheckerAsIs = columnConditionCheckerAsIs
            this.columnConditionCheckerToBe = columnConditionCheckerToBe
            this.conditionPath = conditionPath
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
            return !ScriptUtils.isEmpty(ndflPersonIncome.incomeCode)
        }
    }
    /**
     * Проверка: "Раздел 2. Графа 4,5 заполнены"
     */
    class Column5Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return !ScriptUtils.isEmpty(ndflPersonIncome.incomeType)
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
            return ndflPersonIncome.oktmo != null
        }
    }
    /**
     * Проверка: "Раздел 2. Графа 9 заполнена"
     */
    class Column9Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.kpp != null
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
     * Проверка: "Раздел 2. Графы 7, 11 заполнены"
     */
    class Column7And11Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.incomePayoutDate != null && ndflPersonIncome.incomePayoutSumm != null
        }
    }
    /**
     * Проверка: "Раздел 2. Графа 12 НЕ заполнена"
     */
    class Column12NotFill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ScriptUtils.isEmpty(ndflPersonIncome.totalDeductionsSumm)
        }
    }
    /**
     * Проверка: "Раздел 2. Графы 13 заполнены"
     */
    class Column13Fill implements ColumnFillConditionChecker {
        boolean temporalySolution

        Column13Fill() {
            temporalySolution = false
        }

        Column13Fill(boolean temporalySolution) {
            this.temporalySolution = temporalySolution
        }

        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            if (temporalySolution) {
                return ndflPersonIncome.taxBase != null
            }
            return !ScriptUtils.isEmpty(ndflPersonIncome.taxBase)
        }
    }
    /**
     * Проверка: "Раздел 2. Графы  14 заполнены"
     */
    class Column14Fill implements ColumnFillConditionChecker {
        boolean temporalySolution

        Column14Fill() {
            temporalySolution = false
        }

        Column14Fill(boolean temporalySolution) {
            this.temporalySolution = temporalySolution
        }

        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            if (temporalySolution) {
                return ndflPersonIncome.taxRate != null
            }
            return ndflPersonIncome.taxRate != null
        }
    }
    /**
     * Проверка: "Раздел 2. Графы 15 заполнены"
     */
    class Column15Fill implements ColumnFillConditionChecker {
        boolean temporalySolution

        Column15Fill() {
            temporalySolution = false
        }

        Column15Fill(boolean temporalySolution) {
            this.temporalySolution = temporalySolution
        }

        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            if (temporalySolution) {
                return ndflPersonIncome.taxDate != null
            }
            return ndflPersonIncome.taxDate != null
        }
    }

    /**
     * Проверка: "Раздел 2. Графы 16 заполнена"
     */
    class Column16Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return !ScriptUtils.isEmpty(ndflPersonIncome.calculatedTax)
        }
    }
    /**
     * Проверка: "Раздел 2. Графы 17 заполнена"
     */
    class Column17Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return !ScriptUtils.isEmpty(ndflPersonIncome.withholdingTax)
        }
    }
    /**
     * Проверка: "Раздел 2. Графа 18 заполнена"
     */
    class Column18Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return !ScriptUtils.isEmpty(ndflPersonIncome.notHoldingTax)
        }
    }
    /**
     * Проверка: "Раздел 2. Графа 19 заполнена"
     */
    class Column19Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return !ScriptUtils.isEmpty(ndflPersonIncome.overholdingTax)
        }
    }
    /**
     * Проверка: "Раздел 2. Графы 20 заполнена"
     */
    class Column20Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return !ScriptUtils.isEmpty(ndflPersonIncome.refoundTax)
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
     * Класс для соотнесения вида проверки в зависимости от значений "Код вида дохода" и "Признак вида дохода"
     */
    class DateConditionData {
        List<String> incomeCodes
        List<String> incomeTypes
        DateConditionChecker checker
        String conditionMessage

        DateConditionData(List<String> incomeCodes, List<String> incomeTypes, DateConditionChecker checker, String conditionMessage) {
            this.incomeCodes = incomeCodes
            this.incomeTypes = incomeTypes
            this.checker = checker
            this.conditionMessage = conditionMessage
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
        CalendarService calendarService

        DateConditionWorkDay(CalendarService calendarService) {
            workDayWithOffset0Cache = [:]
            workDayWithOffset1Cache = [:]
            workDayWithOffset30Cache = [:]
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
    }

    interface DateConditionChecker {
        boolean check(NdflPersonIncome ndflPersonIncome, DateConditionWorkDay dateConditionWorkDay)
    }

    /**
     * Проверка: "Графа 6" = "Графе 7"
     */
    class Column6EqualsColumn7 implements DateConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome, DateConditionWorkDay dateConditionWorkDay) {
            String accrued = ndflPersonIncome.incomeAccruedDate?.toString("dd.MM.yyyy")
            String payout = ndflPersonIncome.incomePayoutDate?.toString("dd.MM.yyyy")
            return accrued == payout
        }
    }

    /**
     * Проверка: Соответствия маске
     */
    class MatchMask implements DateConditionChecker {
        String maskRegex

        MatchMask(String maskRegex) {
            this.maskRegex = maskRegex
        }

        @Override
        boolean check(NdflPersonIncome ndflPersonIncome, DateConditionWorkDay dateConditionWorkDay) {
            if (ndflPersonIncome.incomeAccruedDate == null) {
                return false
            }
            String accrued = ndflPersonIncome.incomeAccruedDate.toString("dd.MM.yyyy")
            Pattern pattern = Pattern.compile(maskRegex)
            Matcher matcher = pattern.matcher(accrued)
            if (matcher.matches()) {
                return true
            }
            return false
        }
    }

    /**
     * Проверка "Последний календарный день месяца"
     */
    class LastMonthCalendarDay implements DateConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome, DateConditionWorkDay dateConditionWorkDay) {
            if (ndflPersonIncome.incomeAccruedDate == null) {
                return true
            }
            Calendar calendar = Calendar.getInstance()
            calendar.setTime(ndflPersonIncome.incomeAccruedDate.toDate())
            int currentMonth = calendar.get(Calendar.MONTH)
            calendar.add(calendar.DATE, 1)
            int comparedMonth = calendar.get(Calendar.MONTH)
            return currentMonth != comparedMonth
        }
    }

    /**
     * Проверка: Если «графа 7» < 31.12.20**, то «графа 6» = «графа 7»
     */
    class Column7LastDayOfYear1 implements DateConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome, DateConditionWorkDay dateConditionWorkDay) {
            if (ndflPersonIncome.incomePayoutDate == null) {
                return false
            }
            Calendar calendarPayout = Calendar.getInstance()
            calendarPayout.setTime(ndflPersonIncome.incomePayoutDate.toDate())
            int dayOfMonth = calendarPayout.get(Calendar.DAY_OF_MONTH)
            int month = calendarPayout.get(Calendar.MONTH)
            if (dayOfMonth != 31 || month != 11) {
                return new Column6EqualsColumn7().check(ndflPersonIncome, dateConditionWorkDay)
            } else {
                return true
            }
        }
    }

    /**
     * Проверка: Если «графа 7» < 31.12.20**, то «графа 6» = 31.12.20**
     */
    class Column7LastDayOfYear2 implements DateConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome, DateConditionWorkDay dateConditionWorkDay) {
            if (ndflPersonIncome.incomePayoutDate == null) {
                return false
            }
            Calendar calendarPayout = Calendar.getInstance()
            calendarPayout.setTime(ndflPersonIncome.incomePayoutDate.toDate())
            int dayOfMonth = calendarPayout.get(Calendar.DAY_OF_MONTH)
            int month = calendarPayout.get(Calendar.MONTH)
            if (dayOfMonth != 31 || month != 11) {
                return true
            } else {
                return new MatchMask("31.12.20\\d{2}").check(ndflPersonIncome, dateConditionWorkDay)
            }
        }
    }

    /**
     * Проверка: Доход.Дата.Начисление (Графа 6) последний календарный день месяца (если последний день месяца приходится на выходной, то следующий первый рабочий день)
     */
    class LastMonthWorkDayIncomeAccruedDate implements DateConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome, DateConditionWorkDay dateConditionWorkDay) {
            if (ndflPersonIncome.incomeAccruedDate == null) {
                return false
            }
            Calendar calendar = Calendar.getInstance()
            calendar.setTime(ndflPersonIncome.incomeAccruedDate.toDate())
            // находим последний день месяца
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            Date workDay = calendar.getTime()
            // если последний день месяца приходится на выходной, то следующий первый рабочий день
            int offset = 0
            workDay = dateConditionWorkDay.getWorkDay(workDay, offset)
            return workDay.getTime() == ndflPersonIncome.incomeAccruedDate.toDate().getTime()
        }
    }

    /**
     * Проверка: "Графа 21" = "Графа 7" + "1 рабочий день"
     */
    class Column21EqualsColumn7Plus1WorkingDay implements DateConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome, DateConditionWorkDay dateConditionWorkDay) {
            if (ndflPersonIncome.taxTransferDate == null || ndflPersonIncome.incomePayoutDate == null) {
                return false
            }
            Calendar calendar21 = Calendar.getInstance();
            calendar21.setTime(ndflPersonIncome.taxTransferDate.toDate());

            // "Графа 7" + "1 рабочий день"
            int offset = 1
            Date workDay = dateConditionWorkDay.getWorkDay(ndflPersonIncome.incomePayoutDate.toDate(), offset)
            Calendar calendar7 = Calendar.getInstance();
            calendar7.setTime(workDay);

            return calendar21.equals(calendar7);
        }
    }

    /**
     * Проверка: "Графа 21" <= "Графа 7" + "30 календарных дней", если "Графа 7" + "30 календарных дней" - выходной день, то "Графа 21" <= "Следующий рабочий день" после "Графа 7" + "30 календарных дней"
     */
    class Column21EqualsColumn7Plus30WorkingDays implements DateConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome, DateConditionWorkDay dateConditionWorkDay) {
            if (ndflPersonIncome.taxTransferDate == null || ndflPersonIncome.incomePayoutDate == null) {
                return false
            }
            Calendar calendar21 = Calendar.getInstance();
            calendar21.setTime(ndflPersonIncome.taxTransferDate.toDate());

            // "Следующий рабочий день" после "Графа 7" + "30 календарных дней"
            int offset = 30
            Date workDay = dateConditionWorkDay.getWorkDay(ndflPersonIncome.incomePayoutDate.toDate(), offset)
            Calendar calendar7 = Calendar.getInstance();
            calendar7.setTime(workDay);

            return calendar21.before(calendar7) || calendar21.equals(calendar7);
        }
    }

    /**
     * "Графа 21" = Последний календарный день месяца для месяца "Графы 7", если Последний календарный день месяца - выходной день, то "Графа 21" = следующий рабочий день
     */
    class Column21EqualsColumn7LastDayOfMonth implements DateConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome, DateConditionWorkDay dateConditionWorkDay) {
            if (ndflPersonIncome.taxTransferDate == null || ndflPersonIncome.incomePayoutDate == null) {
                return false
            }
            Calendar calendar21 = Calendar.getInstance();
            calendar21.setTime(ndflPersonIncome.taxTransferDate.toDate());

            Calendar calendar7 = Calendar.getInstance();
            calendar7.setTime(ndflPersonIncome.incomePayoutDate.toDate());

            // находим последний день месяца
            calendar7.set(Calendar.DAY_OF_MONTH, calendar7.getActualMaximum(Calendar.DAY_OF_MONTH))
            Date workDay = calendar7.getTime()
            // если последний день месяца приходится на выходной, то следующий первый рабочий день
            int offset = 0
            workDay = dateConditionWorkDay.getWorkDay(workDay, offset)
            calendar7.setTime(workDay);

            return calendar21.equals(calendar7);
        }
    }

    /**
     * Получить дату окончания отчетного периода
     * @return
     */
    Date getReportPeriodEndDate() {
        if (periodEndDate == null) {
            periodEndDate = reportPeriodService.getEndDate(declarationData.reportPeriodId)?.time
        }
        return periodEndDate
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
        return countryCodeCache;
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
            return Collections.emptyMap();
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
        return documentTypeCodeCache;
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
        return taxpayerStatusCodeCache;
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
        return mapResult;
    }

    /**
     * Получить "Виды доходов"
     * @return
     */
    Map<String, List<Long>> getRefIncomeType() {
        // Map<REF_BOOK_INCOME_KIND.MARK, List<REF_BOOK_INCOME_KIND.INCOME_TYPE_ID>>
        Map<String, List<Long>> mapResult = [:]
        PagingResult<Map<String, RefBookValue>> refBookList = getRefBook(RefBook.Id.INCOME_KIND.id)
        refBookList.each { Map<String, RefBookValue> refBook ->
            String mark = refBook?.MARK?.stringValue
            List<Long> incomeTypeIdList = mapResult.get(mark)
            if (incomeTypeIdList == null) {
                incomeTypeIdList = []
            }
            incomeTypeIdList.add(refBook?.INCOME_TYPE_ID?.referenceValue)
            mapResult.put(mark, incomeTypeIdList)
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
        return deductionTypeCache;
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
        return taxInspectionCache;
    }

    /**
     * Получить "ИНП"
     */
    Map<Long, List<Map<String, RefBookValue>>> getActualRefInpMapByDeclarationDataId() {
        if (inpActualCache.isEmpty()) {
            String whereClause = "exists (select 1 from ndfl_person np where np.declaration_data_id = ${declarationData.id} AND ref_book_id_tax_payer.person_id = np.person_id)"
            Map<Long, Map<String, RefBookValue>> refBookMap = getRefBookByRecordWhere(RefBook.Id.ID_TAX_PAYER.id, whereClause)

            refBookMap.each { Long id, Map<String, RefBookValue> refBook ->
                List<Map<String, RefBookValue>> inpList = inpActualCache.get(refBook?.PERSON_ID?.referenceValue)
                if (inpList == null) {
                    inpList = []
                }
                inpList.add(refBook)
                inpActualCache.put(refBook?.PERSON_ID?.referenceValue, inpList)
            }
        }
        return inpActualCache
    }

    /**
     * Получить "Документ, удостоверяющий личность (ДУЛ)"
     */
    Map<Long, Map<Long, Map<String, RefBookValue>>> getActualRefDulByDeclarationDataId() {
        if (dulActualCache.isEmpty()) {
            String whereClause = "exists (select 1 from ndfl_person np where np.declaration_data_id = ${declarationData.id} " +
                    "AND ref_book_id_doc.person_id = np.person_id) AND ref_book_id_doc.inc_rep = 1"
            Map<Long, Map<String, RefBookValue>> refBookMap = getRefBookByRecordWhere(RefBook.Id.ID_DOC.id, whereClause)

            refBookMap.each { personId, refBookValues ->
                Long refBookPersonId = refBookValues.get("PERSON_ID").getReferenceValue();
                Map<Long, Map<String, RefBookValue>> dulList = dulActualCache.get(refBookPersonId);
                if (dulList == null) {
                    dulList = [:];
                }
                dulList.put(refBookPersonId, refBookValues);
                dulActualCache.put(refBookPersonId, dulList)
            }
        }
        return dulActualCache
    }

    /**
     * Получить "Адреса налогоплательщика"
     * @return
     */
    Map<Long, Map<String, RefBookValue>> getRefAddress(List<Long> addressIds) {
        if (addressCache.size() == 0) {
            Map<Long, Map<String, RefBookValue>> refBookMap = getRefBookByRecordIds(RefBook.Id.PERSON_ADDRESS.getId(), addressIds)
            refBookMap.each { addressId, address ->
                addressCache.put(addressId, address)
            }
        }
        return addressCache;
    }

    Map<Long, FiasCheckInfo> getFiasAddressIdsMap() {
        if (fiasAddressIdsCache.isEmpty()) {
            fiasAddressIdsCache = fiasRefBookService.checkAddressByFias(declarationData.id, 1);
        }
        return fiasAddressIdsCache;
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
                ScriptUtils.isEmpty(ndflPerson.building) && ScriptUtils.isEmpty(ndflPerson.flat);
        return emptyAddress;
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

    /**
     * Возвращает "Сумму применения вычета в текущем периоде"
     * @param ndflPersonIncome
     * @param ndflPersonDeductionList
     * @return
     */
    BigDecimal getDeductionSumForIncome(NdflPersonIncome ndflPersonIncome, List<NdflPersonDeduction> ndflPersonDeductionList) {
        BigDecimal sumNdflDeduction = new BigDecimal(0)
        for (NdflPersonDeduction ndflPersonDeduction in ndflPersonDeductionList) {
            if (ndflPersonIncome.operationId == ndflPersonDeduction.operationId
                    && ndflPersonIncome.incomeAccruedDate?.toLocalDate().equals(ndflPersonDeduction.incomeAccrued?.toLocalDate())
                    && ndflPersonIncome.ndflPersonId == ndflPersonDeduction.ndflPersonId) {
                sumNdflDeduction += ndflPersonDeduction.periodCurrSumm ?: 0
            }
        }
        return sumNdflDeduction
    }

    /**
     * Сравнение чисел с плавающей точкой через эпсилон-окрестности
     */
    boolean comparNumbEquals(BigDecimal d1, BigDecimal d2) {
        if (d1 == null || d2 == null) return false
        return ((d1 - d2).abs() < 0.001)
    }

    boolean comparNumbGreater(BigDecimal d1, BigDecimal d2) {
        if (d1 == null || d2 == null) return false
        return (d1 - d2 > 0.001)
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

    /**
     * Получить дату начала отчетного периода
     * @return
     */
    Date getReportPeriodStartDate() {
        if (reportPeriodStartDate == null) {
            reportPeriodStartDate = reportPeriodService.getStartDate(declarationData.reportPeriodId)?.time
        }
        return reportPeriodStartDate
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
        return documentTypeCache;
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
            return Collections.emptyMap();
        }
        return refBookMap
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
        boolean fatal;

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
}
