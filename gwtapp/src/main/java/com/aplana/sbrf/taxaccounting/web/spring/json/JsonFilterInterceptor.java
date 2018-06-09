package com.aplana.sbrf.taxaccounting.web.spring.json;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * {@code HandlerInterceptor} to work with JSON filters.
 *
 * @author <a href="mailto:ogalkin@aplana.com">Oleg Galkin</a>
 */
public class JsonFilterInterceptor extends HandlerInterceptorAdapter {
    /**
     * Sets the specified {@link JsonMixins.JsonMixin}
     * annotations of a handler for the current thread.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            JsonMixins annotation = handlerMethod.getMethodAnnotation(JsonMixins.class);
            if (annotation != null) {
                JsonMixinThread.setMixins(annotation.value());
            }
        }
        return true;
    }

    /**
     * Removes the current thread's {@link JsonMixins.JsonMixin}
     * values.
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {
        /**
         * костыль для IE т.к. он не знает 'application/json'
         * http://blog.degree.no/2012/09/jquery-json-ie8ie9-treats-response-as-downloadable-file/
         **/
        String userAgent = request.getHeader("User-Agent");
        // Начиная с IE11 токен MSIE пропадает, но ослиные уши и хвост торчат из Trident
        if (userAgent != null && ((userAgent.contains("MSIE")) || (userAgent.contains("Trident"))))  {
            response.setHeader("Content-Type", "text/plain;charset=UTF-8");
        }

        JsonMixinThread.remove();
    }
}
