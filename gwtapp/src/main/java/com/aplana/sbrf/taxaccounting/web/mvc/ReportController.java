package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.FormDataReportType;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.IfrsDataService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;

/**
 * User: avanteev
 */
@Controller
@RequestMapping("/downloadBlobController")
public class ReportController {

    @Autowired
    private BlobDataService blobDataService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private IfrsDataService ifrsDataService;

    @Autowired
    private SecurityService securityService;

    private static final String ENCODING = "UTF-8";

    /**
     * Получает архив ЖА
     * @param uuid
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/processArchiveDownload/{uuid}", method = RequestMethod.GET)
    public void processDownload(@PathVariable String uuid, HttpServletRequest request, HttpServletResponse response) throws IOException {
        BlobData blobData = blobDataService.get(uuid);
        createResponse(request, response, blobData);
    }

    /**
     * Получает отчет по ЖА
     * @param uuid
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/processLogDownload/{uuid}", method = RequestMethod.GET)
    public void processLogDownload(@PathVariable String uuid, HttpServletRequest request, HttpServletResponse response) throws IOException {
        BlobData blobData = blobDataService.get(uuid);
        reportService.deleteAudit(uuid);
        createResponse(request, response, blobData);
    }

    /**
     * Получает архив ЖА
     * @param uuid
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/formDataFile/{uuid}", method = RequestMethod.GET)
    public void processDownloadFormDataFile(@PathVariable String uuid, HttpServletRequest request, HttpServletResponse response) throws IOException {
        BlobData blobData = blobDataService.get(uuid);
        createResponse(request, response, blobData);
    }

    /**
     * Получает файл из "Файлов и комментариев"
     * @param uuid
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/DeclarationDataFile/{uuid}", method = RequestMethod.GET)
    public void processDownloadDeclarationDataFile(@PathVariable String uuid, HttpServletRequest request, HttpServletResponse response) throws IOException {
        BlobData blobData = blobDataService.get(uuid);
        createResponse(request, response, blobData);
    }

    /**
     * Обработка запроса на формирование отчета для налоговых форм
     * @param formDataId
     * @param isShowChecked
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "{reportType}/{formDataId}/{isShowChecked}/{manual}/{saved}",method = RequestMethod.GET)
    public void processFormDataDownload(@PathVariable String reportType, @PathVariable long formDataId, @PathVariable boolean isShowChecked,
                                        @PathVariable boolean manual, @PathVariable boolean saved,
                                        HttpServletRequest request, HttpServletResponse response)
            throws IOException, AsyncTaskException {
        String uuid = reportService.get(securityService.currentUserInfo(), formDataId, FormDataReportType.getFDReportTypeByName(reportType), isShowChecked, manual, saved);
        if (uuid != null) {
            BlobData blobData = blobDataService.get(uuid);
            createResponse(request, response, blobData);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Обработка запроса выгрузки отчетности для МСФО
     * @param reportPeriodId
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "IFRS/{reportPeriodId}",method = RequestMethod.GET)
    public void processCSVFormDataDownload(@PathVariable int reportPeriodId, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String uuid = ifrsDataService.get(reportPeriodId).getBlobDataId();
        if (uuid != null) {
            BlobData blobData = blobDataService.get(uuid);
            createResponse(request, response, blobData);
        }
    }

    /**
     * Получает архив ЖА
     * @param uuid
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/refBookReport/{uuid}", method = RequestMethod.GET)
    public void processDownloadRefBookReport(@PathVariable String uuid, HttpServletRequest request, HttpServletResponse response) throws IOException {
        BlobData blobData = blobDataService.get(uuid);
        if (blobData != null) {
            createResponse(request, response, blobData);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");response.setStatus(HttpServletResponse.SC_NOT_FOUND, "Отчет не найден");
            response.getWriter().printf("Отчет не найден");
        }
    }

    private void createResponse(final HttpServletRequest req, final HttpServletResponse response, final BlobData blobData) throws IOException{
        String fileName = blobData.getName();
        setCorrectFileName(req, response, fileName);

        DataInputStream in = new DataInputStream(blobData.getInputStream());
        OutputStream out = response.getOutputStream();
        int count = 0;
        try {
            count = IOUtils.copy(in, out);
        } finally {
            in.close();
            out.close();
        }
        response.setContentLength(count);
    }

    private void setCorrectFileName(HttpServletRequest request, HttpServletResponse response, String originalFileName) throws UnsupportedEncodingException {
        String userAgent = request.getHeader("User-Agent").toLowerCase();
        String fileName = URLEncoder.encode(originalFileName, ENCODING).replaceAll("\\+", "%20");
        String fileNameAttr = "filename=";
        if (userAgent.contains("msie") || userAgent.contains("webkit")) {
            fileName = "\"" + fileName + "\"";
        } else {
            fileNameAttr = fileNameAttr.replace("=", "*=") + ENCODING + "''";
        }
        response.setHeader("Content-Disposition", "attachment;" + fileNameAttr + fileName);
    }
}
