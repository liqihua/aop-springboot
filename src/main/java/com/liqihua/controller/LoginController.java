package com.liqihua.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/loginController")
public class LoginController {

    //http://localhost:8080/loginController/login1
    @RequestMapping("/login1")
    public String login1(){
        System.out.println("-- login1()");
        return "-- login1 --";
    }

    //如：访问 http://localhost:8080/loginController/login2?name=abc&age=18 进行测试
    @RequestMapping("/login2")
    public String login2(String name,int age){
        System.out.println("-- login2()");
        return "-- login2 --";
    }


    @RequestMapping("/login3")
    public String login3(){
        System.out.println("-- login3()");
        String aa = null;
        boolean bb = aa.equals("123");
        return "-- login3 --";
    }







}
