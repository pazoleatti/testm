package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.aplana.sbrf.taxaccounting.web.service.PropertyLoader;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping(value = "/rest/configService", method = RequestMethod.GET, produces = "application/json")
@EnableWebMvc
public class ConfigurationRestController {

    @Autowired
    private SecurityService securityService;
    @Autowired
    @Qualifier("versionInfoProperties")
    private Properties versionInfoProperties;
    @Autowired
    private DepartmentService departmentService;

    @RequestMapping("/getConfig")
    @ResponseBody
    public Map<String, Object> getConfig(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<String, Object>();

        result.put("gwtMode", PropertyLoader.isProductionMode()?"":"?gwt.codesvr=127.0.0.1:9997");
        result.put("user_data", securityService.currentUserInfo());
        result.put("project_properties", versionInfoProperties);

        TAUser user = securityService.currentUserInfo().getUser();
        Department department = departmentService.getDepartment(user.getDepartmentId());
        result.put("department", department.getName());
        return result;
    }
}