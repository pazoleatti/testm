package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter;

import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.components.AbstractTreeListenerComponent;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.components.SimpleQueryBuilderComponent;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.components.TypeVerifierComponent;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;

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

    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    @Override
    public void init(){
        components = new ArrayList<AbstractTreeListenerComponent>();

        // компонент отражающий простые лексемы в sql выражение
        components.add(applicationContext.getBean(SimpleQueryBuilderComponent.class));

        // компонент реализующий логику проверти типов
        TypeVerifierComponent verifierComponent = applicationContext.getBean(TypeVerifierComponent.class);
        verifierComponent.setHasLastExternalRefBookAttribute(new TypeVerifierComponent.HasLastExternalRefBookAttribute() {
            @Override
            public RefBookAttribute getLastExternalRefBookAttribute() {
                throw new RuntimeException("Simple refbook doesn't know how to work with external alias");
            }
        });
        components.add(verifierComponent);
    }
}
