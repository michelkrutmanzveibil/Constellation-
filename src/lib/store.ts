import { create } from 'zustand';
import { Planet, Moon, Belief, Insight, Status, Priority } from '@/types';
import { PLANETS, MOONS, BELIEFS } from '@/data/seeds';

interface Store {
  planets: Planet[];
  moons: Moon[];
  beliefs: Belief[];
  insights: Insight[];
  selectedMoonId: string | null;
  selectedPlanetId: string | null;

  selectMoon: (id: string | null) => void;
  selectPlanet: (id: string | null) => void;
  updateMoonStatus: (id: string, status: Status) => void;
  updateMoonPriority: (id: string, priority: Priority) => void;
  toggleMilestone: (moonId: string, label: string) => void;
  addMoon: (moon: Moon) => void;
  addInsight: (insight: Insight) => void;
}

export const useStore = create<Store>((set) => ({
  planets: PLANETS,
  moons: MOONS,
  beliefs: BELIEFS,
  insights: [],
  selectedMoonId: null,
  selectedPlanetId: null,

  selectMoon: (id) => set({ selectedMoonId: id, selectedPlanetId: null }),
  selectPlanet: (id) => set({ selectedPlanetId: id, selectedMoonId: null }),

  updateMoonStatus: (id, status) =>
    set((s) => ({ moons: s.moons.map((m) => (m.id === id ? { ...m, status } : m)) })),

  updateMoonPriority: (id, priority) =>
    set((s) => ({ moons: s.moons.map((m) => (m.id === id ? { ...m, priority } : m)) })),

  toggleMilestone: (moonId, label) =>
    set((s) => ({
      moons: s.moons.map((m) =>
        m.id === moonId
          ? { ...m, milestones: m.milestones.map((ms) => ms.label === label ? { ...ms, completed: !ms.completed } : ms) }
          : m
      ),
    })),

  addMoon: (moon) => set((s) => ({ moons: [...s.moons, moon] })),

  addInsight: (insight) => set((s) => ({ insights: [...s.insights, insight] })),
}));
