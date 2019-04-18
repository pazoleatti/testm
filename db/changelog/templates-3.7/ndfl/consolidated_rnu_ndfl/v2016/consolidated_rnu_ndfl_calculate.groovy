package form_template.ndfl.consolidated_rnu_ndfl.v2016

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.*
import com.aplana.sbrf.taxaccounting.model.consolidation.ConsolidationIncome
import com.aplana.sbrf.taxaccounting.model.consolidation.ConsolidationSourceDataSearchFilter
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookKnfType
import com.aplana.sbrf.taxaccounting.model.util.Pair
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory
import com.aplana.sbrf.taxaccounting.script.service.*
import com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils
import com.aplana.sbrf.taxaccounting.service.LockDataService
import com.aplana.sbrf.taxaccounting.service.component.lock.locker.DeclarationLocker
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode

new Calculate(this).run()

@TypeChecked
class Calculate extends AbstractScriptClass {

    NdflPersonService ndflPersonService
    DeclarationData declarationData
    Department department
    RefBookFactory refBookFactory
    ReportPeriodService reportPeriodService
    DepartmentService departmentService
    SourceService sourceService
    PersonService personService
    Set<Long> unacceptedSources
    DeclarationLocker declarationLocker
    LockDataService lockDataService

    //Коды Асну
    List<Long> asnuCache = []
    // Даты создания налоговой формы
    Map<Long, Date> creationDateCache = [:]

    List<LockData> locks

    @TypeChecked(TypeCheckingMode.SKIP)
    Calculate(scriptClass) {
        //noinspection GroovyAssignabilityCheck
        super(scriptClass)
        if (scriptClass.getBinding().hasVariable("ndflPersonService")) {
            this.ndflPersonService = (NdflPersonService) scriptClass.getProperty("ndflPersonService")
        }
        if (scriptClass.getBinding().hasVariable("departmentService")) {
            this.departmentService = (DepartmentService) scriptClass.getProperty("departmentService")
        }
        if (scriptClass.getBinding().hasVariable("declarationData")) {
            this.declarationData = (DeclarationData) scriptClass.getProperty("declarationData")
            if (this.declarationData?.departmentId) {
                this.department = departmentService.get(this.declarationData.departmentId)
            }
        }
        if (scriptClass.getBinding().hasVariable("refBookFactory")) {
            this.refBookFactory = (RefBookFactory) scriptClass.getProperty("refBookFactory")
        }
        if (scriptClass.getBinding().hasVariable("reportPeriodService")) {
            this.reportPeriodService = (ReportPeriodService) scriptClass.getProperty("reportPeriodService")
        }
        if (scriptClass.getBinding().hasVariable("declarationService")) {
            this.declarationService = (DeclarationService) scriptClass.getProperty("declarationService")
        }
        if (scriptClass.getBinding().hasVariable("sourceService")) {
            this.sourceService = (SourceService) scriptClass.getProperty("sourceService")
        }
        if (scriptClass.getBinding().hasVariable("personService")) {
            this.personService = (PersonService) scriptClass.getProperty("personService")
        }
        if (scriptClass.getBinding().hasVariable("unacceptedSources")) {
            this.unacceptedSources = (Set<Long>) scriptClass.getProperty("unacceptedSources")
        }
        if (scriptClass.getBinding().hasVariable("declarationLocker")) {
            this.declarationLocker = (DeclarationLocker) scriptClass.getProperty("declarationLocker")
        }
        if (scriptClass.getBinding().hasVariable("lockDataService")) {
            this.lockDataService = (LockDataService) scriptClass.getProperty("lockDataService")
        }
    }

    @Override
    void run() {
        try {
            initConfiguration()
            switch (formDataEvent) {
                case FormDataEvent.CALCULATE:
                    clearData()
                    consolidation()
            }
        } catch (Throwable e) {
            logger.error(e.toString())
            e.printStackTrace()
        } finally {
            lockDataService.unlockMultipleTasks(locks?.key)
        }
    }

    /**
     * Очистка данных налоговой формы
     * @return
     */
    def clearData() {
        //удаляем рассчитанные данные если есть
        ndflPersonService.deleteAll(declarationData.id)
        //Удаляем все сформированные ранее отчеты налоговой формы
        declarationService.deleteReport(declarationData.id)
    }

