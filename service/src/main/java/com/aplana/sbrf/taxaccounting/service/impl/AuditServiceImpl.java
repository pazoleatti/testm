package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.AuditDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.util.TransactionHelper;
import com.aplana.sbrf.taxaccounting.util.TransactionLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.aplana.sbrf.taxaccounting.dao.AuditDao.SAMPLE_NUMBER;

@Service
@Transactional(readOnly = true)
public class AuditServiceImpl implements AuditService {

	@Autowired
	private AuditDao auditDao;
	@Autowired
	private DepartmentService departmentService;
    @Autowired
    private TransactionHelper tx;
    @Autowired
    private PeriodService periodService;

    private static final String RP_NAME_PATTERN = "%s %s";

    @Override
	public PagingResult<LogSearchResultItem> getLogsByFilter(LogSystemFilter filter) {
		return auditDao.getLogsForAdmin(filter);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void add(final FormDataEvent event, final TAUserInfo userInfo, final Integer departmentId, final Integer reportPeriodId,
                    final String declarationTypeName, final String formTypeName, final Integer formKindId, final String note, final String blobDataId, final Integer formTypeId) {
        tx.executeInNewTransaction(new TransactionLogic() {
            @Override
            public void execute() {
                LogSystem log = new LogSystem();
                log.setIp(userInfo.getIp());
                log.setEventId(event.getCode());
                log.setUserLogin(userInfo.getUser().getLogin());

                StringBuilder roles = new StringBuilder();
                List<TARole> taRoles = userInfo.getUser().getRoles();
                for (int i = 0; i < taRoles.size(); i++) {
                    roles.append(taRoles.get(i).getName());
                    if (i != taRoles.size() - 1) {
                        roles.append(", ");
                    }
                }
                log.setRoles(roles.toString());

                String departmentName = departmentId == null ? "" : (departmentId == 0 ? departmentService.getDepartment(departmentId).getName() : departmentService.getParentsHierarchy(departmentId));
                log.setFormDepartmentName(departmentName.substring(0, Math.min(departmentName.length(), 2000)));
                log.setFormDepartmentId(departmentId);

                if (reportPeriodId == null)
                    log.setReportPeriodName(null);
                else {
                    ReportPeriod reportPeriod = periodService.getReportPeriod(reportPeriodId);
                    log.setReportPeriodName(String.format(RP_NAME_PATTERN, reportPeriod.getTaxPeriod().getYear(), reportPeriod.getName()));
                }
                log.setDeclarationTypeName(declarationTypeName);
                log.setFormTypeName(formTypeName);
                log.setFormKindId(formKindId);
                log.setNote(note != null ? note.substring(0, Math.min(note.length(), 2000)) : null);
                int userDepId = userInfo.getUser().getDepartmentId();
                String userDepartmentName = userDepId == 0 ? departmentService.getDepartment(userDepId).getName() : departmentService.getParentsHierarchy(userDepId);
                log.setUserDepartmentName(userDepartmentName.substring(0, Math.min(userDepartmentName.length(), 2000)));

                log.setBlobDataId(blobDataId);
                log.setFormTypeId(formTypeId);

                auditDao.add(log);
            }

            @Override
            public Object executeWithReturn() {
                return null;
            }
        });
	}

    @Override
    @Transactional(readOnly = false)
    public void removeRecords(List<LogSearchResultItem> items, TAUserInfo userInfo) {
        Date startDate = null;
        Date endDate = null;
        if (!items.isEmpty()){
            List<Long> listIds = new ArrayList<Long>();
            for (LogSearchResultItem item : items){
                listIds.add(item.getId());
                if (startDate == null || item.getLogDate().compareTo(startDate) == -1) {
                    startDate = item.getLogDate();
                }
                if (endDate == null || item.getLogDate().compareTo(endDate) == 1) {
                    endDate = item.getLogDate();
                }
            }
            auditDao.removeRecords(listIds);
        }
    }

    @Override
    public void removeRecords(LogSystemFilter filter, LogSearchResultItem firstRecord, LogSearchResultItem lastRecord, TAUserInfo userInfo) {
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        Date startDate = firstRecord.getLogDate();
        Date endDate = lastRecord.getLogDate();
        auditDao.removeRecords(filter);
        add(
                FormDataEvent.LOG_SYSTEM_BACKUP,
                userInfo,
                userInfo.getUser().getDepartmentId(),
                null, null, null, null,
                "Архивация событий ЖА за период: " + format.format(startDate) + " - " + format.format(endDate),
                null, null);
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
        try {
            TAUser user = userInfo.getUser();
            HashMap<SAMPLE_NUMBER, Collection<Integer>> sampleVal =
                    new HashMap<SAMPLE_NUMBER, Collection<Integer>>(3);
            if (user.hasRole(TARole.ROLE_ADMIN)) {
                return auditDao.getLogsForAdmin(filter);
            } else if (user.hasRole(TARole.ROLE_CONTROL_NS) || user.hasRole(TARole.ROLE_CONTROL)) {

                sampleVal.put(SAMPLE_NUMBER.S_10, departmentService.getBADepartmentIds(userInfo.getUser()));
                sampleVal.put(SAMPLE_NUMBER.S_45, departmentService.getSourcesDepartmentIds(userInfo.getUser(), null, null));
                sampleVal.put(SAMPLE_NUMBER.S_55, departmentService.getAppointmentDepartments(user));

                return auditDao.getLogsBusinessForControl(filter, sampleVal);
            } else if (user.hasRole(TARole.ROLE_OPER)) {
                sampleVal.put(SAMPLE_NUMBER.S_10, departmentService.getBADepartmentIds(userInfo.getUser()));
                sampleVal.put(SAMPLE_NUMBER.S_55, departmentService.getAppointmentDepartments(user));
                return auditDao.getLogsBusinessForOper(filter, sampleVal);
            } else if (user.hasRole(TARole.ROLE_CONTROL_UNP)){
                return auditDao.getLogsBusinessForControlUnp(filter);
            }
        } catch (DaoException e) {
            throw new ServiceException("Поиск по НФ/декларациям.", e);
        }


        return new PagingResult<LogSearchResultItem>(new ArrayList<LogSearchResultItem>(0));
    }

    @Override
    public long getCountRecords(LogSystemFilter filter,  TAUserInfo userInfo) {
        TAUser user = userInfo.getUser();
        if (user.hasRole(TARole.ROLE_ADMIN)) {
            return auditDao.getCount(filter);
        } else if (user.hasRole(TARole.ROLE_CONTROL_NS) || user.hasRole(TARole.ROLE_CONTROL)) {
            HashMap<SAMPLE_NUMBER, Collection<Integer>> sampleVal =
                    new HashMap<SAMPLE_NUMBER, Collection<Integer>>(3);
            sampleVal.put(SAMPLE_NUMBER.S_10, departmentService.getBADepartmentIds(userInfo.getUser()));
            sampleVal.put(SAMPLE_NUMBER.S_45, departmentService.getSourcesDepartmentIds(userInfo.getUser(), null, null));
            sampleVal.put(SAMPLE_NUMBER.S_55, departmentService.getAppointmentDepartments(user));

            return auditDao.getCountForControl(filter, sampleVal);
        } else if (user.hasRole(TARole.ROLE_OPER)) {
            HashMap<SAMPLE_NUMBER, Collection<Integer>> sampleVal =
                    new HashMap<SAMPLE_NUMBER, Collection<Integer>>(2);
            sampleVal.put(SAMPLE_NUMBER.S_10, departmentService.getBADepartmentIds(userInfo.getUser()));
            sampleVal.put(SAMPLE_NUMBER.S_55, departmentService.getAppointmentDepartments(user));
            return auditDao.getCountForOper(filter, sampleVal);
        } else if (user.hasRole(TARole.ROLE_CONTROL_UNP)){
            return auditDao.getCountForControlUnp(filter);
        }
        return auditDao.getCount(filter);
    }
}
