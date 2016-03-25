package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.client;

import com.aplana.gwt.client.DoubleBox;
import com.aplana.gwt.client.LongBox;
import com.aplana.gwt.client.TextArea;
import com.aplana.gwt.client.TextBox;
import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.DepartmentCombined;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.periodpicker.client.PeriodPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookPickerWidget;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.PickerContext;
import com.aplana.sbrf.taxaccounting.web.widget.style.LabelSeparator;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import java.util.*;

/**
 * View для формы настроек подразделений
 *
 * @author Dmitriy Levykin
 */
public class DepartmentConfigView extends ViewWithUiHandlers<DepartmentConfigUiHandlers>
		implements DepartmentConfigPresenter.MyView, Editor<DepartmentCombined> {

    private static final String CONFIRM_TITLE = "Подтверждение операции";
    private static final String CONFIRM_MSG = "Все несохранённые данные будут потеряны. Выйти из режима редактирования?";

	// Признак режима редактирования
	private boolean isEditMode = false;

	interface Binder extends UiBinder<Widget, DepartmentConfigView> {
	}

	interface MyDriver extends SimpleBeanEditorDriver<DepartmentCombined, DepartmentConfigView> {
	}

	private final MyDriver driver = GWT.create(MyDriver.class);

	// Параметры выбранного подразделения
	private DepartmentCombined data;

	// Разыменованные значения справочников. Требуются для отмены.
	Map<Long, String> dereferenceValues;

	// Выбранное подразделение
	private Integer currentDepartmentId;

	// Выбранный период
	private Integer currentReportPeriodId;

	// Выбранный тип налога
	private TaxType currentTaxType = TaxType.INCOME;

	// Признак открытости выбранного отчетного периода
	private boolean isReportPeriodActive;

    private HandlerRegistration resizeHandler;
    private Timer resizeTimer;

	@UiField
	TextBox inn,
			kpp,
			phone,
			taxOrganCode,
            taxOrganCodeProm,
			reorgInn,
			reorgKpp,
			signatorySurname,
			signatoryFirstname,
			signatoryLastname,
			formatVersion;

    @UiField
    @Ignore
    Label taxOrganCodeLabel;

	@UiField
	TextArea approveDocName,
			approveOrgName;

	@UiField
	DoubleBox taxRate;

	@UiField
	LongBox sumTax,
			sumDividends;

	@UiField
    RefBookPickerWidget
			dictRegionId,
			reorgFormCode,
			signatoryId,
			taxPlaceTypeCode,
			obligation,
            oktmo,
			okvedCode,
			type;

    @UiField
    @Editor.Ignore
    LabelSeparator labelResponsible;

    @UiField
    CheckBox prepayment;

    @UiField
    @Editor.Ignore
    Label additionalNameLbl;

	@UiField
	TextArea name,
            additionalName;

	// Контейнер для справочника периодов
	@UiField
	@Editor.Ignore
    PeriodPickerPopupWidget periodPickerPopup;

	@UiField
	@Ignore
	HorizontalPanel sumTaxPanel,
			sumDividendsPanel,
            payPanelObligation,
            payPanelPrepayment,
			payPanelType,
			taxRatePanel,
            taxOrganCodePromPanel;

	@UiField
	@Ignore
	Button saveButton,
            deleteButton,
			cancelButton,
            findButton;

	@UiField
	@Ignore
	Button editButton;

	@UiField
	@Ignore
	FormPanel formPanel;

	@UiField
	@Ignore
	DepartmentPickerPopupWidget departmentPicker;

	@UiField
	@Ignore
	Label editModeLabel;

    @UiField
    @Ignore
    Label taxTypeLabel;

    @UiField
    @Ignore
    HorizontalPanel findButtonPanel,
            reorgCodePanel,
            reorgInnPanel,
            reorgKppPanel;

    @UiField
    @Ignore
    LabelSeparator reorgLabel;

    @UiField
    @Ignore
    Label configPeriodLabel;

    private final static DateTimeFormat SDF = DateTimeFormat.getFormat("dd.MM.yyyy");

    @Inject
	@UiConstructor
	public DepartmentConfigView(final Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));
        driver.initialize(this);
		enableAllChildren(false, formPanel);
		initListeners();
        resizeTimer = new Timer() {
            @Override
            public void run() {
                ellipsizeDepartmentPickerLabel(departmentPicker.isEnabled());
            }
        };
	}

	private void initListeners() {
		periodPickerPopup.addValueChangeHandler(new ValueChangeHandler<List<Integer>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<Integer>> event) {
                if (event.getValue() != null && event.getValue().size() == 1) {
                    onReportPeriodsSelected(event.getValue().get(0));
                } else {
                    clear();
                    currentReportPeriodId = null;
                    isReportPeriodActive = false;
                }
            }
        });
        dictRegionId.addValueChangeHandler(new ValueChangeHandler<List<Long>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<Long>> event) {
                PickerContext pickerContext = new PickerContext();
                Long attributeId = event.getValue().get(0);
                pickerContext.setAttributeId(attributeId);
                pickerContext.setRegionFilter(PickerContext.RegionFilter.DEPARTMENT_CONFIG_FILTER);
                reorgFormCode.setPickerContext(pickerContext);
                signatoryId.setPickerContext(pickerContext);
                taxPlaceTypeCode.setPickerContext(pickerContext);
                obligation.setPickerContext(pickerContext);
                oktmo.setPickerContext(pickerContext);
                okvedCode.setPickerContext(pickerContext);
                type.setPickerContext(pickerContext);
            }
        });
		// Подразделение
		departmentPicker.addValueChangeHandler(new ValueChangeHandler<List<Integer>>() {

            @Override
            public void onValueChange(ValueChangeEvent<List<Integer>> event) {
                Integer selDepartmentId = null;

                if (event != null && !event.getValue().isEmpty()) {
                    selDepartmentId = event.getValue().iterator().next();
                }

                // Проверка совпадения выбранного подразделения с текущим
                if (DepartmentConfigView.this.currentDepartmentId != null
                        && DepartmentConfigView.this.currentDepartmentId.equals(selDepartmentId)) {
                    return;
                }
                DepartmentConfigView.this.currentDepartmentId = selDepartmentId;
                // Очистка формы
                editButton.setEnabled(false);
                clear();
                updateVisibility();
                if (currentReportPeriodId != null && currentDepartmentId != null) {
                    getUiHandlers().getRefBookPeriod(currentReportPeriodId, currentDepartmentId);
                }
            }
        });
	}

	@Override
	public void reloadDepartments() {
		getUiHandlers().reloadDepartments(currentTaxType, currentDepartmentId);
	}

	@Override
	public void reloadDepartmentParams(String uuid) {
		getUiHandlers().reloadDepartmentParams(currentDepartmentId, currentTaxType,
				currentReportPeriodId == null ? null : currentReportPeriodId, uuid);
	}

    @Override
    public void init() {
        setEditMode(false);
        currentDepartmentId = null;
        currentReportPeriodId = null;
        currentTaxType = null;
        isReportPeriodActive = false;

        updateVisibility();
        clear();
        addResizeHandler();
    }

	@Override
	public final void clear() {
        if (dereferenceValues != null) {
            dereferenceValues.clear();
        }
        dictRegionId.setDereferenceValue(null);
        reorgFormCode.setDereferenceValue(null);
        signatoryId.setDereferenceValue(null);
        taxPlaceTypeCode.setDereferenceValue(null);
        obligation.setDereferenceValue(null);
        oktmo.setDereferenceValue(null);
        okvedCode.setDereferenceValue(null);
        type.setDereferenceValue(null);
        configPeriodLabel.setText("");

        driver.edit(new DepartmentCombined());
    }

    /**
     * Отмена редактирования
     */
    private void cancel() {
        setEditMode(false);
        driver.edit(data);
        setDereferenceValue(dereferenceValues);
    }

    /**
	 * Обновление видимости полей
	 */
	private void updateVisibility() {
		boolean isUnp = currentDepartmentId != null && currentDepartmentId == 1;
		// Ставка налога
		taxRatePanel.setVisible(currentTaxType == TaxType.INCOME);
		// Сумма налога на прибыль, выплаченная за пределами Российской Федерации в отчётном периоде
		sumTaxPanel.setVisible(currentDepartmentId != null && isUnp && currentTaxType == TaxType.INCOME);
		// Сумма налога с выплаченных дивидендов за пределами Российской Федерации в последнем квартале отчётного периода
		sumDividendsPanel.setVisible(currentDepartmentId != null && isUnp && currentTaxType == TaxType.INCOME);
		// Обязанность по уплате налога и Признак расчёта
		boolean isPayPanelVisible = currentDepartmentId != null && !isUnp && currentTaxType == TaxType.INCOME;

		payPanelObligation.setVisible(isPayPanelVisible);
		payPanelType.setVisible(isPayPanelVisible);
        payPanelPrepayment.setVisible(currentTaxType == TaxType.TRANSPORT);

        additionalName.setVisible(currentTaxType == TaxType.INCOME);
        additionalNameLbl.setVisible(currentTaxType == TaxType.INCOME);
	}

	@UiHandler("findButton")
	public void onFind(ClickEvent event) {
        if (departmentPicker.getValue() != null && !departmentPicker.getValue().isEmpty() &&
                periodPickerPopup.getValue() != null && !periodPickerPopup.getValue().isEmpty()) {
            reloadDepartmentParams(null);
        } else {
            Dialog.errorMessage("Не заполнены обязательные поля", "Для поиска должно быть заполнено поле \"Подразделение\" и \"Период\"");
        }
    }

	@UiHandler("saveButton")
	public void onSave(ClickEvent event) {
		if (isValid()) {
			getUiHandlers().save(driver.flush(), currentReportPeriodId, currentDepartmentId);
			update();
		} else {
			Dialog.errorMessage("Есть ошибки заполнения формы");
		}


    }

	@UiHandler("deleteButton")
    public void onDelete(ClickEvent event){
        getUiHandlers().delete(driver.flush(), currentReportPeriodId, currentDepartmentId);
    }

	@UiHandler("cancelButton")
	public void onCancel(ClickEvent event) {
		if (isEditMode && driver.isDirty()) {
            Dialog.confirmMessage(CONFIRM_TITLE, CONFIRM_MSG, new DialogHandler() {
                @Override
                public void yes() {
                    DepartmentConfigView.this.cancel();
                    Dialog.hideMessage();
                }

                @Override
                public void no() {
                    Dialog.hideMessage();
                }

                @Override
                public void close() {
                    no();
                }
            });
		} else {
            cancel();
		}
	}

	@UiHandler("editButton")
	public void onEdit(ClickEvent event) {
		if (currentDepartmentId != null && currentReportPeriodId != null) {
            getUiHandlers().edit(currentReportPeriodId, currentDepartmentId);
		}
	}

	/**
	 * Режим редактирования / чтения
	 *
	 * @param isEditMode Флаг
	 */
    @Override
	public void setEditMode(boolean isEditMode) {
		this.isEditMode = isEditMode;
		editModeLabel.setVisible(isEditMode);
		editButton.setVisible(!isEditMode);
        periodPickerPopup.setEnabled(!isEditMode);
        departmentPicker.setEnabled(!isEditMode);
        editButton.setEnabled(currentDepartmentId != null && currentReportPeriodId != null);
        saveButton.setVisible(isEditMode);
        deleteButton.setVisible(isEditMode);
		cancelButton.setVisible(isEditMode);
        findButtonPanel.setVisible(!isEditMode);
		enableAllChildren(isEditMode, formPanel);
        ellipsizeDepartmentPickerLabel(!isEditMode);
	}

	/**
	 * Рекурсивная установка доступности элементов
	 *
	 * @param enable Доступность
	 * @param widget Корневой виджет
	 */
	private void enableAllChildren(boolean enable, Widget widget) {
		if (widget instanceof HasWidgets) {
			for (Widget nextWidget : ((HasWidgets) widget)) {
                if (nextWidget == additionalName && getUiHandlers() != null){
                    additionalName.setEnabled(getUiHandlers().isControlUnp() && enable);
                    continue;
                }
				enableAllChildren(enable, nextWidget);
				if (nextWidget instanceof HasEnabled) {
					((HasEnabled) nextWidget).setEnabled(enable);
				}
			}
		}
	}

	public final void onReportPeriodsSelected(Integer reportPeriodId) {
		// Проверка совпадения выбранного подразделения с текущим
		if (this.currentReportPeriodId != null && reportPeriodId != null && this.currentReportPeriodId.equals(reportPeriodId)) {
			return;
		}
        this.currentReportPeriodId = reportPeriodId;
        editButton.setEnabled(false);
        if (currentReportPeriodId != null && currentDepartmentId != null) {
            getUiHandlers().getRefBookPeriod(currentReportPeriodId, currentDepartmentId);
        }
	}

	@Override
	public void setDepartments(List<Department> departments, Set<Integer> availableDepartments) {
		departmentPicker.setAvalibleValues(departments, availableDepartments);
	}

	@Override
	public void setDepartment(final Department department) {
		if (department != null) {
			departmentPicker.setValue(Arrays.asList(department.getId()), true);
			updateVisibility();
		}

		this.currentDepartmentId = department != null ? department.getId() : null;
	}

	@Override
	public void setReportPeriods(List<ReportPeriod> reportPeriods) {
		periodPickerPopup.setPeriods(reportPeriods);
        if (reportPeriods == null || reportPeriods.isEmpty()) {
            return;
        }
        Integer defaultReportPeriodId = periodPickerPopup.getDefaultReportPeriod();
        ReportPeriod maxPeriod = reportPeriods.get(0);
        for (ReportPeriod reportPeriod : reportPeriods) {
            if (defaultReportPeriodId != null && reportPeriod.getId().equals(defaultReportPeriodId)) {
                periodPickerPopup.setValue(Arrays.asList(defaultReportPeriodId), true);
                maxPeriod = null;
                break;
            }
            if (reportPeriod.getEndDate().after(maxPeriod.getEndDate())) {
                maxPeriod = reportPeriod;
            }
        }
        if (maxPeriod != null) {
            currentReportPeriodId = maxPeriod.getId();
            periodPickerPopup.setValue(Arrays.asList(maxPeriod.getId()), true);
        }
	}

	@Override
	public void setReportPeriod(Integer reportPeriodId) {
		if (reportPeriodId != null) {
			periodPickerPopup.setValue(Arrays.asList(reportPeriodId), true);
		}
	}

	@Override
	public void setDepartmentCombined(DepartmentCombined combinedDepartmentParam) {
		data = combinedDepartmentParam;
		driver.edit(data);
	}

	@Override
	public void setDereferenceValue(Map<Long, String> rbTextValues) {
		this.dereferenceValues = rbTextValues;
		if (dereferenceValues != null) {
			// Заполнение текстовых значений справочников
			dictRegionId.setDereferenceValue(rbTextValues.get(dictRegionId.getAttributeId()));
			//dictRegionId.setPeriodDates(period.getFirst(), period.getSecond());
			reorgFormCode.setDereferenceValue(rbTextValues.get(reorgFormCode.getAttributeId()));
			signatoryId.setDereferenceValue(rbTextValues.get(signatoryId.getAttributeId()));
			taxPlaceTypeCode.setDereferenceValue(rbTextValues.get(taxPlaceTypeCode.getAttributeId()));
			obligation.setDereferenceValue(rbTextValues.get(obligation.getAttributeId()));
			oktmo.setDereferenceValue(rbTextValues.get(oktmo.getAttributeId()));
			okvedCode.setDereferenceValue(rbTextValues.get(okvedCode.getAttributeId()));
			type.setDereferenceValue(rbTextValues.get(type.getAttributeId()));
		}
	}

	@Override
	public void setTaxType(TaxType taxType) {
        periodPickerPopup.setType(taxType.name());
        currentTaxType = taxType;
        taxTypeLabel.setText(taxType.getName());
        //taxOrganCodeLabel.setText("Код налогового органа (кон.):");
        if (TaxType.DEAL.equals(taxType)) {
            labelResponsible.setText("Ответственный за уведомление");
        } else {
            labelResponsible.setText("Ответственный за декларацию");
        }

		reloadDepartments();
	}

    @Override
    public TaxType getTaxType() {
        return currentTaxType;
    }

    @Override
	public void setReportPeriodActive(boolean reportPeriodActive) {
		isReportPeriodActive = reportPeriodActive;
		editButton.setEnabled(currentReportPeriodId != null);
    }

    @Override
    public void updateVisibleEditButton() {
        editButton.setVisible(!isEditMode);
    }

    @Override
    public Integer getCurrentReportPeriodId() {
        return currentReportPeriodId;
    }

    @Override
    public Integer getCurrentDepartmentId() {
        return currentDepartmentId;
    }

    @Override
    public void removeResizeHandler() {
        if (resizeHandler != null) {
            resizeHandler.removeHandler();
        }
    }

    @Override
    public void update() {
        driver.edit(data);

        if (dereferenceValues != null) {
            // Обновление разыменованных значений
            dereferenceValues.clear();
            dereferenceValues.put(dictRegionId.getAttributeId(), dictRegionId.getDereferenceValue());
            dereferenceValues.put(reorgFormCode.getAttributeId(), reorgFormCode.getDereferenceValue());
            dereferenceValues.put(signatoryId.getAttributeId(), signatoryId.getDereferenceValue());
            dereferenceValues.put(taxPlaceTypeCode.getAttributeId(), taxPlaceTypeCode.getDereferenceValue());
            dereferenceValues.put(obligation.getAttributeId(), obligation.getDereferenceValue());
            dereferenceValues.put(oktmo.getAttributeId(), oktmo.getDereferenceValue());
            dereferenceValues.put(okvedCode.getAttributeId(), okvedCode.getDereferenceValue());
            dereferenceValues.put(type.getAttributeId(), type.getDereferenceValue());
        }
    }

    @Override
    public void setRefBookPeriod(Date startDate, Date endDate) {
        clear();
        dictRegionId.setPeriodDates(startDate, endDate);
        reorgFormCode.setPeriodDates(startDate, endDate);
        signatoryId.setPeriodDates(startDate, endDate);
        taxPlaceTypeCode.setPeriodDates(startDate, endDate);
        obligation.setPeriodDates(startDate, endDate);
        oktmo.setPeriodDates(startDate, endDate);
        okvedCode.setPeriodDates(startDate, endDate);
        type.setPeriodDates(startDate, endDate);
    }

    @Override
    public void setConfigPeriod(Date configStartDate, Date configEndDate) {
        configPeriodLabel.setText("Период действия настроек с " +
                SDF.format(configStartDate) + " по " +
                (configEndDate != null ? SDF.format(configEndDate)  : "-")
        );
    }

    private void addResizeHandler() {
        if (resizeHandler == null)
            resizeHandler = Window.addResizeHandler(new ResizeHandler() {
                @Override
                public void onResize(ResizeEvent event) {
                    resizeTimer.scheduleRepeating(5);
                }
            });
    }

    /**
     * Вручную выставляем многоточние в конце, если текст занимает больше 2х строк
     * @param enabled
     */
    private void ellipsizeDepartmentPickerLabel(boolean enabled) {
        if (!enabled) {
            String text = departmentPicker.getText();
            Element el = departmentPicker.getLabel().getElement();
            el.setInnerText(text);
            for(;el.getScrollHeight() > 32;) {
                if (!text.isEmpty()) {
                    text = text.substring(0, text.length() - 1);
                    el.setInnerText(text + "…");
                } else {
                    break;
                }
            }
        }
    }

	/**
	 * Проверка корректности полей
	 *
	 * @return true - все поля валидны, false - не все поля валидны
	 */
	private boolean isValid() {
		return inn.isValid() && kpp.isValid() && reorgInn.isValid() && reorgKpp.isValid();
	}
}