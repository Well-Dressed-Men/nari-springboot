package welldressedmen.narispringboot.config.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import welldressedmen.narispringboot.dto.UpdateRequestDTO;

@Aspect
@Component
@Slf4j
public class LoggingAspect {
    @Autowired
    private ObjectMapper objectMapper;

    @Around(
            "execution(* welldressedmen.narispringboot.service.WeatherParserForUSN.parse(..))"+
            " || execution(* welldressedmen.narispringboot.service.WeatherParserForUSF.parse(..))"+
//            " || execution(* welldressedmen.narispringboot.service.WeatherParserForVF.parse(..))"+
//            " || execution(* welldressedmen.narispringboot.service.WeatherParserForMFL.parse(..))"+
//            " || execution(* welldressedmen.narispringboot.service.WeatherParserForMFT.parse(..))"+
            " || execution(* welldressedmen.narispringboot.service.WeatherParserForAP.parse(..))"
    )
    public Object logParamString(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        if (args != null && args.length > 0 && args[0] instanceof String) {
            String resData = (String) args[0];
            log.info("Method [{}.{}] called with resData = {}", className, methodName, resData);
        }

        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            log.error("Exception occurred in method [{}.{}]: {}", className, methodName, e.getMessage(), e);
            throw e;
        }
    }

    @Around("execution(* welldressedmen.narispringboot.controller.RestApiController.updateUserInfo(..))" +
            "|| execution(* welldressedmen.narispringboot.service.MemberService.updateMemberInfo(..))" +
            "|| execution(* welldressedmen.narispringboot.service.RecommendService.getFashion(..))"
    )
    public Object logParamObject(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        String methodName = joinPoint.getSignature().getName();

        logParameters(args,methodName);
        return joinPoint.proceed();

    }

    private void logParameters(Object[] args, String methodName) {
        for (Object arg : args) {
            try {
                String argJson = objectMapper.writeValueAsString(arg);
                log.info("Method [{}] called with Parameter JSON = {}", methodName, argJson);
            } catch (Exception e) {
                log.error("Method [{}] Error converting parameter to JSON", methodName, e);
            }
        }
    }
}


