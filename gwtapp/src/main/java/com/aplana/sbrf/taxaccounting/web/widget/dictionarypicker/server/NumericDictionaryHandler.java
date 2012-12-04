package com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.server;

import com.aplana.sbrf.taxaccounting.dao.dataprovider.DictionaryDataProvider;
import com.aplana.sbrf.taxaccounting.dao.dataprovider.DictionaryManager;
import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.shared.NumericDictionaryAction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * @author Vitalii Samolovskikh
 */
@Service
public class NumericDictionaryHandler extends DictionaryHandler<NumericDictionaryAction, BigDecimal> {
	@SuppressWarnings("UnusedDeclaration")
	private final Log log = LogFactory.getLog(NumericDictionaryHandler.class);

	@Autowired
	@Qualifier("numericDictionaryManager")
	private DictionaryManager<BigDecimal> dictionaryManager;

	public NumericDictionaryHandler() {
		super(NumericDictionaryAction.class);
	}

	protected DictionaryDataProvider<BigDecimal> getDictionaryDataProvider(String dictionaryCode) {
		return dictionaryManager.getDataProvider(dictionaryCode);
	}
}
