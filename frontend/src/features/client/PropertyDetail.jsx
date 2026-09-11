import { useParams, Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { propertyApi } from '../../api/properties';

export default function PropertyDetail() {
  const { propertyId } = useParams();

  const { data: property, isLoading, error } = useQuery({
    queryKey: ['properties', propertyId],
    queryFn: () => propertyApi.getById(propertyId),
  });

  if (isLoading) return <p>Loading property…</p>;
  if (error) return <p className="error">Property not found.</p>;

  return (
    <div>
      <h1>{property.title}</h1>
      <p>{property.address}, {property.city}</p>
      <p>${property.pricePerMonth}/month · {property.bedrooms} bed · {property.bathrooms} bath</p>
      <p>{property.description}</p>
      <p>Status: {property.status}</p>

      {property.status === 'AVAILABLE' && (
        <Link to={`/client/properties/${property.id}/book`} className="button">
          Book this property
        </Link>
      )}
    </div>
  );
}
