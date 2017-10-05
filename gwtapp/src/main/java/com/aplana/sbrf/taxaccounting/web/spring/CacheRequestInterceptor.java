package com.aplana.sbrf.taxaccounting.web.spring;

import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Класс установки заголовков http ответа связанных с кэшированием.
 */
public class CacheRequestInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        response.setHeader(HttpHeaders.PRAGMA, null);
        response.setHeader(HttpHeaders.EXPIRES, "-1");
        //Рендеринг документа, при открытии во фрейме, производится только в том случае, когда верхний (top) документ – с того же домена.
        response.setHeader("X-Frame-Options", "SAMEORIGIN");

        return true;
    }
}