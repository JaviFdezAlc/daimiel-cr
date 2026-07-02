import {
  type PointerEvent,
  useRef,
  useState,
} from 'react'

import { HomeHero } from '../features/home/components/HomeHero'
import { SponsorsStrip } from '../features/home/components/SponsorsStrip'
import { PublishTripWizard } from '../features/trip-publishing/components/PublishTripWizard'
import { TripSearchView } from '../features/trip-search/components/TripSearchView'
import { SiteHeader } from './layout/SiteHeader'
import type { AppView } from './navigation/app-view'

const publishToday = new Date(2026, 5, 25);

function App() {
  const [activeView, setActiveView] = useState<AppView>("home");
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  const heroFrameRef = useRef<HTMLDivElement>(null);
  const parallaxFrameRef = useRef<number | null>(null);

  const navigateTo = (view: AppView) => {
    setActiveView(view);
    setIsMobileMenuOpen(false);
  };

  const showSearchView = () => navigateTo("search");

  const showPublishView = () => navigateTo("publish");

  const showHomeView = () => navigateTo("home");

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

          <TripSearchView isVisible={activeView === "search"} />

          <PublishTripWizard
            isVisible={activeView === "publish"}
            today={publishToday}
            onReturnHome={showHomeView}
          />

          <SponsorsStrip isVisible={activeView === "home"} />
        </div>
      </section>
    </main>
  );
}

export default App;
