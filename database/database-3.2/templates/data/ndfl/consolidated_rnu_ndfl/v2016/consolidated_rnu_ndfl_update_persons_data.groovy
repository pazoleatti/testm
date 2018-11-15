package form_template.ndfl.consolidated_rnu_ndfl.v2016

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.DeclarationData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import com.aplana.sbrf.taxaccounting.script.service.PersonService
import com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils
import com.aplana.sbrf.taxaccounting.model.DeclarationData
import com.aplana.sbrf.taxaccounting.model.Department
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogEntry
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory
import com.aplana.sbrf.taxaccounting.script.SharedConstants
import com.aplana.sbrf.taxaccounting.script.service.DepartmentReportPeriodService
import com.aplana.sbrf.taxaccounting.script.service.DepartmentService
import com.aplana.sbrf.taxaccounting.script.service.NdflPersonService
import com.aplana.sbrf.taxaccounting.script.service.ReportPeriodService
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode

new UpdatePersonsData(this).run()

@TypeChecked
class UpdatePersonsData extends AbstractScriptClass {

    DeclarationData declarationData
    NdflPersonService ndflPersonService
    DepartmentReportPeriodService departmentReportPeriodService
    DepartmentService departmentService
    RefBookFactory refBookFactory
    ReportPeriodService reportPeriodService

    // Кэш провайдеров
    Map<Long, RefBookDataProvider> providerCache = [:]

    @TypeChecked(TypeCheckingMode.SKIP)
    UpdatePersonsData(scriptClass) {
        //noinspection GroovyAssignabilityCheck
        super(scriptClass)
        if (scriptClass.getBinding().hasVariable("declarationData")) {
            this.declarationData = (DeclarationData) scriptClass.getProperty("declarationData")
        }
        if (scriptClass.getBinding().hasVariable("ndflPersonService")) {
            this.ndflPersonService = (NdflPersonService) scriptClass.getProperty("ndflPersonService")
        }
        if (scriptClass.getBinding().hasVariable("departmentReportPeriodService")) {
            this.departmentReportPeriodService = (DepartmentReportPeriodService) scriptClass.getProperty("departmentReportPeriodService")
        }
        if (scriptClass.getBinding().hasVariable("departmentService")) {
            this.departmentService = (DepartmentService) scriptClass.getProperty("departmentService")
        }
        if (scriptClass.getBinding().hasVariable("refBookFactory")) {
            this.refBookFactory = (RefBookFactory) scriptClass.getProperty("refBookFactory")
        }
        if (scriptClass.getBinding().hasVariable("reportPeriodService")) {
            this.reportPeriodService = (ReportPeriodService) scriptClass.getProperty("reportPeriodService")
        }
    }

    @Override
    void run() {
        initConfiguration()
        switch (formDataEvent) {
            case FormDataEvent.UPDATE_PERSONS_DATA:
                try {
                    doUpdate()
                } catch (Throwable e) {
                    e.printStackTrace()
                    DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
                    String strCorrPeriod = ""
                    if (departmentReportPeriod.getCorrectionDate() != null) {
                        strCorrPeriod = ", с датой сдачи корректировки " + departmentReportPeriod.getCorrectionDate().format(SharedConstants.DATE_FORMAT)
                    }
                    Department department = departmentService.get(departmentReportPeriod.departmentId)
                    logger.error("Невозможно обновить форму: № %s, Период %s, Подразделение %s, Вид \"Консолидированная\". Причина: \"%s\", попробуйте повторить операцию позднее\"",
                            declarationData.id,
                            departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear() + ", " + departmentReportPeriod.getReportPeriod().getName() + strCorrPeriod,
                            department.getName(),
                            e)
                }
        }
    }

