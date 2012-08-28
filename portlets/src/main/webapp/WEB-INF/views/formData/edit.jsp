<%@ page contentType="text/html; UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<portlet:defineObjects />
<c:set var="namespace"><portlet:namespace/></c:set>
<script type="text/javascript">
	dojo.require('dojo.data.ItemFileWriteStore');
	dojo.require('dijit.form.Button');
	dojo.require('dojox.grid.DataGrid');
	dojo.require('dojox.grid.cells.dijit');
	dojo.require('dojo.parser');
	dojo.require('dijit.form.NumberTextBox');
	
	var ${namespace}_grid;
	
	dojo.addOnLoad(function() {
		dojo.parser.parse();
		formatDate = function(inDatum){
			if (inDatum == null) {
				return '';
			}
			return dojo.date.locale.format(new Date(inDatum), this.constraint);
		};
		
		getNumberFormatter = function(precision) {
			return function(inDatum) {
				var widget = new dijit.form.NumberTextBox({
					constraints: { places: precision },
					value: isNaN(inDatum) ? null : inDatum,
					style: 'width: 100%;'
				});
				return widget; 
			}
		};

		getNumber = function(value) {
			console.log(dojo.toJson(value));
			return value;
		}
		
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
</script>
<style type="text/css"><%-- TODO: переделать импорт CSS --%>
	@import "/portal_dojo/v1.4.3/dojox/grid/resources/Grid.css";
	@import "/portal_dojo/v1.4.3/dojox/grid/resources/tundraGrid.css";
</style>
<portlet:resourceURL id="dataRows" var="storeURL"/>
<div id="${namespace}_store" jsid="${namespace}_store" dojoType="dojo.data.ItemFileWriteStore" url="${storeURL}"></div>
<div id="${namespace}_gridDiv"></div>
<button dojoType="dijit.form.Button" onClick="${namespace}_addNewRow">Добавить строку</button>
<portlet:actionURL name="save" var="saveURL"/>
<button dojoType="dijit.form.Button" onClick="window.location.href = '${saveURL}'">Сохранить</button>
<div><portlet:renderURL portletMode="view" windowState="normal" var="backURL" />
<a href="${backURL}">Назад</a>
</div>