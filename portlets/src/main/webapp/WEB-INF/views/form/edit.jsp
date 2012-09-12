<%@ page contentType="text/html; UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<portlet:defineObjects />
<c:set var="namespace"><portlet:namespace /></c:set>

<style type="text/css"><%-- TODO: переделать импорт CSS --%>
	@import "/portal_dojo/v1.4.3/dojox/grid/resources/Grid.css";
	@import "/portal_dojo/v1.4.3/dojox/grid/resources/tundraGrid.css";
	@import "<c:url value="/js/codemirror/lib/codemirror.css"/>";
	table.${namespace}_propTable tbody tr td .value {
		width: 100%;
	}
	.CodeMirror {
		border: 1px solid #eee;
	}
	.CodeMirror-scroll {
		height: 100%;
	}
</style>

<%-- CodeMirror imports --%>
<script src="<c:url value="/js/codemirror/lib/codemirror.js"/>"></script>
<script src="<c:url value="/js/codemirror/mode/groovy/groovy.js"/>"></script>
<%-- End of CodeMirror imports --%>

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
	dojo.require('dijit.layout.AccordionContainer');
	dojo.require('dijit.layout.AccordionPane');
	dojo.require('dijit.layout.BorderContainer');
	dojo.require('dijit.layout.TabContainer');
	dojo.require('dijit.layout.ContentPane');
	dojo.require('dojox.grid.DataGrid');

	var ${namespace}_form = null;
	var ${namespace}_columnsGrid = null;
	var ${namespace}_createScriptEditor = null;
	var ${namespace}_calcScriptsGrid = null;
	var ${namespace}_calcScriptConditionEditor = null;
	var ${namespace}_calcScriptEditor = null;
	var ${namespace}_idCounter = 0;
	
	<%--
		Сгенерировать идентификатор
	--%>
	var ${namespace}_generateId = function() {
		return --${namespace}_idCounter;
	}
	
	<%--
		Возвращает item, соответствующий выбранному в гриде элементу, или null,
		если не выбрано ни одного элемента, или, напротив, выбрано более одного элемента \
		(вообще говоря, такое невозможно, так как во всех таблицах стоит single-выбор)
	--%>
	var ${namespace}_getGridSelectedItem = function(grid) {
		var selectedItems = grid.selection.getSelected();
		if (selectedItems == null || selectedItems.length != 1) {
			return null;
		}
		return selectedItems[0];
	}

	<%--
		Функция загружает информацию о выбранном столбце в панель для редактирования свойств столбца
		Используется при выборе строки в таблице столбцов, а также для того, чтобы отказаться от сделанных изменений
	--%>
	var ${namespace}_selectColumn = function() {
		${namespace}_columnsGrid.acceptChanges();
		var columnItem = ${namespace}_getGridSelectedItem(${namespace}_columnsGrid);
		var div = dojo.byId('${namespace}_columnPropPaneDiv');
		if (columnItem == null) {
			div.style.display = 'none';
			${namespace}_columnUpButton.attr('disabled', true);
			${namespace}_columnDownButton.attr('disabled', true);			
		} else {
			var rowIndex = ${namespace}_columnsGrid.getItemIndex(columnItem);
			${namespace}_columnUpButton.attr('disabled', rowIndex == 0);
			${namespace}_columnDownButton.attr('disabled', rowIndex == ${namespace}_columnsGrid.rowCount);
			var store = ${namespace}_columnsGrid.store;
			div.style.display = 'block';
			${namespace}_columnName.attr('value', store.getValue(columnItem, 'name'));
			${namespace}_columnAlias.attr('value', store.getValue(columnItem, 'alias'));
			${namespace}_columnWidth.attr('value', store.getValue(columnItem, 'width'));
			${namespace}_columnEditable.attr('value', store.getValue(columnItem, 'editable'));
			${namespace}_columnMandatory.attr('value', store.getValue(columnItem, 'mandatory'));
			${namespace}_columnType.attr('value', store.getValue(columnItem, 'type'));
		}
		${namespace}_columnsGrid.selectedColumnItem = columnItem;
	};
	
	<%--
		Сохранение формы
	--%>
	${namespace}_saveForm = function() {		
		var form = ${namespace}_form; 
		${namespace}_columnsGrid.store.save();
		
		if (${namespace}_calcScriptsGrid != null) {
			${namespace}_calcScriptsGrid.acceptChanges();
			${namespace}_calcScriptsGrid.store.save();
		}
		
		if (${namespace}_createScriptEditor != null) {
			if (form.createScript == null) {
				form.createScript = {
					id: ${namespace}_generateId()
				};			
			}
			form.createScript.body = ${namespace}_createScriptEditor.getValue();
		}
		
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
	
	<%--
		Создание редактора кода
	--%>
	var ${namespace}_createCodeEditor = function(textAreaId, script) {
		var scriptBody;
		if (script != null && script.body != null) {
			scriptBody = script.body;
		} else {
			scriptBody = '';
		}

		var result = CodeMirror.fromTextArea(
			dojo.byId(textAreaId), 
			{
				lineNumbers: true,
				matchBrackets: true,
				mode: 'text/x-groovy'        		 
			}
     	);
		result.setValue(scriptBody);
		return result;
	};
	
	<%--
		Берёт два элемента таблицы: выбранный и элемент, стоящий в строке с номером "выбранная строка + step",
		меняет у них значение столбца fieldName местами и пересортировывает таблицу.
		Используется для того, чтобы поменять две строки в таблице местами, также для этого нужно, чтобы
		в таблице была установлена сортировка по столбцу fieldName 
	--%>
	var ${namespace}_swapSelectedItem = function(grid, fieldName, step) {
		var store = grid.store;
		var selectedItem = ${namespace}_getGridSelectedItem(grid);
		if (selectedItem == null) {
			alert('Не выбрана строка в таблице!');
		}
		
		var selectedItemValue = store.getValue(selectedItem, fieldName); 
		var selectedItemIndex = grid.getItemIndex(selectedItem);

		var movedItemIndex = selectedItemIndex + step;
		if (movedItemIndex < 0 || movedItemIndex > grid.rowCount) {
			alert('Невозможно выполнить запрошенную операцию!');
		}
		var movedItem = grid.getItem(movedItemIndex);
		var movedItemValue = store.getValue(movedItem, fieldName);
		store.setValue(selectedItem, fieldName, movedItemValue);
		store.setValue(movedItem, fieldName, selectedItemValue);
		grid.sort();
		grid.resize();
		grid.selection.select(movedItemIndex);
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
				{field: 'name', width: '25em', name: 'Наименование'},
				{field: 'order', width: '1em', name: 'Порядок', styles: 'display: none;'}
			]],
			sortInfo: '3',
			autoHeight: true,
			autoWidth: true,
			canSort: function(index) {
				return false;
			},
			selectionMode: 'single',
			onSelected: ${namespace}_selectColumn
		});
		${namespace}_columnsGrid.selectedColumnItem = null;
		${namespace}_columnsGrid.acceptChanges = function() {
			var columnItem = this.selectedColumnItem;
			if (columnItem == null) {
				return;
			}
			var store = this.store;
			store.setValue(columnItem, 'name', ${namespace}_columnName.attr('value'));
			store.setValue(columnItem, 'alias', ${namespace}_columnAlias.attr('value'));
			store.setValue(columnItem, 'width', ${namespace}_columnWidth.attr('value'));
			store.setValue(columnItem, 'editable', ${namespace}_columnEditable.attr('value'));
			store.setValue(columnItem, 'mandatory', ${namespace}_columnMandatory.attr('value'));
			store.setValue(columnItem, 'type', ${namespace}_columnType.attr('value'));
		}
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
						<button dojoType="dijit.form.Button">
							Добавить столбец
							<script type="dojo/connect" event="onClick">
								var col = {
									id: ${namespace}_generateId(),
									name: 'Новый столбец',
									alias: 'column',
									type: 'string',
									width: 5,
									mandatory: false,
									editable: true
								};
								var item = ${namespace}_columnsGrid.store.newItem(col);
								${namespace}_columnsGrid.selection.select(${namespace}_columnsGrid.rowCount);
							</script>							
						</button>
						<button jsId="${namespace}_removeColumnButton" dojoType="dijit.form.Button">
							Удалить столбец
							<script type="dojo/connect" event="onClick">
								var columnItem = ${namespace}_getGridSelectedItem(${namespace}_columnsGrid);
								var store = ${namespace}_columnsGrid.store;
								var name = store.getValue(columnItem, 'name');
								if (confirm('Вы уверены, что хотите удалить столбец "' + name + '"?')) {
									store.deleteItem(columnItem);
									${namespace}_selectColumn();
								}								
							</script>
						</button>
						<button jsId="${namespace}_columnUpButton" dojoType="dijit.form.Button" disabled="true">
							Вверх
							<script type="dojo/connect" event="onClick">
								${namespace}_swapSelectedItem(${namespace}_columnsGrid, 'order', -1);
							</script>
						</button>
						<button jsId="${namespace}_columnDownButton" dojoType="dijit.form.Button" disabled="true">
							Вниз
							<script type="dojo/connect" event="onClick">
								${namespace}_swapSelectedItem(${namespace}_columnsGrid, 'order', 1);
							</script>
						</button>
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
				</div></div>			
				<div dojoType="dijit.layout.ContentPane" region="center">
					<div id="${namespace}_columnsGridDiv"></div>
				</div>
			</div>
		</div>
		<div dojoType="dijit.layout.ContentPane" title="Скрипт создания">
			<script type="dojo/connect" event="onShow">
				if (${namespace}_createScriptEditor != null) {
					return;
				}
      			${namespace}_createScriptEditor = ${namespace}_createCodeEditor(
					'${namespace}_createScript',
					${namespace}_form.createScript
				);
			</script>
			<textarea id="${namespace}_createScript"></textarea>
		</div>
		<div dojoType="dijit.layout.ContentPane" title="Расчётные скрипты">
			<script type="dojo/connect" event="onShow">
				if (${namespace}_calcScriptsGrid != null) {
					return;
				}
				var calcScriptsData = {
					identifier: 'id',
					items: ${namespace}_form.calcScripts
				}
				var store = new dojo.data.ItemFileWriteStore({data: calcScriptsData});
				store._saveEverything = function(saveCompleteCallback, saveFailedCallback, newFileContentString) {
					${namespace}_form.calcScripts = dojo.fromJson(newFileContentString).items;
				}
				${namespace}_calcScriptsGrid = new dojox.grid.DataGrid({
					store: store,
					structure: [[
						{field: 'name', width: '25em', name: 'Наименование', editable: true},
						{field: 'order', width: '1em', name: 'Порядок', styles: 'display: none;'}
					]],
					sortInfo: '2', <%-- сортируем по скрытому полю order --%>
					autoHeight: true,
					autoWidth: true,
					canSort: function(index) {
						return false;
					},
					selectionMode: 'single',
					onSelected: function(rowIndex) {
						this.acceptChanges();
						dojo.byId('${namespace}_calcScriptProperiesPane').style.display = 'block';
						${namespace}_calcScriptUpButton.attr('disabled', rowIndex == 0);
						${namespace}_calcScriptDownButton.attr('disabled', rowIndex == this.rowCount - 1);
						${namespace}_removeCalcScriptButton.attr('disabled', false);
						var item = this.getItem(rowIndex);
						
						${namespace}_rowScriptCheckbox.attr('value', this.store.getValue(item, 'rowScript'));
						var condition = this.store.getValue(item, 'condition');
						if (condition == null) {
							condition = '';
						}
						${namespace}_calcScriptConditionEditor.setValue(condition);
						var body = this.store.getValue(item, 'body');
						if (body == null) {
							body = '';
						}
						${namespace}_calcScriptEditor.setValue(body);
						this.selectedScriptItem = item;
					},
					onApplyEdit: function(rowIndex) {
						this.selectedScriptItem = this.getItem(rowIndex);
					}
				});
				${namespace}_calcScriptsGrid.selectedScriptItem = null;
				${namespace}_calcScriptsGrid.acceptChanges = function() {
					if (this.selectedScriptItem != null) {
						this.store.setValue(this.selectedScriptItem, 'rowScript', ${namespace}_rowScriptCheckbox.attr('value'));
						this.store.setValue(this.selectedScriptItem, 'condition', ${namespace}_calcScriptConditionEditor.getValue());
						this.store.setValue(this.selectedScriptItem, 'body', ${namespace}_calcScriptEditor.getValue());
					}
				};
				${namespace}_calcScriptsGrid.placeAt('${namespace}_calcScriptsGridDiv');
				${namespace}_calcScriptsGrid.startup();

				${namespace}_calcScriptConditionEditor = ${namespace}_createCodeEditor(
					'${namespace}_calcScriptCondition',
					null
				);
				${namespace}_calcScriptEditor = ${namespace}_createCodeEditor(
					'${namespace}_calcScript',
					null
				);
			</script>
			<div dojoType="dijit.layout.BorderContainer" style="width: 980px; height: 670px">
				<div dojoType="dijit.layout.ContentPane" region="left" style="width: 270px;">
					<button jsId="${namespace}_addRowScriptButton" dojoType="dijit.form.Button">
						Добавить
						<script type="dojo/connect" event="onClick">							
							${namespace}_calcScriptsGrid.store.fetch(
								{
									maxOrder: 1,
									onItem: function(item, request) {
										var ord = ${namespace}_calcScriptsGrid.store.getValue(item, 'order');
										if (request.maxOrder < ord) {
											request.maxOrder = ord;
										}
									},
									onComplete: function(items, request) {
										var newScript = {
											id: ${namespace}_generateId(),
											name: 'Новый скрипт',
											rowScript: true,
											condition: '',
											order: request.maxOrder + 1,
											body: ''
										}
										var newItem = ${namespace}_calcScriptsGrid.store.newItem(newScript);
										var newItemRowIndex = ${namespace}_calcScriptsGrid.getItemIndex(newItem);
										${namespace}_calcScriptsGrid.selection.select(newItemRowIndex);
									}
								}
							);
						</script>
					</button>
					<button jsId="${namespace}_removeCalcScriptButton" dojoType="dijit.form.Button" disabled="true">
						Удалить
						<script type="dojo/connect" event="onClick">
							var rowIndex = ${namespace}_calcScriptsGrid.getItemIndex(${namespace}_calcScriptsGrid.selectedScriptItem);
							${namespace}_calcScriptsGrid.store.deleteItem(${namespace}_calcScriptsGrid.selectedScriptItem);
							${namespace}_calcScriptsGrid.selectedScriptItem = null;
							if (${namespace}_calcScriptsGrid.rowCount > 0) {
								if (rowIndex == 0) {
									${namespace}_calcScriptsGrid.selection.select(0);
								} else {
									${namespace}_calcScriptsGrid.selection.select(rowIndex - 1);
								}
							} else {								
								dojo.byId('${namespace}_calcScriptProperiesPane').style.display = 'none';	
							}
						</script>
					</button>
					<button jsId="${namespace}_calcScriptUpButton" dojoType="dijit.form.Button" disabled="true">
						Вверх
						<script type="dojo/connect" event="onClick">
							${namespace}_swapSelectedItem(${namespace}_calcScriptsGrid, 'order', -1);
						</script>
					</button>
					<button jsId="${namespace}_calcScriptDownButton" dojoType="dijit.form.Button" disabled="true">
						Вниз
						<script type="dojo/connect" event="onClick">
							${namespace}_swapSelectedItem(${namespace}_calcScriptsGrid, 'order', 1);
						</script>
					</button>
					<div id="${namespace}_calcScriptsGridDiv"></div>
				</div>
				<div dojoType="dijit.layout.ContentPane" region="center">
					<div id="${namespace}_calcScriptProperiesPane" style="display: none;">
						<div>
							Строковый скрипт
							<input jsId="${namespace}_rowScriptCheckbox" dojoType="dijit.form.CheckBox" value="true"/>
						</div>
						<div>
							Условие выполнения скрипта
							<textarea id="${namespace}_calcScriptCondition" style="height: 80px;"></textarea>
						</div>
						<div>
							Тело скрипта
							<textarea id="${namespace}_calcScript"></textarea>
						</div>
					</div>
				</div>
			</div>
		</div>
		<div dojoType="dijit.layout.ContentPane" title="Строки">
			Описание строк формы
			<table id="${namespace}_rowsGrid"></table>
		</div>
	</div>	
</div>