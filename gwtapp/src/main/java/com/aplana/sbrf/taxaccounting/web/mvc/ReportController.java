package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * User: avanteev
 */
@RestController
public class ReportController {
    private final BlobDataService blobDataService;
    private final ReportService reportService;

    public ReportController(BlobDataService blobDataService, ReportService reportService) {
        this.blobDataService = blobDataService;
        this.reportService = reportService;
    }

    /**
     * Получает архив ЖА
     *
     * @param uuid     идентификатор
     * @param request  запрос
     * @param response ответ
     * @throws IOException IOException
     */
    @GetMapping(value = "/actions/report/{uuid}/processArchiveDownload")
    public void processDownload(@PathVariable String uuid, HttpServletRequest request, HttpServletResponse response) throws IOException {
        BlobData blobData = blobDataService.get(uuid);
        ResponseUtils.createBlobResponse(request, response, blobData);
    }

    /**
     * Получает отчет по ЖА
     *
     * @param uuid     идентификатор
     * @param request  запрос
     * @param response ответ
     * @throws IOException IOException
     */
    @GetMapping(value = "/actions/report/{uuid}/processLogDownload")
    public void processLogDownload(@PathVariable String uuid, HttpServletRequest request, HttpServletResponse response) throws IOException {
        BlobData blobData = blobDataService.get(uuid);
        reportService.deleteAudit(uuid);
        ResponseUtils.createBlobResponse(request, response, blobData);
    }

    /**
     * Получает файл из "Файлов и комментариев"
     *
     * @param uuid     идентификатор
     * @param request  запрос
     * @param response ответ
     * @throws IOException IOException
     */
    @GetMapping(value = "/actions/report/{uuid}/downloadDeclarationDataFile")
    public void processDownloadDeclarationDataFile(@PathVariable String uuid, HttpServletRequest request, HttpServletResponse response) throws IOException {
        BlobData blobData = blobDataService.get(uuid);
        ResponseUtils.createBlobResponse(request, response, blobData);
    }

    /**
     * Получает архив ЖА
     *
     * @param uuid     идентификатор
     * @param request  запрос
     * @param response ответ
     * @throws IOException IOException
     */
    @GetMapping(value = "/actions/report/{uuid}/downloadRefBookReport")
    public void processDownloadRefBookReport(@PathVariable String uuid, HttpServletRequest request, HttpServletResponse response) throws IOException {
        BlobData blobData = blobDataService.get(uuid);
        if (blobData != null) {
            ResponseUtils.createBlobResponse(request, response, blobData);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
