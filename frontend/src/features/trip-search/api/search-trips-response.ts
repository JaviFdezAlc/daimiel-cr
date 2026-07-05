export type TripLocationResponse =
  | 'DAIMIEL'
  | 'CIUDAD_REAL'

export type TripSummaryResponse = {
  id: string
  origin: TripLocationResponse
  destination: TripLocationResponse
  departureAt: string
  departurePoint: string
  arrivalPoint: string
  availableSeats: number
  contributionAmount: number
}

export type SearchTripsResponse = {
  trips: TripSummaryResponse[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}