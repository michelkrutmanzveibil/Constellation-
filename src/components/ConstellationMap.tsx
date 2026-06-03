'use client';

import { useState } from 'react';
import { useStore } from '@/lib/store';
import { Moon } from '@/types';

const CX = 450, CY = 450;
const PLANET_R = 260;
const MOON_R_ORBIT = 72;
const P_SIZE = 22;
const M_SIZE = 9;

// Deterministic star positions
const STARS = Array.from({ length: 90 }, (_, i) => ({
  id: i,
  cx: ((i * 7919 + 123) % 900),
  cy: ((i * 6271 + 456) % 900),
  r:  0.2 + (i % 5) * 0.2,
  opacity: 0.06 + (i % 8) * 0.045,
}));

const STATUS_OPACITY: Record<string, number> = {
  active: 1, idea: 0.4, paused: 0.55, completed: 0.7,
};

function pPos(order: number, total: number) {
  const a = (order / total) * 2 * Math.PI - Math.PI / 2;
  return { x: CX + PLANET_R * Math.cos(a), y: CY + PLANET_R * Math.sin(a) };
}

function mPos(px: number, py: number, idx: number, total: number) {
  const a = (idx / Math.max(total, 1)) * 2 * Math.PI - Math.PI / 2;
  return { x: px + MOON_R_ORBIT * Math.cos(a), y: py + MOON_R_ORBIT * Math.sin(a) };
}

