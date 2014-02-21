package com.aplana.sbrf.taxaccounting.web.main.api.client;

import com.google.gwt.http.client.Header;
import com.google.gwt.http.client.Response;

/**
 * Подменяем сообщение об ошибке 403, которое возникает при ошибке доступа к хендлеру.
 * По умолчанию сообщение об ошибке берется из /WEB-INF/forbidden.jsp
 *
 * @author fmukhametdinov
 */
public class ResponseImpl extends Response {

    @Override
    public String getHeader(String header) {
        return null;
    }

    @Override
    public Header[] getHeaders() {
        return new Header[0];
    }

    @Override
    public String getHeadersAsString() {
        return null;
    }

    @Override
    public int getStatusCode() {
        return 403;
    }

    @Override
    public String getStatusText() {
        return null;
    }

    @Override
    public String getText() {
        return "Доступ запрещен!";
    }
}
