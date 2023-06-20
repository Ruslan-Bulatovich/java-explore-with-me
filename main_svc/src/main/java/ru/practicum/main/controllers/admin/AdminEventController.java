package ru.practicum.main.controllers.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.dto.event.EventFullDto;
import ru.practicum.main.dto.event.UpdateEventAdminDto;
import ru.practicum.main.enums.EventState;
import ru.practicum.main.services.EventService;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Validated
public class AdminEventController {
    private final EventService eventService;

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable @PositiveOrZero Long eventId,
                                    @RequestBody @Valid UpdateEventAdminDto updateEventAdminDto) {
        return eventService.updateEvent(eventId, updateEventAdminDto);

    }

    @GetMapping
    public List<EventFullDto> getEvents(@RequestParam(name = "users", required = false) @NotBlank List<Long> users,
                                        @RequestParam(name = "states", required = false) @NotBlank EventState states,
                                        @RequestParam(name = "categories", required = false) @NotBlank List<Long> categoriesId,
                                        @RequestParam(name = "rangeStart", required = false) @NotBlank String rangeStart,
                                        @RequestParam(name = "rangeEnd", required = false) @NotBlank String rangeEnd,
                                        @RequestParam(name = "from", required = false, defaultValue = "0") @PositiveOrZero Integer from,
                                        @RequestParam(name = "size", required = false, defaultValue = "10") @PositiveOrZero Integer size) {
        return eventService.getEventsWithParamsByAdmin(users, states, categoriesId, rangeStart, rangeEnd, from, size);
    }
}
