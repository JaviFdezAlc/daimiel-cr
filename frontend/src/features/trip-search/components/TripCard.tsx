import type { SearchTripResult } from '../model/search-trip-result'

type TripCardProps = {
  trip: SearchTripResult
}

export function TripCard({ trip }: TripCardProps) {
  return (
    <article className="trip-card">
      <div className="trip-main">
        <div className="trip-point">
          <strong>{trip.departureTime}</strong>
          <span>{trip.origin}</span>
        </div>

        <div className="trip-duration-line">
          <span
            className="trip-route-segment trip-route-segment-left"
            aria-hidden="true"
          >
            <span className="trip-route-dot" />
            <svg viewBox="0 0 120 36" preserveAspectRatio="none">
              <path d="M12 10 C34 10 31 28 58 28 H112" />
            </svg>
          </span>

          <em>{trip.departurePoint}</em>

          <span
            className="trip-route-segment trip-route-segment-right"
            aria-hidden="true"
          >
            <svg viewBox="0 0 120 36" preserveAspectRatio="none">
              <path d="M8 10 H62 C88 10 84 28 108 28" />
            </svg>
            <span className="trip-route-dot" />
          </span>
        </div>

        <div className="trip-point">
          <strong>{trip.destination}</strong>
          <span>{trip.arrivalPoint}</span>
        </div>
      </div>

      <div className="trip-side">
        <div className="trip-price">
          <strong>{trip.contributionLabel}</strong>
          <span>
            <i aria-hidden="true" /> {trip.availableSeats} plaza
            {trip.availableSeats > 1 ? 's' : ''}
          </span>
        </div>
      </div>
    </article>
  )
}
