package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.service.FormTemplateImpexService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
public class FormTemplateController {

	private static final Log LOG = LogFactory.getLog(FormTemplateController.class);

	@Autowired
	FormTemplateImpexService formTemplateImpexService;

    @RequestMapping(value = "formTemplate/downloadAll")
    public void downloadAll(HttpServletResponse response) throws IOException {
        response.setHeader("Content-Disposition",
                String.format("attachment; filename=\"Templates(%s).zip\"",
                        new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
        );
        response.setCharacterEncoding("UTF-8");
		try {
        	formTemplateImpexService.exportAllTemplates(response.getOutputStream());
		} finally {
        	IOUtils.closeQuietly(response.getOutputStream());
		}
    }

    @ExceptionHandler(ServiceLoggerException.class)
    public void logServiceExceptionHandler(ServiceLoggerException e, final HttpServletResponse response) throws IOException {
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().printf("errorUuid %s", e.getUuid());
    }

	@ExceptionHandler(Exception.class)
	public void exceptionHandler(Exception e, final HttpServletResponse response) {
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		LOG.warn(e.getLocalizedMessage(), e);
		try {
            response.getWriter().printf("error %s", e.getMessage());
		} catch (IOException ioException) {
			LOG.error(ioException.getMessage(), ioException);
		}
	}
}
