package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    @RequestMapping(value = "/pattern", method = RequestMethod.POST)
    public void processUploadXls(HttpServletRequest request, HttpServletResponse response)
            throws FileUploadException, IOException {
        processUpload(request, response);
    }

    private void processUpload(HttpServletRequest request, HttpServletResponse response) throws IOException, FileUploadException {
        request.setCharacterEncoding("UTF-8");

        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        List<FileItem> items = upload.parseRequest(request);
        FileItem fileItem = items.get(0);
        String uuid = blobDataService.create(fileItem.getInputStream(), fileItem.getName());
        response.getWriter().printf("{uuid : \"%s\"}", uuid);
    }
}
