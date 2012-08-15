package com.aplana.sbrf.taxaccounting.controller.forms;

import org.springframework.stereotype.Controller;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

@Controller
public class FormsController {
	@RenderMapping
	String showList() {
		return "list";
	}
}
