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

package com.aplana.sbrf.taxaccounting.gwtp.module.home.client;

import com.aplana.sbrf.taxaccounting.gwtp.main.api.client.RevealContentTypeHolder;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;


/**
 * @author Christian Goudreau
 */
public class HomePagePresenter extends
    Presenter<HomePagePresenter.MyView, HomePagePresenter.MyProxy> {
  /**
   * {@link HomePagePresenter}'s proxy.
   */
  @ProxyCodeSplit
  @NameToken(HomeNameTokens.homePage)
  public interface MyProxy extends ProxyPlace<HomePagePresenter> {
  }

  /**
   * {@link HomePagePresenter}'s view.
   */
  public interface MyView extends View {
  }

  @Inject
  public HomePagePresenter(final EventBus eventBus, final MyView view,
      final MyProxy proxy) {
    super(eventBus, view, proxy);
  }

  @Override
  protected void revealInParent() {
    RevealContentEvent.fire(this, RevealContentTypeHolder.getMainContent(),
        this);
  }
}