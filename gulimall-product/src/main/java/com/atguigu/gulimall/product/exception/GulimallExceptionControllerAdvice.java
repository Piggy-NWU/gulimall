package com.atguigu.gulimall.product.exception;

import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/*
统一集中处理所有异常的类
Advice是面向切面编程（AOP）中的一个概念，表示在某个切入点（Join Point）执行前、执行后或抛出异常时需要执行的逻辑。
本类本质上是一个advice.

@ResponseBody   有了这个注解，返回的对象会自动json化.
@ControllerAdvice(basePackages = "com.atguigu.gulimall.product.controller")

@RestControllerAdvice = @ResponseBody + @ControllerAdvice

@ControllerAdvice 是 Spring MVC 中的一个注解，用于定义全局的异常处理器、数据绑定器、数据预处理器等。当我们在应用程序中抛出异常时，
@ControllerAdvice可以截获这些异常并将它们转换为 HTTP 响应。通过 @ControllerAdvice 可以将异常处理逻辑从控制器中抽离出来，避免代码重复，提高代码复用性。

@ResponseBody 是 Spring MVC 中的一个注解，用于将响应体直接写入 HTTP 响应中。当我们在应用程序中返回 JSON、XML 或其他格式的数据时，
可以使用 @ResponseBody 将这些数据直接写入 HTTP 响应中，从而避免了手动操作响应体的繁琐过程。
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.atguigu.gulimall.product.controller")
public class GulimallExceptionControllerAdvice {

    // 这里只约束做了notValid的Exception校验。 后面还可以对其他Exception做校验。
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleException(MethodArgumentNotValidException e) {
        log.error("数据校验出了问题{}, 异常类型:{}", e.getMessage(), e.getClass());
        BindingResult bindingResult = e.getBindingResult();
        Map<String, String> errorMap = new HashMap<>();
        bindingResult.getFieldErrors().forEach(item -> {
            errorMap.put(item.getField(), item.getDefaultMessage());
        });
        return R.error(BizCodeEnum.VAILD_EXCEPTION.getCode(), BizCodeEnum.VAILD_EXCEPTION.getMsg()).put("data", errorMap);
    }

    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable e) {
        return R.error(BizCodeEnum.UNKNOWN_EXCEPTION.getCode(), BizCodeEnum.UNKNOWN_EXCEPTION.getMsg());
    }

}
