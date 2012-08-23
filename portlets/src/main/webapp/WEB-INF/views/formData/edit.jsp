<%@ page contentType="text/html; UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<portlet:defineObjects />
<c:set var="namespace"><portlet:namespace/></c:set>
<script type="text/javascript">
	dojo.require('dojox.grid.DataGrid');
	dojo.require('dojox.data.JsonRestStore');
	dojo.require('dojo.parser');
	dojo.addOnLoad(function() { dojo.parser.parse();});	
</script>
<style type="text/css"><%-- TODO: переделать импорт CSS --%>
  @import "/portal_dojo/v1.4.3/dojox/grid/resources/Grid.css";
  @import "/portal_dojo/v1.4.3/dojox/grid/resources/tundraGrid.css";
</style>
<portlet:resourceURL id="dataRows" var="storeURL"/>
<div id="${namespace}_Store" jsid="${namespace}_Store" dojoType="dojox.data.JsonRestStore" target="${storeURL}" idAttribute="code"></div>
<table id="${namespace}_Grid" 
		jsid="${namespace}_Grid" 
		dojoType="dojox.grid.DataGrid" 
		store="${namespace}_Store"
		rowsPerPage="100" 
		style="height: 800px;">
	<thead>
		<tr>
			<th field="code" width="100px">#</th>
			<c:forEach var="column" items="${formData.form.columns}">
				<th field="${column.alias}" width="100px">${column.name}</th>
			</c:forEach>
		</tr>
	</thead>
</table>
<portlet:renderURL portletMode="view" windowState="normal" var="backURL" />
<a href="${backURL}">Назад</a>
