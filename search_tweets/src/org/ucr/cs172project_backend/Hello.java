package org.ucr.cs172project_backend;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController

public class Hello {
    @RequestMapping("/")
    public String hello() {
        System.out.println("Hello world");
        return "Hello World";
    }
}
