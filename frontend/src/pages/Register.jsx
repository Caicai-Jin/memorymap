import { useState } from 'react'
import { Link } from 'react-router-dom'
import { BASE_URL } from '../api.js'

function Register() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [submitted, setSubmitted] = useState(false)

  function handleEmailOnChange(e) {
    setEmail(e.target.value)
  }
  function handlePasswordOnChange(e){
    setPassword(e.target.value)
  }
  async function handleRegister(e) {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      const response = await fetch(`${BASE_URL}/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      })

      if (!response.ok) {
        const body = await response.json().catch(() => null)
        setError(body?.message || 'Registration failed. Email may already be in use.')
        return
      }

      setSubmitted(true)
    } catch {
      setError('Could not reach the server. The backend may be waking up from sleep (can take up to a minute) - please try again.')
    } finally {
      setLoading(false)
    }
  }

  if (submitted) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-slate-50 px-4">
        <div className="w-full max-w-sm rounded-2xl border border-slate-200 bg-white p-8 text-center shadow-sm">
          <h1 className="text-xl font-semibold tracking-tight text-slate-900">
            Check your email
          </h1>
          <p className="mt-3 text-sm text-slate-600">
            We sent a verification link to <span className="font-medium">{email}</span>. Click
            it to activate your account, then sign in below.
          </p>
          <Link
            to="/login"
            className="mt-6 inline-block font-medium text-indigo-600 hover:underline"
          >
            Go to sign in
          </Link>
        </div>
      </div>
    )
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-50 px-4">
      <form
        onSubmit={handleRegister}
        className="w-full max-w-sm rounded-2xl border border-slate-200 bg-white p-8 shadow-sm"
      >
        <h1 className="text-center text-xl font-semibold tracking-tight text-slate-900">
          MemoryMap
        </h1>
        <p className="mt-1 text-center text-sm text-slate-500">
          Create an account to start journaling
        </p>
        <div className="mt-6 flex flex-col gap-4">
          <label htmlFor="email" className="flex flex-col gap-1.5">
            <span className="text-sm font-medium text-slate-700">Email</span>
            <input
              id="email"
              type="email"
              value={email}
              onChange={handleEmailOnChange}
              className="rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/30"
            />
          </label>
          <label htmlFor="password" className="flex flex-col gap-1.5">
            <span className="text-sm font-medium text-slate-700">Password</span>
            <input
              id="password"
              type="password"
              value={password}
              onChange={handlePasswordOnChange}
              className="rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/30"
            />
          </label>
        </div>
        <button
          type="submit"
          disabled={loading}
          className="mt-6 w-full rounded-lg bg-indigo-600 px-3 py-2.5 text-sm font-medium text-white shadow-sm transition-colors hover:bg-indigo-700 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {loading ? 'Signing up...' : 'Sign up'}
        </button>
        {error && <p className="mt-3 text-center text-sm text-red-600">{error}</p>}
        <p className="mt-4 text-center text-sm">
          <Link to="/forgot-password" className="font-medium text-indigo-600 hover:underline">
            Forgot password?
          </Link>
        </p>
        <p className="mt-2 text-center text-sm text-slate-500">
          Already have an account?{' '}
          <Link to="/login" className="font-medium text-indigo-600 hover:underline">
            Sign in
          </Link>
        </p>
      </form>
    </div>
  )
}

export default Register
