import { useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { bookingApi } from '../../api/bookings';

export default function BookingRequests() {
  const { propertyId } = useParams();

  const { data: bookings, isLoading, error } = useQuery({
    queryKey: ['bookings', 'property', propertyId],
    queryFn: () => bookingApi.byProperty(propertyId),
  });

  if (isLoading) return <p>Loading bookings…</p>;
  if (error) return <p className="error">Could not load bookings.</p>;

  return (
    <div>
      <h1>Bookings for this property</h1>
      {bookings.length === 0 && <p>No bookings yet.</p>}
      <table>
        <thead>
          <tr>
            <th>Client</th>
            <th>Dates</th>
            <th>Status</th>
            <th>Total</th>
          </tr>
        </thead>
        <tbody>
          {bookings.map((b) => (
            <tr key={b.id}>
              <td>{b.clientId}</td>
              <td>
                {b.startDate} → {b.endDate}
              </td>
              <td>{b.status}</td>
              <td>${b.totalAmount}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
