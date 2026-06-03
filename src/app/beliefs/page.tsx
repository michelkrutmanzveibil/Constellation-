'use client';

import { useStore } from '@/lib/store';

export default function BeliefsPage() {
  const { beliefs, moons } = useStore();

  return (
    <main className="min-h-screen pt-14">
      <div className="max-w-4xl mx-auto px-6 py-8">
        <div className="mb-8">
          <h1 className="text-2xl font-semibold text-white/90 mb-1">Core Beliefs</h1>
          <p className="text-white/40 text-sm">
            10 guiding principles at the center of the constellation — the lenses through which all projects are filtered.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {beliefs.map((b, i) => {
            const tagged = moons.filter((m) => m.beliefIds.includes(b.id) && !m.archivedFlag);
            return (
              <div
                key={b.id}
                className="rounded-2xl border p-5 transition-colors hover:bg-white/[0.02]"
                style={{ borderColor: `${b.color}22`, backgroundColor: `${b.color}07` }}
              >
                <div className="flex items-start gap-3 mb-3">
                  <span
                    className="w-6 h-6 rounded-full flex-shrink-0 flex items-center justify-center text-[10px] font-bold mt-0.5"
                    style={{ backgroundColor: `${b.color}20`, color: b.color }}
                  >
                    {i + 1}
                  </span>
                  <div>
                    <div className="text-sm font-semibold text-white/85 mb-0.5">{b.shortName}</div>
                    <div className="text-[12px] italic" style={{ color: b.color }}>&#8220;{b.quote}&#8221;</div>
                  </div>
                </div>
                <p className="text-[12px] text-white/48 leading-relaxed mb-3">{b.description}</p>
                {tagged.length > 0 && (
                  <div className="flex flex-wrap gap-1.5">
                    {tagged.map((m) => (
                      <span key={m.id}
                        className="text-[10px] px-2 py-0.5 rounded-full border border-white/10 text-white/38"
                      >
                        {m.name}
                      </span>
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
