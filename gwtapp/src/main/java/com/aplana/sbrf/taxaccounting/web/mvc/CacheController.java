package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.cache.CacheManagerDecorator;
import com.aplana.sbrf.taxaccounting.model.UuidEnum;
import com.aplana.sbrf.taxaccounting.web.model.CustomMediaType;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.aplana.sbrf.taxaccounting.web.authentication.SUDIRRedirectLogoutSuccessHandler.WEBSEAL_HEADER_NAME;

/**
 * Контроллер для работы с кэшем
 */
@RestController
public class CacheController {

    private CacheManagerDecorator cacheManagerDecorator;

    public CacheController(CacheManagerDecorator cacheManagerDecorator) {
        this.cacheManagerDecorator = cacheManagerDecorator;
    }

    /**
     * Сбрасывает кэш
     *
     * @return сообщение о сбросе кэша
     */
    @GetMapping(value = "/actions/cache/clear-cache", produces = CustomMediaType.TEXT_HTML_UTF8_VALUE)
    public String clearCache() {
        cacheManagerDecorator.clearAll();
        return "Кэш сброшен";
    }

    @GetMapping(value = "/actions/gc", produces = CustomMediaType.TEXT_HTML_UTF8_VALUE)
    public String runGc() {
        System.gc();
        return "run gc";
    }

    /**
     * Сброс кэша аутентификации
     *
     * @param request  http запрос
     * @param response http ответ
     * @return Json объект с флагом использования webseal
     */
    @GetMapping(value = "/actions/clearAuthenticationCache")
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String logout401(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("WWW-Authenticate", "Basic realm=\"defaultWIMFileBasedRealm\"");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(UuidEnum.IS_WEBSEAL.name(), isWebsealUsed(request));
            return jsonObject.toString();
        } catch (JSONException e) {
            return null;
        }
    }

    /**
     * Определяет был ли сделан запрос через WebSeal или напрямую к серверу
     *
     * @param request запрос
     * @return факт использования WebSeal
     */
    private boolean isWebsealUsed(HttpServletRequest request) {
        return request.getHeader(WEBSEAL_HEADER_NAME) != null;
    }
}