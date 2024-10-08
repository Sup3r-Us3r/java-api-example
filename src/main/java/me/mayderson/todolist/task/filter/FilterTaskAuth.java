package me.mayderson.todolist.task.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.mayderson.todolist.user.IUserRepository;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {
  @Autowired
  private IUserRepository userRepository;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    var servletPath = request.getServletPath();

    if (servletPath.startsWith("/tasks")) {
      String authorization = request.getHeader("Authorization");

      String authEncoded = authorization.substring("Basic".length()).trim();
      byte[] authDecoded = Base64.getDecoder().decode(authEncoded);
      String authString = new String(authDecoded);

      String[] credentials = authString.split(":");
      String username = credentials[0];
      String password = credentials[1];

      var user = this.userRepository.findByUsername(username);

      if (user == null) {
        response.sendError(401, "User without authorization");
        return;
      }

      var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword().toCharArray());

      if (passwordVerify.verified) {
        request.setAttribute("userId", user.getId());
        filterChain.doFilter(request, response);
      } else {
        response.sendError(401, "User or password incorrect");
      }
    } else {
      filterChain.doFilter(request, response);
    }
  }
}
