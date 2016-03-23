package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.StyleDao;
import com.aplana.sbrf.taxaccounting.model.Color;
import com.aplana.sbrf.taxaccounting.model.FormStyle;
import com.aplana.sbrf.taxaccounting.service.StyleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для работы со стилями ячеек НФ
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 15.03.2016 17:47
 */
@Service
@Transactional
public class StyleServiceImpl implements StyleService {

	@Autowired
	private StyleDao styleDao;

	@Override
	public FormStyle get(String alias) {
		if (FormStyle.DEFAULT_STYLE.getAlias().equals(alias)) {
			return FormStyle.DEFAULT_STYLE;
		}
		return styleDao.get(alias);
	}

	@Override
	public List<FormStyle> getAll() {
		List<FormStyle> styles = new ArrayList<FormStyle>();
		styles.add(FormStyle.DEFAULT_STYLE);
		styles.addAll(styleDao.getAll());
		return styles;
	}
}