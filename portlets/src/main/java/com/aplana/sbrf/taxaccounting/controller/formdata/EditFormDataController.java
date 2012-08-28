package com.aplana.sbrf.taxaccounting.controller.formdata;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.portlet.ResourceResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

import com.aplana.sbrf.taxaccounting.dao.FormDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.DateColumn;
import com.aplana.sbrf.taxaccounting.model.Form;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.NumericColumn;
import com.aplana.sbrf.taxaccounting.util.DojoFileStoreResponse;

@Controller
@SessionAttributes({"formData", "form"})
@RequestMapping("EDIT")
public class EditFormDataController {
	private Log logger = LogFactory.getLog(getClass());
	
	@Autowired
	private FormDao formDao;
	@Autowired
	private FormDataDao formDataDao;
	
	private final static int FORM_ID = 1;
	
	@ModelAttribute("form")
	public Form getForm() {
		return formDao.getForm(FORM_ID);
	}
	
	@ModelAttribute("formData")
	protected FormData getFormData(@ModelAttribute("form") Form form) {
		FormData formData = new FormData(null, form);
		DataRow r = formData.appendDataRow("1");
		for (Column<?> col: form.getColumns()) {
			if (col.getClass().equals(DateColumn.class)) {
				r.setColumnValue(col.getAlias(), new Date());
			} else if (col.getClass().equals(NumericColumn.class)) {
				r.setColumnValue(col.getAlias(), new BigDecimal(0));
			} else {
				r.setColumnValue(col.getAlias(), "test");
			}
		}
		r = formData.appendDataRow("2");
		for (Column<?> col: form.getColumns()) {
			if (col.getClass().equals(DateColumn.class)) {
				r.setColumnValue(col.getAlias(), new Date());
			} else if (col.getClass().equals(NumericColumn.class)) {
				r.setColumnValue(col.getAlias(), new BigDecimal(0));
			} else {
				r.setColumnValue(col.getAlias(), "test");
			}
		}
		return formData;
		//return formDataDao.get(2);
	}
	
	@ModelAttribute("gridLayout")
	protected String getGridLayout(@ModelAttribute("formData") FormData formData) throws JsonGenerationException, JsonMappingException, IOException {
		List<Column<?>> columns = formData.getForm().getColumns();
		return getObjectMapper().writeValueAsString(columns);
	}
	
	@ActionMapping(params="action=new")
	public void newRecord() {
		logger.warn("NOT IMPLEMENTED");
	}
	
	@RenderMapping
	public String showEdit() {
		return "formData/edit";
	}
	
	@ResourceMapping("dataRows")
	public void getDataRows(@ModelAttribute("formData") FormData formData, ResourceResponse response) throws JsonGenerationException, JsonMappingException, IOException {
		List<DataRow> dataRows = formData.getDataRows();
		DojoFileStoreResponse<DataRow> data = new DojoFileStoreResponse<DataRow>();
		data.setIdentifier("alias");
		data.setItems(dataRows);

		response.setContentType("application/json");
		getObjectMapper().writeValue(response.getPortletOutputStream(), data);
	}
	
	@ActionMapping("save")
	public void saveFormData(@ModelAttribute("formData") FormData formData) {
		logger.info("Trying to save form data");
		long formDataId = formDataDao.save(formData);
		logger.info("Form data saved, formDataId = " + formDataId);
	}
	
	private ObjectMapper getObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		SimpleModule module = new SimpleModule("taxaccounting", new Version(1, 0, 0, null));
		module.addSerializer(DataRow.class, new DataRowSerializer());
		module.addSerializer(Column.class, new DojoGridColumnSerializer());
		objectMapper.registerModule(module);
		return objectMapper;
	}
}
