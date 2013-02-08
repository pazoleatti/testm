package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.service.DeclarationService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;


@Controller
public class DeclarationDataController {

	@Autowired
	DeclarationService declarationService;

	@Autowired
	private SecurityService securityService;

	@RequestMapping(value = "/downloadExcel/{declarationId}",method = RequestMethod.GET)
	public void processDownloadExcel(@PathVariable int declarationId, HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		TAUser user = securityService.currentUser();
		Integer userId = user.getId();

		if (declarationService.getXlsxData(declarationId, userId) != null) {
			OutputStream respOut = resp.getOutputStream();
			String fileName = "Declaration_" + declarationId + ".xlsx";
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
	public void processDownloadXml(@PathVariable int declarationId, HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		TAUser user = securityService.currentUser();
		Integer userId = user.getId();

		if (declarationService.getXlsxData(declarationId, userId) != null) {
			OutputStream respOut = resp.getOutputStream();
			String fileName = "Declaration_" + declarationId + ".xml";
			resp.setContentType("application/octet-stream");
			resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
			respOut.write(declarationService.getXmlData(declarationId, userId).getBytes());
			respOut.close();
		}
		else {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}
}
