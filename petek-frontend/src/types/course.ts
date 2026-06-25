export type DayOfWeek =
  | 'MONDAY'
  | 'TUESDAY'
  | 'WEDNESDAY'
  | 'THURSDAY'
  | 'FRIDAY'
  | 'SATURDAY'
  | 'SUNDAY';

export interface TimeSlotDTO {
  id: number;
  dayOfWeek: DayOfWeek;
  startTimeString: string;
  endTimeString: string;
  startMinute: number;
  endMinute: number;
  building: string | null;
  room: string | null;
}

export interface CourseDTO {
  crn: number;
  courseCode: string;
  title: string;
  instructor: string | null;
  capacity: number | null;
  enrolled: number | null;
  availableQuota: number | null;
  majorRestrictions: string | null;
  timeSlots: TimeSlotDTO[];
}
