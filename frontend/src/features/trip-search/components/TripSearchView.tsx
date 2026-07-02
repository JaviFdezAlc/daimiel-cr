import {
  useEffect,
  useMemo,
  useRef,
  useState,
} from 'react'

import {
  getDateFromKey,
  getDateKey,
  getDayOffset,
  getMonthLabel,
  getReadableDate,
} from '../../../shared/lib/date'
import { filterTrips } from '../lib/filterTrips'
import { mockTrips } from '../mocks/trips'
import type { TripSearchSort } from '../model/trip-search'
import { SearchControls } from './SearchControls'
import { SearchFilters } from './SearchFilters'
import { SearchResults } from './SearchResults'

type TripSearchViewProps = {
  isVisible: boolean
}

export function TripSearchView({
  isVisible,
}: TripSearchViewProps) {
  const [isMobileFiltersOpen, setIsMobileFiltersOpen] =
    useState(false)
  const [sortKey, setSortKey] =
    useState<TripSearchSort>('earliest')
  const [verifiedOnly, setVerifiedOnly] = useState(false)
  const [searchDate, setSearchDate] = useState(() =>
    getDateKey(new Date()),
  )
  const [minSeats, setMinSeats] = useState(1)
  const [isPassengerPickerOpen, setIsPassengerPickerOpen] =
    useState(false)
  const [isRouteReversed, setIsRouteReversed] =
    useState(false)

  const searchDateInputRef = useRef<HTMLInputElement>(null)

  const todayKey = getDateKey(new Date())

  const origin = isRouteReversed
    ? 'Ciudad Real'
    : 'Daimiel'

  const destination = isRouteReversed
    ? 'Daimiel'
    : 'Ciudad Real'

  const searchDateLabel =
    searchDate === todayKey
      ? 'Hoy'
      : getReadableDate(getDateFromKey(searchDate))

  const searchDateShortLabel = `${
    getDateFromKey(searchDate).getDate()
  } ${getMonthLabel(
    getDateFromKey(searchDate),
  ).slice(0, 3)}`

  const visibleTrips = useMemo(() => {
    const searchDayOffset = getDayOffset(
      searchDate,
      todayKey,
    )

    return filterTrips({
      trips: mockTrips,
      dayOffset: searchDayOffset,
      minSeats,
      verifiedOnly,
      sort: sortKey,
    })
  }, [
    minSeats,
    searchDate,
    sortKey,
    todayKey,
    verifiedOnly,
  ])

  useEffect(() => {
    if (isVisible) {
      return
    }

    const frameId = window.requestAnimationFrame(() => {
      setIsMobileFiltersOpen(false)
    })

    return () => window.cancelAnimationFrame(frameId)
  }, [isVisible])

  const updatePassengerCount = (amount: number) => {
    setMinSeats((currentCount) =>
      Math.min(4, Math.max(1, currentCount + amount)),
    )
  }

  const updateSearchDate = (date: string) => {
    setSearchDate(date || todayKey)
  }

  const toggleSearchRoute = () => {
    setIsRouteReversed((currentValue) => !currentValue)
  }

  const toggleMobileFilters = () => {
    setIsMobileFiltersOpen(
      (currentValue) => !currentValue,
    )
  }

  const togglePassengerPicker = () => {
    setIsPassengerPickerOpen(
      (currentValue) => !currentValue,
    )
  }

  const closePassengerPicker = () => {
    setIsPassengerPickerOpen(false)
  }

  const openSearchDatePicker = () => {
    setIsPassengerPickerOpen(false)
    searchDateInputRef.current?.focus()
    searchDateInputRef.current?.showPicker?.()
  }

  return (
    <section
      className="search-view"
      aria-label="Buscar viaje"
      aria-hidden={!isVisible}
    >
      <div className="search-panel">
        <SearchControls
          origin={origin}
          destination={destination}
          searchDate={searchDate}
          searchDateLabel={searchDateLabel}
          searchDateShortLabel={searchDateShortLabel}
          minimumDateKey={todayKey}
          minSeats={minSeats}
          isMobileFiltersOpen={isMobileFiltersOpen}
          isPassengerPickerOpen={isPassengerPickerOpen}
          searchDateInputRef={searchDateInputRef}
          onToggleRoute={toggleSearchRoute}
          onToggleMobileFilters={toggleMobileFilters}
          onSearchDateChange={updateSearchDate}
          onPassengerCountChange={updatePassengerCount}
          onOpenSearchDatePicker={openSearchDatePicker}
          onTogglePassengerPicker={togglePassengerPicker}
          onClosePassengerPicker={closePassengerPicker}
        />

        <div className="search-layout">
          <SearchFilters
            isOpen={isMobileFiltersOpen}
            origin={origin}
            destination={destination}
            availableTrips={visibleTrips.length}
            searchDateLabel={searchDateLabel}
            minSeats={minSeats}
            sort={sortKey}
            verifiedOnly={verifiedOnly}
            onSortChange={setSortKey}
            onVerifiedOnlyChange={setVerifiedOnly}
            onMinSeatsChange={setMinSeats}
          />

          <SearchResults
            trips={visibleTrips}
            searchDateLabel={searchDateLabel}
            minSeats={minSeats}
            isRouteReversed={isRouteReversed}
          />
        </div>
      </div>
    </section>
  )
}
