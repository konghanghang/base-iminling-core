package com.iminling.core.validation;

import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.hibernate.validator.internal.engine.path.NodeImpl;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

import javax.validation.*;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Set;

/**
 * 校验工具
 * @author yslao@outlook.com
 */
public class ValidationUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationUtils.class);

    private static final String MSG_DEFAULT_CONTENT = "参数校验不通过";

    private static final ResourceBundleMessageInterpolator INTERPOLATOR = new ResourceBundleMessageInterpolator();

    private static ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    private static Validator validator = Validation.byDefaultProvider().configure().messageInterpolator(new MessageInterpolator() {
        @Override
        public String interpolate(String s, Context context) {
            return INTERPOLATOR.interpolate(s, context, Locale.SIMPLIFIED_CHINESE);
        }

        @Override
        public String interpolate(String s, Context context, Locale locale) {
            return INTERPOLATOR.interpolate(s, context, Locale.SIMPLIFIED_CHINESE);
        }
    }).buildValidatorFactory().getValidator();

    static Validator getValidator() {
        return validator;
    }

    private ValidationUtils() {
        super();
    }

    static <T> void validate(T object, Method method, Class[] groups) {
        if (groups == null) {
            groups = new Class[0];
        }
        Set<ConstraintViolation<Object>> validates = validator.validate(object, groups);
        // 处理校验结果
        operateValidateResult(method, validates);
    }

    static void operateValidateResult(Method method, Set<ConstraintViolation<Object>> validates) {
        if (validates != null && !validates.isEmpty()) {
            // 创建异常
            ConstraintViolation<Object> next = validates.iterator().next();
            String message = MSG_DEFAULT_CONTENT + ": " + messageWrapper(next, method);
            throw new ValidationException(message);
        }
    }

    private static String messageWrapper(ConstraintViolation<?> constraintViolation, Method method) {
        ConstraintViolationImpl<?> violation = (ConstraintViolationImpl<?>) constraintViolation;
        try {
            PathImpl path = (PathImpl) violation.getPropertyPath();
            ElementKind kind = path.getLeafNode().getKind();
            if (ElementKind.PROPERTY == kind) {
                return constraintViolation.getPropertyPath() + constraintViolation.getMessage();
            }

            if (ElementKind.PARAMETER == kind) {
                NodeImpl node = path.getLeafNode();
                String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
                if (parameterNames != null) {
                    String parameterName = parameterNames[node.getParameterIndex()];
                    return parameterName + constraintViolation.getMessage();
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return constraintViolation.getMessage();
    }

}
