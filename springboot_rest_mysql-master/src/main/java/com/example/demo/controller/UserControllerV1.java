
package com.example.demo.controller;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserControllerV1 {
    private final UserService service;

    public UserControllerV1(@Qualifier("userServiceV1") UserService service){
        this.service = service;
    }


    @PostMapping public User create(@RequestBody User user){
        return service.createUser(user);
    }


    @GetMapping("/{id}") public User get(@PathVariable Long id){
        return service.getUserById(id);
    }


    @GetMapping public List<User> getAll(){
        return service.getAllUsers();
    }

    //devtool testing
  /*  @GetMapping("/test")
    public String test() {
        return "DEVTOOLS WORKING - V1";
    }*/


    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id,
                           @RequestBody User user) {
        return service.updateUser(id, user);
    }

    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id) {
        service.deleteUser(id);
        return "User deleted successfully with id " + id;
    }
}