    /**
     * Консолидация данных
     */
    void consolidation() {
        Date consolidationDate = new Date()

        long time = System.currentTimeMillis()

        Department parentTB = departmentService.getParentTB(declarationData.departmentId)
        ConfigurationParamModel configurationParamModel = configurationService.getCommonConfig(userInfo)
        Integer dataSelectionDepth = Integer.valueOf(configurationParamModel?.get(ConfigurationParam?.CONSOLIDATION_DATA_SELECTION_DEPTH)?.get(0)?.get(0))
        ReportPeriod reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)

        ConsolidationSourceDataSearchFilter filter = ConsolidationSourceDataSearchFilter.builder()
                .currentDate(new Date())
                .periodStartDate(reportPeriod.startDate)
                .periodEndDate(reportPeriod.endDate)
                .dataSelectionDepth(reportPeriod.taxPeriod.year - dataSelectionDepth)
                .departmentId(parentTB.id)
                .declarationType(DeclarationType.NDFL_PRIMARY)
                .consolidateDeclarationDataYear(reportPeriod.taxPeriod.year)
                .build()
        //noinspection GroovyAssignabilityCheck
        logForDebug("Инициализация данных для поиска источников, (" + ScriptUtils.calcTimeMillis(time))
        ScriptUtils.checkInterrupted()

        time = System.currentTimeMillis()

        List<ConsolidationIncome> operationsForConsolidationList = ndflPersonService.fetchIncomeSourcesConsolidation(filter)

        logForDebug("Определение списка операций, которые нужно включить в КНФ. Отобрано ${operationsForConsolidationList.size()} строк, (" + ScriptUtils.calcTimeMillis(time))
        ScriptUtils.checkInterrupted()

        time = System.currentTimeMillis()

        Set<Long> allSources = operationsForConsolidationList.declarationDataId.toSet()
        locks = declarationLocker.establishLock(new ArrayList<Long>(allSources), OperationType.CONSOLIDATE, userInfo, logger)
        if (logger.containsLevel(LogLevel.ERROR)) {
            return
        }
        logForDebug("Установка блокировок на источники, (" + ScriptUtils.calcTimeMillis(time))

        time = System.currentTimeMillis()

        ScriptUtils.checkInterrupted()

        Map<Pair<String, Long>, List<ConsolidationIncome>> incomesGroupedByOperationIdAndAsnu = [:]
        List<String> kppList = null
        List<Long> declarationDataPersonIds = null
        Map<Long, List<NdflPerson>> ndflPersonById = null
        Set<String> incomeCodesApp2Included = null
        for (ConsolidationIncome income : operationsForConsolidationList) {
            if (declarationData.getKnfType() == RefBookKnfType.BY_KPP) {
                if (kppList == null) {
                    kppList = declarationService.getDeclarationDataKppList(declarationData.id)
                }
                if (kppList && !kppList.contains(income.kpp)) {
                    continue
                }
            } else if (declarationData.getKnfType() == RefBookKnfType.BY_PERSON) {
                if (ndflPersonById == null) {
                    ndflPersonById = ndflPersonService.findByIdList(operationsForConsolidationList.collect {
                        it.ndflPersonId
                    }).collectEntries { [it.id, it] }
                }
                if (declarationDataPersonIds == null) {
                    declarationDataPersonIds = declarationService.getDeclarationDataPersonIds(declarationData.id)
                }
                if (declarationDataPersonIds && !declarationDataPersonIds.contains(ndflPersonById.get(income.ndflPersonId).personId)) {
                    continue
                }
            } else if (declarationData.getKnfType() == RefBookKnfType.FOR_APP2) {
                if (incomeCodesApp2Included == null) {
                    def incomeCodeRecords = refBookFactory.getDataProvider(RefBook.Id.INCOME_CODE.getId()).getRecordDataWhere(" status = 0 and app2_include = 1 ")
                    incomeCodesApp2Included = incomeCodeRecords.collect { key, value -> value."CODE".stringValue }.toSet()
                }
                if (!incomeCodesApp2Included.contains(income.incomeCode)) {
                    continue
                }
            }
            if (!income.isDummy()) {
                Pair<String, Long> operationAsnu = new Pair(income.operationId, income.asnuId)
                List<ConsolidationIncome> group = incomesGroupedByOperationIdAndAsnu.get(operationAsnu)
                if (group == null) {
                    incomesGroupedByOperationIdAndAsnu.put(operationAsnu, [income])
                } else {
                    group << income
                }
            }
        }

