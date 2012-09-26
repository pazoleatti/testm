var aplana_createGridColumnDescriptors = function(columnsStore) {
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
	columnsStore.fetch({
		sort: [{attribute: 'order'}],
		onItem: function(item, request) {
			var d = {
				name: columnsStore.getValue(item, 'name'),
				field: columnsStore.getValue(item, 'alias'),
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
			}
			columnDescriptors.push(d);
		},
		onComplete: function(items, request) {
			dfd.callback(columnDescriptors);
		}
	});
	return dfd;
};