package form_template.ndfl.primary_rnu_ndfl.v2016

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils
import com.aplana.sbrf.taxaccounting.model.DeclarationFormKind
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment
import com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils
import com.aplana.sbrf.taxaccounting.model.DeclarationData
import com.aplana.sbrf.taxaccounting.model.Department
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogEntry
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory
import com.aplana.sbrf.taxaccounting.script.SharedConstants
import com.aplana.sbrf.taxaccounting.script.service.DepartmentReportPeriodService
import com.aplana.sbrf.taxaccounting.script.service.DepartmentService
import com.aplana.sbrf.taxaccounting.script.service.NdflPersonService
import com.aplana.sbrf.taxaccounting.script.service.ReportPeriodService
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

import java.sql.ResultSet
import java.sql.SQLException

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
    NamedParameterJdbcTemplate namedParameterJdbcTemplate

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
        this.namedParameterJdbcTemplate = (NamedParameterJdbcTemplate) scriptClass.getProperty("namedParameterJdbcTemplate")
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
                    logger.error("Невозможно обновить форму: № %s, Период %s, Подразделение %s, Вид: \"%s\". Причина: \"%s\", попробуйте повторить операцию позднее\"",
                            declarationData.id,
                            departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear() + ", " + departmentReportPeriod.getReportPeriod().getName() + strCorrPeriod,
                            department.getName(),
                            declarationTemplate.type.name,
                            e)
                }
        }
    }

    void doUpdate() {
        List<NdflPerson> declarationDataPersonList = ndflPersonService.findNdflPersonWithOperations(declarationData.id)
        String sql = "SELECT rbp.id, rbp.record_id AS inp, rbp.last_name, rbp.first_name, rbp.middle_name, rbp.birth_date, rbc.code AS citizenship, rbp.inn, rbp.inn_foreign, rbts.code AS status, \n" +
                "rbp.snils, rbdt.code AS id_doc_type, rbid.doc_number, rbp.region_code, rbp.postal_code, rbp.district, rbp.city, rbp.locality, rbp.street, rbp.house, rbp.build, rbp.appartment,\n" +
                "rbp.address_foreign as address, rbc2.code AS country_code \n" +
                "FROM ndfl_person np \n" +
                "join ref_book_person rbp_d ON rbp_d.id = np.person_id \n" +
                "join ref_book_person rbp on rbp.record_id=rbp_d.record_id and (rbp.start_date <= :date and (rbp.end_date >= :date or rbp.end_date is null)) AND rbp.record_id = rbp.old_id\n" +
                "LEFT JOIN ref_book_country rbc ON rbp.citizenship = rbc.id AND rbc.status = 0 \n" +
                "LEFT JOIN ref_book_country rbc2 ON rbp.country_id = rbc2.id AND rbc2.status = 0 \n" +
                "LEFT JOIN ref_book_taxpayer_state rbts ON rbp.taxpayer_state = rbts.id AND rbts.status = 0 \n" +
                "LEFT JOIN ref_book_id_tax_payer ritp ON ritp.person_id = rbp.id \n" +
                "LEFT JOIN ref_book_id_doc rbid ON rbid.id = rbp.report_doc \n" +
                "LEFT JOIN ref_book_doc_type rbdt ON rbid.doc_id = rbdt.id AND rbdt.status = 0 \n" +
                "WHERE np.declaration_data_id = :dd"

        MapSqlParameterSource params = new MapSqlParameterSource("date", new java.util.Date())
        params.addValue("dd", declarationData.id)

        List<NdflPerson> refBookPersonList = namedParameterJdbcTemplate.query(sql, params, new RowMapper<NdflPerson>() {
            @Override
            NdflPerson mapRow(ResultSet rs, int rowNum) throws SQLException {
                NdflPerson person = new NdflPerson();

                person.setPersonId(SqlUtils.getLong(rs, "id"));
                person.setRecordId(SqlUtils.getLong(rs, "inp"));
                ;
                person.setInp(String.valueOf(SqlUtils.getLong(rs, "inp")));
                person.setLastName(rs.getString("last_name"));
                person.setFirstName(rs.getString("first_name"));
                person.setMiddleName(rs.getString("middle_name"));
                person.setBirthDay(rs.getDate("birth_date"));
                person.setCitizenship(rs.getString("citizenship"));
                person.setInnNp(rs.getString("inn"));
                person.setInnForeign(rs.getString("inn_foreign"));
                person.setStatus(rs.getString("status"));
                person.setSnils(rs.getString("snils"));
                person.setIdDocType(rs.getString("id_doc_type"));
                person.setIdDocNumber(rs.getString("doc_number"));
                person.setRegionCode(rs.getString("region_code"));
                person.setPostIndex(rs.getString("postal_code"));
                person.setArea(rs.getString("district"));
                person.setCity(rs.getString("city"));
                person.setLocality(rs.getString("locality"));
                person.setStreet(rs.getString("street"));
                person.setHouse(rs.getString("house"));
                person.setBuilding(rs.getString("build"));
                person.setFlat(rs.getString("appartment"));
                person.setCountryCode(rs.getString("country_code"));
                person.setAddress(rs.getString("address"));

                return person;
            }
        })
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
        logger.info("Завершено обновление данных ФЛ формы: № %s, Период %s, Подразделение %s, Вид: \"%s\". Обновлено %s записей.",
                declarationData.id,
                departmentReportPeriodService.formatPeriodName(reportPeriod, SharedConstants.DATE_FORMAT),
                department.getName(),
                declarationTemplate.type.name,
                toUpdatePersons.size())
    }

    /**
     * Создает сообщение об обновлении данных у физдица
     * @param personInfo    информация о Физлице
     * @param updateInfo    информация об обновленных полях
     * @return  сообщение для {@code com.aplana.sbrf.taxaccounting.model.log.Logger}
     */
    LogEntry createLogMessage(String personInfo, List<String> updateInfo) {
        return new LogEntry(LogLevel.INFO, String.format("Обновлены данные физического лица: %s. Изменены значения параметров: %s",
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
                person.inp ? person.inp : "\"_\"",
                dul != ", " ? dul : "\"_\"")
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