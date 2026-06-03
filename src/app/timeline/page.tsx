'use client';

import { useStore } from '@/lib/store';
import { clsx } from 'clsx';
import { Moon } from '@/types';

const WEEKS = 13;

const PLANET_START: Record<string, number> = {
  p1: 0, p2: 0, p3: 0, p4: 2, p5: 0, p6: 0, p7: 0,
};

const PRIORITY_DUR: Record<string, number> = {
  high: 13, medium: 8, low: 4,
};

const MILESTONE_WEEKS = [4, 12];

function startOf(m: Moon) { return PLANET_START[m.planetId] ?? 0; }
function durOf(m: Moon)   { return PRIORITY_DUR[m.priority] ?? 4; }

export default function TimelinePage() {
  const { planets, moons } = useStore();

  const shown = moons.filter(
    (m) => !m.archivedFlag && (m.status === 'active' || m.priority === 'high')
  );

  const stats = [
    { label: 'Active',        value: moons.filter((m) => m.status === 'active').length },
    { label: 'Ideas queued',  value: moons.filter((m) => m.status === 'idea').length },
    { label: 'High priority', value: moons.filter((m) => m.priority === 'high').length },
    { label: 'Milestones hit',value: moons.flatMap((m) => m.milestones).filter((ms) => ms.completed).length },
  ];

  return (
    <main className="min-h-screen pt-14">
      <div className="max-w-6xl mx-auto px-6 py-8">
        <div className="mb-8">
          <h1 className="text-2xl font-semibold text-white/90 mb-1">90-Day Roadmap</h1>
          <p className="text-white/40 text-sm">Active &amp; high-priority projects across 13 weeks.</p>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-4 gap-3 mb-8">
          {stats.map((s) => (
            <div key={s.label} className="rounded-xl border border-white/[0.07] p-4 text-center">
              <div className="text-2xl font-bold text-white/85">{s.value}</div>
              <div className="text-[11px] text-white/35 mt-0.5">{s.label}</div>
            </div>
          ))}
        </div>

        {/* Legend */}
        <div className="flex gap-4 mb-4 text-[11px] text-white/40">
          <span className="flex items-center gap-1.5"><span className="w-6 h-2 rounded-full bg-emerald-500/55 inline-block" /> Active</span>
          <span className="flex items-center gap-1.5"><span className="w-6 h-2 rounded-full bg-blue-500/40 inline-block" /> High Priority</span>
          <span className="flex items-center gap-1.5 ml-2"><span className="inline-block w-px h-3.5 bg-amber-400/60 mr-1" /> 30d / 90d milestone</span>
        </div>

        {/* Gantt */}
        <div className="rounded-2xl border border-white/[0.07] overflow-hidden text-[11px]">
          {/* Week header */}
          <div className="flex border-b border-white/[0.07]">
            <div className="w-44 flex-shrink-0 px-4 py-2 text-white/30">Project</div>
            <div className="flex-1 flex">
              {Array.from({ length: WEEKS }, (_, i) => i + 1).map((w) => (
                <div
                  key={w}
                  className={clsx(
                    'flex-1 text-center py-2 border-l border-white/[0.05]',
                    MILESTONE_WEEKS.includes(w) ? 'text-amber-400/75 bg-amber-400/[0.04]' : 'text-white/22'
                  )}
                >
                  {MILESTONE_WEEKS.includes(w) ? (w === 4 ? '30d' : '90d') : `W${w}`}
                </div>
              ))}
            </div>
          </div>

          {/* Rows */}
          {planets.map((planet) => {
            const rows = shown.filter((m) => m.planetId === planet.id);
            if (!rows.length) return null;
            return (
              <div key={planet.id}>
                {/* Planet divider */}
                <div
                  className="flex border-b border-white/[0.04] bg-white/[0.012]"
                  style={{ borderLeft: `3px solid ${planet.color}` }}
                >
                  <div className="w-44 flex-shrink-0 px-3 py-1.5 text-white/45 font-medium">
                    {planet.icon} {planet.name}
                  </div>
                  <div className="flex-1" />
                </div>
                {rows.map((moon) => {
                  const start = startOf(moon);
                  const dur   = durOf(moon);
                  return (
                    <div key={moon.id} className="flex border-b border-white/[0.035] hover:bg-white/[0.012] transition-colors">
                      <div className="w-44 flex-shrink-0 px-3 py-2.5 text-white/55 truncate pl-6">{moon.name}</div>
                      <div className="flex-1 flex py-2 px-0.5">
                        {Array.from({ length: WEEKS }, (_, wi) => {
                          const inBar = wi >= start && wi < start + dur;
                          const isStart = wi === start;
                          const isEnd   = wi === start + dur - 1;
                          const isMile  = MILESTONE_WEEKS.includes(wi + 1);
                          return (
                            <div key={wi} className={clsx('flex-1 h-5 relative', isMile && 'bg-amber-400/[0.035]')}>
                              {inBar && (
                                <div
                                  className={clsx('absolute inset-y-0.5 inset-x-0',
                                    isStart && 'rounded-l-full', isEnd && 'rounded-r-full')}
                                  style={{
                                    backgroundColor: planet.color,
                                    opacity: moon.status === 'active' ? 0.55 : 0.3,
                                  }}
                                />
                              )}
                              {isMile && (
                                <div className="absolute top-0 bottom-0 left-1/2 w-px bg-amber-400/40" />
                              )}
                            </div>
                          );
                        })}
                      </div>
                    </div>
                  );
                })}
              </div>
            );
          })}
        </div>
      </div>
    </main>
  );
}