        //noinspection GroovyAssignabilityCheck
        logForDebug("Группировка строк по идОперации и АСНУ, (" + ScriptUtils.calcTimeMillis(time))
        ScriptUtils.checkInterrupted()

        time = System.currentTimeMillis()

        // Группруем идентификаторы источников по состоянию: Принята/Не принята. Далее они понадобятся при формировании уведомлений
        Map<Boolean, Set<Long>> acceptedSources = [:]
        acceptedSources.put(Boolean.TRUE, new HashSet<Long>())
        acceptedSources.put(Boolean.FALSE, new HashSet<Long>())

        // Строки отобранные для включения в КНФ
        List<ConsolidationIncome> pickedRows = []

        // В одном цикле делаем сразу несколько действий: 1. Отбираем источники в состояниии "Принята". 2. Находим дубли, сортируем и избавляемся от них
        for (List<ConsolidationIncome> incomes : incomesGroupedByOperationIdAndAsnu.values()) {
            ScriptUtils.checkInterrupted()
            // Группируем все отобранные строки по отпечатку ConsolidationIncome
            // Мы не сравниваем по идОперации и АСНУ поскольку мы внутри группы по идОперации и АСНУ
            Map<String, List<ConsolidationIncome>> transitionalRows = [:]
            Iterator<ConsolidationIncome> incomesIterator = incomes.iterator()
            while (incomesIterator.hasNext()) {
                //noinspection GroovyAssignabilityCheck
                ConsolidationIncome candidate = incomesIterator.next()
                if (candidate.accepted) {

                    acceptedSources.get(Boolean.TRUE) << candidate.declarationDataId

                    String uuid = ScriptUtils.getConsolidationIncomeUUID(candidate)

                    /* список с кандидатами на попаданием в КНФ. Чаще всего в этом списке будет одна строка, чтобы туда попасть нужно иметь уникальный отпечаток.
                    Список создан из-за того, что дубликатами признаются строки из разных источников
                     */
                    List<ConsolidationIncome> pickedCandidates = transitionalRows.get(uuid)
                    if (pickedCandidates == null) {
                        // Если кандитатов в КНФ нет добавляем нового кандидата
                        transitionalRows.put(uuid, [candidate])
                    } else {
                        // Если кандидат есть
                        if (pickedCandidates.get(0).declarationDataId == candidate.declarationDataId) {
                            // Добавлем еще строку если кандидат из той же ПНФ
                            pickedCandidates.add(candidate)
                        } else {
                            if (pickedCandidates.get(0).correctionDate == null) {
                                /* Проверяем если строка не из корректировочного периода, тогда устанавливаем минимальное значение
                                 даты корректировке, чтобы при сортировке эта строка была первой в списке
                                  */
                                pickedCandidates.get(0).correctionDate = new Date(Long.MIN_VALUE)
                            }
                            if (candidate.correctionDate == null) {
                                // Проверяем что кандидат не из корректировочного периода
                                candidate.correctionDate = new Date(Long.MIN_VALUE)
                            }
                            // ЧТобы новый кандидат стал отобранным кандидатом он должен иметь более поздний год
                            // Если года равны тогда новый кандидадт должен иметь больший код отчетного периода
                            // Если периоды равны тогда новый кандидат должен иметь более позднюю дату корректировки
                            // Если даты корректировки одинаковы, тогда новый кандидат должен иметь более позднюю дату создания
                            if (pickedCandidates.get(0).year < candidate.year
                                    || pickedCandidates.get(0).periodCode < candidate.periodCode
                                    || pickedCandidates.get(0).correctionDate < candidate.correctionDate
                                    || getDeclarationDataCreationDate(pickedCandidates.get(0).declarationDataId) < getDeclarationDataCreationDate(candidate.declarationDataId)) {
                                // Если новый кандидат выбран. Очищаем список отобранных кандидатов и добавляем нового кандидата.
                                pickedCandidates.clear()
                                pickedCandidates.add(candidate)
                            }
                        }
                    }
                } else {
                    acceptedSources.get(Boolean.FALSE) << candidate.declarationDataId
                    incomesIterator.remove()
                }
            }

            for (List<ConsolidationIncome> passedIncomes : transitionalRows.values()) {
                pickedRows.addAll(passedIncomes)
            }
        }

