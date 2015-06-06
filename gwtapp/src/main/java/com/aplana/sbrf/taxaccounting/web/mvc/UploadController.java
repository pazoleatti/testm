package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import org.apache.commons.fileupload.FileUploadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
    public void processUploadXls(@RequestParam("uploader") MultipartFile file,
                                 HttpServletRequest request, HttpServletResponse response)
            throws FileUploadException, IOException {
        request.setCharacterEncoding("UTF-8");
        String uuid = blobDataService.create(file.getInputStream(), file.getOriginalFilename());
        response.getWriter().printf("{uuid : \"%s\"}", uuid);
    }
}
