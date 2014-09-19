package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.async.balancing.BalancingVariants;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.async.manager.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.task.AsyncTask;
import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер для формирования отчетов в формате Excel АС Учет налогов
 */

//TODO: Переделать в дальнейшем всю генерацию отчетов через временное хранилище com.aplana.sbrf.taxaccounting.web.mvc.ReportController
@Controller
@RequestMapping("/downloadController")
public class ReportExcelController {

	@Autowired
    PrintingService printingService;
	
	@Autowired
	SecurityService securityService;

    @Autowired
    TAUserService taUserService;

    @Autowired
    AuditService auditService;

    private static final String ENCODING = "UTF-8";

    @RequestMapping(value = "CSV/{formDataId}/{isShowChecked}/{manual}",method = RequestMethod.GET)
    public void processCSVFormDataDownload(@PathVariable int formDataId,@PathVariable boolean isShowChecked , @PathVariable boolean manual, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String filePath = printingService.generateCSV(securityService.currentUserInfo(), formDataId, manual, isShowChecked);
        createResponse(req, resp, filePath);
    }

	private void createResponse(final HttpServletRequest req, final HttpServletResponse response, final String filePath) throws IOException{
        File file = new File(filePath);
		String fileName = file.getName();
		response.setContentLength((int)file.length());
        setCorrectFileName(req, response, fileName);

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

    private void setCorrectFileName(HttpServletRequest request, HttpServletResponse response, String originalFileName) throws UnsupportedEncodingException {
        String userAgent = request.getHeader("User-Agent").toLowerCase();
        String fileName = URLEncoder.encode(originalFileName, ENCODING).replaceAll("\\+", "%20");
        String fileNameAttr = "filename=";
        if (userAgent.contains("msie") || userAgent.contains("webkit")) {
            fileName = "\"" + fileName + "\"";
        } else {
            fileNameAttr = fileNameAttr.replace("=", "*=") + ENCODING + "''";
        }
        response.setHeader("Content-Disposition", "attachment;" + fileNameAttr + fileName);
    }
}
