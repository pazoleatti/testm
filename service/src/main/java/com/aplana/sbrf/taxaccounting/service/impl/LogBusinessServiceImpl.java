package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.LogBusinessDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataSearchService;
import com.aplana.sbrf.taxaccounting.service.FormDataSearchService;
import com.aplana.sbrf.taxaccounting.service.LogBusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class LogBusinessServiceImpl implements LogBusinessService {

	@Autowired
	private LogBusinessDao logBusinessDao;

    @Autowired
    private FormDataSearchService formDataSearchService;

    @Autowired
    private DeclarationDataSearchService declarationDataSearchService;

    @Autowired
    private AuditService auditService;

	@Override
	public List<LogBusiness> getFormLogsBusiness(long formId) {
		return logBusinessDao.getFormLogsBusiness(formId);
	}

    @Override
    public PagingResult<LogSearchResultItem> getLogsBusiness(TAUserInfo userInfo, LogBusinessFilterValues filter) {
        List<Long> formDataIds = null;
        List<Long> declarationDataIds = null;
        FormDataFilter formDataFilter = new FormDataFilter();
        DeclarationDataFilter declarationDataFilter = new DeclarationDataFilter();

        switch (filter.getAuditFormTypeId() != null ? filter.getAuditFormTypeId() : 0){
            case 1:

                formDataFilter.setTaxType(filter.getTaxType());
                /*formDataFilter.setDepartmentIds(filter.getDepartmentId() != null ? Arrays.asList(filter.getDepartmentId()) : new ArrayList<Integer>());*/
                formDataFilter.setFormDataKind(filter.getFormKind());
                formDataFilter.setFormTypeId(filter.getFormTypeId());
                formDataFilter.setReportPeriodIds(filter.getReportPeriodIds());
                formDataIds = formDataSearchService.findDataIdsByUserAndFilter(userInfo, formDataFilter);
                break;
            case 2:

                declarationDataFilter.setTaxType(filter.getTaxType());
                declarationDataFilter.setDepartmentIds(filter.getDepartmentId() != null ? Arrays.asList(filter.getDepartmentId())
                        : new ArrayList<Integer>());
                declarationDataFilter.setReportPeriodIds(filter.getReportPeriodIds());
                declarationDataFilter.setDeclarationTypeId(filter.getDeclarationTypeId());
                declarationDataIds =
                        declarationDataSearchService.getDeclarationIds(declarationDataFilter, DeclarationDataSearchOrdering.ID, false);
                break;
            default:
                formDataFilter.setTaxType(filter.getTaxType());
                /*formDataFilter.setDepartmentIds(filter.getDepartmentId() != null ? Arrays.asList(filter.getDepartmentId()) : new ArrayList<Integer>());*/
                formDataFilter.setFormDataKind(filter.getFormKind());
                formDataFilter.setFormTypeId(filter.getFormTypeId());
                formDataFilter.setReportPeriodIds(filter.getReportPeriodIds());
                formDataIds = formDataSearchService.findDataIdsByUserAndFilter(userInfo, formDataFilter);

                declarationDataFilter.setTaxType(filter.getTaxType());
                declarationDataFilter.setDepartmentIds(filter.getDepartmentId() != null ? Arrays.asList(filter.getDepartmentId())
                        : new ArrayList<Integer>());
                declarationDataFilter.setReportPeriodIds(filter.getReportPeriodIds());
                declarationDataFilter.setDeclarationTypeId(filter.getDeclarationTypeId());
                declarationDataIds =
                        declarationDataSearchService.getDeclarationIds(declarationDataFilter, DeclarationDataSearchOrdering.ID, false);
                break;
        }



        LogBusinessFilterValuesDao filterValuesDao = new LogBusinessFilterValuesDao();
        filterValuesDao.setCountOfRecords(filter.getCountOfRecords());
        filterValuesDao.setToSearchDate(filter.getToSearchDate());
        filterValuesDao.setFromSearchDate(filter.getFromSearchDate());
        filterValuesDao.setStartIndex(filter.getStartIndex());
        filterValuesDao.setDepartmentId(filter.getDepartmentId());
        filterValuesDao.setUserId(filter.getUserId());

        return logBusinessDao.getLogsBusiness(formDataIds, declarationDataIds, filterValuesDao);
    }

    @Override
	@Transactional(readOnly = false)
	public void add(Long formDataId, Long declarationId, TAUserInfo userInfo, FormDataEvent event, String note) {
		LogBusiness log = new LogBusiness();
		log.setFormId(formDataId);
		log.setDeclarationId(declarationId);
		log.setEventId(event.getCode());
		log.setUserId(userInfo.getUser().getId());
		log.setLogDate(new Date());
		log.setNote(note);
		log.setDepartmentId(userInfo.getUser().getDepartmentId());

		StringBuilder roles = new StringBuilder();
		for (TARole role : userInfo.getUser().getRoles()) {
			roles.append(role.getName());
		}
		log.setRoles(roles.toString());

		logBusinessDao.add(log);
	}

    @Override
    @Transactional(readOnly = false)
    public void removeRecords(List<LogSearchResultItem> items, TAUserInfo userInfo) {
        if (!items.isEmpty()){
            List<Long> listIds = new ArrayList<Long>();
            for (LogSearchResultItem item : items)
                listIds.add(item.getId());
            logBusinessDao.removeRecords(listIds);
        }
        auditService.add(FormDataEvent.LOG_SYSTEM_BACKUP, userInfo, userInfo.getUser().getDepartmentId(), null, null, null, null, "Архивация ЖА");
    }

    @Override
	public List<LogBusiness> getDeclarationLogsBusiness(long declarationId) {
		return logBusinessDao.getDeclarationLogsBusiness(declarationId);
	}
}
