<%@ page contentType="text/html; UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<h1>Сессия не инициализирована</h1>
<p>Ваша сессия истекла, либо вы использовали неправильную ссылку для перехода к данной странице</p>
<c:if test="${empty backUrl}"><portlet:renderURL portletMode="view" windowState="normal" var="backUrl"/></c:if>
<p>Для возврата к исходной странице перейдите по <a href="${backUrl}">ссылке</a></p>