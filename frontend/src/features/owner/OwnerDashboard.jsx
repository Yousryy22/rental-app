import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { propertyApi } from '../../api/properties';

export default function OwnerDashboard() {
  const { data: properties, isLoading, error } = useQuery({
    queryKey: ['properties', 'mine'],
    queryFn: propertyApi.mine,
  });

  if (isLoading) return <p>Loading your properties…</p>;
  if (error) return <p className="error">Could not load your properties.</p>;

  return (
    <div>
      <div className="page-header">
        <h1>Your properties</h1>
        <Link to="/owner/properties/new">+ Add property</Link>
      </div>

      {properties.length === 0 && <p>You haven't listed any properties yet.</p>}

      <ul className="property-list">
        {properties.map((p) => (
          <li key={p.id}>
            <Link to={`/owner/properties/${p.id}/bookings`}>{p.title}</Link>
            <span> — {p.city} — ${p.pricePerMonth}/mo — {p.status}</span>
          </li>
        ))}
      </ul>
    </div>
  );
}
