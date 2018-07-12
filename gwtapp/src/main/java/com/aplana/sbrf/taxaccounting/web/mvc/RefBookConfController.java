package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.model.result.RefBookConfListItem;
import com.aplana.sbrf.taxaccounting.script.service.RefBookService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

@RestController
public class RefBookConfController {

    @Autowired
    private RefBookService refBookService;
    @Autowired
    private SecurityService securityService;

    @InitBinder
    public void init(ServletRequestDataBinder binder) {
        binder.registerCustomEditor(PagingParams.class, new RequestParamEditor(PagingParams.class));
    }

    /**
     * Возвращяет список справочников для настройщика
     */
    @GetMapping(value = "/rest/refBookConf", params = "projection=refBookConfList")
    public JqgridPagedList<RefBookConfListItem> fetchRefBookConfList(@RequestParam PagingParams pagingParams) {
        PagingResult<RefBookConfListItem> pagingResult = refBookService.fetchRefBookConfPage(pagingParams, securityService.currentUserInfo());

        return JqgridPagedResourceAssembler.buildPagedList(
                pagingResult,
                pagingResult.getTotalCount(),
                pagingParams
        );
    }

    /**
     * Формирует архив со скриптами и др файлами, связанными со всеми справочниками
     *
     * @return uuid ссылку на уведомления с результатом выполнения
     */
    @GetMapping(value = "/actions/refBookConf/export")
    public void exportRefBooks(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        BlobData blobData = refBookService.exportRefBookConfs(securityService.currentUserInfo());
        if (blobData != null) {
            ResponseUtils.createBlobResponse(req, resp, blobData);
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Выполняет импорт скриптов и др файлов, связанных со справочниками
     *
     * @return uuid ссылку на уведомления с результатом выполнения
     */
    @PostMapping(value = "/actions/refBookConf/import", produces = MediaType.TEXT_HTML_VALUE + "; charset=UTF-8")
    public String importRefBooks(@RequestParam("uploader") MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            return refBookService.importRefBookConfs(inputStream, file.getOriginalFilename(), securityService.currentUserInfo());
        }
    }

}
