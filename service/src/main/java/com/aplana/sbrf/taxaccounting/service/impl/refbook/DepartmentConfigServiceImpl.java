package com.aplana.sbrf.taxaccounting.service.impl.refbook;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.action.DepartmentConfigFetchingAction;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecordVersion;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.result.DepartmentConfigFetchingResult;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import com.aplana.sbrf.taxaccounting.service.refbook.DepartmentConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DepartmentConfigServiceImpl implements DepartmentConfigService {

    @Autowired
    private CommonRefBookService commonRefBookService;
    @Autowired
    private RefBookFactory refBookFactory;
    @Autowired
    private PeriodService periodService;

    private static final String DEPARTMENT_ID_FILTER = "DEPARTMENT_ID = ";

    @Override
    public List<DepartmentConfigFetchingResult> fetchDepartmentConfigs(DepartmentConfigFetchingAction action) {
        RefBook departmentConfigDetailRefBook = commonRefBookService.get(RefBook.Id.NDFL_DETAIL.getId());
        // Отбираются те настройки подразделений, которые ссылаются на тербанк, id которого берется из фильтра
        String departmentConfigFilter = DEPARTMENT_ID_FILTER + action.getDepartmentId();

        ReportPeriod reportPeriod = periodService.fetchReportPeriod(action.getReportPeriodId());

        RefBookDataProvider departmentConfigDetailDataProvider = refBookFactory.getDataProvider(departmentConfigDetailRefBook.getId());

        PagingResult<Map<String, RefBookValue>> departmentConfigDetailRows = departmentConfigDetailDataProvider.getRecords(
                reportPeriod.getEndDate(), null, departmentConfigFilter, null);

        List<DepartmentConfigFetchingResult> toReturn = new ArrayList<>();

        // Создаем кэши для исключения избыточных запросов к БД
        Map<Long, String> presentPlaceCache = new HashMap<>();
        Map<Long, String> oktmoCache = new HashMap<>();
        Map<Long, String> reorganizationCache = new HashMap<>();
        Map<Long, Integer> signatoryIdCache = new HashMap<>();
        RefBookRecordVersion version = null;
        for (Map<String, RefBookValue> row : departmentConfigDetailRows) {
            DepartmentConfigFetchingResult result = new DepartmentConfigFetchingResult();
            Number rowOrd = row.get(DepartmentConfigDetailAliases.ROW_ORD.name()).getNumberValue();
            Long presentPlaceRef = row.get(DepartmentConfigDetailAliases.PRESENT_PLACE.name()).getReferenceValue();
            Long oktmoRef = row.get(DepartmentConfigDetailAliases.OKTMO.name()).getReferenceValue();
            Long reorganizationRef = row.get(DepartmentConfigDetailAliases.REORG_FORM_CODE.name()).getReferenceValue();
            Long signatoryIdRef = row.get(DepartmentConfigDetailAliases.SIGNATORY_ID.name()).getReferenceValue();
            String presentPlaceValue = null;
            String oktmoValue = null;
            String reorganizationValue = null;
            Integer signatoryIdValue = null;
            if (presentPlaceRef != null) {
                presentPlaceValue = presentPlaceCache.get(presentPlaceRef);
                if (presentPlaceValue == null) {
                    presentPlaceValue = getReferedRow(RefBook.Id.PRESENT_PLACE.getId(), presentPlaceRef).get("CODE").getStringValue();
                    presentPlaceCache.put(presentPlaceRef, presentPlaceValue);
                }
            }
            if (oktmoRef != null) {
                oktmoValue = oktmoCache.get(oktmoRef);
                if (oktmoValue == null) {
                    oktmoValue = getReferedRow(RefBook.Id.OKTMO.getId(), oktmoRef).get("CODE").getStringValue();
                    oktmoCache.put(oktmoRef, oktmoValue);
                }
            }
            if (reorganizationRef != null) {
                reorganizationValue = reorganizationCache.get(reorganizationRef);
                if (reorganizationValue == null) {
                    reorganizationValue = getReferedRow(RefBook.Id.REORGANIZATION.getId(), reorganizationRef).get("CODE").getStringValue();
                    reorganizationCache.put(reorganizationRef, reorganizationValue);
                }
            }
            if (signatoryIdRef != null) {
                signatoryIdValue = signatoryIdCache.get(signatoryIdRef);
                if (signatoryIdValue == null) {
                    signatoryIdValue = getReferedRow(RefBook.Id.MARK_SIGNATORY_CODE.getId(), signatoryIdRef).get("CODE").getNumberValue().intValue();
                    signatoryIdCache.put(signatoryIdRef, signatoryIdValue);
                }
            }

            result.setRowOrd(rowOrd != null ? rowOrd.intValue() : null);
            result.setTaxOrganCode(row.get(DepartmentConfigDetailAliases.TAX_ORGAN_CODE.name()).getStringValue());
            result.setKpp(row.get(DepartmentConfigDetailAliases.KPP.name()).getStringValue());
            result.setPresentPlace(presentPlaceValue);
            result.setName(row.get(DepartmentConfigDetailAliases.NAME.name()).getStringValue());
            result.setOktmo(oktmoValue);
            result.setPhone(row.get(DepartmentConfigDetailAliases.PHONE.name()).getStringValue());
            result.setReorganization(reorganizationValue);
            result.setReorgInn(row.get(DepartmentConfigDetailAliases.REORG_INN.name()).getStringValue());
            result.setReorgKpp(row.get(DepartmentConfigDetailAliases.REORG_KPP.name()).getStringValue());
            result.setSignatoryId(signatoryIdValue);
            result.setSignatorySurName(row.get(DepartmentConfigDetailAliases.SIGNATORY_SURNAME.name()).getStringValue());
            result.setSignatoryFirstName(row.get(DepartmentConfigDetailAliases.SIGNATORY_FIRSTNAME.name()).getStringValue());
            result.setSignatoryLastName(row.get(DepartmentConfigDetailAliases.SIGNATORY_LASTNAME.name()).getStringValue());
            result.setApproveDocName(row.get(DepartmentConfigDetailAliases.APPROVE_DOC_NAME.name()).getStringValue());
            result.setApproveOrgName(row.get(DepartmentConfigDetailAliases.APPROVE_ORG_NAME.name()).getStringValue());
            if (version == null) {
                version = departmentConfigDetailDataProvider.getRecordVersionInfo(row.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue());
            }
            result.setDepartmentConfigStartDate(version.getVersionStart());
            result.setDepartmentConfigEndDate(version.getVersionEnd());
            toReturn.add(result);
        }
        return toReturn;
    }

    private Map<String, RefBookValue> getReferedRow(Long refBookId, Long referenceValue) {
        RefBookDataProvider refBookDataProvider = refBookFactory.getDataProvider(refBookId);
        return refBookDataProvider.getRecordData(referenceValue);
    }

    /**
     * Константы соответствующие названиям аттрибутов в справочнике настроек подразделений
     */
    public enum DepartmentConfigDetailAliases {
        ROW_ORD,
        TAX_ORGAN_CODE,
        KPP,
        PRESENT_PLACE,
        NAME,
        OKTMO,
        PHONE,
        REORG_FORM_CODE,
        REORG_INN,
        REORG_KPP,
        SIGNATORY_ID,
        SIGNATORY_SURNAME,
        SIGNATORY_FIRSTNAME,
        SIGNATORY_LASTNAME,
        APPROVE_DOC_NAME,
        APPROVE_ORG_NAME
    }
}
