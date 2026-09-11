import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { loadStripe } from '@stripe/stripe-js';
import { Elements, PaymentElement, useElements, useStripe } from '@stripe/react-stripe-js';
import { paymentApi } from '../../api/bookings';

const stripePromise = loadStripe(import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY);

function CheckoutForm({ bookingId }) {
  const stripe = useStripe();
  const elements = useElements();
  const navigate = useNavigate();
  const [error, setError] = useState(null);
  const [processing, setProcessing] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!stripe || !elements) return;

    setProcessing(true);
    setError(null);

    // Stripe.js confirms the payment directly with Stripe's servers using the card
    // details entered into PaymentElement — that data never passes through our API.
    const { error: confirmError } = await stripe.confirmPayment({
      elements,
      confirmParams: {
        return_url: `${window.location.origin}/client/bookings/${bookingId}/confirmation`,
      },
    });

    if (confirmError) {
      setError(confirmError.message);
      setProcessing(false);
    }
    // On success, Stripe redirects to return_url — the booking is flipped to CONFIRMED
    // server-side only once our webhook receives Stripe's payment_intent.succeeded event.
  };

  return (
    <form onSubmit={handleSubmit}>
      <PaymentElement />
      {error && <p className="error">{error}</p>}
      <button type="submit" disabled={!stripe || processing}>
        {processing ? 'Processing…' : 'Pay now'}
      </button>
    </form>
  );
}

export default function Checkout() {
  const { bookingId } = useParams();
  const [clientSecret, setClientSecret] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    paymentApi
      .initiate(bookingId)
      .then((res) => setClientSecret(res.clientSecret))
      .catch((err) => setError(err.response?.data?.error || 'Could not start payment'));
  }, [bookingId]);

  if (error) return <p className="error">{error}</p>;
  if (!clientSecret) return <p>Preparing checkout…</p>;

  return (
    <div>
      <h1>Complete your payment</h1>
      <Elements stripe={stripePromise} options={{ clientSecret }}>
        <CheckoutForm bookingId={bookingId} />
      </Elements>
    </div>
  );
}
