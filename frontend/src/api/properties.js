import { api } from './client';

export const propertyApi = {
  search: (params) => api.get('/properties/search', { params }).then((r) => r.data),
  getById: (id) => api.get(`/properties/${id}`).then((r) => r.data),
  mine: () => api.get('/properties/mine').then((r) => r.data),
  create: (payload) => api.post('/properties', payload).then((r) => r.data),
  update: (id, payload) => api.put(`/properties/${id}`, payload).then((r) => r.data),
  remove: (id) => api.delete(`/properties/${id}`).then((r) => r.data),
};
