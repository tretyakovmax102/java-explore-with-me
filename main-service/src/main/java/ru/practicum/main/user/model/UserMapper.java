package ru.practicum.main.user.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.main.user.dto.NewUserDto;
import ru.practicum.main.user.dto.UserDto;
import ru.practicum.main.user.dto.UserDtoWithoutEmail;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserMapper {

    public static UserDto userToDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    public static User userFromDto(UserDto user) {
        return new User(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    public static User userFromUserCreateRequestDto(NewUserDto newUserDto) {
        return new User(
                null,
                newUserDto.getName(),
                newUserDto.getEmail()
        );
    }

    public static UserDtoWithoutEmail userToShort(User user) {
        return new UserDtoWithoutEmail(
                user.getId(),
                user.getName()
        );
    }

}