'use client';

import ConstellationMap from '@/components/ConstellationMap';
import SidePanel from '@/components/SidePanel';
import { useStore } from '@/lib/store';

export default function ConstellationPage() {
  const { moons } = useStore();
  const active = moons.filter((m) => m.status === 'active').length;
  const ideas  = moons.filter((m) => m.status === 'idea').length;

  return (
    <main className="min-h-screen pt-14">
      <div className="max-w-7xl mx-auto px-6 py-8">
        {/* Header */}
        <div className="mb-6">
          <h1 className="text-2xl font-semibold text-white/90 mb-1">Your Constellation</h1>
          <p className="text-white/40 text-sm">
            {active} active · {ideas} ideas · 7 planets · click anything to explore
          </p>
        </div>

        <div className="flex gap-6 items-start">
          <div className="flex-1">
            <ConstellationMap />
          </div>
          <SidePanel />
        </div>

        {/* Legend */}
        <div className="mt-6 flex gap-5 text-[12px] text-white/35">
          {[
            { color: '#34d399', label: 'Active' },
            { color: '#60a5fa', label: 'Idea' },
            { color: '#fbbf24', label: 'Paused' },
            { color: '#a78bfa', label: 'Completed' },
          ].map(({ color, label }) => (
            <span key={label} className="flex items-center gap-1.5">
              <span className="w-2.5 h-2.5 rounded-full inline-block" style={{ backgroundColor: color }} />
              {label}
            </span>
          ))}
        </div>
      </div>
    </main>
  );
}
