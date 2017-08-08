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
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import com.aplana.sbrf.taxaccounting.web.service.PropertyLoader;
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
import java.text.SimpleDateFormat;
import java.util.*;

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
    }

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

    public DeclarationDataController(DeclarationDataService declarationService, SecurityService securityService, ReportService reportService,
                                     BlobDataService blobDataService, DeclarationTemplateService declarationTemplateService, LogBusinessService logBusinessService,
                                     TAUserService taUserService, DepartmentService departmentService, DepartmentReportPeriodService departmentReportPeriodService, RefBookFactory rbFactory, AsyncTaskManagerService asyncTaskManagerService,
                                     LogEntryService logEntryService, LockDataService lockDataService, SourceService sourceService) {
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
                in.close();
                out.close();
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
                in.close();
                out.close();
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
        PDFImageUtils.pDFPageToImage(pdfData, response.getOutputStream(),
                pageId, "png", GetDeclarationDataHandler.DEFAULT_IMAGE_RESOLUTION);
        pdfData.close();
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
        String fileName = URLEncoder.encode(declarationService.getXmlDataFileName(declarationDataId, securityService.currentUserInfo()), UTF_8);

        response.setHeader("Content-Disposition", "attachment; filename=\""
                + fileName + "\"");
        try {
            IOUtils.copy(xmlDataIn, response.getOutputStream());
        } finally {
            IOUtils.closeQuietly(xmlDataIn);
        }
    }

    /**
     * Формирует DeclarationResult
     *
     * @param declarationDataId идентификатор декларации
     * @return модель DeclarationResult, в которой содержаться данные о декларации
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
     * @return модель RecalculateDeclarationDataResult, в которой содержаться данные о результате расчета декларации
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
                asyncTaskManagerService.createTask(keyTask, ddReportType.getReportType(), params, cancelTask, PropertyLoader.isProductionMode(), userInfo, logger, new AsyncTaskHandler() {
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
     * @return модель CheckDeclarationDataResult, в которой содержаться данные о результате проверки декларации
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
                    asyncTaskManagerService.createTask(keyTask, ddReportType.getReportType(), params, false, PropertyLoader.isProductionMode(), userInfo, logger, new AsyncTaskHandler() {
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
     * @return модель AcceptDeclarationDataResult, в которой содержаться данные о результате принятия декларации
     */
    @PostMapping(value = "/actions/declarationData/{declarationDataId}/accept")
    public AcceptDeclarationDataResult acceptDeclaration(@PathVariable final long declarationDataId, @RequestParam final boolean force, @RequestParam final boolean cancelTask) {
        final DeclarationDataReportType ddReportType = DeclarationDataReportType.ACCEPT_DEC;
        final AcceptDeclarationDataResult result = new AcceptDeclarationDataResult();
        if (!declarationService.existDeclarationData(declarationDataId)) {
            result.setExistDeclarationData(false);
            result.setDeclarationDataId(declarationDataId);
            return result;
        }
        Logger logger = new Logger();
        TAUserInfo userInfo = securityService.currentUserInfo();
        final TaxType taxType = TaxType.NDFL;
        String uuidXml = reportService.getDec(userInfo, declarationDataId, DeclarationDataReportType.XML_DEC);
        if (uuidXml != null) {
            DeclarationData declarationData = declarationService.get(declarationDataId, userInfo);
            if (!declarationData.getState().equals(State.ACCEPTED)) {
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
                    asyncTaskManagerService.createTask(keyTask, ddReportType.getReportType(), params, cancelTask, PropertyLoader.isProductionMode(), userInfo, logger, new AsyncTaskHandler() {
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
                            declarationService.interruptTask(declarationDataId, userInfo, reportType, TaskInterruptCause.DECLARATION_ACCEPT);
                        }

                        @Override
                        public String getTaskName(ReportType reportType, TAUserInfo userInfo) {
                            return declarationService.getTaskName(ddReportType, taxType);
                        }
                    });
                }
            } else {
                result.setStatus(CreateAsyncTaskStatus.EXIST);
            }
        } else {
            result.setStatus(CreateAsyncTaskStatus.NOT_EXIST_XML);
        }

        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    /**
     * Получение дополнительной информации о файлах декларации с комментариями
     *
     * @param declarationDataId идентификатор декларации
     * @return DeclarationDataFileComment объект модели, в которой содержаться данные о файлах
     * и комментарий для текущей декларации.
     */
    @GetMapping(value = "/rest/declarationData", params = "projection=filesComments")
    public DeclarationDataFileComment fetchFilesComments(@RequestParam long declarationDataId) {
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
     * @return DeclarationDataFileComment   новый объект модели, в котором содержаться данные
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
     * @return список изменений декларации
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
     * @return источники и приемники декларации
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
}