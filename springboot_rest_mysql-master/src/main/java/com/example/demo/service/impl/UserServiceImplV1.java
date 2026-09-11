
package com.example.demo.service.impl;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service("userServiceV1")
public class UserServiceImplV1 implements UserService {
    private final UserRepository repo;
    public UserServiceImplV1(UserRepository repo){ this.repo = repo; }


    public User createUser(User user){
        return repo.save(user);
    }


    public User getUserById(Long id){
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found " + id));
    }


    public List<User> getAllUsers(){ return repo.findAll(); }


    public User updateUser(Long id, User user){
        User u = getUserById(id);
        u.setName(user.getName());
        u.setEmail(user.getEmail());
        return repo.save(u);
    }


    public void deleteUser(Long id){
        repo.deleteById(id);
    }


}
