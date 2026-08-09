import { Link } from 'react-router-dom'

function Landing() {
  return (
    <div>
      <section className="relative flex min-h-screen flex-col items-center justify-center overflow-hidden bg-gradient-to-br from-rose-100 via-amber-50 to-indigo-100 px-4">
        <div className="pointer-events-none absolute -left-24 -top-24 h-72 w-72 rounded-full bg-rose-200 opacity-50 blur-3xl" />
        <div className="pointer-events-none absolute -bottom-24 -right-24 h-72 w-72 rounded-full bg-indigo-200 opacity-50 blur-3xl" />

        <div className="relative flex flex-col items-center text-center">
          <h1 className="text-4xl font-semibold tracking-tight text-slate-900 sm:text-5xl">
            MemoryMap
          </h1>
          <p className="mt-4 max-w-md text-base text-slate-600 sm:text-lg">
            Every moment counts — happy, sad, or somewhere in between.
          </p>
          <div className="mt-8 flex gap-3">
            <Link
              to="/register"
              className="rounded-lg bg-indigo-600 px-5 py-2.5 text-sm font-medium text-white shadow-sm transition-colors hover:bg-indigo-700"
            >
              Sign up
            </Link>
            <Link
              to="/login"
              className="rounded-lg border border-slate-300 bg-white px-5 py-2.5 text-sm font-medium text-slate-700 shadow-sm transition-colors hover:bg-slate-50"
            >
              Log in
            </Link>
          </div>
        </div>
      </section>

      <section className="bg-white px-4 py-20">
        <div className="mx-auto flex max-w-2xl flex-col gap-5">
          <h2 className="text-center text-2xl font-semibold tracking-tight text-slate-900">
            Why MemoryMap
          </h2>
          <p className="text-base leading-relaxed text-slate-700">
            MemoryMap is not a food-review app.
          </p>
          <p className="text-base leading-relaxed text-slate-700">
            It's built on a simple belief: life is made up of moments, and every one of
            them is worth keeping — not just the polished, exciting ones. A great meal
            you finally found time to enjoy. An afternoon of cherry picking, or a gym
            session that left you genuinely happy. A job interview that ended in
            rejection. An argument that turned into a breakup, right there at your usual
            bubble tea spot. A walk through a mall with no particular purpose. An
            anxious, embarrassing moment that turned warm because a stranger, without
            being asked, quietly helped you out. Rain outside your window at night, and
            a calm before sleep that you can't quite explain.
          </p>
          <p className="text-base leading-relaxed text-slate-700">
            None of these are "content." They're just life. MemoryMap exists so you can
            record them as they are — happy, sad, or the kind of open, hard-to-name
            feeling that doesn't fit either label — with text, a photo or video, a
            mood, and optionally, a place.
          </p>
          <p className="text-base leading-relaxed text-slate-700">
            A moment recorded at your kitchen table counts exactly as much as one
            recorded at the top of a mountain — the stressful evenings spent sending
            out job applications from home, the small joy of pulling a cake you made
            yourself out of the oven, both belong here just as much as anywhere else.
          </p>
        </div>
      </section>
    </div>
  )
}

export default Landing