import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { BASE_URL } from '../api.js'

function Login() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const navigate = useNavigate()

  function handleEmailOnChange(e) {
    setEmail(e.target.value)
  }
  function handlePasswordOnChange(e){
    setPassword(e.target.value)
  }
  async function handleLogin(e) {
    e.preventDefault()
    setError('')

    const response = await fetch(`${BASE_URL}/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
    })

    if (!response.ok) {
      setError('Login failed. Check your email and password.')
      return
    }

    const token = await response.text()
    localStorage.setItem('token', token)
    navigate('/moments')
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-50 px-4">
      <form
        onSubmit={handleLogin}
        className="w-full max-w-sm rounded-2xl border border-slate-200 bg-white p-8 shadow-sm"
      >
        <h1 className="text-center text-xl font-semibold tracking-tight text-slate-900">
          MemoryMap
        </h1>
        <p className="mt-1 text-center text-sm text-slate-500">
          Sign in to your moments
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
          className="mt-6 w-full rounded-lg bg-indigo-600 px-3 py-2.5 text-sm font-medium text-white shadow-sm transition-colors hover:bg-indigo-700"
        >
          Sign in
        </button>
        {error && <p className="mt-3 text-center text-sm text-red-600">{error}</p>}
        <p className="mt-6 text-center text-sm text-slate-500">
          Don't have an account?{' '}
          <Link to="/register" className="font-medium text-indigo-600 hover:underline">
            Sign up
          </Link>
        </p>
      </form>
    </div>
  )
}

export default Login
