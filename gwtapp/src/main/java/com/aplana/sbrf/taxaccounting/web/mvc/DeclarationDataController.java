package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.server.GetDeclarationDataHandler;
import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.server.PDFImageUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(value = "/actions/declarationData")
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

    @Autowired
    private LogBusinessService logBusinessService;

    @Autowired
    private TAUserService userService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;

    @Autowired
    private RefBookFactory rbFactory;

    private static final String ENCODING = "UTF-8";

    private static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        }
    };


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

    @RequestMapping(value = "/getDeclarationData/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> fetchDeclarationData(@PathVariable long id) {
        Map<String, Object> result = new HashMap<String, Object>();
        TAUserInfo userInfo = securityService.currentUserInfo();

        if (!declarationService.existDeclarationData(id)) {
            throw new ServiceLoggerException(String.format(DeclarationDataDao.DECLARATION_NOT_FOUND_MESSAGE, id), null);
        }

        DeclarationData declaration = declarationService.get(id, userInfo);
        result.put("department",departmentService.getParentsHierarchy(
                declaration.getDepartmentId()));

        String userLogin = logBusinessService.getFormCreationUserName(declaration.getId());
        if (userLogin != null && !userLogin.isEmpty()) {
            result.put("creator_user_name", userService.getUser(userLogin).getName());
        }

        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declaration.getDeclarationTemplateId());
        result.put("form_kind", declarationTemplate.getDeclarationFormKind().getTitle());

        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(
                declaration.getDepartmentReportPeriodId());
        result.put("report_period", departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear() + ", " +departmentReportPeriod.getReportPeriod().getName());

        result.put("state", declaration.getState().getTitle());

        if (declaration.getAsnuId() != null) {
            RefBookDataProvider asnuProvider = rbFactory.getDataProvider(RefBook.Id.ASNU.getId());
            result.put("asnu_name", asnuProvider.getRecordData(declaration.getAsnuId()).get("NAME").getStringValue());
        }

        result.put("date_and_time_create", sdf.get().format(logBusinessService.getFormCreationDate(declaration.getId())));
        return result;
    }
}
