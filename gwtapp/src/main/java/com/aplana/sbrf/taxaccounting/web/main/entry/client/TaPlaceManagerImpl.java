package com.aplana.sbrf.taxaccounting.web.main.entry.client;


import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.web.main.api.client.TaPlaceManager;
import com.aplana.sbrf.taxaccounting.web.module.home.client.HomeNameTokens;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.user.client.History;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.PlaceManagerImpl;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.TokenFormatter;


public class TaPlaceManagerImpl extends PlaceManagerImpl implements TaPlaceManager{
	private final PlaceRequest defaultPlaceRequest;
	
	private boolean quietlyChange = false;
	private String onLeaveQuestion;

	@Inject
	public TaPlaceManagerImpl(final EventBus eventBus,
			final TokenFormatter tokenFormatter, @DefaultPlace String defaultNameToken) {
		super(eventBus, tokenFormatter);

		this.defaultPlaceRequest = new PlaceRequest(defaultNameToken);
	}

	@Override
	public void revealDefaultPlace() {
		revealPlace(defaultPlaceRequest);
	}

	@Override
	public void setOnLeaveConfirmation(String question) {
		super.setOnLeaveConfirmation(question);
		this.onLeaveQuestion = question;
	}

	@Override
	public void revealErrorPlace(String invalidHistoryToken) {
        Dialog.errorMessage("Ошибка 404. Введен некорректный адрес. Вы будете перенаправлены на главную страницу", new DialogHandler() {
            @Override
            public void close() {
                revealPlace(new PlaceRequest(HomeNameTokens.homePage));
            }
        });
	}


	@Override
    public void navigateBackQuietly() {
		quietlyChange = true;
		History.back();
	}

    /**
     * Проверка нужно ли вызывать диалог onLeaveConfirmation
     */
    public interface CheckOnLeaveConfirmationNeededHandler {
        boolean isNeeded(ValueChangeEvent<String> event);
    }

    private CheckOnLeaveConfirmationNeededHandler checkOnLeaveConfirmationNeededHandler;

    public CheckOnLeaveConfirmationNeededHandler getCheckOnLeaveConfirmationNeededHandler() {
        return checkOnLeaveConfirmationNeededHandler;
    }

    public void setCheckOnLeaveConfirmationNeededHandler(CheckOnLeaveConfirmationNeededHandler checkOnLeaveConfirmationNeededHandler) {
        this.checkOnLeaveConfirmationNeededHandler = checkOnLeaveConfirmationNeededHandler;
    }

    @Override
    public void onValueChange(ValueChangeEvent<String> event) {
        if (quietlyChange) {
            quietlyChange = false;
            return;
        }
        if (checkOnLeaveConfirmationNeededHandler != null && !checkOnLeaveConfirmationNeededHandler.isNeeded(event)) {
            super.setOnLeaveConfirmation(null);
        }

        super.onValueChange(event);

        super.setOnLeaveConfirmation(onLeaveQuestion);
    }
	
	
}