package site.neurotriumph.www.aspect;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import site.neurotriumph.www.constant.Header;
import site.neurotriumph.www.constant.Message;
import site.neurotriumph.www.constant.TokenMarker;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Aspect
@Component
public class WithAuthTokenAspect {
  @Value("${app.secret}")
  private String appSecret;

  @Autowired
  private HttpServletRequest request;

  @Around("@annotation(site.neurotriumph.www.annotation.WithAuthToken)")
  public Object process(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
    /*
     * Obtaining and verifying a token.
     * */

    String token = ((ServletRequestAttributes) requireNonNull(RequestContextHolder.getRequestAttributes()))
      .getRequest()
      .getHeader(Header.AUTHENTICATION_TOKEN);

    if (token == null)
      throw new IllegalStateException(Message.TOKEN_NOT_SPECIFIED);

    try {
      JWT.require(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION))
        .build()
        .verify(token);
    } catch (TokenExpiredException e) {
      throw new IllegalStateException(Message.TOKEN_EXPIRED);
    } catch (JWTVerificationException e) {
      throw new IllegalStateException(Message.INVALID_TOKEN);
    }

    /*
     * Token decoding.
     * */

    DecodedJWT decodedJWT;
    try {
      decodedJWT = JWT.decode(token);
    } catch (JWTDecodeException e) {
      throw new IllegalStateException(Message.INVALID_TOKEN);
    }

    /*
     * If an object of type DecodedJWT is expected as a parameter
     * to the method, then we will replace it with the decodedJWT.
     * */

    List<Object> arguments = Arrays.asList(proceedingJoinPoint.getArgs());
    arguments.replaceAll(o -> o instanceof DecodedJWT ? decodedJWT : o);

    return proceedingJoinPoint.proceed(arguments.toArray());
  }
}
