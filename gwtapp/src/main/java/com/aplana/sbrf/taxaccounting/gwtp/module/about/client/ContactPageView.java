package com.aplana.sbrf.taxaccounting.gwtp.module.about.client;

import com.aplana.sbrf.taxaccounting.gwtp.module.about.client.ContactPagePresenterBase.MyView;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;


public class ContactPageView extends ViewImpl implements MyView {
  interface Binder extends UiBinder<Widget, ContactPageView> {
  }

  private final Widget widget;

  @UiField
  Label navigationHistory;

  @Inject
  public ContactPageView(final Binder binder) {
    widget = binder.createAndBindUi(this);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setNavigationHistory(String navigationHistory) {
    this.navigationHistory.setText(navigationHistory);
  }
}