import type {
  SearchTripsResponse,
  TripLocationResponse,
  TripSummaryResponse,
} from './search-trips-response'
import type {
  SearchTripResult,
  SearchTripsPage,
} from '../model/search-trip-result'

const locationLabels: Record<
  TripLocationResponse,
  string
> = {
  DAIMIEL: 'Daimiel',
  CIUDAD_REAL: 'Ciudad Real',
}

const departureTimeFormatter = new Intl.DateTimeFormat(
  'es-ES',
  {
    timeZone: 'Europe/Madrid',
    hour: '2-digit',
    minute: '2-digit',
    hourCycle: 'h23',
  },
)

const contributionFormatter = new Intl.NumberFormat(
  'es-ES',
  {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  },
)

export function mapTripSummaryResponse(
  trip: TripSummaryResponse,
): SearchTripResult {
  return {
    id: trip.id,
    origin: locationLabels[trip.origin],
    destination: locationLabels[trip.destination],
    departureTime: departureTimeFormatter.format(
      new Date(trip.departureAt),
    ),
    departurePoint: trip.departurePoint,
    arrivalPoint: trip.arrivalPoint,
    availableSeats: trip.availableSeats,
    contributionLabel: `${contributionFormatter.format(
      trip.contributionAmount,
    )} €`,
  }
}

export function mapSearchTripsResponse(
  response: SearchTripsResponse,
): SearchTripsPage {
  return {
    trips: response.trips.map(mapTripSummaryResponse),
    page: response.page,
    size: response.size,
    totalElements: response.totalElements,
    totalPages: response.totalPages,
  }
}