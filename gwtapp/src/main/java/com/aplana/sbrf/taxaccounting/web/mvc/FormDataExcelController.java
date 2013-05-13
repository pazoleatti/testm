package com.aplana.sbrf.taxaccounting.web.mvc;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.aplana.sbrf.taxaccounting.service.FormDataPrintingService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;

@Controller
@RequestMapping("/downloadController")
public class FormDataExcelController {

	@Autowired
	FormDataPrintingService formDataPrintingService;
	
	@Autowired
	private SecurityService securityService;
	
	@RequestMapping(value = "/{formDataId}/{isShowChecked}", method = RequestMethod.GET)
	public void processDownload(@PathVariable int formDataId,@PathVariable boolean isShowChecked, HttpServletRequest req,
			HttpServletResponse resp) throws IOException {
		String filePath = formDataPrintingService.generateExcel(securityService.currentUser().getId(), formDataId, isShowChecked);
		File file = new File(filePath);
		String fileName = file.getName();
		ServletContext context  = req.getSession().getServletContext();
		String mimeType = context.getMimeType(filePath);

		resp.setContentType(mimeType == null ? "application/octet-stream" : mimeType);
		resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet; charset=UTF-8");
		resp.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(fileName, "UTF-8") + "\"");

		DataInputStream in = new DataInputStream(new FileInputStream(file));
		OutputStream out = resp.getOutputStream();
		int count = 0;
		try {
			count = IOUtils.copy(in, out);
		} finally {
			in.close();
			out.close();
			file.delete();
		}
		resp.setContentLength(count);
	}

}
