package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.core.api.ServerInfo;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.google.gwt.user.server.rpc.RPCServletUtils;
import com.gwtplatform.dispatch.server.Dispatch;
import com.gwtplatform.dispatch.server.RequestProvider;
import com.gwtplatform.dispatch.server.spring.DispatchServiceImpl;
import com.gwtplatform.dispatch.shared.Action;
import com.gwtplatform.dispatch.shared.ActionException;
import com.gwtplatform.dispatch.shared.Result;
import com.gwtplatform.dispatch.shared.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Переопределяем несколько методов GWT чтобы прокинуть AccessDeniedException.
 * Для версии java 1.7 достаточно переопределить doUnexpectedFailure().
 *
 * http://jira.aplana.com/browse/SBRFACCTAX-5434
 *
 * @author fmukhametdinov
 */
@Component("rpc-dispatcher")
public class RpcDispatchServiceImpl extends DispatchServiceImpl {

    private static final String noSecurityCookieMessage = "You have to define a security cookie in order to use " +
            "secured actions. See com.gwtplatform.dispatch.shared.SecurityCookie for details.";

    private static final String xsrfAttackMessage = "Cookie provided by RPC doesn't match request cookie, " +
            "aborting action, possible XSRF attack. (Maybe you forgot to set the security cookie?)";

	@Autowired
	private ServerInfo serverInfo;

    @Autowired
    private SecurityService securityService;

    @Autowired
    public RpcDispatchServiceImpl(Logger logger, Dispatch dispatch, RequestProvider requestProvider) {
        super(logger, dispatch, requestProvider);
    }

    @Override
    public String getServletName() {
        return this.getClass().getName();
    }

    @Override
    protected void doUnexpectedFailure(Throwable e) {
		boolean isAccessDeniedException = false;
        Throwable cause = e;

        while (cause.getCause() != null && !(cause instanceof AccessDeniedException)) {
            if (cause.getCause() instanceof AccessDeniedException) {
                isAccessDeniedException = true;
            }
            cause = cause.getCause();
        }

        if (isAccessDeniedException) {
            throw new AccessDeniedException("Access is denied", cause);
        } else {
            try {
                getThreadLocalResponse().reset();
            } catch (IllegalStateException ex) {
                throw new RuntimeException("Unable to report failure", e);
            }
            ServletContext servletContext = getServletContext();
            RPCServletUtils.writeResponseForUnexpectedFailure(servletContext, getThreadLocalResponse(), e);
        }
    }

    @Override
    public Result execute(String cookieSentByRPC, Action<?> action) throws ActionException, ServiceException {
        if (action.isSecured() && !cookieMatch(cookieSentByRPC)) {
            String message = xsrfAttackMessage + " While executing action: " + action.getClass().getName();
            logger.severe(message);
            throw new ServiceException(message);
        }

        try {
            return dispatch.execute(action);
        } catch (ActionException e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, "Action exception while executing " + action.getClass().getName() + ": " +
                        e.getMessage(), e);
            }

            e.initCause(null);
            throw e;
        } catch (ServiceException e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, "Service exception while executing " + action.getClass().getName() + ": " +
                        e.getMessage(), e);
            }

            throw new IllegalStateException("Can't overwrite cause", e);

        } catch (RuntimeException e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, "Unexpected exception while executing " + action.getClass().getName() + ": " +
                        "" + e.getMessage(), e);
            }

            e.initCause(null);
            throw new ServiceException(e);
        }
    }

    private boolean cookieMatch(String cookieSentByRPC) throws ServiceException {

        // Make sure the specified cookie matches the
        HttpServletRequest request = requestProvider.getServletRequest();

        if (getSecurityCookieName() == null) {
            logger.info(noSecurityCookieMessage);
            return false;
        }

        if (cookieSentByRPC == null) {
            logger.info("No cookie sent by client in RPC. (Did you forget to bind the security cookie client-side? Or" +
                    " it could be an attack.)");
            return false;
        }

        // Try to match session tokens to prevent XSRF
        Cookie[] cookies = request.getCookies();
        String cookieInRequest = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(getSecurityCookieName())) {
                    cookieInRequest = cookie.getValue();
                    break;
                }
            }
        }

        if (cookieInRequest == null) {
            logger.info("Cookie \"" + getSecurityCookieName() + "\" not found in HttpServletRequest!");
            return false;
        }

        return cookieInRequest.equals(cookieSentByRPC);
    }

	/**
	 * Дополнительная информация о текущей сессии, которая спровоцировала исключительную ситуацию
	 * @return
	 */
	private String getSessionInfo() {
		if (SecurityContextHolder.getContext().getAuthentication() != null) {
            TAUserInfo userInfo = securityService.currentUserInfo();
			return String.format("Server: %s; Context path: %s; User: %s; IP-address: %s",
					serverInfo.getServerName(),
					getServletContext().getContextPath(),
					userInfo.getUser().getLogin(),
					userInfo.getIp());
		}
		return String.format("Server: %s; Context path: %s; User: ?; IP-address: ?",
				serverInfo.getServerName(),
				getServletContext().getContextPath());
	}

	@Override
	public void log(String message, Throwable t) {
		super.log(getSessionInfo() + System.getProperty("line.separator") + message, t);
	}

	@Override
	public void log(String msg) {
		super.log(getSessionInfo() + System.getProperty("line.separator") + msg);
	}
}
