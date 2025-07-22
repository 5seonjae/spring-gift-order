package gift.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class OperationSeparatorAspect {

    private static final String SEP = "────────────────────────────";

    @Around("execution(* gift..*(..))")
    public Object aroundServiceMethods(ProceedingJoinPoint pjp) throws Throwable {
        Logger log = LoggerFactory.getLogger(pjp.getTarget().getClass());
        String name = pjp.getSignature().toShortString();

        log.info(SEP + " START [{}] " + SEP, name);
        Object result = pjp.proceed();
        log.info(SEP + " END   [{}] " + SEP, name);

        return result;
    }
}
