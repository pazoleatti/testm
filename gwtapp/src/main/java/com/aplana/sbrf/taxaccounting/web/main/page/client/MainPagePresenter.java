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

package com.aplana.sbrf.taxaccounting.web.main.page.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.widget.signin.client.SignInPresenter;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.LockInteractionEvent;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import com.gwtplatform.mvp.client.proxy.RevealRootContentEvent;

/**
 * This is the top-level presenter of the hierarchy. Other presenters reveal
 * themselves within this presenter.
 * <p />
 * The goal of this sample is to show how to use nested presenters. These can
 * be useful to decouple multiple presenters that need to be displayed on the
 * screen simultaneously.
 *
 * @author Christian Goudreau
 */
public class MainPagePresenter extends
    Presenter<MainPagePresenter.MyView, MainPagePresenter.MyProxy> {
  /**
   * {@link MainPagePresenter}'s proxy.
   */
  @ProxyStandard
  public interface MyProxy extends Proxy<MainPagePresenter> {
  }

  /**
   * {@link MainPagePresenter}'s view.
   */
  public interface MyView extends View {
    void showLoading(boolean visibile);
  }

  /**
   * Use this in leaf presenters, inside their {@link #revealInParent} method.
   */
  @ContentSlot
  static final Type<RevealContentHandler<?>> TYPE_SetMainContent = new Type<RevealContentHandler<?>>();
  
  static final Object TYPE_SignInContent = new Object();
  
  
  static {
	  RevealContentTypeHolder.setMainContent(TYPE_SetMainContent);
  }
  
  private final SignInPresenter signInPresenter;

  @Inject
  public MainPagePresenter(final EventBus eventBus, final MyView view,
      final MyProxy proxy, SignInPresenter signInPresenter) {
    super(eventBus, view, proxy);
    this.signInPresenter = signInPresenter;
  }

  @Override
  protected void revealInParent() {
    RevealRootContentEvent.fire(this, this);
  }

  /**
   * We display a short lock message whenever navigation is in progress.
   *
   * @param event The {@link LockInteractionEvent}.
   */
  @ProxyEvent
  public void onLockInteraction(LockInteractionEvent event) {
    getView().showLoading(event.shouldLock());
  }
  
  
  @Override
  protected void onReveal() {
    super.onReveal();
    setInSlot(TYPE_SignInContent, signInPresenter);
  }

  @Override
  protected void onHide() {
    super.onHide();
    clearSlot(TYPE_SignInContent);
  }
  
}
