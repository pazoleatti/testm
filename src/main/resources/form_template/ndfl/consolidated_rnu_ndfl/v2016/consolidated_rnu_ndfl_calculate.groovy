package form_template.ndfl.consolidated_rnu_ndfl.v2016

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.ndfl.*
import com.aplana.sbrf.taxaccounting.model.consolidation.ConsolidationIncome
import com.aplana.sbrf.taxaccounting.model.consolidation.ConsolidationSourceDataSearchFilter
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.util.DeclarationStringComparator
import com.aplana.sbrf.taxaccounting.model.util.Pair
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory
import com.aplana.sbrf.taxaccounting.script.service.DeclarationService
import com.aplana.sbrf.taxaccounting.script.service.DepartmentService
import com.aplana.sbrf.taxaccounting.script.service.NdflPersonService
import com.aplana.sbrf.taxaccounting.script.service.ReportPeriodService
import com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import groovy.xml.MarkupBuilder

new Calculate(this).run()

@TypeChecked
class Calculate extends AbstractScriptClass {

    NdflPersonService ndflPersonService
    DeclarationData declarationData
    RefBookFactory refBookFactory
    ReportPeriodService reportPeriodService
    DepartmentService departmentService

    FileWriter xml

    final String TEMPLATE_PERSON_FL = "%s, ИНП: %s"

    final String R_PERSON = "Физические лица"

    final String RF_RECORD_ID = "RECORD_ID"

    // Кэш провайдеров cправочников
    Map<Long, RefBookDataProvider> providerCache = [:]
    //Коды Асну
    List<Long> asnuCache = []
    // Даты создания налоговой формы
    Map<Long, Date> creationDateCache = [:]

    // Дата окончания отчетного периода
    Date periodStartDate = null
    // Дата окончания отчетного периода
    Date periodEndDate = null

