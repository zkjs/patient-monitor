package com.fintech.hospital.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * @author baoqiang
 */
@ControllerAdvice
public class ErrorControllerAdvice extends ResponseEntityExceptionHandler {

  static final Logger LOG = LoggerFactory.getLogger("AppError");

  @ExceptionHandler({Exception.class})
  @ResponseBody
  public ResponseEntity handleInputParsingException(Throwable t) {
    LOG.error("something wrong: {}", t);
    return new ResponseEntity<>(null, BAD_REQUEST);
  }

}
