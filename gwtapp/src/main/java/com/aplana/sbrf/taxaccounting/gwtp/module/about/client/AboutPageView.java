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

package com.aplana.sbrf.taxaccounting.gwtp.module.about.client;

import com.aplana.sbrf.taxaccounting.gwtp.module.about.client.AboutPagePresenter.MyView;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;

import com.gwtplatform.mvp.client.ViewImpl;


/**
 * @author Christian Goudreau
 */
public class AboutPageView extends ViewImpl implements MyView {
  interface AboutUsViewUiBinder extends UiBinder<Widget, AboutPageView> {
  }

  private static AboutUsViewUiBinder uiBinder = GWT.create(AboutUsViewUiBinder.class);

  private final Widget widget;

  public AboutPageView() {
    widget = uiBinder.createAndBindUi(this);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }
}