        if (pickedRows && declarationData.getKnfType() == RefBookKnfType.BY_NONHOLDING_TAX) {
            Map<String, List<ConsolidationIncome>> incomesByInp = pickedRows.groupBy { it.inp }
            for (def personIncomes : incomesByInp.values()) {
                BigDecimal НачисленныйНалогПоОперации = 0
                BigDecimal УдержанныйНалогПоОперации = 0
                BigDecimal НеУдержанныйНалогПоОперации = 0
                BigDecimal ИзлишнеУдержанныйНалогПоОперации = 0
                for (def income : personIncomes) {
                    НачисленныйНалогПоОперации += income.calculatedTax ?: 0
                    УдержанныйНалогПоОперации += income.withholdingTax ?: 0
                    НеУдержанныйНалогПоОперации += income.notHoldingTax ?: 0
                    ИзлишнеУдержанныйНалогПоОперации += income.overholdingTax ?: 0
                }
                if (НачисленныйНалогПоОперации <= УдержанныйНалогПоОперации && НеУдержанныйНалогПоОперации <= ИзлишнеУдержанныйНалогПоОперации) {
                    pickedRows.removeAll(personIncomes)
                }
            }
        }

        // Если в итоге число строк пустое, выкидываем ошибку, учитывая, были ли найдены подходящие данные в непринятых ПНФ
        if (pickedRows.isEmpty()) {
            Set<Long> acceptedPnfIds = acceptedSources.get(Boolean.TRUE)
            Set<Long> unacceptedPnfIds = acceptedSources.get(Boolean.FALSE)
            if (acceptedPnfIds.isEmpty() && !unacceptedPnfIds.isEmpty()) {
                unacceptedSources?.addAll(unacceptedPnfIds)
                logger.error("Формы содержащие данные для включения в КНФ существуют, но все они находятся в состоянии \"Не принята\". Номера найденных ПНФ: %s", unacceptedPnfIds.join(", "))
            } else {
                logger.error("Не найдено ни одной ПНФ, содержащей данные для включения в КНФ")
            }
            return
        }

        logForDebug("Избавление от дублирующих и фиктивных строк, определение списка операций для включения в КНФ. Включено в КНФ ${pickedRows.size()} строк, (" + ScriptUtils.calcTimeMillis(time))
        ScriptUtils.checkInterrupted()
        time = System.currentTimeMillis()

        // Доходы сгруппированные по физлицам
        Map<Long, List<? extends NdflPersonIncome>> incomesGroupedByPerson = [:]
        // Доходы сгруппированные по идОперации и физлицу. Используются для нахождения вычетов и авансов
        Map<Pair<String, Long>, List<ConsolidationIncome>> incomesGroupedByOperationAndPerson = [:]

        // Заполняем сгруппированные доходы
        for (ConsolidationIncome income : pickedRows) {
            Pair<String, String> operationAndInpKey = new Pair(income.operationId, income.inp)
            Pair<String, Long> operationAndPersonKey = new Pair(income.operationId, income.ndflPersonId)
            List<? extends NdflPersonIncome> personGroup = incomesGroupedByPerson.get(income.ndflPersonId)
            List<ConsolidationIncome> operationAndPersonGroup = incomesGroupedByOperationAndPerson.get(operationAndPersonKey)
            if (personGroup == null) {
                incomesGroupedByPerson.put(income.ndflPersonId, [income])
            } else {
                personGroup << income
            }
            if (operationAndPersonGroup == null) {
                incomesGroupedByOperationAndPerson.put(operationAndPersonKey, [income])
            } else {
                operationAndPersonGroup << income
            }
        }

        List<Long> incomeIdsForFetchingDeductionsAndPrepayments = []

        // Идентификаторы доходов для получения вычетов и авансов
        for (List<ConsolidationIncome> incomes : incomesGroupedByOperationAndPerson.values()) {
            incomeIdsForFetchingDeductionsAndPrepayments << incomes.get(0).id
        }

        //noinspection GroovyAssignabilityCheck
        logForDebug("Подготовка аргументов для получения данных разделов 1, 3, 4, (" + ScriptUtils.calcTimeMillis(time))
        ScriptUtils.checkInterrupted()
        time = System.currentTimeMillis()

