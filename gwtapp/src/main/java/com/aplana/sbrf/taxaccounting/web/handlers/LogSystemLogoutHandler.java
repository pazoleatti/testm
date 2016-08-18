package com.aplana.sbrf.taxaccounting.web.handlers;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * User: avanteev
 */
public class LogSystemLogoutHandler implements LogoutHandler {

    private static final Log LOG = LogFactory.getLog(LogSystemLogoutHandler.class);

    @Autowired
    private LockDataService lockDataService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private SecurityService securityService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            TAUserInfo userInfo = securityService.currentUserInfo();

            LOG.info("Exit: " + userInfo);
            lockDataService.unlockAll(userInfo, true);
            auditService.add(FormDataEvent.LOGOUT, userInfo,
                    userInfo.getUser().getDepartmentId(), null, null, null, null, null, null, null);
            LOG.info("Security logout system success.");
        }
    }
}
