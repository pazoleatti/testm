package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.action.Create2NdflFLAction;
import com.aplana.sbrf.taxaccounting.model.action.CreateDeclarationDataAction;
import com.aplana.sbrf.taxaccounting.model.action.CreateReportAction;
import com.aplana.sbrf.taxaccounting.model.action.CreateReportFormsAction;
import com.aplana.sbrf.taxaccounting.model.action.PrepareSubreportAction;
import com.aplana.sbrf.taxaccounting.model.dto.Declaration2NdflFLDTO;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.filter.Declaration2NdflFLFilter;
import com.aplana.sbrf.taxaccounting.model.filter.NdflFilter;
import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonFilter;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookKnfType;
import com.aplana.sbrf.taxaccounting.model.result.*;
import com.aplana.sbrf.taxaccounting.permissions.DeclarationDataFilePermission;
import com.aplana.sbrf.taxaccounting.permissions.DeclarationDataFilePermissionSetter;
import com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission;
import com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermissionSetter;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.server.PDFImageUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.CharEncoding.UTF_8;

/**
 * Контроллер для работы с декларациями
 */
@RestController
public class DeclarationDataController {
    private static final int DEFAULT_IMAGE_RESOLUTION = 150;

    private DeclarationDataService declarationService;
    private SecurityService securityService;
    private ReportService reportService;
    private BlobDataService blobDataService;
    private DeclarationTemplateService declarationTemplateService;
    private DeclarationDataFilePermissionSetter declarationDataFilePermissionSetter;
    private DeclarationDataPermissionSetter declarationDataPermissionSetter;

    public DeclarationDataController(DeclarationDataService declarationService, SecurityService securityService, ReportService reportService,
                                     BlobDataService blobDataService, DeclarationTemplateService declarationTemplateService,
                                     DeclarationDataFilePermissionSetter declarationDataFilePermissionSetter,
                                     DeclarationDataPermissionSetter declarationDataPermissionSetter) {
        this.declarationService = declarationService;
        this.securityService = securityService;
        this.reportService = reportService;
        this.blobDataService = blobDataService;
        this.declarationTemplateService = declarationTemplateService;
        this.declarationDataFilePermissionSetter = declarationDataFilePermissionSetter;
        this.declarationDataPermissionSetter = declarationDataPermissionSetter;
    }

    /**
     * Привязка данных из параметров запроса
     *
     * @param binder спец. DataBinder для привязки
     */
    @InitBinder
    public void init(ServletRequestDataBinder binder) {
        binder.registerCustomEditor(PagingParams.class, new RequestParamEditor(PagingParams.class));
        binder.registerCustomEditor(DeclarationDataFilter.class, new RequestParamEditor(DeclarationDataFilter.class));
        binder.registerCustomEditor(NdflPersonFilter.class, new RequestParamEditor(NdflPersonFilter.class));
        binder.registerCustomEditor(DeclarationSubreport.class, new RequestParamEditor(DeclarationSubreport.class));
        binder.registerCustomEditor(LogBusiness.class, new RequestParamEditor(LogBusiness.class));
        binder.registerCustomEditor(DataRow.class, new RequestParamEditor(DataRow.class));
        binder.registerCustomEditor(Cell.class, new RequestParamEditor(Cell.class));
        binder.registerCustomEditor(RefBookKnfType.class, new RequestParamEditor(RefBookKnfType.class));
        binder.registerCustomEditor(Ndfl2_6DataReportParams.class, new RequestParamEditor(Ndfl2_6DataReportParams.class));
        binder.registerCustomEditor(NdflFilter.class, new RequestParamEditor(NdflFilter.class));
        binder.registerCustomEditor(Declaration2NdflFLFilter.class, new RequestParamEditor(Declaration2NdflFLFilter.class));
    }

