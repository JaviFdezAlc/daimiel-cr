type PublishSeatsStepProps = {
  seats: number
  onSeatsChange: (seats: number) => void
}

const seatOptions = [1, 2, 3, 4]

export function PublishSeatsStep({
  seats,
  onSeatsChange,
}: PublishSeatsStepProps) {
  return (
    <div className="publish-seat-grid">
      {seatOptions.map((seatCount) => (
        <button
          className={
            seats === seatCount ? 'is-selected' : undefined
          }
          type="button"
          onClick={() => onSeatsChange(seatCount)}
          aria-pressed={seats === seatCount}
          key={seatCount}
        >
          {seatCount}
        </button>
      ))}
    </div>
  )
}