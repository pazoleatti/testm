package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.NotificationService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Контроллер для работы с оповещениями
 */

@RestController
public class NotificationController {

    private SecurityService securityService;
    private DepartmentService departmentService;
    private NotificationService notificationService;

    public NotificationController(SecurityService securityService, DepartmentService departmentService, NotificationService notificationService) {
        this.securityService = securityService;
        this.departmentService = departmentService;
        this.notificationService = notificationService;
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
    public JqgridPagedList<Notification> fetchNotifications(@RequestParam PagingParams pagingParams) {
        TAUser user = securityService.currentUserInfo().getUser();
        List<Integer> userRoles = new ArrayList<Integer>();
        for (TARole role : user.getRoles()) {
            userRoles.add(role.getId());
        }
        NotificationsFilterData filter = new NotificationsFilterData();
        filter.setUserId(user.getId());
        Set<Integer> receiverDepartmentIds = new HashSet<Integer>();
        receiverDepartmentIds.addAll(departmentService.getNDFLDeclarationDepartments(user));
        filter.setReceiverDepartmentIds(new ArrayList<Integer>(receiverDepartmentIds));
        filter.setUserRoleIds(userRoles);

        PagingResult<Notification> notifications = notificationService.getByFilterWithPaging(filter, pagingParams);
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
    public void deleteNotifications(@RequestParam Long[] ids) {
        List<Long> notificationIdList = new ArrayList<Long>();
        Collections.addAll(notificationIdList, ids);
        notificationService.deleteAll(notificationIdList);
    }

    /**
     * Возвращает количество непрочитанных оповещений
     *
     * @return количество непрочитанных оповещений
     */
    @GetMapping(value = "/rest/notification", params = "projection=count")
    public Map<String, Object> fetchNotificationCount() {
        TAUser user = securityService.currentUserInfo().getUser();
        List<Integer> userRoles = new ArrayList<Integer>();
        for (TARole role : user.getRoles()) {
            userRoles.add(role.getId());
        }
        NotificationsFilterData filter = new NotificationsFilterData();
        filter.setUserId(user.getId());
        filter.setUserRoleIds(userRoles);
        filter.setRead(false);

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("notifications_count", notificationService.getCountByFilter(filter));
        return result;
    }

    /**
     * Помечает все оповещения текущего пользователя как прочитанные
     */
    @PostMapping(value = "/actions/notification/markAsRead")
    public void markAsRead() {
        TAUser user = securityService.currentUserInfo().getUser();
        List<Integer> userRoles = new ArrayList<Integer>();
        for (TARole role : user.getRoles()) {
            userRoles.add(role.getId());
        }
        NotificationsFilterData filter = new NotificationsFilterData();
        filter.setUserId(user.getId());
        filter.setUserRoleIds(userRoles);
        notificationService.updateUserNotificationsStatus(filter);
    }
}