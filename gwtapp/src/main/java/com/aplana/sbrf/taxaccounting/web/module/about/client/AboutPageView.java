package com.aplana.sbrf.taxaccounting.web.module.about.client;

import com.aplana.sbrf.taxaccounting.web.module.about.client.AboutPagePresenter.MyView;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;


public class AboutPageView extends ViewImpl implements MyView {
  interface Binder extends UiBinder<Widget, AboutPageView> {
  }

  private final Widget widget;

  @Inject
  public AboutPageView(final Binder binder ) {
    widget = binder.createAndBindUi(this);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }
}