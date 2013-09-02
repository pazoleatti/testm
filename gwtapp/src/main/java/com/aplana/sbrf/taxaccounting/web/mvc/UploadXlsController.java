package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.SignService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
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

    @Autowired
    BlobDataService blobDataService;

    @Autowired
    private SignService signService;

    @RequestMapping(value = "/xls", method = RequestMethod.POST)
    public void processUpload(HttpServletRequest request, HttpServletResponse response)
            throws FileUploadException, IOException, JSONException {

        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        List<FileItem> items = upload.parseRequest(request);
        FileItem fileItem = items.get(0);
        /*JSONTokener jt = new JSONTokener(items.get(1).getString());
        JSONObject jo = new JSONObject(jt);*/

        //checkSign(fileItem);

        /*Logger logger = new Logger();
        formDataService.importFormData(logger, securityService.currentUserInfo(),
                jo.getInt(JSON_ATTR1), jo.getInt(JSON_ATTR2), jo.getInt(JSON_ATTR3),
                FormDataKind.fromId(jo.getInt(JSON_ATTR4)), jo.getInt(JSON_ATTR5), fileItem.getInputStream(), fileItem.getName());
        IOUtils.closeQuietly(fileItem.getInputStream());*/
        String uuid = blobDataService.createTemporary(fileItem.getInputStream(), fileItem.getName());
        response.getWriter().printf("{uuid : \"%s\"}", uuid);
    }

    // пример использования SignService
    private boolean checkSign(FileItem signFile) throws IOException {
        String pathToSignFile = "/" + signFile.getName();
        File file = new File(pathToSignFile);
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(signFile.get());
        IOUtils.closeQuietly(fileOutputStream);

        // означает что подпись удалять не будем
        int delFlag = 0;
        boolean result = signService.checkSign(file.getAbsolutePath(), "C:\\Users\\Илья\\Desktop\\sign.dat", delFlag);
        file.delete();
        return result;
    }
}
