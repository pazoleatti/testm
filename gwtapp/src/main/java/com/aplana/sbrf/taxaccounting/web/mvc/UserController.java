package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.PrintingService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Контроллер вывода информации о пользователях системы для страницы "Список пользователей".
 */
@RestController
public class UserController {

    @Autowired
    private TAUserService taUserService;

    @Autowired
    private PrintingService printingService;

    @Autowired
    BlobDataService blobDataService;


    /**
     * Регистрация парсеров для используемых в параметрах типов.
     *
     * @param binder объект, занимающийся парсингом
     */
    @InitBinder
    public void init(ServletRequestDataBinder binder) {
        binder.registerCustomEditor(PagingParams.class, new RequestParamEditor(PagingParams.class));
        binder.registerCustomEditor(MembersFilterData.class, new RequestParamEditor(MembersFilterData.class));
    }


    /**
     * Получение списка пользователей с пагинацией.
     *
     * @param filter       параметры фильтрации
     * @param pagingParams параметры пагинации
     * @return постраничные данные о пользователях
     */
    @GetMapping("/rest/users")
    public JqgridPagedList<TAUserView> fetchUsers(@RequestParam MembersFilterData filter,
                                                  @RequestParam PagingParams pagingParams) {

        String paramsProperty = pagingParams.getProperty();
        MembersFilterData.SortField sortField = getSortFieldByStringParam(paramsProperty);
        filter.setSortField(sortField);

        boolean isAsc = !pagingParams.getDirection().equals("desc");
        filter.setAsc(isAsc);

        filter.setCountOfRecords(pagingParams.getCount());
        filter.setStartIndex(pagingParams.getStartIndex());

        PagingResult<TAUserView> users = taUserService.getUsersViewWithFilter(filter);

        return JqgridPagedResourceAssembler.buildPagedList(
                users,
                users.getTotalCount(),
                pagingParams
        );
    }


    /**
     * Получение списка пользователей в xlsx-файле.
     *
     * @param filter   параметры фильтрации
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException в случае ошибки при работе с потоками/файлами
     */
    @GetMapping("rest/users/xlsx")
    public void fetchUsersXlsx(@RequestParam MembersFilterData filter,
                               HttpServletRequest request,
                               HttpServletResponse response)
            throws IOException {
        PagingResult<TAUserView> filteredUsers = taUserService.getUsersViewWithFilter(filter);
        // Генерим файл
        String fileUuid = printingService.generateExcelUsers(filteredUsers);
        // Получение содержимого файла
        BlobData fileData = blobDataService.get(fileUuid);
        // Генерация ответа
        ResponseUtils.createBlobResponse(request, response, fileData);
    }


    /**
     * Определение поля сортировки по строковому значению.
     *
     * @param param строка с возможными значениями
     * @return значение из enum {@link com.aplana.sbrf.taxaccounting.model.MembersFilterData.SortField}
     */
    private MembersFilterData.SortField getSortFieldByStringParam(String param) {
        MembersFilterData.SortField sortField;
        switch (param) {
            case "login":
                sortField = MembersFilterData.SortField.LOGIN;
                break;
            case "email":
                sortField = MembersFilterData.SortField.MAIL;
                break;
            case "active":
                sortField = MembersFilterData.SortField.ACTIVE;
                break;
            case "depName":
                sortField = MembersFilterData.SortField.DEPARTMENT;
                break;
            case "roles":
                sortField = MembersFilterData.SortField.ROLE;
                break;
            case "asnu":
                sortField = MembersFilterData.SortField.ASNU;
                break;
            case "name":
            default:
                sortField = MembersFilterData.SortField.NAME;
        }
        return sortField;
    }
}
