'use client';

import { useStore } from '@/lib/store';
import StatusBadge from './StatusBadge';
import PriorityBadge from './PriorityBadge';
import { clsx } from 'clsx';

export default function SidePanel() {
  const { moons, planets, beliefs, selectedMoonId, selectedPlanetId, selectMoon, selectPlanet, toggleMilestone } = useStore();

  const moon   = moons.find((m) => m.id === selectedMoonId);
  const planet = moon
    ? planets.find((p) => p.id === moon.planetId)
    : planets.find((p) => p.id === selectedPlanetId);
  const isOpen = !!(moon || planet);

  if (!isOpen) return null;

  const close = () => { selectMoon(null); selectPlanet(null); };

  const moonBeliefs  = moon ? beliefs.filter((b) => moon.beliefIds.includes(b.id)) : [];
  const planetMoons  = !moon && planet ? moons.filter((m) => m.planetId === planet.id && !m.archivedFlag) : [];

  return (
    <div
      className="w-80 flex-shrink-0 rounded-2xl border border-white/[0.08] bg-white/[0.025] overflow-hidden"
      style={{ borderTop: `2px solid ${planet?.color ?? '#818cf8'}` }}
    >
      {/* Header */}
      <div className="px-5 py-4 border-b border-white/[0.06] flex items-start justify-between">
        <div>
          <div className="text-[11px] text-white/35 mb-0.5">{moon ? planet?.name : 'Planet'}</div>
          <div className="text-sm font-semibold text-white/90 leading-snug">
            {moon?.name ?? planet?.name}
          </div>
        </div>
        <button onClick={close} className="text-white/25 hover:text-white/60 transition-colors mt-0.5 text-lg leading-none">×</button>
      </div>

      <div className="p-5 space-y-5 overflow-y-auto max-h-[calc(100vh-180px)]">
        {moon && (
          <>
            <div className="flex items-center gap-2.5">
              <StatusBadge status={moon.status} />
              <PriorityBadge priority={moon.priority} />
            </div>

            <p className="text-white/55 text-[13px] leading-relaxed">{moon.description}</p>

            {/* Milestones */}
            <div>
              <Label>Milestones</Label>
              <div className="space-y-2 mt-2">
                {moon.milestones.map((ms) => (
                  <button
                    key={ms.label}
                    onClick={() => toggleMilestone(moon.id, ms.label)}
                    className="w-full flex items-start gap-2.5 text-left group"
                  >
                    <span className={clsx(
                      'mt-0.5 flex-shrink-0 w-3.5 h-3.5 rounded-full border flex items-center justify-center text-[8px]',
                      ms.completed
                        ? 'bg-emerald-400 border-emerald-400 text-white'
                        : 'border-white/25 group-hover:border-white/50'
                    )}>
                      {ms.completed && '✓'}
                    </span>
                    <span className={clsx(
                      'text-[12px] leading-relaxed',
                      ms.completed ? 'text-white/30 line-through' : 'text-white/60'
                    )}>
                      <span className="font-medium text-white/40">{ms.label}:</span> {ms.description}
                    </span>
                  </button>
                ))}
              </div>
            </div>

            {/* Outputs */}
            <div>
              <Label>Expected Outputs</Label>
              <ul className="mt-2 space-y-1">
                {moon.outputs.map((o, i) => (
                  <li key={i} className="text-[12px] text-white/50 flex gap-2">
                    <span className="text-white/25 flex-shrink-0">▸</span>{o}
                  </li>
                ))}
              </ul>
            </div>

            {/* Metrics */}
            <div>
              <Label>Metrics</Label>
              <div className="mt-2 flex flex-wrap gap-1.5">
                {moon.metrics.map((m, i) => (
                  <span key={i} className="text-[11px] px-2 py-0.5 rounded bg-white/[0.04] text-white/45 border border-white/[0.07]">
                    {m}
                  </span>
                ))}
              </div>
            </div>

            {/* Beliefs */}
            {moonBeliefs.length > 0 && (
              <div>
                <Label>Core Beliefs</Label>
                <div className="mt-2 space-y-1.5">
                  {moonBeliefs.map((b) => (
                    <div key={b.id} className="text-[12px] px-2.5 py-1.5 rounded-lg border"
                      style={{ borderColor: `${b.color}28`, backgroundColor: `${b.color}0c`, color: b.color }}
                    >
                      {b.shortName}
                    </div>
                  ))}
                </div>
              </div>
            )}
          </>
        )}

        {!moon && planet && (
          <>
            <p className="text-white/50 text-[13px]">{planet.description}</p>
            <div>
              <Label>Projects ({planetMoons.length})</Label>
              <div className="mt-2 space-y-2">
                {planetMoons.map((m) => (
                  <button
                    key={m.id}
                    onClick={() => selectMoon(m.id)}
                    className="w-full text-left px-3 py-2.5 rounded-xl bg-white/[0.03] hover:bg-white/[0.06] border border-white/[0.06] transition-all"
                  >
                    <div className="flex items-center justify-between gap-2">
                      <span className="text-[12px] text-white/75 font-medium truncate">{m.name}</span>
                      <StatusBadge status={m.status} />
                    </div>
                  </button>
                ))}
              </div>
            </div>
          </>
        )}
      </div>
    </div>
  );
}

function Label({ children }: { children: React.ReactNode }) {
  return <div className="text-[11px] font-semibold text-white/35 uppercase tracking-wider">{children}</div>;
}
