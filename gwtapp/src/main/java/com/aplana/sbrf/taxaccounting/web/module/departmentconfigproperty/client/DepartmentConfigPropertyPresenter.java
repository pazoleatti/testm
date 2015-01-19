package com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.sortable.ViewWithSortableTable;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.*;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.*;

import java.util.*;

public class DepartmentConfigPropertyPresenter extends Presenter<DepartmentConfigPropertyPresenter.MyView,
        DepartmentConfigPropertyPresenter.MyProxy> implements DepartmentConfigPropertyUiHandlers {

    private Long recordId;

    private List<RefBookAttribute> attributes;

    private static final long TABLE_PROPERTY_REFBOOK_ID = 206L;
    private static final long PROPERTY_REFBOOK_ID = 99L;

    private static final long TABLE_TRANSPORT_REFBOOK_ID = 310L;
    private static final long TRANSPORT_REFBOOK_ID = 31L;

    private static final long TABLE_INCOME_REFBOOK_ID = 330L;
    private static final long INCOME_REFBOOK_ID = 33L;

    private static final String EDIT_FOUND_TEXT = "В периоде %s найдены экземпляры налоговых форм/деклараций, " +
            "которые используют предыдущие значения формы настроек подразделения. Подтверждаете изменение настроек подразделения?";
    private static final String EDIT_FOUND_TEXT_D = "Настройки используются для форм/уведомлений. Желаете внести изменения в Настройки?";

    private Department userDepartment;

    @Override
    public void onRangeChange(int start, int length) {
    }

    @ProxyCodeSplit
    @NameToken(DepartmentConfigPropertyTokens.departamentConfig)
    public interface MyProxy extends ProxyPlace<DepartmentConfigPropertyPresenter>, Place {
    }

    private final DispatchAsync dispatcher;

    public interface MyView extends ViewWithSortableTable, HasUiHandlers<DepartmentConfigPropertyUiHandlers> {
        @Override
        boolean isAscSorting();

        @Override
        void setSortByColumn(String dataStoreName);

        void setTableData(int startIndex, long count, List<Map<String, TableCell>> itemList);

        void fillNotTableData(Map<String, TableCell> itemList);

        void clearNonTableData();

        void clearTableData();

        List<DataRow<Cell>> getCheckedRows();

        List<DataRow<Cell>> getTableRows();

        void setDepartments(List<Department> departments, Set<Integer> availableDepartments);

        void setDepartment(final Department department);

        void setReportPeriods(List<ReportPeriod> reportPeriods);

        Integer getDepartmentId();

        Integer getReportPeriodId();

        RefBookColumn getParamColumn();

        void setData(List<DataRow<Cell>> data);

        StringColumn getTextColumn();

        void setTableColumns(List<RefBookAttribute> attributes);

        void setTextFieldsParams(List<RefBookAttribute> attributes);

        Map<String, TableCell> getNonTableParams();

        void setTaxType(TaxType taxType);

        TaxType getTaxType();

        DepartmentConfigPropertyView.TABLE_HEADER[] getCurrentTableHeaders();

        void setEditMode(boolean isEditable);

        boolean isFormModified();

        void setIsFormModified(boolean isModified);

        void removeResizeHandler();
        void addResizeHandler();

        void setIsUnp(boolean isUnp);

        void showUnpOnlyBlock(boolean show);
    }

    @Inject
    public DepartmentConfigPropertyPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy,
                                             DispatchAsync dispatcher, PlaceManager placeManager) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        LogCleanEvent.fire(this);
        LogShowEvent.fire(this, false);
        String value = request.getParameter("nType", "");
        TaxType nType = (value != null && !"".equals(value) ? TaxType.valueOf(value) : null);
        getView().setTaxType(nType);
        reloadDepartments(nType, null);
        getView().setEditMode(false);
        getView().addResizeHandler();
    }

    private List<Map<String, TableCell>> convert(List<DataRow<Cell>> rows) {
        List<Map<String, TableCell>> converted = new ArrayList<Map<String, TableCell>>();
        for (DataRow<Cell> row : rows) {
            Map<String, TableCell> tableRow = new HashMap<String, TableCell>();
            for (DepartmentConfigPropertyView.TABLE_HEADER h : getView().getCurrentTableHeaders()) {
                Cell cell = row.getCell(h.name());
                Object val = cell.getValue();
                TableCell tableCell = new TableCell();
                RefBookAttribute attr = getAttributeType(h.name());
                switch (attr.getAttributeType()) {
                    case STRING:
                        tableCell.setStringValue((String) val);
                        tableCell.setType(RefBookAttributeType.STRING);
                        break;
                    case REFERENCE:
                        tableCell.setRefValue((Long) val);
                        tableCell.setDeRefValue(cell.getRefBookDereference());
                        tableCell.setType(RefBookAttributeType.REFERENCE);
                        break;
                    case NUMBER:
                        tableCell.setNumberValue((Number) val);
                        tableCell.setType(RefBookAttributeType.NUMBER);
                        break;
                }
                tableRow.put(h.name(), tableCell);
            }
            converted.add(tableRow);
        }
        return converted;
    }

    @Override
    public void onSave() {
        List<DataRow<Cell>> rows = getView().getTableRows();
        saveData(convert(rows));
    }

    private RefBookAttribute getAttributeType(String alias) {
        for (RefBookAttribute attr : attributes) {
            if (attr.getAlias().equals(alias)) {
                return attr;
            }
        }

        return null;
    }

    @Override
    public void onFind() {
        getData();
    }


    private Long getCurrentRefBookId() {
        if (getView().getTaxType() == TaxType.PROPERTY) {
            return PROPERTY_REFBOOK_ID;
        } else if (getView().getTaxType() == TaxType.TRANSPORT) {
            return TRANSPORT_REFBOOK_ID;
        } else if (getView().getTaxType() == TaxType.INCOME) {
            return INCOME_REFBOOK_ID;
        }

        return null;
    }

    private Long getCurrentTableRefBookId() {
        if (getView().getTaxType() == TaxType.PROPERTY) {
            return TABLE_PROPERTY_REFBOOK_ID;
        } else if (getView().getTaxType() == TaxType.TRANSPORT) {
            return TABLE_TRANSPORT_REFBOOK_ID;
        } else if (getView().getTaxType() == TaxType.INCOME) {
            return TABLE_INCOME_REFBOOK_ID;
        }

        return null;
    }

    @Override
    public void onDelete() {
        Dialog.confirmMessage("Подтверждение операции", "Настройки подразделения будут удалены, начиная с указанного периода. Продолжить?",
                new DialogHandler() {
                    @Override
                    public void yes() {
                        super.yes();
                        DeleteConfigPropertyAction action = new DeleteConfigPropertyAction();
                        action.setRefBookId(getCurrentRefBookId());
                        action.setSlaveRefBookId(getCurrentTableRefBookId());
                        action.setReportPeriodId(getView().getReportPeriodId());
                        action.setDepartmentId(getView().getDepartmentId());
                        action.setRows(convert(getView().getTableRows()));
                        action.setRecordId(recordId);
                        action.setNotTableParams(getView().getNonTableParams());
                        dispatcher.execute(action, CallbackUtils
                                .defaultCallback(new AbstractCallback<DeleteConfigPropertyResult>() {
                                    @Override
                                    public void onSuccess(DeleteConfigPropertyResult result) {
                                        getData();
                                        LogAddEvent.fire(DepartmentConfigPropertyPresenter.this, result.getUuid());

                                    }
                                }, DepartmentConfigPropertyPresenter.this));
                    }
                });

    }

    @Override
    public void onCancel() {
        if (getView().isFormModified()) {
            Dialog.confirmMessage("Подтверждение операции", "Все не сохранённые данные будут потеряны. Выйти из режима редактирования?",
                    new DialogHandler() {
                        @Override
                        public void yes() {
                            getView().setEditMode(false);
                            getView().setIsFormModified(false);
                            getData();
                        }
                    });
        } else {
            getView().setEditMode(false);
        }
    }


    private void getData() {
        LogCleanEvent.fire(DepartmentConfigPropertyPresenter.this);
        GetRefBookValuesAction action = new GetRefBookValuesAction();
        action.setRefBookId(getCurrentRefBookId());
        action.setSlaveRefBookId(getCurrentTableRefBookId());
        action.setReportPeriodId(getView().getReportPeriodId());
        action.setDepartmentId(getView().getDepartmentId());

        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<GetRefBookValuesResult>() {
                    @Override
                    public void onSuccess(GetRefBookValuesResult result) {
                        if (result.getNotTableValues() == null || result.getNotTableValues().isEmpty()) {
                            getView().clearNonTableData();
                            getView().clearTableData();
                            DepartmentConfigPropertyPresenter.this.recordId = null;
                            return;
                        }
                        getView().fillNotTableData(result.getNotTableValues());
                        if (result.getRecordId() != null) {
                            DepartmentConfigPropertyPresenter.this.recordId = result.getRecordId();
                        }

                        if (result.getTableValues() != null && !result.getTableValues().isEmpty()) {
                            List<Map<String, TableCell>> tableData = result.getTableValues();
                            getView().setTableData(0, tableData.size(), tableData);
                        } else {
                            getView().clearTableData();
                        }
                    }
                }, this));
    }

    private void createTableColumns() {
        GetFormAttributesAction action = new GetFormAttributesAction();
        action.setRefBookId(getCurrentRefBookId());
        action.setTableRefBookId(getCurrentTableRefBookId());
        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<GetFormAttributesResult>() {
                    @Override
                    public void onSuccess(GetFormAttributesResult result) {
                        DepartmentConfigPropertyPresenter.this.attributes = result.getTableAttributes();
                        getView().setTableColumns(result.getTableAttributes());
                        getView().setTextFieldsParams(result.getAttributes());
                    }
                }, this));



    }

    private boolean isKppTaxOrgCodeFiled(List<Map<String, TableCell>> rows) {
        for (Map<String, TableCell> row : rows) {
            String kpp = row.get("KPP").getStringValue();
            String taxOrganCode = row.get("TAX_ORGAN_CODE").getStringValue();
            if (kpp == null || kpp.isEmpty()
                    || taxOrganCode == null || taxOrganCode.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void saveData(final List<Map<String, TableCell>> rows) {
        if (!checkBeforeSave(rows)) {
            return;
        }
        LogCleanEvent.fire(DepartmentConfigPropertyPresenter.this);

        final GetCheckDeclarationAction action = new GetCheckDeclarationAction();
        action.setReportPeriodId(getView().getReportPeriodId());
        action.setDepartment(getView().getDepartmentId());
        action.setTaxType(getView().getTaxType());
        dispatcher.execute(action,
                CallbackUtils.defaultCallback(
                        new AbstractCallback<GetCheckDeclarationResult>() {
                            void save() {
                                SaveDepartmentRefBookValuesAction saveAction = new SaveDepartmentRefBookValuesAction();
                                saveAction.setRows(rows);
                                saveAction.setRecordId(recordId);
                                saveAction.setReportPeriodId(getView().getReportPeriodId());
                                saveAction.setDepartmentId(getView().getDepartmentId());
                                saveAction.setNotTableParams(getView().getNonTableParams());
                                saveAction.setRefBookId(getCurrentRefBookId());
                                saveAction.setSlaveRefBookId(getCurrentTableRefBookId());
                                dispatcher.execute(saveAction, CallbackUtils
                                        .defaultCallback(new AbstractCallback<SaveDepartmentRefBookValuesResult>() {
                                            @Override
                                            public void onSuccess(SaveDepartmentRefBookValuesResult result) {
                                                if (result.isHasFatalError()) {
                                                    switch (result.getErrorType()) {
                                                        case HAS_DUPLICATES:
                                                            Dialog.errorMessage("Версия не сохранена. Обнаружены фатальные ошибки!");
                                                            break;
                                                        case INCORRECT_FIELDS:
                                                            Dialog.errorMessage("Поля блока \"Ответственный за декларацию\" заполнены некорректно");
                                                            break;
                                                    }

                                                    LogAddEvent.fire(DepartmentConfigPropertyPresenter.this, result.getUuid());
                                                } else {
                                                    getData();
                                                }
                                            }
                                        }, DepartmentConfigPropertyPresenter.this));
                            }
                            @Override
                            public void onSuccess(final GetCheckDeclarationResult result) {
                                if (result.getUuid() != null) {
                                    LogAddEvent.fire(DepartmentConfigPropertyPresenter.this, result.getUuid());
                                }
                                if (result.isDeclarationFormFound()) {
                                    Dialog.confirmMessage(getView().getTaxType().equals(TaxType.DEAL) ? EDIT_FOUND_TEXT_D : EDIT_FOUND_TEXT,
                                            new DialogHandler() {
                                                @Override
                                                public void yes() {
                                                    super.yes();
                                                    getView().setEditMode(true);

                                                    AddLogAction addLogAction = new AddLogAction();
                                                    addLogAction.setOldUUID(result.getUuid());
                                                    addLogAction.setMessages(Arrays.asList(new LogEntry(LogLevel.WARNING,
                                                            "Для актуализации данных в найденных экземплярах налоговых/форм деклараций их необходимо рассчитать/обновить")));
                                                    dispatcher.execute(addLogAction, CallbackUtils
                                                            .defaultCallback(new AbstractCallback<AddLogResult>() {
                                                                @Override
                                                                public void onSuccess(AddLogResult result) {
                                                                    LogAddEvent.fire(DepartmentConfigPropertyPresenter.this, result.getUuid());
                                                                }
                                                            }, DepartmentConfigPropertyPresenter.this));
                                                    save();
                                                }
                                            });
                                } else {
                                    save();
                                }
                            }
                        }, this));
    }

    private boolean checkBeforeSave(List<Map<String, TableCell>> rows) {
        if (!isKppTaxOrgCodeFiled(rows)) {
            Dialog.errorMessage("Не заполнены обязательные поля", "В таблице не заполнены обязательные поля \"Код налогового органа\" и \"КПП\"");
            return false;
        }
        return true;
    }


    @Override
    public void reloadDepartments(TaxType taxType, final Integer currentDepartmentId) {
        GetDepartmentTreeDataAction action = new GetDepartmentTreeDataAction();
        action.setTaxType(taxType);

        dispatcher.execute(action,
                CallbackUtils.defaultCallback(
                        new AbstractCallback<GetDepartmentTreeDataResult>() {
                            @Override
                            public void onSuccess(GetDepartmentTreeDataResult result) {
                                // Дерево подразделений
                                if (result.getAvailableDepartments() != null && result.getDepartments() != null) {
                                    getView().setDepartments(result.getDepartments(), result.getAvailableDepartments());
                                }
                                else {
                                    getView().setDepartments(new ArrayList<Department>(), new HashSet<Integer>());
                                }

                                if (result.getAvailableDepartments() != null
                                        && result.getAvailableDepartments().contains(currentDepartmentId)) {
                                    // Выбирается подразделение выбранное ранее
                                    for (Department dep : result.getDepartments()) {
                                        if (dep.getId() == currentDepartmentId) {
                                            getView().setDepartment(dep);
                                            break;
                                        }
                                    }
                                } else {
                                    // Выбирается подразделение пользователя
                                    getView().setDepartment(userDepartment);
                                }
                                // Список отчетных периодов
                                getView().setReportPeriods(result.getReportPeriods() == null
                                        ? new ArrayList<ReportPeriod>(0) : result.getReportPeriods());
//
                                createTableColumns();
                                getData();

                            }
                        }, this).addCallback(new ManualRevealCallback<GetDepartmentTreeDataAction>(this)));
    }

    @Override
    public void onDepartmentChanged() {
        if (getView().getTaxType() == TaxType.INCOME) {
            GetDepartmentAction action = new GetDepartmentAction();
            action.setDepartmentId(getView().getDepartmentId());
            dispatcher.execute(action, CallbackUtils
                    .defaultCallback(new AbstractCallback<GetDepartmentResult>() {
                        @Override
                        public void onSuccess(GetDepartmentResult result) {
                            boolean isUnp = "99_6200_00".equals(result.getDepartment().getSbrfCode());//УНП
                            getView().setIsUnp(isUnp);
                            getView().showUnpOnlyBlock(isUnp);
                        }
                    }, this));
        }
    }

    @Override
    protected void onHide() {
        super.onHide();
        getView().removeResizeHandler();
    }
}