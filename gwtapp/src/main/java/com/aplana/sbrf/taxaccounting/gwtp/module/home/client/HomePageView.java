package com.aplana.sbrf.taxaccounting.gwtp.module.home.client;

import com.aplana.sbrf.taxaccounting.gwtp.module.home.client.HomePagePresenter.MyView;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;


public class HomePageView extends ViewImpl implements MyView {
  interface Binder extends UiBinder<Widget, HomePageView> {
  }

  private final Widget widget;

  @Inject
  public HomePageView(final Binder binder) {
    widget = binder.createAndBindUi(this);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }
}