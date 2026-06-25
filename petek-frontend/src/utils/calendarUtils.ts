import type { CourseDTO, DayOfWeek, TimeSlotDTO } from '../types/course';

/** Minutes from midnight for the first row (08:30). */
export const GRID_START_MINUTE_OF_DAY = 8 * 60 + 30;

/** Minutes from midnight for the last row label (17:30). */
export const GRID_END_MINUTE_OF_DAY = 17 * 60 + 30;

export const SLOT_DURATION_MINUTES = 30;

export const WEEKDAYS: { key: DayOfWeek; label: string }[] = [
  { key: 'MONDAY', label: 'Pazartesi' },
  { key: 'TUESDAY', label: 'Salı' },
  { key: 'WEDNESDAY', label: 'Çarşamba' },
  { key: 'THURSDAY', label: 'Perşembe' },
  { key: 'FRIDAY', label: 'Cuma' },
];

const DAY_COLUMN: Partial<Record<DayOfWeek, number>> = {
  MONDAY: 2,
  TUESDAY: 3,
  WEDNESDAY: 4,
  THURSDAY: 5,
  FRIDAY: 6,
};

const BLOCK_COLORS = [
  'bg-blue-100 border-blue-500 text-blue-900',
  'bg-emerald-100 border-emerald-500 text-emerald-900',
  'bg-violet-100 border-violet-500 text-violet-900',
  'bg-amber-100 border-amber-500 text-amber-900',
  'bg-rose-100 border-rose-500 text-rose-900',
  'bg-cyan-100 border-cyan-500 text-cyan-900',
];

export interface CalendarBlock {
  key: string;
  course: CourseDTO;
  slot: TimeSlotDTO;
  gridColumn: number;
  gridRowStart: number;
  gridRowEnd: number;
  colorClass: string;
}

export function generateTimeLabels(): string[] {
  const labels: string[] = [];
  for (
    let minute = GRID_START_MINUTE_OF_DAY;
    minute <= GRID_END_MINUTE_OF_DAY;
    minute += SLOT_DURATION_MINUTES
  ) {
    const hours = Math.floor(minute / 60);
    const mins = minute % 60;
    labels.push(`${String(hours).padStart(2, '0')}:${String(mins).padStart(2, '0')}`);
  }
  return labels;
}

export function minuteOfDay(absoluteMinute: number): number {
  return absoluteMinute % (24 * 60);
}

export function getGridRowStart(startMinute: number): number | null {
  const startOfDay = minuteOfDay(startMinute);
  if (startOfDay < GRID_START_MINUTE_OF_DAY || startOfDay > GRID_END_MINUTE_OF_DAY) {
    return null;
  }
  return Math.floor((startOfDay - GRID_START_MINUTE_OF_DAY) / SLOT_DURATION_MINUTES) + 2;
}

export function getGridRowEnd(endMinute: number): number | null {
  const endOfDay = minuteOfDay(endMinute);
  const clampedEnd = Math.min(Math.max(endOfDay, GRID_START_MINUTE_OF_DAY), GRID_END_MINUTE_OF_DAY + SLOT_DURATION_MINUTES);
  return Math.ceil((clampedEnd - GRID_START_MINUTE_OF_DAY) / SLOT_DURATION_MINUTES) + 2;
}

export function getGridColumn(dayOfWeek: DayOfWeek): number | null {
  return DAY_COLUMN[dayOfWeek] ?? null;
}

export function buildCalendarBlocks(courses: CourseDTO[]): CalendarBlock[] {
  const blocks: CalendarBlock[] = [];

  courses.forEach((course, courseIndex) => {
    course.timeSlots.forEach((slot) => {
      const gridColumn = getGridColumn(slot.dayOfWeek);
      const gridRowStart = getGridRowStart(slot.startMinute);
      const gridRowEnd = getGridRowEnd(slot.endMinute);

      if (gridColumn == null || gridRowStart == null || gridRowEnd == null || gridRowEnd <= gridRowStart) {
        return;
      }

      blocks.push({
        key: `${course.crn}-${slot.id ?? slot.startMinute}`,
        course,
        slot,
        gridColumn,
        gridRowStart,
        gridRowEnd,
        colorClass: BLOCK_COLORS[courseIndex % BLOCK_COLORS.length],
      });
    });
  });

  return blocks;
}
