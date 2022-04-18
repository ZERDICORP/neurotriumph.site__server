package site.neurotriumph.www.exception;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import site.neurotriumph.www.pojo.ErrorResponseBody;

import javax.validation.ConstraintViolationException;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
    MethodArgumentNotValidException ex,
    HttpHeaders headers,
    HttpStatus status,
    WebRequest request) {

    List<String> errors = ex.getBindingResult()
      .getFieldErrors()
      .stream()
      .map(DefaultMessageSourceResolvable::getDefaultMessage).toList();

    return new ResponseEntity<>(new ErrorResponseBody(errors.get(0)), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler({
    ConstraintViolationException.class,
    IllegalStateException.class
  })
  public ResponseEntity<Object> inputValidationException(Exception ex) {
    return new ResponseEntity<>(new ErrorResponseBody(ex.getMessage()), HttpStatus.BAD_REQUEST);
  }
}