import { useEffect, useRef, useState } from "react";

import {
  getDateFromKey,
  getDateKey,
  getMonthLabel,
  getReadableDate,
} from "../../../shared/lib/date";
import type { TripSearchSort } from "../model/trip-search";
import { SearchControls } from "./SearchControls";
import { SearchFilters } from "./SearchFilters";
import { SearchResults } from "./SearchResults";

import { useSearchTrips } from "../hooks/useSearchTrips";
import type { TripLocationResponse } from "../api/search-trips-response";

type TripSearchViewProps = {
  isVisible: boolean;
};

export function TripSearchView({ isVisible }: TripSearchViewProps) {
  const [isMobileFiltersOpen, setIsMobileFiltersOpen] = useState(false);
  const [sortKey, setSortKey] = useState<TripSearchSort>("earliest");
  const [searchDate, setSearchDate] = useState(() => getDateKey(new Date()));
  const [minSeats, setMinSeats] = useState(1);
  const [isPassengerPickerOpen, setIsPassengerPickerOpen] = useState(false);
  const [isRouteReversed, setIsRouteReversed] = useState(false);

  const searchDateInputRef = useRef<HTMLInputElement>(null);

  const todayKey = getDateKey(new Date());

  const origin = isRouteReversed ? "Ciudad Real" : "Daimiel";

  const destination = isRouteReversed ? "Daimiel" : "Ciudad Real";

  const apiOrigin: TripLocationResponse = isRouteReversed
    ? "CIUDAD_REAL"
    : "DAIMIEL";

  const apiDestination: TripLocationResponse = isRouteReversed
    ? "DAIMIEL"
    : "CIUDAD_REAL";

  const searchDateLabel =
    searchDate === todayKey
      ? "Hoy"
      : getReadableDate(getDateFromKey(searchDate));

  const searchDateShortLabel = `${getDateFromKey(
    searchDate,
  ).getDate()} ${getMonthLabel(getDateFromKey(searchDate)).slice(0, 3)}`;

  const { trips, totalTrips, isLoading, errorMessage, retry } = useSearchTrips({
    isEnabled: isVisible,
    origin: apiOrigin,
    destination: apiDestination,
    date: searchDate,
    requiredSeats: minSeats,
    sort: sortKey,
  });

  useEffect(() => {
    if (isVisible) {
      return;
    }

    const frameId = window.requestAnimationFrame(() => {
      setIsMobileFiltersOpen(false);
    });

    return () => window.cancelAnimationFrame(frameId);
  }, [isVisible]);

  const updatePassengerCount = (amount: number) => {
    setMinSeats((currentCount) =>
      Math.min(4, Math.max(1, currentCount + amount)),
    );
  };

  const updateSearchDate = (date: string) => {
    setSearchDate(date || todayKey);
  };

  const toggleSearchRoute = () => {
    setIsRouteReversed((currentValue) => !currentValue);
  };

  const toggleMobileFilters = () => {
    setIsMobileFiltersOpen((currentValue) => !currentValue);
  };

  const togglePassengerPicker = () => {
    setIsPassengerPickerOpen((currentValue) => !currentValue);
  };

  const closePassengerPicker = () => {
    setIsPassengerPickerOpen(false);
  };

  const openSearchDatePicker = () => {
    setIsPassengerPickerOpen(false);
    searchDateInputRef.current?.focus();
    searchDateInputRef.current?.showPicker?.();
  };

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
            availableTrips={totalTrips}
            searchDateLabel={searchDateLabel}
            minSeats={minSeats}
            sort={sortKey}
            onSortChange={setSortKey}
            onMinSeatsChange={setMinSeats}
          />

          <SearchResults
            trips={trips}
            totalTrips={totalTrips}
            searchDateLabel={searchDateLabel}
            minSeats={minSeats}
            isLoading={isLoading}
            errorMessage={errorMessage}
            onRetry={retry}
          />
        </div>
      </div>
    </section>
  );
}
