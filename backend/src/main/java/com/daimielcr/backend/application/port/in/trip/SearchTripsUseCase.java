package com.daimielcr.backend.application.port.in.trip;

public interface SearchTripsUseCase {

    SearchTripsResult search(SearchTripsQuery query);
}