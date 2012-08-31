<%@ page contentType="text/html; UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<portlet:defineObjects />
<c:if test="${not empty gridLayout}"><%-- main check --%>
<c:url value="/js/LoggerPane.js" var="loggerJsUrl"/>
<script type="text/javascript" src="${loggerJsUrl}"></script>
<c:url value="/css/LoggerPane.css" var="loggerCssUrl"/>
<style type="text/css"><%-- TODO: переделать импорт CSS --%>
	@import "/portal_dojo/v1.4.3/dojox/grid/resources/Grid.css";
	@import "/portal_dojo/v1.4.3/dojox/grid/resources/tundraGrid.css";
	@import "${loggerCssUrl}";
</style>
<c:set var="namespace"><portlet:namespace/></c:set>
<portlet:resourceURL id="dataRows" var="storeUrl"/>
<portlet:resourceURL id="saveRows" var="saveUrl"/>
<script type="text/javascript">
	dojo.require('dojo.data.ItemFileWriteStore');
	dojo.require('dijit.form.Button');
	dojo.require('dojox.grid.DataGrid');
	dojo.require('dojox.grid.cells.dijit');
	dojo.require('dojo.parser');
	dojo.require('dijit.form.NumberTextBox');
	
	var ${namespace}_grid;
	var ${namespace}_store;
	dojo.addOnLoad(function() {
		dojo.parser.parse();
		function formatDate(inDatum){
			if (inDatum == null || inDatum == '') {
				return '';
			}
			return dojo.date.locale.format(new Date(inDatum), this.constraint);
		};
		function formatNumber(inDatum){
			if (null == inDatum || '' == inDatum || isNaN(inDatum)) {
				return '';
			}
			return inDatum;
		};		
		
		${namespace}_store = new dojo.data.ItemFileWriteStore({
			clearOnClose: true,
			url: '${storeUrl}'
		});
		
		${namespace}_store._saveEverything = function(successCallback, failureCallback, newContentString) {
			var processLog = function(logEntries) {
				${namespace}_loggerPane.setEntries(logEntries);
				successCallback(logEntries);
			}
			dojo.xhrPost({
				url: '${saveUrl}',
				handleAs: 'json',
				content: {
					data: newContentString
				},
				sync: true,
				load: processLog,
				error: failureCallback
		    });
		};
		
		${namespace}_grid = new dojox.grid.DataGrid({
			id: '${namespace}_grid',
			store: ${namespace}_store,
			structure: [${gridLayout}],
			rowSelector: '1em',
			rowsPerPage: 100,
			autoHeight: true,
			canSort: function(index) { return false; }			
		});		
        ${namespace}_grid.placeAt('${namespace}_gridDiv');
        ${namespace}_grid.startup();
	});
	function ${namespace}_addNewRow() {
		var newItem = {
			alias: '' + (${namespace}_grid.rowCount + 1)
		};
		${namespace}_store.newItem(newItem); 
	}
	function ${namespace}_save() {
		${namespace}_store.save();
		${namespace}_store.close();
		${namespace}_grid._refresh();
	}
</script>
<div jsid="${namespace}_loggerPane" dojoType="com.aplana.taxaccounting.LoggerPane"></div>
<div id="${namespace}_gridDiv"></div>
<button dojoType="dijit.form.Button" onClick="${namespace}_addNewRow">Добавить строку</button>
<portlet:actionURL name="save" var="saveUrl"/>
<button dojoType="dijit.form.Button" onClick="${namespace}_save">Сохранить</button>
</c:if><%-- main check --%>
<div><portlet:renderURL portletMode="view" windowState="normal" var="backUrl" />
<a href="${backUrl}">Назад</a>
</div>