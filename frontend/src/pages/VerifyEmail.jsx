import { useEffect, useRef, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { BASE_URL } from '../api.js'

function VerifyEmail() {
  const [searchParams] = useSearchParams()
  const [status, setStatus] = useState('loading')
  // The verification token is single-use, so a second call for the same
  // token always fails — this ref stops React StrictMode's dev-mode double
  // effect invocation from firing the request twice and showing a false error.
  const requestedTokenRef = useRef(null)

  useEffect(() => {
    const token = searchParams.get('token')
    if (!token) {
      setStatus('error')
      return
    }
    if (requestedTokenRef.current === token) {
      return
    }
    requestedTokenRef.current = token

    fetch(`${BASE_URL}/verify-email?token=${encodeURIComponent(token)}`)
      .then((response) => setStatus(response.ok ? 'success' : 'error'))
      .catch(() => setStatus('error'))
  }, [searchParams])

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-50 px-4">
      <div className="w-full max-w-sm rounded-2xl border border-slate-200 bg-white p-8 text-center shadow-sm">
        <h1 className="text-xl font-semibold tracking-tight text-slate-900">MemoryMap</h1>

        {status === 'loading' && (
          <p className="mt-3 text-sm text-slate-600">Verifying your email...</p>
        )}
        {status === 'success' && (
          <>
            <p className="mt-3 text-sm text-slate-600">
              Your email is verified. You can now sign in.
            </p>
            <Link
              to="/login"
              className="mt-6 inline-block font-medium text-indigo-600 hover:underline"
            >
              Go to sign in
            </Link>
          </>
        )}
        {status === 'error' && (
          <>
            <p className="mt-3 text-sm text-red-600">
              This verification link is invalid or has expired.
            </p>
            <Link
              to="/register"
              className="mt-6 inline-block font-medium text-indigo-600 hover:underline"
            >
              Back to sign up
            </Link>
          </>
        )}
      </div>
    </div>
  )
}

export default VerifyEmail
