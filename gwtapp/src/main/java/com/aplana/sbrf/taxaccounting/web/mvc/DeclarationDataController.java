package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonFilter;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.model.result.CreateResult;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.model.LogBusinessModel;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.server.GetDeclarationDataHandler;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.CheckDeclarationDataResult;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.server.PDFImageUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;

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
        binder.registerCustomEditor(DataRow.class, new RequestParamEditor(DataRow.class));
        binder.registerCustomEditor(Cell.class, new RequestParamEditor(Cell.class));
    }

    private DeclarationDataService declarationService;
    private SecurityService securityService;
    private ReportService reportService;
    private BlobDataService blobDataService;
    private DeclarationTemplateService declarationTemplateService;
    private LogBusinessService logBusinessService;
    private TAUserService taUserService;
    private AsyncTaskManagerService asyncTaskManagerService;

    public DeclarationDataController(DeclarationDataService declarationService, SecurityService securityService, ReportService reportService,
                                     BlobDataService blobDataService, DeclarationTemplateService declarationTemplateService, LogBusinessService logBusinessService,
                                     TAUserService taUserService, AsyncTaskManagerService asyncTaskManagerService) {
        this.declarationService = declarationService;
        this.securityService = securityService;
        this.reportService = reportService;
        this.blobDataService = blobDataService;
        this.declarationTemplateService = declarationTemplateService;
        this.logBusinessService = logBusinessService;
        this.taUserService = taUserService;
        this.asyncTaskManagerService = asyncTaskManagerService;
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
     * @param declarationTypeId ID вида налоговой формы
     * @param departmentId      ID подразделения
     * @param periodId          ID периода
     * @return Результат создания
     */
    @PostMapping(value = "/actions/declarationData/create")
    public CreateResult<Long> createDeclaration(Long declarationTypeId, Integer departmentId, Integer periodId) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.create(userInfo, declarationTypeId, departmentId, periodId);
    }

    /**
     * Вернуть в создана
     *
     * @param declarationDataId идентификатор декларации
     */
    @PostMapping(value = "/actions/declarationData/{declarationDataId}/returnToCreated")
    public void returnToCreatedDeclaration(@PathVariable int declarationDataId, @RequestParam String reason) {
        Logger logger = new Logger();
        declarationService.cancel(logger, declarationDataId, reason, securityService.currentUserInfo());
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
     * @param force             признак для перезапуска задачи
     * @return модель {@link CheckDeclarationDataResult}, в которой содержаться данные о результате проверки декларации
     */
    @PostMapping(value = "/actions/declarationData/{declarationDataId}/check")
    public CheckDeclarationResult checkDeclaration(@PathVariable long declarationDataId, @RequestParam boolean force) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.checkDeclaration(userInfo, declarationDataId, force);
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
     * Принять декларацию
     *
     * @param declarationDataId идентификатор декларации
     * @param force             признак для перезапуска задачи
     * @param cancelTask        признак для отмены задачи
     * @return модель {@link AcceptDeclarationResult}, в которой содержаться данные о результате принятия декларации
     */
    @PostMapping(value = "/actions/declarationData/{declarationDataId}/accept")
    public AcceptDeclarationResult acceptDeclaration(@PathVariable final long declarationDataId, @RequestParam final boolean force, @RequestParam final boolean cancelTask) {
        return asyncTaskManagerService.createAcceptDeclarationTask(securityService.currentUserInfo(), declarationDataId, force, cancelTask);
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
        return declarationService.saveDeclarationFilesComment(dataFileComment);
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
        for (LogBusiness logBusiness : logBusinessService.getDeclarationLogsBusiness(declarationDataId, HistoryBusinessSearchOrdering.DATE, true)) {
            LogBusinessModel logBusinessModel = new LogBusinessModel(logBusiness, (FormDataEvent.getByCode(logBusiness.getEventId())).getTitle(),
                    taUserService.getUser(logBusiness.getUserLogin()).getName());
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
     * Возвращает данные о наличии отчетов
     *
     * @param declarationDataId идентификатор декларации
     * @return данные о наличии отчетов
     */
    @GetMapping(value = "/rest/declarationData/{declarationDataId}", params = "projection=availableReports")
    public ReportAvailableResult checkAvailabilityReports(@PathVariable long declarationDataId) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationService.checkAvailabilityReports(userInfo, declarationDataId);
    }

}