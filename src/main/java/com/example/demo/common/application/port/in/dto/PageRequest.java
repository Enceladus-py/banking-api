package com.example.demo.common.application.port.in.dto;

public record PageRequest(int pageNumber, int pageSize) {
	public PageRequest {
		if (pageNumber < 0)
			throw new IllegalArgumentException("Page number cannot be less than zero");
		if (pageSize < 1)
			throw new IllegalArgumentException("Page size must not be less than one");
	}
}
