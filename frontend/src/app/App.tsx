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

type Sponsor = {
  name: string;
};

type SortKey = "earliest" | "price" | "duration";

type PublishDraft = {
  origin: string;
  destination: string;
  date: string;
  dateKey: string;
  time: string;
  seats: number;
  price: string;
};

type Trip = {
  id: number;
  departureTime: string;
  arrivalTime: string;
  duration: string;
  durationMinutes: number;
  from: string;
  to: string;
  driver: string;
  driverAvatarUrl: string;
  rating: string;
  price: number;
  priceLabel: string;
  seats: number;
  verified: boolean;
  dayOffsets: number[];
  tags: string[];
};

const sponsors: Sponsor[] = [
  { name: "Plaza Mayor" },
  { name: "Taller El Carmen" },
  { name: "Cervantes" },
  { name: "La Vega" },
  { name: "Autoescuela Daimiel" },
  { name: "Casa Azuer" },
  { name: "Tablas Cafe" },
  { name: "Ruta 430" },
];

const sponsorLoop = [...sponsors, ...sponsors];

const trips: Trip[] = [
  {
    id: 1,
    departureTime: "07:15",
    arrivalTime: "07:48",
    duration: "33 min",
    durationMinutes: 33,
    from: "Daimiel centro",
    to: "Ciudad Real AVE",
    driver: "Lucia",
    driverAvatarUrl:
      "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=96&h=96&q=80",
    rating: "4,9",
    price: 4.8,
    priceLabel: "4,80 EUR",
    seats: 2,
    verified: true,
    dayOffsets: [0, 1, 2],
    tags: ["Reserva rapida", "2 plazas"],
  },
  {
    id: 2,
    departureTime: "07:40",
    arrivalTime: "08:18",
    duration: "38 min",
    durationMinutes: 38,
    from: "Estacion de Daimiel",
    to: "Campus Ciudad Real",
    driver: "Mario",
    driverAvatarUrl:
      "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=96&h=96&q=80",
    rating: "5",
    price: 3.9,
    priceLabel: "3,90 EUR",
    seats: 1,
    verified: false,
    dayOffsets: [0],
    tags: ["Economico", "1 plaza"],
  },
  {
    id: 3,
    departureTime: "08:05",
    arrivalTime: "08:39",
    duration: "34 min",
    durationMinutes: 34,
    from: "Daimiel norte",
    to: "Hospital General",
    driver: "Ana",
    driverAvatarUrl:
      "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=96&h=96&q=80",
    rating: "4,8",
    price: 5.2,
    priceLabel: "5,20 EUR",
    seats: 3,
    verified: true,
    dayOffsets: [0, 2],
    tags: ["Verificado", "Max. 2 atras"],
  },
  {
    id: 4,
    departureTime: "14:20",
    arrivalTime: "14:55",
    duration: "35 min",
    durationMinutes: 35,
    from: "Plaza de Espana",
    to: "Ciudad Real centro",
    driver: "Ruben",
    driverAvatarUrl:
      "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=96&h=96&q=80",
    rating: "4,7",
    price: 4.4,
    priceLabel: "4,40 EUR",
    seats: 2,
    verified: true,
    dayOffsets: [0, 1],
    tags: ["Aire acondicionado", "2 plazas"],
  },
  {
    id: 5,
    departureTime: "18:10",
    arrivalTime: "18:46",
    duration: "36 min",
    durationMinutes: 36,
    from: "Daimiel sur",
    to: "Puerta de Toledo",
    driver: "Carmen",
    driverAvatarUrl:
      "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=96&h=96&q=80",
    rating: "5",
    price: 4.2,
    priceLabel: "4,20 EUR",
    seats: 4,
    verified: false,
    dayOffsets: [1, 3],
    tags: ["Flexible", "4 plazas"],
  },
];

