package com.aplana.sbrf.taxaccounting.web.mvc;

import java.io.*;
import java.util.List;

import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateImpexService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Controller
public class DeclarationTemplateController {

	private static final Log logger = LogFactory.getLog(DeclarationTemplateController.class);

	@Autowired
	SecurityService securityService;

	@Autowired
	DeclarationTemplateService declarationTemplateService;
	
	@Autowired
	DeclarationTemplateImpexService declarationTemplateImpexService;
	
	
	@RequestMapping(value = "declarationTemplate/downloadDect/{declarationTemplateId}",method = RequestMethod.GET)
	public void downloadDect(@PathVariable int declarationTemplateId, HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		
		String fileName = "declarationTemplate_" + declarationTemplateId + ".zip";
		resp.setContentType("application/dect");
		resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
		resp.setCharacterEncoding("UTF-8");
		
		declarationTemplateImpexService.exportDeclarationTemplate(securityService.currentUserInfo(), declarationTemplateId, resp.getOutputStream());
		resp.getOutputStream().close();	
	}
	
	
	@RequestMapping(value = "declarationTemplate/uploadDect/{declarationTemplateId}",method = RequestMethod.POST)
	public void uploadDect(@PathVariable int declarationTemplateId, HttpServletRequest req, HttpServletResponse resp)
			throws FileUploadException, UnsupportedEncodingException, IOException {
		FileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		List<FileItem> items = upload.parseRequest(req);
		declarationTemplateImpexService.importDeclarationTemplate
				(securityService.currentUserInfo(), declarationTemplateId, items.get(0).getInputStream());
		IOUtils.closeQuietly(items.get(0).getInputStream());
	}

	@RequestMapping(value = "/downloadJrxml/{declarationTemplateId}",method = RequestMethod.GET)
	public void processDownload(@PathVariable int declarationTemplateId, HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		if (declarationTemplateService.getJrxml(declarationTemplateId) != null) {
			OutputStream respOut = resp.getOutputStream();
			String fileName = "DeclarationTemplate_" + declarationTemplateId + ".jrxml";
			resp.setContentType("application/octet-stream");
			resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
			resp.setCharacterEncoding("UTF-8");
			respOut.write(declarationTemplateService.getJrxml(declarationTemplateId).getBytes("UTF-8"));
			respOut.close();
		}
		else {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	@RequestMapping(value = "/uploadJrxml/{declarationTemplateId}",method = RequestMethod.POST)
	public void processUpload(@PathVariable int declarationTemplateId, HttpServletRequest req, HttpServletResponse resp)
			throws FileUploadException, UnsupportedEncodingException {
		FileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		List<FileItem> items = upload.parseRequest(req);
		declarationTemplateService.setJrxml(declarationTemplateId, items.get(0).getString("UTF-8"));
	}

	@ExceptionHandler(Exception.class)
	public void exceptionHandler(Exception e, final HttpServletResponse response) {
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		logger.warn(e.getLocalizedMessage(), e);
		try {
			response.getWriter().append("error ").append(e.getMessage()).close();
		} catch (IOException ioException) {
			logger.error(ioException.getMessage(), ioException);
		}
	}
}
