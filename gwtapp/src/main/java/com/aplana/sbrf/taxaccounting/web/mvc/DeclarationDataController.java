package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.action.*;
import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonFilter;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.result.*;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.model.LogBusinessModel;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.server.GetDeclarationDataHandler;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.CheckDeclarationDataResult;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.server.PDFImageUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang3.CharEncoding.UTF_8;

/**
 * Контроллер для работы с декларациями
 */
@RestController
public class DeclarationDataController {
    private DeclarationDataService declarationService;
    private SecurityService securityService;
    private ReportService reportService;
    private BlobDataService blobDataService;
    private DeclarationTemplateService declarationTemplateService;
    private LogBusinessService logBusinessService;
    private TAUserService taUserService;

    public DeclarationDataController(DeclarationDataService declarationService, SecurityService securityService, ReportService reportService,
                                     BlobDataService blobDataService, DeclarationTemplateService declarationTemplateService, LogBusinessService logBusinessService,
                                     TAUserService taUserService) {
        this.declarationService = declarationService;
        this.securityService = securityService;
        this.reportService = reportService;
        this.blobDataService = blobDataService;
        this.declarationTemplateService = declarationTemplateService;
        this.logBusinessService = logBusinessService;
        this.taUserService = taUserService;
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
    }

    /**
     * Формирование отчета для декларации в формате xlsx
     *
     * @param declarationDataId идентификатор декларации
     * @param response          ответ
     * @throws IOException IOException
     */
    @GetMapping(value = "/rest/declarationData/{declarationDataId}/xlsx", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void downloadDeclarationXlsx(@PathVariable long declarationDataId, HttpServletResponse response)
            throws IOException {
        TAUserInfo userInfo = securityService.currentUserInfo();

        String fileName = null;
        String xmlDataFileName = declarationService.getXmlDataFileName(declarationDataId, userInfo);
        if (xmlDataFileName != null) {
            fileName = URLEncoder.encode(xmlDataFileName.replace("zip", "xlsx"), UTF_8);
        }

        response.setHeader("Content-Disposition", "attachment; filename=\""
                + fileName + "\"");
        String uuid = reportService.getDec(userInfo, declarationDataId, DeclarationDataReportType.EXCEL_DEC);
        if (uuid != null) {
            BlobData blobData = blobDataService.get(uuid);
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

        String uuid = reportService.getDec(securityService.currentUserInfo(), declarationDataId, ddReportType);
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
                pageId, "png", GetDeclarationDataHandler.DEFAULT_IMAGE_RESOLUTION);
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
        return declarationService.fetchDeclarationData(userInfo, declarationDataId);
    }

    /**
     * Удаление налоговой формы
     *
     * @param declarationDataId Идентификатор налоговой формы
     */
    @PostMapping(value = "/actions/declarationData/{declarationDataId}/delete")
    public void deleteDeclaration(@PathVariable int declarationDataId) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        declarationService.deleteIfExists(declarationDataId, userInfo);
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
    public ActionResult deleteDeclarations(@RequestParam Long[] declarationDataIds) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.deleteDeclarationList(userInfo, Arrays.asList(declarationDataIds));
    }

    /**
     * Создание отчётности
     *
     * @param declarationTypeId идентификатор типа отчётности
     * @param departmentId      идентификатор подразделения
     * @param periodId          идентификатор периода
     * @return модель {@link CreateDeclarationReportResult}, в которой содержаться данные результате операции создания
     */
    @PostMapping(value = "/actions/declarationData/createReport")
    public CreateDeclarationReportResult createReport(Integer declarationTypeId, Integer departmentId, Integer periodId) {
        return declarationService.createReports(securityService.currentUserInfo(), declarationTypeId, departmentId, periodId);
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
    public ActionResult returnToCreatedDeclaration(@RequestParam Long[] declarationDataIds, @RequestParam String reason) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.cancelDeclarationList(Arrays.asList(declarationDataIds), reason, userInfo);
    }

