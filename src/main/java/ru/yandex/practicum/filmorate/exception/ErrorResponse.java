package ru.yandex.practicum.filmorate.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private String error;
    private List<String> details;
}
