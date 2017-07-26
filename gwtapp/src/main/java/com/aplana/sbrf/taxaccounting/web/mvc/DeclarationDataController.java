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
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;

/**
 * Контроллер для работы с формами ПНФ/КНФ
 */
@Controller
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


    /**
     * Формирование отчета для НФ/декларации в формате xlsx
     * @param id идентификатор формы
     * @param response ответ
     * @throws IOException
     */
    @RequestMapping(value = "/actions/declarationData/xlsx/{id}", method = RequestMethod.GET)
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

    /**
     * Формирование специфичного отчета для НФ/декларации
     * @param alias тип специфичного отчета
     * @param id идентификатор формы
     * @param response ответ
     * @throws IOException
     */
    @RequestMapping(value = "/actions/declarationData/specific/{alias}/{id}", method = RequestMethod.GET)
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


    /**
     * Формирует изображение для модели для PDFViewer
     * @param id идентификатор формы
     * @param pageId идентификатор страницы
     * @param response ответ
     * @throws IOException
     */
    @RequestMapping(value = "/actions/declarationData/pageImage/{id}/{pageId}/*", method = RequestMethod.GET)
    public void pageImage(@PathVariable int id, @PathVariable int pageId,
                          HttpServletResponse response) throws IOException {

        InputStream pdfData = declarationService.getPdfDataAsStream(id, securityService.currentUserInfo());
        PDFImageUtils.pDFPageToImage(pdfData, response.getOutputStream(),
                pageId, "png", GetDeclarationDataHandler.DEFAULT_IMAGE_RESOLUTION);
        response.setContentType("image/png");
        pdfData.close();
    }


    /**
     * Формирование отчета для НФ/декларации в формате xml
     * @param id идентификатор формы
     * @param response ответ
     * @throws IOException
     */
    @RequestMapping(value = "/actions/declarationData/xml/{id}", method = RequestMethod.GET)
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

    /**
     * Формирует DeclarationResult
     * @param id идентификатор формы
     * @return модель DeclarationResult, в которой содержаться данные о форме
     */
    @RequestMapping(value = "/rest/declarationData", method = RequestMethod.GET, params="projection=getDeclarationData")
    @ResponseBody
    public DeclarationResult fetchDeclarationData(@RequestParam long id) {
        TAUserInfo userInfo = securityService.currentUserInfo();

        if (!declarationService.existDeclarationData(id)) {
            throw new ServiceLoggerException(String.format(DeclarationDataDao.DECLARATION_NOT_FOUND_MESSAGE, id), null);
        }

        DeclarationResult result = new DeclarationResult();

        DeclarationData declaration = declarationService.get(id, userInfo);
        result.setDepartment(departmentService.getParentsHierarchy(
                declaration.getDepartmentId()));

        result.setState(declaration.getState().getTitle());

        String userLogin = logBusinessService.getFormCreationUserName(declaration.getId());
        if (userLogin != null && !userLogin.isEmpty()) {
            result.setCreationUserName(userService.getUser(userLogin).getName());
        }

        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declaration.getDeclarationTemplateId());
        result.setDeclarationFormKind(declarationTemplate.getDeclarationFormKind().getTitle());

        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(
                declaration.getDepartmentReportPeriodId());
        result.setReportPeriod(departmentReportPeriod.getReportPeriod().getName());
        result.setReportPeriodYear(departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear());

        if (declaration.getAsnuId() != null) {
            RefBookDataProvider asnuProvider = rbFactory.getDataProvider(RefBook.Id.ASNU.getId());
            result.setAsnuName(asnuProvider.getRecordData(declaration.getAsnuId()).get("NAME").getStringValue());
        }

        result.setCreationDate(sdf.get().format(logBusinessService.getFormCreationDate(declaration.getId())));
        return result;
    }
}
