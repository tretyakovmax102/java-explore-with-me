package ru.practicum.main.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.user.dto.NewUserDto;
import ru.practicum.main.user.dto.UserDto;
import ru.practicum.main.user.model.UserMapper;
import ru.practicum.main.user.repository.UserRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserDto> getUsers(Set<Integer> setId, int from, int size) {
        if (setId == null || setId.isEmpty())
            return userRepository.findAll(PageRequest.of(from, size)).getContent()
                    .stream()
                    .map(UserMapper::userToDto)
                    .collect(Collectors.toList());

        return userRepository.findByIdIn(setId, PageRequest.of(from, size))
                .stream()
                .map(UserMapper::userToDto)
                .collect(Collectors.toList());
    }

    public UserDto getUser(Integer id) {
        return UserMapper.userToDto(userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("not found user with id = " + id)));
    }

    public UserDto postUser(NewUserDto newUserDto) {
        return UserMapper.userToDto(userRepository.save(UserMapper.userFromUserCreateRequestDto(newUserDto)));
    }

    public void deleteUser(Integer id) {
        userRepository.findById(id).ifPresentOrElse(userRepository::delete, () -> {
            throw new NotFoundException("User not found id = " +  id);
        });
    }

}
