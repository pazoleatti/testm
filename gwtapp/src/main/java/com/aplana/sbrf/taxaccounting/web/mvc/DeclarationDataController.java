package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.server.GetDeclarationDataHandler;
import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.server.PDFImageUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;

@Controller
@RequestMapping(value = "declarationData")
public class DeclarationDataController {

    @Autowired
    private DeclarationDataService declarationService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private BlobDataService blobDataService;

    @Autowired
    private DeclarationTemplateService declarationTemplateService;

    private static final String ENCODING = "UTF-8";


    @RequestMapping(value = "/xlsx/{id}", method = RequestMethod.GET)
    public void xlsx(@PathVariable long id, HttpServletResponse response)
            throws IOException {
        TAUserInfo userInfo = securityService.currentUserInfo();

        String fileName = null;
        String xmlDataFileName = declarationService.getXmlDataFileName(id, userInfo);
        if (xmlDataFileName != null) {
            fileName = URLEncoder.encode(xmlDataFileName.replace("zip", "xlsx"), ENCODING);
        }

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\""
                + fileName + "\"");
        String uuid = reportService.getDec(userInfo, id, DeclarationDataReportType.EXCEL_DEC);
        if (uuid != null) {
            BlobData blobData = blobDataService.get(uuid);
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
    }

    @RequestMapping(value = "/specific/{alias}/{id}", method = RequestMethod.GET)
    public void specific(@PathVariable String alias, @PathVariable long id, HttpServletResponse response)
            throws IOException {
        TAUserInfo userInfo = securityService.currentUserInfo();
        DeclarationDataReportType ddReportType = DeclarationDataReportType.getDDReportTypeByName(alias);
        DeclarationData declaration = declarationService.get(id, userInfo);
        ddReportType.setSubreport(declarationTemplateService.getSubreportByAlias(declaration.getDeclarationTemplateId(), alias));

        String uuid = reportService.getDec(securityService.currentUserInfo(), id, ddReportType);
        if (uuid != null) {
            BlobData blobData = blobDataService.get(uuid);

            String fileName = URLEncoder.encode(blobData.getName(), ENCODING);
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\""
                    + fileName + "\"");

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
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().printf("Отчет не найден");

        }
    }


    @RequestMapping(value = "/pageImage/{id}/{pageId}/*", method = RequestMethod.GET)
    public void pageImage(@PathVariable int id, @PathVariable int pageId,
                          HttpServletResponse response) throws IOException {

        InputStream pdfData = declarationService.getPdfDataAsStream(id, securityService.currentUserInfo());
        PDFImageUtils.pDFPageToImage(pdfData, response.getOutputStream(),
                pageId, "png", GetDeclarationDataHandler.DEFAULT_IMAGE_RESOLUTION);
        response.setContentType("image/png");
        pdfData.close();
    }


    @RequestMapping(value = "/xml/{id}", method = RequestMethod.GET)
    public void xml(@PathVariable int id, HttpServletResponse response)
            throws IOException {

        InputStream xmlDataIn = declarationService.getXmlDataAsStream(id, securityService.currentUserInfo());
        String fileName = URLEncoder.encode(declarationService.getXmlDataFileName(id, securityService.currentUserInfo()), ENCODING);

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\""
                + fileName + "\"");
		try {
        	IOUtils.copy(xmlDataIn, response.getOutputStream());
		} finally {
        	IOUtils.closeQuietly(xmlDataIn);
		}
    }
}
