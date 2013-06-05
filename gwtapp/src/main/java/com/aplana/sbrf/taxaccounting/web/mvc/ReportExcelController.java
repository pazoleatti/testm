package com.aplana.sbrf.taxaccounting.web.mvc;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.service.FormDataPrintingService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;


@Controller
@RequestMapping("/downloadController")
public class ReportExcelController {

	@Autowired
	FormDataPrintingService formDataPrintingService;
	
	@Autowired
	SecurityService securityService;

    @Autowired
    TAUserService taUserService;
	
	private static String REQUEST_JATTR = "jsonobject";
	private static String LOG_ENTRIES = "listLogEntries";
	private static String JSON_ENTRY_1 = "errorCode";
	private static String JSON_ENTRY_2 = "message";
	
	
	@RequestMapping(value = "/{formDataId}/{isShowChecked}",method = RequestMethod.GET)
	public void processFormDataDownload(@PathVariable int formDataId,@PathVariable boolean isShowChecked , HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String filePath = formDataPrintingService.generateExcel(securityService.currentUserInfo(), formDataId, isShowChecked);
		createResponse(req, resp, filePath);
	}
	
	@RequestMapping(value="/processLogDownload",method = RequestMethod.POST)
	public void processLogDownload(HttpServletRequest req, HttpServletResponse resp) throws IOException, JSONException{

		BufferedReader inReader = new BufferedReader(new InputStreamReader(req.getInputStream(),"UTF-8"));
		Properties pro = new Properties();
		pro.load(inReader);
		inReader.close();

		List<LogEntry> listLogEntries = new LinkedList<LogEntry>(); 
		JSONTokener jt = new JSONTokener(pro.getProperty(REQUEST_JATTR));
		JSONObject jObj = new JSONObject(jt);

		JSONArray jArr = jObj.getJSONArray(LOG_ENTRIES);
		System.out.println(jArr);
		for(int i = 0; i < jArr.length(); i++){
			listLogEntries.add(
					new LogEntry(LogLevel.ERROR.name().equals(jArr.getJSONObject(i).getString(JSON_ENTRY_1))
							?LogLevel.ERROR:LogLevel.WARNING.name().equals(jArr.getJSONObject(i).getString(JSON_ENTRY_1))
									?LogLevel.WARNING:LogLevel.INFO,jArr.getJSONObject(i).getString(JSON_ENTRY_2)));
		}
		String filePath = formDataPrintingService.generateExcelLogEntry(listLogEntries);
		createResponse(req, resp, filePath);
		
	}

    @RequestMapping(value = "/processSecUserDownload",method = RequestMethod.GET)
    public void processSecUserDownload(HttpServletRequest req, HttpServletResponse response) throws IOException {
        String filePath = formDataPrintingService.generateExcelUsers(taUserService.lisAllFullActiveUsers());
        createResponse(req, response, filePath);
    }
	
	private void createResponse(final HttpServletRequest req, final HttpServletResponse response, final String filePath) throws IOException{
		File file = new File(filePath);
		String fileName = file.getName();
		ServletContext context  = req.getSession().getServletContext();
		String mimeType = context.getMimeType(filePath);

		response.setContentType(mimeType == null ? 
				"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet; charset=UTF-8" : mimeType);
		response.setContentLength((int)file.length());
		response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(fileName, "UTF-8") + "\"");

		DataInputStream in = new DataInputStream(new FileInputStream(file));
		OutputStream out = response.getOutputStream();
		int count = 0;
		try {
			count = IOUtils.copy(in, out);
		} finally {
			in.close();
			out.close();
			file.delete();
		}
		response.setContentLength(count);
	}

}
