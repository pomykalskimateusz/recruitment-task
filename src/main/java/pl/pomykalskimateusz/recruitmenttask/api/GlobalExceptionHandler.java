package pl.pomykalskimateusz.recruitmenttask.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.pomykalskimateusz.recruitmenttask.exception.BadRequestException;

@RestControllerAdvice
public class GlobalExceptionHandler {
  public record ErrorResponse(String message) {}

  @ExceptionHandler(BadRequestException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleBadRequest(BadRequestException ex) {
    return new ErrorResponse(ex.getMessage());
  }
}
