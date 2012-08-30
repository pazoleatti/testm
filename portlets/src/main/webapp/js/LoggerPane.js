dojo.provide('com.aplana.taxaccounting.LoggerPane');
dojo.require('dijit._Widget');
dojo.require('dijit._Templated');

dojo.declare('com.aplana.taxaccounting.LoggerPane', [dijit._Widget, dijit._Templated], {
    data: [],
    templateString: '<div class="aplanaLoggerPane"><ul dojoAttachPoint="entries"></ul></div>',
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
	}
});


