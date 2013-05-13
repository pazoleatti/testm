package com.aplana.sbrf.taxaccounting.web.mvc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.server.GetDeclarationDataHandler;
import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.server.PDFImageUtils;

@Controller
@RequestMapping(value = "declarationData")
public class DeclarationDataController {

	@Autowired
	private DeclarationDataService declarationService;

	@Autowired
	private SecurityService securityService;

	
	@RequestMapping(value = "/xlsx/{id}", method = RequestMethod.GET)
	public void xlsx(@PathVariable long id, HttpServletResponse response)
			throws IOException {
		TAUser user = securityService.currentUser();
		Integer userId = user.getId();

		byte[] xlsxData = declarationService.getXlsxData(id, userId);
		String fileName = getFileName(id, userId, "xlsx");

		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment; filename=\""
				+ fileName + "\"");
		response.getOutputStream().write(xlsxData);

	}

	
	@RequestMapping(value = "/pageImage/{id}/{pageId}/*", method = RequestMethod.GET)
	public void pageImage(@PathVariable int id, @PathVariable int pageId,
			HttpServletResponse response) throws IOException {
		TAUser user = securityService.currentUser();
		Integer userId = user.getId();

		InputStream pdfData = new ByteArrayInputStream(
				declarationService.getPdfData(id, userId));
		PDFImageUtils.pDFPageToImage(pdfData, response.getOutputStream(),
				pageId, "png", GetDeclarationDataHandler.DEFAULT_IMAGE_RESOLUTION);
		response.setContentType("image/png");
		pdfData.close();
	}

	
	@RequestMapping(value = "/xml/{id}", method = RequestMethod.GET)
	public void xml(@PathVariable int id, HttpServletResponse response)
			throws IOException {
		TAUser user = securityService.currentUser();
		Integer userId = user.getId();

		String xmlData = declarationService.getXmlData(id, userId);
		String fileName = getFileName(id, userId, "xml");

		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment; filename=\""
				+ fileName + "\"");
		response.getOutputStream().write(xmlData.getBytes("windows-1251"));
	}

	private String getFileName(long id, int userId, String fileExtension) {
		return declarationService.getXmlDataFileName(id, userId) + '.'
				+ fileExtension;
	}
}
