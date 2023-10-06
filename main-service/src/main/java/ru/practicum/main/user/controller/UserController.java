package ru.practicum.main.user.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.user.dto.NewUserDto;
import ru.practicum.main.user.dto.UserDto;
import ru.practicum.main.user.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/admin/users")
@AllArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getUsers(@RequestParam(required = false) Set<Integer> ids,
                             @PositiveOrZero @RequestParam(required = false, defaultValue = "0") int from,
                             @Positive @RequestParam(required = false, defaultValue = "10") int size) {
        log.debug("GET request to /admin/users with parameters: ids = " + ids + " from =" + from + "size = " + size);
        return userService.getUsers(ids, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto postUser(@Valid @RequestBody NewUserDto newUser) {
        log.debug("POST request to /admin/users with parameters: inputUserDto = " + newUser);
        return userService.postUser(newUser);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Integer id) {
        log.debug("DELETE request to /admin/users with parameters: id = " + id);
        userService.deleteUser(id);
    }
}
