//package welldressedmen.narispringboot.exception;
//
//import java.util.logging.Logger;
//
//import org.slf4j.LoggerFactory;
//import org.springframework.core.MethodParameter;
//import org.springframework.http.HttpInputMessage;
//import org.springframework.http.converter.HttpMessageConverter;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;
//
//import java.lang.reflect.Type;
//import java.util.logging.Logger;
//
//@ControllerAdvice
//public class CustomRequestBodyAdviceAdapter extends RequestBodyAdviceAdapter {
//    private Logger logger = LoggerFactory.getLogger(CustomRequestBodyAdviceAdapter.class);
//
//    @Override
//    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
//        return true;
//    }
//    @Override
//    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
//        logger.info("Body: " + body.toString());
//        return super.afterBodyRead(body, inputMessage, parameter, targetType, converterType);
//    }
//}
//
