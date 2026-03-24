<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${not empty errors}">
    <div class="messages">
        <c:forEach items="${errors}" var="msg">
            <p class="error"><c:out value="${msg}"/></p>
        </c:forEach>
    </div>
</c:if>
<c:if test="${not empty successMessage}">
    <div class="messages success">
        <p><c:out value="${successMessage}"/></p>
    </div>
</c:if>
<c:if test="${not empty errorMessage}">
    <div class="messages">
        <p class="error"><c:out value="${errorMessage}"/></p>
    </div>
</c:if>
