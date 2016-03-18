package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.Color;
import com.aplana.sbrf.taxaccounting.model.FormStyle;
import com.aplana.sbrf.taxaccounting.service.StyleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис для работы со стилями ячеек НФ
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 15.03.2016 17:47
 */
@Service
@Transactional
public class StyleServiceImpl implements StyleService {

	@Override
	public FormStyle get(String alias) {
		// встроенные стили
		if (FormStyle.DEFAULT_STYLE.getAlias().equals(alias)) {
			return FormStyle.DEFAULT_STYLE;
		}
		if (FormStyle.MANUAL_EDITABLE_STYLE.getAlias().equals(alias)) {
			return FormStyle.MANUAL_EDITABLE_STYLE;
		}
		if (FormStyle.MANUAL_READ_ONLY_STYLE.getAlias().equals(alias)) {
			return FormStyle.MANUAL_READ_ONLY_STYLE;
		}
		// стили из "бд"
		if (STYLE_NO_CHANGE.equals(alias)) {
			return new FormStyle(StyleService.STYLE_NO_CHANGE, Color.BLACK, Color.GREY, false, false);
		}
		if (STYLE_INSERT.equals(alias)) {
			return new FormStyle(StyleService.STYLE_INSERT, Color.BLACK, Color.PALE_GREEN, false, false);
		}
		if (STYLE_DELETE.equals(alias)) {
			return new FormStyle(StyleService.STYLE_DELETE, Color.BLACK, Color.LIGHT_CORAL, false, false);
		}
		if (STYLE_CHANGE.equals(alias)) {
			return new FormStyle(StyleService.STYLE_CHANGE, Color.RED, Color.WHITE, false, true);
		}
		if (EDITABLE_CELL_STYLE.equals(alias)) {
			return new FormStyle(StyleService.EDITABLE_CELL_STYLE, Color.BLACK, Color.LIGHT_BLUE, false, false);
		}
		if (AUTO_FILL_CELL_STYLE.equals(alias)) {
			return new FormStyle(StyleService.AUTO_FILL_CELL_STYLE, Color.DARK_GREEN, Color.WHITE, false, true);
		}
		return null;
	}
}