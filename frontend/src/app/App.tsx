import { Calendar, UserRound, ArrowRightLeft } from "lucide-react";
import {
  type CSSProperties,
  type PointerEvent,
  useMemo,
  useRef,
  useState,
} from "react";
import { SiteHeader } from "./layout/SiteHeader";
import type { AppView } from "./navigation/app-view";
import { ArrowIcon } from "../shared/icons/ArrowIcon";
import { HomeHero } from "../features/home/components/HomeHero";
import { SponsorsStrip } from "../features/home/components/SponsorsStrip";
import { mockTrips } from "../features/trip-search/mocks/trips";
import type { TripSearchSort } from "../features/trip-search/model/trip-search";
import { filterTrips } from "../features/trip-search/lib/filterTrips";
import {
  getDateFromKey,
  getDateKey,
  getDayOffset,
  getMonthLabel,
  getReadableDate,
  weekDays,
} from "../shared/lib/date";

import { usePublishTripWizard } from "../features/trip-publishing/hooks/usePublishTripWizard";
import {
  publishPriceOptions,
  publishSteps,
} from "../features/trip-publishing/model/publish-trip-draft";
import { PublishRouteStep } from "../features/trip-publishing/components/PublishRouteStep";
import { PublishDateStep } from "../features/trip-publishing/components/PublishDateStep";
import { PublishTimeStep } from "../features/trip-publishing/components/PublishTimeStep";
import { PublishSeatsStep } from "../features/trip-publishing/components/PublishSeatsStep";

const today = new Date(2026, 5, 25);
const calendarMinMonth = new Date(today.getFullYear(), today.getMonth(), 1);

const SearchIcon = () => (
  <svg aria-hidden="true" viewBox="0 0 24 24" focusable="false">
    <circle cx="11" cy="11" r="7" />
    <path d="m16 16 4 4" />
  </svg>
);

const CarIcon = () => (
  <svg aria-hidden="true" viewBox="0 0 24 24" focusable="false">
    <path d="m5 12 1.8-4.2A3 3 0 0 1 9.6 6h4.8a3 3 0 0 1 2.8 1.8L19 12" />
    <path d="M4 12h16v5H4z" />
    <circle cx="7" cy="17" r="2" />
    <circle cx="17" cy="17" r="2" />
  </svg>
);

const StarIcon = () => (
  <svg aria-hidden="true" viewBox="0 0 24 24" focusable="false">
    <path d="m12 3 2.7 5.5 6.1.9-4.4 4.3 1 6.1L12 16.9l-5.4 2.9 1-6.1-4.4-4.3 6.1-.9L12 3z" />
  </svg>
);