        List<NdflPerson> ndflPersonList = ndflPersonService.findByIdList(new ArrayList<>(incomesGroupedByPerson.keySet()))

        //noinspection GroovyAssignabilityCheck
        logForDebug("Получение данных раздела 1, (" + ScriptUtils.calcTimeMillis(time))
        ScriptUtils.checkInterrupted()
        time = System.currentTimeMillis()

        List<NdflPersonDeduction> deductions = ndflPersonService.fetchDeductionsForConsolidation(incomeIdsForFetchingDeductionsAndPrepayments)

        //noinspection GroovyAssignabilityCheck
        logForDebug("Получение данных раздела 3, (" + ScriptUtils.calcTimeMillis(time))
        ScriptUtils.checkInterrupted()
        time = System.currentTimeMillis()

        List<NdflPersonPrepayment> prepayments = ndflPersonService.fetchPrepaymentsForConsolidation(incomeIdsForFetchingDeductionsAndPrepayments)

        //noinspection GroovyAssignabilityCheck
        logForDebug("Получение данных раздела 4, (" + ScriptUtils.calcTimeMillis(time))
        ScriptUtils.checkInterrupted()
        time = System.currentTimeMillis()

        List<NdflPerson> refBookPersonList = ndflPersonService.fetchRefBookPersonsAsNdflPerson(ndflPersonList.personId, new Date())
        //noinspection GroovyAssignabilityCheck
        logForDebug("Получение данных справочника ФЛ, (" + ScriptUtils.calcTimeMillis(time))
        ScriptUtils.checkInterrupted()
        time = System.currentTimeMillis()

        Map<Long, NdflPerson> refBookPersonsGroupedById = [:]

        for (NdflPerson refBookPerson : refBookPersonList) {
            refBookPersonsGroupedById.put(refBookPerson.recordId, refBookPerson)
        }

        Map<Long, List<NdflPersonDeduction>> deductionsGroupedByPerson = [:]
        Map<Long, List<NdflPersonPrepayment>> prepaymentsGroupedByPerson = [:]

        for (NdflPersonDeduction deduction : deductions) {
            List<NdflPersonDeduction> group = deductionsGroupedByPerson.get(deduction.ndflPersonId)
            if (group == null) {
                deductionsGroupedByPerson.put(deduction.ndflPersonId, [deduction])
            } else {
                group << deduction
            }
        }

        for (NdflPersonPrepayment prepayment : prepayments) {
            List<NdflPersonPrepayment> group = prepaymentsGroupedByPerson.get(prepayment.ndflPersonId)
            if (group == null) {
                prepaymentsGroupedByPerson.put(prepayment.ndflPersonId, [prepayment])
            } else {
                group << prepayment
            }
        }

        for (NdflPerson ndflPerson : ndflPersonList) {
            ndflPerson.incomes = incomesGroupedByPerson.get(ndflPerson.id) as List<NdflPersonIncome>
            ndflPerson.deductions = deductionsGroupedByPerson.get(ndflPerson.id)
            ndflPerson.prepayments = prepaymentsGroupedByPerson.get(ndflPerson.id)
        }

        // Физлица для сохранения сгуппированные по идентификатору физлица в справочнике
        Map<Long, NdflPerson> ndflPersonsToPersistGroupedByRefBookPersonId = [:]

        // Данные для заполнения раздела 1

