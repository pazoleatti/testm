
package com.aplana.sbrf.taxaccounting.web.module.home.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter.FilterPresenter;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;


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
  
  private final FilterPresenter filterPresenter;
  static final Object TYPE_testPresenter = new Object();

  @Inject
  public HomePagePresenter(final EventBus eventBus, final MyView view,
      final MyProxy proxy, FilterPresenter filterPresenter) {
    super(eventBus, view, proxy);
    this.filterPresenter = filterPresenter;
  }
  
	@Override
	protected void onReveal() {
		super.onReveal();
		//setInSlot(TYPE_testPresenter, filterPresenter);
	}

	@Override
	protected void onHide() {
		super.onHide();
		clearSlot(TYPE_testPresenter);
	}

  @Override
  protected void revealInParent() {
    RevealContentEvent.fire(this, RevealContentTypeHolder.getMainContent(),
        this);
  }
}