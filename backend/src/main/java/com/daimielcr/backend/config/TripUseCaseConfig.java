package com.daimielcr.backend.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.daimielcr.backend.application.port.in.trip.CreateTripUseCase;
import com.daimielcr.backend.application.port.in.trip.GetTripDetailUseCase;
import com.daimielcr.backend.application.port.in.trip.SearchTripsUseCase;
import com.daimielcr.backend.application.port.out.trip.TripRepositoryPort;
import com.daimielcr.backend.application.port.out.user.UserRepositoryPort;
import com.daimielcr.backend.application.service.trip.CreateTripService;
import com.daimielcr.backend.application.service.trip.GetTripDetailService;
import com.daimielcr.backend.application.service.trip.SearchTripsService;

@Configuration(proxyBeanMethods = false)
public class TripUseCaseConfig {

    @Bean
    public CreateTripUseCase createTripUseCase(
            UserRepositoryPort userRepositoryPort,
            TripRepositoryPort tripRepositoryPort,
            Clock clock
    ) {
        return new CreateTripService(
                userRepositoryPort,
                tripRepositoryPort,
                clock
        );
    }

    @Bean
    public GetTripDetailUseCase getTripDetailUseCase(
            TripRepositoryPort tripRepositoryPort
    ) {
        return new GetTripDetailService(tripRepositoryPort);
    }


    @Bean
    public SearchTripsUseCase getSearchTripsUseCase(
            TripRepositoryPort tripRepositoryPort,
            Clock clock,
            ZoneId applicationZoneId
    ) {
        return new SearchTripsService(tripRepositoryPort, clock, applicationZoneId);
    }
}