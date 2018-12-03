package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.action.*;
import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonFilter;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookKnfType;
import com.aplana.sbrf.taxaccounting.model.result.*;
import com.aplana.sbrf.taxaccounting.permissions.DeclarationDataFilePermission;
import com.aplana.sbrf.taxaccounting.permissions.DeclarationDataFilePermissionSetter;
import com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission;
import com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermissionSetter;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.model.LogBusinessModel;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.*;

import static org.apache.commons.lang3.CharEncoding.UTF_8;

/**
 * Контроллер для работы с декларациями
 */
@RestController
public class DeclarationDataController {
    public static final int DEFAULT_IMAGE_RESOLUTION = 150;

    private DeclarationDataService declarationService;
    private SecurityService securityService;
    private ReportService reportService;
    private BlobDataService blobDataService;
    private DeclarationTemplateService declarationTemplateService;
    private LogBusinessService logBusinessService;
    private TAUserService taUserService;
    private DeclarationDataFilePermissionSetter declarationDataFilePermissionSetter;
    private DeclarationDataPermissionSetter declarationDataPermissionSetter;

    public DeclarationDataController(DeclarationDataService declarationService, SecurityService securityService, ReportService reportService,
                                     BlobDataService blobDataService, DeclarationTemplateService declarationTemplateService, LogBusinessService logBusinessService,
                                     TAUserService taUserService, DeclarationDataFilePermissionSetter declarationDataFilePermissionSetter,
                                     DeclarationDataPermissionSetter declarationDataPermissionSetter) {
        this.declarationService = declarationService;
        this.securityService = securityService;
        this.reportService = reportService;
        this.blobDataService = blobDataService;
        this.declarationTemplateService = declarationTemplateService;
        this.logBusinessService = logBusinessService;
        this.taUserService = taUserService;
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
        String blobId = reportService.getReportFileUuidSafe(declarationDataId, DeclarationDataReportType.EXCEL_DEC);
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
        String blobId = reportService.getReportFileUuidSafe(declarationDataId, DeclarationDataReportType.PDF_DEC);
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
        String blobId = reportService.getReportFileUuidSafe(declarationDataId, DeclarationDataReportType.EXCEL_TEMPLATE_DEC);
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
        DeclarationDataReportType ddReportType = DeclarationDataReportType.getDDReportTypeByName(alias);
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
     * @throws IOException IOException
     */
    @GetMapping(value = "/actions/declarationData/{declarationDataId}/pageCount")
    public Integer getPageImage(@PathVariable int declarationDataId) throws IOException {
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
     * Удаление налоговой формы
     *
     * @param declarationDataId Идентификатор налоговой формы
     */
    @PostMapping(value = "/actions/declarationData/{declarationDataId}/delete")
    public ActionResult deleteDeclaration(@PathVariable int declarationDataId) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.deleteIfExists(declarationDataId, userInfo);
    }

    /**
     * Формирует шаблон ТФ (Excel) для формы
     *
     * @param declarationDataId Идентификатор налоговой формы
     * @return Результат запуска задачи
     */
    @PostMapping(value = "/actions/declarationData/{declarationDataId}/excelTemplate")
    public CreateDeclarationExcelTemplateResult createExcelTemplate(@PathVariable int declarationDataId,
                                                                    @RequestParam boolean force) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.createTaskToCreateExcelTemplate(declarationDataId, userInfo, force);
    }

