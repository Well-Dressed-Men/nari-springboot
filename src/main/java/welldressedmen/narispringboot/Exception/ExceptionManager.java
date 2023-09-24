package welldressedmen.narispringboot.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@ControllerAdvice
public class ExceptionManager {

    @ExceptionHandler(TemporalErrorException.class)
    public ResponseEntity<?> TemporalErrorExceptionHandler(TemporalErrorException e) {
        System.out.println(e.getErrorCode().getStatus());
        System.out.println(HttpStatus.CONFLICT);
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(e.getErrorCode().getMessage());
    }
}
