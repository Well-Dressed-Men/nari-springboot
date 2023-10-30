package welldressedmen.narispringboot.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
@Slf4j

@ControllerAdvice
public class ExceptionManager {

    @ExceptionHandler(TemporalErrorException.class)
    public ResponseEntity<?> TemporalErrorExceptionHandler(TemporalErrorException e) {
        log.info("statusCode, message = {}", e.getErrorCode().getStatus());
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(e.getErrorCode().getMessage());
    }
}
