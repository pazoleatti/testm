package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.service.TARoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Контроллер для работы с ролями пользователей.
 */
@RestController
public class RoleController {

    @Autowired
    private TARoleService roleService;

    /**
     * Получить роли пользователей НДФЛ.
     *
     * @return список ролей
     */
    @GetMapping("/rest/roles")
    public List<TARole> fetchAllNdflRoles() {
        return roleService.getAllNdflRoles();
    }
}