    private Calculate() {
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    public Calculate(scriptClass) {
        super(scriptClass)
        if (scriptClass.getBinding().hasVariable("ndflPersonService")) {
            this.ndflPersonService = (NdflPersonService) scriptClass.getProperty("ndflPersonService");
        }
        if (scriptClass.getBinding().hasVariable("declarationData")) {
            this.declarationData = (DeclarationData) scriptClass.getProperty("declarationData");
        }
        if (scriptClass.getBinding().hasVariable("refBookFactory")) {
            this.refBookFactory = (RefBookFactory) scriptClass.getProperty("refBookFactory");
        }
        if (scriptClass.getBinding().hasVariable("reportPeriodService")) {
            this.reportPeriodService = (ReportPeriodService) scriptClass.getProperty("reportPeriodService");
        }
        if (scriptClass.getBinding().hasVariable("xml")) {
            this.xml = (FileWriter) scriptClass.getProperty("xml");
        }
        if (scriptClass.getBinding().hasVariable("declarationService")) {
            this.declarationService = (DeclarationService) scriptClass.getProperty("declarationService");
        }
        if (scriptClass.getBinding().hasVariable("departmentService")) {
            this.departmentService = (DepartmentService) scriptClass.getProperty("departmentService");
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
                    if (logger.containsLevel(LogLevel.ERROR)) return
                    generateXml()
            }
        } catch (Throwable e) {
            logger.error(e.toString())
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

        long time = System.currentTimeMillis();

        Department parentTB = departmentService.getParentTB(declarationData.departmentId)
        ConfigurationParamModel configurationParamModel = configurationService.getCommonConfig(userInfo);
        Integer dataSelectionDepth = Integer.valueOf(configurationParamModel?.get(ConfigurationParam.CONSOLIDATION_DATA_SELECTION_DEPTH).get(0).get(0))
        ReportPeriod reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)

        ConsolidationSourceDataSearchFilter.Builder filterBuilder = new ConsolidationSourceDataSearchFilter.Builder()
        filterBuilder.currentDate(new Date())
                .periodStartDate(reportPeriod.startDate)
                .periodEndDate(reportPeriod.endDate)
                .dataSelectionDepth(reportPeriod.taxPeriod.year - dataSelectionDepth)
                .departmentId(parentTB.id)
                .declarationType(DeclarationType.NDFL_PRIMARY)
                .consolidateDeclarationDataYear(reportPeriod.taxPeriod.year)

        logForDebug("Инициализация данных для поиска источников, (" + ScriptUtils.calcTimeMillis(time))
        ScriptUtils.checkInterrupted()

        time = System.currentTimeMillis();

        List<ConsolidationIncome> operationsForConsolidationList = ndflPersonService.fetchIncomeSourcesConsolidation(filterBuilder.createConsolidationSourceDataSearchFilter())

        logForDebug("Определение списка операций, которые нужно включить в КНФ. Отобрано ${operationsForConsolidationList.size()} строк, (" + ScriptUtils.calcTimeMillis(time))
        ScriptUtils.checkInterrupted()

        time = System.currentTimeMillis();

        Map<Pair<String, Long>, List<ConsolidationIncome>> incomesGroupedByOperationIdAndAsnu = [:]
        for (ConsolidationIncome income : operationsForConsolidationList) {
            Pair<String, Long> operationAsnu = new Pair(income.operationId, income.asnuId)
            List<ConsolidationIncome> group = incomesGroupedByOperationIdAndAsnu.get(operationAsnu)
            if (group == null) {
                incomesGroupedByOperationIdAndAsnu[operationAsnu] = [income]
            } else {
                group << income
            }
        }

        logForDebug("Группировка строк по идОперации и АСНУ, (" + ScriptUtils.calcTimeMillis(time))
        ScriptUtils.checkInterrupted()

        time = System.currentTimeMillis();

        // Избавляемся от фиктивных строк. Чтобы не перепроверять все строки выбираем только те, у которых идОперации = 0
        List<ConsolidationIncome> dummyRowsCandidates = []
        for (Long asnuId : getAsnuCache()) {
            dummyRowsCandidates.addAll(incomesGroupedByOperationIdAndAsnu[new Pair("0", asnuId)])
        }

        // Проверяем и удаляем фиктивные строки
        for (ConsolidationIncome dummyRowCandidate : dummyRowsCandidates) {
            if (dummyRowCandidate.taxBase == new BigDecimal("0") && dummyRowCandidate.taxRate == 0) {
                if ((dummyRowCandidate.incomeAccruedSumm == new BigDecimal("0") && dummyRowCandidate.calculatedTax == new BigDecimal("0"))
                        || (dummyRowCandidate.incomePayoutSumm == new BigDecimal("0") && dummyRowCandidate.withholdingTax == new BigDecimal("0"))) {
                    incomesGroupedByOperationIdAndAsnu[new Pair("0", dummyRowCandidate.asnuId)].remove(dummyRowCandidate)
                }
            }
        }

        // Группруем идентификаторы источников по состоянию: Принята/Не принята. Далее они понадобятся при формировании уведомлений
        Map<Boolean, Set<Long>> acceptedSources = [:]
        acceptedSources[Boolean.TRUE] = [].toSet()
        acceptedSources[Boolean.FALSE] = [].toSet()

        // Строки отобранные для включения в КНФ
        List<ConsolidationIncome> pickedRows = []

        // В одном цикле делаем сразу несколько действий: 1. Отбираем источники с сотсоянием принята. 2. Находим дубли, сортируем и избавляемся от них
        for (List<ConsolidationIncome> incomes : incomesGroupedByOperationIdAndAsnu.values()) {
            ScriptUtils.checkInterrupted()
            // Группируем все отобранные строки по отпечатку ConsolidationIncome
            // Мы не сравниваем по идОперации и АСНУ поскольку мы внутри группы по идОперации и АСНУ
            Map<String, List<ConsolidationIncome>> transitionalRows = [:]
            Iterator<ConsolidationIncome> incomesIterator = incomes.iterator()
            while (incomesIterator.hasNext()) {
                ConsolidationIncome candidate = incomesIterator.next()
                if (candidate.accepted) {

                    acceptedSources[Boolean.TRUE] << candidate.declarationDataId

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
                            // Добавлем еще строку если кандидат из той же КНФ
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
                                candidate.correctionDate = new Date(Long.MIN_VALUE);
                            }
                            // ЧТобы новый кандидат стал отобранным кандидатом он должен иметь более поздний год
                            // Если года равны тогда новый кандидадт должен иметь больший код отчетного периода
                            // Если периоды равны тогда новый кандидат должен иметь более позднюю дату корректировки
                            // Если даты корректировки одинаковы, тогда новый кандидат должен иметь более позднюю дату создания
                            if (pickedCandidates.get(0).year < candidate.year
                                    || pickedCandidates.get(0).periodCode.compareTo(candidate.periodCode) < 0
                                    || pickedCandidates.get(0).correctionDate.compareTo(candidate.correctionDate) < 0
                                    || getDeclarationDataCreationDate(pickedCandidates.get(0).declarationDataId).compareTo(getDeclarationDataCreationDate(candidate.declarationDataId)) < 0) {
                                // Если новый кандидат выбран. Очищаем список отобранных кандидатов и добавляем нового кандидата.
                                pickedCandidates.clear()
                                pickedCandidates.add(candidate)
                            }
                        }
                    }
                } else {
                    acceptedSources[Boolean.FALSE] << candidate.declarationDataId
                    incomesIterator.remove()
                }
            }

            for (List<ConsolidationIncome> passedIncomes : transitionalRows.values()) {
                pickedRows.addAll(passedIncomes)
            }
        }

        if (pickedRows.isEmpty()) {
            logger.error("Не найдено ни одной ПНФ в состоянии \"Принята\", содержащей данные для включения в КНФ")
            return
        }

        logForDebug("Избавление от дублирующих и фиктивных строк, определение списка операций для включения в КНФ. Включено в КНФ ${pickedRows.size()} строк, (" + ScriptUtils.calcTimeMillis(time))
        ScriptUtils.checkInterrupted()
        time = System.currentTimeMillis();

        // Доходы сгруппированные по идОперации и ИНП. Будут использоваться для вычисления доп полей сортировки
        Map<Pair<String, String>, List<ConsolidationIncome>> incomesGroupedByOperationAndInp = [:]
        // Доходы сгруппированные по физлицам
        Map<Long, List<? extends NdflPersonIncome>> incomesGroupedByPerson = [:]
        // Доходы сгруппированные по идОперации и физлицу. Используются для нахождения вычетов и авансов
        Map<Pair<String, Long>, List<ConsolidationIncome>> incomesGroupedByOperationAndPerson = [:]

        // Заполняем сгруппированные доходы
        for (ConsolidationIncome income : pickedRows) {
            Pair<String, String> operationAndInpKey = new Pair(income.operationId, income.inp)
            Pair<String, Long> operationAndPersonKey = new Pair(income.operationId, income.ndflPersonId)
            List<? extends NdflPersonIncome> personGroup = incomesGroupedByPerson.get(income.ndflPersonId)
            List<ConsolidationIncome> operationAndInpGroup = incomesGroupedByOperationAndInp.get(operationAndInpKey)
            List<ConsolidationIncome> operationAndPersonGroup = incomesGroupedByOperationAndPerson.get(operationAndPersonKey)
            if (operationAndInpGroup == null) {
                incomesGroupedByOperationAndInp.put(operationAndInpKey, [income])
            } else {
                operationAndInpGroup << income
            }
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

        // Вычисленные даты операции для сортировки и сгруппированные по идОперации и ИНП
        Map<Pair<String, String>, Date> operationDates = [:]

        for (List<ConsolidationIncome> group : incomesGroupedByOperationAndInp.values()) {
            Pair<String, String> key = new Pair(group.get(0).operationId, group.get(0).inp)
            List<Date> incomeAccruedDates = group.incomeAccruedDate
            incomeAccruedDates.removeAll([null])
            if (!incomeAccruedDates.isEmpty()) {
                Collections.sort(incomeAccruedDates)
                operationDates.put(key, incomeAccruedDates.get(0))
                continue
            }

            List<Date> incomePayoutDates = group.incomePayoutDate
            incomePayoutDates.removeAll([null])
            if (!incomePayoutDates.isEmpty()) {
                Collections.sort(incomePayoutDates)
                operationDates.put(key, incomePayoutDates.get(0))
                continue
            }

            List<Date> paymentDates = group.paymentDate
            paymentDates.removeAll([null])
            if (!paymentDates.isEmpty()) {
                Collections.sort(paymentDates)
                operationDates.put(key, paymentDates.get(0))
                continue
            }

            operationDates.put(key, null)
        }

        List<Long> incomeIdsForFetchingDeductionsAndPrepayments = []

        // Идентификаторы доходов для получения вычетов и авансов
        for (List<NdflPersonIncome> incomes : incomesGroupedByOperationAndPerson.values()) {
            incomeIdsForFetchingDeductionsAndPrepayments << incomes.get(0).id
        }

        logForDebug("Подготовка аргументов для получения данных разделов 1, 3, 4, (" + ScriptUtils.calcTimeMillis(time))
        ScriptUtils.checkInterrupted()
        time = System.currentTimeMillis();

        List<NdflPerson> ndflPersonList = ndflPersonService.findByIdList(new ArrayList<>(incomesGroupedByPerson.keySet()))

        logForDebug("Получение данных раздела 1, (" + ScriptUtils.calcTimeMillis(time))
        ScriptUtils.checkInterrupted()
        time = System.currentTimeMillis();

        List<NdflPersonDeduction> deductions = ndflPersonService.fetchDeductionsForConsolidation(incomeIdsForFetchingDeductionsAndPrepayments)

        logForDebug("Получение данных раздела 3, (" + ScriptUtils.calcTimeMillis(time))
        ScriptUtils.checkInterrupted()
        time = System.currentTimeMillis();

        List<NdflPersonPrepayment> prepayments = ndflPersonService.fetchPrepaymentsForConsolidation(incomeIdsForFetchingDeductionsAndPrepayments)

        logForDebug("Получение данных раздела 4, (" + ScriptUtils.calcTimeMillis(time))
        ScriptUtils.checkInterrupted()
        time = System.currentTimeMillis();

        Collections.sort(ndflPersonList, new Comparator<NdflPerson>() {
            @Override
            int compare(NdflPerson o1, NdflPerson o2) {
                int lastNameComp = compareValues(o1.lastName, o2.lastName, DeclarationStringComparator.INSTANCE);
                if (lastNameComp != 0) {
                    return lastNameComp
                }

                int firstNameComp = compareValues(o1.firstName, o2.firstName, DeclarationStringComparator.INSTANCE);
                if (firstNameComp != 0) {
                    return firstNameComp;
                }

                int middleNameComp = compareValues(o1.middleName, o2.middleName, DeclarationStringComparator.INSTANCE);
                if (middleNameComp != 0) {
                    return middleNameComp
                }

                int innComp = compareValues(o1.innNp, o2.innNp, DeclarationStringComparator.INSTANCE);
                if (innComp != 0) {
                    return innComp
                }

                int innForeignComp = compareValues(o1.innForeign, o2.innForeign, DeclarationStringComparator.INSTANCE);
                if (innForeignComp != 0) {
                    return innForeignComp
                }

                int birthDayComp = compareValues(o1.birthDay, o2.birthDay, null)
                if (birthDayComp != 0) {
                    return birthDayComp
                }

                return compareValues(o1.idDocNumber, o2.idDocNumber, DeclarationStringComparator.INSTANCE)
            }
        })

        logForDebug("Сортировка данных раздела 1, (" + ScriptUtils.calcTimeMillis(time))
        ScriptUtils.checkInterrupted()
        time = System.currentTimeMillis();

        List<NdflPerson> refBookPersonList = ndflPersonService.fetchRefBookPersonsAsNdflPerson(ndflPersonList.personId, new Date())

        logForDebug("Получение данных справочника ФЛ, (" + ScriptUtils.calcTimeMillis(time))
        ScriptUtils.checkInterrupted()
        time = System.currentTimeMillis();

        Map<Long, NdflPerson> refBookPersonsGroupedById = [:]

        for (NdflPerson refBookPerson : refBookPersonList) {
            refBookPersonsGroupedById.put(refBookPerson.personId, refBookPerson)
        }

        Map<Long, NdflPerson> ndflPersonsToPersist = [:]

        List<NdflPerson> withoutDulPersonList = []


        // Данные для заполнения раздела 1
        for (NdflPerson declarationDataPerson : ndflPersonList) {
            NdflPerson refBookPerson = refBookPersonsGroupedById.get(declarationDataPerson.personId)

            if (refBookPerson == null) {
                logger.error("В Разделе 1 первичной формы ${declarationDataPerson.declarationDataId} для ФЛ:" +
                        "${declarationDataPerson.lastName + " " + declarationDataPerson.firstName + " " + (declarationDataPerson.middleName ?: "")}" +
                        ", ИНП: ${declarationDataPerson.inp}, строка: ${declarationDataPerson.rowNum} не установлена " +
                        "ссылка на запись справочника \"Физические лица\". Выполните операцию идентификации формы " +
                        "${declarationDataPerson.declarationDataId}")
                continue
            }

            if (refBookPerson.idDocType == null) {
                withoutDulPersonList << refBookPerson
            }

            NdflPerson persistingPerson = ndflPersonsToPersist.get(0)
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

            ndflPersonsToPersist.put(declarationDataPerson.personId, declarationDataPerson)
        }

        if (logger.containsLevel(LogLevel.ERROR)) return

        /**
         * Получение данных из справочника физлиц сделан одним запросом. Мы не загружаем связанные со справочником ФЛ справочники ИНП, ДУЛ, и Адресов отдельными запросами.
         * Побочный эффект этого то что ДУЛ будет = null, в случае когда нет в справочнике ДУЛ с признаком включения в отчетность =1 и когда вообще нет ДУЛ у физлицаю
         * Поэтому здесь проверяется причина почему ДУЛ = null и выводится предупреждение
         */

        if (!withoutDulPersonList.isEmpty()) {
            for (NdflPerson person : withoutDulPersonList) {
                RefBookDataProvider provider = getProvider(RefBook.Id.ID_DOC.id)
                int count = provider.getRecordsCount(new Date(), "PERSON_ID = ${person.personId}")
                if (count == 0) {
                    logger.warn("Физическое лицо: %s, идентификатор ФЛ: %s, включено в форму без указания ДУЛ, отсутствуют данные в справочнике 'Документы, удостоверяющие личность",
                            "${person.lastName + " " + person.firstName + " " + (person.middleName ?: "")}",
                            person.inp)
                } else {
                    logger.warn("Физическое лицо: %s, идентификатор ФЛ: %s, включено в форму без указания ДУЛ, отсутствуют данные в справочнике 'Документы, удостоверяющие личность'  с признаком включения в отчетность: 1",
                            "${person.lastName + " " + person.firstName + " " + (person.middleName ?: "")}",
                            person.inp)
                }
            }
        }

        logForDebug("Консолидация данных, (" + ScriptUtils.calcTimeMillis(time))
        ScriptUtils.checkInterrupted()
        time = System.currentTimeMillis();

        Map<Long, List<NdflPersonDeduction>> deductionsGroupedByPerson = [:]
        Map<Long, List<NdflPersonPrepayment>> prepaymentsGroupedByPerson = [:]

        // Сортируем разделы 2, 3, 4
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

        long personRowNum = 0L
        BigDecimal incomeRowNum = new BigDecimal("0")
        BigDecimal deductionRowNum = new BigDecimal("0")
        BigDecimal prepaymentRowNum = new BigDecimal("0")

        for (NdflPerson ndflPerson : ndflPersonList) {
            ndflPerson.incomes = incomesGroupedByPerson.get(ndflPerson.id)
            ndflPerson.deductions = deductionsGroupedByPerson.get(ndflPerson.id)
            ndflPerson.prepayments = prepaymentsGroupedByPerson.get(ndflPerson.id)

            Collections.sort(ndflPerson.incomes, new Comparator<NdflPersonIncome>() {
                int compare(NdflPersonIncome o1, NdflPersonIncome o2) {
                    int operationDateComp = compareValues(operationDates.get(new Pair(o1.operationId, ndflPerson.inp)), operationDates.get(new Pair(o2.operationId, ndflPerson.inp)), null)
                    if (operationDateComp != 0) {
                        return operationDateComp
                    }

                    int operationIdComp = compareValues(o1.operationId, o2.operationId, DeclarationStringComparator.INSTANCE)
                    if (operationIdComp != 0) {
                        return operationIdComp
                    }

                    int actionDateComp = compareValues(getActionDate(o1), getActionDate(o2), null)
                    if (actionDateComp != 0) {
                        return actionDateComp
                    }

                    return compareValues(getRowType(o1), getRowType(o2), null)
                }

            })

            List<String> operationIdOrderList = ndflPerson.incomes.operationId

            Collections.sort(ndflPerson.deductions, new Comparator<NdflPersonDeduction>() {
                @Override
                int compare(NdflPersonDeduction o1, NdflPersonDeduction o2) {
                    int incomeAccruedComp = compareValues(o1.incomeAccrued, o2.incomeAccrued, null)
                    if (incomeAccruedComp != 0) {
                        return incomeAccruedComp
                    }

                    int operationIdComp = compareValues(o1.operationId, o2.operationId, new Comparator<String>() {
                        @Override
                        int compare(String s1, String s2) {
                            return operationIdOrderList.indexOf(s1) - operationIdOrderList.indexOf(s2)
                        }
                    })
                    if (operationIdComp != 0) {
                        return operationIdComp
                    }

                    return compareValues(o1.periodCurrDate, o2.periodCurrDate, null)
                }
            })

            Collections.sort(ndflPerson.prepayments, new Comparator<NdflPersonPrepayment>() {
                @Override
                int compare(NdflPersonPrepayment o1, NdflPersonPrepayment o2) {
                    return compareValues(o1.operationId, o2.operationId, new Comparator<String>() {
                        @Override
                        int compare(String s1, String s2) {
                            return operationIdOrderList.indexOf(s1) - operationIdOrderList.indexOf(s2)
                        }
                    })
                }
            })

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
            ndflPerson.id = null
            ndflPerson.declarationDataId = declarationData.id
            ndflPerson.modifiedDate = null
            ndflPerson.modifiedBy = null
        }

        logForDebug("Сортировка разделов 2,3,4 и присвоение № пп, (" + ScriptUtils.calcTimeMillis(time))
        ScriptUtils.checkInterrupted()
        time = System.currentTimeMillis();

        ndflPersonService.save(ndflPersonsToPersist.values());

        logForDebug("Сохранение данных в БД, (" + ScriptUtils.calcTimeMillis(time))
        ScriptUtils.checkInterrupted()

        // Формируем уведомления

        // Тербанки в состоянии "Принята"
        Map<Department, Integer> acceptedTbDepartments = [:]
        // Источники сгруппированные по тербанкам
        Map<Department, List<Long>> acceptedSourcesByTb = [:]

        acceptedTbDepartments.put(parentTB, 0)

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
            acceptedSourcesByTb.get(department).join(", "))
        }

        if(!(acceptedSources.get(Boolean.FALSE).isEmpty())) {
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
                        notAcceptedSourcesByTb.get(department).join(", "))
            }
        }

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
     * Получить дату начала отчетного периода
     * @return
     */
    Date getReportPeriodStartDate() {
        if (periodStartDate == null) {
            periodStartDate = reportPeriodService.getStartDate(declarationData.reportPeriodId)?.time
        }
        return periodStartDate
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
     * Создание фиктивной xml для привязки к экземпляру
     * declarationDataId
     * @return
     */
    def generateXml() {
        MarkupBuilder builder = new MarkupBuilder((FileWriter) xml)
        createXmlNode(builder, "Файл", ["имя": declarationData.id])
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    def createXmlNode(builder, name, attributes) {
        builder.name(attributes)
    }

/**
 * Получить список всех идентификаторов "АСНУ" из справочника
 * @return  список идентификаторов АСНУ
 */
    List<Long> getRefAsnuList() {
        if (asnuCache.isEmpty()) {
            RefBookDataProvider provider = getProvider(RefBook.Id.ASNU.id)
            PagingResult<Map<String, RefBookValue>> refBookMap = provider.getRecordsVersion(getReportPeriodStartDate(), getReportPeriodEndDate(), null, null)
            refBookMap.each { Map<String, RefBookValue> refBook ->
                Long asnuId = (Long) refBook?.id?.numberValue
                asnuCache.add(asnuId)
            }
        }
        return asnuCache;
    }

    /**
     * Получить дату создания налоговой формы
     * @param declarationDataId идентификатор налоговой формы
     * @return  дата создания налоговой формы
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
     * @param v1            значение 1
     * @param v2            значение 2
     * @param comparator    компаратор
     * @return отрицательное значение если параметр v1 меньше параметра v2, положительное значение если параметр v1
     * больше параметра v2, 0 если параметры v1 и v2 равны
     */
    @TypeChecked(TypeCheckingMode.SKIP)
    int compareValues(def v1, def v2, def comparator) {
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

    /**
     * Получить дату действия
     * @param income объект строки дохода
     * @return  вычисленная дата действия
     */
    Date getActionDate(NdflPersonIncome income) {
        Date toReturn = null
        if (income.taxDate != null) {
            toReturn = income.taxDate
        } else {
            toReturn = income.paymentDate
        }
        return toReturn
    }

    /**
     * Получить тип строки дохода
     * @param income объект строки дохода
     * @return  значение типа
     */
    Integer getRowType(NdflPersonIncome income) {
        if (income.incomeAccruedDate != null) {
            return 100
        } else if (income.incomePayoutDate != null) {
            return 200
        }
        return 300

    }

}
