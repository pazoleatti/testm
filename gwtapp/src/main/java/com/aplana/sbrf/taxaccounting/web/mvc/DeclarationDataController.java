package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
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
        String uuid = reportService.getDec(userInfo, id, ReportType.EXCEL_DEC);
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
