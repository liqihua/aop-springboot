package com.liqihua.aop;


import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @Aspect定义一个切面类
 */
@Aspect
@Configuration
public class AopConfig {



    /**
     * 前置函数
     * execution(* com.liqihua.controller.*.*(..)) -> 匹配com.liqihua.controller下所有类的所有方法
     * 如：execution(* com.liqihua..*.*(..)) -> 匹配com.liqihua.所有子包下所有类的所有方法
     * @param joinPoint
     */
    @Before("execution(* com.liqihua.controller.*.*(..))")
    public void doBefore(JoinPoint joinPoint){
        System.out.println("-------------- doBefore() come !");
        //获取目标方法的参数信息
        Object[] args = joinPoint.getArgs();
        System.out.println("--- 函数收到的参数值 : "+ JSONArray.fromObject(args).toString());
        //用的最多 通知的签名
        Signature signature = joinPoint.getSignature();
        System.out.println("--- 处理目标函数名是："+signature.getName());//AOP代理的是哪一个函数
        System.out.println("--- 处理目标类名是："+signature.getDeclaringTypeName());//AOP代理的是哪一个类

        //获取RequestAttributes
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        //从获取RequestAttributes中获取HttpServletRequest的信息
        HttpServletRequest request = (HttpServletRequest) requestAttributes.resolveReference(RequestAttributes.REFERENCE_REQUEST);
        Enumeration<String> enumeration = request.getParameterNames();
        Map<String,String> paramMap = new HashMap();
        while (enumeration.hasMoreElements()){
            String param = enumeration.nextElement();
            paramMap.put(param,request.getParameter(param));
        }
        System.out.println("-- request请求收到的参数有 : "+JSONObject.fromObject(paramMap).toString());

        //joinPoint.getThis();//AOP代理类的信息
        //joinPoint.getTarget();//代理的目标对象
        //signature.getDeclaringType();//AOP代理类的类（class）信息
        //如果要获取Session信息的话，可以这样写：
        //HttpSession session = (HttpSession) requestAttributes.resolveReference(RequestAttributes.REFERENCE_SESSION);
    }

    /**
     * 后置函数-AfterReturning
     * 如果函数发生异常终止，该切面代码将不执行
     * pointcut/value：这两个属性的作用是一样的，他们都用于指定该切入点对应的切入表达式。当指定了pointcut属性值后，value属性值将会被覆盖
     * @param returnObject
     */
    @AfterReturning(returning = "returnObject", pointcut = "execution(* com.liqihua.controller.*.*(..))")
    public void doAfterReturning(Object returnObject){
        System.out.println("-------------- doAfterReturning() come !");
        System.out.println("--- 目标函数的返回值是："+returnObject.toString());
    }

    /**
     * 后置异常函数
     * @param joinPoint
     * @param ex
     */
    @AfterThrowing(throwing = "ex", pointcut = "execution(* com.liqihua.controller.*.*(..))")
    public void doTrowing(JoinPoint joinPoint,Throwable ex) {
        System.out.println("-------------- doTrowing() come !");
        System.out.println("--- 函数："+joinPoint.getSignature().getName()+"发生异常：");
        if(ex instanceof NullPointerException){
            System.out.println("发生了空指针异常");
        }
    }


    /**
     * 后置函数-doAfter
     * 如果函数发生异常终止，该切面代码也会继续执行
     */
    @After(value = "execution(* com.liqihua.controller.*.*(..))")
    public void doAfter(){
        System.out.println("-------------- doAfter() come !");
    }




}
