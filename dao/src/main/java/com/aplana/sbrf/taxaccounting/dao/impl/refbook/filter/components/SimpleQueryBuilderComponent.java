package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.components;

import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.FilterTreeParser;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Компонент для сборки sql запроса для простых справочников
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SimpleQueryBuilderComponent extends AbstractQueryBuilderComponent {
    @Override
    public void enterInternlAlias(FilterTreeParser.InternlAliasContext ctx) {
        if (ctx.getText().equalsIgnoreCase(RefBook.RECORD_ID_ALIAS)){
            ps.appendQuery("id");
        } else{
            ps.appendQuery(ctx.getText());
        }
    }
}
