import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { propertyApi } from '../../api/properties';

const EMPTY_FORM = {
  title: '',
  description: '',
  address: '',
  city: '',
  pricePerMonth: '',
  bedrooms: '',
  bathrooms: '',
};

export default function PropertyForm() {
  const [form, setForm] = useState(EMPTY_FORM);
  const [error, setError] = useState(null);
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const mutation = useMutation({
    mutationFn: (payload) => propertyApi.create(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['properties', 'mine'] });
      navigate('/owner');
    },
    onError: (err) => setError(err.response?.data?.error || 'Could not save property'),
  });

  const handleChange = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  const handleSubmit = (e) => {
    e.preventDefault();
    setError(null);
    mutation.mutate({
      ...form,
      pricePerMonth: Number(form.pricePerMonth),
      bedrooms: form.bedrooms ? Number(form.bedrooms) : null,
      bathrooms: form.bathrooms ? Number(form.bathrooms) : null,
    });
  };

  return (
    <div>
      <h1>List a new property</h1>
      <form onSubmit={handleSubmit}>
        <label>
          Title
          <input value={form.title} onChange={handleChange('title')} required />
        </label>
        <label>
          Description
          <textarea value={form.description} onChange={handleChange('description')} />
        </label>
        <label>
          Address
          <input value={form.address} onChange={handleChange('address')} required />
        </label>
        <label>
          City
          <input value={form.city} onChange={handleChange('city')} />
        </label>
        <label>
          Price per month ($)
          <input
            type="number"
            min="0"
            step="0.01"
            value={form.pricePerMonth}
            onChange={handleChange('pricePerMonth')}
            required
          />
        </label>
        <label>
          Bedrooms
          <input type="number" min="0" value={form.bedrooms} onChange={handleChange('bedrooms')} />
        </label>
        <label>
          Bathrooms
          <input type="number" min="0" value={form.bathrooms} onChange={handleChange('bathrooms')} />
        </label>
        {error && <p className="error">{error}</p>}
        <button type="submit" disabled={mutation.isPending}>
          {mutation.isPending ? 'Saving…' : 'Save property'}
        </button>
      </form>
    </div>
  );
}
