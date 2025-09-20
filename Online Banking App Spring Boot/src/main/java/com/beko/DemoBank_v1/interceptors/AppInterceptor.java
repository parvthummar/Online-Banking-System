package com.beko.DemoBank_v1.interceptors;

import com.beko.DemoBank_v1.exception.CustomError;
import com.beko.DemoBank_v1.helpers.authorization.JwtService;
import com.beko.DemoBank_v1.models.User;
import com.beko.DemoBank_v1.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AppInterceptor implements HandlerInterceptor {

    public UserRepository userRepository;

    @Autowired
    public AppInterceptor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private JwtService jwtService = new JwtService();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException, CustomError {
        System.out.println("In Pre Handle Interceptor Method");

        if (request.getRequestURI().startsWith("/app") || request.getRequestURI().startsWith("/transact") || request.getRequestURI().startsWith("/logout") || request.getRequestURI().startsWith("/account")) {

            String header = request.getHeader("Authorization");

            if (jwtService.isTokenIncluded(header) == false)
                throw new CustomError("You need to be logged in.", HttpServletResponse.SC_UNAUTHORIZED);

            String token = jwtService.getAccessTokenFromHeader(header);

            Claims claims = jwtService.decodeToken(token);
            String email = claims.getSubject();

            User user = userRepository.getUserDetails(email);

            request.getSession().setAttribute("user", user);
            request.getSession().setAttribute("token", token);

            if (user == null) {
                throw new CustomError("You need to be logged in.", HttpServletResponse.SC_UNAUTHORIZED);
            }
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        System.out.println("After Handle Interceptor Method");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        System.out.println("After Completion Interceptor Method");
    }
}
