package com.aplana.sbrf.taxaccounting.web.widget.menu.client;

import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.MenuItem;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

import java.util.List;

/**
 * Абстрактный класс презентора для меню
 *
 * @author Fail Mukhametdinov
 */
public abstract class AbstractMenuPresenter<V extends AbstractMenuPresenter.MyView> extends PresenterWidget<V> {

    protected final DispatchAsync dispatchAsync;

    public AbstractMenuPresenter(EventBus eventBus, V view, final DispatchAsync dispatchAsync) {
        super(eventBus, view);
        this.dispatchAsync = dispatchAsync;
    }

    public interface MyView extends View {
        void setMenuItems(List<MenuItem> links);
    }
}
