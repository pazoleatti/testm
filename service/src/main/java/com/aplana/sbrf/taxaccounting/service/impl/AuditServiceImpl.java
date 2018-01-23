package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.service.ServerInfo;
import com.aplana.sbrf.taxaccounting.dao.AuditDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.service.TransactionHelper;
import com.aplana.sbrf.taxaccounting.service.TransactionLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
    @Autowired
    private ServerInfo serverInfo;

    private static final String RP_NAME_PATTERN = "%s %s";
    private static final String RP_NAME_WITH_CORR_PATTERN = "%s %s%s";
    private static final ThreadLocal<SimpleDateFormat> SDF_YYYY = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy");
        }
    };
    private static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

	@Override
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void add(FormDataEvent event, TAUserInfo userInfo, Integer departmentId, Integer reportPeriodId,
                    String declarationTypeName, String formTypeName, Integer formKindId, String note, String logId, Integer formTypeId) {
        add(event, userInfo, departmentId, reportPeriodId, declarationTypeName, formTypeName, formKindId, note, logId);
	}

    @Override
    @Transactional(readOnly = false)
    public void add(final FormDataEvent event, final TAUserInfo userInfo, final Integer departmentId, final Integer reportPeriodId,
                    final String declarationTypeName, final String formTypeName, final Integer formKindId, final String note, final String logId) {
        String rpName = null;
        if (reportPeriodId != null) {
            ReportPeriod reportPeriod = periodService.fetchReportPeriod(reportPeriodId);
            rpName = String.format(RP_NAME_PATTERN, reportPeriod.getTaxPeriod().getYear(), reportPeriod.getName());
        }
        add(event, userInfo, rpName, departmentId, declarationTypeName, formTypeName, formKindId, note, null, logId);
    }

    @Override
    @Transactional(readOnly = false)
    public void add(final FormDataEvent event, final TAUserInfo userInfo, final String reportPeriodName, final Integer departmentId,
                    final String declarationTypeName, final String formTypeName, final Integer formKindId, final String note, final AuditFormType formType, final String logId) {
        tx.executeInNewTransaction(new TransactionLogic() {
            @Override
            public Object execute() {

                String departmentName = departmentId == null ?
                        null
                        :
                        (
                                departmentId == 0 ? departmentService.getDepartment(departmentId).getName() :
                                        departmentService.getParentsHierarchy(departmentId)
                        );
                String mnote = note != null ? note.substring(0, Math.min(note.length(), 2000)) : null;

                add(event, userInfo, departmentName, departmentId, reportPeriodName, declarationTypeName, formTypeName, formKindId, mnote, formType, logId);
                return null;
            }
        });
    }

    @Override
    @Transactional(readOnly = false)
    public void add(final FormDataEvent event, final TAUserInfo userInfo, final Date startDate, final Date endDate,
                    final String declarationTemplateName, final String formTemplateName, final String note, final String logId) {
        tx.executeInNewTransaction(new TransactionLogic() {

            @Override
            public Object execute() {

                String rpName;
                if (endDate == null)
                    rpName = String.format("С %s", SDF_YYYY.get().format(startDate));
                else {
                    rpName = String.format("С %s по %s", SDF_YYYY.get().format(startDate), SDF_YYYY.get().format(endDate));
                }
                String mnote = note != null ? note.substring(0, Math.min(note.length(), 2000)) : null;

                add(event, userInfo, null, null, rpName, declarationTemplateName, formTemplateName, null, mnote, null, logId);
				return null;
            }
        });
    }

    @Override
    public void add(final FormDataEvent event, final TAUserInfo userInfo, final DeclarationData declarationData, final String note, final String logId) {
        tx.executeInNewTransaction(new TransactionLogic() {
            @Override
            public Object execute() {
                if (declarationData != null) {
                    int departmentId = declarationData.getDepartmentId();
                    Integer reportPeriodId = declarationData.getReportPeriodId();
                    int departmentRPId = declarationData.getDepartmentReportPeriodId();
                    DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.fetchOne(departmentRPId);
                    String corrStr = departmentReportPeriod.getCorrectionDate() != null ? " в корр.периоде " + sdf.get().format(departmentReportPeriod.getCorrectionDate()) : "";


                    String departmentName = departmentId == 0 ?
                            departmentService.getDepartment(departmentId).getName() :
                            departmentService.getParentsHierarchy(departmentId);

                    ReportPeriod reportPeriod = periodService.fetchReportPeriod(reportPeriodId);
                    String rpName = String.format(
                            RP_NAME_WITH_CORR_PATTERN,
                            reportPeriod.getTaxPeriod().getYear(),
                            reportPeriod.getName(),
                            corrStr);
                    String decTypeName = declarationTemplateService.get(declarationData.getDeclarationTemplateId()).getType().getName();
                    add(event, userInfo, departmentName, departmentId, rpName, decTypeName, null, null, note != null ? note.substring(0, Math.min(note.length(), 2000)) : null, null, logId);
                }
                return null;
            }
        });
    }

    // TODO в метод add передавать параметр AuditFormType извне, а не вычислять как тут
    private AuditFormType getAuditFormType(String formTypeName, String declarationTypeName, String departmentName){
        if (formTypeName != null) {
            if (departmentName != null)
                return AuditFormType.FORM_TYPE_TAX;
            else
                return AuditFormType.FORM_TEMPLATE_VERSION;
        } else if (declarationTypeName != null) {
            if (departmentName != null)
                return AuditFormType.FORM_TYPE_DECLARATION;
            else
                return AuditFormType.DECLARATION_VERSION;
        }
        return null;
    }

    private void add(FormDataEvent event, TAUserInfo userInfo, String departmentName, Integer departmentId, String reportPeriodName,
                     String declarationTypeName, String formTypeName, Integer formKindId, String note, AuditFormType formType, String logId){
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
        log.setRoles(roles.toString().substring(0 , Math.min(roles.toString().length(), 200)));

        log.setFormDepartmentName(departmentName != null ? departmentName.substring(0, Math.min(departmentName.length(), 2000)) : null);
        log.setFormDepartmentId(departmentId);
        log.setReportPeriodName(reportPeriodName);
        log.setDeclarationTypeName(declarationTypeName);
        log.setFormKindId(formKindId);
        log.setFormTypeName(formTypeName);
        log.setNote(note != null ? note.substring(0, Math.min(note.length(), 2000)) : null);
        AuditFormType auditFormType = formType;
        if (formType == null) {
            auditFormType = getAuditFormType(formTypeName, declarationTypeName, departmentName);
        }
        log.setAuditFormTypeId(auditFormType == null ? null : auditFormType.getId());
        int userDepId = userInfo.getUser().getDepartmentId();
        String userDepartmentName = userDepId == 0 ? departmentService.getDepartment(userDepId).getName() : departmentService.getParentsHierarchy(userDepId);
        log.setUserDepartmentName(userDepartmentName != null ? userDepartmentName.substring(0, Math.min(userDepartmentName.length(), 2000)):null);
        log.setLogId(logId);
        log.setServer(serverInfo.getServerName());

        auditDao.add(log);
    }
}
