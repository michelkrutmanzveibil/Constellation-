'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { clsx } from 'clsx';

const LINKS = [
  { href: '/',          label: 'Constellation' },
  { href: '/list',      label: 'Projects' },
  { href: '/beliefs',   label: 'Beliefs' },
  { href: '/timeline',  label: 'Timeline' },
];

export default function Nav() {
  const pathname = usePathname();
  return (
    <nav className="fixed top-0 inset-x-0 z-50 h-14 border-b border-white/[0.06] bg-[#020818]/80 backdrop-blur-md flex items-center">
      <div className="max-w-7xl mx-auto w-full px-6 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <span className="text-amber-400 text-lg">✦</span>
          <span className="text-sm font-semibold tracking-widest text-white/70 uppercase">Constellation</span>
        </div>
        <div className="flex gap-1">
          {LINKS.map((l) => (
            <Link
              key={l.href}
              href={l.href}
              className={clsx(
                'px-4 py-1.5 rounded-full text-sm transition-all duration-150',
                pathname === l.href
                  ? 'bg-indigo-500/20 text-indigo-300 border border-indigo-500/30'
                  : 'text-white/45 hover:text-white/75 hover:bg-white/5'
              )}
            >
              {l.label}
            </Link>
          ))}
        </div>
      </div>
    </nav>
  );
}
