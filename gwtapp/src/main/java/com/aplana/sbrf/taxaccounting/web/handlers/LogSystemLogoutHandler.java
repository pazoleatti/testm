package com.aplana.sbrf.taxaccounting.web.handlers;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.UserAuthenticationToken;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * User: avanteev
 */
public class LogSystemLogoutHandler implements LogoutHandler {

    private static final Log LOG = LogFactory.getLog(LogSystemLogoutHandler.class);

    @Autowired
    private LockDataService lockDataService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        WebApplicationContext springContext =
                WebApplicationContextUtils.getWebApplicationContext(request.getSession().getServletContext());

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            UserAuthenticationToken principal = ((UserAuthenticationToken) (SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal()));
            TAUserInfo userInfo = principal.getUserInfo();

            LOG.info("Exit: " + userInfo);
            lockDataService.unlockAll(userInfo, true);
            AuditService auditService = (AuditService) springContext.getBean("auditServiceImpl");
            auditService.add(FormDataEvent.LOGOUT, userInfo,
                    userInfo.getUser().getDepartmentId(), null, null, null, null, null, null, null);
            LOG.info("Security logout system success.");
        }
    }
}
