import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../auth/AuthContext';

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    email: '',
    password: '',
    fullName: '',
    phone: '',
    role: 'CLIENT',
  });
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const handleChange = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const data = await register(form);
      navigate(data.role === 'OWNER' ? '/owner' : '/client');
    } catch (err) {
      setError(err.response?.data?.error || 'Registration failed');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-page">
      <h1>Create an account</h1>
      <form onSubmit={handleSubmit}>
        <label>
          I am a…
          <select value={form.role} onChange={handleChange('role')}>
            <option value="CLIENT">Client (renting)</option>
            <option value="OWNER">Owner (listing)</option>
          </select>
        </label>
        <label>
          Full name
          <input value={form.fullName} onChange={handleChange('fullName')} required />
        </label>
        <label>
          Email
          <input type="email" value={form.email} onChange={handleChange('email')} required />
        </label>
        <label>
          Phone
          <input value={form.phone} onChange={handleChange('phone')} />
        </label>
        <label>
          Password
          <input
            type="password"
            minLength={8}
            value={form.password}
            onChange={handleChange('password')}
            required
          />
        </label>
        {error && <p className="error">{error}</p>}
        <button type="submit" disabled={submitting}>
          {submitting ? 'Creating account…' : 'Register'}
        </button>
      </form>
      <p>
        Already have an account? <Link to="/login">Log in</Link>
      </p>
    </div>
  );
}
