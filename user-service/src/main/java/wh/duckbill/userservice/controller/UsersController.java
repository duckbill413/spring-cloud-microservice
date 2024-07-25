package wh.duckbill.userservice.controller;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wh.duckbill.userservice.dto.UserDto;
import wh.duckbill.userservice.jpa.UserEntity;
import wh.duckbill.userservice.service.UsersService;
import wh.duckbill.userservice.vo.Greeting;
import wh.duckbill.userservice.vo.RequestUser;
import wh.duckbill.userservice.vo.ResponseUser;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class UsersController {
    private final Environment env;
    private final UsersService usersService;
    private final Greeting greeting;
    private final ModelMapper modelMapper;

    @Timed(value = "users.status", longTask = true)
    @GetMapping("/health-check")
    public String status() {
        String status = """
                This is User Service
                port('local.server.port')=%s
                port('server.port')=%s
                token secret=%s
                token expiration_time=%s
                """;

        return String.format(status,
                env.getProperty("local.server.port"),
                env.getProperty("server.port"),
                env.getProperty("token.secret"),
                env.getProperty("token.expiration_time"));
    }

    @Timed(value = "users.welcome", longTask = true)
    @GetMapping("/welcome")
    public String welcome() {
        return greeting.getMessage();
    }

    @PostMapping("/users")
    public ResponseEntity<ResponseUser> createUser(@RequestBody RequestUser user) {
        UserDto userDto = modelMapper.map(user, UserDto.class);
        usersService.createUser(userDto);

        ResponseUser responseUser = modelMapper.map(userDto, ResponseUser.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseUser);
    }

    @DeleteMapping("/users")
    public void deleteAllUsers() {
        usersService.deleteAllUsers();
    }

    @GetMapping("/users")
    public ResponseEntity<List<ResponseUser>> getUsers() {
        Iterable<UserEntity> userByAll = usersService.getUserByAll();

        List<ResponseUser> result = new ArrayList<>();
        userByAll.forEach(userEntity -> result.add(modelMapper.map(userEntity, ResponseUser.class)));

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ResponseUser> getUser(@PathVariable("userId") String userId) {
        UserDto userDto = usersService.getUserByUserId(userId);
        ResponseUser responseUser = modelMapper.map(userDto, ResponseUser.class);
        return ResponseEntity.status(HttpStatus.OK).body(responseUser);
    }
}
