package com.aplana.sbrf.taxaccounting.web.mvc;

import java.io.*;
import java.net.URLEncoder;
import java.util.List;

import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Controller
public class DeclarationTemplateController {

	@Autowired
	DeclarationTemplateService declarationTemplateService;

	@RequestMapping(value = "/downloadJasper/{declarationTemplateId}",method = RequestMethod.GET)
	public void processDownload(@PathVariable int declarationTemplateId, HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		if (declarationTemplateService.getJasper(declarationTemplateId) != null) {
			OutputStream respOut = resp.getOutputStream();
			String fileName = "DeclarationTemplate_" + declarationTemplateId + ".xlsx";
			resp.setContentType("application/octet-stream");
			resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet; charset=UTF-8");
			resp.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(fileName, "UTF-8") + "\"");
			respOut.write(declarationTemplateService.getJasper(declarationTemplateId));
			respOut.close();
		}
		else {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	@RequestMapping(value = "/uploadJrxml/{declarationTemplateId}",method = RequestMethod.POST)
	public void processUpload(@PathVariable int declarationTemplateId, HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		FileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		try {
			List<FileItem> items = upload.parseRequest(req);
			declarationTemplateService.setJrxml(declarationTemplateId, items.get(0).getString());
		}
		catch (Exception e) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"An error occurred while creating the file : " + e.getMessage());
		}
	}
}
