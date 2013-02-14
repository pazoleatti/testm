package com.aplana.sbrf.taxaccounting.web.module.formdata.client.signers;

import com.aplana.sbrf.taxaccounting.model.FormDataPerformer;
import com.aplana.sbrf.taxaccounting.model.FormDataSigner;
import com.aplana.sbrf.taxaccounting.web.widget.cell.KeyPressableTextInputCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import java.util.ArrayList;
import java.util.List;

/**
 * Форма "Исполнитель и подписанты"
 */
public class SignersView extends PopupViewWithUiHandlers<SignersUiHandlers> implements SignersPresenter.MyView {

	public interface Binder extends UiBinder<PopupPanel, SignersView> {
	}

	@UiField
	TextBox performer;

	@UiField
	TextBox phone;

	@UiField
	DataGrid<FormDataSigner> signersTable;

	@UiField
	Button upColumn;

	@UiField
	Button downColumn;

	@UiField
	Button addColumn;

	@UiField
	Button removeColumn;

	@UiField
	Button saveButton;

	@UiField
	Button cancelButton;

	private final PopupPanel widget;
	private List<FormDataSigner> signers;
	private List<FormDataSigner> clonedSigners;
	private FormDataPerformer clonedPerformer;
	private final SingleSelectionModel<FormDataSigner> selectionModel = new SingleSelectionModel<FormDataSigner>();

	@Inject
	public SignersView(Binder uiBinder, EventBus eventBus) {
		super(eventBus);
		widget = uiBinder.createAndBindUi(this);
		widget.setAnimationEnabled(true);
		initTable();
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setPerformer(FormDataPerformer performer) {
		clonedPerformer = new FormDataPerformer();
		clonedPerformer.setName(performer.getName());
		clonedPerformer.setPhone(performer.getPhone());

		this.performer.setText(performer.getName());
		this.phone.setText(performer.getPhone());
	}

	@Override
	public void setSigners(List<FormDataSigner> signers) {
		this.signers = signers;
		clonedSigners = new ArrayList<FormDataSigner>();
		for (FormDataSigner signer : signers) {
			FormDataSigner clonedSigner = new FormDataSigner();
			clonedSigner.setName(signer.getName());
			clonedSigner.setPosition(signer.getPosition());
			clonedSigners.add(clonedSigner);
		}
		setSigners();
	}

	private void setSigners() {
		signersTable.setRowData(signers);
	}

	@UiHandler("upColumn")
	public void onUpColumn(ClickEvent event){
		FormDataSigner signer = selectionModel.getSelectedObject();
		int ind = signers.indexOf(signer);

		if (signer != null) {
			if (ind > 0) {
				FormDataSigner exchange = signers.get(ind - 1);
				signers.set(ind - 1, signer);
				signers.set(ind, exchange);
				setSigners();
				selectionModel.setSelected(signer, true);
			}
		}
	}

	@UiHandler("downColumn")
	public void onDownColumn(ClickEvent event){
		FormDataSigner signer = selectionModel.getSelectedObject();
		int ind = signers.indexOf(signer);

		if (signer != null) {
			if (ind < signers.size() - 1) {
				FormDataSigner exchange = signers.get(ind + 1);
				signers.set(ind + 1, signer);
				signers.set(ind, exchange);
				setSigners();
				selectionModel.setSelected(signer, true);
			}
		}
	}

	@UiHandler("addColumn")
	public void onAddColumn(ClickEvent event){
		FormDataSigner signer = new FormDataSigner();
		signer.setName("Новый подписант");
		signer.setPosition("должность");
		signers.add(signer);
		setSigners();
	}

	@UiHandler("removeColumn")
	public void onRemoveColumn(ClickEvent event){
		FormDataSigner signer = selectionModel.getSelectedObject();
		final int ind = signers.indexOf(signer);
		signers.remove(ind);
		setSigners();
	}

	@UiHandler("saveButton")
	public void onSave(ClickEvent event){
		getUiHandlers().onSave();
	}

	@UiHandler("cancelButton")
	public void onCancel(ClickEvent event){
		if (isEqualClonedAndCurrentSignersAndReporter()) {
			getUiHandlers().onCancel();
		}
		else {
			if (Window.confirm("Данные изменились, хотите сохранить изменения?")) {
				getUiHandlers().onSave();
			} else {
				getUiHandlers().onCancel();
			}
		}
	}

	private void initTable() {
		signersTable.setSelectionModel(selectionModel);
		TextColumn<FormDataSigner> idColumn = new TextColumn<FormDataSigner>() {
			@Override
			public String getValue(FormDataSigner object) {
				return "" + (signers.indexOf(object) + 1);
			}
		};
		idColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		signersTable.addColumn(idColumn, "№ пп");
		signersTable.setColumnWidth(idColumn, 40, Style.Unit.PX);

		KeyPressableTextInputCell nameCell = new KeyPressableTextInputCell();
		Column<FormDataSigner, String> nameColumn = new Column<FormDataSigner, String>(nameCell) {
			@Override
			public String getValue(FormDataSigner object) {
				return object.getName();
			}
		};
		nameColumn.setFieldUpdater(new FieldUpdater<FormDataSigner, String>() {
			@Override
			public void update(int index, FormDataSigner signer, String value) {
				signer.setName(value);
			}
		});
		signersTable.addColumn(nameColumn, "ФИО подписанта");

		KeyPressableTextInputCell positionCell = new KeyPressableTextInputCell();
		Column<FormDataSigner, String> positionColumn = new Column<FormDataSigner, String>(positionCell) {
			@Override
			public String getValue(FormDataSigner object) {
				return object.getPosition();
			}
		};
		positionColumn.setFieldUpdater(new FieldUpdater<FormDataSigner, String>() {
			@Override
			public void update(int index, FormDataSigner signer, String value) {
				signer.setPosition(value);
			}
		});
		signersTable.addColumn(positionColumn, "Должность");
	}

	private boolean isEqualClonedAndCurrentSignersAndReporter() {
		if (performer.getText().compareTo(clonedPerformer.getName()) != 0 ||
				phone.getText().compareTo(clonedPerformer.getPhone()) != 0) {
			return false;
		}
		if (clonedSigners.size() == signers.size()) {
			for (int i = 0; i < signers.size(); i++) {
				if (signers.get(i).getName().compareTo(clonedSigners.get(i).getName()) != 0 ||
						signers.get(i).getPosition().compareTo(clonedSigners.get(i).getPosition()) != 0) {
					return false;
				}
			}
		} else {
			return false;
		}
		return true;
	}
}
