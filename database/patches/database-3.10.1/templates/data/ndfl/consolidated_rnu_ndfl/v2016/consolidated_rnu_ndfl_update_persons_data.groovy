package form_template.ndfl.consolidated_rnu_ndfl.v2016

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.*
import com.aplana.sbrf.taxaccounting.model.log.LogEntry
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory
import com.aplana.sbrf.taxaccounting.script.SharedConstants
import com.aplana.sbrf.taxaccounting.script.service.DepartmentReportPeriodService
import com.aplana.sbrf.taxaccounting.script.service.DepartmentService
import com.aplana.sbrf.taxaccounting.script.service.NdflPersonService
import com.aplana.sbrf.taxaccounting.script.service.ReportPeriodService
import com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode

new UpdatePersonsData(this).run()

@TypeChecked
class UpdatePersonsData extends AbstractScriptClass {

    DeclarationData declarationData
    DeclarationTemplate declarationTemplate
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
            this.declarationTemplate = declarationService.getTemplate(declarationData.declarationTemplateId)
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
                    String fullFormDesc = declarationService.getFullDeclarationDescription(declarationData.getId())
                    String msg = "Невозможно обновить форму: $fullFormDesc"
                    logger.error(msg + ". Причина: \"%s\", попробуйте повторить операцию позднее\"", e)
                }
        }
    }

    void doUpdate() {
        List<NdflPerson> declarationDataPersonList = ndflPersonService.findNdflPersonWithOperations(declarationData.id)
        List<NdflPerson> refBookPersonList = ndflPersonService.fetchRefBookPersonsAsNdflPerson(declarationData.id, new Date())
        List<NdflPerson> toUpdatePersons = []
        List<LogEntry> logs = []
        Map<Long, NdflPerson> personIdAssociatedRefBookPersons = [:]
        boolean existWarning = false
        for (NdflPerson ndflPerson : refBookPersonList) {
            personIdAssociatedRefBookPersons.put(ndflPerson.personId, ndflPerson)
        }
        for (NdflPerson declarationDataPerson : declarationDataPersonList) {
            NdflPerson refBookPerson = personIdAssociatedRefBookPersons.get(declarationDataPerson.personId)
            if (refBookPerson == null) {
                existWarning = true
                logger.warn("Невозможно обновить запись: ${createPersonInfo(declarationDataPerson)}. Причина: \"Для связанного физического лица в Реестре физических лиц отсутствует актуальная запись о физическом лице\"")
                continue
            }
            String personInfo = createPersonInfo(declarationDataPerson)
            List<String> updateInfo = []
            boolean updated = false
            if (refBookPerson.inp != declarationDataPerson.inp && declarationTemplate.declarationFormKind == DeclarationFormKind.CONSOLIDATED) {
                updateInfo << createUpdateInfo(SharedConstants.INP_FULL, declarationDataPerson.inp ?: "_", refBookPerson.inp ?: "_")
                declarationDataPerson.inp = refBookPerson.inp
                updated = true
            }
            if (refBookPerson.lastName != declarationDataPerson.lastName) {
                if (refBookPerson.lastName == null || !refBookPerson.lastName.equalsIgnoreCase(declarationDataPerson.lastName)) {
                    updateInfo << createUpdateInfo(SharedConstants.LAST_NAME_FULL, declarationDataPerson.lastName ?: "_", refBookPerson.lastName ?: "_")
                    declarationDataPerson.lastName = refBookPerson.lastName
                    updated = true
                }
            }
            if (refBookPerson.firstName != declarationDataPerson.firstName) {
                if (refBookPerson.firstName == null || !refBookPerson.firstName.equalsIgnoreCase(declarationDataPerson.firstName)) {
                    updateInfo << createUpdateInfo(SharedConstants.FIRST_NAME_FULL, declarationDataPerson.firstName ?: "_", refBookPerson.firstName ?: "_")
                    declarationDataPerson.firstName = refBookPerson.firstName
                    updated = true
                }
            }
            if (refBookPerson.middleName != declarationDataPerson.middleName) {
                if (refBookPerson.middleName == null || !refBookPerson.middleName.equalsIgnoreCase(declarationDataPerson.middleName)) {
                    updateInfo << createUpdateInfo(SharedConstants.MIDDLE_NAME_FULL, declarationDataPerson.middleName ?: "_", refBookPerson.middleName ?: "_")
                    declarationDataPerson.middleName = refBookPerson.middleName
                    updated = true
                }
            }
            if (refBookPerson.birthDay != declarationDataPerson.birthDay) {
                updateInfo << createUpdateInfo(SharedConstants.BIRTH_DAY_FULL, declarationDataPerson.birthDay ? declarationDataPerson.birthDay.format(SharedConstants.DATE_FORMAT) : "_", refBookPerson.birthDay ? refBookPerson.birthDay.format(SharedConstants.DATE_FORMAT) : "_")
                declarationDataPerson.birthDay = refBookPerson.birthDay
                updated = true
            }
            if (refBookPerson.citizenship != declarationDataPerson.citizenship) {
                updateInfo << createUpdateInfo(SharedConstants.CITIZENSHIP_FULL, declarationDataPerson.citizenship ?: "_", refBookPerson.citizenship ?: "_")
                declarationDataPerson.citizenship = refBookPerson.citizenship
                updated = true
            }
            if (refBookPerson.innNp != declarationDataPerson.innNp) {
                updateInfo << createUpdateInfo(SharedConstants.INN_FULL, declarationDataPerson.innNp ?: "_", refBookPerson.innNp ?: "_")
                declarationDataPerson.innNp = refBookPerson.innNp
                updated = true
            }
            if (refBookPerson.innForeign != declarationDataPerson.innForeign) {
                updateInfo << createUpdateInfo(SharedConstants.INN_FOREIGN_FULL, declarationDataPerson.innForeign ?: "_", refBookPerson.innForeign ?: "_")
                declarationDataPerson.innForeign = refBookPerson.innForeign
                updated = true
            }
            if (refBookPerson.idDocType != declarationDataPerson.idDocType) {
                updateInfo << createUpdateInfo(SharedConstants.ID_DOC_TYPE_FULL, declarationDataPerson.idDocType ?: "_", refBookPerson.idDocType ?: "_")
                declarationDataPerson.idDocType = refBookPerson.idDocType
                updated = true
            }
            if (refBookPerson.idDocNumber != declarationDataPerson.idDocNumber) {
                updateInfo << createUpdateInfo(SharedConstants.ID_DOC_NUMBER_FULL, declarationDataPerson.idDocNumber ?: "_", refBookPerson.idDocNumber ?: "_")
                declarationDataPerson.idDocNumber = refBookPerson.idDocNumber
                updated = true
            }
            if (refBookPerson.status != declarationDataPerson.status) {
                updateInfo << createUpdateInfo(SharedConstants.STATUS_FULL, declarationDataPerson.status ?: "_", refBookPerson.status ?: "_")
                declarationDataPerson.status = refBookPerson.status
                updated = true
            }
            if (refBookPerson.regionCode != declarationDataPerson.regionCode) {
                updateInfo << createUpdateInfo(SharedConstants.REGION_CODE_FULL, declarationDataPerson.regionCode ?: "_", refBookPerson.regionCode ?: "_")
                declarationDataPerson.regionCode = refBookPerson.regionCode
                updated = true
            }
            if (refBookPerson.postIndex != declarationDataPerson.postIndex) {
                updateInfo << createUpdateInfo(SharedConstants.POST_INDEX_FULL, declarationDataPerson.postIndex ?: "_", refBookPerson.postIndex ?: "_")
                declarationDataPerson.postIndex = refBookPerson.postIndex
                updated = true
            }
            if (refBookPerson.area != declarationDataPerson.area) {
                updateInfo << createUpdateInfo(SharedConstants.AREA_FULL, declarationDataPerson.area ?: "_", refBookPerson.area ?: "_")
                declarationDataPerson.area = refBookPerson.area
                updated = true
            }
            if (refBookPerson.city != declarationDataPerson.city) {
                updateInfo << createUpdateInfo(SharedConstants.CITY_FULL, declarationDataPerson.city ?: "_", refBookPerson.city ?: "_")
                declarationDataPerson.city = refBookPerson.city
                updated = true
            }
            if (refBookPerson.locality != declarationDataPerson.locality) {
                updateInfo << createUpdateInfo(SharedConstants.LOCALITY_FULL, declarationDataPerson.locality ?: "_", refBookPerson.locality ?: "_")
                declarationDataPerson.locality = refBookPerson.locality
                updated = true
            }
            if (refBookPerson.street != declarationDataPerson.street) {
                updateInfo << createUpdateInfo(SharedConstants.STREET_FULL, declarationDataPerson.street ?: "_", refBookPerson.street ?: "_")
                declarationDataPerson.street = refBookPerson.street
                updated = true
            }
            if (refBookPerson.house != declarationDataPerson.house) {
                updateInfo << createUpdateInfo(SharedConstants.HOUSE_FULL, declarationDataPerson.house ?: "_", refBookPerson.house ?: "_")
                declarationDataPerson.house = refBookPerson.house
                updated = true
            }
            if (refBookPerson.building != declarationDataPerson.building) {
                updateInfo << createUpdateInfo(SharedConstants.BUILDING_FULL, declarationDataPerson.building ?: "_", refBookPerson.building ?: "_")
                declarationDataPerson.building = refBookPerson.building
                updated = true
            }
            if (refBookPerson.flat != declarationDataPerson.flat) {
                updateInfo << createUpdateInfo(SharedConstants.FLAT_FULL, declarationDataPerson.flat ?: "_", refBookPerson.flat ?: "_")
                declarationDataPerson.flat = refBookPerson.flat
                updated = true
            }
            if (refBookPerson.snils != declarationDataPerson.snils) {
                updateInfo << createUpdateInfo(SharedConstants.SNILS_FULL, declarationDataPerson.snils ?: "_", refBookPerson.snils ?: "_")
                declarationDataPerson.snils = refBookPerson.snils
                updated = true
            }
            if (refBookPerson.countryCode != declarationDataPerson.countryCode) {
                updateInfo << createUpdateInfo(SharedConstants.COUNTRY_CODE_FULL, declarationDataPerson.countryCode ?: "_", refBookPerson.countryCode ?: "_")
                declarationDataPerson.countryCode = refBookPerson.countryCode
                updated = true
            }
            if (refBookPerson.address != declarationDataPerson.address) {
                updateInfo << createUpdateInfo(SharedConstants.ADDRESS_FULL, declarationDataPerson.address ?: "_", refBookPerson.address ?: "_")
                declarationDataPerson.address = refBookPerson.address
                updated = true
            }
            if (updated) {
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
        logForDebug("Сортировка данных всех разделов, (" + ScriptUtils.calcTimeMillis(time))
        time = System.currentTimeMillis()

        ndflPersonService.updateRowNum(declarationDataPersonList)
        logForDebug("Сохранение данных в БД, (" + ScriptUtils.calcTimeMillis(time))

        Department department = departmentService.get(declarationData.departmentId)
        DepartmentReportPeriod reportPeriod = departmentReportPeriodService.get(declarationData.getDepartmentReportPeriodId())

        String fullFormDesc = declarationService.getFullDeclarationDescription(declarationData.getId())
        String msg = "Завершено обновление данных ФЛ формы: $fullFormDesc"

        if(!existWarning) {
            logger.info(msg + ". Обновлено %s записей.", toUpdatePersons.size())
        } else {
            logger.warn(msg + ". Обновлено %s записей.", toUpdatePersons.size())
        }
    }

    /**
     * Создает сообщение об обновлении данных у физдица
     * @param personInfo информация о Физлице
     * @param updateInfo информация об обновленных полях
     * @return сообщение для {@code com.aplana.sbrf.taxaccounting.model.log.Logger}
     */
    LogEntry createLogMessage(String personInfo, List<String> updateInfo) {
        return new LogEntry(LogLevel.INFO, String.format("Обновлены данные физического лица: %s. Изменены значения параметров: %s",
                personInfo,
                updateInfo.join(", ")))
    }

    /**
     * Создает информацию о физлице
     * @param person объект физлица
     * @return информация о Физлице
     */
    String createPersonInfo(NdflPerson person) {
        String fio = (person.lastName ?: "") + " " + (person.firstName ?: "") + " " + (person.middleName ?: "")
        String dul = (person.idDocType ?: "") + ", " + (person.idDocNumber ?: "")
        return String.format("(%s, ИНП: %s, ДУЛ: %s)",
                fio,
                person.inp ? person.inp : "\"_\"",
                dul != ", " ? dul : "\"_\"")
    }

    /**
     * Создает информацию об обновленных полях
     * @param fieldName название поля
     * @param oldValue старое значение
     * @param newValue новое значение
     * @return информацию об обновленных полях
     */
    String createUpdateInfo(String fieldName, Object oldValue, Object newValue) {
        return String.format("\"%s\". Старое значение: \"%s\". Новое значение \"%s\"",
                fieldName,
                oldValue,
                newValue)
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