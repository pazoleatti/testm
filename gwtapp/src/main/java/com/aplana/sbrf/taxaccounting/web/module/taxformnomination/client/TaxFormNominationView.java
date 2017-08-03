package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormTypeKind;
import com.aplana.sbrf.taxaccounting.model.TaxNominationColumnEnum;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ResizeLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.google.gwt.view.client.DefaultSelectionEventManager.createCustomManager;

/**
 * View для формы форма «Назначение налоговых форм и деклараций»
 *
 * Реализовано согласно <a href="http://conf.aplana.com/pages/viewpage.action?pageId=9583288">аналитике</a>
 * @author Stanislav Yasinskiy
 */
public class TaxFormNominationView extends ViewWithUiHandlers<TaxFormNominationUiHandlers>
		implements TaxFormNominationPresenter.MyView {

	interface Binder extends UiBinder<Widget, TaxFormNominationView> {
	}

	@UiField
    LinkButton assignAnchor;
	@UiField
	LinkButton cancelAnchor;
	@UiField
	GenericDataGrid<FormTypeKind> formGrid;
	@UiField
	GenericDataGrid<FormTypeKind> decGrid;
	@UiField
	LinkButton editAnchor;
	@UiField
	DepartmentPickerPopupWidget departmentPicker;
	@UiField
	Button search;
	@UiField
	Label taxTypeLabel;
	@UiField
	LinkButton switchMode;
	@UiField
	Label formHeader;
	@UiField
	ResizeLayoutPanel formGridWrapper;
	@UiField
	ResizeLayoutPanel declarationGridWrapper;
	@UiField
	FlexiblePager formPager;
	@UiField
	FlexiblePager declarationPager;

	/* признак формы: true - налоговые формы, false - декларации  */
	private Boolean isForm;
	private TaxType taxType;
	// Количество записей по умолчанию на страницу
	private final static int PAGE_SIZE = 100;

	private boolean enterEventDisabled;

	// изменяемые колонки в таблице
	private GenericDataGrid.DataGridResizableHeader receiverSourcesKindTitle, receiverSourcesTypeTitle, declarationTypeHeader;
	private TextColumn<FormTypeKind> departmentColumn;

	private MultiSelectionModel<FormTypeKind> formSM = new MultiSelectionModel<FormTypeKind>();
	private MultiSelectionModel<FormTypeKind> decSM = new MultiSelectionModel<FormTypeKind>();

	private final AsyncDataProvider<FormTypeKind> formDataProvider = new AsyncDataProvider<FormTypeKind>() {
		@Override
		protected void onRangeChanged(HasData<FormTypeKind> display) {
			if (getUiHandlers() != null){
				final Range range = display.getVisibleRange();
                Pair<TaxNominationColumnEnum, Boolean> sort = getSort();
				getUiHandlers().onFormRangeChange(range.getStart(), range.getLength(), sort.getFirst(), sort.getSecond());
			}
		}
	};

	private final AsyncDataProvider<FormTypeKind> decDataProvider = new AsyncDataProvider<FormTypeKind>() {
		@Override
		protected void onRangeChanged(HasData<FormTypeKind> display) {
			if (getUiHandlers() != null){
				final Range range = display.getVisibleRange();
				TaxNominationColumnEnum sort = TaxNominationColumnEnum.DEPARTMENT_FULL_NAME;
				boolean asc = true;
				if(decGrid.getColumnSortList().size()>0){
					ColumnSortList.ColumnSortInfo columnSortInfo = decGrid.getColumnSortList().get(0);
					sort = TaxNominationColumnEnum.valueOf(columnSortInfo.getColumn().getDataStoreName());
					asc = columnSortInfo.isAscending();
				}

				getUiHandlers().onDeclarationRangeChange(range.getStart(), range.getLength(), sort, asc);
			}
		}
	};

	private HandlerRegistration nativePreviewHandler;

	@Inject
	@UiConstructor
	public TaxFormNominationView(final Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));

		DefaultSelectionEventManager<FormTypeKind> multiSelectManager = createCustomManager(
				new DefaultSelectionEventManager.CheckboxEventTranslator<FormTypeKind>(0) {
					@Override
                    public boolean clearCurrentSelection(CellPreviewEvent<FormTypeKind> event) {
						return false;
					}

					@Override
                    public DefaultSelectionEventManager.SelectAction translateSelectionEvent(CellPreviewEvent<FormTypeKind> event) {
						return DefaultSelectionEventManager.SelectAction.TOGGLE;
					}
				});

		formDataProvider.addDataDisplay(formGrid);
		formGrid.setSelectionModel(formSM, multiSelectManager);
		formSM.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				updateButtonsEnabled();
			}
		});

		decDataProvider.addDataDisplay(decGrid);
		decGrid.setSelectionModel(decSM, multiSelectManager);
		decSM.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				updateButtonsEnabled();
			}
		});

		initFormGrid();
		initDecGrid();
		decGrid.addHandler(new FocusHandler() {
			@Override
			public void onFocus(FocusEvent event) {
				enterEventDisabled = true;
			}
		}, FocusEvent.getType());
		decGrid.addHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				enterEventDisabled = false;
			}
		}, BlurEvent.getType());
	}

	/* Инициализация таблицы отображающий данные вкладки "Назначение налоговых форм"   */
	private void initFormGrid() {

		// добавить колонку с чекбоксами
		Column<FormTypeKind, Boolean> checkBoxColumn = new Column<FormTypeKind, Boolean>(new CheckboxCell(true, false)) {
			@Override
			public Boolean getValue(FormTypeKind object) {
				return (object == null || object.getId() == null) ? null : formSM.isSelected(object);
			}
		};

        TextColumn<FormTypeKind> receiverSourcesKindColumn = new TextColumn<FormTypeKind>() {
			@Override
			public String getValue(FormTypeKind object) {
				return object.getKind() != null ? object.getKind().getTitle() : "";
			}
		};

        TextColumn<FormTypeKind> receiverSourcesTypeColumn = new TextColumn<FormTypeKind>() {
			@Override
			public String getValue(FormTypeKind object) {
				return object != null && object.getId() != 0 ? object.getName() : "";
			}
		};

        departmentColumn = new TextColumn<FormTypeKind>() {
            @Override
            public String getValue(FormTypeKind object) {
                return object.getDepartment().getFullName();
            }
        };

        TextColumn<FormTypeKind> performerColumn = new TextColumn<FormTypeKind>() {
            @Override
            public String getValue(FormTypeKind object) {
				if (object.getPerformers() != null && !object.getPerformers().isEmpty()) {
					StringBuilder performers = new StringBuilder();
					for (Department performer : object.getPerformers()) {
						if (performers.length() > 0 ) {
							performers.append("; ");
						}
						performers.append(performer.getFullName());
					}
					return performers.toString();
				} else {
					return "";
				}
            }
        };

		formGrid.addColumn(checkBoxColumn);
		formGrid.setColumnWidth(checkBoxColumn, 40, Style.Unit.PX);

		formGrid.addResizableColumn(departmentColumn, "Подразделение");

		receiverSourcesKindTitle = formGrid.createResizableHeader("Тип налоговой формы", receiverSourcesKindColumn);
		formGrid.addColumn(receiverSourcesKindColumn, receiverSourcesKindTitle);

		receiverSourcesTypeTitle = formGrid.createResizableHeader("Вид налоговой формы", receiverSourcesTypeColumn);
		formGrid.addColumn(receiverSourcesTypeColumn, receiverSourcesTypeTitle);

		formGrid.addResizableColumn(performerColumn, "Исполнитель");

		receiverSourcesKindColumn.setSortable(true);
		receiverSourcesTypeColumn.setSortable(true);
		departmentColumn.setSortable(true);
		performerColumn.setSortable(true);
		receiverSourcesKindColumn.setDataStoreName(TaxNominationColumnEnum.FORM_KIND.name());
		receiverSourcesTypeColumn.setDataStoreName(TaxNominationColumnEnum.FORM_TYPE.name());
		departmentColumn.setDataStoreName(TaxNominationColumnEnum.DEPARTMENT_FULL_NAME.name());
		performerColumn.setDataStoreName(TaxNominationColumnEnum.PERFORMER.name());

		formGrid.addColumnSortHandler(new ColumnSortEvent.AsyncHandler(formGrid));
		formGrid.getColumnSortList().setLimit(1);
	}

	private void initDecGrid(){

		// добавить колонку с чекбоксами
		Column<FormTypeKind, Boolean> checkBoxColumn = new Column<FormTypeKind, Boolean>(new CheckboxCell(true, false)) {
			@Override
			public Boolean getValue(FormTypeKind object) {
				return (object == null || object.getId() == null) ? null : decSM.isSelected(object);
			}
		};

		departmentColumn = new TextColumn<FormTypeKind>(){
			@Override
			public String getValue(FormTypeKind object) {
				return object.getDepartment().getFullName();
			}
		};

        TextColumn<FormTypeKind> declarationType = new TextColumn<FormTypeKind>() {
			@Override
			public String getValue(FormTypeKind object) {
				return object != null && object.getId() != 0 ? object.getName() : "";
			}
		};

        TextColumn<FormTypeKind> performerColumn = new TextColumn<FormTypeKind>() {
            @Override
            public String getValue(FormTypeKind object) {
                if (object.getPerformers() != null && !object.getPerformers().isEmpty()) {
                    StringBuilder performers = new StringBuilder();
                    for (Department performer : object.getPerformers()) {
                        if (performers.length() > 0 ) {
                            performers.append("; ");
                        }
                        performers.append(performer.getFullName());
                    }
                    return performers.toString();
                } else {
                    return "";
                }
            }
        };

        decGrid.addColumn(checkBoxColumn);
		decGrid.setColumnWidth(checkBoxColumn, 40, Style.Unit.PX);

		decGrid.addResizableColumn(departmentColumn, "Подразделение");

		declarationTypeHeader = decGrid.createResizableHeader("Макет", declarationType);
		decGrid.addColumn(declarationType, declarationTypeHeader);

        decGrid.addResizableColumn(performerColumn, "Исполнитель");

        departmentColumn.setSortable(true);
		declarationType.setSortable(true);
        performerColumn.setSortable(true);

		departmentColumn.setDataStoreName(TaxNominationColumnEnum.DEPARTMENT_FULL_NAME.name());
		declarationType.setDataStoreName(TaxNominationColumnEnum.DEC_TYPE.name());
        performerColumn.setDataStoreName(TaxNominationColumnEnum.PERFORMER.name());

		decGrid.addColumnSortHandler(new ColumnSortEvent.AsyncHandler(decGrid));
		decGrid.getColumnSortList().setLimit(1);
	}

	/* Обновление линков редактировать/отменить назначение */
	@Override
	public void updateButtonsEnabled() {
		if (isForm) {
			int selectedCount = getSelectedItemsOnFormGrid().size();
			cancelAnchor.setEnabled(selectedCount > 0);
			editAnchor.setEnabled(selectedCount > 0);
		} else {
			int selectedCount = getSelectedItemsOnDeclarationGrid().size();
            cancelAnchor.setEnabled(selectedCount > 0);
            editAnchor.setEnabled(selectedCount > 0);
		}
	}

	@Override
	public void clearFilter() {
		departmentPicker.clearFilter();
	}

	@Override
	public void setDepartments(List<Integer> department){
		departmentPicker.setValue(department);
	}

	@Override
	public void setDepartments(List<Department> departments, Set<Integer> availableDepartment) {
		departmentPicker.setAvalibleValues(departments, availableDepartment);
	}

	@Override
	public List<Integer> getDepartments(){
		return departmentPicker.getValue();
	}

	/**
	 * Событие "Открытие формы"
	 * Инициализируется при создании формы
	 */
	@Override
	public void init(TaxType nType, boolean canEdit) {
		this.isForm = false;
		this.taxType = nType;
		// Вид налога: в зависимости от налога, выбранного в главном меню ("Вид налога": "Налог на прибыль")
		taxTypeLabel.setText(nType!= null ? nType.getName() : "Неизвестный вид налога");
		initView(canEdit);
	}

	/**
	 * Установить вид представления на "Назначение налоговых форм"
	 * и применить изменения к представлению
	 */
	private void initView(boolean canEdit) {
		formGrid.redrawHeaders();
		decGrid.redrawHeaders();

		// левая ссылка
		switchMode.setVisible(false);
		switchMode.setText("Назначение налоговых форм");
		// средний лейбл
		formHeader.setText("Назначение налоговых форм");

		assignAnchor.setVisible(canEdit);

		editAnchor.setEnabled(false);
		editAnchor.setVisible(canEdit);
		// Кнопка "Отменить назначение" — неактивна (в 0.3.7 удаляем)
		cancelAnchor.setEnabled(false);
		cancelAnchor.setVisible(canEdit);
		// показать соответствующую таблицу
		formGridWrapper.setVisible(false);
		declarationGridWrapper.setVisible(true);
		// очистить таблицу с декларациями
		decGrid.setRowCount(0);

		formPager.setVisible(false);
		declarationPager.setVisible(true);
		declarationPager.setDisplay(decGrid);
		declarationPager.setPageSize(PAGE_SIZE);
	}

	@Override
	public List<FormTypeKind> getSelectedItemsOnDeclarationGrid() {
		return getSelectedItems(false);
	}

	@Override
	public List<FormTypeKind> getSelectedItemsOnFormGrid() {
		return getSelectedItems(true);
	}

	private List<FormTypeKind> getSelectedItems(boolean isForm) {
		return new ArrayList<FormTypeKind>(isForm ? formSM.getSelectedSet() : decSM.getSelectedSet());
	}

	@Override
	public void setDataToFormTable(int start, int totalCount, List<FormTypeKind> departmentFormTypes) {
        formSM.clear();
        if (departmentFormTypes.isEmpty()) {
            if (start != 0) {
                formGrid.setVisibleRange(0, formGrid.getPageSize());
            } else {
                formGrid.setRowCount(0);
            }
			return;
		}
		formGrid.setRowCount(totalCount);
		formGrid.setRowData(start, departmentFormTypes);
	}

    @Override
    public void reloadFormTableData(){
        Range range = new Range(formPager.getPage()*formPager.getPageSize(), formPager.getPageSize());
        formGrid.setVisibleRangeAndClearData(range, true);
    }

    @Override
    public void reloadDeclarationTableData(){
        Range range = new Range(declarationPager.getPage()*declarationPager.getPageSize(), declarationPager.getPageSize());
        decGrid.setVisibleRangeAndClearData(range, true);
    }

	@Override
	public void setDataToDeclarationTable(int start, int totalCount, List<FormTypeKind> departmentFormTypes) {
        decSM.clear();
        if (departmentFormTypes.isEmpty()) {
            if (start != 0) {
                decGrid.setVisibleRange(0, formGrid.getPageSize());
            } else {
                decGrid.setRowCount(0);
            }
			return;
		}

		decGrid.setRowCount(totalCount);
		decGrid.setRowData(start, departmentFormTypes);
	}

	@Override
	public boolean isForm() {
		return isForm;
	}

	@UiHandler("search")
	public void onSearchClick(ClickEvent event) {
		if (isForm) {
            formPager.firstPage();
			reloadFormTableData();
		} else {
			reloadDeclarationTableData();
			declarationPager.firstPage();
		}
	}

	@UiHandler("switchMode")
	public void onSwitchModeClick(ClickEvent event) {
	}


	@UiHandler("editAnchor")
	public void clickEdit(ClickEvent event) {
		if(getUiHandlers() != null){
			getUiHandlers().onClickEditFormDestinations(getSelectedItemsOnDeclarationGrid());
		}
	}

	@UiHandler("assignAnchor")
	public void clickAssignAnchor(ClickEvent event){
		if(getUiHandlers() != null){
			getUiHandlers().onClickOpenDeclarationDestinations();
		}
	}

	@UiHandler("cancelAnchor")
	public void clickCancelAnchor(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onClickDeclarationCancelAnchor();
		}
	}

	@Override
	public void onReveal() {
		departmentPicker.setValue(null, false);
	}

	@Override
	public void clearFormFilter() {
		departmentPicker.setValue(null);
	}

    @Override
    public FlexiblePager getFormPager() {
        return formPager;
    }

    @Override
    public FlexiblePager getDeclarationPager() {
        return declarationPager;
    }

    @Override
    public Pair<TaxNominationColumnEnum, Boolean> getSort() {
        TaxNominationColumnEnum sort = TaxNominationColumnEnum.DEPARTMENT_FULL_NAME;
        Boolean asc = true;
        if(formGrid.getColumnSortList().size()>0){
            ColumnSortList.ColumnSortInfo columnSortInfo = formGrid.getColumnSortList().get(0);
            sort = TaxNominationColumnEnum.valueOf(columnSortInfo.getColumn().getDataStoreName());
            asc = columnSortInfo.isAscending();
        }
        return new Pair<TaxNominationColumnEnum, Boolean>(sort, asc);
    }

	@Override
	public void addEnterNativePreviewHandler() {
		nativePreviewHandler = Event.addNativePreviewHandler(new Event.NativePreviewHandler() {
			@Override
			public void onPreviewNativeEvent(Event.NativePreviewEvent event) {
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER && !enterEventDisabled) {
					if (isForm) {
						formPager.firstPage();
						reloadFormTableData();
					} else {
						reloadDeclarationTableData();
						declarationPager.firstPage();
					}
				}
			}
		});
	}

	@Override
	public void removeEnterNativePreviewHandler() {
		nativePreviewHandler.removeHandler();
	}
}