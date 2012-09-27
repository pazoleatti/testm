dojo.provide('aplana.LoggerPane');
dojo.require('dijit._Widget');
dojo.require('dijit._Templated');

dojo.declare('aplana.LoggerPane', [dijit._Widget, dijit._Templated], {
	url: null,
    data: [],
    templateString: dojo.cache('aplana', 'templates/LoggerPane.html'),
    clear: function() {
    	this.entries.innerHTML = '';
    },
	add: function(logEntry) {
		var node = document.createElement('li');
		node.className = logEntry.level;
		node.appendChild(document.createTextNode(logEntry.message));
		this.entries.appendChild(node);
	},
	setEntries: function(logEntries) {
		this.clear();
		var widget = this;
		dojo.forEach(logEntries, function(logEntry) {widget.add(logEntry);});
	},
	reload: function() {
		var widget = this;
		dojo.xhrGet({
			url: this.url,
    		handleAs: 'json',
    		load: function(logEntries) {
    			widget.setEntries(logEntries);
    		},
    		error: function(error) {
    			alert('Не удалось обновить содержимое журнала: ' + error);
    		}
		});
	}
});


