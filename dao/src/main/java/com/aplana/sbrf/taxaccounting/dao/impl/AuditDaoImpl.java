package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.AuditDao;
import com.aplana.sbrf.taxaccounting.model.LogSystem;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AuditDaoImpl extends AbstractDao implements AuditDao {

    private static final Log LOG = LogFactory.getLog(AuditDaoImpl.class);

    @Override
    public void add(LogSystem logSystem) {
        try {
            JdbcTemplate jt = getJdbcTemplate();
            jt.update(
                    "call Add_Log_System_NDFL (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    logSystem.getIp(),
                    logSystem.getEventId(),
                    logSystem.getUserLogin(),
                    logSystem.getRoles(),
                    logSystem.getFormDepartmentName(),
                    logSystem.getReportPeriodName(),
                    logSystem.getFormKindId(),
                    logSystem.getNote(),
                    logSystem.getUserDepartmentName(),
                    logSystem.getDeclarationTypeName(),
                    logSystem.getFormTypeName(),
                    logSystem.getFormDepartmentId(),
                    logSystem.getFormTypeId(),
                    0,
                    logSystem.getAuditFormTypeId(),
                    logSystem.getServer(),
                    // Для ХП необходим id пользователя (для SBRFNDFL-8565)
                    logSystem.getId()
            );
        } catch (DataAccessException e) {
            LOG.error("Ошибки при логировании.", e);
            throw new DaoException("Ошибки при логировании.", e);
        }
    }
}