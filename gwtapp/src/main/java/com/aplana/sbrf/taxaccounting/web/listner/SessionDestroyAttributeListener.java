package com.aplana.sbrf.taxaccounting.web.listner;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;

/**
 * User: avanteev
 */
public class SessionDestroyAttributeListener implements HttpSessionAttributeListener {
    public static String SPRING_SECURITY_CONTEXT = "SPRING_SECURITY_CONTEXT";

    protected Log logger = LogFactory.getLog(getClass());

    @Override
    public void attributeAdded(HttpSessionBindingEvent event) {
    }

    @Override
    public void attributeRemoved(HttpSessionBindingEvent event) {
        String attributeName = event.getName();
        Object attributeValue = event.getValue();

        if(SPRING_SECURITY_CONTEXT.equals(attributeName)){
            WebApplicationContext springContext =
                    WebApplicationContextUtils.getWebApplicationContext(event.getSession().getServletContext());
            TAUserService userService = (TAUserService)springContext.getBean("taUserService");
            String name = ((UserDetails)(((SecurityContext)attributeValue).getAuthentication().getPrincipal())).getUsername();
            TAUserInfo userInfo = new TAUserInfo();
            userInfo.setUser(userService.getUser(name));
            userInfo.setIp(((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                    .getRequest().getRemoteAddr());

            logger.info("Exit: " + userInfo);
            FormDataService unlockFormData =(FormDataService)springContext.getBean("unlockFormData");
            unlockFormData.unlockAllByUser(userInfo);
            AuditService auditService = (AuditService) springContext.getBean("auditServiceImpl");
            auditService.add(FormDataEvent.LOGOUT, userInfo,
                    userInfo.getUser().getDepartmentId(), null, null, null, null,
                    "Attribute removed : " + attributeName);
            logger.info("Attribute removed : " + attributeName + " : " + attributeValue);
        }

    }

    @Override
    public void attributeReplaced(HttpSessionBindingEvent event) {
    }
}
