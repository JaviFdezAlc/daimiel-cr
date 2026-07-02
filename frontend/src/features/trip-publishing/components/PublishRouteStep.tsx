type PublishRouteStepProps = {
  origin: string
  destination: string
  onOriginChange: (origin: string) => void
  onDestinationChange: (destination: string) => void
  onSwapRoute: () => void
}

const SwitchIcon = () => (
  <svg aria-hidden="true" viewBox="0 0 24 24" focusable="false">
    <path d="M7 7h10m0 0-3-3m3 3-3 3" />
    <path d="M17 17H7m0 0 3 3m-3-3 3-3" />
  </svg>
)

export function PublishRouteStep({
  origin,
  destination,
  onOriginChange,
  onDestinationChange,
  onSwapRoute,
}: PublishRouteStepProps) {
  return (
    <div className="publish-route-step">
      <label>
        <span>Origen</span>
        <input
          value={origin}
          onChange={(event) => onOriginChange(event.target.value)}
        />
      </label>

      <button
        className="publish-swap"
        type="button"
        onClick={onSwapRoute}
        aria-label="Invertir origen y destino"
      >
        <SwitchIcon />
      </button>

      <label>
        <span>Destino</span>
        <input
          value={destination}
          onChange={(event) => onDestinationChange(event.target.value)}
        />
      </label>
    </div>
  )
}
