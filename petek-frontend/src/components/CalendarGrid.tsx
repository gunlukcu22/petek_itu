import { useMemo } from 'react';
import type { CourseDTO } from '../types/course';
import {
  buildCalendarBlocks,
  generateTimeLabels,
  WEEKDAYS,
} from '../utils/calendarUtils';

interface CalendarGridProps {
  courses: CourseDTO[];
}

export default function CalendarGrid({ courses }: CalendarGridProps) {
  const timeLabels = useMemo(() => generateTimeLabels(), []);
  const blocks = useMemo(() => buildCalendarBlocks(courses), [courses]);

  return (
    <section className="flex min-w-0 flex-1 flex-col rounded-lg border border-gray-200 bg-white shadow-sm">
      <div className="border-b border-gray-200 px-4 py-3">
        <h2 className="text-lg font-semibold text-gray-900">Haftalık Program</h2>
        <p className="text-sm text-gray-500">
          {courses.length} ders · {blocks.length} zaman dilimi
        </p>
      </div>

      <div className="overflow-x-auto p-4">
        <div
          className="relative grid min-w-[720px] border border-gray-200 bg-gray-50"
          style={{
            gridTemplateColumns: '4.5rem repeat(5, minmax(0, 1fr))',
            gridTemplateRows: `2.5rem repeat(${timeLabels.length}, 2.5rem)`,
          }}
        >
          <div className="border-b border-r border-gray-200 bg-gray-100" />
          {WEEKDAYS.map((day) => (
            <div
              key={day.key}
              className="flex items-center justify-center border-b border-r border-gray-200 bg-gray-100 px-2 py-2 text-center text-xs font-semibold text-gray-700 last:border-r-0"
            >
              {day.label}
            </div>
          ))}

          {timeLabels.map((label, index) => (
            <div key={label} className="contents">
              <div
                className="flex items-start justify-end border-b border-r border-gray-200 bg-gray-100 px-2 pt-1 text-[10px] font-medium text-gray-500"
                style={{ gridRow: index + 2, gridColumn: 1 }}
              >
                {label}
              </div>
              {WEEKDAYS.map((day, dayIndex) => (
                <div
                  key={`${label}-${day.key}`}
                  className="border-b border-r border-gray-200 bg-white last:border-r-0"
                  style={{ gridRow: index + 2, gridColumn: dayIndex + 2 }}
                />
              ))}
            </div>
          ))}

          {blocks.map((block) => (
            <div
              key={block.key}
              className={`z-10 m-0.5 overflow-hidden rounded border-l-4 px-1.5 py-1 text-[10px] leading-tight shadow-sm ${block.colorClass}`}
              style={{
                gridColumn: block.gridColumn,
                gridRowStart: block.gridRowStart,
                gridRowEnd: block.gridRowEnd,
              }}
              title={`${block.course.courseCode} — ${block.course.title}`}
            >
              <p className="truncate font-semibold">{block.course.courseCode}</p>
              <p className="truncate opacity-80">
                {block.slot.startTimeString}–{block.slot.endTimeString}
              </p>
              {block.slot.room && (
                <p className="truncate opacity-70">{block.slot.room}</p>
              )}
            </div>
          ))}
        </div>
      </div>

      {courses.length === 0 && (
        <p className="px-4 pb-4 text-center text-sm text-gray-500">
          Henüz ders yüklenmedi. Bölüm kodu girip arama yapın.
        </p>
      )}
    </section>
  );
}