    /**
     * Формирование отчета для декларации в формате xlsx
     *
     * @param declarationDataId идентификатор декларации
     * @param response          ответ
     * @throws IOException IOException
     */
    @GetMapping(value = "/rest/declarationData/{declarationDataId}/xlsx", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void downloadDeclarationXlsx(@PathVariable long declarationDataId, HttpServletRequest req, HttpServletResponse response)
            throws IOException {
        String blobId = reportService.getReportFileUuidSafe(declarationDataId, DeclarationReportType.EXCEL_DEC);
        createBlobResponse(blobId, req, response);
    }

    /**
     * Формирование отчета для налоговой формы в формате pdf
     *
     * @param declarationDataId идентификатор декларации
     * @param response          ответ
     * @throws IOException IOException
     */
    @GetMapping(value = "/rest/declarationData/{declarationDataId}/pdf", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void downloadDeclarationPdf(@PathVariable long declarationDataId, HttpServletRequest req, HttpServletResponse response)
            throws IOException {
        String blobId = reportService.getReportFileUuidSafe(declarationDataId, DeclarationReportType.PDF_DEC);
        BlobData blobData = null;
        if (blobId != null) {
            blobData = blobDataService.get(blobId);
        }
        if (blobData != null) {
            TAUserInfo userInfo = securityService.currentUserInfo();
            blobData.setName(declarationService.createPdfFileName(declarationDataId, userInfo));
        }
        createBlobResponse(blobData, req, response);
    }

    /**
     * Сохранить шаблон ТФ (Excel) для формы
     *
     * @param declarationDataId идентификатор декларации
     * @param response          ответ
     * @throws IOException IOException
     */
    @GetMapping(value = "/rest/declarationData/{declarationDataId}/excelTemplate", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void downloadDeclarationExcelTemplate(@PathVariable long declarationDataId, HttpServletRequest req, HttpServletResponse response)
            throws IOException {
        String blobId = reportService.getReportFileUuidSafe(declarationDataId, DeclarationReportType.EXCEL_TEMPLATE_DEC);
        createBlobResponse(blobId, req, response);
    }

    private void createBlobResponse(String blobId, HttpServletRequest req, HttpServletResponse response) throws IOException {
        BlobData blobData = null;
        if (blobId != null) {
            blobData = blobDataService.get(blobId);
        }
        createBlobResponse(blobData, req, response);
    }

    private void createBlobResponse(BlobData blobData, HttpServletRequest req, HttpServletResponse response) throws IOException {
        if (blobData != null) {
            ResponseUtils.createBlobResponse(req, response, blobData);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Формирование специфичного отчета для декларации
     *
     * @param alias             тип специфичного отчета
     * @param declarationDataId идентификатор декларации
     * @param response          ответ
     * @throws IOException IOException
     */
    @GetMapping(value = "/rest/declarationData/{declarationDataId}/specific/{alias}")
    public void downloadDeclarationSpecific(@PathVariable String alias, @PathVariable long declarationDataId, HttpServletResponse response)
            throws IOException {
        TAUserInfo userInfo = securityService.currentUserInfo();
        DeclarationReportType ddReportType = DeclarationReportType.createSpecificReport();
        DeclarationData declaration = declarationService.get(declarationDataId, userInfo);
        ddReportType.setSubreport(declarationTemplateService.getSubreportByAlias(declaration.getDeclarationTemplateId(), alias));

        String uuid = reportService.getReportFileUuidSafe(declarationDataId, ddReportType);
        if (uuid != null) {
            BlobData blobData = blobDataService.get(uuid);

            String fileName = URLEncoder.encode(blobData.getName(), UTF_8);
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader("Content-Disposition", "attachment; filename=\""
                    + fileName + "\"");

            DataInputStream in = new DataInputStream(blobData.getInputStream());
            OutputStream out = response.getOutputStream();
            int count;
            try {
                count = IOUtils.copy(in, out);
            } finally {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }
            response.setContentLength(count);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }


    /**
     * Формирует изображение для просмотра делкарации в PDFViewer
     *
     * @param declarationDataId идентификатор декларации
     * @param pageId            идентификатор страницы
     * @param response          ответ
     * @throws IOException IOException
     */
    @GetMapping(value = "/rest/declarationData/{declarationDataId}/pageImage/{pageId}/*", produces = MediaType.IMAGE_PNG_VALUE)
    public void getPageImage(@PathVariable int declarationDataId, @PathVariable int pageId,
                             HttpServletResponse response) throws IOException {

        InputStream pdfData = declarationService.getPdfDataAsStream(declarationDataId, securityService.currentUserInfo());
        OutputStream out = response.getOutputStream();
        PDFImageUtils.pDFPageToImage(pdfData, response.getOutputStream(),
                pageId, "png", DEFAULT_IMAGE_RESOLUTION);
        IOUtils.closeQuietly(pdfData);
        IOUtils.closeQuietly(out);
    }

    /**
     * Получить количество страниц в отчете
     *
     * @param declarationDataId идентификатор декларации
     */
    @GetMapping(value = "/actions/declarationData/{declarationDataId}/pageCount")
    public Integer getPageImage(@PathVariable int declarationDataId) {
        InputStream pdfData = declarationService.getPdfDataAsStream(declarationDataId, securityService.currentUserInfo());
        Integer result = PDFImageUtils.getPageNumber(pdfData);
        IOUtils.closeQuietly(pdfData);
        return result;
    }

    /**
     * Формирование отчета для декларации в формате xml
     *
     * @param declarationDataId идентификатор декларации
     * @param response          ответ
     * @throws IOException IOException
     */
    @GetMapping(value = "/rest/declarationData/{declarationDataId}/xml", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void downloadDeclarationXml(@PathVariable int declarationDataId, HttpServletResponse response)
            throws IOException {

        InputStream xmlDataIn = declarationService.getXmlDataAsStream(declarationDataId, securityService.currentUserInfo());
        OutputStream out = response.getOutputStream();
        String fileName = URLEncoder.encode(declarationService.getXmlDataFileName(declarationDataId, securityService.currentUserInfo()), UTF_8);

        response.setHeader("Content-Disposition", "attachment; filename=\""
                + fileName + "\"");
        try {
            IOUtils.copy(xmlDataIn, response.getOutputStream());
        } finally {
            IOUtils.closeQuietly(xmlDataIn);
            IOUtils.closeQuietly(out);
        }
    }

    /**
     * Формирует DeclarationResult
     *
     * @param declarationDataId идентификатор декларации
     * @return модель {@link DeclarationResult}, в которой содержаться данные о декларации
     */
    @GetMapping(value = "/rest/declarationData/{declarationDataId}", params = "projection=declarationData")
    public DeclarationResult fetchDeclarationData(@PathVariable long declarationDataId) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        DeclarationResult declarationResult = declarationService.fetchDeclarationData(userInfo, declarationDataId);
        if (declarationResult.isDeclarationDataExists()) {
            DeclarationData declarationData = declarationService.get(declarationDataId, userInfo);
            declarationDataPermissionSetter.setPermissions(declarationData, null);
            declarationResult.setPermissions(declarationData.getPermissions());
        }
        return declarationResult;
    }

    /**
     * Импорт данных из excel в форму
     *
     * @param declarationDataId Идентификатор налоговой формы
     * @return Результат запуска задачи
     */
    @PostMapping(value = "/actions/declarationData/{declarationDataId}/import")
    public ActionResult importExcel(@RequestParam(value = "uploader") MultipartFile file,
                                    @PathVariable int declarationDataId)
            throws IOException {
        if (file.isEmpty()) {
            throw new ServiceException("Ошибка при загрузке файла \"" + file.getOriginalFilename() + "\". Выбранный файл пуст.");
        }
        TAUserInfo userInfo = securityService.currentUserInfo();
        try (InputStream inputStream = file.getInputStream()) {
            return declarationService.createTaskToImportExcel(declarationDataId, file.getOriginalFilename(), inputStream, file.getSize(), userInfo);
        }
    }

    /**
     * Удаление налоговых форм
     *
     * @param declarationDataIds Идентификаторы налоговых форм
     * @return Модель {@link ActionResult}, в которой содержаться данные о результате операции
     */
    @PostMapping(value = "/actions/declarationData/delete")
    public ActionResult deleteDeclarations(@RequestBody List<Long> declarationDataIds) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.createDeleteDeclarationDataTask(userInfo, declarationDataIds);
    }

    /**
     * Создание налоговой формы
     *
     * @param action параметры создания формы
     * @return Результат создания
     */
    @PostMapping(value = "/actions/declarationData/create")
    public CreateResult<Long> createDeclaration(CreateDeclarationDataAction action) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.create(userInfo, action);
    }

    /**
     * Создание отчетной формы
     *
     * @param action параметры создания отчетности
     * @return модель {@link CreateDeclarationReportResult}, в которой содержаться данные результате операции создания
     */
    @PostMapping(value = "/actions/declarationData/createReportForm")
    public ActionResult createReportForm(@RequestBody CreateReportFormsAction action) {
        return declarationService.createReportsCreateTask(action, securityService.currentUserInfo());
    }

    /**
     * Создание налоговой формы
     *
     * @param action параметры создания формы
     * @return Результат создания
     */
    @PostMapping(value = "/actions/declarationData/create2NdflFL")
    public ActionResult create2NdflFL(@RequestBody Create2NdflFLAction action) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.create2NdflFL(userInfo, action);
    }

    /**
     * Вернуть в создана список налоговых форм
     *
     * @param declarationDataIds Идентификаторы налоговых форм
     * @return Модель {@link ActionResult}, в которой содержаться данные о результате операции
     */
    @PostMapping(value = "/actions/declarationData/returnToCreated")
    public ResponseEntity returnToCreatedDeclaration(@RequestBody List<Long> declarationDataIds, @RequestParam String reason) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        declarationService.cancelDeclarationList(declarationDataIds, reason, userInfo);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Идентификация ФЛ налоговых форм
     *
     * @param declarationDataIds Идентификаторы налоговых форм
     * @return Модель {@link ActionResult}, в которой содержатся данные о результате операции
     */
    @PostMapping(value = "/actions/declarationData/identify")
    public ActionResult identifyDeclarationList(@RequestBody Long[] declarationDataIds) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.createIdentifyDeclarationDataTask(userInfo, Arrays.asList(declarationDataIds));
    }

    /**
     * Консолидация списка налоговых форм
     *
     * @param declarationDataIds Идентификаторы налоговых форм
     * @return Модель {@link ActionResult}, в которой содержатся данные о результате операции
     */
    @PostMapping(value = "/actions/declarationData/consolidate")
    public ActionResult consolidateDeclarationList(@RequestBody Long[] declarationDataIds) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.consolidateDeclarationDataList(userInfo, Arrays.asList(declarationDataIds));
    }

    /**
     * Проверить декларацию
     *
     * @param declarationDataId идентификатор декларации
     * @return модель , в которой содержаться данные о результате проверки декларации
     */
    @PostMapping(value = "/rest/declarationData/{declarationDataId}/check", produces = MediaType.APPLICATION_JSON_VALUE)
    public ActionResult checkDeclaration(@PathVariable long declarationDataId) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.createCheckDeclarationDataTask(userInfo, Collections.singletonList(declarationDataId));
    }

