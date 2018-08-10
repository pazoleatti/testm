package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.Configuration;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.impl.ConfigurationServiceImpl;
import com.aplana.sbrf.taxaccounting.service.impl.print.AbstractReportBuilder;
import com.aplana.sbrf.taxaccounting.utils.FileWrapper;
import com.aplana.sbrf.taxaccounting.utils.ResourceUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class DocumentController {
    protected static final Log LOG = LogFactory.getLog(DocumentController.class);

    @Autowired
    private ConfigurationServiceImpl configurationService;

    @GetMapping(value = "/rest/document")
    public void downloadDocument(HttpServletResponse response, @RequestParam String fileName) throws IOException {
        Configuration configuration = configurationService.fetchByEnum(ConfigurationParam.MANUAL_PATH);
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        if (configuration == null) {
            response.getWriter().print("Параметр \"" + ConfigurationParam.MANUAL_PATH.name() + "\" не задан");
        } else {
            try {
                FileWrapper fileWrapper = ResourceUtils.getSharedResource(configuration.getValue() + "/" + fileName);
                response.setContentType("application/pdf");
                IOUtils.copy(fileWrapper.getInputStream(), response.getOutputStream());
            } catch (ServiceException e) {
                response.getWriter().print(e.getMessage());
            } catch (Exception e) {
                LOG.error(e);
                response.getWriter().print(e.getMessage());
            }
        }
    }


}