const publishSteps = ["Ruta", "Fecha", "Hora", "Plazas", "Precio", "Resumen"];
const weekDays = ["Dom", "Lun", "Mar", "Mie", "Jue", "Vie", "Sab"];
const monthNames = [
  "enero",
  "febrero",
  "marzo",
  "abril",
  "mayo",
  "junio",
  "julio",
  "agosto",
  "septiembre",
  "octubre",
  "noviembre",
  "diciembre",
];
const today = new Date(2026, 5, 25);
const calendarMinMonth = new Date(today.getFullYear(), today.getMonth(), 1);
const publishPriceOptions = ["3,50", "4,00", "4,50", "5,00"];
const initialPublishDraft: PublishDraft = {
  origin: "Daimiel",
  destination: "Ciudad Real",
  date: "25 junio",
  dateKey: "2026-06-25",
  time: "07:30",
  seats: 2,
  price: "4,00",
};

const getDateKey = (date: Date) =>
  `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;

const getReadableDate = (date: Date) =>
  `${date.getDate()} ${monthNames[date.getMonth()]}`;

const getMonthLabel = (date: Date) => {
  const month = monthNames[date.getMonth()];

  return `${month.charAt(0).toUpperCase()}${month.slice(1)}`;
};

const getCalendarMonth = (date: Date) => {
  const year = date.getFullYear();
  const month = date.getMonth();
  const daysInMonth = new Date(year, month + 1, 0).getDate();

  return {
    key: `${year}-${month}`,
    label: getMonthLabel(date),
    year,
    month,
    startOffset: new Date(year, month, 1).getDay(),
    days: Array.from(
      { length: daysInMonth },
      (_, index) => new Date(year, month, index + 1),
    ),
  };
};

const addMonths = (date: Date, amount: number) =>
  new Date(date.getFullYear(), date.getMonth() + amount, 1);

const isBeforeMonth = (date: Date, compareDate: Date) =>
  date.getFullYear() < compareDate.getFullYear() ||
  (date.getFullYear() === compareDate.getFullYear() &&
    date.getMonth() < compareDate.getMonth());

const getDateFromKey = (dateKey: string) => {
  const [year, month, day] = dateKey.split("-").map(Number);

  return new Date(year, month - 1, day);
};

const getDayOffset = (dateKey: string, baseDateKey: string) => {
  const millisecondsPerDay = 24 * 60 * 60 * 1000;

  return Math.round(
    (getDateFromKey(dateKey).getTime() -
      getDateFromKey(baseDateKey).getTime()) /
      millisecondsPerDay,
  );
};


const ArrowIcon = () => (
  <svg aria-hidden="true" viewBox="0 0 24 24" focusable="false">
    <path d="M5 12h14m-5-5 5 5-5 5" />
  </svg>
);

const SwitchIcon = () => (
  <svg aria-hidden="true" viewBox="0 0 24 24" focusable="false">
    <path d="M7 7h10m0 0-3-3m3 3-3 3" />
    <path d="M17 17H7m0 0 3 3m-3-3 3-3" />
  </svg>
);

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
  const [publishStep, setPublishStep] = useState(0);
  const [isPublishComplete, setIsPublishComplete] = useState(false);
  const [publishDraft, setPublishDraft] =
    useState<PublishDraft>(initialPublishDraft);
  const [calendarCursor, setCalendarCursor] = useState(calendarMinMonth);
  const [sortKey, setSortKey] = useState<SortKey>("earliest");
  const [verifiedOnly, setVerifiedOnly] = useState(false);
  const [searchDate, setSearchDate] = useState(() => getDateKey(new Date()));
  const [minSeats, setMinSeats] = useState(1);
  const [isPassengerPickerOpen, setIsPassengerPickerOpen] = useState(false);
  const [isRouteReversed, setIsRouteReversed] = useState(false);
  const heroFrameRef = useRef<HTMLDivElement>(null);
  const parallaxFrameRef = useRef<number | null>(null);
  const searchDateInputRef = useRef<HTMLInputElement>(null);

  const origin = isRouteReversed ? "Ciudad Real" : "Daimiel";
  const destination = isRouteReversed ? "Daimiel" : "Ciudad Real";
  const currentPublishStep = publishSteps[publishStep];
  const isPublishSummaryStep = publishStep === publishSteps.length - 1;
  const visibleCalendarMonths = [
    getCalendarMonth(calendarCursor),
    getCalendarMonth(addMonths(calendarCursor, 1)),
  ];
  const canGoToPreviousCalendarMonth = !isBeforeMonth(
    addMonths(calendarCursor, -1),
    calendarMinMonth,
  );
  const publishProgress = `${(publishStep / (publishSteps.length - 1)) * 100}%`;
  const todayKey = getDateKey(new Date());
  const searchDateLabel =
    searchDate === todayKey
      ? "Hoy"
      : getReadableDate(getDateFromKey(searchDate));
  const searchDateShortLabel = `${getDateFromKey(searchDate).getDate()} ${getMonthLabel(getDateFromKey(searchDate)).slice(0, 3)}`;

  const visibleTrips = useMemo(() => {
    const searchDayOffset = getDayOffset(searchDate, todayKey);

    return trips
      .filter((trip) => trip.dayOffsets.includes(searchDayOffset))
      .filter((trip) => !verifiedOnly || trip.verified)
      .filter((trip) => trip.seats >= minSeats)
      .toSorted((firstTrip, secondTrip) => {
        if (sortKey === "price") {
          return firstTrip.price - secondTrip.price;
        }

        if (sortKey === "duration") {
          return firstTrip.durationMinutes - secondTrip.durationMinutes;
        }

        return firstTrip.departureTime.localeCompare(secondTrip.departureTime);
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

  const updatePublishDraft = <Key extends keyof PublishDraft>(
    key: Key,
    value: PublishDraft[Key],
  ) => {
    setPublishDraft((currentDraft) => ({
      ...currentDraft,
      [key]: value,
    }));
    setIsPublishComplete(false);
  };

  const goToNextPublishStep = () => {
    if (isPublishSummaryStep) {
      setIsPublishComplete(true);
      return;
    }

    setPublishStep((currentStep) =>
      Math.min(currentStep + 1, publishSteps.length - 1),
    );
  };

  const goToPreviousPublishStep = () => {
    setIsPublishComplete(false);
    setPublishStep((currentStep) => Math.max(currentStep - 1, 0));
  };

  const resetPublishWizard = () => {
    setPublishDraft(initialPublishDraft);
    setPublishStep(0);
    setIsPublishComplete(false);
    setCalendarCursor(calendarMinMonth);
  };

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

          <div className="hero-content" aria-hidden={activeView !== "home"}>
            <p className="eyebrow">Viajes compartidos locales</p>
            <h1 id="hero-title">Muevete entre Daimiel y Ciudad Real</h1>
            <div className="hero-actions" aria-label="Acciones principales">
              <button
                className="primary-link"
                type="button"
                onClick={showSearchView}
              >
                Buscar viaje
                <ArrowIcon />
              </button>
              <button
                className="secondary-link"
                type="button"
                onClick={showPublishView}
              >
                Publicar salida
              </button>
            </div>
          </div>

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
                        <div className="publish-route-step">
                          <label>
                            <span>Origen</span>
                            <input
                              value={publishDraft.origin}
                              onChange={(event) =>
                                updatePublishDraft("origin", event.target.value)
                              }
                            />
                          </label>
                          <button
                            className="publish-swap"
                            type="button"
                            onClick={() =>
                              setPublishDraft((currentDraft) => ({
                                ...currentDraft,
                                origin: currentDraft.destination,
                                destination: currentDraft.origin,
                              }))
                            }
                            aria-label="Invertir origen y destino"
                          >
                            <SwitchIcon />
                          </button>
                          <label>
                            <span>Destino</span>
                            <input
                              value={publishDraft.destination}
                              onChange={(event) =>
                                updatePublishDraft(
                                  "destination",
                                  event.target.value,
                                )
                              }
                            />
                          </label>
                        </div>
                      )}

                      {currentPublishStep === "Fecha" && (
                        <div className="publish-calendar-step">
                          <div className="calendar-toolbar">
                            <button
                              type="button"
                              onClick={() =>
                                setCalendarCursor((currentDate) =>
                                  addMonths(currentDate, -1),
                                )
                              }
                              disabled={!canGoToPreviousCalendarMonth}
                              aria-label="Mostrar mes anterior"
                            >
                              <ArrowIcon />
                            </button>
                            <span>{publishDraft.date}</span>
                            <button
                              type="button"
                              onClick={() =>
                                setCalendarCursor((currentDate) =>
                                  addMonths(currentDate, 1),
                                )
                              }
                              aria-label="Mostrar mes siguiente"
                            >
                              <ArrowIcon />
                            </button>
                          </div>

                          <div className="calendar-months">
                            {visibleCalendarMonths.map((month, monthIndex) => (
                              <div
                                className={`calendar-month month-${monthIndex + 1}`}
                                key={month.key}
                              >
                                <h3>{month.label}</h3>
                                <div className="calendar-grid is-weekdays">
                                  {weekDays.map((weekDay) => (
                                    <span key={weekDay}>{weekDay}</span>
                                  ))}
                                </div>
                                <div className="calendar-grid">
                                  {Array.from(
                                    { length: month.startOffset },
                                    (_, index) => (
                                      <span
                                        className="calendar-empty"
                                        key={`${month.key}-empty-${index}`}
                                      />
                                    ),
                                  )}
                                  {month.days.map((dayDate) => {
                                    const dateKey = getDateKey(dayDate);
                                    const dateLabel = getReadableDate(dayDate);
                                    const isPast = dateKey < getDateKey(today);

                                    return (
                                      <button
                                        className={
                                          publishDraft.dateKey === dateKey
                                            ? "is-selected"
                                            : undefined
                                        }
                                        type="button"
                                        onClick={() => {
                                          updatePublishDraft("date", dateLabel);
                                          updatePublishDraft(
                                            "dateKey",
                                            dateKey,
                                          );
                                        }}
                                        aria-pressed={
                                          publishDraft.dateKey === dateKey
                                        }
                                        disabled={isPast}
                                        key={dateKey}
                                      >
                                        {dayDate.getDate()}
                                      </button>
                                    );
                                  })}
                                </div>
                              </div>
                            ))}
                          </div>
                        </div>
                      )}

                      {currentPublishStep === "Hora" && (
                        <div className="publish-time-step">
                          <label>
                            <span>Hora de salida</span>
                            <input
                              type="time"
                              value={publishDraft.time}
                              step="900"
                              onChange={(event) =>
                                updatePublishDraft("time", event.target.value)
                              }
                            />
                          </label>
                        </div>
                      )}

                      {currentPublishStep === "Plazas" && (
                        <div className="publish-seat-grid">
                          {[1, 2, 3, 4].map((seatCount) => (
                            <button
                              className={
                                publishDraft.seats === seatCount
                                  ? "is-selected"
                                  : undefined
                              }
                              type="button"
                              onClick={() =>
                                updatePublishDraft("seats", seatCount)
                              }
                              aria-pressed={publishDraft.seats === seatCount}
                              key={seatCount}
                            >
                              {seatCount}
                            </button>
                          ))}
                        </div>
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

          <div
            className="sponsors-strip"
            aria-label="Patrocinadores locales"
            aria-hidden={activeView !== "home"}
          >
            <div className="sponsor-marquee">
              <div className="sponsor-track">
                {sponsorLoop.map((sponsor, index) => (
                  <a
                    className="sponsor-logo"
                    href="/"
                    key={`${sponsor.name}-${index}`}
                  >
                    {sponsor.name}
                  </a>
                ))}
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>
  );
}

export default App;
