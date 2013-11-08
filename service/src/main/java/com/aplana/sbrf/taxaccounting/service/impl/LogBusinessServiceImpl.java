package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.LogBusinessDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataSearchService;
import com.aplana.sbrf.taxaccounting.service.FormDataSearchService;
import com.aplana.sbrf.taxaccounting.service.LogBusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

	@Override
	public List<LogBusiness> getFormLogsBusiness(long formId) {
		return logBusinessDao.getFormLogsBusiness(formId);
	}

    @Override
    public PagingResult<LogBusinessSearchResultItem> getLogsBusiness(TAUserInfo userInfo, LogBusinessFilterValues filter) {
        FormDataFilter formDataFilter = new FormDataFilter();
        formDataFilter.setTaxType(filter.getTaxType());
        formDataFilter.setDepartmentIds(Arrays.asList(filter.getDepartmentId()));
        formDataFilter.setFormDataKind(filter.getFormKind());
        formDataFilter.setFormTypeId(filter.getFormTypeId());
        formDataFilter.setReportPeriodIds(filter.getReportPeriodIds());
        List<Long> formDataIds = formDataSearchService.findDataIdsByUserAndFilter(userInfo, formDataFilter);


        DeclarationDataFilter declarationDataFilter = new DeclarationDataFilter();
        declarationDataFilter.setTaxType(filter.getTaxType());
        declarationDataFilter.setDepartmentIds(Arrays.asList(filter.getDepartmentId()));
        declarationDataFilter.setReportPeriodIds(filter.getReportPeriodIds());
        List<Long> declarationDataIds =
                declarationDataSearchService.getDeclarationIds(declarationDataFilter, DeclarationDataSearchOrdering.ID, false);

        LogBusinessFilterValuesDao filterValuesDao = new LogBusinessFilterValuesDao();
        filterValuesDao.setCountOfRecords(filter.getCountOfRecords());
        filterValuesDao.setToSearchDate(filter.getToSearchDate());
        filterValuesDao.setFromSearchDate(filter.getFromSearchDate());
        filterValuesDao.setStartIndex(filter.getStartIndex());
        filterValuesDao.setDepartmentId(filter.getDepartmentId());

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
	public List<LogBusiness> getDeclarationLogsBusiness(long declarationId) {
		return logBusinessDao.getDeclarationLogsBusiness(declarationId);
	}
}
