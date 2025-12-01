package com.example.capstone_design.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Enumeration;

@Component
public class DebugFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;

        System.out.println("========== DebugFilter: Incoming Request ==========");
        System.out.println(req.getMethod() + " " + req.getRequestURI());

        Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String header = headerNames.nextElement();
            System.out.println(header + " : " + req.getHeader(header));
        }
        System.out.println("===================================================");

        chain.doFilter(request, response);
    }
}
