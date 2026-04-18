<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<div class="inner">
  <div class="logo"><html:link page="/home.do">Ski Resort Shop</html:link></div>
  <ul class="app-nav">
    <li><html:link page="/home.do">首页</html:link></li>
    <li><html:link page="/products.do">商品</html:link></li>
    <li><html:link page="/coupons/available.do">优惠券</html:link></li>
    <logic:present name="loginUser" scope="session">
      <li><html:link page="/orders.do">订单历史</html:link></li>
      <li><html:link page="/points.do">积分</html:link></li>
      <li><html:link page="/addresses.do">地址簿</html:link></li>
      <logic:equal name="loginUser" property="role" value="ADMIN">
        <li><html:link page="/admin/products.do">管理：商品</html:link></li>
        <li><html:link page="/admin/orders.do">管理：订单</html:link></li>
        <li><html:link page="/admin/coupons.do">管理：优惠券</html:link></li>
        <li><html:link page="/admin/shipping.do">管理：配送方式</html:link></li>
      </logic:equal>
    </logic:present>
    <logic:notPresent name="loginUser" scope="session">
      <li><html:link page="/login.do">登录</html:link></li>
      <li><html:link page="/register.do">注册</html:link></li>
    </logic:notPresent>
  </ul>
  <div class="actions">
    <html:form action="/products.do" method="get" styleClass="header-search">
      <input type="text" name="keyword" value="" placeholder="按商品名或品牌搜索" />
      <button type="submit">🔍</button>
    </html:form>
    <html:link page="/cart.do" styleClass="btn">🛒 购物车</html:link>
    <logic:present name="loginUser" scope="session">
      <span class="user-name">你好，<bean:write name="loginUser" property="username"/></span>
      <html:link page="/logout.do">退出登录</html:link>
    </logic:present>
    <logic:notPresent name="loginUser" scope="session">
      <html:link page="/login.do">登录</html:link>
      <html:link page="/register.do" styleClass="btn">注册</html:link>
    </logic:notPresent>
  </div>
</div>
