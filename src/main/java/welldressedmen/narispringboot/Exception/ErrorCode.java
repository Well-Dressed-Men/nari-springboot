package welldressedmen.narispringboot.Exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    DUPLICATED_USER_ID(HttpStatus.CONFLICT, "아이디가 중복되었습니다"),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "invalid parameter"),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "기상청 서버의 일시적인 문제로 요청을 처리할 수 없습니다.");

    private HttpStatus status;
    private String message;
}

