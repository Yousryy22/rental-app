import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { bookingApi } from '../../api/bookings';

export default function BookingForm() {
  const { propertyId } = useParams();
  const navigate = useNavigate();
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [error, setError] = useState(null);

  const mutation = useMutation({
    mutationFn: () => bookingApi.create({ propertyId, startDate, endDate }),
    onSuccess: (booking) => navigate(`/client/bookings/${booking.id}/checkout`),
    onError: (err) => setError(err.response?.data?.error || 'Could not create booking'),
  });

  const handleSubmit = (e) => {
    e.preventDefault();
    setError(null);
    mutation.mutate();
  };

  return (
    <div>
      <h1>Choose your dates</h1>
      <form onSubmit={handleSubmit}>
        <label>
          Start date
          <input type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} required />
        </label>
        <label>
          End date
          <input type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} required />
        </label>
        {error && <p className="error">{error}</p>}
        <button type="submit" disabled={mutation.isPending}>
          {mutation.isPending ? 'Requesting…' : 'Continue to payment'}
        </button>
      </form>
    </div>
  );
}
