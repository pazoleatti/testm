package com.aplana.sbrf.taxaccounting.web.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/**
 * User: avanteev
 */
@Controller
@RequestMapping("/timer")
public class TimerController {

    @RequestMapping(value = "/ping", method = RequestMethod.GET)
    public void ping(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.addHeader("Cache-Control", "no-cache");
        response.getWriter().print(String.valueOf(new Date().getTime()));
        response.getWriter().close();
    }
}
