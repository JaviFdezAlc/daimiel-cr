import type { TripSearchItem } from '../model/trip-search'

type TripCardProps = {
  trip: TripSearchItem
  isRouteReversed: boolean
}

function StarIcon() {
  return (
    <svg aria-hidden="true" viewBox="0 0 24 24" focusable="false">
      <path d="m12 3 2.78 5.63 6.22.9-4.5 4.38 1.06 6.19L12 17.18 6.44 20.1 7.5 13.91 3 9.53l6.22-.9L12 3Z" />
    </svg>
  )
}

export function TripCard({
  trip,
  isRouteReversed,
}: TripCardProps) {
  return (
    <article className="trip-card">
      <div className="trip-main">
        <div className="trip-point">
          <strong>{trip.departureTime}</strong>
          <span>
            {isRouteReversed ? trip.to : trip.from}
          </span>
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

          <em>{trip.duration}</em>

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
          <strong>{trip.arrivalTime}</strong>
          <span>
            {isRouteReversed ? trip.from : trip.to}
          </span>
        </div>
      </div>

      <div className="trip-side">
        <div className="trip-price">
          <strong>
            {trip.priceLabel.replace(' EUR', '€')}
          </strong>
          <span>
            <i aria-hidden="true" /> {trip.seats} plaza
            {trip.seats > 1 ? 's' : ''}
          </span>
        </div>

        <div className="driver-summary">
          <div>
            <strong>{trip.driver}</strong>
            <span>
              <StarIcon />
              {trip.rating}
            </span>
          </div>

          <span className="driver-photo">
            <img src={trip.driverAvatarUrl} alt="" />
            {trip.verified && (
              <i aria-label="Conductor verificado" />
            )}
          </span>
        </div>
      </div>
    </article>
  )
}