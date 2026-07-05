import {
  useEffect,
  useState,
} from 'react'

import {
  SearchTripsError,
  searchTrips,
} from '../api/search-trips'
import type { TripLocationResponse } from '../api/search-trips-response'
import type { SearchTripsPage } from '../model/search-trip-result'
import type { TripSearchSort } from '../model/trip-search'

type UseSearchTripsParams = {
  isEnabled: boolean
  origin: TripLocationResponse
  destination: TripLocationResponse
  date: string
  requiredSeats: number
  sort: TripSearchSort
}

export function useSearchTrips({
  isEnabled,
  origin,
  destination,
  date,
  requiredSeats,
  sort,
}: UseSearchTripsParams) {
  const [searchPage, setSearchPage] =
    useState<SearchTripsPage | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [errorMessage, setErrorMessage] =
    useState<string | null>(null)
  const [requestVersion, setRequestVersion] = useState(0)

  useEffect(() => {
    if (!isEnabled) {
      return
    }

    const controller = new AbortController()
    const frameId = window.requestAnimationFrame(() => {
      setIsLoading(true)
      setErrorMessage(null)
      setSearchPage(null)

      void searchTrips({
        origin,
        destination,
        date,
        requiredSeats,
        sort,
        signal: controller.signal,
      })
        .then((page) => {
          if (!controller.signal.aborted) {
            setSearchPage(page)
          }
        })
        .catch((error: unknown) => {
          if (controller.signal.aborted) {
            return
          }

          if (error instanceof SearchTripsError) {
            setErrorMessage(error.message)
            return
          }

          setErrorMessage(
            'No se pudieron cargar los viajes.',
          )
        })
        .finally(() => {
          if (!controller.signal.aborted) {
            setIsLoading(false)
          }
        })
    })

    return () => {
      window.cancelAnimationFrame(frameId)
      controller.abort()
    }
  }, [
    date,
    destination,
    isEnabled,
    origin,
    requestVersion,
    requiredSeats,
    sort,
  ])

  return {
    trips: searchPage?.trips ?? [],
    totalTrips: searchPage?.totalElements ?? 0,
    isLoading,
    errorMessage,
    retry: () =>
      setRequestVersion((currentVersion) => currentVersion + 1),
  }
}
