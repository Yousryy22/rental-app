import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { bookingApi } from '../../api/bookings';

export default function MyBookings() {
  const queryClient = useQueryClient();

  const { data: bookings, isLoading, error } = useQuery({
    queryKey: ['bookings', 'mine'],
    queryFn: bookingApi.mine,
  });

  const cancelMutation = useMutation({
    mutationFn: (id) => bookingApi.cancel(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['bookings', 'mine'] }),
  });

  if (isLoading) return <p>Loading your bookings…</p>;
  if (error) return <p className="error">Could not load your bookings.</p>;

  return (
    <div>
      <h1>Your bookings</h1>
      {bookings.length === 0 && <p>No bookings yet.</p>}
      <ul className="property-list">
        {bookings.map((b) => (
          <li key={b.id}>
            {b.startDate} → {b.endDate} — {b.status} — ${b.totalAmount}
            {(b.status === 'PENDING' || b.status === 'CONFIRMED') && (
              <button onClick={() => cancelMutation.mutate(b.id)} disabled={cancelMutation.isPending}>
                Cancel
              </button>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}
