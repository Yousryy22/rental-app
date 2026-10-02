import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { propertyApi } from '../../api/properties';

export default function PropertySearch() {
  const [filters, setFilters] = useState({ city: '', minPrice: '', maxPrice: '' });
  const [appliedFilters, setAppliedFilters] = useState({});

  const { data, isLoading, error } = useQuery({
    queryKey: ['properties', 'search', appliedFilters],
    queryFn: () => propertyApi.search(appliedFilters),
  });

  const handleSearch = (e) => {
    e.preventDefault();
    const cleaned = Object.fromEntries(
      Object.entries(filters).filter(([, v]) => v !== '')
    );
    setAppliedFilters(cleaned);
  };

  return (
    <div>
      <h1>Find a place to rent</h1>
      <form onSubmit={handleSearch} className="search-form">
        <input
          placeholder="City"
          value={filters.city}
          onChange={(e) => setFilters({ ...filters, city: e.target.value })}
        />
        <input
          type="number"
          placeholder="Min price"
          value={filters.minPrice}
          onChange={(e) => setFilters({ ...filters, minPrice: e.target.value })}
        />
        <input
          type="number"
          placeholder="Max price"
          value={filters.maxPrice}
          onChange={(e) => setFilters({ ...filters, maxPrice: e.target.value })}
        />
        <button type="submit">Search</button>
      </form>

      {isLoading && <p>Loading properties…</p>}
      {error && <p className="error">Could not load properties.</p>}

      <ul className="property-list">
        {data?.content?.map((p) => (
          <li key={p.id}>
            <Link to={`/client/properties/${p.id}`}>{p.title}</Link>
            <span> — {p.city} — ${p.pricePerMonth}/mo</span>
          </li>
        ))}
      </ul>
    </div>
  );
}
