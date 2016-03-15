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
		if (STYLE_NO_CHANGE.equals(alias)) {
			return new FormStyle() {{
				setBackColor(Color.GREY);
			}};
		}
		if (STYLE_INSERT.equals(alias)) {
			return new FormStyle() {{
				setBackColor(Color.PALE_GREEN);
			}};
		}
		if (STYLE_DELETE.equals(alias)) {
			return new FormStyle() {{
				setBackColor(Color.LIGHT_CORAL);
			}};
		}
		if (STYLE_CHANGE.equals(alias)) {
			return new FormStyle() {{
				setFontColor(Color.RED);
				setBold(true);
			}};
		}
		if (EDITABLE_CELL_STYLE.equals(alias)) {
			return new FormStyle() {{
				setBackColor(Color.LIGHT_BLUE);
			}};
		}
		if (AUTO_FILL_CELL_STYLE.equals(alias)) {
			return new FormStyle() {{
				setFontColor(Color.DARK_GREEN);
				setBold(true);
			}};
		}
		return null;
	}

}