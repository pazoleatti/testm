
package com.aplana.sbrf.taxaccounting.gwtp.main.control.menu.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;


public class MainMenu extends Composite {
  interface MainMenuUiBinder extends UiBinder<Widget, MainMenu> {
  }

  private static MainMenuUiBinder uiBinder = GWT.create(MainMenuUiBinder.class);

  public MainMenu() {
    initWidget(uiBinder.createAndBindUi(this));
  }
  
}