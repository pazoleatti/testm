package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.UuidEnum;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.apache.commons.lang3.CharEncoding.UTF_8;

/**
 * Класс для обработки исключений
 */

@ControllerAdvice
public class GlobalControllerExceptionHandler {
    private static final Log LOG = LogFactory.getLog(GlobalControllerExceptionHandler.class);
    final private LogEntryService logEntryService;

    public GlobalControllerExceptionHandler(LogEntryService logEntryService) {
        this.logEntryService = logEntryService;
    }

    /**
     * Обработка исключений, связанных с тем, что скрипт выполнился с ошибками
     *
     * @param e        исключение
     * @param response ответ
     * @throws IOException   IOException
     * @throws JSONException JSONException
     */
    @ExceptionHandler(ServiceLoggerException.class)
    public void logServiceExceptionHandler(ServiceLoggerException e, final HttpServletResponse response) throws IOException, JSONException {
        JSONObject errors = new JSONObject();
        response.setCharacterEncoding(UTF_8);
        errors.put(UuidEnum.ERROR_UUID.toString(), e.getUuid());
        response.getWriter().printf(errors.toString());
    }

    /**
     * Обработка стандартных исключений
     *
     * @param e        исключение
     * @param response ответ
     * @throws JSONException JSONException
     */
    @ExceptionHandler(Exception.class)
    public void exceptionHandler(Exception e, final HttpServletResponse response) throws JSONException {
        response.setCharacterEncoding(UTF_8);
        LOG.error(e.getLocalizedMessage(), e);
        JSONObject errors = new JSONObject();
        try {
            Logger log = new Logger();
            log.error(e.getMessage());
            errors.put(UuidEnum.ERROR_UUID.toString(), logEntryService.save(log.getEntries()));
            response.getWriter().printf(errors.toString());
        } catch (IOException ioException) {
            LOG.error(ioException.getMessage(), ioException);
        }
    }
}
