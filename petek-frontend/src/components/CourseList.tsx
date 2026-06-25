import type { CourseDTO } from '../types/course';

interface CourseListProps {
  courses: CourseDTO[];
  selectedCourseCrns: Set<number>;
  onAddCourse: (course: CourseDTO) => void;
  onRemoveCourse: (crn: number) => void;
}

export default function CourseList({
  courses,
  selectedCourseCrns,
  onAddCourse,
  onRemoveCourse,
}: CourseListProps) {
  return (
    <div className="flex min-h-0 flex-col rounded-lg border border-gray-200 bg-white shadow-sm">
      <div className="border-b border-gray-200 px-4 py-3">
        <h2 className="text-lg font-semibold text-gray-900">Course List</h2>
        <p className="text-sm text-gray-500">{courses.length} ders bulundu</p>
      </div>

      <div className="min-h-0 flex-1 overflow-y-auto">
        {courses.map((course) => {
          const isAdded = selectedCourseCrns.has(course.crn);
          return (
            <div key={course.crn} className="border-b border-gray-100 p-3 last:border-b-0">
              <div className="flex items-start justify-between gap-2">
                <div className="min-w-0 flex-1">
                  <p className="text-sm font-semibold text-gray-900">
                    {course.courseCode} ({course.crn})
                  </p>
                  <p className="text-sm text-gray-700">{course.title}</p>
                  <p className="mt-1 text-xs text-gray-500">
                    Kontenjan: {course.capacity ?? '-'} · Yazılan: {course.enrolled ?? '-'} · Boş:{' '}
                    {course.availableQuota ?? '-'}
                  </p>
                </div>
                {isAdded ? (
                  <button
                    type="button"
                    onClick={() => onRemoveCourse(course.crn)}
                    className="rounded-md border border-red-300 px-2 py-1 text-sm font-semibold text-red-700 hover:bg-red-50"
                  >
                    -
                  </button>
                ) : (
                  <button
                    type="button"
                    onClick={() => onAddCourse(course)}
                    className="rounded-md border border-blue-300 px-2 py-1 text-sm font-semibold text-blue-700 hover:bg-blue-50"
                  >
                    +
                  </button>
                )}
              </div>
            </div>
          );
        })}
        {courses.length === 0 && (
          <p className="p-4 text-sm text-gray-500">Bölüm seçtikten sonra dersler burada listelenecek.</p>
        )}
      </div>
    </div>
  );
}
