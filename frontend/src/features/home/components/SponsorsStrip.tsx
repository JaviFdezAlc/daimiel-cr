import { sponsorLoop } from '../mocks/sponsors'

type SponsorsStripProps = {
  isVisible: boolean
}

export function SponsorsStrip({
  isVisible,
}: SponsorsStripProps) {
  return (
    <div
      className="sponsors-strip"
      aria-label="Patrocinadores locales"
      aria-hidden={!isVisible}
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
  )
}