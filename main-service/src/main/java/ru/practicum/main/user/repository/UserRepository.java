package ru.practicum.main.user.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.main.user.model.User;

import java.util.List;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    @Query("SELECT a FROM User a WHERE ?1 IS null OR a.id IN ?1")
    List<User> findByIdIn(Set<Integer> ids, Pageable pageable);

}