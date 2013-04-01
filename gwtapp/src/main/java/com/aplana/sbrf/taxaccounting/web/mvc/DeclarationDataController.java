package com.aplana.sbrf.taxaccounting.web.mvc;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;

@Controller
public class DeclarationDataController {

	@Autowired
	DeclarationDataService declarationService;

	@Autowired
	private SecurityService securityService;

	@RequestMapping(value = "/downloadExcel/{declarationId}", method = RequestMethod.GET)
	public void processDownloadExcel(@PathVariable int declarationId,
			HttpServletResponse resp) throws IOException {
		TAUser user = securityService.currentUser();
		Integer userId = user.getId();

		if (declarationService.getXlsxData(declarationId, userId) != null) {
			OutputStream respOut = resp.getOutputStream();
			String fileName = getFileName(declarationId, userId, "xlsx");;
			resp.setContentType("application/octet-stream");
			resp.setHeader("Content-Disposition", "attachment; filename=\""
					+ fileName + "\"");
			respOut.write(declarationService.getXlsxData(declarationId, userId));
			respOut.close();
		} else {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	@RequestMapping(value = "/downloadXml/{declarationId}", method = RequestMethod.GET)
	public void processDownloadXml(@PathVariable int declarationId,
			HttpServletResponse resp) throws IOException {
		TAUser user = securityService.currentUser();
		Integer userId = user.getId();

		if (declarationService.getXmlData(declarationId, userId) != null) {
			String fileName = getFileName(declarationId, userId, "xml");
			OutputStream respOut = resp.getOutputStream();
			resp.setContentType("application/octet-stream");
			resp.setHeader("Content-Disposition", "attachment; filename=\""
					+ fileName + "\"");
			respOut.write(declarationService.getXmlData(declarationId, userId)
					.getBytes());
			respOut.close();
		} else {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	@RequestMapping(value = "/downloadPDF/{declarationId}", method = RequestMethod.GET)
	public void processDownloadPdf(@PathVariable int declarationId,
			HttpServletResponse resp) throws IOException {
		TAUser user = securityService.currentUser();
		Integer userId = user.getId();

		if (declarationService.getPdfData(declarationId, userId) != null) {
			OutputStream respOut = resp.getOutputStream();
			resp.setContentType("application/pdf");
			byte[] pdf = declarationService.getPdfData(declarationId, userId);
			resp.setContentLength(pdf.length);
			respOut.write(pdf);
			respOut.close();
		} else {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	// TODO: Получение имени должно быть в сервисе.
	protected String getFileName(int declarationId, int userId,
			String fileExtension) {
		return declarationService.getXmlDataFileName(declarationId, userId)
				+ '.' + fileExtension;
	}
}
