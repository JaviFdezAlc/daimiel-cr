type PublishTimeStepProps = {
  time: string
  onTimeChange: (time: string) => void
}

export function PublishTimeStep({
  time,
  onTimeChange,
}: PublishTimeStepProps) {
  return (
    <div className="publish-time-step">
      <label>
        <span>Hora de salida</span>
        <input
          type="time"
          value={time}
          step="900"
          onChange={(event) => onTimeChange(event.target.value)}
        />
      </label>
    </div>
  )
}