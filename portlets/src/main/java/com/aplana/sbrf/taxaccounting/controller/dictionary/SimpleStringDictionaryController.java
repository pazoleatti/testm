package com.aplana.sbrf.taxaccounting.controller.dictionary;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.aplana.sbrf.taxaccounting.dao.dataprovider.SimpleDictionaryDataProvider;
import com.aplana.sbrf.taxaccounting.dao.dataprovider.impl.StringDictionaryManager;
import com.aplana.sbrf.taxaccounting.model.dictionary.SimpleDictionaryItem;

/**
 * Контроллер сервлета для обработки запросов данных по простым строковым справочникам 
 */
@Controller
public class SimpleStringDictionaryController {
	@Autowired
	StringDictionaryManager dictionaryManager;
	
	@RequestMapping("/string/{dictionaryCode}")
	@ResponseBody
	List<SimpleDictionaryItem<String>> getValues(
			@PathVariable String dictionaryCode, 
			@RequestParam(value = "value", defaultValue = "*") String valuePattern) 
	{
		SimpleDictionaryDataProvider<String> dp = dictionaryManager.getDataProvider(dictionaryCode);
		if (valuePattern.endsWith("*")) {
			valuePattern = valuePattern.substring(0, valuePattern.length() - 1) + "%";
		}
		return dp.getValues(valuePattern);
	}
	
	@RequestMapping("/string/{dictionaryCode}/{value}")
	@ResponseBody
	SimpleDictionaryItem<String> getItem(
			@PathVariable("dictionaryCode") String dictionaryCode,
			@PathVariable("value") String value) 
	{
		SimpleDictionaryDataProvider<String> dp = dictionaryManager.getDataProvider(dictionaryCode);
		return dp.getItem(value);
	}
}
