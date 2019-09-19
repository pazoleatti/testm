package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.AuditDao;
import com.aplana.sbrf.taxaccounting.model.AuditFormType;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.LogSystem;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.service.ServerInfo;
import com.aplana.sbrf.taxaccounting.service.TransactionHelper;
import com.aplana.sbrf.taxaccounting.service.TransactionLogic;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class AuditServiceImpl implements AuditService {
    private static final Log LOG = LogFactory.getLog(AuditServiceImpl.class);

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

    private static final String RP_NAME_PATTERN = "%s, %s";
    private static final String RP_NAME_WITH_CORR_PATTERN = "%s, %s%s";
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
    @Transactional
    public void add(final FormDataEvent event, final TAUserInfo userInfo, final String note) {
        add(event, userInfo, null, null, null, null, null,
                null, null, note != null ? note.substring(0, Math.min(note.length(), 2000)) : null, null);
    }

    @Override
    @Transactional
    public void add(final FormDataEvent event, final TAUserInfo userInfo, final Integer departmentId, final Integer reportPeriodId,
                    final String declarationTypeName, final String formTypeName, final AuditFormType auditFormType, final Integer formKindId, final String note, final String logId) {
        ReportPeriod reportPeriod = null;
        if (reportPeriodId != null) {
            reportPeriod = periodService.fetchReportPeriod(reportPeriodId);
        }
        final String reportPeriodName = reportPeriod == null ? null : String.format(RP_NAME_PATTERN, reportPeriod.getTaxPeriod().getYear(), reportPeriod.getName());

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

                add(event, userInfo, departmentName, departmentId, reportPeriodName, declarationTypeName, formTypeName, auditFormType, formKindId, mnote, logId);
                return null;
            }
        });
    }

    @Override
    @Transactional
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

                add(event, userInfo, null, null, rpName, declarationTemplateName, formTemplateName, AuditFormType.FORM_TEMPLATE_VERSION, null, mnote, logId);
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
                    String corrStr = departmentReportPeriod.getCorrectionDate() != null ? " (корр. " + sdf.get().format(departmentReportPeriod.getCorrectionDate()) + ")" : "";


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
                    add(event, userInfo, departmentName, departmentId, rpName, decTypeName, decTypeName, AuditFormType.FORM_TYPE_TAX, null, note != null ? note.substring(0, Math.min(note.length(), 2000)) : null, logId);
                }
                return null;
            }
        });
    }

    private void add(FormDataEvent event, TAUserInfo userInfo, String departmentName, Integer departmentId, String reportPeriodName,
                     String declarationTypeName, String formTypeName, AuditFormType auditFormType, Integer formKindId, String note, String logId) {
        LogSystem log = new LogSystem();
        log.setIp(userInfo.getIp());
        log.setEventId(event != null ? event.getCode() : null);
        // Реализовать передачу ФИО вместе с логином пользователя в ЖА АС "Учет налогов" (SBRFNDFL-8565)
        log.setId(Long.valueOf(userInfo.getUser().getId()));
        log.setUserLogin(userInfo.getUser().getName() + " (" + userInfo.getUser().getLogin() + ")");

        StringBuilder roles = new StringBuilder();
        List<TARole> taRoles = userInfo.getUser().getRoles();
        for (int i = 0; i < taRoles.size(); i++) {
            roles.append(taRoles.get(i).getName());
            if (i != taRoles.size() - 1) {
                roles.append(", ");
            }
        }
        log.setRoles(roles.toString().substring(0, Math.min(roles.toString().length(), 200)));

        log.setFormDepartmentName(departmentName != null ? departmentName.substring(0, Math.min(departmentName.length(), 2000)) : null);
        log.setFormDepartmentId(departmentId);
        log.setReportPeriodName(reportPeriodName);
        log.setDeclarationTypeName(declarationTypeName);
        log.setFormKindId(formKindId);
        log.setFormTypeName(formTypeName);
        log.setNote(note != null ? note.substring(0, Math.min(note.length(), 2000)) : null);
        log.setAuditFormTypeId(auditFormType == null ? null : auditFormType.getId());
        int userDepId = userInfo.getUser().getDepartmentId();
        String userDepartmentName = userDepId == 0 ? departmentService.getDepartment(userDepId).getName() : departmentService.getParentsHierarchy(userDepId);
        log.setUserDepartmentName(userDepartmentName != null ? userDepartmentName.substring(0, Math.min(userDepartmentName.length(), 2000)) : null);
        log.setLogId(logId);
        log.setServer(serverInfo.getServerName());

        auditDao.add(log);
    }
}