    /**
     * Рассчитать декларацию
     *
     * @param declarationDataId идентификатор декларации
     * @param force             признак для перезапуска задачи
     * @param cancelTask        признак для отмены задачи
     * @return модель {@link RecalculateDeclarationResult}, в которой содержаться данные о результате расчета декларации
     */
    @PostMapping(value = "/actions/declarationData/{declarationDataId}/recalculate")
    public RecalculateDeclarationResult recalculateDeclaration(@PathVariable long declarationDataId, @RequestParam boolean force, @RequestParam boolean cancelTask) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.recalculateDeclaration(userInfo, declarationDataId, force, cancelTask);
    }

    /**
     * Рассчитать налоговые формы
     *
     * @param declarationDataIds Идентификаторы налоговых форм
     * @return Модель {@link ActionResult}, в которой содержатся данные о результате операции
     */
    @PostMapping(value = "/actions/declarationData/recalculate")
    public ActionResult recalculateDeclarationList(@RequestParam Long[] declarationDataIds) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.recalculateDeclarationList(userInfo, Arrays.asList(declarationDataIds));
    }

    /**
     * Проверить декларацию
     *
     * @param declarationDataId идентификатор декларации
     * @param action
     * @return модель {@link CheckDeclarationDataResult}, в которой содержаться данные о результате проверки декларации
     */
    @PostMapping(value = "/rest/declarationData/{declarationDataId}/check", produces = MediaType.APPLICATION_JSON_VALUE)
    public CheckDeclarationResult checkDeclaration(@PathVariable long declarationDataId, @RequestBody CheckDeclarationDataAction action) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.checkDeclaration(userInfo, declarationDataId, action.isForce());
    }

    /**
     * Проверить налоговые формы
     *
     * @param declarationDataIds Идентификаторы налоговых форм
     * @return Модель {@link ActionResult}, в которой содержатся данные о результате операции
     */
    @PostMapping(value = "/actions/declarationData/check")
    public ActionResult checkDeclaration(@RequestParam Long[] declarationDataIds) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.checkDeclarationList(userInfo, Arrays.asList(declarationDataIds));
    }

    /**
     * Принять НФ
     *
     * @param action
     * @return
     */
    @PostMapping(value = "/rest/declarationData/{declarationDataId}/accept", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AcceptDeclarationResult accept(@PathVariable final long declarationDataId, @RequestBody AcceptDeclarationDataAction action) {
        action.setDeclarationId(declarationDataId);
        return declarationService.createAcceptDeclarationTask(securityService.currentUserInfo(), action);
    }

    /**
     * Принять список налоговых форм
     *
     * @param declarationDataIds Идентификаторы налоговых форм
     * @return Модель {@link ActionResult}, в которой содержатся данные о результате операции
     */
    @PostMapping(value = "/actions/declarationData/accept")
    public ActionResult acceptDeclarationList(@RequestParam Long[] declarationDataIds) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.acceptDeclarationList(userInfo, Arrays.asList(declarationDataIds));
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
        return declarationService.fetchFilesComments(declarationDataId);
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

        return JqgridPagedResourceAssembler.buildPagedList(
                pagingResult,
                pagingResult.getTotalCount(),
                pagingParams
        );
    }

    /**
     * Формирование рну ндфл для отдельного физ лица`
     *
     * @param declarationDataId идентификатор декларации
     * @param personId          идентификатор физ лица
     * @param ndflPersonFilter  заполненные поля при поиске
     * @return источники и приемники декларации
     */
    @PostMapping(value = "/actions/declarationData/{declarationDataId}/rnuDoc")
    public CreateDeclarationReportResult createReportRnu(@PathVariable("declarationDataId") long declarationDataId, @RequestParam long personId, @RequestParam NdflPersonFilter ndflPersonFilter) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.createReportRnu(userInfo, declarationDataId, personId, ndflPersonFilter);
    }

    /**
     * Формирование рну ндфл для всех физ лиц
     *
     * @param declarationDataId идентификатор декларации
     */
    @PostMapping(value = "/actions/declarationData/{declarationDataId}/allRnuReport")
    public CreateDeclarationReportResult createReportAllRnus(@PathVariable("declarationDataId") long declarationDataId, @RequestParam boolean force) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.createReportAllRnu(userInfo, declarationDataId, force);
    }

    /**
     * Формирование реестра сформированной отчетности
     *
     * @param declarationDataId
     * @param force
     * @param create
     * @return
     */
    @PostMapping(value = "/actions/declarationData/{declarationDataId}/pairKppOktmoReport")
    public CreateDeclarationReportResult createPairKppOktmo(@PathVariable("declarationDataId") long declarationDataId, @RequestParam boolean force, @RequestParam boolean create) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.createPairKppOktmoReport(userInfo, declarationDataId, force, create);
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
     * Возвращает признак существования формы
     *
     * @param declarationDataId ид формы
     */
    @GetMapping(value = "/rest/declarationData/{declarationDataId}", params = "projection=checkExistence")
    public DeclarationDataExistenceResult isDeclarationDataExists(@PathVariable long declarationDataId) {
        return new DeclarationDataExistenceResult(declarationService.existDeclarationData(declarationDataId));
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
     * Выгрузка отчетности
     *
     * @param declarationDataIds
     * @return
     */
    @PostMapping(value = "/actions/declarationData/downloadReports")
    public ActionResult downloadReports(@RequestParam Long[] declarationDataIds) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.downloadReports(userInfo, Arrays.asList(declarationDataIds));
    }

    /**
     * Создание отчетов и спецотчетов
     *
     * @param action
     * @return
     */
    @PostMapping(value = "/rest/createReport", consumes = MediaType.APPLICATION_JSON_VALUE)
    public CreateReportResult createPdfReport(@RequestBody CreateReportAction action) {
        return declarationService.createReportForReportDD(securityService.currentUserInfo(), action);
    }

    /**
     * Подготовить данные для спецотчета. Используется для получения списка ФЛ, для выбора одного из них для
     * создания спецотчета.
     * @param action
     * @return
     */
    @PostMapping(value = "/rest/declarationData/prepareSpecificReport", consumes = MediaType.APPLICATION_JSON_VALUE)
    public PrepareSubreportResult prepareSubreport(@RequestBody PrepareSubreportAction action) {
        PrepareSubreportResult result = declarationService.prepareSubreport(securityService.currentUserInfo(), action);
        return result;
    }
}