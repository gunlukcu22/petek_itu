import axios from 'axios';
import type { CourseDTO } from '../types/course';

export const api = axios.create({
  baseURL: 'http://localhost:8080/api',
});

export async function getCourses(majorCode?: string): Promise<CourseDTO[]> {
  const params = majorCode ? { majorCode } : undefined;
  const { data } = await api.get<CourseDTO[]>('/courses', { params });
  return data;
}
