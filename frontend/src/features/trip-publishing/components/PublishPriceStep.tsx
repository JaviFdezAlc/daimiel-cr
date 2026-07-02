import { publishPriceOptions } from '../model/publish-trip-draft'

type PublishPriceStepProps = {
  price: string
  onPriceChange: (price: string) => void
}

export function PublishPriceStep({
  price,
  onPriceChange,
}: PublishPriceStepProps) {
  return (
    <div className="publish-price-step">
      <div className="publish-choice-grid">
        {publishPriceOptions.map((priceOption) => (
          <button
            className={
              price === priceOption ? 'is-selected' : undefined
            }
            type="button"
            onClick={() => onPriceChange(priceOption)}
            aria-pressed={price === priceOption}
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
          value={price}
          onChange={(event) =>
            onPriceChange(event.target.value)
          }
        />
      </label>
    </div>
  )
}