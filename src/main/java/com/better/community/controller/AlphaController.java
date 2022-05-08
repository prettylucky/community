package com.better.community.controller;

import com.better.community.service.AlphaService;
import javafx.print.Printer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @param
 * @author Bubble
 * @description
 * @return
 * @Date 2022/5/2
 */
@Controller
@RequestMapping("/alpha")
public class AlphaController {

    @Autowired
    private AlphaService alphaService;

    @RequestMapping("/hello")
    @ResponseBody   //指定方法返回的值即http响应体的值，而不是通过视图解析器解析的一个网页。
    public String sayHeollo(){
        return "Hello Spring Boot.";
    }

    @RequestMapping("/alphaservice")
    @ResponseBody
    public String alphaService() {
        return alphaService.select();
    }
    
    @RequestMapping("/http")
    public void http(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //request接收请求数据
        String user = request.getParameter("user");

        //response利用流响应数据
        response.setContentType("text/html;charset=utf-8");

        PrintWriter out =  response.getWriter();
        out.print("<h1>牛牛牛</h1>");

        out.close();
    }

    // GET请求
    // 请求路径： /students?current=1&limit=20
    @RequestMapping(path = "/students", method = RequestMethod.GET)
    @ResponseBody
    public String getStudents(
            @RequestParam(name = "current", required = false, defaultValue = "1") int current,
            @RequestParam(name = "limit", required = false, defaultValue = "1") int limit){
        System.out.println(current +","+ limit);
        return "";
    }
    // GET请求
    // RestFul风格传参
    // 请求路径 /student/3
    @RequestMapping(value = "/student/{id}", method = RequestMethod.GET)
    @ResponseBody
    public void getStudent(@PathVariable(name = "id") int id){
        System.out.println(id);
    }

    //POST请求
    @RequestMapping(value = "/student", method = RequestMethod.POST)
    @ResponseBody
    public String saveStudent(@RequestParam(name = "name") String name,@RequestParam("age") int age){
        System.out.println(name + "," +age);
        return "success";
    }

    // ModelAndView响应数据
    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public ModelAndView getUser (){
        ModelAndView mav = new ModelAndView();
        //添加Model数据
        mav.addObject("name", "小S");
        mav.addObject("age", 18);
        //设定需要返回的View(会通过视图解析器把路径补全)
        mav.setViewName("/demo/test");

        return mav;
    }

    // 直接返回String类型的数据，会默认当作一个ViewName处理，（会经过视图解析器）
    @RequestMapping(value = "/user2",method = RequestMethod.GET)
    public String getUser2(Model model) {
        model.addAttribute("name","小Y");
        model.addAttribute("age", 18);
        return "/demo/test";
    }

    //返回json数据 ：加@ResponseBody注解后返回任何Java对象都会转换为JSON格式的字符串再响应
    @GetMapping("/emp")
    @ResponseBody
    public Map<String, String> getEmp (){
        Map<String, String > emp = new HashMap<>();

        emp.put("name","张三");
        emp.put("age","18");
        emp.put("salary", "5000");

        return emp;
    }

    @GetMapping("/emps")
    @ResponseBody
    public List<Map<String, String>> getEmps (){
        List<Map<String, String>> emps = new ArrayList<>();

        Map<String, String > emp = new HashMap<>();
        emp.put("name","张三");
        emp.put("age","18");
        emp.put("salary", "5000");
        emps.add(emp);

        emp = new HashMap<>();
        emp.put("name","李四");
        emp.put("age","19");
        emp.put("salary", "10000");
        emps.add(emp);

        emp = new HashMap<>();
        emp.put("name","wangwu");
        emp.put("age","20");
        emp.put("salary", "5000");
        emps.add(emp);

        return emps;
    }
}
