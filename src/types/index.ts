export type Status = 'active' | 'idea' | 'paused' | 'completed';
export type Priority = 'high' | 'medium' | 'low';

export interface Milestone {
  label: '30d' | '90d' | '365d';
  description: string;
  completed: boolean;
}

export interface Planet {
  id: string;
  name: string;
  color: string;
  icon: string;
  description: string;
  order: number;
}

export interface Moon {
  id: string;
  planetId: string;
  name: string;
  description: string;
  status: Status;
  priority: Priority;
  outputs: string[];
  milestones: Milestone[];
  metrics: string[];
  beliefIds: string[];
  createdDate: string;
  archivedFlag: boolean;
}

export interface Belief {
  id: string;
  shortName: string;
  quote: string;
  description: string;
  color: string;
}

export interface Insight {
  id: string;
  content: string;
  dateCaptured: string;
  tags: string[];
  linkedMoonIds: string[];
}
