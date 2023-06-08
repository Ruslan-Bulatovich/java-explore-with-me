package ru.practicum.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@Builder
public class CreateEndpointHitDto {
    @Pattern(regexp = "w+", message = "invalid app")
    @NotNull(message = "field app can't be null")
    private String app;
    @Pattern(regexp = "^[//]/w+//d+", message = "invalid uri")
    @NotNull(message = "field uri can't be null")
    private String uri;
    @Pattern(regexp = "^[d+]/.d+/.d+/.d+", message = "invalid ip")
    @NotNull(message = "field ip can't be null")
    private String ip;
    private String timestamp;
}