function App() {
  const [activeView, setActiveView] = useState<AppView>("home");
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [isMobileFiltersOpen, setIsMobileFiltersOpen] = useState(false);
  const [sortKey, setSortKey] = useState<TripSearchSort>("earliest");
  const [verifiedOnly, setVerifiedOnly] = useState(false);
  const [searchDate, setSearchDate] = useState(() => getDateKey(new Date()));
  const [minSeats, setMinSeats] = useState(1);
  const [isPassengerPickerOpen, setIsPassengerPickerOpen] = useState(false);
  const [isRouteReversed, setIsRouteReversed] = useState(false);

  const {
    publishStep,
    isPublishComplete,
    publishDraft,
    currentPublishStep,
    isPublishSummaryStep,
    visibleCalendarMonths,
    canGoToPreviousCalendarMonth,
    publishProgress,
    updatePublishDraft,
    selectPublishDate,
    swapRoute,
    goToNextPublishStep,
    goToPreviousPublishStep,
    resetPublishWizard,
    goToPreviousCalendarMonth,
    goToNextCalendarMonth,
  } = usePublishTripWizard({
    calendarMinMonth,
  });

  const heroFrameRef = useRef<HTMLDivElement>(null);
  const parallaxFrameRef = useRef<number | null>(null);
  const searchDateInputRef = useRef<HTMLInputElement>(null);

  const origin = isRouteReversed ? "Ciudad Real" : "Daimiel";
  const destination = isRouteReversed ? "Daimiel" : "Ciudad Real";

  const todayKey = getDateKey(new Date());
  const publishMinimumDateKey = getDateKey(today);
  const searchDateLabel =
    searchDate === todayKey
      ? "Hoy"
      : getReadableDate(getDateFromKey(searchDate));
  const searchDateShortLabel = `${getDateFromKey(searchDate).getDate()} ${getMonthLabel(getDateFromKey(searchDate)).slice(0, 3)}`;

  const visibleTrips = useMemo(() => {
    const searchDayOffset = getDayOffset(searchDate, todayKey);

    return filterTrips({
      trips: mockTrips,
      dayOffset: searchDayOffset,
      minSeats,
      verifiedOnly,
      sort: sortKey,
    });
  }, [minSeats, searchDate, sortKey, todayKey, verifiedOnly]);

  const navigateTo = (view: AppView) => {
    setActiveView(view);
    setIsMobileMenuOpen(false);
    setIsMobileFiltersOpen(false);
  };

  const showSearchView = () => navigateTo("search");

  const showPublishView = () => navigateTo("publish");

  const showHomeView = () => navigateTo("home");

  const updatePassengerCount = (amount: number) => {
    setMinSeats((currentCount) =>
      Math.min(4, Math.max(1, currentCount + amount)),
    );
  };

  const openSearchDatePicker = () => {
    setIsPassengerPickerOpen(false);
    searchDateInputRef.current?.focus();
    searchDateInputRef.current?.showPicker?.();
  };

  const handleHeroPointerMove = (event: PointerEvent<HTMLElement>) => {
    if (event.pointerType !== "mouse") {
      return;
    }

    const frame = heroFrameRef.current;

    if (!frame) {
      return;
    }

    const rect = frame.getBoundingClientRect();
    const x = (event.clientX - rect.left) / rect.width - 0.5;
    const y = (event.clientY - rect.top) / rect.height - 0.5;

    if (parallaxFrameRef.current !== null) {
      cancelAnimationFrame(parallaxFrameRef.current);
    }

    parallaxFrameRef.current = requestAnimationFrame(() => {
      frame.style.setProperty("--parallax-x", `${x.toFixed(4)}`);
      frame.style.setProperty("--parallax-y", `${y.toFixed(4)}`);
      parallaxFrameRef.current = null;
    });
  };

  const handleHeroPointerLeave = () => {
    const frame = heroFrameRef.current;

    if (!frame) {
      return;
    }

    if (parallaxFrameRef.current !== null) {
      cancelAnimationFrame(parallaxFrameRef.current);
      parallaxFrameRef.current = null;
    }

    frame.style.setProperty("--parallax-x", "0");
    frame.style.setProperty("--parallax-y", "0");
  };

  return (
    <main className="app-shell">
      <section
        className="premium-hero"
        aria-labelledby="hero-title"
        onPointerMove={handleHeroPointerMove}
      >
        <div
          className={`hero-frame is-${activeView}`}
          ref={heroFrameRef}
          onPointerLeave={handleHeroPointerLeave}
        >
          <div className="hero-backdrop" aria-hidden="true">
            <div className="hero-backdrop-image" />
            <div className="hero-water-glow" />
          </div>

          <SiteHeader
            activeView={activeView}
            isMobileMenuOpen={isMobileMenuOpen}
            onNavigate={navigateTo}
            onToggleMobileMenu={() =>
              setIsMobileMenuOpen((currentValue) => !currentValue)
            }
            onCloseMobileMenu={() => setIsMobileMenuOpen(false)}
          />

          <HomeHero
            isVisible={activeView === "home"}
            onSearchTrips={showSearchView}
            onPublishTrip={showPublishView}
          />

          <section
            className="search-view"
            aria-label="Buscar viaje"
            aria-hidden={activeView !== "search"}
          >
            <div className="search-panel">
              <div className="mobile-search-summary" role="search">
                <SearchIcon />
                <button
                  className="mobile-route-summary"
                  type="button"
                  onClick={() =>
                    setIsRouteReversed((currentValue) => !currentValue)
                  }
                  aria-label="Cambiar origen y destino"
                >
                  <strong>
                    <span>{origin}</span>
                    <ArrowIcon />
                    <span>{destination}</span>
                  </strong>
                  <span>
                    {searchDateLabel}, {minSeats} plaza{minSeats > 1 ? "s" : ""}
                  </span>
                </button>
                <button
                  className="mobile-filter-toggle"
                  type="button"
                  onClick={() =>
                    setIsMobileFiltersOpen((currentValue) => !currentValue)
                  }
                  aria-expanded={isMobileFiltersOpen}
                  aria-controls="mobile-filters-panel"
                >
                  Filtrar
                </button>
                <div className="mobile-search-controls">
                  <label className="mobile-date-control">
                    <span>Fecha</span>
                    <input
                      type="date"
                      min={todayKey}
                      value={searchDate}
                      onChange={(event) =>
                        setSearchDate(event.target.value || todayKey)
                      }
                      aria-label="Fecha del viaje"
                    />
                  </label>
                  <div
                    className="mobile-passenger-control"
                    aria-label="Pasajeros"
                  >
                    <button
                      type="button"
                      onClick={() => updatePassengerCount(-1)}
                      disabled={minSeats === 1}
                      aria-label="Quitar un pasajero"
                    >
                      -
                    </button>
                    <span>{minSeats}</span>
                    <button
                      type="button"
                      onClick={() => updatePassengerCount(1)}
                      disabled={minSeats === 4}
                      aria-label="Anadir un pasajero"
                    >
                      +
                    </button>
                  </div>
                </div>
              </div>

              <div className="search-bar" role="search">
                <div className="search-field">
                  <span>Origen</span>
                  <strong>{origin}</strong>
                </div>
                <button
                  className="swap-route"
                  type="button"
                  onClick={() =>
                    setIsRouteReversed((currentValue) => !currentValue)
                  }
                  aria-label="Cambiar origen y destino"
                >
                  <ArrowRightLeft aria-hidden="true" />
                </button>
                <div className="search-field">
                  <span>Destino</span>
                  <strong>{destination}</strong>
                </div>
                <label
                  className="search-field search-date-field is-small"
                  onClick={openSearchDatePicker}
                >
                  <span>Fecha</span>
                  <strong className="search-field-value">
                    {searchDateShortLabel}
                    <Calendar aria-hidden="true" />
                  </strong>
                  <input
                    ref={searchDateInputRef}
                    type="date"
                    min={todayKey}
                    value={searchDate}
                    onChange={(event) =>
                      setSearchDate(event.target.value || todayKey)
                    }
                    aria-label="Fecha del viaje"
                  />
                </label>
                <div className="search-field passenger-search-field is-small">
                  <span>Pasajeros</span>
                  <button
                    className="passenger-display"
                    type="button"
                    onClick={() =>
                      setIsPassengerPickerOpen((currentValue) => !currentValue)
                    }
                    aria-expanded={isPassengerPickerOpen}
                    aria-controls="passenger-picker"
                  >
                    <UserRound aria-hidden="true" />
                    <strong>{minSeats}</strong>
                  </button>
                  <div
                    className={`passenger-popover ${isPassengerPickerOpen ? "is-open" : ""}`}
                    id="passenger-picker"
                  >
                    <button
                      type="button"
                      onClick={() => updatePassengerCount(-1)}
                      disabled={minSeats === 1}
                      aria-label="Quitar un pasajero"
                    >
                      -
                    </button>
                    <strong>{minSeats}</strong>
                    <button
                      type="button"
                      onClick={() => updatePassengerCount(1)}
                      disabled={minSeats === 4}
                      aria-label="Anadir un pasajero"
                    >
                      +
                    </button>
                  </div>
                </div>
                <button
                  className="search-submit"
                  type="button"
                  onClick={() => setIsPassengerPickerOpen(false)}
                >
                  Buscar
                </button>
              </div>

              <div className="search-layout">
                <aside
                  className={`filters-panel ${isMobileFiltersOpen ? "is-open" : ""}`}
                  id="mobile-filters-panel"
                  aria-label="Filtros de busqueda"
                >
                  <div className="route-card">
                    <strong className="route-title">
                      <span>{origin}</span>
                      <ArrowIcon />
                      <span>{destination}</span>
                    </strong>
                    <small>
                      {visibleTrips.length} viajes disponibles ·{" "}
                      {searchDateLabel}, {minSeats} plaza
                      {minSeats > 1 ? "s" : ""}
                    </small>
                  </div>

                  <div className="filter-group">
                    <div className="filter-heading">
                      <h2>Ordenar por</h2>
                    </div>
                    <button
                      className={`filter-option ${sortKey === "earliest" ? "is-selected" : ""}`}
                      type="button"
                      onClick={() => setSortKey("earliest")}
                      aria-pressed={sortKey === "earliest"}
                    >
                      <span />
                      Salida mas temprana
                    </button>
                    <button
                      className={`filter-option ${sortKey === "price" ? "is-selected" : ""}`}
                      type="button"
                      onClick={() => setSortKey("price")}
                      aria-pressed={sortKey === "price"}
                    >
                      <span />
                      Precio mas bajo
                    </button>
                    <button
                      className={`filter-option ${sortKey === "duration" ? "is-selected" : ""}`}
                      type="button"
                      onClick={() => setSortKey("duration")}
                      aria-pressed={sortKey === "duration"}
                    >
                      <span />
                      Viaje mas corto
                    </button>
                  </div>

                  <div className="filter-group">
                    <div className="filter-heading">
                      <h2>Confianza</h2>
                    </div>
                    <label className="toggle-option">
                      <input
                        type="checkbox"
                        checked={verifiedOnly}
                        onChange={(event) =>
                          setVerifiedOnly(event.target.checked)
                        }
                      />
                      <span>Solo conductores verificados</span>
                    </label>
                  </div>

                  <div className="filter-group">
                    <div className="filter-heading">
                      <h2>Plazas</h2>
                    </div>
                    <div className="seat-controls" aria-label="Plazas minimas">
                      {[1, 2, 3].map((seatCount) => (
                        <button
                          className={
                            minSeats === seatCount ? "is-selected" : undefined
                          }
                          type="button"
                          onClick={() => setMinSeats(seatCount)}
                          aria-pressed={minSeats === seatCount}
                          key={seatCount}
                        >
                          {seatCount}
                        </button>
                      ))}
                    </div>
                  </div>
                </aside>

                <div className="results-column">
                  <div className="results-header">
                    <div className="results-summary">
                      <strong>{visibleTrips.length} viajes disponibles</strong>
                      <span>
                        {searchDateLabel}, {minSeats} plaza
                        {minSeats > 1 ? "s" : ""}
                      </span>
                    </div>
                    <div className="results-tabs" aria-label="Tipos de viaje">
                      <button className="is-selected" type="button">
                        Todo
                      </button>
                      <button type="button">
                        <CarIcon />
                        Coche
                      </button>
                    </div>
                  </div>

                  <div className="trip-list">
                    {visibleTrips.length > 0 ? (
                      visibleTrips.map((trip) => (
                        <article className="trip-card" key={trip.id}>
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
                                <svg
                                  viewBox="0 0 120 36"
                                  preserveAspectRatio="none"
                                >
                                  <path d="M12 10 C34 10 31 28 58 28 H112" />
                                </svg>
                              </span>
                              <em>{trip.duration}</em>
                              <span
                                className="trip-route-segment trip-route-segment-right"
                                aria-hidden="true"
                              >
                                <svg
                                  viewBox="0 0 120 36"
                                  preserveAspectRatio="none"
                                >
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
                                {trip.priceLabel.replace(" EUR", "€")}
                              </strong>
                              <span>
                                <i aria-hidden="true" /> {trip.seats} plaza
                                {trip.seats > 1 ? "s" : ""}
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
                      ))
                    ) : (
                      <div className="empty-results">
                        <strong>No hay viajes con esos filtros</strong>
                        <span>
                          Prueba otra fecha o reduce el numero de pasajeros.
                        </span>
                      </div>
                    )}
                  </div>
                </div>
              </div>
            </div>
          </section>

          <section
            className="publish-view"
            aria-label="Publicar viaje"
            aria-hidden={activeView !== "publish"}
          >
            <div className="publish-card">
              {isPublishComplete ? (
                <div className="publish-success">
                  <span className="publish-kicker">Viaje preparado</span>
                  <h2>Tu salida esta lista para publicarse</h2>
                  <p>
                    Hemos guardado el borrador de {publishDraft.origin} a{" "}
                    {publishDraft.destination}. En una version real aqui se
                    enviaria al backend.
                  </p>
                  <div className="publish-summary-card">
                    <div>
                      <span>Ruta</span>
                      <strong>
                        {publishDraft.origin} <ArrowIcon />{" "}
                        {publishDraft.destination}
                      </strong>
                    </div>
                    <div>
                      <span>Fecha y hora</span>
                      <strong>
                        {publishDraft.date}, {publishDraft.time}
                      </strong>
                    </div>
                    <div>
                      <span>Plazas</span>
                      <strong>{publishDraft.seats}</strong>
                    </div>
                    <div>
                      <span>Precio</span>
                      <strong>{publishDraft.price} EUR</strong>
                    </div>
                  </div>
                  <div className="publish-actions">
                    <button
                      className="secondary-link"
                      type="button"
                      onClick={resetPublishWizard}
                    >
                      Crear otro viaje
                    </button>
                    <button
                      className="primary-link"
                      type="button"
                      onClick={showHomeView}
                    >
                      Volver al inicio
                    </button>
                  </div>
                </div>
              ) : (
                <>
                  <div className="publish-step-shell" key={publishStep}>
                    <div
                      className={`publish-copy ${isPublishSummaryStep ? "is-summary" : ""}`}
                    >
                      <h2>
                        {currentPublishStep === "Ruta" &&
                          "Define la ruta del viaje"}
                        {currentPublishStep === "Fecha" &&
                          "Elige el dia de salida"}
                        {currentPublishStep === "Hora" && "A que hora sales"}
                        {currentPublishStep === "Plazas" &&
                          "Cuantas plazas ofreces"}
                        {currentPublishStep === "Precio" &&
                          "Pon precio por plaza"}
                        {currentPublishStep === "Resumen" &&
                          "Viaje listo para publicar"}
                      </h2>
                      <p>
                        {currentPublishStep === "Ruta" &&
                          "Usa la ruta habitual o ajustala antes de continuar."}
                        {currentPublishStep === "Hora" &&
                          "Selecciona la hora de salida."}
                        {currentPublishStep === "Plazas" &&
                          "Indica cuantas personas pueden reservar tu coche."}
                        {currentPublishStep === "Precio" &&
                          "El precio se mostrara a los pasajeros en la busqueda."}
                        {currentPublishStep === "Resumen" &&
                          "Comprueba los datos antes de hacer visible tu salida."}
                      </p>
                    </div>

                    <div className="publish-step-body">
                      {currentPublishStep === "Ruta" && (
                        <PublishRouteStep
                          origin={publishDraft.origin}
                          destination={publishDraft.destination}
                          onOriginChange={(origin) =>
                            updatePublishDraft("origin", origin)
                          }
                          onDestinationChange={(destination) =>
                            updatePublishDraft("destination", destination)
                          }
                          onSwapRoute={swapRoute}
                        />
                      )}

                      {currentPublishStep === "Fecha" && (
                        <PublishDateStep
                          selectedDateKey={publishDraft.dateKey}
                          selectedDateLabel={publishDraft.date}
                          visibleMonths={visibleCalendarMonths}
                          canGoToPreviousMonth={canGoToPreviousCalendarMonth}
                          minimumDateKey={publishMinimumDateKey}
                          onPreviousMonth={goToPreviousCalendarMonth}
                          onNextMonth={goToNextCalendarMonth}
                          onSelectDate={selectPublishDate}
                        />
                      )}

                      {currentPublishStep === "Hora" && (
                        <PublishTimeStep
                          time={publishDraft.time}
                          onTimeChange={(time) =>
                            updatePublishDraft("time", time)
                          }
                        />
                      )}

                      {currentPublishStep === "Plazas" && (
                        <PublishSeatsStep
                          seats={publishDraft.seats}
                          onSeatsChange={(seats) =>
                            updatePublishDraft("seats", seats)
                          }
                        />
                      )}

                      {currentPublishStep === "Precio" && (
                        <div className="publish-price-step">
                          <div className="publish-choice-grid">
                            {publishPriceOptions.map((priceOption) => (
                              <button
                                className={
                                  publishDraft.price === priceOption
                                    ? "is-selected"
                                    : undefined
                                }
                                type="button"
                                onClick={() =>
                                  updatePublishDraft("price", priceOption)
                                }
                                aria-pressed={
                                  publishDraft.price === priceOption
                                }
                                key={priceOption}
                              >
                                {priceOption} EUR
                              </button>
                            ))}
                          </div>
                          <label>
                            <span>Otro precio</span>
                            <input
                              inputMode="decimal"
                              value={publishDraft.price}
                              onChange={(event) =>
                                updatePublishDraft("price", event.target.value)
                              }
                            />
                          </label>
                        </div>
                      )}

                      {currentPublishStep === "Resumen" && (
                        <div className="publish-review-card">
                          <div className="review-route">
                            <span className="review-eyebrow">Trayecto</span>
                            <div className="review-route-line">
                              <div className="review-place">
                                <small>Salida</small>
                                <strong>{publishDraft.origin}</strong>
                              </div>
                              <span
                                className="review-route-track"
                                aria-hidden="true"
                              >
                                <i />
                                <ArrowIcon />
                              </span>
                              <div className="review-place">
                                <small>Llegada</small>
                                <strong>{publishDraft.destination}</strong>
                              </div>
                            </div>
                          </div>

                          <div
                            className="review-details"
                            aria-label="Detalles del viaje"
                          >
                            <div>
                              <span>Dia</span>
                              <strong>{publishDraft.date}</strong>
                            </div>
                            <div>
                              <span>Hora</span>
                              <strong>{publishDraft.time}</strong>
                            </div>
                            <div>
                              <span>Plazas</span>
                              <strong>
                                {publishDraft.seats} plaza
                                {publishDraft.seats > 1 ? "s" : ""}
                              </strong>
                            </div>
                            <div>
                              <span>Precio</span>
                              <strong>{publishDraft.price} EUR</strong>
                            </div>
                          </div>
                        </div>
                      )}
                    </div>
                  </div>

                  <div className="publish-actions">
                    <button
                      className="secondary-link"
                      type="button"
                      onClick={goToPreviousPublishStep}
                      disabled={publishStep === 0}
                    >
                      Atras
                    </button>
                    <button
                      className="primary-link"
                      type="button"
                      onClick={goToNextPublishStep}
                    >
                      {isPublishSummaryStep ? "Publicar viaje" : "Continuar"}
                      <ArrowIcon />
                    </button>
                  </div>

                  <div
                    className="publish-mobile-progress"
                    style={
                      { "--publish-progress": publishProgress } as CSSProperties
                    }
                    aria-label={`Paso ${publishStep + 1} de ${publishSteps.length}: ${currentPublishStep}`}
                  >
                    <span>
                      Paso {publishStep + 1} de {publishSteps.length}
                    </span>
                    <i aria-hidden="true" />
                  </div>

                  <div
                    className="publish-progress"
                    aria-label="Progreso de publicacion"
                  >
                    {publishSteps.map((step, index) => (
                      <div
                        className={`publish-progress-item ${index < publishStep ? "is-complete" : ""} ${
                          index === publishStep ? "is-current" : ""
                        }`}
                        key={step}
                      >
                        <span aria-label={`${step}, paso ${index + 1}`}>
                          {index + 1}
                        </span>
                        {index < publishSteps.length - 1 && (
                          <i aria-hidden="true" />
                        )}
                      </div>
                    ))}
                  </div>
                </>
              )}
            </div>
          </section>
          <SponsorsStrip isVisible={activeView === "home"} />
        </div>
      </section>
    </main>
  );
}

export default App;
