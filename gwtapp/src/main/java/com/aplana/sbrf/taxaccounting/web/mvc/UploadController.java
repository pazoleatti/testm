package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.SignService;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * User: avanteev
 * Сервлет для обработки загрузки файла из налоговых форм на сервер
 */

@Controller
@RequestMapping("/uploadController")
public class UploadController {

    @Autowired
    BlobDataService blobDataService;

    @Autowired
    private SignService signService;

    @RequestMapping(value = "/patterntemp", method = RequestMethod.POST)
    public void processUploadXlsTemp(HttpServletRequest request, HttpServletResponse response)
            throws FileUploadException, IOException {
        processUpload(request, response, true);
    }

    @RequestMapping(value = "/pattern", method = RequestMethod.POST)
    public void processUploadXls(HttpServletRequest request, HttpServletResponse response)
            throws FileUploadException, IOException {
        processUpload(request, response, false);
    }

    private void processUpload(HttpServletRequest request, HttpServletResponse response, boolean uploadAsTemporal) throws IOException, FileUploadException {
        System.out.println("processUpload: "+uploadAsTemporal);
        request.setCharacterEncoding("UTF-8");

        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        List<FileItem> items = upload.parseRequest(request);
        FileItem fileItem = items.get(0);
        String uuid;
        if (uploadAsTemporal) {
            uuid = blobDataService.createTemporary(fileItem.getInputStream(), fileItem.getName());
        } else {
            uuid = blobDataService.create(fileItem.getInputStream(), fileItem.getName());
        }
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
