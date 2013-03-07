/**
 * Copyright 2011 ArcBees Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;

/**
 * A {@link BaseTabPanel} styled to contain {@link SimpleTab}.
 * <p />
 */
public class SimpleTabPanel extends BaseTabPanel {

  /**
   */
  public interface Binder extends UiBinder<Widget, SimpleTabPanel> { }
private static final Binder binder = GWT.create(Binder.class);


  @Inject
  public SimpleTabPanel() {
    initWidget(binder.createAndBindUi(this));
  }

  @Override
  protected BaseTab createNewTab(TabData tabData) {
    // TODO Try using assisted injection here (to inject UiBinder in SimpleTab)
    return new SimpleTab(tabData);
  }
}
