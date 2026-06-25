import { useState } from 'react';
import CalendarGrid from './components/CalendarGrid';
import CourseList from './components/CourseList';
import DepartmentSelector from './components/DepartmentSelector';
import { DEPARTMENTS, type DepartmentOption } from './constants/departments';
import { getCourses } from './services/api';
import type { CourseDTO } from './types/course';

function App() {
  const [selectedDepartment, setSelectedDepartment] = useState<DepartmentOption | null>(null);
  const [courses, setCourses] = useState<CourseDTO[]>([]);
  const [selectedCourses, setSelectedCourses] = useState<CourseDTO[]>([]);
  const [loading, setLoading] = useState(false);

  const handleDepartmentSelect = async (department: DepartmentOption) => {
    setSelectedDepartment(department);
    setSelectedCourses([]);
    setLoading(true);

    try {
      const data = await getCourses(department.code);
      setCourses(data);
      console.log('Fetched courses for', department.code, data);
    } catch (error) {
      console.error('Failed to fetch courses:', error);
      setCourses([]);
      alert('Dersler alınırken bir hata oluştu.');
    } finally {
      setLoading(false);
    }
  };

  const hasConflict = (candidate: CourseDTO, existing: CourseDTO[]): boolean => {
    return candidate.timeSlots.some((candidateSlot) =>
      existing.some((existingCourse) =>
        existingCourse.timeSlots.some((existingSlot) => {
          if (candidateSlot.dayOfWeek !== existingSlot.dayOfWeek) {
            return false;
          }

          return (
            candidateSlot.startMinute < existingSlot.endMinute &&
            existingSlot.startMinute < candidateSlot.endMinute
          );
        }),
      ),
    );
  };

  const handleAddCourse = (course: CourseDTO) => {
    if (selectedCourses.some((item) => item.crn === course.crn)) {
      return;
    }

    if (course.availableQuota === 0) {
      const confirmed = window.confirm(
        'Bu dersin boş kontenjanı yok. Eklemek istediğinizden emin misiniz?',
      );
      if (!confirmed) {
        return;
      }
    }

    if (hasConflict(course, selectedCourses)) {
      alert('Bu ders seçili derslerle zaman çakışması yaşıyor.');
      return;
    }

    setSelectedCourses((prev) => [...prev, course]);
  };

  const handleRemoveCourse = (crn: number) => {
    setSelectedCourses((prev) => prev.filter((course) => course.crn !== crn));
  };

  return (
    <div className="flex h-screen flex-col overflow-hidden bg-gray-50">
      <nav className="shrink-0 border-b border-gray-200 bg-white shadow-sm">
        <div className="mx-auto flex w-full max-w-[1400px] items-center px-4 py-4 sm:px-6 lg:px-8">
          <h1 className="text-xl font-bold text-gray-900">Petek - İTÜ Ders Programı</h1>
        </div>
      </nav>

      <main className="mx-auto flex min-h-0 w-full max-w-[1400px] flex-1 flex-col gap-6 overflow-hidden p-4 sm:p-6 lg:flex-row lg:p-8">
        <section className="flex w-full shrink-0 flex-col gap-4 lg:w-80 lg:max-w-80">
          <DepartmentSelector
            departments={DEPARTMENTS}
            selectedDepartmentCode={selectedDepartment?.code ?? null}
            onSelect={handleDepartmentSelect}
          />

          <div className="min-h-0 flex-1 overflow-hidden">
            {loading ? (
              <div className="rounded-lg border border-gray-200 bg-white p-4 text-sm text-gray-500 shadow-sm">
                Dersler yükleniyor...
              </div>
            ) : (
              <CourseList
                courses={courses}
                selectedCourseCrns={new Set(selectedCourses.map((course) => course.crn))}
                onAddCourse={handleAddCourse}
                onRemoveCourse={handleRemoveCourse}
              />
            )}
          </div>
        </section>

        <section className="flex min-h-0 min-w-0 flex-1 overflow-hidden">
          <CalendarGrid courses={selectedCourses} />
        </section>
      </main>
    </div>
  );
}

export default App;
