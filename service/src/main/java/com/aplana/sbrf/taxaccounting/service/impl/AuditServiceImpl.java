package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.AuditDao;
import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class AuditServiceImpl implements AuditService {

	@Autowired
	private AuditDao auditDao;
	@Autowired
	private DepartmentService departmentService;
    @Autowired
	private DeclarationTypeDao declarationTypeDao;
    @Autowired
    private FormDataSearchService formDataSearchService;
    @Autowired
    private DeclarationDataSearchService declarationDataSearchService;
    @Autowired
    private PeriodService periodService;

	@Override
	public PagingResult<LogSearchResultItem> getLogsByFilter(LogSystemFilter filter) {
		return auditDao.getLogs(filter);
	}

	@Override
	@Transactional(readOnly = false)
	public void add(FormDataEvent event, TAUserInfo userInfo, int departmentId, Integer reportPeriodId,
					Integer declarationTypeId, Integer formTypeId, Integer formKindId, String note) {
		LogSystem log = new LogSystem();
		log.setLogDate(new Date());
		log.setIp(userInfo.getIp());
		log.setEventId(event.getCode());
		log.setUserId(userInfo.getUser().getId());

		StringBuilder roles = new StringBuilder();
        List<TARole> taRoles = userInfo.getUser().getRoles();
        for (int i = 0; i < taRoles.size(); i++) {
            roles.append(taRoles.get(i).getName());
            if (i != taRoles.size() - 1) {
                roles.append(", ");
            }
        }
		log.setRoles(roles.toString());

		log.setDepartmentName(departmentService.getParentsHierarchy(departmentId));
        if (reportPeriodId == null)
            log.setReportPeriodName(null);
        else {
            ReportPeriod reportPeriod = periodService.getReportPeriod(reportPeriodId);
            log.setReportPeriodName(String.format(AuditService.RP_NAME_PATTERN, reportPeriod.getTaxPeriod().getYear(), reportPeriod.getName()));
        }
		log.setDeclarationTypeId(declarationTypeId);
		log.setFormTypeId(formTypeId);
		log.setFormKindId(formKindId);
		log.setNote(note);
		log.setUserDepartmentName(departmentService.getParentsHierarchy(userInfo.getUser().getDepartmentId()));

		auditDao.add(log);
	}

    @Override
    @Transactional(readOnly = false)
    public void removeRecords(List<LogSearchResultItem> items, TAUserInfo userInfo) {
        if (!items.isEmpty()){
            List<Long> listIds = new ArrayList<Long>();
            for (LogSearchResultItem item : items)
                listIds.add(item.getId());
            auditDao.removeRecords(listIds);
        }
        add(FormDataEvent.LOG_SYSTEM_BACKUP, userInfo, userInfo.getUser().getDepartmentId(), null, null, null, null, "Архивация ЖА");
    }

    @Override
    public Date getLastArchiveDate() {
        try {
            return auditDao.lastArchiveDate();
        } catch (DaoException e){
            throw new ServiceException("Ошибка при получении последней даты архивации.", e);
        }
    }

    @Override
    public PagingResult<LogSearchResultItem> getLogsBusiness(LogSystemFilter filter, TAUserInfo userInfo) {
        List<Long> formDataIds = new ArrayList<Long>(0);
        List<Long> declarationDataIds = new ArrayList<Long>(0);
        FormDataFilter formDataFilter = new FormDataFilter();
        DeclarationDataFilter declarationDataFilter = new DeclarationDataFilter();
        LogSystemFilterDao filterDao = new LogSystemFilterDao();

        switch (filter.getAuditFormTypeId() != null ? filter.getAuditFormTypeId() : 0){
            case 1:
                formDataFilter.setTaxType(filter.getTaxType());
                if ((filter.getFormKind() != null)){
                    formDataFilter.setFormDataKind(Arrays.asList((long) filter.getFormKind().getId()));
                }
                if ((filter.getFormTypeId() != null)){
                    formDataFilter.setFormTypeId(Arrays.asList(Long.valueOf(filter.getFormTypeId())));
                }
                /*formDataFilter.setReportPeriodIds(filter.getReportPeriodIds());*/
                formDataIds = formDataSearchService.findDataIdsByUserAndFilter(userInfo, formDataFilter);
                if(formDataIds.isEmpty())
                    return new PagingResult<LogSearchResultItem>(new ArrayList<LogSearchResultItem>(), 0);
                break;
            case 2:
                declarationDataFilter.setDepartmentIds(departmentService.getTaxFormDepartments(userInfo.getUser(),
                        filter.getTaxType() != null ? Arrays.asList(filter.getTaxType()) : Arrays.asList(TaxType.values()), null, null));
                declarationDataFilter.setTaxType(filter.getTaxType());
                /*declarationDataFilter.setReportPeriodIds(filter.getReportPeriodIds());*/
                declarationDataFilter.setDeclarationTypeId(filter.getDeclarationTypeId());
                declarationDataIds =
                        declarationDataSearchService.getDeclarationIds(declarationDataFilter, DeclarationDataSearchOrdering.ID, false);
                if(declarationDataIds.isEmpty())
                    return new PagingResult<LogSearchResultItem>(new ArrayList<LogSearchResultItem>(), 0);
                break;
            default:
                formDataFilter.setTaxType(filter.getTaxType());
                formDataFilter.setFormDataKind(filter.getFormKind() != null ?
                        Arrays.asList((long)filter.getFormKind().getId()) : null);
                if ((filter.getFormTypeId() != null)){
                    formDataFilter.setFormTypeId(Arrays.asList(Long.valueOf(filter.getFormTypeId())));
                }
                /*formDataFilter.setReportPeriodIds(filter.getReportPeriodIds());*/
                formDataIds = formDataSearchService.findDataIdsByUserAndFilter(userInfo, formDataFilter);

                //Проставляю доступные подразделения для пользователя, подразделение не выбрано в фильтре
                //Т.к. перешли на хранение полного имени подразделения, то поиск по имени конкретного подразделению
                //уже в ЖА.
                declarationDataFilter.setDepartmentIds(departmentService.getTaxFormDepartments(userInfo.getUser(),
                        filter.getTaxType() != null ? Arrays.asList(filter.getTaxType()) : Arrays.asList(TaxType.values()), null, null));
                declarationDataFilter.setTaxType(filter.getTaxType());
                declarationDataFilter.setDeclarationTypeId(filter.getDeclarationTypeId());
                declarationDataIds =
                        declarationDataSearchService.getDeclarationIds(declarationDataFilter, DeclarationDataSearchOrdering.ID, false);
                if (formDataIds.isEmpty() && declarationDataIds.isEmpty()){
                    return new PagingResult<LogSearchResultItem>(new ArrayList<LogSearchResultItem>(), 0);
                }


        }
        filterDao.setFormDataIds(formDataIds);
        filterDao.setDeclarationDataIds(declarationDataIds);
        filterDao.setFromSearchDate(filter.getFromSearchDate());
        filterDao.setToSearchDate(filter.getToSearchDate());
        filterDao.setCountOfRecords(filter.getCountOfRecords());
        filterDao.setReportPeriodName(filter.getReportPeriodName());
        filterDao.setSearchOrdering(filter.getSearchOrdering());
        filterDao.setAscSorting(filter.isAscSorting());
        filterDao.setStartIndex(filter.getStartIndex());
        filterDao.setDepartmentName(filter.getDepartmentName());
        try {
            return auditDao.getLogsBusiness(filterDao);
        } catch (DaoException e){
            throw new ServiceException("Поиск по НФ/декларациям.", e);
        }
    }
}
