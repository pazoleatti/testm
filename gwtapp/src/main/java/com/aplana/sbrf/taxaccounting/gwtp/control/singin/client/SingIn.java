package com.aplana.sbrf.taxaccounting.gwtp.control.singin.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class SingIn extends Composite {
  interface SingInUiBinder extends UiBinder<Widget, SingIn> {
  }

  private static SingInUiBinder uiBinder = GWT.create(SingInUiBinder.class);

  public SingIn() {
    initWidget(uiBinder.createAndBindUi(this));
  }
  
}