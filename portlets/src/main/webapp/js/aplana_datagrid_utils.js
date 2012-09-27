dojo.require('dijit.form.FilteringSelect');
dojo.require('dojox.data.JsonRestStore');
dojo.require('dijit.form.NumberTextBox');
dojo.require('dojox.grid.cells.dijit');

var aplana_createGridColumnDescriptors = function(columnsStore, context) {
	var formatDate = function(inDatum){
		if (!inDatum || inDatum == null || inDatum == '') {
			return ' ';
		}
		return dojo.date.locale.format(new Date(inDatum), this.constraint);
	};
	var formatNumber = function(inDatum){
		if (!inDatum || null == inDatum || '' == inDatum || isNaN(inDatum)) {
			return ' ';
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