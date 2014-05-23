package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.AuditDao;
import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static java.util.Arrays.asList;

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
    private PeriodService periodService;
    @Autowired
    private FormDataAccessService formDataAccessService;
    @Autowired
    private SourceService sourceService;

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
            log.setReportPeriodName(String.format(AuditService.RP_NAME_PATTERN, reportPeriod.getTaxPeriod().getYear(), reportPeriod.getName()));
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
        LogSystemFilterDao filterDao = new LogSystemFilterDao();

        filterDao.setAuditFormTypeId(filter.getAuditFormTypeId());
        filterDao.setDeclarationTypeId(filter.getDeclarationTypeId());
        filterDao.setFromSearchDate(filter.getFromSearchDate());
        filterDao.setToSearchDate(filter.getToSearchDate());
        filterDao.setCountOfRecords(filter.getCountOfRecords());
        filterDao.setReportPeriodName(filter.getReportPeriodName());
        filterDao.setSearchOrdering(filter.getSearchOrdering());
        filterDao.setAscSorting(filter.isAscSorting());
        filterDao.setStartIndex(filter.getStartIndex());
        filterDao.setUserIds(filter.getUserIds());
        createFormDataDaoFilter(userInfo, filter, filterDao);
        try {
            return auditDao.getLogsBusiness(filterDao);
        } catch (DaoException e){
            throw new ServiceException("Поиск по НФ/декларациям.", e);
        }
    }

    @SuppressWarnings("unchecked")
    private LogSystemFilterDao createFormDataDaoFilter(TAUserInfo userInfo, LogSystemFilter logSystemFilter, LogSystemFilterDao logSystemFilterDao) {

        // Подразделения (могут быть не заданы - тогда все доступные по выборке 40 - http://conf.aplana.com/pages/viewpage.action?pageId=11380670)
        List<Integer> departments;
        if (logSystemFilter.getDepartmentIds() == null || logSystemFilter.getDepartmentIds().isEmpty()) {
            departments = departmentService.getTaxFormDepartments(userInfo.getUser(),
                    logSystemFilter.getTaxType() != null ? asList(logSystemFilter.getTaxType()) : asList(TaxType.values()));
        } else
            departments = logSystemFilter.getDepartmentIds();

        logSystemFilterDao.setDepartmentIds(departments);
        // Отчетные периоды
        logSystemFilterDao.setReportPeriodName(logSystemFilter.getReportPeriodName());
        // Типы форм
        if (logSystemFilter.getFormKind() != null && !logSystemFilter.getFormKind().isEmpty()) {
            logSystemFilterDao.setFormDataKinds(new ArrayList<FormDataKind>(CollectionUtils.collect(logSystemFilter.getFormKind(), new Transformer() {
                @Override
                public Object transform(Object input) {
                    return FormDataKind.fromId((Integer)input);
                }
            })));
        } else {
            logSystemFilterDao.setFormDataKinds(formDataAccessService.getAvailableFormDataKind(userInfo, asList(logSystemFilter.getTaxType())));
        }
        // Виды форм
        TAUser tAUser = userInfo.getUser();
        List<Long> formTypes = logSystemFilter.getFormTypeId() != null ?
                logSystemFilter.getFormTypeId() : new ArrayList<Long>(0);
        if (formTypes.isEmpty()) {
            if (!tAUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
                if (tAUser.hasRole(TARole.ROLE_CONTROL_NS) || tAUser.hasRole(TARole.ROLE_CONTROL)) {
                    formTypes = sourceService.getDFTFormTypeBySource(tAUser.getDepartmentId(), logSystemFilter.getTaxType(), logSystemFilterDao.getFormDataKinds());
                } else {
                    formTypes = sourceService.getDFTByPerformerDep(tAUser.getDepartmentId(), logSystemFilter.getTaxType(), logSystemFilterDao.getFormDataKinds());
                }
                List<Department> departments10 = new ArrayList<Department>();
                if (tAUser.hasRole(TARole.ROLE_CONTROL_NS)) {
                    departments10 = departmentService.getBADepartments(tAUser);
                } else {
                    departments10.addAll(departmentService.getAllChildren(tAUser.getDepartmentId()));
                }
                List<DepartmentFormType> departmentFormTypeList = new ArrayList<DepartmentFormType>();
                for (Department department : departments10) {
                    departmentFormTypeList.addAll(sourceService.getDFTByDepartment(department.getId(), logSystemFilter.getTaxType()));
                }
                for(DepartmentFormType departmentFormType : departmentFormTypeList) {
                    formTypes.add((long)departmentFormType.getFormTypeId());
                }
                Set<Long> tFormTypes =  new HashSet<Long>(formTypes);
                formTypes.clear();
                formTypes.addAll(tFormTypes);
            }
        }
        logSystemFilterDao.setFormTypeIds(formTypes);

        // Вид налога
        if (logSystemFilter.getTaxType() != null) {
            logSystemFilterDao.setTaxTypes(asList(logSystemFilter.getTaxType()));
        }
        return logSystemFilterDao;
    }
}
