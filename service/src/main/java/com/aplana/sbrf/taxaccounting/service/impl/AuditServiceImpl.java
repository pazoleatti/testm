package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.AuditDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.*;
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
    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;
    @Autowired
    private DeclarationTemplateService declarationTemplateService;

    private static final String RP_NAME_PATTERN = "%s %s";
    private static final String RP_NAME_WITH_CORR_PATTERN = "%s %s%s";
    private static final SimpleDateFormat SDF_YYYY = new SimpleDateFormat("yyyy");
    private static final SimpleDateFormat SDF_DD_MM_YYYY = new SimpleDateFormat("dd.MM.yyyy");

    @Override
	public PagingResult<LogSearchResultItem> getLogsByFilter(LogSystemFilter filter) {
		return auditDao.getLogsForAdmin(filter);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void add(FormDataEvent event, TAUserInfo userInfo, Integer departmentId, Integer reportPeriodId,
                    String declarationTypeName, String formTypeName, Integer formKindId, String note, String blobDataId, Integer formTypeId) {
        add(event, userInfo, departmentId, reportPeriodId, declarationTypeName, formTypeName, formKindId, note, blobDataId);
	}

    @Override
    public void add(final FormDataEvent event, final TAUserInfo userInfo, final Integer departmentId, final Integer reportPeriodId,
                    final String declarationTypeName, final String formTypeName, final Integer formKindId, final String note, final String blobDataId) {
        tx.executeInNewTransaction(new TransactionLogic() {
            @Override
            public Object execute() {

                String departmentName = departmentId == null ?
                        departmentService.getParentsHierarchy(userInfo.getUser().getDepartmentId())
                        :
                        (
                                departmentId == 0 ? departmentService.getDepartment(departmentId).getName() :
                                departmentService.getParentsHierarchy(departmentId)
                        );

                String rpName = null;
                if (reportPeriodId != null) {
                    ReportPeriod reportPeriod = periodService.getReportPeriod(reportPeriodId);
                    rpName = String.format(RP_NAME_PATTERN, reportPeriod.getTaxPeriod().getYear(), reportPeriod.getName());
                }
                String mnote = note != null ? note.substring(0, Math.min(note.length(), 2000)) : null;

                add(event, userInfo, departmentName, departmentId, rpName, declarationTypeName, formTypeName, formKindId, mnote, blobDataId);
				return null;
            }
        });
    }

    @Override
    public void add(final FormDataEvent event, final TAUserInfo userInfo, final Date startDate, final Date endDate,
                    final String declarationTemplateName, final String formTemplateName, final String note, final String blobDataId) {
        tx.executeInNewTransaction(new TransactionLogic() {

            @Override
            public Object execute() {

                String rpName;
                if (endDate == null)
                    rpName = String.format("С %s", SDF_YYYY.format(startDate));
                else {
                    rpName = String.format("С %s по %s", SDF_YYYY.format(startDate), SDF_YYYY.format(endDate));
                }
                String mnote = note != null ? note.substring(0, Math.min(note.length(), 2000)) : null;

                add(event, userInfo, null, null, rpName, declarationTemplateName, formTemplateName, null, mnote, blobDataId);
				return null;
            }
        });
    }

    @Override
    public void add(final FormDataEvent event, final TAUserInfo userInfo, final DeclarationData declarationData, final FormData formData, final String note, final String blobDataId) {
        tx.executeInNewTransaction(new TransactionLogic() {
            @Override
            public Object execute() {
                int departmentId = declarationData != null ? declarationData.getDepartmentId() : formData.getDepartmentId();
                Integer reportPeriodId = declarationData != null ? declarationData.getReportPeriodId() : formData.getReportPeriodId();
                int departmentRPId = declarationData != null ? declarationData.getDepartmentReportPeriodId() : formData.getDepartmentReportPeriodId();
                DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(departmentRPId);
                String corrStr = departmentReportPeriod.getCorrectionDate() != null ? " в корр.периоде " + SDF_DD_MM_YYYY.format(departmentReportPeriod.getCorrectionDate()) : "";


                String departmentName = departmentId == 0 ?
                        departmentService.getDepartment(departmentId).getName() :
                        departmentService.getParentsHierarchy(departmentId);

                ReportPeriod reportPeriod = periodService.getReportPeriod(reportPeriodId);
                String rpName =  String.format(
                        RP_NAME_WITH_CORR_PATTERN,
                        reportPeriod.getTaxPeriod().getYear(),
                        reportPeriod.getName(),
                        corrStr);
                String decTypeName = null, ftName = null;
                Integer formKindId =null;
                if (declarationData != null){
                    decTypeName = declarationTemplateService.get(declarationData.getDeclarationTemplateId()).getType().getName();
                } else {
                    ftName = formData.getFormType().getName();
                    formKindId = formData.getKind().getId();
                }

                add(event, userInfo, departmentName, departmentId, rpName, decTypeName, ftName, formKindId, note != null ? note.substring(0, Math.min(note.length(), 2000)) : null, blobDataId);
                return null;
            }
        });
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

    private void add(FormDataEvent event, TAUserInfo userInfo, String departmentName, Integer departmentId, String reportPeriodName,
                     String declarationTypeName, String formTypeName, Integer formKindId, String note, String blobDataId){
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

        log.setFormDepartmentName(departmentName != null ? departmentName.substring(0, Math.min(departmentName.length(), 2000)) : null);
        log.setFormDepartmentId(departmentId);
        log.setReportPeriodName(reportPeriodName);
        log.setDeclarationTypeName(declarationTypeName);
        log.setFormTypeName(formTypeName);
        log.setFormKindId(formKindId);
        log.setNote(note != null ? note.substring(0, Math.min(note.length(), 2000)) : null);
        int userDepId = userInfo.getUser().getDepartmentId();
        String userDepartmentName = userDepId == 0 ? departmentService.getDepartment(userDepId).getName() : departmentService.getParentsHierarchy(userDepId);
        log.setUserDepartmentName(userDepartmentName != null ? userDepartmentName.substring(0, Math.min(userDepartmentName.length(), 2000)):null);
        log.setBlobDataId(blobDataId);

        auditDao.add(log);
    }
}