    /**
     * Импорт данных из excel в форму
     *
     * @param declarationDataId Идентификатор налоговой формы
     * @return Результат запуска задачи
     */
    @PostMapping(value = "/actions/declarationData/{declarationDataId}/import")
    public ImportDeclarationExcelResult importExcel(@RequestParam(value = "uploader") MultipartFile file,
                                                    @RequestParam boolean force,
                                                    @PathVariable int declarationDataId)
            throws IOException {
        TAUserInfo userInfo = securityService.currentUserInfo();
        try (InputStream inputStream = file.getInputStream()) {
            return declarationService.createTaskToImportExcel(declarationDataId, file.getOriginalFilename(), inputStream, userInfo, force);
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
        return declarationService.deleteDeclarationList(userInfo, declarationDataIds);
    }

    /**
     * Создание отчётности
     *
     * @param action параметры создания отчетности
     * @return модель {@link CreateDeclarationReportResult}, в которой содержаться данные результате операции создания
     */
    @PostMapping(value = "/actions/declarationData/createReport")
    public CreateDeclarationReportResult createReport(@RequestBody CreateDeclarationReportAction action) {
        return declarationService.createReports(action, securityService.currentUserInfo());
    }

    /**
     * Создание налоговой формы
     *
     * @param action
     * @return Результат создания
     */
    @PostMapping(value = "/actions/declarationData/create")
    public CreateResult<Long> createDeclaration(CreateDeclarationDataAction action) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.create(userInfo, action);
    }

    /**
     * Вернуть в создана
     *
     * @param declarationDataId идентификатор декларации
     */
    @PostMapping(value = "/rest/declarationData/{declarationDataId}/moveToCreated")
    public ResponseEntity returnToCreatedDeclaration(@PathVariable int declarationDataId, @RequestBody MoveToCreateAction action) {
        Logger logger = new Logger();
        declarationService.cancel(logger, declarationDataId, action.getReason(), securityService.currentUserInfo());
        return new ResponseEntity<>(HttpStatus.OK);
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
     * Идентифицировать ФЛ
     *
     * @param declarationDataId идентификатор декларации
     * @param force             признак для перезапуска задачи
     * @param cancelTask        признак для отмены задачи
     * @return модель {@link RecalculateDeclarationResult}, в которой содержаться данные о результате расчета декларации
     */
    @PostMapping(value = "/actions/declarationData/{declarationDataId}/identify")
    public RecalculateDeclarationResult identify(@PathVariable long declarationDataId, @RequestParam boolean force, @RequestParam boolean cancelTask) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.identifyDeclarationData(userInfo, declarationDataId, force, cancelTask);
    }

