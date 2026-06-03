package com.example.demo.application.port.in.dto;

import java.util.List;

public record PageResult<T>(List<T> content, int pageNumber, int pageSize, long totalElements, int totalPages) {
}
