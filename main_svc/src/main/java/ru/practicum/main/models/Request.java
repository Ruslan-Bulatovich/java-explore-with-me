package ru.practicum.main.models;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.practicum.main.enums.RequestStatus;

import javax.persistence.*;
import java.time.LocalDateTime;


@Getter
@Setter
@Entity
@Table(name = "requests", schema = "public")
@AllArgsConstructor
@NoArgsConstructor
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime created;
    @ManyToOne
    @JoinColumn(name = "event")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Event event;
    @ManyToOne
    @JoinColumn(name = "requester")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User requester;
    @Enumerated(EnumType.STRING)
    private RequestStatus status;

}