    /**
     * Консолидировать
     *
     * @param declarationDataId идентификатор декларации
     * @return модель {@link RecalculateDeclarationResult}, в которой содержаться данные о результате расчета декларации
     */
    @PostMapping(value = "/actions/declarationData/{declarationDataId}/consolidate")
    public RecalculateDeclarationResult recalculateDeclaration(@PathVariable long declarationDataId) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.createConsolidateDeclarationTask(userInfo, declarationDataId);
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
        return declarationService.identifyDeclarationDataList(userInfo, Arrays.asList(declarationDataIds));
    }

    /**
     * Консолидация списка налоговых форм
     *
     * @param declarationDataIds Идентификаторы налоговых форм
     * @return Модель {@link ActionResult}, в которой содержатся данные о результате операции
     */
    @PostMapping(value = "/actions/declarationData/consolidate")
    public ActionResult recalculateDeclarationList(@RequestBody Long[] declarationDataIds) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.createConsolidateDeclarationListTask(userInfo, Arrays.asList(declarationDataIds));
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
        return declarationService.checkDeclarationList(userInfo, Collections.singletonList(declarationDataId));
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
        return declarationService.checkDeclarationList(userInfo, declarationDataIds);
    }

    /**
     * Принять НФ
     *
     * @return
     */
    @PostMapping(value = "/rest/declarationData/{declarationDataId}/accept", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ActionResult accept(@PathVariable final long declarationDataId) {
        return declarationService.acceptDeclarationList(securityService.currentUserInfo(), Collections.singletonList(declarationDataId));
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
        return declarationService.acceptDeclarationList(userInfo, declarationDataIds);
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
     * @param dataFileComment сохраняемый объект декларации, в котором содержаться
     *                        данные о файлах и комментарий для текущей декларации.
     * @return новый объект модели {@link DeclarationDataFileComment}, в котором содержаться данные
     * о файлах и комментарий для текущей декларации.
     */
    @PostMapping(value = "/rest/declarationData", params = "projection=filesComments")
    public DeclarationDataFileComment saveDeclarationFilesComment(@RequestBody DeclarationDataFileComment dataFileComment) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.saveDeclarationFilesComment(userInfo, dataFileComment);
    }

    /**
     * Возвращает историю измений декларации по её идентификатору
     *
     * @param declarationDataId идентификатор декларации
     * @param pagingParams      параметры пагинации
     * @return список изменений декларации {@link LogBusinessModel}
     */
    @GetMapping(value = "/rest/declarationData/{declarationDataId}", params = "projection=businessLogs")
    public JqgridPagedList<LogBusinessModel> fetchDeclarationBusinessLogs(@PathVariable long declarationDataId, @RequestParam PagingParams pagingParams) {
        ArrayList<LogBusinessModel> logBusinessModelArrayList = new ArrayList<LogBusinessModel>();
        for (LogBusiness logBusiness : logBusinessService.getDeclarationLogsBusiness(declarationDataId, pagingParams)) {
            LogBusinessModel logBusinessModel;
            if (FormDataEvent.SAVE.getCode() == logBusiness.getEventId()) {
                logBusinessModel = new LogBusinessModel(logBusiness, FormDataEvent.DECLARATION_SAVE_EVENT_TITLE_2,
                        taUserService.getUser(logBusiness.getUserLogin()).getName());
            } else {
                logBusinessModel = new LogBusinessModel(logBusiness, (FormDataEvent.getByCode(logBusiness.getEventId())).getTitle(),
                        taUserService.getUser(logBusiness.getUserLogin()).getName());
            }
            logBusinessModelArrayList.add(logBusinessModel);
        }
        PagingResult<LogBusinessModel> result = new PagingResult<LogBusinessModel>(logBusinessModelArrayList, logBusinessModelArrayList.size());
        return JqgridPagedResourceAssembler.buildPagedList(
                result,
                result.size(),
                pagingParams
        );
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
                            DeclarationDataPermission.CREATE,
                            DeclarationDataPermission.EDIT_ASSIGNMENT, DeclarationDataPermission.DOWNLOAD_REPORTS, DeclarationDataPermission.IDENTIFY);
                    item.setPermissions(declaration.getPermissions());
                }
            }
        }
    }

    /**
     * Формирование рну ндфл для отдельного физ лица`
     *
     * @param declarationDataId идентификатор декларации
     * @param ndflPersonId      идентификатор данных о физическом лице {@link com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson}
     * @param ndflPersonFilter  заполненные поля при поиске
     * @return источники и приемники декларации
     */
    @PostMapping(value = "/actions/declarationData/{declarationDataId}/rnuDoc")
    public CreateDeclarationReportResult createReportRnu(@PathVariable("declarationDataId") long declarationDataId, @RequestParam long ndflPersonId, @RequestParam NdflPersonFilter ndflPersonFilter) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.createReportRnu(userInfo, declarationDataId, ndflPersonId, ndflPersonFilter);
    }

    /**
     * Формирование рну ндфл для всех физ лиц
     *
     * @param declarationDataId идентификатор декларации
     */
    @PostMapping(value = "/actions/declarationData/{declarationDataId}/specific/{alias}")
    public CreateDeclarationReportResult createReportAllRnus(@PathVariable("declarationDataId") long declarationDataId, @PathVariable String alias, @RequestParam boolean force) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.createTaskToCreateSpecificReport(declarationDataId, alias, userInfo, force);
    }

    /**
     * Формирование реестра сформированной отчетности
     *
     * @param declarationDataId идентификатор декларации
     * @param force             признак для перезапуска задачи
     * @return информация о создании отчета
     */
    @PostMapping(value = "/actions/declarationData/{declarationDataId}/pairKppOktmoReport")
    public CreateDeclarationReportResult createPairKppOktmo(@PathVariable("declarationDataId") long declarationDataId, @RequestParam boolean force) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.createPairKppOktmoReport(userInfo, declarationDataId, force);
    }

    /**
     * Формирование отчета в xlsx
     *
     * @param declarationDataId идентификатор декларации
     * @param force             признак для перезапуска задачи
     * @return информация о создании отчета
     */
    @PostMapping(value = "/actions/declarationData/{declarationDataId}/reportXsls")
    public CreateDeclarationReportResult CreateDeclarationReportXlsx(@PathVariable("declarationDataId") long declarationDataId, @RequestParam boolean force) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.createReportXlsx(userInfo, declarationDataId, force);
    }

    /**
     * Установить блокировку на форму
     *
     * @param declarationDataId Идентификатор формы
     * @return Удалось ли установить блокировку
     */
    @PostMapping(value = "/actions/declarationData/{declarationDataId}/lock")
    public DeclarationLockResult createLock(@PathVariable("declarationDataId") long declarationDataId) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.createLock(declarationDataId, userInfo);
    }

    /**
     * Снять блокировку с формы
     *
     * @param declarationDataId идентификатор декларации
     */
    @PostMapping(value = "/actions/declarationData/{declarationDataId}/unlock")
    public void deleteLock(@PathVariable("declarationDataId") long declarationDataId) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        declarationService.unlock(declarationDataId, userInfo);
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
    @PostMapping(value = "/actions/declarationData/downloadReports")
    public ActionResult downloadReports(@RequestBody List<Long> declarationDataIds) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.asyncExportReports(declarationDataIds, userInfo);
    }

    /**
     * Выгрузка отчетности по фильтру
     */
    @PostMapping(value = "/actions/declarationData/downloadReportsByFilter")
    public ActionResult downloadReportsByFilter(@RequestParam DeclarationDataFilter filter) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.asyncExportReports(filter, userInfo);
    }

    /**
     * Создание отчетов и спецотчетов
     */
    @PostMapping(value = "/rest/createReport", consumes = MediaType.APPLICATION_JSON_VALUE)
    public CreateReportResult createReport(@RequestBody CreateReportAction action) {
        return declarationService.createReportForReportDD(securityService.currentUserInfo(), action);
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
    public void editNdflIncomesAndTax(@PathVariable Long declarationDataId, @RequestBody NdflPersonIncomeDTO personIncome) {
        declarationService.updateNdflIncomesAndTax(declarationDataId, securityService.currentUserInfo(), personIncome);
    }

    /**
     * Обновляет данные строки для раздела 3 (Сведения о вычетах)
     *
     * @param declarationDataId идентификатор формы, строка которой редактируется
     * @param personDeduction   измененные данные строки
     */
    @PostMapping(value = "/rest/declarationData/{declarationDataId}/editNdflDeduction", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void editNdflDeduction(@PathVariable Long declarationDataId, @RequestBody NdflPersonDeductionDTO personDeduction) {
        declarationService.updateNdflDeduction(declarationDataId, securityService.currentUserInfo(), personDeduction);
    }

    /**
     * Обновляет данные строки для раздела 4 (Сведения о доходах в виде авансовых платежей)
     *
     * @param declarationDataId идентификатор формы, строка которой редактируется
     * @param personPrepayment  измененные данные строки
     */
    @PostMapping(value = "/rest/declarationData/{declarationDataId}/editNdflPrepayment", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void editNdflPrepayment(@PathVariable Long declarationDataId, @RequestBody NdflPersonPrepaymentDTO personPrepayment) {
        declarationService.updateNdflPrepayment(declarationDataId, securityService.currentUserInfo(), personPrepayment);
    }

    /**
     * Обновляет данные ФЛ КНФ
     *
     * @param declarationDataId иднтификатор формы данные которые обновляются
     * @return строка с uuid уведомлений об обновлении данных ФЛ КНФ
     */
    @GetMapping(value = "/actions/declarationData/{declarationDataId}/updatePersonsData", produces = MediaType.TEXT_HTML_VALUE)
    public String updatePersonData(@PathVariable long declarationDataId) throws JSONException {
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
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(UuidEnum.UUID.toString(), declarationService.uploadFile(file.getInputStream(), file.getOriginalFilename(), declarationDataId));
        return jsonObject.toString();
    }

    /**
     * Загрузка на сервер файла
     *
     * @return строка с uuid
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

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity handleAccessDeniedException() {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
}