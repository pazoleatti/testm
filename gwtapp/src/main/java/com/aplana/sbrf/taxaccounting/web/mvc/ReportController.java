package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.BlobData;
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
import java.io.OutputStream;
import java.net.URLEncoder;

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
        BlobData blobData = blobDataService.get(uuid);
        createResponse(response, blobData);
        blobDataService.delete(uuid);
    }

    private void createResponse(final HttpServletResponse response, final BlobData blobData) throws IOException {

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(blobData.getName(), "UTF-8") + "\"");

        DataInputStream in = new DataInputStream(blobData.getInputStream());
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
