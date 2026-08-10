import { useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { BASE_URL } from '../api.js'

function ResetPassword() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token')

  const [newPassword, setNewPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [submitted, setSubmitted] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      const response = await fetch(`${BASE_URL}/reset-password`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ token, newPassword }),
      })

      if (!response.ok) {
        const body = await response.json().catch(() => null)
        setError(body?.message || 'This reset link is invalid or has expired.')
        return
      }

      setSubmitted(true)
    } catch {
      setError('Could not reach the server. The backend may be waking up from sleep (can take up to a minute) - please try again.')
    } finally {
      setLoading(false)
    }
  }

  if (!token) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-slate-50 px-4">
        <div className="w-full max-w-sm rounded-2xl border border-slate-200 bg-white p-8 text-center shadow-sm">
          <h1 className="text-xl font-semibold tracking-tight text-slate-900">MemoryMap</h1>
          <p className="mt-3 text-sm text-red-600">
            This reset link is missing its token. Request a new one below.
          </p>
          <Link
            to="/forgot-password"
            className="mt-6 inline-block font-medium text-indigo-600 hover:underline"
          >
            Request a new link
          </Link>
        </div>
      </div>
    )
  }

  if (submitted) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-slate-50 px-4">
        <div className="w-full max-w-sm rounded-2xl border border-slate-200 bg-white p-8 text-center shadow-sm">
          <h1 className="text-xl font-semibold tracking-tight text-slate-900">Password reset</h1>
          <p className="mt-3 text-sm text-slate-600">
            Your password has been updated. You can now sign in with it.
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
        onSubmit={handleSubmit}
        className="w-full max-w-sm rounded-2xl border border-slate-200 bg-white p-8 shadow-sm"
      >
        <h1 className="text-center text-xl font-semibold tracking-tight text-slate-900">
          MemoryMap
        </h1>
        <p className="mt-1 text-center text-sm text-slate-500">Choose a new password</p>
        <div className="mt-6 flex flex-col gap-4">
          <label htmlFor="newPassword" className="flex flex-col gap-1.5">
            <span className="text-sm font-medium text-slate-700">New password</span>
            <input
              id="newPassword"
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              className="rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/30"
            />
          </label>
        </div>
        <button
          type="submit"
          disabled={loading}
          className="mt-6 w-full rounded-lg bg-indigo-600 px-3 py-2.5 text-sm font-medium text-white shadow-sm transition-colors hover:bg-indigo-700 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {loading ? 'Resetting...' : 'Reset password'}
        </button>
        {error && <p className="mt-3 text-center text-sm text-red-600">{error}</p>}
        <p className="mt-6 text-center text-sm text-slate-500">
          <Link to="/login" className="font-medium text-indigo-600 hover:underline">
            Back to sign in
          </Link>
        </p>
      </form>
    </div>
  )
}

export default ResetPassword
