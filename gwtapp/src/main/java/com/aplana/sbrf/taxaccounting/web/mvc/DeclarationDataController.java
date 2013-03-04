package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.*;
import java.io.*;
import java.text.*;
import java.util.*;


@Controller
public class DeclarationDataController {

	public static final String DATE_FORMAT = "yyyyMMdd";

	@Autowired
	DeclarationDataService declarationService;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private DeclarationTemplateService declarationTemplateService;

	@Autowired
	private DepartmentService departmentService;

	@RequestMapping(value = "/downloadExcel/{declarationId}",method = RequestMethod.GET)
	public void processDownloadExcel(@PathVariable int declarationId, HttpServletResponse resp)
			throws IOException {
		TAUser user = securityService.currentUser();
		Integer userId = user.getId();

		if (declarationService.getXlsxData(declarationId, userId) != null) {
			OutputStream respOut = resp.getOutputStream();
			String fileName = generateFileName(declarationId, userId, "xlsx");
			resp.setContentType("application/octet-stream");
			resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
			respOut.write(declarationService.getXlsxData(declarationId, userId));
			respOut.close();
		}
		else {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	@RequestMapping(value = "/downloadXml/{declarationId}",method = RequestMethod.GET)
	public void processDownloadXml(@PathVariable int declarationId, HttpServletResponse resp)
			throws IOException {
		TAUser user = securityService.currentUser();
		Integer userId = user.getId();

		if (declarationService.getXmlData(declarationId, userId) != null) {
			OutputStream respOut = resp.getOutputStream();
			String fileName = generateFileName(declarationId, userId, "xml");
			resp.setContentType("application/octet-stream");
			resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
			respOut.write(declarationService.getXmlData(declarationId, userId).getBytes());
			respOut.close();
		}
		else {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	@RequestMapping(value = "/downloadPDF/{declarationId}",method = RequestMethod.GET)
	public void processDownloadPdf(@PathVariable int declarationId, HttpServletResponse resp)
			throws IOException {
		TAUser user = securityService.currentUser();
		Integer userId = user.getId();

		if (declarationService.getPdfData(declarationId, userId) != null) {
			OutputStream respOut = resp.getOutputStream();
			String fileName = generateFileName(declarationId, userId, "pdf");
			resp.setContentType("application/octet-stream");
			resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
			respOut.write(declarationService.getPdfData(declarationId, userId));
			respOut.close();
		}
		else {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	protected String generateFileName(int declarationId, int userId, String fileExtension) {
		DeclarationData declaration = declarationService.get(declarationId, userId);
		DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
		String declarationPrefix =
				declarationTemplateService.get(
						declaration.getDeclarationTemplateId()
				).getDeclarationType().getTaxType().getDeclarationPrefix();
		DepartmentParam departmentParam = departmentService.getDepartmentParam(declaration.getDepartmentId());
		Calendar calendar = Calendar.getInstance();
		StringBuilder stringBuilder = new StringBuilder(declarationPrefix);
		stringBuilder.append('_' +
				departmentParam.getTaxOrganCode() + '_' +
				departmentParam.getTaxOrganCode() + '_' +
				departmentParam.getInn() + departmentParam.getKpp() + '_' +
				dateFormat.format(calendar.getTime()) + '_' +
				UUID.randomUUID().toString().toUpperCase() + '.' +
				fileExtension);
		return stringBuilder.toString();
	}
}
