package ru.practicum.main.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.client.StatClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.main.constants.Pattern;
import ru.practicum.main.dto.event.*;
import ru.practicum.main.enums.*;
import ru.practicum.main.exceptions.*;
import ru.practicum.main.mappers.EventMapper;
import ru.practicum.main.models.Category;
import ru.practicum.main.models.Event;
import ru.practicum.main.models.User;
import ru.practicum.main.repositories.CategoryRepository;
import ru.practicum.main.repositories.EventRepository;
import ru.practicum.main.repositories.RequestRepository;
import ru.practicum.main.repositories.UserRepository;
import ru.practicum.main.services.EventService;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final StatClient statClient;
    private final EntityManager entityManager;
    private final String datePattern = Pattern.DATE;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(datePattern);

    @Override
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new CategoryNotExistException(""));
        LocalDateTime eventDate = newEventDto.getEventDate();
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new WrongTimeException("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value:" + eventDate);
        }
        Event event = eventMapper.toEventModel(newEventDto);
        event.setCategory(category);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotExistException(String.format("Can't create event, the user with id = %s doesn't exist", userId)));
        event.setInitiator(user);
        EventFullDto eventFullDto = setConfirmedRequest(event);
        return eventFullDto;
    }

    @Override
    public List<EventShortDto> getEvents(Long userId, Integer from, Integer size) {
        Pageable page = PageRequest.of(from / size, size);
        return eventMapper.toEventShortDtoList(eventRepository.findAllByInitiatorId(userId, page).toList());
    }

    @Override
    public EventFullDto updateEvent(Long eventId, UpdateEventAdminDto updateEventAdminDto) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotExistException(String.format("Can't update event with id = %s", eventId)));
        if (updateEventAdminDto == null) {
            return eventMapper.toEventFullDto(event);
        }

        if (updateEventAdminDto.getAnnotation() != null) {
            event.setAnnotation(updateEventAdminDto.getAnnotation());
        }
        if (updateEventAdminDto.getCategory() != null) {
            Category category = categoryRepository.findById(updateEventAdminDto.getCategory()).orElseThrow(() -> new CategoryNotExistException(""));
            event.setCategory(category);
        }
        if (updateEventAdminDto.getDescription() != null) {
            event.setDescription(updateEventAdminDto.getDescription());
        }
        if (updateEventAdminDto.getLocation() != null) {
            event.setLocation(updateEventAdminDto.getLocation());
        }
        if (updateEventAdminDto.getPaid() != null) {
            event.setPaid(updateEventAdminDto.getPaid());
        }
        if (updateEventAdminDto.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventAdminDto.getParticipantLimit().intValue());
        }
        if (updateEventAdminDto.getRequestModeration() != null) {
            event.setRequestModeration(updateEventAdminDto.getRequestModeration());
        }
        if (updateEventAdminDto.getTitle() != null) {
            event.setTitle(updateEventAdminDto.getTitle());
        }
        if (updateEventAdminDto.getStateAction() != null) {
            if (updateEventAdminDto.getStateAction().equals(StateActionForAdmin.PUBLISH_EVENT)) {
                if (event.getPublishedOn() != null) {
                    throw new AlreadyPublishedException("Event already published");
                }
                if (event.getState().equals(EventState.CANCELED)) {
                    throw new EventAlreadyCanceledException("Event already canceled");
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (updateEventAdminDto.getStateAction().equals(StateActionForAdmin.REJECT_EVENT)) {
                if (event.getPublishedOn() != null) {
                    throw new AlreadyPublishedException("Event already published");
                }
                event.setState(EventState.CANCELED);
            }
        }
        if (updateEventAdminDto.getEventDate() != null) {
            LocalDateTime eventDateTime = updateEventAdminDto.getEventDate();
            if (eventDateTime.isBefore(LocalDateTime.now())) {
                throw new WrongTimeException("The start date of the event to be modified is less than one hour from the publication date.");
            }

            event.setEventDate(updateEventAdminDto.getEventDate());
        }
        EventFullDto eventFullDto = setConfirmedRequest(event);
        return eventFullDto;
    }

    @Override
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserDto updateEventUserDto) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new EventNotExistException(""));

        if (event.getPublishedOn() != null) {
            throw new AlreadyPublishedException("Event already published");
        }

        if (updateEventUserDto == null) {
            return eventMapper.toEventFullDto(event);
        }

        if (updateEventUserDto.getAnnotation() != null) {
            event.setAnnotation(updateEventUserDto.getAnnotation());
        }
        if (updateEventUserDto.getCategory() != null) {
            Category category = categoryRepository.findById(updateEventUserDto.getCategory()).orElseThrow(() -> new CategoryNotExistException(""));
            event.setCategory(category);
        }
        if (updateEventUserDto.getDescription() != null) {
            event.setDescription(updateEventUserDto.getDescription());
        }
        if (updateEventUserDto.getEventDate() != null) {
            LocalDateTime eventDateTime = updateEventUserDto.getEventDate();
            if (eventDateTime.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new WrongTimeException("The start date of the event to be modified is less than one hour from the publication date.");
            }
            event.setEventDate(updateEventUserDto.getEventDate());
        }
        if (updateEventUserDto.getLocation() != null) {
            event.setLocation(updateEventUserDto.getLocation());
        }
        if (updateEventUserDto.getPaid() != null) {
            event.setPaid(updateEventUserDto.getPaid());
        }
        if (updateEventUserDto.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventUserDto.getParticipantLimit().intValue());
        }
        if (updateEventUserDto.getRequestModeration() != null) {
            event.setRequestModeration(updateEventUserDto.getRequestModeration());
        }
        if (updateEventUserDto.getTitle() != null) {
            event.setTitle(updateEventUserDto.getTitle());
        }

        if (updateEventUserDto.getStateAction() != null) {
            if (updateEventUserDto.getStateAction().equals(StateActionForUser.SEND_TO_REVIEW)) {
                event.setState(EventState.PENDING);
            } else {
                event.setState(EventState.CANCELED);
            }
        }
        EventFullDto eventFullDto = setConfirmedRequest(event);
        return eventFullDto;
    }

    @Override
    public EventFullDto getEventByUser(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId).orElseThrow(() -> new EventNotExistException(""));
        EventFullDto eventFullDto = setConfirmedRequest(event);
        return eventFullDto;
    }

    @Override
    public List<EventFullDto> getEventsWithParamsByAdmin(List<Long> users, EventState states, List<Long> categoriesId, LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {
        LocalDateTime start = rangeStart != null ? rangeStart : null;
        LocalDateTime end = rangeEnd != null ? rangeEnd : null;

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> query = builder.createQuery(Event.class);

        Root<Event> root = query.from(Event.class);
        Predicate criteria = builder.conjunction();

        if (categoriesId != null && categoriesId.size() > 0) {
            Predicate containCategories = root.get("category").in(categoriesId);
            criteria = builder.and(criteria, containCategories);
        }

        if (users != null && users.size() > 0) {
            Predicate containUsers = root.get("initiator").in(users);
            criteria = builder.and(criteria, containUsers);
        }

        if (states != null) {
            Predicate containStates = root.get("state").in(states);
            criteria = builder.and(criteria, containStates);
        }

        if (rangeStart != null) {
            Predicate greaterTime = builder.greaterThanOrEqualTo(root.get("eventDate").as(LocalDateTime.class), start);
            criteria = builder.and(criteria, greaterTime);
        }
        if (rangeEnd != null) {
            Predicate lessTime = builder.lessThanOrEqualTo(root.get("eventDate").as(LocalDateTime.class), end);
            criteria = builder.and(criteria, lessTime);
        }

        query.select(root).where(criteria);
        List<Event> events = entityManager.createQuery(query)
                .setFirstResult(from)
                .setMaxResults(size)
                .getResultList();

        if (events.size() == 0) {
            return new ArrayList<>();
        }
        List<EventFullDto> eventFullDtoList = eventMapper.toEventFullDtoList(events);
        Map<Long, Long> confirmedRequests = getConfirmedRequestsList(events);
        for (EventFullDto dto : eventFullDtoList) {
            dto.setConfirmedRequests(confirmedRequests.getOrDefault(dto.getId(), 0L));
        }
        setView(eventFullDtoList);
        return eventFullDtoList;
    }

    @Override
    public List<EventFullDto> getEventsWithParamsByUser(String text, List<Long> categories, Boolean paid, String rangeStart,
                                                        String rangeEnd, Boolean onlyAvailable, SortValue sort, Integer from, Integer size, String ip, String uri) {
        LocalDateTime start = rangeStart != null ? LocalDateTime.parse(rangeStart, dateFormatter) : null;
        LocalDateTime end = rangeEnd != null ? LocalDateTime.parse(rangeEnd, dateFormatter) : null;
        checkDateTime(start, end);
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> query = builder.createQuery(Event.class);

        Root<Event> root = query.from(Event.class);
        Predicate criteria = builder.conjunction();

        if (text != null) {
            Predicate annotationContain = builder.like(builder.lower(root.get("annotation")),
                    "%" + text.toLowerCase() + "%");
            Predicate descriptionContain = builder.like(builder.lower(root.get("description")),
                    "%" + text.toLowerCase() + "%");
            Predicate containText = builder.or(annotationContain, descriptionContain);

            criteria = builder.and(criteria, containText);
        }

        if (categories != null && categories.size() > 0) {
            Predicate containStates = root.get("category").in(categories);
            criteria = builder.and(criteria, containStates);
        }

        if (paid != null) {
            Predicate isPaid;
            if (paid) {
                isPaid = builder.isTrue(root.get("paid"));
            } else {
                isPaid = builder.isFalse(root.get("paid"));
            }
            criteria = builder.and(criteria, isPaid);
        }

        if (rangeStart != null) {
            Predicate greaterTime = builder.greaterThanOrEqualTo(root.get("eventDate").as(LocalDateTime.class), start);
            criteria = builder.and(criteria, greaterTime);
        }
        if (rangeEnd != null) {
            Predicate lessTime = builder.lessThanOrEqualTo(root.get("eventDate").as(LocalDateTime.class), end);
            criteria = builder.and(criteria, lessTime);
        }

        query.select(root).where(criteria).orderBy(builder.asc(root.get("eventDate")));
        List<Event> events = entityManager.createQuery(query)
                .setFirstResult(from)
                .setMaxResults(size)
                .getResultList();

        if (events.size() == 0) {
            return new ArrayList<>();
        }
        List<EventFullDto> eventFullDtoList = eventMapper.toEventFullDtoList(events);
        Map<Long, Long> confirmedRequests = getConfirmedRequestsList(events);
        for (EventFullDto dto : eventFullDtoList) {
            dto.setConfirmedRequests(confirmedRequests.getOrDefault(dto.getId(), 0L));
        }
        setView(eventFullDtoList);
        if (onlyAvailable) {
            eventFullDtoList = eventFullDtoList.stream()
                    .filter((eventFullDto -> eventFullDto.getConfirmedRequests() < (long) eventFullDto.getParticipantLimit()))
                    .collect(Collectors.toList());
        }
        if (sort != null) {
            if (sort.equals(SortValue.EVENT_DATE)) {
                eventFullDtoList = eventFullDtoList.stream().sorted(Comparator.comparing(EventFullDto::getEventDate)).collect(Collectors.toList());
            } else {
                eventFullDtoList = eventFullDtoList.stream().sorted(Comparator.comparing(EventFullDto::getViews)).collect(Collectors.toList());
            }
        }
        sendStat(events, ip, uri);
        return eventFullDtoList;
    }

    @Override
    public EventFullDto getEvent(Long id, String ip, String uri) {
        Event event = eventRepository.findByIdAndPublishedOnIsNotNull(id).orElseThrow(() -> new EventNotExistException(String.format("Can't find event with id = %s event doesn't exist", id)));
        EventFullDto eventFullDto = setConfirmedRequest(event);
        sendStat(eventFullDto, ip, uri);
        eventFullDto.setViews(setView(event));
        return eventFullDto;
    }

    public void sendStat(EventFullDto event, String ip, String uri) {
        LocalDateTime now = LocalDateTime.now();
        String remoteAddr = ip;
        String nameService = "main-service";

        EndpointHitDto requestDto = new EndpointHitDto();
        requestDto.setTimestamp(now.format(dateFormatter));
        requestDto.setUri("/events");
        requestDto.setApp(nameService);
        requestDto.setIp(remoteAddr);
        statClient.addStats(requestDto);
        sendStatForTheEvent(event.getId(), remoteAddr, now, nameService);
    }

    public void sendStat(List<Event> events, String ip, String uri) {
        LocalDateTime now = LocalDateTime.now();
        String remoteAddr = ip;
        String nameService = "main-service";

        EndpointHitDto requestDto = new EndpointHitDto();
        requestDto.setTimestamp(now.format(dateFormatter));
        requestDto.setUri("/events");
        requestDto.setApp(nameService);
        requestDto.setIp(ip);
        statClient.addStats(requestDto);
    }

    public void setView(List<EventFullDto> events) {
        LocalDateTime start = LocalDateTime.parse(events.get(0).getCreatedOn());
        List<String> uris = new ArrayList<>();
        Map<String, EventFullDto> eventsUri = new HashMap<>();
        String uri = "";
        for (EventFullDto event : events) {
            LocalDateTime createdOn = LocalDateTime.parse(events.get(0).getCreatedOn());
            if (start.isBefore(createdOn)) {
                start = createdOn;
            }
            uri = "/events/" + event.getId();
            uris.add(uri);
            eventsUri.put(uri, event);
            event.setViews(0L);
        }
        String startTime = start.format(DateTimeFormatter.ofPattern(Pattern.DATE));
        String endTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern(Pattern.DATE));
        List<ViewStatsDto> stats = getStats(startTime, endTime, uris);
        stats.forEach((stat) ->
                eventsUri.get(stat.getUri()).setViews(stat.getHits()));
    }

    public Long setView(Event event) {

        String startTime = event.getCreatedOn().format(dateFormatter);
        String endTime = LocalDateTime.now().format(dateFormatter);
        List<String> uris = List.of("/events/" + event.getId());
        List<ViewStatsDto> stats = getStats(startTime, endTime, uris);
        if (stats.size() == 1) {
            return stats.get(0).getHits();
        } else {
            return 1L;
        }
    }

    private void checkDateTime(LocalDateTime start, LocalDateTime end) {
        if (start == null) {
            start = LocalDateTime.now().minusYears(100);
        }
        if (end == null) {
            end = LocalDateTime.now();
        }
        if (start.isAfter(end)) {
            throw new WrongTimeException("Некорректный запрос. Дата окончания события задана позже даты старта");
        }
    }

    private List<ViewStatsDto> getStats(String startTime, String endTime, List<String> uris) {
        return statClient.getStats(startTime, endTime, uris, false);
    }

    private void sendStatForTheEvent(Long eventId, String remoteAddr, LocalDateTime now,
                                     String nameService) {
        EndpointHitDto requestDto = new EndpointHitDto();
        requestDto.setTimestamp(now.format(dateFormatter));
        requestDto.setUri("/events/" + eventId);
        requestDto.setApp(nameService);
        requestDto.setIp(remoteAddr);
        statClient.addStats(requestDto);
    }

    private EventFullDto setConfirmedRequest(Event event) {
        Integer confirmed = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        EventFullDto eventFullDto = eventMapper.toEventFullDto(eventRepository.save(event));
        eventFullDto.setConfirmedRequests((long) confirmed);
        return eventFullDto;
    }

    private Map<Long, Long> getConfirmedRequestsList(List<Event> events) {
        List<Event> publishedEvents = getPublished(events);

        Map<Long, Long> confirmedRequests = requestRepository.findAllByEventInAndStatus(publishedEvents, RequestStatus.CONFIRMED)
                .stream()
                .collect(Collectors.groupingBy(eventRequest -> eventRequest.getEvent().getId(), Collectors.counting()));
        return confirmedRequests;
    }

    private List<Event> getPublished(List<Event> events) {
        return events.stream()
                .filter(event -> event.getPublishedOn() != null)
                .collect(Collectors.toList());
    }
}
