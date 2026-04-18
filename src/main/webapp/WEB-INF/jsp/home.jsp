<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<div class="hero">
	<h1>欢迎来到 Azure SkiShop</h1>
	<p>用最高品质的滑雪与单板滑雪装备，开启您的冬日冒险之旅。</p>
	<div class="hero-actions">
		<html:link page="/products.do" styleClass="btn">浏览商品</html:link>
	</div>
</div>

<h2 class="page-title">推荐滑雪装备</h2>
<logic:notEmpty name="featuredProducts">
	<div class="products-grid">
		<logic:iterate id="product" name="featuredProducts">
			<bean:define id="pid" name="product" property="id" type="java.lang.String"/>
			<div class="product-card">
				<div class="name">
					<html:link page="/product.do" paramId="id" paramName="product" paramProperty="id">
						<bean:write name="product" property="name"/>
					</html:link>
				</div>
				<div class="price">¥<bean:write name="product" property="price"/></div>
				<div class="tags">
					<logic:notEmpty name="product" property="brand"><span class="tag"><bean:write name="product" property="brand"/></span></logic:notEmpty>
				</div>
				<div>
					<html:link page="/product.do" paramId="id" paramName="product" paramProperty="id" styleClass="btn">查看详情</html:link>
				</div>
			</div>
		</logic:iterate>
	</div>
</logic:notEmpty>
<logic:empty name="featuredProducts">
	<p>推荐商品正在准备中。</p>
</logic:empty>