    void doUpdate() {
        List<NdflPerson> declarationDataPersonList = ndflPersonService.findNdflPersonWithOperations(declarationData.id)
        List<NdflPerson> refBookPersonList = ndflPersonService.fetchRefBookPersonsAsNdflPerson(declarationData.id, new Date())
        List<NdflPerson> toUpdatePersons = []
        List<LogEntry> logs = []
        Map<Long, NdflPerson> personIdAssociatedRefBookPersons = [:]
        for (NdflPerson ndflPerson : refBookPersonList) {
            personIdAssociatedRefBookPersons.put(ndflPerson.personId, ndflPerson)
        }
        for (NdflPerson declarationDataPerson : declarationDataPersonList) {
            NdflPerson refBookPerson = personIdAssociatedRefBookPersons.get(declarationDataPerson.personId)
            if (refBookPerson == null) {
                logger.warn("Невозможно обновить запись: ${createPersonInfo(declarationDataPerson)}. Причина: \"Для связанного физического лица в Реестре физических лиц отсутствует актуальная запись о физическом лице\"")
                continue
            }
            String personInfo
            List<String> updateInfo = []
            boolean updated = false
            if (refBookPerson.inp != declarationDataPerson.inp) {
                if (refBookPerson.inp == null) {
                    logger.warn(createAbsentValueMessage(declarationDataPerson, SharedConstants.INP_FULL, SharedConstants.REF_PERSON_REC_ID))
                } else {
                    updateInfo << createUpdateInfo(SharedConstants.INP_FULL, declarationDataPerson.inp ?: "", refBookPerson.inp ?: "")
                    declarationDataPerson.inp = refBookPerson.inp
                    updated = true
                }
            }
            if (refBookPerson.lastName != declarationDataPerson.lastName) {
                if (refBookPerson.lastName == null) {
                    logger.warn(createAbsentValueMessage(declarationDataPerson, SharedConstants.LAST_NAME_FULL, SharedConstants.REF_PERSON_LAST_NAME))
                } else {
                    updateInfo << createUpdateInfo(SharedConstants.LAST_NAME_FULL, declarationDataPerson.lastName ?: "", refBookPerson.lastName ?: "")
                    declarationDataPerson.lastName = refBookPerson.lastName
                    updated = true
                }
            }
            if (refBookPerson.firstName != declarationDataPerson.firstName) {
                if (refBookPerson.firstName == null) {
                    logger.warn(createAbsentValueMessage(declarationDataPerson, SharedConstants.FIRST_NAME_FULL, SharedConstants.REF_PERSON_FIRST_NAME))
                } else {
                    updateInfo << createUpdateInfo(SharedConstants.FIRST_NAME_FULL, declarationDataPerson.firstName ?: "", refBookPerson.firstName ?: "")
                    declarationDataPerson.firstName = refBookPerson.firstName
                    updated = true
                }
            }
            if (refBookPerson.middleName != declarationDataPerson.middleName) {
                if (refBookPerson.middleName == null) {
                    logger.warn(createAbsentValueMessage(declarationDataPerson, SharedConstants.MIDDLE_NAME_FULL, SharedConstants.REF_PERSON_MIDDLE_NAME))
                } else {
                    updateInfo << createUpdateInfo(SharedConstants.MIDDLE_NAME_FULL, declarationDataPerson.middleName ?: "", refBookPerson.middleName ?: "")
                    declarationDataPerson.middleName = refBookPerson.middleName
                    updated = true
                }
            }
            if (refBookPerson.birthDay != declarationDataPerson.birthDay) {
                if (refBookPerson.birthDay == null) {
                    logger.warn(createAbsentValueMessage(declarationDataPerson, SharedConstants.BIRTH_DAY_FULL, SharedConstants.REF_PERSON_BIRTH_DAY))
                } else {
                    updateInfo << createUpdateInfo(SharedConstants.BIRTH_DAY_FULL, declarationDataPerson.birthDay ? declarationDataPerson.birthDay.format(SharedConstants.DATE_FORMAT) : "", refBookPerson.birthDay ? refBookPerson.birthDay.format(SharedConstants.DATE_FORMAT) : "")
                    declarationDataPerson.birthDay = refBookPerson.birthDay
                    updated = true
                }
            }
            if (refBookPerson.citizenship != declarationDataPerson.citizenship) {
                if (refBookPerson.citizenship == null) {
                    logger.warn(createAbsentValueMessage(declarationDataPerson, SharedConstants.CITIZENSHIP_FULL, SharedConstants.REF_PERSON_CITIZENSHIP))
                } else {
                    updateInfo << createUpdateInfo(SharedConstants.CITIZENSHIP_FULL, declarationDataPerson.citizenship ?: "", refBookPerson.citizenship ?: "")
                    declarationDataPerson.citizenship = refBookPerson.citizenship
                    updated = true
                }
            }
            if (refBookPerson.innNp != declarationDataPerson.innNp) {
                if (refBookPerson.innNp == null) {
                    logger.warn(createAbsentValueMessage(declarationDataPerson, SharedConstants.INN_FULL, SharedConstants.REF_PERSON_INN))
                } else {
                    updateInfo << createUpdateInfo(SharedConstants.INN_FULL, declarationDataPerson.innNp ?: "", refBookPerson.innNp ?: "")
                    declarationDataPerson.innNp = refBookPerson.innNp
                    updated = true
                }

            }
            if (refBookPerson.innForeign != declarationDataPerson.innForeign) {
                if (refBookPerson.innForeign == null) {
                    logger.warn(createAbsentValueMessage(declarationDataPerson, SharedConstants.INN_FOREIGN_FULL, SharedConstants.REF_PERSON_INN_FOREIGN))
                } else {
                    updateInfo << createUpdateInfo(SharedConstants.INN_FOREIGN_FULL, declarationDataPerson.innForeign ?: "", refBookPerson.innForeign ?: "")
                    declarationDataPerson.innForeign = refBookPerson.innForeign
                    updated = true
                }

            }
            if (refBookPerson.idDocType != declarationDataPerson.idDocType) {
                if (refBookPerson.idDocType == null) {
                    logger.warn(createAbsentValueMessage(declarationDataPerson, SharedConstants.ID_DOC_TYPE_FULL, SharedConstants.REF_ID_DOC_TYPE))
                } else {
                    updateInfo << createUpdateInfo(SharedConstants.ID_DOC_TYPE_FULL, declarationDataPerson.idDocType ?: "", refBookPerson.idDocType ?: "")
                    declarationDataPerson.idDocType = refBookPerson.idDocType
                    updated = true
                }
            }
            if (refBookPerson.idDocNumber != declarationDataPerson.idDocNumber) {
                if (refBookPerson.idDocNumber == null) {
                    logger.warn(createAbsentValueMessage(declarationDataPerson, SharedConstants.ID_DOC_NUMBER_FULL, SharedConstants.REF_ID_DOC_NUMBER))
                } else {
                    updateInfo << createUpdateInfo(SharedConstants.ID_DOC_NUMBER_FULL, declarationDataPerson.idDocNumber ?: "", refBookPerson.idDocNumber ?: "")
                    declarationDataPerson.idDocNumber = refBookPerson.idDocNumber
                    updated = true
                }
            }
            if (refBookPerson.status != declarationDataPerson.status) {
                if (refBookPerson.status == null) {
                    logger.warn(createAbsentValueMessage(declarationDataPerson, SharedConstants.STATUS_FULL, SharedConstants.REF_PERSON_STATUS))
                } else {
                    updateInfo << createUpdateInfo(SharedConstants.STATUS_FULL, declarationDataPerson.status ?: "", refBookPerson.status ?: "")
                    declarationDataPerson.status = refBookPerson.status
                    updated = true
                }
            }
            if (refBookPerson.regionCode != declarationDataPerson.regionCode) {
                if (refBookPerson.regionCode == null) {
                    logger.warn(createAbsentValueMessage(declarationDataPerson, SharedConstants.REGION_CODE_FULL, SharedConstants.ADDRESS_REGION_CODE))
                } else {
                    updateInfo << createUpdateInfo(SharedConstants.REGION_CODE_FULL, declarationDataPerson.regionCode ?: "", refBookPerson.regionCode ?: "")
                    declarationDataPerson.regionCode = refBookPerson.regionCode
                    updated = true
                }
            }
            if (refBookPerson.postIndex != declarationDataPerson.postIndex) {
                if (refBookPerson.postIndex == null) {
                    logger.warn(createAbsentValueMessage(declarationDataPerson, SharedConstants.POST_INDEX_FULL, SharedConstants.ADDRESS_POST_INDEX))
                } else {
                    updateInfo << createUpdateInfo(SharedConstants.POST_INDEX_FULL, declarationDataPerson.postIndex ?: "", refBookPerson.postIndex ?: "")
                    declarationDataPerson.postIndex = refBookPerson.postIndex
                    updated = true
                }
            }
            if (refBookPerson.area != declarationDataPerson.area) {
                if (refBookPerson.area == null) {
                    logger.warn(createAbsentValueMessage(declarationDataPerson, SharedConstants.AREA_FULL, SharedConstants.ADDRESS_AREA))
                } else {
                    updateInfo << createUpdateInfo(SharedConstants.AREA_FULL, declarationDataPerson.area ?: "", refBookPerson.area ?: "")
                    declarationDataPerson.area = refBookPerson.area
                    updated = true
                }
            }
            if (refBookPerson.city != declarationDataPerson.city) {
                if (refBookPerson.city == null) {
                    logger.warn(createAbsentValueMessage(declarationDataPerson, SharedConstants.CITY_FULL, SharedConstants.ADDRESS_CITY))
                } else {
                    updateInfo << createUpdateInfo(SharedConstants.CITY_FULL, declarationDataPerson.city ?: "", refBookPerson.city ?: "")
                    declarationDataPerson.city = refBookPerson.city
                    updated = true
                }
            }
            if (refBookPerson.locality != declarationDataPerson.locality) {
                if (refBookPerson.locality == null) {
                    logger.warn(createAbsentValueMessage(declarationDataPerson, SharedConstants.LOCALITY_FULL, SharedConstants.ADDRESS_LOCALITY))
                } else {
                    updateInfo << createUpdateInfo(SharedConstants.LOCALITY_FULL, declarationDataPerson.locality ?: "", refBookPerson.locality ?: "")
                    declarationDataPerson.locality = refBookPerson.locality
                    updated = true
                }
            }
            if (refBookPerson.street != declarationDataPerson.street) {
                if (refBookPerson.street == null) {
                    logger.warn(createAbsentValueMessage(declarationDataPerson, SharedConstants.STREET_FULL, SharedConstants.ADDRESS_STREET))
                } else {
                    updateInfo << createUpdateInfo(SharedConstants.STREET_FULL, declarationDataPerson.street ?: "", refBookPerson.street ?: "")
                    declarationDataPerson.street = refBookPerson.street
                    updated = true
                }
            }
            if (refBookPerson.house != declarationDataPerson.house) {
                if (refBookPerson.house == null) {
                    logger.warn(createAbsentValueMessage(declarationDataPerson, SharedConstants.HOUSE_FULL, SharedConstants.ADDRESS_HOUSE))
                } else {
                    updateInfo << createUpdateInfo(SharedConstants.HOUSE_FULL, declarationDataPerson.house ?: "", refBookPerson.house ?: "")
                    declarationDataPerson.house = refBookPerson.house
                    updated = true
                }
            }
            if (refBookPerson.building != declarationDataPerson.building) {
                if (refBookPerson.building == null) {
                    logger.warn(createAbsentValueMessage(declarationDataPerson, SharedConstants.BUILDING_FULL, SharedConstants.ADDRESS_BUILDING))
                } else {
                    updateInfo << createUpdateInfo(SharedConstants.BUILDING_FULL, declarationDataPerson.building ?: "", refBookPerson.building ?: "")
                    declarationDataPerson.building = refBookPerson.building
                    updated = true
                }
            }
            if (refBookPerson.flat != declarationDataPerson.flat) {
                if (refBookPerson.flat == null) {
                    logger.warn(createAbsentValueMessage(declarationDataPerson, SharedConstants.FLAT_FULL, SharedConstants.ADDRESS_FLAT))
                } else {
                    updateInfo << createUpdateInfo(SharedConstants.FLAT_FULL, declarationDataPerson.flat ?: "", refBookPerson.flat ?: "")
                    declarationDataPerson.flat = refBookPerson.flat
                    updated = true
                }
            }
            if (refBookPerson.snils != declarationDataPerson.snils) {
                if (refBookPerson.snils == null) {
                    logger.warn(createAbsentValueMessage(declarationDataPerson, SharedConstants.SNILS_FULL, SharedConstants.REF_PERSON_SNILS))
                } else {
                    updateInfo << createUpdateInfo(SharedConstants.SNILS_FULL, declarationDataPerson.snils ?: "", refBookPerson.snils ?: "")
                    declarationDataPerson.snils = refBookPerson.snils
                    updated = true
                }
            }
            if (refBookPerson.countryCode != declarationDataPerson.countryCode) {
                if (refBookPerson.countryCode == null) {
                    logger.warn(createAbsentValueMessage(declarationDataPerson, SharedConstants.COUNTRY_CODE_FULL, SharedConstants.ADDRESS_COUNTRY_CODE))
                } else {
                    updateInfo << createUpdateInfo(SharedConstants.COUNTRY_CODE_FULL, declarationDataPerson.countryCode ?: "", refBookPerson.countryCode ?: "")
                    declarationDataPerson.countryCode = refBookPerson.countryCode
                    updated = true
                }
            }
            if (refBookPerson.address != declarationDataPerson.address) {
                if (refBookPerson.address == null) {
                    logger.warn(createAbsentValueMessage(declarationDataPerson, SharedConstants.ADDRESS_FULL, SharedConstants.ADDRESS_ADDRESS))
                } else {
                    updateInfo << createUpdateInfo(SharedConstants.ADDRESS_FULL, declarationDataPerson.address ?: "", refBookPerson.address ?: "")
                    declarationDataPerson.address = refBookPerson.address
                    updated = true
                }
            }
            if (updated) {
                personInfo = createPersonInfo(declarationDataPerson)
                logs << createLogMessage(personInfo, updateInfo)
                declarationDataPerson.modifiedDate = new Date()
                declarationDataPerson.modifiedBy = "${userInfo.getUser().getName()} (${userInfo.getUser().getLogin()})"
                toUpdatePersons << declarationDataPerson
            }
        }
        if (!toUpdatePersons.isEmpty()) {
            ndflPersonService.updateNdflPersons(toUpdatePersons)
            logger.getEntries().addAll(logs)
        }

        long time = System.currentTimeMillis()
        // Сортировка всех разделов формы
        Collections.sort(declarationDataPersonList, NdflPerson.getComparator())

        Long personRowNum = 0L
        BigDecimal incomeRowNum = new BigDecimal("0")
        BigDecimal deductionRowNum = new BigDecimal("0")
        BigDecimal prepaymentRowNum = new BigDecimal("0")
        for (NdflPerson ndflPerson : declarationDataPersonList) {
            Collections.sort(ndflPerson.incomes, NdflPersonIncome.getComparator(ndflPerson))

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
        logForDebug("Сортировка данных всех разделов, (" + ScriptUtils.calcTimeMillis(time))
        time = System.currentTimeMillis()

        ndflPersonService.updateRowNum(declarationDataPersonList)
        logForDebug("Сохранение данных в БД, (" + ScriptUtils.calcTimeMillis(time))

        Department department = departmentService.get(declarationData.departmentId)
        DepartmentReportPeriod reportPeriod = departmentReportPeriodService.get(declarationData.getDepartmentReportPeriodId())
        logger.info("Завершено обновление данных ФЛ формы: № %s, Период %s, Подразделение %s, Вид \"Консолидированная\". Обновлено %s записей.",
                declarationData.id,
                departmentReportPeriodService.formatPeriodName(reportPeriod, SharedConstants.DATE_FORMAT),
                department.getName(),
                toUpdatePersons.size())
    }

    /**
     * Создает сообщение об обновлении данных у физдица
     * @param personInfo    информация о Физлице
     * @param updateInfo    информация об обновленных полях
     * @return  сообщение для {@code com.aplana.sbrf.taxaccounting.model.log.Logger}
     */
    LogEntry createLogMessage(String personInfo, List<String> updateInfo) {
        return new LogEntry(LogLevel.INFO, String.format("Обновлены данные у ФЛ: %s. Обновлены поля: %s",
                personInfo,
                updateInfo.join(", ")))
    }

    /**
     * Создает информацию о физлице
     * @param person    объект физлица
     * @return  информация о Физлице
     */
    String createPersonInfo(NdflPerson person) {
        String fio = (person.lastName ?: "") + " " + (person.firstName ?: "") + " " + (person.middleName ?: "")
        String dul = (person.idDocType ?: "") + ", " + (person.idDocNumber ?: "")
        return String.format("(%s, ИНП: %s, ДУЛ: %s)",
                fio,
                person.inp,
                dul)
    }

    /**
     * Создает информацию об обновленных полях
     * @param fieldName название поля
     * @param oldValue  старое значение
     * @param newValue  новое значение
     * @return  информацию об обновленных полях
     */
    String createUpdateInfo(String fieldName, Object oldValue, Object newValue) {
        return String.format("\"%s\". Старое значение: \"%s\". Новое значение \"%s\"",
                fieldName,
                oldValue,
                newValue)
    }

    /**
     * Создает сообщение об осутствии значения поля в справочнике ФЛ для имеющегося значения в РазделеРеквизиты
     * @param person            физическое лицо
     * @param fieldName         название поля в РНУ
     * @param refBookFieldName  название поля в справочнике
     * @return  сообщение об осутствии значения поля в справочнике ФЛ
     */
    static String createAbsentValueMessage(NdflPerson person, String fieldName, String refBookFieldName) {
        return "Физическое лицо: ${(person.lastName ?: "") + " " + (person.firstName ?: "") + " " + (person.middleName ?: "")}, идентификатор ФЛ: ${person.inp}, включено в форму без указания $fieldName, отсутствуют данные в Реестре физических лиц $refBookFieldName."
    }
    /**
     * Получение провайдера с использованием кеширования.
     * @param providerId
     * @return
     */
    RefBookDataProvider getProvider(Long providerId) {
        if (!providerCache.containsKey(providerId)) {
            RefBookDataProvider provider = refBookFactory.getDataProvider(providerId)
            providerCache.put(providerId, provider)
        }
        return providerCache.get(providerId)
    }

}