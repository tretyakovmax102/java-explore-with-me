package ru.practicum.main.compilation.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.main.event.model.Event;

import javax.persistence.*;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "compilations")
public class Compilation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    Boolean pinned;
    String title;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "compilation_to_event",
            joinColumns = @JoinColumn(name = "compilation_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id"))
    List<Event> events;
}
