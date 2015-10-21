package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.server;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookPickerUtils;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.PickerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Класс для формирвоания фильтра для запроса по выборке данных для компонента выбора из справочника
 *
 * @author aivanov
 */
@Component
public class RefBookPickerFilterBuilder {

    @Autowired
    RefBookFactory refBookFactory;

    @Autowired
    DepartmentService departmentService;

    @Autowired
    SecurityService securityService;

    /**
     * Формирование строчки sql запроса для фильтрации данных для линейного справочника
     *
     * @param filter        технический филтььтр
     * @param searchPattern строка поиска
     * @param refBook       ссылка на справочник
     * @param context       контекст справочника
     * @return sql для запроса
     */
    public String buildMultiPickerFilter(String filter, String searchPattern, RefBook refBook, PickerContext context) {
        StringBuilder resultFilter = new StringBuilder();
        if (filter != null && !filter.trim().isEmpty()) {
            resultFilter.append(filter.trim());
        }

        if ((refBook != null)
                && (refBook.getRegionAttribute() != null)
                && (context != null)) {

            String regionFilter;
            switch (context.getRegionFilter()) {
                case DEPARTMENT_CONFIG_FILTER:
                    regionFilter = refBook.getRegionAttribute().getAlias() + " = " + context.getAttributeId();
                    break;
                case DEFAULT:
                case FORM_FILTER:
                    Department department = null;
                    if (context.getFormDataId() != null) {
                        department = departmentService.getFormDepartment(context.getFormDataId());
                    }
                    regionFilter = RefBookPickerUtils.buildRegionFilterForUser(department == null ? null : Arrays.asList(department), refBook);
                    if (regionFilter != null && regionFilter.equals(RefBookPickerUtils.NO_REGION_MATCHES_FLAG)) {
                        return regionFilter;
                    }
                    break;
                default:
                    regionFilter = null;
            }

            if (regionFilter != null) {
                if (resultFilter.length() > 0) {
                    resultFilter.append(" and ");
                }
                resultFilter.append("(" + regionFilter + ")");
            }

        }

        String resultSearch = refBookFactory.getSearchQueryStatement(searchPattern, refBook.getId());

        if (resultFilter.length() > 0 && resultSearch != null && resultSearch.length() > 0) {
            return "(" + resultFilter.toString() + ") and (" + resultSearch + ")";
        } else if (resultFilter.length() > 0 && (resultSearch == null || resultSearch.isEmpty())) {
            return resultFilter.toString();
        } else if (resultSearch != null && resultSearch.length() > 0 && resultFilter.length() == 0) {
            return resultSearch;
        } else if ("".equals(filter)) {
            return "";
        }
        return null;
    }

    /**
     * Формирование строчки sql запроса для фильтрации данных для иерархического справочника
     *
     * @param filter        технический филтььтр
     * @param searchPattern строка поиска
     * @param refBook       ссылка на справочник
     * @return sql для запроса
     */
    public String buildTreePickerFilter(String filter, String searchPattern, RefBook refBook) {
        StringBuilder resultFilter = new StringBuilder();
        if (filter != null && !filter.trim().isEmpty()) {
            resultFilter.append(filter.trim());
        }

        Department dep = departmentService.getDepartment(securityService.currentUserInfo().getUser().getDepartmentId());
        String regionFilter = RefBookPickerUtils.buildRegionFilterForUser(dep == null ? null : Arrays.asList(dep), refBook);
        if (regionFilter != null) {
            if (regionFilter.equals(RefBookPickerUtils.NO_REGION_MATCHES_FLAG)) {
                return regionFilter;
            }
            if (resultFilter.length() > 0) {
                resultFilter.append(" and ");
            }
            resultFilter.append("(" + regionFilter + ")");
        }

        String resultSearch = refBookFactory.getSearchQueryStatement(searchPattern, refBook.getId());
        if (resultFilter.length() > 0 && resultSearch != null && resultSearch.length() > 0) {
            return "(" + resultFilter.toString() + ") and (" + resultSearch + ")";
        } else if (resultFilter.length() > 0 && resultSearch != null && resultSearch.isEmpty()) {
            return resultFilter.toString();
        } else if (resultSearch != null && resultSearch.length() > 0 && resultFilter.length() == 0) {
            return resultSearch;
        } else {
            return null;
        }

    }

}
