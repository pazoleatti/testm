package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.HistoryBusinessSearchOrdering;
import com.aplana.sbrf.taxaccounting.model.LogBusiness;
import com.aplana.sbrf.taxaccounting.service.LogBusinessService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.web.widget.history.shared.LogBusinessClient;
import com.aplana.sbrf.taxaccounting.web.widget.history.shared.SortFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by AFateev on 28.06.2017.
 */
@Controller
public class LogBusinessController {

    @Autowired
    LogBusinessService logBusinessService;

    @Autowired
    TAUserService taUserService;
    @RequestMapping(value = "/rest/logBusines/{declarationId}", method = RequestMethod.GET)
    @ResponseBody
    public List<LogBusinessClient> getMessagesLog(@PathVariable long declarationId) {
        ArrayList<LogBusinessClient> resultListBusines=new ArrayList<LogBusinessClient>() {};
        for (LogBusiness logBusiness : logBusinessService.getDeclarationLogsBusiness(declarationId, HistoryBusinessSearchOrdering.DATE, true)) {
            LogBusinessClient logBusinessClient=new LogBusinessClient(logBusiness);
            logBusinessClient.setEventName((FormDataEvent.getByCode(logBusiness.getEventId())).getTitle());
            logBusinessClient.setUserFullName(taUserService.getUser(logBusiness.getUserLogin()).getName());
            resultListBusines.add(logBusinessClient);
        }
        return  resultListBusines;
    }
}