import { api } from './client';

export const bookingApi = {
  create: (payload) => api.post('/bookings', payload).then((r) => r.data),
  cancel: (id) => api.post(`/bookings/${id}/cancel`).then((r) => r.data),
  mine: () => api.get('/bookings/mine').then((r) => r.data),
  byProperty: (propertyId) => api.get(`/bookings/property/${propertyId}`).then((r) => r.data),
};

export const paymentApi = {
  initiate: (bookingId) =>
    api.post(`/payments/bookings/${bookingId}/initiate`).then((r) => r.data),
};
