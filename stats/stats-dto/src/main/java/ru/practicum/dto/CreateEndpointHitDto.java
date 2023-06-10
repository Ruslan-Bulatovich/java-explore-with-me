package ru.practicum.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@Builder
public class CreateEndpointHitDto {

    @NotNull(message = "field app can't be null")
    @NotBlank
    @Size(max = 255)
    private String app;
    @NotNull(message = "field uri can't be null")
    @NotBlank
    @Size(max = 255)
    private String uri;
    @NotNull(message = "field ip can't be null")
    @NotBlank
    @Size(max = 255)
    private String ip;
    @NotNull
    private String timestamp;
}
