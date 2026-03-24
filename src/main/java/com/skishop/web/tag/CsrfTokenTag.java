package com.skishop.web.tag;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.tagext.TagSupport;

public class CsrfTokenTag extends TagSupport {
    @Override
    public int doStartTag() throws JspException {
        HttpSession session = pageContext.getSession();
        if (session == null) return SKIP_BODY;
        String token = (String) session.getAttribute("_csrfToken");
        if (token == null) return SKIP_BODY;
        try {
            JspWriter out = pageContext.getOut();
            out.write("<input type=\"hidden\" name=\"_csrfToken\" value=\"");
            out.write(token);
            out.write("\" />");
        } catch (Exception e) {
            throw new JspException("Error writing CSRF token", e);
        }
        return SKIP_BODY;
    }
}
