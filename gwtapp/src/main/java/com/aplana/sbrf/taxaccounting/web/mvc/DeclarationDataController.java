package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.model.LogBusinessModel;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.server.GetDeclarationDataHandler;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.AcceptDeclarationDataResult;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.CheckDeclarationDataResult;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.CreateAsyncTaskStatus;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.RecalculateDeclarationDataResult;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.server.GetDeclarationFilterDataHandler;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.AcceptDeclarationListResult;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.CheckDeclarationListResult;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.DeleteDeclarationListResult;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.server.PDFImageUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDateTime;
import org.springframework.http.MediaType;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.apache.commons.lang3.CharEncoding.UTF_8;

/**
 * Контроллер для работы с декларациями
 */
@RestController
public class DeclarationDataController {

    private static final Log LOG = LogFactory.getLog(DeclarationDataController.class);

    /**
     * Привязка данных из параметров запроса
     *
     * @param binder спец. DataBinder для привязки
     */
    @InitBinder
    public void init(ServletRequestDataBinder binder) {
        binder.registerCustomEditor(PagingParams.class, new RequestParamEditor(PagingParams.class));
    }

    private DeclarationDataSearchService declarationDataSearchService;
    private DeclarationDataService declarationService;
    private SecurityService securityService;
    private ReportService reportService;
    private BlobDataService blobDataService;
    private DeclarationTemplateService declarationTemplateService;
    private LogBusinessService logBusinessService;
    private TAUserService taUserService;
    private DepartmentService departmentService;
    private DepartmentReportPeriodService departmentReportPeriodService;
    private RefBookFactory rbFactory;
    private AsyncTaskManagerService asyncTaskManagerService;
    private LogEntryService logEntryService;
    private LockDataService lockDataService;
    private SourceService sourceService;
    private NotificationService notificationService;
    private PeriodService periodService;

    public DeclarationDataController(DeclarationDataService declarationService, SecurityService securityService, ReportService reportService,
                                     BlobDataService blobDataService, DeclarationTemplateService declarationTemplateService, LogBusinessService logBusinessService,
                                     TAUserService taUserService, DepartmentService departmentService, DepartmentReportPeriodService departmentReportPeriodService, RefBookFactory rbFactory, AsyncTaskManagerService asyncTaskManagerService,
                                     LogEntryService logEntryService, LockDataService lockDataService, SourceService sourceService,
                                     DeclarationDataSearchService declarationDataSearchService, NotificationService notificationService, PeriodService periodService) {
        this.declarationDataSearchService = declarationDataSearchService;
        this.declarationService = declarationService;
        this.securityService = securityService;
        this.reportService = reportService;
        this.blobDataService = blobDataService;
        this.declarationTemplateService = declarationTemplateService;
        this.logBusinessService = logBusinessService;
        this.taUserService = taUserService;
        this.departmentService = departmentService;
        this.departmentReportPeriodService = departmentReportPeriodService;
        this.rbFactory = rbFactory;
        this.asyncTaskManagerService = asyncTaskManagerService;
        this.logEntryService = logEntryService;
        this.lockDataService = lockDataService;
        this.sourceService = sourceService;
        this.notificationService = notificationService;
        this.periodService = periodService;
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
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        if (!declarationService.existDeclarationData(declarationDataId)) {
            throw new ServiceLoggerException(String.format(DeclarationDataDao.DECLARATION_NOT_FOUND_MESSAGE, declarationDataId), null);
        }

        DeclarationResult result = new DeclarationResult();

        DeclarationData declaration = declarationService.get(declarationDataId, userInfo);
        result.setDepartment(departmentService.getParentsHierarchy(
                declaration.getDepartmentId()));

        result.setState(declaration.getState().getTitle());

        String userLogin = logBusinessService.getFormCreationUserName(declaration.getId());
        if (userLogin != null && !userLogin.isEmpty()) {
            result.setCreationUserName(taUserService.getUser(userLogin).getName());
        }

        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declaration.getDeclarationTemplateId());
        result.setDeclarationFormKind(declarationTemplate.getDeclarationFormKind().getTitle());

        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(
                declaration.getDepartmentReportPeriodId());
        result.setReportPeriod(departmentReportPeriod.getReportPeriod().getName());
        result.setReportPeriodYear(departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear());

