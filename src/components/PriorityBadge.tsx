import { clsx } from 'clsx';
import { Priority } from '@/types';

const CFG: Record<Priority, { label: string; cls: string }> = {
  high:   { label: '↑ High',   cls: 'text-rose-400' },
  medium: { label: '→ Med',    cls: 'text-amber-400' },
  low:    { label: '↓ Low',    cls: 'text-slate-500' },
};

export default function PriorityBadge({ priority }: { priority: Priority }) {
  const { label, cls } = CFG[priority];
  return <span className={clsx('text-[11px] font-medium', cls)}>{label}</span>;
}
