<%@ page contentType="text/html; UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<portlet:defineObjects />
<c:set var="namespace"><portlet:namespace/></c:set>
<portlet:resourceURL id="dataRows" var="storeURL"/>
<portlet:resourceURL id="saveRows" var="saveURL"/>
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
			url: '${storeURL}'
		});
		
		${namespace}_store._saveEverything = function(successCallback, failureCallback, newContentString) {
			dojo.xhrPost({
				url: '${saveURL}',
				content: {
					data: newContentString
				},
				load: successCallback,
				error: failureCallback
		    });
		};
		
		${namespace}_grid = new dojox.grid.DataGrid({
			id: '${namespace}_grid',
			store: ${namespace}_store,
			structure: [${gridLayout}],
			rowSelector: '2em',
			rowsPerPage: 100,
			autoHeight: true			
		});		
        ${namespace}_grid.placeAt('${namespace}_gridDiv');
        ${namespace}_grid.startup();
	});
	function ${namespace}_addNewRow() {
		${namespace}_store.newItem({alias: (${namespace}_grid.rowCount + 1)});
	}
	function ${namespace}_save() {
		${namespace}_store.save();
	}
</script>
<style type="text/css"><%-- TODO: переделать импорт CSS --%>
	@import "/portal_dojo/v1.4.3/dojox/grid/resources/Grid.css";
	@import "/portal_dojo/v1.4.3/dojox/grid/resources/tundraGrid.css";
</style>
<div id="${namespace}_gridDiv"></div>
<button dojoType="dijit.form.Button" onClick="${namespace}_addNewRow">Добавить строку</button>
<portlet:actionURL name="save" var="saveURL"/>
<button dojoType="dijit.form.Button" onClick="${namespace}_save">Сохранить</button>
<div><portlet:renderURL portletMode="view" windowState="normal" var="backURL" />
<a href="${backURL}">Назад</a>
</div>