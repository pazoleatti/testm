<%@ page contentType="text/html; UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<portlet:defineObjects />
<c:url value="/js/LoggerPane.js" var="loggerJsUrl"/>
<script type="text/javascript" src="${loggerJsUrl}"></script>
<script src="<c:url value="/js/aplana_datagrid_utils.js"/>"></script>
<c:url value="/css/LoggerPane.css" var="loggerCssUrl"/>
<style type="text/css"><%-- TODO: переделать импорт CSS --%>
	@import "/portal_dojo/v1.4.3/dojox/grid/resources/Grid.css";
	@import "/portal_dojo/v1.4.3/dojox/grid/resources/tundraGrid.css";
	@import "${loggerCssUrl}";
</style>
<c:set var="namespace"><portlet:namespace/></c:set>
<portlet:resourceURL id="dataRows" var="storeUrl"/>
<portlet:resourceURL id="log" var="logUrl"/>
<portlet:resourceURL id="saveRows" var="saveUrl"/>
<script type="text/javascript">
	dojo.require('dojo.data.ItemFileReadStore');
	dojo.require('dojo.data.ItemFileWriteStore');
	dojo.require('dijit.form.Button');
	dojo.require('dojox.grid.DataGrid');
	dojo.require('dojox.grid.cells.dijit');
	dojo.require('dojo.parser');
	dojo.require('dijit.form.NumberTextBox');
	
	var ${namespace}_grid;
	var ${namespace}_store;
	var ${namespace}_loggerPane;
	dojo.addOnLoad(function() {
		dojo.parser.parse();

		${namespace}_store = new dojo.data.ItemFileWriteStore({
			clearOnClose: true,
			url: '${storeUrl}'
		});
		
		${namespace}_store._saveEverything = function(successCallback, failureCallback, newContentString) {
			var processLog = function(logEntries) {
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
		var columnsStore = new dojo.data.ItemFileReadStore({
			data: ${formColumnsData}
		});
		aplana_createGridColumnDescriptors(columnsStore).addCallback(function(columnDescriptors){
			columnsStore.close();
			${namespace}_grid = new dojox.grid.DataGrid({
				id: '${namespace}_grid',
				store: ${namespace}_store,
				structure: [columnDescriptors],
				autoHeight: true,
				autoWidth: true,
				canSort: function(index) { return false; }			
			});		
        	${namespace}_grid.placeAt('${namespace}_gridDiv');
        	${namespace}_grid.startup();
		});
        
		${namespace}_loggerPane = new com.aplana.taxaccounting.LoggerPane(
			{ url: '${logUrl}'}, 
			dojo.byId('${namespace}_loggerPane')
		);
        ${namespace}_loggerPane.reload();
	});
</script>
<div>
<button dojoType="dijit.form.Button" onClick="${namespace}_addNewRow">
	Добавить строку
	<script type="dojo/connect" event="onClick">
		var newItem = {
			alias: '' + (${namespace}_grid.rowCount + 1)
		};
		${namespace}_store.newItem(newItem);
	</script>
</button>
<portlet:actionURL name="save" var="saveUrl"/>
<button dojoType="dijit.form.Button" onClick="${namespace}_save">
	Сохранить
	<script type="dojo/connect" event="onClick">
		${namespace}_store.save();
		${namespace}_store.close();
		${namespace}_grid._refresh(true);
		${namespace}_loggerPane.reload();
	</script>
</button>
<portlet:renderURL portletMode="view" windowState="normal" var="backUrl" />
<button dojoType="dijit.form.Button" onClick="window.location.href = '${backUrl}'">Отмена</button>
<div id="${namespace}_loggerPane"></div>
<div id="${namespace}_gridDiv"></div>