        if (declaration.getAsnuId() != null) {
            RefBookDataProvider asnuProvider = rbFactory.getDataProvider(RefBook.Id.ASNU.getId());
            result.setAsnuName(asnuProvider.getRecordData(declaration.getAsnuId()).get("NAME").getStringValue());
        }

        result.setCreationDate(sdf.format(logBusinessService.getFormCreationDate(declaration.getId())));
        return result;
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
     * @return модель {@link RecalculateDeclarationDataResult}, в которой содержаться данные о результате расчета декларации
     */
    @PostMapping(value = "/actions/declarationData/{declarationDataId}/recalculate")
    public RecalculateDeclarationDataResult recalculateDeclaration(@PathVariable final long declarationDataId, @RequestParam final boolean force, @RequestParam final boolean cancelTask) {
        final DeclarationDataReportType ddReportType = DeclarationDataReportType.XML_DEC;
        final RecalculateDeclarationDataResult result = new RecalculateDeclarationDataResult();
        if (!declarationService.existDeclarationData(declarationDataId)) {
            result.setExistDeclarationData(false);
            result.setDeclarationDataId(declarationDataId);
            return result;
        }
        TAUserInfo userInfo = securityService.currentUserInfo();
        final TaxType taxType = TaxType.NDFL;

        Logger logger = new Logger();
        try {
            declarationService.preCalculationCheck(logger, declarationDataId, userInfo);
        } catch (Exception e) {
            String uuid;
            if (e instanceof ServiceLoggerException) {
                uuid = ((ServiceLoggerException) e).getUuid();
            } else {
                uuid = logEntryService.save(logger.getEntries());
            }
            throw new ServiceLoggerException("%s. Обнаружены фатальные ошибки", uuid, !TaxType.DEAL.equals(taxType) ? "Налоговая форма не может быть сформирована" : "Уведомление не может быть сформировано");
        }

        try {
            String keyTask = declarationService.generateAsyncTaskKey(declarationDataId, ddReportType);
            Pair<Boolean, String> restartStatus = asyncTaskManagerService.restartTask(keyTask, declarationService.getTaskName(ddReportType, taxType), userInfo, force, logger);
            if (restartStatus != null && restartStatus.getFirst()) {
                result.setStatus(CreateAsyncTaskStatus.LOCKED);
                result.setRestartMsg(restartStatus.getSecond());
            } else if (restartStatus != null && !restartStatus.getFirst()) {
                result.setStatus(CreateAsyncTaskStatus.CREATE);
            } else {
                result.setStatus(CreateAsyncTaskStatus.CREATE);
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("declarationDataId", declarationDataId);
                params.put("docDate", new Date());
                asyncTaskManagerService.createTask(keyTask, ddReportType.getReportType(), params, cancelTask, userInfo, logger, new AsyncTaskHandler() {
                    @Override
                    public LockData createLock(String keyTask, ReportType reportType, TAUserInfo userInfo) {
                        return lockDataService.lock(keyTask, userInfo.getUser().getId(),
                                declarationService.getDeclarationFullName(declarationDataId, ddReportType),
                                LockData.State.IN_QUEUE.getText());
                    }

                    @Override
                    public void executePostCheck() {
                        result.setStatus(CreateAsyncTaskStatus.EXIST_TASK);
                    }

                    @Override
                    public boolean checkExistTask(ReportType reportType, TAUserInfo userInfo, Logger logger) {
                        return declarationService.checkExistTask(declarationDataId, reportType, logger);
                    }

                    @Override
                    public void interruptTask(ReportType reportType, TAUserInfo userInfo) {
                        declarationService.interruptTask(declarationDataId, userInfo, reportType, TaskInterruptCause.DECLARATION_RECALCULATION);
                    }

                    @Override
                    public String getTaskName(ReportType reportType, TAUserInfo userInfo) {
                        return declarationService.getTaskName(ddReportType, taxType);
                    }
                });
            }
        } catch (Exception ignored) {
        }

        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    /**
     * Проверить декларацию
     *
     * @param declarationDataId идентификатор декларации
     * @param force             признак для перезапуска задачи
     * @return модель {@link CheckDeclarationDataResult}, в которой содержаться данные о результате проверки декларации
     */
    @PostMapping(value = "/actions/declarationData/{declarationDataId}/check")
    public CheckDeclarationDataResult checkDeclaration(@PathVariable final long declarationDataId, @RequestParam final boolean force) {
        final DeclarationDataReportType ddReportType = DeclarationDataReportType.CHECK_DEC;
        final CheckDeclarationDataResult result = new CheckDeclarationDataResult();

        if (!declarationService.existDeclarationData(declarationDataId)) {
            result.setExistDeclarationData(false);
            result.setDeclarationDataId(declarationDataId);
            return result;
        }
        TAUserInfo userInfo = securityService.currentUserInfo();
        final TaxType taxType = TaxType.NDFL;
        Logger logger = new Logger();
        LockData lockDataAccept = lockDataService.getLock(declarationService.generateAsyncTaskKey(declarationDataId, DeclarationDataReportType.ACCEPT_DEC));
        if (lockDataAccept == null) {
            String uuidXml = reportService.getDec(userInfo, declarationDataId, DeclarationDataReportType.XML_DEC);
            if (uuidXml != null) {
                String keyTask = declarationService.generateAsyncTaskKey(declarationDataId, ddReportType);
                Pair<Boolean, String> restartStatus = asyncTaskManagerService.restartTask(keyTask, declarationService.getTaskName(ddReportType, taxType), userInfo, force, logger);
                if (restartStatus != null && restartStatus.getFirst()) {
                    result.setStatus(CreateAsyncTaskStatus.LOCKED);
                    result.setRestartMsg(restartStatus.getSecond());
                } else if (restartStatus != null && !restartStatus.getFirst()) {
                    result.setStatus(CreateAsyncTaskStatus.CREATE);
                } else {
                    result.setStatus(CreateAsyncTaskStatus.CREATE);
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("declarationDataId", declarationDataId);
                    asyncTaskManagerService.createTask(keyTask, ddReportType.getReportType(), params, false, userInfo, logger, new AsyncTaskHandler() {
                        @Override
                        public LockData createLock(String keyTask, ReportType reportType, TAUserInfo userInfo) {
                            return lockDataService.lock(keyTask, userInfo.getUser().getId(),
                                    declarationService.getDeclarationFullName(declarationDataId, ddReportType),
                                    LockData.State.IN_QUEUE.getText());
                        }

                        @Override
                        public void executePostCheck() {
                        }

                        @Override
                        public boolean checkExistTask(ReportType reportType, TAUserInfo userInfo, Logger logger) {
                            return false;
                        }

                        @Override
                        public void interruptTask(ReportType reportType, TAUserInfo userInfo) {
                        }

                        @Override
                        public String getTaskName(ReportType reportType, TAUserInfo userInfo) {
                            return declarationService.getTaskName(ddReportType, taxType);
                        }
                    });
                }
            } else {
                result.setStatus(CreateAsyncTaskStatus.NOT_EXIST_XML);
            }
        } else {
            try {
                lockDataService.addUserWaitingForLock(lockDataAccept.getKey(), userInfo.getUser().getId());
            } catch (Exception ignored) {
            }
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            logger.error(
                    String.format(
                            LockData.LOCK_CURRENT,
                            sdf.format(lockDataAccept.getDateLock()),
                            taUserService.getUser(lockDataAccept.getUserId()).getName(),
                            declarationService.getTaskName(DeclarationDataReportType.ACCEPT_DEC, taxType))
            );
            throw new ServiceLoggerException("Для текущего экземпляра %s запущена операция, при которой ее проверка невозможна", logEntryService.save(logger.getEntries()), taxType.getDeclarationShortName());
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
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
        if (!declarationService.existDeclarationData(declarationDataId)) {
            throw new ServiceLoggerException(String.format(DeclarationDataDao.DECLARATION_NOT_FOUND_MESSAGE, declarationDataId), null);
        }

        DeclarationDataFileComment result = new DeclarationDataFileComment();

        result.setDeclarationDataId(declarationDataId);
        result.setDeclarationDataFiles(declarationService.getFiles(declarationDataId));
        result.setComment(declarationService.getNote(declarationDataId));
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
        long declarationDataId = dataFileComment.getDeclarationDataId();

        DeclarationDataFileComment result = new DeclarationDataFileComment();
        if (!declarationService.existDeclarationData(declarationDataId)) {
            throw new ServiceLoggerException(String.format(DeclarationDataDao.DECLARATION_NOT_FOUND_MESSAGE, declarationDataId), null);
        }
        //TODO: Добавить логирование и проверку на доступность изменений для текущего пользователя.

        declarationService.saveFilesComments(declarationDataId, dataFileComment.getComment(), dataFileComment.getDeclarationDataFiles());

        result.setDeclarationDataFiles(declarationService.getFiles(declarationDataId));
        result.setComment(declarationService.getNote(declarationDataId));
        result.setDeclarationDataId(declarationDataId);

        return result;
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
        if (declarationService.existDeclarationData(declarationDataId)) {
            TAUserInfo userInfo = securityService.currentUserInfo();
            Logger logger = new Logger();
            DeclarationData declaration = declarationService.get(declarationDataId, userInfo);

            List<Relation> relationList = new ArrayList<Relation>();
            relationList.addAll(sourceService.getDeclarationSourcesInfo(declaration, true, false, null, userInfo, logger));
            relationList.addAll(sourceService.getDeclarationDestinationsInfo(declaration, true, false, null, userInfo, logger));
            return relationList;
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Получение списка налоговых форм
     *
     * @param pagingParams параметры для пагинации
     * @return список налоговых форм {@link DeclarationDataJournalItem}
     */
    @GetMapping(value = "/rest/declarationData", params = "projection=declarations")
    public JqgridPagedList<DeclarationDataJournalItem> fetchDeclarations(@RequestParam PagingParams pagingParams, @RequestParam boolean isReport) {

        //TODO: переместить реализацию в сервис
        TAUser currentUser = securityService.currentUserInfo().getUser();
        Set<Integer> receiverDepartmentIds = new HashSet<Integer>();

        receiverDepartmentIds.addAll(departmentService.getTaxFormDepartments(currentUser, TaxType.NDFL, null, null));

        List<Long> availableDeclarationFormKindIds = new ArrayList<Long>();

        for (DeclarationFormKind declarationFormKind : GetDeclarationFilterDataHandler.getAvailableDeclarationFormKind(TaxType.NDFL, isReport, currentUser)) {
            availableDeclarationFormKindIds.add(declarationFormKind.getId());
        }

        DeclarationDataFilter dataFilter = new DeclarationDataFilter();
        dataFilter.setAsnuIds(currentUser.getAsnuIds());
        dataFilter.setDepartmentIds(new ArrayList<Integer>(receiverDepartmentIds));
        dataFilter.setFormKindIds(availableDeclarationFormKindIds);

        if (!currentUser.hasRoles(TARole.N_ROLE_CONTROL_UNP) && currentUser.hasRoles(TARole.N_ROLE_CONTROL_NS)) {
            dataFilter.setUserDepartmentId(departmentService.getParentTB(currentUser.getDepartmentId()).getId());
            dataFilter.setControlNs(true);
        } else if (!currentUser.hasRoles(TARole.N_ROLE_CONTROL_UNP) && currentUser.hasRoles(TARole.N_ROLE_OPER)) {
            dataFilter.setUserDepartmentId(currentUser.getDepartmentId());
            dataFilter.setControlNs(false);
        }

        PagingResult<DeclarationDataJournalItem> pagingResult = declarationDataSearchService.findDeclarationDataJournalItems(dataFilter, pagingParams);

        return JqgridPagedResourceAssembler.buildPagedList(
                pagingResult,
                pagingResult.getTotalCount(),
                pagingParams
        );
    }

    /**
     * Проверить список деклараций
     *
     * @param declarationDataIds идентификаторы деклараций
     * @return модель {@link CheckDeclarationListResult}, в которой содержаться данные о результате проверки декларации
     */
    @PostMapping(value = "/actions/declarationData/checkDeclarationDataList")
    public CheckDeclarationListResult checkDeclarationDataList(@RequestParam final long[] declarationDataIds) {
        final DeclarationDataReportType ddReportType = DeclarationDataReportType.CHECK_DEC;
        final CheckDeclarationListResult result = new CheckDeclarationListResult();

        TAUserInfo userInfo = securityService.currentUserInfo();
        final TaxType taxType = TaxType.NDFL;
        final String taskName = declarationService.getTaskName(ddReportType, taxType);
        Logger logger = new Logger();
        for (Long id : declarationDataIds) {
            if (declarationService.existDeclarationData(id)) {
                final Long declarationId = id;
                final String prefix = String.format("Постановка операции \"%s\" для формы № %d в очередь на исполнение: ", taskName, declarationId);
                try {
                    LockData lockDataAccept = lockDataService.getLock(declarationService.generateAsyncTaskKey(declarationId, DeclarationDataReportType.ACCEPT_DEC));
                    if (lockDataAccept == null) {
                        String uuidXml = reportService.getDec(userInfo, declarationId, DeclarationDataReportType.XML_DEC);
                        if (uuidXml != null) {
                            String keyTask = declarationService.generateAsyncTaskKey(declarationId, ddReportType);
                            Pair<Boolean, String> restartStatus = asyncTaskManagerService.restartTask(keyTask, declarationService.getTaskName(ddReportType, taxType), userInfo, false, logger);
                            if (restartStatus != null && restartStatus.getFirst()) {
                                logger.warn(prefix + "Данная операция уже запущена");
                            } else if (restartStatus != null && !restartStatus.getFirst()) {
                                // задача уже была создана, добавляем пользователя в получатели
                            } else {
                                Map<String, Object> params = new HashMap<String, Object>();
                                params.put("declarationDataId", declarationId);
                                asyncTaskManagerService.createTask(keyTask, ddReportType.getReportType(), params, false, userInfo, logger, new AsyncTaskHandler() {
                                    @Override
                                    public LockData createLock(String keyTask, ReportType reportType, TAUserInfo userInfo) {
                                        return lockDataService.lock(keyTask, userInfo.getUser().getId(),
                                                declarationService.getDeclarationFullName(declarationId, ddReportType),
                                                LockData.State.IN_QUEUE.getText());
                                    }

                                    @Override
                                    public void executePostCheck() {
                                    }

                                    @Override
                                    public boolean checkExistTask(ReportType reportType, TAUserInfo userInfo, Logger logger) {
                                        return false;
                                    }

                                    @Override
                                    public void interruptTask(ReportType reportType, TAUserInfo userInfo) {
                                    }

                                    @Override
                                    public String getTaskName(ReportType reportType, TAUserInfo userInfo) {
                                        return declarationService.getTaskName(ddReportType, taxType);
                                    }
                                });
                            }
                        } else {
                            logger.error(prefix + "Экземпляр налоговой формы не заполнен данными.");
                        }
                    } else {
                        try {
                            lockDataService.addUserWaitingForLock(lockDataAccept.getKey(), userInfo.getUser().getId());
                        } catch (Exception e) {
                        }
                        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                        logger.error(
                                String.format(
                                        LockData.LOCK_CURRENT,
                                        sdf.format(lockDataAccept.getDateLock()),
                                        taUserService.getUser(lockDataAccept.getUserId()).getName(),
                                        declarationService.getTaskName(DeclarationDataReportType.ACCEPT_DEC, taxType))
                        );
                        logger.error(prefix + "Запущена операция, при которой выполнение данной операции невозможно");
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                    logger.error(prefix + e.getMessage());
                }
            } else {
                logger.warn(DeclarationDataDao.DECLARATION_NOT_FOUND_MESSAGE, id);
            }
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    /**
     * Удаление списка деклараций
     *
     * @param declarationDataIds идентификаторы деклараций
     * @return модель {@link DeleteDeclarationListResult}, в которой содержаться данные о результате удаления деклараций
     */
    @PostMapping(value = "/actions/declarationData/deleteDeclarationDataList")
    public DeleteDeclarationListResult deleteDeclarationDataList(@RequestParam long[] declarationDataIds) {
        DeleteDeclarationListResult result = new DeleteDeclarationListResult();
        TAUserInfo taUserInfo = securityService.currentUserInfo();
        Logger logger = new Logger();
        for (Long declarationId : declarationDataIds) {
            if (declarationService.existDeclarationData(declarationId)) {
                try {
                    Date startDate = new Date();
                    String declarationFullName = declarationService.getDeclarationFullName(declarationId, null);
                    declarationService.delete(declarationId, taUserInfo);
                    Date endDate = new Date();
                    Long divTime = endDate.getTime() - startDate.getTime();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                    String msg = String.format("Длительность выполнения операции: %d мс (%s - %s)", divTime, sdf.format(startDate), sdf.format(endDate));
                    logger.info("Успешно удалён объект: %s, № %d.", declarationFullName, declarationId);
                    logger.info(msg);
                    sendNotifications("Успешно удалён объект: " + declarationFullName + ", № " + declarationId, logEntryService.save(logger.getEntries()), taUserInfo.getUser().getId(), NotificationType.DEFAULT, null);
                    logger.clear();
                } catch (Exception e) {
                    logger.error("При удалении объекта: %s возникли ошибки:", declarationService.getDeclarationFullName(declarationId, DeclarationDataReportType.DELETE_DEC));
                    if (e instanceof ServiceLoggerException) {
                        logger.getEntries().addAll(logEntryService.getAll(((ServiceLoggerException) e).getUuid()));
                    } else {
                        logger.error(e);
                    }
                }
            } else {
                logger.warn(DeclarationDataDao.DECLARATION_NOT_FOUND_MESSAGE, declarationId);
            }
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    /**
     * Принятие списка деклараций
     *
     * @param declarationDataIds идентификаторы деклараций
     * @return модель {@link AcceptDeclarationListResult}, в которой содержаться данные о результате принятия деклараций
     */
    @PostMapping(value = "/actions/declarationData/acceptDeclarationDataList")
    public AcceptDeclarationListResult acceptDeclarationDataList(@RequestParam long[] declarationDataIds) {
        final DeclarationDataReportType ddToAcceptedReportType = DeclarationDataReportType.ACCEPT_DEC;
        AcceptDeclarationListResult result = new AcceptDeclarationListResult();

        final Logger logger = new Logger();
        TAUserInfo userInfo = securityService.currentUserInfo();
        final TaxType taxType = TaxType.NDFL;

        final String acceptTaskName = declarationService.getTaskName(ddToAcceptedReportType, taxType);
        for (Long id : declarationDataIds) {
            if (declarationService.existDeclarationData(id)) {
                final Long declarationId = id;
                final String prefix = String.format("Постановка операции \"%s\" для формы № %d в очередь на исполнение: ", acceptTaskName, declarationId);
                try {
                    String uuidXml = reportService.getDec(userInfo, declarationId, DeclarationDataReportType.XML_DEC);
                    if (uuidXml != null) {
                        DeclarationData declarationData = declarationService.get(declarationId, userInfo);
                        if (!declarationData.getState().equals(State.ACCEPTED)) {
                            String keyTask = declarationService.generateAsyncTaskKey(declarationId, ddToAcceptedReportType);
                            Pair<Boolean, String> restartStatus = asyncTaskManagerService.restartTask(keyTask, declarationService.getTaskName(ddToAcceptedReportType, taxType), userInfo, false, logger);
                            if (restartStatus != null && restartStatus.getFirst()) {
                                logger.warn(prefix + "Данная операция уже запущена");
                            } else if (restartStatus != null && !restartStatus.getFirst()) {
                                // задача уже была создана, добавляем пользователя в получатели
                            } else {
                                Map<String, Object> params = new HashMap<String, Object>();
                                params.put("declarationDataId", declarationId);
                                asyncTaskManagerService.createTask(keyTask, ddToAcceptedReportType.getReportType(), params, false, userInfo, logger, new AsyncTaskHandler() {
                                    @Override
                                    public LockData createLock(String keyTask, ReportType reportType, TAUserInfo userInfo) {
                                        return lockDataService.lock(keyTask, userInfo.getUser().getId(),
                                                declarationService.getDeclarationFullName(declarationId, ddToAcceptedReportType),
                                                LockData.State.IN_QUEUE.getText());
                                    }

                                    @Override
                                    public void executePostCheck() {
                                        logger.error(prefix + "Найдена запущенная задача, которая блокирует выполнение операции.");
                                    }

                                    @Override
                                    public boolean checkExistTask(ReportType reportType, TAUserInfo userInfo, Logger logger) {
                                        return declarationService.checkExistTask(declarationId, reportType, logger);
                                    }

                                    @Override
                                    public void interruptTask(ReportType reportType, TAUserInfo userInfo) {
                                        declarationService.interruptTask(declarationId, userInfo, reportType, TaskInterruptCause.DECLARATION_ACCEPT);
                                    }

                                    @Override
                                    public String getTaskName(ReportType reportType, TAUserInfo userInfo) {
                                        return declarationService.getTaskName(ddToAcceptedReportType, taxType);
                                    }
                                });
                            }
                        } else {
                            logger.error(prefix + "Налоговая форма уже находиться в статусе \"%s\".", State.ACCEPTED.getTitle());
                        }
                    } else {
                        logger.error(prefix + "Экземпляр налоговой формы не заполнен данными.");
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                    logger.error(prefix + e.getMessage());
                }
            } else {
                logger.warn(DeclarationDataDao.DECLARATION_NOT_FOUND_MESSAGE, id);
            }
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    /**
     * Вернуть в статус "Создана" список делкараций
     *
     * @param declarationDataIds идентификаторы декларациий
     * @return модель {@link AcceptDeclarationListResult}, в которой содержаться данные о результате возврате деклараций в статус "Создана"
     */
    @PostMapping(value = "/actions/declarationData/returnToCreatedDeclarationDataList")
    public AcceptDeclarationListResult returnToCreatedDeclarationDataList(@RequestParam long[] declarationDataIds, @RequestParam String reasonForReturn) {
        AcceptDeclarationListResult result = new AcceptDeclarationListResult();

        final Logger logger = new Logger();
        TAUserInfo userInfo = securityService.currentUserInfo();

        for (Long id : declarationDataIds) {
            if (declarationService.existDeclarationData(id)) {
                String declarationFullName = declarationService.getDeclarationFullName(id, DeclarationDataReportType.TO_CREATE_DEC);

                // Блокировка формы
                LockData lockData = lockDataService.lock(declarationService.generateAsyncTaskKey(id, DeclarationDataReportType.TO_CREATE_DEC),
                        userInfo.getUser().getId(), declarationFullName);

                if (lockData != null) {
                    DeclarationData declaration = declarationService.get(id, userInfo);
                    Department department = departmentService.getDepartment(declaration.getDepartmentId());
                    DeclarationTemplate declarationTemplate = declarationTemplateService.get(declaration.getDeclarationTemplateId());
                    logger.error("Форма \"%s\" из \"%s\" заблокирована", declarationTemplate.getType().getName(), department.getName());
                    continue;
                }
                logger.info("Операция \"Возврат в Создана\" для налоговой формы № %d поставлена в очередь на исполнение", id);
                String message = "";
                try {
                    List<Long> receiversIdList = declarationService.getReceiversAcceptedPrepared(id, logger, userInfo);
                    if (!receiversIdList.isEmpty()) {
                        StringBuilder sb = new StringBuilder("Отмена принятия текущей формы невозможна. Формы-приёмники ");
                        for (Long receiver : receiversIdList) {
                            sb.append(receiver)
                                    .append(", ");
                        }
                        sb.delete(sb.length() - 2, sb.length());
                        sb.append(" имеют состояние, отличное от \"Создана\". Выполните \"Возврат в Создана\" для перечисленных форм и повторите операцию.");
                        message = sb.toString();
                        logger.error(message);
                        sendNotifications(message, logEntryService.save(logger.getEntries()), userInfo.getUser().getId(), NotificationType.DEFAULT, null);
                        logger.clear();
                        continue;
                    }
                    declarationService.cancel(logger, id, reasonForReturn, securityService.currentUserInfo());
                    message = new Formatter().format("Налоговая форма № %d успешно переведена в статус \"%s\".", id, State.CREATED.getTitle()).toString();
                    logger.info(message);
                    sendNotifications("Выполнена операция \"Возврат в Создана\"", logEntryService.save(logger.getEntries()), userInfo.getUser().getId(), NotificationType.DEFAULT, null);
                    logger.clear();
                } catch (Exception e) {
                    logger.error(e);
                } finally {
                    lockDataService.unlock(declarationService.generateAsyncTaskKey(id, DeclarationDataReportType.TO_CREATE_DEC), userInfo.getUser().getId());
                }
            } else {
                logger.warn(DeclarationDataDao.DECLARATION_NOT_FOUND_MESSAGE, id);
            }
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    /**
     * Получение открытых периодов
     *
     * @return Список объектов {@link ReportPeriod}
     */
    @PostMapping(value = "/actions/declarationData/getReportPeriods")
    public List<ReportPeriod> getReportPeriods() {
        TAUserInfo userInfo = securityService.currentUserInfo();
        List<ReportPeriod> reportPeriods = new ArrayList<ReportPeriod>();
        reportPeriods.addAll(periodService.getOpenForUser(userInfo.getUser(), TaxType.NDFL));
        return reportPeriods;
    }

    private void sendNotifications(String msg, String uuid, Integer userId, NotificationType notificationType, String reportId) {
        if (msg != null && !msg.isEmpty()) {
            List<Notification> notifications = new ArrayList<Notification>();
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setCreateDate(new LocalDateTime());
            notification.setText(msg);
            notification.setLogId(uuid);
            notification.setReportId(reportId);
            notification.setNotificationType(notificationType);
            notifications.add(notification);
            notificationService.saveList(notifications);
        }
    }
}