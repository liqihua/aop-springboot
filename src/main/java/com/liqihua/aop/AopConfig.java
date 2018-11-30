package com.liqihua.aop;


import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @Aspect定义一个切面类
 */
@Aspect
@Configuration
public class AopConfig {
    private static final Logger log = LoggerFactory.getLogger(AopConfig.class);



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


    /**
     * 环绕日志以及参数处理
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around(value = "execution (* com.liqihua.controller.*.*(..))")
    public Object controllerAround(ProceedingJoinPoint joinPoint) throws Throwable{
        /**
         * 请求日志打印
         */
        String classAndMethodName = null;
        //获取当前请求属性集
        ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        //获取请求
        HttpServletRequest request = sra.getRequest();
        //获取请求地址
        String requestUrl = request.getRequestURL().toString();
        //记录请求地址
        log.info("请求路径[{}]", requestUrl);
        //记录请求开始时间
        long startTime = System.currentTimeMillis();
        try {
            Class<?> target = joinPoint.getTarget().getClass();
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            Class<?>[] paramTypes = methodSignature.getParameterTypes();
            String methodName = joinPoint.getSignature().getName();
            //获取当前执行方法
            Method currentMethod = target.getMethod(methodName, paramTypes);
            //拼接输出字符串
            classAndMethodName = target.getName() + " 的 " + currentMethod.getName() + " 方法";
            log.info("正在执行：{}", classAndMethodName);
            //打印参数
            Enumeration<String> enumeration = request.getParameterNames();
            while (enumeration.hasMoreElements()){
                String param = enumeration.nextElement();
                log.info("参数 " + param+" : "+request.getParameter(param));
            }

            /**
             * RequestParam注解参数非空参数拦截
             */
            Parameter[] params = currentMethod.getParameters();
            if(params != null && params.length > 0){
                Object[] args = joinPoint.getArgs();
                for(int i=0; i< params.length; i++){
                    Annotation[] annos = params[i].getDeclaredAnnotations();
                    if(annos != null && annos.length > 0){
                        for(Annotation anno : annos){
                            if(anno instanceof RequestParam){
                                RequestParam annoObj = (RequestParam)anno;
                                if(annoObj.required()){
                                    if(args[i] == null || (args[i] instanceof String && StrUtil.isBlank(args[i]+""))){
                                        String paramName = methodSignature.getParameterNames()[i];
                                        String msg = "参数 "+paramName+" 要求非空，参数值："+(args[i]==null?"":args[i].toString());
                                        log.info(msg);
                                        return msg;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable e) {
            log.error("controllerAround 发生异常:", e);
        }
        Object object = joinPoint.proceed();
        log.info("返回: {}", object==null?"空": JSON.toJSONString(object));
        long endTime = System.currentTimeMillis();
        log.info("响应时间 {} 毫秒", endTime-startTime);
        return object;
    }

}
