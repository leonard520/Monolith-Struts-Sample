<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${not empty errors}">
  <div class="messages">
    <c:forEach var="err" items="${errors}">
      <p><c:out value="${err}"/></p>
    </c:forEach>
  </div>
</c:if>
<c:if test="${not empty error}">
  <div class="messages">
    <p><c:out value="${error}"/></p>
  </div>
</c:if>
<c:if test="${not empty successMessage}">
  <div class="messages" style="background:#e6f4ea;border-color:#a8dab5;color:#1e7e34;">
    <p><c:out value="${successMessage}"/></p>
  </div>
</c:if>
