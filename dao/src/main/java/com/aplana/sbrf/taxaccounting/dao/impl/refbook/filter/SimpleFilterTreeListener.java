package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter;

import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Объект совпадает по реализации с UniversalFilterTreeListener
 * и отличается только одним методом, возможно если возникнут изменения придется отказаться от
 * наследования
 *
 * User: ekuvshinov
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)@Qualifier("simpleFilterTreeListener")
public class SimpleFilterTreeListener extends UniversalFilterTreeListener {

    @Override
    public void enterInternlAlias(@NotNull FilterTreeParser.InternlAliasContext ctx) {
        ps.appendQuery(ctx.getText());
    }
}
