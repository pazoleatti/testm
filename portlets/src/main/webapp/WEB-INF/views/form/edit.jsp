<%@ page contentType="text/html; UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<portlet:defineObjects />
<c:set var="namespace"><portlet:namespace /></c:set>

<style type="text/css"><%-- TODO: переделать импорт CSS --%>
	@import "/portal_dojo/v1.4.3/dojox/grid/resources/Grid.css";
	@import "/portal_dojo/v1.4.3/dojox/grid/resources/tundraGrid.css";
	table.${namespace}_propTable tbody tr td .value {
		width: 100%;
	}
</style>

<portlet:resourceURL var="getFormUrl" id="getForm"/>
<portlet:resourceURL var="saveFormUrl" id="saveForm"/>
<portlet:renderURL var="refreshUrl"/>

<script type="text/javascript">
	dojo.require('dojo.parser');
	dojo.require('dojo.data.ItemFileWriteStore');
	dojo.require('dijit.form.Button');
	dojo.require('dijit.form.TextBox');
	dojo.require('dijit.form.NumberTextBox');
	dojo.require('dijit.form.CheckBox');
	dojo.require('dijit.form.Textarea');	
	dojo.require('dijit.form.Select');
	dojo.require('dijit.layout.BorderContainer');
	dojo.require('dijit.layout.TabContainer');
	dojo.require('dijit.layout.ContentPane');
	dojo.require('dojox.grid.DataGrid');

	var ${namespace}_form;
	var ${namespace}_columnsGrid;
	var ${namespace}_idCounter = 0;

	<%--
		Функция загружает информацию о выбранном столбце в панель для редактирования свойств столбца
		Используется при выборе строки в таблице столбцов, а также для того, чтобы отказаться от сделанных изменений
	--%>
	var ${namespace}_selectColumn = function() {
		var selectedItems = ${namespace}_columnsGrid.selection.getSelected();
		var columnItem = null;
		if (selectedItems != null && selectedItems.length == 1) {
			columnItem = selectedItems[0];
		}
		var div = dojo.byId('${namespace}_columnPropPaneDiv');
		if (columnItem == null) {
			div.style.display = 'none';
		} else {
			var store = ${namespace}_columnsGrid.store;
			div.style.display = 'block';
			${namespace}_columnName.attr('value', store.getValue(columnItem, 'name'));
			${namespace}_columnAlias.attr('value', store.getValue(columnItem, 'alias'));
			${namespace}_columnWidth.attr('value', store.getValue(columnItem, 'width'));
			${namespace}_columnEditable.attr('value', store.getValue(columnItem, 'editable'));
			${namespace}_columnMandatory.attr('value', store.getValue(columnItem, 'mandatory'));
			${namespace}_columnType.attr('value', store.getValue(columnItem, 'type'));
		}
	};
	
	<%--
		Функция принимает значения из формы редактирования параметров столбца и сохраняет их в DataStore
	--%>
	var ${namespace}_acceptColumn = function() {
		var selectedItems = ${namespace}_columnsGrid.selection.getSelected();
		if (selectedItems == null || selectedItems.length != 1) {
			alert('Не выбран столбец!');
			return;
		}
		var columnItem = selectedItems[0];
		var store = ${namespace}_columnsGrid.store; 
		store.setValue(columnItem, 'name', ${namespace}_columnName.attr('value'));
		store.setValue(columnItem, 'alias', ${namespace}_columnAlias.attr('value'));
		store.setValue(columnItem, 'width', ${namespace}_columnWidth.attr('value'));
		store.setValue(columnItem, 'editable', ${namespace}_columnEditable.attr('value'));
		store.setValue(columnItem, 'mandatory', ${namespace}_columnMandatory.attr('value'));
		
		var type = ${namespace}_columnType.attr('value');
		store.setValue(columnItem, 'type', type);
	};
	
	<%-- 
		Добавление нового столбца
	--%>
	${namespace}_addColumn = function() {
		var col = {
			id: --${namespace}_idCounter,
			name: 'Новый столбец',
			alias: 'column',
			type: 'string',
			width: 5,
			mandatory: false,
			editable: true
		};
		var item = ${namespace}_columnsGrid.store.newItem(col);
		${namespace}_columnsGrid.selection.select(${namespace}_columnsGrid.rowCount);
	}

	<%-- 
		Сохранение формы
	--%>
	${namespace}_saveForm = function() {
		${namespace}_columnsGrid.store.save();
		dojo.xhrPost({
			url: '${saveFormUrl}',
			content: {
				formData: dojo.toJson(${namespace}_form)
			},
			sync: true,
			load: function(data) {
				window.location.href = '${refreshUrl}';
			},
			error: function(error) {
				alert(error);
			}
		});
	}

	dojo.addOnLoad(function(){
		dojo.parser.parse();
		
		dojo.xhrGet({
			url: '${getFormUrl}',
			handleAs: 'json',
			sync: true,
			preventCache: true,
			load: function(data) {
				${namespace}_form = data;
			},
			error: function(error) {
				alert(error);
			}
		});
		
		var columnsData = {
			identifier: 'id',
			items: ${namespace}_form.columns
		}
		
		var store = new dojo.data.ItemFileWriteStore({data: columnsData});
		store._saveEverything = function(saveCompleteCallback, saveFailedCallback, newFileContentString) {
			${namespace}_form.columns = dojo.fromJson(newFileContentString).items;
		}
		${namespace}_columnsGrid = new dojox.grid.DataGrid({
			store: store,
			structure: [[
				{field: 'alias', width: '25em', name: 'Алиас'},
				{field: 'name', width: '25em', name: 'Наименование'}
			]],
			autoHeight: true,
			autoWidth: true,
			canSort: function(index) {
				return false;
			},
			selectionMode: 'single',
			onSelected: ${namespace}_selectColumn
		});		
		${namespace}_columnsGrid.placeAt('${namespace}_columnsGridDiv');
		${namespace}_columnsGrid.startup();
		${namespace}_selectColumn();
	});
