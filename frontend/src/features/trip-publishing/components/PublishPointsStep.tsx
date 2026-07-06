type PublishPointsStepProps = {
  departurePoint: string
  arrivalPoint: string
  onDeparturePointChange: (
    departurePoint: string,
  ) => void
  onArrivalPointChange: (
    arrivalPoint: string,
  ) => void
}

export function PublishPointsStep({
  departurePoint,
  arrivalPoint,
  onDeparturePointChange,
  onArrivalPointChange,
}: PublishPointsStepProps) {
  return (
    <div className="publish-points-step">
      <label>
        <span>Punto de salida</span>
        <input
          value={departurePoint}
          maxLength={120}
          placeholder="Ej. Estacion de Daimiel"
          onChange={(event) =>
            onDeparturePointChange(event.target.value)
          }
        />
      </label>

      <label>
        <span>Punto de llegada</span>
        <input
          value={arrivalPoint}
          maxLength={120}
          placeholder="Ej. Campus Universitario"
          onChange={(event) =>
            onArrivalPointChange(event.target.value)
          }
        />
      </label>
    </div>
  )
}
