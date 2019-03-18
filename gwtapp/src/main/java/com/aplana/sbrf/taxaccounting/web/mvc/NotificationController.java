package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.NotificationService;
import com.aplana.sbrf.taxaccounting.service.PrintingService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.model.CustomMediaType;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * Контроллер для работы с оповещениями
 */
@RestController
public class NotificationController {

    private final SecurityService securityService;
    private final DepartmentService departmentService;
    private final NotificationService notificationService;
    private final PrintingService printingService;
    private final BlobDataService blobDataService;

    public NotificationController(SecurityService securityService,
                                  DepartmentService departmentService,
                                  NotificationService notificationService,
                                  PrintingService printingService,
                                  BlobDataService blobDataService) {
        this.securityService = securityService;
        this.departmentService = departmentService;
        this.notificationService = notificationService;
        this.printingService = printingService;
        this.blobDataService = blobDataService;
    }

    /**
     * Привязка данных из параметров запроса
     *
     * @param binder спец. DataBinder для привязки
     */
    @InitBinder
    public void init(ServletRequestDataBinder binder) {
        binder.registerCustomEditor(PagingParams.class, new RequestParamEditor(PagingParams.class));
    }

    /**
     * Возвращает список оповещений
     *
     * @param pagingParams параметры для пагинации
     * @return список оповещений
     */
    @GetMapping(value = "/rest/notification")
    public JqgridPagedList<Notification> fetchNotifications(
            @RequestParam PagingParams pagingParams,
            @RequestParam(required = false) String text,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd.MM.yyyy'T'HH:mm") Date timeFrom,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd.MM.yyyy'T'HH:mm") Date timeTo) {
        TAUser user = securityService.currentUserInfo().getUser();

        NotificationsFilterData filter = new NotificationsFilterData();
        filter.setUserId(user.getId());
        filter.setUserRoleIds(user.getRoleIds());
        filter.setReceiverDepartmentIds(departmentService.findAllAvailableIds(user));
        filter.setText(text);
        filter.setTimeFrom(timeFrom);
        filter.setTimeTo(timeTo);

        PagingResult<Notification> notifications = notificationService.findByFilter(filter, pagingParams);
        return JqgridPagedResourceAssembler.buildPagedList(
                notifications,
                notifications.getTotalCount(),
                pagingParams
        );
    }

    /**
     * Удаляет выбранные оповещения
     *
     * @param ids - массив id оповещений
     */
    @PostMapping(value = "/actions/notification/delete")
    public void deleteNotifications(@RequestBody List<Long> ids) {
        notificationService.deleteByIdIn(ids);
    }

    /**
     * Возвращает количество непрочитанных оповещений
     *
     * @return количество непрочитанных оповещений
     */
    @GetMapping(value = "/rest/notification", params = "projection=count")
    public Map<String, Object> fetchNotificationCount() {
        TAUser user = securityService.currentUserInfo().getUser();

        NotificationsFilterData filter = new NotificationsFilterData();
        filter.setUserId(user.getId());
        filter.setUserRoleIds(user.getRoleIds());
        filter.setRead(false);

        Map<String, Object> result = new HashMap<>();
        result.put("notifications_count", notificationService.countByFilter(filter));
        return result;
    }

    /**
     * Помечает все оповещения текущего пользователя как прочитанные
     */
    @PostMapping(value = "/actions/notification/markAsRead")
    public void markAsRead() {
        TAUser user = securityService.currentUserInfo().getUser();

        NotificationsFilterData filter = new NotificationsFilterData();
        filter.setUserId(user.getId());
        filter.setUserRoleIds(user.getRoleIds());
        notificationService.setReadTrueByFilter(filter);
    }

    /**
     * Выгрузка файла по uuid из окна оповещения
     *
     * @param uuid уникальный идентификатор файла
     * @param req  запрос
     * @param resp ответ
     * @throws IOException из обработки файла
     */
    @GetMapping(value = "/actions/notification/{uuid}/download")
    public void processDownloadNotif(@PathVariable String uuid, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Notification stub = new Notification();
        stub.setUserId(securityService.currentUserInfo().getUser().getId());
        stub.setReportId(uuid);
        BlobData blobData = notificationService.getNotificationBlobData(stub);
        if (blobData != null) {
            ResponseUtils.createBlobResponse(req, resp, blobData);
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Выгрузка выбранных оповещений в формате csv.
     *
     * @param ids      идентификаторы выбранных оповещений.
     * @param response http-ответ, в теле которого возвращается файл.
     */
    @GetMapping(value = "/actions/notification/downloadCsv", produces = CustomMediaType.APPLICATION_VND_UTF8_VALUE)
    public void downloadNotificationsCsv(@RequestParam List<Long> ids, HttpServletRequest request, HttpServletResponse response) throws IOException {

        List<Notification> notifications = notificationService.findByIdIn(ids);
        if (notifications.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String fileUuid = printingService.generateCsvNotifications(notifications);
        BlobData blobData = blobDataService.get(fileUuid);
        if (blobData != null) {
            ResponseUtils.createBlobResponse(request, response, blobData);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Создание асинхронной задачи на выгрузку протокола (уведомлений) выбранных оповещений.
     */
    @PostMapping("/actions/notification/createLogs")
    public ActionResult createNotificationsLogsReport(@RequestBody List<Long> ids) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return notificationService.createLogsReportAsync(ids, userInfo);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity handleAccessDeniedException() {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
}
