package welldressedmen.narispringboot.Exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TemporalErrorException extends RuntimeException{
    private ErrorCode errorCode;
    public TemporalErrorException(){
        this.errorCode = ErrorCode.SERVICE_UNAVAILABLE;
    }

    @Override
    public String toString(){
        return errorCode.getMessage();
    }
}