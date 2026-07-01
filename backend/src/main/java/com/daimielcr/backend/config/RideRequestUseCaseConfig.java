package com.daimielcr.backend.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.daimielcr.backend.application.port.in.ride_request.AcceptRideRequestUseCase;
import com.daimielcr.backend.application.port.in.ride_request.CreateRideRequestUseCase;
import com.daimielcr.backend.application.port.out.ride_request.RideRequestRepositoryPort;
import com.daimielcr.backend.application.port.out.trip.TripRepositoryPort;
import com.daimielcr.backend.application.port.out.user.UserRepositoryPort;
import com.daimielcr.backend.application.service.ride_request.AcceptRideRequestService;
import com.daimielcr.backend.application.service.ride_request.CreateRideRequestService;

@Configuration(proxyBeanMethods = false)
public class RideRequestUseCaseConfig {

    @Bean
    public CreateRideRequestUseCase createRideRequestUseCase(
            UserRepositoryPort userRepositoryPort,
            TripRepositoryPort tripRepositoryPort,
            RideRequestRepositoryPort rideRequestRepositoryPort,
            Clock clock) {
        return new CreateRideRequestService(
                userRepositoryPort,
                tripRepositoryPort,
                rideRequestRepositoryPort,
                clock);
    }

    @Bean
    public AcceptRideRequestUseCase acceptRideRequestUseCase(
            RideRequestRepositoryPort rideRequestRepositoryPort,
            TripRepositoryPort tripRepositoryPort,
            Clock clock) {
        return new AcceptRideRequestService(
                rideRequestRepositoryPort,
                tripRepositoryPort,
                clock);
    }
}