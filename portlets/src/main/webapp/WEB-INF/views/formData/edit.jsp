<%@ page contentType="text/html; UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<portlet:defineObjects/>
<c:set var="namespace"><portlet:namespace/></c:set>
<h1>Здесь будут выводиться данные по выбранной/создаваемой налоговой форме</h1>
<portlet:renderURL portletMode="view" windowState="normal" var="backURL"/>
<a href="${backURL}">Назад</a>
