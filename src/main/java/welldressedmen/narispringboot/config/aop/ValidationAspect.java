package welldressedmen.narispringboot.config.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import welldressedmen.narispringboot.dto.UpdateRequestDTO;

@Aspect
@Component
@Slf4j
public class ValidationAspect {

    @Before("execution(* welldressedmen.narispringboot.service.MemberService.updateMemberInfo(..)) && args(updateRequestDTO, ..)")
    public void validateUpdateMemberInfo(UpdateRequestDTO updateRequestDTO) {
        //예상치 못하게 null을 받는 경우를 대비한 예외처리
        if (updateRequestDTO == null) {
            throw new IllegalArgumentException("UpdateRequestDTO cannot be null");
        }
    }
}
