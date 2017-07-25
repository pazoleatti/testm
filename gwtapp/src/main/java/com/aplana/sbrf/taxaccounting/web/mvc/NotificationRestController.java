package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.NotificationService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Контроллер для работы с оповещениями
 */

@Controller
@EnableSpringDataWebSupport
public class NotificationRestController {

    @InitBinder
    public void init(ServletRequestDataBinder binder) {
        binder.registerCustomEditor(PagingParams.class, new RequestParamEditor(PagingParams.class));
    }

    @Autowired
    SecurityService securityService;
    @Autowired
    DepartmentService departmentService;
    @Autowired
    NotificationService notificationService;

    /**
     * Возвращает список оповещений
     *
     * @param pagingParams параметры для пагинации
     * @return список оповещений
     */
    @RequestMapping(value = "/rest/notification", method = RequestMethod.GET, params = "projection=get")
    @ResponseBody
    public JqgridPagedList<Notification> fetchNotifications(@RequestParam PagingParams pagingParams) {
        TAUser user = securityService.currentUserInfo().getUser();
        List<Integer> userRoles = new ArrayList<Integer>();
        for (TARole role : user.getRoles()) {
            userRoles.add(role.getId());
        }
        NotificationsFilterData filter = new NotificationsFilterData();
        filter.setUserId(user.getId());
        Set<Integer> receiverDepartmentIds = new HashSet<Integer>();
        receiverDepartmentIds.addAll(departmentService.getTaxFormDepartments(user, TaxType.NDFL, null, null));
        receiverDepartmentIds.addAll(departmentService.getTaxFormDepartments(user, TaxType.PFR, null, null));
        filter.setReceiverDepartmentIds(new ArrayList<Integer>(receiverDepartmentIds));
        filter.setUserRoleIds(userRoles);

        PagingResult<Notification> notifications = notificationService.getByFilter(filter);
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
    @RequestMapping(value = "/actions/notification/delete", method = RequestMethod.POST)
    @ResponseBody
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
    @RequestMapping(value = "/rest/notification", method = RequestMethod.GET, params = "projection=count")
    @ResponseBody
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
    @RequestMapping(value = "/actions/notification/markAsRead", method = RequestMethod.PUT)
    @ResponseBody
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
