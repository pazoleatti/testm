package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.model.result.RefBookConfListItem;
import com.aplana.sbrf.taxaccounting.script.service.RefBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RestController
public class RefBookConfController {

    @Autowired
    private RefBookService refBookService;

    @InitBinder
    public void init(ServletRequestDataBinder binder) {
        binder.registerCustomEditor(PagingParams.class, new RequestParamEditor(PagingParams.class));
    }

    /**
     * Возвращяет список справочников для настройщика
     */
    @GetMapping(value = "/rest/refBookConf", params = "projection=refBookConfList")
    public List<RefBookConfListItem> fetchRefBookConfList() {
        return refBookService.getRefBookConfList();
    }

    /**
     * Формирует архив со скриптами и др файлами, связанными со всеми справочниками
     *
     * @return uuid ссылку на уведомления с результатом выполнения
     */
    @GetMapping(value = "/rest/refBookConf", params = "projection=exportRefBooks")
    public ActionResult exportRefBooks() {
        return new ActionResult(refBookService.exportRefBookConfs());
    }

    /**
     * Выполняет импорт скриптов и др файлов, связанных со справочниками
     *
     * @return uuid ссылку на уведомления с результатом выполнения
     */
    @PostMapping(value = "/rest/refBookConf/import", produces = MediaType.TEXT_HTML_VALUE + "; charset=UTF-8")
    public String importRefBooks(@RequestParam("uploader") MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            return refBookService.importRefBookConfs(inputStream, file.getOriginalFilename());
        }
    }
}