export default function ConstellationMap() {
  const { planets, moons, selectedMoonId, selectedPlanetId, selectMoon, selectPlanet } = useStore();
  const [hoverMoon, setHoverMoon] = useState<string | null>(null);
  const [hoverPlanet, setHoverPlanet] = useState<string | null>(null);

  return (
    <svg
      viewBox="0 0 900 900"
      className="w-full max-w-[680px] aspect-square"
      style={{ overflow: 'visible' }}
    >
      <defs>
        <radialGradient id="bgG" cx="50%" cy="50%" r="50%">
          <stop offset="0%" stopColor="#0f1f40" />
          <stop offset="100%" stopColor="#020818" />
        </radialGradient>
        <filter id="glow" x="-50%" y="-50%" width="200%" height="200%">
          <feGaussianBlur stdDeviation="4" result="blur" />
          <feMerge><feMergeNode in="blur" /><feMergeNode in="SourceGraphic" /></feMerge>
        </filter>
        <filter id="glow2" x="-100%" y="-100%" width="300%" height="300%">
          <feGaussianBlur stdDeviation="9" result="blur" />
          <feMerge><feMergeNode in="blur" /><feMergeNode in="SourceGraphic" /></feMerge>
        </filter>
      </defs>

      {/* Background */}
      <rect width="900" height="900" rx="20" fill="url(#bgG)" />

      {/* Stars */}
      {STARS.map((s) => (
        <circle key={s.id} cx={s.cx} cy={s.cy} r={s.r} fill="white" opacity={s.opacity} />
      ))}

      {/* Planet orbit ring */}
      <circle cx={CX} cy={CY} r={PLANET_R} fill="none" stroke="rgba(255,255,255,0.035)" strokeWidth="1" strokeDasharray="4 7" />

      {/* Spoke lines from center */}
      {planets.map((p, i) => {
        const pos = pPos(i, planets.length);
        return (
          <line key={`spoke-${p.id}`}
            x1={CX} y1={CY} x2={pos.x} y2={pos.y}
            stroke={p.color} strokeWidth="0.6" strokeOpacity="0.12"
          />
        );
      })}

      {/* Moon orbit rings */}
      {planets.map((p, i) => {
        const pos = pPos(i, planets.length);
        return (
          <circle key={`morb-${p.id}`}
            cx={pos.x} cy={pos.y} r={MOON_R_ORBIT}
            fill="none" stroke={p.color} strokeOpacity="0.1" strokeWidth="0.8" strokeDasharray="3 5"
          />
        );
      })}

      {/* Moons */}
      {planets.map((p, pi) => {
        const ppos = pPos(pi, planets.length);
        const planetMoons = moons.filter((m) => m.planetId === p.id && !m.archivedFlag);
        return planetMoons.map((moon, mi) => {
          const mp = mPos(ppos.x, ppos.y, mi, planetMoons.length);
          const sel = moon.id === selectedMoonId;
          const hov = moon.id === hoverMoon;
          const opacity = STATUS_OPACITY[moon.status] ?? 1;
          return (
            <g key={moon.id}>
              {(sel || hov) && (
                <circle cx={mp.x} cy={mp.y} r={M_SIZE + 5}
                  fill={p.color} fillOpacity="0.2" filter="url(#glow)" />
              )}
              <circle
                cx={mp.x} cy={mp.y}
                r={sel ? M_SIZE + 2 : M_SIZE}
                fill={p.color}
                fillOpacity={opacity}
                stroke={sel ? p.color : 'none'}
                strokeWidth="1.5" strokeOpacity="0.8"
                style={{ cursor: 'pointer', transition: 'r 0.12s' }}
                onClick={() => selectMoon(moon.id === selectedMoonId ? null : moon.id)}
                onMouseEnter={() => setHoverMoon(moon.id)}
                onMouseLeave={() => setHoverMoon(null)}
              />
              {hov && (
                <text x={mp.x} y={mp.y - M_SIZE - 7}
                  textAnchor="middle" fill="white" fontSize="9" opacity="0.9"
                  style={{ pointerEvents: 'none' }}
                >
                  {moon.name}
                </text>
              )}
            </g>
          );
        });
      })}

      {/* Planets */}
      {planets.map((p, i) => {
        const pos = pPos(i, planets.length);
        const sel = p.id === selectedPlanetId;
        const hov = p.id === hoverPlanet;
        const activeCt = moons.filter((m) => m.planetId === p.id && m.status === 'active').length;
        return (
          <g key={p.id}>
            {(sel || hov) && (
              <circle cx={pos.x} cy={pos.y} r={P_SIZE + 10}
                fill={p.color} fillOpacity="0.15" filter="url(#glow)" />
            )}
            <circle
              cx={pos.x} cy={pos.y} r={P_SIZE}
              fill={p.color} fillOpacity="0.88" filter="url(#glow)"
              style={{ cursor: 'pointer', transition: 'opacity 0.12s' }}
              onClick={() => selectPlanet(p.id === selectedPlanetId ? null : p.id)}
              onMouseEnter={() => setHoverPlanet(p.id)}
              onMouseLeave={() => setHoverPlanet(null)}
            />
            <text x={pos.x} y={pos.y + 5} textAnchor="middle"
              fill="white" fontSize="11" fontWeight="700"
              style={{ pointerEvents: 'none' }}
            >
              {activeCt > 0 ? activeCt : p.icon}
            </text>
            <text x={pos.x} y={pos.y + P_SIZE + 15} textAnchor="middle"
              fill="white" fillOpacity="0.65" fontSize="9.5"
              style={{ pointerEvents: 'none' }}
            >
              {p.name}
            </text>
          </g>
        );
      })}

      {/* Center node */}
      <circle cx={CX} cy={CY} r={44} fill="rgba(251,191,36,0.08)" filter="url(#glow2)" />
      <circle cx={CX} cy={CY} r={34}
        fill="#0f0c00" stroke="#fbbf24" strokeWidth="1.8" filter="url(#glow)"
      />
      <text x={CX} y={CY - 5} textAnchor="middle" fill="#fbbf24" fontSize="10" fontWeight="800">
        YOU
      </text>
      <text x={CX} y={CY + 9} textAnchor="middle" fill="#fbbf24" fontSize="7.5" opacity="0.6">
        10 beliefs
      </text>
    </svg>
  );
}