        for (NdflPerson declarationDataPerson : ndflPersonList) {
            NdflPerson refBookPerson = refBookPersonsGroupedById.get(declarationDataPerson.recordId)
            declarationDataPerson.personId = refBookPerson.personId
            if (declarationDataPerson.recordId == null) {
                logger.error("ПНФ: ${declarationDataPerson.declarationDataId} " +
                        "Раздел 1. Строка: ${declarationDataPerson.rowNum}. Для ФЛ:" +
                        "${declarationDataPerson.lastName + " " + declarationDataPerson.firstName + " " + (declarationDataPerson.middleName ?: "")}" +
                        ", ИНП: ${declarationDataPerson.inp}, отсутствует ссылка на запись Реестра физических лиц. Выполните идентификацию ПНФ")
                continue
            }
            if (refBookPerson == null) {
                logger.error("ПНФ: ${declarationDataPerson.declarationDataId} Раздел 1. Для ФЛ:" +
                        "${declarationDataPerson.lastName + " " + declarationDataPerson.firstName + " " + (declarationDataPerson.middleName ?: "")}" +
                        ", ИНП: ${declarationDataPerson.inp} установлена ссылка на физическое лицо из Реестр физических лиц, для которой отсутствует актуальная версия.")
                continue
            }

            declarationDataPerson.id = null
            declarationDataPerson.modifiedDate = consolidationDate
            declarationDataPerson.modifiedBy = null
            declarationDataPerson.declarationDataId = declarationData.id

            if (refBookPerson.idDocType == null && refBookPerson.idDocNumber == null) {
                logger.warn("Раздел 1. ФЛ: %s, идентификатор ФЛ: %s, включено в форму без заполнения Графы 10 (\"ДУЛ Код\") и Графы 11 (\"ДУЛ Номер\"), т.к. информация о ДУЛ, включаемом в отчетность, отсутствует  в Реестре ФЛ.",
                        "${refBookPerson.lastName + " " + refBookPerson.firstName + " " + (refBookPerson.middleName ?: "")}",
                        refBookPerson.recordId)
            }

            NdflPerson persistingPerson = ndflPersonsToPersistGroupedByRefBookPersonId.get(declarationDataPerson.recordId)
            if (persistingPerson != null) {
                persistingPerson.incomes.addAll(declarationDataPerson.incomes)
                persistingPerson.deductions.addAll(declarationDataPerson.deductions)
                persistingPerson.prepayments.addAll(declarationDataPerson.prepayments)
                continue
            }

            if (refBookPerson.inp != declarationDataPerson.inp) {
                declarationDataPerson.inp = refBookPerson.inp
            }
            if (refBookPerson.lastName != declarationDataPerson.lastName) {
                declarationDataPerson.lastName = refBookPerson.lastName
            }
            if (refBookPerson.firstName != declarationDataPerson.firstName) {
                declarationDataPerson.firstName = refBookPerson.firstName
            }
            if (refBookPerson.middleName != declarationDataPerson.middleName) {
                declarationDataPerson.middleName = refBookPerson.middleName
            }
            if (refBookPerson.birthDay != declarationDataPerson.birthDay) {
                declarationDataPerson.birthDay = refBookPerson.birthDay
            }
            if (refBookPerson.citizenship != declarationDataPerson.citizenship) {
                declarationDataPerson.citizenship = refBookPerson.citizenship
            }
            if (refBookPerson.innNp != declarationDataPerson.innNp) {
                declarationDataPerson.innNp = refBookPerson.innNp
            }
            if (refBookPerson.innForeign != declarationDataPerson.innForeign) {
                declarationDataPerson.innForeign = refBookPerson.innForeign
            }
            if (refBookPerson.idDocType != declarationDataPerson.idDocType) {
                declarationDataPerson.idDocType = refBookPerson.idDocType
            }
            if (refBookPerson.idDocNumber != declarationDataPerson.idDocNumber) {
                declarationDataPerson.idDocNumber = refBookPerson.idDocNumber
            }
            if (refBookPerson.status != declarationDataPerson.status) {
                declarationDataPerson.status = refBookPerson.status
            }
            if (refBookPerson.regionCode != declarationDataPerson.regionCode) {
                declarationDataPerson.regionCode = refBookPerson.regionCode
            }
            if (refBookPerson.postIndex != declarationDataPerson.postIndex) {
                declarationDataPerson.postIndex = refBookPerson.postIndex
            }
            if (refBookPerson.area != declarationDataPerson.area) {
                declarationDataPerson.area = refBookPerson.area
            }
            if (refBookPerson.city != declarationDataPerson.city) {
                declarationDataPerson.city = refBookPerson.city
            }
            if (refBookPerson.locality != declarationDataPerson.locality) {
                declarationDataPerson.locality = refBookPerson.locality
            }
            if (refBookPerson.street != declarationDataPerson.street) {
                declarationDataPerson.street = refBookPerson.street
            }
            if (refBookPerson.house != declarationDataPerson.house) {
                declarationDataPerson.house = refBookPerson.house
            }
            if (refBookPerson.building != declarationDataPerson.building) {
                declarationDataPerson.building = refBookPerson.building
            }
            if (refBookPerson.flat != declarationDataPerson.flat) {
                declarationDataPerson.flat = refBookPerson.flat
            }
            if (refBookPerson.snils != declarationDataPerson.snils) {
                declarationDataPerson.snils = refBookPerson.snils
            }
            if (refBookPerson.countryCode != declarationDataPerson.countryCode) {
                declarationDataPerson.countryCode = refBookPerson.countryCode
            }
            if (refBookPerson.address != declarationDataPerson.address) {
                declarationDataPerson.address = refBookPerson.address
            }

            ndflPersonsToPersistGroupedByRefBookPersonId.put(declarationDataPerson.recordId, declarationDataPerson)
        }

