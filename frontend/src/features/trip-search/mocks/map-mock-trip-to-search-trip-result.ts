import type { SearchTripResult } from '../model/search-trip-result'
import type { TripSearchItem } from '../model/trip-search'

export function mapMockTripToSearchTripResult(
  trip: TripSearchItem,
  isRouteReversed: boolean,
): SearchTripResult {
  const origin = isRouteReversed
    ? 'Ciudad Real'
    : 'Daimiel'

  const destination = isRouteReversed
    ? 'Daimiel'
    : 'Ciudad Real'

  return {
    id: String(trip.id),
    origin,
    destination,
    departureTime: trip.departureTime,
    departurePoint: isRouteReversed
      ? trip.to
      : trip.from,
    arrivalPoint: isRouteReversed
      ? trip.from
      : trip.to,
    availableSeats: trip.seats,
    contributionLabel: trip.priceLabel.replace(
      ' EUR',
      ' €',
    ),
  }
}
