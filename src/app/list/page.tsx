'use client';

import { useState } from 'react';
import { useStore } from '@/lib/store';
import StatusBadge from '@/components/StatusBadge';
import PriorityBadge from '@/components/PriorityBadge';
import { Status } from '@/types';
import { clsx } from 'clsx';

const FILTERS: Array<Status | 'all'> = ['all', 'active', 'idea', 'paused', 'completed'];

export default function ListPage() {
  const { planets, moons, selectMoon } = useStore();
  const [filter, setFilter] = useState<Status | 'all'>('all');
  const [collapsed, setCollapsed] = useState<Set<string>>(new Set());

  const toggle = (id: string) =>
    setCollapsed((s) => { const n = new Set(s); n.has(id) ? n.delete(id) : n.add(id); return n; });

  const filtered = (pid: string) =>
    moons.filter((m) => m.planetId === pid && !m.archivedFlag && (filter === 'all' || m.status === filter));

  const total = moons.filter((m) => !m.archivedFlag && (filter === 'all' || m.status === filter)).length;

  return (
    <main className="min-h-screen pt-14">
      <div className="max-w-4xl mx-auto px-6 py-8">
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-2xl font-semibold text-white/90 mb-1">All Projects</h1>
            <p className="text-white/40 text-sm">{total} projects</p>
          </div>
          <div className="flex gap-1">
            {FILTERS.map((f) => (
              <button
                key={f}
                onClick={() => setFilter(f)}
                className={clsx(
                  'px-3 py-1 rounded-full text-xs capitalize transition-all',
                  filter === f
                    ? 'bg-white/10 text-white border border-white/20'
                    : 'text-white/40 hover:text-white/65 border border-transparent hover:border-white/10'
                )}
              >
                {f}
              </button>
            ))}
          </div>
        </div>

        <div className="space-y-3">
          {planets.map((planet) => {
            const pm = filtered(planet.id);
            if (!pm.length) return null;
            const open = !collapsed.has(planet.id);
            return (
              <div key={planet.id} className="rounded-2xl border border-white/[0.07] overflow-hidden"
                style={{ borderTopWidth: '2px', borderTopColor: planet.color }}
              >
                <button
                  onClick={() => toggle(planet.id)}
                  className="w-full flex items-center justify-between px-5 py-3.5 hover:bg-white/[0.02] transition-colors"
                >
                  <div className="flex items-center gap-3">
                    <span className="text-xl">{planet.icon}</span>
                    <div className="text-left">
                      <div className="text-sm font-medium text-white/80">{planet.name}</div>
                      <div className="text-[11px] text-white/35">{pm.length} project{pm.length !== 1 ? 's' : ''}</div>
                    </div>
                  </div>
                  <span className="text-white/25 text-xs">{open ? '▲' : '▼'}</span>
                </button>

                {open && (
                  <div className="divide-y divide-white/[0.04]">
                    {pm.map((moon) => (
                      <button
                        key={moon.id}
                        onClick={() => selectMoon(moon.id)}
                        className="w-full text-left px-5 py-3 flex items-center gap-4 hover:bg-white/[0.025] transition-colors"
                      >
                        <div className="flex-1 min-w-0">
                          <div className="text-[13px] font-medium text-white/75 truncate">{moon.name}</div>
                          <div className="text-[11px] text-white/35 mt-0.5 truncate">{moon.description}</div>
                        </div>
                        <div className="flex items-center gap-2.5 flex-shrink-0">
                          <PriorityBadge priority={moon.priority} />
                          <StatusBadge status={moon.status} />
                          {/* milestone dots */}
                          <div className="flex gap-1">
                            {moon.milestones.map((ms) => (
                              <div key={ms.label}
                                className={clsx('w-1.5 h-1.5 rounded-full', ms.completed ? 'bg-emerald-400' : 'bg-white/12')}
                                title={`${ms.label}: ${ms.description}`}
                              />
                            ))}
                          </div>
                        </div>
                      </button>
                    ))}
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </div>
    </main>
  );
}
