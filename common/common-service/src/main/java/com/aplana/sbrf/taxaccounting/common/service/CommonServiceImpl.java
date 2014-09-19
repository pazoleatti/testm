package com.aplana.sbrf.taxaccounting.common.service;

import com.aplana.sbrf.taxaccounting.dao.AuditDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Спринговая реализация общего сервиса. Нужна для того, чтобы все работало в dev-моде.
 * Чтобы код не дублировался, он также вызывается из ejb-реалзации
 * @author dloshkarev
 */
@Service
public class CommonServiceImpl implements CommonService {

    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private PeriodService periodService;
    @Autowired
    private AuditDao auditDao;

    private static final String RP_NAME_PATTERN = "%s %s";

    @Override
    public void addAuditLog(FormDataEvent event, TAUserInfo userInfo, Integer departmentId, Integer reportPeriodId,
                    String declarationTypeName, String formTypeName, Integer formKindId, String note, String blobDataId) {
        LogSystem log = new LogSystem();
        log.setLogDate(new Date());
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
        if (departmentId != null && departmentId != 0) {
            Department department = departmentService.getParentTB(departmentId);
            if (department != null) {
                log.setDepartmentTBId(department.getId());
            }
        }

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

        auditDao.add(log);
    }
}
