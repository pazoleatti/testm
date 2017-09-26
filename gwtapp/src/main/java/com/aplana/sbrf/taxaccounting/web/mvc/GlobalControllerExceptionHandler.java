package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.error.ExceptionMessage;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.service.ErrorService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletResponse;

import static org.apache.commons.lang3.CharEncoding.UTF_8;

/**
 * Класс для обработки исключений
 */

@ControllerAdvice
public class GlobalControllerExceptionHandler {
    private static final Log LOG = LogFactory.getLog(GlobalControllerExceptionHandler.class);

    @Autowired
    private ErrorService errorService;

    /**
     * Обработка стандартных исключений
     *
     * @param e        исключение
     * @param response ответ
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    ExceptionMessage exceptionHandler(Exception e, final HttpServletResponse response) {
        if (e.getClass().equals(org.springframework.security.access.AccessDeniedException.class)){
            response.setStatus(403);
        } else {
            response.setStatus(500);
        }
        LOG.error(e.getLocalizedMessage(), e);
        response.setCharacterEncoding(UTF_8);
        return errorService.getExceptionMessage(e);
    }

}