</script>

<div dojoType="dijit.layout.BorderContainer" style="width: 1000px; height: 750px">
	<div dojoType="dijit.layout.ContentPane" region="top">
		Параметры формы
		<div style="float: right;">
			<portlet:renderURL portletMode="view" var="backUrl" />
			<button dojoType="dijit.form.Button" onClick="window.location.href = '${backUrl}'">Отмена</button>
			<button dojoType="dijit.form.Button" onClick="${namespace}_saveForm();">Сохранить</button>
		</div>
	</div>
	<div dojoType="dijit.layout.TabContainer" style="width: 1000px; height: 750px;" region="center">
		<div dojoType="dijit.layout.ContentPane" title="Столбцы">
			<div dojoType="dijit.layout.BorderContainer" style="width: 980px; height: 740px">
				<div dojoType="dijit.layout.ContentPane" region="top">
					Список столбцов
					<div style="float: right;">
						<button dojoType="dijit.form.Button" onClick="${namespace}_addColumn"/>Добавить столбец</button>
					</div>
				</div>
				<div dojoType="dijit.layout.ContentPane" region="right" style="width: 250px"><div id="${namespace}_columnPropPaneDiv" style="display:none;">
					<table class="${namespace}_propTable"><%-- Свойства выбранного столбца --%>
						<thead><col width="40%"/><col width="60%"/></thead>
						<tbody>
							<tr>
								<td>Название</td>
								<td><input jsId="${namespace}_columnName" dojoType="dijit.form.Textarea" class="value"/></td>
							</tr>
							<tr>
								<td>Алиас</td>
								<td><input jsId="${namespace}_columnAlias" dojoType="dijit.form.TextBox" class="value"/></td>
							</tr>
							<tr>
								<td>Ширина</td>
								<td><input jsId="${namespace}_columnWidth" dojoType="dijit.form.NumberTextBox" class="value"/></td>
							</tr>
							<tr>
								<td>Обязательный</td>
								<td><input jsId="${namespace}_columnMandatory" dojoType="dijit.form.CheckBox" value="true"/></td>
							</tr>
							<tr>
								<td>Редактируемый</td>
								<td><input jsId="${namespace}_columnEditable" dojoType="dijit.form.CheckBox" value="true"/></td>
							</tr>						
							<tr>
								<td>Тип</td>
								<td>
									<select jsId="${namespace}_columnType" dojoType="dijit.form.Select" class="value">
										<option value="string">Строка/Текст</option>
										<option value="date">Дата</option>
										<option value="numeric">Число</option>
									</select>
								</td>
							</tr>
						</tbody>
					</table>
					<button dojoType="dijit.form.Button" onClick="${namespace}_acceptColumn">Применить</button>
					<button dojoType="dijit.form.Button" onClick="${namespace}_selectColumn">Отменить изменения</button>
				</div></div>			
				<div dojoType="dijit.layout.ContentPane" region="center">
					<div id="${namespace}_columnsGridDiv"></div>
				</div>
			</div>
		</div>
		<div dojoType="dijit.layout.ContentPane" title="Строки">
			Описание строк формы
			<table id="${namespace}_rowsGrid"></table>
		</div>
		<div dojoType="dijit.layout.ContentPane" title="Скрипты">
			Описание скриптов
			<table id="${namespace}_scriptsGrid"></table>
		</div>
	</div>	
</div>