package com.aplana.sbrf.taxaccounting.web.mvc;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;

@Controller
public class DeclarationDataController {

	private static final String ATTR_FILE_ID = "ИдФайл";
	private static final String TAG_FILE = "Файл";

	@Autowired
	DeclarationDataService declarationService;

	@Autowired
	private SecurityService securityService;

	@RequestMapping(value = "/downloadExcel/{declarationId}",method = RequestMethod.GET)
	public void processDownloadExcel(@PathVariable int declarationId, HttpServletResponse resp)
			throws IOException {
		TAUser user = securityService.currentUser();
		Integer userId = user.getId();

		if (declarationService.getXlsxData(declarationId, userId) != null) {
			OutputStream respOut = resp.getOutputStream();
			String fileName = "declaration_" + declarationId + ".xlsx";
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
	
	
	//TODO: Получение имени должно быть в сервисе.
	protected String getFileName(int declarationId, int userId, String fileExtension) {
		String xml = declarationService.getXmlData(declarationId, userId);
		InputSource inputSource = new InputSource(new StringReader(xml));
		try {
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputSource);
			Node fileNode = document.getElementsByTagName(TAG_FILE).item(0);
			NamedNodeMap attributes = fileNode.getAttributes();
			Node fileNameNode = attributes.getNamedItem(ATTR_FILE_ID);
			return fileNameNode.getTextContent() + '.' + fileExtension;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
