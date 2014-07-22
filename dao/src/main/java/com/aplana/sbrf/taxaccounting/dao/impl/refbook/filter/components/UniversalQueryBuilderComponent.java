package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.components;

import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.FilterTreeParser;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Собирает sql выражение но при этом не учитывает
 * алиасы-параметры которые являются ссылками на другие справочники
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UniversalQueryBuilderComponent extends AbstractQueryBuilderComponent {
    @Override
    public void enterInternlAlias(@NotNull FilterTreeParser.InternlAliasContext ctx) {
        ps.appendQuery(buildAliasStr(ctx.getText()));
    }

    private String buildAliasStr(String alias){
        if (alias.equalsIgnoreCase(RefBook.RECORD_ID_ALIAS)){
            return "id";
        } else {
            StringBuffer sb = new StringBuffer();
            RefBookAttribute attribute = refBook.getAttribute(alias);
            if (attribute == null){
                throw new IllegalArgumentException("Не найден атрибут с алиасом = "+alias);
            }
            sb.append("a");
            sb.append(alias);
            sb.append(".");
            sb.append(attribute.getAttributeType().toString());
            sb.append("_value");

            return sb.toString();
        }
    }
}
