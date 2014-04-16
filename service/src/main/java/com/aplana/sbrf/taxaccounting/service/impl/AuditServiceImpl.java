package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.AuditDao;
import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
	@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
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

		log.setDepartmentId(departmentId);
        if (reportPeriodId == null)
            log.setReportPeriodName(null);
        else {
            ReportPeriod reportPeriod = periodService.getReportPeriod(reportPeriodId);
            log.setReportPeriodName(String.valueOf(reportPeriod.getTaxPeriod().getYear()) + " " + reportPeriod.getName());
        }
		log.setDeclarationTypeId(declarationTypeId);
		log.setFormTypeId(formTypeId);
		log.setFormKindId(formKindId);
		log.setNote(note);
		log.setUserDepartmentId(userInfo.getUser().getDepartmentId());

		auditDao.add(log);
	}

	@Override
	public LogSystemFilterAvailableValues getFilterAvailableValues(TAUser user) {
		LogSystemFilterAvailableValues values = new LogSystemFilterAvailableValues();
        if (user.hasRole(TARole.ROLE_ADMIN) || user.hasRole(TARole.ROLE_CONTROL_UNP))
            values.setDepartments(departmentService.listAll());
        else if (user.hasRole(TARole.ROLE_CONTROL_NS)){
            List<Integer> departments = departmentService.getTaxFormDepartments(user, Arrays.asList(TaxType.values()));
            if (departments.isEmpty()){
                values.setDepartments(new ArrayList<Department>());
            } else{
                Set<Integer> departmentIds = new HashSet<Integer>(departments);
                values.setDepartments(new ArrayList<Department>(departmentService.getRequiredForTreeDepartments(departmentIds).values()));
            }
        }

		/*values.setFormTypeIds(formTypeDao.getAll());*/
		values.setDeclarationTypes(declarationTypeDao.listAll());
		return values;
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
        List<Long> formDataIds;
        List<Long> declarationDataIds;
        FormDataFilter formDataFilter = new FormDataFilter();
        DeclarationDataFilter declarationDataFilter = new DeclarationDataFilter();
        LogSystemFilterDao filterDao = new LogSystemFilterDao();

        //Проставляю доступные подразделения для пользователя, подразделение не выбрано в фильтре
        if (filter.getDepartmentId() != null){
            formDataFilter.setDepartmentIds(Arrays.asList(filter.getDepartmentId()));
            declarationDataFilter.setDepartmentIds(Arrays.asList(filter.getDepartmentId()));
        }
        //Только для деклараций, потому что в сервисе для НФ уже есть подобная проверка
        else {
            declarationDataFilter.setDepartmentIds(departmentService.getTaxFormDepartments(userInfo.getUser(),
                    filter.getTaxType() != null ? Arrays.asList(filter.getTaxType()) : Arrays.asList(TaxType.values())));
        }

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
                filterDao.setFormDataIds(formDataIds);
                break;
            case 2:
                declarationDataFilter.setTaxType(filter.getTaxType());
                /*declarationDataFilter.setReportPeriodIds(filter.getReportPeriodIds());*/
                declarationDataFilter.setDeclarationTypeId(filter.getDeclarationTypeId());
                declarationDataIds =
                        declarationDataSearchService.getDeclarationIds(declarationDataFilter, DeclarationDataSearchOrdering.ID, false);
                if(declarationDataIds.isEmpty())
                    return new PagingResult<LogSearchResultItem>(new ArrayList<LogSearchResultItem>(), 0);
                filterDao.setDeclarationDataIds(declarationDataIds);
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

                declarationDataFilter.setTaxType(filter.getTaxType());
                /*declarationDataFilter.setReportPeriodIds(filter.getReportPeriodIds());*/
                declarationDataFilter.setDeclarationTypeId(filter.getDeclarationTypeId());
                declarationDataIds =
                        declarationDataSearchService.getDeclarationIds(declarationDataFilter, DeclarationDataSearchOrdering.ID, false);
                if (formDataIds.isEmpty() && declarationDataIds.isEmpty()){
                    return new PagingResult<LogSearchResultItem>(new ArrayList<LogSearchResultItem>(), 0);
                }
                filterDao.setFormDataIds(formDataIds);
                filterDao.setDeclarationDataIds(declarationDataIds);

        }
        filterDao.setFromSearchDate(filter.getFromSearchDate());
        filterDao.setToSearchDate(filter.getToSearchDate());
        filterDao.setCountOfRecords(filter.getCountOfRecords());

        return auditDao.getLogsBusiness(filterDao);
    }
}
