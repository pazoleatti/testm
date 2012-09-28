dojo.require('dijit.form.FilteringSelect');
dojo.require('dojox.data.JsonRestStore');
dojo.require('dijit.form.NumberTextBox');
dojo.require('dojox.grid.cells.dijit');

/**
 * Метод формирует на основе данных о столбцах налоговой формы описатель
 * для колонок dojo DataGrid, а также формирует шаблон объекта для добавления новой строки,
 * в котором определены все поля, отображаемые в таблице
 * 
 * Возвращает Deferred-объект, в который передаётся объект с двумя полями:
 * columnDescriptors - список описателей столбцов формы
 * rowItemPrototype - шаблон объекта для добавления новой строки в таблицу
 */
var aplana_createGridColumnDescriptors = function(columnsStore, context) {
	var formatDate = function(inDatum){
		if (!inDatum || inDatum == null || inDatum == '') {
			return '';
		}
		return dojo.date.locale.format(new Date(inDatum), this.constraint);
	};
	var formatNumber = function(inDatum){
		if (!inDatum || null == inDatum || '' == inDatum || isNaN(inDatum)) {
			return '';
		}
		return inDatum;
	};
	var dfd = new dojo.Deferred();
	var columnDescriptors = new Array();
	var rowItemPrototype = new Object();
	columnsStore.fetch({
		sort: [{attribute: 'order'}],
		onItem: function(item, request) {
			var field = columnsStore.getValue(item, 'alias');
			rowItemPrototype[field] = null;
			var d = {
				name: columnsStore.getValue(item, 'name'),
				field: field,
				width: columnsStore.getValue(item, 'width'),
				editable: columnsStore.getValue(item, 'editable')
			};
			var type = columnsStore.getValue(item, 'type');
			if (type == 'numeric') {
				d.type = dojox.grid.cells._Widget;
				d.widgetClass = dijit.form.NumberTextBox;
				d.constraint = {
					places: columnsStore.getValue(item, 'precision')						
				};
				d.formatter = formatNumber;
			} else if (type == 'date') {
				d.type = dojox.grid.cells.DateTextBox;
				d.constraint = {
					datePattern: 'dd.MM.yyyy',
					selector: 'date'
				};
				d.formatter = formatDate;
			} else if (type == 'string') {
				rowItemPrototype[field] = '';
				var dictionaryCode = columnsStore.getValue(item, 'dictionaryCode');
				if (dictionaryCode) {
					var store = new dojox.data.JsonRestStore({
						target: context + '/dictionary/string/' + dictionaryCode, 
						idAttribute: 'value'
					});
					d.type = dojox.grid.cells._Widget;
					d.widgetClass = dijit.form.FilteringSelect;
					d.widgetProps = {
						store: store,
						searchAttr: 'value',
						valueAttr: 'value'
					};
				}
			}
			columnDescriptors.push(d);
		},
		onComplete: function(items, request) {
			dfd.callback({
				columnDescriptors: columnDescriptors, 
				rowItemPrototype: rowItemPrototype
			});
		}
	});
	return dfd;
};

/**
 * Берёт два элемента таблицы: выбранный и элемент, стоящий в строке с номером "выбранная строка + step",
 * меняет у них значение столбца fieldName местами и пересортировывает таблицу.
 * Используется для того, чтобы поменять две строки в таблице местами, также для этого нужно, чтобы
 * в таблице была установлена сортировка по столбцу fieldName 
 */
var aplana_moveGridSelectedItem = function(grid, fieldName, step) {
	var store = grid.store;
	var selectedItem = aplana_getGridSelectedItem(grid);
	if (selectedItem == null) {
		console.log('Не выбрана строка в таблице!');
	}
	var selectedItemValue = store.getValue(selectedItem, fieldName); 
	var selectedItemIndex = grid.getItemIndex(selectedItem);
	var movedItemIndex = selectedItemIndex + step;
	if (movedItemIndex < 0 || movedItemIndex > grid.rowCount) {
		console.log('Невозможно выполнить запрошенную операцию!');
	}
	var movedItem = grid.getItem(movedItemIndex);
	var movedItemValue = store.getValue(movedItem, fieldName);
	store.setValue(selectedItem, fieldName, movedItemValue);
	store.setValue(movedItem, fieldName, selectedItemValue);
	grid.sort();
	grid.resize();
	grid.selection.select(movedItemIndex);
};

/**
 * Возвращает item, соответствующий выбранному в гриде элементу, или null,
 * если не выбрано ни одного элемента, или, напротив, выбрано более одного элемента.
 * (Данный метод предназначен для работы с таблицами, в которых стоит single-выбор)
 */
var aplana_getGridSelectedItem = function(grid) {
	var selectedItems = grid.selection.getSelected();
	if (selectedItems == null) {
		if (selectedItems && selectedItems.length > 1) {
			console.log('Multiple items selected in grid');
		}		
		return null;
	}
	return selectedItems[0];
};

/**
 * Осуществляет в хранилище данных поиск максимального значения числового поля
 * Ожидается, что все значения этого поля в хранилище больше нуля, если хранилище пустое, то вернёт 0,
 * также ожидается, что у всех объектов в хранилище значение этого поля будет определено.
 * Параметры: store - хранилище, field - имя поля, значения которого будут просматриваться.
 * Возвращает Deferred-объект, в который передаётся число - максимальное найденное значение поля field в хранилище
 * store или 0, если хранилище пустое. 
 */
var aplana_findStoreMaxValue = function(store, field) {
	var dfd = new dojo.Deferred();
	store.fetch({
		max: 0,
		onItem: function(item, request) {
			var val = store.getValue(item, field);
			if (request.max < val) {
				request.max = val;
			}
		},
		onComplete: function(items, request) {
			dfd.callback(request.max);
		}
	});
	return dfd;
};
