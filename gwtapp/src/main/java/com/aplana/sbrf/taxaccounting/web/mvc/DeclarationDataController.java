package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonFilter;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.model.LogBusinessModel;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.server.GetDeclarationDataHandler;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.CheckDeclarationDataResult;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.CreateDeclarationResult;
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
    private DeclarationDataService declarationDataService;
    private LogEntryService logEntryService;

    public DeclarationDataController(DeclarationDataService declarationService, SecurityService securityService, ReportService reportService,
                                     BlobDataService blobDataService, DeclarationTemplateService declarationTemplateService, LogBusinessService logBusinessService,
                                     TAUserService taUserService, AsyncTaskManagerService asyncTaskManagerService, DeclarationDataService declarationDataService,
                                     LogEntryService LogEntryService) {
        this.declarationService = declarationService;
        this.securityService = securityService;
        this.reportService = reportService;
        this.blobDataService = blobDataService;
        this.declarationTemplateService = declarationTemplateService;
        this.logBusinessService = logBusinessService;
        this.taUserService = taUserService;
        this.asyncTaskManagerService = asyncTaskManagerService;
        this.declarationDataService = declarationDataService;
        this.logEntryService = LogEntryService;
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
     * Удаление декларации
     *
     * @param declarationDataId идентификатор декларации
     */
    @PostMapping(value = "/actions/declarationData/{declarationDataId}/delete")
    public void deleteDeclaration(@PathVariable int declarationDataId) {
        if (declarationService.existDeclarationData(declarationDataId)) {
            declarationService.delete(declarationDataId, securityService.currentUserInfo());
        }
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
    public CreateDeclarationResult createDeclaration(Long declarationTypeId, Integer departmentId, Integer periodId) {
        Logger logger = new Logger();
        CreateDeclarationResult result = new CreateDeclarationResult();

        Long declarationId = declarationDataService.create(securityService.currentUserInfo(), logger, declarationTypeId, departmentId, periodId);
        result.setDeclarationId(declarationId);
        if (!logger.getEntries().isEmpty()) {
            result.setUuid(logEntryService.save(logger.getEntries()));
        }

        return result;
    }

    /**
     * Вернуть в создана
     *
     * @param declarationDataId идентификатор декларации
     */
    @PostMapping(value = "/actions/declarationData/{declarationDataId}/returnToCreated")
    public void returnToCreatedDeclaration(@PathVariable int declarationDataId) {
        Logger logger = new Logger();
        declarationService.cancel(logger, declarationDataId, null, securityService.currentUserInfo());
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
        return declarationDataService.recalculateDeclaration(userInfo, declarationDataId, force, cancelTask);
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
        return declarationDataService.checkDeclaration(userInfo, declarationDataId, force);
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
     * Получение дополнительной информации о файлах декларации с комментариями
     *
     * @param declarationDataId идентификатор декларации
     * @return объект модели {@link DeclarationDataFileComment}, в которой содержаться данные о файлах
     * и комментарий для текущей декларации.
     */
    @GetMapping(value = "/rest/declarationData/{declarationDataId}", params = "projection=filesComments")
    public DeclarationDataFileComment fetchFilesComments(@PathVariable long declarationDataId) {
        return declarationDataService.fetchFilesComments(declarationDataId);
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
        return declarationDataService.saveDeclarationFilesComment(dataFileComment);
    }

    /**
     * Возвращает историю измений декларации по её идентификатору
     *
     * @param declarationDataId идентификатор декларации
     * @param pagingParams      параметры пагинации
     * @return список изменений декларации {@link LogBusinessModel}
     */
    @GetMapping(value = "/rest/declarationData", params = "projection=businessLogs")
    public JqgridPagedList<LogBusinessModel> fetchDeclarationBusinessLogs(@RequestParam long declarationDataId, @RequestParam PagingParams pagingParams) {
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
        return declarationDataService.getDeclarationSourcesAndDestinations(userInfo, declarationDataId);
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
        PagingResult<DeclarationDataJournalItem> pagingResult = declarationDataService.fetchDeclarations(userInfo, filter, pagingParams);

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
    public CreateDeclarationReportResult creteReportRnu(@PathVariable("declarationDataId") long declarationDataId, @RequestParam long personId, @RequestParam NdflPersonFilter ndflPersonFilter) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationDataService.creteReportRnu(userInfo, declarationDataId, personId, ndflPersonFilter);
    }
}