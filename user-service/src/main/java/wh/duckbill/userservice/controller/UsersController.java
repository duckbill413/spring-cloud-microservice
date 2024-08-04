package wh.duckbill.userservice.controller;

import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.core.env.Environment;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Tag(name = "user-controller", description = "사용자 서비스를 위한 컨트롤러")
public class UsersController {
    private final Environment env;
    private final UsersService usersService;
    private final Greeting greeting;
    private final ModelMapper modelMapper;

    @Operation(summary = "Health check API", description = "Health check를 위한 API (포트 및 Token Secret 정보 확인 가능)")
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

    @Operation(summary = "환영 메시지 출력 API", description = "Welcome message를 출력하기 위한 API")
    @Timed(value = "users.welcome", longTask = true)
    @GetMapping("/welcome")
    public String welcome(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("users.welcome ip: " + request.getRemoteAddr() +
                           ", " + request.getRemoteHost() +
                           ", " + request.getRequestURI() +
                           ", " + request.getRequestURL());
        return greeting.getMessage();
    }

    @Operation(summary = "사용자 회원 가입을 위한 API", description = "user-service에 회원 가입을 위한 API")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "CREATED"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR"),
    })
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

    @Operation(summary = "전체 사용자 목록조회 API", description = "현재 회원 가입 된 전체 사용자 목록을 조회하기 위한 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized (인증 실패 오류)"),
            @ApiResponse(responseCode = "403", description = "Forbidden (권한이 없는 페이지에 엑세스)"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR"),
    })
    @GetMapping("/users")
    public ResponseEntity<List<ResponseUser>> getUsers() {
        Iterable<UserEntity> userByAll = usersService.getUserByAll();

        List<ResponseUser> result = new ArrayList<>();
        userByAll.forEach(userEntity -> result.add(modelMapper.map(userEntity, ResponseUser.class)));

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Operation(summary = "사용자 정보 상세조회 API", description = "사용자에 대한 상세 정보조회를 위한 API (사용자 정보 + 주문 내역 확인)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized (인증 실패 오류)"),
            @ApiResponse(responseCode = "403", description = "Forbidden (권한이 없는 페이지에 엑세스)"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND (회원 정보가 없을 경우)"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR"),
    })
    @GetMapping("/users/{userId}")
    public ResponseEntity<ResponseUser> getUser(@PathVariable("userId") String userId) {
        UserDto userDto = usersService.getUserByUserId(userId);
        if (userDto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        ResponseUser responseUser = modelMapper.map(userDto, ResponseUser.class);
        // Hateoas Model
        EntityModel<ResponseUser> entityModel = EntityModel.of(responseUser);
        WebMvcLinkBuilder linkTo = linkTo(methodOn(this.getClass()).getUsers());
        entityModel.add(linkTo.withRel("all-users"));

        return ResponseEntity.status(HttpStatus.OK).body(responseUser);
    }

    @GetMapping("/users/hateoas")
    public ResponseEntity<CollectionModel<EntityModel<ResponseUser>>> getUsersWithHateoas() {
        List<EntityModel<ResponseUser>> result = new ArrayList<>();
        Iterable<UserEntity> users = usersService.getUserByAll();

        for (UserEntity user : users) {
            ResponseUser responseUser = modelMapper.map(user, ResponseUser.class);
            EntityModel<ResponseUser> entityModel = EntityModel.of(responseUser);
            entityModel.add(linkTo(methodOn(this.getClass()).getUser(user.getUserId())).withSelfRel());
            result.add(entityModel);
        }

        return ResponseEntity.ok(CollectionModel.of(result, linkTo(methodOn(this.getClass()).getUsersWithHateoas()).withSelfRel()));
    }
}
