package com.iminling.core.validation;

import com.iminling.core.annotation.Validate;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Configuration;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.executable.ExecutableValidator;
import java.lang.reflect.Method;
import java.util.Set;


/**
 * 参数校验切面
 * @author yslao@outlook.com
 */
@Aspect
@Configuration
public class ValidationInterceptor {

    private static final Validator VALIDATOR = ValidationUtils.getValidator();

    private static final ExecutableValidator EXECUTABLE_VALIDATOR = VALIDATOR.forExecutables();

    @Before("@annotation(com.iminling.core.annotation.Validate)")
    public void doValid(JoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Validate annotation = method.getAnnotation(Validate.class);
        Object[] args = point.getArgs();
        if (args != null && args.length > 0) {
            // 获取分组信息
            Class<?>[] groups = annotation.value();
            // 1.先校验方法上的规则
            Set<ConstraintViolation<Object>> validateResults = EXECUTABLE_VALIDATOR.validateParameters(point.getTarget(), method, args);
            ValidationUtils.operateValidateResult(method, validateResults);

            // 2. 循环校验每个方法参数bean内部
            for (Object arg : args) {
                if (arg == null) {
                    continue;
                }
                ValidationUtils.validate(arg, method, groups);
            }
        }
    }

}
