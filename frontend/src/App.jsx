import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from './auth/AuthContext';
import { ProtectedRoute } from './auth/ProtectedRoute';
import Navbar from './components/Navbar';

import LoginPage from './features/auth/LoginPage';
import RegisterPage from './features/auth/RegisterPage';

import OwnerDashboard from './features/owner/OwnerDashboard';
import PropertyForm from './features/owner/PropertyForm';
import BookingRequests from './features/owner/BookingRequests';

import PropertySearch from './features/client/PropertySearch';
import PropertyDetail from './features/client/PropertyDetail';
import BookingForm from './features/client/BookingForm';
import MyBookings from './features/client/MyBookings';

import Checkout from './features/payments/Checkout';

const queryClient = new QueryClient();

function Unauthorized() {
  return <p>You don't have access to this page.</p>;
}

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <BrowserRouter>
          <Navbar />
          <main className="container">
            <Routes>
              <Route path="/" element={<Navigate to="/login" replace />} />
              <Route path="/login" element={<LoginPage />} />
              <Route path="/register" element={<RegisterPage />} />
              <Route path="/unauthorized" element={<Unauthorized />} />

              {/* Owner routes */}
              <Route path="/owner" element={
                <ProtectedRoute role="OWNER"><OwnerDashboard /></ProtectedRoute>
              } />
              <Route path="/owner/properties/new" element={
                <ProtectedRoute role="OWNER"><PropertyForm /></ProtectedRoute>
              } />
              <Route path="/owner/properties/:propertyId/bookings" element={
                <ProtectedRoute role="OWNER"><BookingRequests /></ProtectedRoute>
              } />

              {/* Client routes */}
              <Route path="/client" element={
                <ProtectedRoute role="CLIENT"><PropertySearch /></ProtectedRoute>
              } />
              <Route path="/client/properties/:propertyId" element={
                <ProtectedRoute role="CLIENT"><PropertyDetail /></ProtectedRoute>
              } />
              <Route path="/client/properties/:propertyId/book" element={
                <ProtectedRoute role="CLIENT"><BookingForm /></ProtectedRoute>
              } />
              <Route path="/client/bookings" element={
                <ProtectedRoute role="CLIENT"><MyBookings /></ProtectedRoute>
              } />
              <Route path="/client/bookings/:bookingId/checkout" element={
                <ProtectedRoute role="CLIENT"><Checkout /></ProtectedRoute>
              } />
            </Routes>
          </main>
        </BrowserRouter>
      </AuthProvider>
    </QueryClientProvider>
  );
}
