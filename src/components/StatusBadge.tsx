import { clsx } from 'clsx';
import { Status } from '@/types';

const CFG: Record<Status, { label: string; cls: string }> = {
  active:    { label: 'Active',    cls: 'bg-emerald-500/15 text-emerald-300 border-emerald-500/25' },
  idea:      { label: 'Idea',      cls: 'bg-blue-500/15 text-blue-300 border-blue-500/25' },
  paused:    { label: 'Paused',    cls: 'bg-amber-500/15 text-amber-300 border-amber-500/25' },
  completed: { label: 'Completed', cls: 'bg-purple-500/15 text-purple-300 border-purple-500/25' },
};

export default function StatusBadge({ status }: { status: Status }) {
  const { label, cls } = CFG[status];
  return (
    <span className={clsx('inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[11px] border', cls)}>
      <span className="w-1.5 h-1.5 rounded-full bg-current opacity-75" />
      {label}
    </span>
  );
}
