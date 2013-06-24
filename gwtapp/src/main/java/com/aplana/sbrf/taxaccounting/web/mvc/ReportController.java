package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * User: avanteev
 */
@Controller
@RequestMapping("/downloadBlobController")
public class ReportController {

    @Autowired
    BlobDataService blobDataService;

    /**
     * Обработка запроса для формирования отчета по журналу аудита
     * @param uuid
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/processLogDownload/{uuid}", method = RequestMethod.GET)
    public void processDownload(@PathVariable String uuid, HttpServletRequest request, HttpServletResponse response) throws IOException {
        InputStream is = blobDataService.get(uuid);
        createResponse(request, response, is);
        blobDataService.delete(uuid);
    }

    private void createResponse(final HttpServletRequest req, final HttpServletResponse response, final InputStream is) throws IOException {

        String mimeType = null;

        response.setContentType(mimeType == null ?
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet; charset=UTF-8" : mimeType);
        response.setHeader("Content-Disposition", "attachment;");

        DataInputStream in = new DataInputStream(is);
        OutputStream out = response.getOutputStream();
        int count = 0;
        try {
            count = IOUtils.copy(in, out);
        } finally {
            in.close();
            out.close();
        }
        response.setContentLength(count);

    }
}
