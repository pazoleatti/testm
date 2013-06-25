package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

/**
 * User: avanteev
 * Сервлет для обработки загрузки файла из налоговых форм на сервер
 */

@Controller
@RequestMapping("/uploadController")
public class UploadXlsController {

    @Autowired
    private FormDataService formDataService;

    @Autowired
    private SecurityService securityService;

    private static String JSON_ATTR1 = "formDataId";
    private static String JSON_ATTR2 = "formTemplateId";
    private static String JSON_ATTR3 = "departmentId";
    private static String JSON_ATTR4 = "formDataKindId";
    private static String JSON_ATTR5 = "formDataRPId";
    @RequestMapping(value = "/xls", method = RequestMethod.POST)
    public void processUpload(HttpServletRequest request, HttpServletResponse response) throws FileUploadException, IOException, JSONException {

        /*BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
        char[] chars = new char[request.getInputStream().available()];
        System.out.println(request.getInputStream().available());
        String str;
        while ((str = reader.readLine()) != null){
            System.out.println(str);
        }
        reader.close();*/

        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        List<FileItem> items = upload.parseRequest(request);
        FileItem fileItem = items.get(0);
        JSONTokener jt = new JSONTokener(items.get(1).getString());
        JSONObject jo = new JSONObject(jt);

        Logger logger = new Logger();
        formDataService.importFormData(logger, securityService.currentUserInfo(),
                jo.getInt(JSON_ATTR1), jo.getInt(JSON_ATTR2), jo.getInt(JSON_ATTR3),
                FormDataKind.fromId(jo.getInt(JSON_ATTR4)), jo.getInt(JSON_ATTR5), fileItem.getInputStream());
        IOUtils.closeQuietly(fileItem.getInputStream());
        /*response.setContentType("text/plain; charset=UTF-8");*/

    }
}