        if (logger.containsLevel(LogLevel.ERROR)) return

        //noinspection GroovyAssignabilityCheck
        logForDebug("Консолидация данных, (" + ScriptUtils.calcTimeMillis(time))
        ScriptUtils.checkInterrupted()
        time = System.currentTimeMillis()

        List<NdflPerson> ndflPersonsToPersistList = new ArrayList<>(ndflPersonsToPersistGroupedByRefBookPersonId.values())

        Collections.sort(ndflPersonsToPersistList, NdflPerson.getComparator())

        ndflPersonService.fillNdflPersonIncomeSortFields(ndflPersonsToPersistList)

        //noinspection GroovyAssignabilityCheck
        logForDebug("Сортировка данных раздела 1, (" + ScriptUtils.calcTimeMillis(time))

        Long personRowNum = 0L
        BigDecimal incomeRowNum = new BigDecimal("0")
        BigDecimal deductionRowNum = new BigDecimal("0")
        BigDecimal prepaymentRowNum = new BigDecimal("0")
        for (NdflPerson ndflPerson : ndflPersonsToPersistList) {
            Collections.sort(ndflPerson.incomes, NdflPersonIncome.getComparator())

            Collections.sort(ndflPerson.deductions, NdflPersonDeduction.getComparator(ndflPerson))

            Collections.sort(ndflPerson.prepayments, NdflPersonPrepayment.getComparator(ndflPerson))

            for (NdflPersonIncome income : ndflPerson.incomes) {
                incomeRowNum = incomeRowNum.add(new BigDecimal("1"))
                income.rowNum = incomeRowNum
            }

            for (NdflPersonDeduction deduction : ndflPerson.deductions) {
                deductionRowNum = deductionRowNum.add(new BigDecimal("1"))
                deduction.rowNum = deductionRowNum
            }

            for (NdflPersonPrepayment prepayment : ndflPerson.prepayments) {
                prepaymentRowNum = prepaymentRowNum.add(new BigDecimal("1"))
                prepayment.rowNum = prepaymentRowNum
            }

            ndflPerson.rowNum = ++personRowNum
        }

        //noinspection GroovyAssignabilityCheck
        logForDebug("Сортировка разделов 2,3,4 и присвоение № пп, (" + ScriptUtils.calcTimeMillis(time))
        ScriptUtils.checkInterrupted()
        time = System.currentTimeMillis()

        ndflPersonService.save(ndflPersonsToPersistList)

        //noinspection GroovyAssignabilityCheck
        logForDebug("Сохранение данных в БД, (" + ScriptUtils.calcTimeMillis(time))
        ScriptUtils.checkInterrupted()

        // Формируем уведомления

        // Тербанки в состоянии "Принята"
        Map<Department, Integer> acceptedTbDepartments = [:]
        // Источники сгруппированные по тербанкам
        Map<Department, List<Long>> acceptedSourcesByTb = [:]

        List<Long> allSourcesIdList = []

        allSourcesIdList.addAll(acceptedSources.get(Boolean.TRUE))
        allSourcesIdList.addAll(acceptedSources.get(Boolean.FALSE))

        sourceService.deleteDeclarationConsolidateInfo(declarationData.id)
        sourceService.addDeclarationConsolidationInfo(declarationData.id, allSourcesIdList)