    /**
     * Проверить налоговые формы
     *
     * @param declarationDataIds Идентификаторы налоговых форм
     * @return Модель {@link ActionResult}, в которой содержатся данные о результате операции
     */
    @PostMapping(value = "/actions/declarationData/check")
    public ActionResult checkDeclaration(@RequestBody List<Long> declarationDataIds) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.createCheckDeclarationDataTask(userInfo, declarationDataIds);
    }

    /**
     * Принять НФ
     */
    @PostMapping(value = "/rest/declarationData/{declarationDataId}/accept", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ActionResult accept(@PathVariable final long declarationDataId) {
        return declarationService.createAcceptDeclarationDataTask(securityService.currentUserInfo(), Collections.singletonList(declarationDataId));
    }

    /**
     * Принять список налоговых форм
     *
     * @param declarationDataIds Идентификаторы налоговых форм
     * @return Модель {@link ActionResult}, в которой содержатся данные о результате операции
     */
    @PostMapping(value = "/actions/declarationData/accept")
    public ActionResult acceptDeclarationList(@RequestBody List<Long> declarationDataIds) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.createAcceptDeclarationDataTask(userInfo, declarationDataIds);
    }

    /**
     * Получение дополнительной информации о файлах декларации с комментариями
     *
     * @param declarationDataId идентификатор декларации
     * @return объект модели {@link DeclarationDataFileComment}, в которой содержаться данные о файлах
     * и комментарий для текущей декларации.
     */
    @GetMapping(value = "/rest/declarationData/{declarationDataId}", params = "projection=filesComments")
    public DeclarationDataFileComment fetchFilesComments(@PathVariable long declarationDataId) {
        DeclarationDataFileComment result = declarationService.fetchFilesComments(declarationDataId);
        if (!result.getDeclarationDataFiles().isEmpty()) {
            declarationDataFilePermissionSetter.setPermissions(result.getDeclarationDataFiles(), DeclarationDataFilePermission.DELETE);
        }
        return result;
    }

    /**
     * Сохранение дополнительной информации о файлах декларации с комментариями
     *
     * @param dataFileComment сохраняемый объект декларации, в котором содержатся данные о файлах и комментарий для текущей декларации.
     * @return новый объект модели {@link DeclarationDataFileComment}, в котором содержатся данные
     * о файлах и комментарий для текущей декларации.
     */
    @PostMapping(value = "/rest/declarationData", params = "projection=filesComments")
    public DeclarationDataFileComment updateDeclarationFilesAndComments(@RequestBody DeclarationDataFileComment dataFileComment) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.updateDeclarationFilesComments(dataFileComment, userInfo);
    }

    /**
     * Получение источников и приемников декларации
     *
     * @param declarationDataId идентификатор декларации
     * @return источники и приемники декларации {@link Relation}
     */
    @GetMapping(value = "/rest/declarationData/{declarationDataId}", params = "projection=sources")
    public List<Relation> getDeclarationSourcesAndDestinations(@PathVariable long declarationDataId) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.getDeclarationSourcesAndDestinations(userInfo, declarationDataId);
    }

    /**
     * Получение списка налоговых форм
     *
     * @param pagingParams параметры для пагинации
     * @return список налоговых форм {@link DeclarationDataJournalItem}
     */
    @GetMapping(value = "/rest/declarationData", params = "projection=declarations")
    public JqgridPagedList<DeclarationDataJournalItem> fetchDeclarations(@RequestParam DeclarationDataFilter filter, @RequestParam PagingParams pagingParams) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        PagingResult<DeclarationDataJournalItem> pagingResult = declarationService.fetchDeclarations(userInfo, filter, pagingParams);

        setDeclarationDataJournalItemsPermissions(pagingResult);

        return JqgridPagedResourceAssembler.buildPagedList(
                pagingResult,
                pagingResult.getTotalCount(),
                pagingParams
        );
    }

    /**
     * Возвращяет список форм 2-НДФЛ (ФЛ) по фильтру и пагинации
     */
    @GetMapping(value = "/rest/declarationData", params = "projection=2ndflFLDeclarations")
    public JqgridPagedList<Declaration2NdflFLDTO> findAll2NdflFL(@RequestParam Declaration2NdflFLFilter filter, @RequestParam PagingParams pagingParams) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        PagingResult<Declaration2NdflFLDTO> pagingResult = declarationService.findAll2NdflFL(filter, pagingParams);

        return JqgridPagedResourceAssembler.buildPagedList(
                pagingResult,
                pagingResult.getTotalCount(),
                pagingParams
        );
    }

    /**
     * Установка прав доступа для всех налоговых форм страницы
     *
     * @param page Страница списка налоговых форм
     */
    private void setDeclarationDataJournalItemsPermissions(PagingResult<DeclarationDataJournalItem> page) {
        if (!page.isEmpty()) {
            //Получение id всех форм
            List<Long> declarationIds = new ArrayList<>();
            for (DeclarationDataJournalItem item : page) {
                declarationIds.add(item.getDeclarationDataId());
            }

            //Сохранение в мапе для получения формы по id
            Map<Long, DeclarationData> declarationDataMap = new HashMap<>();
            for (DeclarationData declarationData : declarationService.get(declarationIds)) {
                declarationDataMap.put(declarationData.getId(), declarationData);
            }

            //Для каждого элемента страницы взять форму, определить права доступа на нее и установить их элементу страницы
            for (DeclarationDataJournalItem item : page) {
                DeclarationData declaration = declarationDataMap.get(item.getDeclarationDataId());
                if (declaration != null) {//noinspection unchecked
                    declarationDataPermissionSetter.setPermissions(declaration, DeclarationDataPermission.VIEW,
                            DeclarationDataPermission.DELETE, DeclarationDataPermission.RETURN_TO_CREATED,
                            DeclarationDataPermission.ACCEPTED, DeclarationDataPermission.CHECK,
                            DeclarationDataPermission.CREATE, DeclarationDataPermission.EDIT_ASSIGNMENT, DeclarationDataPermission.DOWNLOAD_REPORTS,
                            DeclarationDataPermission.IDENTIFY, DeclarationDataPermission.CONSOLIDATE);
                    item.setPermissions(declaration.getPermissions());
                }
            }
        }
    }

    /**
     * Установить блокировку на раздел "Файлы и комментарии" формы.
     *
     * @param declarationDataId идентификатор декларации
     * @return результат операции
     */
    @PostMapping(value = "/actions/declarationData/{declarationDataId}/lockFilesAndComments")
    public ActionResult lockFilesAndComments(@PathVariable("declarationDataId") long declarationDataId) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.lock(declarationDataId, OperationType.EDIT_FILE, userInfo);
    }

    /**
     * Установить блокировку на редактирование формы.
     *
     * @param declarationDataId идентификатор декларации
     * @return результат операции
     */
    @PostMapping(value = "/actions/declarationData/{declarationDataId}/lockEdit")
    public ActionResult lockEdit(@PathVariable("declarationDataId") long declarationDataId) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.lock(declarationDataId, OperationType.EDIT, userInfo);
    }

    /**
     * Снять блокировку с раздела "Файлы и комментарии" формы.
     *
     * @param declarationDataId идентификатор декларации
     * @return результат операции
     */
    @PostMapping(value = "/actions/declarationData/{declarationDataId}/unlockFilesAndComments")
    public ActionResult unlockFilesAndComments(@PathVariable("declarationDataId") long declarationDataId) {
        return declarationService.unlock(declarationDataId, OperationType.EDIT_FILE);
    }

    /**
     * Снять блокировку с редактирования формы.
     *
     * @param declarationDataId идентификатор декларации
     * @return результат операции
     */
    @PostMapping(value = "/actions/declarationData/{declarationDataId}/unlockEdit")
    public ActionResult unlockEdit(@PathVariable("declarationDataId") long declarationDataId) {
        return declarationService.unlock(declarationDataId, OperationType.EDIT);
    }

    /**
     * Возвращает признак существования формы и, если она существует, ее тип
     *
     * @param declarationDataId ид формы
     */
    @GetMapping(value = "/rest/declarationData/{declarationDataId}", params = "projection=existenceAndKind")
    public DeclarationDataExistenceAndKindResult fetchDeclarationDataExistenceAndKind(@PathVariable long declarationDataId) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.fetchDeclarationDataExistenceAndKind(userInfo, declarationDataId);
    }

    /**
     * Возвращает данные о наличии отчетов ПНФ и КНФ
     *
     * @param declarationDataId Идентификатор формы
     * @return данные о наличии отчетов
     */
    @GetMapping(value = "/rest/declarationData/{declarationDataId}", params = "projection=availableReports")
    public ReportAvailableResult checkAvailabilityReports(@PathVariable long declarationDataId) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.checkAvailabilityReports(userInfo, declarationDataId);
    }

    /**
     * Возвращает данные о наличии отчетов
     *
     * @param declarationDataId идентификатор декларации
     * @return данные о наличии отчетов
     */
    @GetMapping(value = "/rest/declarationData/{declarationDataId}", params = "projection=availableNdflReports")
    public ReportAvailableReportDDResult checkAvailabilityNdflReports(@PathVariable long declarationDataId) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.checkAvailabilityReportDD(userInfo, declarationDataId);
    }

    /**
     * Выгрузка отчетности по списку ид форм
     */
    @PostMapping(value = "/actions/declarationData/exportReportForms")
    public ActionResult exportReportForms(@RequestBody List<Long> declarationDataIds) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.asyncExportReports(declarationDataIds, userInfo);
    }

    /**
     * Выгрузка отчетности по фильтру
     */
    @PostMapping(value = "/actions/declarationData/exportReportFormsByFilter")
    public ActionResult exportReportFormsByFilter(@RequestParam DeclarationDataFilter filter) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.asyncExportReports(filter, userInfo);
    }

    /**
     * Изменение состояния ЭД по списку ид форм
     */
    @PostMapping(value = "/actions/declarationData/updateDocState")
    public ActionResult updateDocState(@RequestBody List<Long> declarationDataIds, @RequestParam long docStateId) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.createTaskToUpdateDocState(declarationDataIds, docStateId, userInfo);
    }

    /**
     * Изменение состояния ЭД по фильтру форм
     */
    @PostMapping(value = "/actions/declarationData/updateDocStateByFilter")
    public ActionResult updateDocState(@RequestParam DeclarationDataFilter filter, @RequestParam long docStateId) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.createTaskToUpdateDocState(filter, docStateId, userInfo);
    }

    /**
     * Отправить в ЭДО по списку ид форм
     */
    @PostMapping(value = "/actions/declarationData/sendEdo")
    public ActionResult sendEdo(@RequestBody List<Long> declarationDataIds) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.createTaskToSendEdo(declarationDataIds, userInfo);
    }

    /**
     * Отправить в ЭДО по фильтру форм
     */
    @PostMapping(value = "/actions/declarationData/sendEdoByFilter")
    public ActionResult sendEdoByFilter(@RequestParam DeclarationDataFilter filter) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.createTaskToSendEdo(filter, userInfo);
    }

    /**
     * Формирование спецотчета по физ лицу для 2-НДФЛ
     */
    @PostMapping(value = "/actions/declarationData/{declarationDataId}/reportNdflByPerson")
    public ActionResult createReportNdflByPersonReport(@PathVariable("declarationDataId") long declarationDataId, @RequestBody CreateReportAction action) {
        return declarationService.createReportNdflByPersonReport(declarationDataId, action, securityService.currentUserInfo());
    }

    /**
     * Подготовить данные для спецотчета. Используется для получения списка ФЛ, для выбора одного из них для
     * создания спецотчета.
     */
    @PostMapping(value = "/rest/declarationData/prepareSpecificReport", consumes = MediaType.APPLICATION_JSON_VALUE)
    public PrepareSubreportResult prepareSubreport(@RequestBody PrepareSubreportAction action) {
        return declarationService.prepareSubreport(securityService.currentUserInfo(), action);
    }

    /**
     * Обновляет данные строки для раздела 2 (Сведения о доходах и НДФЛ)
     *
     * @param declarationDataId идентификатор формы, строка которой редактируется
     * @param personIncome      измененные данные строки
     */
    @PostMapping(value = "/rest/declarationData/{declarationDataId}/editNdflIncomesAndTax", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ActionResult editNdflIncomesAndTax(@PathVariable Long declarationDataId, @RequestBody NdflPersonIncomeDTO personIncome) {
        return declarationService.updateNdflIncomesAndTax(declarationDataId, securityService.currentUserInfo(), personIncome);
    }

    /**
     * Обновляет данные строки для раздела 3 (Сведения о вычетах)
     *
     * @param declarationDataId идентификатор формы, строка которой редактируется
     * @param personDeduction   измененные данные строки
     */
    @PostMapping(value = "/rest/declarationData/{declarationDataId}/editNdflDeduction", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ActionResult editNdflDeduction(@PathVariable Long declarationDataId, @RequestBody NdflPersonDeductionDTO personDeduction) {
        return declarationService.updateNdflDeduction(declarationDataId, securityService.currentUserInfo(), personDeduction);
    }

    /**
     * Обновляет данные строки для раздела 4 (Сведения о доходах в виде авансовых платежей)
     *
     * @param declarationDataId идентификатор формы, строка которой редактируется
     * @param personPrepayment  измененные данные строки
     */
    @PostMapping(value = "/rest/declarationData/{declarationDataId}/editNdflPrepayment", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ActionResult editNdflPrepayment(@PathVariable Long declarationDataId, @RequestBody NdflPersonPrepaymentDTO personPrepayment) {
        return declarationService.updateNdflPrepayment(declarationDataId, securityService.currentUserInfo(), personPrepayment);
    }

    /**
     * Массовое редактирование дат в строках раздела 2.
     */
    @PostMapping("/rest/declarationData/{declarationDataId}/editNdflIncomeDates")
    public ActionResult editNdflIncomeDates(@PathVariable Long declarationDataId, @RequestBody NdflPersonIncomeDatesDTO incomeDates) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.updateNdflIncomeDates(declarationDataId, userInfo, incomeDates);
    }

    /**
     * Массовое редактирование дат в строках раздела 2, подходящих под переданный фильтр.
     */
    @PostMapping("/rest/declarationData/{declarationDataId}/editNdflIncomeDatesByFilter")
    public ActionResult editNdflIncomeDatesByFilter(@PathVariable Long declarationDataId, @RequestBody NdflPersonIncomeDatesDTO incomeDates, @RequestParam NdflFilter filter) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.updateNdflIncomeDatesByFilter(declarationDataId, userInfo, incomeDates, filter);
    }

    /**
     * Обновляет данные ФЛ КНФ
     *
     * @param declarationDataId иднтификатор формы данные которые обновляются
     * @return строка с uuid уведомлений об обновлении данных ФЛ КНФ
     */
    @GetMapping(value = "/actions/declarationData/{declarationDataId}/updatePersonsData", produces = MediaType.TEXT_HTML_VALUE)
    public String updatePersonData(@PathVariable long declarationDataId) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.createUpdatePersonsDataTask(declarationDataId, userInfo);
    }

    /**
     * Загрузка на сервер файла
     *
     * @param file файл
     * @return строка с uuid
     * @throws IOException в случае исключения при работе с потоками/файлами
     */
    @PostMapping(value = "/actions/declarationData/uploadFile", produces = MediaType.TEXT_HTML_VALUE + "; charset=UTF-8")
    public String uploadFile(@RequestParam("uploader") MultipartFile file, @RequestParam Long declarationDataId) throws IOException, JSONException {
        if (file.isEmpty()) {
            throw new ServiceException("Файл пустой.");
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(UuidEnum.UUID.toString(), declarationService.uploadFile(file.getInputStream(), file.getOriginalFilename(), declarationDataId));
        return jsonObject.toString();
    }

    /**
     * Загрузка на сервер файла
     *
     * @throws IOException в случае исключения при работе с потоками/файлами
     */
    @GetMapping(value = "/actions/declarationData/{declarationDataId}/download/{uuid}")
    public void downloadFile(@PathVariable("uuid") String uuid, @PathVariable("declarationDataId") Long declarationDataId, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        DeclarationDataFile stub = new DeclarationDataFile();
        stub.setDeclarationDataId(declarationDataId);
        stub.setUuid(uuid);
        BlobData blobData = declarationService.downloadFile(stub);
        if (blobData != null) {
            ResponseUtils.createBlobResponse(req, resp, blobData);
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Адрес для проверки параметра "Максимальное число строк формы РНУ для редактирования дат"
     *
     * @param count проверяемое число
     * @return ActionResult с результатом проверки и логами
     */
    @GetMapping("/actions/checkRowsEditCountParam")
    public ActionResult checkRowsEditCountParam(@RequestParam int count) {
        return declarationService.checkRowsEditCountParam(count);
    }

    /**
     * Формирование отчета в xlsx
     *
     * @param declarationDataId идентификатор декларации
     * @return информация о создании отчета
     */
    @PostMapping(value = "/actions/declarationData/{declarationDataId}/reportXsls")
    public String createDeclarationReportXlsx(@PathVariable("declarationDataId") long declarationDataId) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.createTaskToCreateReportXlsx(userInfo, declarationDataId);
    }

    /**
     * Формирование спецотчета по физ лицу для РНУ НДФЛ
     *
     * @param declarationDataId идентификатор декларации
     * @param ndflPersonId      идентификатор данных о физическом лице {@link com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson}
     * @return источники и приемники декларации
     */
    @PostMapping(value = "/actions/declarationData/{declarationDataId}/rnuNdflByPerson")
    public String createRnuNdflByPersonReport(@PathVariable("declarationDataId") long declarationDataId, @RequestParam long ndflPersonId) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        Map<String, Object> reportParams = new HashMap<>();
        reportParams.put("PERSON_ID", ndflPersonId);
        return declarationService.createTaskToCreateSpecificReport(declarationDataId, SubreportAliasConstants.RNU_NDFL_PERSON_DB, reportParams, userInfo);
    }

    /**
     * Формирование спецотчета
     *
     * @param declarationDataId идентификатор декларации
     */
    @PostMapping(value = "/actions/declarationData/{declarationDataId}/specific/{alias}")
    public String createSpecificReport(@PathVariable("declarationDataId") long declarationDataId, @PathVariable String alias,
                                       @RequestBody(required = false) Ndfl2_6DataReportParams params) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        Map<String, Object> reportParams = new HashMap<>();
        reportParams.put("params", params);
        return declarationService.createTaskToCreateSpecificReport(declarationDataId, alias, reportParams, userInfo);
    }

    /**
     * Формирует шаблон ТФ (Excel) для формы
     *
     * @param declarationDataId Идентификатор налоговой формы
     * @return Результат запуска задачи
     */
    @PostMapping(value = "/actions/declarationData/{declarationDataId}/excelTemplate")
    public String createExcelTemplate(@PathVariable int declarationDataId) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.createTaskToCreateExcelTemplate(declarationDataId, userInfo);
    }

    @PostMapping(value = "/actions/declarationData/{declarationDataId}/pdf")
    public String createPdfReport(@PathVariable("declarationDataId") long declarationDataId) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.createPdfTask(userInfo, declarationDataId);
    }


    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity handleAccessDeniedException() {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
}