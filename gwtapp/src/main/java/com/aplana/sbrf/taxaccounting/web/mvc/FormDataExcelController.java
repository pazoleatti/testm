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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.aplana.sbrf.taxaccounting.service.FormDataPrintingService;


@Controller
@RequestMapping("/downloadController")
public class FormDataExcelController {

	private static final int BUFSIZE = 4096;

	@Autowired
	FormDataPrintingService formDataPrintingService;
	
	@RequestMapping(value = "/{formDataId}",method = RequestMethod.GET)
	public void processDownload(@PathVariable int formDataId, HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String filePath = formDataPrintingService.generateExcel(formDataId);
		File file = new File(filePath);
		OutputStream respOut = resp.getOutputStream();
		int length   = 0;
		
		ServletContext context  = req.getSession().getServletContext();
		String mimeType = context.getMimeType(filePath);
		if (mimeType == null) {
			mimeType = "application/octet-stream";
        }
		resp.setContentType(mimeType);
		resp.setContentLength((int)file.length());
		String fileName = file.getName();
		resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet; charset=UTF-8");
		resp.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(fileName, "UTF-8") + "\"");
		
		byte[] byteBuffer = new byte[BUFSIZE];
        DataInputStream in = new DataInputStream(new FileInputStream(file));
        while ((in!=null)&&(length = in.read(byteBuffer))!=-1)
			respOut.write(byteBuffer, 0, length);
        in.close();
        respOut.close();
        file.delete();
	}

}
