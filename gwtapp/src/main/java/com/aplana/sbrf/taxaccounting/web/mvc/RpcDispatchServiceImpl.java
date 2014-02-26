package com.aplana.sbrf.taxaccounting.web.mvc;

import com.google.gwt.user.server.rpc.RPCServletUtils;
import com.gwtplatform.dispatch.server.Dispatch;
import com.gwtplatform.dispatch.server.RequestProvider;
import com.gwtplatform.dispatch.server.spring.DispatchServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContext;
import java.util.logging.Logger;

/**
 * Переопределяем метод doUnexpectedFailure() чтобы прокинуть AccessDeniedException.
 * http://jira.aplana.com/browse/SBRFACCTAX-5434
 *
 * @author fmukhametdinov
 */
@Component("rpc-dispatcher")
public class RpcDispatchServiceImpl extends DispatchServiceImpl {

    @Autowired
    public RpcDispatchServiceImpl(Logger logger, Dispatch dispatch, RequestProvider requestProvider) {
        super(logger, dispatch, requestProvider);
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
            RPCServletUtils.writeResponseForUnexpectedFailure(servletContext,
                    getThreadLocalResponse(), e);
        }
    }
}
