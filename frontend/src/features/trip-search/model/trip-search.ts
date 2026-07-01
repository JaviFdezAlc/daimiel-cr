export type TripSearchSort = 'earliest' | 'price' | 'duration'

export type TripSearchItem = {
  id: number
  departureTime: string
  arrivalTime: string
  duration: string
  durationMinutes: number
  from: string
  to: string
  driver: string
  driverAvatarUrl: string
  rating: string
  price: number
  priceLabel: string
  seats: number
  verified: boolean
  dayOffsets: number[]
  tags: string[]
}