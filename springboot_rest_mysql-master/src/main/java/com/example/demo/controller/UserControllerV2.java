
package com.example.demo.controller;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/users")
public class UserControllerV2 {
    private final UserService service;
    public UserControllerV2(@Qualifier("userServiceV2") UserService service){ this.service = service; }


    @PostMapping
    public User create(@RequestBody User user){
        return service.createUser(user);
    }

    @GetMapping("/{id}")
    public User get(@PathVariable Long id){
        
        return service.getUserById(id);
    }
}
