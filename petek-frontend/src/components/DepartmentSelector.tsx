import { useMemo, useState } from 'react';
import type { DepartmentOption } from '../constants/departments';

interface DepartmentSelectorProps {
  departments: DepartmentOption[];
  selectedDepartmentCode: string | null;
  onSelect: (department: DepartmentOption) => void;
}

export default function DepartmentSelector({
  departments,
  selectedDepartmentCode,
  onSelect,
}: DepartmentSelectorProps) {
  const [query, setQuery] = useState('');

  const selectedDepartment = useMemo(
    () => departments.find((item) => item.code === selectedDepartmentCode) ?? null,
    [departments, selectedDepartmentCode],
  );

  const normalizedQuery = query.trim().toLowerCase();
  const shouldShowDropdown = normalizedQuery.length >= 2;

  const filtered = useMemo(() => {
    if (!shouldShowDropdown) {
      return [];
    }

    return departments
      .filter(
        (item) =>
          item.code.toLowerCase().includes(normalizedQuery) ||
          item.name.toLowerCase().includes(normalizedQuery),
      )
      .slice(0, 25);
  }, [departments, normalizedQuery, shouldShowDropdown]);

  return (
    <div className="rounded-lg border border-gray-200 bg-white p-4 shadow-sm">
      <h2 className="mb-3 text-lg font-semibold text-gray-900">Bölüm Seçimi</h2>

      {selectedDepartment && (
        <div className="mb-3 rounded-md border border-blue-100 bg-blue-50 px-3 py-2 text-sm text-blue-900">
          <span className="font-semibold">Seçili bölüm:</span> {selectedDepartment.code} -{' '}
          {selectedDepartment.name}
        </div>
      )}

      <input
        type="text"
        value={query}
        onChange={(event) => setQuery(event.target.value)}
        placeholder="Kod veya bölüm adı ile ara..."
        className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
      />

      {!shouldShowDropdown && (
        <p className="mt-2 text-xs text-gray-500">Listeyi görmek için en az 2 karakter yazın.</p>
      )}

      {shouldShowDropdown && (
        <div className="mt-3 max-h-64 overflow-y-auto rounded-md border border-gray-200">
          {filtered.length > 0 ? (
            filtered.map((department) => (
              <button
                key={department.code}
                type="button"
                onClick={() => {
                  onSelect(department);
                  setQuery('');
                }}
                className="block w-full border-b border-gray-100 px-3 py-2 text-left text-sm hover:bg-gray-50 last:border-b-0"
              >
                <div className="font-medium text-gray-900">{department.code}</div>
                <div className="text-xs text-gray-600">{department.name}</div>
              </button>
            ))
          ) : (
            <p className="px-3 py-4 text-sm text-gray-500">Eşleşen bölüm bulunamadı.</p>
          )}
        </div>
      )}
    </div>
  );
}
