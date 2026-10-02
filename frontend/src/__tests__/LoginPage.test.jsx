import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import LoginPage from '../features/auth/LoginPage';
import { useAuth } from '../auth/AuthContext';

vi.mock('../auth/AuthContext', () => ({
  useAuth: vi.fn(),
}));

describe('LoginPage', () => {
  it('calls login with entered credentials on submit', async () => {
    const login = vi.fn().mockResolvedValue({ role: 'CLIENT' });
    useAuth.mockReturnValue({ login });

    render(
      <MemoryRouter>
        <LoginPage />
      </MemoryRouter>
    );

    fireEvent.change(screen.getByLabelText(/email/i), { target: { value: 'a@b.com' } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'secret123' } });
    fireEvent.click(screen.getByRole('button', { name: /log in/i }));

    await waitFor(() => expect(login).toHaveBeenCalledWith('a@b.com', 'secret123'));
  });

  it('shows an error message when login fails', async () => {
    const login = vi.fn().mockRejectedValue({ response: { data: { error: 'Invalid credentials' } } });
    useAuth.mockReturnValue({ login });

    render(
      <MemoryRouter>
        <LoginPage />
      </MemoryRouter>
    );

    fireEvent.change(screen.getByLabelText(/email/i), { target: { value: 'a@b.com' } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'wrong' } });
    fireEvent.click(screen.getByRole('button', { name: /log in/i }));

    expect(await screen.findByText('Invalid credentials')).toBeInTheDocument();
  });
});
