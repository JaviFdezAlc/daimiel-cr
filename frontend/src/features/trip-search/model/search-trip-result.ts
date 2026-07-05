export type SearchTripResult = {
  id: string
  origin: string
  destination: string
  departureAt: string
  departureTime: string
  departurePoint: string
  arrivalPoint: string
  availableSeats: number
  contributionAmount: number
  contributionLabel: string
}

export type SearchTripsPage = {
  trips: SearchTripResult[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}