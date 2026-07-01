import type {
  TripSearchItem,
  TripSearchSort,
} from '../model/trip-search'

type FilterTripsParams = {
  trips: readonly TripSearchItem[]
  dayOffset: number
  minSeats: number
  verifiedOnly: boolean
  sort: TripSearchSort
}

export function filterTrips({
  trips,
  dayOffset,
  minSeats,
  verifiedOnly,
  sort,
}: FilterTripsParams): TripSearchItem[] {
  return trips
    .filter((trip) => trip.dayOffsets.includes(dayOffset))
    .filter((trip) => !verifiedOnly || trip.verified)
    .filter((trip) => trip.seats >= minSeats)
    .toSorted((firstTrip, secondTrip) => {
      if (sort === 'price') {
        return firstTrip.price - secondTrip.price
      }

      if (sort === 'duration') {
        return firstTrip.durationMinutes - secondTrip.durationMinutes
      }

      return firstTrip.departureTime.localeCompare(
        secondTrip.departureTime,
      )
    })
}