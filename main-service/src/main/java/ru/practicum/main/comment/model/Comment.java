package ru.practicum.main.comment.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @JoinColumn(name = "commentator_id")
    @OneToOne
    private User commentator;
    @JoinColumn(name = "event_id")
    @ManyToOne
    private Event event;
    private String text;
    private LocalDateTime created;
    private LocalDateTime edited;
}