        Set<Long> allPrimariesExceptAccepted = []
        def tbWithChildren = departmentService.getAllChildren(declarationData.departmentId)
        for (def department : tbWithChildren) {
            def departmentPnfs = declarationService.findAllDeclarationData(DeclarationType.NDFL_PRIMARY, department.id, reportPeriod.id)
            for (def departmentPnf : departmentPnfs) {
                if (!allSourcesIdList.contains(departmentPnf.id)) {
                    allPrimariesExceptAccepted.add(departmentPnf.id)
                }
            }
        }
        for (Long sourceId : acceptedSources.get(Boolean.TRUE)) {
            ScriptUtils.checkInterrupted()
            DeclarationData source = declarationService.getDeclarationData(sourceId)
            Department tbOfSource = departmentService.getParentTB(source.departmentId)
            Integer count = acceptedTbDepartments.get(tbOfSource)
            List<Long> sources = acceptedSourcesByTb.get(tbOfSource)
            if (count == null) {
                acceptedTbDepartments.put(tbOfSource, 1)
            } else {
                acceptedTbDepartments.put(tbOfSource, ++count)
            }
            if (sources == null) {
                acceptedSourcesByTb.put(tbOfSource, [sourceId])
            } else {
                sources << sourceId
            }
        }

        for (Department department : acceptedTbDepartments.keySet()) {
            logger.info("Консолидация выполнена из ПНФ ТБ: \"%s\" (всего %d): %s",
                    department.shortName,
                    acceptedTbDepartments.get(department),
                    acceptedSourcesByTb.get(department)?.join(", "))
        }

        if (!(acceptedSources.get(Boolean.FALSE).isEmpty())) {
            Map<Department, Integer> notAcceptedTbDepartments = [:]
            Map<Department, List<Long>> notAcceptedSourcesByTb = [:]

            for (Long sourceId : acceptedSources.get(Boolean.FALSE)) {
                ScriptUtils.checkInterrupted()
                DeclarationData source = declarationService.getDeclarationData(sourceId)
                Department tbOfSource = departmentService.getParentTB(source.departmentId)
                Integer count = notAcceptedTbDepartments.get(tbOfSource)
                List<Long> sources = notAcceptedSourcesByTb.get(tbOfSource)
                if (count == null) {
                    notAcceptedTbDepartments.put(tbOfSource, 1)
                } else {
                    notAcceptedTbDepartments.put(tbOfSource, ++count)
                }
                if (sources == null) {
                    notAcceptedSourcesByTb.put(tbOfSource, [sourceId])
                } else {
                    sources << sourceId
                }
            }

            for (Department department : notAcceptedTbDepartments.keySet()) {
                logger.info("ПНФ из ТБ: \"%s\" не использованы в консолидации, так как не находятся в состоянии \"Принята\" (всего %d): %s",
                        department.shortName,
                        notAcceptedTbDepartments.get(department),
                        notAcceptedSourcesByTb.get(department)?.join(", "))
            }
        }

        if (allPrimariesExceptAccepted) {
            logger.info("ПНФ из ТБ: \"${department.name}\" (и дочерних подразделений) не использованы в консолидации, так как не содержат операций, " +
                    "попадающих в период «${reportPeriod.taxPeriod.year}, ${reportPeriod.name}» и содержащих КПП/ОКТМО относящихся к указанному ТБ: " +
                    "${allPrimariesExceptAccepted.join(", ")} (всего ${allPrimariesExceptAccepted.size()})")
        }

    }

    /**
     * Получить дату создания налоговой формы
     * @param declarationDataId идентификатор налоговой формы
     * @return дата создания налоговой формы
     */
    Date getDeclarationDataCreationDate(Long declarationDataId) {
        Date toReturn = creationDateCache.get(declarationDataId)
        if (toReturn == null) {
            toReturn = declarationService.getDeclarationDataCreationDate(declarationDataId)
            creationDateCache.put(declarationDataId, toReturn)
        }
        return toReturn
    }

    /**
     * Сравнить значения
     * @param v1 значение 1
     * @param v2 значение 2
     * @param comparator компаратор
     * @return отрицательное значение если параметр v1 меньше параметра v2, положительное значение если параметр v1
     * больше параметра v2, 0 если параметры v1 и v2 равны
     */
    @TypeChecked(TypeCheckingMode.SKIP)
    static int compareValues(v1, v2, comparator) {
        int result = 0
        if (v1 != null && v2 != null) {
            if (comparator != null) {
                result = comparator.compare(v1, v2)
            } else {
                result = v1.compareTo(v2)
            }
        } else if (v1 == null && v2 != null) {
            return Integer.MAX_VALUE
        } else if (v1 != null) {
            return Integer.MIN_VALUE
        }
        return result
    }
}
