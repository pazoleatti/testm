package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.servlet.http.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;


@Controller
public class DeclarationDataController {

	private static final String ATTR_FILE_ID = "ИдФайл";
	private static final String TAG_FILE = "Файл";

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
			String fileName = getFileName(declarationId, userId, "xlsx");
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
			throws IOException, ParserConfigurationException, SAXException {
		TAUser user = securityService.currentUser();
		Integer userId = user.getId();

		if (declarationService.getXmlData(declarationId, userId) != null) {
			String fileName = getFileName(declarationId, userId, "xml");
			OutputStream respOut = resp.getOutputStream();
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
			resp.setContentType("application/pdf");
			respOut.write(declarationService.getPdfData(declarationId, userId));
			respOut.close();
		}
		else {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	protected String getFileName(int declarationId, int userId, String fileExtension) {
		String xml = declarationService.getXmlData(declarationId, userId);
		InputSource inputSource = new InputSource(new StringReader(xml));
		try {
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputSource);
			Node fileNode = document.getElementsByTagName(TAG_FILE).item(0);
			NamedNodeMap attributes = fileNode.getAttributes();
			Node fileNameNode = attributes.getNamedItem(ATTR_FILE_ID);
			String fileName = fileNameNode.getTextContent() + '.' + fileExtension;
			return fileName;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
