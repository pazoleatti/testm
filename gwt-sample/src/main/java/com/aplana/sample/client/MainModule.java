package com.aplana.sample.client;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import com.gwtplatform.mvp.client.gin.DefaultModule;

/**
 * @author Vitaliy Samolovskikh
 */
public class MainModule extends AbstractPresenterModule {
	@Override
	protected void configure() {
		install(new DefaultModule(SamplePlaceManager.class));

		bindPresenter(
				MainPagePresenter.class,
				MainPagePresenter.MyView.class,
				MainPageView.class,
				MainPagePresenter.MyProxy.class
		);
	}
}
