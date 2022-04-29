package site.neurotriumph.www.aspect;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import site.neurotriumph.www.annotation.AuthTokenPayload;
import site.neurotriumph.www.annotation.ConfirmationTokenPayload;
import site.neurotriumph.www.annotation.WithConfirmationToken;
import site.neurotriumph.www.constant.Message;
import site.neurotriumph.www.constant.TokenMarker;
import site.neurotriumph.www.pojo.ConfirmationRequestBody;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Aspect
@Component
public class WithConfirmationTokenAspect {
  @Value("${app.secret}")
  private String appSecret;

  @Around("@annotation(site.neurotriumph.www.annotation.WithConfirmationToken)")
  public Object process(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
    /*
     * Obtaining and verifying a token.
     * */

    Object[] arguments = proceedingJoinPoint.getArgs();
    ConfirmationRequestBody confirmationRequestBody = (ConfirmationRequestBody) Stream.of(arguments)
      .filter(o -> o instanceof ConfirmationRequestBody)
      .findAny()
      .orElseThrow(() -> new RuntimeException(Message.CONFIRMATION_REQUEST_BODY_REQUIRED));

    TokenMarker tokenMarker = ((MethodSignature) proceedingJoinPoint.getSignature())
      .getMethod()
      .getAnnotation(WithConfirmationToken.class)
      .value();

    try {
      JWT.require(Algorithm.HMAC256(appSecret + tokenMarker))
        .build()
        .verify(confirmationRequestBody.getToken());
    } catch (JWTVerificationException e) {
      throw new IllegalStateException(Message.INVALID_TOKEN);
    }

    /*
     * Token decoding.
     * */

    DecodedJWT decodedJWT;
    try {
      decodedJWT = JWT.decode(confirmationRequestBody.getToken());
    } catch (JWTDecodeException e) {
      throw new IllegalStateException(Message.INVALID_TOKEN);
    }

    /*
     * If an object of type DecodedJWT with annotation @ConfirmationTokenPayload
     * is expected as a parameter to the method, then we will replace it
     * with the decodedJWT.
     * */

    Annotation[][] annotations = ((MethodSignature) proceedingJoinPoint.getSignature())
      .getMethod()
      .getParameterAnnotations();

    for (int i = 0; i < arguments.length; i++) {
      if (!(arguments[i] instanceof DecodedJWT)) {
        continue;
      }

      for (int j = 0; j < annotations[i].length; j++) {
        if (!annotations[i][j].annotationType()
          .equals(ConfirmationTokenPayload.class)) {
          continue;
        }

        arguments[i] = decodedJWT;
        break;
      }
    }

    return proceedingJoinPoint.proceed(arguments);
  }
}