package com.aplana.sbrf.taxaccounting.web.mvc;

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

    @RequestMapping("/getConfig")
    @ResponseBody
    public Map<String, Object> getConfig(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<String, Object>();

        result.put("gwtMode", PropertyLoader.isProductionMode()?"":"?gwt.codesvr=127.0.0.1:9997");
        return result;
    }
}